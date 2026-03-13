package org.opensearch.securityanalytics.commons.downloader;

import org.opensearch.securityanalytics.commons.connector.Connector;
import org.opensearch.securityanalytics.commons.consumer.IOCConsumer;
import org.opensearch.securityanalytics.commons.factory.ConnectorFactory;
import org.opensearch.securityanalytics.commons.model.FeedConfiguration;
import org.opensearch.securityanalytics.commons.model.IOC;

public class FeedDownloader {
    private final Connector<IOC> connector;
    private final IOCConsumer consumer;

    public FeedDownloader(final FeedConfiguration feedConfiguration,
                          final ConnectorFactory connectorFactory,
                          final IOCConsumer consumer) {
        this.connector = connectorFactory.create(feedConfiguration);
        this.consumer = consumer;
    }

    public void loadIOCs() {
        connector.load(consumer);

        // Manually flush remaining IOCs after object download is finished
        consumer.flushIOCs();
    }
}
