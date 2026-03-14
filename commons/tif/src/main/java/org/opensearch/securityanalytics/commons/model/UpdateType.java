/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.securityanalytics.commons.model;

/**
 * The type of update to apply to the local feed store when updating the IOC set
 */
public enum UpdateType {
    /**
     * The provided IOCs should be considered the entire set of IOCs in the feed. FeedStores are expected to purge any
     * existing IOCs for this feed in favor of the received set
     */
    REPLACE,
    /**
     * The provided IOCs should be considered a delta from the current state of the feed store. Any conflicts should be resolved
     * by updating the feed store with the new definition of a given IOC
     */
    DELTA
}
