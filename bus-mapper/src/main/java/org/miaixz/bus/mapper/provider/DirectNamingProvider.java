/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org mapper.io and other contributors.         ~
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
package org.miaixz.bus.mapper.provider;

import org.miaixz.bus.mapper.Args;
import org.miaixz.bus.mapper.parsing.FieldMeta;
import org.miaixz.bus.mapper.parsing.TableMeta;

/**
 * 提供标准的命名风格生成器，使用原始类名和字段名作为表名和列名。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DirectNamingProvider implements NamingProvider {

    /**
     * 获取命名风格，返回标准命名风格。
     *
     * @return 标准命名风格标识
     */
    @Override
    public Object type() {
        return Args.NORMAL;
    }

    /**
     * 获取表名，使用实体类的简单类名。
     *
     * @param entityClass 实体类
     * @return 表名
     */
    @Override
    public String tableName(Class<?> entityClass) {
        return entityClass.getSimpleName();
    }

    /**
     * 获取列名，使用字段的名称。
     *
     * @param entityTable 实体表信息
     * @param field       实体字段信息
     * @return 列名
     */
    @Override
    public String columnName(TableMeta entityTable, FieldMeta field) {
        return field.getName();
    }

}