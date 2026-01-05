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
