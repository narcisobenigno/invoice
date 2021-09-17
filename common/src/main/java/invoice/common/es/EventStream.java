package invoice.common.es;

import java.util.List;

public interface EventStream {
    void publish(List<Event.Default> event) throws Exception;

    List<PersistedEvent> all() throws Exception;

    class Exception extends java.lang.Exception {
        public Exception(Throwable cause) {
            super(cause);
        }
    }
}
