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
package org.miaixz.bus.starter.jdbc;

import org.miaixz.bus.mapper.Holder;

/**
 * A thread-local holder for the key of the current data source.
 * <p>
 * This class extends {@link Holder} to provide a specific context for managing the data source key on a per-thread
 * basis. It is used by the dynamic data source routing mechanism to determine which data source to use for the current
 * operation.
 *
 * <p>
 * The key is set by an aspect (e.g., {@link AspectjJdbcProxy}) before a method execution and cleared afterward. The
 * {@link DynamicDataSource} then retrieves this key to select the appropriate data source.
 *
 * <p>
 * This class can be extended for further customization if needed.
 *
 * @param <T> The generic type parameter, which is part of the extended {@link Holder} class.
 * @author Kimi Liu
 * @since Java 17+
 */
public class DataSourceHolder<T> extends Holder<T> {

}
