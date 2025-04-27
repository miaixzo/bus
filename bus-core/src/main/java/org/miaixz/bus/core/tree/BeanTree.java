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
 * 本类是用于构建树的工具类，特点是采取lambda，以及满足指定类型的Bean进行树操作 Bean需要满足三个属性：
 * <ul>
 * <li>包含不为null的主键(例如id)</li>
 * <li>包含容许为null的关联外键(例如parentId)</li>
 * <li>包含自身的子集，例如类型为List的children</li>
 * </ul>
 * 本类的构建方法是通过{@code BeanTree.of} 进行构建，例如：
 * 
 * <pre>{@code
 * final BeanTree beanTree = BeanTree.of(JavaBean::getId, JavaBean::getParentId, null, JavaBean::getChildren,
 *         JavaBean::setChildren);
 * }</pre>
 * 
 * 得到的BeanTree实例可以调用toTree方法，将集合转换为树，例如：
 * 
 * <pre>{@code
 * final List<JavaBean> javaBeanTree = beanTree.toTree(originJavaBeanList);
 * }</pre>
 * 
 * 也可以将已有的树转换为集合，例如：
 * 
 * <pre>{@code
 * final List<JavaBean> javaBeanList = beanTree.flat(originJavaBeanTree);
 * }</pre>
 *
 * <p>
 * 最后，引用一句电影经典台词： 无处安放的双手，以及无处安放的灵魂。《Hello!树先生》
 * </p>
 *
 * @param <T> Bean类型
 * @param <R> 主键、外键类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class BeanTree<T, R extends Comparable<R>> {

    /**
     * 主键getter
     */
    private final FunctionX<T, R> idGetter;
    /**
     * 外键getter
     */
    private final FunctionX<T, R> pidGetter;
    /**
     * 外键匹配值(保留此属性主要是性能较外键条件匹配稍微好一点)
     */
    private final R pidValue;
    /**
     * 外键匹配条件
     */
    private final PredicateX<T> parentPredicate;
    /**
     * 子集getter
     */
    private final FunctionX<T, List<T>> childrenGetter;
    /**
     * 子集setter
     */
    private final BiConsumerX<T, List<T>> childrenSetter;

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
     * 构建BeanTree
     *
     * @param idGetter       主键getter，例如 {@code JavaBean::getId}
     * @param pidGetter      外键getter，例如 {@code JavaBean::getParentId}
     * @param pidValue       根节点的外键值，例如 {@code null}
     * @param childrenGetter 子集getter，例如 {@code JavaBean::getChildren}
     * @param childrenSetter 子集setter，例如 {@code JavaBean::setChildren}
     * @param <T>            Bean类型
     * @param <R>            主键、外键类型
     * @return BeanTree
     */
    public static <T, R extends Comparable<R>> BeanTree<T, R> of(final FunctionX<T, R> idGetter,
            final FunctionX<T, R> pidGetter, final R pidValue, final FunctionX<T, List<T>> childrenGetter,
            final BiConsumerX<T, List<T>> childrenSetter) {
        return new BeanTree<>(idGetter, pidGetter, pidValue, null, childrenGetter, childrenSetter);
    }

    /**
     * 构建BeanTree
     *
     * @param idGetter        主键getter，例如 {@code JavaBean::getId}
     * @param pidGetter       外键getter，例如 {@code JavaBean::getParentId}
     * @param parentPredicate 根节点判断条件，例如 {@code o -> Objects.isNull(o.getParentId())}
     * @param childrenGetter  子集getter，例如 {@code JavaBean::getChildren}
     * @param childrenSetter  子集setter，例如 {@code JavaBean::setChildren}
     * @param <T>             Bean类型
     * @param <R>             主键、外键类型
     * @return BeanTree
     */
    public static <T, R extends Comparable<R>> BeanTree<T, R> ofMatch(final FunctionX<T, R> idGetter,
            final FunctionX<T, R> pidGetter, final PredicateX<T> parentPredicate,
            final FunctionX<T, List<T>> childrenGetter, final BiConsumerX<T, List<T>> childrenSetter) {
        return new BeanTree<>(idGetter, pidGetter, null,
                Objects.requireNonNull(parentPredicate, "parentPredicate must not be null"), childrenGetter,
                childrenSetter);
    }

    /**
     * 将集合转换为树
     *
     * @param list 集合
     * @return 转换后的树
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
     * 将树扁平化为集合，相当于将树里的所有节点都放到一个集合里
     * <p>
     * 本方法会主动将节点的子集合字段置为null
     * </p>
     *
     * @param tree 树
     * @return 集合
     */
    public List<T> flat(final List<T> tree) {
        final AtomicReference<Function<T, EasyStream<T>>> recursiveRef = new AtomicReference<>();
        final Function<T, EasyStream<T>> recursive = e -> EasyStream.of(childrenGetter.apply(e))
                .flat(recursiveRef.get()).unshift(e);
        recursiveRef.set(recursive);
        return EasyStream.of(tree).flat(recursive).peek(e -> childrenSetter.accept(e, null)).toList();
    }

    /**
     * 树的过滤操作，本方法一般适用于寻找某人所在部门以及所有上级部门类似的逻辑 通过{@link PredicateX}指定的过滤规则，本节点或子节点满足过滤条件，则保留当前节点，否则抛弃节点及其子节点
     * 即，一条路径上只要有一个节点符合条件，就保留整条路径上的节点
     *
     * @param tree      树
     * @param condition 节点过滤规则函数，只需处理本级节点本身即可，{@link PredicateX#test(Object)}为{@code true}保留
     * @return 过滤后的树
     */
    public List<T> filter(final List<T> tree, final PredicateX<T> condition) {
        Objects.requireNonNull(condition, "filter condition must be not null");
        final AtomicReference<Predicate<T>> recursiveRef = new AtomicReference<>();
        final Predicate<T> recursive = PredicateX.multiOr(condition::test,
                e -> Optional.ofEmptyAble(childrenGetter.apply(e))
                        .map(children -> EasyStream.of(children).filter(recursiveRef.get()).toList())
                        .ifPresent(children -> childrenSetter.accept(e, children)).filter(s -> !s.isEmpty())
                        .isPresent());
        recursiveRef.set(recursive);
        return EasyStream.of(tree).filter(recursive).toList();
    }

    /**
     * 树节点遍历操作
     *
     * @param tree   数
     * @param action 操作
     * @return 树
     */
    public List<T> forEach(final List<T> tree, final ConsumerX<T> action) {
        Objects.requireNonNull(action, "action must be not null");
        final AtomicReference<Consumer<T>> recursiveRef = new AtomicReference<>();
        final Consumer<T> recursive = ConsumerX.multi(action::accept, e -> Optional.ofEmptyAble(childrenGetter.apply(e))
                .ifPresent(children -> EasyStream.of(children).forEach(recursiveRef.get())));
        recursiveRef.set(recursive);
        EasyStream.of(tree).forEach(recursive);
        return tree;
    }

    /**
     * 内联函数，设置每个节点的子集
     *
     * @param list         集合
     * @param pIdValuesMap 父id与子集的映射
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
