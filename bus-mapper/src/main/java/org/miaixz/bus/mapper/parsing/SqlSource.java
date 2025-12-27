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

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.session.Configuration;

/**
 * A custom {@link org.apache.ibatis.mapping.SqlSource} implementation used to replace the original source with modified
 * SQL.
 *
 * <p>
 * This class is typically used in MyBatis interceptors (plugins). When SQL needs to be modified dynamically (e.g., for
 * pagination, multi-tenancy, or data permission filtering), the original {@link BoundSql} cannot be modified directly
 * as it might be immutable or shared.
 * </p>
 *
 * <p>
 * The core logic is to create a new container that holds the modified SQL string (`actualSql`) while reusing the
 * parameter mapping configuration (`ParameterMapping`) from the original `BoundSql` (`delegate`).
 * </p>
 *
 * <p>
 * <strong>Usage Example:</strong>
 * </p>
 *
 * <pre>{@code
 * // 1. Retrieve the original BoundSql
 * BoundSql originalBoundSql = ms.getBoundSql(parameter);
 *
 * // 2. Generate the modified SQL (e.g., appending LIMIT or WHERE clauses)
 * String modifiedSql = applyChanges(originalBoundSql.getSql());
 *
 * // 3. Create this custom SqlSource, passing the original BoundSql as the delegate
 * SqlSource modifiedSource = new SqlSource(ms.getConfiguration(), originalBoundSql, modifiedSql);
 *
 * // 4. Replace the sqlSource in MappedStatement using reflection
 * MetaObject msMetaObject = SystemMetaObject.forObject(ms);
 * msMetaObject.setValue("sqlSource", modifiedSource);
 * }</pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SqlSource implements org.apache.ibatis.mapping.SqlSource {

    /**
     * The global MyBatis configuration.
     */
    private final Configuration configuration;

    /**
     * The original BoundSql object (the delegate).
     * <p>
     * It is retained to provide the {@code List<ParameterMapping>}, which defines the relationship between parameters
     * and SQL placeholders (?). Even if the SQL text changes, the underlying parameter logic usually remains
     * compatible.
     * </p>
     */
    private final BoundSql delegate;

    /**
     * The actual SQL statement to be executed.
     * <p>
     * This is the rewritten SQL string processed by the interceptor.
     * </p>
     */
    private final String actualSql;

    /**
     * Constructs a SqlSource with the specified configuration, original BoundSql, and modified SQL.
     *
     * @param configuration the MyBatis configuration
     * @param delegate      the original BoundSql (preserved for parameter mappings)
     * @param actualSql     the modified SQL string (assigned to actualSql)
     */
    public SqlSource(Configuration configuration, BoundSql delegate, String actualSql) {
        this.configuration = configuration;
        this.delegate = delegate;
        this.actualSql = actualSql;
    }

    /**
     * Returns a new BoundSql with the modified SQL while preserving parameter mappings and additional parameters.
     *
     * <p>
     * This method is called during the SQL execution preparation phase. It combines the "new SQL" with the "old
     * parameter mappings" to generate a valid {@link BoundSql}.
     * </p>
     *
     * @param parameterObject the parameter object for the SQL execution (e.g., Map, POJO, or primitive)
     * @return a new BoundSql instance containing the modified SQL and original mappings
     */
    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        // Key logic:
        // 1. Use this.actualSql (the modified SQL)
        // 2. Reuse delegate.getParameterMappings() (the original parameter configuration)
        // 3. Use the current parameterObject (runtime value)
        BoundSql newBoundSql = new BoundSql(configuration, actualSql, delegate.getParameterMappings(), parameterObject);

        // Copy Additional Parameters
        // Reason: During dynamic SQL parsing (e.g., <if>, <foreach>, <bind>), MyBatis generates
        // temporary context variables stored in AdditionalParameters.
        // If these are not copied, the new SQL execution might fail with "Parameter 'xxx' not found" errors.
        for (String key : delegate.getAdditionalParameters().keySet()) {
            newBoundSql.setAdditionalParameter(key, delegate.getAdditionalParameter(key));
        }

        return newBoundSql;
    }

    /**
     * Gets the MyBatis configuration.
     *
     * @return the configuration object
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Gets the original BoundSql delegate.
     *
     * @return the original BoundSql
     */
    public BoundSql getDelegate() {
        return delegate;
    }

    /**
     * Gets the modified actual SQL string.
     *
     * @return the modified SQL string
     */
    public String getActualSql() {
        return actualSql;
    }

}
