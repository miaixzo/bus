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
package org.miaixz.bus.auth.source.protocol.saml.server;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Session;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.resolver.ConsumerMetadata;
import org.miaixz.bus.auth.source.DriverServices;
import org.miaixz.bus.auth.source.protocol.saml.AuthnRequest;
import org.miaixz.bus.auth.source.protocol.saml.Response;
import org.miaixz.bus.auth.source.protocol.saml.Saml;
import org.miaixz.bus.auth.source.protocol.saml.SamlBinding;
import org.miaixz.bus.auth.worker.SessionCoordinator;
import org.miaixz.bus.auth.worker.loader.ConsumerLoader;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Processes the SAML 2.0 Web Browser SSO profile at an identity provider's SingleSignOnService.
 * <p>
 * The external HTTP-Redirect adapter performs secure XML parsing and signature validation before invoking this typed
 * service. This service establishes the registered service-provider and assertion-consumer boundary, validates the
 * standard request profile, and delegates standard assertion construction to {@link AssertionIssuer}.
 * </p>
 *
 * @author Kimi Liu
 */
public class SingleSignOnService {

    /**
     * Validated identity-provider options.
     */
    private final SamlServerOptions options;

    /**
     * External loaders and framework-owned parsers used by this service.
     */
    private final DriverServices services;

    /**
     * Internal standard assertion and response issuer.
     */
    private final AssertionIssuer assertionIssuer;

    /**
     * Standard SAML protocol error-response mapper.
     */
    private final SamlErrorMapper errorMapper;
    /**
     * Framework coordinator for Source-isolated authentication Session transitions.
     */
    private final SessionCoordinator sessions;

    /**
     * Creates a SingleSignOnService from its policy, execution services, issuer, and error mapper.
     *
     * @param options         validated SAML identity-provider options
     * @param services        external loaders and pure parsers
     * @param assertionIssuer internal SAML assertion issuer
     * @param errorMapper     standard SAML error-response mapper
     * @param sessions        Source-isolated Session lifecycle coordinator
     * @throws IllegalArgumentException if a collaborator is {@code null}
     */
    public SingleSignOnService(final SamlServerOptions options, final DriverServices services,
            final AssertionIssuer assertionIssuer, final SamlErrorMapper errorMapper,
            final SessionCoordinator sessions) {
        this.options = Assert.notNull(options, "SAML identity-provider options must not be null");
        this.services = Assert.notNull(services, "SAML execution services must not be null");
        this.assertionIssuer = Assert.notNull(assertionIssuer, "SAML Assertion issuer must not be null");
        this.errorMapper = Assert.notNull(errorMapper, "SAML error mapper must not be null");
        this.sessions = Assert.notNull(sessions, "SAML Session coordinator must not be null");
    }

    /**
     * Creates a non-sensitive framework failure.
     *
     * @param code        shared Bus error code
     * @param description safe diagnostic description
     * @return immutable closed failure
     */
    private static Outcome.Failure failure(final Errors code, final String description) {
        return new Outcome.Failure(code, description, new JsonValue.ObjectValue(Map.of()));
    }

    /**
     * Creates a type-inferred completed stage.
     *
     * @param outcome completed outcome
     * @param <T>     success value type
     * @return completed asynchronous stage
     */
    private static <T> CompletionStage<Outcome<T>> completed(final Outcome<T> outcome) {
        return CompletableFuture.completedFuture(outcome);
    }

    /**
     * Validates one Authentication Request and issues a standard SAML Response.
     *
     * @param request standard SAML Authentication Request validated at the Redirect Binding boundary
     * @param context immutable invocation context containing the authenticated subject and session
     * @param timeout shared end-to-end timeout
     * @return stage containing a success/error SAML Response or a closed framework failure when no safe response route
     *         exists
     */
    public CompletionStage<Outcome<Response>> singleSignOn(
            final AuthnRequest request,
            final Context context,
            final Timeout timeout) {
        Assert.notNull(request, "SAML Authentication Request must not be null");
        Assert.notNull(context, "SAML Single Sign-On context must not be null");
        Assert.notNull(timeout, "SAML Single Sign-On timeout must not be null");
        final Outcome<String> requester = requester(request, timeout);
        return switch (requester) {
            case Outcome.Succeeded<String> success -> Outcome.mapStage(
                    () -> services.consumerLoader()
                            .load(new ConsumerLoader.Request(services.entry(), success.value()), context, timeout),
                    loaded -> services.consumerParser().parse(services.entry(), success.value(), loaded))
                    .thenCompose(resolved -> switch (resolved) {
                        case Outcome.Succeeded<ConsumerMetadata> client -> issue(
                                request,
                                client.value(),
                                context,
                                timeout);
                        case Outcome.Rejected<ConsumerMetadata> rejected -> completed(
                                Outcome.rejected(rejected.failure()));
                        case Outcome.Failed<ConsumerMetadata> failed -> completed(Outcome.failed(failed.failure()));
                        default -> throw new IllegalStateException("Unsupported Outcome implementation");
                    });
            case Outcome.Rejected<String> rejected -> completed(Outcome.rejected(rejected.failure()));
            case Outcome.Failed<String> failed -> completed(Outcome.failed(failed.failure()));
            default -> throw new IllegalStateException("Unsupported Outcome implementation");
        };
    }

