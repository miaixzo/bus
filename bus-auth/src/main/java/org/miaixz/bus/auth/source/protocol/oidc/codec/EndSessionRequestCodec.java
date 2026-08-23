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
package org.miaixz.bus.auth.source.protocol.oidc.codec;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.auth.FabricX.*;
import org.miaixz.bus.auth.codec.NameValue;
import org.miaixz.bus.auth.codec.QueryCodec;
import org.miaixz.bus.auth.source.protocol.oauth2.OAuth2;
import org.miaixz.bus.auth.source.protocol.oidc.EndSessionRequest;
import org.miaixz.bus.auth.source.protocol.oidc.OpenIdConnect;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.Protocol;

/**
 * Encodes and decodes OpenID Connect RP-Initiated Logout requests without defining a response entity.
 * <p>
 * The codec owns only transport grammar. It does not verify an ID Token or logout hint, resolve a client, validate a
 * post-logout URI against registration, or modify session state. A success redirect is produced only after the service
 * has returned success for the same immutable request.
 * </p>
 *
 * @author Kimi Liu
 */
public class EndSessionRequestCodec {

    /**
     * Creates a stateless RP-Initiated Logout codec.
     */
    public EndSessionRequestCodec() {
        // No initialization required.
    }

    /**
     * Copies query parameters into a unique registered-name map.
     *
     * @param url parsed request URL
     * @return immutable-name mutable-value map
     * @throws ValidateException if a name is unknown or repeated
     */
    private static Map<String, String> parameters(final Url url) {
        final Map<String, String> values = new LinkedHashMap<>(url.querySize());
        for (int index = 0; index < url.querySize(); index++) {
            final String name = url.queryParameterName(index);
            if (!registered(name)) {
                throw new ValidateException("OpenID Connect end-session request contains an unknown parameter");
            }
            if (values.putIfAbsent(name, url.queryParameterValue(index)) != null) {
                throw new ValidateException("OpenID Connect end-session parameters must not be repeated");
            }
        }
        return values;
    }

    /**
     * Identifies RP-Initiated Logout request parameters represented by the typed request.
     *
     * @param name exact query parameter name
     * @return {@code true} for a standard end-session request parameter
     */
    private static boolean registered(final String name) {
        return switch (name) {
            case OpenIdConnect.Parameters.ID_TOKEN_HINT, OpenIdConnect.Parameters.LOGOUT_HINT, OAuth2.Parameters.CLIENT_ID, OpenIdConnect.Parameters.POST_LOGOUT_REDIRECT_URI, OAuth2.Parameters.STATE, OpenIdConnect.Parameters.UI_LOCALES -> true;
            default -> false;
        };
    }

    /**
     * Parses optional single-space-delimited UI locale preferences.
     *
     * @param value optional wire value
     * @return immutable locale sequence
     * @throws ValidateException if a present value is empty or has repeated separators
     */
    private static List<String> spaceSeparated(final String value) {
        if (value == null) {
            return List.of();
        }
        if (value.isEmpty()) {
            throw new ValidateException("OpenID Connect logout ui_locales must not be empty");
        }
        final String[] parts = value.split(Symbol.SPACE, -1);
        final List<String> locales = new ArrayList<>(parts.length);
        for (String part : parts) {
            if (part.isEmpty()) {
                throw new ValidateException("OpenID Connect logout ui_locales must use one ASCII space between values");
            }
            locales.add(part);
        }
        return List.copyOf(locales);
    }

    /**
     * Validates an absolute HTTPS endpoint and ensures it does not predefine owned parameters.
     *
     * @param endpoint candidate deployment endpoint
     * @throws ValidateException if endpoint shape or query ownership is invalid
     */
    private static void validateEndpoint(final Url endpoint) {
        final URI uri = endpoint.toUri();
        if (!uri.isAbsolute() || !Protocol.HTTPS.name.equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null
                || uri.getRawUserInfo() != null || uri.getRawFragment() != null) {
            throw new ValidateException(
                    "OpenID Connect end-session endpoint must be absolute userinfo-free HTTPS without fragment");
        }
        for (String name : endpoint.queryParameterNames()) {
            if (registered(name)) {
                throw new ValidateException(
                        "OpenID Connect end-session endpoint predefines request parameter: " + name);
            }
        }
    }

    /**
     * Appends optional state to an exact service-validated post-logout redirect URI.
     *
     * @param redirect exact registered redirect URI
     * @param state    optional state value
     * @return exact redirect with optional encoded state query parameter
     * @throws ValidateException if URI syntax or existing state multiplicity is unsafe
     */
    private static String appendState(final String redirect, final String state) {
        final URI uri;
        try {
            uri = new URI(redirect);
        } catch (URISyntaxException exception) {
            throw new ValidateException("Validated OpenID Connect post-logout redirect URI is invalid", exception);
        }
        if (!uri.isAbsolute() || uri.getRawFragment() != null || uri.getRawUserInfo() != null) {
            throw new ValidateException(
                    "Validated OpenID Connect post-logout redirect must be absolute and fragment-free");
        }
        if (state == null) {
            return redirect;
        }
        final QueryCodec codec = new QueryCodec();
        final List<NameValue> existing = uri.getRawQuery() == null ? List.of() : codec.decode(uri.getRawQuery());
        if (existing.stream().anyMatch(parameter -> OAuth2.Parameters.STATE.equals(parameter.name()))) {
            throw new ValidateException("OpenID Connect post-logout redirect URI already contains state");
        }
        final String separator = uri.getRawQuery() == null ? Symbol.QUESTION_MARK
                : uri.getRawQuery().isEmpty() || uri.getRawQuery().endsWith(Symbol.AND) ? Normal.EMPTY : Symbol.AND;
        return redirect + separator + codec.encode(List.of(new NameValue(OAuth2.Parameters.STATE, state)));
    }

