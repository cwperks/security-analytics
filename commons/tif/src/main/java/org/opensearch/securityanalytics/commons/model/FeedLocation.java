package org.opensearch.securityanalytics.commons.model;

import org.opensearch.securityanalytics.commons.exceptions.InvalidFeedConfigurationException;

public enum FeedLocation {
    S3;

    public static FeedLocation fromFeedConfiguration(final FeedConfiguration feedConfiguration) {
        if (feedConfiguration.getS3ConnectorConfig() != null) {
            return FeedLocation.S3;
        }

        throw new InvalidFeedConfigurationException("Unable to determine feed location type from feed configuration");
    }
}
