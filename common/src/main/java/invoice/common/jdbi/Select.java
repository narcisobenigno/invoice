package invoice.common.jdbi;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.mapper.RowMapper;

import java.util.List;

public class Select {
    private final Handle handle;
    private final TableConfigurations configurations;
    private final String table;
    private final Where where;

    public Select(Handle handle, TableConfigurations configurations, String table) {
        this(handle, configurations, table, new Where.NoWhere());
    }

    private Select(Handle handle, TableConfigurations configurations, String table, Where where) {
        this.handle = handle;
        this.configurations = configurations;
        this.table = table;
        this.where = where;
    }

    public Select where(Condition condition) {
        return new Select(
                this.handle,
                this.configurations,
                this.table,
                new Where.Condition(condition)
        );
    }

    public <T> List<T> query(RowMapper<T> mapper) {
        return this.where.bind(handle.createQuery(
                        String.format(
                                "SELECT %s FROM \"%s\" %s",
                                configurations.columnsNames(),
                                this.table,
                                this.where.sql()
                        )
                ))
                .map(mapper)
                .list();
    }
}
