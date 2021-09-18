package invoice.common.es;

import invoice.common.serialization.JSON;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

public interface Event {
    UUID aggregateID();

    Payload payload();

    Version version();

    interface Payload {
        JSON json();
    }

    @EqualsAndHashCode
    @ToString
    class Unpublished implements Event {
        private final UUID aggregateID;
        private final Event.Payload payload;
        private final Version version;

        public Unpublished(UUID aggregateID, Event.Payload payload, Version version) {
            this.aggregateID = aggregateID;
            this.payload = payload;
            this.version = version;
        }

        @Override
        public UUID aggregateID() {
            return aggregateID;
        }

        @Override
        public Event.Payload payload() {
            return payload;
        }

        @Override
        public Version version() {
            return version;
        }

    }

    @EqualsAndHashCode
    @ToString
    class PublishedEvent implements Event {
        private final Event event;
        private final long position;
        private final LocalDateTime recordedAt;

        public PublishedEvent(Event event, long position, LocalDateTime recordedAt) {
            this.event = event;
            this.position = position;
            this.recordedAt = recordedAt;
        }

        @Override
        public UUID aggregateID() {
            return this.event.aggregateID();
        }

        @Override
        public Payload payload() {
            return this.event.payload();
        }

        @Override
        public Version version() {
            return this.event.version();
        }

        public long position() {
            return this.position;
        }

        public LocalDateTime recordedAt() {
            return this.recordedAt;
        }
    }
}

