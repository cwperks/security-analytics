/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.securityanalytics.commons.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * An implementation of an IOC defined in the <a href="https://oasis-open.github.io/cti-documentation/stix/intro.html">STIX2</a> format
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class STIX2 extends IOC {
    public static final String ID_FIELD = "id";
    public static final String NAME_FIELD = "name";
    public static final String TYPE_FIELD = "type";
    public static final String VALUE_FIELD = "value";
    public static final String SEVERITY_FIELD = "severity";
    public static final String CREATED_FIELD = "created";
    public static final String MODIFIED_FIELD = "modified";
    public static final String DESCRIPTION_FIELD = "description";
    public static final String LABELS_FIELD = "labels";
    public static final String SPEC_VERSION_FIELD = "spec_version";
    public static final String FEED_ID_FIELD = "feed_id";
    public static final String FEED_NAME_FIELD = "feed_name";

    private String id;
    private String name;
    private String type;

    private String value;
    private String severity;
    private Instant created;
    private Instant modified;
    private String description;
    private List<String> labels;

    @JsonProperty(SPEC_VERSION_FIELD)
    private String specVersion;

    @JsonProperty(FEED_ID_FIELD)
    private String feedId;

    @JsonProperty(FEED_NAME_FIELD)
    private String feedName;
}
