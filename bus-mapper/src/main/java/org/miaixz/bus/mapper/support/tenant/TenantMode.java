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
package org.miaixz.bus.mapper.support.tenant;

/**
 * Multi-tenancy mode enumeration.
 *
 * <p>
 * Defines three common multi-tenancy implementation modes.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum TenantMode {

    /**
     * Independent database mode.
     *
     * <p>
     * Each tenant uses an independent database (DataSource).
     * </p>
     *
     * <ul>
     * <li>Advantages: Highest data isolation, best security</li>
     * <li>Disadvantages: High resource consumption, high maintenance cost</li>
     * <li>Use cases: Scenarios requiring extremely high data isolation</li>
     * </ul>
     */
    DATASOURCE("Independent Database", "Each tenant uses an independent database"),

    /**
     * Independent schema mode.
     *
     * <p>
     * Multiple tenants share the same database but use independent schemas.
     * </p>
     *
     * <ul>
     * <li>Advantages: High data isolation, moderate resource utilization</li>
     * <li>Disadvantages: Requires database support for schemas, medium maintenance complexity</li>
     * <li>Use cases: Scenarios with certain data isolation requirements but aiming to save resources</li>
     * </ul>
     */
    SCHEMA("Independent Schema", "Multiple tenants share database but use independent schemas"),

    /**
     * Shared table mode (tenant column).
     *
     * <p>
     * All tenants share databases and tables, distinguished by tenant ID column.
     * </p>
     *
     * <ul>
     * <li>Advantages: Highest resource utilization, lowest maintenance cost</li>
     * <li>Disadvantages: Lowest data isolation, requires strict access control</li>
     * <li>Use cases: Scenarios with many tenants and high resource utilization requirements</li>
     * </ul>
     */
    COLUMN("Shared Table", "All tenants share tables, distinguished by tenant ID column");

    /**
     * Mode name.
     */
    private final String name;

    /**
     * Mode description.
     */
    private final String description;

    /**
     * Constructor.
     *
     * @param name        the mode name
     * @param description the mode description
     */
    TenantMode(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /**
     * Get the mode name.
     *
     * @return the mode name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the mode description.
     *
     * @return the mode description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Check if SQL rewriting is needed.
     *
     * <p>
     * DATASOURCE mode does not need SQL rewriting, other modes do.
     * </p>
     *
     * @return true if rewriting is needed, false otherwise
     */
    public boolean needRewriteSql() {
        return this != DATASOURCE;
    }

    /**
     * Check if a tenant ID column is needed.
     *
     * <p>
     * Only COLUMN mode requires a tenant ID column.
     * </p>
     *
     * @return true if needed, false otherwise
     */
    public boolean needTenantColumn() {
        return this == COLUMN;
    }

}
