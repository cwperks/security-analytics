/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.securityanalytics.commons.connector;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opensearch.securityanalytics.commons.connector.codec.InputCodec;
import org.opensearch.securityanalytics.commons.connector.exceptions.ConnectorParsingException;
import org.opensearch.securityanalytics.commons.connector.model.S3ConnectorConfig;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class S3ConnectorTests {
    private static final String BUCKET_NAME = UUID.randomUUID().toString();
    private static final String OBJECT_KEY = UUID.randomUUID().toString();
    private static final String REGION = UUID.randomUUID().toString();
    private static final String ROLE_ARN = UUID.randomUUID().toString();
    private static final S3ConnectorConfig S3_CONNECTOR_CONFIG = new S3ConnectorConfig(
            BUCKET_NAME,
            OBJECT_KEY,
            REGION,
            ROLE_ARN
    );

    @Mock
    private S3Client s3Client;
    @Mock
    private InputCodec<String> inputCodec;
    @Mock
    private ResponseInputStream<GetObjectResponse> responseInputStream;
    @Mock
    private GetObjectResponse getObjectResponse;
    @Mock
    private Consumer<String> consumer;

    private S3Connector<String> s3Connector;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        this.s3Connector = new S3Connector<>(S3_CONNECTOR_CONFIG, s3Client, inputCodec);
    }

    @AfterEach
    public void tearDown() {
        verifyNoMoreInteractions(s3Client, inputCodec, consumer);
    }

    @Test
    public void testLoadIOCs() {
        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(responseInputStream);
        when(responseInputStream.response()).thenReturn(getObjectResponse);
        when(getObjectResponse.contentLength()).thenReturn(S3Connector.MAX_SIZE_BYTES);

        s3Connector.load(consumer);

        final ArgumentCaptor<GetObjectRequest> argumentCaptor = ArgumentCaptor.forClass(GetObjectRequest.class);
        verify(s3Client).getObject(argumentCaptor.capture());
        verify(inputCodec).parse(eq(responseInputStream), eq(consumer));

        assertEquals(OBJECT_KEY, argumentCaptor.getValue().key());
        assertEquals(BUCKET_NAME, argumentCaptor.getValue().bucket());
    }

    @Test
    public void testLoadIOCs_ExceptionGettingObject() {
        when(s3Client.getObject(any(GetObjectRequest.class))).thenThrow(new RuntimeException());

        assertThrows(RuntimeException.class, () -> s3Connector.load(consumer));

        final ArgumentCaptor<GetObjectRequest> argumentCaptor = ArgumentCaptor.forClass(GetObjectRequest.class);
        verify(s3Client).getObject(argumentCaptor.capture());

        assertEquals(OBJECT_KEY, argumentCaptor.getValue().key());
        assertEquals(BUCKET_NAME, argumentCaptor.getValue().bucket());
    }

    @Test
    public void testLoadIOCs_ExceptionParsingObject() {
        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(responseInputStream);
        when(responseInputStream.response()).thenReturn(getObjectResponse);
        when(getObjectResponse.contentLength()).thenReturn(S3Connector.MAX_SIZE_BYTES);
        doThrow(new RuntimeException()).when(inputCodec).parse(eq(responseInputStream), eq(consumer));

        assertThrows(RuntimeException.class, () -> s3Connector.load(consumer));

        final ArgumentCaptor<GetObjectRequest> argumentCaptor = ArgumentCaptor.forClass(GetObjectRequest.class);
        verify(s3Client).getObject(argumentCaptor.capture());
        verify(inputCodec).parse(eq(responseInputStream), eq(consumer));

        assertEquals(OBJECT_KEY, argumentCaptor.getValue().key());
        assertEquals(BUCKET_NAME, argumentCaptor.getValue().bucket());
    }

    @Test
    public void testLoadIOCs_ExceptionObjectMaxSize() {
        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(responseInputStream);
        when(responseInputStream.response()).thenReturn(getObjectResponse);
        when(getObjectResponse.contentLength()).thenReturn(S3Connector.MAX_SIZE_BYTES + 1);

        assertThrows(ConnectorParsingException.class, () -> s3Connector.load(consumer));

        final ArgumentCaptor<GetObjectRequest> argumentCaptor = ArgumentCaptor.forClass(GetObjectRequest.class);
        verify(s3Client).getObject(argumentCaptor.capture());

        assertEquals(OBJECT_KEY, argumentCaptor.getValue().key());
        assertEquals(BUCKET_NAME, argumentCaptor.getValue().bucket());
    }
}
