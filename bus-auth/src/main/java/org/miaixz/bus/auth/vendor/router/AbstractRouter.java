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
package org.miaixz.bus.auth.vendor.router;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.miaixz.bus.auth.Callback;
import org.miaixz.bus.auth.vendor.VendorIdentity;
import org.miaixz.bus.auth.vendor.VendorRequestBuilder;
import org.miaixz.bus.auth.vendor.VendorTokenSet;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.fabric.Fabric;

/**
 * Shared OAuth2 client router implemented exclusively on an injected Fabric context.
 *
 * <p>
 * The router retains no credentials, does not use global transport state, and keeps vendor-specific behavior in a
 * single protected extension surface. Returned token and identity objects remain vendor client DTOs.
 * </p>
 *
 * @author Kimi Liu
 */
public abstract class AbstractRouter implements OAuth2Router {

    /**
     * Immutable case-sensitive spellings of standard OAuth2 and supported platform parameters.
     */
    private static final Set<String> STANDARD_PARAMS = Set.of(
            "client_id",
            "response_type",
            "redirect_uri",
            "scope",
            "state",
            "code",
            "grant_type",
            "refresh_token",
            "access_token",
            "client_secret",
            "username",
            "password",
            "prompt",
            "corp_id",
            "corpId");

    /**
     * Caller-owned Fabric context used by every network operation.
     */
    private final org.miaixz.bus.fabric.Context fabric;

    /**
     * Creates a router with an explicit Fabric context.
     *
     * @param fabric non-null caller-owned Fabric context
     */
    protected AbstractRouter(final org.miaixz.bus.fabric.Context fabric) {
        this.fabric = Objects.requireNonNull(fabric, "Fabric context must not be null");
    }

    /**
     * Returns a string response field.
     *
     * @param data response fields
     * @param name field name
     * @return scalar text, or {@code null}
     */
    private static String value(final Map<String, Object> data, final String name) {
        return scalar(data.get(name));
    }

    /**
     * Converts a scalar to text.
     *
     * @param value optional scalar
     * @return scalar text, or {@code null}
     */
    private static String scalar(final Object value) {
        return value == null ? null : StringKit.toString(value);
    }

