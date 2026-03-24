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
package org.miaixz.bus.shade.screw.engine;

import java.io.File;
import java.io.IOException;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.shade.screw.Builder;

import lombok.Getter;
import lombok.Setter;

/**
 * Abstract base class for template engines. Provides common functionality such as handling engine configuration and
 * managing output files.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
public abstract class AbstractEngine implements TemplateEngine {

    /**
     * The configuration for the template engine.
     */
    private EngineConfig engineConfig;

    /**
     * Private default constructor to prevent direct instantiation without configuration.
     */
    private AbstractEngine() {
    }

    /**
     * Constructs an {@code AbstractEngine} with the given engine configuration.
     *
     * @param engineConfig The configuration for the template engine. Must not be null.
     */
    public AbstractEngine(EngineConfig engineConfig) {
        Assert.notNull(engineConfig, "EngineConfig can not be empty!");
        this.engineConfig = engineConfig;
    }

    /**
     * Creates and returns a {@link File} object for the output document. The filename is constructed as a combination
     * of the document name, version, and file type suffix. If no output directory is specified, it defaults to a "doc"
     * folder in the current project directory.
     *
     * @param docName The base name for the document.
     * @return A {@link File} object representing the output file.
     */
    protected File getFile(String docName) {
        File file;
        // If the output directory is not specified, default to the 'doc' directory in the current project path.
        if (StringKit.isBlank(getEngineConfig().getFileOutputDir())) {
            String dir = System.getProperty("user.dir");
            file = new File(dir + "/doc");
        } else {
            file = new File(getEngineConfig().getFileOutputDir());
        }
        // Create the directory if it does not exist.
        if (!file.exists()) {
            file.mkdirs();
        }
        // Get the file suffix from the configuration.
        String suffix = getEngineConfig().getFileType().getFileSuffix();
        file = new File(file, docName + suffix);
        // Update the output directory path in the configuration.
        getEngineConfig().setFileOutputDir(file.getParent());
        return file;
    }

    /**
     * Opens the output directory in the system's default file explorer if configured to do so. This method supports
     * both Mac and Windows operating systems.
     */
    protected void openOutputDir() {
        // Open the output directory if the option is enabled.
        if (getEngineConfig().isOpenOutputDir() && StringKit.isNotBlank(getEngineConfig().getFileOutputDir())) {
            try {
                // Get the operating system name.
                String osName = System.getProperty("os.name");
                if (null != osName) {
                    if (osName.contains(Builder.MAC)) {
                        Runtime.getRuntime().exec("open " + getEngineConfig().getFileOutputDir());
                    } else if (osName.contains(Builder.WINDOWS)) {
                        Runtime.getRuntime().exec("cmd /c start " + getEngineConfig().getFileOutputDir());
                    }
                }
            } catch (IOException e) {
                throw new InternalException(e);
            }
        }
    }

}
