package invoice.common.jdbi;

public class TextColumn extends Column.Definition {
    public TextColumn(String name) {
        super(name, "TEXT");
    }
}
