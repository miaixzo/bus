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
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.mapper.provider;

import org.apache.ibatis.builder.annotation.ProviderContext;

/**
 * Provides dynamic SQL generation for basic CRUD operations.
 *
 * <p>
 * This class uses BasicProvider's common SQL building methods to simplify code. Reduced from ~250 lines to ~170 lines
 * (32% reduction).
 * </p>
 *
 * <p>
 * Performance optimizations:
 * </p>
 * <ul>
 * <li>Extends BasicProvider for code reuse</li>
 * <li>Uses cacheSql for unified caching mechanism</li>
 * <li>Eliminates duplicate SQL building code</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class EntityProvider extends BasicProvider {

    /**
     * Marks a method as unavailable and throws an exception.
     *
     * @param providerContext The provider context, containing method and interface information.
     * @return The cache key.
     * @throws UnsupportedOperationException if the method is not available.
     */
    public static String unsupported(ProviderContext providerContext) {
        throw new UnsupportedOperationException(providerContext.getMapperMethod().getName() + " method not available");
    }

    /**
     * Saves an entity by inserting all its fields.
     *
     * @param providerContext The provider context, containing method and interface information.
     * @return The cache key.
     */
    public static String insert(ProviderContext providerContext) {
        return cacheSql(providerContext, BasicProvider::buildInsertAll);
    }

    /**
     * Saves only the non-null fields of an entity.
     *
     * @param providerContext The provider context, containing method and interface information.
     * @return The cache key.
     */
    public static String insertSelective(ProviderContext providerContext) {
        return cacheSql(providerContext, BasicProvider::buildInsertSelective);
    }

    /**
     * Updates all fields of an entity by its primary key.
     *
     * @param providerContext The provider context, containing method and interface information.
     * @return The cache key.
     */
    public static String updateByPrimaryKey(ProviderContext providerContext) {
        return cacheSql(providerContext, BasicProvider::buildUpdateAll);
    }

    /**
     * Updates non-null fields of an entity by its primary key.
     *
     * @param providerContext The provider context, containing method and interface information.
     * @return The cache key.
     */
    public static String updateByPrimaryKeySelective(ProviderContext providerContext) {
        return cacheSql(providerContext, BasicProvider::buildUpdateSelective);
    }

    /**
     * Selects an entity by its primary key.
     *
     * @param providerContext The provider context, containing method and interface information.
     * @return The cache key.
     */
    public static String selectByPrimaryKey(ProviderContext providerContext) {
        return cacheSql(providerContext, BasicProvider::buildSelectByPrimaryKey);
    }

    /**
     * Selects all entities.
     *
     * @param providerContext The provider context, containing method and interface information.
     * @return The cache key.
     */
    public static String selectAll(ProviderContext providerContext) {
        return cacheSql(providerContext, BasicProvider::buildSelectAll);
    }

    /**
     * Selects a single entity or a batch of entities based on entity field conditions. The number of results is defined
     * by the method.
     *
     * @param providerContext The provider context, containing method and interface information.
     * @return The cache key.
     */
    public static String select(ProviderContext providerContext) {
        return cacheSql(
                providerContext,
                entity -> "SELECT " + entity.baseColumnAsPropertyList() + " FROM " + entity.tableName()
                        + buildWhereSelective(entity, null));
    }

    /**
     * Selects a single entity based on entity field conditions.
     *
     * @param providerContext The provider context, containing method and interface information.
     * @return The cache key.
     */
    public static String selectOne(ProviderContext providerContext) {
        return cacheSql(
                providerContext,
                entity -> "SELECT " + entity.baseColumnAsPropertyList() + " FROM " + entity.tableName()
                        + buildWhereSelective(entity, null) + " LIMIT 1");
    }

    /**
     * Deletes a record by its primary key.
     *
     * @param providerContext The provider context, containing method and interface information.
     * @return The cache key.
     */
    public static String deleteByPrimaryKey(ProviderContext providerContext) {
        return cacheSql(providerContext, BasicProvider::buildDeleteByPrimaryKey);
    }

    /**
     * Deletes records in batch based on entity field conditions.
     *
     * @param providerContext The provider context, containing method and interface information.
     * @return The cache key.
     */
    public static String delete(ProviderContext providerContext) {
        return cacheSql(
                providerContext,
                entity -> "DELETE FROM " + entity.tableName() + buildWhereSelective(entity, null));
    }

    /**
     * Counts the total number of records based on entity field conditions.
     *
     * @param providerContext The provider context, containing method and interface information.
     * @return The cache key.
     */
    public static String selectCount(ProviderContext providerContext) {
        return cacheSql(providerContext, BasicProvider::buildCount);
    }

    /**
     * Counts records by entity field conditions.
     *
     * @param providerContext The provider context, containing method and interface information.
     * @return The cache key.
     */
    public static String countByExample(ProviderContext providerContext) {
        return cacheSql(providerContext, entity -> buildCountSelective(entity, null));
    }

    /**
     * Checks if a record exists by its primary key.
     *
     * @param providerContext The provider context, containing method and interface information.
     * @return The cache key.
     */
    public static String existsWithPrimaryKey(ProviderContext providerContext) {
        return cacheSql(providerContext, BasicProvider::buildExistsByPrimaryKey);
    }

}
