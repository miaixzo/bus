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
import org.miaixz.bus.auth.protocol.oauth1.ProtectedResourceRequest;
import org.miaixz.bus.auth.protocol.oauth1.codec.OAuth1AuthorizationHeaderCodec;
import org.miaixz.bus.auth.protocol.oauth1.security.OAuth1Signer;
import org.miaixz.bus.auth.resolver.CredentialStore;
import org.miaixz.bus.auth.shared.ExecutionServices;
import org.miaixz.bus.auth.shared.SecretLease;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.extra.json.JsonValue;
import org.miaixz.bus.fabric.Fabric;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;

/**
 * Signs and executes RFC 5849 protected-resource requests.
 *
 * @author Kimi Liu
 */
public final class ProtectedResourceClient {

    /**
     * Namespace used to isolate token credential secrets.
     */
    private final String namespaceId;

    /**
     * Source registration that owns token credential secrets.
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
     * OAuth 1.0 request signer.
     */
    private final OAuth1Signer signer;

    /**
     * Creates a protected-resource client for one compiled Source.
     *
     * @param namespaceId namespace that isolates dynamic credentials
     * @param sourceId    Source registration identifier
     * @param settings    validated OAuth 1.0 settings
     * @param services    externally owned runtime dependencies
     */
    public ProtectedResourceClient(final String namespaceId, final String sourceId, final OAuth1ClientSettings settings,
            final ExecutionServices services) {
        this.namespaceId = Assert.notBlank(namespaceId, "OAuth 1.0 namespace id must not be blank");
        this.sourceId = Assert.notBlank(sourceId, "OAuth 1.0 Source id must not be blank");
        this.settings = Assert.notNull(settings, "OAuth 1.0 client settings must not be null");
        this.services = Assert.notNull(services, "OAuth 1.0 execution services must not be null");
        this.headerCodec = new OAuth1AuthorizationHeaderCodec();
        this.signer = new OAuth1Signer(services);
    }

    /**
     * Extracts the unique token credential identifier from signed protocol parameters.
     *
     * @param parameters decoded request parameters
     * @return opaque token credential identifier
     * @throws ValidateException if oauth_token is absent or duplicated
     */
    private static String token(final List<OAuth1Parameter> parameters) {
        String token = null;
        for (OAuth1Parameter parameter : parameters) {
            if (OAuth1.Parameters.TOKEN.equals(parameter.name())) {
                if (token != null) {
                    throw new ValidateException("Protected resource request must contain exactly one oauth_token");
                }
                token = Assert.notBlank(parameter.value(), "Protected resource oauth_token must not be blank");
            }
        }
        if (token == null) {
            throw new ValidateException("Protected resource request must contain exactly one oauth_token");
        }
        return token;
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
     * Accesses a protected resource according to RFC 5849 section 3.
     *
     * @param request exact request before OAuth signing
     * @param context immutable invocation context
     * @param timeout shared end-to-end time budget
     * @return stage containing a caller-owned Fabric response or a closed framework failure
     */
    public CompletionStage<Outcome<HttpResponse>> access(
            final ProtectedResourceRequest request,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(request, "Protected resource request must not be null");
        Assert.notNull(context, "OAuth 1.0 invocation context must not be null");
        Assert.notNull(timeout, "OAuth 1.0 time budget must not be null");
        final String token = token(request.parameters());
        return services.credentialStore().resolve(key(token), context, timeout)
                .thenCompose(resolved -> switch (resolved) {
                    case Outcome.Succeeded<SecretLease> success -> signAndExecute(
                            request,
                            success.value(),
                            context,
                            timeout);
                    case Outcome.Rejected<SecretLease> rejected -> completed(Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<SecretLease> failed -> completed(Outcome.failed(failed.failure()));
                });
    }

    /**
     * Signs and executes one request while retaining the resolved token secret lease.
     *
     * @param request     exact protected resource request
     * @param tokenSecret resolved token credential secret
     * @param context     immutable invocation context
     * @param timeout     shared time budget
     * @return asynchronous response outcome
     */
    private CompletionStage<Outcome<HttpResponse>> signAndExecute(
            final ProtectedResourceRequest request,
            final SecretLease tokenSecret,
            final Context context,
            final Timeout.Budget timeout) {
        return signer.sign(
                request.method(),
                request.url(),
                request.parameters(),
                settings,
                Optional.of(tokenSecret),
                context,
                timeout).thenCompose(signed -> switch (signed) {
                    case Outcome.Succeeded<List<OAuth1Parameter>> success -> execute(request, success.value(), timeout);
                    case Outcome.Rejected<List<OAuth1Parameter>> rejected -> completed(
                            Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<List<OAuth1Parameter>> failed -> completed(Outcome.failed(failed.failure()));
                }).whenComplete((ignored, thrown) -> tokenSecret.close());
    }

    /**
     * Executes an already signed request on the caller-owned executor.
     *
     * @param request exact protected resource request
     * @param signed  signed Authorization header parameters
     * @param timeout shared time budget
     * @return asynchronous caller-owned response outcome
     */
    private CompletionStage<Outcome<HttpResponse>> execute(
            final ProtectedResourceRequest request,
            final List<OAuth1Parameter> signed,
            final Timeout.Budget timeout) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                final var builder = Fabric.http(services.fabricContext()).url(request.url().toString())
                        .method(request.method()).headers(request.headers())
                        .header(Http.Header.AUTHORIZATION, headerCodec.encode(withRealm(signed)))
                        .timeout(timeout.forFabric())
                        .addressPolicy(services.securityBaseline().require(Protocol.OAUTH1).addressPolicy());
                if (request.body().length > 0) {
                    builder.body(request.body());
                }
                return Outcome.succeeded(builder.execute());
            } catch (final RuntimeException failure) {
                return Outcome
                        .<HttpResponse>failed(failure(ErrorCode._502, "OAuth 1.0 protected resource request failed"));
            }
        }, services.executor());
    }

    /**
     * Builds an isolated token credential key from an irreversible digest.
     *
     * @param token opaque token credential identifier
     * @return external credential key
     */
    private CredentialStore.Key key(final String token) {
        return new CredentialStore.Key(namespaceId, sourceId, TokenCredentialsClient.TOKEN_PURPOSE,
                Builder.sha256Hex(token), Credential.Type.SHARED_SECRET);
    }

    /**
     * Adds an optional non-signing realm to Authorization header parameters.
     *
     * @param signed signed OAuth parameters
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
