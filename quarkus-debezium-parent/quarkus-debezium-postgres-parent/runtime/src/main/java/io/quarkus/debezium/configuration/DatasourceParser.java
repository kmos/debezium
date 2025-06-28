/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */

package io.quarkus.debezium.configuration;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DatasourceParser {

    public static final String REGEX = "jdbc:[a-z]+://(?<host>[^:/;?]+)(:(?<port>\\d+))?([/;](?<database>[^?;]+))?";
    private static final Pattern pattern = Pattern.compile(REGEX);
    private final String value;

    public DatasourceParser(String value) {
        this.value = value;
    }

    public Optional<JdbcDatasource> asString() {
        Matcher matcher = pattern.matcher(value);

        if (matcher.find()) {
            String host = matcher.group("host");
            String port = matcher.group("port");
            String database = matcher.group("database");

            return Optional.of(new JdbcDatasource(host, port, database));
        }

        return Optional.empty();
    }

    public record JdbcDatasource(String host, String port, String database) {

    }
}