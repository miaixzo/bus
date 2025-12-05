/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.cache.provider;

import java.io.Serial;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;

import org.miaixz.bus.core.lang.mutable.Mutable;
import org.miaixz.bus.core.lang.mutable.MutableObject;

/**
 * SIEVE Cache Algorithm Implementation.
 * <p>
 * SIEVE is a cache algorithm that is simpler and often more efficient than LRU. <br>
 * Core features:
 * <ul>
 * <li>On cache hit: The node's {@code visited} flag is set to true; the node is NOT moved.</li>
 * <li>On eviction: The {@code hand} pointer scans from the tail, evicting nodes where {@code visited=false}.</li>
 * <li>If the {@code hand} encounters a node with {@code visited=true}, it flips the flag to false and moves to the
 * previous node (giving it a second chance).</li>
 * <li>New nodes are added to the head with {@code visited=false}. The Hand pointer will eventually scan them, providing
 * scan resistance.</li>
 * </ul>
 *
 * @param <K> The type of keys.
 * @param <V> The type of values.
 * @author Kimi Liu
 * @since Java 17+
 */
public class SieveCache<K, V> extends LockedCache<K, V> {

    @Serial
    private static final long serialVersionUID = 2852232053892L;

    /**
     * The head node of the doubly linked list.
     */
    private SieveCacheObject<K, V> head;
    /**
     * The tail node of the doubly linked list.
     */
    private SieveCacheObject<K, V> tail;
    /**
     * The 'hand' pointer used for the SIEVE eviction scan.
     */
    private SieveCacheObject<K, V> hand;

    /**
     * Constructs a {@code SieveCache} with the specified capacity and no timeout.
     *
     * @param capacity The maximum number of items.
     */
    public SieveCache(final int capacity) {
        this(capacity, 0);
    }

    /**
     * Constructs a {@code SieveCache}.
     *
     * @param capacity The maximum number of items.
     * @param timeout  The default timeout in milliseconds.
     */
    public SieveCache(int capacity, final long timeout) {
        if (Integer.MAX_VALUE == capacity) {
            capacity -= 1;
        }

        this.capacity = capacity;
        this.timeout = timeout;

        // Initialize HashMap with capacity + 1 and load factor 1.0f to avoid resizing
        this.cacheMap = new HashMap<>(capacity + 1, 1.0f);
        this.lock = new ReentrantLock();
    }

    @Override
    protected void putWithoutLock(final K key, final V object, final long timeout) {
        final Mutable<K> keyObject = MutableObject.of(key);
        SieveCacheObject<K, V> co = (SieveCacheObject<K, V>) cacheMap.get(keyObject);

        if (co != null) {
            final SieveCacheObject<K, V> newCo = new SieveCacheObject<>(key, object, timeout);

            // Updated nodes are marked as visited to prevent immediate eviction
            newCo.visited = true;

            // Replace the old node in the list structure
            replaceNode(co, newCo);
            cacheMap.put(keyObject, newCo);
        } else {
            co = new SieveCacheObject<>(key, object, timeout);
            cacheMap.put(keyObject, co);
            addToHead(co);
            // New nodes start as not visited
            co.visited = false;

            if (cacheMap.size() > capacity) {
                pruneCache();
            }
        }
    }

    /**
     * Replaces {@code oldNode} with {@code newNode} in the doubly linked list, maintaining the list structure.
     *
     * @param oldNode The node to be replaced.
     * @param newNode The new node to insert.
     */
    private void replaceNode(final SieveCacheObject<K, V> oldNode, final SieveCacheObject<K, V> newNode) {
        newNode.prev = oldNode.prev;
        newNode.next = oldNode.next;

        // Update forward pointers
        if (oldNode.prev != null) {
            oldNode.prev.next = newNode;
        } else {
            head = newNode;
        }

        // Update backward pointers
        if (oldNode.next != null) {
            oldNode.next.prev = newNode;
        } else {
            tail = newNode;
        }

        // Transfer the 'hand' pointer to the new node to prevent accidental eviction of hot data
        if (hand == oldNode) {
            hand = newNode;
        }

        oldNode.prev = null;
        oldNode.next = null;
    }

