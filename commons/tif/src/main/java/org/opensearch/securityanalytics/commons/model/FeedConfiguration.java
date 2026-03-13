package org.opensearch.securityanalytics.commons.model;

import lombok.Data;
import org.opensearch.securityanalytics.commons.connector.model.InputCodecSchema;
import org.opensearch.securityanalytics.commons.connector.model.S3ConnectorConfig;

@Data
public class FeedConfiguration {
    private final IOCSchema iocSchema;
    private final InputCodecSchema inputCodecSchema;
    private final S3ConnectorConfig s3ConnectorConfig;
}
