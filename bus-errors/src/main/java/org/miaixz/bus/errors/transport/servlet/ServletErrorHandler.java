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
import java.nio.charset.StandardCharsets;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;

import org.miaixz.bus.errors.ErrorBoundary;
import org.miaixz.bus.errors.ErrorContext;
import org.miaixz.bus.errors.ErrorDecision.Action;
import org.miaixz.bus.errors.FailureFacts;
import org.miaixz.bus.errors.transport.ProtocolErrorBoundary;
import org.miaixz.bus.errors.transport.http.HttpErrorResponse;
import org.miaixz.bus.errors.transport.http.PublicHttpError;

/**
 * Handles public Servlet rejections, MVC fallbacks and committed failures.
 *
 * @author Kimi Liu
 */
public final class ServletErrorHandler {

    /**
     * Stable label for direct Servlet rejection before MVC dispatch.
     */
    private static final ErrorContext CONTEXT = new ErrorContext("servlet", "http", ErrorContext.Phase.OPEN);

    /**
     * Request attribute sharing the terminal observation across Servlet and MVC dispatches.
     */
    private static final String OPERATION = ServletErrorHandler.class.getName() + ".operation";

    /**
     * Prevents construction of the stateless Servlet error utility.
     */
    private ServletErrorHandler() {
    }

    /**
     * Rejects a request with an empty, boundary-owned HTTP error.
     *
     * @param boundary shared classifier and observer for Servlet
     * @param response response associated with the request
     * @param status   verified rejection status
     * @param cause    optional diagnostic cause
     * @throws IOException if the container cannot send the error
     */
    public static void reject(ErrorBoundary boundary, HttpServletResponse response, int status, Throwable cause)
            throws IOException {
        write(boundary, null, response, status, cause, HttpHeaders.EMPTY, null, false);
    }

    /**
     * Writes a verified empty error and optional cache directive without invoking a container error page.
     *
     * @param request      active request for correlation
     * @param response     uncommitted response
     * @param status       verified status
     * @param cause        diagnostic failure
     * @param cacheControl public cache directive, or null
     * @throws IOException if the connection cannot complete the response
     */
    public static void write(
            HttpServletRequest request,
            HttpServletResponse response,
            int status,
            Throwable cause,
            String cacheControl) throws IOException {
        write(ErrorBoundary.standard(), request, response, status, cause, HttpHeaders.EMPTY, cacheControl, false);
    }

    /**
     * Writes the safe MVC fallback and the protocol headers associated with its status.
     *
     * @param request  active request for correlation
     * @param response uncommitted response
     * @param status   verified status
     * @param cause    diagnostic failure
     * @param headers  verified framework or route headers
     * @throws IOException if the connection cannot complete the response
     */
    public static void fallback(
            HttpServletRequest request,
            HttpServletResponse response,
            int status,
            Throwable cause,
            HttpHeaders headers) throws IOException {
        write(ErrorBoundary.standard(), request, response, status, cause, headers, "no-store", true);
    }

    /**
     * Gets the single boundary operation shared by this Servlet request.
     *
     * @param boundary classifier to use for the first error
     * @param request  active request, or null for callers outside dispatch
     * @return request-scoped failure operation
     */
    public static ErrorBoundary.Operation operation(ErrorBoundary boundary, HttpServletRequest request) {
        if (request == null) {
            return boundary.open();
        }
        if (request.getAttribute(OPERATION) instanceof ErrorBoundary.Operation operation) {
            return operation;
        }
        var operation = boundary.open();
        request.setAttribute(OPERATION, operation);
        return operation;
    }

    /**
     * Records a failed response which has already been committed.
     *
     * @param request active request for correlation
     * @param cause   diagnostic failure
     */
    public static void terminate(HttpServletRequest request, Throwable cause) {
        var context = new ErrorContext("servlet", "http", ErrorContext.Phase.COMMITTED);
        var operation = operation(ErrorBoundary.standard(), request);
        var decision = operation.decide(context, FailureFacts.UNKNOWN, cause);
        if (decision.action() == Action.TERMINATE) {
            ProtocolErrorBoundary.claim(operation, context, decision, cause, request.getRequestId());
        }
    }

    /**
     * Writes a bodyless rejection and prevents a second renderer from taking the response.
     *
     * @param boundary     shared classifier and observer
     * @param request      active request, if available
     * @param response     current response
     * @param status       verified status
     * @param cause        diagnostic failure
     * @param headers      verified framework or route headers
     * @param cacheControl optional public cache directive
     * @param fallbackBody whether to write the fixed HTTP fallback body
     * @throws IOException if the connection cannot complete the response
     */
    private static void write(
            ErrorBoundary boundary,
            HttpServletRequest request,
            HttpServletResponse response,
            int status,
            Throwable cause,
            HttpHeaders headers,
            String cacheControl,
            boolean fallbackBody) throws IOException {
        if (response.isCommitted()) {
            return;
        }
        var operation = operation(boundary, request);
        int errorStatus = status >= 400 && status <= 599 ? status : HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        var decision = operation.decide(CONTEXT, new FailureFacts(FailureFacts.Source.LOCAL, null, errorStatus), cause);
        if (decision.action() == Action.RENDER) {
            var selected = HttpErrorResponse.empty(
                    HttpStatusCode.valueOf(decision.status()),
                    headers,
                    cacheControl == null ? "" : cacheControl);
            operation.complete(CONTEXT, decision, cause, request == null ? null : request.getRequestId(), () -> {
                ServletErrorFilter.approve(response);
                response.setStatus(selected.status().value());
                selected.headers().forEach((name, values) -> values.forEach(value -> response.addHeader(name, value)));
                if (fallbackBody) {
                    var error = PublicHttpError.forStatus(selected.status());
                    byte[] body = ("{\"status\":" + selected.status().value() + ",\"error\":\"" + error.description()
                            + "\"}").getBytes(StandardCharsets.UTF_8);
                    response.setContentType("application/json");
                    response.setContentLength(body.length);
                    if (request == null || !"HEAD".equals(request.getMethod())) {
                        response.getOutputStream().write(body);
                    }
                } else {
                    response.setContentLength(0);
                }
                return null;
            });
        }
    }

}
