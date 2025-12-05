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
package org.miaixz.bus.mapper.binding;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.apache.ibatis.annotations.Lang;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.UpdateProvider;
import org.miaixz.bus.mapper.Caching;
import org.miaixz.bus.mapper.binding.basic.EntityMapper;
import org.miaixz.bus.mapper.binding.batch.BatchMapper;
import org.miaixz.bus.mapper.binding.condition.Condition;
import org.miaixz.bus.mapper.binding.condition.ConditionMapper;
import org.miaixz.bus.mapper.binding.condition.ConditionWrapper;
import org.miaixz.bus.mapper.binding.cursor.CursorMapper;
import org.miaixz.bus.mapper.binding.function.Fn;
import org.miaixz.bus.mapper.provider.FunctionProvider;

/**
 * A base mapper interface that integrates common database operations. It supports inheritance and method overriding to
 * provide custom functionality.
 *
 * @param <T> The type of the entity class.
 * @param <I> The type of the primary key.
 * @author Kimi Liu
 * @since Java 17+
 */
public interface BasicMapper<T, I extends Serializable>
        extends EntityMapper<T, I>, ConditionMapper<T, Condition<T>>, CursorMapper<T, Condition<T>>, BatchMapper<T> {

    /**
     * Creates a {@link ConditionWrapper} object for building complex queries.
     *
     * @return A new {@link ConditionWrapper} instance.
     */
    default ConditionWrapper<T, I> wrapper() {
        return new ConditionWrapper<>(this, condition());
    }

    /**
     * Updates non-null fields of an entity by its primary key, while forcing an update on specified fields.
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
     * Updates a specified set of fields for an entity by its primary key.
     *
     * @param entity The entity object containing the updated values.
     * @param fields A collection of fields to be updated, created via {@link Fn#of(Fn...)}.
     * @param <S>    The type of the entity.
     * @return The number of affected rows (1 for success, 0 for failure).
     */
    @Lang(Caching.class)
    @UpdateProvider(type = FunctionProvider.class, method = "updateForFieldListByPrimaryKey")
    <S extends T> int updateForFieldListByPrimaryKey(@Param("entity") S entity, @Param("fns") Fn.FnArray<T> fields);

    /**
     * Selects a list of entities where the specified field is in the given list of values (i.e.,
     * {@code field IN (fieldValueList)}).
     *
     * @param field          A method reference to the field (e.g., {@code User::getName}).
     * @param fieldValueList A collection of values for the specified field.
     * @param <F>            The type of the field.
     * @return A list of entities that match the condition.
     */
    default <F> List<T> selectByFieldList(Fn<T, F> field, Collection<F> fieldValueList) {
        Condition<T> condition = new Condition<>();
        condition.createCriteria().andIn((Fn<T, Object>) field.in(entityClass()), fieldValueList);
        return selectByCondition(condition);
    }

    /**
     * Deletes entities where the specified field is in the given list of values (i.e.,
     * {@code field IN (fieldValueList)}).
     *
     * @param field          A method reference to the field (e.g., {@code User::getName}).
     * @param fieldValueList A collection of values for the specified field.
     * @param <F>            The type of the field.
     * @return The number of deleted records.
     */
    default <F> int deleteByFieldList(Fn<T, F> field, Collection<F> fieldValueList) {
        Condition<T> condition = new Condition<>();
        condition.createCriteria().andIn((Fn<T, Object>) field.in(entityClass()), fieldValueList);
        return deleteByCondition(condition);
    }

}
