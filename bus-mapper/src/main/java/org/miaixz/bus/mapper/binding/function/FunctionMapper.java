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
package org.miaixz.bus.mapper.binding.function;

import java.util.List;

import org.apache.ibatis.annotations.Lang;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.mapper.Caching;
import org.miaixz.bus.mapper.provider.FunctionProvider;

/**
 * 可指定字段的操作接口，提供基于字段的更新和查询方法
 *
 * @param <T> 实体类类型
 * @author Kimi Liu
 * @since Java 17+
 */
public interface FunctionMapper<T> {

    /**
     * 根据主键更新实体中非空字段，并强制更新指定字段
     *
     * @param entity 实体对象
     * @param fields 强制更新的字段集合，通过 {@link Fn#of(Fn...)} 创建 {@link Fn.FnArray}
     * @param <S>    实体类型
     * @return 1 表示成功，0 表示失败
     */
    @Lang(Caching.class)
    @UpdateProvider(type = FunctionProvider.class, method = "updateByPrimaryKeySelectiveWithForceFields")
    <S extends T> int updateByPrimaryKeySelectiveWithForceFields(@Param("entity") S entity,
            @Param("fns") Fn.FnArray<T> fields);

    /**
     * 根据实体字段条件查询唯一实体，仅返回指定字段
     *
     * @param entity       实体对象
     * @param selectFields 查询的字段集合，通过 {@link Fn#of(Fn...)} 创建 {@link Fn.FnArray}
     * @return 唯一实体对象，若结果多条则抛出异常，可能为空
     */
    @Lang(Caching.class)
    @SelectProvider(type = FunctionProvider.class, method = "selectColumns")
    Optional<T> selectColumnsOne(@Param("entity") T entity, @Param("fns") Fn.FnArray<T> selectFields);

    /**
     * 根据实体字段条件批量查询，仅返回指定字段
     *
     * @param entity       实体对象
     * @param selectFields 查询的字段集合，通过 {@link Fn#of(Fn...)} 创建 {@link Fn.FnArray}
     * @return 实体对象列表
     */
    @Lang(Caching.class)
    @SelectProvider(type = FunctionProvider.class, method = "selectColumns")
    List<T> selectColumns(@Param("entity") T entity, @Param("fns") Fn.FnArray<T> selectFields);

}