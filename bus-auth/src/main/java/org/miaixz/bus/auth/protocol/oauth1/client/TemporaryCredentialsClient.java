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

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Credential;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.protocol.oauth1.OAuth1Parameter;
import org.miaixz.bus.auth.protocol.oauth1.TemporaryCredentialsRequest;
import org.miaixz.bus.auth.protocol.oauth1.TemporaryCredentialsResponse;
import org.miaixz.bus.auth.protocol.oauth1.codec.OAuth1AuthorizationHeaderCodec;
import org.miaixz.bus.auth.protocol.oauth1.codec.OAuth1FormCodec;
import org.miaixz.bus.auth.protocol.oauth1.codec.OAuth1ResponseDecoder;
import org.miaixz.bus.auth.protocol.oauth1.security.OAuth1Signer;
import org.miaixz.bus.auth.resolver.CredentialStore;
import org.miaixz.bus.auth.shared.ExecutionServices;
import org.miaixz.bus.auth.shared.SecretLease;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.extra.json.JsonValue;
import org.miaixz.bus.fabric.Fabric;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;

/**
 * Executes the RFC 5849 temporary credentials request for one compiled OAuth 1.0 Source.
 *
 * @author Kimi Liu
 */
public final class TemporaryCredentialsClient {

    /**
     * Dynamic credential key purpose for temporary credential secrets.
     */
    static final String TEMPORARY_PURPOSE = "oauth1-temporary";

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
     * RFC 5849 Authorization header codec.
     */
    private final OAuth1AuthorizationHeaderCodec headerCodec;

    /**
     * RFC 5849 form codec.
     */
    private final OAuth1FormCodec formCodec;

    /**
     * Strict temporary and token credentials response decoder.
     */
    private final OAuth1ResponseDecoder responseDecoder;

    /**
     * OAuth 1.0 request signer.
     */
    private final OAuth1Signer signer;

