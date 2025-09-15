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
package org.miaixz.bus.mapper.binding.cursor;

import org.apache.ibatis.annotations.Lang;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.cursor.Cursor;
import org.miaixz.bus.mapper.Caching;
import org.miaixz.bus.mapper.binding.condition.Condition;
import org.miaixz.bus.mapper.provider.ConditionProvider;
import org.miaixz.bus.mapper.provider.EntityProvider;

/**
 * 游标查询接口，提供基于实体和条件的游标查询方法
 *
 * @param <T> 实体类类型
 * @param <E> 符合 Condition 数据结构的对象，如 {@link Condition} 或 MBG 生成的 Condition 对象
 * @author Kimi Liu
 * @since Java 17+
 */
public interface CursorMapper<T, E> {

    /**
     * 根据实体字段条件进行游标查询
     *
     * @param entity 实体对象
     * @return 实体对象游标
     */
    @Lang(Caching.class)
    @SelectProvider(type = EntityProvider.class, method = "select")
    Cursor<T> selectCursor(T entity);

    /**
     * 根据 Condition 条件进行游标查询
     *
     * @param condition 条件对象
     * @return 实体对象游标
     */
    @Lang(Caching.class)
    @SelectProvider(type = ConditionProvider.class, method = "selectByCondition")
    Cursor<T> selectCursorByCondition(E condition);

}