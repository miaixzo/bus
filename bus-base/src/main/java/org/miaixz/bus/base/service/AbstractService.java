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

import org.miaixz.bus.base.entity.BaseEntity;
import org.miaixz.bus.base.mapper.SharedMapper;
import org.miaixz.bus.core.basic.entity.Result;
import org.miaixz.bus.core.basic.normal.Consts;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.mapper.binding.condition.Condition;
import org.miaixz.bus.mapper.binding.condition.ConditionWrapper;
import org.miaixz.bus.mapper.binding.function.Fn;
import org.miaixz.bus.mapper.parsing.ColumnMeta;
import org.miaixz.bus.mapper.parsing.TableMeta;
import org.miaixz.bus.mapper.support.paging.Page;
import org.miaixz.bus.mapper.support.paging.PageContext;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Abstract base implementation of {@link SharedService} providing standard CRUD functionality.
 * <p>
 * This class integrates with {@link BaseEntity} to automatically handle common lifecycle fields (e.g., status, creator,
 * creation time, modifier, update time) during insert and update operations.
 * </p>
 * <p>
 * If your business logic requires custom handling of these fields or does not use {@link BaseEntity}, consider
 * overriding the specific methods or extending a different base class.
 * </p>
 *
 * @param <T> the entity type, must extend {@link BaseEntity}
 * @param <I> the type of the primary key, must implement {@link Serializable}
 * @param <M> the mapper type, must extend {@link SharedMapper}
 * @author Kimi Liu
 * @since Java 17+
 */
