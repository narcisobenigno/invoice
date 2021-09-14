package invoice.common.clock;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public interface Clock {
    LocalDateTime now();

    class InMemoryClock implements Clock {
        private final Map<String, LocalDateTime> current;

        public InMemoryClock(LocalDateTime current) {
            this.current = new ConcurrentHashMap<>(Map.of("current", current));
        }

        @Override
        public LocalDateTime now() {
            final var current = this.current.get("current");
            this.current.replace("current", current.plusSeconds(1));

            return current;
        }
    }
}
