/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.securityanalytics.commons.connector.factory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensearch.securityanalytics.commons.connector.codec.NewlineDelimitedJsonCodec;
import org.opensearch.securityanalytics.commons.connector.model.InputCodecSchema;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class InputCodecFactoryTests {
    private InputCodecFactory inputCodecFactory;

    @BeforeEach
    public void setup() {
        inputCodecFactory = new InputCodecFactory();
    }

    @Test
    public void testDoCreate_ND_JSON() {
        assertInstanceOf(NewlineDelimitedJsonCodec.class, inputCodecFactory.doCreate(Object.class, InputCodecSchema.ND_JSON));
    }
}