    /**
     * Creates a temporary credentials client for one compiled Source.
     *
     * @param namespaceId namespace that isolates dynamic credentials
     * @param sourceId    Source registration identifier
     * @param settings    validated OAuth 1.0 settings
     * @param services    externally owned runtime dependencies
     */
    public TemporaryCredentialsClient(final String namespaceId, final String sourceId,
            final OAuth1ClientSettings settings, final ExecutionServices services) {
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
     * Returns only application parameters that belong in the form body.
     *
     * @param parameters complete decoded request parameters
     * @return immutable non-OAuth parameters
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
     * Creates a completed outcome stage.
     *
     * @param outcome outcome to wrap
     * @param <T>     success value type
     * @return completed stage
     */
    private static <T> CompletionStage<Outcome<T>> completed(final Outcome<T> outcome) {
        return CompletableFuture.completedFuture(outcome);
    }

    /**
     * Creates a safe framework failure using an existing Bus error.
     *
     * @param error       existing Bus error
     * @param description non-sensitive failure description
     * @return closed failure value
     */
    private static Outcome.Failure failure(
            final org.miaixz.bus.core.basic.normal.Errors error,
            final String description) {
        return new Outcome.Failure(error, description, new JsonValue.ObjectValue(Map.of()));
    }

    /**
     * Obtains and securely retains temporary credentials according to RFC 5849 section 2.1.
     *
     * @param request standard temporary credentials request
     * @param context immutable invocation context
     * @param timeout shared end-to-end time budget
     * @return stage containing the standard response or a closed framework failure
     */
    public CompletionStage<Outcome<TemporaryCredentialsResponse>> temporaryCredentials(
            final TemporaryCredentialsRequest request,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(request, "Temporary credentials request must not be null");
        Assert.notNull(context, "OAuth 1.0 invocation context must not be null");
        Assert.notNull(timeout, "OAuth 1.0 time budget must not be null");
        return signer.sign(
                settings.temporaryCredentialsEndpoint().method().getOrNull(),
                settings.temporaryCredentialsEndpoint().url(),
                request.parameters(),
                settings,
                Optional.empty(),
                context,
                timeout).thenCompose(signed -> switch (signed) {
                    case Outcome.Succeeded<List<OAuth1Parameter>> success -> execute(
                            request,
                            success.value(),
                            context,
                            timeout);
                    case Outcome.Rejected<List<OAuth1Parameter>> rejected -> completed(
                            Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<List<OAuth1Parameter>> failed -> completed(Outcome.failed(failed.failure()));
                });
    }

    /**
     * Executes the signed request on the caller-owned executor and stores the returned temporary secret.
     *
     * @param request original standard request
     * @param signed  signed OAuth Authorization header parameters
     * @param context immutable invocation context
     * @param timeout shared time budget
     * @return asynchronous standard response outcome
     */
    private CompletionStage<Outcome<TemporaryCredentialsResponse>> execute(
            final TemporaryCredentialsRequest request,
            final List<OAuth1Parameter> signed,
            final Context context,
            final Timeout.Budget timeout) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                final List<OAuth1Parameter> bodyParameters = nonOAuth(request.parameters());
                final var builder = Fabric.http(services.fabricContext())
                        .url(settings.temporaryCredentialsEndpoint().url().toString())
                        .method(settings.temporaryCredentialsEndpoint().method().getOrNull())
                        .header(Http.Header.AUTHORIZATION, headerCodec.encode(withRealm(signed)))
                        .timeout(timeout.forFabric())
                        .addressPolicy(services.securityBaseline().require(Protocol.OAUTH1).addressPolicy());
                if (!bodyParameters.isEmpty()) {
                    builder.body(formCodec.encode(bodyParameters), MediaType.APPLICATION_FORM_URLENCODED_TYPE);
                }
                final HttpResponse response = builder.execute();
                return Outcome.succeeded(responseDecoder.temporaryCredentials(response));
            } catch (final RuntimeException failure) {
                return Outcome.<TemporaryCredentialsResponse>failed(
                        failure(ErrorCode._502, "OAuth 1.0 temporary credentials request failed"));
            }
        }, services.executor()).thenCompose(outcome -> switch (outcome) {
            case Outcome.Succeeded<TemporaryCredentialsResponse> success -> store(success.value(), context, timeout);
            case Outcome.Rejected<TemporaryCredentialsResponse> rejected -> completed(
                    Outcome.rejected(rejected.failure()));
            case Outcome.Failed<TemporaryCredentialsResponse> failed -> completed(Outcome.failed(failed.failure()));
        });
    }

    /**
     * Stores the returned temporary credential secret until the browser callback is consumed.
     *
     * @param response decoded standard temporary credentials response
     * @param context  immutable invocation context
     * @param timeout  shared time budget
     * @return response outcome completed only after secure persistence finishes
     */
    private CompletionStage<Outcome<TemporaryCredentialsResponse>> store(
            final TemporaryCredentialsResponse response,
            final Context context,
            final Timeout.Budget timeout) {
        final SecretLease lease = new SecretLease(response.oauthTokenSecret().toCharArray());
        final Instant expiresAt = timeout.clock().now().plus(settings.temporaryCredentialLifetime());
        final CompletionStage<Outcome<Void>> stage;
        try {
            stage = services.credentialStore().store(
                    credentialKey(TEMPORARY_PURPOSE, response.oauthToken()),
                    lease,
                    Optional.of(expiresAt),
                    context,
                    timeout);
        } catch (final RuntimeException failure) {
            lease.close();
            return completed(Outcome.failed(failure(ErrorCode._500, "OAuth 1.0 temporary credential storage failed")));
        }
        return stage.handle((stored, thrown) -> {
            lease.close();
            if (thrown != null) {
                return Outcome.<TemporaryCredentialsResponse>failed(
                        failure(ErrorCode._500, "OAuth 1.0 temporary credential storage failed"));
            }
            return switch (stored) {
                case Outcome.Succeeded<Void> ignored -> Outcome.succeeded(response);
                case Outcome.Rejected<Void> rejected -> Outcome.rejected(rejected.failure());
                case Outcome.Failed<Void> failed -> Outcome.failed(failed.failure());
            };
        });
    }

    /**
     * Builds an isolated dynamic credential key from a one-way token digest.
     *
     * @param purpose stable credential purpose
     * @param token   opaque OAuth credential identifier
     * @return isolated credential store key
     */
    CredentialStore.Key credentialKey(final String purpose, final String token) {
        return new CredentialStore.Key(namespaceId, sourceId, purpose, Builder.sha256Hex(token),
                Credential.Type.SHARED_SECRET);
    }

    /**
     * Prepends an optional Authorization header realm without signing it.
     *
     * @param signed signed OAuth protocol parameters
     * @return immutable header parameter list
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

}
