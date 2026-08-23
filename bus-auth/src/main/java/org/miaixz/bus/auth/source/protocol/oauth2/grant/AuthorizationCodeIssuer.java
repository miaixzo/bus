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
package org.miaixz.bus.auth.source.protocol.oauth2.grant;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Subject;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.cache.AuthorizationCodeCache;
import org.miaixz.bus.auth.cache.ExpiringValue;
import org.miaixz.bus.auth.guard.RedirectUriValidator;
import org.miaixz.bus.auth.guard.ScopeValidator;
import org.miaixz.bus.auth.resolver.ConsumerMetadata;
import org.miaixz.bus.auth.shared.pkce.PkceMethod;
import org.miaixz.bus.auth.source.DriverServices;
import org.miaixz.bus.auth.source.protocol.oauth2.*;
import org.miaixz.bus.auth.worker.ConsentService;
import org.miaixz.bus.auth.worker.loader.ConsumerLoader;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.RandomKit;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Validates an OAuth 2.x authorization-code request and issues a one-time opaque authorization code.
 * <p>
 * This class is public only so the exported server facade can invoke it across package boundaries. Its package is not
 * exported by JPMS. Client registration, consent, and atomic state persistence remain external runtime ports; this
 * implementation owns their strict sequencing and never exposes an unvalidated redirect target.
 * </p>
 *
 * @author Kimi Liu
 */
public class AuthorizationCodeIssuer {

    /**
     * Metadata member identifying an RFC 7591 native client.
     */
    private static final String APPLICATION_TYPE = "application_type";

    /**
     * RFC 7591 native application type lexical value.
     */
    private static final String NATIVE = "native";

    /**
     * Metadata member carrying the non-sensitive registered client display name.
     */
    private static final String CLIENT_NAME = "client_name";

    /**
     * Source identifier used to isolate persisted authorization-code digests.
     */
    private final String sourceId;

    /**
     * Frozen authorization-server options governing this issuer.
     */
    private final GrantPolicy options;

    /**
     * Caller-owned runtime ports used for registration, consent, and atomic state.
     */
    private final DriverServices services;

    /**
     * Exact registered redirect URI validator shared with other OAuth operations.
     */
    private final RedirectUriValidator redirectUriValidator;

    /**
     * Standard OAuth scope parser and subset validator.
     */
    private final ScopeValidator scopeValidator;

    /**
     * Compile-time selected OpenID wire-subject binder.
     */
    private final OpenIdBinder openIdBinder;

    /**
     * Creates an authorization-code issuer for one compiled OAuth authorization server.
     *
     * @param sourceId             compiled server-role Source identifier used for state isolation
     * @param options              validated authorization-server options
     * @param services             externally implemented runtime dependencies
     * @param redirectUriValidator exact redirect URI validator
     * @param scopeValidator       standard scope validator
     * @throws IllegalArgumentException if the identifier is blank or a collaborator is {@code null}
     */
    public AuthorizationCodeIssuer(final String sourceId, final GrantPolicy options, final DriverServices services,
            final RedirectUriValidator redirectUriValidator, final ScopeValidator scopeValidator) {
        this(sourceId, options, services, redirectUriValidator, scopeValidator,
                (subject, consumer, binding, context, timeout) -> CompletableFuture
                        .completedFuture(Outcome.succeeded(binding)));
    }

    /**
     * Creates an authorization-code issuer with an explicit OpenID subject binder.
     *
     * @param sourceId             compiled server-role Source identifier used for state isolation
     * @param options              validated authorization-server options
     * @param services             externally implemented runtime dependencies
     * @param redirectUriValidator exact redirect URI validator
     * @param scopeValidator       standard scope validator
     * @param openIdBinder         compile-time selected OpenID wire-subject binder
     * @throws IllegalArgumentException if the identifier is blank or a collaborator is {@code null}
     */
    public AuthorizationCodeIssuer(final String sourceId, final GrantPolicy options, final DriverServices services,
            final RedirectUriValidator redirectUriValidator, final ScopeValidator scopeValidator,
            final OpenIdBinder openIdBinder) {
        this.sourceId = Assert.notBlank(sourceId, "OAuth 2.x Source id must not be blank");
        this.options = Assert.notNull(options, "OAuth 2.x authorization server options must not be null");
        this.services = Assert.notNull(services, "OAuth 2.x execution services must not be null");
        this.redirectUriValidator = Assert
                .notNull(redirectUriValidator, "OAuth 2.x redirect URI validator must not be null");
        this.scopeValidator = Assert.notNull(scopeValidator, "OAuth 2.x scope validator must not be null");
        this.openIdBinder = Assert.notNull(openIdBinder, "OpenID Connect subject binder must not be null");
    }

    /**
     * Determines whether a stored consent exactly belongs to the request and covers every requested value.
     *
     * @param consent optional stored consent
     * @param request current validated consent request
     * @param now     current shared-clock instant
     * @return whether the stored consent may be reused
     */
    private static boolean covering(
            final ConsentService.Snapshot consent,
            final ConsentService.Request request,
            final Instant now) {
        return consent != null && consent.activeAt(now) && consent.sourceId().equals(request.sourceId())
                && consent.providerId().equals(request.providerId()) && consent.subject().equals(request.subject())
                && consent.clientId().equals(request.clientId()) && consent.scopes().containsAll(request.scopes())
                && consent.resources().containsAll(request.resources());
    }

