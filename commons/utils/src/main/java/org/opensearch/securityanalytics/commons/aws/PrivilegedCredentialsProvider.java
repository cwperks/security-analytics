/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.securityanalytics.commons.aws;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;

/**
 * This class helps in fetching the credentials by making socket connections in
 * privileged mode.
 */
public class PrivilegedCredentialsProvider implements AWSCredentialsProvider {
    private final AWSCredentialsProvider credentials;

    public PrivilegedCredentialsProvider(AWSCredentialsProvider credentials) {
        this.credentials = credentials;
    }

    @Override
    public AWSCredentials getCredentials() {
        return SocketAccess.doPrivileged(credentials::getCredentials);
    }

    @Override
    public void refresh() {
        SocketAccess.doPrivilegedVoid(credentials::refresh);
    }

    public AWSCredentialsProvider wrappedProvider() {
        return credentials;
    }
}
