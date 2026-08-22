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
package org.miaixz.bus.auth.protocol.oidc.client;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.FabricX.Url;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.protocol.oauth2.*;
import org.miaixz.bus.auth.protocol.oauth2.client.OAuth2Client;
import org.miaixz.bus.auth.protocol.oidc.*;
import org.miaixz.bus.auth.protocol.oidc.codec.AuthenticationRequestEncoder;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Aggregates OpenID Connect relying-party operations with the underlying OAuth 2.x client operations.
 * <p>
 * The facade delegates every operation to a single owner. ID Token verification and browser callback decoding remain
 * explicit protocol steps and are not hidden behind invented authentication or token-exchange methods.
 * </p>
 *
 * @author Kimi Liu
 */
public class OpenIdClient {

    /**
     * Standard OAuth 2.x operation facade used by OpenID Connect.
     */
    private final OAuth2Client oauth2Client;

    /**
     * Encoder that appends OIDC parameters to a validated OAuth authorization URL.
     */
    private final AuthenticationRequestEncoder authenticationRequestEncoder;

    /**
     * OpenID Provider Discovery operation client.
     */
    private final DiscoveryClient discoveryClient;

    /**
     * OpenID Connect UserInfo operation client.
     */
    private final Optional<UserInfoClient> userInfoClient;

    /**
     * RP-Initiated Logout operation client.
     */
    private final Optional<EndSessionClient> endSessionClient;

    /**
     * Creates an immutable OpenID Connect client facade.
     *
     * @param oauth2Client                 standard OAuth 2.x client operations
     * @param authenticationRequestEncoder OpenID Connect Authentication Request encoder
     * @param discoveryClient              OpenID Provider Discovery client
     * @param userInfoClient               optional OpenID Connect UserInfo client
     * @param endSessionClient             optional RP-Initiated Logout client
     * @throws IllegalArgumentException if any collaborator is {@code null}
     */
    public OpenIdClient(final OAuth2Client oauth2Client,
            final AuthenticationRequestEncoder authenticationRequestEncoder, final DiscoveryClient discoveryClient,
            final Optional<UserInfoClient> userInfoClient, final Optional<EndSessionClient> endSessionClient) {
        this.oauth2Client = Assert.notNull(oauth2Client, "OpenID Connect OAuth 2.x client must not be null");
        this.authenticationRequestEncoder = Assert.notNull(
                authenticationRequestEncoder,
                "OpenID Connect Authentication Request encoder must not be null");
        this.discoveryClient = Assert.notNull(discoveryClient, "OpenID Connect discovery client must not be null");
        Assert.notNull(userInfoClient, "OpenID Connect UserInfo client container must not be null");
        Assert.notNull(endSessionClient, "OpenID Connect end-session client container must not be null");
        this.userInfoClient = Optional.ofNullable(userInfoClient.getOrNull());
        this.endSessionClient = Optional.ofNullable(endSessionClient.getOrNull());
    }

    /**
     * Creates a completed rejection for an optional operation absent from the compiled Source manifest.
     *
     * @param <T>         operation success type
     * @param description safe missing-capability description
     * @return completed rejected outcome
     */
    private static <T> CompletionStage<Outcome<T>> unavailable(final String description) {
        return CompletableFuture.completedFuture(
                Outcome.rejected(
                        new Outcome.Failure(ErrorCode._404, description, new JsonValue.ObjectValue(Map.of()))));
    }

    /**
     * Converts the OAuth-decoded response into the required OpenID Connect token success type.
     *
     * @param response decoded token endpoint success
     * @param required whether the originating authorization-code grant requires an ID Token
     * @return OpenID Connect success or a standards-bound rejection
     */
    private static Outcome<TokenEndpointResponse> openIdToken(
            final TokenEndpointResponse response,
            final boolean required) {
        if (!(response instanceof TokenResponse token)) {
            return Outcome.rejected(
                    new Outcome.Failure(ErrorCode._400,
                            "OpenID Connect token endpoint returned a non-OIDC success type",
                            new JsonValue.ObjectValue(Map.of())));
        }
        if (!token.extensions().values().containsKey(OpenIdConnect.Parameters.ID_TOKEN) && !required) {
            return Outcome.succeeded(token);
        }
        try {
            return Outcome.succeeded(OpenIdTokenResponse.from(token));
        } catch (RuntimeException cause) {
            return Outcome.rejected(
                    new Outcome.Failure(ErrorCode._400,
                            required ? "OpenID Connect token endpoint omitted the required ID Token"
                                    : "OpenID Connect refresh response returned an invalid ID Token",
                            new JsonValue.ObjectValue(Map.of())));
        }
    }

