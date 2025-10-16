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
package org.miaixz.bus.pager.dialect.replace;

import org.miaixz.bus.pager.dialect.ReplaceSql;

/**
 * Implements {@link ReplaceSql} to handle SQL Server's `with(nolock)` hint using regular expressions. It temporarily
 * replaces `with(nolock)` with a placeholder for SQL parsing and then restores it.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RegexWithNolock implements ReplaceSql {

    /**
     * Placeholder for `with(nolock)` during SQL parsing.
     */
    protected String WITHNOLOCK = ", PAGEWITHNOLOCK";

    /**
     * Replaces `with(nolock)` clauses in the SQL with a temporary placeholder. This is done to prevent parsing errors
     * with SQL parsers that do not understand `with(nolock)`.
     *
     * @param sql the original SQL string
     * @return the SQL string with `with(nolock)` replaced by a placeholder
     */
    @Override
    public String replace(String sql) {
        return sql.replaceAll("((?i)\\s*(\\w+)\\s*with\\s*\\(\\s*nolock\\s*\\))", " $2_PAGEWITHNOLOCK");
    }

    /**
     * Restores the `with(nolock)` clauses in the SQL from their temporary placeholders. This is called after SQL
     * parsing and modification.
     *
     * @param sql the SQL string with placeholders
     * @return the SQL string with `with(nolock)` restored
     */
    @Override
    public String restore(String sql) {
        return sql.replaceAll("\\s*(\\w*?)_PAGEWITHNOLOCK", " $1 WITH(NOLOCK)");
    }

}
