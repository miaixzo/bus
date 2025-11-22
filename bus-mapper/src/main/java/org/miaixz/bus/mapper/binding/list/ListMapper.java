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
package org.miaixz.bus.mapper.binding.list;

import java.util.List;

import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Lang;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.UpdateProvider;
import org.miaixz.bus.mapper.Caching;
import org.miaixz.bus.mapper.provider.ListProvider;

/**
 * An interface for batch operations, providing methods for batch insertion and updates of entity lists.
 *
 * @param <T> The type of the entity class.
 * @author Kimi Liu
 * @since Java 17+
 */
public interface ListMapper<T> {

    /**
     * Performs a batch insert of a list of entities.
     *
     * @param list The list of entity objects to insert.
     * @param <S>  A subtype of the entity class.
     * @return The number of successfully inserted records. A return value equal to {@code entityList.size()} indicates
     *         success; otherwise, it indicates failure.
     */
    @Lang(Caching.class)
    @InsertProvider(type = ListProvider.class, method = "insertList")
    <S extends T> int insertList(@Param("list") List<S> list);

    /**
     * Performs a batch update of a list of entities based on their primary keys.
     *
     * @param list The list of entity objects to update.
     * @param <S>  A subtype of the entity class.
     * @return The number of successfully updated records.
     */
    @Lang(Caching.class)
    @UpdateProvider(type = ListProvider.class, method = "updateList")
    <S extends T> int updateList(@Param("list") List<S> list);

    /**
     * Performs a batch update of the non-null fields of a list of entities based on their primary keys.
     *
     * @param list The list of entity objects to update.
     * @param <S>  A subtype of the entity class.
     * @return The number of successfully updated records.
     */
    @Lang(Caching.class)
    @UpdateProvider(type = ListProvider.class, method = "updateListSelective")
    <S extends T> int updateListSelective(@Param("list") List<S> list);

}
