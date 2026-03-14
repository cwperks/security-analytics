/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.securityanalytics.commons.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.io.Serializable;

/**
 * A base model to define an IOC. Schema-specific IOC implementations are expected to extend this class
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class IOC implements Serializable {
    @NonNull
    private String id;

    @NonNull
    private String feedId;
}
