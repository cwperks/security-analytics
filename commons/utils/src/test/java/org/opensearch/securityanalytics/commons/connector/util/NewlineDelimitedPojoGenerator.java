/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.securityanalytics.commons.connector.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opensearch.securityanalytics.commons.utils.testUtils.PojoGenerator;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;

public class NewlineDelimitedPojoGenerator implements PojoGenerator {
    private final ObjectMapper objectMapper;
    private final TestPojoGenerator testPojoGenerator;

    public NewlineDelimitedPojoGenerator() {
        this.objectMapper = new ObjectMapper();
        this.testPojoGenerator = new TestPojoGenerator();
    }

    @Override
    public void write(final int numberOfPojos, final OutputStream outputStream) {
        try (final PrintWriter printWriter = new PrintWriter(outputStream)) {
            writeLines(numberOfPojos, printWriter);
        }
    }

    private void writeLines(final int numberOfPojos, final PrintWriter printWriter) {
        final List<TestPojo> testPojos = testPojoGenerator.generateTestPojo(numberOfPojos);
        testPojos.forEach(testPojo -> writeLine(testPojo, printWriter));
    }

    private void writeLine(final TestPojo testPojo, final PrintWriter printWriter) {
        try {
            final String testPojoAsString = objectMapper.writeValueAsString(testPojo);
            printWriter.write(testPojoAsString + "\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
