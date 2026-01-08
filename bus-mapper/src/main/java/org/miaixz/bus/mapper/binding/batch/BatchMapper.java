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
package org.miaixz.bus.mapper.binding.batch;

import java.util.List;

import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Lang;
import org.apache.ibatis.annotations.Param;
import org.miaixz.bus.mapper.Caching;
import org.miaixz.bus.mapper.provider.BatchProvider;

/**
 * An interface for batch operations, providing high-performance bulk insert, update, and upsert methods.
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
 * // Batch insert
 * int count = userMapper.batchInsert(userList);
 *
 * // Batch upsert
 * int count = userMapper.batchUpsert(userList);
 * }</pre>
 *
 * @param <T> The type of the entity class.
 * @author Kimi Liu
 * @since Java 17+
 */
public interface BatchMapper<T> {

    /**
     * Batch insert entities using Multi-Values INSERT.
     *
     * <p>
     * Generates SQL like:
     * </p>
     *
     * <pre>
     * INSERT INTO table (col1, col2, col3) VALUES
     * (?, ?, ?),
     * (?, ?, ?),
     * ...
     * </pre>
     *
     * @param list The list of entities to insert.
     * @return The number of affected rows.
     */
    @Lang(Caching.class)
    @InsertProvider(type = BatchProvider.class, method = "insertBatch")
    int insertBatch(@Param("list") List<T> list);

    /**
     * Batch upsert entities (insert or update if exists).
     *
     * <p>
     * Generates database-specific SQL:
     * </p>
     * <ul>
     * <li>MySQL: INSERT ... ON DUPLICATE KEY UPDATE</li>
     * <li>PostgreSQL: INSERT ... ON CONFLICT DO UPDATE</li>
     * <li>SQLite: INSERT OR REPLACE</li>
     * </ul>
     *
     * @param list The list of entities to upsert.
     * @return The number of affected rows.
     */
    @Lang(Caching.class)
    @InsertProvider(type = BatchProvider.class, method = "insertUpBatch")
    int insertUpBatch(@Param("list") List<T> list);

    /**
     * Batch insert entities with selective columns (only non-null fields).
     *
     * @param list The list of entities to insert.
     * @return The number of affected rows.
     */
    @Lang(Caching.class)
    @InsertProvider(type = BatchProvider.class, method = "insertSelectiveBatch")
    int insertSelectiveBatch(@Param("list") List<T> list);

}
