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
package org.miaixz.bus.starter.auth;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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

import org.miaixz.bus.auth.guard.GuardConfig.Admission;
import org.miaixz.bus.auth.guard.GuardEvaluator;
import org.miaixz.bus.auth.guard.SignValidator;
import org.miaixz.bus.auth.guard.inbound.InboundDecision;
import org.miaixz.bus.auth.guard.inbound.InboundDecision.Status;
import org.miaixz.bus.core.lang.exception.RelevantException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.spring.web.wrapper.CachedBodyRequestWrapper;

/**
 * Applies the common service identity decision at the Servlet boundary.
 * <p>
 * Admitted requests expose any verified identity through {@link InboundDecision#ATTRIBUTE}. The request wrapper hides
 * reserved service identity headers from the remaining filter chain.
 *
 * @author Kimi Liu
 */
public class ServiceGuardFilter extends OncePerRequestFilter {

    /**
     * Identity component shared with the outbound adapter.
     */
    private final GuardEvaluator identity;

    /**
     * Whether the application explicitly binds the listener to a loopback address.
     */
    private final boolean loopbackListener;

    /**
     * Additional admission rules indexed by the exact Servlet path.
     */
    private final Map<String, Admission> admission;

    /**
     * Creates the Servlet adapter.
     *
     * @param identity shared identity component
     */
    public ServiceGuardFilter(GuardEvaluator identity) {
        this(identity, List.of(), null);
    }

    /**
     * Creates the Servlet adapter with endpoint admission rules.
     *
     * @param identity        shared identity component
     * @param admission       declared endpoint admission rules
     * @param listenerAddress explicitly configured listener address, when present
     */
    public ServiceGuardFilter(GuardEvaluator identity, List<Admission> admission, String listenerAddress) {
        this.identity = identity;
        this.admission = admission.stream().collect(Collectors.toUnmodifiableMap(Admission::path, Function.identity()));
        this.loopbackListener = isLoopback(listenerAddress);
    }

    /**
     * Checks the context-relative route before passing the request down the filter chain.
     * <p>
     * A rejected identity returns HTTP 401. If identity verification is unavailable, the filter returns HTTP 503. For
     * an admitted {@code application/json} POST carrying a forwarding proof header, the filter caches the body if it
     * has not already been wrapped and returns HTTP 413 if the cache limit is exceeded.
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
        Admission rule = admission.get(path);
        if (rule != null) {
            boolean directRead = rule.directGetParameter() != null
                    && Http.Method.GET.value().equals(request.getMethod())
                    && request.getParameter(rule.directGetParameter()) != null;
            boolean local = !identity.inboundEnabled() && loopbackListener && isLoopback(request.getLocalAddr())
                    && isLoopback(request.getRemoteAddr());
            if (!rule.admits(decision.verified(), directRead, local)) {
                reject(response, Http.Status.UNAUTHORIZED);
                return;
            }
        }
        request.setAttribute(InboundDecision.ATTRIBUTE, decision);
        HttpServletRequest admitted = request;
        if (Http.Method.POST.value().equals(request.getMethod())
                && MediaType.APPLICATION_JSON.equalsIgnoreCase(request.getContentType())
                && request.getHeader(Http.Auth.FORWARD_PROOF_HEADER) != null
                && WebUtils.getNativeRequest(request, CachedBodyRequestWrapper.class) == null) {
            if (request.getContentLengthLong() > SignValidator.FORWARD_BODY_LIMIT) {
                reject(response, HttpStatus.CONTENT_TOO_LARGE.value());
                return;
            }
            try {
                admitted = new CachedBodyRequestWrapper(request, SignValidator.FORWARD_BODY_LIMIT);
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
            public Enumeration<String> getHeaders(String name) {
                return identity.reservesHeader(null, name) ? Collections.emptyEnumeration() : super.getHeaders(name);
            }

            /**
             * Returns visible header names.
             *
             * @return header names excluding those reserved for service identity
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
     * Recognizes the literal loopback endpoints used by Servlet listeners without resolving names.
     *
     * @param address configured or socket endpoint address
     * @return whether it is a loopback literal
     */
    private static boolean isLoopback(String address) {
        return "127.0.0.1".equals(address) || "::1".equals(address) || "0:0:0:0:0:0:0:1".equals(address);
    }

    /**
     * Writes the rejection status as an uncached JSON response.
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
