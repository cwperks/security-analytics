/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.securityanalytics.commons.connector;

import org.opensearch.securityanalytics.commons.connector.exceptions.ConnectorParsingException;

import java.util.function.Consumer;

/**
 * An interface to facilitate retrieving data from an external storage system. The Connector loads the external data as the
 * parametrized type and then passes it to the provided consumer.
 *
 * @param <T> - The class of the data being retrieved
 */
public interface Connector<T> {
    /**
     * Loads a list of Ts from a storage system
     *
     * @param consumer - a consumer that process the retrieved Ts
     * @throws ConnectorParsingException - thrown when an exception is encountered parsing the data from the storage system
     */
    void load(Consumer<T> consumer);
}
