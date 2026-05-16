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
package org.miaixz.bus.mapper.feature.schema;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.mapper.Charter.Schema;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Entity schema initialization configuration.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
@Accessors(fluent = true)
public class SchemaConfig {

    /**
     * Whether entity schema initialization is enabled.
     */
    private boolean enabled = false;

    /**
     * Entity schema initialization mode.
     */
    private Schema mode = Schema.NONE;

    /**
     * Whether generated SQL should avoid execution.
     */
    private boolean dryRun = true;

    /**
     * Whether generated SQL should be logged.
     */
    private boolean printSql = true;

    /**
     * Whether execution should stop after the first failure.
     */
    private boolean failFast = true;

    /**
     * Whether execution should continue after a DDL failure.
     */
    private boolean continueOnError = false;

    /**
     * Table names explicitly included in schema initialization.
     */
    private Set<String> includeTables = new HashSet<>();

    /**
     * Table names excluded from schema initialization.
     */
    private Set<String> excludeTables = new HashSet<>();

    /**
     * Entity class names explicitly included in schema initialization.
     */
    private Set<String> includeEntities = new HashSet<>();

    /**
     * Entity class names excluded from schema initialization.
     */
    private Set<String> excludeEntities = new HashSet<>();

    /**
     * Whether missing tables may be created.
     */
    private boolean allowCreateTable = false;

    /**
     * Whether missing columns may be added.
     */
    private boolean allowAddColumn = false;

    /**
     * Whether existing column SQL types may be changed.
     */
    private boolean allowModifyType = false;

    /**
     * Whether character column lengths may be expanded.
     */
    private boolean allowExpandLength = false;

    /**
     * Whether character column lengths may be shrunk.
     */
    private boolean allowShrinkLength = false;

    /**
     * Whether numeric precision or scale may be expanded.
     */
    private boolean allowExpandDecimal = false;

    /**
     * Whether numeric precision or scale may be shrunk.
     */
    private boolean allowShrinkDecimal = false;

    /**
     * Whether column nullable constraints may be changed.
     */
    private boolean allowModifyNullable = false;

    /**
     * Whether unmapped database columns may be dropped.
     */
    private boolean allowDropColumn = false;

    /**
     * Whether configured column renames may be executed.
     */
    private boolean allowRenameColumn = false;

    /**
     * Whether missing indexes may be created.
     */
    private boolean allowCreateIndex = false;

    /**
     * Whether unmapped database indexes may be dropped.
     */
    private boolean allowDropIndex = false;

    /**
     * Whether missing unique constraints may be created.
     */
    private boolean allowCreateUnique = false;

    /**
     * Whether unmapped database unique constraints may be dropped.
     */
    private boolean allowDropUnique = false;

    /**
     * Whether dangerous schema changes may be executed.
     */
    private boolean allowDangerous = false;

    /**
     * Whitelist entries that allow dangerous schema changes.
     */
    private Set<String> dangerousWhitelist = new HashSet<>();

    /**
     * Configured column rename mappings.
     */
    private Map<String, String> renameMappings = new HashMap<>();

    /**
     * Optional output path for generated schema SQL.
     */
    private String scriptLocation = Normal.EMPTY;

    /**
     * Optional datasource key used by starter integration.
     */
    private String datasourceKey = Normal.EMPTY;

}
