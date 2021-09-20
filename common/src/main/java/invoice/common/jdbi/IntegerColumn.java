package invoice.common.jdbi;

public class IntegerColumn extends Column.Definition {
    public IntegerColumn(String name) {
        super(name, "INTEGER");
    }
}
