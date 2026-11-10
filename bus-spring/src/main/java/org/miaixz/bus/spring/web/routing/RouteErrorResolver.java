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
package org.miaixz.bus.spring.web.routing;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.web.ErrorResponse;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.ServletRequestPathUtils;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import org.miaixz.bus.core.lang.annotation.Nullable;

/**
 * Preserves selected MVC HTTP errors for configured route prefixes before shared exception advice runs.
 * <p>
 * Routing can fail before MVC selects a handler. The resolver uses the request path so that missing routes, unsupported
 * methods, and unsupported media types can receive the same treatment. Matched errors retain their HTTP status and
 * headers with an empty response body. Other errors continue through the resolver chain.
 *
 * @author Kimi Liu
 */
public final class RouteErrorResolver implements HandlerExceptionResolver, Ordered {

    /**
     * Requires leading and trailing slashes and at least one nonempty segment made up of literal path characters.
     */
    private static final Pattern LITERAL_PREFIX = Pattern.compile("/(?:[\\p{L}\\p{M}\\p{N}._~!$&'()+,=:@-]+/)+");

    /**
     * Compiled prefixes matched against the parsed MVC request path.
     */
    private final List<PathPattern> prefixes;

    /**
     * HTTP client error statuses eligible for this resolver.
     */
    private final Set<Integer> statuses;

    /**
     * Optional override for the Cache-Control header on handled responses.
     */
    private final String cacheControl;

    /**
     * Creates a resolver for literal, slash-terminated prefixes and an explicit set of client error statuses.
     *
     * @param pathPrefixes nonempty list of literal paths relative to the MVC servlet mapping, each beginning and ending
     *                     with a slash
     * @param statuses     nonempty set of HTTP status codes in the range 400 to 499
     * @param cacheControl optional Cache-Control header value, blank to preserve the existing header
     * @throws IllegalArgumentException if a prefix is invalid, the prefix or status collection is empty, a status is
     *                                  outside the client error range, or the cache value contains a line break
     */
    public RouteErrorResolver(List<String> pathPrefixes, Set<Integer> statuses, String cacheControl) {
        if (pathPrefixes.isEmpty() || pathPrefixes.stream().anyMatch(prefix -> !validPrefix(prefix))) {
            throw new IllegalArgumentException(
                    "route-errors.path-prefixes requires literal paths starting and ending with /");
        }
        if (statuses.isEmpty() || statuses.stream().anyMatch(status -> status < 400 || status > 499)) {
            throw new IllegalArgumentException("route-errors.statuses requires at least one 4xx HTTP status");
        }
        if (cacheControl.indexOf('\r') >= 0 || cacheControl.indexOf('\n') >= 0) {
            throw new IllegalArgumentException("Invalid route-errors.cache-control");
        }
        this.prefixes = pathPrefixes.stream().map(PathPatternParser.defaultInstance::parse).toList();
        this.statuses = Set.copyOf(statuses);
        this.cacheControl = cacheControl;
    }

    /**
     * Gives route-specific handling precedence over the standard MVC exception resolver chain.
     *
     * @return the highest resolver precedence
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    /**
     * Writes the status and headers from a matching {@link ErrorResponse}. Repeated header values are preserved and the
     * configured cache policy, when present, overrides Cache-Control.
     *
     * @param request   current servlet request used to match the configured route prefixes
     * @param response  servlet response to update while it is still uncommitted
     * @param handler   handler selected by MVC, or {@code null} when routing failed before selection
     * @param exception exception raised during MVC dispatch
     * @return an empty {@link ModelAndView} when the error was handled, or {@code null} to continue the resolver chain
     */
    @Override
    @Nullable
    public ModelAndView resolveException(
            HttpServletRequest request,
            HttpServletResponse response,
            @Nullable Object handler,
            Exception exception) {
        if (response.isCommitted() || !(exception instanceof ErrorResponse error)
                || !this.statuses.contains(error.getStatusCode().value()) || !matches(request)) {
            return null;
        }
        error.getHeaders().forEach((name, values) -> values.forEach(value -> response.addHeader(name, value)));
        if (!this.cacheControl.isBlank()) {
            response.setHeader(HttpHeaders.CACHE_CONTROL, this.cacheControl);
        }
        // Set the status directly to keep servlet error-page handling from replacing the empty response.
        response.setStatus(error.getStatusCode().value());
        return new ModelAndView();
    }

    /**
     * Matches prefixes using the same parsed path as MVC, excluding the application context and servlet mapping prefix.
     * Spring decodes each segment individually, so encoded separators cannot create new path segments.
     *
     * @param request current servlet request
     * @return whether a configured prefix matches the parsed path
     */
    private boolean matches(HttpServletRequest request) {
        try {
            var requestPath = ServletRequestPathUtils.hasParsedRequestPath(request)
                    ? ServletRequestPathUtils.getParsedRequestPath(request)
                    : ServletRequestPathUtils.parse(request);
            return this.prefixes.stream()
                    .anyMatch(prefix -> prefix.matchStartOfPath(requestPath.pathWithinApplication()) != null);
        } catch (IllegalArgumentException invalidPath) {
            // Leave the original exception available to the remaining resolvers.
            return false;
        }
    }

    /**
     * Checks the literal path syntax and rejects dot segments before a prefix is compiled.
     *
     * @param prefix configured path prefix
     * @return whether the prefix can be used as a literal MVC route prefix
     */
    private static boolean validPrefix(String prefix) {
        return prefix != null && LITERAL_PREFIX.matcher(prefix).matches() && !prefix.contains("/./")
                && !prefix.contains("/../");
    }

}
