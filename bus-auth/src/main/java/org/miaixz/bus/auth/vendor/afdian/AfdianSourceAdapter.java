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
package org.miaixz.bus.auth.vendor.afdian;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.codec.FormCodec;
import org.miaixz.bus.auth.codec.NameValue;
import org.miaixz.bus.auth.codec.QueryCodec;
import org.miaixz.bus.auth.protocol.oauth2.AuthorizationCodeResponse;
import org.miaixz.bus.auth.protocol.oauth2.GrantType;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2;
import org.miaixz.bus.auth.protocol.oauth2.ResponseType;
import org.miaixz.bus.auth.protocol.oauth2.codec.AuthorizationResponseDecoder;
import org.miaixz.bus.auth.shared.SecretLease;
import org.miaixz.bus.auth.source.DriverServices;
import org.miaixz.bus.auth.source.ExternalIdentity;
import org.miaixz.bus.auth.source.SourceWorkflow;
import org.miaixz.bus.auth.vendor.RedirectManager;
import org.miaixz.bus.auth.vendor.VariantManifest;
import org.miaixz.bus.auth.vendor.VendorAdapter;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.extra.json.JsonValue;
import org.miaixz.bus.fabric.Fabric;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;

/**
 * Implements the frozen Afdian creator browser authentication contract.
 *
 * @author Kimi Liu
 */
public final class AfdianSourceAdapter implements VendorAdapter {

    /**
     * Registered Source identifier.
     */
    private final String sourceId;
    /**
     * Selected Afdian manifest.
     */
    private final VariantManifest.Variant variant;
    /**
     * Validated Afdian options.
     */
    private final AfdianOptions options;
    /**
     * External runtime dependencies.
     */
    private final DriverServices services;
    /**
     * Shared browser security lifecycle.
     */
    private final RedirectManager redirectManager;
    /**
     * Strict query codec.
     */
    private final QueryCodec queryCodec = new QueryCodec();
    /**
     * Strict form codec.
     */
    private final FormCodec formCodec = new FormCodec();
    /**
     * Standard OAuth 2.x authorization response decoder.
     */
    private final AuthorizationResponseDecoder authorizationResponseDecoder = new AuthorizationResponseDecoder();

    /**
     * Creates one Source-bound Afdian adapter.
     *
     * @param namespaceId registration namespace
     * @param sourceId    registration Source identifier
     * @param manifest    selected Afdian manifest
     * @param variant     selected default manifest
     * @param options     decoded Afdian options
     * @param services    external runtime dependencies
     */
    public AfdianSourceAdapter(final String namespaceId, final String sourceId, final AfdianManifest manifest,
            final VariantManifest.Variant variant, final AfdianOptions options, final DriverServices services) {
        Assert.notNull(manifest, "Afdian manifest must not be null");
        this.sourceId = Assert.notBlank(sourceId, "Afdian Source id must not be blank");
        this.variant = Assert.notNull(variant, "Afdian manifest must not be null");
        this.options = Assert.notNull(options, "Afdian options must not be null");
        this.services = Assert.notNull(services, "Afdian execution services must not be null");
        this.redirectManager = RedirectManager.create(namespaceId, sourceId, variant, options, services);
    }

    /**
     * Narrows a delegated outcome through the declared response class.
     *
     * @param stage        delegated stage
     * @param responseType declared success class
     * @param <S>          expected success type
     * @return narrowed stage
     */
    private static <S> CompletionStage<Outcome<S>> narrow(
            final CompletionStage<? extends Outcome<?>> stage,
            final Class<S> responseType) {
        return stage.thenApply(outcome -> switch (outcome) {
            case Outcome.Succeeded<?> success -> Outcome.succeeded(responseType.cast(success.value()));
            case Outcome.Rejected<?> rejected -> Outcome.rejected(rejected.failure());
            case Outcome.Failed<?> failed -> Outcome.failed(failed.failure());
        });
    }

    /**
     * Creates a typed rejection for a capability absent from the compiled Afdian manifest.
     *
     * @param <S> expected success type of the attempted invocation
     * @return completed undeclared-capability rejection
     */
    private static <S> CompletionStage<Outcome<S>> missing() {
        return completed(rejected("Afdian capability is not declared"));
    }

    /**
     * Creates a typed rejection when the request value does not match the declared Afdian capability contract.
     *
     * @param <S> expected success type of the attempted invocation
     * @return completed request mismatch rejection
     */
    private static <S> CompletionStage<Outcome<S>> mismatch() {
        return completed(rejected("Afdian capability request is invalid"));
    }

    /**
     * Wraps an already classified Afdian outcome in a completed asynchronous stage.
     *
     * @param <T>     success value type
     * @param outcome classified outcome to expose asynchronously
     * @return completed outcome stage
     */
    private static <T> CompletionStage<Outcome<T>> completed(final Outcome<T> outcome) {
        return CompletableFuture.completedFuture(outcome);
    }

