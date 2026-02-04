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
package org.miaixz.bus.shade.screw;

import java.io.Serial;
import java.io.Serializable;

import javax.sql.DataSource;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.shade.screw.engine.EngineConfig;
import org.miaixz.bus.shade.screw.process.ProcessConfig;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Main configuration class for the documentation generation process.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
public class Config implements Serializable {

    @Serial
    private static final long serialVersionUID = -1L;

    /**
     * The name of the organization.
     */
    private String organization;
    /**
     * The URL of the organization.
     */
    private String organizationUrl;
    /**
     * The title of the generated document.
     */
    private String title;
    /**
     * The version number of the document.
     */
    private String version;
    /**
     * A description for the document.
     */
    private String description;
    /**
     * The data source for connecting to the database. Using the {@link DataSource} interface allows for flexibility
     * with any data source implementation.
     */
    private DataSource dataSource;
    /**
     * Configuration for data processing.
     */
    private ProcessConfig produceConfig;
    /**
     * Configuration for the documentation generation engine.
     */
    private EngineConfig engineConfig;

    /**
     * Constructs a new {@code Config} instance.
     *
     * @param organization    The name of the organization.
     * @param organizationUrl The URL of the organization.
     * @param title           The title of the document.
     * @param version         The version of the document.
     * @param description     A description for the document.
     * @param dataSource      The JDBC data source. Must not be null.
     * @param produceConfig   The configuration for data processing.
     * @param engineConfig    The configuration for the documentation engine. Must not be null.
     */
    private Config(String organization, String organizationUrl, String title, String version, String description,
            DataSource dataSource, ProcessConfig produceConfig, EngineConfig engineConfig) {
        Assert.notNull(dataSource, "DataSource can not be empty!");
        Assert.notNull(engineConfig, "EngineConfig can not be empty!");
        this.title = title;
        this.organizationUrl = organizationUrl;
        this.organization = organization;
        this.version = version;
        this.description = description;
        this.dataSource = dataSource;
        this.engineConfig = engineConfig;
        this.produceConfig = produceConfig;
    }

}
