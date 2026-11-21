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
package org.miaixz.bus.starter.anchor;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;

/**
 * Rejects internal calls without a valid service identity token.
 * <p>
 * The filter only answers whether the caller is the configured service. It does not authenticate users and does not
 * replace any credential verification performed by the target handler.
 * </p>
 *
 * @author Kimi Liu
 */
public class ServiceAnchorFilter extends OncePerRequestFilter {

    /**
     * Recursive path suffix.
     */
    private static final String RECURSIVE = Symbol.SLASH + Symbol.STAR + Symbol.STAR;

    /**
     * Deployment settings.
     */
    private final AnchorProperties properties;

    /**
     * Rotating key holder.
     */
    private final AnchorKeys keys;

    /**
     * Creates the filter.
     *
     * @param properties deployment settings
     * @param keys       rotating key holder
     */
    public ServiceAnchorFilter(final AnchorProperties properties, final AnchorKeys keys) {
        this.properties = properties;
        this.keys = keys;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doFilterInternal(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final FilterChain chain) throws ServletException, IOException {
        final String path = request.getRequestURI().substring(request.getContextPath().length());
        if (!protectedPath(path, properties.inbound().paths())) {
            chain.doFilter(request, response);
            return;
        }
        final List<String> values = Collections.list(request.getHeaders(properties.inbound().header()));
        if (values.size() != 1 || !keys.accepts(values.getFirst())) {
            response.setStatus(Http.Status.UNAUTHORIZED);
            response.setHeader(Http.Header.CACHE_CONTROL, "no-store");
            response.setContentType(MediaType.APPLICATION_JSON);
            response.getWriter().write("{\"code\":\"401\",\"desc\":\"UNAUTHORIZED\"}");
            return;
        }
        chain.doFilter(request, response);
    }

    /**
     * Reports whether the path is protected.
     *
     * @param path   context-relative path
     * @param config comma-separated patterns
     * @return whether the request needs a service token
     */
    private static boolean protectedPath(final String path, final String config) {
        for (String pattern : config.split(Symbol.COMMA)) {
            final String value = pattern.strip();
            if (value.isEmpty()) {
                continue;
            }
            if (value.endsWith(RECURSIVE)) {
                if (path.startsWith(value.substring(0, value.length() - RECURSIVE.length()))) {
                    return true;
                }
            } else if (value.equals(path)) {
                return true;
            }
        }
        return false;
    }

}
