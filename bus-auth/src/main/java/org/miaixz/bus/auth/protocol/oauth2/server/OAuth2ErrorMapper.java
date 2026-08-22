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
package org.miaixz.bus.auth.protocol.oauth2.server;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.FabricX.*;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.codec.NameValue;
import org.miaixz.bus.auth.codec.QueryCodec;
import org.miaixz.bus.auth.protocol.oauth2.*;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.UncheckedException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.extra.json.JsonProvider;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Maps closed framework failures to endpoint-specific standard OAuth 2.x HTTP error responses.
 * <p>
 * Only registered OAuth wire parameters leave this boundary. Internal error codes and structured failure details are
 * never serialized, and an authorization error is redirected only when the service explicitly marks its exact
 * registered redirect URI as already validated.
 * </p>
 *
 * @author Kimi Liu
 */
public class OAuth2ErrorMapper {

    /**
     * Authorization endpoint error values defined by RFC 6749.
     */
    private static final Set<OAuth2ErrorCode> AUTHORIZATION_ERRORS = Set.of(
            OAuth2ErrorCode.INVALID_REQUEST,
            OAuth2ErrorCode.UNAUTHORIZED_CLIENT,
            OAuth2ErrorCode.ACCESS_DENIED,
            OAuth2ErrorCode.UNSUPPORTED_RESPONSE_TYPE,
            OAuth2ErrorCode.INVALID_SCOPE,
            OAuth2ErrorCode.SERVER_ERROR,
            OAuth2ErrorCode.TEMPORARILY_UNAVAILABLE);

    /**
     * Token endpoint error values implemented by the enabled OAuth grants.
     */
    private static final Set<OAuth2ErrorCode> TOKEN_ERRORS = Set.of(
            OAuth2ErrorCode.INVALID_REQUEST,
            OAuth2ErrorCode.INVALID_CLIENT,
            OAuth2ErrorCode.INVALID_GRANT,
            OAuth2ErrorCode.UNAUTHORIZED_CLIENT,
            OAuth2ErrorCode.UNSUPPORTED_GRANT_TYPE,
            OAuth2ErrorCode.INVALID_SCOPE,
            OAuth2ErrorCode.AUTHORIZATION_PENDING,
            OAuth2ErrorCode.SLOW_DOWN,
            OAuth2ErrorCode.ACCESS_DENIED,
            OAuth2ErrorCode.EXPIRED_TOKEN,
            OAuth2ErrorCode.INVALID_TARGET,
            OAuth2ErrorCode.SERVER_ERROR,
            OAuth2ErrorCode.TEMPORARILY_UNAVAILABLE);

    /**
     * Device authorization endpoint error values defined by RFC 6749 and RFC 8628.
     */
    private static final Set<OAuth2ErrorCode> DEVICE_ERRORS = Set.of(
            OAuth2ErrorCode.INVALID_REQUEST,
            OAuth2ErrorCode.INVALID_CLIENT,
            OAuth2ErrorCode.UNAUTHORIZED_CLIENT,
            OAuth2ErrorCode.INVALID_SCOPE,
            OAuth2ErrorCode.SERVER_ERROR,
            OAuth2ErrorCode.TEMPORARILY_UNAVAILABLE);

    /**
     * Introspection endpoint errors that can be represented without inventing an RFC 7662 success envelope.
     */
    private static final Set<OAuth2ErrorCode> INTROSPECTION_ERRORS = Set.of(
            OAuth2ErrorCode.INVALID_REQUEST,
            OAuth2ErrorCode.INVALID_CLIENT,
            OAuth2ErrorCode.SERVER_ERROR,
            OAuth2ErrorCode.TEMPORARILY_UNAVAILABLE);

    /**
     * Revocation endpoint error values defined by RFC 7009 plus operational server values.
     */
    private static final Set<OAuth2ErrorCode> REVOCATION_ERRORS = Set.of(
            OAuth2ErrorCode.INVALID_REQUEST,
            OAuth2ErrorCode.INVALID_CLIENT,
            OAuth2ErrorCode.UNSUPPORTED_TOKEN_TYPE,
            OAuth2ErrorCode.SERVER_ERROR,
            OAuth2ErrorCode.TEMPORARILY_UNAVAILABLE);

