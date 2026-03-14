package org.opensearch.securityanalytics.commons.util;

import org.opensearch.securityanalytics.commons.connector.model.InputCodecSchema;
import org.opensearch.securityanalytics.commons.connector.model.S3ConnectorConfig;
import org.opensearch.securityanalytics.commons.model.FeedConfiguration;
import org.opensearch.securityanalytics.commons.model.IOCSchema;

import java.util.UUID;

public class TestHelpers {
    public static FeedConfiguration getFeedConfiguration() {
        return new FeedConfiguration(
                IOCSchema.STIX2,
                InputCodecSchema.ND_JSON,
                getS3ConnectorConfig()
        );
    }

    public static S3ConnectorConfig getS3ConnectorConfig() {
        return new S3ConnectorConfig(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString()
        );
    }
}
