/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.center.map;

import java.util.*;

import org.miaixz.bus.core.bean.desc.PropDesc;
import org.miaixz.bus.core.xyz.BeanKit;
import org.miaixz.bus.core.xyz.ObjectKit;

/**
 * Provides a {@link Map} interface to a Java Bean, treating its properties as key-value pairs. This implementation uses
 * reflection to dynamically access and modify the bean's properties. The map's keys are the property names, and the
 * values are the corresponding property values.
 * <p>
 * Note: Operations that would alter the bean's structure, such as {@link #remove(Object)} and {@link #clear()}, are not
 * supported and will throw an {@link UnsupportedOperationException}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BeanMap implements Map<String, Object> {

    /**
     * The underlying Java Bean instance that this map wraps.
     */
    private final Object bean;
    /**
     * A cache of property descriptors for the bean, mapping property names to their metadata.
     */
    private final Map<String, PropDesc> propDescMap;

    /**
     * Constructs a new {@code BeanMap} that wraps the given bean.
     *
     * @param bean The Java Bean to be wrapped. Must not be {@code null}.
     */
    public BeanMap(final Object bean) {
        this.bean = bean;
        this.propDescMap = BeanKit.getBeanDesc(bean.getClass()).getPropMap(false);
    }

    /**
     * A static factory method to create a new {@code BeanMap} for the given bean.
     *
     * @param bean The Java Bean to be wrapped.
     * @return A new {@code BeanMap} instance.
     */
    public static BeanMap of(final Object bean) {
        return new BeanMap(bean);
    }

    /**
     * Returns the number of readable properties in the bean.
     *
     * @return The number of properties.
     */
    @Override
    public int size() {
        return this.propDescMap.size();
    }

    /**
     * Checks if the bean has any readable properties.
     *
     * @return {@code true} if the bean has no properties, {@code false} otherwise.
     */
    @Override
    public boolean isEmpty() {
        return propDescMap.isEmpty();
    }

    /**
     * Checks if the bean contains a property with the specified key (name).
     *
     * @param key The property name to check.
     * @return {@code true} if a property with the given name exists, {@code false} otherwise.
     */
    @Override
    public boolean containsKey(final Object key) {
        return this.propDescMap.containsKey(key);
    }

    /**
     * Checks if any property in the bean has the specified value.
     *
     * @param value The value to check for.
     * @return {@code true} if at least one property has the given value, {@code false} otherwise.
     */
    @Override
    public boolean containsValue(final Object value) {
        for (final PropDesc propDesc : this.propDescMap.values()) {
            if (ObjectKit.equals(propDesc.getValue(bean, false), value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieves the value of the property with the specified key (name).
     *
     * @param key The name of the property to retrieve.
     * @return The value of the property, or {@code null} if no such property exists.
     */
    @Override
    public Object get(final Object key) {
        final PropDesc propDesc = this.propDescMap.get(key);
        if (null != propDesc) {
            return propDesc.getValue(bean, false);
        }
        return null;
    }

    /**
     * Retrieves the value of a property using a nested path expression (e.g., "user.address.city").
     *
     * @param expression The nested property path expression.
     * @return The value of the specified property, or {@code null} if not found.
     */
    public Object getProperty(final String expression) {
        return BeanKit.getProperty(bean, expression);
    }

    /**
     * Sets the value of the property with the specified key (name).
     *
     * @param key   The name of the property to set.
     * @param value The new value for the property.
     * @return The old value of the property, or {@code null} if the property did not exist.
     */
    @Override
    public Object put(final String key, final Object value) {
        final PropDesc propDesc = this.propDescMap.get(key);
        if (null != propDesc) {
            final Object oldValue = propDesc.getValue(bean, false);
            propDesc.setValue(bean, value);
            return oldValue;
        }
        return null;
    }

    /**
     * Sets the value of a property using a nested path expression (e.g., "user.address.city").
     *
     * @param expression The nested property path expression.
     * @param value      The new value to set.
     */
    public void putProperty(final String expression, final Object value) {
        BeanKit.setProperty(bean, expression, value);
    }

    /**
     * This operation is not supported. Beans are fixed structures, and their properties cannot be removed.
     *
     * @param key The key to remove.
     * @return Never returns, always throws an exception.
     * @throws UnsupportedOperationException always.
     */
    @Override
    public Object remove(final Object key) {
        throw new UnsupportedOperationException("Cannot remove a field from a Bean!");
    }

    /**
     * Copies all mappings from the specified map to this bean map.
     *
     * @param m The map containing the properties to set.
     */
    @Override
    public void putAll(final Map<? extends String, ?> m) {
        m.forEach(this::put);
    }

    /**
     * This operation is not supported. Beans are fixed structures, and their properties cannot be cleared.
     *
     * @throws UnsupportedOperationException always.
     */
    @Override
    public void clear() {
        throw new UnsupportedOperationException("Cannot clear fields from a Bean!");
    }

    /**
     * Returns a {@link Set} view of the property names (keys) in this bean map.
     *
     * @return A set of property names.
     */
    @Override
    public Set<String> keySet() {
        return this.propDescMap.keySet();
    }

    /**
     * Returns a {@link Collection} view of the property values in this bean map.
     *
     * @return A collection of property values.
     */
    @Override
    public Collection<Object> values() {
        final List<Object> list = new ArrayList<>(size());
        for (final PropDesc propDesc : this.propDescMap.values()) {
            list.add(propDesc.getValue(bean, false));
        }
        return list;
    }

    /**
     * Returns a {@link Set} view of the mappings (entries) in this bean map.
     *
     * @return A set of map entries, where each entry is a property name and its value.
     */
    @Override
    public Set<Entry<String, Object>> entrySet() {
        final HashSet<Entry<String, Object>> set = new HashSet<>(size(), 1);
        this.propDescMap.forEach(
                (key, propDesc) -> set.add(new AbstractMap.SimpleEntry<>(key, propDesc.getValue(bean, false))));
        return set;
    }

}
