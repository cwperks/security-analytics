package org.opensearch.securityanalytics.commons.factory;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opensearch.securityanalytics.commons.connector.Connector;
import org.opensearch.securityanalytics.commons.connector.S3Connector;
import org.opensearch.securityanalytics.commons.connector.codec.InputCodec;
import org.opensearch.securityanalytics.commons.connector.factory.InputCodecFactory;
import org.opensearch.securityanalytics.commons.connector.factory.S3ClientFactory;
import org.opensearch.securityanalytics.commons.connector.model.InputCodecSchema;
import org.opensearch.securityanalytics.commons.connector.model.S3ConnectorConfig;
import org.opensearch.securityanalytics.commons.exceptions.InvalidFeedConfigurationException;
import org.opensearch.securityanalytics.commons.model.FeedConfiguration;
import org.opensearch.securityanalytics.commons.model.IOC;
import org.opensearch.securityanalytics.commons.model.IOCSchema;
import org.opensearch.securityanalytics.commons.util.TestHelpers;
import software.amazon.awssdk.services.s3.S3Client;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class ConnectorFactoryTests {
    @Mock
    private S3ClientFactory s3ClientFactory;
    @Mock
    private S3Client s3Client;
    @Mock
    private InputCodecFactory inputCodecFactory;
    @Mock
    private InputCodec inputCodec;

    private ConnectorFactory connectorFactory;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        connectorFactory = new ConnectorFactory(inputCodecFactory, s3ClientFactory);
    }

    @AfterEach
    public void teardown() {
        verifyNoMoreInteractions(s3ClientFactory, inputCodecFactory, s3Client);
    }

    @Test
    public void testDoCreate_S3Connector() {
        final FeedConfiguration feedConfiguration = TestHelpers.getFeedConfiguration();
        final S3ConnectorConfig s3ConnectorConfig = feedConfiguration.getS3ConnectorConfig();

        when(s3ClientFactory.create(eq(s3ConnectorConfig.getRoleArn()), eq(s3ConnectorConfig.getRegion()))).thenReturn(s3Client);
        when(inputCodecFactory.create(eq(feedConfiguration.getIocSchema().getModelClass()), eq(feedConfiguration.getInputCodecSchema())))
                .thenReturn(inputCodec);

        final Connector<IOC> result = connectorFactory.create(feedConfiguration);
        assertInstanceOf(S3Connector.class, result);

        verify(s3ClientFactory).create(eq(s3ConnectorConfig.getRoleArn()), eq(s3ConnectorConfig.getRegion()));
        verify(inputCodecFactory).create(eq(feedConfiguration.getIocSchema().getModelClass()), eq(feedConfiguration.getInputCodecSchema()));
    }

    @Test
    public void testDoCreate_InvalidFeedLocation() {
        final FeedConfiguration feedConfiguration = new FeedConfiguration(IOCSchema.STIX2, InputCodecSchema.ND_JSON, null);

        assertThrows(InvalidFeedConfigurationException.class, () -> connectorFactory.create(feedConfiguration));
    }
}
