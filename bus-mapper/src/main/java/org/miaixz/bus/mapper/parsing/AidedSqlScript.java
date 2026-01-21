/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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

import org.miaixz.bus.mapper.dialect.Dialect;

/**
 * An aided SQL script interface with enhanced building capabilities.
 *
 * <p>
 * "Aided" means this interface provides additional aid (helper references) to facilitate SQL building. It extends the
 * base {@link SqlScript} interface by providing contextual helpers for more powerful SQL generation.
 * </p>
 *
 * <p>
 * This interface combines two powerful SQL generation approaches:
 * </p>
 * <ul>
 * <li>Callback-based SQL generation - Passes a helper reference for simplified coding</li>
 * <li>Dialect-based SQL generation - Adapts SQL to different database dialects dynamically</li>
 * </ul>
 *
 * <p>
 * Usage Pattern 1: Callback Style (simplified coding)
 * </p>
 *
 * <pre>{@code
 *
 * // Implement for simplified SQL building
 * public class UserMapperSql implements AidedSqlScript {
 *
 *     @Override
 *     public String getSql(TableMeta entity, SqlScript sqlScript) {
 *         return sqlScript.where(() -> sqlScript.ifTest("name != null", "name = #{name}"));
 *     }
 * }
 *
 * // Or use lambda directly
 * String sql = SqlScript.caching(providerContext, (entity, script) -> {
 *     return script.where(() -> script.ifTest("status != null", "status = #{status}"));
 * });
 * }</pre>
 *
 * <p>
 * Usage Pattern 2: Dialect Style (multi-database support)
 * </p>
 *
 * <pre>{@code
 *
 * // In Provider class for UPSERT operations
 * public static String insertUp(ProviderContext providerContext) {
 *     return SqlScript.cachingDynamic(providerContext, (entity, dialect) -> {
 *         if (!dialect.supportsUpsert()) {
 *             throw new UnsupportedOperationException(dialect.getDatabase() + " does not support UPSERT");
 *         }
 *         String template = dialect.getUpsertTemplate();
 *         // Build UPSERT SQL using template
 *         return String.format(template, tableName, columns, values);
 *     });
 * }
 * }</pre>
 *
 * <p>
 * Design Philosophy:
 * </p>
 * <ul>
 * <li>Callback style: SQL generated at cache time (static), optimized for simplified coding</li>
 * <li>Dialect style: SQL generated at execution time (dynamic), optimized for multi-database scenarios</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface AidedSqlScript extends SqlScript {

    /**
     * Generates SQL based on database dialect.
     *
     * <p>
     * This method is designed for multi-database scenarios where SQL needs to be generated differently based on the
     * current datasource's dialect. The SQL is generated at execution time when the dialect is known.
     * </p>
     *
     * @param entity  The entity class information.
     * @param dialect The database dialect for the current datasource.
     * @return The XML SQL script.
     */
    String getSql(TableMeta entity, Dialect dialect);

    /**
     * Generates SQL with callback support for using helper methods.
     *
     * <p>
     * This method is designed for simplified SQL building where you can use the provided {@code sqlScript} reference to
     * access helper methods like {@code where()}, {@code ifTest()}, {@code foreach()}, etc.
     * </p>
     *
     * <p>
     * The SQL is generated at cache time and remains constant during execution.
     * </p>
     *
     * @param entity    The entity class information.
     * @param sqlScript A reference to the current object, useful for using its methods within a lambda.
     * @return The XML SQL script.
     */
    default String getSql(TableMeta entity, SqlScript sqlScript) {
        throw new UnsupportedOperationException("Callback-style SQL generation not implemented. "
                + "Please use dialect-style getSql(TableMeta, Dialect) instead.");
    }

    /**
     * The default implementation for standard {@link SqlScript} interface.
     *
     * <p>
     * Delegates to the callback-style method with {@code this} as the sqlScript parameter.
     * </p>
     *
     * @param entity The entity class information.
     * @return The XML SQL script.
     */
    @Override
    default String getSql(TableMeta entity) {
        return getSql(entity, this);
    }

}