    /**
     * Runtime-selected provider-neutral JSON serializer.
     */
    private final JsonProvider jsonProvider;

    /**
     * Creates a standard OAuth error mapper.
     *
     * @param jsonProvider externally selected Bus JSON provider
     * @throws IllegalArgumentException if the provider is {@code null}
     */
    public OAuth2ErrorMapper(final JsonProvider jsonProvider) {
        this.jsonProvider = Assert.notNull(jsonProvider, "OAuth 2.x error JSON provider must not be null");
    }

    /**
     * Converts an internal failure to a validated standard error parameter set.
     *
     * @param failure  closed internal failure
     * @param allowed  exact endpoint error allow-list
     * @param fallback protocol fallback error
     * @param state    decoded authorization state or empty
     * @return immutable standard error response
     */
    private static AuthorizationErrorResponse authorizationResponse(
            final Outcome.Failure failure,
            final Set<OAuth2ErrorCode> allowed,
            final OAuth2ErrorCode fallback,
            final Optional<String> state) {
        final OAuth2ErrorCode error = operational(failure, allowed, fallback);
        final String description = nqschar(failure.safeDescription()) ? failure.safeDescription() : null;
        return new AuthorizationErrorResponse(error, Optional.ofNullable(description), Optional.empty(), state,
                new JsonValue.ObjectValue(Map.of()));
    }

    /**
     * Converts an internal failure to a standard token endpoint error body.
     *
     * @param failure  closed internal failure
     * @param allowed  exact endpoint error allow-list
     * @param fallback protocol fallback error
     * @return immutable token error response
     */
    private static TokenErrorResponse tokenResponse(
            final Outcome.Failure failure,
            final Set<OAuth2ErrorCode> allowed,
            final OAuth2ErrorCode fallback) {
        final OAuth2ErrorCode error = operational(failure, allowed, fallback);
        final String description = nqschar(failure.safeDescription()) ? failure.safeDescription() : null;
        return new TokenErrorResponse(error, Optional.ofNullable(description), Optional.empty(),
                new JsonValue.ObjectValue(Map.of()));
    }

    /**
     * Resolves operational errors first and otherwise accepts only an endpoint-allowed detail value.
     *
     * @param failure  closed internal failure
     * @param allowed  exact endpoint error allow-list
     * @param fallback protocol fallback error
     * @return allowed registered OAuth error
     */
    private static OAuth2ErrorCode operational(
            final Outcome.Failure failure,
            final Set<OAuth2ErrorCode> allowed,
            final OAuth2ErrorCode fallback) {
        if (same(failure, ErrorCode._408)) {
            return OAuth2ErrorCode.TEMPORARILY_UNAVAILABLE;
        }
        if (serverFailure(failure)) {
            return OAuth2ErrorCode.SERVER_ERROR;
        }
        final JsonValue value = failure.details().values().get(Builder.OAUTH_ERROR);
        if (value instanceof JsonValue.StringValue text) {
            try {
                final OAuth2ErrorCode candidate = new OAuth2ErrorCode(text.value());
                if (allowed.contains(candidate)) {
                    return candidate;
                }
            } catch (RuntimeException ignored) {
                // Invalid internal detail is deliberately replaced by the endpoint fallback.
            }
        }
        return fallback;
    }

    /**
     * Returns an exact redirect URI only after explicit prior validation.
     *
     * @param failure closed internal authorization failure
     * @return validated redirect URI or {@code null}
     */
    private static String validatedRedirect(final Outcome.Failure failure) {
        final JsonValue validated = failure.details().values().get(Builder.REDIRECT_VALIDATED);
        final JsonValue redirect = failure.details().values().get(OAuth2.Parameters.REDIRECT_URI);
        return validated instanceof JsonValue.BooleanValue flag && flag.value()
                && redirect instanceof JsonValue.StringValue text ? text.value() : null;
    }

