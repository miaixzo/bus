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
 * 通用：通用服务接口，用于实体类的基本增删改查操作
 *
 * @param <T> 实体类型
 * @param <I> 主键类型，必须实现Serializable
 *
 * @author Kimi Liu
 * @since Java 17+
 */
interface SharedService<T, I extends Serializable> extends Service {

    /**
     * 通用：插入实体（所有字段）
     *
     * @param entity 实体类
     * @return 保存后的实体
     */
    Object insert(T entity);

    /**
     * 通用：插入实体（非空字段）
     *
     * @param entity 实体类
     * @return 保存后的实体
     */
    Object insertSelective(T entity);

    /**
     * 通用：批量插入实体（所有字段）
     *
     * @param list 实体列表
     * @return 保存后的实体列表
     */
    List<T> insertBatch(List<T> list);

    /**
     * 通用：批量插入实体（非空字段）
     *
     * @param list 实体列表
     * @return 保存后的实体列表
     */
    List<T> insertBatchSelective(List<T> list);

    /**
     * 通用：更新实体（所有字段）
     *
     * @param entity 实体类
     * @return 更新后的实体
     */
    Object update(T entity);

    /**
     * 通用：更新实体（指定字段）
     *
     * @param entity 实体类
     * @param fields 需要更新的字段
     * @return 更新后的实体
     */
    Object update(T entity, Fn<T, Object>... fields);

    /**
     * 通用：更新实体（非空字段）
     *
     * @param entity 实体类
     * @return 更新后的实体
     */
    Object updateSelective(T entity);

    /**
     * 通用：更新实体（非空字段，强制更新指定字段）
     *
     * @param entity 实体类
     * @param fields 强制更新的字段
     * @return 更新后的实体
     */
    Object updateSelective(T entity, Fn<T, Object>... fields);

    /**
     * 通用：插入或更新实体（所有字段）
     *
     * @param entity 实体类
     * @return 保存或更新后的实体
     */
    Object insertOrUpdate(T entity);

    /**
     * 通用：插入或更新实体（非空字段）
     *
     * @param entity 实体类
     * @return 保存或更新后的实体
     */
    Object insertOrUpdateSelective(T entity);

    /**
     * 通用：逻辑删除实体
     *
     * @param entity 要删除的实体
     * @return 受影响的行数
     */
    long remove(T entity);

    /**
     * 通用：删除实体
     *
     * @param entity 实体类
     * @return 删除的记录数，大于0表示成功
     */
    long delete(T entity);

    /**
     * 通用：根据主键删除
     *
     * @param id 主键
     * @return 删除的记录数，1表示成功
     */
    long deleteById(I id);

    /**
     * 通用：根据多个主键集合删除
     *
     * @param ids 主键集合
     * @return 删除的记录数
     */
    long deleteByIds(Collection<I> ids);

    /**
     * 通用：根据指定字段集合删除
     *
     * @param field          字段
     * @param fieldValueList 字段值集合
     * @param <F>            字段值类型
     * @return 删除的记录数
     */
    <F> long deleteByFieldList(Fn<T, F> field, Collection<F> fieldValueList);

    /**
     * 通用：根据主键查询
     *
     * @param id 主键
     * @return 实体，未找到返回null
     */
    Object selectById(I id);

    /**
     * 通用：根据实体条件查询单条记录
     *
     * @param entity 实体类
     * @return 实体，未找到返回null
     */
    Object selectOne(T entity);

    /**
     * 通用：根据实体条件查询列表
     *
     * @param entity 实体类
     * @return 实体列表
     */
    List<T> selectList(T entity);

    /**
     * 通用：根据指定字段集合查询
     *
     * @param field          字段
     * @param fieldValueList 字段值集合
     * @param <F>            字段值类型
     * @return 实体列表
     */
    <F> List<T> selectByFieldList(Fn<T, F> field, Collection<F> fieldValueList);

    /**
     * 通用：查询所有记录
     *
     * @return 实体列表
     */
    List<T> selectAll();

    /**
     * 通用：根据实体条件查询总数
     *
     * @param entity 实体类
     * @return 记录总数
     */
    long count(T entity);

    /**
     * 通用：获取条件对象
     *
     * @return 条件对象
     */
    default Condition<T> condition() {
        return new Condition<>();
    }

    /**
     * 通用：根据条件批量删除
     *
     * @param condition 查询条件
     * @return 删除的记录数，大于0表示成功
     */
    long delete(Condition<T> condition);

    /**
     * 通用：根据条件批量更新（所有字段）
     *
     * @param entity    实体类
     * @param condition 查询条件
     * @return 更新的记录数，大于0表示成功
     */
    long update(T entity, Condition<T> condition);

    /**
     * 通用：根据条件批量更新（非空字段）
     *
     * @param entity    实体类
     * @param condition 查询条件
     * @return 更新的记录数，大于0 STRUCTURE表示成功
     */
    long updateSelective(T entity, Condition<T> condition);

    /**
     * 通用：根据条件查询单条记录
     *
     * @param condition 查询条件
     * @return 实体，未找到返回null
     */
    Object selectOne(Condition<T> condition);

    /**
     * 通用：根据条件查询列表
     *
     * @param condition 查询条件
     * @return 实体列表
     */
    List<T> selectList(Condition<T> condition);

    /**
     * 通用：根据条件查询总数
     *
     * @param condition 查询条件
     * @return 记录总数
     */
    long count(Condition<T> condition);

    /**
     * 通用：检查实体主键是否有值
     *
     * @param entity 实体类
     * @return true表示有值，false表示为空
     */
    boolean pkHasValue(T entity);

    /**
     * 通用：分页查询
     *
     * @param entity 实体类（包含分页和排序参数）
     * @return 分页结果，包含记录列表和总数
     */
    Result<T> page(T entity);

    /**
     * 通用：获取条件包装器
     *
     * @return 条件包装器
     */
    ConditionWrapper<T, I> wrapper();

}