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
package org.miaixz.bus.spring.web.interceptor;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerInterceptor;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.spring.ContextBuilder;
import org.miaixz.bus.spring.web.wrapper.CachedBodyRequestWrapper;

/**
 * Records a bounded, value-free request lifecycle audit entry.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class SentinelRequestHandler implements HandlerInterceptor {

    /**
     * Request attribute storing the handler start timestamp.
     */
    private static final String START_ATTRIBUTE = SentinelRequestHandler.class.getName() + Symbol.DOT + "START";
    /**
     * Request attribute storing the observed parameter count.
     */
    private static final String PARAMETER_COUNT_ATTRIBUTE = SentinelRequestHandler.class.getName() + Symbol.DOT
            + "PARAMETERS";
    /**
     * Request attribute storing the observed body size.
     */
    private static final String BODY_BYTES_ATTRIBUTE = SentinelRequestHandler.class.getName() + Symbol.DOT
            + "BODY_BYTES";

    /**
     * Application-context-scoped runtime context facade.
     */
    private final ContextBuilder contextBuilder;
    /**
     * Creates a request audit interceptor with Context-scoped correlation state.
     *
     * @param contextBuilder application-context-scoped runtime context facade
     */
    public SentinelRequestHandler(ContextBuilder contextBuilder) {
        this.contextBuilder = Objects.requireNonNull(contextBuilder, "contextBuilder");
    }

    /**
     * Records only correlation metadata and aggregate input counts.
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        long start = System.nanoTime();
        int parameterCount = request.getParameterMap().size();
        int bodyBytes = request instanceof CachedBodyRequestWrapper cached ? cached.getBody().length : 0;
        request.setAttribute(START_ATTRIBUTE, start);
        request.setAttribute(PARAMETER_COUNT_ATTRIBUTE, parameterCount);
        request.setAttribute(BODY_BYTES_ATTRIBUTE, bodyBytes);
        Logger.info(
                true,
                "Starter",
                "Request: requestId={}, method={}, path={}, parameterCount={}, bodyBytes={}",
                requestId(),
                request.getMethod(),
                normalizePath(request.getRequestURI()),
                parameterCount,
                bodyBytes);
        return true;
    }

    /**
     * Records completion status and elapsed time without headers, parameter values, or body content.
     */
    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception exception) {
        long duration = elapsedMillis(request.getAttribute(START_ATTRIBUTE));
        Logger.info(
                false,
                "Starter",
                "Response: requestId={}, method={}, path={}, status={}, durationMs={}, parameterCount={}, bodyBytes={}",
                requestId(),
                request.getMethod(),
                normalizePath(request.getRequestURI()),
                response.getStatus(),
                duration,
                count(request.getAttribute(PARAMETER_COUNT_ATTRIBUTE)),
                count(request.getAttribute(BODY_BYTES_ATTRIBUTE)));
    }

    /**
     * Resolves the request identifier used for access logging.
     *
     * @return the existing request identifier, or a generated identifier when absent
     */
    private String requestId() {
        String requestId = contextBuilder.getRequestId();
        return requestId == null ? Normal.EMPTY : requestId;
    }

    /**
     * Calculates elapsed request processing time in milliseconds.
     *
     * @param start request start timestamp
     * @return non-negative elapsed time in milliseconds
     */
    private long elapsedMillis(Object start) {
        if (!(start instanceof Long started)) {
            return 0;
        }
        return TimeUnit.NANOSECONDS.toMillis(Math.max(0, System.nanoTime() - started));
    }

    /**
     * Parses a non-negative request measurement.
     *
     * @param value request attribute value
     * @return parsed non-negative count, or zero when invalid
     */
    private int count(Object value) {
        return value instanceof Integer count ? count : 0;
    }

    /**
     * Removes query, fragment, and matrix-parameter values from an access-log path.
     *
     * @param path request path
     * @return normalized value-free path
     */
    private String normalizePath(String path) {
        if (path == null || path.isBlank()) {
            return Symbol.SLASH;
        }
        int boundary = path.length();
        int query = path.indexOf(Symbol.C_QUESTION_MARK);
        int fragment = path.indexOf(Symbol.C_HASH);
        if (query >= 0) {
            boundary = Math.min(boundary, query);
        }
        if (fragment >= 0) {
            boundary = Math.min(boundary, fragment);
        }
        StringBuilder normalized = new StringBuilder(boundary);
        boolean matrixParameter = false;
        boolean previousSlash = false;
        for (int index = 0; index < boundary; index++) {
            char character = path.charAt(index);
            if (character == Symbol.C_SEMICOLON) {
                matrixParameter = true;
                continue;
            }
            if (character == Symbol.C_SLASH) {
                matrixParameter = false;
                if (!previousSlash) {
                    normalized.append(character);
                    previousSlash = true;
                }
                continue;
            }
            if (!matrixParameter) {
                normalized.append(character);
                previousSlash = false;
            }
        }
        return normalized.isEmpty() ? Symbol.SLASH : normalized.toString();
    }

}
