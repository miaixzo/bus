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
package org.miaixz.bus.mapper.mapping;

import java.util.Collections;
import java.util.List;

import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.mapper.ORDER;

/**
 * 实体类信息工厂接口，可通过 SPI 加入处理链以扩展列信息创建逻辑
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface ColumnFactory extends ORDER {

    /**
     * 忽略字段的默认值，表示空列信息列表
     */
    Optional<List<MapperColumn>> IGNORE = Optional.of(Collections.emptyList());

    /**
     * 创建列信息，一个字段可能不是列，也可能是列，还可能对应多个列（如 ValueObject 对象）
     *
     * @param entityTable 实体表信息
     * @param field       字段信息
     * @param chain       工厂链，用于调用下一个处理逻辑
     * @return 实体类中列的信息的 Optional 包装对象，若为空则表示不属于实体中的列
     */
    Optional<List<MapperColumn>> createEntityColumn(MapperTable entityTable, MapperFields field, Chain chain);

    /**
     * 工厂链接口，用于链式调用列信息创建逻辑
     */
    interface Chain {
        /**
         * 创建列信息，一个字段可能不是列，也可能是列，还可能对应多个列（如 ValueObject 对象）
         *
         * @param entityTable 实体表信息
         * @param field       字段信息
         * @return 实体类中列的信息的 Optional 包装对象，若为空则表示不属于实体中的列
         */
        Optional<List<MapperColumn>> createEntityColumn(MapperTable entityTable, MapperFields field);
    }

}