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

import org.miaixz.bus.shade.safety.Builder;
import org.miaixz.bus.shade.safety.Launcher;

/**
 * A custom launcher for standard JAR files that integrates with the {@link Launcher} to provide enhanced security
 * features such as JAR encryption and decryption. This class acts as the entry point for launching encrypted JARs.
 *
 * @author Kimi Liu
 * @since Java 17+
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
        JarClassLoader jarClassLoader;

        ClassLoader classLoader = this.getClass().getClassLoader();
        if (classLoader instanceof URLClassLoader) {
            URLClassLoader urlClassLoader = (URLClassLoader) classLoader;
            jarClassLoader = new JarClassLoader(urlClassLoader.getURLs(), classLoader.getParent(),
                    launcher.decryptorProvider, launcher.encryptorProvider, launcher.key);
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
            jarClassLoader = new JarClassLoader(new URL[] { url }, classLoader.getParent(), launcher.decryptorProvider,
                    launcher.encryptorProvider, launcher.key);
        }

        Thread.currentThread().setContextClassLoader(jarClassLoader);
        URL resource = jarClassLoader.findResource(Builder.META_INF_MANIFEST);
        try (InputStream in = resource.openStream()) {
            Manifest manifest = new Manifest(in);
            Attributes attributes = manifest.getMainAttributes();
            String jarMainClass = attributes.getValue("Jar-Main-Class");
            Class<?> mainClass = jarClassLoader.loadClass(jarMainClass);
            Method mainMethod = mainClass.getMethod("main", String[].class);
            mainMethod.invoke(null, new Object[] { launcher.args });
        }
    }

}
