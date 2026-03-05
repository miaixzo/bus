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
package org.miaixz.bus.shade.screw.execute;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.shade.screw.Builder;
import org.miaixz.bus.shade.screw.Config;

/**
 * Abstract base class for execution tasks. Provides common functionality, such as configuration handling and document
 * name generation.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractExecute implements Execute {

    /**
     * The main configuration object for the execution task.
     */
    protected Config config;

    /**
     * Constructs an {@code AbstractExecute} with the given configuration.
     *
     * @param config The {@link Config} object. Must not be null.
     */
    public AbstractExecute(Config config) {
        Assert.notNull(config, "Configuration can not be empty!");
        this.config = config;
    }

    /**
     * Generates the document name based on the configuration. If a custom file name is provided in the engine
     * configuration, it is used. Otherwise, the name is constructed from the database name, description, and version.
     *
     * @param database The name of the database.
     * @return The generated document name.
     */
    String getDocName(String database) {
        // Use custom file name if provided.
        if (StringKit.isNotBlank(config.getEngineConfig().getFileName())) {
            return config.getEngineConfig().getFileName();
        }
        // Use description from config, or default if blank.
        String description = config.getDescription();
        if (StringKit.isBlank(description)) {
            description = Builder.DESCRIPTION;
        }
        // Use version from config.
        String version = config.getVersion();
        if (StringKit.isBlank(version)) {
            return database + Symbol.UNDERLINE + description;
        }
        return database + Symbol.UNDERLINE + description + Symbol.UNDERLINE + version;
    }

}
