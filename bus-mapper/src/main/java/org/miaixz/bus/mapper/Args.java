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

import java.util.regex.Pattern;

/**
 * This class defines constants for MyBatis mapper configuration and SQL fragments.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Args {

    /**
     * Constructs a new Args instance.
     */
    public Args() {
        // No initialization required.
    }

    /**
     * Regular expression to remove potential delimiters (like backticks or brackets) from field names.
     */
    public static final Pattern DELIMITER = Pattern.compile("^[`\\[\"]?(.*?)[`\\]\"]?$");

    /**
     * Shared (global) configuration prefix for cross-datasource settings.
     */
    public static final String SHARED_KEY = "shared";

    /**
     * Configuration key for table-related settings (prefix, ignore).
     */
    public static final String TABLE_KEY = "table";

    /**
     * Configuration key for tenant (multi-tenancy) settings.
     */
    public static final String TENANT_KEY = "tenant";

    /**
     * Configuration key for populate (auto-fill) functionality.
     */
    public static final String POPULATE_KEY = "populate";

    /**
     * Configuration key for visible (data perimeter) functionality.
     */
    public static final String VISIBLE_KEY = "visible";

    /**
     * Configuration key for audit functionality.
     */
    public static final String AUDIT_KEY = "audit";

    /**
     * Common property name for ignore tables list (used by multiple plugins).
     */
    public static final String PROP_IGNORE = "ignore";

    /**
     * Tenant property key: column.
     */
    public static final String TENANT_COLUMN = "column";

    /**
     * Default column name for tenant ID.
     */
    public static final String TENANT_ID = "tenant_id";

    /**
     * Table property: prefix value.
     */
    public static final String TABLE_PREFIX = "prefix";

    /**
     * Populate property: enable created time field.
     */
    public static final String POPULATE_CREATED = "created";

    /**
     * Populate property: enable modified time field.
     */
    public static final String POPULATE_MODIFIED = "modified";

    /**
     * Populate property: enable creator field.
     */
    public static final String POPULATE_CREATOR = "creator";

    /**
     * Populate property: enable modifier field.
     */
    public static final String POPULATE_MODIFIER = "modifier";

    /**
     * Audit property: slow SQL threshold in milliseconds.
     */
    public static final String AUDIT_SLOW_SQL_THRESHOLD = "slowSqlThreshold";

    /**
     * Audit property: whether to log SQL parameters.
     */
    public static final String AUDIT_LOG_PARAMETERS = "logParameters";

    /**
     * Audit property: whether to log SQL results.
     */
    public static final String AUDIT_LOG_RESULTS = "logResults";

    /**
     * Audit property: whether to log all SQL.
     */
    public static final String AUDIT_LOG_ALL_SQL = "logAllSql";

    /**
     * Audit property: whether to print to console.
     */
    public static final String AUDIT_PRINT_CONSOLE = "printConsole";

    /**
     * Page property: auto-delimit keywords.
     */
    public static final String PAGE_AUTO_DELIMIT_KEYWORDS = "autoDelimitKeywords";

    /**
     * Page property: reasonable pagination.
     */
    public static final String PAGE_REASONABLE = "reasonable";

    /**
     * Page property: support method arguments.
     */
    public static final String PAGE_SUPPORT_METHOD_ARGUMENTS = "supportMethodsArguments";

    /**
     * Page property: params configuration.
     */
    public static final String PAGE_PARAMS = "params";

    /**
     * Provider key: used to pass provider object in Properties. The actual key format is: "_provider" (underscore
     * prefix to avoid conflicts).
     */
    public static final String PROVIDER_KEY = "provider";

    /**
     * Configuration key for the naming convention.
     */
    public static final String NAMING_KEY = "naming";

    /**
     * Configuration key for enabling/disabling one-time caching.
     */
    public static final String USEONCE_KEY = "useOnce";

    /**
     * Configuration key for the initial size of the cache.
     */
    public static final String INITSIZE_KEY = "initSize";

    /**
     * Configuration key for concurrency level of primary key generation.
     */
    public static final String CONCURRENCY_KEY = "concurrency";

    /**
     * Default name for the base result map.
     */
    public static final String RESULT_MAP_NAME = "SuperResultMap";

}
