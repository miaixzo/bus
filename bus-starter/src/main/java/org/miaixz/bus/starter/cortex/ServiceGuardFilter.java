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
package org.miaixz.bus.starter.cortex;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.stream.Collectors;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.PathContainer.PathSegment;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ServletRequestPathUtils;
import org.springframework.web.util.WebUtils;

import org.miaixz.bus.core.lang.exception.RelevantException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.cortex.guard.GuardEvaluator;
import org.miaixz.bus.cortex.guard.inbound.InboundDecision.Status;
import org.miaixz.bus.spring.web.wrapper.CachedBodyRequestWrapper;
import org.miaixz.bus.vortex.guard.SignatureVerifier;

/**
 * Applies the common service identity decision at the Servlet boundary.
 *
 * @author Kimi Liu
 */
public class ServiceGuardFilter extends OncePerRequestFilter {

    /**
     * Identity component shared with the outbound adapter.
     */
    private final GuardEvaluator identity;

    /**
     * Creates the Servlet adapter.
     *
     * @param identity shared identity component
     */
    public ServiceGuardFilter(GuardEvaluator identity) {
        this.identity = identity;
    }

    /**
     * Checks the context-relative route and exposes only the verified identity to downstream handlers.
     *
     * @param request  current Servlet request
     * @param response current Servlet response
     * @param chain    remaining Servlet filters
     * @throws ServletException when downstream handling fails
     * @throws IOException      when the request or response stream fails
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String path = ServletRequestPathUtils.parseAndCache(request).pathWithinApplication().elements().stream()
                .map(part -> part instanceof PathSegment segment ? segment.valueToMatch() : part.value())
                .collect(Collectors.joining());
        var decision = identity.acceptHeaders(path, null, name -> Collections.list(request.getHeaders(name)));
        if (decision.status() != Status.ACCEPTED) {
            int status = decision.status() == Status.REJECTED ? Http.Status.UNAUTHORIZED
                    : Http.Status.SERVICE_UNAVAILABLE;
            reject(response, status);
            return;
        }
        request.setAttribute(GuardEvaluator.VERIFIED_ATTRIBUTE, decision.verified());
        HttpServletRequest admitted = request;
        if (Http.Method.POST.value().equals(request.getMethod())
                && MediaType.APPLICATION_JSON.equalsIgnoreCase(request.getContentType())
                && request.getHeader(Http.Auth.FORWARD_PROOF_HEADER) != null
                && WebUtils.getNativeRequest(request, CachedBodyRequestWrapper.class) == null) {
            if (request.getContentLengthLong() > SignatureVerifier.FORWARD_BODY_LIMIT) {
                reject(response, HttpStatus.CONTENT_TOO_LARGE.value());
                return;
            }
            try {
                admitted = new CachedBodyRequestWrapper(request, SignatureVerifier.FORWARD_BODY_LIMIT);
            } catch (RelevantException oversized) {
                reject(response, HttpStatus.CONTENT_TOO_LARGE.value());
                return;
            }
        }
        chain.doFilter(new HttpServletRequestWrapper(admitted) {

            /**
             * Returns a visible request header.
             *
             * @param name requested header
             * @return value or null when the header is reserved for service identity
             */
            @Override
            public String getHeader(String name) {
                return identity.reservesHeader(null, name) ? null : super.getHeader(name);
            }

            /**
             * Returns all visible values of a request header.
             *
             * @param name requested header
             * @return values with service identity credentials omitted
             */
            @Override
            public java.util.Enumeration<String> getHeaders(String name) {
                return identity.reservesHeader(null, name) ? Collections.emptyEnumeration() : super.getHeaders(name);
            }

            /**
             * Returns visible header names.
             *
             * @return header names excluding the consumed service identity header
             */
            @Override
            public Enumeration<String> getHeaderNames() {
                return Collections.enumeration(
                        Collections.list(super.getHeaderNames()).stream()
                                .filter(name -> !identity.reservesHeader(null, name)).toList());
            }

        }, response);
    }

    /**
     * Writes an uncached JSON response for an identity check that did not admit the request.
     *
     * @param response current Servlet response
     * @param status   HTTP status code to send
     * @throws IOException if the response body cannot be written
     */
    private static void reject(HttpServletResponse response, int status) throws IOException {
        response.setStatus(status);
        response.setHeader(Http.Header.CACHE_CONTROL, Http.Cache.NO_STORE);
        response.setContentType(MediaType.APPLICATION_JSON);
        response.getWriter().write("{\"code\":\"" + status + "\"}");
    }

}
