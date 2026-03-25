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
package org.miaixz.bus.shade.safety.boot;

import org.miaixz.bus.shade.safety.Launcher;
import org.springframework.boot.loader.launch.PropertiesLauncher;

/**
 * A custom {@link PropertiesLauncher} for Spring Boot applications that integrates with the {@link Launcher} to provide
 * enhanced security features such as JAR encryption and decryption. This class acts as the entry point for launching
 * encrypted Spring Boot applications configured via properties.
 *
 * @author Kimi Liu
 * @since Java 21+
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
