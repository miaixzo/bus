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
package org.miaixz.bus.mapper.binding.basic;

import java.util.List;

import org.apache.ibatis.annotations.*;
import org.miaixz.bus.mapper.Caching;
import org.miaixz.bus.mapper.provider.EntityProvider;

/**
 * An interface for basic entity operations, providing common methods for CRUD (Create, Read, Update, Delete).
 *
 * @param <T> The type of the entity class.
 * @param <I> The type of the primary key.
 * @author Kimi Liu
 * @since Java 17+
 */
public interface EntityMapper<T, I> extends ClassMapper<T> {

    /**
     * Saves an entity object.
     *
     * @param entity The entity object to save.
     * @param <S>    A subtype of the entity class.
     * @return The number of affected rows (1 for success, 0 for failure).
     */
    @Lang(Caching.class)
    @InsertProvider(type = EntityProvider.class, method = "insert")
    <S extends T> int insert(S entity);

    /**
     * Saves only the non-null fields of an entity object.
     *
     * @param entity The entity object to save.
     * @param <S>    A subtype of the entity class.
     * @return The number of affected rows (1 for success, 0 for failure).
     */
    @Lang(Caching.class)
    @InsertProvider(type = EntityProvider.class, method = "insertSelective")
    <S extends T> int insertSelective(S entity);

    /**
     * Deletes an entity by its primary key.
     *
     * @param id The primary key of the entity to delete.
     * @return The number of affected rows (1 for success, 0 for failure).
     */
    @Lang(Caching.class)
    @DeleteProvider(type = EntityProvider.class, method = "deleteByPrimaryKey")
    int deleteByPrimaryKey(I id);

    /**
     * Deletes entities based on the conditions provided in the entity object.
     *
     * @param entity The entity object containing the deletion criteria.
     * @return The number of deleted records (>=1 for success, 0 for failure).
     */
    @Lang(Caching.class)
    @DeleteProvider(type = EntityProvider.class, method = "delete")
    int delete(T entity);

    /**
     * Updates an entity object by its primary key.
     *
     * @param entity The entity object with updated values.
     * @param <S>    A subtype of the entity class.
     * @return The number of affected rows (1 for success, 0 for failure).
     */
    @Lang(Caching.class)
    @UpdateProvider(type = EntityProvider.class, method = "updateByPrimaryKey")
    <S extends T> int updateByPrimaryKey(S entity);

    /**
     * Updates only the non-null fields of an entity object by its primary key.
     *
     * @param entity The entity object with updated values.
     * @param <S>    A subtype of the entity class.
     * @return The number of affected rows (1 for success, 0 for failure).
     */
    @Lang(Caching.class)
    @UpdateProvider(type = EntityProvider.class, method = "updateByPrimaryKeySelective")
    <S extends T> int updateByPrimaryKeySelective(S entity);

    /**
     * Selects an entity by its primary key.
     *
     * @param id The primary key of the entity to retrieve.
     * @return The entity object, or null if not found.
     */
    @Lang(Caching.class)
    @SelectProvider(type = EntityProvider.class, method = "selectByPrimaryKey")
    T selectByPrimaryKey(I id);

    /**
     * Selects a single entity based on the conditions provided in the entity object. Throws an exception if more than
     * one record is found.
     *
     * @param entity The entity object containing the query criteria.
     * @return The unique entity object, or null if not found.
     */
    @Lang(Caching.class)
    @SelectProvider(type = EntityProvider.class, method = "select")
    T selectOne(T entity);

    /**
     * Selects a list of entities based on the conditions provided in the entity object.
     *
     * @param entity The entity object containing the query criteria.
     * @return A list of entity objects.
     */
    @Lang(Caching.class)
    @SelectProvider(type = EntityProvider.class, method = "select")
    List<T> selectList(T entity);

    /**
     * Selects the total number of records matching the conditions provided in the entity object.
     *
     * @param entity The entity object containing the query criteria.
     * @return The total number of matching records.
     */
    @Lang(Caching.class)
    @SelectProvider(type = EntityProvider.class, method = "selectCount")
    long selectCount(T entity);

}
