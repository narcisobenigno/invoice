package invoice.common.es.domain;

import invoice.common.serialization.JSON;
import lombok.EqualsAndHashCode;
import lombok.ToString;

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
    class Default implements Event {
        private final UUID aggregateID;
        private final Event.Payload payload;
        private final Version version;

        public Default(UUID aggregateID, Event.Payload payload, Version version) {
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
}

