package invoice.common.jdbi;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TableConfigurations implements TableConfiguration {
    private final List<TableConfiguration> configurations;

    public TableConfigurations() {
        this(List.of());
    }

    public TableConfigurations(List<TableConfiguration> columns) {
        this.configurations = columns;
    }

    public TableConfigurations plus(TableConfiguration configuration) {
        return new TableConfigurations(
                Stream.concat(
                        this.configurations.stream(),
                        Stream.of(configuration)
                ).collect(Collectors.toList())
        );
    }

    @Override
    public String asString() {
        return this.configurations.stream()
                .map(TableConfiguration::asString)
                .collect(Collectors.joining(",\n"));
    }
}