    /**
     * Appends standard authorization error parameters to an absolute native- or web-client redirect URI.
     *
     * @param redirectUri exact redirect URI previously validated against client registration
     * @param error       standard authorization error response
     * @return complete Location field value
     * @throws ValidateException if URI syntax or existing response-parameter multiplicity is unsafe
     */
    private static String redirectLocation(final String redirectUri, final AuthorizationErrorResponse error) {
        final URI target;
        try {
            target = new URI(redirectUri);
        } catch (URISyntaxException exception) {
            throw new ValidateException("OAuth 2.x authorization error redirect URI is invalid", exception);
        }
        if (!target.isAbsolute() || target.getRawUserInfo() != null || target.getRawFragment() != null) {
            throw new ValidateException(
                    "OAuth 2.x authorization error redirect must be absolute, userinfo-free, and fragment-free");
        }
        final QueryCodec codec = new QueryCodec();
        final List<NameValue> existing = target.getRawQuery() == null ? List.of() : codec.decode(target.getRawQuery());
        final Set<String> reserved = Set.of(
                OAuth2.Parameters.ERROR,
                OAuth2.Parameters.ERROR_DESCRIPTION,
                OAuth2.Parameters.ERROR_URI,
                OAuth2.Parameters.STATE);
        if (existing.stream().anyMatch(parameter -> reserved.contains(parameter.name()))) {
            throw new ValidateException("OAuth 2.x redirect URI predefines an authorization error parameter");
        }
        final List<NameValue> parameters = new ArrayList<>();
        parameters.add(new NameValue(OAuth2.Parameters.ERROR, error.error().value()));
        error.errorDescription()
                .ifPresent(value -> parameters.add(new NameValue(OAuth2.Parameters.ERROR_DESCRIPTION, value)));
        error.errorUri().ifPresent(value -> parameters.add(new NameValue(OAuth2.Parameters.ERROR_URI, value)));
        error.state().ifPresent(value -> parameters.add(new NameValue(OAuth2.Parameters.STATE, value)));
        final String query = codec.encode(parameters);
        final String separator = target.getRawQuery() == null ? Symbol.QUESTION_MARK
                : target.getRawQuery().isEmpty() || target.getRawQuery().endsWith(Symbol.AND) ? Normal.EMPTY
                        : Symbol.AND;
        return redirectUri + separator + query;
    }

    /**
     * Creates an empty HTTP response with cache prevention.
     *
     * @param request originating HTTP request
     * @param status  HTTP status
     * @return complete empty response
     */
    private static Response empty(final Request request, final int status) {
        return Response.builder().request(request).code(status).headers(noStoreHeaders().build()).build();
    }

    /**
     * Creates the standard cache-prevention header builder used by OAuth responses.
     *
     * @return mutable header builder containing no-store and no-cache directives
     */
    private static HeadersBuilder noStoreHeaders() {
        return Headers.builder().add(Http.Header.CACHE_CONTROL, Http.Cache.NO_STORE)
                .add(Http.Header.PRAGMA, Http.Cache.NO_CACHE);
    }

    /**
     * Selects the HTTP status for one OAuth error.
     *
     * @param error   mapped standard error
     * @param failure source failure
     * @return standard HTTP status
     */
    private static int status(final OAuth2ErrorCode error, final Outcome.Failure failure) {
        if (OAuth2ErrorCode.INVALID_CLIENT.equals(error)) {
            return Http.Status.UNAUTHORIZED;
        }
        if (OAuth2ErrorCode.TEMPORARILY_UNAVAILABLE.equals(error) || same(failure, ErrorCode._408)) {
            return Http.Status.SERVICE_UNAVAILABLE;
        }
        return OAuth2ErrorCode.SERVER_ERROR.equals(error) || serverFailure(failure) ? Http.Status.INTERNAL_SERVER_ERROR
                : Http.Status.BAD_REQUEST;
    }

