/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.securityanalytics.commons.connector.factory;

import org.opensearch.securityanalytics.commons.connector.codec.InputCodec;
import org.opensearch.securityanalytics.commons.connector.model.InputCodecSchema;
import org.opensearch.securityanalytics.commons.factory.BinaryParameterCachingFactory;

/**
 * A factory to generate InputCodecs for the provided object class and schema
 */
public class InputCodecFactory extends BinaryParameterCachingFactory<Class<?>, InputCodecSchema, InputCodec<?>> {
    @Override
    public InputCodec<?> doCreate(final Class<?> clazz, final InputCodecSchema inputCodecSchema) {
        return inputCodecSchema.getInputCodecConstructor().apply(clazz);
    }
}
