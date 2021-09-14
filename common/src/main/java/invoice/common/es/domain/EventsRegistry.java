package invoice.common.es.domain;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

public interface EventsRegistry {
    Event.Payload event(String name, byte[] content);

    String name(Class<? extends Event.Payload> clazz);

    class InMemory implements EventsRegistry {
        private final Map<String, Class<? extends Event.Payload>> classIndex;
        private final Map<Class<? extends Event.Payload>, String> nameIndex;

        public InMemory(Map<String, Class<? extends Event.Payload>> registry) {
            this.classIndex = Collections.unmodifiableMap(registry);
            this.nameIndex = registry.entrySet().stream()
                    .collect(Collectors.toUnmodifiableMap(Map.Entry::getValue, Map.Entry::getKey));
        }

        @Override
        public Event.Payload event(String name, byte[] content) {
            try {
                final Constructor<? extends Event.Payload> constructor = this.classIndex.get(name).getConstructor(byte[].class);
                constructor.setAccessible(true);
                return constructor.newInstance(content);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new IllegalArgumentException("not able to create payload", e);
            }
        }

        @Override
        public String name(Class<? extends Event.Payload> clazz) {
            return this.nameIndex.get(clazz);
        }
    }
}
