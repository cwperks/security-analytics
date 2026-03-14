/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.securityanalytics.commons.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * A definition of the currently supported IOC schemas
 */
@AllArgsConstructor
@Getter
public enum IOCSchema {
    STIX2(STIX2.class);

    private final Class<? extends IOC> modelClass;
}
