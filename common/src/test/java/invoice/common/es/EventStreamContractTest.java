package invoice.common.es;

import invoice.common.clock.Clock;
import invoice.common.serialization.JSON;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public interface EventStreamContractTest {
    EventStream createEventStream(Clock streamClock, EventsRegistry eventsRegistry);

    @Test
    default void publishes_event() throws EventStream.Exception {
        var stream = this.createEventStream(
                new Clock.InMemoryClock(LocalDateTime.of(2021, 1, 1, 0, 0, 0)),
                new EventsRegistry.InMemory(Map.of(
                        "sample-test", SampleEvent.class
                ))
        );

        stream.publish(List.of(
                new Event.Unpublished(
                        UUID.nameUUIDFromBytes("event-uuid-1".getBytes(StandardCharsets.UTF_8)),
                        new SampleEvent("sample value 1"),
                        new Version(1)
                ),
                new Event.Unpublished(
                        UUID.nameUUIDFromBytes("event-uuid-2".getBytes(StandardCharsets.UTF_8)),
                        new SampleEvent("sample value 2"),
                        new Version(1)
                )
        ));

        assertEquals(
                List.of(
                        new Event.PublishedEvent(
                                new Event.Unpublished(
                                        UUID.nameUUIDFromBytes("event-uuid-1".getBytes(StandardCharsets.UTF_8)),
                                        new SampleEvent("sample value 1"),
                                        new Version(1)
                                ),
                                1,
                                LocalDateTime.of(2021, 1, 1, 0, 0, 0)
                        ),
                        new Event.PublishedEvent(
                                new Event.Unpublished(
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
    default void conflicts_when_publishing_duplicated_version_for_an_aggregate() {
        var stream = this.createEventStream(
                new Clock.InMemoryClock(LocalDateTime.of(2021, 1, 1, 0, 0, 0)),
                new EventsRegistry.InMemory(Map.of(
                        "sample-test", SampleEvent.class
                ))
        );
        assertThrows(
                EventStream.Exception.class,
                () -> stream.publish(List.of(
                        new Event.Unpublished(
                                UUID.nameUUIDFromBytes("event-uuid-1".getBytes(StandardCharsets.UTF_8)),
                                new SampleEvent("sample value 1"),
                                new Version(1)
                        ),
                        new Event.Unpublished(
                                UUID.nameUUIDFromBytes("event-uuid-1".getBytes(StandardCharsets.UTF_8)),
                                new SampleEvent("sample value 2"),
                                new Version(1)
                        )
                ))
        );
    }

    @Test
    default void conflicts_when_publishing_existing_version_for_an_aggregate() throws EventStream.Exception {
        var stream = this.createEventStream(
                new Clock.InMemoryClock(LocalDateTime.of(2021, 1, 1, 0, 0, 0)),
                new EventsRegistry.InMemory(Map.of(
                        "sample-test", SampleEvent.class
                ))
        );
        stream.publish(List.of(
                new Event.Unpublished(
                        UUID.nameUUIDFromBytes("event-uuid-1".getBytes(StandardCharsets.UTF_8)),
                        new SampleEvent("sample value 1"),
                        new Version(1)
                )
        ));
        assertThrows(
                EventStream.Exception.class,
                () -> stream.publish(List.of(
                        new Event.Unpublished(
                                UUID.nameUUIDFromBytes("event-uuid-1".getBytes(StandardCharsets.UTF_8)),
                                new SampleEvent("sample value 2"),
                                new Version(1)
                        )
                ))
        );
    }

    @Test
    default void retrieves_events_by_aggregate_id() throws EventStream.Exception {
        var stream = this.createEventStream(
                new Clock.InMemoryClock(LocalDateTime.of(2021, 1, 1, 0, 0, 0)),
                new EventsRegistry.InMemory(Map.of(
                        "sample-test", SampleEvent.class,
                        "another-sample-test", AnotherSampleEvent.class
                ))
        );
        stream.publish(List.of(
                new Event.Unpublished(
                        UUID.nameUUIDFromBytes("event-uuid-1".getBytes(StandardCharsets.UTF_8)),
                        new SampleEvent("sample value"),
                        new Version(1)
                ),
                new Event.Unpublished(
                        UUID.nameUUIDFromBytes("event-uuid-1".getBytes(StandardCharsets.UTF_8)),
                        new AnotherSampleEvent("another sample value"),
                        new Version(2)
                ),
                new Event.Unpublished(
                        UUID.nameUUIDFromBytes("event-uuid-2".getBytes(StandardCharsets.UTF_8)),
                        new SampleEvent("event from other aggregate"),
                        new Version(1)
                )
        ));
        assertEquals(
                List.of(
                        new Event.PublishedEvent(
                                new Event.Unpublished(
                                        UUID.nameUUIDFromBytes("event-uuid-1".getBytes(StandardCharsets.UTF_8)),
                                        new SampleEvent("sample value"),
                                        new Version(1)
                                ),
                                1,
                                LocalDateTime.of(2021, 1, 1, 0, 0, 0)
                        ),
                        new Event.PublishedEvent(
                                new Event.Unpublished(
                                        UUID.nameUUIDFromBytes("event-uuid-1".getBytes(StandardCharsets.UTF_8)),
                                        new AnotherSampleEvent("another sample value"),
                                        new Version(2)
                                ),
                                2,
                                LocalDateTime.of(2021, 1, 1, 0, 0, 1)
                        )

                ),
                stream.events(UUID.nameUUIDFromBytes("event-uuid-1".getBytes(StandardCharsets.UTF_8)))
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


    @EqualsAndHashCode
    @ToString
    class AnotherSampleEvent implements Event.Payload {
        private final String value;

        public AnotherSampleEvent(JSON json) {
            this(json.stringValue("Value"));
        }

        public AnotherSampleEvent(String value) {
            this.value = value;
        }

        @Override
        public JSON json() {
            return new JSON.Object(Map.of("Value", this.value));
        }
    }
}
