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
package org.miaixz.bus.core.lang.loader.classloader;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import org.miaixz.bus.core.io.file.FileName;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ClassKit;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.MethodKit;
import org.miaixz.bus.core.xyz.UrlKit;

/**
 * A {@link ClassLoader} for loading classes from external JAR files.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JarClassLoader extends URLClassLoader {

    /**
     * Constructs a new JarClassLoader with an empty URL array.
     */
    public JarClassLoader() {
        this(new URL[] {});
    }

    /**
     * Constructs a new JarClassLoader for the specified URLs.
     *
     * @param urls The URLs from which to load classes and resources.
     */
    public JarClassLoader(final URL[] urls) {
        super(urls, ClassKit.getClassLoader());
    }

    /**
     * Constructs a new JarClassLoader for the specified URLs and parent class loader.
     *
     * @param urls        The URLs from which to load classes and resources.
     * @param classLoader The parent class loader for delegation.
     */
    public JarClassLoader(final URL[] urls, final ClassLoader classLoader) {
        super(urls, classLoader);
    }

    /**
     * Creates a {@link JarClassLoader} and loads all JAR files and classes from the specified directory.
     *
     * @param dir The directory containing JAR files or class files.
     * @return A new {@code JarClassLoader} instance.
     */
    public static JarClassLoader load(final File dir) {
        final JarClassLoader loader = new JarClassLoader();
        loader.addJar(dir); // Find and load all JARs
        loader.addURL(dir); // Add the directory itself for class files
        return loader;
    }

    /**
     * Creates a {@link JarClassLoader} and loads the specified JAR file or all JARs in the specified directory.
     *
     * @param jarFile The JAR file or a directory containing JAR files.
     * @return A new {@code JarClassLoader} instance.
     */
    public static JarClassLoader loadJar(final File jarFile) {
        final JarClassLoader loader = new JarClassLoader();
        loader.addJar(jarFile);
        return loader;
    }

    /**
     * Loads a JAR file into the specified {@link URLClassLoader}.
     *
     * @param loader  The {@link URLClassLoader} to which the JAR will be added.
     * @param jarFile The JAR file to load.
     * @throws InternalException If an I/O error occurs or the addURL method cannot be invoked.
     */
    public static void loadJar(final URLClassLoader loader, final File jarFile) throws InternalException {
        try {
            final Method method = MethodKit.getMethod(URLClassLoader.class, "addURL", URL.class);
            if (null != method) {
                final List<File> jars = loopJar(jarFile);
                for (final File jar : jars) {
                    MethodKit.invoke(loader, method, jar.toURI().toURL());
                }
            }
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Loads a JAR file into the system class loader.
     *
     * @param jarFile The JAR file to load.
     * @return The system {@link URLClassLoader}.
     */
    public static URLClassLoader loadJarToSystemClassLoader(final File jarFile) {
        final URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        loadJar(urlClassLoader, jarFile);
        return urlClassLoader;
    }

    /**
     * Recursively finds all JAR files in a given file or directory.
     *
     * @param file The file or directory to search.
     * @return A list of JAR files.
     */
    private static List<File> loopJar(final File file) {
        return FileKit.loopFiles(file, JarClassLoader::isJarFile);
    }

    /**
     * Checks if a file is a JAR file based on its extension.
     *
     * @param file The file to check.
     * @return {@code true} if the file is a JAR file, {@code false} otherwise.
     */
    private static boolean isJarFile(final File file) {
        return FileKit.isFile(file) && FileName.isType(file.getName(), Normal.URL_PROTOCOL_JAR);
    }

    /**
     * Adds a JAR file or a directory of JAR files to this classloader's classpath.
     *
     * @param jarFileOrDir The JAR file or the directory containing JAR files.
     * @return this {@code JarClassLoader} instance.
     */
    public JarClassLoader addJar(final File jarFileOrDir) {
        // The loopJar method returns the file itself if it's a single JAR file.
        final List<File> jars = loopJar(jarFileOrDir);
        for (final File jar : jars) {
            addURL(jar);
        }
        return this;
    }

    /**
     * Adds a URL to the classpath.
     *
     * @param url the URL to be added to the classpath.
     */
    @Override
    public void addURL(final URL url) {
        super.addURL(url);
    }

    /**
     * Adds a directory or a JAR file to the classpath. If it's a directory, it will be searched for class files. If
     * it's a file, it must be a JAR.
     *
     * @param dir The directory or JAR file to add.
     * @return this {@code JarClassLoader} instance.
     */
    public JarClassLoader addURL(final File dir) {
        super.addURL(UrlKit.getURL(dir));
        return this;
    }

}
