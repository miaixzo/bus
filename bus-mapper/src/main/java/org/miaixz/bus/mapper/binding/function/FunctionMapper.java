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
