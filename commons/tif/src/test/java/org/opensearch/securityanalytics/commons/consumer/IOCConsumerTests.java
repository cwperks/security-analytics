package org.opensearch.securityanalytics.commons.consumer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opensearch.securityanalytics.commons.model.IOC;
import org.opensearch.securityanalytics.commons.model.UpdateAction;
import org.opensearch.securityanalytics.commons.model.UpdateType;
import org.opensearch.securityanalytics.commons.store.FeedStore;
import org.opensearch.securityanalytics.commons.util.STIX2Generator;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class IOCConsumerTests {
    private static final int BATCH_SIZE = new Random().nextInt(99) + 1;

    @Mock
    private FeedStore feedStore;

    private IOCConsumer iocConsumer;
    private LinkedBlockingQueue<IOC> queue;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        queue = new LinkedBlockingQueue<>(BATCH_SIZE);
        iocConsumer = new IOCConsumer(queue, feedStore, UpdateType.REPLACE);
    }

    @AfterEach
    public void tearDown() {
        verifyNoMoreInteractions(feedStore);
    }

    @Test
    public void testAccept_QueueHasSlots() {
        final IOC ioc = STIX2Generator.randomSTIX2();
        iocConsumer.accept(ioc);
        assertEquals(1, queue.size());
    }

    @Test
    public void testAccept_QueueIsFull() {
        final List<IOC> iocs = STIX2Generator.generateSTIX2(BATCH_SIZE);

        // Fill the queue
        iocs.forEach(ioc -> iocConsumer.accept(ioc));
        assertEquals(BATCH_SIZE, queue.size());

        // Add another object to flush the queue
        final IOC ioc = STIX2Generator.randomSTIX2();
        iocConsumer.accept(ioc);
        assertEquals(1, queue.size());

        final Map<IOC, UpdateAction> expectedStoreParam = getExpectedStoreParam(iocs, UpdateAction.UPSERT);
        verify(feedStore).storeIOCs(eq(expectedStoreParam));
    }

    @Test
    public void testFlush_UpdateReplacementType() {
        final List<IOC> iocs = STIX2Generator.generateSTIX2(BATCH_SIZE);
        iocs.forEach(ioc -> queue.offer(ioc));
        assertEquals(BATCH_SIZE, queue.size());

        iocConsumer.flushIOCs();
        assertEquals(0, queue.size());

        final Map<IOC, UpdateAction> expectedStoreParam = getExpectedStoreParam(iocs, UpdateAction.UPSERT);
        verify(feedStore).storeIOCs(eq(expectedStoreParam));
    }

    @Test
    public void testFlush_DeltaReplacementType() {
        final List<IOC> iocs = STIX2Generator.generateSTIX2(BATCH_SIZE);
        iocs.forEach(ioc -> queue.offer(ioc));
        assertEquals(BATCH_SIZE, queue.size());

        final IOCConsumer testIOCConsumer = new IOCConsumer(queue, feedStore, UpdateType.DELTA);
        assertThrows(UnsupportedOperationException.class, testIOCConsumer::flushIOCs);
    }

    @Test
    public void testFlush_NoOpIfQueueIsEmpty() {
        assertTrue(queue.isEmpty());
        iocConsumer.flushIOCs();
        assertTrue(queue.isEmpty());
    }

    private Map<IOC, UpdateAction> getExpectedStoreParam(final List<IOC> iocs, final UpdateAction updateAction) {
        return iocs.stream()
                .map(i -> Map.entry(i, updateAction))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