    /**
     * Selects the empty HTTP status for the metadata resource.
     *
     * @param failure source failure
     * @return metadata HTTP status
     */
    private static int metadataStatus(final Outcome.Failure failure) {
        if (same(failure, ErrorCode._408)) {
            return Http.Status.SERVICE_UNAVAILABLE;
        }
        if (same(failure, ErrorCode._401)) {
            return Http.Status.UNAUTHORIZED;
        }
        final String key = failure.error().getKey();
        return key.length() == 3 && key.charAt(0) == Symbol.C_FOUR ? Http.Status.BAD_REQUEST
                : Http.Status.INTERNAL_SERVER_ERROR;
    }

    /**
     * Detects a Basic client-authentication attempt without interpreting credentials.
     *
     * @param request originating HTTP request
     * @return whether the Authorization scheme is Basic
     */
    private static boolean basic(final Request request) {
        final String authorization = request.headers().get(Http.Header.AUTHORIZATION);
        return authorization != null && authorization.length() > 6
                && authorization.regionMatches(true, 0, "Basic ", 0, 6);
    }

    /**
     * Tests whether an internal description is safe for the RFC 6749 NQSCHAR wire grammar.
     *
     * @param value internal safe description
     * @return whether every character may be emitted as error_description
     */
    private static boolean nqschar(final String value) {
        for (int index = 0; index < value.length(); index++) {
            final char character = value.charAt(index);
            if (character < 0x20 || character > 0x7e || character == 0x22 || character == 0x5c) {
                return false;
            }
        }
        return true;
    }

    /**
     * Determines whether a failure belongs to the internal server-error class.
     *
     * @param failure closed internal failure
     * @return whether the Bus error key is a five-hundred status
     */
    private static boolean serverFailure(final Outcome.Failure failure) {
        final String key = failure.error().getKey();
        return key.length() == 3 && key.charAt(0) == Symbol.C_FIVE;
    }

    /**
     * Compares one failure with a shared Bus error definition by stable key.
     *
     * @param failure closed internal failure
     * @param error   shared Bus error definition
     * @return whether the stable keys are equal
     */
    private static boolean same(final Outcome.Failure failure, final Errors error) {
        return failure.error().getKey().equals(error.getKey());
    }

    /**
     * Maps an authorization failure to a safe redirect or a local standard JSON error.
     *
     * @param request       originating Fabric HTTP request
     * @param authorization decoded authorization request supplying only validated state
     * @param failure       closed internal failure
     * @return complete authorization endpoint response
     */
    public Response authorization(
            final Request request,
            final AuthorizationRequest authorization,
            final Outcome.Failure failure) {
        Assert.notNull(request, "OAuth 2.x authorization HTTP request must not be null");
        Assert.notNull(authorization, "OAuth 2.x decoded authorization request must not be null");
        Assert.notNull(failure, "OAuth 2.x authorization failure must not be null");
        final AuthorizationErrorResponse error = authorizationResponse(
                failure,
                AUTHORIZATION_ERRORS,
                OAuth2ErrorCode.INVALID_REQUEST,
                authorization.state());
        final String redirectUri = validatedRedirect(failure);
        if (redirectUri == null) {
            return authorizationJson(request, error, status(error.error(), failure));
        }
        try {
            final String location = redirectLocation(redirectUri, error);
            return Response.builder().request(request).code(Http.Status.FOUND)
                    .headers(
                            Headers.of(
                                    Http.Header.LOCATION,
                                    location,
                                    Http.Header.CACHE_CONTROL,
                                    Http.Cache.NO_STORE,
                                    Http.Header.PRAGMA,
                                    Http.Cache.NO_CACHE))
                    .build();
        } catch (RuntimeException exception) {
            return authorizationJson(request, error, status(error.error(), failure));
        }
    }

    /**
     * Maps an undecodable authorization request without reflecting unverified redirect or state input.
     *
     * @param request originating Fabric HTTP request
     * @return local invalid_request response
     */
    public Response authorizationMalformed(final Request request) {
        return malformed(request);
    }

