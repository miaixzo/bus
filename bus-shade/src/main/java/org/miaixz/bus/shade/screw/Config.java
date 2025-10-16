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
package org.miaixz.bus.shade.screw;

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
