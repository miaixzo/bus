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
import org.miaixz.bus.core.xyz.ListKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.mapper.binding.condition.Condition;
import org.miaixz.bus.mapper.binding.condition.ConditionWrapper;
import org.miaixz.bus.mapper.binding.function.Fn;
import org.miaixz.bus.mapper.parsing.ColumnMeta;
import org.miaixz.bus.mapper.parsing.TableMeta;
import org.miaixz.bus.pager.Page;
import org.miaixz.bus.pager.PageContext;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Abstract service implementation based on Spring, implementing the {@link SharedService} interface. This class
 * provides common CRUD operations and integrates with {@link BaseEntity} for common fields like status, creator, etc.
 * If specific business requirements do not include fields like status, creator, etc., this class and {@link BaseEntity}
 * should be overridden, and business classes should inherit from the new class.
 *
 * @param <T> the entity type, which must extend {@link BaseEntity}
 * @param <I> the type of the primary key, which must implement {@link Serializable}
 * @param <M> the mapper type, which must extend {@link SharedMapper}
 * @author Kimi Liu
 * @since Java 17+
 */
public class AbstractService<T extends BaseEntity, I extends Serializable, M extends SharedMapper<T, I>>
        implements SharedService<T, I> {

    /**
     * The mapper instance for database operations.
     */
    @Autowired
    protected M mapper;

    /**
     * Inserts a new entity with all fields. Sets common entity properties like status, creator, and creation time
     * before insertion.
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
     * Inserts a new entity, only including non-null fields. Sets common entity properties like status, creator, and
     * creation time before insertion.
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
     * Inserts a batch of entities with all fields. Each entity in the list will have its common properties set before
     * insertion.
     *
     * @param list the list of entities to insert
     * @return the list of inserted entities
     */
    @Override
    public List<T> insertBatch(List<T> list) {
        List<T> data = ListKit.of();
        list.forEach(item -> {
            T t = (T) this.insertSelective(item);
            data.add(t);
        });
        return data;
    }

    /**
     * Inserts a batch of entities, only including non-null fields. Each entity in the list will have its common
     * properties set before insertion.
     *
     * @param list the list of entities to insert
     * @return the list of inserted entities
     */
    @Override
    public List<T> insertBatchSelective(List<T> list) {
        List<T> data = ListKit.of();
        list.forEach(item -> {
            T t = (T) this.insertSelective(item);
            data.add(t);
        });
        return data;
    }

    /**
     * Updates an existing entity with all fields. Sets common entity properties like modifier and modification time
     * before updating.
     *
     * @param entity the entity to update
     * @return the updated entity
     */
    @Override
    public Object update(T entity) {
        entity.setUpdate(entity);
        return mapper.updateByPrimaryKey(entity);
    }

    /**
     * Updates an existing entity, specifying which fields to update. Sets common entity properties like modifier and
     * modification time before updating.
     *
     * @param entity the entity to update
     * @param fields the fields to be updated
     * @return the updated entity
     */
    @Override
    public Object update(T entity, Fn<T, Object>... fields) {
        entity.setUpdate(entity);
        return mapper.updateForFieldListByPrimaryKey(entity, Fn.of(fields));
    }

    /**
     * Updates an existing entity, only including non-null fields. Sets common entity properties like modifier and
     * modification time before updating.
     *
     * @param entity the entity to update
     * @return the updated entity
     */
    @Override
    public Object updateSelective(T entity) {
        entity.setUpdate(entity);
        return mapper.updateByPrimaryKeySelective(entity);
    }

    /**
     * Updates an existing entity, only including non-null fields, and forces update for specified fields. Sets common
     * entity properties like modifier and modification time before updating.
     *
     * @param entity the entity to update
     * @param fields the fields to force update
     * @return the updated entity
     */
    @Override
    public Object updateSelective(T entity, Fn<T, Object>... fields) {
        entity.setUpdate(entity);
        return mapper.updateByPrimaryKeySelectiveWithForceFields(entity, Fn.of(fields));
    }

    /**
     * Inserts a new entity or updates an existing one with all fields. Determines whether to insert or update based on
     * the primary key's value.
     *
     * @param entity the entity to insert or update
     * @return the inserted or updated entity
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
     * Inserts a new entity or updates an existing one, only including non-null fields. Determines whether to insert or
     * update based on the primary key's value.
     *
     * @param entity the entity to insert or update
     * @return the inserted or updated entity
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
     * Performs a logical deletion of an entity. This involves setting the entity's status to {@link Consts#MINUS_ONE}
     * and updating the entity.
     *
     * @param entity the entity to be logically removed
     * @return the number of affected rows
     */
    @Override
    public long remove(T entity) {
        entity.setStatus(Consts.MINUS_ONE);
        entity.setUpdate(entity);
        return mapper.updateByPrimaryKeySelective(entity);
    }

    /**
     * Performs a physical deletion of an entity from the database.
     *
     * @param entity the entity to delete
     * @return the number of records deleted, greater than 0 indicates success
     */
    @Override
    public long delete(T entity) {
        return mapper.delete(entity);
    }

    /**
     * Deletes an entity by its primary key.
     *
     * @param id the primary key
     * @return the number of records deleted, 1 indicates success
     */
    @Override
    public long deleteById(I id) {
        return mapper.deleteByPrimaryKey(id);
    }

    /**
     * Deletes entities by a collection of primary keys.
     *
     * @param ids the collection of primary keys
     * @return the number of records deleted
     */
    @Override
    public long deleteByIds(Collection<I> ids) {
        return deleteByFieldList(entity -> (I) entity.getId(), ids);
    }

    /**
     * Deletes entities by a collection of field values.
     *
     * @param field          the field to match against
     * @param fieldValueList the collection of field values
     * @param <F>            the type of the field value
     * @return the number of records deleted
     */
    @Override
    public <F> long deleteByFieldList(Fn<T, F> field, Collection<F> fieldValueList) {
        return mapper.deleteByFieldList(field, fieldValueList);
    }

    /**
     * Retrieves an entity by its primary key.
     *
     * @param id the primary key
     * @return the entity, or null if not found
     */
    @Override
    public Object selectById(I id) {
        return mapper.selectByPrimaryKey(id);
    }

    /**
     * Retrieves a single entity based on the provided entity conditions.
     *
     * @param entity the entity containing query conditions
     * @return the entity, or null if not found
     */
    @Override
    public Object selectOne(T entity) {
        return mapper.selectOne(entity);
    }

    /**
     * Retrieves a list of entities based on the provided entity conditions.
     *
     * @param entity the entity containing query conditions
     * @return a list of entities
     */
    @Override
    public List<T> selectList(T entity) {
        return mapper.selectList(entity);
    }

    /**
     * Retrieves a list of entities based on a collection of field values.
     *
     * @param field          the field to match against
     * @param fieldValueList the collection of field values
     * @param <F>            the type of the field value
     * @return a list of entities
     */
    @Override
    public <F> List<T> selectByFieldList(Fn<T, F> field, Collection<F> fieldValueList) {
        return mapper.selectByFieldList(field, fieldValueList);
    }

    /**
     * Retrieves all records.
     *
     * @return a list of all entities
     */
    @Override
    public List<T> selectAll() {
        return mapper.selectList(null);
    }

    /**
     * Counts the number of records matching the provided entity conditions.
     *
     * @param entity the entity containing query conditions
     * @return the total number of records
     */
    @Override
    public long count(T entity) {
        return mapper.selectCount(entity);
    }

    /**
     * Returns a new {@link Condition} object for building complex queries.
     *
     * @return a new Condition object
     */
    @Override
    public Condition<T> condition() {
        return SharedService.super.condition();
    }

    /**
     * Deletes records based on the provided {@link Condition}.
     *
     * @param condition the query condition
     * @return the number of records deleted, greater than 0 indicates success
     */
    @Override
    public long delete(Condition<T> condition) {
        return mapper.deleteByCondition(condition);
    }

    /**
     * Updates records based on the provided entity and {@link Condition}, updating all fields.
     *
     * @param entity    the entity with updated information
     * @param condition the query condition
     * @return the number of records updated, greater than 0 indicates success
     */
    @Override
    public long update(T entity, Condition<T> condition) {
        return mapper.updateByCondition(entity, condition);
    }

    /**
     * Updates records based on the provided entity and {@link Condition}, only updating non-null fields.
     *
     * @param entity    the entity with updated information
     * @param condition the query condition
     * @return the number of records updated, greater than 0 indicates success
     */
    @Override
    public long updateSelective(T entity, Condition<T> condition) {
        return mapper.updateByConditionSelective(entity, condition);
    }

    /**
     * Retrieves a single entity based on the provided {@link Condition}.
     *
     * @param condition the query condition
     * @return the entity, or null if not found
     */
    @Override
    public Object selectOne(Condition<T> condition) {
        return mapper.selectOneByCondition(condition).orElse(null);
    }

    /**
     * Retrieves the first entity found based on the provided {@link Condition}.
     *
     * @param condition the query condition
     * @return the first entity found, or null if no entities match the condition
     */
    @Override
    public Object selectFirst(Condition<T> condition) {
        List<T> list = mapper.selectByCondition(condition);
        return (list == null || list.isEmpty()) ? null : list.get(0);
    }

    /**
     * Retrieves a list of entities based on the provided {@link Condition}.
     *
     * @param condition the query condition
     * @return a list of entities
     */
    @Override
    public List<T> selectList(Condition<T> condition) {
        return mapper.selectByCondition(condition);
    }

    /**
     * Counts the number of records matching the provided {@link Condition}.
     *
     * @param condition the query condition
     * @return the total number of records
     */
    @Override
    public long count(Condition<T> condition) {
        return mapper.countByCondition(condition);
    }

    /**
     * Checks if the primary key of the given entity has a value.
     *
     * @param entity the entity to check
     * @return true if the primary key has a value, false otherwise
     */
    @Override
    public boolean pkHasValue(T entity) {
        TableMeta entityTable = mapper.entityTable();
        List<ColumnMeta> idColumns = entityTable.idColumns();
        return idColumns.get(0).fieldMeta().get(entity) != null;
    }

    /**
     * Performs a paginated query based on the provided entity, which should contain pagination and sorting parameters.
     *
     * @param entity the entity containing pagination and sorting parameters
     * @return a {@link Result} object containing the paginated list of records and the total count
     */
    @Override
    public Result<T> page(T entity) {
        PageContext.startPage(entity.getPageNo(), entity.getPageSize());
        if (StringKit.isNotEmpty(entity.getOrderBy())) {
            PageContext.orderBy(entity.getOrderBy());
        }
        Page<T> list = (Page<T>) mapper.selectList(entity);
        return Result.<T>builder().rows(list.getResult()).total(list.getTotal()).build();
    }

    /**
     * Returns a new {@link ConditionWrapper} object for building complex query conditions.
     *
     * @return a new ConditionWrapper object
     */
    @Override
    public ConditionWrapper<T, I> wrapper() {
        return mapper.wrapper();
    }

    /**
     * Sets common entity property values such as status, operator, and operation time. If the entity's status is empty,
     * it defaults to {@link Consts#ONE}.
     *
     * @param entity the entity to set values for
     * @return the ID of the entity, or null if the entity is null
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
