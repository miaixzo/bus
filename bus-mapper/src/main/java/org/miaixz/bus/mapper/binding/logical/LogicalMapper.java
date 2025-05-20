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
 * 逻辑删除操作接口，覆盖基础查询、删除、更新方法以支持逻辑删除
 *
 * @param <T> 实体类类型
 * @param <I> 主键类型
 * @author Kimi Liu
 * @since Java 17+
 */
public interface LogicalMapper<T, I extends Serializable> extends BasicMapper<T, I>, FunctionMapper<T> {

    /**
     * 根据主键更新实体中非空字段，并强制更新指定字段
     *
     * @param entity 实体对象
     * @param fields 强制更新的字段集合，通过 {@link Fn#of(Fn...)} 创建 {@link Fn.FnArray}
     * @param <S>    实体类型
     * @return 1 表示成功，0 表示失败
     */
    @Override
    @Lang(Caching.class)
    @UpdateProvider(type = LogicalProvider.class, method = "updateByPrimaryKeySelectiveWithForceFields")
    <S extends T> int updateByPrimaryKeySelectiveWithForceFields(@Param("entity") S entity,
            @Param("fns") Fn.FnArray<T> fields);

    /**
     * 根据实体字段条件查询唯一实体，仅返回指定字段
     *
     * @param entity       实体对象
     * @param selectFields 查询的字段集合，通过 {@link Fn#of(Fn...)} 创建 {@link Fn.FnArray}
     * @return 唯一实体对象，若结果多条则抛出异常，可能为空
     */
    @Override
    @Lang(Caching.class)
    @SelectProvider(type = LogicalProvider.class, method = "selectColumns")
    Optional<T> selectColumnsOne(@Param("entity") T entity, @Param("fns") Fn.FnArray<T> selectFields);

    /**
     * 根据实体字段条件批量查询，仅返回指定字段
     *
     * @param entity       实体对象
     * @param selectFields 查询的字段集合，通过 {@link Fn#of(Fn...)} 创建 {@link Fn.FnArray}
     * @return 实体对象列表
     */
    @Override
    @Lang(Caching.class)
    @SelectProvider(type = LogicalProvider.class, method = "selectColumns")
    List<T> selectColumns(@Param("entity") T entity, @Param("fns") Fn.FnArray<T> selectFields);

    /**
     * 根据主键逻辑删除记录
     *
     * @param id 主键
     * @return 1 表示成功，0 表示失败
     */
    @Override
    @Lang(Caching.class)
    @DeleteProvider(type = LogicalProvider.class, method = "deleteByPrimaryKey")
    int deleteByPrimaryKey(I id);

    /**
     * 根据实体条件逻辑删除记录
     *
     * @param entity 实体对象
     * @return 大于等于 1 表示成功，0 表示失败
     */
    @Override
    @Lang(Caching.class)
    @DeleteProvider(type = LogicalProvider.class, method = "delete")
    int delete(T entity);

    /**
     * 根据主键更新实体
     *
     * @param entity 实体对象
     * @param <S>    实体类型
     * @return 1 表示成功，0 表示失败
     */
    @Override
    @Lang(Caching.class)
    @UpdateProvider(type = LogicalProvider.class, method = "updateByPrimaryKey")
    <S extends T> int updateByPrimaryKey(S entity);

    /**
     * 根据主键更新实体中非空字段
     *
     * @param entity 实体对象
     * @param <S>    实体类型
     * @return 1 表示成功，0 表示失败
     */
    @Override
    @Lang(Caching.class)
    @UpdateProvider(type = LogicalProvider.class, method = "updateByPrimaryKeySelective")
    <S extends T> int updateByPrimaryKeySelective(S entity);

    /**
     * 根据主键查询实体
     *
     * @param id 主键
     * @return 实体对象，可能为空
     */
    @Override
    @Lang(Caching.class)
    @SelectProvider(type = LogicalProvider.class, method = "selectByPrimaryKey")
    Optional<T> selectByPrimaryKey(I id);

    /**
     * 根据实体字段条件查询唯一实体
     *
     * @param entity 实体对象
     * @return 唯一实体对象，若结果多条则抛出异常，可能为空
     */
    @Override
    @Lang(Caching.class)
    @SelectProvider(type = LogicalProvider.class, method = "select")
    Optional<T> selectOne(T entity);

