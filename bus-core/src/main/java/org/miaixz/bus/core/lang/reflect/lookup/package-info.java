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
/**
 * Provides encapsulation for creating {@link java.lang.invoke.MethodHandles.Lookup} instances, which are used to find
 * {@link java.lang.invoke.MethodHandles} based on different conditions.
 *
 * <p>
 * In JDK 8, directly calling {@link java.lang.invoke.MethodHandles#lookup()} to obtain a
 * {@link java.lang.invoke.MethodHandles.Lookup} instance may lead to permission issues ("no private access for
 * invokespecial") when invoking {@code findSpecial} and {@code unreflectSpecial}. Therefore, this package provides
 * encapsulated lookup methods specifically for JDK 8 and JDK 9+.
 * <p>
 * Reference: <a href=
 * "https://blog.csdn.net/u013202238/article/details/108687086">https://blog.csdn.net/u013202238/article/details/108687086</a>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
package org.miaixz.bus.core.lang.reflect.lookup;
