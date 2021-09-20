package invoice.common.jdbi;

public class UUIDColumn extends Column.Definition {
    public UUIDColumn(String name) {
        super(name, "UUID");
    }
}
