/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.center.map.multiple;

import java.io.Serial;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

import org.miaixz.bus.core.xyz.CollKit;

/**
 * A {@link Map} that supports undirected graph structures, essentially an adjacency list implemented on top of
 * {@link SetValueMap}.
 *
 * @param <T> The type of the nodes.
 * @author Kimi Liu
 * @since Java 17+
 */
public class Graph<T> extends SetValueMap<T, T> {

    /**
     * Constructs a new Graph. Utility class constructor for static access.
     */
    public Graph() {
    }

    @Serial
    private static final long serialVersionUID = 2852277389299L;

    /**
     * Adds an undirected edge between two nodes.
     *
     * @param target1 The first node.
     * @param target2 The second node.
     */
    public void putEdge(final T target1, final T target2) {
        this.putValue(target1, target2);
        this.putValue(target2, target1);
    }

    /**
     * Checks if an undirected edge exists between two nodes.
     *
     * @param target1 The first node.
     * @param target2 The second node.
     * @return {@code true} if an edge exists, {@code false} otherwise.
     */
    public boolean containsEdge(final T target1, final T target2) {
        return this.getValues(target1).contains(target2) && this.getValues(target2).contains(target1);
    }

    /**
     * Removes the undirected edge between two nodes.
     *
     * @param target1 The first node.
     * @param target2 The second node.
     */
    public void removeEdge(final T target1, final T target2) {
        this.removeValue(target1, target2);
        this.removeValue(target2, target1);
    }

    /**
     * Removes a node (point/vertex) and all edges connected to it.
     *
     * @param target The node to remove.
     */
    public void removePoint(final T target) {
        final Collection<T> associatedPoints = this.remove(target);
        if (CollKit.isNotEmpty(associatedPoints)) {
            associatedPoints.forEach(p -> this.removeValue(p, target));
        }
    }

    /**
     * Checks if two nodes are connected, either directly or indirectly (i.e., if a path exists between them).
     *
     * @param target1 The first node.
     * @param target2 The second node.
     * @return {@code true} if the nodes are connected.
     */
    public boolean containsAssociation(final T target1, final T target2) {
        if (!this.containsKey(target1) || !this.containsKey(target2)) {
            return false;
        }
        final AtomicBoolean flag = new AtomicBoolean(false);
        visitAssociatedPoints(target1, t -> {
            if (Objects.equals(t, target2)) {
                flag.set(true);
                return true; // Break traversal
            }
            return false; // Continue traversal
        });
        return flag.get();
    }

    /**
     * Gets all nodes that are directly or indirectly connected to the target node, using a breadth-first search.
     *
     * @param target        The starting node.
     * @param includeTarget Whether to include the starting node in the result.
     * @return A collection of all connected nodes.
     */
    public Collection<T> getAssociatedPoints(final T target, final boolean includeTarget) {
        final Set<T> points = visitAssociatedPoints(target, t -> false);
        if (!includeTarget) {
            points.remove(target);
        }
        return points;
    }

    /**
     * Gets the adjacent nodes (neighbors) of a given node.
     *
     * @param target The node.
     * @return A collection of adjacent nodes.
     */
    public Collection<T> getAdjacentPoints(final T target) {
        return this.getValues(target);
    }

    /**
     * Performs a breadth-first traversal of all nodes connected to the given key, applying a predicate to break the
     * traversal if needed.
     *
     * @param key     The starting node key.
     * @param breaker A predicate that, if it returns {@code true}, stops the traversal.
     * @return A set of all visited nodes.
     */
    private Set<T> visitAssociatedPoints(final T key, final Predicate<T> breaker) {
        if (!this.containsKey(key)) {
            return Collections.emptySet();
        }
        final Set<T> accessed = new HashSet<>();
        final Deque<T> deque = new LinkedList<>();
        deque.add(key);
        while (!deque.isEmpty()) {
            final T t = deque.removeFirst();
            if (accessed.contains(t)) {
                continue;
            }
            accessed.add(t);

            if (breaker.test(t)) {
                break;
            }

            final Collection<T> neighbours = this.getValues(t);
            if (!neighbours.isEmpty()) {
                deque.addAll(neighbours);
            }
        }
        return accessed;
    }

}
