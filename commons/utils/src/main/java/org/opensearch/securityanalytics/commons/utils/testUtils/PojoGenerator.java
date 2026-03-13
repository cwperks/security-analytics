/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.securityanalytics.commons.utils.testUtils;

import java.io.IOException;
import java.io.OutputStream;

public interface PojoGenerator {
    void write(int numberOfPojos, OutputStream outputStream) throws IOException;
}
