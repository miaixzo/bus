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
package org.miaixz.bus.base.service;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.miaixz.bus.core.basic.entity.Result;
import org.miaixz.bus.core.basic.service.Service;
import org.miaixz.bus.mapper.binding.condition.Condition;
import org.miaixz.bus.mapper.binding.condition.ConditionWrapper;
import org.miaixz.bus.mapper.binding.function.Fn;

/**
 * Generic service interface defining standard CRUD (Create, Retrieve, Update, Delete) operations.
 * <p>
 * This interface serves as a base for service implementations, providing common methods for entity persistence, batch
 * operations, and conditional queries.
 *
 * @param <T> the entity type
 * @param <I> the type of the primary key, which must implement {@link Serializable}
 * @author Kimi Liu
 * @since Java 17+
 */
public interface SharedService<T, I extends Serializable> extends Service {

    /**
     * Persists a new entity, saving all fields (including nulls).
     *
     * @param entity the entity to insert
     * @return the persisted entity (typically with populated ID)
     */
    Object insert(T entity);

    /**
     * Persists a new entity, saving only non-null fields.
     * <p>
     * Fields with null values will rely on the database's default values.
     *
     * @param entity the entity to insert
     * @return the persisted entity
     */
    Object insertSelective(T entity);

    /**
     * Persists a batch of entities, saving all fields for each entity.
     *
     * @param list the list of entities to insert
     * @return the list of inserted entities
     */
    Object insertBatch(List<T> list);

    /**
     * Batch insert or update (Upsert) operation.
     * <p>
     * Typically attempts to insert the entities, and updates them if a primary key or unique constraint violation
     * occurs.
     *
     * @param list the list of entities to insert or update
     * @return the result of the batch operation
     */
    Object insertUpBatch(List<T> list);

    /**
     * Persists a batch of entities, saving only non-null fields for each entity.
     *
     * @param list the list of entities to insert
     * @return the list of inserted entities
     */
    Object insertSelectiveBatch(List<T> list);

    /**
     * Updates an existing entity, overwriting all fields (including setting fields to null).
     *
     * @param entity the entity to update
     * @return the updated entity or operation result
     */
    Object update(T entity);

    /**
     * Updates an existing entity, forcing updates on the specific fields provided.
     *
     * @param entity the entity to update
     * @param fields the specific fields to force update
     * @return the updated entity or operation result
     */
    Object update(T entity, Fn<T, Object>... fields);

    /**
     * Updates an existing entity, only modifying fields that are non-null.
     *
     * @param entity the entity to update
     * @return the updated entity or operation result
     */
    Object updateSelective(T entity);

    /**
     * Updates an existing entity, only modifying non-null fields, while forcing updates on the additional specified
     * fields.
     *
     * @param entity the entity to update
     * @param fields the additional fields to force update (even if they are null in the entity)
     * @return the updated entity or operation result
     */
    Object updateSelective(T entity, Fn<T, Object>... fields);

    /**
     * Saves the entity: inserts if it's new, or updates if it already exists (based on all fields).
     *
     * @param entity the entity to save
     * @return the saved entity
     */
    Object insertOrUpdate(T entity);

    /**
     * Saves the entity: inserts if it's new, or updates if it already exists (based on non-null fields).
     *
     * @param entity the entity to save
     * @return the saved entity
     */
    Object insertOrUpdateSelective(T entity);

    /**
     * Performs a logical deletion of the entity.
     * <p>
     * Instead of physically removing the row, this typically updates a "deleted" flag or status field.
     *
     * @param entity the entity to logically delete
     * @return the number of affected rows
     */
    long remove(T entity);

    /**
     * Performs a physical deletion of the entity from the database.
     *
     * @param entity the entity to delete
     * @return the number of deleted records (usually 1 if successful)
     */
    long delete(T entity);

    /**
     * Physically deletes an entity by its primary key.
     *
     * @param id the primary key of the entity to delete
     * @return the number of deleted records
     */
    long deleteById(I id);

    /**
     * Physically deletes multiple entities by a collection of primary keys.
     *
     * @param ids the collection of primary keys
     * @return the number of deleted records
     */
    long deleteByIds(Collection<I> ids);