    /**
     * Verifies that an external consent record result represents the approved decision just submitted.
     *
     * @param consent  returned persisted snapshot
     * @param decision approved consent decision
     * @param now      record operation instant
     * @return whether the returned snapshot is coherent and active
     */
    private static boolean recorded(
            final ConsentService.Snapshot consent,
            final ConsentService.Decision decision,
            final Instant now) {
        return consent != null && consent.activeAt(now) && consent.sourceId().equals(decision.request().sourceId())
                && consent.providerId().equals(decision.request().providerId())
                && consent.subject().equals(decision.request().subject())
                && consent.clientId().equals(decision.request().clientId())
                && consent.scopes().equals(decision.grantedScopes())
                && consent.resources().equals(decision.request().resources());
    }

    /**
     * Reads the registered native-client marker without inferring it from redirect URI shape.
     *
     * @param client resolved client registration
     * @return whether metadata explicitly identifies a native application
     */
    private static boolean nativeClient(final ConsumerMetadata client) {
        final JsonValue value = client.metadata().values().get(APPLICATION_TYPE);
        return value instanceof JsonValue.StringValue text && NATIVE.equals(text.value());
    }

    /**
     * Reads the non-sensitive registered client display name with the identifier as a deterministic fallback.
     *
     * @param client resolved client registration
     * @return non-blank consent display name
     */
    private static String clientName(final ConsumerMetadata client) {
        final JsonValue value = client.metadata().values().get(CLIENT_NAME);
        if (value instanceof JsonValue.StringValue text && !text.value().isBlank()) {
            return text.value();
        }
        return client.id();
    }

    /**
     * Extracts RFC 8707 resource indicators from the implementation-neutral extension representation.
     *
     * @param request standard authorization request
     * @return immutable ordered resource indicator list
     * @throws ValidateException if the extension is not a string or string array, repeats, or contains an invalid URI
     */
    private static List<String> resources(final AuthorizationRequest request) {
        final JsonValue value = request.extensions().values().get(OAuth2.Parameters.RESOURCE);
        if (value == null) {
            return List.of();
        }
        final List<String> values = new ArrayList<>();
        if (value instanceof JsonValue.StringValue text) {
            values.add(text.value());
        } else if (value instanceof JsonValue.ArrayValue array) {
            for (JsonValue element : array.values()) {
                if (!(element instanceof JsonValue.StringValue text)) {
                    throw new ValidateException("OAuth 2.x resource extension array must contain only strings");
                }
                values.add(text.value());
            }
        } else {
            throw new ValidateException("OAuth 2.x resource extension must be a string or string array");
        }
        final Set<String> unique = new LinkedHashSet<>(values);
        if (values.isEmpty() || unique.size() != values.size()) {
            throw new ValidateException("OAuth 2.x resource extension must contain unique values");
        }
        for (String resource : values) {
            validateResource(resource);
        }
        return List.copyOf(values);
    }

    /**
     * Validates one RFC 8707 absolute resource URI without a fragment.
     *
     * @param value resource indicator lexical value
     * @throws ValidateException if the value is not an absolute fragment-free URI
     */
    private static void validateResource(final String value) {
        Assert.notBlank(value, "OAuth 2.x resource indicator must not be blank");
        try {
            final URI uri = new URI(value);
            if (!uri.isAbsolute() || uri.getRawFragment() != null) {
                throw new ValidateException("OAuth 2.x resource indicator must be an absolute URI without fragment");
            }
        } catch (URISyntaxException exception) {
            throw new ValidateException("OAuth 2.x resource indicator must be a valid URI", exception);
        }
    }

    /**
     * Generates one URL-safe opaque authorization code and clears its temporary random byte array.
     *
     * @return unpadded Base64URL authorization code
     */
    private static String authorizationCode() {
        final byte[] bytes = RandomKit.randomBytes(Normal._32, RandomKit.getSecureRandom());
        try {
            return Base64.encodeUrlSafe(bytes);
        } finally {
            Arrays.fill(bytes, (byte) 0);
        }
    }

    /**
     * Creates a consent operational failure carrying only safe OAuth routing detail.
     *
     * @param description non-sensitive operational description
     * @param redirectUri registration-validated redirect target
     * @return failed authorization outcome
     */
    private static Outcome<Result> consentFailure(final String description, final String redirectUri) {
        return Outcome.failed(failure(ErrorCode._500, OAuth2ErrorCode.SERVER_ERROR, description, redirectUri));
    }

