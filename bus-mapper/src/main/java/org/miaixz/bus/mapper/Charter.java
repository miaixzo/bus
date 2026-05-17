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
package org.miaixz.bus.mapper;

import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Mapper charter definitions shared by behavior contracts, schema initialization, and dialect SQL generation.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class Charter {

    /**
     * Creates no instances.
     */
    private Charter() {

    }

    /**
     * Operation type group.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum Group {

        /**
         * UPSERT SQL generation behavior.
         */
        UPSERT,

        /**
         * Schema DDL behavior.
         */
        SCHEMA,

        /**
         * Database metadata read behavior.
         */
        METADATA

    }

    /**
     * Operation types recognized by mapper database behaviors.
     *
     * <p>
     * Each constant represents a stable capability shape that dialects can advertise and providers can switch on when
     * building SQL for a specific database family.
     * </p>
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    @Getter
    @Accessors(fluent = true)
    public enum Behavior {

        /**
         * UPSERT is not supported.
         */
        NONE(Group.UPSERT),

        /**
         * MySQL style: INSERT ... ON DUPLICATE KEY UPDATE.
         */
        INSERT_ON_DUPLICATE(Group.UPSERT),

        /**
         * PostgreSQL style: INSERT ... ON CONFLICT (...) DO UPDATE.
         */
        INSERT_ON_CONFLICT(Group.UPSERT),

        /**
         * SQLite style: INSERT OR REPLACE.
         */
        INSERT_OR_REPLACE(Group.UPSERT),

        /**
         * Firebird style: UPDATE OR INSERT ... MATCHING (...).
         */
        UPDATE_OR_INSERT(Group.UPSERT),

        /**
         * MERGE using VALUES source rows.
         */
        MERGE_USING_VALUES(Group.UPSERT),

        /**
         * MERGE INTO table KEY (...) VALUES (...) style.
         */
        MERGE_INTO_KEY(Group.UPSERT),

        /**
         * MERGE using DUAL / source select.
         */
        MERGE_USING_DUAL(Group.UPSERT),

        /**
         * Create table operation.
         */
        CREATE_TABLE(Group.SCHEMA),

        /**
         * Add column operation.
         */
        ADD_COLUMN(Group.SCHEMA),

        /**
         * Modify column SQL type operation.
         */
        MODIFY_COLUMN_TYPE(Group.SCHEMA),

        /**
         * Modify character length operation.
         */
        MODIFY_COLUMN_LENGTH(Group.SCHEMA),

        /**
         * Modify numeric precision or scale operation.
         */
        MODIFY_COLUMN_DECIMAL(Group.SCHEMA),

        /**
         * Modify column nullable operation.
         */
        MODIFY_COLUMN_NULLABLE(Group.SCHEMA),

        /**
         * Rename column operation.
         */
        RENAME_COLUMN(Group.SCHEMA),

        /**
         * Drop column operation.
         */
        DROP_COLUMN(Group.SCHEMA),

        /**
         * Create index operation.
         */
        CREATE_INDEX(Group.SCHEMA),

        /**
         * Drop index operation.
         */
        DROP_INDEX(Group.SCHEMA),

        /**
         * Create unique constraint operation.
         */
        CREATE_UNIQUE(Group.SCHEMA),

        /**
         * Drop unique constraint operation.
         */
        DROP_UNIQUE(Group.SCHEMA),

        /**
         * Read table metadata operation.
         */
        READ_TABLE_METADATA(Group.METADATA),

        /**
         * Read column metadata operation.
         */
        READ_COLUMN_METADATA(Group.METADATA),

        /**
         * Read index metadata operation.
         */
        READ_INDEX_METADATA(Group.METADATA);

        /**
         * Operation group for the behavior type.
         */
        private final Group group;

        /**
         * Creates a behavior type.
         *
         * @param group the operation group
         */
        Behavior(Group group) {
            this.group = group;
        }

    }

    /**
     * Entity schema initialization mode.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum Schema {

        /**
         * Disables metadata reads, SQL generation, and DDL execution.
         */
        NONE,

        /**
         * Generates SQL script output without executing DDL.
         */
        SCRIPT,

        /**
         * Creates missing tables only.
         */
        CREATE,

        /**
         * Reads metadata and reports differences without executing DDL.
         */
        VALIDATE,

        /**
         * Executes explicitly allowed schema differences.
         */
        UPDATE

    }

    /**
     * Common column modification shapes used by concrete dialects.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum Modify {

        /**
         * Uses {@code ALTER COLUMN <definition>} syntax.
         */
        ALTER_COLUMN,

        /**
         * Uses {@code MODIFY <definition>} syntax.
         */
        MODIFY,

        /**
         * Uses {@code MODIFY (<definition>)} syntax.
         */
        MODIFY_PARENTHESES,

        /**
         * Uses {@code ALTER COLUMN <name> TYPE <type>} syntax.
         */
        ALTER_COLUMN_TYPE,

        /**
         * Uses {@code ALTER COLUMN <name> SET DATA TYPE <type>} syntax.
         */
        SET_DATA_TYPE

    }

    /**
     * Risk level for a schema change.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum Risk {

        /**
         * Safe schema difference.
         */
        SAFE,

        /**
         * Cautionary schema difference.
         */
        CAUTION,

        /**
         * Dangerous schema difference.
         */
        DANGEROUS

    }

    /**
     * Handler operation type used by mapper handler indexing.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum Handler {

        /**
         * Query operation for {@code isQuery} and {@code query} methods.
         */
        QUERY,

        /**
         * Update operation for {@code isUpdate} and {@code update} methods.
         */
        UPDATE,

        /**
         * Statement preparation operation.
         */
        PREPARE,

        /**
         * Bound SQL retrieval operation.
         */
        GET_BOUND_SQL

    }

    /**
     * Multi-tenancy data isolation strategy.
     *
     * <p>
     * Defines common ways to isolate tenant data across datasources, schemas, or tenant columns.
     * </p>
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    @Getter
    public enum Isolation {

        /**
         * Isolates tenants with independent databases or datasources.
         */
        DATASOURCE("Independent Database", "Each tenant uses an independent database"),

        /**
         * Isolates tenants with independent database schemas.
         */
        SCHEMA("Independent Schema", "Multiple tenants share database but use independent schemas"),

        /**
         * Isolates tenants with a tenant discriminator column in shared tables.
         */
        COLUMN("Shared Table", "All tenants share tables, distinguished by tenant ID column");

        /**
         * Isolation strategy display name.
         */
        private final String name;

        /**
         * Isolation strategy description.
         */
        private final String description;

        /**
         * Creates a tenant isolation strategy.
         *
         * @param name        the display name
         * @param description the description
         */
        Isolation(String name, String description) {
            this.name = name;
            this.description = description;
        }

        /**
         * Tests whether SQL rewriting is needed.
         *
         * <p>
         * Datasource isolation switches the datasource before SQL execution, so it does not need SQL rewriting.
         * </p>
         *
         * @return {@code true} when SQL rewriting is needed
         */
        public boolean needRewriteSql() {
            return this != DATASOURCE;
        }

        /**
         * Tests whether a tenant ID column is needed.
         *
         * @return {@code true} when the isolation strategy uses a tenant column
         */
        public boolean needTenantColumn() {
            return this == COLUMN;
        }

    }

}
