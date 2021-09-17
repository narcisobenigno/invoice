package invoice.common.es.port.adapter;

import invoice.common.clock.Clock;
import invoice.common.es.domain.Event;
import invoice.common.es.domain.EventStream;
import invoice.common.es.domain.EventsRegistry;
import invoice.common.es.domain.PersistedEvent;
import invoice.common.es.domain.Version;
import invoice.common.serialization.JSON;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.postgresql.util.PGobject;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class PostgresEventStream implements EventStream {
    private final static String TABLE_NAME = "events";
    private final Jdbi jdbi;
    private final Clock clock;
    private final EventsRegistry eventsRegistry;

    public PostgresEventStream(Credentials credentials, Clock clock, EventsRegistry eventsRegistry) {
        this.jdbi = Jdbi.create(
                        String.format(
                                "jdbc:postgresql://%s/%s",
                                credentials.host,
                                credentials.dbname
                        ),
                        credentials.username,
                        credentials.password
                )
                .installPlugin(new PostgresPlugin());
        this.clock = clock;
        this.eventsRegistry = eventsRegistry;
    }

    public void createTable() {
        this.jdbi.useHandle(handle -> handle.execute(String.format(
                                "CREATE TABLE %1$s(" +
                                        "position SERIAL PRIMARY KEY," +
                                        "aggregate_id UUID NOT NULL," +
                                        "version INTEGER NOT NULL," +
                                        "type TEXT NOT NULL," +
                                        "payload JSON NOT NULL," +
                                        "recorded_at TIMESTAMPTZ DEFAULT now() NOT NULL, " +

                                        "CONSTRAINT \"%1$s_optimistic_lock\" UNIQUE (\"aggregate_id\", \"version\")" +
                                        ");",
                                TABLE_NAME
                        )

                )
        );
    }

    public void dropTable() {
        this.jdbi.useHandle(handle -> handle.execute("DROP TABLE IF EXISTS events;"));
    }

    public void publish(List<Event.Default> events) throws Exception {
        try (var handle = this.jdbi.open()) {
            var batch = handle.prepareBatch(
                    "INSERT INTO events(aggregate_id, version, payload, type, recorded_at) " +
                            "VALUES (:aggregate_id, :version, :payload, :type, :recorded_at)"
            );
            events.forEach(event -> {
                var payload = new PGobject();
                payload.setType("json");
                try {
                    payload.setValue(event.payload().json().string());
                } catch (SQLException e) {
                    throw new IllegalStateException(e);
                }
                batch
                        .bind("aggregate_id", event.aggregateID())
                        .bind("version", event.version().value())
                        .bind("type", this.eventsRegistry.name(event.payload().getClass()))
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

    public List<PersistedEvent> all() {
        try (final var handler = this.jdbi.open()) {

            return handler.createQuery(
                    "SELECT " +
                            "position, " +
                            "aggregate_id, " +
                            "version, " +
                            "type, " +
                            "payload, " +
                            "recorded_at " +
                            "FROM events"
            ).map((rs, ctx) ->
                    new PersistedEvent(
                            new Event.Default(
                                    UUID.fromString(rs.getString("aggregate_id")),
                                    this.eventsRegistry.event(rs.getString("type"), new JSON.Object(rs.getString("payload"))),
                                    new Version(rs.getInt("version"))
                            ),
                            rs.getLong("position"),
                            rs.getTimestamp("recorded_at").toLocalDateTime()
                    )
            ).list();
        }
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
