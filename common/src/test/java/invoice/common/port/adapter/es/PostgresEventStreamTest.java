package invoice.common.port.adapter.es;

import invoice.common.clock.Clock;
import invoice.common.es.Event;
import invoice.common.es.EventStream;
import invoice.common.es.EventsRegistry;
import invoice.common.es.PersistedEvent;
import invoice.common.es.Version;
import invoice.common.serialization.JSON;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("integration")
class PostgresEventStreamTest {
    @Test
    void publishes_event() throws EventStream.Exception {
        PostgresEventStream stream = this.createStream(LocalDateTime.of(2021, 1, 1, 0, 0, 0));
        stream.publish(List.of(
                new Event.Default(
                        UUID.nameUUIDFromBytes("event-uuid-1".getBytes(StandardCharsets.UTF_8)),
                        new SampleEvent("sample value 1"),
                        new Version(1)
                ),
                new Event.Default(
                        UUID.nameUUIDFromBytes("event-uuid-2".getBytes(StandardCharsets.UTF_8)),
                        new SampleEvent("sample value 2"),
                        new Version(1)
                )
        ));

        assertEquals(
                List.of(
                        new PersistedEvent(
                                new Event.Default(
                                        UUID.nameUUIDFromBytes("event-uuid-1".getBytes(StandardCharsets.UTF_8)),
                                        new SampleEvent("sample value 1"),
                                        new Version(1)
                                ),
                                1,
                                LocalDateTime.of(2021, 1, 1, 0, 0, 0)
                        ),
                        new PersistedEvent(
                                new Event.Default(
                                        UUID.nameUUIDFromBytes("event-uuid-2".getBytes(StandardCharsets.UTF_8)),
                                        new SampleEvent("sample value 2"),
                                        new Version(1)
                                ),
                                2,
                                LocalDateTime.of(2021, 1, 1, 0, 0, 1)
                        )
                ),
                stream.all()
        );

        stream.dropTable();
    }

    @Test
    void conflicts_when_version_already_exist() {
        PostgresEventStream stream = this.createStream(LocalDateTime.of(2021, 1, 1, 0, 0, 0));
        assertThrows(
                EventStream.Exception.class,
                () -> stream.publish(List.of(
                        new Event.Default(
                                UUID.nameUUIDFromBytes("event-uuid-1".getBytes(StandardCharsets.UTF_8)),
                                new SampleEvent("sample value 1"),
                                new Version(1)
                        ),
                        new Event.Default(
                                UUID.nameUUIDFromBytes("event-uuid-1".getBytes(StandardCharsets.UTF_8)),
                                new SampleEvent("sample value 2"),
                                new Version(1)
                        )
                ))
        );

        stream.dropTable();
    }

    private PostgresEventStream createStream(LocalDateTime clockStartTime) {
        var stream = new PostgresEventStream(
                new PostgresEventStream.Credentials(new JSON.Object(System.getenv("POSTGRES_CREDENTIAL"))),
                new Clock.InMemoryClock(clockStartTime),
                new EventsRegistry.InMemory(Map.of(
                        "sample-test", SampleEvent.class
                ))
        );
        stream.dropTable();
        stream.createTable();
        return stream;
    }

    @EqualsAndHashCode
    @ToString
    static class SampleEvent implements Event.Payload {
        private final String value;

        public SampleEvent(JSON json) {
            this(json.stringValue("Value"));
        }

        public SampleEvent(String value) {
            this.value = value;
        }

        @Override
        public JSON json() {
            return new JSON.Object(Map.of("Value", this.value));
        }
    }
}