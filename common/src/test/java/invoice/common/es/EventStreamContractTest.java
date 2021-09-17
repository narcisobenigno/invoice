package invoice.common.es;

import invoice.common.clock.Clock;
import invoice.common.serialization.JSON;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public interface EventStreamContractTest {
    EventStream createEventStream(Clock streamClock);

    void tearDown();

    @AfterEach
    default void afterEach() {
        this.tearDown();
    }

    @Test
    default void publishes_event() throws EventStream.Exception {
        var stream = this.createEventStream(new Clock.InMemoryClock(LocalDateTime.of(2021, 1, 1, 0, 0, 0)));

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
    }

    @Test
    default void conflicts_when_version_already_exist() {
        var stream = this.createEventStream(new Clock.InMemoryClock(LocalDateTime.of(2021, 1, 1, 0, 0, 0)));
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
    }

    @EqualsAndHashCode
    @ToString
    class SampleEvent implements Event.Payload {
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
