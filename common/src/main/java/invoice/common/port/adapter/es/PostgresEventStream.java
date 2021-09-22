package invoice.common.port.adapter.es;

import invoice.common.clock.Clock;
import invoice.common.es.Event;
import invoice.common.es.EventStream;
import invoice.common.es.EventsRegistry;
import invoice.common.es.Version;
import invoice.common.jdbi.DefaultColumn;
import invoice.common.jdbi.EqualToCondition;
import invoice.common.jdbi.InsertBatch;
import invoice.common.jdbi.IntegerColumn;
import invoice.common.jdbi.JSONColumn;
import invoice.common.jdbi.NotNullColumn;
import invoice.common.jdbi.PrimaryKeyColumn;
import invoice.common.jdbi.SerialColumn;
import invoice.common.jdbi.Table;
import invoice.common.jdbi.TextColumn;
import invoice.common.jdbi.TimestamptzColumn;
import invoice.common.jdbi.UUIDColumn;
import invoice.common.jdbi.UUIDValue;
import invoice.common.jdbi.UniqueConstraint;
import invoice.common.serialization.JSON;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.jdbi.v3.postgres.PostgresPlugin;

import java.util.List;
import java.util.UUID;

public class PostgresEventStream implements EventStream {
    private final Table events;
    private final EventsRegistry registry;
    private final Clock clock;

    public PostgresEventStream(Credentials credentials, EventsRegistry registry, Clock clock) {
        this(
                new Table(
                        Jdbi.create(
                                        String.format(
                                                "jdbc:postgresql://%s/%s",
                                                credentials.host,
                                                credentials.dbname
                                        ),
                                        credentials.username,
                                        credentials.password
                                )
                                .installPlugin(new PostgresPlugin())
                        , "events")
                        .with(new PrimaryKeyColumn(new SerialColumn("position")))
                        .with(new NotNullColumn(new UUIDColumn("aggregate_id")))
                        .with(new NotNullColumn(new IntegerColumn("version")))
                        .with(new NotNullColumn(new TextColumn("type")))
                        .with(new NotNullColumn(new JSONColumn("payload")))
                        .with(new NotNullColumn(new DefaultColumn(new TimestamptzColumn("recorded_at"), "now()")))
                        .with(new UniqueConstraint("events_optimistic_lock", "aggregate_id", "version")),
                registry,
                clock
        );
    }

    private PostgresEventStream(Table events, EventsRegistry registry, Clock clock) {
        this.events = events;
        this.registry = registry;
        this.clock = clock;
    }

    public void createTable() {
        this.events.create();
    }

    public void dropTable() {
        this.events.drop();
    }

    public void publish(List<Event.Unpublished> events) throws Exception {
        var batch = this.events.insertBatch();
        for (Event.Unpublished event : events) {
            batch = batch.row()
                    .with(new InsertBatch.Parameter("aggregate_id", event.aggregateID()))
                    .with(new InsertBatch.Parameter("version", event.version().value()))
                    .with(new InsertBatch.Parameter("type", this.registry.name(event.payload().getClass())))
                    .with(new InsertBatch.Parameter("payload", event.payload().json()))
                    .with(new InsertBatch.Parameter("recorded_at", this.clock.now()))
                    .add();
        }

        try {
            batch.execute();
        } catch (UnableToExecuteStatementException e) {
            throw new Exception(e);
        }
    }

    public List<Event.PublishedEvent> all() {
        return this.events.select()
                .query((rs, ctx) -> new Event.PublishedEvent(
                        new Event.Unpublished(
                                UUID.fromString(rs.getString("aggregate_id")),
                                this.registry.event(rs.getString("type"), new JSON.Object(rs.getString("payload"))),
                                new Version(rs.getInt("version"))
                        ),
                        rs.getLong("position"),
                        rs.getTimestamp("recorded_at").toLocalDateTime()
                ));
    }

    @Override
    public List<Event.PublishedEvent> events(UUID id) {
        return this.events.select()
                .where(new EqualToCondition("aggregate_id", new UUIDValue(id)))
                .query((rs, ctx) -> new Event.PublishedEvent(
                        new Event.Unpublished(
                                UUID.fromString(rs.getString("aggregate_id")),
                                this.registry.event(rs.getString("type"), new JSON.Object(rs.getString("payload"))),
                                new Version(rs.getInt("version"))
                        ),
                        rs.getLong("position"),
                        rs.getTimestamp("recorded_at").toLocalDateTime()
                ));
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
}
