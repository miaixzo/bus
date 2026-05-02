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
package org.miaixz.bus.shade.safety.boot.jar;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.shade.safety.Builder;
import org.miaixz.bus.shade.safety.Launcher;

/**
 * A custom launcher for standard JAR files that integrates with the {@link Launcher} to provide enhanced security
 * features such as JAR encryption and decryption. This class acts as the entry point for launching encrypted JARs.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class JarLauncher {

    /**
     * The internal {@link Launcher} instance responsible for handling encryption/decryption setup.
     */
    private final Launcher launcher;

    /**
     * Constructs a new {@code JarLauncher} and initializes the underlying {@link Launcher}.
     *
     * @param args Command-line arguments passed to the application.
     * @throws Exception If an error occurs during the initialization of the {@link Launcher}.
     */
    public JarLauncher(String... args) throws Exception {
        this.launcher = new Launcher(args);
    }

    /**
     * The main entry point for launching the JAR application with security features.
     *
     * @param args Command-line arguments.
     * @throws Exception If an error occurs during application launch.
     */
    public static void main(String... args) throws Exception {
        new JarLauncher(args).launch();
    }

    /**
     * Launches the JAR application using a custom {@link JarClassLoader} that supports encrypted resources. It
     * determines the main class from the JAR's manifest and invokes its {@code main} method.
     *
     * @throws Exception If an error occurs during the launch process, such as failing to find the main class or method.
     */
    public void launch() throws Exception {
        Logger.info(
                true,
                "Shade",
                "Jar launcher started: argCount={}",
                launcher.args == null ? 0 : launcher.args.length);
        try {
            JarClassLoader jarClassLoader;

            ClassLoader classLoader = this.getClass().getClassLoader();
            if (classLoader instanceof URLClassLoader) {
                URLClassLoader urlClassLoader = (URLClassLoader) classLoader;
                jarClassLoader = new JarClassLoader(urlClassLoader.getURLs(), classLoader.getParent(),
                        launcher.decryptorProvider, launcher.encryptorProvider, launcher.key);
                Logger.debug(
                        false,
                        "Shade",
                        "Jar launcher class loader prepared: source=urlClassLoader, urlCount={}",
                        urlClassLoader.getURLs().length);
            } else {
                ProtectionDomain domain = this.getClass().getProtectionDomain();
                CodeSource source = domain.getCodeSource();
                URI location = (null == source ? null : source.getLocation().toURI());
                String path = (null == location ? null : location.getSchemeSpecificPart());
                if (null == path) {
                    throw new IllegalStateException("Unable to determine code source archive");
                }
                File jar = new File(path);
                URL url = jar.toURI().toURL();
                jarClassLoader = new JarClassLoader(new URL[] { url }, classLoader.getParent(),
                        launcher.decryptorProvider, launcher.encryptorProvider, launcher.key);
                Logger.debug(
                        false,
                        "Shade",
                        "Jar launcher class loader prepared: source=codeSource, jarFile={}",
                        jar.getName());
            }

            Thread.currentThread().setContextClassLoader(jarClassLoader);
            URL resource = jarClassLoader.findResource(Builder.META_INF_MANIFEST);
            Logger.debug(false, "Shade", "Jar launcher manifest resolved: manifestPresent={}", resource != null);
            try (InputStream in = resource.openStream()) {
                Manifest manifest = new Manifest(in);
                Attributes attributes = manifest.getMainAttributes();
                String jarMainClass = attributes.getValue("Jar-Main-Class");
                Logger.info(true, "Shade", "Jar application main invocation started: mainClass={}", jarMainClass);
                Class<?> mainClass = jarClassLoader.loadClass(jarMainClass);
                Method mainMethod = mainClass.getMethod("main", String[].class);
                mainMethod.invoke(null, new Object[] { launcher.args });
                Logger.info(false, "Shade", "Jar application main invocation finished: mainClass={}", jarMainClass);
            }
        } catch (Exception e) {
            Logger.error(false, "Shade", e, "Jar launcher failed: exception={}", e.getClass().getSimpleName());
            throw e;
        }
    }

}
