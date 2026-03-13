package org.opensearch.securityanalytics.commons.factory;

import org.opensearch.securityanalytics.commons.connector.Connector;
import org.opensearch.securityanalytics.commons.connector.S3Connector;
import org.opensearch.securityanalytics.commons.connector.codec.InputCodec;
import org.opensearch.securityanalytics.commons.connector.factory.InputCodecFactory;
import org.opensearch.securityanalytics.commons.connector.factory.S3ClientFactory;
import org.opensearch.securityanalytics.commons.connector.model.S3ConnectorConfig;
import org.opensearch.securityanalytics.commons.model.FeedConfiguration;
import org.opensearch.securityanalytics.commons.model.FeedLocation;
import org.opensearch.securityanalytics.commons.model.IOC;
import software.amazon.awssdk.services.s3.S3Client;

public class ConnectorFactory extends UnaryParameterCachingFactory<FeedConfiguration, Connector<IOC>>{
    private final InputCodecFactory inputCodecFactory;
    private final S3ClientFactory s3ClientFactory;

    public ConnectorFactory(final InputCodecFactory inputCodecFactory, final S3ClientFactory s3ClientFactory) {
        this.inputCodecFactory = inputCodecFactory;
        this.s3ClientFactory = s3ClientFactory;
    }

    public Connector<IOC> doCreate(final FeedConfiguration feedConfiguration) {
        final FeedLocation feedLocation = FeedLocation.fromFeedConfiguration(feedConfiguration);

        switch(feedLocation) {
            case S3: return createS3Connector(feedConfiguration);
            default: throw new IllegalArgumentException("Unsupported feedLocation: " + feedLocation);
        }
    }

    private S3Connector<IOC> createS3Connector(final FeedConfiguration feedConfiguration) {
        final S3ConnectorConfig s3ConnectorConfig = feedConfiguration.getS3ConnectorConfig();
        final S3Client s3Client = s3ClientFactory.create(s3ConnectorConfig.getRoleArn(), s3ConnectorConfig.getRegion());
        final InputCodec<IOC> inputCodec = (InputCodec<IOC>) inputCodecFactory.create(feedConfiguration.getIocSchema().getModelClass(),
                feedConfiguration.getInputCodecSchema());

        return new S3Connector<>(s3ConnectorConfig, s3Client, inputCodec);
    }
}
