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
package org.miaixz.bus.core.basic.advice;

import java.util.ServiceLoader;

import org.miaixz.bus.core.basic.service.ErrorService;

/**
 * Handles exceptions by delegating to registered {@link ErrorService} implementations.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ErrorAdvice {

    /**
     * Constructs a new ErrorAdvice. Utility class constructor for static access.
     */
    public ErrorAdvice() {

    }

    /**
     * This method is called before the business processor handles the request to process the user's request. If it
     * returns {@code true}, subsequent interceptors and the target method will be called; otherwise, the request is
     * terminated. This is a suitable place for implementing login verification, permission interception, request
     * limiting, etc.
     *
     * @param ex The exception object.
     * @return {@code true} if the execution chain should continue, {@code false} otherwise.
     */
    public boolean handler(Exception ex) {
        // Load all implementations of the ErrorService interface.
        final ServiceLoader<ErrorService> loader = ServiceLoader.load(ErrorService.class);
        boolean continueChain = true;
        // Iterate through each ErrorService and call its before and after methods.
        // The chain continues only if all services return true.
        for (ErrorService service : loader) {
            continueChain &= service.before(ex) && service.after(ex);
        }
        // If no ErrorService implementations are found, use a default implementation.
        if (!loader.iterator().hasNext()) {
            ErrorService defaultService = new ErrorService() {
            };
            continueChain &= defaultService.before(ex) && defaultService.after(ex);
        }
        return continueChain;
    }

}
