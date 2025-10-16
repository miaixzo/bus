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
package org.miaixz.bus.shade.safety.boot;

import org.miaixz.bus.shade.safety.Launcher;
import org.springframework.boot.loader.launch.PropertiesLauncher;

/**
 * A custom {@link PropertiesLauncher} for Spring Boot applications that integrates with the {@link Launcher} to provide
 * enhanced security features such as JAR encryption and decryption. This class acts as the entry point for launching
 * encrypted Spring Boot applications configured via properties.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BootPropertiesLauncher extends PropertiesLauncher {

    /**
     * The internal {@link Launcher} instance responsible for handling encryption/decryption setup.
     */
    private final Launcher launcher;

    /**
     * Constructs a new {@code BootPropertiesLauncher} and initializes the underlying {@link Launcher}.
     *
     * @param args Command-line arguments passed to the application.
     * @throws Exception If an error occurs during the initialization of the {@link Launcher}.
     */
    public BootPropertiesLauncher(String... args) throws Exception {
        this.launcher = new Launcher(args);
    }

    /**
     * The main entry point for launching the Spring Boot application with security features.
     *
     * @param args Command-line arguments.
     * @throws Exception If an error occurs during application launch.
     */
    public static void main(String[] args) throws Exception {
        new BootPropertiesLauncher(args).launch();
    }

    /**
     * Launches the Spring Boot application using the arguments processed by the internal {@link Launcher}.
     *
     * @throws Exception If an error occurs during the launch process.
     */
    public void launch() throws Exception {
        launch(launcher.args);
    }

}
