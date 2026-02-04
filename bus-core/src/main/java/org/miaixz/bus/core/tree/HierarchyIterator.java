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
package org.miaixz.bus.core.tree;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.miaixz.bus.core.center.stream.EasyStream;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.StreamKit;
import org.miaixz.bus.core.xyz.TreeKit;

/**
 * An iterator for traversing hierarchical structures (like trees or graphs), supporting both {@link #depthFirst
 * depth-first} and {@link #breadthFirst breadth-first} traversal modes. This iterator is designed for read-only access
 * and does not support the {@link Iterator#remove} method. For building or manipulating tree structures, see
 * {@link BeanTree} or {@link TreeKit}.
 * <p>
 * This iterator bridges the gap between hierarchical data structures and traditional collections, allowing you to
 * operate on nodes in a tree or graph as if they were in a standard iterable collection. For example:
 * 
 * <pre>{@code
 * Tree root = // ... build your tree structure
 * // Find all nodes at level 3 and sort them by weight
 * List<Tree> thirdLevelNodes = StreamKit.iterateHierarchies(root, Tree::getChildren)
 *     .filter(node -> node.getLevel() == 3)
 *     .sorted(Comparator.comparing(Tree::getWeight))
 *     .toList();
 * }</pre>
 *
 * @param <T> The type of elements in the hierarchy.
 * @author Kimi Liu
 * @see EasyStream#iterateHierarchies
 * @see StreamKit#iterateHierarchies
 * @since Java 17+
 */
public abstract class HierarchyIterator<T> implements Iterator<T> {

    /**
     * Function to discover the next level of nodes.
     */
    protected final Function<T, Collection<T>> elementDiscoverer;
    /**
     * A filter for nodes. Nodes that do not match (and their subtrees) will be ignored.
     */
    protected final Predicate<T> filter;
    /**
     * A set of nodes that have already been visited to prevent cycles.
     */
    protected final Set<T> accessed = new HashSet<>();
    /**
     * A queue of nodes waiting to be traversed.
     */
    protected final LinkedList<T> queue = new LinkedList<>();

    /**
     * Creates a new hierarchy iterator.
     *
     * @param root              The root element. It cannot be filtered out.
     * @param elementDiscoverer A function to get the children of an element.
     * @param filter            A predicate to filter elements. Non-matching elements and their children will be
     *                          skipped.
     */
    HierarchyIterator(final T root, final Function<T, Collection<T>> elementDiscoverer, final Predicate<T> filter) {
        // The root node cannot be filtered out.
        Assert.isTrue(filter.test(root), "root node cannot be filtered!");
        queue.add(root);
        this.elementDiscoverer = Assert.notNull(elementDiscoverer);
        this.filter = Assert.notNull(filter);
    }

    /**
     * Creates an iterator for breadth-first traversal of a hierarchical structure.
     *
     * @param <T>            The type of the elements.
     * @param root           The root element. It cannot be filtered out.
     * @param nextDiscoverer A function to get the children of an element.
     * @param filter         A predicate to filter elements. Non-matching elements and their children will be skipped.
     * @return A new breadth-first iterator.
     */
    public static <T> HierarchyIterator<T> breadthFirst(
            final T root,
            final Function<T, Collection<T>> nextDiscoverer,
            final Predicate<T> filter) {
        return new BreadthFirst<>(root, nextDiscoverer, filter);
    }

    /**
     * Creates an iterator for breadth-first traversal of a hierarchical structure with no filtering.
     *
     * @param <T>            The type of the elements.
     * @param root           The root element.
     * @param nextDiscoverer A function to get the children of an element.
     * @return A new breadth-first iterator.
     */
    public static <T> HierarchyIterator<T> breadthFirst(final T root, final Function<T, Collection<T>> nextDiscoverer) {
        return breadthFirst(root, nextDiscoverer, t -> true);
    }

