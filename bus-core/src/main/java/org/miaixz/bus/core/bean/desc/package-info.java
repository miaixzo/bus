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
 * @since Java 21+
 */
package org.miaixz.bus.core.bean.desc;
