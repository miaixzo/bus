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
package org.miaixz.bus.auth.vendor;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import org.miaixz.bus.auth.Callback;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Descriptor;
import org.miaixz.bus.auth.Endpoint;
import org.miaixz.bus.auth.cache.StateStore;
import org.miaixz.bus.auth.codec.state.StateEnvelopeCodec;
import org.miaixz.bus.auth.guard.ReplayKey;
import org.miaixz.bus.auth.resolver.SecretResolver;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.data.id.ID;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.net.url.UrlEncoder;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.Fabric;
import org.miaixz.bus.fabric.Payload;

/**
 * Shared client implementation for third-party authentication vendors.
 *
 * <p>
 * The base owns only explicit runtime dependencies. HTTP requests are created from the injected Fabric context,
 * security time comes from the injected Fabric clock, authorization state uses the injected atomic store, and client
 * secrets are resolved for each root operation context without being retained in fields.
 * </p>
 *
 * @author Kimi Liu
 */
public abstract class AbstractProvider implements VendorProvider {

    /**
     * Immutable non-secret vendor client registration.
     */
    protected final VendorRegistration registration;

    /**
     * Immutable vendor metadata and provider factory definition.
     */
    protected final VendorDefinition definition;

    /**
     * Caller-owned Fabric context used by every vendor HTTP request.
     */
    protected final org.miaixz.bus.fabric.Context fabric;

    /**
     * Explicit Fabric clock used by every security deadline decision.
     */
    protected final org.miaixz.bus.fabric.Clock clock;

    /**
     * Tenant-aware atomic authorization state store.
     */
    protected final StateStore stateStore;

    /**
     * Caller-owned resolver for operation-scoped vendor client secrets.
     */
    protected final SecretResolver secrets;

    /**
     * Creates a vendor provider from the complete explicit dependency aggregate.
     *
     * @param configuration non-null vendor runtime dependencies
     * @param definition    non-null immutable vendor definition
     * @throws NullPointerException if an argument or configuration component is null
     * @throws AuthorizedException  if the static registration is invalid for the definition
     */
    protected AbstractProvider(final VendorConfiguration configuration, final VendorDefinition definition) {
        final VendorConfiguration current = Objects
                .requireNonNull(configuration, "Vendor configuration must not be null");
        this.registration = Objects.requireNonNull(current.registration(), "Vendor registration must not be null");
        this.definition = Objects.requireNonNull(definition, "Vendor definition must not be null");
        this.fabric = Objects.requireNonNull(current.fabric(), "Fabric context must not be null");
        this.clock = Objects.requireNonNull(current.clock(), "Fabric clock must not be null");
        this.stateStore = Objects.requireNonNull(current.stateStore(), "State store must not be null");
        this.secrets = Objects.requireNonNull(current.secrets(), "Secret resolver must not be null");
        VendorValidator.validateRegistration(registration, definition);
    }

