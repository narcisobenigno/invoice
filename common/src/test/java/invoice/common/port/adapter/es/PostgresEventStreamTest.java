package invoice.common.port.adapter.es;

import invoice.common.clock.Clock;
import invoice.common.es.EventStream;
import invoice.common.es.EventStreamContractTest;
import invoice.common.es.EventsRegistry;
import invoice.common.serialization.JSON;
import invoice.common.test.Integration;

import java.util.Map;

@Integration
class PostgresEventStreamTest implements EventStreamContractTest {
    private PostgresEventStream stream;

    @Override
    public EventStream createEventStream(Clock streamClock) {
        this.stream = new PostgresEventStream(
                new PostgresEventStream.Credentials(new JSON.Object(System.getenv("POSTGRES_CREDENTIAL"))),
                streamClock,
                new EventsRegistry.InMemory(Map.of(
                        "sample-test", SampleEvent.class
                ))
        );
        this.stream.dropTable();
        this.stream.createTable();
        return this.stream;
    }

    @Override
    public void tearDown() {
        this.stream.dropTable();
    }
}