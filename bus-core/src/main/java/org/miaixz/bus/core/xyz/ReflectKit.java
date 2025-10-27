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

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;

import org.miaixz.bus.core.center.map.TripletTable;
import org.miaixz.bus.core.center.map.reference.WeakConcurrentMap;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.reflect.creator.DefaultObjectCreator;
import org.miaixz.bus.core.lang.reflect.creator.PossibleObjectCreator;
import org.miaixz.bus.core.text.StringTrimer;

/**
 * Reflection utility class.
 * <p>
 * This class has been refactored, and many of its methods have been moved to {@link FieldKit}, {@link MethodKit},
 * {@link ModifierKit}, etc.
 *
 * JVM type descriptors:
 * <ul>
 * <li>byte = B</li>
 * <li>char = C</li>
 * <li>double = D</li>
 * <li>long = J</li>
 * <li>short = S</li>
 * <li>boolean = Z</li>
 * <li>void = V</li>
 * <li>Object types start with 'L' and end with ';', e.g., Ljava/lang/Object;</li>
 * <li>Array types are prefixed with '[', e.g., java.lang.String[][] = [[Ljava/lang/String;</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ReflectKit {

    /**
     * JVM type descriptor for void (V).
     */
    public static final char JVM_VOID = 'V';

    /**
     * JVM type descriptor for boolean (Z).
     */
    public static final char JVM_BOOLEAN = 'Z';

    /**
     * JVM type descriptor for byte (B).
     */
    public static final char JVM_BYTE = 'B';

    /**
     * JVM type descriptor for char (C).
     */
    public static final char JVM_CHAR = 'C';

    /**
     * JVM type descriptor for double (D).
     */
    public static final char JVM_DOUBLE = 'D';

    /**
     * JVM type descriptor for float (F).
     */
    public static final char JVM_FLOAT = 'F';

    /**
     * JVM type descriptor for int (I).
     */
    public static final char JVM_INT = 'I';

    /**
     * JVM type descriptor for long (J).
     */
    public static final char JVM_LONG = 'J';

    /**
     * JVM type descriptor for short (S).
     */
    public static final char JVM_SHORT = 'S';

    /**
     * A table mapping the 9 primitive types to their descriptors and names.
     */
    private static final TripletTable<Class<?>, Character, String> PRIMITIVE_TABLE = new TripletTable<>(9);
    /**
     * Constructor cache.
     */
    private static final WeakConcurrentMap<Class<?>, Constructor<?>[]> CONSTRUCTORS_CACHE = new WeakConcurrentMap<>();

    static {
        PRIMITIVE_TABLE.put(void.class, JVM_VOID, "void");
        PRIMITIVE_TABLE.put(boolean.class, JVM_BOOLEAN, "boolean");
        PRIMITIVE_TABLE.put(byte.class, JVM_BYTE, "byte");
        PRIMITIVE_TABLE.put(char.class, JVM_CHAR, "char");
        PRIMITIVE_TABLE.put(double.class, JVM_DOUBLE, "double");
        PRIMITIVE_TABLE.put(float.class, JVM_FLOAT, "float");
        PRIMITIVE_TABLE.put(int.class, JVM_INT, "int");
        PRIMITIVE_TABLE.put(long.class, JVM_LONG, "long");
        PRIMITIVE_TABLE.put(short.class, JVM_SHORT, "short");
    }

    /**
     * Converts a class descriptor string to a `Class` object.
     * 
     * <pre>{@code
     * "[Z" => boolean[].class
     * "[[Ljava/util/Map;" => java.util.Map[][].class
     * }</pre>
     *
     * @param desc The class descriptor.
     * @return The `Class` object.
     * @throws InternalException if the class is not found.
     */
    public static Class<?> descToClass(final String desc) throws InternalException {
        return descToClass(desc, true, null);
    }

    /**
     * Converts a class descriptor string to a `Class` object.
     *
     * @param desc          The class descriptor.
     * @param isInitialized Whether to initialize the class.
     * @param cl            The `ClassLoader`.
     * @return The `Class` object.
     * @throws InternalException if the class is not found.
     */
    public static Class<?> descToClass(String desc, final boolean isInitialized, final ClassLoader cl)
            throws InternalException {
        Assert.notNull(desc, "Name must not be null");
        final char firstChar = desc.charAt(0);
        final Class<?> clazz = PRIMITIVE_TABLE.getLeftByMiddle(firstChar);
        if (null != clazz) {
            return clazz;
        }

        desc = StringKit.trim(desc, StringTrimer.TrimMode.SUFFIX, (c) -> Symbol.C_SLASH == c || Symbol.C_DOT == c);

        if ('L' == firstChar) {
            // "Ljava/lang/Object;" ==> "java.lang.Object"
            desc = desc.substring(1, desc.length() - 1);
        }

        return ClassKit.forName(desc, isInitialized, cl);
    }

    /**
     * Gets the JVM descriptor for a class.
     * 
     * <pre>{@code
     * getDesc(boolean.class)       // Z
     * getDesc(Boolean.class)       // Ljava/lang/Boolean;
     * getDesc(double[][][].class)  // [[[D
     * }</pre>
     *
     * @param c The class.
     * @return The descriptor string.
     */
    public static String getDesc(Class<?> c) {
        final StringBuilder ret = new StringBuilder();
        while (c.isArray()) {
            ret.append('[');
            c = c.getComponentType();
        }
        if (c.isPrimitive()) {
            final Character desc = PRIMITIVE_TABLE.getMiddleByLeft(c);
            if (null != desc) {
                ret.append(desc.charValue());
            }
        } else {
            ret.append('L');
            ret.append(c.getName().replace('.', '/'));
            ret.append(Symbol.C_SEMICOLON);
        }
        return ret.toString();
    }

    /**
     * Gets the descriptor for a method or constructor.
     *
     * @param methodOrConstructor The method or constructor.
     * @param appendName          Whether to include the method name.
     * @return The descriptor string.
     */
    public static String getDesc(final Executable methodOrConstructor, final boolean appendName) {
        final StringBuilder ret = new StringBuilder();
        if (appendName && methodOrConstructor instanceof Method) {
            ret.append(methodOrConstructor.getName());
        }
        ret.append(Symbol.C_PARENTHESE_LEFT);

        final Class<?>[] parameterTypes = methodOrConstructor.getParameterTypes();
        for (final Class<?> parameterType : parameterTypes) {
            ret.append(getDesc(parameterType));
        }

        ret.append(Symbol.C_PARENTHESE_RIGHT);
        if (methodOrConstructor instanceof Method) {
            ret.append(getDesc(((Method) methodOrConstructor).getReturnType()));
        } else {
            ret.append('V');
        }

        return ret.toString();
    }

    /**
     * Gets the name of a class. For arrays, returns "xxx[]" format.
     *
     * @param c The class.
     * @return The class name.
     */
    public static String getName(Class<?> c) {
        if (c.isArray()) {
            final StringBuilder sb = new StringBuilder();
            do {
                sb.append("[]");
                c = c.getComponentType();
            } while (c.isArray());
            return c.getName() + sb;
        }
        return c.getName();
    }

    /**
     * Gets the name representation of a constructor or method.
     *
     * @param executable The method or constructor.
     * @return The name string.
     */
    public static String getName(final Executable executable) {
        final StringBuilder ret = new StringBuilder(Symbol.PARENTHESE_LEFT);
        if (executable instanceof Method) {
            ret.append(getName(((Method) executable).getReturnType())).append(Symbol.C_SPACE);
        }
        final Class<?>[] parameterTypes = executable.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            if (i > 0) {
                ret.append(Symbol.C_COMMA);
            }
            ret.append(getName(parameterTypes[i]));
        }
        ret.append(Symbol.C_PARENTHESE_RIGHT);
        return ret.toString();
    }

    /**
     * Converts a class name to a `Class` object.
     *
     * @param name          The class name.
     * @param isInitialized Whether to initialize the class.
     * @param cl            The ClassLoader.
     * @return The `Class` instance.
     */
    public static Class<?> nameToClass(String name, final boolean isInitialized, final ClassLoader cl) {
        Assert.notNull(name, "Name must not be null");
        name = StringKit.trim(name, StringTrimer.TrimMode.SUFFIX, (c) -> Symbol.C_SLASH == c || Symbol.C_DOT == c);

        int c = 0;
        final int index = name.indexOf('[');
        if (index > 0) {
            c = (name.length() - index) / 2;
            name = name.substring(0, index);
        }

        if (c > 0) {
            final StringBuilder sb = new StringBuilder();
            while (c-- > 0) {
                sb.append('[');
            }
            final Class<?> clazz = PRIMITIVE_TABLE.getLeftByRight(name);
            if (null != clazz) {
                sb.append(PRIMITIVE_TABLE.getMiddleByLeft(clazz).charValue());
            } else {
                sb.append('L').append(name).append(Symbol.C_SEMICOLON);
            }
            name = sb.toString();
        } else {
            final Class<?> clazz = PRIMITIVE_TABLE.getLeftByRight(name);
            if (null != clazz) {
                return clazz;
            }
        }

        return ClassKit.forName(name, isInitialized, cl);
    }

    /**
     * Converts a class name to a JVM descriptor.
     *
     * @param name The class name.
     * @return The descriptor string.
     */
    public static String nameToDesc(String name) {
        final StringBuilder sb = new StringBuilder();
        int c = 0;
        final int index = name.indexOf('[');
        if (index > 0) {
            c = (name.length() - index) / 2;
            name = name.substring(0, index);
        }
        while (c-- > 0) {
            sb.append('[');
        }
        final Class<?> clazz = PRIMITIVE_TABLE.getLeftByRight(name);
        if (null != clazz) {
            sb.append(PRIMITIVE_TABLE.getMiddleByLeft(clazz).charValue());
        } else {
            sb.append('L').append(name.replace(Symbol.C_DOT, Symbol.C_SLASH)).append(Symbol.C_SEMICOLON);
        }
        return sb.toString();
    }

    /**
     * Converts a JVM descriptor to a class name.
     *
     * @param desc The descriptor string.
     * @return The class name.
     */
    public static String descToName(final String desc) {
        final StringBuilder sb = new StringBuilder();
        int c = desc.lastIndexOf('[') + 1;
        if (desc.length() == c + 1) {
            final char descChar = desc.charAt(c);
            final Class<?> clazz = PRIMITIVE_TABLE.getLeftByMiddle(descChar);
            if (null != clazz) {
                sb.append(PRIMITIVE_TABLE.getRightByLeft(clazz));
            } else {
                throw new InternalException("Unsupported primitive desc: {}", desc);
            }
        } else {
            sb.append(desc.substring(c + 1, desc.length() - 1).replace(Symbol.C_SLASH, Symbol.C_DOT));
        }
        while (c-- > 0) {
            sb.append("[]");
        }
        return sb.toString();
    }

    /**
     * Gets the code base location for a class.
     *
     * @param clazz The class.
     * @return The code base path.
     */
    public static String getCodeBase(final Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        final ProtectionDomain domain = clazz.getProtectionDomain();
        if (domain == null) {
            return null;
        }
        final CodeSource source = domain.getCodeSource();
        if (source == null) {
            return null;
        }
        final URL location = source.getLocation();
        if (location == null) {
            return null;
        }
        return location.getFile();
    }

    /**
     * Sets an `AccessibleObject` to be accessible, suppressing any exceptions.
     *
     * @param <T>              The type of the `AccessibleObject`.
     * @param accessibleObject The object to make accessible.
     * @return The accessible object.
     */
    public static <T extends AccessibleObject> T setAccessibleQuietly(final T accessibleObject) {
        try {
            setAccessible(accessibleObject);
        } catch (final RuntimeException ignore) {
            // ignore
        }
        return accessibleObject;
    }

    /**
     * Sets an `AccessibleObject` to be accessible. Note: This may fail on JDK 9+ unless the module is opened.
     *
     * @param <T>              The type of the `AccessibleObject`.
     * @param accessibleObject The object to make accessible.
     * @return The accessible object.
     * @throws SecurityException if access is denied.
     */
    public static <T extends AccessibleObject> T setAccessible(final T accessibleObject) throws SecurityException {
        if (null != accessibleObject && !accessibleObject.isAccessible()) {
            accessibleObject.setAccessible(true);
        }
        return accessibleObject;
    }

    /**
     * Finds a constructor with matching parameter types.
     *
     * @param <T>            The object type.
     * @param clazz          The class.
     * @param parameterTypes The parameter types.
     * @return The `Constructor`, or `null` if not found.
     */
    public static <T> Constructor<T> getConstructor(final Class<T> clazz, final Class<?>... parameterTypes) {
        if (null == clazz) {
            return null;
        }
        final Constructor<?>[] constructors = getConstructors(clazz);
        Class<?>[] pts;
        for (final Constructor<?> constructor : constructors) {
            pts = constructor.getParameterTypes();
            if (ClassKit.isAllAssignableFrom(pts, parameterTypes)) {
                ReflectKit.setAccessible(constructor);
                return (Constructor<T>) constructor;
            }
        }
        return null;
    }

    /**
     * Gets all constructors of a class.
     *
     * @param <T>       The type of the object.
     * @param beanClass The class.
     * @return An array of constructors.
     * @throws SecurityException if access is denied.
     */
    public static <T> Constructor<T>[] getConstructors(final Class<T> beanClass) throws SecurityException {
        Assert.notNull(beanClass);
        return (Constructor<T>[]) CONSTRUCTORS_CACHE
                .computeIfAbsent(beanClass, (key) -> getConstructorsDirectly(beanClass));
    }

    /**
     * Gets all constructors of a class directly via reflection (no cache).
     *
     * @param beanClass The class.
     * @return An array of constructors.
     * @throws SecurityException if access is denied.
     */
    public static Constructor<?>[] getConstructorsDirectly(final Class<?> beanClass) throws SecurityException {
        return beanClass.getDeclaredConstructors();
    }

    /**
     * Instantiates an object from its class name. The class must have a no-arg constructor.
     *
     * @param <T>   The object type.
     * @param clazz The class name.
     * @return The new instance.
     * @throws InternalException if instantiation fails.
     */
    public static <T> T newInstance(final String clazz) throws InternalException {
        return (T) DefaultObjectCreator.of(clazz).create();
    }

    /**
     * Instantiates an object from its class and constructor arguments.
     *
     * @param <T>   The object type.
     * @param clazz The class.
     * @param args  The constructor arguments.
     * @return The new instance.
     * @throws InternalException if instantiation fails.
     */
    public static <T> T newInstance(final Class<T> clazz, final Object... args) throws InternalException {
        return DefaultObjectCreator.of(clazz, args).create();
    }

    /**
     * Tries to instantiate a class by iterating through its constructors.
     *
     * @param <T>  The object type.
     * @param type The class to instantiate.
     * @return The new instance, or `null` on failure.
     */
    public static <T> T newInstanceIfPossible(final Class<T> type) {
        return PossibleObjectCreator.of(type).create();
    }

}
