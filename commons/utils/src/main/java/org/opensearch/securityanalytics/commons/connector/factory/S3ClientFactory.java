/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.securityanalytics.commons.connector.factory;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.opensearch.securityanalytics.commons.aws.ExpirableCredentialsProviderFactory;
import org.opensearch.securityanalytics.commons.aws.InternalAuthCredentialsClient;
import org.opensearch.securityanalytics.commons.aws.InternalAuthCredentialsClientPool;
import org.opensearch.securityanalytics.commons.factory.BinaryParameterCachingFactory;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;

/**
 * A factory to generate S3Clients using the credentials for the provided IAM role and region
 */
public class S3ClientFactory extends BinaryParameterCachingFactory<String, String, S3Client> {
    private final StsAssumeRoleCredentialsProviderFactory stsAssumeRoleCredentialsProviderFactory;
    private InternalAuthCredentialsClient internalApiCredentialsClient;

    public S3ClientFactory(final StsAssumeRoleCredentialsProviderFactory stsAssumeRoleCredentialsProviderFactory) {
        super();
        this.stsAssumeRoleCredentialsProviderFactory = stsAssumeRoleCredentialsProviderFactory;
    }

    public S3ClientFactory(final StsAssumeRoleCredentialsProviderFactory stsAssumeRoleCredentialsProviderFactory, String endpoint) {
        this(stsAssumeRoleCredentialsProviderFactory);
        this.internalApiCredentialsClient = InternalAuthCredentialsClientPool.instance.getInternalAuthClient(getClass().getName(), endpoint);
    }

    // TODO hurneyt refactor sa-commons to no longer use the software.amazon.awssdk dependencies.
    //  Similar to notifications plugin, only use the com.amazonaws dependencies and AmazonS3.
    @Override
    protected S3Client doCreate(final String roleArn, final String region) {
        final AwsCredentialsProvider credentialsProvider = stsAssumeRoleCredentialsProviderFactory.create(roleArn, region);
        return AccessController.doPrivileged((PrivilegedAction<S3Client>) () -> S3Client.builder()
                .credentialsProvider(credentialsProvider)
                .region(Region.of(region))
                .build());
    }

    public AmazonS3 createAmazonS3(final String roleArn, final String region, final List<String> clusterTuple) {
        final AWSCredentialsProvider credentialsProvider = new ExpirableCredentialsProviderFactory(internalApiCredentialsClient, clusterTuple)
                .getCredentialsProvider(region, roleArn);
        return AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .withCredentials(credentialsProvider)
                .build();
    }
}