    /**
     * Creates a safe framework failure and includes redirect routing only after exact registration validation.
     *
     * @param error       existing Bus error definition
     * @param oauthError  registered OAuth error code
     * @param description non-sensitive diagnostic description
     * @param redirectUri validated redirect target, or {@code null} before redirect validation
     * @return immutable safe failure
     */
    private static Outcome.Failure failure(
            final Errors error,
            final OAuth2ErrorCode oauthError,
            final String description,
            final String redirectUri) {
        final Map<String, JsonValue> details = new LinkedHashMap<>();
        details.put(org.miaixz.bus.auth.Builder.OAUTH_ERROR, new JsonValue.StringValue(oauthError.value()));
        if (redirectUri != null) {
            details.put(OAuth2.Parameters.REDIRECT_URI, new JsonValue.StringValue(redirectUri));
            details.put(org.miaixz.bus.auth.Builder.REDIRECT_VALIDATED, new JsonValue.BooleanValue(true));
        }
        return new Outcome.Failure(error, description, new JsonValue.ObjectValue(details));
    }

    /**
     * Creates an already-completed stage for synchronous validation outcomes.
     *
     * @param outcome completed authorization outcome
     * @return completed stage
     */
    private static CompletionStage<Outcome<Result>> completed(final Outcome<Result> outcome) {
        return CompletableFuture.completedFuture(outcome);
    }

    /**
     * Authorizes one standard request and returns its response with the validated HTTP redirect target.
     *
     * @param request internal authorization request with an optional protocol binding
     * @param context invocation context carrying the already authenticated resource owner
     * @param timeout shared end-to-end timeout
     * @return asynchronous success, expected protocol rejection, or operational failure
     */
    public CompletionStage<Outcome<Result>> authorize(
            final Request request,
            final Context context,
            final Timeout timeout) {
        Assert.notNull(request, "OAuth 2.x authorization request must not be null");
        final AuthorizationRequest authorizationRequest = request.authorizationRequest();
        Assert.notNull(context, "OAuth 2.x authorization context must not be null");
        Assert.notNull(timeout, "OAuth 2.x authorization timeout must not be null");
        if (timeout.expired()) {
            return completed(
                    Outcome.failed(
                            failure(
                                    ErrorCode._408,
                                    OAuth2ErrorCode.TEMPORARILY_UNAVAILABLE,
                                    "OAuth 2.x authorization has no remaining timeout",
                                    null)));
        }
        final Subject subject = context.authenticatedSubject().getOrNull();
        if (subject == null) {
            return completed(
                    Outcome.rejected(
                            failure(
                                    ErrorCode._401,
                                    OAuth2ErrorCode.ACCESS_DENIED,
                                    "OAuth 2.x authorization requires an authenticated subject",
                                    null)));
        }

        final CompletionStage<Outcome<ConsumerMetadata>> resolution;
        try {
            resolution = Outcome.mapStage(
                    () -> services.consumerLoader().load(
                            new ConsumerLoader.Request(services.entry(), authorizationRequest.clientId()),
                            context,
                            timeout),
                    loaded -> services.consumerParser()
                            .parse(services.entry(), authorizationRequest.clientId(), loaded));
        } catch (RuntimeException exception) {
            return completed(
                    Outcome.failed(
                            failure(
                                    ErrorCode._500,
                                    OAuth2ErrorCode.SERVER_ERROR,
                                    "OAuth 2.x client resolution failed",
                                    null)));
        }
        return resolution.handle(
                (outcome, thrown) -> thrown == null && outcome != null ? outcome
                        : Outcome.<ConsumerMetadata>failed(
                                failure(
                                        ErrorCode._500,
                                        OAuth2ErrorCode.SERVER_ERROR,
                                        "OAuth 2.x client resolution failed",
                                        null)))
                .thenCompose(outcome -> switch (outcome) {
                    case Outcome.Succeeded<ConsumerMetadata> success -> success.value() == null
                            ? completed(
                                    Outcome.failed(
                                            failure(
                                                    ErrorCode._500,
                                                    OAuth2ErrorCode.SERVER_ERROR,
                                                    "OAuth 2.x client resolution returned no registration",
                                                    null)))
                            : authorizeRegistered(
                                    authorizationRequest,
                                    request.openIdBinding(),
                                    subject,
                                    success.value(),
                                    context,
                                    timeout);
                    case Outcome.Rejected<ConsumerMetadata> rejected -> completed(
                            Outcome.rejected(
                                    failure(
                                            rejected.failure().error(),
                                            OAuth2ErrorCode.UNAUTHORIZED_CLIENT,
                                            "OAuth 2.x client is not registered for authorization",
                                            null)));
                    case Outcome.Failed<ConsumerMetadata> failed -> completed(
                            Outcome.failed(
                                    failure(
                                            failed.failure().error(),
                                            OAuth2ErrorCode.SERVER_ERROR,
                                            "OAuth 2.x client resolution failed",
                                            null)));
                    default -> throw new IllegalStateException("Unsupported Outcome implementation");
                });
    }

