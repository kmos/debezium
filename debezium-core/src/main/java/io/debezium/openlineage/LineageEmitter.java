/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.openlineage;

import io.debezium.connector.common.BaseSourceTask;
import io.debezium.relational.Table;

public interface LineageEmitter {

    public void emit(BaseSourceTask.State state);

    public void emit(BaseSourceTask.State state, Throwable t);

    public void emit(BaseSourceTask.State state, Table event);

    public void emit(BaseSourceTask.State state, Table event, Throwable t);
}
