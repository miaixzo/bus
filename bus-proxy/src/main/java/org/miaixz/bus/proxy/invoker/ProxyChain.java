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
package org.miaixz.bus.proxy.invoker;

/**
 * Represents a chain of interceptors for a method invocation. It extends {@link Invocation} with additional
 * capabilities, such as accessing parameter names and proceeding with modified arguments.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface ProxyChain extends Invocation {

    /**
     * Gets the names of the method parameters. Note: This may not be available on all platforms or without specific
     * compiler flags.
     *
     * @return An array of parameter names.
     */
    Object[] getNames();

    /**
     * Proceeds with the invocation, but with a new set of arguments.
     *
     * @param arguments The new arguments to use for the method invocation.
     * @return The result of the method invocation.
     * @throws Throwable if the underlying method throws an exception.
     */
    Object proceed(Object[] arguments) throws Throwable;

}
