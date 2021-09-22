package invoice.common.jdbi;

import org.jdbi.v3.core.statement.Query;

public interface Where extends Script {
    Query bind(Query query);

    class NoWhere implements Where {
        @Override
        public String sql() {
            return "";
        }

        @Override
        public Query bind(Query query) {
            return query;
        }
    }

    class Condition implements Where {
        private final invoice.common.jdbi.Condition condition;

        public Condition(invoice.common.jdbi.Condition condition) {
            this.condition = condition;
        }

        @Override
        public String sql() {
            return "WHERE " + this.condition.sql();
        }

        @Override
        public Query bind(Query query) {
            return this.condition.bind(query);
        }
    }
}
