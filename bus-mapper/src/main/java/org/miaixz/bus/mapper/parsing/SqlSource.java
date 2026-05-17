/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.mapper.parsing;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.sql.DataSource;

import lombok.Getter;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver;
import org.apache.ibatis.session.Configuration;

import org.miaixz.bus.mapper.dialect.Dialect;
import org.miaixz.bus.mapper.dialect.DialectRegistry;

/**
 * A custom {@link org.apache.ibatis.mapping.SqlSource} that wraps a modified SQL while ensuring parameter mappings
 * match the current SQL structure.
 *
 * <p>
 * <strong>Core Design:</strong>
 * </p>
 * <p>
 * This SqlSource saves the actual SQL (after interceptor processing) and delegates to the original SqlSource to get
 * correct parameter mappings. This ensures that:
 * </p>
 * <ul>
 * <li>Modified SQL (with table prefix, tenant conditions, etc.) is preserved</li>
 * <li>Parameter mappings are dynamically generated based on current parameters (no cache pollution)</li>
 * <li>Subsequent interceptors can process the modified SQL correctly</li>
 * <li>Correctness is prioritized over performance</li>
 * </ul>
 *
 * <p>
 * <strong>Dynamic SQL Generation:</strong>
 * </p>
 * <p>
 * For multi-datasource scenarios with different database dialects, this SqlSource supports dynamic SQL generation. If
 * the underlying SqlMetaCache is dynamic (has a Dialect function), SQL will be generated at runtime based on the
 * current datasource's dialect.
 * </p>
 *
 * <p>
 * <strong>Why Pure Delegation?</strong>
 * </p>
 * <p>
 * This implementation uses pure delegation - it always delegates to the original SqlSource to get ParameterMappings.
 * This approach avoids cache pollution issues where dynamic SQL (e.g., with foreach) generates different
 * ParameterMappings for different parameter instances. The trade-off is a slight performance cost for static SQL, but
 * correctness is guaranteed for all SQL types.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
public class SqlSource implements org.apache.ibatis.mapping.SqlSource {

    /**
     * The global MyBatis configuration.
     */
    private final Configuration configuration;

    /**
     * The actual SQL (after interceptor processing).
     * <p>
     * This SQL includes all modifications from previous interceptors (table prefix, tenant conditions, etc.). It is
     * "global" in the sense that it's saved and reused across invocations.
     * </p>
     */
    private final String actualSql;

    /**
     * The original SqlSource from the MappedStatement (before any interceptor modifications).
     * <p>
     * We delegate to this SqlSource to get parameter mappings and additional parameters. This ensures that dynamic SQL
     * (e.g., with foreach) generates correct ParameterMappings for each parameter instance.
     * </p>
     */
    private final org.apache.ibatis.mapping.SqlSource sqlSource;

    /**
     * The SQL metadata cache for dynamic SQL generation.
     * <p>
     * If this cache is dynamic, SQL will be generated at runtime based on the current dialect.
     * </p>
     */
    private final SqlMetaCache cache;

    /**
     * Mapper SQL cache key, normally the mapped statement id.
     */
    private final String cacheKey;

    /**
     * Cache of parsed dynamic MyBatis SQL sources.
     */
    private final ConcurrentMap<DynamicSqlSourceKey, org.apache.ibatis.mapping.SqlSource> dynamicSqlSourceCache = new ConcurrentHashMap<>();

    /**
     * Constructs a SqlSource with actual SQL and original SqlSource (static SQL).
     *
     * @param ms        the MappedStatement
     * @param actualSql the actual SQL (after interceptor processing)
     * @param sqlSource the original SqlSource before interceptor modifications
     */
    public SqlSource(MappedStatement ms, String actualSql, org.apache.ibatis.mapping.SqlSource sqlSource) {
        this(ms, actualSql, sqlSource, null);
    }

    /**
     * Constructs a SqlSource with actual SQL, original SqlSource, and SqlMetaCache (dynamic SQL support).
     *
     * @param ms        the MappedStatement
     * @param actualSql the actual SQL (after interceptor processing)
     * @param sqlSource the original SqlSource before interceptor modifications
     * @param cache     the SQL metadata cache for dynamic SQL generation (optional)
     */
    public SqlSource(MappedStatement ms, String actualSql, org.apache.ibatis.mapping.SqlSource sqlSource,
            SqlMetaCache cache) {
        this.configuration = ms.getConfiguration();
        this.actualSql = actualSql;
        this.sqlSource = sqlSource;
        this.cache = cache;
        this.cacheKey = ms.getId();
    }

    /**
     * Returns a BoundSql with the actual SQL and parameter mappings from the original SqlSource.
     * <p>
     * This method always delegates to the original SqlSource to get correct ParameterMappings. This pure delegation
     * approach ensures:
     * </p>
     * <ul>
     * <li>Dynamic SQL (with foreach, if conditions, etc.) always gets correct ParameterMappings</li>
     * <li>Static SQL gets ParameterMappings that match the current parameter instance</li>
     * <li>No cache pollution: different parameter instances get different ParameterMappings when needed</li>
     * </ul>
     * <p>
     * If the underlying SqlMetaCache is dynamic, SQL will be generated at runtime based on the current datasource's
     * dialect.
     * </p>
     *
     * @param parameterObject the parameter object for the SQL execution
     * @return a BoundSql instance with actual SQL and correct parameter mappings
     */
    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        // For dynamic SQL, we need to regenerate SqlSource at runtime based on dialect
        if (cache != null && cache.isDynamic()) {
            // Get dialect for current datasource and generate SQL dynamically
            Dialect dialect = resolveDialect();
            String dynamicSql = cache.getSqlScript(dialect);
            Class<?> parameterType = parameterObject == null ? Object.class : parameterObject.getClass();
            DynamicSqlSourceKey key = new DynamicSqlSourceKey(configuration, cacheKey,
                    dialect == null ? "Unknown" : dialect.getDatabase(), parameterType,
                    dynamicSql == null ? 0 : dynamicSql.hashCode());

            org.apache.ibatis.mapping.SqlSource dynamicSqlSource = dynamicSqlSourceCache
                    .computeIfAbsent(key, ignored -> {
                        XMLLanguageDriver xmlLanguageDriver = new XMLLanguageDriver();
                        return xmlLanguageDriver.createSqlSource(this.configuration, dynamicSql, parameterType);
                    });

            // Get BoundSql from the dynamically created SqlSource
            return dynamicSqlSource.getBoundSql(parameterObject);
        }

        // For static SQL, use the actual SQL with parameter mappings from original
        // Delegate to original SqlSource to get correct ParameterMappings
        BoundSql boundSql = this.sqlSource.getBoundSql(parameterObject);
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();

        // Create new BoundSql with actual SQL and parameter mappings from original
        BoundSql newBoundSql = new BoundSql(this.configuration, actualSql, parameterMappings, parameterObject);

        // Copy additional parameters if present
        boundSql.getAdditionalParameters().forEach(newBoundSql::setAdditionalParameter);

        return newBoundSql;
    }

    /**
     * Resolves the dialect for dynamic SQL generation.
     *
     * @return the dialect resolved from the datasource key or MyBatis environment datasource
     */
    private Dialect resolveDialect() {
        Dialect dialect = DialectRegistry.getDialect();
        if (isResolvedDialect(dialect)) {
            return dialect;
        }
        Environment environment = this.configuration.getEnvironment();
        if (environment == null) {
            return dialect;
        }
        DataSource dataSource = environment.getDataSource();
        if (dataSource == null) {
            return dialect;
        }
        return DialectRegistry.getDialect(dataSource);
    }

    /**
     * Tests whether the dialect represents a concrete database implementation.
     *
     * @param dialect the dialect to test
     * @return {@code true} when the dialect is concrete
     */
    private boolean isResolvedDialect(Dialect dialect) {
        return dialect != null && !"Unknown".equalsIgnoreCase(dialect.getDatabase());
    }

    /**
     * Cache key for parsed dynamic MyBatis SQL sources.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    private static final class DynamicSqlSourceKey {

        /**
         * MyBatis configuration compared by identity.
         */
        private final Configuration configuration;

        /**
         * Mapper cache key.
         */
        private final String cacheKey;

        /**
         * Database dialect name.
         */
        private final String dialectName;

        /**
         * Runtime parameter type used to parse the dynamic SQL source.
         */
        private final Class<?> parameterType;

        /**
         * Hash of the dynamic SQL script.
         */
        private final int sqlHash;

        /**
         * Creates a dynamic SQL source cache key.
         *
         * @param configuration the MyBatis configuration
         * @param cacheKey      the mapper cache key
         * @param dialectName   the database dialect name
         * @param parameterType the runtime parameter type
         * @param sqlHash       the SQL script hash
         */
        private DynamicSqlSourceKey(Configuration configuration, String cacheKey, String dialectName,
                Class<?> parameterType, int sqlHash) {
            this.configuration = configuration;
            this.cacheKey = cacheKey;
            this.dialectName = dialectName;
            this.parameterType = parameterType;
            this.sqlHash = sqlHash;
        }

        /**
         * Tests equality using configuration identity and value fields.
         *
         * @param object the object to compare
         * @return {@code true} when both keys identify the same parsed SQL source
         */
        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (!(object instanceof DynamicSqlSourceKey that)) {
                return false;
            }
            return configuration == that.configuration && sqlHash == that.sqlHash
                    && Objects.equals(cacheKey, that.cacheKey) && Objects.equals(dialectName, that.dialectName)
                    && Objects.equals(parameterType, that.parameterType);
        }

        /**
         * Returns a hash code based on configuration identity and value fields.
         *
         * @return the hash code
         */
        @Override
        public int hashCode() {
            return Objects.hash(System.identityHashCode(configuration), cacheKey, dialectName, parameterType, sqlHash);
        }

    }

}
