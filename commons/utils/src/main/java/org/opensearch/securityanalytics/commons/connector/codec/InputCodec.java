/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.securityanalytics.commons.connector.codec;

import org.opensearch.securityanalytics.commons.connector.exceptions.ConnectorParsingException;

import java.io.InputStream;
import java.util.function.Consumer;

/**
 * An InputCodec defines the format of data. InputCodecs are used to parse data from streams into the provided type.
 *
 * @param <T> - The class of the data being parsed
 */
public interface InputCodec<T> {
    /**
     * Parses an {@link InputStream} into the provided type and enters them into the consumer.
     *
     * @param inputStream - The input stream for code to process
     * @param consumer - A consumer of the Ts parsed from the input stream
     * @throws ConnectorParsingException - thrown when an exception was hit parsing the input stream
     */
    void parse(InputStream inputStream, Consumer<T> consumer);
}
