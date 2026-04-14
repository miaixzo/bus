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
package org.miaixz.bus.mapper.dialect;

import org.miaixz.bus.mapper.support.paging.Pageable;

/**
 * Dialect resolver and final dialect implementation for Polardb product-family endpoints.
 *
 * <p>
 * The registry stores a template instance created through the public no-argument constructor. When a JDBC URL is
 * resolved, this dialect inspects the endpoint and returns a new final instance bound to a concrete internal engine
 * family. The resolved instance then behaves as a normal dialect for downstream pagination and UPSERT SQL generation.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class Polardb extends AbstractDialect {

    /**
     * Internal engine family resolved from the JDBC URL.
     */
    private final Engine engine;

    /**
     * Internal Polardb engine families currently supported by the framework.
     */
    private enum Engine {
        UNKNOWN, MYSQL, POSTGRESQL
    }

    /**
     * Creates the registry template instance used to resolve Polardb URLs.
     */
    public Polardb() {
        super("Polardb", "jdbc:");
        this.engine = Engine.UNKNOWN;
    }

    /**
     * Creates a Polardb dialect for the specified internal engine family.
     *
     * @param engine the resolved Polardb engine family
     */
    private Polardb(Engine engine) {
        super(engine == Engine.MYSQL ? "Polardb MySQL" : "Polardb PostgreSQL",
                engine == Engine.MYSQL ? "jdbc:mysql:" : "jdbc:postgresql:");
        this.engine = engine;
    }

    /**
     * Resolves the supplied JDBC URL to a final Polardb dialect instance.
     *
     * @param jdbcUrl the JDBC URL to inspect
     * @return the resolved Polardb dialect, or {@code null} when the URL does not belong to Polardb
     */
    @Override
    public Dialect resolve(String jdbcUrl) {
        if (jdbcUrl == null || jdbcUrl.isEmpty()) {
            return null;
        }
        String lower = jdbcUrl.toLowerCase();
        if (!lower.contains(".polardb.")) {
            return null;
        }
        if (lower.startsWith("jdbc:mysql:")) {
            return new Polardb(Engine.MYSQL);
        }
        if (lower.startsWith("jdbc:postgresql:")) {
            return new Polardb(Engine.POSTGRESQL);
        }
        return null;
    }

    /**
     * Returns the UPSERT type used by the resolved internal engine family.
     *
     * @return {@link Dialect.Type#INSERT_ON_DUPLICATE} for MySQL-compatible Polardb, or
     *         {@link Dialect.Type#INSERT_ON_CONFLICT} for PostgreSQL-compatible Polardb
     */
    @Override
    public Dialect.Type getUpsertType() {
        return switch (engine) {
            case MYSQL -> Dialect.Type.INSERT_ON_DUPLICATE;
            case POSTGRESQL -> Dialect.Type.INSERT_ON_CONFLICT;
            case UNKNOWN -> throw new IllegalStateException("Polardb template instance must be resolved before use");
        };
    }

    /**
     * Builds paginated SQL using the syntax of the resolved internal engine family.
     *
     * @param originalSql the original SQL statement
     * @param pageable    the requested pagination information
     * @return the paginated SQL statement
     */
    @Override
    public String buildPaginationSql(String originalSql, Pageable pageable) {
        if (engine == Engine.UNKNOWN) {
            throw new IllegalStateException("Polardb template instance must be resolved from a JDBC URL before use");
        }
        return buildPaginatedSql(originalSql, pageable);
    }

}
