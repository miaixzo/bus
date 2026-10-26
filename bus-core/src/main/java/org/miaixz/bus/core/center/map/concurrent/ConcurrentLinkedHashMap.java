/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.center.map.concurrent;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.miaixz.bus.core.center.function.BiConsumerX;
import org.miaixz.bus.core.center.queue.DiscardingQueue;
import org.miaixz.bus.core.center.queue.Linked;
import org.miaixz.bus.core.center.queue.LinkedDeque;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.RuntimeKit;

/**
 * A hash table supporting full concurrency of retrievals, adjustable expected concurrency for updates, and a maximum
 * capacity to bound the map by. This implementation differs from {@link ConcurrentHashMap} in that it maintains a page
 * replacement algorithm that is used to evict an entry when the map has exceeded its capacity. Unlike the
 * {@code Java Collections Framework}, instances are normally configured through a {@link Builder} and constructed from
 * that configuration.
 * <p>
 * An entry is evicted from the map when the {@code weighted capacity} exceeds its {@code maximum weighted capacity}
 * threshold. A {@link EntryWeigher} determines how many units of capacity that an entry consumes. The default weigher
 * assigns each value a weight of {@code 1} to bound the map by the total number of key-value pairs. A map that holds
 * collections may choose to weigh values by the number of elements in the collection and bound the map by the total
 * number of elements that it contains. A change to a value that modifies its weight requires that an update operation
 * is performed on the map.
 * <p>
 * An {@link BiConsumerX} may be supplied for notification when an entry is evicted from the map. This listener is
 * invoked on a caller's thread and will not block other threads from operating on the map. An implementation should be
 * aware that the caller's thread will not expect long execution times or failures as a side effect of the listener
 * being notified. Execution safety and a fast turn around time can be achieved by performing the operation
 * asynchronously, such as by submitting a task to an {@link java.util.concurrent.ExecutorService}.
 * <p>
 * The {@code concurrency level} is an initial sizing hint for the backing concurrent hash table, not a limit on the
 * number of threads that may update it. Retention order and capacity accounting are maintained asynchronously under the
 * eviction lock, so a write may be visible before its policy task has been applied and the capacity may be exceeded
 * temporarily.
 * <p>
 * Buffered reads and writes make retention order approximate rather than strict access-order LRU. Ordered snapshot
 * methods traverse the policy deque after bounded maintenance; they are not atomic captures of concurrent updates.
 * <p>
 * This class and its views and iterators implement all of the <em>optional</em> methods of the {@link Map} and
 * {@link Iterator} interfaces.
 * <p>
 * Like {@link Hashtable} but unlike {@link HashMap}, this class does <em>not</em> allow {@code null} to be used as a
 * key or value. Unlike {@link LinkedHashMap}, this class does <em>not</em> provide predictable iteration order. A
 * snapshot of the keys and entries may be obtained in ascending and descending order of retention.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 * @author Kimi Liu
 */
public class ConcurrentLinkedHashMap<K, V> extends AbstractMap<K, V> implements ConcurrentMap<K, V>, Serializable {

    @Serial
    private static final long serialVersionUID = 2535885800001L;

    /**
     * The number of CPUs.
     */
    static final int NCPU = RuntimeKit.getProcessorCount();

    /**
     * The maximum weighted capacity of the map.
     */
    static final long MAXIMUM_CAPACITY = Long.MAX_VALUE - Integer.MAX_VALUE;

    /**
     * The number of read buffers to use.
     */
    static final int NUMBER_OF_READ_BUFFERS = ceilingNextPowerOfTwo(NCPU);

    /**
     * Mask value for indexing into the read buffers.
     */
    static final int READ_BUFFERS_MASK = NUMBER_OF_READ_BUFFERS - 1;

    /**
     * The number of pending read operations before attempting to drain.
     */
    static final int READ_BUFFER_THRESHOLD = 32;

    /**
     * The maximum number of read operations to perform per amortized drain.
     */
    static final int READ_BUFFER_DRAIN_THRESHOLD = 2 * READ_BUFFER_THRESHOLD;

    /**
     * The maximum number of pending reads per buffer.
     */
    static final int READ_BUFFER_SIZE = 2 * READ_BUFFER_DRAIN_THRESHOLD;

    /**
     * Mask value for indexing into the read buffer.
     */
    static final int READ_BUFFER_INDEX_MASK = READ_BUFFER_SIZE - 1;

    /**
     * The maximum number of write operations to perform per amortized drain.
     */
    static final int WRITE_BUFFER_DRAIN_THRESHOLD = 16;

    /**
     * The backing data store holding the key-value associations.
     */
    final ConcurrentMap<K, Node<K, V>> data;

    /**
     * The estimated number of concurrently updating threads.
     */
    final int concurrencyLevel;

    /**
     * The total number of reads recorded by each read buffer.
     */
    final long[] readBufferReadCount;

    /**
     * The eviction deque used to implement the page replacement policy.
     */
    final LinkedDeque<Node<K, V>> evictionDeque;

    /**
     * Sum of node weights currently registered in the eviction policy, which can lag backing-map updates. Mutations
     * occur under the eviction lock; the atomic holder permits lock-free observation.
     */
    final AtomicLong weightedSize;

    /**
     * The maximum weighted capacity of the map.
     */
    final AtomicLong capacity;

    /**
     * Lock protecting the eviction deque, each node's registered policy weight, and policy-size mutations.
     */
    final Lock evictionLock;

    /**
     * A buffer for pending write operations.
     */
    final Queue<Runnable> writeBuffer;

    /**
     *
     * The total number of writes recorded by each read buffer.
     */
    final AtomicLong[] readBufferWriteCount;

    /**
     * The write count at which the last drain of each read buffer occurred.
     */
    final AtomicLong[] readBufferDrainAtWriteCount;

    /**
     * A circular array of buffers for recording read operations.
     */
    final AtomicReference<Node<K, V>>[][] readBuffers;

    /**
     * The current draining status of the buffers.
     */
    final AtomicReference<DrainStatus> drainStatus;

    /**
     * The weigher used to determine the weight of each entry.
     */
    final EntryWeigher<? super K, ? super V> weigher;

    /**
     * A queue of evicted entries pending notification to the listener.
     */
    final Queue<Node<K, V>> pendingNotifications;

    /**
     * The listener to be notified when an entry is evicted.
     */
    final BiConsumerX<K, V> listener;

    /**
     * A cached view of the key set.
     */
    transient Set<K> keySet;

    /**
     * A cached view of the values.
     */
    transient Collection<V> values;

    /**
     * A cached view of the entry set.
     */
    transient Set<Entry<K, V>> entrySet;

    /**
     * Creates an instance based on the builder's configuration.
     *
     * @param builder The builder to configure the map.
     */
    public ConcurrentLinkedHashMap(final Builder<K, V> builder) {
        // The data store and its maximum capacity
        concurrencyLevel = builder.concurrencyLevel;
        capacity = new AtomicLong(Math.min(builder.capacity, MAXIMUM_CAPACITY));
        data = new ConcurrentHashMap<>(builder.initialCapacity, Normal.DEFAULT_LOAD_FACTOR, concurrencyLevel);

        // The eviction support
        weigher = builder.weigher;
        evictionLock = new ReentrantLock();
        weightedSize = new AtomicLong();
        evictionDeque = new LinkedDeque<>();
        writeBuffer = new ConcurrentLinkedQueue<>();
        drainStatus = new AtomicReference<>(DrainStatus.IDLE);

        readBufferReadCount = new long[NUMBER_OF_READ_BUFFERS];
        readBufferWriteCount = new AtomicLong[NUMBER_OF_READ_BUFFERS];
        readBufferDrainAtWriteCount = new AtomicLong[NUMBER_OF_READ_BUFFERS];
        readBuffers = new AtomicReference[NUMBER_OF_READ_BUFFERS][READ_BUFFER_SIZE];
        for (int i = 0; i < NUMBER_OF_READ_BUFFERS; i++) {
            readBufferWriteCount[i] = new AtomicLong();
            readBufferDrainAtWriteCount[i] = new AtomicLong();
            readBuffers[i] = new AtomicReference[READ_BUFFER_SIZE];
            for (int j = 0; j < READ_BUFFER_SIZE; j++) {
                readBuffers[i][j] = new AtomicReference<>();
            }
        }

        // The notification queue and listener
        listener = builder.listener;
        pendingNotifications = (listener == DiscardingListener.INSTANCE) ? DiscardingQueue.getInstance()
                : new ConcurrentLinkedQueue<>();
    }

