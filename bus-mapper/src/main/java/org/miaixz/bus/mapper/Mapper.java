/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.mapper;

import java.io.Serializable;

import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Lang;
import org.apache.ibatis.annotations.Options;
import org.miaixz.bus.mapper.binding.BasicMapper;
import org.miaixz.bus.mapper.provider.EntityProvider;

/**
 * An example of a custom Mapper interface that overrides the {@code insert} method for auto-incrementing primary keys.
 * This is primarily intended to demonstrate usage.
 * <p>
 * When using databases like Oracle, you can customize the primary key generation logic using the {@code @SelectKey}
 * annotation.
 * </p>
 *
 * @param <T> The type of the entity class.
 * @param <I> The type of the primary key.
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Mapper<T, I extends Serializable> extends BasicMapper<T, I>, Marker {

    /**
     * Saves an entity, assuming the primary key is auto-incrementing and named "id".
     * <p>
     * This method serves as an example of how to override parent interface configurations in a custom interface.
     * </p>
     *
     * @param entity The entity object to save.
     * @param <S>    A subtype of the entity class.
     * @return The number of affected rows, typically 1 for success or 0 for failure.
     */
    @Override
    @Lang(Caching.class)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    @InsertProvider(type = EntityProvider.class, method = "insert")
    <S extends T> int insert(S entity);

    /**
     * Saves non-null fields of an entity, assuming the primary key is auto-incrementing and named "id".
     * <p>
     * This method serves as an example of how to override parent interface configurations in a custom interface.
     * </p>
     *
     * @param entity The entity object to save.
     * @param <S>    A subtype of the entity class.
     * @return The number of affected rows, typically 1 for success or 0 for failure.
     */
    @Override
    @Lang(Caching.class)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    @InsertProvider(type = EntityProvider.class, method = "insertSelective")
    <S extends T> int insertSelective(S entity);

}