    /**
     * Physically deletes entities that match a list of values for a specific field.
     *
     * @param field          the field to match against (e.g., User::getAge)
     * @param fieldValueList the collection of values to match
     * @param <F>            the type of the field value
     * @return the number of deleted records
     */
    <F> long deleteByFieldList(Fn<T, F> field, Collection<F> fieldValueList);

    /**
     * Retrieves a single entity by its primary key.
     *
     * @param id the primary key
     * @return the found entity, or null if not found
     */
    Object selectById(I id);

    /**
     * Retrieves a single entity matching the properties set in the provided entity object.
     *
     * @param entity the entity acting as a query prototype
     * @return the found entity, or null if not found
     */
    Object selectOne(T entity);

    /**
     * Retrieves a list of entities matching the properties set in the provided entity object.
     *
     * @param entity the entity acting as a query prototype
     * @return a list of matching entities
     */
    List<T> selectList(T entity);

    /**
     * Retrieves a list of entities where the specified field matches any value in the provided collection (IN query).
     *
     * @param field          the field to query (e.g., User::getId)
     * @param fieldValueList the collection of values to match
     * @param <F>            the type of the field value
     * @return a list of matching entities
     */
    <F> List<T> selectByFieldList(Fn<T, F> field, Collection<F> fieldValueList);

    /**
     * Retrieves all records from the table.
     *
     * @return a list containing all entities
     */
    List<T> selectAll();

    /**
     * Counts the number of records matching the properties set in the provided entity object.
     *
     * @param entity the entity acting as a query prototype
     * @return the count of matching records
     */
    long count(T entity);

    /**
     * Creates and returns a new {@link Condition} instance for building complex queries.
     *
     *
     * @return a new Condition object
     */
    default Condition<T> condition() {
        return new Condition<>();
    }

    /**
     * Deletes records matching the provided {@link Condition}.
     *
     * @param condition the complex query condition
     * @return the number of deleted records
     */
    long delete(Condition<T> condition);

    /**
     * Updates records matching the {@link Condition} with values from the provided entity (updates all fields).
     *
     * @param entity    the source of the new values
     * @param condition the condition defining which records to update
     * @return the number of updated records
     */
    long update(T entity, Condition<T> condition);

    /**
     * Updates records matching the {@link Condition} with values from the provided entity (updates only non-null
     * fields).
     *
     * @param entity    the source of the new values
     * @param condition the condition defining which records to update
     * @return the number of updated records
     */
    long updateSelective(T entity, Condition<T> condition);

    /**
     * Retrieves a single entity matching the provided {@link Condition}.
     * <p>
     * Note: Use this when you expect exactly one or zero results. If multiple results exist, implementation behavior
     * may vary (throw exception or return first).
     *
     * @param condition the query condition
     * @return the found entity, or null if not found
     */
    Object selectOne(Condition<T> condition);

    /**
     * Retrieves the first entity matching the provided {@link Condition}.
     * <p>
     * Typically limits the query result to 1 record.
     *
     * @param condition the query condition
     * @return the first matching entity, or null if none found
     */
    Object selectFirst(Condition<T> condition);

    /**
     * Retrieves a list of entities matching the provided {@link Condition}.
     *
     * @param condition the query condition
     * @return a list of matching entities
     */
    List<T> selectList(Condition<T> condition);

    /**
     * Counts the number of records matching the provided {@link Condition}.
     *
     * @param condition the query condition
     * @return the total count of matching records
     */
    long count(Condition<T> condition);

    /**
     * Checks if the primary key of the provided entity is set (not null/empty).
     *
     * @param entity the entity to check
     * @return true if the primary key has a value, false otherwise
     */
    boolean pkHasValue(T entity);

    /**
     * Performs a paginated query based on the parameters within the entity.
     * <p>
     * The entity is expected to contain pagination (page number, size) and sorting information.
     *
     * @param entity the entity containing query, pagination, and sorting params
     * @return a {@link Result} containing the list of records and total count
     */
    Result<T> page(T entity);

    /**
     * Creates and returns a new {@link ConditionWrapper} for fluent query construction.
     *
     * @return a new ConditionWrapper instance
     */
    ConditionWrapper<T, I> wrapper();

}
