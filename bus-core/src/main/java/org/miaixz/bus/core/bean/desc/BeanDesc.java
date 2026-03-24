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
package org.miaixz.bus.core.bean.desc;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import org.miaixz.bus.core.lang.reflect.Invoker;

/**
 * Describes the properties of a JavaBean, serving as an alternative to {@link java.beans.BeanInfo}. This object holds
 * information about the bean's setters and getters. When searching for Getter and Setter methods, it will:
 *
 * <pre>
 * 1. Ignore case for field and method names.
 * 2. For Getters, search for methods like {@code
 * getXXX
 * }, {@code
 * isXXX
 * }, and {@code
 * getIsXXX
 * }.
 * 3. For Setters, search for methods like {@code
 * setXXX
 * } and {@code
 * setIsXXX
 * }.
 * 4. For Setters, it ignores cases where parameter values do not match field values. Therefore, if there are multiple overloaded methods with different parameter types, the first matching one will be called.
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface BeanDesc extends Serializable {

    /**
     * Retrieves a map of field names to their corresponding {@link PropDesc} objects.
     *
     * @param ignoreCase {@code true} to ignore case when matching field names, {@code false} otherwise.
     * @return A map where keys are field names and values are {@link PropDesc} objects.
     */
    Map<String, PropDesc> getPropMap(final boolean ignoreCase);

    /**
     * Retrieves the number of properties (fields) in the Bean.
     *
     * @return The number of properties.
     */
    default int size() {
        return getPropMap(false).size();
    }

    /**
     * Checks if the Bean has no properties.
     *
     * @return {@code true} if the Bean has no properties, {@code false} otherwise.
     */
    default boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Checks if the Bean has any readable fields (i.e., fields with a getter method or public access).
     *
     * @param checkTransient {@code true} to include transient fields in the check, {@code false} to ignore them.
     * @return {@code true} if there is at least one readable field, {@code false} otherwise.
     */
    default boolean isReadable(final boolean checkTransient) {
        for (final Map.Entry<String, PropDesc> entry : getPropMap(false).entrySet()) {
            if (entry.getValue().isReadable(checkTransient)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the Bean has any writable fields (i.e., fields with a setter method or public access).
     *
     * @param checkTransient {@code true} to include transient fields in the check, {@code false} to ignore them.
     * @return {@code true} if there is at least one writable field, {@code false} otherwise.
     */
    default boolean isWritable(final boolean checkTransient) {
        for (final Map.Entry<String, PropDesc> entry : getPropMap(false).entrySet()) {
            if (entry.getValue().isWritable(checkTransient)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieves a collection of all property descriptors for the Bean.
     *
     * @return A {@link Collection} of {@link PropDesc} objects.
     */
    default Collection<PropDesc> getProps() {
        return getPropMap(false).values();
    }

    /**
     * Retrieves the property descriptor for a given field name.
     *
     * @param fieldName The name of the field.
     * @return The {@link PropDesc} object for the field, or {@code null} if not found.
     */
    default PropDesc getProp(final String fieldName) {
        return getPropMap(false).get(fieldName);
    }

    /**
     * Retrieves the {@link Invoker} for the getter method of a given field.
     *
     * @param fieldName The name of the field.
     * @return The {@link Invoker} for the getter method, or {@code null} if no getter exists.
     */
    default Invoker getGetter(final String fieldName) {
        final PropDesc desc = getProp(fieldName);
        return null == desc ? null : desc.getGetter();
    }

    /**
     * Retrieves the {@link Invoker} for the setter method of a given field.
     *
     * @param fieldName The name of the field.
     * @return The {@link Invoker} for the setter method, or {@code null} if no setter exists.
     */
    default Invoker getSetter(final String fieldName) {
        final PropDesc desc = getProp(fieldName);
        return null == desc ? null : desc.getSetter();
    }

}