    /**
     * 根据实体字段条件批量查询
     *
     * @param entity 实体对象
     * @return 实体对象列表
     */
    @Override
    @Lang(Caching.class)
    @SelectProvider(type = LogicalProvider.class, method = "select")
    List<T> selectList(T entity);

    /**
     * 根据实体字段条件查询记录总数
     *
     * @param entity 实体对象
     * @return 记录总数
     */
    @Override
    @Lang(Caching.class)
    @SelectProvider(type = LogicalProvider.class, method = "selectCount")
    long selectCount(T entity);

    /**
     * 根据实体字段条件进行游标查询
     *
     * @param entity 实体对象
     * @return 实体对象游标
     */
    @Override
    @Lang(Caching.class)
    @SelectProvider(type = LogicalProvider.class, method = "select")
    Cursor<T> selectCursor(T entity);

    /**
     * 根据 Condition 条件进行游标查询
     *
     * @param condition 条件对象
     * @return 实体对象游标
     */
    @Override
    @Lang(Caching.class)
    @SelectProvider(type = LogicalProvider.class, method = "selectByCondition")
    Cursor<T> selectCursorByCondition(Condition<T> condition);

    /**
     * 创建 Condition 查询对象
     *
     * @return Condition 对象
     */
    @Override
    default Condition<T> condition() {
        return BasicMapper.super.condition();
    }

    /**
     * 根据 Condition 条件逻辑删除记录
     *
     * @param condition 条件对象
     * @return 大于等于 1 表示成功，0 表示失败
     */
    @Override
    @Lang(Caching.class)
    @DeleteProvider(type = LogicalProvider.class, method = "deleteByCondition")
    int deleteByCondition(Condition<T> condition);

    /**
     * 根据 Condition 条件批量更新实体
     *
     * @param entity    实体对象
     * @param condition 条件对象
     * @param <S>       实体类型
     * @return 大于等于 1 表示成功，0 表示失败
     */
    @Override
    @Lang(Caching.class)
    @UpdateProvider(type = LogicalProvider.class, method = "updateByCondition")
    <S extends T> int updateByCondition(@Param("entity") S entity, @Param("condition") Condition<T> condition);

    /**
     * 根据 Condition 条件和 SET 值更新字段
     *
     * @param condition 条件对象
     * @return 大于等于 1 表示成功，0 表示失败
     */
    @Override
    @Lang(Caching.class)
    @UpdateProvider(type = LogicalProvider.class, method = "updateByConditionSetValues")
    int updateByConditionSetValues(@Param("condition") Condition<T> condition);

    /**
     * 根据 Condition 条件批量更新实体非空字段
     *
     * @param entity    实体对象
     * @param condition 条件对象
     * @param <S>       实体类型
     * @return 大于等于 1 表示成功，0 表示失败
     */
    @Override
    @Lang(Caching.class)
    @UpdateProvider(type = LogicalProvider.class, method = "updateByConditionSelective")
    <S extends T> int updateByConditionSelective(@Param("entity") S entity, @Param("condition") Condition<T> condition);

    /**
     * 根据 Condition 条件批量查询
     *
     * @param condition 条件对象
     * @return 实体对象列表
     */
    @Override
    @Lang(Caching.class)
    @SelectProvider(type = LogicalProvider.class, method = "selectByCondition")
    List<T> selectByCondition(Condition<T> condition);

    /**
     * 根据 Condition 条件查询唯一实体
     *
     * @param condition 条件对象
     * @return 唯一实体对象，若结果多条则抛出异常，可能为空
     */
    @Override
    @Lang(Caching.class)
    @SelectProvider(type = LogicalProvider.class, method = "selectByCondition")
    Optional<T> selectOneByCondition(Condition<T> condition);

    /**
     * 根据 Condition 条件查询记录总数
     *
     * @param condition 条件对象
     * @return 记录总数
     */
    @Override
    @Lang(Caching.class)
    @SelectProvider(type = LogicalProvider.class, method = "countByCondition")
    long countByCondition(Condition<T> condition);

    /**
     * 根据 Condition 条件分页查询
     *
     * @param condition 条件对象
     * @param rowBounds 分页信息
     * @return 实体对象列表
     */
    @Override
    List<T> selectByCondition(Condition<T> condition, RowBounds rowBounds);

}