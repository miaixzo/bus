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
    private ErrorAdvice() {
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
