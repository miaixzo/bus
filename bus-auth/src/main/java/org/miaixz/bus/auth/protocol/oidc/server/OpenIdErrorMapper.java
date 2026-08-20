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
package org.miaixz.bus.auth.protocol.oidc.server;

import java.util.Set;

import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.extra.json.JsonValue;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.protocol.http.HttpRequest;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;

/**
 * Maps OpenID Provider resource failures to endpoint-specific standard HTTP responses.
 * <p>
 * Discovery, JWK Set, and RP-Initiated Logout define no reusable JSON error envelope, so their error bodies remain
 * empty. UserInfo failures use only the RFC 6750 Bearer challenge and never serialize internal errors, descriptions,
 * token values, hints, redirects, or structured failure details.
 * </p>
 *
 * @author Kimi Liu
 */
public final class OpenIdErrorMapper {

    /**
     * Failure detail member carrying one registered OAuth bearer error.
     */
    private static final String OAUTH_ERROR = "oauth_error";

    /**
     * Bearer errors allowed at the UserInfo protected-resource boundary.
     */
    private static final Set<OAuth2ErrorCode> USERINFO_ERRORS = Set
            .of(OAuth2ErrorCode.INVALID_REQUEST, OAuth2ErrorCode.INVALID_TOKEN, OAuth2ErrorCode.INSUFFICIENT_SCOPE);

    /**
     * Creates a stateless OpenID endpoint error mapper.
     */
    public OpenIdErrorMapper() {
        // No initialization required.
    }

    /**
     * Accepts only a registered UserInfo Bearer error from safe internal details.
     *
     * @param failure  closed internal failure
     * @param fallback default standard error
     * @return allowed registered bearer error
     */
    private static OAuth2ErrorCode bearerError(final Outcome.Failure failure, final OAuth2ErrorCode fallback) {
        final JsonValue value = failure.details().values().get(OAUTH_ERROR);
        if (value instanceof JsonValue.StringValue string) {
            try {
                final OAuth2ErrorCode candidate = new OAuth2ErrorCode(string.value());
                if (USERINFO_ERRORS.contains(candidate)) {
                    return candidate;
                }
            } catch (RuntimeException ignored) {
                // An invalid internal detail is deliberately replaced by the standard fallback.
            }
        }
        return fallback;
    }

    /**
     * Builds an empty response with mandatory cache prevention and an optional Bearer challenge.
     *
     * @param request   originating Fabric HTTP request
     * @param status    exact HTTP status
     * @param challenge optional prevalidated WWW-Authenticate field value
     * @return complete empty HTTP response
     */
    private static HttpResponse empty(final HttpRequest request, final int status, final String challenge) {
        Assert.notNull(request, "OpenID Connect originating HTTP request must not be null");
        final Headers.Builder headers = Headers.builder().add(Http.Header.CACHE_CONTROL, Http.Cache.NO_STORE)
                .add(Http.Header.PRAGMA, Http.Cache.NO_CACHE);
        if (challenge != null) {
            headers.add(Http.Header.WWW_AUTHENTICATE, challenge);
        }
        return HttpResponse.builder().request(request).code(status).headers(headers.build()).build();
    }

    /**
     * Detects a shared Bus five-hundred failure class without serializing it.
     *
     * @param failure closed internal failure
     * @return whether the stable Bus error key begins with five
     */
    private static boolean serverFailure(final Outcome.Failure failure) {
        final String key = failure.error().getKey();
        return key.length() == 3 && key.charAt(0) == Symbol.C_FIVE;
    }

    /**
     * Maps a discovery service failure to an empty internal-error response.
     *
     * @param request originating Fabric HTTP request
     * @param failure closed internal failure
     * @return complete empty error response
     */
    public HttpResponse discovery(final HttpRequest request, final Outcome.Failure failure) {
        Assert.notNull(failure, "OpenID Connect discovery failure must not be null");
        return empty(request, Http.Status.INTERNAL_SERVER_ERROR, null);
    }

    /**
     * Maps an invalid discovery resource request to an empty bad-request response.
     *
     * @param request originating Fabric HTTP request
     * @return complete empty error response
     */
    public HttpResponse discoveryMalformed(final HttpRequest request) {
        return empty(request, Http.Status.BAD_REQUEST, null);
    }

    /**
     * Maps a public JWK Set service failure to an empty internal-error response.
     *
     * @param request originating Fabric HTTP request
     * @param failure closed internal failure
     * @return complete empty error response
     */
    public HttpResponse jwks(final HttpRequest request, final Outcome.Failure failure) {
        Assert.notNull(failure, "OpenID Connect JWK Set failure must not be null");
        return empty(request, Http.Status.INTERNAL_SERVER_ERROR, null);
    }

    /**
     * Maps an invalid JWK Set resource request to an empty bad-request response.
     *
     * @param request originating Fabric HTTP request
     * @return complete empty error response
     */
    public HttpResponse jwksMalformed(final HttpRequest request) {
        return empty(request, Http.Status.BAD_REQUEST, null);
    }

    /**
     * Maps a UserInfo failure to an empty RFC 6750 Bearer response or an empty server error.
     *
     * @param request originating Fabric HTTP request
     * @param failure closed internal failure
     * @return complete UserInfo error response
     */
    public HttpResponse userInfo(final HttpRequest request, final Outcome.Failure failure) {
        Assert.notNull(failure, "OpenID Connect UserInfo failure must not be null");
        if (serverFailure(failure)) {
            return empty(request, Http.Status.INTERNAL_SERVER_ERROR, null);
        }
        final OAuth2ErrorCode error = bearerError(failure, OAuth2ErrorCode.INVALID_TOKEN);
        final int status = OAuth2ErrorCode.INSUFFICIENT_SCOPE.equals(error) ? Http.Status.FORBIDDEN
                : OAuth2ErrorCode.INVALID_REQUEST.equals(error) ? Http.Status.BAD_REQUEST : Http.Status.UNAUTHORIZED;
        return empty(request, status, "Bearer error=\"" + error.value() + "\"");
    }

    /**
     * Maps an undecodable UserInfo request to the RFC 6750 invalid-request challenge.
     *
     * @param request originating Fabric HTTP request
     * @return complete empty bad-request response
     */
    public HttpResponse userInfoMalformed(final HttpRequest request) {
        return empty(request, Http.Status.BAD_REQUEST, "Bearer error=\"invalid_request\"");
    }

    /**
     * Maps an end-session service rejection or failure without redirecting unverified input.
     *
     * @param request originating Fabric HTTP request
     * @param failure closed internal failure
     * @return complete empty local error response
     */
    public HttpResponse endSession(final HttpRequest request, final Outcome.Failure failure) {
        Assert.notNull(failure, "OpenID Connect end-session failure must not be null");
        return empty(
                request,
                serverFailure(failure) ? Http.Status.INTERNAL_SERVER_ERROR : Http.Status.BAD_REQUEST,
                null);
    }

    /**
     * Maps an undecodable end-session request without redirecting any unverified URI.
     *
     * @param request originating Fabric HTTP request
     * @return complete empty bad-request response
     */
    public HttpResponse endSessionMalformed(final HttpRequest request) {
        return empty(request, Http.Status.BAD_REQUEST, null);
    }

}
