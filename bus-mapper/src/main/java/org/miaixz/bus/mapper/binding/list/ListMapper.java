/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org mybatis.io and other contributors.         ~
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
package org.miaixz.bus.mapper.binding.list;

import java.util.List;

import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Lang;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.UpdateProvider;
import org.miaixz.bus.mapper.Caching;
import org.miaixz.bus.mapper.provider.ListProvider;

/**
 * 批量操作接口，提供实体列表的批量插入和更新方法
 *
 * @param <T> 实体类类型
 * @author Kimi Liu
 * @since Java 17+
 */
public interface ListMapper<T> {

    /**
     * 批量插入实体列表
     *
     * @param entityList 实体对象列表
     * @param <S>        实体类型
     * @return 插入成功的记录数，等于 entityList.size() 表示成功，否则失败
     */
    @Lang(Caching.class)
    @InsertProvider(type = ListProvider.class, method = "insertList")
    <S extends T> int insertList(@Param("entityList") List<S> entityList);

    /**
     * 批量更新实体列表
     *
     * @param entityList 实体对象列表
     * @param <S>        实体类型
     * @return 更新成功的记录数
     */
    @Lang(Caching.class)
    @UpdateProvider(type = ListProvider.class, method = "updateList")
    <S extends T> int updateList(@Param("entityList") List<S> entityList);

    /**
     * 批量更新实体列表中非空字段
     *
     * @param entityList 实体对象列表
     * @param <S>        实体类型
     * @return 更新成功的记录数
     */
    @Lang(Caching.class)
    @UpdateProvider(type = ListProvider.class, method = "updateListSelective")
    <S extends T> int updateListSelective(@Param("entityList") List<S> entityList);

}