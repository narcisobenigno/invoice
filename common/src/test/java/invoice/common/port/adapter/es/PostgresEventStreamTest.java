package invoice.common.port.adapter.es;

import invoice.common.clock.Clock;
import invoice.common.es.EventStream;
import invoice.common.es.EventStreamContractTest;
import invoice.common.es.EventsRegistry;
import invoice.common.serialization.JSON;
import invoice.common.test.Integration;
import org.junit.jupiter.api.AfterEach;

@Integration
class PostgresEventStreamTest implements EventStreamContractTest {
    private PostgresEventStream stream;

    @Override
    public EventStream createEventStream(Clock streamClock, EventsRegistry eventsRegistry) {
        this.stream = new PostgresEventStream(
                new PostgresEventStream.Credentials(new JSON.Object(System.getenv("POSTGRES_CREDENTIAL"))),
                streamClock,
                eventsRegistry
        );
        this.stream.dropTable();
        this.stream.createTable();
        return this.stream;
    }

    @AfterEach
    public void afterEach() {
        this.stream.dropTable();
    }
}