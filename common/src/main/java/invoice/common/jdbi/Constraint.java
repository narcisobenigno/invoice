package invoice.common.jdbi;

public interface Constraint extends TableConfiguration {
    class Base implements Constraint {
        private final String name;
        private final String constraint;

        public Base(String name, String constraint) {
            this.name = name;
            this.constraint = constraint;
        }

        @Override
        public final String asString() {
            return String.format(
                    "CONSTRAINT \"%s\" %s",
                    this.name,
                    this.constraint
            );
        }
    }
}
