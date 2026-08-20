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
package org.miaixz.bus.auth.protocol.oauth1.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Credential;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.protocol.oauth1.OAuth1;
import org.miaixz.bus.auth.protocol.oauth1.OAuth1Parameter;
import org.miaixz.bus.auth.protocol.oauth1.TokenCredentialsRequest;
import org.miaixz.bus.auth.protocol.oauth1.TokenCredentialsResponse;
import org.miaixz.bus.auth.protocol.oauth1.codec.OAuth1AuthorizationHeaderCodec;
import org.miaixz.bus.auth.protocol.oauth1.codec.OAuth1FormCodec;
import org.miaixz.bus.auth.protocol.oauth1.codec.OAuth1ResponseDecoder;
import org.miaixz.bus.auth.protocol.oauth1.security.OAuth1Signer;
import org.miaixz.bus.auth.resolver.CredentialStore;
import org.miaixz.bus.auth.shared.ExecutionServices;
import org.miaixz.bus.auth.shared.SecretLease;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.extra.json.JsonValue;
import org.miaixz.bus.fabric.Fabric;

/**
 * Exchanges an authorized temporary credential for RFC 5849 token credentials.
 *
 * @author Kimi Liu
 */
public final class TokenCredentialsClient {

    /**
     * Dynamic credential key purpose for token credential secrets.
     */
    static final String TOKEN_PURPOSE = "oauth1-token";

    /**
     * Namespace used to isolate dynamic credentials.
     */
    private final String namespaceId;

    /**
     * Source registration that owns dynamic credentials.
     */
    private final String sourceId;

    /**
     * Validated OAuth 1.0 client settings.
     */
    private final OAuth1ClientSettings settings;

    /**
     * Externally implemented runtime dependencies.
     */
    private final ExecutionServices services;

    /**
     * Authorization header codec.
     */
    private final OAuth1AuthorizationHeaderCodec headerCodec;

    /**
     * Form body codec.
     */
    private final OAuth1FormCodec formCodec;

    /**
     * Strict credentials response decoder.
     */
    private final OAuth1ResponseDecoder responseDecoder;

    /**
     * OAuth 1.0 request signer.
     */
    private final OAuth1Signer signer;

    /**
     * Creates a token credentials client for one compiled Source.
     *
     * @param namespaceId namespace that isolates dynamic credentials
     * @param sourceId    Source registration identifier
     * @param settings    validated OAuth 1.0 settings
     * @param services    externally owned runtime dependencies
     */
    public TokenCredentialsClient(final String namespaceId, final String sourceId, final OAuth1ClientSettings settings,
            final ExecutionServices services) {
        this.namespaceId = Assert.notBlank(namespaceId, "OAuth 1.0 namespace id must not be blank");
        this.sourceId = Assert.notBlank(sourceId, "OAuth 1.0 Source id must not be blank");
        this.settings = Assert.notNull(settings, "OAuth 1.0 client settings must not be null");
        this.services = Assert.notNull(services, "OAuth 1.0 execution services must not be null");
        this.headerCodec = new OAuth1AuthorizationHeaderCodec();
        this.formCodec = new OAuth1FormCodec();
        this.responseDecoder = new OAuth1ResponseDecoder(formCodec,
                services.securityBaseline().require(Protocol.OAUTH1).maximumMessageBytes());
        this.signer = new OAuth1Signer(services);
    }

    /**
     * Combines dedicated token and verifier fields with extension parameters for signing.
     *
     * @param request standard request
     * @return immutable decoded parameter list
     */
    private static List<OAuth1Parameter> requestParameters(final TokenCredentialsRequest request) {
        final List<OAuth1Parameter> result = new ArrayList<>(request.parameters().size() + 2);
        result.add(new OAuth1Parameter(OAuth1.Parameters.TOKEN, request.oauthToken()));
        result.add(new OAuth1Parameter(OAuth1.Parameters.VERIFIER, request.oauthVerifier()));
        result.addAll(request.parameters());
        return List.copyOf(result);
    }

