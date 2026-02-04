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
package org.miaixz.bus.mapper.binding.condition;

import java.util.List;

import org.apache.ibatis.annotations.*;
import org.apache.ibatis.session.RowBounds;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.mapper.Caching;
import org.miaixz.bus.mapper.provider.ConditionProvider;

/**
 * An interface for condition-based queries and operations, providing methods for conditional queries, updates, and
 * deletions.
 *
 * @param <T> The type of the entity class.
 * @param <E> An object that conforms to the Condition data structure, such as {@link Condition} or an MBG-generated
 *            Condition object.
 * @author Kimi Liu
 * @since Java 17+
 */
public interface ConditionMapper<T, E> {

    /**
     * Creates and returns a new {@link Condition} object.
     *
     * @return A new {@link Condition} object.
     */
    default Condition<T> condition() {
        return new Condition<>();
    }

    /**
     * Deletes records based on the given {@link Condition}.
     *
     * @param condition The condition object.
     * @return The number of affected rows (>=1 for success, 0 for failure).
     */
    @Lang(Caching.class)
    @DeleteProvider(type = ConditionProvider.class, method = "deleteByCondition")
    int deleteByCondition(E condition);

    /**
     * Updates entity records based on the given {@link Condition}.
     *
     * @param entity    The entity object with updated values.
     * @param condition The condition object.
     * @param <S>       A subtype of the entity class.
     * @return The number of affected rows (>=1 for success, 0 for failure).
     */
    @Lang(Caching.class)
    @UpdateProvider(type = ConditionProvider.class, method = "updateByCondition")
    <S extends T> int updateByCondition(@Param("entity") S entity, @Param("condition") E condition);

    /**
     * Updates fields based on the given {@link Condition} and the values set in it.
     *
     * @param condition The condition object containing the values to be set.
     * @return The number of affected rows (>=1 for success, 0 for failure).
     */
    @Lang(Caching.class)
    @UpdateProvider(type = ConditionProvider.class, method = "updateByConditionSetValues")
    int updateByConditionSetValues(@Param("condition") E condition);

    /**
     * Updates non-null fields of an entity based on the given {@link Condition}.
     *
     * @param entity    The entity object with updated values.
     * @param condition The condition object.
     * @param <S>       A subtype of the entity class.
     * @return The number of affected rows (>=1 for success, 0 for failure).
     */
    @Lang(Caching.class)
    @UpdateProvider(type = ConditionProvider.class, method = "updateByConditionSelective")
    <S extends T> int updateByConditionSelective(@Param("entity") S entity, @Param("condition") E condition);

    /**
     * Selects a list of entities based on the given {@link Condition}.
     *
     * @param condition The condition object.
     * @return A list of entity objects.
     */
    @Lang(Caching.class)
    @SelectProvider(type = ConditionProvider.class, method = "selectByCondition")
    List<T> selectByCondition(E condition);

    /**
     * Selects a single entity based on the given {@link Condition}. Throws an exception if more than one record is
     * found.
     *
     * @param condition The condition object.
     * @return An {@link Optional} containing the unique entity object, or an empty Optional if not found.
     */
    @Lang(Caching.class)
    @SelectProvider(type = ConditionProvider.class, method = "selectByCondition")
    T selectOneByCondition(E condition);

    /**
     * Counts the number of records matching the given {@link Condition}.
     *
     * @param condition The condition object.
     * @return The total number of matching records.
     */
    @Lang(Caching.class)
    @SelectProvider(type = ConditionProvider.class, method = "countByCondition")
    long countByCondition(E condition);

    /**
     * Selects a list of entities with pagination based on the given {@link Condition}.
     *
     * @param condition The condition object.
     * @param rowBounds The pagination information.
     * @return A list of entity objects for the current page.
     */
    List<T> selectByCondition(E condition, RowBounds rowBounds);

}
