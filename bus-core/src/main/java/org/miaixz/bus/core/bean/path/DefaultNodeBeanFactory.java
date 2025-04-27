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
package org.miaixz.bus.core.bean.path;

import java.lang.reflect.Field;
import java.util.*;

import org.miaixz.bus.core.bean.DynaBean;
import org.miaixz.bus.core.bean.path.node.*;
import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.*;

/**
 * 默认的Bean创建器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DefaultNodeBeanFactory implements NodeBeanFactory<Object> {

    /**
     * 单例
     */
    public static final DefaultNodeBeanFactory INSTANCE = new DefaultNodeBeanFactory();

    /**
     * 获取指定名称或下标列表对应的值 如果为name列表，则获取Map或Bean中对应key或字段值列表 如果为数字列表，则获取对应下标值列表
     *
     * @param bean Bean
     * @param node 列表节点
     * @return 值
     */
    private static Object getValueByListNode(final Object bean, final ListNode node) {
        final String[] names = node.getUnWrappedNames();

        if (bean instanceof Collection) {
            return CollKit.getAny((Collection<?>) bean, Convert.convert(int[].class, names));
        } else if (ArrayKit.isArray(bean)) {
            return ArrayKit.getAny(bean, Convert.convert(int[].class, names));
        } else {
            final Map<String, Object> map;
            if (bean instanceof Map) {
                // 只支持String为key的Map
                map = (Map<String, Object>) bean;
            } else {
                // 一次性使用，包装Bean避免无用转换
                map = BeanKit.toBeanMap(bean);
            }
            return MapKit.getAny(map, names);
        }
    }

    /**
     * 获取指定名称的值，支持Map、Bean等
     *
     * @param bean Bean
     * @param node 节点
     * @return 值
     */
    private static Object getValueByNameNode(final Object bean, final NameNode node) {
        final String name = node.getName();
        if (Symbol.DOLLAR.equals(name)) {
            return bean;
        }
        Object value = DynaBean.of(bean).get(name);
        if (null == value && StringKit.lowerFirst(ClassKit.getClassName(bean, true)).equals(name)) {
            // 如果bean类名与属性名相同，则返回bean本身
            value = bean;
        }
        return value;
    }

    /**
     * 获取指定范围的值，只支持集合和数组
     *
     * @param bean Bean
     * @param node 范围节点
     * @return 值
     */
    private static Object getValueByRangeNode(final Object bean, final RangeNode node) {
        if (bean instanceof Collection) {
            return CollKit.sub((Collection<?>) bean, node.getStart(), node.getEnd(), node.getStep());
        } else if (ArrayKit.isArray(bean)) {
            return ArrayKit.sub(bean, node.getStart(), node.getEnd(), node.getStep());
        }

        throw new UnsupportedOperationException("Can not get range value for: " + bean.getClass());
    }

    @Override
    public Object create(final Object parent, final BeanPath<Object> beanPath) {
        if (parent instanceof Map || parent instanceof List || ArrayKit.isArray(parent)) {
            // 根据下一个节点类型，判断当前节点名称对应类型
            final Node node = beanPath.next().getNode();
            if (node instanceof NameNode) {
                return ((NameNode) node).isNumber() ? new ArrayList<>() : new HashMap<>();
            }
            return new HashMap<>();
        }

        // 普通Bean
        final Node node = beanPath.getNode();
        if (node instanceof NameNode) {
            final String name = ((NameNode) node).getName();

            final Field field = FieldKit.getField(parent.getClass(), name);
            if (null == field) {
                throw new IllegalArgumentException("No field found for name: " + name);
            }
            return ReflectKit.newInstanceIfPossible(field.getType());
        }

        throw new UnsupportedOperationException("Unsupported node type: " + node.getClass());
    }

    @Override
    public Object getValue(final Object bean, final BeanPath<Object> beanPath) {
        final Node node = beanPath.getNode();
        if (null == node || node instanceof EmptyNode) {
            return null;
        } else if (node instanceof ListNode) {
            return getValueByListNode(bean, (ListNode) node);
        } else if (node instanceof NameNode) {
            return getValueByNameNode(bean, (NameNode) node);
        } else if (node instanceof RangeNode) {
            return getValueByRangeNode(bean, (RangeNode) node);
        }

        throw new UnsupportedOperationException("Unsupported node type: " + node.getClass());
    }

    @Override
    public Object setValue(final Object bean, final Object value, final BeanPath<Object> beanPath) {
        final Node node = beanPath.getNode();
        if (null == node || node instanceof EmptyNode) {
            return bean;
        } else if (node instanceof NameNode) {
            return DynaBean.of(bean).set(((NameNode) node).getName(), value).getBean();
        }

        throw new UnsupportedOperationException("Unsupported node type: " + node.getClass());
    }

}
