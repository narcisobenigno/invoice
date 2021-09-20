package invoice.common.jdbi;

public class JSONColumn extends Column.Definition {
    public JSONColumn(String name) {
        super(name, "JSON");
    }
}
