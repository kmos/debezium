/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.connector.jdbc.dialect.mysql;

import java.util.Set;

import org.hibernate.SessionFactory;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.MariaDBDialect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.debezium.connector.jdbc.JdbcSinkConnectorConfig;
import io.debezium.connector.jdbc.JdbcSinkRecord;
import io.debezium.connector.jdbc.dialect.DatabaseDialect;
import io.debezium.connector.jdbc.dialect.DatabaseDialectProvider;
import io.debezium.connector.jdbc.dialect.SqlStatementBuilder;
import io.debezium.connector.jdbc.dialect.maria.DoubleVectorType;
import io.debezium.connector.jdbc.dialect.maria.FloatVectorType;
import io.debezium.connector.jdbc.relational.TableDescriptor;

/**
 * A {@link DatabaseDialect} implementation for MariaDB.
 *
 */
public class MariaDbDatabaseDialect extends MySqlDatabaseDialect {

    private static final Logger LOGGER = LoggerFactory.getLogger(MariaDbDatabaseDialect.class);

    public static class MariaDbDatabaseDialectProvider implements DatabaseDialectProvider {
        @Override
        public boolean supports(Dialect dialect) {
            return dialect instanceof MariaDBDialect;
        }

        @Override
        public Class<?> name() {
            return MariaDbDatabaseDialect.class;
        }

        @Override
        public DatabaseDialect instantiate(JdbcSinkConnectorConfig config, SessionFactory sessionFactory) {
            LOGGER.info("MariaDB Dialect instantiated.");
            return new MariaDbDatabaseDialect(config, sessionFactory);
        }
    }

    private MariaDbDatabaseDialect(JdbcSinkConnectorConfig config, SessionFactory sessionFactory) {
        super(config, sessionFactory);
    }

    @Override
    protected void registerTypes() {
        super.registerTypes();

        registerType(FloatVectorType.INSTANCE);
        registerType(DoubleVectorType.INSTANCE);
    }

    /*
     * The use of VALUES() to refer to the new row and columns is deprecated in recent versions of MySQL,
     * but not followed by MariaDB yet.
     */
    @Override
    public String getUpsertStatement(TableDescriptor table, JdbcSinkRecord record) {
        final SqlStatementBuilder builder = new SqlStatementBuilder();
        builder.append("INSERT INTO ");
        builder.append(getQualifiedTableName(table.getId()));
        builder.append(" (");
        builder.appendLists(", ", record.keyFieldNames(), record.nonKeyFieldNames(), name -> columnNameFromField(name, record));
        builder.append(") VALUES (");
        builder.appendLists(", ", record.keyFieldNames(), record.nonKeyFieldNames(), name -> columnQueryBindingFromField(name, table, record));
        builder.append(") ");

        final Set<String> updateColumnNames = record.nonKeyFieldNames().isEmpty()
                ? record.keyFieldNames()
                : record.nonKeyFieldNames();

        builder.append("ON DUPLICATE KEY UPDATE ");
        builder.appendList(",", updateColumnNames, name -> {
            final String columnName = columnNameFromField(name, record);
            return columnName + "=VALUES(" + columnName + ")";
        });

        return builder.build();
    }

}
