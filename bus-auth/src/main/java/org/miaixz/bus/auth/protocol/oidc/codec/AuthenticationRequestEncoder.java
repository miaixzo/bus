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
package org.miaixz.bus.auth.protocol.oidc.codec;

import java.net.URI;

import org.miaixz.bus.auth.protocol.oidc.AuthenticationRequest;
import org.miaixz.bus.auth.protocol.oidc.OpenIdConnect;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.extra.json.JsonProvider;
import org.miaixz.bus.extra.json.JsonValue;
import org.miaixz.bus.fabric.UnoUrl;

/**
 * Appends OpenID Connect Authentication Request parameters to an already encoded OAuth authorization URL.
 * <p>
 * The shared OAuth encoder remains the sole owner of response type, client, redirect, scope, state, PKCE, resource, and
 * OAuth extension encoding. This class appends only OIDC members in deterministic protocol order and performs no
 * browser navigation, network access, policy evaluation, or state generation.
 * </p>
 *
 * @author Kimi Liu
 */
public final class AuthenticationRequestEncoder {

    /**
     * Externally selected provider-neutral JSON implementation.
     */
    private final JsonProvider jsonProvider;

    /**
     * Creates an OIDC Authentication Request encoder.
     *
     * @param jsonProvider externally selected provider-neutral JSON implementation
     * @throws IllegalArgumentException if {@code jsonProvider} is {@code null}
     */
    public AuthenticationRequestEncoder(final JsonProvider jsonProvider) {
        this.jsonProvider = Assert
                .notNull(jsonProvider, "OpenID Connect Authentication Request JSON provider must not be null");
    }

    /**
     * Validates the already encoded OAuth URL before OIDC parameters are appended.
     *
     * @param url candidate OAuth authorization URL
     * @throws ValidateException if transport, authority, userinfo, fragment, or query ownership is invalid
     */
    private static void validateBase(final UnoUrl url) {
        final URI uri = url.toUri();
        if (!uri.isAbsolute() || !Protocol.HTTPS.name.equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null
                || uri.getRawUserInfo() != null || uri.getRawFragment() != null) {
            throw new ValidateException(
                    "OpenID Connect OAuth authorization URL must be absolute userinfo-free HTTPS without fragment");
        }
        for (String name : url.queryParameterNames()) {
            if (registered(name)) {
                throw new ValidateException(
                        "OpenID Connect OAuth authorization URL already contains an OIDC parameter: " + name);
            }
        }
    }

    /**
     * Appends one caller-defined scalar OIDC extension parameter.
     *
     * @param url   destination URL builder
     * @param name  exact extension name
     * @param value provider-neutral scalar value
     * @throws ValidateException if a registered name or non-scalar JSON value is supplied
     */
    private static void extension(final UnoUrl.Builder url, final String name, final JsonValue value) {
        Assert.notBlank(name, "OpenID Connect Authentication extension name must not be blank");
        if (registered(name)) {
            throw new ValidateException("OpenID Connect Authentication extension duplicates a registered parameter");
        }
        if (value instanceof JsonValue.StringValue string) {
            url.query(name, string.value());
        } else if (value instanceof JsonValue.NumberValue number) {
            url.query(name, number.value().toString());
        } else if (value instanceof JsonValue.BooleanValue flag) {
            url.query(name, Boolean.toString(flag.value()));
        } else {
            throw new ValidateException("OpenID Connect Authentication query extensions must be JSON scalars");
        }
    }

    /**
     * Identifies OpenID Connect request parameters represented by explicit components.
     *
     * @param name exact query parameter name
     * @return {@code true} for a typed OpenID Connect request parameter
     */
    private static boolean registered(final String name) {
        return switch (name) {
            case OpenIdConnect.Parameters.NONCE, OpenIdConnect.Parameters.DISPLAY, OpenIdConnect.Parameters.PROMPT, OpenIdConnect.Parameters.MAX_AGE, OpenIdConnect.Parameters.UI_LOCALES, OpenIdConnect.Parameters.ID_TOKEN_HINT, OpenIdConnect.Parameters.LOGIN_HINT, OpenIdConnect.Parameters.ACR_VALUES, OpenIdConnect.Parameters.CLAIMS, OpenIdConnect.Parameters.RESPONSE_MODE, OpenIdConnect.Parameters.REQUEST, OpenIdConnect.Parameters.REQUEST_URI -> true;
            default -> false;
        };
    }

    /**
     * Appends one typed OIDC request to the corresponding OAuth authorization URL.
     *
     * @param oauthAuthorizationUrl URL already encoded from {@code request.authorizationRequest()}
     * @param request               validated OIDC Authentication Request
     * @return complete immutable authorization URL
     * @throws IllegalArgumentException if an argument is {@code null}
     * @throws ValidateException        if the base URL or an extension violates the query contract
     */
    public UnoUrl encode(final UnoUrl oauthAuthorizationUrl, final AuthenticationRequest request) {
        Assert.notNull(oauthAuthorizationUrl, "OpenID Connect OAuth authorization URL must not be null");
        Assert.notNull(request, "OpenID Connect Authentication Request must not be null");
        validateBase(oauthAuthorizationUrl);
        final UnoUrl.Builder url = oauthAuthorizationUrl.newBuilder();
        request.nonce().ifPresent(value -> url.query(OpenIdConnect.Parameters.NONCE, value));
        request.display().ifPresent(value -> url.query(OpenIdConnect.Parameters.DISPLAY, value.value()));
        request.prompt().ifPresent(value -> url.query(OpenIdConnect.Parameters.PROMPT, value.format()));
        request.maxAge().ifPresent(value -> url.query(OpenIdConnect.Parameters.MAX_AGE, Long.toString(value)));
        if (!request.uiLocales().isEmpty()) {
            url.query(OpenIdConnect.Parameters.UI_LOCALES, String.join(Symbol.SPACE, request.uiLocales()));
        }
        request.idTokenHint().ifPresent(value -> url.query(OpenIdConnect.Parameters.ID_TOKEN_HINT, value));
        request.loginHint().ifPresent(value -> url.query(OpenIdConnect.Parameters.LOGIN_HINT, value));
        if (!request.acrValues().isEmpty()) {
            url.query(OpenIdConnect.Parameters.ACR_VALUES, String.join(Symbol.SPACE, request.acrValues()));
        }
        request.claims().ifPresent(value -> url.query(OpenIdConnect.Parameters.CLAIMS, json(value)));
        request.responseMode().ifPresent(value -> url.query(OpenIdConnect.Parameters.RESPONSE_MODE, value));
        request.extensions().values().forEach((name, value) -> extension(url, name, value));
        return url.build();
    }

    /**
     * Serializes a claims request as compact provider-neutral UTF-8 JSON.
     *
     * @param claims claims request object
     * @return JSON text
     */
    private String json(final JsonValue.ObjectValue claims) {
        return new String(jsonProvider.writeValue(claims), Charset.UTF_8);
    }

}