    /**
     * Maps a token endpoint failure to a standard JSON response.
     *
     * @param request originating Fabric HTTP request
     * @param failure closed internal failure
     * @return complete token endpoint response
     */
    public Response token(final Request request, final Outcome.Failure failure) {
        return endpoint(request, failure, TOKEN_ERRORS);
    }

    /**
     * Maps an undecodable token request to invalid_request.
     *
     * @param request originating Fabric HTTP request
     * @param failure shared Bus parsing failure, optionally carrying a registered OAuth error identifier
     * @return standard malformed token response
     */
    public Response tokenMalformed(final Request request, final RuntimeException failure) {
        Assert.notNull(request, "OAuth 2.x token HTTP request must not be null");
        Assert.notNull(failure, "OAuth 2.x token parsing failure must not be null");
        final OAuth2ErrorCode error = failure instanceof UncheckedException unchecked
                && OAuth2ErrorCode.UNSUPPORTED_GRANT_TYPE.value().equals(unchecked.getErrcode())
                        ? OAuth2ErrorCode.UNSUPPORTED_GRANT_TYPE
                        : OAuth2ErrorCode.INVALID_REQUEST;
        return tokenJson(
                request,
                new TokenErrorResponse(error, Optional.empty(), Optional.empty(), new JsonValue.ObjectValue(Map.of())),
                Http.Status.BAD_REQUEST);
    }

    /**
     * Maps an introspection endpoint failure without fabricating an active response.
     *
     * @param request originating Fabric HTTP request
     * @param failure closed internal failure
     * @return complete introspection error response
     */
    public Response introspection(final Request request, final Outcome.Failure failure) {
        return endpoint(request, failure, INTROSPECTION_ERRORS);
    }

    /**
     * Maps an undecodable introspection request to invalid_request.
     *
     * @param request originating Fabric HTTP request
     * @return standard malformed introspection response
     */
    public Response introspectionMalformed(final Request request) {
        return malformed(request);
    }

    /**
     * Maps a revocation endpoint failure while preserving RFC 7009 error names.
     *
     * @param request originating Fabric HTTP request
     * @param failure closed internal failure
     * @return complete revocation error response
     */
    public Response revocation(final Request request, final Outcome.Failure failure) {
        return endpoint(request, failure, REVOCATION_ERRORS);
    }

    /**
     * Maps an undecodable revocation request to invalid_request.
     *
     * @param request originating Fabric HTTP request
     * @return standard malformed revocation response
     */
    public Response revocationMalformed(final Request request) {
        return malformed(request);
    }

    /**
     * Maps a device authorization endpoint failure to RFC 8628-compatible JSON.
     *
     * @param request originating Fabric HTTP request
     * @param failure closed internal failure
     * @return complete device authorization error response
     */
    public Response deviceAuthorization(final Request request, final Outcome.Failure failure) {
        return endpoint(request, failure, DEVICE_ERRORS);
    }

    /**
     * Maps an undecodable device authorization request to invalid_request.
     *
     * @param request originating Fabric HTTP request
     * @return standard malformed device authorization response
     */
    public Response deviceAuthorizationMalformed(final Request request) {
        return malformed(request);
    }

    /**
     * Maps a metadata service failure to an empty HTTP error because RFC 8414 defines no OAuth error envelope here.
     *
     * @param request originating Fabric HTTP request
     * @param failure closed internal failure
     * @return empty metadata error response
     */
    public Response metadata(final Request request, final Outcome.Failure failure) {
        Assert.notNull(request, "OAuth 2.x metadata HTTP request must not be null");
        Assert.notNull(failure, "OAuth 2.x metadata failure must not be null");
        return empty(request, metadataStatus(failure));
    }

    /**
     * Maps an invalid metadata resource request to an empty bad-request response.
     *
     * @param request originating Fabric HTTP request
     * @return empty metadata bad-request response
     */
    public Response metadataMalformed(final Request request) {
        Assert.notNull(request, "OAuth 2.x metadata HTTP request must not be null");
        return empty(request, Http.Status.BAD_REQUEST);
    }

