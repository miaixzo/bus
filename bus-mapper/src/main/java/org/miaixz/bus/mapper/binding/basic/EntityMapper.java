/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org mapper.io and other contributors.         ~
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
package org.miaixz.bus.mapper.binding.basic;

import java.util.List;

import org.apache.ibatis.annotations.*;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.mapper.Caching;
import org.miaixz.bus.mapper.provider.EntityProvider;

/**
 * 实体类基本操作接口，提供对实体类的增删改查等常用方法
 *
 * @param <T> 实体类类型
 * @param <I> 主键类型
 * @author Kimi Liu
 * @since Java 17+
 */
public interface EntityMapper<T, I> extends ClassMapper<T> {

    /**
     * 保存实体对象
     *
     * @param entity 实体对象
     * @return 1表示成功，0表示失败
     */
    @Lang(Caching.class)
    @InsertProvider(type = EntityProvider.class, method = "insert")
    <S extends T> int insert(S entity);

    /**
     * 保存实体对象中非空字段
     *
     * @param entity 实体对象
     * @return 1表示成功，0表示失败
     */
    @Lang(Caching.class)
    @InsertProvider(type = EntityProvider.class, method = "insertSelective")
    <S extends T> int insertSelective(S entity);

    /**
     * 根据主键删除实体
     *
     * @param id 主键
     * @return 1表示成功，0表示失败
     */
    @Lang(Caching.class)
    @DeleteProvider(type = EntityProvider.class, method = "deleteByPrimaryKey")
    int deleteByPrimaryKey(I id);

    /**
     * 根据实体条件批量删除
     *
     * @param entity 实体对象
     * @return 大于等于1表示成功，0表示失败
     */
    @Lang(Caching.class)
    @DeleteProvider(type = EntityProvider.class, method = "delete")
    int delete(T entity);

    /**
     * 根据主键更新实体对象
     *
     * @param entity 实体对象
     * @return 1表示成功，0表示失败
     */
    @Lang(Caching.class)
    @UpdateProvider(type = EntityProvider.class, method = "updateByPrimaryKey")
    <S extends T> int updateByPrimaryKey(S entity);

    /**
     * 根据主键更新实体对象中非空字段
     *
     * @param entity 实体对象
     * @return 1表示成功，0表示失败
     */
    @Lang(Caching.class)
    @UpdateProvider(type = EntityProvider.class, method = "updateByPrimaryKeySelective")
    <S extends T> int updateByPrimaryKeySelective(S entity);

    /**
     * 根据主键查询实体对象
     *
     * @param id 主键
     * @return 实体对象，可能为空
     */
    @Lang(Caching.class)
    @SelectProvider(type = EntityProvider.class, method = "selectByPrimaryKey")
    Optional<T> selectByPrimaryKey(I id);

    /**
     * 根据实体条件查询唯一实体
     *
     * @param entity 实体对象
     * @return 唯一实体对象，若结果多条则抛出异常，可能为空
     */
    @Lang(Caching.class)
    @SelectProvider(type = EntityProvider.class, method = "select")
    Optional<T> selectOne(T entity);

    /**
     * 根据实体条件批量查询
     *
     * @param entity 实体对象
     * @return 实体对象列表
     */
    @Lang(Caching.class)
    @SelectProvider(type = EntityProvider.class, method = "select")
    List<T> selectList(T entity);

    /**
     * 根据实体条件查询记录总数
     *
     * @param entity 实体对象
     * @return 记录总数
     */
    @Lang(Caching.class)
    @SelectProvider(type = EntityProvider.class, method = "selectCount")
    long selectCount(T entity);

}