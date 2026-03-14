/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.securityanalytics.commons.connector.factory;

import org.opensearch.securityanalytics.commons.factory.UnaryParameterCachingFactory;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sts.StsClient;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * A factory used to generate StsClients for the given region
 */
public class StsClientFactory extends UnaryParameterCachingFactory<String, StsClient> {
    @Override
    protected StsClient doCreate(final String region) {
        return AccessController.doPrivileged((PrivilegedAction<StsClient>) () -> StsClient.builder()
                .region(Region.of(region))
                .build());
    }
}
