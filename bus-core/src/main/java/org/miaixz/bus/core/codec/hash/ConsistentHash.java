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
package org.miaixz.bus.core.codec.hash;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.SortedMap;
import java.util.TreeMap;

import org.miaixz.bus.core.xyz.HashKit;

/**
 * An implementation of the Consistent Hashing algorithm. This is useful for distributing items among a set of nodes in
 * a way that minimizes remapping when nodes are added or removed.
 *
 * @param <T> The type of the node.
 * @author Kimi Liu
 * @since Java 17+
 */
public class ConsistentHash<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852259219707L;

    /**
     * The number of virtual replicas for each physical node.
     */
    private final int numberOfReplicas;
    /**
     * The hash circle, a sorted map of hash values to nodes.
     */
    private final SortedMap<Integer, T> circle = new TreeMap<>();
    /**
     * The hash function used to map nodes and keys to the circle.
     */
    Hash32<Object> hashFunc;

    /**
     * Constructs a {@code ConsistentHash} with a specified number of replicas and a collection of nodes, using the
     * default FNV1a hash algorithm.
     *
     * @param numberOfReplicas The number of virtual nodes for each physical node, to improve distribution.
     * @param nodes            The initial collection of physical nodes.
     */
    public ConsistentHash(final int numberOfReplicas, final Collection<T> nodes) {
        this.numberOfReplicas = numberOfReplicas;
        this.hashFunc = key -> HashKit.fnvHash(key.toString());
        // Initialize nodes
        for (final T node : nodes) {
            add(node);
        }
    }

    /**
     * Constructs a {@code ConsistentHash} with a custom hash function, a specified number of replicas, and a collection
     * of nodes.
     *
     * @param hashFunc         The custom hash function.
     * @param numberOfReplicas The number of virtual nodes for each physical node.
     * @param nodes            The initial collection of physical nodes.
     */
    public ConsistentHash(final Hash32<Object> hashFunc, final int numberOfReplicas, final Collection<T> nodes) {
        this.numberOfReplicas = numberOfReplicas;
        this.hashFunc = hashFunc;
        // Initialize nodes
        for (final T node : nodes) {
            add(node);
        }
    }

    /**
     * Adds a new node to the hash circle, including its virtual replicas. The hash is based on the node's
     * {@code toString()} value.
     *
     * @param node The physical node to add.
     */
    public void add(final T node) {
        for (int i = 0; i < numberOfReplicas; i++) {
            circle.put(hashFunc.hash32(node.toString() + i), node);
        }
    }

    /**
     * Removes a node and its corresponding virtual replicas from the hash circle.
     *
     * @param node The physical node to remove.
     */
    public void remove(final T node) {
        for (int i = 0; i < numberOfReplicas; i++) {
            circle.remove(hashFunc.hash32(node.toString() + i));
        }
    }

    /**
     * Gets the node responsible for the given key by finding the nearest clockwise node on the hash circle.
     *
     * @param key The key to map to a node.
     * @return The responsible node object.
     */
    public T get(final Object key) {
        if (circle.isEmpty()) {
            return null;
        }
        int hash = hashFunc.hash32(key);
        if (!circle.containsKey(hash)) {
            // Get the part of the map whose keys are greater than or equal to the hash
            SortedMap<Integer, T> tailMap = circle.tailMap(hash);
            hash = tailMap.isEmpty() ? circle.firstKey() : tailMap.firstKey();
        }
        // Direct hit or wrap-around
        return circle.get(hash);
    }

}
