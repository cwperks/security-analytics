/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.securityanalytics.commons.aws;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * This class handles the connections to AWS ES internal service endpoint, to
 * fetch the temporary credentials to assume the role.
 */
public class InternalAuthCredentialsApiRequest {
    private static final Logger logger = LogManager.getLogger(InternalAuthCredentialsApiRequest.class);
    private static final InternalAwsCredentials EMPTY_CREDENTIALS = new InternalAwsCredentials();
    private String endpoint = "";
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    static {
        JSON_MAPPER.setPropertyNamingStrategy(SnakeCaseStrategy.SNAKE_CASE);
    }

    private final CloseableHttpClient httpClient;
    private final String policyType;

    public InternalAuthCredentialsApiRequest(CloseableHttpClient httpClient, String policyType, String endpoint) {
        this.httpClient = httpClient;
        this.policyType = policyType;

        assert endpoint != null && !endpoint.isEmpty();
        this.endpoint = endpoint;
    }

    public InternalAwsCredentials execute() throws IOException {
        HttpResponse response = getHttpResponse();
        validateResponseStatus(response);
        String responseString = getResponseString(response);
        return httpResponseAsCredentialsObject(responseString);
    }

    private HttpResponse getHttpResponse() {
        HttpGet internalAuthGetRequest = new HttpGet(internalAuthUri());
        return AccessController.doPrivileged((PrivilegedAction<HttpResponse>) () -> {
            try {
                return httpClient.execute(internalAuthGetRequest);
            } catch (IOException e) {
                logger.error("IOException while executing internalAuthGetRequest:", e);
                throw new RuntimeException(e);
            } catch (Exception e) {
                logger.error("Exception while executing internalAuthGetRequest:", e);
                throw e;
            }
        });
    }

    private URI internalAuthUri() {
        try {
            return new URIBuilder(endpoint)
                .addParameter("policy_id", policyType)
                .build();
        } catch (URISyntaxException exception) {
            logger.error(exception);
            throw new IllegalStateException("Error creating URI", exception);
        }
    }

    private String getResponseString(HttpResponse response) throws IOException {
        HttpEntity entity = response.getEntity();
        if (entity == null) return "{}";
        String responseString = EntityUtils.toString(entity);
        logger.debug("Internal Auth response: {}", responseString);
        return responseString;
    }

    private void validateResponseStatus(HttpResponse response) throws IOException {
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != 200) {
            logger.error("Request to internal auth failed with not OK response: {}", response);
            throw new IOException("Request to internal auth failed with not OK response");
        }
    }

    private InternalAwsCredentials httpResponseAsCredentialsObject(String responseString) {
        try {
            return JSON_MAPPER.readValue(responseString, InternalAwsCredentials.class);
        } catch (JsonParseException | JsonMappingException e) {
            logger.error("Error in parsing internal aws credentials response", e);
            return EMPTY_CREDENTIALS;
        } catch (IOException e) {
            logger.error("Error in parsing internal aws credentials response", e);
            return EMPTY_CREDENTIALS;
        }
    }
}
