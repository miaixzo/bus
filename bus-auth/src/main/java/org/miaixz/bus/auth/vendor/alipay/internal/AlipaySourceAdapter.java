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
package org.miaixz.bus.auth.vendor.alipay.internal;

import java.net.URI;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.codec.FormCodec;
import org.miaixz.bus.auth.codec.Parameter;
import org.miaixz.bus.auth.codec.QueryCodec;
import org.miaixz.bus.auth.protocol.oauth2.GrantType;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2;
import org.miaixz.bus.auth.resolver.KeyResolver;
import org.miaixz.bus.auth.shared.ExecutionServices;
import org.miaixz.bus.auth.source.ExternalIdentity;
import org.miaixz.bus.auth.source.SourceAuthentication;
import org.miaixz.bus.auth.source.SourceAuthenticationRequest;
import org.miaixz.bus.auth.vendor.RedirectManager;
import org.miaixz.bus.auth.vendor.VendorAdapter;
import org.miaixz.bus.auth.vendor.VendorDefinition;
import org.miaixz.bus.auth.vendor.alipay.AlipayDefinition;
import org.miaixz.bus.auth.vendor.alipay.AlipaySourceSettings;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.*;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.crypto.center.Sign;
import org.miaixz.bus.extra.json.JsonValue;
import org.miaixz.bus.fabric.Fabric;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;

/**
 * Implements the frozen Alipay RSA2 browser and signed gateway identity flow.
 *
 * @author Kimi Liu
 */
public final class AlipaySourceAdapter implements VendorAdapter {

    /**
     * Trusted Alipay gateway authority.
     */
    private static final String AUTHORITY = "https://openapi.alipay.com";
    /**
     * Maximum nesting accepted for one Alipay gateway JSON document.
     */
    private static final int MAXIMUM_JSON_DEPTH = 64;
    /**
     * Alipay gateway timestamp formatter.
     */
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(java.time.ZoneId.of(ZoneId.CTT.getEnName()));

    /**
     * Registered Source identifier.
     */
    private final String sourceId;
    /**
     * Selected Alipay definition.
     */
    private final VendorDefinition.Definition variantDefinition;
    /**
     * Validated Alipay settings.
     */
    private final AlipaySourceSettings settings;
    /**
     * External runtime dependencies.
     */
    private final ExecutionServices services;
    /**
     * Shared browser security lifecycle.
     */
    private final RedirectManager redirectManager;
    /**
     * Strict authorization query codec.
     */
    private final QueryCodec queryCodec = new QueryCodec();
    /**
     * Strict gateway form codec.
     */
    private final FormCodec formCodec = new FormCodec();

    /**
     * Creates one Source-bound Alipay adapter.
     *
     * @param namespaceId       registration namespace
     * @param sourceId          registration Source identifier
     * @param vendorDefinition  selected Alipay definition
     * @param variantDefinition selected definition
     * @param settings          decoded settings
     * @param services          external runtime dependencies
     */
    public AlipaySourceAdapter(final String namespaceId, final String sourceId, final AlipayDefinition vendorDefinition,
            final VendorDefinition.Definition variantDefinition, final AlipaySourceSettings settings,
            final ExecutionServices services) {
        Assert.notNull(vendorDefinition, "Alipay definition must not be null");
        this.sourceId = Assert.notBlank(sourceId, "Alipay Source id must not be blank");
        this.variantDefinition = Assert.notNull(variantDefinition, "Alipay definition must not be null");
        this.settings = Assert.notNull(settings, "Alipay settings must not be null");
        this.services = Assert.notNull(services, "Alipay execution services must not be null");
        this.redirectManager = RedirectManager.create(namespaceId, sourceId, variantDefinition, settings, services);
    }