    /**
     * Returns application parameters that belong in a form body.
     *
     * @param parameters complete request parameters
     * @return immutable non-OAuth parameter list
     */
    private static List<OAuth1Parameter> nonOAuth(final List<OAuth1Parameter> parameters) {
        final List<OAuth1Parameter> result = new ArrayList<>();
        for (OAuth1Parameter parameter : parameters) {
            if (!parameter.name().startsWith("oauth_")) {
                result.add(parameter);
            }
        }
        return List.copyOf(result);
    }

    /**
     * Wraps an outcome in a completed stage.
     *
     * @param outcome outcome to wrap
     * @param <T>     success value type
     * @return completed stage
     */
    private static <T> CompletionStage<Outcome<T>> completed(final Outcome<T> outcome) {
        return CompletableFuture.completedFuture(outcome);
    }

    /**
     * Creates a safe failure using an existing Bus error.
     *
     * @param error       existing Bus error
     * @param description non-sensitive description
     * @return closed failure value
     */
    private static Outcome.Failure failure(final Errors error, final String description) {
        return new Outcome.Failure(error, description, new JsonValue.ObjectValue(Map.of()));
    }

    /**
     * Obtains token credentials according to RFC 5849 section 2.3.
     *
     * @param request standard token credentials request
     * @param context immutable invocation context
     * @param timeout shared end-to-end time budget
     * @return stage containing the standard response or a closed framework failure
     */
    public CompletionStage<Outcome<TokenCredentialsResponse>> tokenCredentials(
            final TokenCredentialsRequest request,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(request, "Token credentials request must not be null");
        Assert.notNull(context, "OAuth 1.0 invocation context must not be null");
        Assert.notNull(timeout, "OAuth 1.0 time budget must not be null");
        return services.credentialStore()
                .take(key(TemporaryCredentialsClient.TEMPORARY_PURPOSE, request.oauthToken()), context, timeout)
                .thenCompose(resolved -> switch (resolved) {
                    case Outcome.Succeeded<SecretLease> success -> exchange(request, success.value(), context, timeout);
                    case Outcome.Rejected<SecretLease> rejected -> completed(Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<SecretLease> failed -> completed(Outcome.failed(failed.failure()));
                });
    }

    /**
     * Signs and executes the token credentials exchange with one consumed temporary secret lease.
     *
     * @param request         standard token credentials request
     * @param temporarySecret atomically consumed temporary credential secret
     * @param context         immutable invocation context
     * @param timeout         shared time budget
     * @return asynchronous token credentials outcome
     */
    private CompletionStage<Outcome<TokenCredentialsResponse>> exchange(
            final TokenCredentialsRequest request,
            final SecretLease temporarySecret,
            final Context context,
            final Timeout.Budget timeout) {
        final List<OAuth1Parameter> parameters = requestParameters(request);
        return signer.sign(
                settings.tokenCredentialsEndpoint().method().getOrNull(),
                settings.tokenCredentialsEndpoint().url(),
                parameters,
                settings,
                Optional.of(temporarySecret),
                context,
                timeout).thenCompose(signed -> switch (signed) {
                    case Outcome.Succeeded<List<OAuth1Parameter>> success -> execute(
                            parameters,
                            success.value(),
                            context,
                            timeout);
                    case Outcome.Rejected<List<OAuth1Parameter>> rejected -> completed(
                            Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<List<OAuth1Parameter>> failed -> completed(Outcome.failed(failed.failure()));
                }).whenComplete((ignored, thrown) -> temporarySecret.close());
    }

    /**
     * Executes the signed HTTP request and stores the returned token credential secret.
     *
     * @param parameters complete decoded request parameters
     * @param signed     signed Authorization header parameters
     * @param context    immutable invocation context
     * @param timeout    shared time budget
     * @return response outcome completed after dynamic secret persistence
     */
    private CompletionStage<Outcome<TokenCredentialsResponse>> execute(
            final List<OAuth1Parameter> parameters,
            final List<OAuth1Parameter> signed,
            final Context context,
            final Timeout.Budget timeout) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                final List<OAuth1Parameter> body = nonOAuth(parameters);
                final var builder = Fabric.http(services.fabricContext())
                        .url(settings.tokenCredentialsEndpoint().url().toString())
                        .method(settings.tokenCredentialsEndpoint().method().getOrNull())
                        .header(Http.Header.AUTHORIZATION, headerCodec.encode(withRealm(signed)))
                        .timeout(timeout.forFabric())
                        .addressPolicy(services.securityBaseline().require(Protocol.OAUTH1).addressPolicy());
                if (!body.isEmpty()) {
                    builder.body(formCodec.encode(body), MediaType.APPLICATION_FORM_URLENCODED_TYPE);
                }
                return Outcome.succeeded(responseDecoder.tokenCredentials(builder.execute()));
            } catch (final RuntimeException failure) {
                return Outcome.<TokenCredentialsResponse>failed(
                        failure(ErrorCode._502, "OAuth 1.0 token credentials request failed"));
            }
        }, services.executor()).thenCompose(outcome -> switch (outcome) {
            case Outcome.Succeeded<TokenCredentialsResponse> success -> store(success.value(), context, timeout);
            case Outcome.Rejected<TokenCredentialsResponse> rejected -> completed(Outcome.rejected(rejected.failure()));
            case Outcome.Failed<TokenCredentialsResponse> failed -> completed(Outcome.failed(failed.failure()));
        });
    }

    /**
     * Stores the long-lived token credential secret in the external credential store.
     *
     * @param response decoded token credentials response
     * @param context  immutable invocation context
     * @param timeout  shared time budget
     * @return original response only after secure persistence succeeds
     */
    private CompletionStage<Outcome<TokenCredentialsResponse>> store(
            final TokenCredentialsResponse response,
            final Context context,
            final Timeout.Budget timeout) {
        final SecretLease lease = new SecretLease(response.oauthTokenSecret().toCharArray());
        final CompletionStage<Outcome<Void>> stage;
        try {
            stage = services.credentialStore()
                    .store(key(TOKEN_PURPOSE, response.oauthToken()), lease, Optional.empty(), context, timeout);
        } catch (final RuntimeException failure) {
            lease.close();
            return completed(Outcome.failed(failure(ErrorCode._500, "OAuth 1.0 token credential storage failed")));
        }
        return stage.handle((stored, thrown) -> {
            lease.close();
            if (thrown != null) {
                return Outcome.<TokenCredentialsResponse>failed(
                        failure(ErrorCode._500, "OAuth 1.0 token credential storage failed"));
            }
            return switch (stored) {
                case Outcome.Succeeded<Void> ignored -> Outcome.succeeded(response);
                case Outcome.Rejected<Void> rejected -> Outcome.rejected(rejected.failure());
                case Outcome.Failed<Void> failed -> Outcome.failed(failed.failure());
            };
        });
    }

    /**
     * Adds the optional non-signing Authorization realm.
     *
     * @param signed signed OAuth parameters
     * @return immutable header parameters
     */
    private List<OAuth1Parameter> withRealm(final List<OAuth1Parameter> signed) {
        if (settings.realm().isEmpty()) {
            return signed;
        }
        final List<OAuth1Parameter> result = new ArrayList<>(signed.size() + 1);
        result.add(new OAuth1Parameter("realm", settings.realm().getOrNull()));
        result.addAll(signed);
        return List.copyOf(result);
    }

    /**
     * Builds a dynamic credential key using an irreversible OAuth token digest.
     *
     * @param purpose stable credential purpose
     * @param token   opaque OAuth token identifier
     * @return isolated external credential key
     */
    private CredentialStore.Key key(final String purpose, final String token) {
        return new CredentialStore.Key(namespaceId, sourceId, purpose, Builder.sha256Hex(token),
                Credential.Type.SHARED_SECRET);
    }

}