    /**
     * Creates an expected client-side Afdian rejection without retaining platform response material.
     *
     * @param <T>         expected success value type
     * @param description sanitized rejection description
     * @return safe expected rejection
     */
    private static <T> Outcome<T> rejected(final String description) {
        return Outcome.rejected(new Outcome.Failure(ErrorCode._400, description, new JsonValue.ObjectValue(Map.of())));
    }

    /**
     * Creates a sanitized upstream Afdian failure without retaining credentials or raw platform payloads.
     *
     * @param <T>         expected success value type
     * @param description sanitized upstream failure description
     * @return safe upstream failure
     */
    private static <T> Outcome<T> failed(final String description) {
        return Outcome.failed(new Outcome.Failure(ErrorCode._502, description, new JsonValue.ObjectValue(Map.of())));
    }

    /**
     * @return exact Afdian runtime manifest
     */
    @Override
    public Capability.Manifest manifest() {
        return variant.capabilityManifest();
    }

    /**
     * Routes the two Source authentication capabilities.
     *
     * @param capability runtime-owned capability
     * @param request    exact request
     * @param context    invocation context
     * @param timeout    shared budget
     * @param <Q>        request type
     * @param <S>        success type
     * @return Afdian outcome
     */
    @Override
    public <Q, S> CompletionStage<Outcome<S>> invoke(
            final Capability<Q, S> capability,
            final Q request,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(capability, "Afdian capability must not be null");
        Assert.notNull(context, "Afdian context must not be null");
        Assert.notNull(timeout, "Afdian budget must not be null");
        if (!manifest().capabilities().contains(capability))
            return missing();
        if (capability.key().equals(SourceWorkflow.INITIATE.key())
                && request instanceof SourceWorkflow.Request.BrowserStart start) {
            return narrow(redirectManager.initiate(start, this::prepare, context, timeout), capability.responseType());
        }
        if (capability.key().equals(SourceWorkflow.COMPLETE.key())
                && request instanceof SourceWorkflow.Request.BrowserCallback callback) {
            return narrow(
                    redirectManager.complete(callback, this::state, this::identity, context, timeout),
                    capability.responseType());
        }
        return mismatch();
    }

    /**
     * Builds the exact ordered Afdian authorization redirect.
     *
     * @param initiation generated state
     * @param context    unchanged invocation context
     * @param timeout    shared budget
     * @return prepared redirect
     */
    private CompletionStage<Outcome<RedirectManager.Prepared>> prepare(
            final RedirectManager.Initiation initiation,
            final Context context,
            final Timeout.Budget timeout) {
        if (timeout.expired())
            return completed(failed("Afdian authorization budget is exhausted"));
        final List<String> scopes = options.scopes().isEmpty() ? variant.defaultScopes() : options.scopes();
        final List<NameValue> parameters = List.of(
                new NameValue(OAuth2.Parameters.RESPONSE_TYPE, ResponseType.CODE.value()),
                new NameValue(OAuth2.Parameters.SCOPE, String.join(Symbol.SPACE, scopes)),
                new NameValue(OAuth2.Parameters.CLIENT_ID, options.clientId()),
                new NameValue(OAuth2.Parameters.REDIRECT_URI, options.redirectUri().getOrNull()),
                new NameValue(OAuth2.Parameters.STATE, initiation.state()));
        final String base = variant.targets().resolve(options).authorization().getOrNull().url().toString();
        return completed(
                Outcome.succeeded(
                        new RedirectManager.Prepared(
                                base + (base.contains(Symbol.QUESTION_MARK) ? Symbol.C_AND : Symbol.C_QUESTION_MARK)
                                        + queryCodec.encode(parameters),
                                initiation.state())));
    }

    /**
     * Returns the unique callback state after exact callback validation.
     *
     * @param callback raw callback
     * @return state value
     */
    private String state(final Callback.Inbound callback) {
        return authorizationResponse(callback).state().getOrNull();
    }

