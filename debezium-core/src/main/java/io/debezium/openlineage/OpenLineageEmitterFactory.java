/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */

package io.debezium.openlineage;

import io.debezium.config.CommonConnectorConfig;
import io.debezium.config.Configuration;
import io.openlineage.client.OpenLineage;

public class OpenLineageEmitterFactory implements LineageEmitterFactory {
    private static final String CONNECTOR_NAME_PROPERTY = "name";

    @Override
    public LineageEmitter get(Configuration configuration, String connName) {
        boolean isEnabled = configuration.getBoolean("openlineage.integration.enabled", false);
        String path = configuration.getString("openlineage.integration.config.path", ".");
        Configuration subsetConfiguration = configuration.subset("openlineage.integration", false);
        String topic = configuration.getString(CommonConnectorConfig.TOPIC_PREFIX);
        String connectorName = configuration.getString(CONNECTOR_NAME_PROPERTY);

        if (isEnabled) {
            return new OpenLineageEmitter(connName, configuration, OpenLineageContext.of(
                    new OpenLineage(new OpenLineageEventEmitter(path).getProducer()),
                    subsetConfiguration,
                    new OpenLineageJobIdentifier(topic, connectorName)),
                    new OpenLineageEventEmitter(path));
        }

        return new NoOpLineageEmitter();
    }
}