    /**
     * Builds the exact lexicographically ordered RSA2 signing input.
     *
     * @param fields non-empty request fields excluding sign
     * @return canonical UTF-8 text
     */
    private static String canonical(final Map<String, String> fields) {
        final StringBuilder builder = new StringBuilder();
        fields.forEach((name, value) -> {
            if (value != null && !value.isEmpty()) {
                if (!builder.isEmpty())
                    builder.append(Symbol.C_AND);
                builder.append(name).append(Symbol.C_EQUAL).append(value);
            }
        });
        return builder.toString();
    }

    /**
     * Reads one non-blank string member from a verified Alipay object.
     *
     * @param object verified Alipay JSON object
     * @param name   exact member name
     * @return non-blank member value, or {@code null} when absent or invalid
     */
    private static String string(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        return value instanceof JsonValue.StringValue string && !string.value().isBlank() ? string.value() : null;
    }

    /**
     * Narrows a browser-flow outcome to the capability response type after runtime type validation.
     *
     * @param stage        delegated browser-flow stage
     * @param responseType exact capability response class
     * @param <S>          capability response type
     * @return stage preserving the delegated success or failure classification
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
     * Creates a completed rejection for a capability absent from the Alipay manifest.
     *
     * @param <S> expected capability response type
     * @return completed undeclared-capability rejection
     */
    private static <S> CompletionStage<Outcome<S>> missing() {
        return completed(rejected("Alipay capability is not declared"));
    }

    /**
     * Creates a completed rejection for a request incompatible with its declared capability.
     *
     * @param <S> expected capability response type
     * @return completed request-mismatch rejection
     */
    private static <S> CompletionStage<Outcome<S>> mismatch() {
        return completed(rejected("Alipay capability request is invalid"));
    }

    /**
     * Wraps one synchronous outcome in an already completed stage.
     *
     * @param outcome outcome to expose asynchronously
     * @param <T>     outcome value type
     * @return completed outcome stage
     */
    private static <T> CompletionStage<Outcome<T>> completed(final Outcome<T> outcome) {
        return CompletableFuture.completedFuture(outcome);
    }

    /**
     * Creates a sanitized expected rejection without copying gateway diagnostics.
     *
     * @param description fixed non-sensitive description
     * @param <T>         expected result type
     * @return rejected outcome using the shared Bus error code
     */
    private static <T> Outcome<T> rejected(final String description) {
        return Outcome.rejected(new Outcome.Failure(ErrorCode._400, description, new JsonValue.ObjectValue(Map.of())));
    }

    /**
     * Creates a sanitized dependency or validation failure without sensitive details.
     *
     * @param description fixed non-sensitive description
     * @param <T>         expected result type
     * @return failed outcome using the shared Bus error code
     */
    private static <T> Outcome<T> failed(final String description) {
        return Outcome.failed(new Outcome.Failure(ErrorCode._502, description, new JsonValue.ObjectValue(Map.of())));
    }

    /**
     * Returns the immutable capability manifest frozen by the selected Alipay definition.
     *
     * @return exact Alipay Source-authentication capability manifest
     */
    @Override
    public Capability.Manifest manifest() {
        return variantDefinition.manifest();
    }

    /**
     * Routes only Alipay Source authentication initiation and completion.
     *
     * @param capability runtime-owned capability
     * @param request    exact request
     * @param context    invocation context
     * @param timeout    shared budget
     * @param <Q>        request type
     * @param <S>        success type
     * @return Alipay outcome
     */
    @Override
    public <Q, S> CompletionStage<Outcome<S>> invoke(
            final Capability<Q, S> capability,
            final Q request,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(capability, "Alipay capability must not be null");
        Assert.notNull(context, "Alipay invocation context must not be null");
        Assert.notNull(timeout, "Alipay time budget must not be null");
        if (!manifest().capabilities().contains(capability))
            return missing();
        if (capability.key().equals(SourceAuthentication.INITIATE.key())
                && request instanceof SourceAuthenticationRequest.BrowserStart start) {
            return narrow(redirectManager.initiate(start, this::prepare, context, timeout), capability.responseType());
        }
        if (capability.key().equals(SourceAuthentication.COMPLETE.key())
                && request instanceof SourceAuthenticationRequest.BrowserCallback callback) {
            return narrow(
                    redirectManager.complete(callback, this::state, this::identity, context, timeout),
                    capability.responseType());
        }
        return mismatch();
    }

