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
package org.miaixz.bus.core.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.miaixz.bus.core.center.function.BiConsumerX;
import org.miaixz.bus.core.center.function.ConsumerX;
import org.miaixz.bus.core.center.function.FunctionX;
import org.miaixz.bus.core.center.function.PredicateX;
import org.miaixz.bus.core.center.stream.EasyStream;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.ListKit;

/**
 * A utility class for building tree structures from a list of beans using lambda expressions. The bean type must have
 * the following properties:
 * <ul>
 * <li>A non-null primary key (e.g., id).</li>
 * <li>A nullable foreign key to associate with a parent (e.g., parentId).</li>
 * <li>A list to hold its own children (e.g., a {@code List<T>} named children).</li>
 * </ul>
 * <p>
 * To use this class, create an instance via the static factory method {@code BeanTree.of()}, for example:
 * 
 * <pre>{@code
 * 
 * BeanTree<JavaBean, Long> beanTree = BeanTree.of(
 *         JavaBean::getId,
 *         JavaBean::getParentId,
 *         null, // Value of the parent
 *               // ID for root nodes
 *         JavaBean::getChildren,
 *         JavaBean::setChildren);
 * }</pre>
 * <p>
 * Once you have a {@code BeanTree} instance, you can convert a flat list into a tree:
 * 
 * <pre>{@code
 * 
 * List<JavaBean> tree = beanTree.toTree(flatList);
 * }</pre>
 * <p>
 * You can also flatten an existing tree structure back into a list:
 * 
 * <pre>{@code
 * 
 * List<JavaBean> flatList = beanTree.flat(tree);
 * }</pre>
 *
 * @param <T> The type of the bean.
 * @param <R> The type of the primary key and foreign key.
 * @author Kimi Liu
 * @since Java 17+
 */
public class BeanTree<T, R extends Comparable<R>> {

    /**
     * The getter for the primary key.
     */
    private final FunctionX<T, R> idGetter;
    /**
     * The getter for the parent ID (foreign key).
     */
    private final FunctionX<T, R> pidGetter;
    /**
     * The value of the parent ID for root nodes. Using this is slightly more performant than a predicate.
     */
    private final R pidValue;
    /**
     * The predicate to identify root nodes.
     */
    private final PredicateX<T> parentPredicate;
    /**
     * The getter for the list of children.
     */
    private final FunctionX<T, List<T>> childrenGetter;
    /**
     * The setter for the list of children.
     */
    private final BiConsumerX<T, List<T>> childrenSetter;

    /**
     * Private constructor to initialize the {@code BeanTree}.
     *
     * @param idGetter        The getter for the primary key.
     * @param pidGetter       The getter for the parent ID.
     * @param pidValue        The parent ID value that identifies a root node.
     * @param parentPredicate The predicate to identify a root node.
     * @param childrenGetter  The getter for the children list.
     * @param childrenSetter  The setter for the children list.
     */
    private BeanTree(final FunctionX<T, R> idGetter, final FunctionX<T, R> pidGetter, final R pidValue,
            final PredicateX<T> parentPredicate, final FunctionX<T, List<T>> childrenGetter,
            final BiConsumerX<T, List<T>> childrenSetter) {
        this.idGetter = Objects.requireNonNull(idGetter, "idGetter must not be null");
        this.pidGetter = Objects.requireNonNull(pidGetter, "pidGetter must not be null");
        this.pidValue = pidValue;
        this.parentPredicate = parentPredicate;
        this.childrenGetter = Objects.requireNonNull(childrenGetter, "childrenGetter must not be null");
        this.childrenSetter = Objects.requireNonNull(childrenSetter, "childrenSetter must not be null");
    }

    /**
     * Creates a {@code BeanTree} instance.
     *
     * @param idGetter       The getter for the primary key (e.g., {@code JavaBean::getId}).
     * @param pidGetter      The getter for the parent ID (e.g., {@code JavaBean::getParentId}).
     * @param pidValue       The parent ID value for root nodes (e.g., {@code null}).
     * @param childrenGetter The getter for the children list (e.g., {@code JavaBean::getChildren}).
     * @param childrenSetter The setter for the children list (e.g., {@code JavaBean::setChildren}).
     * @param <T>            The type of the bean.
     * @param <R>            The type of the primary and foreign keys.
     * @return A new {@code BeanTree} instance.
     */
    public static <T, R extends Comparable<R>> BeanTree<T, R> of(
            final FunctionX<T, R> idGetter,
            final FunctionX<T, R> pidGetter,
            final R pidValue,
            final FunctionX<T, List<T>> childrenGetter,
            final BiConsumerX<T, List<T>> childrenSetter) {
        return new BeanTree<>(idGetter, pidGetter, pidValue, null, childrenGetter, childrenSetter);
    }

