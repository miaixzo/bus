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

/**
 * {@link MethodHandles.Lookup} factory for creating {@link MethodHandles.Lookup} objects. A
 * {@link MethodHandles.Lookup} is a method handle lookup object used to find method handles that match a given method
 * name and method type within a specified class.
 *
 * <p>
 * Reference: <a href=
 * "https://blog.csdn.net/u013202238/article/details/108687086">https://blog.csdn.net/u013202238/article/details/108687086</a>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface LookupFactory {

    /**
     * In JDK 8, directly calling {@link MethodHandles#lookup()} to obtain a {@link MethodHandles.Lookup} may lead to
     * permission issues ("no private access for invokespecial") when invoking {@code findSpecial} and
     * {@code unreflectSpecial}. Therefore, this method provides a way to obtain a {@code Lookup} instance that works
     * correctly across JDK 8 and JDK 9+.
     *
     * @param callerClass The class or interface from which the lookup is being performed.
     * @return A {@link MethodHandles.Lookup} object with appropriate access privileges.
     */
    MethodHandles.Lookup lookup(final Class<?> callerClass);

}
