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
package org.miaixz.bus.core.lang.loader;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.jar.JarFile;

/**
 * Utility class for creating various types of resource loaders.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class Loaders {

    /**
     * Creates a classpath resource loader, which is an enhanced encapsulation of
     * {@link ClassLoader#getResources(String)}. By default, it uses the {@link Thread#currentThread()}'s
     * {@link Thread#getContextClassLoader()} as the ClassLoader. If the current thread's context class loader is
     * {@code null}, it falls back to {@link ClassLoader#getSystemClassLoader()}.
     * <p>
     * Examples:
     * <p>
     * 1. {@code Loaders.std().load("org/miaixz/bus/core/loader");} Loads all resources under the
     * "org/miaixz/bus/core/loader" directory in the classpath, but not its subdirectories.
     * <p>
     * 2. {@code Loaders.std().load("org/", true);} Loads all resources under the "org/" directory in the classpath,
     * including its subdirectories.
     *
     * @return A classpath resource loader.
     */
    public static Loader std() {
        return new StdLoader();
    }

    /**
     * Creates a classpath resource loader with a specified {@link ClassLoader}.
     * <p>
     * Examples:
     * <p>
     * 1. {@code Loaders.std(myClassLoader).load("org/miaixz/bus/core/loader");} Loads all resources under the
     * "org/miaixz/bus/core/loader" directory in the classpath using {@code myClassLoader}, but not its subdirectories.
     * <p>
     * 2. {@code Loaders.std(myClassLoader).load("org/", true);} Loads all resources under the "org/" directory in the
     * classpath using {@code myClassLoader}, including its subdirectories.
     *
     * @param classLoader The class loader to use.
     * @return A classpath resource loader.
     */
    public static Loader std(ClassLoader classLoader) {
        return new StdLoader(classLoader);
    }

    /**
     * Creates a package-name-based resource loader. This is a delegating loader that does not have its own resource
     * loading logic but delegates to an actual resource loader. In this creation method, it delegates to the
     * {@link Loaders#std()} resource loader.
     * <p>
     * Example: The resource path expression in a resource loader created by {@link Loaders#std()} is not very
     * intuitive; using package names is often clearer and easier to understand.
     * <p>
     * 1. {@code Loaders.pkg().load("io.loadkit");} Loads all resources under the "io.loadkit" package in the classpath,
     * but not its subpackages.
     * <p>
     * 2. {@code Loaders.pkg().load("io", true);} Loads all resources under the "io" package in the classpath, including
     * its subpackages.
     *
     * @return A package-name-based resource loader.
     */
    public static Loader pkg() {
        return new PkgLoader();
    }

    /**
     * Creates a package-name-based resource loader. This is a delegating loader that does not have its own resource
     * loading logic but delegates to an actual resource loader. In this creation method, it delegates to the
     * {@link Loaders#std(ClassLoader)} resource loader.
     * <p>
     * Example: The resource path expression in a resource loader created by {@link Loaders#std()} is not very
     * intuitive; using package names is often clearer and easier to understand.
     * <p>
     * 1. {@code Loaders.pkg(myClassLoader).load("org.miaixz.bus.core.lang.loader");} Loads all resources under the
     * "org.miaixz.bus.core.lang.loader" package in the classpath using {@code myClassLoader}, but not its subpackages.
     * <p>
     * 2. {@code Loaders.pkg(myClassLoader).load("org", true);} Loads all resources under the "org" package in the
     * classpath using {@code myClassLoader}, including its subpackages.
     *
     * @param classLoader The class loader to use.
     * @return A package-name-based resource loader.
     */
    public static Loader pkg(ClassLoader classLoader) {
        return new PkgLoader(classLoader);
    }

    /**
     * Creates a package-name-based resource loader. This is a delegating loader that does not have its own resource
     * loading logic but delegates to a specified delegate resource loader. This allows for more flexible loading
     * methods by wrapping an actual resource loader.
     * <p>
     * Examples:
     * <p>
     * 1. {@code Loaders.pkg(Loaders.ant()).load("org.miaixz.bus.core.lang.loader.*");} Loads resources under the
     * {@code org.miaixz.bus.core.loader} package, but does not recursively load subpackages.
     *
     * <p>
     * 2. {@code Loaders.pkg(Loaders.ant()).load("org.**");} Loads resources under the {@code io} package and
     * recursively loads all subpackages at any level.
     *
     * <p>
     * 3. {@code Loaders.pkg(Loaders.ant()).load("org.miaixz.bus.core.load???.*");} Loads all resources under
     * subpackages of {@code io} that start with "load" followed by three characters.
     *
     *
     * @param delegate The delegate loader.
     * @return A package-name-based resource loader.
     */
    public static Loader pkg(Loader delegate) {
        return new PkgLoader(delegate);
    }

    /**
     * Creates an ANT-style path expression resource loader. This is a delegating loader that does not have its own
     * resource loading logic but delegates to an actual resource loader. In this creation method, it delegates to the
     * {@link Loaders#std()} resource loader.
     * <p>
     * Examples:
     *
     * <p>
     * 1. {@code Loaders.ant().load("org/miaixz/bus/core/loader/*");} Loads resources under the
     * {@code org/miaixz/bus/core/loader/} directory, but not its subdirectories.
     *
     * <p>
     * 2. {@code Loaders.ant().load("io/**");} Loads resources under the {@code io/} directory and recursively loads all
     * resources in its subdirectories.
     *
     * <p>
     * 3. {@code Loaders.ant().load("org/miaixz/bus/core/loader/*Loader.class");} Loads resources under the
     * {@code org/miaixz/bus/core/loader/} directory that end with "Loader.class".
     *
     * @return An ANT-style path expression resource loader.
     */
    public static Loader ant() {
        return new AntLoader();
    }

    /**
     * Creates an ANT-style path expression resource loader. This is a delegating loader that does not have its own
     * resource loading logic but delegates to an actual resource loader. In this creation method, it delegates to the
     * {@link Loaders#std(ClassLoader)} resource loader.
     * <p>
     * Examples:
     *
     * <p>
     * 1. {@code Loaders.ant(myClassLoader).load("org/miaixz/bus/core/loader/*");} Loads resources under the
     * {@code org/miaixz/bus/core/loader/} directory using {@code myClassLoader}, but not its subdirectories.
     *
     * <p>
     * 2. {@code Loaders.ant(myClassLoader).load("org/**");} Loads resources under the {@code io/} directory using
     * {@code myClassLoader} and recursively loads all resources in its subdirectories.
     *
     * <p>
     * 3. {@code Loaders.ant(myClassLoader).load("org/miaixz/bus/core/loader/*Loader.class");} Loads resources under the
     * {@code org/miaixz/bus/core/loader/} directory using {@code myClassLoader} that end with "Loader.class".
     *
     * @param classLoader The class loader to use.
     * @return An ANT-style path expression resource loader.
     */
    public static Loader ant(ClassLoader classLoader) {
        return new AntLoader(classLoader);
    }

    /**
     * Creates an ANT-style path expression resource loader. This is a delegating loader that does not have its own
     * resource loading logic but delegates to a specified delegate resource loader.
     * <p>
     * Examples:
     *
     * <p>
     * 1. {@code Loaders.ant(myDelegateLoader).load("org/miaixz/bus/core/loader/*");} Loads resources under the
     * {@code org/miaixz/bus/core/loader/} directory using {@code myDelegateLoader}, but not its subdirectories.
     *
     * <p>
     * 2. {@code Loaders.ant(myDelegateLoader).load("org/**");} Loads resources under the {@code io/} directory using
     * {@code myDelegateLoader} and recursively loads all resources in its subdirectories.
     *
     * <p>
     * 3. {@code Loaders.ant(myDelegateLoader).load("org/miaixz/bus/core/loader/*Loader.class");} Loads resources under
     * the {@code org/miaixz/bus/core/loader/} directory using {@code myDelegateLoader} that end with "Loader.class".
     *
     * @param delegate The delegate loader.
     * @return An ANT-style path expression resource loader.
     */
    public static Loader ant(Loader delegate) {
        return new AntLoader(delegate);
    }

    /**
     * Creates a regular expression resource loader. This is a delegating loader that does not have its own resource
     * loading logic but delegates to an actual resource loader. In this creation method, it delegates to the
     * {@link Loaders#std()} resource loader.
     * <p>
     * Examples:
     *
     * <p>
     * 1. {@code Loaders.regex().load("org/miaixz/bus/core/loader/\\w+Loader.class");} Loads resources under the
     * {@code org/miaixz/bus/core/loader/} directory that match the regular expression {@code \w+Loader.class}.
     *
     * <p>
     * 2. {@code Loaders.regex().load("org/.*");} Loads all resources under the {@code org/} package.
     *
     * @return A regular expression resource loader.
     */
    public static Loader regex() {
        return new RegexLoader();
    }

    /**
     * Creates a regular expression resource loader. This is a delegating loader that does not have its own resource
     * loading logic but delegates to an actual resource loader. In this creation method, it delegates to the
     * {@link Loaders#std(ClassLoader)} resource loader.
     * <p>
     * Examples:
     *
     * <p>
     * 1. {@code Loaders.regex(myClassLoader).load("org/miaixz/bus/core/loader/\\w+Loader.class");} Loads resources
     * under the {@code org/miaixz/bus/core/loader/} directory using {@code myClassLoader} that match the regular
     * expression {@code \w+Loader.class}.
     *
     * <p>
     * 2. {@code Loaders.regex(myClassLoader).load("org/.*");} Loads all resources under the {@code org/} package using
     * {@code myClassLoader}.
     *
     * @param classLoader The class loader to use.
     * @return A regular expression resource loader.
     */
    public static Loader regex(ClassLoader classLoader) {
        return new RegexLoader(classLoader);
    }

    /**
     * Creates a regular expression resource loader. This is a delegating loader that does not have its own resource
     * loading logic but delegates to a specified delegate resource loader.
     * <p>
     * Examples:
     *
     * <p>
     * 1. {@code Loaders.regex(myDelegateLoader).load("org/miaixz/bus/core/loader/\\w+Loader.class");} Loads resources
     * under the {@code org/miaixz/bus/core/loader/} directory using {@code myDelegateLoader} that match the regular
     * expression {@code \w+Loader.class}.
     *
     * <p>
     * 2. {@code Loaders.regex(myDelegateLoader).load("org/.*");} Loads all resources under the {@code org/} package
     * using {@code myDelegateLoader}.
     *
     * @param delegate The delegate loader.
     * @return A regular expression resource loader.
     */
    public static Loader regex(Loader delegate) {
        return new RegexLoader(delegate);
    }

    /**
     * Creates a file resource loader.
     *
     * @param root The root directory for file resources.
     * @return A file resource loader.
     * @throws IOException If an I/O error occurs during initialization.
     */
    public static Loader file(File root) throws IOException {
        return new FileLoader(root);
    }

    /**
     * Creates a file resource loader.
     *
     * @param fileURL The URL of the root directory for file resources.
     * @return A file resource loader.
     */
    public static Loader file(URL fileURL) {
        return new FileLoader(fileURL);
    }

    /**
     * Creates a file resource loader.
     *
     * @param context The URL context for the file resources.
     * @param root    The root directory for file resources.
     * @return A file resource loader.
     */
    public static Loader file(URL context, File root) {
        return new FileLoader(context, root);
    }

    /**
     * Creates a JAR package resource loader.
     *
     * @param file The JAR package file.
     * @return A JAR package resource loader.
     * @throws IOException If an I/O error occurs during initialization.
     */
    public static Loader jar(File file) throws IOException {
        return new JarLoader(file);
    }

    /**
     * Creates a JAR package resource loader.
     *
     * @param jarURL The URL of the JAR package.
     * @return A JAR package resource loader.
     * @throws IOException If an I/O error occurs during initialization.
     */
    public static Loader jar(URL jarURL) throws IOException {
        return new JarLoader(jarURL);
    }

    /**
     * Creates a JAR package resource loader.
     *
     * @param context The URL context for the JAR package.
     * @param jarFile The JAR file.
     * @return A JAR package resource loader.
     */
    public static Loader jar(URL context, JarFile jarFile) {
        return new JarLoader(context, jarFile);
    }

    /**
     * Creates a native library resource loader.
     * <p>
     * This loader loads dynamic library information (e.g., .dll/.so files) from the current JAR archive. 1. Files are
     * copied from the current JAR archive to a system temporary directory. 2. The files in the JAR are copied to the
     * system temporary directory and then loaded. Temporary files are deleted upon exit.
     *
     * @return A native library resource loader.
     */
    public static Loader nat() {
        return new NatLoader();
    }

}