public class AbstractService<T extends BaseEntity, I extends Serializable, M extends SharedMapper<T, I>>
        implements SharedService<T, I> {

    /**
     * The underlying mapper instance for database interactions. Automatically injected by Spring.
     */
    @Autowired
    protected M mapper;

    /**
     * Persists a new entity with all fields.
     * <p>
     * Automatically populates common metadata (status, creator, create time) before insertion.
     * </p>
     *
     * @param entity the entity to insert
     * @return the inserted entity
     */
    @Override
    public Object insert(T entity) {
        this.setValue(entity);
        return mapper.insert(entity);
    }

    /**
     * Persists a new entity, saving only non-null fields.
     * <p>
     * Automatically populates common metadata (status, creator, create time) before insertion.
     * </p>
     *
     * @param entity the entity to insert
     * @return the inserted entity
     */
    @Override
    public Object insertSelective(T entity) {
        this.setValue(entity);
        return mapper.insertSelective(entity);
    }

    /**
     * Persists a batch of entities, saving all fields for each entity.
     * <p>
     * Unlike single insert, this method typically delegates directly to the mapper without the individual `setValue`
     * hook loop for performance, unless handled within the mapper/interceptor.
     * </p>
     *
     * @param list the list of entities to insert
     * @return the list of inserted entities or operation result
     */
    @Override
    public Object insertBatch(List<T> list) {
        return mapper.insertBatch(list);
    }

    /**
     * Batch insert or update (Upsert) operation.
     *
     * @param list the list of entities to insert or update
     * @return the result of the batch operation
     */
    @Override
    public Object insertUpBatch(List<T> list) {
        return mapper.insertUpBatch(list);
    }

    /**
     * Persists a batch of entities, saving only non-null fields.
     *
     * @param list the list of entities to insert
     * @return the list of inserted entities or operation result
     */
    @Override
    public Object insertSelectiveBatch(List<T> list) {
        return mapper.insertSelectiveBatch(list);
    }

    /**
     * Updates an existing entity with all fields.
     * <p>
     * Automatically updates modification metadata (modifier, update time) before execution.
     * </p>
     *
     * @param entity the entity to update
     * @return the updated entity or operation result
     */
    @Override
    public Object update(T entity) {
        entity.setUpdate(entity);
        return mapper.updateByPrimaryKey(entity);
    }

    /**
     * Updates an existing entity, forcing updates on specified fields.
     * <p>
     * Automatically updates modification metadata (modifier, update time).
     * </p>
     *
     * @param entity the entity to update
     * @param fields the specific fields to force update
     * @return the updated entity or operation result
     */
    @Override
    public Object update(T entity, Fn<T, Object>... fields) {
        entity.setUpdate(entity);
        return mapper.updateForFieldListByPrimaryKey(entity, Fn.of(fields));
    }

    /**
     * Updates an existing entity, only modifying fields that are non-null.
     * <p>
     * Automatically updates modification metadata (modifier, update time).
     * </p>
     *
     * @param entity the entity to update
     * @return the updated entity or operation result
     */
    @Override
    public Object updateSelective(T entity) {
        entity.setUpdate(entity);
        return mapper.updateByPrimaryKeySelective(entity);
    }

    /**
     * Updates an existing entity, only modifying non-null fields, while forcing updates on additional specified fields.
     * <p>
     * Automatically updates modification metadata (modifier, update time).
     * </p>
     *
     * @param entity the entity to update
     * @param fields the additional fields to force update
     * @return the updated entity or operation result
     */
    @Override
    public Object updateSelective(T entity, Fn<T, Object>... fields) {
        entity.setUpdate(entity);
        return mapper.updateByPrimaryKeySelectiveWithForceFields(entity, Fn.of(fields));
    }

    /**
     * Saves the entity: inserts if primary key is missing, otherwise updates (all fields).
     *
     * @param entity the entity to insert or update
     * @return the saved entity
     */
    @Override
    public Object insertOrUpdate(T entity) {
        if (pkHasValue(entity)) {
            return update(entity);
        } else {
            return insert(entity);
        }
    }

    /**
     * Saves the entity: inserts if primary key is missing, otherwise updates (selective fields).
     *
     * @param entity the entity to insert or update
     * @return the saved entity
     */
    @Override
    public Object insertOrUpdateSelective(T entity) {
        if (pkHasValue(entity)) {
            return updateSelective(entity);
        } else {
            return insertSelective(entity);
        }
    }

    /**
     * Performs a logical deletion.
     * <p>
     * Sets the entity's status to {@link Consts#MINUS_ONE} (Deleted), updates the modification metadata, and persists
     * the change.
     * </p>
     *
     * @param entity the entity to logically delete
     * @return the number of affected rows
     */
    @Override
    public long remove(T entity) {
        entity.setStatus(Consts.MINUS_ONE);
        entity.setUpdate(entity);
        return mapper.updateByPrimaryKeySelective(entity);
    }

    /**
     * Performs a physical deletion of the entity from the database.
     *
     * @param entity the entity to delete
     * @return the number of deleted records
     */
    @Override
    public long delete(T entity) {
        return mapper.delete(entity);
    }

    /**
     * Physically deletes an entity by its primary key.
     *
     * @param id the primary key
     * @return the number of deleted records
     */
    @Override
    public long deleteById(I id) {
        return mapper.deleteByPrimaryKey(id);
    }

    /**
     * Physically deletes entities by a collection of primary keys.
     *
     * @param ids the collection of primary keys
     * @return the number of deleted records
     */
    @Override
    public long deleteByIds(Collection<I> ids) {
        return deleteByFieldList(entity -> (I) entity.getId(), ids);
    }

    /**
     * Physically deletes entities that match a list of values for a specific field.
     *
     * @param field          the field to match against
     * @param fieldValueList the collection of field values
     * @param <F>            the type of the field value
     * @return the number of deleted records
     */
    @Override
    public <F> long deleteByFieldList(Fn<T, F> field, Collection<F> fieldValueList) {
        return mapper.deleteByFieldList(field, fieldValueList);
    }

    /**
     * Retrieves a single entity by its primary key.
     *
     * @param id the primary key
     * @return the found entity, or null if not found
     */
    @Override
    public Object selectById(I id) {
        return mapper.selectByPrimaryKey(id);
    }

    /**
     * Retrieves a single entity matching the properties set in the provided entity object.
     *
     * @param entity the entity acting as a query prototype
     * @return the found entity, or null if not found
     */
    @Override
    public Object selectOne(T entity) {
        return mapper.selectOne(entity);
    }

    /**
     * Retrieves a list of entities matching the properties set in the provided entity object.
     *
     * @param entity the entity acting as a query prototype
     * @return a list of matching entities
     */
    @Override
    public List<T> selectList(T entity) {
        return mapper.selectList(entity);
    }

    /**
     * Retrieves a list of entities where the specified field matches any value in the provided collection.
     *
     * @param field          the field to query
     * @param fieldValueList the collection of values to match
     * @param <F>            the type of the field value
     * @return a list of matching entities
     */
    @Override
    public <F> List<T> selectByFieldList(Fn<T, F> field, Collection<F> fieldValueList) {
        return mapper.selectByFieldList(field, fieldValueList);
    }

    /**
     * Retrieves all records from the table.
     *
     * @return a list of all entities
     */
    @Override
    public List<T> selectAll() {
        return mapper.selectList(null);
    }

    /**
     * Counts the number of records matching the properties set in the provided entity object.
     *
     * @param entity the entity acting as a query prototype
     * @return the count of matching records
     */
    @Override
    public long count(T entity) {
        return mapper.selectCount(entity);
    }

    /**
     * Creates and returns a new {@link Condition} instance for building complex queries.
     *
     * @return a new Condition object
     */
    @Override
    public Condition<T> condition() {
        return SharedService.super.condition();
    }

    /**
     * Deletes records matching the provided {@link Condition}.
     *
     * @param condition the query condition
     * @return the number of deleted records
     */
    @Override
    public long delete(Condition<T> condition) {
        return mapper.deleteByCondition(condition);
    }

    /**
     * Updates records matching the {@link Condition} with values from the provided entity (updates all fields).
     *
     * @param entity    the source of the new values
     * @param condition the condition defining which records to update
     * @return the number of updated records
     */
    @Override
    public long update(T entity, Condition<T> condition) {
        return mapper.updateByCondition(entity, condition);
    }

    /**
     * Updates records matching the {@link Condition} with values from the provided entity (updates only non-null
     * fields).
     *
     * @param entity    the source of the new values
     * @param condition the condition defining which records to update
     * @return the number of updated records
     */
    @Override
    public long updateSelective(T entity, Condition<T> condition) {
        return mapper.updateByConditionSelective(entity, condition);
    }

    /**
     * Retrieves a single entity matching the provided {@link Condition}.
     *
     * @param condition the query condition
     * @return the found entity, or null if not found
     */
    @Override
    public Object selectOne(Condition<T> condition) {
        return mapper.selectOneByCondition(condition);
    }

    /**
     * Retrieves the first entity matching the provided {@link Condition}.
     *
     * @param condition the query condition
     * @return the first matching entity, or null if none found
     */
    @Override
    public Object selectFirst(Condition<T> condition) {
        List<T> list = mapper.selectByCondition(condition);
        return (list == null || list.isEmpty()) ? null : list.get(0);
    }

    /**
     * Retrieves a list of entities matching the provided {@link Condition}.
     *
     * @param condition the query condition
     * @return a list of matching entities
     */
    @Override
    public List<T> selectList(Condition<T> condition) {
        return mapper.selectByCondition(condition);
    }

    /**
     * Counts the number of records matching the provided {@link Condition}.
     *
     * @param condition the query condition
     * @return the total count of matching records
     */
    @Override
    public long count(Condition<T> condition) {
        return mapper.countByCondition(condition);
    }

    /**
     * Checks if the primary key of the provided entity is populated.
     * <p>
     * This uses the underlying {@link TableMeta} to identify the ID column and reflection to check its value.
     * </p>
     *
     * @param entity the entity to check
     * @return true if the primary key is not null, false otherwise
     */
    @Override
    public boolean pkHasValue(T entity) {
        TableMeta entityTable = mapper.entityTable();
        List<ColumnMeta> idColumns = entityTable.idColumns();
        return idColumns.get(0).fieldMeta().get(entity) != null;
    }

    /**
     * Performs a paginated query based on the parameters within the entity.
     * <p>
     * This method initializes the {@link PageContext} using the entity's page number, size, and sort order, then
     * delegates to the mapper.
     * </p>
     *
     * @param entity the entity containing pagination and sorting parameters
     * @return a {@link Result} containing the list of records and total count
     */
    @Override
    public Result<T> page(T entity) {
        PageContext.of(entity.getPageNo(), entity.getPageSize());
        if (StringKit.isNotEmpty(entity.getOrderBy())) {
            PageContext.orderBy(entity.getOrderBy());
        }
        Page<T> list = (Page<T>) mapper.selectList(entity);
        return Result.<T>builder().rows(list.getResult()).total(list.getTotal()).build();
    }

    /**
     * Returns a new {@link ConditionWrapper} for fluent query construction.
     *
     * @return a new ConditionWrapper instance
     */
    @Override
    public ConditionWrapper<T, I> wrapper() {
        return mapper.wrapper();
    }

    /**
     * Helper method to set common entity lifecycle properties before persistence.
     * <p>
     * Sets default Status (1) if empty, and triggers {@link BaseEntity#setValue(Object)} for creator/modifier/timestamp
     * handling.
     * </p>
     *
     * @param entity the entity to process
     * @return the ID of the entity
     */
    protected String setValue(T entity) {
        if (ObjectKit.isEmpty(entity)) {
            return null;
        }
        if (ObjectKit.isEmpty(entity.getStatus())) {
            entity.setStatus(Consts.ONE);
        }
        entity.setValue(entity);
        return entity.getId();
    }

}
