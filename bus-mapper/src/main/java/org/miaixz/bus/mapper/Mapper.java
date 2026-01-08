/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