    /**
     * Creates an iterator for depth-first traversal of a hierarchical structure.
     *
     * @param <T>            The type of the elements.
     * @param root           The root element. It cannot be filtered out.
     * @param nextDiscoverer A function to get the children of an element.
     * @param filter         A predicate to filter elements. Non-matching elements and their children will be skipped.
     * @return A new depth-first iterator.
     */
    public static <T> HierarchyIterator<T> depthFirst(
            final T root,
            final Function<T, Collection<T>> nextDiscoverer,
            final Predicate<T> filter) {
        return new DepthFirst<>(root, nextDiscoverer, filter);
    }

    /**
     * Creates an iterator for depth-first traversal of a hierarchical structure with no filtering.
     *
     * @param <T>            The type of the elements.
     * @param root           The root element.
     * @param nextDiscoverer A function to get the children of an element.
     * @return A new depth-first iterator.
     */
    public static <T> HierarchyIterator<T> depthFirst(final T root, final Function<T, Collection<T>> nextDiscoverer) {
        return depthFirst(root, nextDiscoverer, t -> true);
    }

    /**
     * Returns {@code true} if the iteration has more elements.
     *
     * @return {@code true} if the iteration has more elements.
     */
    @Override
    public boolean hasNext() {
        return !queue.isEmpty();
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return The next element in the iteration.
     * @throws NoSuchElementException if the iteration has no more elements.
     */
    @Override
    public T next() {
        if (queue.isEmpty()) {
            throw new NoSuchElementException();
        }
        final T curr = queue.removeFirst();
        accessed.add(curr);
        Collection<T> nextElements = elementDiscoverer.apply(curr);
        if (Objects.nonNull(nextElements) && !nextElements.isEmpty()) {
            nextElements = nextElements.stream().filter(filter).collect(Collectors.toList());
            collectNextElementsToQueue(nextElements);
        }
        return curr;
    }

    /**
     * Collects the next level of elements and adds them to the traversal queue.
     *
     * @param nextElements The elements to be added to the queue.
     * @see #queue
     * @see #accessed
     */
    protected abstract void collectNextElementsToQueue(final Collection<T> nextElements);

    /**
     * Implements depth-first traversal.
     *
     * @param <T> The type of the elements.
     */
    static class DepthFirst<T> extends HierarchyIterator<T> {

        /**
         * Creates a new depth-first iterator.
         *
         * @param root           The root element.
         * @param nextDiscoverer A function to get the children of an element.
         * @param filter         A predicate to filter elements.
         */
        DepthFirst(final T root, final Function<T, Collection<T>> nextDiscoverer, final Predicate<T> filter) {
            super(root, nextDiscoverer, filter);
        }

        /**
         * Adds the next elements to the front of the queue to achieve depth-first traversal.
         *
         * @param nextElements The elements to be added.
         */
        @Override
        protected void collectNextElementsToQueue(final Collection<T> nextElements) {
            int idx = 0;
            for (final T nextElement : nextElements) {
                if (!accessed.contains(nextElement)) {
                    queue.add(idx++, nextElement);
                    accessed.add(nextElement);
                }
            }
        }
    }

    /**
     * Implements breadth-first traversal.
     *
     * @param <T> The type of the elements.
     */
    static class BreadthFirst<T> extends HierarchyIterator<T> {

        /**
         * Creates a new breadth-first iterator.
         *
         * @param root           The root element.
         * @param nextDiscoverer A function to get the children of an element.
         * @param filter         A predicate to filter elements.
         */
        BreadthFirst(final T root, final Function<T, Collection<T>> nextDiscoverer, final Predicate<T> filter) {
            super(root, nextDiscoverer, filter);
        }

        /**
         * Adds the next elements to the end of the queue to achieve breadth-first traversal.
         *
         * @param nextElements The elements to be added.
         */
        @Override
        protected void collectNextElementsToQueue(final Collection<T> nextElements) {
            for (final T nextElement : nextElements) {
                if (!accessed.contains(nextElement)) {
                    queue.addLast(nextElement);
                    accessed.add(nextElement);
                }
            }
        }

    }

}
