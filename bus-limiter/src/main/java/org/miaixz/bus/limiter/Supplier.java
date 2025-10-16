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
package org.miaixz.bus.limiter;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * Abstract base class for a supplier that provides a unique identifier for a user or request. This supplier is used in
 * limiting scenarios to identify the entity being limited. It also provides a default interception message for blocked
 * requests.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class Supplier {

    /**
     * Retrieves the current user or request identifier. This method must be implemented by concrete subclasses to
     * provide the specific identification logic.
     *
     * @return A {@link Serializable} object representing the user or request identifier.
     */
    public abstract Serializable get();

    /**
     * Provides a default interception message when a request is blocked by a limiter. Subclasses can override this
     * method to provide custom messages or actions upon interception.
     *
     * @param bean   The target object on which the method was invoked.
     * @param method The {@link Method} that was attempted to be executed.
     * @param args   The arguments passed to the method invocation.
     * @return A {@link Serializable} object, typically a message, indicating the request was intercepted.
     */
    public Serializable intercept(Object bean, Method method, Object[] args) {
        return "Your request is frequent. Please wait...";
    }

}
