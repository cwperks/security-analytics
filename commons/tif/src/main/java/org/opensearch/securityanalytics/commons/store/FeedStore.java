package org.opensearch.securityanalytics.commons.store;

import org.opensearch.securityanalytics.commons.model.IOC;
import org.opensearch.securityanalytics.commons.model.UpdateAction;

import java.util.Map;

public interface FeedStore {
    /**
     * Accepts a Map of actions to IOCs and applies the actions to the local feed store
     *
     * @param actionToIOCs - A map of the IOC to the action to perform
     */
    void storeIOCs(Map<IOC, UpdateAction> actionToIOCs);
}
