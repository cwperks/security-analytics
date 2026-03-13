package org.opensearch.securityanalytics.commons.downloader;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opensearch.securityanalytics.commons.connector.Connector;
import org.opensearch.securityanalytics.commons.consumer.IOCConsumer;
import org.opensearch.securityanalytics.commons.factory.ConnectorFactory;
import org.opensearch.securityanalytics.commons.model.FeedConfiguration;
import org.opensearch.securityanalytics.commons.model.IOC;
import org.opensearch.securityanalytics.commons.util.TestHelpers;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class FeedDownloaderTests {
    private static final FeedConfiguration FEED_CONFIGURATION = TestHelpers.getFeedConfiguration();

    @Mock
    private IOCConsumer iocConsumer;
    @Mock
    private ConnectorFactory connectorFactory;
    @Mock
    private Connector<IOC> connector;

    private FeedDownloader feedDownloader;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        when(connectorFactory.create(eq(FEED_CONFIGURATION))).thenReturn(connector);

        feedDownloader = new FeedDownloader(FEED_CONFIGURATION, connectorFactory, iocConsumer);
    }

    @AfterEach
    public void tearDown() {
        verify(connectorFactory).create(eq(FEED_CONFIGURATION));
        verifyNoMoreInteractions(iocConsumer, connectorFactory, connector);
    }

    @Test
    public void testLoadIOCs() {
        feedDownloader.loadIOCs();

        verify(connector).load(eq(iocConsumer));
        verify(iocConsumer).flushIOCs();
    }
}
