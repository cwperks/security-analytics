package org.opensearch.securityanalytics.commons.model;

/**
 * The type of action to apply. Usually paired with an IOC when updating the local feed store.
 */
public enum UpdateAction {
    UPSERT,
    DELETE
}
