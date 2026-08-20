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
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.protocol.oidc.OpenIdProviderMetadata;
import org.miaixz.bus.auth.protocol.oidc.codec.OpenIdProviderMetadataCodec;
import org.miaixz.bus.auth.shared.ExecutionServices;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.extra.json.JsonValue;
import org.miaixz.bus.fabric.Fabric;

/**
 * Retrieves OpenID Provider Metadata and binds it to the configured issuer.
 *
 * @author Kimi Liu
 */
public final class DiscoveryClient {

    /**
     * Validated relying-party settings containing the trusted issuer and discovery endpoint.
     */
    private final OpenIdClientSettings settings;

    /**
     * Caller-owned runtime dependencies and Fabric context.
     */
    private final ExecutionServices services;

    /**
     * Strict OpenID Provider Metadata codec.
     */
    private final OpenIdProviderMetadataCodec codec;

    /**
     * Creates a Discovery client for one compiled OpenID Connect Source.
     *
     * @param settings validated OpenID Connect client settings
     * @param services externally owned runtime dependencies
     * @param codec    strict OpenID Provider Metadata codec
     * @throws IllegalArgumentException if a collaborator is {@code null} or discovery is not configured
     */
    public DiscoveryClient(final OpenIdClientSettings settings, final ExecutionServices services,
            final OpenIdProviderMetadataCodec codec) {
        this.settings = Assert.notNull(settings, "OpenID Connect client settings must not be null");
        Assert.notNull(
                settings.discoveryEndpoint().getOrNull(),
                "OpenID Connect discovery endpoint must be configured");
        this.services = Assert.notNull(services, "OpenID Connect execution services must not be null");
        this.codec = Assert.notNull(codec, "OpenID Connect metadata codec must not be null");
    }

    /**
     * Creates a non-sensitive framework failure.
     *
     * @param code        shared Bus error code
     * @param description safe diagnostic text
     * @return closed failure value
     */
    private static Outcome.Failure failure(final Errors code, final String description) {
        return new Outcome.Failure(code, description, new JsonValue.ObjectValue(Map.of()));
    }

    /**
     * Creates a completed asynchronous result.
     *
     * @param <T>     success value type
     * @param outcome completed outcome
     * @return completed stage
     */
    private static <T> CompletionStage<Outcome<T>> completed(final Outcome<T> outcome) {
        return CompletableFuture.completedFuture(outcome);
    }

    /**
     * Retrieves metadata and verifies its exact issuer binding.
     *
     * @param context immutable invocation context
     * @param timeout shared end-to-end time budget
     * @return stage containing issuer-bound metadata or framework failure
     */
    public CompletionStage<Outcome<OpenIdProviderMetadata>> discover(
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(context, "OpenID Connect discovery context must not be null");
        Assert.notNull(timeout, "OpenID Connect discovery time budget must not be null");
        if (timeout.expired()) {
            return completed(
                    Outcome.failed(failure(ErrorCode._408, "OpenID Connect discovery request has no time budget")));
        }
        return CompletableFuture.supplyAsync(() -> execute(timeout), services.executor());
    }

    /**
     * Executes one unauthenticated metadata request and performs exact issuer matching.
     *
     * @param timeout decreasing operation budget
     * @return standard metadata outcome
     */
    private Outcome<OpenIdProviderMetadata> execute(final Timeout.Budget timeout) {
        try {
            if (timeout.expired()) {
                return Outcome
                        .failed(failure(ErrorCode._408, "OpenID Connect discovery request exhausted its time budget"));
            }
            final var endpoint = settings.discoveryEndpoint().getOrNull();
            final var response = Fabric.http(services.fabricContext()).url(endpoint.url().toString())
                    .method(Http.Method.GET).timeout(timeout.forFabric())
                    .addressPolicy(services.securityBaseline().require(Protocol.OIDC).addressPolicy()).execute();
            final OpenIdProviderMetadata metadata = codec.decode(response);
            if (!settings.expectedIssuer().equals(metadata.authorizationServerMetadata().issuer())) {
                return Outcome.rejected(
                        failure(
                                ErrorCode._400,
                                "OpenID Connect metadata issuer does not match the configured Source issuer"));
            }
            if (metadata.authorizationServerMetadata().jwksUri().isEmpty()
                    || metadata.idTokenSigningAlgValuesSupported().isEmpty()) {
                return Outcome.rejected(
                        failure(
                                ErrorCode._400,
                                "OpenID Connect metadata omits a required JWK Set URI or ID Token algorithm"));
            }
            return Outcome.succeeded(metadata);
        } catch (RuntimeException exception) {
            return Outcome.failed(failure(ErrorCode._502, "OpenID Connect discovery endpoint request failed"));
        }
    }

}
