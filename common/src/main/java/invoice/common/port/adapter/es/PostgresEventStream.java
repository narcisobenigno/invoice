package invoice.common.port.adapter.es;

import invoice.common.clock.Clock;
import invoice.common.es.Event;
import invoice.common.es.EventStream;
import invoice.common.es.EventsRegistry;
import invoice.common.es.Version;
import invoice.common.jdbi.DefaultColumn;
import invoice.common.jdbi.IntegerColumn;
import invoice.common.jdbi.JSONColumn;
import invoice.common.jdbi.NotNullColumn;
import invoice.common.jdbi.PrimaryKeyColumn;
import invoice.common.jdbi.SerialColumn;
import invoice.common.jdbi.Table;
import invoice.common.jdbi.TextColumn;
import invoice.common.jdbi.TimestamptzColumn;
import invoice.common.jdbi.UUIDColumn;
import invoice.common.jdbi.UniqueConstraint;
import invoice.common.serialization.JSON;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.postgresql.util.PGobject;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PostgresEventStream implements EventStream {
    private final EventsTable eventsTable;

    public PostgresEventStream(Credentials credentials, Clock clock, EventsRegistry registry) {
        this.eventsTable = new EventsTable(
                Jdbi.create(
                                String.format(
                                        "jdbc:postgresql://%s/%s",
                                        credentials.host,
                                        credentials.dbname
                                ),
                                credentials.username,
                                credentials.password
                        )
                        .installPlugin(new PostgresPlugin()),
                registry,
                clock
        );
    }

    public void createTable() {
        this.eventsTable.create();
    }

    public void dropTable() {
        this.eventsTable.drop();
    }

    public void publish(List<Event.Unpublished> events) throws Exception {
        this.eventsTable.insert().batch(events);
    }

    public List<Event.PublishedEvent> all() {
        return this.eventsTable.select().events();
    }

    @Override
    public List<Event.PublishedEvent> events(UUID id) {
        return this.eventsTable.select().whereAggregateID(id).events();
    }

    public static class Credentials {
        private final String host;
        private final String dbname;
        private final String username;
        private final String password;

        public Credentials(JSON.Object credential) {
            this(
                    credential.stringValue("host"),
                    credential.stringValue("dbname"),
                    credential.stringValue("username"),
                    credential.stringValue("password")
            );
        }

        public Credentials(String host, String dbname, String username, String password) {
            this.host = host;
            this.dbname = dbname;
            this.username = username;
            this.password = password;
        }
    }

    private static class EventsTable {
        private final static String TABLE_NAME = "events";

        private final Jdbi jdbi;
        private final EventsRegistry registry;
        private final Clock clock;
        private final Table table;

        public EventsTable(Jdbi jdbi, EventsRegistry registry, Clock clock) {
            this.jdbi = jdbi;
            this.registry = registry;
            this.clock = clock;
            this.table = new Table(jdbi, "events")
                    .with(new PrimaryKeyColumn(new SerialColumn("position")))
                    .with(new NotNullColumn(new UUIDColumn("aggregate_id")))
                    .with(new NotNullColumn(new IntegerColumn("version")))
                    .with(new NotNullColumn(new TextColumn("type")))
                    .with(new JSONColumn("payload"))
                    .with(new NotNullColumn(new DefaultColumn(new TimestamptzColumn("recorded_at"), "now()")))
                    .with(new UniqueConstraint("events_optimistic_lock", "aggregate_id", "version"));
        }

        public Select select() {
            return new Select(this.jdbi.open(), this.registry);
        }

        public void batchInsert(List<Event.Unpublished> events) throws Exception {
            try (var handle = this.jdbi.open()) {
                var batch = handle.prepareBatch(
                        "INSERT INTO events(aggregate_id, version, payload, type, recorded_at) " +
                                "VALUES (:aggregate_id, :version, :payload, :type, :recorded_at)"
                );
                events.forEach(event -> {
                    var payload = new PGobject();
                    payload.setType("json");
                    try {
                        payload.setValue(event.payload().json().marshelled());
                    } catch (SQLException e) {
                        throw new IllegalStateException(e);
                    }
                    batch
                            .bind("aggregate_id", event.aggregateID())
                            .bind("version", event.version().value())
                            .bind("type", this.registry.name(event.payload().getClass()))
                            .bind("payload", payload)
                            .bind("recorded_at", this.clock.now())
                            .add();
                });

                try {
                    batch.execute();
                } catch (UnableToExecuteStatementException e) {
                    throw new EventStream.Exception(e);
                }
            }
        }

        public Insert insert() {
            return new Insert(this.jdbi.open(), this.registry, this.clock);
        }

        public void create() {
            this.table.create();
        }

        public void drop() {
            this.jdbi.useHandle(handle -> handle.execute("DROP TABLE IF EXISTS events;"));
        }

        private static class Select {
            private final Handle handle;
            private final EventsRegistry registry;
            private final Map<String, Object> parameters;

            public Select(Handle handle, EventsRegistry registry) {
                this(handle, registry, Map.of());
            }

            private Select(Handle handle, EventsRegistry registry, Map<String, Object> parameters) {
                this.handle = handle;
                this.registry = registry;
                this.parameters = parameters;
            }

            public Select whereAggregateID(UUID id) {
                final var parameters = Stream.concat(
                        this.parameters.entrySet().stream(),
                        Map.of("aggregate_id", id).entrySet().stream()
                );

                return new Select(
                        this.handle,
                        this.registry,
                        parameters.collect(
                                Collectors.toUnmodifiableMap(
                                        Map.Entry::getKey,
                                        Map.Entry::getValue
                                )
                        )
                );
            }

            public List<Event.PublishedEvent> events() {
                final var parameters = this.parameters.keySet().stream()
                        .map(name -> String.format("\"%1$s\" = :%1$s", name))
                        .collect(Collectors.joining(" AND "));

                var where = "";
                if (!parameters.isBlank()) {
                    where = String.join(" ", "WHERE", parameters);
                }
                var query = this.handle.createQuery(
                        String.format(
                                "SELECT " +
                                        "position, " +
                                        "aggregate_id, " +
                                        "version, " +
                                        "type, " +
                                        "payload, " +
                                        "recorded_at " +
                                        "FROM %s " +
                                        where,
                                TABLE_NAME
                        )
                );

                for (Map.Entry<String, Object> parameter : this.parameters.entrySet()) {
                    query = query.bind(parameter.getKey(), parameter.getValue());
                }

                final var events = query.map((rs, ctx) ->
                        new Event.PublishedEvent(
                                new Event.Unpublished(
                                        UUID.fromString(rs.getString("aggregate_id")),
                                        this.registry.event(rs.getString("type"), new JSON.Object(rs.getString("payload"))),
                                        new Version(rs.getInt("version"))
                                ),
                                rs.getLong("position"),
                                rs.getTimestamp("recorded_at").toLocalDateTime()
                        )
                ).list();
                this.handle.close();
                return events;
            }
        }

        private static class Insert {
            private final Handle handle;
            private final EventsRegistry registry;
            private final Clock clock;

            private Insert(Handle handle, EventsRegistry registry, Clock clock) {
                this.handle = handle;
                this.registry = registry;
                this.clock = clock;
            }

            void batch(List<Event.Unpublished> events) throws Exception {
                var batch = handle.prepareBatch(
                        "INSERT INTO events(aggregate_id, version, payload, type, recorded_at) " +
                                "VALUES (:aggregate_id, :version, :payload, :type, :recorded_at)"
                );
                events.forEach(event -> {
                    var payload = new PGobject();
                    payload.setType("json");
                    try {
                        payload.setValue(event.payload().json().marshelled());
                    } catch (SQLException e) {
                        throw new IllegalStateException(e);
                    }
                    batch
                            .bind("aggregate_id", event.aggregateID())
                            .bind("version", event.version().value())
                            .bind("type", this.registry.name(event.payload().getClass()))
                            .bind("payload", payload)
                            .bind("recorded_at", this.clock.now())
                            .add();
                });

                try {
                    batch.execute();
                } catch (UnableToExecuteStatementException e) {
                    throw new EventStream.Exception(e);
                } finally {
                    this.handle.close();
                }
            }
        }
    }
}
