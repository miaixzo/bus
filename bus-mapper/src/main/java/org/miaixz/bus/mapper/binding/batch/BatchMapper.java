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
package org.miaixz.bus.mapper.binding.batch;

import java.util.List;

import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Lang;
import org.apache.ibatis.annotations.Param;
import org.miaixz.bus.mapper.Caching;
import org.miaixz.bus.mapper.provider.BatchProvider;

/**
 * A mapper interface for native batch SQL writes.
 *
 * <p>
 * This interface is for dialect-aware, SQL-optimized bulk writes. It focuses on generating native batch SQL such as
 * multi-values {@code INSERT} and database-specific native {@code UPSERT} statements.
 * </p>
 *
 * <p>
 * It is intentionally different from {@link org.miaixz.bus.mapper.binding.list.ListMapper}:
 * </p>
 * <ul>
 * <li><b>BatchMapper:</b> Native SQL first, stronger dialect dependency, best insert/upsert throughput.</li>
 * <li><b>ListMapper:</b> Generic list processing first, better suited for batch update and cross-database
 * fallback.</li>
 * </ul>
 *
 * <p>
 * Choose this interface when:
 * </p>
 * <ul>
 * <li>You need maximum insert throughput.</li>
 * <li>You want to use database-native batch UPSERT syntax.</li>
 * <li>The target dialect explicitly supports the generated SQL.</li>
 * </ul>
 *
 * <p>
 * Do not treat this interface as the default choice for every batch write. If batch updates are required, or native
 * UPSERT is not guaranteed across dialects, prefer {@link org.miaixz.bus.mapper.binding.list.ListMapper}.
 * </p>
 *
 * <p>
 * Performance comparison (10,000 records):
 * </p>
 * <ul>
 * <li>Traditional loop insert: ~2000-3000ms</li>
 * <li>JDBC Batch: ~500-800ms (3-4x improvement)</li>
 * <li>Multi-Values: ~150-200ms (10-15x improvement)</li>
 * </ul>
 *
 * <p>
 * Usage example:
 * </p>
 *
 * <pre>{@code
 * 
 * public interface UserMapper extends BatchMapper<User> {
 * }
 *
 * // Native batch insert
 * int count = userMapper.insertBatch(userList);
 *
 * // Native batch upsert
 * int count = userMapper.insertUpBatch(userList);
 * }</pre>
 *
 * @param <T> The type of the entity class.
 * @author Kimi Liu
 * @since Java 21+
 */
public interface BatchMapper<T> {

    /**
     * Batch insert entities using native multi-values SQL.
     *
     * <p>
     * This method targets high-throughput insertion and usually generates a single SQL statement like:
     * </p>
     *
     * <pre>
     * INSERT INTO table (col1, col2, col3) VALUES
     * (?, ?, ?),
     * (?, ?, ?),
     * ...
     * </pre>
     *
     * <p>
     * Compared with {@code ListMapper.insertList(...)}, this method is optimized for native SQL efficiency rather than
     * generic list-processing compatibility.
     * </p>
     *
     * @param list The list of entities to insert.
     * @return The number of affected rows.
     */
    @Lang(Caching.class)
    @InsertProvider(type = BatchProvider.class, method = "insertBatch")
    int insertBatch(@Param("list") List<T> list);

    /**
     * Batch upsert entities using database-native UPSERT syntax.
     *
     * <p>
     * This method is dialect-sensitive and only works correctly on databases with stable native batch UPSERT support.
     * It generates database-specific SQL such as:
     * </p>
     * <ul>
     * <li>MySQL: INSERT ... ON DUPLICATE KEY UPDATE</li>
     * <li>PostgreSQL: INSERT ... ON CONFLICT DO UPDATE</li>
     * <li>SQLite: INSERT OR REPLACE</li>
     * </ul>
     *
     * <p>
     * If the target database does not support native batch UPSERT SQL, callers should fall back to
     * {@code insertBatch(...)} plus update operations, or to single-row processing at the service layer.
     * </p>
     *
     * @param list The list of entities to upsert.
     * @return The number of affected rows.
     */
    @Lang(Caching.class)
    @InsertProvider(type = BatchProvider.class, method = "insertUpBatch")
    int insertUpBatch(@Param("list") List<T> list);

    /**
     * Batch insert entities with native selective batch SQL.
     *
     * <p>
     * Only non-null fields are included in the generated batch insert statement. This method keeps the same native SQL
     * orientation as {@link #insertBatch(List)}, but with selective column generation.
     * </p>
     *
     * @param list The list of entities to insert.
     * @return The number of affected rows.
     */
    @Lang(Caching.class)
    @InsertProvider(type = BatchProvider.class, method = "insertSelectiveBatch")
    int insertSelectiveBatch(@Param("list") List<T> list);

}