    /**
     * Validates a resolved registration before entering the consent and code persistence flow.
     *
     * @param request       standard authorization request
     * @param openIdBinding optional OpenID Connect authorization context
     * @param subject       authenticated resource owner
     * @param client        resolved immutable client registration
     * @param context       immutable invocation context
     * @param timeout       shared operation timeout
     * @return asynchronous authorization outcome
     */
    private CompletionStage<Outcome<Result>> authorizeRegistered(
            final AuthorizationRequest request,
            final Optional<AuthorizationCodeCache.OpenIdBinding> openIdBinding,
            final Subject subject,
            final ConsumerMetadata client,
            final Context context,
            final Timeout timeout) {
        if (!request.clientId().equals(client.id())) {
            return completed(
                    Outcome.rejected(
                            failure(
                                    ErrorCode._400,
                                    OAuth2ErrorCode.UNAUTHORIZED_CLIENT,
                                    "OAuth 2.x resolved client identifier does not match the request",
                                    null)));
        }
        if (!client.grantTypes().contains(GrantType.AUTHORIZATION_CODE.value())
                || !client.responseTypes().contains(ResponseType.CODE.value())) {
            return completed(
                    Outcome.rejected(
                            failure(
                                    ErrorCode._400,
                                    OAuth2ErrorCode.UNAUTHORIZED_CLIENT,
                                    "OAuth 2.x client is not registered for the authorization code flow",
                                    null)));
        }

        final String redirectUri;
        try {
            redirectUri = selectRedirectUri(request, client);
        } catch (RuntimeException exception) {
            return completed(
                    Outcome.rejected(
                            failure(
                                    ErrorCode._400,
                                    OAuth2ErrorCode.INVALID_REQUEST,
                                    "OAuth 2.x redirect URI is not valid for the registered client",
                                    null)));
        }
        final Validation validation = validateAuthorizedRequest(request, client);
        if (validation == Validation.UNSUPPORTED_RESPONSE_TYPE) {
            return completed(
                    Outcome.rejected(
                            failure(
                                    ErrorCode._400,
                                    OAuth2ErrorCode.UNSUPPORTED_RESPONSE_TYPE,
                                    "OAuth 2.x response type is not supported",
                                    redirectUri)));
        }
        if (validation == Validation.INVALID_SCOPE) {
            return completed(
                    Outcome.rejected(
                            failure(
                                    ErrorCode._400,
                                    OAuth2ErrorCode.INVALID_SCOPE,
                                    "OAuth 2.x requested scope is not allowed",
                                    redirectUri)));
        }
        if (validation == Validation.INVALID_REQUEST) {
            return completed(
                    Outcome.rejected(
                            failure(
                                    ErrorCode._400,
                                    OAuth2ErrorCode.INVALID_REQUEST,
                                    "OAuth 2.x authorization request is invalid",
                                    redirectUri)));
        }

        final List<String> requestedScopes = request.scope().isEmpty() ? List.of()
                : request.scope().getOrNull().values();
        final List<String> requestedResources;
        try {
            requestedResources = resources(request);
        } catch (RuntimeException exception) {
            return completed(
                    Outcome.rejected(
                            failure(
                                    ErrorCode._400,
                                    OAuth2ErrorCode.INVALID_REQUEST,
                                    "OAuth 2.x authorization resource indicator is invalid",
                                    redirectUri)));
        }
        if (requestedScopes.isEmpty()) {
            return issue(
                    request,
                    openIdBinding,
                    subject,
                    client,
                    redirectUri,
                    requestedScopes,
                    requestedResources,
                    context,
                    timeout,
                    1);
        }
        final ConsentService.Request consentRequest;
        try {
            consentRequest = new ConsentService.Request(sourceId, services.entry().resource().getProvider_id(),
                    subject.reference(), client.id(), clientName(client), redirectUri,
                    new LinkedHashSet<>(requestedScopes), requestedResources);
        } catch (RuntimeException exception) {
            return completed(
                    Outcome.rejected(
                            failure(
                                    ErrorCode._400,
                                    OAuth2ErrorCode.INVALID_REQUEST,
                                    "OAuth 2.x authorization resource indicator is invalid",
                                    redirectUri)));
        }
        return consent(request, openIdBinding, subject, client, redirectUri, consentRequest, context, timeout);
    }

    /**
     * Selects and validates the exact redirect target for a resolved client registration.
     *
     * @param request standard authorization request
     * @param client  resolved client registration
     * @return exact registration-validated redirect URI
     * @throws ValidateException if no unambiguous registered target exists
     */
    private String selectRedirectUri(final AuthorizationRequest request, final ConsumerMetadata client) {
        final String requested = request.redirectUri().getOrNull();
        if (requested == null && client.redirectUris().size() != 1) {
            throw new ValidateException(
                    "OAuth 2.x redirect URI may be omitted only when the client has one registered redirect URI");
        }
        final String selected = requested == null ? client.redirectUris().get(0) : requested;
        redirectUriValidator.validate(selected, client.redirectUris(), nativeClient(client));
        return selected;
    }

    /**
     * Validates the response type, scope bounds, and mandatory S256 PKCE binding after redirect safety is established.
     *
     * @param request standard authorization request
     * @param client  resolved client registration
     * @return closed validation result used to select the registered OAuth error code
     */
    private Validation validateAuthorizedRequest(final AuthorizationRequest request, final ConsumerMetadata client) {
        if (!ResponseType.CODE.equals(request.responseType())) {
            return Validation.UNSUPPORTED_RESPONSE_TYPE;
        }
        if (!request.scope().isEmpty()) {
            final List<String> requested = request.scope().getOrNull().values();
            try {
                scopeValidator.validateRequested(requested, client.scopes());
                scopeValidator.validateRequested(requested, options.scopesSupported());
            } catch (RuntimeException exception) {
                return Validation.INVALID_SCOPE;
            }
        }
        if (options.pkceRequired() && (request.codeChallenge().isEmpty()
                || !PkceMethod.S256.value().equals(request.codeChallengeMethod().getOrNull()))) {
            return Validation.INVALID_REQUEST;
        }
        return Validation.VALID;
    }

