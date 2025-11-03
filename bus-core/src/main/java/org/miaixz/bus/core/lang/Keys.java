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
package org.miaixz.bus.core.lang;

import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;

import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.BooleanKit;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * A constant pool for system property names, encapsulating information about the Java runtime environment, Java Virtual
 * Machine, Java class information, operating system, and user details.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Keys {

    /**
     * The core framework identifier.
     */
    public static final String BUS = "bus";

    /**
     * The name of the framework.
     */
    public static final String NAME = "bus.name";

    /**
     * The version of the framework.
     */
    public static final String VERSION = "bus.version";

    /**
     * The name of the operating system.
     */
    public static final String OS_NAME = "os.name";

    /**
     * The architecture of the operating system.
     */
    public static final String OS_ARCH = "os.arch";

    /**
     * The version of the operating system.
     */
    public static final String OS_VERSION = "os.version";
    /**
     * The user's account name.
     */
    public static final String USER_NAME = "user.name";

    /**
     * The user's home directory.
     */
    public static final String USER_HOME = "user.home";

    /**
     * The current working directory.
     */
    public static final String USER_DIR = "user.dir";
    /**
     * The default character encoding for files.
     */
    public static final String FILE_ENCODING = "file.encoding";
    /**
     * The current user's language.
     */
    public static final String USER_LANGUAGE = "user.language";
    /**
     * The current user's country or region.
     */
    public static final String USER_COUNTRY = "user.country";
    /**
     * The current user's region.
     */
    public static final String USER_REGION = "user.region";
    /**
     * The file path separator. On Unix and Linux, it is {@code '/'}; on Windows, it is {@code '\'}.
     */
    public static final String FILE_SEPARATOR = "file.separator";
    /**
     * The path separator used between multiple paths. On Unix and Linux, it is {@code ':'}; on Windows, it is
     * {@code ';'}.
     */
    public static final String PATH_SEPARATOR = "path.separator";
    /**
     * The line separator. On Unix, it is {@code '\n'}.
     */
    public static final String LINE_SEPARATOR = "line.separator";

    /**
     * The Java Runtime Environment version.
     */
    public static final String JAVA_VERSION = "java.version";

    /**
     * The Java Runtime Environment vendor.
     */
    public static final String JAVA_VENDOR = "java.vendor";

    /**
     * The URL of the Java vendor.
     */
    public static final String JAVA_VENDOR_URL = "java.vendor.url";

    /**
     * The Java installation directory.
     */
    public static final String JAVA_HOME = "java.home";

    /**
     * The Java Virtual Machine specification version.
     */
    public static final String JAVA_VM_SPECIFICATION_VERSION = "java.vm.specification.version";

    /**
     * The Java Virtual Machine specification vendor.
     */
    public static final String JAVA_VM_SPECIFICATION_VENDOR = "java.vm.specification.vendor";

    /**
     * The Java Virtual Machine specification name.
     */
    public static final String JAVA_VM_SPECIFICATION_NAME = "java.vm.specification.name";

    /**
     * The Java Virtual Machine implementation version.
     */
    public static final String JAVA_VM_VERSION = "java.vm.version";

    /**
     * The Java Virtual Machine implementation vendor.
     */
    public static final String JAVA_VM_VENDOR = "java.vm.vendor";

    /**
     * The Java Virtual Machine implementation name.
     */
    public static final String JAVA_VM_NAME = "java.vm.name";
    /**
     * Additional information about the Java Virtual Machine implementation.
     */
    public static final String JAVA_VM_INFO = " java.vm.info";

    /**
     * The Java Runtime Environment specification version.
     */
    public static final String JAVA_SPECIFICATION_VERSION = "java.specification.version";

    /**
     * The Java Runtime Environment specification vendor.
     */
    public static final String JAVA_SPECIFICATION_VENDOR = "java.specification.vendor";

    /**
     * The Java Runtime Environment specification name.
     */
    public static final String JAVA_SPECIFICATION_NAME = "java.specification.name";

    /**
     * The Java class format version number.
     */
    public static final String JAVA_CLASS_VERSION = "java.class.version";

    /**
     * The Java ClassPath used by the system.
     */
    public static final String JAVA_CLASS_PATH = "java.class.path";

    /**
     * The list of paths searched when loading libraries.
     */
    public static final String JAVA_LIBRARY_PATH = "java.library.path";

    /**
     * The default temporary file path.
     */
    public static final String JAVA_IO_TMPDIR = "java.io.tmpdir";

    /**
     * The name of the JIT compiler to use.
     */
    public static final String JAVA_COMPILER = "java.compiler";

    /**
     * The path to one or more extension directories.
     */
    public static final String JAVA_EXT_DIRS = "java.ext.dirs";
    /**
     * The name of the Java Runtime Environment.
     */
    public static final String JAVA_RUNTIME_NAME = " java.runtime.name";
    /**
     * The version of the Java Runtime Environment.
     */
    public static final String JAVA_RUNTIME_VERSION = "java.runtime.version";
    /**
     * The directory for endorsed standards override mechanism.
     */
    public static final String JAVA_ENDORSED_DIRS = "java.endorsed.dirs";
    /**
     * The class name of the {@code Transient} annotation in Java Beans.
     */
    public static final String JAVA_BEANS_TRANSIENT = "java.beans.Transient";
    /**
     * The boot classpath used by the BootstrapClassLoader.
     */
    public static final String SUN_BOOT_CLASS_PATH = "sun.boot.class.path";
    /**
     * The bitness of the JVM system (e.g., "32" or "64").
     */
    public static final String SUN_ARCH_DATA_MODEL = "sun.arch.data.model";
    /**
     * The base name for resource bundles, typically "messages".
     */
    public static final String BUNDLE_NAME = "messages";
    /**
     * Custom system property: whether to parse date strings in strict mode.
     */
    public static final String DATE_LENIENT = "bus.date.lenient";
    /**
     * The major version of the JVM (e.g., 8 for Java 8, 17 for Java 25).
     */
    public static final int JVM_VERSION;
    /**
     * Indicates whether the current JVM version is 25 or later.
     */
    public static final boolean IS_AT_LEAST_JDK25;
    /**
     * Indicates whether the current environment is Android.
     */
    public static final boolean IS_ANDROID;
    /**
     * Indicates whether the current JVM is OpenJ9.
     */
    public static final boolean IS_OPENJ9;

    /**
     * Indicates whether the current environment is GraalVM Native Image.
     */
    public static final boolean IS_GRAALVM_NATIVE;

    static {
        // Initialize JVM version.
        JVM_VERSION = _getJvmVersion();
        IS_AT_LEAST_JDK25 = JVM_VERSION >= 25;

        // Initialize JVM name related flags.
        final String jvmName = _getJvmName();
        IS_ANDROID = jvmName.equals("Dalvik");
        IS_OPENJ9 = jvmName.contains("OpenJ9");
        // Initialize GraalVM Native Image flag.
        IS_GRAALVM_NATIVE = null != System.getProperty("org.graalvm.nativeimage.imagecode");
    }

    /**
     * Retrieves a system property or environment variable value. If a {@link SecurityException} occurs due to Java
     * security restrictions, the error is logged, and the {@code defaultValue} is returned.
     *
     * @param name         The name of the property or environment variable.
     * @param defaultValue The default value to return if the property is not found or access is denied.
     * @return The property value, or {@code defaultValue} if not found or access is denied.
     * @see System#getProperty(String)
     * @see System#getenv(String)
     */
    public static String get(final String name, final String defaultValue) {
        return ObjectKit.defaultIfNull(get(name), defaultValue);
    }

    /**
     * Retrieves a system property or environment variable value. If a {@link SecurityException} occurs, it is logged,
     * and {@code null} is returned.
     *
     * @param key The name of the property or environment variable.
     * @return The property value, or {@code null} if not found or access is denied.
     * @see System#getProperty(String)
     * @see System#getenv(String)
     */
    public static String get(final String key) {
        return get(key, false);
    }

    /**
     * Retrieves a system property or environment variable value, suppressing any {@link SecurityException}s. If a
     * security exception occurs, {@code null} is returned without logging the error.
     *
     * @param key The name of the property or environment variable.
     * @return The property value, or {@code null} if not found or access is denied.
     * @see System#getProperty(String)
     * @see System#getenv(String)
     */
    public static String getQuietly(final String key) {
        return get(key, true);
    }

    /**
     * Retrieves a system property or environment variable value. If a {@link SecurityException} occurs due to Java
     * security restrictions, the error is logged to {@code System.err} (unless in quiet mode), and {@code null} is
     * returned.
     *
     * @param name  The name of the property or environment variable.
     * @param quiet If {@code true}, security exceptions are suppressed and not logged to {@code System.err}.
     * @return The property value, or {@code null} if not found or access is denied.
     * @see System#getProperty(String)
     * @see System#getenv(String)
     */
    public static String get(final String name, final boolean quiet) {
        String value = null;
        try {
            value = System.getProperty(name);
        } catch (final SecurityException e) {
            if (!quiet) {
                Console.error(
                        "Caught a SecurityException reading the system property '{}'; "
                                + "the Keys property value will default to null.",
                        name);
            }
        }

        if (null == value) {
            try {
                value = System.getenv(name);
            } catch (final SecurityException e) {
                if (!quiet) {
                    Console.error(
                            "Caught a SecurityException reading the system env '{}'; "
                                    + "the Keys env value will default to null.",
                            name);
                }
            }
        }

        return value;
    }

    /**
     * Retrieves a system property or environment variable value and converts it to a boolean.
     *
     * @param key          The name of the property or environment variable.
     * @param defaultValue The default boolean value to return if the property is not found or cannot be converted.
     * @return The boolean value of the property, or {@code defaultValue} if not found or conversion fails.
     */
    public static boolean getBoolean(final String key, final boolean defaultValue) {
        final String value = get(key);
        if (value == null) {
            return defaultValue;
        }

        return BooleanKit.toBoolean(value);
    }

    /**
     * Retrieves a system property or environment variable value and converts it to an integer.
     *
     * @param key          The name of the property or environment variable.
     * @param defaultValue The default integer value to return if the property is not found or cannot be converted.
     * @return The integer value of the property, or {@code defaultValue} if not found or conversion fails.
     */
    public static int getInt(final String key, final int defaultValue) {
        return Convert.toInt(get(key), defaultValue);
    }

    /**
     * Retrieves a system property or environment variable value and converts it to a long.
     *
     * @param key          The name of the property or environment variable.
     * @param defaultValue The default long value to return if the property is not found or cannot be converted.
     * @return The long value of the property, or {@code defaultValue} if not found or conversion fails.
     */
    public static long getLong(final String key, final long defaultValue) {
        return Convert.toLong(get(key), defaultValue);
    }

    /**
     * Retrieves all system properties as a {@link Properties} object.
     *
     * @return A {@link Properties} object containing all system properties.
     */
    public static Properties getProps() {
        return System.getProperties();
    }

    /**
     * Sets a system property. If {@code value} is {@code null}, the property is removed.
     *
     * @param key   The name of the property to set or remove.
     * @param value The value to set for the property, or {@code null} to remove the property.
     */
    public static void set(final String key, final String value) {
        if (null == value) {
            System.clearProperty(key);
        } else {
            System.setProperty(key, value);
        }
    }

    /**
     * Retrieves the Java ClassPath entries, excluding JRE-specific paths.
     *
     * @return An array of strings representing the Java ClassPath entries.
     */
    public static String[] getJavaClassPaths() {
        return get(JAVA_CLASS_PATH).split(get(PATH_SEPARATOR));
    }

    /**
     * Retrieves the user's home directory path.
     *
     * @return The absolute path to the user's home directory.
     */
    public static String getUserHomePath() {
        return get(USER_HOME);
    }

    /**
     * Retrieves the default temporary directory path.
     *
     * @return The absolute path to the temporary directory.
     */
    public static String getTmpDirPath() {
        return get(JAVA_IO_TMPDIR);
    }

    /**
     * Retrieves the name of the Java Virtual Machine (JVM).
     *
     * @return The JVM name.
     */
    static String _getJvmName() {
        return getQuietly(JAVA_VM_NAME);
    }

    /**
     * Retrieves the major version number of the JVM based on the {@code java.specification.version} property. Defaults
     * to 17 if the version cannot be determined or is not explicitly set.
     *
     * @return The major JVM version number.
     */
    static int _getJvmVersion() {
        int jvmVersion = 17;

        String javaSpecVer = getQuietly(JAVA_SPECIFICATION_VERSION);
        if (StringKit.isNotBlank(javaSpecVer)) {
            if (javaSpecVer.startsWith("1.")) {
                javaSpecVer = javaSpecVer.substring(2);
            }
            if (javaSpecVer.indexOf('.') == -1) {
                jvmVersion = Integer.parseInt(javaSpecVer);
            }
        }

        return jvmVersion;
    }

    /**
     * Retrieves attributes for a specified JNDI URI from the naming service. For example, to get DNS attributes, the
     * URI might be {@code "dns:miaixz.org"}.
     *
     * @param uri     The URI string, typically in the format {@code [scheme:][name]/[domain]}.
     * @param attrIds An array of attribute IDs to retrieve.
     * @return An {@link Attributes} object containing the requested attributes.
     * @throws InternalException if a {@link NamingException} occurs during the operation.
     */
    public static Attributes getAttributes(final String uri, final String... attrIds) {
        try {
            return createInitialDirContext(null).getAttributes(uri, attrIds);
        } catch (final NamingException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Creates a new {@link InitialDirContext} instance.
     *
     * @param environment A map of environment properties for the context. If {@code null} or empty, no environment
     *                    properties are used.
     * @return A new {@link InitialDirContext} instance.
     * @throws InternalException if a {@link NamingException} occurs during context creation.
     */
    static InitialDirContext createInitialDirContext(final Map<String, String> environment) {
        try {
            if (MapKit.isEmpty(environment)) {
                return new InitialDirContext();
            }
            return new InitialDirContext(Convert.convert(Hashtable.class, environment));
        } catch (final NamingException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Creates a new {@link InitialContext} instance.
     *
     * @param environment A map of environment properties for the context. If {@code null} or empty, no environment
     *                    properties are used.
     * @return A new {@link InitialContext} instance.
     * @throws InternalException if a {@link NamingException} occurs during context creation.
     */
    static InitialContext createInitialContext(final Map<String, String> environment) {
        try {
            if (MapKit.isEmpty(environment)) {
                return new InitialContext();
            }
            return new InitialContext(Convert.convert(Hashtable.class, environment));
        } catch (final NamingException e) {
            throw new InternalException(e);
        }
    }

}
