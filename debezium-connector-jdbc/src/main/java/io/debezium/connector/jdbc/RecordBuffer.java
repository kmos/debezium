/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.connector.jdbc;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.kafka.connect.data.Schema;

import io.debezium.annotation.VisibleForTesting;
import io.debezium.connector.jdbc.relational.TableDescriptor;

/**
 * A buffer of {@link JdbcSinkRecord}. It contains the logic of when is the time to flush
 *
 * @author Mario Fiore Vitale
 */
public class RecordBuffer implements Buffer {

    private final JdbcSinkConnectorConfig connectorConfig;
    private Schema keySchema;
    private Schema valueSchema;
    private final ArrayList<JdbcSinkRecord> records = new ArrayList<>();
    private final TableDescriptor tableDescriptor;

    @VisibleForTesting
    public RecordBuffer(JdbcSinkConnectorConfig connectorConfig) {
        this.connectorConfig = connectorConfig;
        this.tableDescriptor = null;
    }

    public RecordBuffer(JdbcSinkConnectorConfig connectorConfig, TableDescriptor tableDescriptor) {
        this.connectorConfig = connectorConfig;
        this.tableDescriptor = tableDescriptor;
    }

    @Override
    public List<JdbcSinkRecord> add(JdbcSinkRecord record) {
        List<JdbcSinkRecord> flushed = new ArrayList<>();
        boolean isSchemaChanged = false;

        if (records.isEmpty()) {
            keySchema = record.keySchema();
            valueSchema = record.valueSchema();
        }

        if (!Objects.equals(keySchema, record.keySchema()) || !Objects.equals(valueSchema, record.valueSchema())) {
            keySchema = record.keySchema();
            valueSchema = record.valueSchema();
            flushed = flush();
            isSchemaChanged = true;
        }

        records.add(record);

        if (isSchemaChanged) {
            // current record is already added in internal buffer after flush
            // just return the flushed buffer ignoring buffer size check
            return flushed;
        }

        if (records.size() >= connectorConfig.getBatchSize()) {
            flushed = flush();
        }

        return flushed;
    }

    @Override
    public List<JdbcSinkRecord> flush() {

        List<JdbcSinkRecord> flushed = new ArrayList<>(records);
        records.clear();

        return flushed;
    }

    @Override
    public boolean isEmpty() {
        return records.isEmpty();
    }

    @Override
    public TableDescriptor getTableDescriptor() {
        return tableDescriptor;
    }

    @Override
    public void remove(JdbcSinkRecord record) {
        throw new IllegalStateException("Can't remove record in simple buffer");
    }
}
