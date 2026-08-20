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
package org.miaixz.bus.auth.protocol.oauth2.client;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.protocol.oauth2.*;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.extra.json.JsonValue;
import org.miaixz.bus.fabric.UnoUrl;

/**
 * Aggregates the six supported OAuth 2.x client operations without duplicating operation implementation.
 * <p>
 * Each operation is delegated unchanged to its single-operation client. Browser authorization returns a user-agent URL;
 * the later callback is decoded independently from the external project's {@code Callback.Inbound} value.
 * </p>
 *
 * @author Kimi Liu
 */
public final class OAuth2Client {

    /**
     * Single-operation authorization request client.
     */
    private final AuthorizationClient authorizationClient;

    /**
     * Single-operation token endpoint client.
     */
    private final TokenClient tokenClient;

    /**
     * Single-operation introspection endpoint client.
     */
    private final Optional<IntrospectionClient> introspectionClient;

    /**
     * Single-operation revocation endpoint client.
     */
    private final Optional<RevocationClient> revocationClient;

    /**
     * Single-operation device authorization endpoint client.
     */
    private final Optional<DeviceAuthorizationClient> deviceAuthorizationClient;

    /**
     * Single-operation authorization server metadata client.
     */
    private final Optional<AuthorizationServerMetadataClient> metadataClient;

    /**
     * Creates an immutable OAuth 2.x client facade from its operation clients.
     *
     * @param authorizationClient       authorization URL client
     * @param tokenClient               token endpoint client
     * @param introspectionClient       optional token introspection client
     * @param revocationClient          optional token revocation client
     * @param deviceAuthorizationClient optional device authorization client
     * @param metadataClient            optional authorization server metadata client
     * @throws IllegalArgumentException if any collaborator is {@code null}
     */
    public OAuth2Client(final AuthorizationClient authorizationClient, final TokenClient tokenClient,
            final Optional<IntrospectionClient> introspectionClient, final Optional<RevocationClient> revocationClient,
            final Optional<DeviceAuthorizationClient> deviceAuthorizationClient,
            final Optional<AuthorizationServerMetadataClient> metadataClient) {
        this.authorizationClient = Assert
                .notNull(authorizationClient, "OAuth 2.x authorization client must not be null");
        this.tokenClient = Assert.notNull(tokenClient, "OAuth 2.x token client must not be null");
        Assert.notNull(introspectionClient, "OAuth 2.x introspection client container must not be null");
        Assert.notNull(revocationClient, "OAuth 2.x revocation client container must not be null");
        Assert.notNull(deviceAuthorizationClient, "OAuth 2.x device authorization client container must not be null");
        Assert.notNull(metadataClient, "OAuth 2.x metadata client container must not be null");
        this.introspectionClient = Optional.ofNullable(introspectionClient.getOrNull());
        this.revocationClient = Optional.ofNullable(revocationClient.getOrNull());
        this.deviceAuthorizationClient = Optional.ofNullable(deviceAuthorizationClient.getOrNull());
        this.metadataClient = Optional.ofNullable(metadataClient.getOrNull());
    }

    /**
     * Creates a completed rejection for an operation whose endpoint is absent from the compiled Source.
     *
     * @param description safe missing-capability description
     * @param <T>         operation success type
     * @return completed rejected outcome
     */
    private static <T> CompletionStage<Outcome<T>> unavailable(final String description) {
        return CompletableFuture.completedFuture(
                Outcome.rejected(
                        new Outcome.Failure(ErrorCode._404, description, new JsonValue.ObjectValue(Map.of()))));
    }

    /**
     * Builds the standard authorization request URL for the user agent.
     *
     * @param request standard authorization request
     * @param context immutable invocation context
     * @param timeout shared end-to-end time budget
     * @return stage containing an absolute authorization URL or framework failure
     */
    public CompletionStage<Outcome<UnoUrl>> authorize(
            final AuthorizationRequest request,
            final Context context,
            final Timeout.Budget timeout) {
        return authorizationClient.authorize(request, context, timeout);
    }

    /**
     * Executes any supported grant at the standard token endpoint.
     *
     * @param request standard token request
     * @param context immutable invocation context
     * @param timeout shared end-to-end time budget
     * @return stage containing a standard token response or framework failure
     */
    public CompletionStage<Outcome<TokenEndpointResponse>> token(
            final TokenRequest request,
            final Context context,
            final Timeout.Budget timeout) {
        return tokenClient.token(request, context, timeout);
    }

    /**
     * Introspects an opaque token according to RFC 7662.
     *
     * @param request standard introspection request
     * @param context immutable invocation context
     * @param timeout shared end-to-end time budget
     * @return stage containing a standard introspection response or framework failure
     */
    public CompletionStage<Outcome<IntrospectionResponse>> introspect(
            final IntrospectionRequest request,
            final Context context,
            final Timeout.Budget timeout) {
        final IntrospectionClient client = introspectionClient.getOrNull();
        return client == null ? unavailable("OAuth 2.x introspection capability is not configured")
                : client.introspect(request, context, timeout);
    }

    /**
     * Revokes an access or refresh token according to RFC 7009.
     *
     * @param request standard revocation request
     * @param context immutable invocation context
     * @param timeout shared end-to-end time budget
     * @return stage whose successful value is {@code null} because RFC 7009 defines no response entity
     */
    public CompletionStage<Outcome<Void>> revoke(
            final RevocationRequest request,
            final Context context,
            final Timeout.Budget timeout) {
        final RevocationClient client = revocationClient.getOrNull();
        return client == null ? unavailable("OAuth 2.x revocation capability is not configured")
                : client.revoke(request, context, timeout);
    }

    /**
     * Obtains device and user codes according to RFC 8628.
     *
     * @param request standard device authorization request
     * @param context immutable invocation context
     * @param timeout shared end-to-end time budget
     * @return stage containing a standard device authorization response or framework failure
     */
    public CompletionStage<Outcome<DeviceAuthorizationResponse>> deviceAuthorization(
            final DeviceAuthorizationRequest request,
            final Context context,
            final Timeout.Budget timeout) {
        final DeviceAuthorizationClient client = deviceAuthorizationClient.getOrNull();
        return client == null ? unavailable("OAuth 2.x device authorization capability is not configured")
                : client.deviceAuthorization(request, context, timeout);
    }

    /**
     * Retrieves RFC 8414 authorization server metadata.
     *
     * @param context immutable invocation context
     * @param timeout shared end-to-end time budget
     * @return stage containing standard authorization server metadata or framework failure
     */
    public CompletionStage<Outcome<AuthorizationServerMetadata>> discover(
            final Context context,
            final Timeout.Budget timeout) {
        final AuthorizationServerMetadataClient client = metadataClient.getOrNull();
        return client == null ? unavailable("OAuth 2.x metadata capability is not configured")
                : client.metadata(context, timeout);
    }

}
