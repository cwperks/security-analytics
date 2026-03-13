/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.securityanalytics.commons.aws;

import com.amazonaws.ClientConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class ClientConfigurationHelper {

    private static final String SOURCE_ACCOUNT_HEADER = "x-amz-source-account";
    private static final String SOURCE_ARN_HEADER = "x-amz-source-arn";
    private static final String OS_DOMAIN_ARN_FORMAT = "arn:%s:es:%s:%s:domain/%s";
    private static final Logger logger = LogManager.getLogger(ClientConfigurationHelper.class);

    public static ClientConfiguration getConfusedDeputyConfiguration(List<String> clusterNameTuple, String region) {
        String clientId = clusterNameTuple.get(0);
        String domainArn = generateDomainArn(clusterNameTuple, region);

        // Confused Deputy Protection Requirement
        // https://w.amazon.com/bin/view/AWSAuth/AccessManagement/Resource_Policy_Confused_Deputy_Protection
        logger.debug("Adding Source ARN {} and Source Account {} in request headers for Confused Deputy Protection", domainArn, clientId);
        return new ClientConfiguration().withHeader(SOURCE_ARN_HEADER, domainArn)
                .withHeader(SOURCE_ACCOUNT_HEADER, clientId);
    }

    private static String generateDomainArn(List<String> clusterNameTuple, String region) {
        String partition = getPartition(region);
        return String.format(OS_DOMAIN_ARN_FORMAT, partition, region, clusterNameTuple.get(0), clusterNameTuple.get(1));
    }

    private static String getPartition(String region) {
        String partition = System.getenv("DOMAIN_PARTITION");
        if (partition != null && !partition.isEmpty()) {
            return partition;
        }
        logger.warn("Domain Partition is missing from environment variable, assuming partition on the basis of current region");
        if (region.contains("gov")) {
            return "aws-us-gov";
        }
        if (region.contains("-isob-")) {
            return "aws-iso-b";
        }
        if (region.contains("-iso-")) {
            return "aws-iso";
        }
        return region.startsWith("cn-") ? "aws-cn" : "aws";
    }
}