    /**
     * Builds the exact Alipay public-application authorization URL.
     *
     * @param initiation generated state
     * @param context    invocation context
     * @param timeout    shared budget
     * @return prepared redirect
     */
    private CompletionStage<Outcome<RedirectManager.Prepared>> prepare(
            final RedirectManager.Initiation initiation,
            final Context context,
            final Timeout.Budget timeout) {
        final URI redirect = URI.create(settings.redirectUri().getOrNull());
        if (redirect.getHost() == null || Protocol.HOST_LOCAL.equalsIgnoreCase(redirect.getHost())) {
            return completed(rejected("Alipay redirect URI must use a registered non-local host"));
        }
        final List<Parameter> query = List.of(
                new Parameter("app_id", settings.clientId()),
                new Parameter(OAuth2.Parameters.SCOPE, "auth_user"),
                new Parameter(OAuth2.Parameters.REDIRECT_URI, settings.redirectUri().getOrNull()),
                new Parameter(OAuth2.Parameters.STATE, initiation.state()));
        final String base = variantDefinition.targets().resolve(settings).authorization().getOrNull().url().toString();
        return completed(
                Outcome.succeeded(
                        new RedirectManager.Prepared(base + Symbol.C_QUESTION_MARK + queryCodec.encode(query),
                                initiation.state())));
    }

    /**
     * Extracts the unique Alipay state after exact callback validation.
     *
     * @param callback raw callback
     * @return state value
     */
    private String state(final Callback.Inbound callback) {
        return callback(callback).state();
    }

