/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.securityanalytics.commons.aws;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;

import java.util.HashMap;
import java.util.Map;

/**
 * This class fetches credentials to assume role by making AWS ES internal service call
 */
public class InternalAuthApiCredentialsProvider implements AWSCredentialsProvider {
    private final InternalAuthCredentialsClient internalApiCredentialsClient;
    private final String policyType;
    private AWSCredentials awsCredentials;
    private long expiryTimestamp;

    public static final Map<String, String> POLICY_TYPES;

    static {
        POLICY_TYPES = new HashMap<>();
        POLICY_TYPES.put("ASSUME_ROLE", "AR");
    }

    public InternalAuthApiCredentialsProvider(InternalAuthCredentialsClient internalApiCredentialsClient, String policyType) {
        this.internalApiCredentialsClient = internalApiCredentialsClient;
        this.policyType = policyType;
        this.awsCredentials = null;
        this.expiryTimestamp = 0;
    }

    /**
     * Gets the expiry timestamp of the temporary credentials
     *
     * @return expiry timestamp
     */
    public long getExpiryTimestamp() {
        return expiryTimestamp;
    }

    /**
     * Fetches credentials. It refreshes the credentials if expired
     *
     * @return AWSCredentials
     */
    @Override
    public AWSCredentials getCredentials() {
        if (credentialsHaveExpired()) {
            refresh();
        }
        return awsCredentials;
    }

    /**
     * Refreshes credentials
     */
    @Override
    public synchronized void refresh() {
        if (!credentialsHaveExpired()) {
            return;
        }
        InternalAwsCredentials apiCredentials = internalApiCredentialsClient.getAwsCredentials(policyType);
        if (apiCredentials == null) {
            resetCredentials();
        } else {
            awsCredentials = new BasicSessionCredentials(
                    apiCredentials.getAccessKey(),
                    apiCredentials.getSecretKey(),
                    apiCredentials.getSessionToken()
            );
            // subtracting 10 seconds to give the buffer for the requests handled on the boundary
            expiryTimestamp = apiCredentials.getExpiry() - 10000;
        }
    }

    private boolean credentialsHaveExpired() {
        return awsCredentials == null || System.currentTimeMillis() > expiryTimestamp;
    }

    private void resetCredentials() {
        awsCredentials = null;
        expiryTimestamp = 0;
    }
}