    /**
     * Reuses an active covering consent or obtains and records a new external consent decision.
     *
     * @param request        standard authorization request
     * @param openIdBinding  optional OpenID Connect authorization context retained with the authorization code
     * @param subject        authenticated resource owner
     * @param client         resolved client registration
     * @param redirectUri    registration-validated redirect target
     * @param consentRequest validated consent display context
     * @param context        immutable invocation context
     * @param timeout        shared operation timeout
     * @return asynchronous authorization outcome
     */
    private CompletionStage<Outcome<Result>> consent(
            final AuthorizationRequest request,
            final Optional<AuthorizationCodeCache.OpenIdBinding> openIdBinding,
            final Subject subject,
            final ConsumerMetadata client,
            final String redirectUri,
            final ConsentService.Request consentRequest,
            final Context context,
            final Timeout timeout) {
        final CompletionStage<Outcome<Optional<ConsentService.Snapshot>>> lookup;
        try {
            lookup = services.consentService().find(consentRequest, context, timeout);
        } catch (RuntimeException exception) {
            return completed(consentFailure("OAuth 2.x consent lookup failed", redirectUri));
        }
        return lookup.handle(
                (outcome, thrown) -> thrown == null && outcome != null ? outcome
                        : Outcome.<Optional<ConsentService.Snapshot>>failed(
                                failure(
                                        ErrorCode._500,
                                        OAuth2ErrorCode.SERVER_ERROR,
                                        "OAuth 2.x consent lookup failed",
                                        redirectUri)))
                .thenCompose(outcome -> switch (outcome) {
                    case Outcome.Succeeded<Optional<ConsentService.Snapshot>> success -> {
                        if (success.value() == null) {
                            yield completed(
                                    consentFailure(
                                            "OAuth 2.x consent lookup returned no optional container",
                                            redirectUri));
                        }
                        final ConsentService.Snapshot existing = success.value().getOrNull();
                        if (covering(existing, consentRequest, timeout.clock().now())) {
                            yield issue(
                                    request,
                                    openIdBinding,
                                    subject,
                                    client,
                                    redirectUri,
                                    List.copyOf(consentRequest.scopes()),
                                    consentRequest.resources(),
                                    context,
                                    timeout,
                                    1);
                        }
                        yield decide(
                                request,
                                openIdBinding,
                                subject,
                                client,
                                redirectUri,
                                consentRequest,
                                context,
                                timeout);
                    }
                    case Outcome.Rejected<Optional<ConsentService.Snapshot>> rejected -> completed(
                            Outcome.rejected(
                                    failure(
                                            rejected.failure().error(),
                                            OAuth2ErrorCode.ACCESS_DENIED,
                                            "OAuth 2.x consent lookup was rejected",
                                            redirectUri)));
                    case Outcome.Failed<Optional<ConsentService.Snapshot>> failed -> completed(
                            Outcome.failed(
                                    failure(
                                            failed.failure().error(),
                                            OAuth2ErrorCode.SERVER_ERROR,
                                            "OAuth 2.x consent lookup failed",
                                            redirectUri)));
                    default -> throw new IllegalStateException("Unsupported Outcome implementation");
                });
    }

    /**
     * Obtains an explicit consent decision and persists every approval before issuing a code.
     *
     * @param request        standard authorization request
     * @param openIdBinding  optional OpenID Connect authorization context
     * @param subject        authenticated resource owner
     * @param client         resolved client registration
     * @param redirectUri    registration-validated redirect target
     * @param consentRequest validated consent display context
     * @param context        immutable invocation context
     * @param timeout        shared operation timeout
     * @return asynchronous authorization outcome
     */
    private CompletionStage<Outcome<Result>> decide(
            final AuthorizationRequest request,
            final Optional<AuthorizationCodeCache.OpenIdBinding> openIdBinding,
            final Subject subject,
            final ConsumerMetadata client,
            final String redirectUri,
            final ConsentService.Request consentRequest,
            final Context context,
            final Timeout timeout) {
        final CompletionStage<Outcome<ConsentService.Decision>> decision;
        try {
            decision = services.consentService().decide(consentRequest, context, timeout);
        } catch (RuntimeException exception) {
            return completed(consentFailure("OAuth 2.x consent decision failed", redirectUri));
        }
        return decision.handle(
                (outcome, thrown) -> thrown == null && outcome != null ? outcome
                        : Outcome.<ConsentService.Decision>failed(
                                failure(
                                        ErrorCode._500,
                                        OAuth2ErrorCode.SERVER_ERROR,
                                        "OAuth 2.x consent decision failed",
                                        redirectUri)))
                .thenCompose(outcome -> switch (outcome) {
                    case Outcome.Succeeded<ConsentService.Decision> success -> {
                        if (success.value() == null || !consentRequest.equals(success.value().request())) {
                            yield completed(
                                    consentFailure(
                                            "OAuth 2.x consent decision returned an inconsistent request",
                                            redirectUri));
                        }
                        if (success.value().status() == ConsentService.Status.DENIED) {
                            yield completed(
                                    Outcome.rejected(
                                            failure(
                                                    ErrorCode._403,
                                                    OAuth2ErrorCode.ACCESS_DENIED,
                                                    "OAuth 2.x authorization was denied by the resource owner",
                                                    redirectUri)));
                        }
                        yield record(
                                request,
                                openIdBinding,
                                subject,
                                client,
                                redirectUri,
                                success.value(),
                                context,
                                timeout);
                    }
                    case Outcome.Rejected<ConsentService.Decision> rejected -> completed(
                            Outcome.rejected(
                                    failure(
                                            rejected.failure().error(),
                                            OAuth2ErrorCode.ACCESS_DENIED,
                                            "OAuth 2.x consent decision was rejected",
                                            redirectUri)));
                    case Outcome.Failed<ConsentService.Decision> failed -> completed(
                            Outcome.failed(
                                    failure(
                                            failed.failure().error(),
                                            OAuth2ErrorCode.SERVER_ERROR,
                                            "OAuth 2.x consent decision failed",
                                            redirectUri)));
                    default -> throw new IllegalStateException("Unsupported Outcome implementation");
                });
    }

