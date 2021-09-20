package invoice.common.jdbi;

import org.jdbi.v3.core.statement.Query;

public class EqualToCondition implements Condition {
    private final String column;
    private final Value value;

    public EqualToCondition(String column, Value value) {
        this.column = column;
        this.value = value;
    }

    @Override
    public String sql() {
        return String.format("\"%1$s\" = :%1$s", this.column);
    }

    @Override
    public Query bind(Query query) {
        return this.value.bind(this.column, query);
    }
}
