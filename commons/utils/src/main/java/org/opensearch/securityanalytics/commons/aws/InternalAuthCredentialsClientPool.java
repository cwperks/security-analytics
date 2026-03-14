/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.securityanalytics.commons.aws;

import java.util.HashMap;
import java.util.Map;

/**
 * This class fetches credentials provider from different sources (based on priority) and uses the first one that works.
 */
public class InternalAuthCredentialsClientPool {

    private final Map<String, InternalAuthCredentialsClient> clientPool;

    private InternalAuthCredentialsClientPool() {
        clientPool = new HashMap<>();
    }

    public synchronized InternalAuthCredentialsClient getInternalAuthClient(String factoryName, String endpoint) {
        return clientPool.getOrDefault(factoryName, newClient(factoryName, endpoint));
    }

    private InternalAuthCredentialsClient newClient(String factoryName, String endpoint) {
        InternalAuthCredentialsClient client = new InternalAuthCredentialsClient(endpoint);
        clientPool.put(factoryName, client);
        return client;
    }

    public static final InternalAuthCredentialsClientPool instance = new InternalAuthCredentialsClientPool();
}
