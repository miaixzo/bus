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
package org.miaixz.bus.core.lang.reflect;

import java.lang.reflect.Type;

/**
 * Defines the specification for invoking methods on a target object. This interface allows for dynamic method
 * invocation, enhancing code flexibility and extensibility. Inspired by
 * {@code org.apache.ibatis.reflection.invoker.Invoker}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Invoker {

    /**
     * Invokes a method on the specified target object.
     *
     * @param target The target object on which the method is to be invoked.
     * @param args   The array of arguments to be passed to the method.
     * @return The return value of the method invocation. The return type can be any type.
     * @param <T> The expected return type of the method.
     */
    <T> T invoke(Object target, Object... args);

    /**
     * Retrieves the name of the invoked method.
     *
     * @return The name of the method as a {@code String}.
     */
    String getName();

    /**
     * Retrieves the generic return type of the invoked method, or the type of a parameter or field.
     *
     * @return The {@link Type} representing the return type, parameter type, or field type.
     */
    Type getType();

    /**
     * Retrieves the raw {@link Class} of the invoked method's return type, or the type of a parameter or field. This is
     * the erased type, without generic information.
     *
     * @return The {@link Class} object representing the raw type.
     */
    Class<?> getTypeClass();

}
