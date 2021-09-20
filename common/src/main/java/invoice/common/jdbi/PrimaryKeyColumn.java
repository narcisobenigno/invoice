package invoice.common.jdbi;

public class PrimaryKeyColumn extends Column.Constraint {
    public PrimaryKeyColumn(Column configured) {
        super(configured, "PRIMARY KEY");
    }
}