    @Override
    protected CacheObject<K, V> getOrRemoveExpiredWithoutLock(final K key) {
        final Mutable<K> keyObject = MutableObject.of(key);
        final SieveCacheObject<K, V> co = (SieveCacheObject<K, V>) cacheMap.get(keyObject);

        if (null != co) {
            if (co.isExpired()) {
                removeWithoutLock(key);
                return null;
            }
            // Lazy promotion: mark as visited instead of moving to head
            co.visited = true;
            co.lastAccess = System.currentTimeMillis();
        }
        return co;
    }

    @Override
    protected CacheObject<K, V> removeWithoutLock(final K key) {
        final Mutable<K> keyObject = MutableObject.of(key);
        final SieveCacheObject<K, V> co = (SieveCacheObject<K, V>) cacheMap.remove(keyObject);
        if (co != null) {
            removeNode(co);
        }
        return co;
    }

    /**
     * Prunes the cache. Priorities: 1. Remove expired objects. 2. If capacity is still exceeded, perform SIEVE
     * eviction: scan from {@code hand} (initially tail). Evict nodes where {@code visited} is false. If {@code visited}
     * is true, flip it to false and continue scanning.
     */
    @Override
    protected int pruneCache() {
        int count = 0;

        // 1. Clean up expired objects first
        if (isPruneExpiredActive()) {
            final Iterator<CacheObject<K, V>> values = cacheObjIter();
            CacheObject<K, V> co;
            while (values.hasNext()) {
                co = values.next();
                if (co.isExpired()) {
                    values.remove();
                    removeNode((SieveCacheObject<K, V>) co);
                    onRemove(co.key, co.object);
                    count++;
                }
            }
        }

        // 2. SIEVE eviction policy
        if (cacheMap.size() > capacity) {
            if (hand == null) {
                hand = tail;
            }

            while (cacheMap.size() > capacity) {
                if (hand == null) {
                    hand = tail;
                }

                if (!hand.visited) {
                    // Evict unvisited node
                    final SieveCacheObject<K, V> victim = hand;
                    hand = hand.prev;

                    final Mutable<K> keyObject = MutableObject.of(victim.key);
                    cacheMap.remove(keyObject);
                    removeNode(victim);
                    onRemove(victim.key, victim.object);
                    count++;
                } else {
                    // Give a second chance: reset visited status and move hand
                    hand.visited = false;
                    hand = hand.prev;
                }
            }
        }
        return count;
    }

    /**
     * Adds a node to the head of the linked list.
     *
     * @param node The node to add.
     */
    private void addToHead(final SieveCacheObject<K, V> node) {
        node.next = head;
        node.prev = null;
        if (head != null) {
            head.prev = node;
        }
        head = node;
        if (tail == null) {
            tail = node;
        }
    }

    /**
     * Removes a node from the linked list.
     *
     * @param node The node to remove.
     */
    private void removeNode(final SieveCacheObject<K, V> node) {
        // If removing the node currently pointed to by 'hand', move 'hand' back one step
        if (node == hand) {
            hand = node.prev;
        }

        if (node.prev != null) {
            node.prev.next = node.next;
        } else {
            head = node.next;
        }

        if (node.next != null) {
            node.next.prev = node.prev;
        } else {
            tail = node.prev;
        }

        node.next = null;
        node.prev = null;
    }

    /**
     * Cache object wrapper extending {@link CacheObject} to include SIEVE-specific attributes.
     */
    private static class SieveCacheObject<K, V> extends CacheObject<K, V> {

        @Serial
        private static final long serialVersionUID = 2852232053891L;
        /**
         * Indicates if the node has been accessed recently.
         */
        boolean visited = false;
        /**
         * Pointer to the previous node in the linked list.
         */
        SieveCacheObject<K, V> prev;
        /**
         * Pointer to the next node in the linked list.
         */
        SieveCacheObject<K, V> next;

        /**
         * Constructor.
         *
         * @param key The key.
         * @param obj The value.
         * @param ttl The time-to-live in milliseconds.
         */
        protected SieveCacheObject(final K key, final V obj, final long ttl) {
            super(key, obj, ttl);
        }
    }

}
