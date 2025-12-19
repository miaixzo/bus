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
 * The default implementation of {@link NodeBeanFactory} for creating, getting, and setting Bean objects based on
 * {@link BeanPath} nodes. It handles various types of objects including Maps, Beans, Collections, and Arrays.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DefaultNodeBeanFactory implements NodeBeanFactory<Object> {

    /**
     * The singleton instance of {@code DefaultNodeBeanFactory}.
     */
    public static final DefaultNodeBeanFactory INSTANCE = new DefaultNodeBeanFactory();

    /**
     * Retrieves values from a Bean, Collection, or Array based on a list of names or indices. If it's a list of names,
     * it retrieves corresponding key/field values from a Map or Bean. If it's a list of numbers, it retrieves
     * corresponding indexed values from a Collection or Array.
     *
     * @param bean The Bean, Collection, or Array object.
     * @param node The {@link ListNode} containing names or indices.
     * @return The retrieved value(s).
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
                // Only supports Maps with String keys.
                map = (Map<String, Object>) bean;
            } else {
                // For one-time use, wrap the Bean to avoid unnecessary conversions.
                map = BeanKit.toBeanMap(bean);
            }
            return MapKit.getAny(map, names);
        }
    }

    /**
     * Retrieves the value for a given name from a Bean or Map. Supports accessing properties by name from Maps and
     * Beans.
     *
     * @param bean The Bean or Map object.
     * @param node The {@link NameNode} containing the name of the property.
     * @return The retrieved value.
     */
    private static Object getValueByNameNode(final Object bean, final NameNode node) {
        final String name = node.getName();
        if (Symbol.DOLLAR.equals(name)) {
            return bean;
        }

        if (bean instanceof Collection) {
            if (Symbol.STAR.equals(name)) {
                return bean;
            }
        }

        Object value = DynaBean.of(bean).get(name);
        if (null == value && StringKit.lowerFirst(ClassKit.getClassName(bean, true)).equals(name)) {
            // If the bean class name is the same as the property name, return the bean itself.
            value = bean;
        }
        return value;
    }

    /**
     * Retrieves a sub-range of values from a Collection or Array.
     *
     * @param bean The Collection or Array object.
     * @param node The {@link RangeNode} specifying the start, end, and step of the range.
     * @return A sub-collection or sub-array of values.
     * @throws UnsupportedOperationException if the bean is not a Collection or an Array.
     */
    private static Object getValueByRangeNode(final Object bean, final RangeNode node) {
        if (bean instanceof Collection) {
            return CollKit.sub((Collection<?>) bean, node.getStart(), node.getEnd(), node.getStep());
        } else if (ArrayKit.isArray(bean)) {
            return ArrayKit.sub(bean, node.getStart(), node.getEnd(), node.getStep());
        }

        throw new UnsupportedOperationException("Can not get range value for: " + bean.getClass());
    }

    /**
     * Creates a new Bean object based on the parent and the current {@link BeanPath}. If the parent is a Map, List, or
     * Array, it creates a new Map or List based on the next node type. If the parent is a regular Bean, it creates a
     * new instance of the field's type.
     *
     * @param parent   The parent Bean object.
     * @param beanPath The current {@link BeanPath} instance.
     * @return The newly created Bean object.
     * @throws IllegalArgumentException      if a field is not found for a Bean parent.
     * @throws UnsupportedOperationException if the node type is not supported for creation.
     */
    @Override
    public Object create(final Object parent, final BeanPath<Object> beanPath) {
        if (parent instanceof Map || parent instanceof List || ArrayKit.isArray(parent)) {
            // Based on the next node type, determine the type of the current node name.
            final Node node = beanPath.next().getNode();
            if (node instanceof NameNode) {
                return ((NameNode) node).isNumber() ? new ArrayList<>() : new HashMap<>();
            }
            return new HashMap<>();
        }

        // Regular Bean.
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

    /**
     * Retrieves the value of the node specified by the {@link BeanPath} from the given Bean.
     *
     * @param bean     The Bean object.
     * @param beanPath The current {@link BeanPath} instance.
     * @return The value of the node, or {@code null} if the node is empty or not found.
     * @throws UnsupportedOperationException if the node type is not supported for value retrieval.
     */
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

    /**
     * Sets the value of the node specified by the {@link BeanPath} in the given Bean.
     *
     * @param bean     The Bean object.
     * @param value    The value to set for the node.
     * @param beanPath The current {@link BeanPath} instance.
     * @return The Bean object. If the value is set on the original Bean object, the original Bean is returned;
     *         otherwise, a new Bean is returned.
     * @throws UnsupportedOperationException if the node type is not supported for value setting.
     */
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
