package invoice.common.jdbi;

public class SerialColumn extends Column.Definition {
    public SerialColumn(String name) {
        super(name, "SERIAL");
    }
}
