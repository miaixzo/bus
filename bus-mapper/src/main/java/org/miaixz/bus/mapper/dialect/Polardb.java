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

import java.util.EnumSet;

import org.miaixz.bus.mapper.Charter.Behavior;
import org.miaixz.bus.mapper.parsing.ColumnMeta;
import org.miaixz.bus.mapper.parsing.IndexMeta;
import org.miaixz.bus.mapper.parsing.TableMeta;
import org.miaixz.bus.mapper.support.paging.Pageable;
import org.miaixz.bus.mapper.support.schema.ColumnSnapshot;
import org.miaixz.bus.mapper.support.schema.SqlTypeDescriptor;

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
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    private enum Engine {

        /**
         * Engine family has not been resolved.
         */
        UNKNOWN,

        /**
         * MySQL-compatible Polardb engine.
         */
        MYSQL,

        /**
         * PostgreSQL-compatible Polardb engine.
         */
        POSTGRESQL

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
     * @return {@link Behavior#INSERT_ON_DUPLICATE} for MySQL-compatible Polardb, or {@link Behavior#INSERT_ON_CONFLICT}
     *         for PostgreSQL-compatible Polardb
     */
    @Override
    public Behavior getUpsertType() {
        return switch (engine) {
            case MYSQL -> Behavior.INSERT_ON_DUPLICATE;
            case POSTGRESQL -> Behavior.INSERT_ON_CONFLICT;
            case UNKNOWN -> throw new IllegalStateException("Polardb template instance must be resolved before use");
        };
    }

    /**
     * Returns the database behavior set advertised by this dialect.
     *
     * @return the supported behavior set
     */
    @Override
    public EnumSet<Behavior> types() {
        return engine == Engine.UNKNOWN ? EnumSet.noneOf(Behavior.class) : schemaTypes(getUpsertType());
    }

    /**
     * Resolves the SQL type descriptor used by this dialect for the supplied mapper column.
     *
     * @param column the mapper column metadata
     * @return the SQL type descriptor for the column
     */
    @Override
    public SqlTypeDescriptor resolveType(ColumnMeta column) {
        return switch (engine) {
            case MYSQL -> mysqlType(column);
            case POSTGRESQL -> postgresqlType(column);
            case UNKNOWN -> throw unresolved();
        };
    }

    /**
     * Builds the DDL used to change the SQL type of a column.
     *
     * @param table        the mapper table metadata
     * @param column       the mapper column metadata
     * @param actualColumn the column metadata currently present in the database
     * @return the generated type-change SQL
     */
    @Override
    public String modifyColumnType(TableMeta table, ColumnMeta column, ColumnSnapshot actualColumn) {
        return switch (engine) {
            case MYSQL -> mysqlModifyColumn(table, column);
            case POSTGRESQL -> postgresqlModifyColumnType(table, column);
            case UNKNOWN -> throw unresolved();
        };
    }

    /**
     * Builds the DDL used to change the character length of a column.
     *
     * @param table        the mapper table metadata
     * @param column       the mapper column metadata
     * @param actualColumn the column metadata currently present in the database
     * @return the generated length-change SQL
     */
    @Override
    public String modifyColumnLength(TableMeta table, ColumnMeta column, ColumnSnapshot actualColumn) {
        return modifyColumnType(table, column, actualColumn);
    }

    /**
     * Builds the DDL used to change the numeric precision or scale of a column.
     *
     * @param table        the mapper table metadata
     * @param column       the mapper column metadata
     * @param actualColumn the column metadata currently present in the database
     * @return the generated decimal-change SQL
     */
    @Override
    public String modifyColumnDecimal(TableMeta table, ColumnMeta column, ColumnSnapshot actualColumn) {
        return modifyColumnType(table, column, actualColumn);
    }

    /**
     * Builds the DDL used to change the nullable flag for a column.
     *
     * @param table        the mapper table metadata
     * @param column       the mapper column metadata
     * @param actualColumn the column metadata currently present in the database
     * @return the generated nullable-change SQL
     */
    @Override
    public String modifyColumnNullable(TableMeta table, ColumnMeta column, ColumnSnapshot actualColumn) {
        return switch (engine) {
            case MYSQL -> mysqlModifyColumn(table, column);
            case POSTGRESQL -> postgresqlModifyColumnNullable(table, column);
            case UNKNOWN -> throw unresolved();
        };
    }

    /**
     * Builds the DDL used to drop an index from a table.
     *
     * @param table the mapper table metadata
     * @param index the mapper index metadata
     * @return the generated drop-index SQL
     */
    @Override
    public String dropIndex(TableMeta table, IndexMeta index) {
        return switch (engine) {
            case MYSQL -> mysqlDropIndex(table, index);
            case POSTGRESQL -> super.dropIndex(table, index);
            case UNKNOWN -> throw unresolved();
        };
    }

    /**
     * Builds the dialect-specific DDL used to replace or modify a column definition.
     *
     * @param table  the mapper table metadata
     * @param column the mapper column metadata
     * @return the generated column modification SQL
     */
    @Override
    protected String modifyColumn(TableMeta table, ColumnMeta column) {
        return switch (engine) {
            case MYSQL -> mysqlModifyColumn(table, column);
            case POSTGRESQL -> postgresqlModifyColumnType(table, column);
            case UNKNOWN -> throw unresolved();
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

    /**
     * Creates the exception used when the template instance is used before URL resolution.
     *
     * @return the unresolved template exception
     */
    private IllegalStateException unresolved() {
        return new IllegalStateException("Polardb template instance must be resolved before use");
    }

}
