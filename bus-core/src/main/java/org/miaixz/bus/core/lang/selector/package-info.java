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
 * Provides classes related to selection strategies, often used in load balancing.
 * <p>
 * This package includes:
 * <ul>
 * <li>{@link org.miaixz.bus.core.lang.selector.Selector}: The core interface for all selectors.</li>
 * <li>{@link org.miaixz.bus.core.lang.selector.RandomSelector}: A selector that picks an element randomly.</li>
 * <li>{@link org.miaixz.bus.core.lang.selector.IncrementSelector}: A selector that picks elements in a round-robin
 * fashion.</li>
 * <li>{@link org.miaixz.bus.core.lang.selector.WeightRandomSelector}: A selector that uses a weighted random
 * algorithm.</li>
 * <li>{@link org.miaixz.bus.core.lang.selector.SmoothWeightSelector}: A selector that uses a smooth weighted
 * round-robin algorithm.</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
package org.miaixz.bus.core.lang.selector;
