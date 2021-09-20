package invoice.common.jdbi;

import org.jdbi.v3.core.statement.Query;

public interface Condition {
    String sql();

    Query bind(Query query);

    interface Value {
        Query bind(String name, Query query);
    }
}
