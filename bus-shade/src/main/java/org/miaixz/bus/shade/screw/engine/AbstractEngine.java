/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
 * @since Java 17+
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