    /**
     * Persists one approved consent and issues only after the external store confirms the coherent snapshot.
     *
     * @param request       standard authorization request
     * @param openIdBinding optional OpenID Connect authorization context
     * @param subject       authenticated resource owner
     * @param client        resolved client registration
     * @param redirectUri   registration-validated redirect target
     * @param decision      approved consent decision
     * @param context       immutable invocation context
     * @param timeout       shared operation timeout
     * @return asynchronous authorization outcome
     */
    private CompletionStage<Outcome<Result>> record(
            final AuthorizationRequest request,
            final Optional<AuthorizationCodeCache.OpenIdBinding> openIdBinding,
            final Subject subject,
            final ConsumerMetadata client,
            final String redirectUri,
            final ConsentService.Decision decision,
            final Context context,
            final Timeout timeout) {
        final Instant now = timeout.clock().now();
        final CompletionStage<Outcome<ConsentService.Snapshot>> recording;
        try {
            recording = services.consentService()
                    .record(new ConsentService.Save(decision, now, Optional.empty()), context, timeout);
        } catch (RuntimeException exception) {
            return completed(consentFailure("OAuth 2.x consent recording failed", redirectUri));
        }
        return recording.handle(
                (outcome, thrown) -> thrown == null && outcome != null ? outcome
                        : Outcome.<ConsentService.Snapshot>failed(
                                failure(
                                        ErrorCode._500,
                                        OAuth2ErrorCode.SERVER_ERROR,
                                        "OAuth 2.x consent recording failed",
                                        redirectUri)))
                .thenCompose(outcome -> switch (outcome) {
                    case Outcome.Succeeded<ConsentService.Snapshot> success -> {
                        if (!recorded(success.value(), decision, now)) {
                            yield completed(
                                    consentFailure(
                                            "OAuth 2.x consent recording returned an inconsistent snapshot",
                                            redirectUri));
                        }
                        yield issue(
                                request,
                                openIdBinding,
                                subject,
                                client,
                                redirectUri,
                                List.copyOf(decision.grantedScopes()),
                                decision.request().resources(),
                                context,
                                timeout,
                                1);
                    }
                    case Outcome.Rejected<ConsentService.Snapshot> rejected -> completed(
                            Outcome.rejected(
                                    failure(
                                            rejected.failure().error(),
                                            OAuth2ErrorCode.ACCESS_DENIED,
                                            "OAuth 2.x consent recording was rejected",
                                            redirectUri)));
                    case Outcome.Failed<ConsentService.Snapshot> failed -> completed(
                            Outcome.failed(
                                    failure(
                                            failed.failure().error(),
                                            OAuth2ErrorCode.SERVER_ERROR,
                                            "OAuth 2.x consent recording failed",
                                            redirectUri)));
                    default -> throw new IllegalStateException("Unsupported Outcome implementation");
                });
    }

