/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.openlineage;

import java.util.ServiceLoader;

import io.debezium.config.Configuration;

/**
 * A utility class for emitting OpenLineage events from Debezium connectors.
 * <p>
 * This class serves as a facade for the underlying OpenLineage integration, providing
 * static methods for initializing the emitter and emitting lineage events at various
 * points in the Debezium connector lifecycle. The implementation uses a thread-safe
 * approach to ensure proper initialization across multiple threads.
 * <p>
 * The emitter will only be active if OpenLineage integration is enabled in the configuration.
 * Otherwise, a no-operation implementation is used that performs no actual emission.
 *
 * @see LineageEmitter
 * @see OpenLineageEventEmitter
 * @see OpenLineageContext
 */
public class DebeziumOpenLineageEmitter {

    private static final ServiceLoader<LineageEmitterFactory> factory = ServiceLoader.load(LineageEmitterFactory.class);
    private static volatile LineageEmitter instance;

    /**
     * Initializes the lineage emitter with the given configuration.
     * <p>
     * This method must be called before any emission methods are used. It sets up the
     * OpenLineage context and emitter if OpenLineage integration is enabled in the configuration.
     * The initialization is thread-safe and ensures the context is only created once.
     *
     * @param configuration
     * @param connName      The name of the connector used for event attribution
     */
    public static LineageEmitter getInstance(Configuration configuration, String connName) {
        if (instance == null) {
            synchronized (LineageEmitter.class) {
                if (instance == null) {
                    instance = factory
                            .stream()
                            .findFirst()
                            .map(ServiceLoader.Provider::get)
                            .orElse((ignore, ignore2) -> new NoOpLineageEmitter())
                            .get(configuration, connName);
                }
            }
        }

        return instance;
    }

    public static LineageEmitter getInstance() {
        if (instance == null) {
            throw new IllegalStateException("DebeziumOpenLineageEmitter not yet initialized");
        }
        return instance;
    }

}