    /**
     * Resolves the client secret and starts the private Afdian identity exchange.
     *
     * @param completion consumed callback material
     * @param context    invocation context
     * @param timeout    shared budget
     * @return verified external identity
     */
    private CompletionStage<Outcome<ExternalIdentity>> identity(
            final RedirectManager.Completion completion,
            final Context context,
            final Timeout.Budget timeout) {
        final String code;
        try {
            code = authorizationResponse(completion.callback()).code();
        } catch (RuntimeException cause) {
            return completed(rejected("Afdian callback is invalid"));
        }
        return Outcome
                .mapStage(
                        () -> services.secretLoader()
                                .load(services.registration(), options.credential(), context, timeout),
                        loaded -> services.secretParser().parse(services.registration(), options.credential(), loaded))
                .thenCompose(outcome -> switch (outcome) {
                    case Outcome.Succeeded<SecretLease> success -> exchange(code, success.value(), timeout);
                    case Outcome.Rejected<SecretLease> rejected -> completed(Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<SecretLease> failed -> completed(Outcome.failed(failed.failure()));
                });
    }

    /**
     * Sends the exact Afdian form and consumes its private response envelope.
     *
     * @param code    callback authorization code
     * @param secret  owned client-secret lease
     * @param timeout shared budget
     * @return verified identity outcome
     */
    private CompletionStage<Outcome<ExternalIdentity>> exchange(
            final String code,
            final SecretLease secret,
            final Timeout.Budget timeout) {
        return CompletableFuture.supplyAsync(() -> {
            byte[] body = null;
            try {
                body = formCodec.encode(
                        List.of(
                                new NameValue(OAuth2.Parameters.GRANT_TYPE, GrantType.AUTHORIZATION_CODE.value()),
                                new NameValue(OAuth2.Parameters.CLIENT_ID, options.clientId()),
                                new NameValue(OAuth2.Parameters.CLIENT_SECRET, new String(secret.material())),
                                new NameValue(OAuth2.Parameters.CODE, code),
                                new NameValue(OAuth2.Parameters.REDIRECT_URI, options.redirectUri().getOrNull())));
                final String endpoint = variant.targets().resolve(options).token().getOrNull().url().toString();
                try (HttpResponse response = Fabric.http(services.fabricContext()).url(endpoint)
                        .method(Http.Method.POST).timeout(timeout.forFabric())
                        .addressPolicy(services.securityBaseline().require(Protocol.VENDOR_AUTH).addressPolicy())
                        .body(body, MediaType.APPLICATION_FORM_URLENCODED_TYPE).execute()) {
                    if (response.code() == 429 || response.code() >= Http.Status.INTERNAL_SERVER_ERROR) {
                        return failed("Afdian identity endpoint is unavailable");
                    }
                    if (response.code() >= Http.Status.BAD_REQUEST) {
                        return rejected("Afdian rejected the authorization code");
                    }
                    if (response.code() != Http.Status.OK
                            || !MediaType.APPLICATION_JSON_TYPE.isCompatible(response.body().media())) {
                        return failed("Afdian returned an invalid identity response");
                    }
                    return decode(response.bytes(Normal.MEBI), timeout);
                }
            } catch (RuntimeException cause) {
                return failed("Afdian identity exchange failed");
            } finally {
                if (body != null)
                    Arrays.fill(body, (byte) 0);
                secret.close();
            }
        }, services.executor());
    }

    /**
     * Decodes exact ec/data.user_id semantics without creating an OAuth token model.
     *
     * @param body    bounded JSON body
     * @param timeout shared clock
     * @return verified identity or closed failure
     */
    private Outcome<ExternalIdentity> decode(final byte[] body, final Timeout.Budget timeout) {
        final JsonValue parsed = services.jsonProvider().readValue(body);
        if (!(parsed instanceof JsonValue.ObjectValue root)
                || !(root.values().get("ec") instanceof JsonValue.NumberValue ec)) {
            return failed("Afdian response requires integral ec");
        }
        final long status;
        try {
            status = ec.value().longValueExact();
        } catch (ArithmeticException cause) {
            return failed("Afdian response ec is not integral");
        }
        if (status != 200L)
            return rejected("Afdian rejected the identity exchange");
        if (!(root.values().get("data") instanceof JsonValue.ObjectValue data)
                || !(data.values().get("user_id") instanceof JsonValue.StringValue user) || user.value().isBlank()) {
            return failed("Afdian response requires data.user_id");
        }
        final JsonValue privateId = data.values().get("user_private_id");
        if (privateId != null && !(privateId instanceof JsonValue.StringValue)) {
            return failed("Afdian response user_private_id must be a string");
        }
        final Map<String, JsonValue> attributes = new LinkedHashMap<>(data.values());
        attributes.remove("user_id");
        final Evidence evidence = new Evidence(Evidence.Type.FEDERATED, Evidence.Strength.SINGLE_FACTOR,
                new Evidence.Claim("afdian_user_id", new JsonValue.StringValue(user.value()), "https://afdian.net",
                        timeout.clock().now()));
        return Outcome.succeeded(
                new ExternalIdentity(sourceId, user.value(), new JsonValue.ObjectValue(attributes), List.of(evidence)));
    }

    /**
     * Decodes one standard OAuth authorization response after validating the registered callback URI.
     *
     * @param callback raw callback captured by the external project
     * @return standard authorization-code response
     */
    private AuthorizationCodeResponse authorizationResponse(final Callback.Inbound callback) {
        if (!options.redirectUri().getOrNull().equals(callback.requestUri())) {
            throw new IllegalArgumentException("Afdian callback transport is invalid");
        }
        return switch (authorizationResponseDecoder.decode(callback)) {
            case AuthorizationResponseDecoder.Success success -> success.response();
            case AuthorizationResponseDecoder.Error error -> throw new IllegalArgumentException(
                    "Afdian returned an OAuth authorization error");
        };
    }

}
