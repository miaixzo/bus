/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
/**
 * This package contains a universal type converter and its various implementation classes.
 * <p>
 * The conversion framework is a classic application of the Strategy Pattern, allowing for custom conversion strategies.
 * The primary interfaces for customization are:
 * <ul>
 * <li>{@link org.miaixz.bus.core.convert.Converter}: A standard conversion interface, which is invoked after a
 * type-matching strategy is applied.</li>
 * <li>{@link org.miaixz.bus.core.convert.MatcherConverter}: A {@code Converter} with a {@code match} method that
 * determines if it can handle a specific conversion.</li>
 * </ul>
 * <p>
 * The public converter implementations include:
 * <ul>
 * <li>{@link org.miaixz.bus.core.convert.RegisterConverter}: Provides a registry for both predefined and custom
 * conversion rules.</li>
 * <li>{@link org.miaixz.bus.core.convert.CompositeConverter}: A composite converter that combines registered rules,
 * special conversions (e.g., for generics), and other mechanisms to achieve universal conversion.</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
package org.miaixz.bus.core.convert;
