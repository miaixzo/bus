/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.mapper.parsing;

import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.ibatis.builder.annotation.ProviderContext;
import org.miaixz.bus.mapper.dialect.Dialect;

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
     * The dynamic SQL script function that accepts Dialect parameter.
     * <p>
     * If this is non-null, SQL will be generated dynamically based on the current dialect. This is used for
     * multi-datasource scenarios where different databases may require different SQL syntax.
     * </p>
     */
    private final Function<Dialect, String> dynamicSqlScriptFunction;

    /**
     * Constructor to initialize the SQL cache (static SQL).
     *
     * @param providerContext   The execution method context.
     * @param tableMeta         The entity class metadata.
     * @param sqlScriptSupplier The SQL script supplier.
     */
    public SqlMetaCache(ProviderContext providerContext, TableMeta tableMeta, Supplier<String> sqlScriptSupplier) {
        this(providerContext, tableMeta, sqlScriptSupplier, null);
    }

    /**
     * Constructor to initialize the SQL cache (with optional dynamic SQL support).
     *
     * @param providerContext          The execution method context.
     * @param tableMeta                The entity class metadata.
     * @param sqlScriptSupplier        The SQL script supplier (used if dynamicSqlScriptFunction is null).
     * @param dynamicSqlScriptFunction The dynamic SQL function that accepts Dialect (optional).
     */
    public SqlMetaCache(ProviderContext providerContext, TableMeta tableMeta, Supplier<String> sqlScriptSupplier,
            Function<Dialect, String> dynamicSqlScriptFunction) {
        this.providerContext = providerContext;
        this.tableMeta = tableMeta;
        this.sqlScriptSupplier = sqlScriptSupplier;
        this.dynamicSqlScriptFunction = dynamicSqlScriptFunction;
    }

    /**
     * Gets the SQL script.
     * <p>
     * If this cache has a dynamic SQL function, it will use the default dialect. For multi-datasource scenarios, use
     * {@link #getSqlScript(Dialect)} instead.
     * </p>
     *
     * @return The SQL script.
     */
    public String getSqlScript() {
        return sqlScriptSupplier.get();
    }

    /**
     * Gets the SQL script for a specific dialect.
     * <p>
     * If this cache has a dynamic SQL function, it will generate SQL using the provided dialect. Otherwise, it returns
     * the static SQL.
     * </p>
     *
     * @param dialect The database dialect.
     * @return The SQL script.
     */
    public String getSqlScript(Dialect dialect) {
        if (dynamicSqlScriptFunction != null) {
            return dynamicSqlScriptFunction.apply(dialect);
        }
        return sqlScriptSupplier.get();
    }

    /**
     * Checks if this cache uses dynamic SQL generation.
     *
     * @return true if dynamic SQL function is present, false otherwise.
     */
    public boolean isDynamic() {
        return dynamicSqlScriptFunction != null;
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
