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
package org.miaixz.bus.core.lang.reflect.lookup;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;

/**
 * In JDK 8, directly calling {@link MethodHandles#lookup()} to obtain a {@link MethodHandles.Lookup} may lead to
 * permission issues ("no private access for invokespecial") when invoking {@code findSpecial} and
 * {@code unreflectSpecial}. This factory addresses this by using reflection to create a {@link MethodHandles.Lookup}
 * instance with appropriate access rights, specifically for JDK 8 and earlier versions.
 *
 * <p>
 * Reference: <a href=
 * "https://blog.csdn.net/u013202238/article/details/108687086">https://blog.csdn.net/u013202238/article/details/108687086</a>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ConstructorLookupFactory implements LookupFactory {

    /**
     * Allowed lookup modes for the {@link MethodHandles.Lookup} constructor. This combination grants private,
     * protected, package, and public access.
     */
    private static final int ALLOWED_MODES = MethodHandles.Lookup.PRIVATE | MethodHandles.Lookup.PROTECTED
            | MethodHandles.Lookup.PACKAGE | MethodHandles.Lookup.PUBLIC;

    /**
     * The constructor for {@link MethodHandles.Lookup} obtained via reflection.
     */
    private final Constructor<MethodHandles.Lookup> lookupConstructor;

    /**
     * Constructs a new {@code ConstructorLookupFactory}. This constructor attempts to find and make accessible the
     * private constructor {@code MethodHandles.Lookup(Class, int)} using reflection.
     *
     * @throws IllegalStateException if the {@code Lookup(Class, int)} constructor is not found, indicating an
     *                               incompatible JDK version (e.g., a version where this constructor is not available
     *                               or has a different signature).
     */
    public ConstructorLookupFactory() {
        this.lookupConstructor = createLookupConstructor();
    }

    /**
     * Attempts to find and make accessible the private constructor {@code MethodHandles.Lookup(Class, int)}. This
     * constructor is typically used in JDK 8 and earlier to create a {@code Lookup} instance with full access
     * privileges.
     *
     * @return The accessible {@link Constructor} for {@code MethodHandles.Lookup}.
     * @throws IllegalStateException if the constructor is not found.
     */
    private static Constructor<MethodHandles.Lookup> createLookupConstructor() {
        final Constructor<MethodHandles.Lookup> constructor;
        try {
            constructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);
        } catch (final NoSuchMethodException e) {
            // This likely means the JDK version is below 8 or the constructor signature has changed.
            throw new IllegalStateException(
                    "There is no 'Lookup(Class, int)' constructor in java.lang.invoke.MethodHandles.", e);
        }
        constructor.setAccessible(true);
        return constructor;
    }

    /**
     * Obtains a {@link MethodHandles.Lookup} instance with full access capabilities for the specified caller class.
     * This method uses the reflected constructor {@code MethodHandles.Lookup(Class, int)} to achieve the necessary
     * access.
     *
     * @param callerClass The class or interface from which the lookup is being performed. This class will be granted
     *                    private access.
     * @return A {@link MethodHandles.Lookup} object with appropriate access privileges.
     * @throws IllegalStateException if an error occurs during the instantiation of {@code MethodHandles.Lookup}.
     */
    @Override
    public MethodHandles.Lookup lookup(final Class<?> callerClass) {
        try {
            return lookupConstructor.newInstance(callerClass, ALLOWED_MODES);
        } catch (final Exception e) {
            throw new IllegalStateException("no 'Lookup(Class, int)' method in java.lang.invoke.MethodHandles.", e);
        }
    }

}