    /**
     * Calculates the next power of two, ceiling.
     *
     * @param x the value to round
     * @return the next power of two
     */
    static int ceilingNextPowerOfTwo(final int x) {
        return 1 << (Integer.SIZE - Integer.numberOfLeadingZeros(x - 1));
    }

    /**
     * Determines the index of the read buffer to use for the current thread.
     *
     * @return the index of the read buffer
     */
    static long readBufferIndex() {
        return Thread.currentThread().threadId() & READ_BUFFERS_MASK;
    }

    /**
     * Retrieves the maximum weighted capacity of the map.
     *
     * @return the maximum weighted capacity
     */
    public long capacity() {
        return capacity.get();
    }

    /**
     * Sets the maximum weighted capacity and eagerly evicts registered policy entries until their weight is within the
     * new bound. Pending or concurrent writes can remain temporarily visible before their weights are registered.
     *
     * @param capacity the maximum weighted capacity of the map
     * @throws IllegalArgumentException if the capacity is negative
     */
    public void setCapacity(final long capacity) {
        Assert.isTrue(capacity >= 0);
        evictionLock.lock();
        try {
            this.capacity.lazySet(Math.min(capacity, MAXIMUM_CAPACITY));
            drainBuffers();
            evict();
        } finally {
            evictionLock.unlock();
        }
        notifyListener();
    }

    /**
     * Determines whether the map has exceeded its capacity.
     *
     * @return if the map has overflowed
     */
    boolean hasOverflowed() {
        return weightedSize.get() > capacity.get();
    }

    /**
     * Evicts entries from the map while it exceeds the capacity and appends evicted entries to the notification queue
     * for processing.
     */
    void evict() {
        while (hasOverflowed()) {
            final Node<K, V> node = evictionDeque.poll();
            if (node == null) {
                return;
            }
            if (data.remove(node.key, node)) {
                pendingNotifications.add(node);
            }
            makeDead(node);
        }
    }

    /**
     * Performs the post-processing work required after a read.
     *
     * @param node the entry in the page replacement policy
     */
    void afterRead(final Node<K, V> node) {
        final long bufferIndex = readBufferIndex();
        final long writeCount = recordRead(bufferIndex, node);
        drainOnReadIfNeeded(bufferIndex, writeCount);
        notifyListener();
    }

    /**
     * Records a read in the buffer and return its write count.
     *
     * @param bufferIndex the index to the chosen read buffer
     * @param node        the entry in the page replacement policy
     * @return the number of writes on the chosen read buffer
     */
    long recordRead(final long bufferIndex, final Node<K, V> node) {
        final AtomicLong counter = readBufferWriteCount[(int) bufferIndex];
        final long writeCount = counter.get();
        counter.lazySet(writeCount + 1);
        final int index = (int) (writeCount & READ_BUFFER_INDEX_MASK);
        readBuffers[(int) bufferIndex][index].lazySet(node);
        return writeCount;
    }

    /**
     * Attempts to drain the buffers if it is determined to be needed when post-processing a read.
     *
     * @param bufferIndex the index to the chosen read buffer
     * @param writeCount  the number of writes on the chosen read buffer
     */
    void drainOnReadIfNeeded(final long bufferIndex, final long writeCount) {
        final long pending = (writeCount - readBufferDrainAtWriteCount[(int) bufferIndex].get());
        final boolean delayable = (pending < READ_BUFFER_THRESHOLD);
        final DrainStatus status = drainStatus.get();
        if (status.shouldDrainBuffers(delayable)) {
            tryToDrainBuffers();
        }
    }

    /**
     * Performs the post-processing work required after a write.
     *
     * @param task the pending operation to be applied
     */
    void afterWrite(final Runnable task) {
        writeBuffer.add(task);
        drainStatus.lazySet(DrainStatus.REQUIRED);
        tryToDrainBuffers();
        notifyListener();
    }

    /**
     * Attempts to acquire the eviction lock and apply the pending operations, up to the amortized threshold, to the
     * page replacement policy.
     */
    void tryToDrainBuffers() {
        if (evictionLock.tryLock()) {
            try {
                drainStatus.lazySet(DrainStatus.PROCESSING);
                drainBuffers();
            } finally {
                drainStatus.compareAndSet(DrainStatus.PROCESSING, DrainStatus.IDLE);
                evictionLock.unlock();
            }
        }
    }

    /**
     * Drains the read and write buffers up to an amortized threshold.
     */
    void drainBuffers() {
        drainReadBuffers();
        drainWriteBuffer();
    }

    /**
     * Drains the read buffers, each up to an amortized threshold.
     */
    void drainReadBuffers() {
        final long start = Thread.currentThread().threadId();
        final long end = start + NUMBER_OF_READ_BUFFERS;
        for (long i = start; i < end; i++) {
            drainReadBuffer((int) (i & READ_BUFFERS_MASK));
        }
    }

    /**
     * Drains the read buffer up to an amortized threshold.
     *
     * @param bufferIndex the index to the chosen read buffer
     */
    void drainReadBuffer(final int bufferIndex) {
        final long writeCount = readBufferWriteCount[bufferIndex].get();
        for (int i = 0; i < READ_BUFFER_DRAIN_THRESHOLD; i++) {
            final int index = (int) (readBufferReadCount[bufferIndex] & READ_BUFFER_INDEX_MASK);
            final AtomicReference<Node<K, V>> slot = readBuffers[bufferIndex][index];
            final Node<K, V> node = slot.get();
            if (node == null) {
                break;
            }

            slot.lazySet(null);
            applyRead(node);
            readBufferReadCount[bufferIndex]++;
        }
        readBufferDrainAtWriteCount[bufferIndex].lazySet(writeCount);
    }

    /**
     * Updates the node's location in the page replacement policy.
     *
     * @param node the node to update
     */
    void applyRead(final Node<K, V> node) {
        if (evictionDeque.contains(node)) {
            evictionDeque.moveToBack(node);
        }
    }

    /**
     * Drains the write buffer up to an amortized threshold.
     */
    void drainWriteBuffer() {
        for (int i = 0; i < WRITE_BUFFER_DRAIN_THRESHOLD; i++) {
            final Runnable task = writeBuffer.poll();
            if (task == null) {
                break;
            }
            task.run();
        }
    }

    /**
     * Attempts to transition the node from the {@code alive} state to the {@code retired} state.
     *
     * @param node   the entry in the page replacement policy
     * @param expect the expected weighted value
     * @return {@code true} if successful
     */
    boolean tryToRetire(final Node<K, V> node, final WeightedValue<V> expect) {
        if (expect.isAlive()) {
            final WeightedValue<V> retired = new WeightedValue<>(expect.value, -expect.weight);
            return node.compareAndSet(expect, retired);
        }
        return false;
    }