    /**
     * Generates and atomically stores one opaque authorization code, retrying only digest collisions.
     *
     * @param request       standard authorization request
     * @param openIdBinding optional OpenID Connect authorization context
     * @param subject       authenticated resource owner
     * @param client        resolved client registration
     * @param redirectUri   registration-validated redirect target
     * @param grantedScopes exact approved scope tokens
     * @param resources     exact validated resource indicators bound to the grant
     * @param context       invocation context retained for OpenID subject binding
     * @param timeout       shared operation timeout
     * @param attempt       one-based create attempt number
     * @return asynchronous authorization outcome
     */
    private CompletionStage<Outcome<Result>> issue(
            final AuthorizationRequest request,
            final Optional<AuthorizationCodeCache.OpenIdBinding> openIdBinding,
            final Subject subject,
            final ConsumerMetadata client,
            final String redirectUri,
            final List<String> grantedScopes,
            final List<String> resources,
            final Context context,
            final Timeout timeout,
            final int attempt) {
        if (timeout.expired()) {
            return completed(
                    Outcome.failed(
                            failure(
                                    ErrorCode._408,
                                    OAuth2ErrorCode.TEMPORARILY_UNAVAILABLE,
                                    "OAuth 2.x authorization has no remaining timeout",
                                    redirectUri)));
        }
        final AuthorizationCodeCache.OpenIdBinding binding = openIdBinding.getOrNull();
        if (binding != null && binding.subject().isEmpty()) {
            final CompletionStage<Outcome<AuthorizationCodeCache.OpenIdBinding>> stage;
            try {
                stage = openIdBinder.bind(subject.key(), client, binding, context, timeout);
            } catch (RuntimeException cause) {
                return completed(
                        Outcome.failed(
                                failure(
                                        ErrorCode._500,
                                        OAuth2ErrorCode.SERVER_ERROR,
                                        "OpenID Connect subject binding failed",
                                        redirectUri)));
            }
            if (stage == null) {
                return completed(
                        Outcome.failed(
                                failure(
                                        ErrorCode._500,
                                        OAuth2ErrorCode.SERVER_ERROR,
                                        "OpenID Connect subject binder returned no stage",
                                        redirectUri)));
            }
            return stage.handle((outcome, cause) -> cause == null ? outcome : null).thenCompose(outcome -> {
                if (outcome instanceof Outcome.Succeeded<AuthorizationCodeCache.OpenIdBinding> success
                        && success.value() != null && success.value().subject().isPresent()) {
                    return issue(
                            request,
                            Optional.of(success.value()),
                            subject,
                            client,
                            redirectUri,
                            grantedScopes,
                            resources,
                            context,
                            timeout,
                            attempt);
                }
                return completed(
                        Outcome.failed(
                                failure(
                                        ErrorCode._500,
                                        OAuth2ErrorCode.SERVER_ERROR,
                                        "OpenID Connect subject binding failed",
                                        redirectUri)));
            });
        }
        final String code = authorizationCode();
        final Instant expiresAt = timeout.clock().now().plus(options.authorizationCodeLifetime());
        final AuthorizationCodeCache.Entry entry = new AuthorizationCodeCache.Entry(sourceId, client.id(),
                subject.key().value(), redirectUri, request.redirectUri().isPresent(), grantedScopes, resources,
                request.codeChallenge(), request.codeChallengeMethod(), openIdBinding);
        final ExpiringValue<AuthorizationCodeCache.Entry> stored = new ExpiringValue<>(entry, expiresAt);
        final CompletionStage<Boolean> creation;
        try {
            creation = services.authorizationCodeCache().issue(codeKey(code), stored);
        } catch (RuntimeException exception) {
            return completed(
                    Outcome.failed(
                            failure(
                                    ErrorCode._500,
                                    OAuth2ErrorCode.SERVER_ERROR,
                                    "OAuth 2.x authorization code persistence failed",
                                    redirectUri)));
        }
        return creation.handle((created, thrown) -> new CreateResult(Boolean.TRUE.equals(created), thrown))
                .thenCompose(result -> {
                    if (result.failure() != null) {
                        return completed(
                                Outcome.failed(
                                        failure(
                                                ErrorCode._500,
                                                OAuth2ErrorCode.SERVER_ERROR,
                                                "OAuth 2.x authorization code persistence failed",
                                                redirectUri)));
                    }
                    if (!result.created()) {
                        if (attempt < org.miaixz.bus.auth.Builder.MAXIMUM_RETRY_ATTEMPTS) {
                            return issue(
                                    request,
                                    openIdBinding,
                                    subject,
                                    client,
                                    redirectUri,
                                    grantedScopes,
                                    resources,
                                    context,
                                    timeout,
                                    attempt + 1);
                        }
                        return completed(
                                Outcome.failed(
                                        failure(
                                                ErrorCode._500,
                                                OAuth2ErrorCode.SERVER_ERROR,
                                                "OAuth 2.x authorization code allocation failed",
                                                redirectUri)));
                    }
                    final List<String> requested = request.scope().isEmpty() ? List.of()
                            : request.scope().getOrNull().values();
                    final Optional<Scope> responseScope = requested.equals(grantedScopes) ? Optional.empty()
                            : Optional.of(new Scope(grantedScopes));
                    final Map<String, JsonValue> extensions = new LinkedHashMap<>();
                    responseScope.ifPresent(
                            scope -> extensions
                                    .put(OAuth2.Parameters.SCOPE, new JsonValue.StringValue(scope.format())));
                    extensions.put(OAuth2.Parameters.ISS, new JsonValue.StringValue(options.issuer()));
                    final AuthorizationResponse response = new AuthorizationCodeResponse(code, request.state(),
                            new JsonValue.ObjectValue(extensions));
                    return completed(Outcome.succeeded(new Result(response, redirectUri)));
                });
    }

