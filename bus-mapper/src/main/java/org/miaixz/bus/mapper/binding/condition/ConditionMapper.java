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
package org.miaixz.bus.mapper.binding.condition;

import java.util.List;

import org.apache.ibatis.annotations.*;
import org.apache.ibatis.session.RowBounds;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.mapper.Caching;
import org.miaixz.bus.mapper.provider.ConditionProvider;

/**
 * 基于 Condition 的查询和操作接口，提供条件查询、更新、删除等功能
 *
 * @param <T> 实体类类型
 * @param <E> 符合 Condition 数据结构的对象，如 {@link Condition} 或 MBG 生成的 Condition 对象
 * @author Kimi Liu
 * @since Java 17+
 */
public interface ConditionMapper<T, E> {

    /**
     * 创建并返回 Condition 对象
     *
     * @return 新创建的 Condition 对象
     */
    default Condition<T> condition() {
        return new Condition<>();
    }

    /**
     * 根据 Condition 条件删除记录
     *
     * @param condition 条件对象
     * @return 大于等于1表示成功，0表示失败
     */
    @Lang(Caching.class)
    @DeleteProvider(type = ConditionProvider.class, method = "deleteByCondition")
    int deleteByCondition(E condition);

    /**
     * 根据 Condition 条件批量更新实体
     *
     * @param entity    实体对象
     * @param condition 条件对象
     * @return 大于等于1表示成功，0表示失败
     */
    @Lang(Caching.class)
    @UpdateProvider(type = ConditionProvider.class, method = "updateByCondition")
    <S extends T> int updateByCondition(@Param("entity") S entity, @Param("condition") E condition);

    /**
     * 根据 Condition 条件和 SET 值更新字段
     *
     * @param condition 条件对象
     * @return 大于等于1表示成功，0表示失败
     */
    @Lang(Caching.class)
    @UpdateProvider(type = ConditionProvider.class, method = "updateByConditionSetValues")
    int updateByConditionSetValues(@Param("condition") E condition);

    /**
     * 根据 Condition 条件批量更新实体非空字段
     *
     * @param entity    实体对象
     * @param condition 条件对象
     * @return 大于等于1表示成功，0表示失败
     */
    @Lang(Caching.class)
    @UpdateProvider(type = ConditionProvider.class, method = "updateByConditionSelective")
    <S extends T> int updateByConditionSelective(@Param("entity") S entity, @Param("condition") E condition);

    /**
     * 根据 Condition 条件批量查询
     *
     * @param condition 条件对象
     * @return 实体对象列表
     */
    @Lang(Caching.class)
    @SelectProvider(type = ConditionProvider.class, method = "selectByCondition")
    List<T> selectByCondition(E condition);

    /**
     * 根据 Condition 条件查询唯一实体
     *
     * @param condition 条件对象
     * @return 唯一实体对象，若结果多条则抛出异常，可能为空
     */
    @Lang(Caching.class)
    @SelectProvider(type = ConditionProvider.class, method = "selectByCondition")
    Optional<T> selectOneByCondition(E condition);

    /**
     * 根据 Condition 条件查询记录总数
     *
     * @param condition 条件对象
     * @return 记录总数
     */
    @Lang(Caching.class)
    @SelectProvider(type = ConditionProvider.class, method = "countByCondition")
    long countByCondition(E condition);

    /**
     * 根据 Condition 条件分页查询
     *
     * @param condition 条件对象
     * @param rowBounds 分页信息
     * @return 实体对象列表
     */
    List<T> selectByCondition(E condition, RowBounds rowBounds);

}