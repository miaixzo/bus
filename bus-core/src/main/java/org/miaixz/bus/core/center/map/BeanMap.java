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
 * supported and will throw an {@link UnsupportedOperationException}. Collection views are backed by the bean and do not
 * support structural changes; entries support property updates through {@link Entry#setValue(Object)}.
 *
 * @author Kimi Liu
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
     * @throws NullPointerException if the bean is {@code null}
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
     * @throws NullPointerException if the bean is {@code null}
     */
    public static BeanMap of(final Object bean) {
        return new BeanMap(bean);
    }

    /**
     * Returns the number of properties represented by the bean's property descriptors.
     *
     * @return The number of properties.
     */
    @Override
    public int size() {
        return this.propDescMap.size();
    }

    /**
     * Checks whether the bean has any represented properties.
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
     * Returns a read-only, backed {@link Set} view of the property names. Structural modification must not affect the
     * shared property descriptors used by other instances of the same bean type.
     *
     * @return A set of property names.
     */
    @Override
    public Set<String> keySet() {
        return Collections.unmodifiableSet(this.propDescMap.keySet());
    }

    /**
     * Returns a read-only, backed {@link Collection} view of the property values. Each iteration reads the current
     * values from the bean; removing elements cannot remove bean properties.
     *
     * @return A collection of property values.
     */
    @Override
    public Collection<Object> values() {
        return Collections.unmodifiableCollection(new AbstractCollection<>() {

            /**
             * Creates an iterator that reads each represented property's current value from the bean.
             *
             * @return a property-value iterator in descriptor encounter order
             */
            @Override
            public Iterator<Object> iterator() {
                final Iterator<PropDesc> descriptors = propDescMap.values().iterator();
                return new Iterator<>() {

                    /**
                     * Tests whether another property descriptor remains to be read.
                     *
                     * @return {@code true} if another property value is available
                     */
                    @Override
                    public boolean hasNext() {
                        return descriptors.hasNext();
                    }

                    /**
                     * Reads the current value of the next represented property.
                     *
                     * @return the next property value, which may be {@code null}
                     * @throws NoSuchElementException if no property descriptor remains
                     */
                    @Override
                    public Object next() {
                        return descriptors.next().getValue(bean, false);
                    }
                };
            }

            /**
             * Returns the number of represented bean properties, including null-valued properties.
             *
             * @return the size of the owning bean map
             */
            @Override
            public int size() {
                return BeanMap.this.size();
            }

            /**
             * Searches the current bean-property values for the supplied value.
             *
             * @param value value to locate, including {@code null}
             * @return {@code true} if at least one current property value matches
             */
            @Override
            public boolean contains(final Object value) {
                return BeanMap.this.containsValue(value);
            }
        });
    }

    /**
     * Returns a backed {@link Set} view of the bean's property mappings. The set does not support structural changes,
     * but each entry reads its current property value and writes updates through {@link #put(String, Object)}.
     *
     * @return A set of map entries, where each entry is a property name and its value.
     */
    @Override
    public Set<Entry<String, Object>> entrySet() {
        return Collections.unmodifiableSet(new AbstractSet<>() {

            /**
             * Creates an iterator of entries that read and update the corresponding bean properties.
             *
             * @return a property-entry iterator in descriptor encounter order
             */
            @Override
            public Iterator<Entry<String, Object>> iterator() {
                final Iterator<String> names = propDescMap.keySet().iterator();
                return new Iterator<>() {

                    /**
                     * Tests whether another represented property name remains.
                     *
                     * @return {@code true} if another property entry is available
                     */
                    @Override
                    public boolean hasNext() {
                        return names.hasNext();
                    }

                    /**
                     * Creates a backed entry for the next represented property name.
                     *
                     * @return an entry that reads and writes the named property on the owning bean
                     * @throws NoSuchElementException if no property name remains
                     */
                    @Override
                    public Entry<String, Object> next() {
                        final String name = names.next();
                        return new AbstractEntry<>() {

                            /**
                             * Returns the property name captured by this entry.
                             *
                             * @return the represented bean-property name
                             */
                            @Override
                            public String getKey() {
                                return name;
                            }

                            /**
                             * Reads the property's current value from the owning bean rather than a snapshot.
                             *
                             * @return the current property value, which may be {@code null}
                             */
                            @Override
                            public Object getValue() {
                                return BeanMap.this.get(name);
                            }

                            /**
                             * Updates the represented property through the owning bean map.
                             *
                             * @param value replacement property value
                             * @return the property value observed before the update
                             */
                            @Override
                            public Object setValue(final Object value) {
                                return BeanMap.this.put(name, value);
                            }
                        };
                    }
                };
            }

            /**
             * Returns the number of represented property mappings in the owning bean map.
             *
             * @return the current property-descriptor count
             */
            @Override
            public int size() {
                return BeanMap.this.size();
            }
        });
    }

    /**
     * Compares property mappings using the standard Map content contract, including present null-valued properties.
     *
     * @param object object to compare
     * @return whether both maps contain the same mappings
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof Map<?, ?> other) || other.size() != size()) {
            return false;
        }
        try {
            for (final Entry<String, Object> entry : entrySet()) {
                final Object value = entry.getValue();
                final Object otherValue = other.get(entry.getKey());
                if (!Objects.equals(value, otherValue) || value == null && !other.containsKey(entry.getKey())) {
                    return false;
                }
            }
            return true;
        } catch (final ClassCastException | NullPointerException ignored) {
            return false;
        }
    }

    /**
     * Returns the sum of the current property entries' standard Map hash codes.
     *
     * @return the content-based hash code of the current property mappings
     */
    @Override
    public int hashCode() {
        int hash = 0;
        for (final Entry<String, Object> entry : entrySet()) {
            hash += entry.hashCode();
        }
        return hash;
    }

}
