/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.securityanalytics.commons.connector.codec;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import com.google.common.annotations.VisibleForTesting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.securityanalytics.commons.connector.exceptions.ConnectorParsingException;

import java.io.IOException;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.function.Consumer;

/**
 * An implementation of InputCodec<T> used to parse objects from an input stream where each line is its own object
 *
 * @param <T> - The class of the objects being parsed
 */
public class NewlineDelimitedJsonCodec<T> implements InputCodec<T> {
    private static final Logger logger = LogManager.getLogger(NewlineDelimitedJsonCodec.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new AfterburnerModule())
            .registerModule(new JavaTimeModule());
    private final ObjectReader objectReader;

    public NewlineDelimitedJsonCodec(final Class<T> clazz) {
        this.objectReader = OBJECT_MAPPER.readerFor(clazz);
    }

    @VisibleForTesting
    NewlineDelimitedJsonCodec(final ObjectReader objectReader) {
        this.objectReader = objectReader;
    }

    @Override
    public void parse(final InputStream inputStream, final Consumer<T> consumer) {
        try {
            final MappingIterator<T> mappingIterator = AccessController.doPrivileged((PrivilegedAction<MappingIterator<T>>) () -> {
                try {
                    return objectReader.readValues(inputStream);
                } catch (IOException e) {
                    logger.error("Failed to read values from stream.", e);
                    throw new RuntimeException(e);
                }
            });
            mappingIterator.forEachRemaining(consumer);
        } catch (final Exception e) {
            logger.error("Failed to parse input stream.", e);
            throw new ConnectorParsingException(e);
        }
    }
}
