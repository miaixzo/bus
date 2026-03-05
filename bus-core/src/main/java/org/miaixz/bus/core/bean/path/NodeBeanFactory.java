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
package org.miaixz.bus.core.bean.path;

/**
 * A factory interface for creating, getting, and setting Bean objects corresponding to {@link BeanPath} nodes.
 *
 * @param <T> The type of the Bean.
 * @author Kimi Liu
 * @since Java 17+
 */
public interface NodeBeanFactory<T> {

    /**
     * Creates a new Bean object for the current path. For example, if the parent object is 'a' and the {@code beanPath}
     * is 'a.b', then this method creates the Bean corresponding to 'a.b.c'. The given parent 'a' is guaranteed to
     * exist, but if the Bean corresponding to 'b' in the current path does not exist, the object created is the value
     * of 'b', which is represented by 'c'.
     *
     * @param parent   The parent Bean object.
     * @param beanPath The current {@link BeanPath} instance.
     * @return The newly created Bean object.
     */
    T of(final T parent, final BeanPath<T> beanPath);

    /**
     * Retrieves the value of the node corresponding to the given Bean and {@link BeanPath}.
     *
     * @param bean     The Bean object.
     * @param beanPath The current {@link BeanPath} instance.
     * @return The value of the node.
     */
    Object getValue(T bean, final BeanPath<T> beanPath);

    /**
     * Sets the value of the node corresponding to the given Bean and {@link BeanPath}.
     *
     * @param bean     The Bean object.
     * @param value    The value to set for the node.
     * @param beanPath The current {@link BeanPath} instance.
     * @return The Bean object. If the value is set on the original Bean object, the original Bean is returned;
     *         otherwise, a new Bean is returned.
     */
    T setValue(T bean, Object value, final BeanPath<T> beanPath);

}
