/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.securityanalytics.commons.connector.codec;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectReader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opensearch.securityanalytics.commons.connector.exceptions.ConnectorParsingException;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class NewlineDelimitedJsonCodecTests {
    @Mock
    private ObjectReader objectReader;
    @Mock
    private MappingIterator mappingIterator;
    @Mock
    private InputStream inputStream;
    @Mock
    private Consumer<String> consumer;

    private NewlineDelimitedJsonCodec<String> codec;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        this.codec = new NewlineDelimitedJsonCodec<>(objectReader);
    }

    @AfterEach
    public void teardown() {
        verifyNoMoreInteractions(objectReader, mappingIterator, inputStream, consumer);
    }

    @Test
    public void testParse() throws IOException {
        when(objectReader.readValues(eq(inputStream))).thenReturn(mappingIterator);

        codec.parse(inputStream, consumer);

        verify(objectReader).readValues(eq(inputStream));
        verify(mappingIterator).forEachRemaining(eq(consumer));
    }

    @Test
    public void testParse_ExceptionReadingValues() throws IOException {
        when(objectReader.readValues(eq(inputStream))).thenThrow(new IOException());

        assertThrows(ConnectorParsingException.class, () -> codec.parse(inputStream, consumer));

        verify(objectReader).readValues(eq(inputStream));
    }

    @Test
    public void testParse_ExceptionReadingIterator() throws IOException {
        when(objectReader.readValues(eq(inputStream))).thenReturn(mappingIterator);
        doThrow(new RuntimeException()).when(mappingIterator).forEachRemaining(eq(consumer));

        assertThrows(ConnectorParsingException.class, () -> codec.parse(inputStream, consumer));

        verify(objectReader).readValues(eq(inputStream));
        verify(mappingIterator).forEachRemaining(eq(consumer));
    }
}
