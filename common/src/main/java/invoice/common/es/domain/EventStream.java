package invoice.common.es.domain;

import java.util.List;

public interface EventStream {
    void publish(List<Event.Default> event) throws Exception;

    class Exception extends java.lang.Exception {
        public Exception(Throwable cause) {
            super(cause);
        }
    }
}
