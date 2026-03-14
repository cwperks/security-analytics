/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.securityanalytics.commons.connector.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.opensearch.securityanalytics.commons.connector.codec.InputCodec;
import org.opensearch.securityanalytics.commons.connector.codec.NewlineDelimitedJsonCodec;

import java.util.function.Function;

/**
 * Defines the currently supported InputCodecs and provides a reference to their constructor
 */
@Getter
@AllArgsConstructor
public enum InputCodecSchema {
    ND_JSON(NewlineDelimitedJsonCodec::new);

    private final Function<Class<?>, InputCodec<?>> inputCodecConstructor;
}
