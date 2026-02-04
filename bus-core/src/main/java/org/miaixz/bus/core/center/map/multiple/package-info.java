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
 * This package provides specialized {@link java.util.Map} implementations that handle multiple values per key or use
 * multiple keys to map to a single value, extending the standard map functionalities. It includes implementations for:
 * <ul>
 * <li>{@code MultiValueMap}: A map where a single key can be associated with multiple values, typically stored in a
 * collection.</li>
 * <li>{@code Table}: A data structure that uses two keys (row and column) to map to a single value, similar to a
 * spreadsheet.</li>
 * </ul>
 *
 * <p>
 * The hierarchy for multi-value maps is as follows:
 * 
 * 
 * <pre>
 *                   MultiValueMap
 *                         |
 *                   AbstractCollValueMap
 *                         ||
 *   [CollectionValueMap, SetValueMap, ListValueMap]
 * </pre>
 * 
 * <p>
 * The hierarchy for table-like maps is as follows:
 * 
 * 
 * <pre>
 *                       Table
 *                         |
 *                      AbstractTable
 *                         ||
 *                    [RowKeyTable]
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
package org.miaixz.bus.core.center.map.multiple;
