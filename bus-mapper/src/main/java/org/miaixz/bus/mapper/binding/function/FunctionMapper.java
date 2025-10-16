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
 * An interface for operations that can target specific fields, providing methods for field-based updates and queries.
 *
 * @param <T> The type of the entity class.
 * @author Kimi Liu
 * @since Java 17+
 */
public interface FunctionMapper<T> {

    /**
     * Updates non-null fields of an entity by its primary key, while also forcing an update on specified fields.
     *
     * @param entity The entity object containing the updated values.
     * @param fields A collection of fields to be forcibly updated, created via {@link Fn#of(Fn...)}.
     * @param <S>    The type of the entity.
     * @return The number of affected rows (1 for success, 0 for failure).
     */
    @Lang(Caching.class)
    @UpdateProvider(type = FunctionProvider.class, method = "updateByPrimaryKeySelectiveWithForceFields")
    <S extends T> int updateByPrimaryKeySelectiveWithForceFields(
            @Param("entity") S entity,
            @Param("fns") Fn.FnArray<T> fields);

    /**
     * Selects a single, unique entity based on the fields of the provided entity, returning only the specified columns.
     *
     * @param entity       The entity object containing the query criteria.
     * @param selectFields A collection of fields to be selected, created via {@link Fn#of(Fn...)}.
     * @return An {@link Optional} containing the unique entity object, or an empty Optional if not found. Throws an
     *         exception if more than one record is found.
     */
    @Lang(Caching.class)
    @SelectProvider(type = FunctionProvider.class, method = "selectColumns")
    Optional<T> selectColumnsOne(@Param("entity") T entity, @Param("fns") Fn.FnArray<T> selectFields);

    /**
     * Selects a list of entities based on the fields of the provided entity, returning only the specified columns.
     *
     * @param entity       The entity object containing the query criteria.
     * @param selectFields A collection of fields to be selected, created via {@link Fn#of(Fn...)}.
     * @return A list of entity objects.
     */
    @Lang(Caching.class)
    @SelectProvider(type = FunctionProvider.class, method = "selectColumns")
    List<T> selectColumns(@Param("entity") T entity, @Param("fns") Fn.FnArray<T> selectFields);

}
