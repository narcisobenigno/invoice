package invoice.common.jdbi;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TableConfigurations {
    private final List<Column> columns;
    private final List<Constraint> constraints;

    public TableConfigurations() {
        this(List.of(), List.of());
    }

    public TableConfigurations(List<Column> columns, List<Constraint> constraints) {
        this.columns = columns;
        this.constraints = constraints;
    }

    public TableConfigurations plus(Constraint constraint) {
        return new TableConfigurations(
                this.columns,
                Stream.concat(
                        this.constraints.stream(),
                        Stream.of(constraint)
                ).collect(Collectors.toList())
        );
    }

    public TableConfigurations plus(Column column) {
        return new TableConfigurations(
                Stream.concat(
                        this.columns.stream(),
                        Stream.of(column)
                ).collect(Collectors.toList()),
                this.constraints
        );
    }

    public String sql() {
        return Stream.concat(
                this.columns.stream()
                        .map(Column::sql),
                this.constraints.stream()
                        .map(Constraint::sql)
        ).collect(Collectors.joining(",\n"));
    }

    public String columnsNames() {
        return this.columns.stream().map(Column::name).collect(Collectors.joining(",\n"));
    }
}
