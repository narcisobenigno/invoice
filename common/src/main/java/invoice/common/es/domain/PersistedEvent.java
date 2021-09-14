package invoice.common.es.domain;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

@EqualsAndHashCode
@ToString
public class PersistedEvent implements Event {
    private final Event event;
    private final long position;
    private final LocalDateTime recordedAt;

    public PersistedEvent(Event event, long position, LocalDateTime recordedAt) {
        this.event = event;
        this.position = position;
        this.recordedAt = recordedAt;
    }

    @Override
    public UUID aggregateID() {
        return this.event.aggregateID();
    }

    @Override
    public Default.Payload payload() {
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
