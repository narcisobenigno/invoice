package invoice.common.jdbi;

import invoice.common.serialization.JSON;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.postgresql.util.PGobject;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InsertBatch {
    private final Handle handle;
    private final String table;
    private final List<Row> rows;
    private final Set<String> columnNames;

    public InsertBatch(Handle handle, String table) {
        this(handle, table, List.of(), Set.of());
    }

    private InsertBatch(Handle handle, String table, List<Row> rows, Set<String> columnNames) {
        this.handle = handle;
        this.table = table;
        this.rows = rows;
        this.columnNames = columnNames;
    }

    public int[] execute() {
        final var columnNames = new ArrayList<>(this.columnNames);
        var bound = handle.prepareBatch(
                String.format(
                        "INSERT INTO %s(%s) VALUES (%s)",
                        this.table,
                        String.join(", ", columnNames),
                        columnNames.stream().map(n -> String.format(":%s", n)).collect(Collectors.joining(", "))
                )
        );
        for (var row : this.rows) {
            bound = row.bind(bound);
        }

        return bound.execute();
    }

    public Row row() {
        return new Row(this);
    }

    private InsertBatch add(Row row, Set<String> columns) {
        return new InsertBatch(
                this.handle,
                this.table,
                Stream.concat(this.rows.stream(), Stream.of(row)).collect(Collectors.toList()),
                Stream.concat(this.columnNames.stream(), columns.stream()).collect(Collectors.toSet())
        );
    }

    interface Prepare {
        PreparedBatch bind(PreparedBatch batch);
    }

    public static class Row {
        private final InsertBatch batch;
        private final List<Parameter> parameters;

        private Row(InsertBatch batch) {
            this(batch, List.of());
        }

        private Row(InsertBatch batch, List<Parameter> parameters) {
            this.batch = batch;
            this.parameters = parameters;
        }

        public Row with(Parameter parameter) {
            return new Row(
                    this.batch,
                    Stream.concat(
                            this.parameters.stream(),
                            Stream.of(parameter)
                    ).collect(Collectors.toList())
            );
        }

        public InsertBatch add() {
            return this.batch.add(
                    this,
                    this.parameters.stream().map(Parameter::name).collect(Collectors.toSet())
            );
        }

        private PreparedBatch bind(PreparedBatch batch) {
            var preparing = batch;
            for (Parameter parameter : this.parameters) {
                preparing = parameter.bind(batch);
            }
            return preparing.add();
        }
    }

    public static class Parameter implements Prepare {
        private final Prepare prepare;
        private final String name;

        public Parameter(String name, JSON value) {
            this(name, batch -> {
                var payload = new PGobject();
                payload.setType("json");
                try {
                    payload.setValue(value.marshelled());
                } catch (SQLException e) {
                    throw new IllegalStateException(e);
                }
                return batch.bind(name, payload);
            });
        }

        public Parameter(String name, String value) {
            this(name, batch -> batch.bind(name, value));
        }

        public Parameter(String name, int value) {
            this(name, batch -> batch.bind(name, value));
        }

        public Parameter(String name, UUID value) {
            this(name, batch -> batch.bind(name, value));
        }

        public Parameter(String name, LocalDateTime value) {
            this(name, batch -> batch.bind(name, value));
        }

        public Parameter(String name, Prepare prepare) {
            this.name = name;
            this.prepare = prepare;
        }

        @Override
        public PreparedBatch bind(PreparedBatch batch) {
            return this.prepare.bind(batch);
        }

        public String name() {
            return this.name;
        }
    }
}
