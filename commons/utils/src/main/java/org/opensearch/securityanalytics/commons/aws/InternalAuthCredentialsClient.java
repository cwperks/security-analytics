/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.securityanalytics.commons.aws;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * This class handles client configuration for AWS ES internal service calls
 */
public class InternalAuthCredentialsClient {
    private static final Logger logger = LogManager.getLogger(InternalAuthCredentialsClient.class);
    private static final int TIMEOUT_MILLISECONDS = 70000;
    private static final int SOCKET_TIMEOUT_MILLISECONDS = 70000;
    private static final CloseableHttpClient HTTP_CLIENT = createHttpClient();
    private String endpoint = "";

    public InternalAuthCredentialsClient(String endpoint) {
        this.endpoint = endpoint;
    }

    private static CloseableHttpClient createHttpClient() {
        RequestConfig config = RequestConfig.custom()
            .setConnectTimeout(TIMEOUT_MILLISECONDS)
            .setConnectionRequestTimeout(TIMEOUT_MILLISECONDS)
            .setSocketTimeout(SOCKET_TIMEOUT_MILLISECONDS)
            .build();
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setDefaultMaxPerRoute(5);
        return HttpClientBuilder.create()
            .setDefaultRequestConfig(config)
            .setConnectionManager(connectionManager)
            .setRetryHandler(new DefaultHttpRequestRetryHandler())
            .build();
    }

    public InternalAwsCredentials getAwsCredentials(String policyType) {
        try {
            InternalAwsCredentials internalAwsCredentials = getInternalAwsCredentials(policyType);
            return !internalAwsCredentials.isEmpty() ? internalAwsCredentials : null;
        } catch (IOException e) {
            logger.error("Could not fetch AWS credentials", e);
            return null;
        }
    }

    private InternalAwsCredentials getInternalAwsCredentials(String policyType) throws IOException {
        return new InternalAuthCredentialsApiRequest(HTTP_CLIENT, policyType, endpoint).execute();
    }
}
