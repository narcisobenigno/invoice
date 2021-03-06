package invoice.common.jdbi;

import org.jdbi.v3.core.Jdbi;

public class Table {
    private final Jdbi jdbi;
    private final String name;
    private final TableConfigurations configurations;

    public Table(Jdbi jdbi, String name) {
        this(jdbi, name, new TableConfigurations());
    }

    private Table(Jdbi jdbi, String name, TableConfigurations configurations) {
        this.jdbi = jdbi;
        this.name = name;
        this.configurations = configurations;
    }

    public Table with(Column column) {
        return new Table(
                this.jdbi,
                this.name,
                this.configurations.plus(column)
        );
    }

    public Table with(Constraint constraint) {
        return new Table(
                this.jdbi,
                this.name,
                this.configurations.plus(constraint)
        );
    }

    public void create() {
        this.jdbi.useHandle(handle -> handle.execute(String.format(
                                "CREATE TABLE %s(%s)",
                                this.name(),
                                this.configurations.sql()
                        )
                )
        );
    }

    public void drop() {
        this.jdbi.useHandle(handle -> handle.execute(String.format("DROP TABLE IF EXISTS %s;", this.name())));
    }

    public Select select() {
        return new Select(this.jdbi.open(), this.configurations, this.name());
    }

    public InsertBatch insertBatch() {
        return new InsertBatch(this.jdbi.open(), this.name());
    }

    String name() {
        return this.name;
    }
}
