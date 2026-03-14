/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.securityanalytics.commons.aws;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.util.EC2MetadataUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Factory class that provides temporary credentials. It refreshes the credentials on demand.
 */
public class ExpirableCredentialsProviderFactory {
    private static final Logger logger = LogManager.getLogger(ExpirableCredentialsProviderFactory.class);
    private InternalAuthCredentialsClient internalAuthCredentialsClient;
    private List<String> clusterNameTuple;

    public ExpirableCredentialsProviderFactory(InternalAuthCredentialsClient internalAuthCredentialsClient, List<String> clusterTuple) {
        this.internalAuthCredentialsClient = internalAuthCredentialsClient;
        this.clusterNameTuple = clusterTuple;
    }

    /**
     * Provide expirable credentials.
     *
     * @param roleArn IAM role arn
     * @return AWSCredentialsProvider which holds the credentials.
     */
    public AWSCredentialsProvider getCredentialsProvider(String region, String roleArn) {
        return getExpirableCredentialsProvider(roleArn);
    }

    private AWSCredentialsProvider getExpirableCredentialsProvider(String roleArn) {
        return findStsAssumeRoleCredentialsProvider(roleArn);
    }

    private AWSCredentialsProvider findStsAssumeRoleCredentialsProvider(String roleArn) {
        AWSCredentialsProvider assumeRoleApiCredentialsProvider = getAssumeRoleApiCredentialsProvider();
        if (assumeRoleApiCredentialsProvider != null) {
            logger.info("Fetching credentials from STS for assumed role");
            return getStsAssumeCustomerRoleProvider(assumeRoleApiCredentialsProvider, roleArn);
        }
        logger.info("Could not fetch credentials from internal service to assume role");
        return null;
    }

    private AWSCredentialsProvider getAssumeRoleApiCredentialsProvider() {
        InternalAuthApiCredentialsProvider internalAuthApiCredentialsProvider = new InternalAuthApiCredentialsProvider(
                internalAuthCredentialsClient,
                InternalAuthApiCredentialsProvider.POLICY_TYPES.get("ASSUME_ROLE")
        );
        return internalAuthApiCredentialsProvider.getCredentials() != null ? internalAuthApiCredentialsProvider : null;
    }

    private AWSCredentialsProvider getStsAssumeCustomerRoleProvider(AWSCredentialsProvider apiCredentialsProvider, String roleArn) {
        String region = "us-east-1";
        try {
            region = EC2MetadataUtils.getEC2InstanceRegion();
        } catch (Exception ex) {
            logger.warn("Exception occurred while fetching the region info from EC2 metadata. Defaulting to us-east-1");
        }
        logger.debug("Region resolved to {}", region);

        ClientConfiguration configurationWithConfusedDeputyHeaders = ClientConfigurationHelper.getConfusedDeputyConfiguration(clusterNameTuple, region);
        AWSSecurityTokenServiceClientBuilder stsClientBuilder = AWSSecurityTokenServiceClientBuilder.standard()
                .withCredentials(apiCredentialsProvider)
                .withClientConfiguration(configurationWithConfusedDeputyHeaders)
                .withRegion(region);
        AWSSecurityTokenService stsClient = stsClientBuilder.build();
        STSAssumeRoleSessionCredentialsProvider.Builder providerBuilder = new STSAssumeRoleSessionCredentialsProvider.Builder(roleArn, "alerting-notification")
                .withStsClient(stsClient);
        return new PrivilegedCredentialsProvider(providerBuilder.build());
    }
}
