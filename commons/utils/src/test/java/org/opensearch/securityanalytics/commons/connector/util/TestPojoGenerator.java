/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.securityanalytics.commons.connector.util;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TestPojoGenerator {
    private static final Random RANDOM = new Random();

    public List<TestPojo> generateTestPojo(final int count) {
        return generateTestPojo(count, i -> randomTestPojo());
    }

    public List<TestPojo> generateTestPojo(final int count, final String feedId) {
        return generateTestPojo(count, i -> randomTestPojo(feedId));
    }

    public List<TestPojo> generateTestPojo(final int count, final Function<Integer, TestPojo> generatorFunction) {
        return IntStream.range(0, count)
                .mapToObj(generatorFunction::apply)
                .collect(Collectors.toList());
    }

    public TestPojo randomTestPojo() {
        return randomTestPojo(UUID.randomUUID().toString());
    }

    public TestPojo randomTestPojo(final String fieldValue) {
        return TestPojo.builder()
                .field1(fieldValue)
                .field2(RANDOM.nextInt())
                .field3(RANDOM.nextLong())
                .build();
    }
}