    /**
     * Atomically transitions the node from the {@code alive} state to the {@code retired} state.
     *
     * @param node the entry in the page replacement policy
     */
    void makeRetired(final Node<K, V> node) {
        for (;;) {
            final WeightedValue<V> current = node.get();
            if (!current.isAlive()) {
                return;
            }
            final WeightedValue<V> retired = new WeightedValue<>(current.value, -current.weight);
            if (node.compareAndSet(current, retired)) {
                return;
            }
        }
    }

    /**
     * Marks a node dead and releases only the weight currently registered in the policy. The eviction lock must be
     * held. Repeated calls release no additional weight; an unprocessed add task therefore cannot cause a negative
     * deduction.
     *
     * @param node the entry in the page replacement policy
     */
    void makeDead(final Node<K, V> node) {
        for (;;) {
            final WeightedValue<V> current = node.get();
            if (current.isDead()) {
                break;
            }
            final WeightedValue<V> dead = new WeightedValue<>(current.value, 0);
            if (node.compareAndSet(current, dead)) {
                break;
            }
        }
        // Current value weight can differ from the amount registered by delayed policy tasks.
        weightedSize.lazySet(weightedSize.get() - node.policyWeight);
        node.policyWeight = 0;
    }

    /**
     * Reconciles a live node with the weight already registered in the eviction policy. The eviction lock must be held.
     * Old tasks cannot register removed nodes, and repeated tasks account for the current value only once.
     *
     * @param node node observed by a policy task
     * @return whether the node is still the live mapping for its key
     */
    private boolean reconcileWeight(final Node<K, V> node) {
        final WeightedValue<V> current = node.get();
        if (!current.isAlive() || data.get(node.key) != node) {
            evictionDeque.remove(node);
            makeDead(node);
            return false;
        }
        weightedSize.lazySet(weightedSize.get() + current.weight - node.policyWeight);
        node.policyWeight = current.weight;
        return true;
    }

    /**
     * Notifies the listener of entries that were evicted.
     *
     * @throws RuntimeException if a listener callback fails
     */
    void notifyListener() {
        Node<K, V> node;
        while ((node = pendingNotifications.poll()) != null) {
            listener.accept(node.key, node.getValue());
        }
    }

    /**
     * Returns {@code true} if this map contains no key-value mappings.
     *
     * @return {@code true} if this map contains no key-value mappings
     */
    @Override
    public boolean isEmpty() {
        return data.isEmpty();
    }

    /**
     * Returns the number of key-value mappings in this map.
     *
     * @return the number of key-value mappings in this map
     */
    @Override
    public int size() {
        return data.size();
    }

    /**
     * Returns the asynchronously maintained policy weight, clamped to zero. Pending tasks mean this is not necessarily
     * the exact combined weight of the mappings currently visible in the backing hash table.
     *
     * @return the non-negative weight currently registered in the eviction policy
     */
    public long weightedSize() {
        return Math.max(0, weightedSize.get());
    }

    /**
     * Removes observed mappings and their registered policy weights, including entries whose add tasks have not run.
     * Concurrent new writes follow the backing map's weakly consistent clear semantics. Delayed tasks cannot revive
     * removed nodes or reapply historical weight differences.
     */
    @Override
    public void clear() {
        evictionLock.lock();
        try {
            // Completed writes can still be missing from the asynchronously maintained policy deque.
            for (final Node<K, V> entry : data.values()) {
                if (data.remove(entry.key, entry)) {
                    makeRetired(entry);
                    evictionDeque.remove(entry);
                    makeDead(entry);
                }
            }

            // Discard all entries
            Node<K, V> node;
            while ((node = evictionDeque.poll()) != null) {
                data.remove(node.key, node);
                makeDead(node);
            }

            // Discard all pending reads
            for (final AtomicReference<Node<K, V>>[] buffer : readBuffers) {
                for (final AtomicReference<Node<K, V>> slot : buffer) {
                    slot.lazySet(null);
                }
            }

            // Apply all pending writes
            Runnable task;
            while ((task = writeBuffer.poll()) != null) {
                task.run();
            }
        } finally {
            evictionLock.unlock();
        }
    }

    /**
     * Returns {@code true} if this map contains a mapping for the specified key.
     *
     * @param key the key whose presence in this map is to be tested
     * @return {@code true} if this map contains a mapping for the specified key
     * @throws NullPointerException if the key is {@code null}
     */
    @Override
    public boolean containsKey(final Object key) {
        return data.containsKey(key);
    }