    /**
     * Builds an OAuth authorization URL for the user agent.
     *
     * @param request standard OpenID Connect Authentication Request
     * @param context immutable invocation context
     * @param timeout shared end-to-end timeout
     * @return stage containing the authorization URL or framework failure
     */
    public CompletionStage<Outcome<Url>> authorize(
            final AuthenticationRequest request,
            final Context context,
            final Timeout timeout) {
        Assert.notNull(request, "OpenID Connect Authentication Request must not be null");
        return oauth2Client.authorize(request.authorizationRequest(), context, timeout)
                .thenApply(outcome -> switch (outcome) {
                    case Outcome.Succeeded<Url> success -> {
                        try {
                            yield Outcome.succeeded(authenticationRequestEncoder.encode(success.value(), request));
                        } catch (RuntimeException exception) {
                            yield Outcome.rejected(
                                    new Outcome.Failure(ErrorCode._400,
                                            "OpenID Connect Authentication Request encoding failed",
                                            new JsonValue.ObjectValue(Map.of())));
                        }
                    }
                    case Outcome.Rejected<Url> rejected -> Outcome.rejected(rejected.failure());
                    case Outcome.Failed<Url> failed -> Outcome.failed(failed.failure());
                    default -> throw new IllegalStateException("Unsupported Outcome implementation");
                });
    }

    /**
     * Executes a supported OAuth grant at the token endpoint.
     *
     * @param request standard OAuth token request
     * @param context immutable invocation context
     * @param timeout shared end-to-end timeout
     * @return stage containing the standard token response or framework failure
     */
    public CompletionStage<Outcome<TokenEndpointResponse>> token(
            final TokenRequest request,
            final Context context,
            final Timeout timeout) {
        return oauth2Client.token(request, context, timeout).thenApply(outcome -> switch (outcome) {
            case Outcome.Succeeded<TokenEndpointResponse> success -> openIdToken(
                    success.value(),
                    request.grant() instanceof AuthorizationCodeGrant);
            case Outcome.Rejected<TokenEndpointResponse> rejected -> Outcome.rejected(rejected.failure());
            case Outcome.Failed<TokenEndpointResponse> failed -> Outcome.failed(failed.failure());
            default -> throw new IllegalStateException("Unsupported Outcome implementation");
        });
    }

    /**
     * Introspects an OAuth token.
     *
     * @param request standard introspection request
     * @param context immutable invocation context
     * @param timeout shared end-to-end timeout
     * @return stage containing the standard introspection response or framework failure
     */
    public CompletionStage<Outcome<IntrospectionResponse>> introspect(
            final IntrospectionRequest request,
            final Context context,
            final Timeout timeout) {
        return oauth2Client.introspect(request, context, timeout);
    }

    /**
     * Revokes an OAuth access or refresh token.
     *
     * @param request standard revocation request
     * @param context immutable invocation context
     * @param timeout shared end-to-end timeout
     * @return stage whose successful value is {@code null}
     */
    public CompletionStage<Outcome<Void>> revoke(
            final RevocationRequest request,
            final Context context,
            final Timeout timeout) {
        return oauth2Client.revoke(request, context, timeout);
    }

    /**
     * Obtains OAuth device and user codes.
     *
     * @param request standard device authorization request
     * @param context immutable invocation context
     * @param timeout shared end-to-end timeout
     * @return stage containing the standard device authorization response or framework failure
     */
    public CompletionStage<Outcome<DeviceAuthorizationResponse>> deviceAuthorization(
            final DeviceAuthorizationRequest request,
            final Context context,
            final Timeout timeout) {
        return oauth2Client.deviceAuthorization(request, context, timeout);
    }

    /**
     * Retrieves OAuth Authorization Server Metadata.
     *
     * @param context immutable invocation context
     * @param timeout shared end-to-end timeout
     * @return stage containing RFC 8414 metadata or framework failure
     */
    public CompletionStage<Outcome<AuthorizationServerMetadata>> metadata(
            final Context context,
            final Timeout timeout) {
        return oauth2Client.discover(context, timeout);
    }

    /**
     * Retrieves and issuer-binds OpenID Provider Metadata.
     *
     * @param context immutable invocation context
     * @param timeout shared end-to-end timeout
     * @return stage containing OpenID Provider Metadata or framework failure
     */
    public CompletionStage<Outcome<OpenIdProviderMetadata>> discover(final Context context, final Timeout timeout) {
        return discoveryClient.discover(context, timeout);
    }

    /**
     * Retrieves claims from the OpenID Connect UserInfo endpoint.
     *
     * @param request standard UserInfo request
     * @param context immutable invocation context
     * @param timeout shared end-to-end timeout
     * @return stage containing a standard UserInfo response or framework failure
     */
    public CompletionStage<Outcome<UserInfoResponse>> userInfo(
            final UserInfoRequest request,
            final Context context,
            final Timeout timeout) {
        final UserInfoClient client = userInfoClient.getOrNull();
        return client == null ? unavailable("OpenID Connect UserInfo capability is not configured")
                : client.userInfo(request, context, timeout);
    }

    /**
     * Initiates relying-party logout at the OpenID Provider.
     *
     * @param request standard RP-Initiated Logout request
     * @param context immutable invocation context
     * @param timeout shared end-to-end timeout
     * @return stage containing the absolute logout URL for user-agent navigation or framework failure
     */
    public CompletionStage<Outcome<Url>> endSession(
            final EndSessionRequest request,
            final Context context,
            final Timeout timeout) {
        final EndSessionClient client = endSessionClient.getOrNull();
        return client == null ? unavailable("OpenID Connect end-session capability is not configured")
                : client.endSession(request, context, timeout);
    }

}
