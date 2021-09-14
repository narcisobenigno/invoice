package invoice.common.es.port.adapter;

import invoice.common.es.domain.Clock;
import invoice.common.es.domain.Event;
import invoice.common.es.domain.EventsRegistry;
import invoice.common.es.domain.PersistedEvent;
import invoice.common.es.domain.Version;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PostgresEventStreamTest {
    @Test
    void publishes_event() {
        var stream = new PostgresEventStream(
                new PostgresEventStream.Credentials("localhost:5432", "invoice", "user", "password"),
                new Clock.InMemoryClock(LocalDateTime.of(2021, 1, 1, 0, 0, 0)),
                new EventsRegistry.InMemory(Map.of(
                        "sample-test", SampleEvent.class
                ))
        );
        stream.dropTable();
        stream.createTable();
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

    static class SampleEvent implements Event.Payload {
        private final String value;

        public SampleEvent(byte[] json) {
            this(new JSONObject(new String(json, StandardCharsets.UTF_8)).getString("Value"));
        }

        public SampleEvent(String value) {
            this.value = value;
        }

        @Override
        public byte[] json() {
            var value = new JSONObject(Map.of("Value", this.value));

            var out = new ByteArrayOutputStream();
            try (var output = new OutputStreamWriter(out)) {
                value.write(output);
                output.flush();
                return out.toByteArray();
            } catch (IOException e) {
                throw new IllegalStateException("error to create byte array output stream", e);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SampleEvent that = (SampleEvent) o;
            return value.equals(that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }
}