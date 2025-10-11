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
 ~ OUT of OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.pager.handler;

import java.util.List;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import org.miaixz.bus.mapper.Args;

/**
 * Interface for providing row-level multi-tenancy support. Implementations of this interface define how tenant
 * information is retrieved and applied to SQL queries.
 */
public interface TenantProvider {

    /**
     * Retrieves the tenant ID value as an {@link Expression}. This method should only support a single tenant ID value.
     *
     * @return the tenant ID expression
     */
    Expression getTenantId();

    /**
     * Retrieves the name of the tenant field.
     *
     * @return the tenant field name, defaulting to "tenant_id"
     */
    default String getColumn() {
        return Args.TENANT_TABLE_COLUMN;
    }

    /**
     * Determines whether to ignore multi-tenancy conditions for a specific table.
     *
     * @param name the name of the table
     * @return true to ignore, false to apply tenant conditions
     */
    default boolean ignore(String name) {
        return false;
    }

    /**
     * Determines whether to ignore inserting the tenant field during an INSERT operation. This is typically used to
     * check if the tenant column is already present in the list of columns.
     *
     * @param columns the list of columns in the INSERT statement
     * @param column  the name of the tenant ID field
     * @return true to ignore, false to insert the tenant field
     */
    default boolean ignore(List<Column> columns, String column) {
        return columns.stream().map(Column::getColumnName).anyMatch(name -> name.equalsIgnoreCase(column));
    }

}
