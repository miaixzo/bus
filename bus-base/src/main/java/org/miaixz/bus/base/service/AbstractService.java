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
package org.miaixz.bus.base.service;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.miaixz.bus.base.mapper.SharedMapper;
import org.miaixz.bus.core.basic.entity.Entity;
import org.miaixz.bus.core.basic.entity.Result;
import org.miaixz.bus.core.basic.normal.Consts;
import org.miaixz.bus.core.data.id.ID;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.CastKit;
import org.miaixz.bus.core.xyz.DateKit;
import org.miaixz.bus.core.xyz.FieldKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.mapper.binding.condition.Condition;
import org.miaixz.bus.mapper.binding.condition.ConditionWrapper;
import org.miaixz.bus.mapper.binding.function.Fn;
import org.miaixz.bus.mapper.support.paging.Page;
import org.miaixz.bus.mapper.support.paging.PageContext;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Abstract base implementation of {@link SharedService} providing standard CRUD functionality.
 * <p>
 * This class applies lifecycle defaults and reads shared fields by convention so the same CRUD logic can work with
 * different entity models. Conventional shared fields are {@code id}, {@code status}, {@code creator}, {@code created},
 * {@code modifier}, {@code modified}, {@code pageNo}, {@code pageSize}, and {@code orderBy}. The current user is read
 * from {@code x_user_id} or {@code X_USER_ID} when present.
 * </p>
 * <p>
 * It also keeps the service-layer distinction between two batch models:
 * </p>
 * <ul>
 * <li>Native batch SQL methods such as {@code insertBatch} and {@code insertUpBatch}, delegated to
 * {@code BatchMapper}.</li>
 * <li>List-oriented batch methods such as {@code insertList}, {@code update(List)} and {@code updateListSelective},
 * delegated to {@code ListMapper}.</li>
 * </ul>
 * <p>
 * If your business logic requires custom handling of lifecycle fields or batch strategy selection, override the
 * relevant methods in a concrete service.
 * </p>
 *
 * @param <T> the entity type
 * @param <I> the type of the primary key, must implement {@link Serializable}
 * @param <M> the mapper type, must extend {@link SharedMapper}
 * @author Kimi Liu
 * @since Java 21+
 */
