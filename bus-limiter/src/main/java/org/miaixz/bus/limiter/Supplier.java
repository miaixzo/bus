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
package org.miaixz.bus.limiter;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * Abstract base class for a supplier that provides a unique identifier for a user or request. This supplier is used in
 * limiting scenarios to identify the entity being limited. It also provides a default interception message for blocked
 * requests.
 *
 * @author Kimi Liu
 * @since Java 21+
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
