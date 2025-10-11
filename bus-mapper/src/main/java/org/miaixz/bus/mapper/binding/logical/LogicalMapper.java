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
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.mapper.binding.logical;

import java.io.Serializable;
import java.util.List;

import org.apache.ibatis.annotations.*;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.session.RowBounds;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.mapper.Caching;
import org.miaixz.bus.mapper.binding.BasicMapper;
import org.miaixz.bus.mapper.binding.condition.Condition;
import org.miaixz.bus.mapper.binding.function.Fn;
import org.miaixz.bus.mapper.binding.function.FunctionMapper;
import org.miaixz.bus.mapper.provider.LogicalProvider;

/**
 * An interface for logical delete operations, overriding base query, delete, and update methods to support
 * soft-deleting records instead of physically removing them.
 *
 * @param <T> The type of the entity class.
 * @param <I> The type of the primary key.
 * @author Kimi Liu
 * @since Java 17+
 */
public interface LogicalMapper<T, I extends Serializable> extends BasicMapper<T, I>, FunctionMapper<T> {

    /**
     * Updates non-null fields of an entity by its primary key, while also forcing an update on specified fields. This
     * operation respects logical deletion rules.
     *
     * @param entity The entity object containing the updated values.
     * @param fields A collection of fields to be forcibly updated, created via {@link Fn#of(Fn...)}.
     * @param <S>    The type of the entity.
     * @return The number of affected rows (1 for success, 0 for failure).
     */
    @Override
    @Lang(Caching.class)
    @UpdateProvider(type = LogicalProvider.class, method = "updateByPrimaryKeySelectiveWithForceFields")
    <S extends T> int updateByPrimaryKeySelectiveWithForceFields(
            @Param("entity") S entity,
            @Param("fns") Fn.FnArray<T> fields);

    /**
     * Selects a single, unique entity based on its fields, returning only the specified columns. This query will
     * exclude logically deleted records.
     *
     * @param entity       The entity object containing the query criteria.
     * @param selectFields A collection of fields to be selected, created via {@link Fn#of(Fn...)}.
     * @return An {@link Optional} containing the unique entity object, or an empty Optional if not found.
     */
    @Override
    @Lang(Caching.class)
    @SelectProvider(type = LogicalProvider.class, method = "selectColumns")
    Optional<T> selectColumnsOne(@Param("entity") T entity, @Param("fns") Fn.FnArray<T> selectFields);

    /**
     * Selects a list of entities based on their fields, returning only the specified columns. This query will exclude
     * logically deleted records.
     *
     * @param entity       The entity object containing the query criteria.
     * @param selectFields A collection of fields to be selected, created via {@link Fn#of(Fn...)}.
     * @return A list of entity objects.
     */
    @Override
    @Lang(Caching.class)
    @SelectProvider(type = LogicalProvider.class, method = "selectColumns")
    List<T> selectColumns(@Param("entity") T entity, @Param("fns") Fn.FnArray<T> selectFields);

    /**
     * Logically deletes a record by its primary key.
     *
     * @param id The primary key.
     * @return The number of affected rows (1 for success, 0 for failure).
     */
    @Override
    @Lang(Caching.class)
    @DeleteProvider(type = LogicalProvider.class, method = "deleteByPrimaryKey")
    int deleteByPrimaryKey(I id);

    /**
     * Logically deletes records based on the conditions in the entity.
     *
     * @param entity The entity object containing the deletion criteria.
     * @return The number of affected rows (>=1 for success, 0 for failure).
     */
    @Override
    @Lang(Caching.class)
    @DeleteProvider(type = LogicalProvider.class, method = "delete")
    int delete(T entity);

    /**
     * Updates an entity by its primary key. This operation respects logical deletion rules.
     *
     * @param entity The entity object with updated values.
     * @param <S>    A subtype of the entity class.
     * @return The number of affected rows (1 for success, 0 for failure).
     */
    @Override
    @Lang(Caching.class)
    @UpdateProvider(type = LogicalProvider.class, method = "updateByPrimaryKey")
    <S extends T> int updateByPrimaryKey(S entity);

    /**
     * Updates non-null fields of an entity by its primary key. This operation respects logical deletion rules.
     *
     * @param entity The entity object with updated values.
     * @param <S>    A subtype of the entity class.
     * @return The number of affected rows (1 for success, 0 for failure).
     */
    @Override
    @Lang(Caching.class)
    @UpdateProvider(type = LogicalProvider.class, method = "updateByPrimaryKeySelective")
    <S extends T> int updateByPrimaryKeySelective(S entity);

    /**
     * Selects an entity by its primary key, excluding logically deleted records.
     *
     * @param id The primary key.
     * @return The entity object, or null if not found or logically deleted.
     */
    @Override
    @Lang(Caching.class)
    @SelectProvider(type = LogicalProvider.class, method = "selectByPrimaryKey")
    T selectByPrimaryKey(I id);

    /**
     * Selects a single entity based on its fields, excluding logically deleted records.
     *
     * @param entity The entity object containing the query criteria.
     * @return The unique entity object, or null if not found.
     */
    @Override
    @Lang(Caching.class)
    @SelectProvider(type = LogicalProvider.class, method = "select")
    T selectOne(T entity);