public abstract class AbstractService<T, I extends Serializable, M extends SharedMapper<T, I>>
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
        this.setInsert(entity);
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
        this.setInsert(entity);
        return mapper.insertSelective(entity);
    }

    /**
     * Persists a list of entities using list-oriented batch insertion semantics.
     * <p>
     * Automatically populates common metadata (status, creator, create time) before insertion.
     * </p>
     *
     * @param list the list of entities to insert
     * @return the list-oriented batch insert result
     */
    @Override
    public Object insertList(List<T> list) {
        if (list != null && !list.isEmpty()) {
            list.forEach(this::setInsert);
        }
        return mapper.insertList(list);
    }

    /**
     * Persists a batch of entities, saving all fields for each entity.
     * <p>
     * Automatically populates common metadata (status, creator, create time) before insertion.
     * </p>
     *
     * @param list the list of entities to insert
     * @return the list of inserted entities or operation result
     */
    @Override
    public Object insertBatch(List<T> list) {
        if (list != null && !list.isEmpty()) {
            list.forEach(this::setInsert);
        }
        return mapper.insertBatch(list);
    }

    /**
     * Batch insert or update (Upsert) operation.
     * <p>
     * Automatically populates common metadata (status, creator, create time, modifier, update time) before operation.
     * </p>
     *
     * @param list the list of entities to insert or update
     * @return the result of the batch operation
     */
    @Override
    public Object insertUpBatch(List<T> list) {
        if (list != null && !list.isEmpty()) {
            list.forEach(this::setInsert);
        }
        return mapper.insertUpBatch(list);
    }

    /**
     * Persists a batch of entities, saving only non-null fields.
     * <p>
     * Automatically populates common metadata (status, creator, create time) before insertion.
     * </p>
     *
     * @param list the list of entities to insert
     * @return the list of inserted entities or operation result
     */
    @Override
    public Object insertSelectiveBatch(List<T> list) {
        if (list != null && !list.isEmpty()) {
            list.forEach(this::setInsert);
        }
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
        this.setUpdate(entity);
        return mapper.updateByPrimaryKey(entity);
    }

    /**
     * Updates a list of entities using list-oriented batch update semantics.
     * <p>
     * Automatically updates modification metadata (modifier, update time) before execution.
     * </p>
     *
     * @param list the list of entities to update
     * @return the list-oriented batch update result
     */
    @Override
    public Object update(List<T> list) {
        if (list != null && !list.isEmpty()) {
            list.forEach(this::setUpdate);
        }
        return mapper.updateList(list);
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
        this.setUpdate(entity);
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
        this.setUpdate(entity);
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
        this.setUpdate(entity);
        return mapper.updateByPrimaryKeySelectiveWithForceFields(entity, Fn.of(fields));
    }

    /**
     * Updates a list of entities using list-oriented selective batch update semantics.
     * <p>
     * Automatically updates modification metadata (modifier, update time) before execution.
     * </p>
     *
     * @param list the list of entities to update
     * @return the list-oriented selective batch update result
     */
    @Override
    public Object updateListSelective(List<T> list) {
        if (list != null && !list.isEmpty()) {
            list.forEach(this::setUpdate);
        }
        return mapper.updateListSelective(list);
    }

    /**
     * Saves the entity: inserts if primary key is missing, otherwise updates (all fields).
     *
     * @param entity the entity to insert or update
     * @return the saved entity
     */
    @Override
    public Object insertOrUpdate(T entity) {
        if (this.pkHasValue(entity)) {
            return this.update(entity);
        } else {
            return this.insert(entity);
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
        if (this.pkHasValue(entity)) {
            return this.updateSelective(entity);
        } else {
            return this.insertSelective(entity);
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
        this.setRemove(entity);
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
     * <p>
     * The primary key is read from the entity's conventional {@code id} field.
     * </p>
     *
     * @param ids the collection of primary keys
     * @return the number of deleted records
     */
    @Override
    public long deleteByIds(Collection<I> ids) {
        return this.deleteByFieldList(entity -> CastKit.cast(getFieldValue(entity, "id")), ids);
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
     * This uses the framework's conventional {@code id} field to read the identifier value.
     * </p>
     *
     * @param entity the entity to check
     * @return true if the primary key is not null, false otherwise
     */
    @Override
    public boolean pkHasValue(T entity) {
        return hasFieldValue(entity, "id");
    }

    /**
     * Performs a paginated query based on the parameters within the entity.
     * <p>
     * This method initializes the {@link PageContext} using the entity's conventional {@code pageNo}, {@code pageSize}
     * and {@code orderBy} fields, then delegates to the mapper.
     * </p>
     *
     * @param entity the entity containing pagination and sorting parameters
     * @return a {@link Result} containing the list of records and total count
     */
    @Override
    public Result<T> page(T entity) {
        Integer pageNo = CastKit.cast(getFieldValue(entity, "pageNo"));
        Integer pageSize = CastKit.cast(getFieldValue(entity, "pageSize"));
        PageContext.of(pageNo, pageSize);
        String orderBy = CastKit.cast(getFieldValue(entity, "orderBy"));
        if (StringKit.isNotEmpty(orderBy)) {
            PageContext.orderBy(orderBy);
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
     * Applies insert-time defaults and returns the conventional {@code id} field.
     *
     * @param entity the entity to process
     * @return the ID of the entity
     */
    protected I setInsert(T entity) {
        if (entity == null) {
            return null;
        }
        long now = DateKit.current();
        String userId = null;
        if (!hasFieldValue(entity, "id")) {
            setFieldValue(entity, "id", ID.objectId());
        }
        if (!hasFieldValue(entity, "status")) {
            setFieldValue(entity, "status", Consts.ONE);
        }
        if (!hasFieldValue(entity, "creator")) {
            userId = operatorId(entity);
            setFieldValue(entity, "creator", userId);
        }
        setFieldValue(entity, "created", now);
        if (!hasFieldValue(entity, "modifier")) {
            if (userId == null) {
                userId = operatorId(entity);
            }
            setFieldValue(entity, "modifier", userId);
        }
        setFieldValue(entity, "modified", now);
        return CastKit.cast(getFieldValue(entity, "id"));
    }

    /**
     * Applies update-time defaults.
     *
     * @param entity the entity to process
     */
    protected void setUpdate(T entity) {
        if (entity == null) {
            return;
        }
        setFieldValue(entity, "modified", DateKit.current());
        if (!hasFieldValue(entity, "modifier")) {
            setFieldValue(entity, "modifier", operatorId(entity));
        }
    }

    /**
     * Applies logical-delete defaults.
     *
     * @param entity the entity to process
     */
    protected void setRemove(T entity) {
        if (entity == null) {
            return;
        }
        setFieldValue(entity, "status", Consts.MINUS_ONE);
        setFieldValue(entity, "modified", DateKit.current());
        if (!hasFieldValue(entity, "modifier")) {
            setFieldValue(entity, "modifier", operatorId(entity));
        }
    }

    /**
     * Resolves the current operator identifier from the entity.
     *
     * @param entity the entity to inspect
     * @return the current operator identifier
     */
    protected String operatorId(T entity) {
        Object userId = firstFieldValue(entity, "x_user_id", "X_User_Id", "X_USER_ID");
        return ObjectKit.isEmpty(userId) ? String.valueOf(Normal.__1) : String.valueOf(userId);
    }

    /**
     * Returns whether a conventional field exists and has a non-empty value.
     *
     * @param entity the entity to inspect
     * @param field  the conventional field name
     * @return {@code true} when the field has a value
     */
    protected boolean hasFieldValue(T entity, String field) {
        return !ObjectKit.isEmpty(getFieldValue(entity, field));
    }

    /**
     * Reads a field value when the field exists.
     *
     * @param entity the entity to inspect
     * @param field  the field name
     * @return the field value, or {@code null} when absent
     */
    protected Object getFieldValue(T entity, String field) {
        if (entity == null || StringKit.isEmpty(field)) {
            return null;
        }
        if (entity instanceof Entity baseEntity) {
            return baseEntity.getValue(entity, field);
        }
        if (!FieldKit.hasField(entity.getClass(), field)) {
            return null;
        }
        return FieldKit.getFieldValue(entity, field);
    }

    /**
     * Writes a required conventional field value.
     *
     * @param entity the entity to update
     * @param field  the field name
     * @param value  the field value
     */
    protected void setFieldValue(T entity, String field, Object value) {
        if (entity == null || StringKit.isEmpty(field)) {
            return;
        }
        if (entity instanceof Entity baseEntity) {
            baseEntity.setValue(entity, new String[] { field }, new Object[] { value });
            return;
        }
        if (!FieldKit.hasField(entity.getClass(), field)) {
            throw new IllegalStateException(
                    StringKit.format("Missing field {} on {}", field, entity.getClass().getName()));
        }
        FieldKit.setFieldValue(entity, field, value);
    }

    /**
     * Reads the first available conventional field value.
     *
     * @param entity the entity to inspect
     * @param fields candidate field names
     * @return the first matching field value
     */
    private Object firstFieldValue(T entity, String... fields) {
        if (fields == null || fields.length == 0) {
            return null;
        }
        for (String field : fields) {
            Object value = getFieldValue(entity, field);
            if (!ObjectKit.isEmpty(value)) {
                return value;
            }
        }
        return null;
    }

}
