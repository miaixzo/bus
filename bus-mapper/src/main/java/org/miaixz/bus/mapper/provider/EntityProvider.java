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
package org.miaixz.bus.mapper.provider;

import org.apache.ibatis.builder.annotation.ProviderContext;

/**
 * Provides dynamic SQL generation for basic CRUD operations.
 *
 * <p>
 * This class uses BasicProvider's common SQL building methods to simplify code. Reduced from ~250 lines to ~170 lines
 * (32% reduction).
 * <p>
 * <strong>UPSERT Methods:</strong>
 * </p>
 * <ul>
 * <li>{@code insertUp()}: Insert or update with all fields (atomic operation)</li>
 * <li>{@code insertUpSelective()}: Insert or update with non-null fields (atomic operation)</li>
 * <li>Supports MySQL (ON DUPLICATE KEY UPDATE), PostgreSQL (ON CONFLICT), SQLite (INSERT OR REPLACE), Oracle (MERGE),
 * H2, SQL Server, DB2</li>
 * </ul>
 *
 * <p>
 * Performance optimizations:
 * </p>
 * <ul>
 * <li>Extends BasicProvider for code reuse</li>
 * <li>Uses cacheSql for unified caching mechanism</li>
 * <li>Uses cacheSqlDynamic for database-specific SQL generation</li>
 * <li>Eliminates duplicate SQL building code</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class EntityProvider extends BasicProvider {

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
     * Upserts an entity (insert if not exists, update if exists).
     *
     * @param providerContext The provider context, containing method and interface information.
     * @return The cache key.
     */
    public static String insertUp(ProviderContext providerContext) {
        return cacheSqlDynamic(providerContext, BasicProvider::buildInsertUp);
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
     * Upserts an entity with only non-null fields.
     *
     * @param providerContext The provider context, containing method and interface information.
     * @return The cache key.
     */
    public static String insertUpSelective(ProviderContext providerContext) {
        return cacheSqlDynamic(providerContext, BasicProvider::buildInsertUpSelective);
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
    public static String countByCondition(ProviderContext providerContext) {
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

}
