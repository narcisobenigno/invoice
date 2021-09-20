package invoice.common.jdbi;

public interface Column extends TableConfiguration {
    class Definition implements Column {
        private final String name;
        private final String typeName;

        public Definition(String name, String typeName) {
            this.name = name;
            this.typeName = typeName;
        }

        @Override
        public final String asString() {
            return String.format(
                    "%s %s ",
                    this.name,
                    this.typeName
            );
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
        public final String asString() {
            return String.format("%s %s ", this.configured.asString(), this.configuration);
        }
    }
}