    /**
     * Creates a {@code BeanTree} instance using a predicate to find root nodes.
     *
     * @param idGetter        The getter for the primary key (e.g., {@code JavaBean::getId}).
     * @param pidGetter       The getter for the parent ID (e.g., {@code JavaBean::getParentId}).
     * @param parentPredicate The predicate to identify root nodes (e.g., {@code o -> Objects.isNull(o.getParentId())}).
     * @param childrenGetter  The getter for the children list (e.g., {@code JavaBean::getChildren}).
     * @param childrenSetter  The setter for the children list (e.g., {@code JavaBean::setChildren}).
     * @param <T>             The type of the bean.
     * @param <R>             The type of the primary and foreign keys.
     * @return A new {@code BeanTree} instance.
     */
    public static <T, R extends Comparable<R>> BeanTree<T, R> ofMatch(
            final FunctionX<T, R> idGetter,
            final FunctionX<T, R> pidGetter,
            final PredicateX<T> parentPredicate,
            final FunctionX<T, List<T>> childrenGetter,
            final BiConsumerX<T, List<T>> childrenSetter) {
        return new BeanTree<>(idGetter, pidGetter, null,
                Objects.requireNonNull(parentPredicate, "parentPredicate must not be null"), childrenGetter,
                childrenSetter);
    }

    /**
     * Converts a flat list of items into a tree structure.
     *
     * @param list The flat list of items.
     * @return The list of root nodes of the tree.
     */
    public List<T> toTree(final List<T> list) {
        if (CollKit.isEmpty(list)) {
            return ListKit.zero();
        }
        if (Objects.isNull(parentPredicate)) {
            final Map<R, List<T>> pIdValuesMap = EasyStream.of(list).peek(
                    e -> Objects.requireNonNull(idGetter.apply(e), () -> "The data of tree node must not be null " + e))
                    .group(pidGetter);
            final List<T> parents = pIdValuesMap.getOrDefault(pidValue, new ArrayList<>());
            findChildren(list, pIdValuesMap);
            return parents;
        }
        final List<T> parents = new ArrayList<>();
        final Map<R, List<T>> pIdValuesMap = EasyStream.of(list).peek(e -> {
            if (parentPredicate.test(e)) {
                parents.add(e);
            }
            Objects.requireNonNull(idGetter.apply(e));
        }).group(pidGetter);
        findChildren(list, pIdValuesMap);
        return parents;
    }

    /**
     * Flattens a tree structure into a single list of nodes. This method will set the children list of each node to
     * {@code null}.
     *
     * @param tree The tree structure (list of root nodes).
     * @return The flattened list of all nodes in the tree.
     */
    public List<T> flat(final List<T> tree) {
        final AtomicReference<Function<T, EasyStream<T>>> recursiveRef = new AtomicReference<>();
        final Function<T, EasyStream<T>> recursive = e -> EasyStream.of(childrenGetter.apply(e))
                .flat(recursiveRef.get()).unshift(e);
        recursiveRef.set(recursive);
        return EasyStream.of(tree).flat(recursive).peek(e -> childrenSetter.accept(e, null)).toList();
    }

    /**
     * Filters the tree. A node is kept if it or any of its descendants match the given condition. This is useful for
     * scenarios like finding an employee's department and all its parent departments. If a child node matches, its
     * parent nodes will be kept to preserve the path from the root.
     *
     * @param tree      The tree to filter.
     * @param condition The condition to test on each node. If a node returns {@code true}, it is kept.
     * @return The filtered tree.
     */
    public List<T> filter(final List<T> tree, final PredicateX<T> condition) {
        Objects.requireNonNull(condition, "filter condition must be not null");
        final AtomicReference<Predicate<T>> recursiveRef = new AtomicReference<>();
        final Predicate<T> recursive = PredicateX.multiOr(
                condition::test,
                e -> Optional.ofEmptyAble(childrenGetter.apply(e))
                        .map(children -> EasyStream.of(children).filter(recursiveRef.get()).toList())
                        .ifPresent(children -> childrenSetter.accept(e, children)).filter(s -> !s.isEmpty())
                        .isPresent());
        recursiveRef.set(recursive);
        return EasyStream.of(tree).filter(recursive).toList();
    }

    /**
     * Performs an action on each node in the tree (depth-first).
     *
     * @param tree   The tree to iterate over.
     * @param action The action to perform on each node.
     * @return The original tree.
     */
    public List<T> forEach(final List<T> tree, final ConsumerX<T> action) {
        Objects.requireNonNull(action, "action must be not null");
        final AtomicReference<Consumer<T>> recursiveRef = new AtomicReference<>();
        final Consumer<T> recursive = ConsumerX.multi(
                action::accept,
                e -> Optional.ofEmptyAble(childrenGetter.apply(e))
                        .ifPresent(children -> EasyStream.of(children).forEach(recursiveRef.get())));
        recursiveRef.set(recursive);
        EasyStream.of(tree).forEach(recursive);
        return tree;
    }

    /**
     * Private helper method to find and set the children for each node in the list.
     *
     * @param list         The full list of nodes.
     * @param pIdValuesMap A map from parent ID to a list of its direct children.
     */
    private void findChildren(final List<T> list, final Map<R, List<T>> pIdValuesMap) {
        for (final T node : list) {
            final List<T> children = pIdValuesMap.get(idGetter.apply(node));
            if (children != null) {
                childrenSetter.accept(node, children);
            }
        }
    }

}
