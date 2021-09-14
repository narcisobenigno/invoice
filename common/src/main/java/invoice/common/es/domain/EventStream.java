package invoice.common.es.domain;

import java.util.List;

public interface EventStream {
    void publish(List<Event.Default> event);
}
