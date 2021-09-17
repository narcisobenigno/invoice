module invoice.common {
    exports invoice.common.es;
    exports invoice.common.clock;
    exports invoice.common.serialization;
    exports invoice.common.port.adapter.es;

    requires static lombok;

    requires org.json;

    requires org.jdbi.v3.core;
    requires org.jdbi.v3.postgres;
    requires org.postgresql.jdbc;
    requires java.sql;
}