    /**
     * Produces a Source-isolated irreversible key for the opaque authorization code.
     *
     * @param code freshly generated opaque authorization code
     * @return hexadecimal SHA-256 lookup key
     */
    private String codeKey(final String code) {
        return Builder.sha256Hex(sourceId + Symbol.C_NUL + code);
    }

    /**
     * Enumerates the closed synchronous validation result without introducing protocol-specific exceptions.
     *
     * @author Kimi Liu
     */
    private enum Validation {
        /**
         * Every response type, scope, and PKCE condition is valid.
         */
        VALID,
        /**
         * The requested response type is not the implemented authorization-code response.
         */
        UNSUPPORTED_RESPONSE_TYPE,
        /**
         * At least one requested scope lies outside the client or server-role Source options.
         */
        INVALID_SCOPE,
        /**
         * A remaining authorization request security condition is invalid.
         */
        INVALID_REQUEST

    }

    /**
     * Completes an OpenID authorization binding with its final wire subject.
     *
     * @author Kimi Liu
     */
    @FunctionalInterface
    public interface OpenIdBinder {

        /**
         * Binds one internal subject and Consumer to an immutable OpenID authorization context.
         *
         * @param subject  authenticated project Subject key
         * @param consumer resolved immutable Consumer metadata
         * @param binding  OpenID authentication context awaiting its final wire subject
         * @param context  immutable invocation context
         * @param timeout  shared end-to-end operation timeout
         * @return asynchronous bound OpenID authorization context outcome
         */
        CompletionStage<Outcome<AuthorizationCodeCache.OpenIdBinding>> bind(
                Subject.Key subject,
                ConsumerMetadata consumer,
                AuthorizationCodeCache.OpenIdBinding binding,
                Context context,
                Timeout timeout);

    }

    /**
     * Carries one standard OAuth authorization request and its optional atomically persisted higher-level binding.
     * <p>
     * This value exists only inside the unexported server implementation. Pure OAuth callers must use
     * {@link #oauth(AuthorizationRequest)}; OpenID Connect orchestration supplies its typed binding through
     * {@link #openId(AuthorizationRequest, AuthorizationCodeCache.OpenIdBinding)}.
     * </p>
     *
     * @param authorizationRequest standard OAuth authorization request
     * @param openIdBinding        optional OpenID Connect context to bind to the issued code
     * @author Kimi Liu
     */
    public record Request(AuthorizationRequest authorizationRequest,
            Optional<AuthorizationCodeCache.OpenIdBinding> openIdBinding) {

        /**
         * Validates and normalizes one internal authorization issue request.
         *
         * @param authorizationRequest standard OAuth authorization request
         * @param openIdBinding        optional typed OpenID Connect binding
         * @throws IllegalArgumentException if a component or optional container is {@code null}
         */
        public Request {
            Assert.notNull(authorizationRequest, "OAuth 2.x authorization request must not be null");
            Assert.notNull(openIdBinding, "OpenID Connect authorization binding container must not be null");
            openIdBinding = Optional.ofNullable(openIdBinding.getOrNull());
        }

        /**
         * Creates a pure OAuth authorization issue request with no OpenID Connect state.
         *
         * @param request standard OAuth authorization request
         * @return internal request with an empty OpenID Connect binding
         */
        public static Request oauth(final AuthorizationRequest request) {
            return new Request(request, Optional.empty());
        }

        /**
         * Creates an OpenID Connect authorization issue request whose binding is persisted with the code atomically.
         *
         * @param request nested standard OAuth authorization request
         * @param binding validated OpenID Connect authentication context
         * @return internal request carrying the required OpenID Connect binding
         */
        public static Request openId(
                final AuthorizationRequest request,
                final AuthorizationCodeCache.OpenIdBinding binding) {
            return new Request(request,
                    Optional.of(Assert.notNull(binding, "OpenID Connect authorization binding must not be null")));
        }

    }

    /**
     * Couples the standard successful response with its registration-validated redirect target for the HTTP adapter.
     *
     * @param response    standard successful authorization response
     * @param redirectUri exact validated redirect URI
     * @author Kimi Liu
     */
    public record Result(AuthorizationResponse response, String redirectUri) {

        /**
         * Validates the internal routing result without repeating client registration policy.
         *
         * @throws IllegalArgumentException if a component is {@code null} or blank
         * @throws ValidateException        if the redirect target is not an absolute fragment-free URI
         */
        public Result {
            Assert.notNull(response, "OAuth 2.x authorization response must not be null");
            Assert.notBlank(redirectUri, "OAuth 2.x authorization redirect URI must not be blank");
            try {
                final URI uri = new URI(redirectUri);
                if (!uri.isAbsolute() || uri.getRawFragment() != null) {
                    throw new ValidateException(
                            "OAuth 2.x authorization redirect URI must be absolute and fragment-free");
                }
            } catch (URISyntaxException exception) {
                throw new ValidateException("OAuth 2.x authorization redirect URI must be valid", exception);
            }
        }

    }

    /**
     * Carries an atomic-create result without allowing backend exceptions to escape the Outcome boundary.
     *
     * @param created whether the authorization-code value was created
     * @param failure backend completion failure, or {@code null}
     * @author Kimi Liu
     */
    private record CreateResult(boolean created, Throwable failure) {

    }

}