    /**
     * Validates request fields that must be trusted before resolving the registered service provider.
     *
     * @param request standard Authentication Request
     * @param timeout operation timeout
     * @return registered requester entityID candidate or a failure that must not be returned to an untrusted ACS
     */
    private Outcome<String> requester(final AuthnRequest request, final Timeout timeout) {
        if (timeout.expired()) {
            return Outcome.failed(failure(ErrorCode._408, "SAML Authentication Request has no remaining timeout"));
        }
        if (!options.singleSignOnServiceEndpoint().url().toString().equals(request.destination().getOrNull())) {
            return Outcome.rejected(failure(ErrorCode._400, "SAML Authentication Request Destination is invalid"));
        }
        final var issuer = request.issuer().getOrNull();
        if (issuer == null) {
            return Outcome.rejected(failure(ErrorCode._400, "SAML Authentication Request Issuer is required"));
        }
        final var issuerName = issuer.nameId();
        final String format = issuerName.format().getOrNull();
        if ((format != null && !Saml.NameIdFormats.ENTITY.equals(format)) || issuerName.nameQualifier().isPresent()
                || issuerName.spNameQualifier().isPresent() || issuerName.spProvidedId().isPresent()) {
            return Outcome.rejected(
                    failure(ErrorCode._400, "SAML Authentication Request Issuer is not an entity identifier"));
        }
        final Instant now = timeout.clock().now();
        if (request.issueInstant().isBefore(now.minus(options.clockSkew()))
                || request.issueInstant().isAfter(now.plus(options.clockSkew()))) {
            return Outcome.rejected(
                    failure(
                            ErrorCode._400,
                            "SAML Authentication Request IssueInstant is outside the accepted interval"));
        }
        if (!SamlBinding.HTTP_POST.equals(request.protocolBinding().getOrNull())
                || request.assertionConsumerServiceIndex().isPresent()
                || request.assertionConsumerServiceUrl().isEmpty()) {
            return Outcome.rejected(
                    failure(ErrorCode._400, "SAML Authentication Request must select an explicit HTTP-POST ACS URL"));
        }
        return Outcome.succeeded(issuerName.value());
    }

    /**
     * Establishes the safe ACS route and delegates response issuance.
     *
     * @param request validated standard Authentication Request
     * @param client  resolved registered service provider
     * @param context invocation context
     * @param timeout shared operation timeout
     * @return issued standard response stage
     */
    private CompletionStage<Outcome<Response>> issue(
            final AuthnRequest request,
            final ConsumerMetadata client,
            final Context context,
            final Timeout timeout) {
        final String issuer = request.issuer().getOrNull().nameId().value();
        final String destination = request.assertionConsumerServiceUrl().getOrNull();
        if (!issuer.equals(client.id()) || !client.redirectUris().contains(destination)) {
            return completed(
                    Outcome.rejected(
                            failure(
                                    ErrorCode._400,
                                    "SAML Assertion Consumer Service is not registered for the requester")));
        }
        if (context.authenticatedSubject().isEmpty() || context.authentication().isEmpty()) {
            return completed(
                    Outcome.succeeded(
                            errorMapper.response(
                                    request,
                                    destination,
                                    Saml.Statuses.REQUEST_DENIED,
                                    "Authenticated subject context is required",
                                    timeout.clock().now())));
        }
        final Session session = context.authentication().getOrNull().session();
        return sessions.establish(session, context, timeout).thenCompose(established -> switch (established) {
            case Outcome.Succeeded<Void> ignored -> assertionIssuer.singleSignOn(request, client, context, timeout);
            case Outcome.Rejected<Void> rejected -> completed(Outcome.rejected(rejected.failure()));
            case Outcome.Failed<Void> failed -> completed(Outcome.failed(failed.failure()));
            default -> throw new IllegalStateException("Unsupported Outcome implementation");
        });
    }

}
