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
package org.miaixz.bus.mapper.support.keysql;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.SqlSource;

/**
 * Wraps an {@link SqlSource} to enable primary key generation before insertion.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class GenIdSqlSource implements SqlSource {

    /**
     * The original SQL source.
     */
    private final SqlSource sqlSource;

    /**
     * The primary key generator.
     */
    private final GenIdKeyGenerator keyGenerator;

    /**
     * Constructs a new GenIdSqlSource, initializing the SQL source and key generator.
     *
     * @param sqlSource    The original SQL source.
     * @param keyGenerator The primary key generator.
     */
    public GenIdSqlSource(SqlSource sqlSource, GenIdKeyGenerator keyGenerator) {
        this.sqlSource = sqlSource;
        this.keyGenerator = keyGenerator;
    }

    /**
     * Gets the bound SQL and generates the primary key if necessary before execution.
     *
     * @param parameterObject The parameter object.
     * @return The bound SQL object.
     */
    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        // Ensure primary key generation for the first time if missed during initialization.
        keyGenerator.prepare(parameterObject);
        return sqlSource.getBoundSql(parameterObject);
    }

}