    /**
     * Executes signed token and profile gateway methods and enforces the same user identifier.
     *
     * @param completion consumed callback material
     * @param context    invocation context
     * @param timeout    shared budget
     * @return verified Alipay identity
     */
    private CompletionStage<Outcome<ExternalIdentity>> identity(
            final RedirectManager.Completion completion,
            final Context context,
            final Timeout.Budget timeout) {
        final String code;
        try {
            code = callback(completion.callback()).authorizationCode();
        } catch (RuntimeException cause) {
            return completed(rejected("Alipay callback is invalid"));
        }
        return gateway(
                "alipay.system.oauth.token",
                Map.of(
                        OAuth2.Parameters.GRANT_TYPE,
                        GrantType.AUTHORIZATION_CODE.value(),
                        OAuth2.Parameters.CODE,
                        code),
                context,
                timeout).thenCompose(token -> switch (token) {
                    case Outcome.Succeeded<JsonValue.ObjectValue> success -> token(success.value(), context, timeout);
                    case Outcome.Rejected<JsonValue.ObjectValue> rejected -> completed(
                            Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<JsonValue.ObjectValue> failed -> completed(Outcome.failed(failed.failure()));
                });
    }

    /**
     * Extracts the private token result and invokes the signed user information method.
     *
     * @param token   verified token gateway object
     * @param context invocation context
     * @param timeout shared budget
     * @return verified Alipay identity
     */
    private CompletionStage<Outcome<ExternalIdentity>> token(
            final JsonValue.ObjectValue token,
            final Context context,
            final Timeout.Budget timeout) {
        final String accessToken = string(token, OAuth2.Parameters.ACCESS_TOKEN);
        final String tokenUser = string(token, "user_id");
        if (accessToken == null || tokenUser == null) {
            return completed(failed("Alipay token response is incomplete"));
        }
        return gateway("alipay.user.info.share", Map.of("auth_token", accessToken), context, timeout)
                .thenApply(profile -> switch (profile) {
                    case Outcome.Succeeded<JsonValue.ObjectValue> success -> profile(
                            success.value(),
                            tokenUser,
                            timeout);
                    case Outcome.Rejected<JsonValue.ObjectValue> rejected -> Outcome.rejected(rejected.failure());
                    case Outcome.Failed<JsonValue.ObjectValue> failed -> Outcome.failed(failed.failure());
                });
    }

    /**
     * Maps a signature-verified profile whose user_id matches the token response.
     *
     * @param profile   verified profile object
     * @param tokenUser token response user identifier
     * @param timeout   shared clock
     * @return verified external identity
     */
    private Outcome<ExternalIdentity> profile(
            final JsonValue.ObjectValue profile,
            final String tokenUser,
            final Timeout.Budget timeout) {
        final String profileUser = string(profile, "user_id");
        if (profileUser == null || !tokenUser.equals(profileUser)) {
            return rejected("Alipay token and profile user identifiers do not match");
        }
        final Map<String, JsonValue> attributes = new LinkedHashMap<>(profile.values());
        attributes.remove("user_id");
        final Evidence evidence = new Evidence(Evidence.Type.FEDERATED, Evidence.Strength.SINGLE_FACTOR,
                new Evidence.Claim("alipay_user_id", new JsonValue.StringValue(profileUser), AUTHORITY,
                        timeout.clock().now()));
        return Outcome.succeeded(
                new ExternalIdentity(sourceId, profileUser, new JsonValue.ObjectValue(attributes), List.of(evidence)));
    }

    /**
     * Resolves the exact signing key before invoking one Alipay gateway method.
     *
     * @param method   official gateway method
     * @param business exact business parameters
     * @param context  invocation context
     * @param timeout  shared budget
     * @return signature-verified business response object
     */
    private CompletionStage<Outcome<JsonValue.ObjectValue>> gateway(
            final String method,
            final Map<String, String> business,
            final Context context,
            final Timeout.Budget timeout) {
        final Instant now = timeout.clock().now();
        final KeyResolver.Query query = new KeyResolver.Query(AUTHORITY, Optional.of(settings.credential().id()), "sig",
                Algorithm.SHA256WITHRSA.getValue(), now);
        return services.keyResolver().resolve(query, context, timeout).thenCompose(resolved -> switch (resolved) {
            case Outcome.Succeeded<KeyResolver.ResolvedKey> success -> send(
                    method,
                    business,
                    success.value(),
                    now,
                    context,
                    timeout);
            case Outcome.Rejected<KeyResolver.ResolvedKey> rejected -> completed(Outcome.rejected(rejected.failure()));
            case Outcome.Failed<KeyResolver.ResolvedKey> failed -> completed(Outcome.failed(failed.failure()));
        });
    }

    /**
     * Signs and sends one exact Alipay gateway form.
     *
     * @param method   official method
     * @param business business parameters
     * @param key      resolved signing key
     * @param now      shared-clock instant
     * @param context  invocation context
     * @param timeout  shared budget
     * @return verified response stage
     */
    private CompletionStage<Outcome<JsonValue.ObjectValue>> send(
            final String method,
            final Map<String, String> business,
            final KeyResolver.ResolvedKey key,
            final Instant now,
            final Context context,
            final Timeout.Budget timeout) {
        if (!settings.credential().id().equals(key.keyId())
                || !Algorithm.SHA256WITHRSA.getValue().equals(key.algorithm())
                || !(key.key() instanceof PrivateKey privateKey) || now.isBefore(key.notBefore())
                || !now.isBefore(key.notAfter())) {
            return completed(rejected("Alipay signing key does not match the configured key"));
        }
        final TreeMap<String, String> fields = new TreeMap<>();
        fields.put("app_id", settings.clientId());
        fields.put("method", method);
        fields.put("format", "json");
        fields.put(MediaType.CHARSET_PARAMETER, Charset.DEFAULT_UTF_8.toLowerCase(Locale.ROOT));
        fields.put("sign_type", "RSA2");
        fields.put("timestamp", TIMESTAMP.format(now));
        fields.put("version", "1.0");
        fields.putAll(business);
        try {
            final String canonical = canonical(fields);
            final Sign signer = new Sign(Algorithm.SHA256WITHRSA, new KeyPair(null, privateKey));
            fields.put("sign", Base64.encode(signer.sign(canonical.getBytes(Charset.UTF_8))));
        } catch (RuntimeException cause) {
            return completed(failed("Alipay request signing failed"));
        }
        return CompletableFuture.supplyAsync(() -> request(fields, timeout), services.executor())
                .thenCompose(response -> switch (response) {
                    case Outcome.Succeeded<byte[]> success -> verify(method, success.value(), context, timeout);
                    case Outcome.Rejected<byte[]> rejected -> completed(Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<byte[]> failed -> completed(Outcome.failed(failed.failure()));
                });
    }

    /**
     * Sends the signed form and returns only a bounded raw JSON document.
     *
     * @param fields  complete signed form fields
     * @param timeout shared budget
     * @return raw response outcome
     */
    private Outcome<byte[]> request(final Map<String, String> fields, final Timeout.Budget timeout) {
        byte[] body = null;
        try {
            final List<Parameter> parameters = new ArrayList<>(fields.size());
            fields.forEach((name, value) -> parameters.add(new Parameter(name, value)));
            body = formCodec.encode(parameters);
            final String endpoint = variantDefinition.targets().resolve(settings).token().getOrNull().url().toString();
            try (HttpResponse response = Fabric.http(services.fabricContext()).url(endpoint).method(Http.Method.POST)
                    .timeout(timeout.forFabric())
                    .addressPolicy(services.securityBaseline().require(Protocol.VENDOR_AUTH).addressPolicy())
                    .body(body, MediaType.APPLICATION_FORM_URLENCODED_TYPE).execute()) {
                if (response.code() == Http.Status.TOO_MANY_REQUESTS
                        || response.code() >= Http.Status.INTERNAL_SERVER_ERROR)
                    return failed("Alipay gateway is unavailable");
                if (response.code() >= Http.Status.BAD_REQUEST)
                    return rejected("Alipay gateway rejected the request");
                if (response.code() != Http.Status.OK
                        || !MediaType.APPLICATION_JSON_TYPE.isCompatible(response.body().media())) {
                    return failed("Alipay gateway returned an invalid response");
                }
                return Outcome.succeeded(response.bytes(Normal.MEBI));
            }
        } catch (RuntimeException cause) {
            return failed("Alipay gateway request failed");
        } finally {
            if (body != null)
                Arrays.fill(body, (byte) 0);
        }
    }

    /**
     * Resolves the exact public key and verifies the original response member bytes.
     *
     * @param method  official gateway method
     * @param body    original raw JSON body
     * @param context invocation context
     * @param timeout shared budget
     * @return verified business response object
     */
    private CompletionStage<Outcome<JsonValue.ObjectValue>> verify(
            final String method,
            final byte[] body,
            final Context context,
            final Timeout.Budget timeout) {
        final Instant now = timeout.clock().now();
        final KeyResolver.Query query = new KeyResolver.Query(AUTHORITY, Optional.of(settings.verificationKeyId()),
                "sig", Algorithm.SHA256WITHRSA.getValue(), now);
        try {
            return services.keyResolver().resolve(query, context, timeout).handle((resolved, cause) -> {
                try {
                    if (cause != null)
                        return failed("Alipay verification key resolution failed");
                    if (resolved == null)
                        return failed("Alipay verification key resolution failed");
                    return switch (resolved) {
                        case Outcome.Succeeded<KeyResolver.ResolvedKey> success -> verified(
                                method,
                                body,
                                success.value(),
                                now);
                        case Outcome.Rejected<KeyResolver.ResolvedKey> rejected -> Outcome.rejected(rejected.failure());
                        case Outcome.Failed<KeyResolver.ResolvedKey> failed -> Outcome.failed(failed.failure());
                    };
                } finally {
                    Arrays.fill(body, (byte) 0);
                }
            });
        } catch (RuntimeException cause) {
            Arrays.fill(body, (byte) 0);
            return completed(failed("Alipay verification key resolution failed"));
        }
    }

    /**
     * Verifies one method response against the original JSON value bytes before reading business fields.
     *
     * @param method official gateway method
     * @param body   original raw JSON body
     * @param key    resolved verification key
     * @param now    shared-clock instant
     * @return verified object or closed failure
     */
    private Outcome<JsonValue.ObjectValue> verified(
            final String method,
            final byte[] body,
            final KeyResolver.ResolvedKey key,
            final Instant now) {
        if (!settings.verificationKeyId().equals(key.keyId())
                || !Algorithm.SHA256WITHRSA.getValue().equals(key.algorithm())
                || !(key.key() instanceof PublicKey publicKey) || now.isBefore(key.notBefore())
                || !now.isBefore(key.notAfter())) {
            return rejected("Alipay verification key does not match the configured key");
        }
        byte[] raw = null;
        byte[] signatureBytes = null;
        try {
            final JsonValue parsed = services.jsonProvider().readValue(body, MAXIMUM_JSON_DEPTH, true);
            if (!(parsed instanceof JsonValue.ObjectValue root))
                return failed("Alipay gateway JSON root is invalid");
            if (root.values().containsKey("error_response"))
                return rejected("Alipay gateway returned an error");
            final String member = method.replace(Symbol.C_DOT, Symbol.C_UNDERLINE) + "_response";
            final JsonValue response = root.values().get(member);
            final String signature = string(root, "sign");
            if (!(response instanceof JsonValue.ObjectValue object) || signature == null) {
                return failed("Alipay gateway response branch is incomplete");
            }
            raw = services.jsonProvider().extractValue(body, member, MAXIMUM_JSON_DEPTH, true);
            signatureBytes = Base64.decode(signature);
            final Sign verifier = new Sign(Algorithm.SHA256WITHRSA, new KeyPair(publicKey, null));
            if (!verifier.verify(raw, signatureBytes)) {
                return failed("Alipay gateway response signature is invalid");
            }
            final String code = string(object, "code");
            if (code != null && !"10000".equals(code))
                return rejected("Alipay gateway rejected the operation");
            return Outcome.succeeded(object);
        } catch (RuntimeException cause) {
            return failed("Alipay gateway response validation failed");
        } finally {
            if (raw != null)
                Arrays.fill(raw, (byte) 0);
            if (signatureBytes != null)
                Arrays.fill(signatureBytes, (byte) 0);
        }
    }

    /**
     * Validates exact GET callback parameters.
     *
     * @param callback raw callback
     * @return typed private callback values
     */
    private CallbackWire callback(final Callback.Inbound callback) {
        if (callback.method() != Http.Method.GET || !settings.redirectUri().getOrNull().equals(callback.requestUri())) {
            throw new IllegalArgumentException("Alipay callback transport is invalid");
        }
        String authorizationCode = null;
        String state = null;
        for (Callback.Parameter parameter : callback.parameters()) {
            switch (parameter.name()) {
                case "auth_code" -> {
                    if (authorizationCode != null) {
                        throw new IllegalArgumentException("Alipay callback auth_code is duplicated");
                    }
                    authorizationCode = parameter.value();
                }
                case OAuth2.Parameters.STATE -> {
                    if (state != null) {
                        throw new IllegalArgumentException("Alipay callback state is duplicated");
                    }
                    state = parameter.value();
                }
                default -> throw new IllegalArgumentException("Alipay callback member is unknown");
            }
        }
        if (authorizationCode == null || authorizationCode.isBlank() || state == null || state.isBlank()) {
            throw new IllegalArgumentException("Alipay callback requires auth_code and state");
        }
        return new CallbackWire(authorizationCode, state);
    }

    /**
     * Carries the private Alipay callback vocabulary without exposing it as a public OAuth response.
     *
     * @param authorizationCode Alipay auth_code value
     * @param state             correlated state value
     */
    private record CallbackWire(String authorizationCode, String state) {

    }

}
