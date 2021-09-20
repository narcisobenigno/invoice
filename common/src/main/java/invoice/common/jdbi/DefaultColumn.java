package invoice.common.jdbi;

public class DefaultColumn extends Column.Constraint {
    public DefaultColumn(Column configured, String defaultExpression) {
        super(configured, String.format("DEFAULT %s", defaultExpression));
    }
}
