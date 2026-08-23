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
package org.miaixz.bus.auth.source.protocol.oauth2.client;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.FabricX;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.source.DriverServices;
import org.miaixz.bus.auth.source.protocol.oauth2.AuthorizationServerMetadata;
import org.miaixz.bus.auth.source.protocol.oauth2.codec.AuthorizationServerMetadataCodec;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Retrieves and issuer-binds RFC 8414 authorization server metadata.
 *
 * @author Kimi Liu
 */
public class AuthorizationServerMetadataClient {

    /**
     * Validated Source options containing the trusted issuer and metadata endpoint.
     */
    private final OAuth2ClientOptions options;

    /**
     * Capability-limited Source services supplying security policies and execution dependencies.
     */
    private final DriverServices services;

    /**
     * Strict RFC 8414 JSON codec.
     */
    private final AuthorizationServerMetadataCodec codec;

    /**
     * Creates a metadata client for one compiled Source.
     *
     * @param options  validated OAuth 2.x client options
     * @param services capability-limited Source services
     * @param codec    strict RFC 8414 metadata codec
     * @throws IllegalArgumentException if a collaborator is {@code null} or no metadata endpoint is configured
     */
    public AuthorizationServerMetadataClient(final OAuth2ClientOptions options, final DriverServices services,
            final AuthorizationServerMetadataCodec codec) {
        this.options = Assert.notNull(options, "OAuth 2.x client options must not be null");
        Assert.notNull(
                options.authorizationServerMetadataEndpoint().getOrNull(),
                "OAuth 2.x authorization server metadata endpoint must be configured");
        Assert.notNull(
                options.expectedIssuer().getOrNull(),
                "OAuth 2.x authorization server metadata expected issuer must be configured");
        this.services = Assert.notNull(services, "OAuth 2.x execution services must not be null");
        this.codec = Assert.notNull(codec, "OAuth 2.x authorization server metadata codec must not be null");
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
     * Creates a completed stage with inferred success type.
     *
     * @param <T>     outcome success type
     * @param outcome completed outcome
     * @return completed stage
     */
    private static <T> CompletionStage<Outcome<T>> completed(final Outcome<T> outcome) {
        return CompletableFuture.completedFuture(outcome);
    }

    /**
     * Retrieves metadata and verifies that its issuer exactly matches the configured trusted issuer.
     *
     * @param context immutable invocation context
     * @param timeout shared end-to-end timeout
     * @return stage containing issuer-bound metadata or closed framework failure
     */
    public CompletionStage<Outcome<AuthorizationServerMetadata>> metadata(
            final Context context,
            final Timeout timeout) {
        Assert.notNull(context, "OAuth 2.x metadata context must not be null");
        Assert.notNull(timeout, "OAuth 2.x metadata timeout must not be null");
        if (timeout.expired()) {
            return completed(Outcome.failed(failure(ErrorCode._408, "OAuth 2.x metadata request has no timeout")));
        }
        return CompletableFuture.supplyAsync(() -> execute(timeout), services.executor());
    }

    /**
     * Executes one unauthenticated metadata GET and performs exact issuer binding.
     *
     * @param timeout decreasing operation timeout
     * @return standard metadata outcome
     */
    private Outcome<AuthorizationServerMetadata> execute(final Timeout timeout) {
        try {
            if (timeout.expired()) {
                return Outcome.failed(failure(ErrorCode._408, "OAuth 2.x metadata request exhausted its timeout"));
            }
            final var endpoint = options.authorizationServerMetadataEndpoint().getOrNull();
            final var response = FabricX.http(Protocol.OAUTH2, timeout, services.policies())
                    .url(endpoint.url().toString()).method(Http.Method.GET).execute();
            final AuthorizationServerMetadata metadata = codec.decode(response);
            if (!options.expectedIssuer().getOrNull().equals(metadata.issuer())) {
                return Outcome.rejected(
                        failure(
                                ErrorCode._400,
                                "OAuth 2.x metadata issuer does not match the configured Source issuer"));
            }
            return Outcome.succeeded(metadata);
        } catch (RuntimeException exception) {
            return Outcome.failed(failure(ErrorCode._502, "OAuth 2.x authorization server metadata request failed"));
        }
    }

}
