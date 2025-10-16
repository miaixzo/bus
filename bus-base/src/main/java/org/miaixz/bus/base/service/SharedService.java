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
 * Generic service interface for basic CRUD (Create, Retrieve, Update, Delete) operations on entities.
 *
 * @param <T> the entity type
 * @param <I> the type of the primary key, which must implement {@link Serializable}
 * @author Kimi Liu
 * @since Java 17+
 */
interface SharedService<T, I extends Serializable> extends Service {

    /**
     * Inserts a new entity with all fields.
     *
     * @param entity the entity to insert
     * @return the inserted entity
     */
    Object insert(T entity);

    /**
     * Inserts a new entity, only including non-null fields.
     *
     * @param entity the entity to insert
     * @return the inserted entity
     */
    Object insertSelective(T entity);

    /**
     * Inserts a batch of entities with all fields.
     *
     * @param list the list of entities to insert
     * @return the list of inserted entities
     */
    List<T> insertBatch(List<T> list);

    /**
     * Inserts a batch of entities, only including non-null fields.
     *
     * @param list the list of entities to insert
     * @return the list of inserted entities
     */
    List<T> insertBatchSelective(List<T> list);

    /**
     * Updates an existing entity with all fields.
     *
     * @param entity the entity to update
     * @return the updated entity
     */
    Object update(T entity);

    /**
     * Updates an existing entity, specifying which fields to update.
     *
     * @param entity the entity to update
     * @param fields the fields to be updated
     * @return the updated entity
     */
    Object update(T entity, Fn<T, Object>... fields);

    /**
     * Updates an existing entity, only including non-null fields.
     *
     * @param entity the entity to update
     * @return the updated entity
     */
    Object updateSelective(T entity);

    /**
     * Updates an existing entity, only including non-null fields, and forces update for specified fields.
     *
     * @param entity the entity to update
     * @param fields the fields to force update
     * @return the updated entity
     */
    Object updateSelective(T entity, Fn<T, Object>... fields);

    /**
     * Inserts a new entity or updates an existing one with all fields.
     *
     * @param entity the entity to insert or update
     * @return the inserted or updated entity
     */
    Object insertOrUpdate(T entity);

    /**
     * Inserts a new entity or updates an existing one, only including non-null fields.
     *
     * @param entity the entity to insert or update
     * @return the inserted or updated entity
     */
    Object insertOrUpdateSelective(T entity);

    /**
     * Performs a logical deletion of an entity. This typically involves updating a status field rather than physically
     * deleting the record.
     *
     * @param entity the entity to be logically removed
     * @return the number of affected rows
     */
    long remove(T entity);

    /**
     * Performs a physical deletion of an entity from the database.
     *
     * @param entity the entity to delete
     * @return the number of records deleted, greater than 0 indicates success
     */
    long delete(T entity);

    /**
     * Deletes an entity by its primary key.
     *
     * @param id the primary key
     * @return the number of records deleted, 1 indicates success
     */
    long deleteById(I id);

    /**
     * Deletes entities by a collection of primary keys.
     *
     * @param ids the collection of primary keys
     * @return the number of records deleted
     */
    long deleteByIds(Collection<I> ids);

    /**
     * Deletes entities by a collection of field values.
     *
     * @param field          the field to match against
     * @param fieldValueList the collection of field values
     * @param <F>            the type of the field value
     * @return the number of records deleted
     */
    <F> long deleteByFieldList(Fn<T, F> field, Collection<F> fieldValueList);

    /**
     * Retrieves an entity by its primary key.
     *
     * @param id the primary key
     * @return the entity, or null if not found
     */
    Object selectById(I id);

    /**
     * Retrieves a single entity based on the provided entity conditions.
     *
     * @param entity the entity containing query conditions
     * @return the entity, or null if not found
     */
    Object selectOne(T entity);

    /**
     * Retrieves a list of entities based on the provided entity conditions.
     *
     * @param entity the entity containing query conditions
     * @return a list of entities
     */
    List<T> selectList(T entity);

    /**
     * Retrieves a list of entities based on a collection of field values.
     *
     * @param field          the field to match against
     * @param fieldValueList the collection of field values
     * @param <F>            the type of the field value
     * @return a list of entities
     */
    <F> List<T> selectByFieldList(Fn<T, F> field, Collection<F> fieldValueList);

    /**
     * Retrieves all records.
     *
     * @return a list of all entities
     */
    List<T> selectAll();

    /**
     * Counts the number of records matching the provided entity conditions.
     *
     * @param entity the entity containing query conditions
     * @return the total number of records
     */
    long count(T entity);

    /**
     * Returns a new {@link Condition} object for building complex queries.
     *
     * @return a new Condition object
     */
    default Condition<T> condition() {
        return new Condition<>();
    }

    /**
     * Deletes records based on the provided {@link Condition}.
     *
     * @param condition the query condition
     * @return the number of records deleted, greater than 0 indicates success
     */
    long delete(Condition<T> condition);

    /**
     * Updates records based on the provided entity and {@link Condition}, updating all fields.
     *
     * @param entity    the entity with updated information
     * @param condition the query condition
     * @return the number of records updated, greater than 0 indicates success
     */
    long update(T entity, Condition<T> condition);

    /**
     * Updates records based on the provided entity and {@link Condition}, only updating non-null fields.
     *
     * @param entity    the entity with updated information
     * @param condition the query condition
     * @return the number of records updated, greater than 0 indicates success
     */
    long updateSelective(T entity, Condition<T> condition);

    /**
     * Retrieves a single entity based on the provided {@link Condition}.
     *
     * @param condition the query condition
     * @return the entity, or null if not found
     */
    Object selectOne(Condition<T> condition);

    /**
     * Retrieves the first entity found based on the provided {@link Condition}.
     *
     * @param condition the query condition
     * @return the entity
     */
    Object selectFirst(Condition<T> condition);

    /**
     * Retrieves a list of entities based on the provided {@link Condition}.
     *
     * @param condition the query condition
     * @return a list of entities
     */
    List<T> selectList(Condition<T> condition);

    /**
     * Counts the number of records matching the provided {@link Condition}.
     *
     * @param condition the query condition
     * @return the total number of records
     */
    long count(Condition<T> condition);

    /**
     * Checks if the primary key of the given entity has a value.
     *
     * @param entity the entity to check
     * @return true if the primary key has a value, false otherwise
     */
    boolean pkHasValue(T entity);

    /**
     * Performs a paginated query based on the provided entity, which should contain pagination and sorting parameters.
     *
     * @param entity the entity containing pagination and sorting parameters
     * @return a {@link Result} object containing the paginated list of records and the total count
     */
    Result<T> page(T entity);

    /**
     * Returns a new {@link ConditionWrapper} object for building complex query conditions.
     *
     * @return a new ConditionWrapper object
     */
    ConditionWrapper<T, I> wrapper();

}
