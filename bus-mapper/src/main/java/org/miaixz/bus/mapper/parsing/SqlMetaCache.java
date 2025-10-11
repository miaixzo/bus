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
package org.miaixz.bus.mapper.parsing;

import java.util.function.Supplier;

import org.apache.ibatis.builder.annotation.ProviderContext;

/**
 * An SQL cache class for deferred generation of SQL scripts.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SqlMetaCache {

    /**
     * A null object instance.
     */
    public static final SqlMetaCache NULL = new SqlMetaCache(null, null, null);

    /**
     * The execution method context.
     */
    private final ProviderContext providerContext;

    /**
     * The entity class metadata.
     */
    private final TableMeta tableMeta;

    /**
     * The SQL script supplier.
     */
    private final Supplier<String> sqlScriptSupplier;

    /**
     * Constructor to initialize the SQL cache.
     *
     * @param providerContext   The execution method context.
     * @param tableMeta         The entity class metadata.
     * @param sqlScriptSupplier The SQL script supplier.
     */
    public SqlMetaCache(ProviderContext providerContext, TableMeta tableMeta, Supplier<String> sqlScriptSupplier) {
        this.providerContext = providerContext;
        this.tableMeta = tableMeta;
        this.sqlScriptSupplier = sqlScriptSupplier;
    }

    /**
     * Gets the SQL script. Execution is deferred until the SqlSource is finally generated.
     *
     * @return The SQL script.
     */
    public String getSqlScript() {
        return sqlScriptSupplier.get();
    }

    /**
     * Gets the execution method context.
     *
     * @return The execution method context.
     */
    public ProviderContext getProviderContext() {
        return providerContext;
    }

    /**
     * Gets the entity class metadata.
     *
     * @return The entity class metadata.
     */
    public TableMeta getTableMeta() {
        return tableMeta;
    }

}
