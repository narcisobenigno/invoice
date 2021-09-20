package invoice.common.jdbi;

public class NotNullColumn extends Column.Constraint {
    public NotNullColumn(Column configured) {
        super(configured, "NOT NULL");
    }
}
