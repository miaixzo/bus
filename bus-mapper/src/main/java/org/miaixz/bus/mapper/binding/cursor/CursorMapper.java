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
package org.miaixz.bus.mapper.binding.cursor;

import org.apache.ibatis.annotations.Lang;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.cursor.Cursor;
import org.miaixz.bus.mapper.Caching;
import org.miaixz.bus.mapper.binding.condition.Condition;
import org.miaixz.bus.mapper.provider.ConditionProvider;
import org.miaixz.bus.mapper.provider.EntityProvider;

/**
 * An interface for cursor-based queries, providing methods for querying with cursors based on entities and conditions.
 * Cursors are suitable for handling large result sets by fetching data row by row, which can reduce memory consumption.
 *
 * @param <T> The type of the entity class.
 * @param <E> An object that conforms to the Condition data structure, such as {@link Condition} or an MBG-generated
 *            Condition object.
 * @author Kimi Liu
 * @since Java 17+
 */
public interface CursorMapper<T, E> {

    /**
     * Performs a cursor-based query based on the fields of the given entity.
     *
     * @param entity The entity object containing the query criteria.
     * @return A {@link Cursor} for the entity objects.
     */
    @Lang(Caching.class)
    @SelectProvider(type = EntityProvider.class, method = "select")
    Cursor<T> selectCursor(T entity);

    /**
     * Performs a cursor-based query based on the given {@link Condition}.
     *
     * @param condition The condition object.
     * @return A {@link Cursor} for the entity objects.
     */
    @Lang(Caching.class)
    @SelectProvider(type = ConditionProvider.class, method = "selectByCondition")
    Cursor<T> selectCursorByCondition(E condition);

}
