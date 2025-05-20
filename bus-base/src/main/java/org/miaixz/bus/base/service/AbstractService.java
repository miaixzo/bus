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
 * 基于 spring 实现 BasicService 接口
 *
 * 根据业务需要如无status，creator等相关属性内容 重写此类及{@link BaseEntity} 业务类继承新类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AbstractService<T extends BaseEntity, I extends Serializable, M extends SharedMapper<T, I>>
        implements SharedService<T, I> {

    @Autowired
    protected M mapper;

    /**
     * 通用：插入实体（所有字段）
     *
     * @param entity 实体类
     * @return 保存后的实体
     */
    @Override
    public Object insert(T entity) {
        this.setValue(entity);
        return mapper.insert(entity);
    }

    /**
     * 通用：插入实体（非空字段）
     *
     * @param entity 实体类
     * @return 保存后的实体
     */
    @Override
    public Object insertSelective(T entity) {
        this.setValue(entity);
        return mapper.insertSelective(entity);
    }

    /**
     * 通用：批量插入实体（所有字段）
     *
     * @param list 实体列表
     * @return 保存后的实体列表
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
     * 通用：批量插入实体（非空字段）
     *
     * @param list 实体列表
     * @return 保存后的实体列表
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
     * 通用：更新实体（所有字段）
     *
     * @param entity 实体类
     * @return 更新后的实体
     */
    @Override
    public Object update(T entity) {
        entity.setUpdate(entity);
        return mapper.updateByPrimaryKey(entity);
    }

    /**
     * 通用：更新实体（指定字段）
     *
     * @param entity 实体类
     * @param fields 需要更新的字段
     * @return 更新后的实体
     */
    @Override
    public Object update(T entity, Fn<T, Object>... fields) {
        entity.setUpdate(entity);
        return mapper.updateForFieldListByPrimaryKey(entity, Fn.of(fields));
    }

    /**
     * 通用：更新实体（非空字段）
     *
     * @param entity 实体类
     * @return 更新后的实体
     */
    @Override
    public Object updateSelective(T entity) {
        entity.setUpdate(entity);
        return mapper.updateByPrimaryKeySelective(entity);
    }

    /**
     * 通用：更新实体（非空字段，强制更新指定字段）
     *
     * @param entity 实体类
     * @param fields 强制更新的字段
     * @return 更新后的实体
     */
    @Override
    public Object updateSelective(T entity, Fn<T, Object>... fields) {
        entity.setUpdate(entity);
        return mapper.updateByPrimaryKeySelectiveWithForceFields(entity, Fn.of(fields));
    }

    /**
     * 通用：插入或更新实体（所有字段）
     *
     * @param entity 实体类
     * @return 保存或更新后的实体
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
     * 通用：插入或更新实体（非空字段）
     *
     * @param entity 实体类
     * @return 保存或更新后的实体
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
     * 通用：逻辑删除实体
     *
     * @param entity 要删除的实体
     * @return 受影响的行数
     */
    @Override
    public long remove(T entity) {
        entity.setStatus(Consts.STATUS_MINUS_ONE);
        entity.setUpdate(entity);
        return mapper.updateByPrimaryKey(entity);
    }

    /**
     * 通用：删除实体
     *
     * @param entity 实体类
     * @return 删除的记录数，大于0表示成功
     */
    @Override
    public long delete(T entity) {
        return mapper.delete(entity);
    }

    /**
     * 通用：根据主键删除
     *
     * @param id 主键
     * @return 删除的记录数，1表示成功
     */
    @Override
    public long deleteById(I id) {
        return mapper.deleteByPrimaryKey(id);
    }

    /**
     * 通用：根据多个主键集合删除
     *
     * @param ids 主键集合
     * @return 删除的记录数
     */
    @Override
    public long deleteByIds(Collection<I> ids) {
        return deleteByFieldList(entity -> (I) entity.getId(), ids);
    }

    /**
     * 通用：根据指定字段集合删除
     *
     * @param field          字段
     * @param fieldValueList 字段值集合
     * @param <F>            字段值类型
     * @return 删除的记录数
     */
    @Override
    public <F> long deleteByFieldList(Fn<T, F> field, Collection<F> fieldValueList) {
        return mapper.deleteByFieldList(field, fieldValueList);
    }

    /**
     * 通用：根据主键查询
     *
     * @param id 主键
     * @return 实体，未找到返回null
     */
    @Override
    public Object selectById(I id) {
        return mapper.selectByPrimaryKey(id).orElse(null);
    }

    /**
     * 通用：根据实体条件查询单条记录
     *
     * @param entity 实体类
     * @return 实体，未找到返回null
     */
    @Override
    public Object selectOne(T entity) {
        return mapper.selectOne(entity).orElse(null);
    }

    /**
     * 通用：根据实体条件查询列表
     *
     * @param entity 实体类
     * @return 实体列表
     */
    @Override
    public List<T> selectList(T entity) {
        return mapper.selectList(entity);
    }

    /**
     * 通用：根据指定字段集合查询
     *
     * @param field          字段
     * @param fieldValueList 字段值集合
     * @param <F>            字段值类型
     * @return 实体列表
     */
    @Override
    public <F> List<T> selectByFieldList(Fn<T, F> field, Collection<F> fieldValueList) {
        return mapper.selectByFieldList(field, fieldValueList);
    }

    /**
     * 通用：查询所有记录
     *
     * @return 实体列表
     */
    @Override
    public List<T> selectAll() {
        return mapper.selectList(null);
    }

    /**
     * 通用：根据实体条件查询总数
     *
     * @param entity 实体类
     * @return 记录总数
     */
    @Override
    public long count(T entity) {
        return mapper.selectCount(entity);
    }

    @Override
    public Condition<T> condition() {
        return SharedService.super.condition();
    }

    /**
     * 通用：根据条件批量删除
     *
     * @param condition 查询条件
     * @return 删除的记录数，大于0表示成功
     */
    @Override
    public long delete(Condition<T> condition) {
        return mapper.deleteByCondition(condition);
    }

    /**
     * 通用：根据条件批量更新（所有字段）
     *
     * @param entity    实体类
     * @param condition 查询条件
     * @return 更新的记录数，大于0表示成功
     */
    @Override
    public long update(T entity, Condition<T> condition) {
        return mapper.updateByCondition(entity, condition);
    }

    /**
     * 通用：根据条件批量更新（非空字段）
     *
     * @param entity    实体类
     * @param condition 查询条件
     * @return 更新的记录数，大于0 STRUCTURE表示成功
     */
    @Override
    public long updateSelective(T entity, Condition<T> condition) {
        return mapper.updateByConditionSelective(entity, condition);
    }

    /**
     * 通用：根据条件查询单条记录
     *
     * @param condition 查询条件
     * @return 实体，未找到返回null
     */
    @Override
    public Object selectOne(Condition<T> condition) {
        return mapper.selectOneByCondition(condition).orElse(null);
    }

    /**
     * 通用：根据条件查询列表
     *
     * @param condition 查询条件
     * @return 实体列表
     */
    @Override
    public List<T> selectList(Condition<T> condition) {
        return mapper.selectByCondition(condition);
    }

    /**
     * 通用：根据条件查询总数
     *
     * @param condition 查询条件
     * @return 记录总数
     */
    @Override
    public long count(Condition<T> condition) {
        return mapper.countByCondition(condition);
    }

    /**
     * 通用：检查实体主键是否有值
     *
     * @param entity 实体类
     * @return true表示有值，false表示为空
     */
    @Override
    public boolean pkHasValue(T entity) {
        TableMeta entityTable = mapper.entityTable();
        List<ColumnMeta> idColumns = entityTable.idColumns();
        return idColumns.get(0).field().get(entity) != null;
    }

    /**
     * 通用：分页查询
     *
     * @param entity 实体类（包含分页和排序参数）
     * @return 分页结果，包含记录列表和总数
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
     * 通用：获取条件包装器
     *
     * @return 条件包装器
     */
    @Override
    public ConditionWrapper<T, I> wrapper() {
        return mapper.wrapper();
    }

    /**
     * 设置实体属性值（状态、操作人、操作时间等）
     *
     * @param entity 实体类
     * @return 实体ID
     */
    protected String setValue(T entity) {
        if (ObjectKit.isEmpty(entity)) {
            return null;
        }
        if (ObjectKit.isEmpty(entity.getStatus())) {
            entity.setStatus(Consts.STATUS_ONE);
        }
        entity.setValue(entity);
        return entity.getId();
    }

}