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
package org.miaixz.bus.core.lang.reflect;

import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.miaixz.bus.core.center.iterator.EnumerationIterator;
import org.miaixz.bus.core.io.file.FileType;
import org.miaixz.bus.core.io.resource.JarResource;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Keys;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.net.url.UrlDecoder;
import org.miaixz.bus.core.xyz.*;

/**
 * Class scanner for scanning classes in packages, JARs, and classpaths. This utility provides methods to find classes
 * based on package name, annotations, or superclasses/interfaces.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ClassScanner implements Serializable {

    /**
     * The serial version UID for serialization.
     */
    @Serial
    private static final long serialVersionUID = 2852276616111L;

    /**
     * The package name to scan.
     */
    private final String packageName;
    /**
     * The package name with a trailing dot, used to avoid ambiguity when checking prefixes. If the package name is
     * empty, this will also be empty.
     */
    private final String packageNameWithDot;
    /**
     * The package directory name, used for file system operations.
     */
    private final String packageDirName;
    /**
     * The package path, used for operations within JAR files. On Linux, this is consistent with {@code packageDirName}.
     */
    private final String packagePath;
    /**
     * The class filter predicate. Only classes that satisfy this predicate will be included in the scan results.
     */
    private final Predicate<Class<?>> classPredicate;
    /**
     * The character set used for decoding URLs and file paths.
     */
    private final java.nio.charset.Charset charset;
    /**
     * The set of scanned classes that satisfy the filter conditions.
     */
    private final Set<Class<?>> classes = new HashSet<>();
    /**
     * A set of class names that failed to load during scanning, if {@code ignoreLoadError} is true.
     */
    private final Set<String> classesOfLoadError = new HashSet<>();
    /**
     * The class loader to use for loading classes.
     */
    private ClassLoader classLoader;
    /**
     * Flag indicating whether to initialize classes when loading them.
     */
    private boolean initialize;
    /**
     * Flag indicating whether to ignore errors during class loading. If {@code true}, loading errors will be recorded
     * in {@code classesOfLoadError} but will not stop the scan.
     */
    private boolean ignoreLoadError = false;

    /**
     * Constructs a new {@code ClassScanner} with default settings (UTF-8 encoding, no package filter).
     */
    public ClassScanner() {
        this(null);
    }

    /**
     * Constructs a new {@code ClassScanner} for the given package name, with default UTF-8 encoding.
     *
     * @param packageName The package name to scan. Use {@code null} or an empty string to scan all packages.
     */
    public ClassScanner(final String packageName) {
        this(packageName, null);
    }

    /**
     * Constructs a new {@code ClassScanner} for the given package name and class filter, with default UTF-8 encoding.
     *
     * @param packageName    The package name to scan. Use {@code null} or an empty string to scan all packages.
     * @param classPredicate The class filter predicate. Can be {@code null} to accept all classes.
     */
    public ClassScanner(final String packageName, final Predicate<Class<?>> classPredicate) {
        this(packageName, classPredicate, Charset.UTF_8);
    }

    /**
     * Constructs a new {@code ClassScanner} for the given package name, class filter, and character set.
     *
     * @param packageName    The package name to scan. Use {@code null} or an empty string to scan all packages.
     * @param classPredicate The class filter predicate. Can be {@code null} to accept all classes.
     * @param charset        The character set for decoding URLs and file paths.
     */
    public ClassScanner(String packageName, final Predicate<Class<?>> classPredicate,
            final java.nio.charset.Charset charset) {
        packageName = StringKit.toStringOrEmpty(packageName);
        this.packageName = packageName;
        this.packageNameWithDot = StringKit.addSuffixIfNot(packageName, Symbol.DOT);
        this.packageDirName = packageName.replace(Symbol.C_DOT, File.separatorChar);
        this.packagePath = packageName.replace(Symbol.C_DOT, Symbol.C_SLASH);
        this.classPredicate = classPredicate;
        this.charset = charset;
    }

    /**
     * Scans all packages (including those in other loaded JARs or classes) for classes annotated with the specified
     * annotation.
     *
     * @param packageName     The package path. {@code null} or empty string indicates scanning all packages.
     * @param annotationClass The annotation class to search for.
     * @return A set of classes that are annotated with the specified annotation.
     */
    public static Set<Class<?>> scanAllPackageByAnnotation(
            final String packageName,
            final Class<? extends Annotation> annotationClass) {
        return scanAllPackage(packageName, clazz -> clazz.isAnnotationPresent(annotationClass));
    }

    /**
     * Scans the specified package path for classes annotated with the specified annotation. If classes are already
     * present in the classpath, other loaded JARs or classes are not scanned.
     *
     * @param packageName     The package path. {@code null} or empty string indicates scanning all packages.
     * @param annotationClass The annotation class to search for.
     * @return A set of classes that are annotated with the specified annotation.
     */
    public static Set<Class<?>> scanPackageByAnnotation(
            final String packageName,
            final Class<? extends Annotation> annotationClass) {
        return scanPackage(packageName, clazz -> clazz.isAnnotationPresent(annotationClass));
    }

    /**
     * Scans all packages (including those in other loaded JARs or classes) for subclasses or implementations of the
     * specified superclass or interface. The superclass itself is excluded.
     *
     * @param packageName The package path. {@code null} or empty string indicates scanning all packages.
     * @param superClass  The superclass or interface (excluded from results).
     * @return A set of classes that are subclasses or implementations of the specified superclass/interface.
     */
    public static Set<Class<?>> scanAllPackageBySuper(final String packageName, final Class<?> superClass) {
        return scanAllPackage(packageName, clazz -> superClass.isAssignableFrom(clazz) && !superClass.equals(clazz));
    }

    /**
     * Scans the specified package path for subclasses or implementations of the specified superclass or interface. The
     * superclass itself is excluded. If classes are already present in the classpath, other loaded JARs or classes are
     * not scanned.
     *
     * @param packageName The package path. {@code null} or empty string indicates scanning all packages.
     * @param superClass  The superclass or interface (excluded from results).
     * @return A set of classes that are subclasses or implementations of the specified superclass/interface.
     */
    public static Set<Class<?>> scanPackageBySuper(final String packageName, final Class<?> superClass) {
        return scanPackage(packageName, clazz -> superClass.isAssignableFrom(clazz) && !superClass.equals(clazz));
    }

    /**
     * Scans all class files in the entire classpath, including those in other loaded JARs or classes.
     *
     * @return A set of all scanned classes.
     */
    public static Set<Class<?>> scanAllPackage() {
        return scanAllPackage(Normal.EMPTY, null);
    }

    /**
     * Scans all class files in the classpath. If classes are already present in the classpath, other loaded JARs or
     * classes are not scanned.
     *
     * @return A set of all scanned classes.
     */
    public static Set<Class<?>> scanPackage() {
        return scanPackage(Normal.EMPTY, null);
    }

    /**
     * Scans all class files within the specified package path.
     *
     * @param packageName The package path (e.g., "com", "com.", "com.abs", "com.abs.").
     * @return A set of classes found in the specified package.
     */
    public static Set<Class<?>> scanPackage(final String packageName) {
        return scanPackage(packageName, null);
    }

    /**
     * Scans all class files in the specified package path and all loaded classes in the classpath that satisfy the
     * class filter conditions. If the package path is "com.abs" and a class is "com.abs.A.class", but the input is
     * "abs", it may cause a ClassNotFoundException because the className should be "com.abs.A" but becomes "abs.A".
     * This utility handles such exceptions by ignoring them.
     *
     * @param packageName The package path (e.g., "com", "com.", "com.abs", "com.abs.").
     * @param classFilter The class filter predicate to filter out unwanted classes. Can be {@code null}.
     * @return A set of classes that satisfy the filter conditions.
     */
    public static Set<Class<?>> scanAllPackage(final String packageName, final Predicate<Class<?>> classFilter) {
        return new ClassScanner(packageName, classFilter).scan(true);
    }

    /**
     * Scans all class files in the specified package path that satisfy the class filter conditions. If the package path
     * is "com.abs" and a class is "com.abs.A.class", but the input is "abs", it may cause a ClassNotFoundException
     * because the className should be "com.abs.A" but becomes "abs.A". This utility handles such exceptions by ignoring
     * them.
     *
     * @param packageName The package path (e.g., "com", "com.", "com.abs", "com.abs.").
     * @param classFilter The class filter predicate to filter out unwanted classes. Can be {@code null}.
     * @return A set of classes that satisfy the filter conditions.
     */
    public static Set<Class<?>> scanPackage(final String packageName, final Predicate<Class<?>> classFilter) {
        return new ClassScanner(packageName, classFilter).scan();
    }

    /**
     * Scans all class files in the package path that satisfy the class filter conditions. This method first scans the
     * resource directories of the specified package. If no classes are found, it then scans all loaded classes in the
     * entire classpath.
     *
     * @return A set of classes that satisfy the filter conditions.
     */
    public Set<Class<?>> scan() {
        return scan(false);
    }

    /**
     * Scans all class files in the package path that satisfy the class filter conditions.
     *
     * @param forceScanJavaClassPaths Whether to force scanning of other classes located in classpath-associated JARs.
     * @return A set of classes that satisfy the filter conditions.
     */
    public Set<Class<?>> scan(final boolean forceScanJavaClassPaths) {
        // Clear previous scan history for multiple scans
        this.classes.clear();
        this.classesOfLoadError.clear();

        for (final URL url : ResourceKit.getResourceUrlIter(this.packagePath, this.classLoader)) {
            switch (url.getProtocol()) {
                case "file":
                    scanFile(new File(UrlDecoder.decode(url.getFile(), this.charset)), null);
                    break;

                case "jar":
                    scanJar(new JarResource(url).getJarFile());
                    break;
            }
        }

        // If no classes are found in the classpath, or if forced, scan other JARs in the classpath.
        if (forceScanJavaClassPaths || CollKit.isEmpty(this.classes)) {
            scanJavaClassPaths();
        }

        return Collections.unmodifiableSet(this.classes);
    }

    /**
     * After scanning with {@code ignoreLoadError} set to true, this method returns the set of class names that failed
     * to load during the scan.
     *
     * @return A set of class names that encountered loading errors.
     */
    public Set<String> getClassesOfLoadError() {
        return Collections.unmodifiableSet(this.classesOfLoadError);
    }

    /**
     * Sets whether to ignore all errors during class loading. If set to {@code true}, loading errors will be recorded
     * but will not stop the scanning process.
     *
     * @param ignoreLoadError {@code true} to ignore loading errors, {@code false} otherwise.
     */
    public void setIgnoreLoadError(final boolean ignoreLoadError) {
        this.ignoreLoadError = ignoreLoadError;
    }

    /**
     * Sets whether to initialize classes when they are scanned and loaded.
     *
     * @param initialize {@code true} to initialize classes, {@code false} otherwise.
     */
    public void setInitialize(final boolean initialize) {
        this.initialize = initialize;
    }

    /**
     * Sets a custom class loader to be used for loading classes during the scan.
     *
     * @param classLoader The custom class loader.
     */
    public void setClassLoader(final ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * Scans the Java specified ClassPath paths. This method iterates through all entries in the Java classpath and
     * scans them for classes.
     */
    private void scanJavaClassPaths() {
        final String[] javaClassPaths = Keys.getJavaClassPaths();
        for (String classPath : javaClassPaths) {
            // Bug fix: JARs not found due to spaces and Chinese characters in the path.
            classPath = UrlDecoder.decode(classPath, Charset.defaultCharset());

            scanFile(new File(classPath), null);
        }
    }

    /**
     * Scans for classes within a given file or directory.
     *
     * @param file    The file or directory to scan.
     * @param rootDir The absolute path of the classpath corresponding to the package name. If {@code null}, it will be
     *                determined from the file path.
     */
    private void scanFile(final File file, final String rootDir) {
        if (file.isFile()) {
            final String fileName = file.getAbsolutePath();
            if (fileName.endsWith(FileType.CLASS)) {
                final String className = fileName//
                        // 8 is the length of "classes", fileName.length() - 6 is the length of ".class"
                        .substring(rootDir.length(), fileName.length() - 6)//
                        .replace(File.separatorChar, Symbol.C_DOT);//
                // Add classes that meet the conditions
                addIfAccept(className);
            } else if (fileName.endsWith(FileType.JAR)) {
                try {
                    scanJar(new JarFile(file));
                } catch (final IOException e) {
                    throw new InternalException(e);
                }
            }
        } else if (file.isDirectory()) {
            final File[] files = file.listFiles();
            if (null != files) {
                for (final File subFile : files) {
                    scanFile(subFile, (null == rootDir) ? subPathBeforePackage(file) : rootDir);
                }
            }
        }
    }

    /**
     * Scans a JAR file for classes. The JAR file is closed after scanning.
     *
     * @param jar The JAR file to scan.
     */
    private void scanJar(final JarFile jar) {
        try {
            String name;
            for (final JarEntry entry : new EnumerationIterator<>(jar.entries())) {
                name = StringKit.removePrefix(entry.getName(), Symbol.SLASH);
                if (StringKit.isEmpty(packagePath) || name.startsWith(this.packagePath)) {
                    if (name.endsWith(FileType.CLASS) && !entry.isDirectory()) {
                        final String className = name//
                                .substring(0, name.length() - 6)//
                                .replace(Symbol.C_SLASH, Symbol.C_DOT);//
                        addIfAccept(loadClass(className));
                    }
                }
            }
        } finally {
            IoKit.closeQuietly(jar);
        }
    }

    /**
     * Loads a class by its fully qualified name.
     *
     * @param className The fully qualified name of the class to load.
     * @return The loaded {@code Class} object, or {@code null} if the class could not be loaded.
     */
    protected Class<?> loadClass(final String className) {
        ClassLoader loader = this.classLoader;
        if (null == loader) {
            loader = ClassKit.getClassLoader();
            this.classLoader = loader;
        }

        Class<?> clazz = null;
        try {
            clazz = Class.forName(className, this.initialize, loader);
        } catch (final NoClassDefFoundError | ClassNotFoundException e) {
            // Classes that cannot be loaded due to dependent libraries are skipped directly.
        } catch (final UnsupportedClassVersionError e) {
            // Incompatible classes due to version differences are skipped.
        } catch (final Exception e) {
            classesOfLoadError.add(className);
        } catch (final Throwable e) {
            if (!this.ignoreLoadError) {
                throw ExceptionKit.wrapRuntime(e);
            } else {
                classesOfLoadError.add(className);
            }
        }
        return clazz;
    }

    /**
     * Adds a class to the results if it matches the package name and satisfies the class filter.
     *
     * @param className The fully qualified name of the class to potentially add.
     */
    private void addIfAccept(final String className) {
        if (StringKit.isBlank(className)) {
            return;
        }
        final int classLen = className.length();
        final int packageLen = this.packageName.length();
        if (classLen == packageLen) {
            // If class name and package name have the same length, the user might have provided a class name as package
            // name.
            if (className.equals(this.packageName)) {
                addIfAccept(loadClass(className));
            }
        } else if (classLen > packageLen) {
            // Check if the class name starts with the specified package name, followed by a dot.
            if (Symbol.DOT.equals(this.packageNameWithDot) || className.startsWith(this.packageNameWithDot)) {
                addIfAccept(loadClass(className));
            }
        }
    }

    /**
     * Adds a class to the results if it is not {@code null} and satisfies the class filter.
     *
     * @param clazz The {@code Class} object to potentially add.
     */
    private void addIfAccept(final Class<?> clazz) {
        if (null != clazz) {
            final Predicate<Class<?>> classFilter = this.classPredicate;
            if (classFilter == null || classFilter.test(clazz)) {
                this.classes.add(clazz);
            }
        }
    }

    /**
     * Extracts the path segment before the package name from an absolute file path.
     *
     * @param file The file from which to extract the path.
     * @return The path segment before the package name, with a trailing file separator.
     */
    private String subPathBeforePackage(final File file) {
        String filePath = file.getAbsolutePath();
        if (StringKit.isNotEmpty(this.packageDirName)) {
            filePath = StringKit.subBefore(filePath, this.packageDirName, true);
        }
        return StringKit.addSuffixIfNot(filePath, File.separator);
    }

}
