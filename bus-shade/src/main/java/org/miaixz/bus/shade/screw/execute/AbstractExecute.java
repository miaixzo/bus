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
