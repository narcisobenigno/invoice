package invoice.common.es;

import invoice.common.clock.Clock;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface EventStream {
    void publish(List<Event.Unpublished> event) throws Exception;

    List<Event.PublishedEvent> all() throws Exception;

    List<Event.PublishedEvent> events(UUID id);

    class Exception extends java.lang.Exception {
        public Exception(Throwable cause) {
            super(cause);
        }
    }

    class InMemory implements EventStream {
        private final Clock clock;
        private final List<Event.PublishedEvent> published;
        private final Constraints constraints;
        private long currentPosition;

        public InMemory(Clock clock) {
            this.clock = clock;
            this.published = new ArrayList<>();
            this.currentPosition = 1;
            this.constraints = events -> new UniqueConstraint(
                    published,
                    new UniqueConstraint(events)
            ).events();
        }

        @Override
        public void publish(List<Event.Unpublished> events) throws Exception {
            synchronized (this.published) {
                this.constraints
                        .apply(events)
                        .forEach(event -> this.published.add(new Event.PublishedEvent(
                                event,
                                this.currentPosition++,
                                this.clock.now()
                        )));
            }
        }

        @Override
        public List<Event.PublishedEvent> all() {
            return List.copyOf(this.published);
        }

        @Override
        public List<Event.PublishedEvent> events(UUID id) {
            return this.published.stream()
                    .filter(event -> event.aggregateID().equals(id))
                    .collect(Collectors.toList());
        }

        private interface Constraints {
            List<? extends Event> apply(List<? extends Event> events) throws Exception;
        }

        private static class UniqueConstraint {

            private final List<? extends Event> events;

            public UniqueConstraint(List<? extends Event> events1, UniqueConstraint event2) throws Exception {
                this(
                        Stream.concat(events1.stream(), event2.events().stream())
                                .collect(Collectors.toList())
                );
            }

            public UniqueConstraint(List<? extends Event> events) {
                this.events = events;
            }

            public List<? extends Event> events() throws Exception {
                final var unique = this.events.stream()
                        .map(UniqueConstraintKey::new)
                        .collect(Collectors.toSet());

                if (unique.size() < this.events.size()) {
                    throw new Exception(new IllegalStateException("events violates unique constraint"));
                }

                return List.copyOf(this.events);
            }

        }

        @EqualsAndHashCode
        private static class UniqueConstraintKey {
            private final UUID id;

            private final Version version;

            public UniqueConstraintKey(Event event) {
                this(event.aggregateID(), event.version());
            }

            private UniqueConstraintKey(UUID id, Version version) {
                this.id = id;
                this.version = version;
            }

        }
    }
}