    /**
     * Maps one non-authorization OAuth endpoint failure through its allowed error set.
     *
     * @param request originating HTTP request
     * @param failure closed internal failure
     * @param allowed exact endpoint error allow-list
     * @return complete JSON error response
     */
    private Response endpoint(
            final Request request,
            final Outcome.Failure failure,
            final Set<OAuth2ErrorCode> allowed) {
        Assert.notNull(request, "OAuth 2.x HTTP request must not be null");
        Assert.notNull(failure, "OAuth 2.x endpoint failure must not be null");
        final TokenErrorResponse error = tokenResponse(failure, allowed, OAuth2ErrorCode.INVALID_REQUEST);
        return tokenJson(request, error, status(error.error(), failure));
    }

    /**
     * Creates the fixed malformed-request JSON response shared by OAuth form endpoints.
     *
     * @param request originating HTTP request
     * @return standard invalid_request response
     */
    private Response malformed(final Request request) {
        Assert.notNull(request, "OAuth 2.x malformed HTTP request must not be null");
        return tokenJson(
                request,
                new TokenErrorResponse(OAuth2ErrorCode.INVALID_REQUEST, Optional.empty(), Optional.empty(),
                        new JsonValue.ObjectValue(Map.of())),
                Http.Status.BAD_REQUEST);
    }

    /**
     * Serializes a standard OAuth error model as UTF-8 JSON with mandatory no-store headers.
     *
     * @param request originating HTTP request
     * @param error   standard OAuth error parameters
     * @param status  HTTP status
     * @return complete JSON response
     */
    private Response tokenJson(final Request request, final TokenErrorResponse error, final int status) {
        final Map<String, JsonValue> members = new LinkedHashMap<>();
        members.put(OAuth2.Parameters.ERROR, new JsonValue.StringValue(error.error().value()));
        error.errorDescription()
                .ifPresent(value -> members.put(OAuth2.Parameters.ERROR_DESCRIPTION, new JsonValue.StringValue(value)));
        error.errorUri().ifPresent(value -> members.put(OAuth2.Parameters.ERROR_URI, new JsonValue.StringValue(value)));
        error.extensions().values().forEach(members::put);
        final HeadersBuilder headers = noStoreHeaders();
        if (status == Http.Status.UNAUTHORIZED && basic(request)) {
            headers.add(Http.Header.WWW_AUTHENTICATE, "Basic realm=\"oauth2\"");
        }
        final byte[] body = jsonProvider.writeValue(new JsonValue.ObjectValue(members));
        return Response.builder().request(request).code(status).headers(headers.build())
                .body(Body.of(body, MediaType.APPLICATION_JSON_TYPE)).build();
    }

    /**
     * Serializes a standard authorization error for a local non-redirect response.
     *
     * @param request originating HTTP request
     * @param error   standard authorization error response
     * @param status  HTTP status
     * @return complete JSON response
     */
    private Response authorizationJson(
            final Request request,
            final AuthorizationErrorResponse error,
            final int status) {
        final Map<String, JsonValue> members = new LinkedHashMap<>();
        members.put(OAuth2.Parameters.ERROR, new JsonValue.StringValue(error.error().value()));
        error.errorDescription()
                .ifPresent(value -> members.put(OAuth2.Parameters.ERROR_DESCRIPTION, new JsonValue.StringValue(value)));
        error.errorUri().ifPresent(value -> members.put(OAuth2.Parameters.ERROR_URI, new JsonValue.StringValue(value)));
        error.state().ifPresent(value -> members.put(OAuth2.Parameters.STATE, new JsonValue.StringValue(value)));
        error.extensions().values().forEach(members::put);
        final byte[] body = jsonProvider.writeValue(new JsonValue.ObjectValue(members));
        return Response.builder().request(request).code(status).headers(noStoreHeaders().build())
                .body(Body.of(body, MediaType.APPLICATION_JSON_TYPE)).build();
    }

}