    /**
     * Returns {@code true} if this map maps one or more keys to the specified value.
     *
     * @param value value whose presence in this map is to be tested
     * @return {@code true} if this map maps one or more keys to the specified value
     * @throws IllegalArgumentException if the value is {@code null}
     */
    @Override
    public boolean containsValue(final Object value) {
        Assert.notNull(value);
        for (final Node<K, V> node : data.values()) {
            if (node.getValue().equals(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the value to which the specified key is mapped, or {@code null} if this map contains no mapping for the
     * key.
     * <p>
     * This operation is recorded with the page replacement policy.
     *
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped, or {@code null} if this map contains no mapping for the
     *         key
     * @throws NullPointerException if the key is {@code null}
     */
    @Override
    public V get(final Object key) {
        final Node<K, V> node = data.get(key);
        if (node == null) {
            return null;
        }
        afterRead(node);
        return node.getValue();
    }

    /**
     * Returns the value to which the specified key is mapped, or {@code null} if this map contains no mapping for the
     * key. This method differs from {@link #get(Object)} in that it does not record the operation with the page
     * replacement policy.
     *
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped, or {@code null} if this map contains no mapping for the
     *         key
     * @throws NullPointerException if the specified key is null
     */
    public V getQuietly(final K key) {
        final Node<K, V> node = data.get(key);
        return (node == null) ? null : node.getValue();
    }

    /**
     * Maps the specified key to the specified value in this map. Neither the key nor the value can be {@code null}.
     * <p>
     * This operation is recorded with the page replacement policy.
     *
     * @param key   key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with {@code key}, or {@code null} if there was no mapping for {@code key}
     * @throws IllegalArgumentException if the key or value is {@code null}, or the configured weigher returns a
     *                                  non-positive weight
     */
    @Override
    public V put(final K key, final V value) {
        return put(key, value, false);
    }

    /**
     * If the specified key is not already associated with a value, associates it with the given value and performs the
     * necessary page replacement policy operations.
     *
     * @param key   key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with the specified key, or {@code null} if there was no mapping
     * @throws IllegalArgumentException if the key or value is {@code null}, or the configured weigher returns a
     *                                  non-positive weight
     */
    @Override
    public V putIfAbsent(final K key, final V value) {
        return put(key, value, true);
    }

    /**
     * Adds a node to the list and the data store. If an existing node is found, then its value is updated if allowed.
     *
     * @param key          key with which the specified value is to be associated
     * @param value        value to be associated with the specified key
     * @param onlyIfAbsent a write is performed only if the key is not already associated with a value
     * @return the prior value in the data store or null if no mapping was found
     * @throws IllegalArgumentException if the key or value is {@code null}, or the configured weigher returns a
     *                                  non-positive weight
     */
    V put(final K key, final V value, final boolean onlyIfAbsent) {
        Assert.notNull(key);
        Assert.notNull(value);

        final int weight = weigher.weightOf(key, value);
        final WeightedValue<V> weightedValue = new WeightedValue<>(value, weight);
        final Node<K, V> node = new Node<>(key, weightedValue);

        for (;;) {
            final Node<K, V> prior = data.putIfAbsent(node.key, node);
            if (prior == null) {
                afterWrite(new AddTask(node));
                return null;
            } else if (onlyIfAbsent) {
                afterRead(prior);
                return prior.getValue();
            }
            for (;;) {
                final WeightedValue<V> oldWeightedValue = prior.get();
                if (!oldWeightedValue.isAlive()) {
                    break;
                }

                if (prior.compareAndSet(oldWeightedValue, weightedValue)) {
                    final int weightedDifference = weight - oldWeightedValue.weight;
                    if (weightedDifference == 0) {
                        afterRead(prior);
                    } else {
                        afterWrite(new UpdateTask(prior));
                    }
                    return oldWeightedValue.value;
                }
            }
        }
    }

    /**
     * Removes the mapping for a key from this map if it is present.
     * <p>
     * This operation is recorded with the page replacement policy.
     *
     * @param key key whose mapping is to be removed from the map
     * @return the previous value associated with {@code key}, or {@code null} if there was no mapping for {@code key}
     * @throws NullPointerException if the key is {@code null}
     */
    @Override
    public V remove(final Object key) {
        final Node<K, V> node = data.remove(key);
        if (node == null) {
            return null;
        }

        makeRetired(node);
        afterWrite(new RemovalTask(node));
        return node.getValue();
    }

    /**
     * Removes the entry for a key only if it is currently mapped to a given value.
     * <p>
     * This operation is recorded with the page replacement policy if successful.
     *
     * @param key   key with which the specified value is associated
     * @param value value expected to be associated with the specified key; {@code null} never matches
     * @return {@code true} if the value was removed
     * @throws NullPointerException if the key is {@code null}
     */
    @Override
    public boolean remove(final Object key, final Object value) {
        final Node<K, V> node = data.get((K) key);
        if ((node == null) || (value == null)) {
            return false;
        }

        WeightedValue<V> weightedValue = node.get();
        for (;;) {
            if (weightedValue.contains(value)) {
                if (tryToRetire(node, weightedValue)) {
                    if (data.remove(key, node)) {
                        afterWrite(new RemovalTask(node));
                        return true;
                    }
                } else {
                    weightedValue = node.get();
                    if (weightedValue.isAlive()) {
                        continue;
                    }
                }
            }
            return false;
        }
    }

    /**
     * Replaces the entry for a key only if it is currently mapped to some value.
     * <p>
     * This operation is recorded with the page replacement policy.
     *
     * @param key   key with which the specified value is associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with the specified key, or {@code null} if there was no mapping for the key
     * @throws IllegalArgumentException if the key or value is {@code null}, or the configured weigher returns a
     *                                  non-positive weight
     */
    @Override
    public V replace(final K key, final V value) {
        Assert.notNull(key);
        Assert.notNull(value);

        final int weight = weigher.weightOf(key, value);
        final WeightedValue<V> weightedValue = new WeightedValue<>(value, weight);

        final Node<K, V> node = data.get(key);
        if (node == null) {
            return null;
        }
        for (;;) {
            final WeightedValue<V> oldWeightedValue = node.get();
            if (!oldWeightedValue.isAlive()) {
                return null;
            }
            if (node.compareAndSet(oldWeightedValue, weightedValue)) {
                final int weightedDifference = weight - oldWeightedValue.weight;
                if (weightedDifference == 0) {
                    afterRead(node);
                } else {
                    afterWrite(new UpdateTask(node));
                }
                return oldWeightedValue.value;
            }
        }
    }

    /**
     * Replaces the entry for a key only if currently mapped to the given value.
     * <p>
     * This operation is recorded with the page replacement policy if successful.
     *
     * @param key      key with which the specified value is associated
     * @param oldValue value expected to be associated with the specified key
     * @param newValue value to be associated with the specified key
     * @return {@code true} if the value was replaced
     * @throws IllegalArgumentException if the key or either value is {@code null}, or the configured weigher returns a
     *                                  non-positive weight
     */
    @Override
    public boolean replace(final K key, final V oldValue, final V newValue) {
        Assert.notNull(key);
        Assert.notNull(oldValue);
        Assert.notNull(newValue);

        final int weight = weigher.weightOf(key, newValue);
        final WeightedValue<V> newWeightedValue = new WeightedValue<>(newValue, weight);

        final Node<K, V> node = data.get(key);
        if (node == null) {
            return false;
        }
        for (;;) {
            final WeightedValue<V> weightedValue = node.get();
            if (!weightedValue.isAlive() || !weightedValue.contains(oldValue)) {
                return false;
            }
            if (node.compareAndSet(weightedValue, newWeightedValue)) {
                final int weightedDifference = weight - weightedValue.weight;
                if (weightedDifference == 0) {
                    afterRead(node);
                } else {
                    afterWrite(new UpdateTask(node));
                }
                return true;
            }
        }
    }

    /**
     * Returns a {@link Set} view of the keys contained in this map. The set is backed by the map, so changes to the map
     * are reflected in the set, and vice-versa.
     *
     * @return a set view of the keys contained in this map
     */
    @Override
    public Set<K> keySet() {
        final Set<K> ks = keySet;
        return (ks == null) ? (keySet = new KeySet()) : ks;
    }

    /**
     * Returns a unmodifiable snapshot {@link Set} view of the keys contained in this map. The set's iterator returns
     * the keys whose order of iteration is the ascending order in which its entries are considered eligible for
     * retention, from the least-likely to be retained to the most-likely.
     * <p>
     * Beware that, unlike in {@link #keySet()}, obtaining the set is <em>NOT</em> a constant-time operation. Because of
     * the asynchronous nature of the page replacement policy, determining the retention ordering requires a traversal
     * of the keys.
     *
     * @return an ascending snapshot view of the keys in this map
     */
    public Set<K> ascendingKeySet() {
        return ascendingKeySetWithLimit(Integer.MAX_VALUE);
    }

    /**
     * Returns an unmodifiable snapshot {@link Set} view of the keys contained in this map. The set's iterator returns
     * the keys whose order of iteration is the ascending order in which its entries are considered eligible for
     * retention, from the least-likely to be retained to the most-likely.
     * <p>
     * Beware that, unlike in {@link #keySet()}, obtaining the set is <em>NOT</em> a constant-time operation. Because of
     * the asynchronous nature of the page replacement policy, determining the retention ordering requires a traversal
     * of the keys.
     *
     * @param limit the maximum size of the returned set
     * @return a ascending snapshot view of the keys in this map
     * @throws IllegalArgumentException if the limit is negative
     */
    public Set<K> ascendingKeySetWithLimit(final int limit) {
        return orderedKeySet(true, limit);
    }

    /**
     * Returns an unmodifiable snapshot {@link Set} view of the keys contained in this map. The set's iterator returns
     * the keys whose order of iteration is the descending order in which its entries are considered eligible for
     * retention, from the most-likely to be retained to the least-likely.
     * <p>
     * Beware that, unlike in {@link #keySet()}, obtaining the set is <em>NOT</em> a constant-time operation. Because of
     * the asynchronous nature of the page replacement policy, determining the retention ordering requires a traversal
     * of the keys.
     *
     * @return a descending snapshot view of the keys in this map
     */
    public Set<K> descendingKeySet() {
        return descendingKeySetWithLimit(Integer.MAX_VALUE);
    }

    /**
     * Returns an unmodifiable snapshot {@link Set} view of the keys contained in this map. The set's iterator returns
     * the keys whose order of iteration is the descending order in which its entries are considered eligible for
     * retention, from the most-likely to be retained to the least-likely.
     * <p>
     * Beware that, unlike in {@link #keySet()}, obtaining the set is <em>NOT</em> a constant-time operation. Because of
     * the asynchronous nature of the page replacement policy, determining the retention ordering requires a traversal
     * of the keys.
     *
     * @param limit the maximum size of the returned set
     * @return a descending snapshot view of the keys in this map
     * @throws IllegalArgumentException if the limit is negative
     */
    public Set<K> descendingKeySetWithLimit(final int limit) {
        return orderedKeySet(false, limit);
    }

    /**
     * Builds an immutable key snapshot in eviction-policy order.
     *
     * @param ascending whether to iterate from least to most retainable
     * @param limit     maximum number of keys
     * @return ordered key snapshot
     * @throws IllegalArgumentException if the limit is negative
     */
    Set<K> orderedKeySet(final boolean ascending, final int limit) {
        Assert.isTrue(limit >= 0);
        evictionLock.lock();
        try {
            drainBuffers();

            final int initialCapacity = (weigher == Weighers.entrySingleton()) ? Math.min(limit, (int) weightedSize())
                    : 16;
            final Set<K> keys = new LinkedHashSet<>(initialCapacity);
            final Iterator<Node<K, V>> iterator = ascending ? evictionDeque.iterator()
                    : evictionDeque.descendingIterator();
            while (iterator.hasNext() && (limit > keys.size())) {
                keys.add(iterator.next().key);
            }
            return Collections.unmodifiableSet(keys);
        } finally {
            evictionLock.unlock();
        }
    }

    /**
     * Returns a {@link Collection} view of the values contained in this map. The collection is backed by the map, so
     * changes to the map are reflected in the collection, and vice-versa.
     *
     * @return a collection view of the values contained in this map
     */
    @Override
    public Collection<V> values() {
        final Collection<V> vs = values;
        return (vs == null) ? (values = new Values()) : vs;
    }

    /**
     * Returns a {@link Set} view of the mappings contained in this map. The set is backed by the map, so changes to the
     * map are reflected in the set, and vice-versa.
     *
     * @return a set view of the mappings contained in this map
     */
    @Override
    public Set<Entry<K, V>> entrySet() {
        final Set<Entry<K, V>> es = entrySet;
        return (es == null) ? (entrySet = new EntrySet()) : es;
    }

    /**
     * Returns an unmodifiable snapshot {@link Map} view of the mappings contained in this map. The map's collections
     * return the mappings whose order of iteration is the ascending order in which its entries are considered eligible
     * for retention, from the least-likely to be retained to the most-likely.
     * <p>
     * Beware that obtaining the mappings is <em>NOT</em> a constant-time operation. Because of the asynchronous nature
     * of the page replacement policy, determining the retention ordering requires a traversal of the entries.
     *
     * @return a ascending snapshot view of this map
     */
    public Map<K, V> ascendingMap() {
        return ascendingMapWithLimit(Integer.MAX_VALUE);
    }

    /**
     * Returns an unmodifiable snapshot {@link Map} view of the mappings contained in this map. The map's collections
     * return the mappings whose order of iteration is the ascending order in which its entries are considered eligible
     * for retention, from the least-likely to be retained to the most-likely.
     * <p>
     * Beware that obtaining the mappings is <em>NOT</em> a constant-time operation. Because of the asynchronous nature
     * of the page replacement policy, determining the retention ordering requires a traversal of the entries.
     *
     * @param limit the maximum size of the returned map
     * @return a ascending snapshot view of this map
     * @throws IllegalArgumentException if the limit is negative
     */
    public Map<K, V> ascendingMapWithLimit(final int limit) {
        return orderedMap(true, limit);
    }

    /**
     * Returns an unmodifiable snapshot {@link Map} view of the mappings contained in this map. The map's collections
     * return the mappings whose order of iteration is the descending order in which its entries are considered eligible
     * for retention, from the most-likely to be retained to the least-likely.
     * <p>
     * Beware that obtaining the mappings is <em>NOT</em> a constant-time operation. Because of the asynchronous nature
     * of the page replacement policy, determining the retention ordering requires a traversal of the entries.
     *
     * @return a descending snapshot view of this map
     */
    public Map<K, V> descendingMap() {
        return descendingMapWithLimit(Integer.MAX_VALUE);
    }

    /**
     * Returns an unmodifiable snapshot {@link Map} view of the mappings contained in this map. The map's collections
     * return the mappings whose order of iteration is the descending order in which its entries are considered eligible
     * for retention, from the most-likely to be retained to the least-likely.
     * <p>
     * Beware that obtaining the mappings is <em>NOT</em> a constant-time operation. Because of the asynchronous nature
     * of the page replacement policy, determining the retention ordering requires a traversal of the entries.
     *
     * @param limit the maximum size of the returned map
     * @return a descending snapshot view of this map
     * @throws IllegalArgumentException if the limit is negative
     */
    public Map<K, V> descendingMapWithLimit(final int limit) {
        return orderedMap(false, limit);
    }

    /**
     * Builds an immutable entry snapshot in eviction-policy order.
     *
     * @param ascending whether to iterate from least to most retainable
     * @param limit     maximum number of entries
     * @return ordered map snapshot
     * @throws IllegalArgumentException if the limit is negative
     */
    Map<K, V> orderedMap(final boolean ascending, final int limit) {
        Assert.isTrue(limit >= 0);
        evictionLock.lock();
        try {
            drainBuffers();

            final int initialCapacity = (weigher == Weighers.entrySingleton()) ? Math.min(limit, (int) weightedSize())
                    : 16;
            final Map<K, V> map = new LinkedHashMap<>(initialCapacity);
            final Iterator<Node<K, V>> iterator = ascending ? evictionDeque.iterator()
                    : evictionDeque.descendingIterator();
            while (iterator.hasNext() && (limit > map.size())) {
                final Node<K, V> node = iterator.next();
                map.put(node.key, node.getValue());
            }
            return Collections.unmodifiableMap(map);
        } finally {
            evictionLock.unlock();
        }
    }

    /**
     * Substitutes a serialization proxy containing configuration and an entry snapshot, without serializing runtime
     * queues, locks, or eviction links.
     *
     * @return the serialization proxy
     */
    Object writeReplace() {
        return new SerializationProxy<>(this);
    }

    /**
     * Rejects direct deserialization; instances must be reconstructed through their serialization proxy.
     *
     * @param stream the input stream, whose contents are not read
     * @throws InvalidObjectException always, because a serialization proxy is required
     */
    private void readObject(final ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }

    /**
     * The draining status of the buffers.
     *
     * @author Kimi Liu
     */
    enum DrainStatus {

        /**
         * A drain is not taking place.
         */
        IDLE {

            /**
             * Determines whether buffers should be drained when in idle state.
             *
             * @param delayable if a drain should be delayed until required
             * @return {@code true} if drain should not be delayed
             */
            @Override
            boolean shouldDrainBuffers(final boolean delayable) {
                return !delayable;
            }
        },

        /**
         * A drain is required due to a pending write modification.
         */
        REQUIRED {

            /**
             * Determines whether buffers should be drained when in required state.
             *
             * @param delayable if a drain should be delayed until required
             * @return {@code true} as drain is required
             */
            @Override
            boolean shouldDrainBuffers(final boolean delayable) {
                return true;
            }
        },

        /**
         * A drain is in progress.
         */
        PROCESSING {

            /**
             * Determines whether buffers should be drained when already processing.
             *
             * @param delayable if a drain should be delayed until required
             * @return {@code false} as drain is already in progress
             */
            @Override
            boolean shouldDrainBuffers(final boolean delayable) {
                return false;
            }
        };

        /**
         * Determines whether the buffers should be drained.
         *
         * @param delayable if a drain should be delayed until required
         * @return if a drain should be attempted
         */
        abstract boolean shouldDrainBuffers(boolean delayable);

    }

    /**
     * A listener that ignores all notifications.
     *
     * @author Kimi Liu
     */
    enum DiscardingListener implements BiConsumerX<Object, Object> {

        /**
         * Shared listener that intentionally discards eviction notifications.
         */
        INSTANCE;

        /**
         * Accepts a key-value pair notification when an entry is evicted from the map. This implementation discards all
         * notifications.
         *
         * @param key   the key of the evicted entry
         * @param value the value of the evicted entry
         */
        @Override
        public void accepting(final Object key, final Object value) {
        }

    }

    /**
     * Immutable value and signed lifecycle weight. The current live value weight is independent of the amount that
     * delayed tasks have registered in {@link Node#policyWeight}.
     *
     * @param <V> the mapped value type
     * @author Kimi Liu
     */
    static final class WeightedValue<V> {

        /**
         * Signed weight encoding the entry lifecycle state.
         */
        final int weight;

        /**
         * Value associated with the map entry.
         */
        final V value;

        /**
         * Creates a weighted value.
         *
         * @param value  entry value
         * @param weight positive live, negative retired, or zero dead weight
         */
        WeightedValue(final V value, final int weight) {
            this.weight = weight;
            this.value = value;
        }

        /**
         * Tests the wrapped value using identity and equality.
         *
         * @param o candidate value
         * @return {@code true} when the wrapped value matches
         */
        boolean contains(final Object o) {
            return (o == value) || value.equals(o);
        }

        /**
         * Tests whether this value represents a live mapping, which may still await its policy add task.
         *
         * @return {@code true} when the lifecycle weight is positive
         */
        boolean isAlive() {
            return weight > 0;
        }

        /**
         * Tests whether the mapping is retired and awaiting policy cleanup.
         *
         * @return {@code true} when the lifecycle weight is negative
         */
        boolean isRetired() {
            return weight < 0;
        }

        /**
         * Tests whether the value has been marked dead. Releasing registered policy weight is handled separately while
         * holding the eviction lock.
         *
         * @return {@code true} when the lifecycle weight is zero
         */
        boolean isDead() {
            return weight == 0;
        }

    }

    /**
     * A node contains the data, the weighted value, and the linkage pointers on the page-replacement algorithm's data
     * structures.
     *
     * @param <K> the key type
     * @param <V> the value type
     */
    static final class Node<K, V> extends AtomicReference<WeightedValue<V>> implements Linked<Node<K, V>> {

        @Serial
        private static final long serialVersionUID = 2535888961801L;

        /**
         * Immutable map key.
         */
        final K key;

        /**
         * Weight currently registered in the eviction policy; accessed only while holding the eviction lock.
         */
        int policyWeight;

        /**
         * Previous node in eviction order.
         */
        Node<K, V> prev;

        /**
         * Next node in eviction order.
         */
        Node<K, V> next;

        /**
         * Creates a new, unlinked node with no weight registered in the eviction policy.
         *
         * @param key           immutable map key
         * @param weightedValue initial value and lifecycle weight
         */
        Node(final K key, final WeightedValue<V> weightedValue) {
            super(weightedValue);
            this.key = key;
        }

        /**
         * Returns the previous node in the eviction deque.
         *
         * @return the previous node, or {@code null} if this is the first node
         */
        @Override
        public Node<K, V> getPrevious() {
            return prev;
        }

        /**
         * Sets the previous node in the eviction deque.
         *
         * @param prev the node to set as previous
         */
        @Override
        public void setPrevious(final Node<K, V> prev) {
            this.prev = prev;
        }

        /**
         * Returns the next node in the eviction deque.
         *
         * @return the next node, or {@code null} if this is the last node
         */
        @Override
        public Node<K, V> getNext() {
            return next;
        }

        /**
         * Sets the next node in the eviction deque.
         *
         * @param next the node to set as next
         */
        @Override
        public void setNext(final Node<K, V> next) {
            this.next = next;
        }

        /**
         * Retrieves the value held by the current {@code WeightedValue}.
         *
         * @return the current mapped value, regardless of lifecycle state
         */
        V getValue() {
            return get().value;
        }

    }

    /**
     * A weigher that requires every stored entry to consume at least one unit of capacity.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @author Kimi Liu
     */
    static final class BoundedEntryWeigher<K, V> implements EntryWeigher<K, V>, Serializable {

        @Serial
        private static final long serialVersionUID = 2535891203266L;;

        /**
         * Delegate entry weigher whose result is range-checked.
         */
        final EntryWeigher<? super K, ? super V> weigher;

        /**
         * Creates a range-checking entry weigher.
         *
         * @param weigher delegate weigher
         * @throws IllegalArgumentException if the delegate is {@code null}
         */
        BoundedEntryWeigher(final EntryWeigher<? super K, ? super V> weigher) {
            Assert.notNull(weigher);
            this.weigher = weigher;
        }

        /**
         * Returns the weight of the entry for the given key and value.
         *
         * @param key   the key of the entry
         * @param value the value of the entry
         * @return the weight of the entry (must be at least 1)
         * @throws IllegalArgumentException if the delegate returns a weight below one
         */
        @Override
        public int weightOf(final K key, final V value) {
            final int weight = weigher.weightOf(key, value);
            Assert.isTrue(weight >= 1);
            return weight;
        }

        /**
         * Serializes the underlying weigher instead of its validation wrapper.
         *
         * @return delegate weigher
         */
        Object writeReplace() {
            return weigher;
        }

    }

    /**
     * A proxy that is serialized instead of the map. The page-replacement algorithm's data structures are not
     * serialized so the deserialized instance contains only the entries. This is acceptable as caches hold transient
     * data that is recomputable and serialization would tend to be used as a fast warm-up process.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @author Kimi Liu
     */
    static final class SerializationProxy<K, V> implements Serializable {

        @Serial
        private static final long serialVersionUID = 2535895000001L;;

        /**
         * Entry-weighing strategy restored with the map.
         */
        final EntryWeigher<? super K, ? super V> weigher;

        /**
         * Eviction listener restored with the map.
         */
        final BiConsumerX<K, V> listener;

        /**
         * Concurrency sizing hint restored with the map.
         */
        final int concurrencyLevel;

        /**
         * Entry snapshot serialized by the proxy.
         */
        final Map<K, V> data;

        /**
         * Maximum weighted capacity restored with the map.
         */
        final long capacity;

        /**
         * Captures serializable map configuration and entries.
         *
         * @param map map to serialize
         */
        SerializationProxy(final ConcurrentLinkedHashMap<K, V> map) {
            concurrencyLevel = map.concurrencyLevel;
            data = new HashMap<>(map);
            capacity = map.capacity.get();
            listener = map.listener;
            weigher = map.weigher;
        }

        /**
         * Reconstructs a map from the serialized configuration and entries.
         *
         * @return reconstructed concurrent map
         */
        Object readResolve() {
            final ConcurrentLinkedHashMap<K, V> map = new Builder<K, V>().concurrencyLevel(concurrencyLevel)
                    .maximumWeightedCapacity(capacity).listener(listener).weigher(weigher).build();
            map.putAll(data);
            return map;
        }

    }

    /**
     * A builder that creates {@link ConcurrentLinkedHashMap} instances. It provides a flexible approach for
     * constructing customized instances with a named parameter syntax. It can be used in the following manner:
     *
     * <pre>{@code
     *
     * ConcurrentMap<Vertex, Set<Edge>> graph = new Builder<Vertex, Set<Edge>>().maximumWeightedCapacity(5000)
     *         .weigher(Weighers.<Edge>set()).build();
     * }</pre>
     *
     * @param <K> the key type
     * @param <V> the value type
     * @author Kimi Liu
     */
    public static class Builder<K, V> {

        /**
         * Listener notified for evicted entries.
         */
        BiConsumerX<K, V> listener;

        /**
         * Strategy used to weigh entries.
         */
        EntryWeigher<? super K, ? super V> weigher;

        /**
         * Expected concurrent update level.
         */
        int concurrencyLevel;

        /**
         * Initial hash-table capacity.
         */
        int initialCapacity;

        /**
         * Maximum weighted capacity.
         */
        long capacity;

        /**
         * Constructs a new Builder.
         */
        public Builder() {
            capacity = -1;
            weigher = Weighers.entrySingleton();
            initialCapacity = Normal._16;
            concurrencyLevel = Normal._16;
            listener = (BiConsumerX<K, V>) DiscardingListener.INSTANCE;
        }

        /**
         * Specifies the initial capacity of the hash table (default {@code 16}). This is the number of data-value pairs
         * that the hash table can hold before a resize operation is required.
         *
         * @param initialCapacity the initial capacity used to size the hash table to accommodate this many entries.
         * @return this
         * @throws IllegalArgumentException if the initialCapacity is negative
         */
        public Builder<K, V> initialCapacity(final int initialCapacity) {
            Assert.isTrue(initialCapacity >= 0);
            this.initialCapacity = initialCapacity;
            return this;
        }

        /**
         * Specifies the maximum weighted capacity to coerce the map to and may exceed it temporarily.
         *
         * @param capacity the weighted threshold to bound the map by
         * @return this
         * @throws IllegalArgumentException if the maximumWeightedCapacity is negative
         */
        public Builder<K, V> maximumWeightedCapacity(final long capacity) {
            Assert.isTrue(capacity >= 0);
            this.capacity = capacity;
            return this;
        }

        /**
         * Specifies the estimated number of concurrently updating threads. The implementation performs internal sizing
         * to try to accommodate this many threads (default {@code 16}).
         *
         * @param concurrencyLevel the estimated number of concurrently updating threads
         * @return this
         * @throws IllegalArgumentException if the concurrencyLevel is less than or equal to zero
         */
        public Builder<K, V> concurrencyLevel(final int concurrencyLevel) {
            Assert.isTrue(concurrencyLevel > 0);
            this.concurrencyLevel = concurrencyLevel;
            return this;
        }

        /**
         * Specifies an optional listener that is registered for notification when an entry is evicted.
         *
         * @param listener the object to forward evicted entries to
         * @return this
         * @throws IllegalArgumentException if the listener is null
         */
        public Builder<K, V> listener(final BiConsumerX<K, V> listener) {
            Assert.notNull(listener);
            this.listener = listener;
            return this;
        }

        /**
         * Specifies an algorithm to determine how many the units of capacity a value consumes. The default algorithm
         * bounds the map by the number of key-value pairs by giving each entry a weight of {@code 1}. A custom weigher
         * must return a positive weight for every stored value.
         *
         * @param weigher the algorithm determining a value's capacity weight
         * @return this
         * @throws IllegalArgumentException if the weigher is null
         */
        public Builder<K, V> weigher(final Weigher<? super V> weigher) {
            this.weigher = (weigher == Weighers.singleton()) ? Weighers.entrySingleton()
                    : new BoundedEntryWeigher<>(Weighers.asEntryWeigher(weigher));
            return this;
        }

        /**
         * Specifies an algorithm to determine how many the units of capacity an entry consumes. The default algorithm
         * bounds the map by the number of key-value pairs by giving each entry a weight of {@code 1}. A custom weigher
         * must return a positive weight for every stored entry.
         *
         * @param weigher the algorithm determining an entry's capacity weight
         * @return this
         * @throws IllegalArgumentException if the weigher is null
         */
        public Builder<K, V> weigher(final EntryWeigher<? super K, ? super V> weigher) {
            this.weigher = (weigher == Weighers.entrySingleton()) ? Weighers.entrySingleton()
                    : new BoundedEntryWeigher<>(weigher);
            return this;
        }

        /**
         * Creates a new {@link ConcurrentLinkedHashMap} instance.
         *
         * @return ConcurrentLinkedHashMap
         * @throws IllegalStateException if the maximum weighted capacity was not set
         */
        public ConcurrentLinkedHashMap<K, V> build() {
            Assert.state(capacity >= 0);
            return new ConcurrentLinkedHashMap<>(this);
        }

    }

    /**
     * Reconciles a delayed add with the node's current value and links it only while it remains the live mapping. Tasks
     * run while the eviction lock is held and do not replay historical weight deltas.
     *
     * @author Kimi Liu
     */
    final class AddTask implements Runnable {

        /**
         * Node being added to the eviction policy.
         */
        final Node<K, V> node;

        /**
         * Creates an eviction-policy add task.
         *
         * @param node node to add
         */
        AddTask(final Node<K, V> node) {
            this.node = node;
        }

        /**
         * Executes the task to add the node to the eviction policy and update the weighted size.
         */
        @Override

        public void run() {
            if (reconcileWeight(node)) {
                if (!evictionDeque.contains(node)) {
                    evictionDeque.add(node);
                }
                evict();
            }
        }

    }

    /**
     * Removes a node from the page replacement policy.
     *
     * @author Kimi Liu
     */
    final class RemovalTask implements Runnable {

        /**
         * Node being removed from the eviction policy.
         */
        final Node<K, V> node;

        /**
         * Creates an eviction-policy removal task.
         *
         * @param node node to remove
         */
        RemovalTask(final Node<K, V> node) {
            this.node = node;
        }

        /**
         * Executes the task to remove the node from the eviction policy and mark it as dead.
         */
        @Override

        public void run() {
            // add may not have been processed yet
            evictionDeque.remove(node);
            makeDead(node);
        }

    }

    /**
     * Reconciles the node's current live weight and recency, then evicts on overflow. The task runs under the eviction
     * lock and does not retain or replay the weight difference that originally caused it to be queued.
     *
     * @author Kimi Liu
     */
    final class UpdateTask implements Runnable {

        /**
         * Node whose weight and recency changed.
         */
        final Node<K, V> node;

        /**
         * Creates an eviction-policy update task.
         *
         * @param node node to update
         */
        public UpdateTask(final Node<K, V> node) {
            this.node = node;
        }

        /**
         * Executes the task to update the weighted size and potentially evict entries.
         */
        @Override

        public void run() {
            if (reconcileWeight(node)) {
                if (evictionDeque.contains(node)) {
                    applyRead(node);
                } else {
                    evictionDeque.add(node);
                }
                evict();
            }
        }

    }

    /**
     * Backed key-set view whose removals also schedule the owning map's eviction-policy cleanup.
     *
     * @author Kimi Liu
     */
    final class KeySet extends AbstractSet<K> {

        /**
         * Owning map exposed through this view.
         */
        final ConcurrentLinkedHashMap<K, V> map = ConcurrentLinkedHashMap.this;

        /**
         * Returns the number of keys in the map.
         *
         * @return the number of keys
         */
        @Override
        public int size() {
            return map.size();
        }

        /**
         * Clears all keys from the map.
         */
        @Override
        public void clear() {
            map.clear();
        }

        /**
         * Returns an iterator over the keys in the map.
         *
         * @return an iterator over the keys
         */
        @Override
        public Iterator<K> iterator() {
            return new KeyIterator();
        }

        /**
         * Returns {@code true} if this set contains the specified key.
         *
         * @param object the key to check for presence
         * @return {@code true} if this set contains the specified key
         */
        @Override
        public boolean contains(final Object object) {
            return containsKey(object);
        }

        /**
         * Removes the specified key from the map.
         *
         * @param object the key to remove
         * @return {@code true} if the key was removed
         */
        @Override
        public boolean remove(final Object object) {
            return (map.remove(object) != null);
        }

        /**
         * Returns an array containing all of the keys in this set.
         *
         * @return an array containing all of the keys in this set
         */
        @Override
        public Object[] toArray() {
            return map.data.keySet().toArray();
        }

        /**
         * Returns an array containing all of the keys in this set; the runtime type of the returned array is that of
         * the specified array.
         *
         * @param <T>   the component type of the array
         * @param array the array into which the elements of this set are to be stored
         * @return an array containing all of the keys in this set
         */
        @Override
        public <T> T[] toArray(final T[] array) {
            return map.data.keySet().toArray(array);
        }

    }

    /**
     * Weakly consistent traversal of backing-map keys with policy-aware removal through the owning map.
     *
     * @author Kimi Liu
     */
    final class KeyIterator implements Iterator<K> {

        /**
         * Iterator over keys in the backing hash table.
         */
        final Iterator<K> iterator = data.keySet().iterator();

        /**
         * Key most recently returned by this iterator.
         */
        K current;

        /**
         * Returns {@code true} if the iteration has more keys.
         *
         * @return {@code true} if the iteration has more keys
         */
        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        /**
         * Returns the next element in the iteration.
         *
         * @return the next element
         * @throws NoSuchElementException if the iteration has no more elements
         */
        @Override
        public K next() {
            current = iterator.next();
            return current;
        }

        /**
         * Removes the mapping for the last returned key through the owning map. A replacement at that key is not
         * protected from this key-based removal.
         *
         * @throws IllegalStateException if no key was returned or it has already been removed
         */
        @Override
        public void remove() {
            Assert.state(current != null);
            ConcurrentLinkedHashMap.this.remove(current);
            current = null;
        }

    }

    /**
     * Backed value collection using the owning map's size, membership, clearing, and policy-aware iteration.
     *
     * @author Kimi Liu
     */
    final class Values extends AbstractCollection<V> {

        /**
         * Returns the number of values in the map.
         *
         * @return the number of values
         */
        @Override
        public int size() {
            return ConcurrentLinkedHashMap.this.size();
        }

        /**
         * Clears all values from the map.
         */
        @Override
        public void clear() {
            ConcurrentLinkedHashMap.this.clear();
        }

        /**
         * Returns an iterator over the values in the map.
         *
         * @return an iterator over the values
         */
        @Override
        public Iterator<V> iterator() {
            return new ValueIterator();
        }

        /**
         * Returns {@code true} if this collection contains the specified value.
         *
         * @param o the value to check for presence
         * @return {@code true} if this collection contains the specified value
         */
        @Override
        public boolean contains(final Object o) {
            return containsValue(o);
        }

    }

    /**
     * Weakly consistent traversal of backing-node values with removal through the owning map.
     *
     * @author Kimi Liu
     */
    final class ValueIterator implements Iterator<V> {

        /**
         * Iterator over nodes in the backing hash table.
         */
        final Iterator<Node<K, V>> iterator = data.values().iterator();

        /**
         * Node most recently returned by this iterator.
         */
        Node<K, V> current;

        /**
         * Returns {@code true} if the iteration has more values.
         *
         * @return {@code true} if the iteration has more values
         */
        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        /**
         * Returns the next value in the iteration.
         *
         * @return the next value
         * @throws NoSuchElementException if the backing node iterator is exhausted
         */
        @Override
        public V next() {
            current = iterator.next();
            return current.getValue();
        }

        /**
         * Removes the mapping for the last returned node's key through the owning map. A later replacement at that key
         * is not protected from this key-based removal.
         *
         * @throws IllegalStateException if no value was returned or it has already been removed
         */
        @Override
        public void remove() {
            Assert.state(current != null);
            ConcurrentLinkedHashMap.this.remove(current.key);
            current = null;
        }

    }

    /**
     * Backed entry set supporting absent-only insertion and conditional removal through the owning map.
     *
     * @author Kimi Liu
     */
    final class EntrySet extends AbstractSet<Entry<K, V>> {

        /**
         * Owning map exposed through this view.
         */
        final ConcurrentLinkedHashMap<K, V> map = ConcurrentLinkedHashMap.this;

        /**
         * Returns the number of entries in the map.
         *
         * @return the number of entries
         */
        @Override
        public int size() {
            return map.size();
        }

        /**
         * Clears all entries from the map.
         */
        @Override
        public void clear() {
            map.clear();
        }

        /**
         * Returns an iterator over the entries in the map.
         *
         * @return an iterator over the entries
         */
        @Override
        public Iterator<Entry<K, V>> iterator() {
            return new EntryIterator();
        }

        /**
         * Returns {@code true} if this set contains the specified entry.
         *
         * @param object the entry to check for presence
         * @return {@code true} if this set contains the specified entry
         */
        @Override
        public boolean contains(final Object object) {
            if (!(object instanceof Entry)) {
                return false;
            }
            final Entry<K, Node<K, V>> entry = (Entry<K, Node<K, V>>) object;
            final Node<K, V> node = map.data.get(entry.getKey());
            return (node != null) && (node.getValue().equals(entry.getValue()));
        }

        /**
         * Adds the specified entry to the map if not already present.
         *
         * @param entry the entry to add
         * @return {@code true} if the entry was added
         */
        @Override
        public boolean add(final Entry<K, V> entry) {
            return (map.putIfAbsent(entry.getKey(), entry.getValue()) == null);
        }

        /**
         * Removes the specified entry from the map.
         *
         * @param object the entry to remove
         * @return {@code true} if the entry was removed
         */
        @Override
        public boolean remove(final Object object) {
            if (!(object instanceof Entry<?, ?>)) {
                return false;
            }
            final Entry<K, V> entry = (Entry<K, V>) object;
            return map.remove(entry.getKey(), entry.getValue());
        }

    }

    /**
     * Weakly consistent traversal producing value snapshots whose updates write through to the owning map.
     *
     * @author Kimi Liu
     */
    final class EntryIterator implements Iterator<Entry<K, V>> {

        /**
         * Iterator over nodes in the backing hash table.
         */
        final Iterator<Node<K, V>> iterator = data.values().iterator();

        /**
         * Node most recently returned by this iterator.
         */
        Node<K, V> current;

        /**
         * Returns {@code true} if the iteration has more entries.
         *
         * @return {@code true} if the iteration has more entries
         */
        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        /**
         * Returns the next entry in the iteration.
         *
         * @return the next entry
         * @throws NoSuchElementException if the backing node iterator is exhausted
         */
        @Override
        public Entry<K, V> next() {
            current = iterator.next();
            return new WriteThroughEntry(current);
        }

        /**
         * Removes the mapping for the last returned entry's key through the owning map. A later replacement at that key
         * is not protected from this key-based removal.
         *
         * @throws IllegalStateException if no entry was returned or it has already been removed
         */
        @Override
        public void remove() {
            Assert.state(current != null);
            ConcurrentLinkedHashMap.this.remove(current.key);
            current = null;
        }

    }

    /**
     * An entry that allows updates to write through to the map.
     *
     * @author Kimi Liu
     */
    final class WriteThroughEntry extends SimpleEntry<K, V> {

        @Serial
        private static final long serialVersionUID = 2535898150001L;

        /**
         * Creates an entry view backed by a map node.
         *
         * @param node backing node
         */
        WriteThroughEntry(final Node<K, V> node) {
            super(node.key, node.getValue());
        }

        /**
         * Sets the value for this entry, writing through to the underlying map.
         *
         * @param value the new value to be stored
         * @return the value previously held by this entry snapshot, which may differ from a concurrent map value
         * @throws IllegalArgumentException if the value is {@code null} or its configured weight is non-positive
         */
        @Override
        public V setValue(final V value) {
            put(getKey(), value);
            return super.setValue(value);
        }

        /**
         * Replaces the live entry view with a detached entry for serialization.
         *
         * @return detached entry snapshot
         */
        Object writeReplace() {
            return new SimpleEntry<>(this);
        }

    }

}
