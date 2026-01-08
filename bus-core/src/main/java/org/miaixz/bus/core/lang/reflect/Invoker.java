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