    /**
     * Retrieves immutable default scope names from scope metadata.
     *
     * @param scopes authorization scope metadata, or null
     * @return immutable default scope names, possibly empty
     */
    protected static List<String> getScopes(final AuthorizeScope[] scopes) {
        if (scopes == null || scopes.length == 0) {
            return List.of();
        }
        return Arrays.stream(scopes).filter(AuthorizeScope::isDefault).map(AuthorizeScope::getScope)
                .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Applies non-null map entries to a Fabric HTTP builder operation.
     *
     * @param values   optional map
     * @param consumer Fabric builder operation
     */
    private static void apply(
            final Map<String, ?> values,
            final java.util.function.BiConsumer<String, Object> consumer) {
        if (values != null) {
            values.forEach((name, value) -> {
                if (name != null && value != null) {
                    consumer.accept(name, value);
                }
            });
        }
    }

    /**
     * Parses a required single-line content type.
     *
     * @param contentType content type text
     * @return parsed media type
     * @throws ValidateException if the value is blank or contains line breaks
     */
    private static MediaType media(final String contentType) {
        if (StringKit.isBlank(contentType) || StringKit.containsAny(contentType, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException("Content-Type must be non-blank and single-line");
        }
        return MediaType.parse(contentType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Descriptor descriptor() {
        return definition.descriptor();
    }

    /**
     * Builds a standard OAuth/OIDC or SAML authorization URL and atomically registers its state.
     *
     * @param context immutable root operation context
     * @param state   optional caller-supplied state; a Bus identifier is generated when absent
     * @return authorization URL result or the standard unsupported result
     * @throws AuthorizedException if state registration fails
     */
    @Override
    public Message<String> build(final Context context, final String state) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        if (!supports(VendorEndpoint.AUTHORIZE)) {
            return VendorProvider.super.build(current, state);
        }
        final String actualState = state(current, state);
        final VendorRequestBuilder request = VendorRequestBuilder.fromUrl(endpoint(VendorEndpoint.AUTHORIZE));
        if (definition.protocol() == Protocol.SAML) {
            return Message.success(request.queryParam("RelayState", actualState).build());
        }
        if (definition.protocol() != Protocol.OIDC) {
            return VendorProvider.super.build(current, state);
        }
        return Message.success(
                request.queryParam("response_type", "code").queryParam("client_id", registration.clientId())
                        .queryParam("redirect_uri", registration.redirectUri()).queryParam("state", actualState)
                        .queryParam("scope", scopes(Symbol.SPACE, true, getScopes(null))).build());
    }

    /**
     * Validates a callback, atomically consumes state, exchanges tokens, and resolves the vendor identity.
     *
     * @param context  immutable root operation context
     * @param callback immutable inbound callback
     * @return vendor identity result preserving vendor Message failures
     */
    @Override
    public Message<VendorIdentity> authorize(final Context context, final Callback.Inbound callback) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final Callback.Inbound inbound = Objects.requireNonNull(callback, "Callback must not be null");
        if (!current.tenantId().equals(inbound.context().tenantId())
                || !current.correlationId().equals(inbound.context().correlationId())) {
            throw new AuthorizedException(VendorErrors._110004);
        }
        VendorValidator.validateCallback(inbound, definition);
        if (!registration.ignoreState() && definition.protocol() == Protocol.OIDC) {
            VendorValidator.consumeState(current, inbound.value("state").orElse(null), stateStore).toCompletableFuture()
                    .join();
        }
        final Message<VendorTokenSet> token = token(current, inbound);
        if (token == null) {
            return Message.failure(VendorErrors._110004);
        }
        if (!ErrorCode._SUCCESS.getKey().equals(token.getErrcode()) || token.getData() == null) {
            return Message.failure(token.getErrcode(), token.getErrmsg());
        }
        final Message<VendorIdentity> identity = userInfo(current, token.getData());
        return identity == null ? Message.failure(VendorErrors._110004) : identity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Message<VendorTokenSet> token(final Context context, final Callback.Inbound callback) {
        return VendorProvider.super.token(context, callback);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Message<VendorIdentity> userInfo(final Context context, final VendorTokenSet token) {
        return VendorProvider.super.userInfo(context, token);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Message<VendorTokenSet> refresh(final Context context, final VendorTokenSet token) {
        return VendorProvider.super.refresh(context, token);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Message<Void> revoke(final Context context, final VendorTokenSet token) {
        return VendorProvider.super.revoke(context, token);
    }

    /**
     * Sends a GET request through the injected Fabric context.
     *
     * @param url target URL
     * @return response text
     */
    protected final String get(final String url) {
        return get(url, null, null);
    }

    /**
     * Sends a GET request with query parameters through the injected Fabric context.
     *
     * @param url   target URL
     * @param query optional query parameters
     * @return response text
     */
    protected final String get(final String url, final Map<String, ?> query) {
        return get(url, query, null);
    }

    /**
     * Sends a GET request with query parameters and headers through the injected Fabric context.
     *
     * @param url     target URL
     * @param query   optional query parameters
     * @param headers optional headers
     * @return response text
     */
    protected final String get(final String url, final Map<String, ?> query, final Map<String, ?> headers) {
        final var builder = Fabric.http(fabric).get(url);
        apply(query, builder::query);
        apply(headers, builder::header);
        return builder.executeText();
    }

    /**
     * Sends an empty form POST through the injected Fabric context.
     *
     * @param url target URL
     * @return response text
     */
    protected final String post(final String url) {
        return Fabric.http(fabric).post(url).body(Payload.empty(), MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .executeText();
    }

    /**
     * Sends a form POST through the injected Fabric context.
     *
     * @param url  target URL
     * @param form form values
     * @return response text
     */
    protected final String post(final String url, final Map<String, ?> form) {
        return post(url, form, null);
    }

    /**
     * Sends a form POST with headers through the injected Fabric context.
     *
     * @param url     target URL
     * @param form    form values
     * @param headers optional headers
     * @return response text
     */
    protected final String post(final String url, final Map<String, ?> form, final Map<String, ?> headers) {
        final var builder = Fabric.http(fabric).post(url);
        apply(headers, builder::header);
        if (form == null || form.isEmpty()) {
            builder.body(Payload.empty(), MediaType.APPLICATION_FORM_URLENCODED_TYPE);
        } else {
            apply(form, builder::form);
        }
        return builder.executeText();
    }

    /**
     * Sends a raw POST through the injected Fabric context.
     *
     * @param url         target URL
     * @param data        body text
     * @param contentType validated content type
     * @return response text
     */
    protected final String post(final String url, final String data, final String contentType) {
        return post(url, data, null, contentType);
    }

    /**
     * Sends a raw POST with headers through the injected Fabric context.
     *
     * @param url         target URL
     * @param data        body text
     * @param headers     optional headers
     * @param contentType validated content type
     * @return response text
     */
    protected final String post(
            final String url,
            final String data,
            final Map<String, ?> headers,
            final String contentType) {
        final var builder = Fabric.http(fabric).post(url).body(data == null ? Normal.EMPTY : data, media(contentType));
        apply(headers, builder::header);
        return builder.executeText();
    }

    /**
     * Builds the standard authorization-code token URL using an operation-scoped resolved secret.
     *
     * @param context root operation context used to resolve secret material
     * @param code    authorization code
     * @return token request URL
     */
    protected String tokenUrl(final Context context, final String code) {
        return VendorRequestBuilder.fromUrl(endpoint(VendorEndpoint.TOKEN)).queryParam("code", code)
                .queryParam("client_id", registration.clientId()).queryParam("client_secret", secret(context))
                .queryParam("grant_type", "authorization_code").queryParam("redirect_uri", registration.redirectUri())
                .build();
    }

    /**
     * Builds the standard refresh-token URL using an operation-scoped resolved secret.
     *
     * @param context root operation context used to resolve secret material
     * @param token   sensitive refresh token
     * @return refresh request URL
     */
    protected String refreshUrl(final Context context, final String token) {
        return VendorRequestBuilder.fromUrl(endpoint(VendorEndpoint.REFRESH))
                .queryParam("client_id", registration.clientId()).queryParam("client_secret", secret(context))
                .queryParam("refresh_token", token).queryParam("grant_type", "refresh_token")
                .queryParam("redirect_uri", registration.redirectUri()).build();
    }

    /**
     * Builds the standard user-info URL.
     *
     * @param token vendor token set
     * @return user-info request URL
     */
    protected String userInfoUrl(final VendorTokenSet token) {
        return VendorRequestBuilder.fromUrl(endpoint(VendorEndpoint.USERINFO))
                .queryParam("access_token", token.getToken()).build();
    }

    /**
     * Builds the standard token-revocation URL.
     *
     * @param token vendor token set
     * @return revocation request URL
     */
    protected String revokeUrl(final VendorTokenSet token) {
        return VendorRequestBuilder.fromUrl(endpoint(VendorEndpoint.REVOKE))
                .queryParam("access_token", token.getToken()).build();
    }

    /**
     * Executes a standard form POST token exchange.
     *
     * @param context root operation context used to resolve secret material
     * @param code    authorization code
     * @return response text
     */
    protected String doPostToken(final Context context, final String code) {
        return post(tokenUrl(context, code));
    }

    /**
     * Executes a standard GET token exchange.
     *
     * @param context root operation context used to resolve secret material
     * @param code    authorization code
     * @return response text
     */
    protected String doGetToken(final Context context, final String code) {
        return get(tokenUrl(context, code));
    }

    /**
     * Executes a standard GET user-info request.
     *
     * @param token vendor token set
     * @return response text
     */
    protected String doGetUserInfo(final VendorTokenSet token) {
        return get(userInfoUrl(token));
    }

    /**
     * Executes a standard GET revocation request.
     *
     * @param token vendor token set
     * @return response text
     */
    protected String doGetRevoke(final VendorTokenSet token) {
        return get(revokeUrl(token));
    }

    /**
     * Returns the configured endpoint URL, preferring the registration override.
     *
     * @param role vendor endpoint role
     * @return endpoint URI text
     * @throws AuthorizedException if the role has no configured endpoint
     */
    protected final String endpoint(final VendorEndpoint role) {
        final Endpoint endpoint = registration.endpoints().getOrDefault(role, definition.endpoints().get(role));
        if (endpoint == null) {
            throw new AuthorizedException(VendorErrors._110000);
        }
        return endpoint.address().toUri().toString();
    }

    /**
     * Reports whether the effective registration or definition contains an endpoint role.
     *
     * @param role vendor endpoint role
     * @return true when the role is configured
     */
    protected final boolean supports(final VendorEndpoint role) {
        return registration.endpoints().containsKey(role) || definition.endpoints().containsKey(role);
    }

    /**
     * Constructs a scope string from registration scopes or immutable defaults.
     *
     * @param separator     scope delimiter, defaulting to a space
     * @param encode        whether to URL-encode the complete scope string
     * @param defaultScopes immutable defaults used when registration scopes are empty
     * @return scope string, possibly empty
     */
    protected final String scopes(String separator, final boolean encode, final List<String> defaultScopes) {
        final List<String> selected = registration.scopes().isEmpty()
                ? defaultScopes == null ? List.of() : List.copyOf(defaultScopes)
                : registration.scopes();
        if (selected.isEmpty()) {
            return Normal.EMPTY;
        }
        if (separator == null) {
            separator = Symbol.SPACE;
        }
        final String value = String.join(separator, selected);
        return encode ? UrlEncoder.encodeAll(value) : value;
    }

    /**
     * Resolves a caller-owned vendor client secret and clears its character buffer after conversion.
     *
     * @param context root operation context
     * @return transient immutable secret text for immediate request construction
     * @throws AuthorizedException if resolution returns no secret
     */
    protected final String secret(final Context context) {
        final CompletionStage<char[]> stage = Objects.requireNonNull(
                secrets.resolve(
                        Objects.requireNonNull(context, "Context must not be null"),
                        "vendor-client",
                        registration.secretId()),
                "Secret resolver returned no stage");
        final char[] value = stage.toCompletableFuture().join();
        if (value == null || value.length == 0) {
            throw new AuthorizedException(VendorErrors._110011);
        }
        try {
            return new String(value);
        } finally {
            Arrays.fill(value, '\0');
        }
    }

    /**
     * Generates or preserves state and records it atomically for the operation tenant.
     *
     * @param context  root operation context
     * @param supplied optional caller-supplied opaque state
     * @return non-empty registered state value
     * @throws AuthorizedException if the state already exists or the operation has expired
     */
    protected final String state(final Context context, final String supplied) {
        final String value = StringKit.isEmpty(supplied) ? ID.objectId() : supplied;
        if (registration.ignoreState()) {
            return value;
        }
        final Duration ttl = context.remaining(clock);
        if (ttl.isZero()) {
            throw new AuthorizedException(VendorErrors._110006);
        }
        final String key = ReplayKey.derive(context.tenantId(), "oauth2", "state", value);
        final byte[] marker = StateEnvelopeCodec.INSTANCE.encode(value.getBytes(StandardCharsets.UTF_8));
        final Boolean inserted = Objects.requireNonNull(
                stateStore.putIfAbsent(context, key, marker, ttl),
                "State-store create stage must not be null").toCompletableFuture().join();
        if (!Boolean.TRUE.equals(inserted)) {
            throw new AuthorizedException(VendorErrors._110006);
        }
        return value;
    }

}
