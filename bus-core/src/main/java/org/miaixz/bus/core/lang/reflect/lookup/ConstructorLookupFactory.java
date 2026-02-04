/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
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
