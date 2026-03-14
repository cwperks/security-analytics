package org.opensearch.securityanalytics.commons.consumer;

import com.google.common.annotations.VisibleForTesting;
import org.opensearch.securityanalytics.commons.model.IOC;
import org.opensearch.securityanalytics.commons.model.UpdateAction;
import org.opensearch.securityanalytics.commons.model.UpdateType;
import org.opensearch.securityanalytics.commons.store.FeedStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * This class creates the queue of [IOC]s to be stored. Once compiled, the 'flushIOCs' method can be called to execute the store function configured in the [FeedStore].
 * This class buffers the IOCs as they are downloaded, so they can be flushed in batches.
 */
public class IOCConsumer implements Consumer<IOC> {
    private final LinkedBlockingQueue<IOC> queue;
    private final FeedStore feedStore;
    private final UpdateType updateType;

    public IOCConsumer(final int batchSize, final FeedStore feedStore, final UpdateType updateType) {
        this.queue = new LinkedBlockingQueue<>(batchSize);
        this.feedStore = feedStore;
        this.updateType = updateType;
    }

    @VisibleForTesting
    IOCConsumer(final LinkedBlockingQueue<IOC> queue, final FeedStore feedStore, final UpdateType updateType) {
        this.queue = queue;
        this.feedStore = feedStore;
        this.updateType = updateType;
    }

    @Override
    public void accept(final IOC ioc) {
        if (queue.offer(ioc)) {
            return;
        }

        flushIOCs();
        queue.offer(ioc);
    }

    public void flushIOCs() {
        if (queue.isEmpty()) {
            return;
        }

        final List<IOC> iocsToFlush = new ArrayList<>(queue.size());
        queue.drainTo(iocsToFlush);

        final Map<IOC, UpdateAction> iocToActions = buildIOCToActions(iocsToFlush);
        feedStore.storeIOCs(iocToActions);
    }

    private Map<IOC, UpdateAction> buildIOCToActions(final List<IOC> iocs) {
        switch (updateType) {
            case REPLACE: return mapIOCsToUpsertAction(iocs);
            case DELTA: return buildDeltaActions(iocs);
            default: throw new IllegalArgumentException("Invalid update type: " + updateType);
        }
    }

    /**
     * Maps each [IOC] to the [UPSERT] [UpdateAction].
     * Upserting will replace the IOC if it exists, and otherwise add the IOC.
     * @param iocs [IOC]s to be upserted.
     * @return The inputted IOCs mapped to the upsert action.
     */
    private Map<IOC, UpdateAction> mapIOCsToUpsertAction(final List<IOC> iocs) {
        return iocs.stream()
                .map(ioc -> Map.entry(ioc, UpdateAction.UPSERT))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Maps each [IOC] to action that will indicate that the existing IOCs should be
     * updated with values from the new IOCs.
     * @param iocs [IOC]s to be updated.
     * @return The inputted IOCs mapped to the update action.
     */
    private Map<IOC, UpdateAction> buildDeltaActions(final List<IOC> iocs) {
        throw new UnsupportedOperationException("Delta update type is not yet supported");
    }
}
