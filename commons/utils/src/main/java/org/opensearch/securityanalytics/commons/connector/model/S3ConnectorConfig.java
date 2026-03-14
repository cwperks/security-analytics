/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.securityanalytics.commons.connector.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * A model class to hold the configuration information necessary to download an object from S3
 */
@Data
@AllArgsConstructor
public class S3ConnectorConfig {
    private final String bucketName;
    private final String objectKey;
    private final String region;
    private final String roleArn;
}
