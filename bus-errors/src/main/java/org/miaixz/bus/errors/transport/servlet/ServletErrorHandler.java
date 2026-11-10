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
package org.miaixz.bus.errors.transport.servlet;

import java.io.IOException;

import jakarta.servlet.http.HttpServletResponse;

import org.miaixz.bus.errors.ErrorBoundary;
import org.miaixz.bus.errors.ErrorContext;
import org.miaixz.bus.errors.ErrorDecision.Action;
import org.miaixz.bus.errors.FailureFacts;

/**
 * Handles explicit Servlet filter rejections with the existing container error dispatch.
 *
 * @author Kimi Liu
 */
public final class ServletErrorHandler {

    /**
     * Stable label for direct Servlet rejection before MVC dispatch.
     */
    private static final ErrorContext CONTEXT = new ErrorContext("servlet", "http", ErrorContext.Phase.OPEN);

    /**
     * Prevents construction of the stateless Servlet error utility.
     */
    private ServletErrorHandler() {
    }

    /**
     * Rejects a request while preserving the container's existing error dispatch.
     *
     * @param boundary shared classifier and observer for Servlet
     * @param response response associated with the request
     * @param status   verified rejection status
     * @param cause    optional diagnostic cause
     * @throws IOException if the container cannot send the error
     */
    public static void reject(ErrorBoundary boundary, HttpServletResponse response, int status, Throwable cause)
            throws IOException {
        if (response.isCommitted()) {
            return;
        }
        var operation = boundary.open();
        var decision = operation.decide(CONTEXT, new FailureFacts(FailureFacts.Source.LOCAL, null, status), cause);
        if (decision.action() == Action.RENDER) {
            operation.complete(CONTEXT, decision, cause, null, () -> {
                response.sendError(decision.status());
                return null;
            });
        }
    }

}
