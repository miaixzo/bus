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
package org.miaixz.bus.core.xyz;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.miaixz.bus.core.bean.NullWrapper;
import org.miaixz.bus.core.center.stream.EasyStream;
import org.miaixz.bus.core.lang.*;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.loader.classloader.JarClassLoader;
import org.miaixz.bus.core.lang.reflect.ClassScanner;
import org.miaixz.bus.core.net.url.UrlDecoder;
import org.miaixz.bus.core.text.CharsBacker;

/**
 * Class utility.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ClassKit {

    /**
     * Constructs a new ClassKit. Utility class constructor for static access.
     */
    private ClassKit() {
    }

    /**
     * Map of primitive type names to their corresponding wrapper classes.
     */
    private static final Map<String, Class<?>> PRIMITIVE_WRAPPER_MAP = new HashMap<>();

    /**
     * Gets the type of an object in a null-safe manner.
     *
     * @param <T>    The object type.
     * @param object The object. If null, returns null.
     * @return The object's class, or null if the object is null.
     */
    public static <T> Class<T> getClass(final T object) {
        return ((null == object) ? null : (Class<T>) object.getClass());
    }

    /**
     * Gets the enclosing class. Returns the class in which this class or anonymous class is defined. Returns
     * {@code null} if the class is a top-level class.
     *
     * @param clazz The class.
     * @return The enclosing class.
     */
    public static Class<?> getEnclosingClass(final Class<?> clazz) {
        return null == clazz ? null : clazz.getEnclosingClass();
    }

    /**
     * Gets the package name of the given class.
     *
     * @param clazz The class.
     * @return The package name, or an empty string if the class is in the default package.
     */
    public static String getPackageName(Class<?> clazz) {
        return getPackageName(clazz.getName());
    }

    /**
     * Gets the package name of the given class name.
     *
     * @param className The fully qualified class name.
     * @return The package name, or an empty string if the class is in the default package.
     */
    public static String getPackageName(String className) {
        Assert.notNull(className, "Class name must not be null");
        int lastDotIndex = className.lastIndexOf(Symbol.C_DOT);
        return (lastDotIndex != -1 ? className.substring(0, lastDotIndex) : Normal.EMPTY);
    }

    /**
     * Checks if the class is a top-level class (not an inner, local, or anonymous class).
     *
     * @param clazz The class.
     * @return {@code true} if it is a top-level class.
     */
    public static boolean isTopLevelClass(final Class<?> clazz) {
        if (null == clazz) {
            return false;
        }
        return null == getEnclosingClass(clazz);
    }

    /**
     * Gets the class name of an object.
     *
     * @param object   The object to get the class name from.
     * @param isSimple If true, returns the simple name without the package.
     * @return The class name.
     */
    public static String getClassName(final Object object, final boolean isSimple) {
        if (null == object) {
            return null;
        }
        final Class<?> clazz = object.getClass();
        return getClassName(clazz, isSimple);
    }

    /**
     * Gets the class name. The name does not include the ".class" extension.
     * <p>
     * Example for `ClassKit`:
     * 
     * <pre>
     * isSimple=false: "org.miaixz.bus.core.xyz.ClassKit"
     * isSimple=true: "ClassKit"
     * </pre>
     *
     * @param clazz    The class.
     * @param isSimple If true, returns the simple name without the package.
     * @return The class name.
     */
    public static String getClassName(final Class<?> clazz, final boolean isSimple) {
        if (null == clazz) {
            return null;
        }
        return isSimple ? clazz.getSimpleName() : clazz.getName();
    }

    /**
     * Gets the short version of a fully qualified class name. e.g., `org.miaixz.bus.core.xyz.StringKit` becomes
     * `o.m.b.c.x.StringKit`.
     *
     * @param className The class name.
     * @return The short class name.
     */
    public static String getShortClassName(final String className) {
        final List<String> packages = CharsBacker.split(className, Symbol.DOT);
        if (null == packages || packages.size() < 2) {
            return className;
        }

        final int size = packages.size();
        final StringBuilder result = StringKit.builder();
        result.append(packages.get(0).charAt(0));
        for (int i = 1; i < size - 1; i++) {
            result.append(Symbol.C_DOT).append(packages.get(i).charAt(0));
        }
        result.append(Symbol.C_DOT).append(packages.get(size - 1));
        return result.toString();
    }

    /**
     * Gets an array of classes from an array of objects. If an element in the object array is {@code null}, it is
     * considered `Object.class`.
     *
     * @param objects The object array.
     * @return An array of classes.
     */
    public static Class<?>[] getClasses(final Object... objects) {
        final Class<?>[] classes = new Class<?>[objects.length];
        Object object;
        for (int i = 0; i < objects.length; i++) {
            object = objects[i];
            if (object instanceof NullWrapper) {
                // Custom type for null value
                classes[i] = ((NullWrapper<?>) object).getWrappedClass();
            } else if (null == object) {
                classes[i] = Object.class;
            } else {
                classes[i] = object.getClass();
            }
        }
        return classes;
    }

    /**
     * Checks if the specified class has the same name as the given class name.
     *
     * @param clazz      The class.
     * @param className  The class name (can be fully qualified or simple).
     * @param ignoreCase If true, the comparison is case-insensitive.
     * @return {@code true} if the names match.
     */
    public static boolean equals(final Class<?> clazz, final String className, final boolean ignoreCase) {
        if (null == clazz || StringKit.isBlank(className)) {
            return false;
        }
        if (ignoreCase) {
            return className.equalsIgnoreCase(clazz.getName()) || className.equalsIgnoreCase(clazz.getSimpleName());
        } else {
            return className.equals(clazz.getName()) || className.equals(clazz.getSimpleName());
        }
    }

    /**
     * Scans for all classes with a specific annotation in a given package.
     *
     * @param packageName     The package name.
     * @param annotationClass The annotation class.
     * @return A set of classes.
     * @see ClassScanner#scanPackageByAnnotation(String, Class)
     */
    public static Set<Class<?>> scanPackageByAnnotation(
            final String packageName,
            final Class<? extends Annotation> annotationClass) {
        return ClassScanner.scanPackageByAnnotation(packageName, annotationClass);
    }

    /**
     * Scans for all subclasses or implementations of a given superclass or interface in a package.
     *
     * @param packageName The package name.
     * @param superClass  The superclass or interface.
     * @return A set of classes.
     * @see ClassScanner#scanPackageBySuper(String, Class)
     */
    public static Set<Class<?>> scanPackageBySuper(final String packageName, final Class<?> superClass) {
        return ClassScanner.scanPackageBySuper(packageName, superClass);
    }

    /**
     * Scans all classes in the entire classpath.
     *
     * @return A set of classes.
     * @see ClassScanner#scanPackage()
     */
    public static Set<Class<?>> scanPackage() {
        return ClassScanner.scanPackage();
    }

    /**
     * Scans all classes in a given package.
     *
     * @param packageName The package name.
     * @return A set of classes.
     * @see ClassScanner#scanPackage(String)
     */
    public static Set<Class<?>> scanPackage(final String packageName) {
        return ClassScanner.scanPackage(packageName);
    }

    /**
     * Scans a package for all classes that satisfy a filter.
     *
     * @param packageName The package name.
     * @param classFilter The filter for classes.
     * @return A set of classes.
     */
    public static Set<Class<?>> scanPackage(final String packageName, final Predicate<Class<?>> classFilter) {
        return ClassScanner.scanPackage(packageName, classFilter);
    }

    /**
     * Gets the classpath resources without decoding special characters in the path (e.g., spaces, Chinese characters).
     *
     * @return A set of classpath resource strings.
     */
    public static Set<String> getClassPathResources() {
        return getClassPathResources(false);
    }

    /**
     * Gets the classpath resources.
     *
     * @param isDecode If true, decodes special characters in the path.
     * @return A set of classpath resource strings.
     */
    public static Set<String> getClassPathResources(final boolean isDecode) {
        return getClassPaths(Normal.EMPTY, isDecode);
    }

    /**
     * Gets the classpath resources for a specific package without decoding special characters.
     *
     * @param packageName The package name.
     * @return A set of classpath resource strings.
     */
    public static Set<String> getClassPaths(final String packageName) {
        return getClassPaths(packageName, false);
    }

    /**
     * Gets the classpath resources for a specific package.
     *
     * @param packageName The package name.
     * @param isDecode    If true, decodes special characters in the path.
     * @return A set of classpath resource strings.
     */
    public static Set<String> getClassPaths(final String packageName, final boolean isDecode) {
        final String packagePath = packageName.replace(Symbol.DOT, Symbol.SLASH);
        final Enumeration<URL> resources;
        try {
            resources = getClassLoader().getResources(packagePath);
        } catch (final IOException e) {
            throw new InternalException(e, "Loading classPath [{}] error!", packagePath);
        }
        final Set<String> paths = new HashSet<>();
        String path;
        while (resources.hasMoreElements()) {
            path = resources.nextElement().getPath();
            paths.add(isDecode ? UrlDecoder.decode(path, Charset.defaultCharset()) : path);
        }
        return paths;
    }

    /**
     * Gets the normalized classpath. Decodes special characters.
     *
     * @return The classpath string.
     */
    public static String getClassPath() {
        return getClassPath(false);
    }

    /**
     * Gets the normalized classpath.
     *
     * @param isEncoded If true, returns the encoded path.
     * @return The classpath string.
     */
    public static String getClassPath(final boolean isEncoded) {
        final URL classPathUrl = ResourceKit.getResourceUrl(Normal.EMPTY);
        final String url = isEncoded ? classPathUrl.getPath() : UrlKit.getDecodedPath(classPathUrl);
        return FileKit.normalize(url);
    }

    /**
     * Checks if each class in `types2` is assignable to the corresponding class in `types1`.
     *
     * @param types1 The array of target types.
     * @param types2 The array of source types.
     * @return {@code true} if all types are assignable.
     */
    public static boolean isAllAssignableFrom(final Class<?>[] types1, final Class<?>[] types2) {
        if (ArrayKit.isEmpty(types1) && ArrayKit.isEmpty(types2)) {
            return true;
        }
        if (null == types1 || null == types2) {
            return false;
        }
        if (types1.length != types2.length) {
            return false;
        }

        Class<?> type1;
        Class<?> type2;
        for (int i = 0; i < types1.length; i++) {
            type1 = types1[i];
            type2 = types2[i];
            if (isBasicType(type1) && isBasicType(type2)) {
                if (EnumValue.Type.unWrap(type1) != EnumValue.Type.unWrap(type2)) {
                    return false;
                }
            } else if (!type1.isAssignableFrom(type2)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if a class is a primitive wrapper type.
     *
     * @param clazz The class.
     * @return {@code true} if it is a wrapper type.
     */
    public static boolean isPrimitiveWrapper(final Class<?> clazz) {
        return EnumValue.Type.isPrimitiveWrapper(clazz);
    }

    /**
     * Checks if a class is a basic type (primitive or wrapper).
     *
     * @param clazz The class.
     * @return {@code true} if it is a basic type.
     */
    public static boolean isBasicType(final Class<?> clazz) {
        if (null == clazz) {
            return false;
        }
        return (clazz.isPrimitive() || isPrimitiveWrapper(clazz));
    }

    /**
     * Checks if two types are a primitive/wrapper pair. e.g., `int` and `Integer`.
     *
     * @param class1 The first class.
     * @param class2 The second class.
     * @return {@code true} if they are a primitive/wrapper pair.
     */
    public static boolean isBasicTypeMatch(final Class<?> class1, final Class<?> class2) {
        if (class1 == class2) {
            return true;
        }
        if (null == class1 || null == class2) {
            return false;
        }
        if (class1.isPrimitive() && EnumValue.Type.wrap(class1) == class2) {
            return true;
        }
        return class2.isPrimitive() && EnumValue.Type.wrap(class2) == class1;
    }

    /**
     * Checks if a class is a simple value type or an array of a simple value type.
     *
     * @param clazz The class.
     * @return {@code true} if it is a simple type or an array of a simple type.
     * @see #isSimpleValueType(Class)
     */
    public static boolean isSimpleTypeOrArray(final Class<?> clazz) {
        if (null == clazz) {
            return false;
        }
        return isSimpleValueType(clazz) || (clazz.isArray() && isSimpleValueType(clazz.getComponentType()));
    }

    /**
     * Checks if a class is a simple value type. This includes:
     *
     * <pre>
     * - Primitives and their wrappers
     * - Enums
     * - String and other CharSequence implementations
     * - Number implementations
     * - Date and its subclasses
     * - URI, URL, Locale, Class
     * - JDK8+ temporal types (TemporalAccessor)
     * </pre>
     *
     * @param clazz The class.
     * @return {@code true} if it is a simple value type.
     */
    public static boolean isSimpleValueType(final Class<?> clazz) {
        return isBasicType(clazz) || clazz.isEnum() || CharSequence.class.isAssignableFrom(clazz)
                || Number.class.isAssignableFrom(clazz) || Date.class.isAssignableFrom(clazz) || clazz.equals(URI.class)
                || clazz.equals(URL.class) || clazz.equals(Locale.class) || clazz.equals(Class.class)
                || TemporalAccessor.class.isAssignableFrom(clazz);
    }

    /**
     * Checks if the target type is assignable from the source type. This includes object inheritance, interface
     * implementation, and primitive/wrapper conversions.
     *
     * @param targetType The target type.
     * @param sourceType The source type.
     * @return {@code true} if assignable.
     */
    public static boolean isAssignable(final Class<?> targetType, final Class<?> sourceType) {
        if (null == targetType || null == sourceType) {
            return false;
        }

        // Standard assignability check
        if (targetType.isAssignableFrom(sourceType)) {
            return true;
        }

        // Primitive/wrapper conversion check
        if (targetType.isPrimitive()) {
            return targetType.equals(EnumValue.Type.unWrap(sourceType));
        } else {
            final Class<?> resolvedWrapper = EnumValue.Type.wrap(sourceType, true);
            return resolvedWrapper != null && targetType.isAssignableFrom(resolvedWrapper);
        }
    }

    /**
     * Checks if the given class implements {@link Serializable}.
     *
     * @param clazz The class.
     * @return {@code true} if it is serializable.
     */
    public static boolean isSerializable(final Class<?> clazz) {
        return Serializable.class.isAssignableFrom(clazz);
    }

    /**
     * Checks if a class is a "normal" class (not an interface, abstract class, enum, array, annotation, synthetic, or
     * primitive type).
     *
     * @param clazz The class.
     * @return {@code true} if it is a normal class.
     */
    public static boolean isNormalClass(final Class<?> clazz) {
        return null != clazz && !clazz.isInterface() && !ModifierKit.isAbstract(clazz) && !clazz.isEnum()
                && !clazz.isArray() && !clazz.isAnnotation() && !clazz.isSynthetic() && !clazz.isPrimitive();
    }

    /**
     * Checks if the class is an enum type.
     *
     * @param clazz The class.
     * @return {@code true} if it is an enum.
     */
    public static boolean isEnum(final Class<?> clazz) {
        return null != clazz && clazz.isEnum();
    }

    /**
     * Gets the first generic type argument of the given class.
     *
     * @param clazz The class to inspect.
     * @return The {@link Class} of the first generic type argument.
     */
    public static Class<?> getTypeArgument(final Class<?> clazz) {
        return getTypeArgument(clazz, 0);
    }

    /**
     * Gets the generic type argument of a class at a specified index.
     *
     * @param clazz The class to inspect.
     * @param index The index of the generic type argument.
     * @return The {@link Class} of the generic type argument.
     */
    public static Class<?> getTypeArgument(final Class<?> clazz, final int index) {
        final Type argumentType = TypeKit.getTypeArgument(clazz, index);
        return TypeKit.getClass(argumentType);
    }

    /**
     * Gets the package name of the given class.
     *
     * @param clazz The class.
     * @return The package name.
     */
    public static String getPackage(final Class<?> clazz) {
        if (clazz == null) {
            return Normal.EMPTY;
        }
        final String className = clazz.getName();
        final int packageEndIndex = className.lastIndexOf(Symbol.DOT);
        if (packageEndIndex == -1) {
            return Normal.EMPTY;
        }
        return className.substring(0, packageEndIndex);
    }

    /**
     * Gets the package path of the given class (dots replaced with slashes).
     *
     * @param clazz The class.
     * @return The package path.
     */
    public static String getPackagePath(final Class<?> clazz) {
        return getPackage(clazz).replace(Symbol.C_DOT, Symbol.C_SLASH);
    }

    /**
     * Gets the default value for a given type. Returns 0 for primitive numeric types, false for boolean, and null for
     * object types.
     *
     * @param clazz The class.
     * @return The default value.
     */
    public static Object getDefaultValue(final Class<?> clazz) {
        if (clazz.isPrimitive()) {
            return getPrimitiveDefaultValue(clazz);
        }
        return null;
    }

    /**
     * Gets the default value for a given primitive type.
     *
     * @param clazz The primitive class.
     * @return The default value.
     */
    public static Object getPrimitiveDefaultValue(final Class<?> clazz) {
        if (long.class == clazz) {
            return 0L;
        } else if (int.class == clazz) {
            return 0;
        } else if (short.class == clazz) {
            return (short) 0;
        } else if (char.class == clazz) {
            return (char) 0;
        } else if (byte.class == clazz) {
            return (byte) 0;
        } else if (double.class == clazz) {
            return 0D;
        } else if (float.class == clazz) {
            return 0f;
        } else if (boolean.class == clazz) {
            return false;
        }
        return null;
    }

    /**
     * Gets an array of default values for an array of classes.
     *
     * @param classes The classes.
     * @return An array of default values.
     */
    public static Object[] getDefaultValues(final Class<?>... classes) {
        final Object[] values = new Object[classes.length];
        for (int i = 0; i < classes.length; i++) {
            values[i] = getDefaultValue(classes[i]);
        }
        return values;
    }

    /**
     * Checks if a class is a JDK class, based on its package name (`java.` or `javax.`) or if its ClassLoader is null.
     *
     * @param clazz The class to check.
     * @return {@code true} if it is a JDK class.
     */
    public static boolean isJdkClass(final Class<?> clazz) {
        final Package objectPackage = clazz.getPackage();
        if (null == objectPackage) {
            return false;
        }
        final String objectPackageName = objectPackage.getName();
        return objectPackageName.startsWith("java.") || objectPackageName.startsWith("javax.")
                || clazz.getClassLoader() == null;
    }

    /**
     * Gets the location (URL) of the code source for a class. This will be the directory or JAR file from which the
     * class was loaded.
     *
     * @param clazz The class.
     * @return The location URL.
     */
    public static URL getLocation(final Class<?> clazz) {
        if (null == clazz) {
            return null;
        }
        return clazz.getProtectionDomain().getCodeSource().getLocation();
    }

    /**
     * Gets the path of the code source for a class.
     *
     * @param clazz The class.
     * @return The location path.
     */
    public static String getLocationPath(final Class<?> clazz) {
        final URL location = getLocation(clazz);
        if (null == location) {
            return null;
        }
        return location.getPath();
    }

    /**
     * Checks if a class with the specified name is present on the classpath.
     *
     * @param className The fully qualified class name.
     * @param loader    The {@link ClassLoader}.
     * @return {@code true} if the class is present.
     */
    public static boolean isForName(final String className, final ClassLoader loader) {
        try {
            return null != forName(className, false, loader);
        } catch (final Exception e) {
            return false;
        }
    }

    /**
     * Loads a class by its name. This method supports:
     * <ul>
     * <li>Replacing "/" with "."</li>
     * <li>Automatically finding inner classes (e.g., `java.lang.Thread.State` becomes `java.lang.Thread$State`)</li>
     * </ul>
     *
     * @param name   The class name.
     * @param loader The {@link ClassLoader} (null for default).
     * @return The class, or throws an exception if not found.
     * @see Class#forName(String, boolean, ClassLoader)
     */
    public static Class<?> forName(String name, ClassLoader loader) {
        return forName(name, false, loader);
    }

    /**
     * Loads a class by its name. This method supports:
     * <ul>
     * <li>Replacing "/" with "."</li>
     * <li>Automatically finding inner classes (e.g., `java.lang.Thread.State` becomes `java.lang.Thread$State`)</li>
     * </ul>
     *
     * @param name          The class name.
     * @param isInitialized Whether to initialize the class.
     * @param loader        The {@link ClassLoader} (null for default).
     * @return The class, or throws an exception if not found.
     * @see Class#forName(String, boolean, ClassLoader)
     */
    public static Class<?> forName(String name, final boolean isInitialized, ClassLoader loader) {
        Assert.notNull(name, "Name must not be null");

        name = name.replace(Symbol.C_SLASH, Symbol.C_DOT);

        // "java.lang.String[]" style arrays
        if (name.endsWith(Symbol.BRACKET)) {
            String elementClassName = name.substring(0, name.length() - Symbol.BRACKET.length());
            Class<?> elementClass = forName(elementClassName, loader);
            return Array.newInstance(elementClass, 0).getClass();
        }

        // "[Ljava.lang.String;" style arrays
        if (name.startsWith(Symbol.NON_PREFIX) && name.endsWith(Symbol.SEMICOLON)) {
            String elementName = name.substring(Symbol.NON_PREFIX.length(), name.length() - 1);
            Class<?> elementClass = forName(elementName, loader);
            return Array.newInstance(elementClass, 0).getClass();
        }

        // "[[I" or "[[Ljava.lang.String;" style arrays
        if (name.startsWith(Symbol.BRACKET_LEFT)) {
            String elementName = name.substring(Symbol.BRACKET_LEFT.length());
            Class<?> elementClass = forName(elementName, loader);
            return Array.newInstance(elementClass, 0).getClass();
        }

        if (null == loader) {
            loader = getClassLoader();
        }

        try {
            return Class.forName(name, isInitialized, loader);
        } catch (final ClassNotFoundException ex) {
            // Try to find as an inner class
            final Class<?> clazz = forNameInnerClass(name, isInitialized, loader);
            if (null == clazz) {
                throw new InternalException(ex);
            }
            return clazz;
        }
    }

    /**
     * Gets all superclasses of the given class, excluding the class itself.
     *
     * @param clazz The class.
     * @return A list of all superclasses, or an empty list if none.
     */
    public static List<Class<?>> getSuperClasses(final Class<?> clazz) {
        if (clazz == null) {
            return Collections.emptyList();
        }
        final List<Class<?>> superclasses = new ArrayList<>();
        traverseTypeHierarchy(clazz, t -> !t.isInterface(), superclasses::add, false);
        return superclasses;
    }

    /**
     * Gets all interfaces implemented by the given class and its superclasses.
     *
     * @param cls The class to inspect.
     * @return A list of all implemented interfaces.
     */
    public static List<Class<?>> getInterfaces(final Class<?> cls) {
        if (cls == null) {
            return Collections.emptyList();
        }
        final List<Class<?>> interfaces = new ArrayList<>();
        traverseTypeHierarchy(cls, t -> true, t -> {
            if (t.isInterface()) {
                interfaces.add(t);
            }
        }, false);
        return interfaces;
    }

    /**
     * Traverses the type hierarchy of a class (breadth-first) until the terminator predicate returns false.
     *
     * @param root       The root class.
     * @param terminator A predicate to stop traversal.
     */
    public static void traverseTypeHierarchyWhile(final Class<?> root, final Predicate<Class<?>> terminator) {
        traverseTypeHierarchyWhile(root, t -> true, terminator);
    }

    /**
     * Traverses the type hierarchy of a class (breadth-first) until the terminator predicate returns false.
     *
     * @param root       The root class.
     * @param filter     A filter to exclude types and their hierarchies.
     * @param terminator A predicate to stop traversal.
     */
    public static void traverseTypeHierarchyWhile(
            final Class<?> root,
            final Predicate<Class<?>> filter,
            final Predicate<Class<?>> terminator) {
        EasyStream.iterateHierarchies(root, ClassKit::getNextTypeHierarchies, filter).takeWhile(terminator).exec();
    }

    /**
     * Traverses the type hierarchy of a class (breadth-first).
     *
     * @param root        The root class.
     * @param filter      A filter to exclude types and their hierarchies.
     * @param consumer    An operation to perform on each visited type.
     * @param includeRoot If true, the root class itself is included in the traversal.
     */
    public static void traverseTypeHierarchy(
            final Class<?> root,
            final Predicate<Class<?>> filter,
            final Consumer<Class<?>> consumer,
            final boolean includeRoot) {
        Objects.requireNonNull(root);
        Objects.requireNonNull(filter);
        Objects.requireNonNull(consumer);
        final Function<Class<?>, Collection<Class<?>>> function = t -> {
            if (includeRoot || !root.equals(t)) {
                consumer.accept(t);
            }
            return getNextTypeHierarchies(t);
        };
        EasyStream.iterateHierarchies(root, function, filter).exec();
    }

    /**
     * Gets the appropriate ClassLoader in the following order:
     * <ol>
     * <li>The caller's ContextClassLoader</li>
     * <li>The current thread's ContextClassLoader</li>
     * <li>The ClassLoader of this class (ClassKit)</li>
     * <li>The system ClassLoader</li>
     * </ol>
     *
     * @return The ClassLoader.
     */
    public static ClassLoader getClassLoader() {
        ClassLoader classLoader = CallerKit.getCallers().getClassLoader();
        if (null == classLoader) {
            classLoader = getContextClassLoader();
        }
        if (classLoader == null) {
            classLoader = ClassKit.class.getClassLoader();
            if (null == classLoader) {
                classLoader = getSystemClassLoader();
            }
        }
        return classLoader;
    }

    /**
     * Gets the caller's {@link ClassLoader}.
     *
     * @return The caller's ClassLoader.
     */
    public static ClassLoader getCallerClassLoader() {
        return CallerKit.getCallers().getClassLoader();
    }

    /**
     * Gets the current thread's Context ClassLoader.
     *
     * @return The current thread's context ClassLoader.
     * @see Thread#getContextClassLoader()
     */
    public static ClassLoader getContextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    /**
     * Gets the system {@link ClassLoader}.
     *
     * @return The system ClassLoader.
     * @see ClassLoader#getSystemClassLoader()
     */
    public static ClassLoader getSystemClassLoader() {
        return ClassLoader.getSystemClassLoader();
    }

    /**
     * Creates a new {@link JarClassLoader} to load classes and JARs from a directory.
     *
     * @param jarOrDir A JAR file or a directory containing JARs and class files.
     * @return A {@link JarClassLoader}.
     */
    public static JarClassLoader getJarClassLoader(final File jarOrDir) {
        return JarClassLoader.load(jarOrDir);
    }

    /**
     * Loads a class by name using the default ClassLoader and initializes it.
     *
     * @param <T>  The type of the class.
     * @param name The class name.
     * @return The corresponding class.
     * @throws InternalException wrapping a {@link ClassNotFoundException}.
     */
    public static <T> Class<T> loadClass(final String name) throws InternalException {
        return loadClass(name, true);
    }

    /**
     * Loads a class by name using the default ClassLoader.
     *
     * @param <T>           The type of the class.
     * @param name          The class name.
     * @param isInitialized If true, the class will be initialized.
     * @return The corresponding class.
     * @throws InternalException wrapping a {@link ClassNotFoundException}.
     */
    public static <T> Class<T> loadClass(final String name, final boolean isInitialized) throws InternalException {
        return loadClass(name, isInitialized, null);
    }

    /**
     * Loads a class by name with an optional ClassLoader.
     *
     * @param <T>           The type of the class.
     * @param name          The class name.
     * @param classLoader   The {@link ClassLoader} (uses default if null).
     * @param isInitialized If true, the class will be initialized.
     * @return The corresponding class.
     * @throws InternalException wrapping a {@link ClassNotFoundException}.
     */
    public static <T> Class<T> loadClass(final String name, final boolean isInitialized, final ClassLoader classLoader)
            throws InternalException {
        return (Class<T>) ReflectKit.nameToClass(name, isInitialized, classLoader);
    }

    /**
     * Loads an external class from a JAR or directory.
     *
     * @param jarOrDir A JAR file or a directory.
     * @param name     The class name.
     * @return The class.
     */
    public static Class<?> loadClass(final File jarOrDir, final String name) {
        try {
            return getJarClassLoader(jarOrDir).loadClass(name);
        } catch (final ClassNotFoundException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Checks if a class is present on the classpath using the default ClassLoader.
     *
     * @param className The class name.
     * @return {@code true} if the class is present.
     */
    public static boolean isPresent(final String className) {
        return isPresent(className, null);
    }

    /**
     * Checks if a class is present on the classpath using a specific ClassLoader.
     *
     * @param className   The class name.
     * @param classLoader The {@link ClassLoader}.
     * @return {@code true} if the class is present.
     */
    public static boolean isPresent(final String className, final ClassLoader classLoader) {
        try {
            loadClass(className, false, classLoader);
            return true;
        } catch (final Throwable ex) {
            return false;
        }
    }

    /**
     * Converts a primitive class to its corresponding wrapper class.
     *
     * @param cls The class to convert.
     * @return The wrapper class, or the original class if it's not a primitive.
     */
    public static Class<?> primitiveToWrapper(final Class<?> cls) {
        Class<?> convertedClass = cls;
        if (null != cls && cls.isPrimitive()) {
            convertedClass = PRIMITIVE_WRAPPER_MAP.get(cls);
        }
        return convertedClass;
    }

    /**
     * Tries to load a class by name, attempting to resolve it as an inner class if necessary.
     *
     * @param name          The class name.
     * @param isInitialized If true, initialize the class.
     * @param classLoader   The ClassLoader.
     * @return The class, or null if not found.
     */
    private static Class<?> forNameInnerClass(String name, final boolean isInitialized, final ClassLoader classLoader) {
        int lastDotIndex = name.lastIndexOf(Symbol.C_DOT);
        Class<?> clazz = null;
        while (lastDotIndex > 0) {
            if (!Character.isUpperCase(name.charAt(lastDotIndex + 1))) {
                break;
            }
            name = name.substring(0, lastDotIndex) + Symbol.C_DOLLAR + name.substring(lastDotIndex + 1);
            try {
                clazz = Class.forName(name, isInitialized, classLoader);
                break;
            } catch (final ClassNotFoundException ignore) {
                // ignore and try next level
            }
            lastDotIndex = name.lastIndexOf(Symbol.C_DOT);
        }
        return clazz;
    }

    /**
     * Gets the direct superclass and implemented interfaces of a class.
     *
     * @param clazz The class.
     * @return A set containing the superclass and interfaces.
     */
    private static Set<Class<?>> getNextTypeHierarchies(final Class<?> clazz) {
        final Set<Class<?>> next = new LinkedHashSet<>();
        final Class<?> superclass = clazz.getSuperclass();
        if (Objects.nonNull(superclass)) {
            next.add(superclass);
        }
        next.addAll(Arrays.asList(clazz.getInterfaces()));
        return next;
    }

}