    /**
     * Selects a list of entities based on their fields, excluding logically deleted records.
     *
     * @param entity The entity object containing the query criteria.
     * @return A list of entity objects.
     */
    @Override
    @Lang(Caching.class)
    @SelectProvider(type = LogicalProvider.class, method = "select")
    List<T> selectList(T entity);

    /**
     * Counts the number of records matching the entity's fields, excluding logically deleted records.
     *
     * @param entity The entity object containing the query criteria.
     * @return The total number of matching records.
     */
    @Override
    @Lang(Caching.class)
    @SelectProvider(type = LogicalProvider.class, method = "selectCount")
    long selectCount(T entity);

    /**
     * Performs a cursor-based query based on the entity's fields, excluding logically deleted records.
     *
     * @param entity The entity object containing the query criteria.
     * @return A {@link Cursor} for the entity objects.
     */
    @Override
    @Lang(Caching.class)
    @SelectProvider(type = LogicalProvider.class, method = "select")
    Cursor<T> selectCursor(T entity);

    /**
     * Performs a cursor-based query based on a {@link Condition}, excluding logically deleted records.
     *
     * @param condition The condition object.
     * @return A {@link Cursor} for the entity objects.
     */
    @Override
    @Lang(Caching.class)
    @SelectProvider(type = LogicalProvider.class, method = "selectByCondition")
    Cursor<T> selectCursorByCondition(Condition<T> condition);

    /**
     * Creates and returns a new {@link Condition} object.
     *
     * @return A new {@link Condition} object.
     */
    @Override
    default Condition<T> condition() {
        return BasicMapper.super.condition();
    }

    /**
     * Logically deletes records based on the given {@link Condition}.
     *
     * @param condition The condition object.
     * @return The number of affected rows (>=1 for success, 0 for failure).
     */
    @Override
    @Lang(Caching.class)
    @DeleteProvider(type = LogicalProvider.class, method = "deleteByCondition")
    int deleteByCondition(Condition<T> condition);

    /**
     * Updates entity records based on a {@link Condition}. This operation respects logical deletion rules.
     *
     * @param entity    The entity object with updated values.
     * @param condition The condition object.
     * @param <S>       A subtype of the entity class.
     * @return The number of affected rows (>=1 for success, 0 for failure).
     */
    @Override
    @Lang(Caching.class)
    @UpdateProvider(type = LogicalProvider.class, method = "updateByCondition")
    <S extends T> int updateByCondition(@Param("entity") S entity, @Param("condition") Condition<T> condition);

    /**
     * Updates fields based on a {@link Condition} and the values set in it. This operation respects logical deletion
     * rules.
     *
     * @param condition The condition object containing the values to be set.
     * @return The number of affected rows (>=1 for success, 0 for failure).
     */
    @Override
    @Lang(Caching.class)
    @UpdateProvider(type = LogicalProvider.class, method = "updateByConditionSetValues")
    int updateByConditionSetValues(@Param("condition") Condition<T> condition);

    /**
     * Updates non-null fields of an entity based on a {@link Condition}. This operation respects logical deletion
     * rules.
     *
     * @param entity    The entity object with updated values.
     * @param condition The condition object.
     * @param <S>       A subtype of the entity class.
     * @return The number of affected rows (>=1 for success, 0 for failure).
     */
    @Override
    @Lang(Caching.class)
    @UpdateProvider(type = LogicalProvider.class, method = "updateByConditionSelective")
    <S extends T> int updateByConditionSelective(@Param("entity") S entity, @Param("condition") Condition<T> condition);

    /**
     * Selects a list of entities based on a {@link Condition}, excluding logically deleted records.
     *
     * @param condition The condition object.
     * @return A list of entity objects.
     */
    @Override
    @Lang(Caching.class)
    @SelectProvider(type = LogicalProvider.class, method = "selectByCondition")
    List<T> selectByCondition(Condition<T> condition);

    /**
     * Selects a single entity based on a {@link Condition}, excluding logically deleted records.
     *
     * @param condition The condition object.
     * @return An {@link Optional} containing the unique entity object, or an empty Optional if not found.
     */
    @Override
    @Lang(Caching.class)
    @SelectProvider(type = LogicalProvider.class, method = "selectByCondition")
    Optional<T> selectOneByCondition(Condition<T> condition);

    /**
     * Counts the number of records matching a {@link Condition}, excluding logically deleted records.
     *
     * @param condition The condition object.
     * @return The total number of matching records.
     */
    @Override
    @Lang(Caching.class)
    @SelectProvider(type = LogicalProvider.class, method = "countByCondition")
    long countByCondition(Condition<T> condition);

    /**
     * Selects a list of entities with pagination based on a {@link Condition}, excluding logically deleted records.
     *
     * @param condition The condition object.
     * @param rowBounds The pagination information.
     * @return A list of entity objects for the current page.
     */
    @Override
    List<T> selectByCondition(Condition<T> condition, RowBounds rowBounds);

}
