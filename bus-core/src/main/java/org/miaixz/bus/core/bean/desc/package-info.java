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
/**
 * Provides classes and interfaces for describing JavaBean properties.
 * <p>
 * This package contains the core abstractions for introspecting and working with JavaBeans:
 * <ul>
 * <li>{@link org.miaixz.bus.core.bean.desc.BeanDesc}: The primary interface for describing a bean's properties,
 * providing access to property descriptors and metadata.</li>
 * <li>{@link org.miaixz.bus.core.bean.desc.PropDesc}: Describes a single property (field) of a bean, including its
 * getter, setter, and constraint information.</li>
 * <li>{@link org.miaixz.bus.core.bean.desc.BeanDescFactory}: Factory for creating {@code BeanDesc} instances, with
 * support for different introspection strategies.</li>
 * <li>Implementations: {@link org.miaixz.bus.core.bean.desc.SimpleBeanDesc},
 * {@link org.miaixz.bus.core.bean.desc.StrictBeanDesc}, and {@link org.miaixz.bus.core.bean.desc.RecordBeanDesc} for
 * different bean types.</li>
 * </ul>
 * <p>
 * These descriptors serve as an alternative to {@code java.beans.BeanInfo}, with a more flexible API that ignores case
 * for field and method names, and supports various method naming conventions (e.g., {@code getXXX}, {@code isXXX},
 * {@code
 * getIsXXX} for getters; {@code setXXX}, {@code setIsXXX} for setters).
 *
 * @author Kimi Liu
 * @since Java 17+
 */
package org.miaixz.bus.core.bean.desc;
