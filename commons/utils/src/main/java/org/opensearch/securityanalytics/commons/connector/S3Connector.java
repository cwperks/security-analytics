/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.securityanalytics.commons.connector;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.securityanalytics.commons.connector.codec.InputCodec;
import org.opensearch.securityanalytics.commons.connector.exceptions.ConnectorParsingException;
import org.opensearch.securityanalytics.commons.connector.model.S3ConnectorConfig;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.function.Consumer;

/**
 * An implementation of a Connector<T> to fetch data from an S3 object
 *
 * @param <T> - The class of objects within the S3 object
 */
public class S3Connector<T> implements Connector<T> {
    private static final Logger logger = LogManager.getLogger(S3Connector.class);

    public static final long MAX_SIZE_MB = 300;
    public static final long MAX_SIZE_BYTES = MAX_SIZE_MB * 1024 * 1024; // Max size in bytes
    public static final String MAX_CONTENT_ERROR = String.format("The bucket object content exceeds the max size of %s MB.", MAX_SIZE_MB);
    public static final ConnectorParsingException MAX_CONTENT_EXCEPTION =
            new ConnectorParsingException(new IllegalArgumentException(MAX_CONTENT_ERROR));

    private final S3ConnectorConfig s3ConnectorConfig;
    private S3Client s3Client;
    private AmazonS3 amazonS3;
    private final InputCodec<T> inputCodec;

    public S3Connector(final S3ConnectorConfig s3ConnectorConfig, final S3Client s3Client, final InputCodec<T> inputCodec) {
        this.s3ConnectorConfig = s3ConnectorConfig;
        this.s3Client = s3Client;
        this.inputCodec = inputCodec;
    }

    public S3Connector(final S3ConnectorConfig s3ConnectorConfig, final AmazonS3 amazonS3, final InputCodec<T> inputCodec) {
        this.s3ConnectorConfig = s3ConnectorConfig;
        this.amazonS3 = amazonS3;
        this.inputCodec = inputCodec;
    }

    @Override
    public void load(final Consumer<T> consumer) {
        if (s3Client != null) {
            s3ClientLoad(consumer);
        } else if (amazonS3 != null) {
            amazons3Load(consumer);
        } else {
            logger.error("No client available to call S3.");
            throw new IllegalArgumentException("No client available to call S3.");
        }
    }

    private void s3ClientLoad(final Consumer<T> consumer) {
        try {
            final ResponseInputStream<GetObjectResponse> response = AccessController.doPrivileged((PrivilegedAction<ResponseInputStream<GetObjectResponse>>) () -> {
                try {
                    final GetObjectRequest getObjectRequest = getObjectRequest();
                    return s3Client.getObject(getObjectRequest);
                } catch (Exception e) {
                    logger.error("Failed GetObjectRequest to S3.", e);
                    throw e;
                }
            });

            long contentSize = response.response().contentLength();
            if (contentSize > MAX_SIZE_BYTES) {
                logger.debug(String.format("%s Content size is %s bytes.", MAX_SIZE_MB, contentSize));
                throw MAX_CONTENT_EXCEPTION;
            }

            try {
                inputCodec.parse(response, consumer);
            } catch (Exception e) {
                logger.error("Failed to parse S3 GetObjectResponse.", e);
                throw e;
            }
        } catch (Exception e) {
            logger.error("Failed to execute privileged GetObjectRequest to S3.", e);
            throw e;
        }
    }

    private void amazons3Load(final Consumer<T> consumer) {
        try {
            final S3Object response = AccessController.doPrivileged((PrivilegedAction<S3Object>) () -> {
                try {
                    final com.amazonaws.services.s3.model.GetObjectRequest getObjectRequest =
                            new com.amazonaws.services.s3.model.GetObjectRequest(s3ConnectorConfig.getBucketName(), s3ConnectorConfig.getObjectKey());
                    return amazonS3.getObject(getObjectRequest);
                } catch (Exception e) {
                    logger.error("Failed AmazonS3 getObject request.", e);
                    throw e;
                }
            });

            long contentSize = response.getObjectMetadata().getContentLength();
            if (contentSize > MAX_SIZE_BYTES) {
                logger.debug(String.format("%s Content size is %s bytes.", MAX_SIZE_MB, contentSize));
                throw MAX_CONTENT_EXCEPTION;
            }

            try {
                inputCodec.parse(response.getObjectContent(), consumer);
            } catch (Exception e) {
                logger.error("Failed to parse AmazonS3 getObject response.", e);
                throw e;
            }
        } catch (Exception e) {
            logger.error("Failed to execute privileged AmazonS3 getObject request.", e);
            throw e;
        }
    }

    /**
     * Makes a call to the object in the bucket specified by the user without downloading the contents of the object.
     * For reference, this is the official documentation for the 'headObject' function:
     * <a href="https://sdk.amazonaws.com/java/api/latest/software/amazon/awssdk/services/s3/S3Client.html#headObject(software.amazon.awssdk.services.s3.model.HeadObjectRequest)">...</a>
     * @param s3ConnectorConfig The config containing the bucketName, and objectKey.
     * @return <HeadObjectResponse>
     */
    public HeadObjectResponse testS3Connection(S3ConnectorConfig s3ConnectorConfig) {
        try {
            return AccessController.doPrivileged((PrivilegedAction<HeadObjectResponse>) () -> {
                try {
                    HeadObjectRequest request = HeadObjectRequest.builder()
                            .bucket(s3ConnectorConfig.getBucketName())
                            .key(s3ConnectorConfig.getObjectKey())
                            .build();
                    return s3Client.headObject(request);
                } catch (Exception e) {
                    logger.error("Failed HeadObjectRequest to S3.", e);
                    throw e;
                }
            });
        } catch (Exception e) {
            logger.error("Failed to execute privileged HeadObjectRequest to S3.", e);
            throw e;
        }
    }

    public boolean testAmazonS3Connection(S3ConnectorConfig s3ConnectorConfig) {
        try {
            return AccessController.doPrivileged((PrivilegedAction<Boolean>) () -> {
                try {
                    return amazonS3.doesObjectExist(s3ConnectorConfig.getBucketName(), s3ConnectorConfig.getObjectKey());
                } catch (Exception e) {
                    logger.error("Failed doesObjectExist to AmazonS3.", e);
                    throw e;
                }
            });
        } catch (Exception e) {
            logger.error("Failed to execute privileged doesObjectExist to AmazonS3.", e);
            throw e;
        }
    }

    private GetObjectRequest getObjectRequest() {
        return GetObjectRequest.builder()
                .bucket(s3ConnectorConfig.getBucketName())
                .key(s3ConnectorConfig.getObjectKey())
                .build();
    }
}
