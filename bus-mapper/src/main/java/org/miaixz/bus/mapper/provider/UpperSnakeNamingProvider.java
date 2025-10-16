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
package org.miaixz.bus.mapper.provider;

import org.miaixz.bus.mapper.Args;
import org.miaixz.bus.mapper.parsing.FieldMeta;
import org.miaixz.bus.mapper.parsing.TableMeta;

/**
 * Provides an uppercase snake_case naming style generator for table and column names, converting camelCase names to
 * uppercase with underscores.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class UpperSnakeNamingProvider extends SnakeCaseNamingProvider {

    /**
     * Gets the naming style, returning the uppercase snake_case naming style identifier.
     *
     * @return The uppercase snake_case naming style identifier.
     */
    @Override
    public String type() {
        return Args.CAMEL_UNDERLINE_UPPER_CASE;
    }

    /**
     * Gets the table name, converting it from camelCase to uppercase snake_case.
     *
     * @param entityClass The entity class.
     * @return The table name in uppercase snake_case.
     */
    @Override
    public String tableName(Class<?> entityClass) {
        return super.tableName(entityClass).toUpperCase();
    }

    /**
     * Gets the column name, converting it from camelCase to uppercase snake_case.
     *
     * @param entityTable The entity table information.
     * @param field       The entity field information.
     * @return The column name in uppercase snake_case.
     */
    @Override
    public String columnName(TableMeta entityTable, FieldMeta field) {
        return super.columnName(entityTable, field).toUpperCase();
    }

}
