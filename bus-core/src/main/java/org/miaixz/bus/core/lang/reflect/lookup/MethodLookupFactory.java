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
 * @since Java 21+
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
