/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.securityanalytics.commons.connector.exceptions;

/**
 * An exception thrown when a connector is unable to parse data retrieved from an external store
 */
public class ConnectorParsingException extends RuntimeException {
    public ConnectorParsingException(final Throwable cause) {
        super(cause);
    }
}
