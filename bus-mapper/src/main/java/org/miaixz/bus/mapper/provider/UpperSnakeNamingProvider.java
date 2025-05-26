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
 * 提供大写下划线命名风格的表名和列名生成器，将驼峰命名转换为大写下划线命名。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class UpperSnakeNamingProvider extends SnakeCaseNamingProvider {

    /**
     * 获取命名风格，返回大写下划线命名风格。
     *
     * @return 大写下划线命名风格标识
     */
    @Override
    public String type() {
        return Args.CAMEL_UNDERLINE_UPPER_CASE;
    }

    /**
     * 获取表名，将驼峰风格的表名转换为大写下划线风格。
     *
     * @param entityClass 实体类
     * @return 大写下划线风格的表名
     */
    @Override
    public String tableName(Class<?> entityClass) {
        return super.tableName(entityClass).toUpperCase();
    }

    /**
     * 获取列名，将驼峰风格的列名转换为大写下划线风格。
     *
     * @param entityTable 实体表信息
     * @param field       实体字段信息
     * @return 大写下划线风格的列名
     */
    @Override
    public String columnName(TableMeta entityTable, FieldMeta field) {
        return super.columnName(entityTable, field).toUpperCase();
    }

}