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
