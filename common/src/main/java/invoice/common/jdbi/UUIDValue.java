package invoice.common.jdbi;

import org.jdbi.v3.core.statement.Query;

import java.util.UUID;

public class UUIDValue implements Condition.Value {
    private final UUID uuid;

    public UUIDValue(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public Query bind(String name, Query query) {
        return query.bind(name, this.uuid);
    }
}
