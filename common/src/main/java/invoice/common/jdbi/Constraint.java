package invoice.common.jdbi;

public interface Constraint {
    String sql();

    class Base implements Constraint {
        private final String name;
        private final String constraint;

        public Base(String name, String constraint) {
            this.name = name;
            this.constraint = constraint;
        }

        @Override
        public final String sql() {
            return String.format(
                    "CONSTRAINT \"%s\" %s",
                    this.name,
                    this.constraint
            );
        }
    }
}
