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
