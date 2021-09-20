package invoice.common.jdbi;

public interface Column {
    String sql();

    String name();

    class Definition implements Column {
        private final String name;
        private final String typeName;

        public Definition(String name, String typeName) {
            this.name = name;
            this.typeName = typeName;
        }

        @Override
        public final String sql() {
            return String.format(
                    "%s %s ",
                    this.name,
                    this.typeName
            );
        }

        @Override
        public String name() {
            return this.name;
        }
    }

    class Constraint implements Column {
        private final Column configured;
        private final String configuration;

        public Constraint(Column configured, String configuration) {
            this.configured = configured;
            this.configuration = configuration;
        }

        @Override
        public final String sql() {
            return String.format("%s %s ", this.configured.sql(), this.configuration);
        }

        @Override
        public String name() {
            return this.configured.name();
        }
    }
}
