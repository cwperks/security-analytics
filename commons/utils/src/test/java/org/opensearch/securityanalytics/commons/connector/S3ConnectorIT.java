/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.securityanalytics.commons.connector;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.opensearch.securityanalytics.commons.connector.codec.InputCodec;
import org.opensearch.securityanalytics.commons.connector.factory.InputCodecFactory;
import org.opensearch.securityanalytics.commons.connector.model.InputCodecSchema;
import org.opensearch.securityanalytics.commons.connector.model.S3ConnectorConfig;
import org.opensearch.securityanalytics.commons.connector.util.NewlineDelimitedPojoGenerator;
import org.opensearch.securityanalytics.commons.utils.testUtils.S3ObjectGenerator;
import org.opensearch.securityanalytics.commons.connector.util.TestPojo;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Integration test class for the S3 connector. The following system parameters must be specified to successfully run the tests:
 *
 * tests.s3connector.bucket - the name of the S3 bucket to use for the tests
 * tests.s3connector.region - the AWS region of the S3 bucket
 * tests.s3connector.roleArn - the IAM role ARN to assume when making S3 calls
 *
 * The local system must have sufficient credentials to write to S3, delete from S3, and assume the provided role.
 *
 * The tests are disabled by default as there is no default value for the tests.s3connector.bucket system property. This is
 * intentional as the tests will fail when run without the proper setup, such as during CI workflows.
 *
 * Example command to manually run this class's ITs:
 * ./gradlew :utils:s3ConnectorIT -Dtests.s3connector.bucket=<BUCKET_NAME> -Dtests.s3connector.region=<REGION> -Dtests.s3connector.roleArn=<ROLE_ARN>
 */
@EnabledIfSystemProperty(named = "tests.s3connector.bucket", matches = ".+")
public class S3ConnectorIT {
    private static final int NUMBER_OF_TEST_POJOS = new Random().nextInt(100);

    private S3Client s3Client;
    private S3ObjectGenerator s3ObjectGenerator;
    private TestConsumer consumer;
    private String bucket;
    private String region;
    private String roleArn;

    @BeforeEach
    public void setup() {
        region = System.getProperty("tests.s3connector.region");
        roleArn = System.getProperty("tests.s3connector.roleArn");
        bucket = System.getProperty("tests.s3connector.bucket");

        s3Client = S3Client.builder()
                .region(Region.of(region))
                .build();
        s3ObjectGenerator = new S3ObjectGenerator(s3Client, bucket);
        consumer = new TestConsumer();
    }

    private S3Connector<TestPojo> createS3Connector(final S3ConnectorConfig s3ConnectorConfig) {
        final InputCodecFactory inputCodecFactory = new InputCodecFactory();
        final InputCodec<TestPojo> inputCodec = (InputCodec<TestPojo>) inputCodecFactory.create(TestPojo.class, InputCodecSchema.ND_JSON);

        return new S3Connector<>(s3ConnectorConfig, s3Client, inputCodec);
    }

    @Test
    public void testS3Connector_load_Success() throws IOException {
        final String objectKey = UUID.randomUUID().toString();
        s3ObjectGenerator.write(NUMBER_OF_TEST_POJOS, objectKey, new NewlineDelimitedPojoGenerator());

        final S3ConnectorConfig s3ConnectorConfig = new S3ConnectorConfig(
                bucket,
                objectKey,
                region,
                roleArn
        );
        final S3Connector<TestPojo> s3Connector = createS3Connector(s3ConnectorConfig);

        s3Connector.load(consumer);
        assertEquals(NUMBER_OF_TEST_POJOS, consumer.getTestPojos().size());

        deleteObject(objectKey);
    }

    @Test
    public void testS3Connector_load_BucketDoesNotExist() {
        final String objectKey = UUID.randomUUID().toString();
        final S3ConnectorConfig s3ConnectorConfig = new S3ConnectorConfig(
                UUID.randomUUID().toString(),
                objectKey,
                region,
                roleArn
        );
        final S3Connector<TestPojo> s3Connector = createS3Connector(s3ConnectorConfig);

        assertThrows(NoSuchBucketException.class, () -> s3Connector.load(consumer));
    }

    @Test
    public void testS3Connector_load_ObjectDoesNotExist() {
        final S3ConnectorConfig s3ConnectorConfig = new S3ConnectorConfig(
                bucket,
                UUID.randomUUID().toString(),
                region,
                roleArn
        );
        final S3Connector<TestPojo> s3Connector = createS3Connector(s3ConnectorConfig);

        assertThrows(NoSuchKeyException.class, () -> s3Connector.load(consumer));
    }

    @Test
    public void testS3Connector_testS3Connection_Success() throws IOException {
        final String objectKey = UUID.randomUUID().toString();

        // Only adding 1 pojo to the bucket object as the 'testS3Connection' function doesn't actually
        // retrieve the contents of the bucket object. We just need the bucket object to be created for the test.
        s3ObjectGenerator.write(1, objectKey, new NewlineDelimitedPojoGenerator());
        final S3ConnectorConfig s3ConnectorConfig = new S3ConnectorConfig(
                bucket,
                objectKey,
                region,
                roleArn
        );
        final S3Connector<TestPojo> s3Connector = createS3Connector(s3ConnectorConfig);

        HeadObjectResponse response = s3Connector.testS3Connection(s3ConnectorConfig);
        assertEquals(200, response.sdkHttpResponse().statusCode());
    }

    @Test
    public void testS3Connector_testS3Connection_ObjectDoesNotExist() {
        final S3ConnectorConfig s3ConnectorConfig = new S3ConnectorConfig(
                bucket,
                "fakeobjectkey",
                region,
                roleArn
        );
        final S3Connector<TestPojo> s3Connector = createS3Connector(s3ConnectorConfig);
        assertThrows(NoSuchKeyException.class, () -> s3Connector.testS3Connection(s3ConnectorConfig));
    }

    @Test
    public void testS3Connector_testS3Connection_BucketDoesNotExist() {
        final S3ConnectorConfig s3ConnectorConfig = new S3ConnectorConfig(
                "fakebucket",
                UUID.randomUUID().toString(),
                region,
                roleArn
        );
        final S3Connector<TestPojo> s3Connector = createS3Connector(s3ConnectorConfig);
        assertThrows(S3Exception.class, () -> s3Connector.testS3Connection(s3ConnectorConfig));
    }

    private void deleteObject(final String objectKey) {
        final DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .build();
        s3Client.deleteObject(deleteObjectRequest);
    }

    private static class TestConsumer implements Consumer<TestPojo> {
        private final List<TestPojo> testPojos;

        public TestConsumer() {
            this.testPojos = new ArrayList<>();
        }

        @Override
        public void accept(final TestPojo testPojo) {
            testPojos.add(testPojo);
        }

        public List<TestPojo> getTestPojos() {
            return testPojos;
        }
    }
}