    /**
     * Decodes one strict HTTPS GET end-session request.
     *
     * @param request immutable Fabric HTTP request
     * @return validated RP-Initiated Logout request model
     * @throws IllegalArgumentException if {@code request} is {@code null}
     * @throws ValidateException        if transport, body, multiplicity, name, or value syntax is invalid
     */
    public EndSessionRequest decodeRequest(final Request request) {
        Assert.notNull(request, "OpenID Connect end-session HTTP request must not be null");
        if (request.method() != Http.Method.GET || !Protocol.HTTPS.name.equalsIgnoreCase(request.url().scheme())) {
            throw new ValidateException("OpenID Connect end-session endpoint requires HTTPS GET");
        }
        if (request.url().fragment() != null || request.body().length() != 0L) {
            throw new ValidateException("OpenID Connect end-session GET must not contain fragment or body");
        }
        final Map<String, String> values = parameters(request.url());
        return new EndSessionRequest(Optional.ofNullable(values.get(OpenIdConnect.Parameters.ID_TOKEN_HINT)),
                Optional.ofNullable(values.get(OpenIdConnect.Parameters.LOGOUT_HINT)),
                Optional.ofNullable(values.get(OAuth2.Parameters.CLIENT_ID)),
                Optional.ofNullable(values.get(OpenIdConnect.Parameters.POST_LOGOUT_REDIRECT_URI)),
                Optional.ofNullable(values.get(OAuth2.Parameters.STATE)),
                spaceSeparated(values.get(OpenIdConnect.Parameters.UI_LOCALES)));
    }

    /**
     * Appends one RP-Initiated Logout request to a configured absolute HTTPS endpoint.
     *
     * @param endpoint validated deployment endpoint URL
     * @param request  standard logout request
     * @return complete immutable user-agent URL
     * @throws IllegalArgumentException if an argument is {@code null}
     * @throws ValidateException        if endpoint transport or query ownership is invalid
     */
    public Url encode(final Url endpoint, final EndSessionRequest request) {
        Assert.notNull(endpoint, "OpenID Connect end-session endpoint must not be null");
        Assert.notNull(request, "OpenID Connect end-session request must not be null");
        validateEndpoint(endpoint);
        final UrlBuilder url = endpoint.newBuilder();
        request.idTokenHint().ifPresent(value -> url.query(OpenIdConnect.Parameters.ID_TOKEN_HINT, value));
        request.logoutHint().ifPresent(value -> url.query(OpenIdConnect.Parameters.LOGOUT_HINT, value));
        request.clientId().ifPresent(value -> url.query(OAuth2.Parameters.CLIENT_ID, value));
        request.postLogoutRedirectUri()
                .ifPresent(value -> url.query(OpenIdConnect.Parameters.POST_LOGOUT_REDIRECT_URI, value));
        request.state().ifPresent(value -> url.query(OAuth2.Parameters.STATE, value));
        if (!request.uiLocales().isEmpty()) {
            url.query(OpenIdConnect.Parameters.UI_LOCALES, String.join(Symbol.SPACE, request.uiLocales()));
        }
        return url.build();
    }

    /**
     * Encodes an empty successful logout response or an already service-validated post-logout redirect.
     *
     * @param request originating Fabric HTTP request
     * @param decoded exact request already accepted by the end-session service
     * @return empty HTTP 204 or 302 response with cache prevention
     * @throws IllegalArgumentException if an argument is {@code null}
     * @throws ValidateException        if a validated redirect cannot safely receive state
     */
    public Response encodeSuccess(final Request request, final EndSessionRequest decoded) {
        Assert.notNull(request, "OpenID Connect end-session HTTP request must not be null");
        Assert.notNull(decoded, "OpenID Connect validated end-session request must not be null");
        final HeadersBuilder headers = Headers.builder().add(Http.Header.CACHE_CONTROL, Http.Cache.NO_STORE)
                .add(Http.Header.PRAGMA, Http.Cache.NO_CACHE);
        final String redirect = decoded.postLogoutRedirectUri().getOrNull();
        if (redirect == null) {
            return Response.builder().request(request).code(Http.Status.NO_CONTENT).headers(headers.build()).build();
        }
        headers.add(Http.Header.LOCATION, appendState(redirect, decoded.state().getOrNull()));
        return Response.builder().request(request).code(Http.Status.FOUND).headers(headers.build()).build();
    }

}
