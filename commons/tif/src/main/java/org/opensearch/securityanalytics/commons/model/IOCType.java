/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.securityanalytics.commons.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.google.common.collect.ImmutableList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * A class to define the supported types of IOCs.
 */
public class IOCType {
    private static final Logger logger = LogManager.getLogger(IOCType.class);

    public static final String DOMAIN_NAME_TYPE = "domain-name";
    public static final String HASHES_TYPE = "hashes";
    public static final String IPV4_TYPE = "ipv4-addr";
    public static final String IPV6_TYPE = "ipv6-addr";

    public static final List<String> types = ImmutableList.of(
            DOMAIN_NAME_TYPE,
            HASHES_TYPE,
            IPV4_TYPE,
            IPV6_TYPE
    );

    private final String type;

    public IOCType(String type) {
        this.type = fromString(type);
    }

    /**
     * Determines whether the supplied String is a supported IOCType.
     * @param type The String to evaluate.
     * @return The formatted String if it's supported.
     * @throws IllegalArgumentException when the supplied String is not a supported type.
     */
    public static String fromString(String type) {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException(String.format(
                    "IOCType is required. Supported types are %s",
                    String.join(", ", types)));
        }

        String formattedType = type.toLowerCase(Locale.ROOT);
        if (types.contains(formattedType)) {
            return formattedType;
        } else {
            String error = String.format(
                    "Unrecognized IOCType: %s. Supported types are %s",
                    type,
                    String.join(", ", types));
            logger.warn(error);
            throw new IllegalArgumentException(error);
        }
    }

    /**
     * Determines whether the supplied String is a supported IOCType; case-sensitive.
     * @param type The String to evaluate.
     * @return TRUE when the supplied String exactly matches a supported type; else returns FALSE.
     */
    public static boolean supportedType(String type) {
        try {
            String formattedType = fromString(type);
            return formattedType.equals(type);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String toString() {
        return type;
    }

    protected static class IOCTypeDeserializer extends JsonDeserializer<IOCType> {
        private static final Logger logger = LogManager.getLogger(IOCTypeDeserializer.class);

        // No args constructor needed for S3 deserialization
        public IOCTypeDeserializer() {}

        @Override
        public IOCType deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
            try {
                String parsedType = IOCType.fromString(jsonParser.getText());
                return new IOCType(parsedType);
            } catch (Exception e) {
                logger.error("Failed to deserialize IOCType.", e);
                throw e;
            }
        }
    }
}
