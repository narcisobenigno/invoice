package invoice.common.jdbi;

public class TimestamptzColumn extends Column.Definition {
    public TimestamptzColumn(String name) {
        super(name, "TIMESTAMPTZ");
    }
}
