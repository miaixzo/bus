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
 * @since Java 21+
 */
public interface ListMapper<T> {

    /**
     * Performs a batch insert of a list of entities.
     *
     * @param list The list of entity objects to insert.
     * @param <S>  A subtype of the entity class.
     * @return The number of successfully inserted records. A return value equal to {@code list.size()} indicates
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
