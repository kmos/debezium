/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.openlineage;

import java.util.List;
import java.util.UUID;

import io.debezium.config.Configuration;
import io.openlineage.client.OpenLineage;
import io.openlineage.client.utils.UUIDUtils;

public class OpenLineageContext {

    private static final String LIST_SEPARATOR = ",";

    private final UUID runUuid;
    private final OpenLineage openLineage;
    private final OpenLineageJobIdentifier jobIdentifier;
    private final String description;
    private final List<String> tags;
    private final List<String> owners;

    private OpenLineageContext(OpenLineage openLineage, Configuration configuration, OpenLineageJobIdentifier jobIdentifier) {
        this.openLineage = openLineage;
        this.jobIdentifier = jobIdentifier;
        this.runUuid = UUIDUtils.generateNewUUID();
        this.description = configuration.getString("openlineage.integration.job.description", "");
        this.tags = configuration.getList("openlineage.integration.tags", LIST_SEPARATOR, s -> s);
        this.owners = configuration.getList("openlineage.integration.owners", LIST_SEPARATOR, s -> s);
    }

    public static OpenLineageContext of(OpenLineage openLineage,
                                        Configuration configuration,
                                        OpenLineageJobIdentifier openLineageJobIdentifier) {
        return new OpenLineageContext(
                openLineage,
                configuration,
                openLineageJobIdentifier);
    }

    public OpenLineage getOpenLineage() {
        return openLineage;
    }

    public String getIntegrationJobDescription() {
        return description;

    }

    public List<String> getTags() {
        return tags;
    }

    public List<String> owners() {
        return owners;
    }

    public UUID getRunUuid() {
        return runUuid;
    }

    public OpenLineageJobIdentifier getJobIdentifier() {
        return jobIdentifier;
    }
}