    /**
     * Parses a JSON object into a string-keyed invocation-local map without unchecked casts.
     *
     * @param document JSON document
     * @return parsed object fields
     * @throws AuthorizedException if the document is not a JSON object or contains a non-string key
     */
    private static Map<String, Object> objectMap(final String document) {
        final Object parsed = JsonKit.toPojo(document, Object.class);
        if (!(parsed instanceof Map<?, ?> values)) {
            throw new AuthorizedException("OAuth router response must be a JSON object");
        }
        final Map<String, Object> result = new LinkedHashMap<>();
        values.forEach((name, value) -> {
            if (!(name instanceof String key)) {
                throw new AuthorizedException("OAuth router response contains a non-string field name");
            }
            result.put(key, value);
        });
        return Map.copyOf(result);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String buildUrl(
            final String authUrl,
            final String clientId,
            final String redirectUri,
            final String scope,
            final String state,
            final Map<String, Object> params) {
        final VendorRequestBuilder builder = VendorRequestBuilder.fromUrl(authUrl).queryParam("client_id", clientId)
                .queryParam("response_type", "code").queryParam("redirect_uri", redirectUri);
        if (scope != null) {
            builder.queryParam("scope", scope);
        }
        if (state != null) {
            builder.queryParam("state", state);
        }
        addPlatformAuthorizeParams(builder, params);
        appendExtensions(builder, params);
        return builder.build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VendorTokenSet getToken(
            final Callback.Inbound callback,
            final String tokenUrl,
            final String clientId,
            final char[] clientSecret,
            final String redirectUri,
            final Map<String, Object> params) {
        final Callback.Inbound inbound = Objects.requireNonNull(callback, "Callback must not be null");
        final char[] credential = Objects.requireNonNull(clientSecret, "Client secret must not be null");
        try {
            final VendorRequestBuilder builder = VendorRequestBuilder.fromUrl(tokenUrl)
                    .queryParam("grant_type", "authorization_code").queryParam("client_id", clientId)
                    .queryParam("client_secret", new String(credential))
                    .queryParam("code", inbound.value("code").orElse(null)).queryParam("redirect_uri", redirectUri);
            addPlatformTokenParams(builder, inbound, params);
            appendExtensions(builder, params);
            return buildAuthorization(objectMap(get(builder.build())));
        } catch (RuntimeException exception) {
            throw new AuthorizedException("OAuth router token request failed");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VendorIdentity getUserinfo(final VendorTokenSet authorization, final String userinfoUrl) {
        final VendorTokenSet token = Objects.requireNonNull(authorization, "Token set must not be null");
        try {
            final Map<String, String> headers = new LinkedHashMap<>();
            addUserinfoHeaders(headers, token);
            final VendorRequestBuilder builder = VendorRequestBuilder.fromUrl(userinfoUrl);
            addPlatformUserinfoParams(builder, token);
            final String document = get(builder.build(), null, headers);
            return buildClaims(objectMap(document), document);
        } catch (RuntimeException exception) {
            throw new AuthorizedException("OAuth router user-info request failed");
        }
    }

    /**
     * Adds platform-specific authorization parameters.
     *
     * @param builder mutable request builder owned by this invocation
     * @param params  optional extension parameters
     */
    protected void addPlatformAuthorizeParams(final VendorRequestBuilder builder, final Map<String, Object> params) {
        // Default client flow adds no platform fields.
    }

    /**
     * Adds platform-specific token parameters.
     *
     * @param builder  mutable request builder owned by this invocation
     * @param callback immutable inbound callback
     * @param params   optional extension parameters
     */
    protected void addPlatformTokenParams(
            final VendorRequestBuilder builder,
            final Callback.Inbound callback,
            final Map<String, Object> params) {
        // Default client flow adds no platform fields.
    }

    /**
     * Adds user-info headers.
     *
     * @param headers       invocation-local mutable headers
     * @param authorization non-null token set
     */
    protected void addUserinfoHeaders(final Map<String, String> headers, final VendorTokenSet authorization) {
        if (authorization.getToken() != null) {
            headers.put(Http.Header.AUTHORIZATION, Http.Auth.BEARER_PREFIX + authorization.getToken());
        }
    }

    /**
     * Adds platform-specific user-info query parameters.
     *
     * @param builder       mutable request builder owned by this invocation
     * @param authorization non-null token set
     */
    protected void addPlatformUserinfoParams(final VendorRequestBuilder builder, final VendorTokenSet authorization) {
        // Default client flow adds no platform fields.
    }

    /**
     * Maps a token response.
     *
     * @param data parsed response fields
     * @return mapped token set
     */
    protected VendorTokenSet buildAuthorization(final Map<String, Object> data) {
        return VendorTokenSet.builder().token(value(data, "access_token")).expireIn(getInt(data, "expires_in"))
                .refresh(value(data, "refresh_token")).scope(value(data, "scope")).build();
    }

    /**
     * Maps a user-info response without retaining a mutable response map.
     *
     * @param data    parsed response fields
     * @param rawJson original JSON response
     * @return mapped vendor identity
     */
    protected VendorIdentity buildClaims(final Map<String, Object> data, final String rawJson) {
        String username = value(data, "name");
        String email = value(data, "email");
        final Object nested = data.get("attributes");
        if (nested instanceof Map<?, ?> attributes) {
            if (username == null) {
                username = scalar(attributes.get("userName"));
            }
            if (email == null) {
                email = scalar(attributes.get("email"));
            }
        }
        return VendorIdentity.builder().uuid(value(data, "id")).username(username).email(email)
                .nickname(value(data, "nickname")).avatar(value(data, "avatar")).rawJson(rawJson).build();
    }

    /**
     * Encodes a redirect URI and opaque state as a Base64 JSON envelope.
     *
     * @param redirectUri redirect URI
     * @param state       opaque state
     * @return prefixed encoded envelope
     */
    protected String encodeState(final String redirectUri, final String state) {
        final Map<String, String> values = new LinkedHashMap<>();
        values.put("redirectUri", redirectUri);
        values.put("state", state);
        return "oauth:" + Base64.encode(JsonKit.toJsonString(values).getBytes(Charset.UTF_8));
    }

    /**
     * Decodes an encoded state envelope or preserves an unencoded state.
     *
     * @param encodedState encoded or opaque state
     * @return immutable redirect and state values
     */
    protected Map<String, String> decodeState(final String encodedState) {
        final String current = Objects.requireNonNull(encodedState, "Encoded state must not be null");
        if (!current.startsWith("oauth:")) {
            return Map.of("state", current);
        }
        final Map<String, Object> values = objectMap(
                new String(Base64.decode(current.substring("oauth:".length())), Charset.UTF_8));
        final Map<String, String> result = new LinkedHashMap<>();
        if (values.get("redirectUri") != null) {
            result.put("redirectUri", scalar(values.get("redirectUri")));
        }
        if (values.get("state") != null) {
            result.put("state", scalar(values.get("state")));
        }
        return result;
    }

    /**
     * Prefixes a token with a platform discriminator.
     *
     * @param token  sensitive token
     * @param prefix platform discriminator
     * @return prefixed token
     */
    protected String encodeToken(final String token, final String prefix) {
        return prefix + Symbol.COLON + token;
    }

    /**
     * Removes a matching platform discriminator.
     *
     * @param encodedToken prefixed or plain token
     * @param prefix       expected platform discriminator
     * @return plain token
     */
    protected String decodeToken(final String encodedToken, final String prefix) {
        final String marker = prefix + Symbol.COLON;
        return encodedToken.startsWith(marker) ? encodedToken.substring(marker.length()) : encodedToken;
    }

    /**
     * Sends a JSON POST without additional headers.
     *
     * @param url  target URL
     * @param data JSON object fields
     * @return response text
     */
    protected String postJson(final String url, final Map<String, Object> data) {
        return postJson(url, data, null);
    }

    /**
     * Sends a JSON POST with headers.
     *
     * @param url     target URL
     * @param data    JSON object fields
     * @param headers optional request headers
     * @return response text
     */
    protected String postJson(final String url, final Map<String, Object> data, final Map<String, String> headers) {
        return post(url, JsonKit.toJsonString(data), headers, MediaType.APPLICATION_JSON);
    }

    /**
     * Sends a GET through the injected Fabric context.
     *
     * @param url target URL
     * @return response text
     */
    protected String get(final String url) {
        return get(url, null, null);
    }

    /**
     * Sends a GET with optional query and headers through the injected Fabric context.
     *
     * @param url     target URL
     * @param query   optional query values
     * @param headers optional request headers
     * @return response text
     */
    protected String get(final String url, final Map<String, ?> query, final Map<String, String> headers) {
        final var request = Fabric.http(fabric).get(url);
        if (query != null) {
            query.forEach((name, value) -> {
                if (name != null && value != null) {
                    request.query(name, value);
                }
            });
        }
        if (headers != null) {
            headers.forEach((name, value) -> {
                if (name != null && value != null) {
                    request.header(name, value);
                }
            });
        }
        return request.executeText();
    }

    /**
     * Sends a raw POST through the injected Fabric context.
     *
     * @param url         target URL
     * @param body        request body
     * @param headers     optional request headers
     * @param contentType validated content type
     * @return response text
     */
    protected String post(
            final String url,
            final String body,
            final Map<String, String> headers,
            final String contentType) {
        final var request = Fabric.http(fabric).post(url).body(body, MediaType.parse(contentType));
        if (headers != null) {
            headers.forEach((name, value) -> {
                if (name != null && value != null) {
                    request.header(name, value);
                }
            });
        }
        return request.executeText();
    }

    /**
     * Reports whether a parameter belongs to the fixed standard set.
     *
     * @param param parameter name
     * @return {@code true} for a standard parameter
     */
    protected boolean isStandardParam(final String param) {
        if (StringKit.isEmpty(param)) {
            return false;
        }
        return STANDARD_PARAMS.stream().anyMatch(value -> value.equalsIgnoreCase(param));
    }

    /**
     * Reads an integral JSON number.
     *
     * @param map response fields
     * @param key field name
     * @return integer value, or zero for an absent/non-numeric field
     */
    protected int getInt(final Map<String, Object> map, final String key) {
        final Object value = map.get(key);
        return value instanceof Number number ? number.intValue() : 0;
    }

    /**
     * Appends non-standard extension parameters.
     *
     * @param builder invocation-local request builder
     * @param params  optional extension parameters
     */
    private void appendExtensions(final VendorRequestBuilder builder, final Map<String, Object> params) {
        if (params != null) {
            params.forEach((name, value) -> {
                if (!isStandardParam(name) && value != null) {
                    builder.queryParam(name, StringKit.toString(value));
                }
            });
        }
    }

}
