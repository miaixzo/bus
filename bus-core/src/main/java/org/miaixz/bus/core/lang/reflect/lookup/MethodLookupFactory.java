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
package org.miaixz.bus.core.lang.reflect.lookup;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.miaixz.bus.core.lang.exception.InternalException;

/**
 * In JDK 11+, directly calling {@link MethodHandles#lookup()} to obtain a {@link MethodHandles.Lookup} only grants
 * access to public methods. For non-public methods or classes, especially in the context of {@code findSpecial} and
 * {@code unreflectSpecial}, a more privileged lookup is required. This factory utilizes the
 * {@code MethodHandles#privateLookupIn(Class, MethodHandles.Lookup)} method, available since JDK 9, to obtain a lookup
 * object with sufficient access rights.
 * <p>
 * Reference: <a href=
 * "https://blog.csdn.net/u013202238/article/details/108687086">https://blog.csdn.net/u013202238/article/details/108687086</a>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MethodLookupFactory implements LookupFactory {

    /**
     * The {@code privateLookupIn} method obtained via reflection, used for creating privileged lookups.
     */
    private final Method privateLookupInMethod;

    /**
     * Constructs a new {@code MethodLookupFactory}. This constructor attempts to find the {@code privateLookupIn}
     * method, which is essential for creating lookups with private access in JDK 9 and later.
     *
     * @throws IllegalStateException if the {@code privateLookupIn} method is not found, indicating an incompatible JDK
     *                               version (e.g., JDK 8 or earlier).
     */
    public MethodLookupFactory() {
        this.privateLookupInMethod = createJdk9PrivateLookupInMethod();
    }

    /**
     * Attempts to find the {@code privateLookupIn(Class, MethodHandles.Lookup)} method using reflection. This method is
     * available from JDK 9 onwards.
     *
     * @return The {@link Method} object for {@code privateLookupIn}.
     * @throws IllegalStateException if the {@code privateLookupIn} method is not found.
     */
    private static Method createJdk9PrivateLookupInMethod() {
        try {
            return MethodHandles.class.getMethod("privateLookupIn", Class.class, MethodHandles.Lookup.class);
        } catch (final NoSuchMethodException e) {
            // This likely means the JDK version is below 9.
            throw new IllegalStateException(
                    "There is no 'privateLookupIn(Class, Lookup)' method in java.lang.invoke.MethodHandles.", e);
        }
    }

    /**
     * Obtains a {@link MethodHandles.Lookup} instance with private access capabilities for the specified caller class.
     * This method uses the {@code privateLookupIn} method (available from JDK 9+) to achieve the necessary access.
     *
     * @param callerClass The class or interface from which the lookup is being performed. This class will be granted
     *                    private access.
     * @return A {@link MethodHandles.Lookup} object with appropriate access privileges.
     * @throws InternalException if an {@link IllegalAccessException} or {@link InvocationTargetException} occurs during
     *                           the invocation of {@code privateLookupIn}.
     */
    @Override
    public MethodHandles.Lookup lookup(final Class<?> callerClass) {
        try {
            return (MethodHandles.Lookup) privateLookupInMethod
                    .invoke(MethodHandles.class, callerClass, MethodHandles.lookup());
        } catch (final IllegalAccessException e) {
            throw new InternalException(e);
        } catch (final InvocationTargetException e) {
            throw new InternalException(e.getTargetException());
        }
    }

}
