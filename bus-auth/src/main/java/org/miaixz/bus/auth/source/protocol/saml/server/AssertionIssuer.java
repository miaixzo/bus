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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.resolver.ConsumerMetadata;
import org.miaixz.bus.auth.source.DriverServices;
import org.miaixz.bus.auth.source.protocol.saml.*;
import org.miaixz.bus.auth.source.protocol.saml.codec.SamlMessageCodec;
import org.miaixz.bus.auth.worker.loader.AttributeLoader;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.data.id.UUID;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Issues a standard SAML 2.0 assertion and successful Web Browser SSO Response.
 * <p>
 * This server-side issuer derives every security-sensitive value from validated registration and invocation state. It
 * creates an unsigned semantic model; the compiled identity-provider runtime applies XML signatures according to
 * {@link SamlServerOptions} before the model crosses the Roster boundary.
 * </p>
 *
 * @author Kimi Liu
 */
public class AssertionIssuer {

    /**
     * Unspecified attribute-name format used when external attributes do not declare another SAML format.
     */
    private static final String UNSPECIFIED_ATTRIBUTE = "urn:oasis:names:tc:SAML:2.0:attrname-format:unspecified";

    /**
     * Fallback standard authentication context when the external boundary supplies no class reference.
     */
    private static final String UNSPECIFIED_AUTHN_CONTEXT = "urn:oasis:names:tc:SAML:2.0:ac:classes:unspecified";

    /**
     * Validated identity-provider options.
     */
    private final SamlServerOptions options;

    /**
     * External attribute loader and framework-owned attribute parser.
     */
    private final DriverServices services;

    /**
     * Strict SAML XML value codec.
     */
    private final SamlMessageCodec messageCodec;

    /**
     * Creates an assertion issuer from identity-provider policy and external attribute resolution.
     *
     * @param options      validated SAML identity-provider options
     * @param services     external loaders and pure parsers
     * @param messageCodec strict SAML message and AttributeValue codec
     * @throws IllegalArgumentException if a collaborator is {@code null}
     */
    public AssertionIssuer(final SamlServerOptions options, final DriverServices services,
            final SamlMessageCodec messageCodec) {
        this.options = Assert.notNull(options, "SAML identity-provider options must not be null");
        this.services = Assert.notNull(services, "SAML execution services must not be null");
        this.messageCodec = Assert.notNull(messageCodec, "SAML message codec must not be null");
    }

    /**
     * Generates an unpredictable XML NCName-compatible SAML identifier using bus-core UUID support.
     *
     * @return underscore-prefixed identifier
     */
    private static String identifier() {
        return Symbol.C_UNDERLINE + UUID.randomUUID().toString(true);
    }

    /**
     * Creates a non-sensitive framework failure.
     *
     * @param code        shared Bus error code
     * @param description safe diagnostic description
     * @return immutable failure value
     */
    private static Outcome.Failure failure(final Errors code, final String description) {
        return new Outcome.Failure(code, description, new JsonValue.ObjectValue(Map.of()));
    }

    /**
     * Resolves current subject attributes and issues one successful standard Response.
     *
     * @param request validated Authentication Request
     * @param client  validated registered service provider
     * @param context authenticated subject and session context
     * @param timeout shared end-to-end timeout
     * @return stage containing the successful semantic Response or a closed framework failure
     */
    public CompletionStage<Outcome<Response>> singleSignOn(
            final AuthnRequest request,
            final ConsumerMetadata client,
            final Context context,
            final Timeout timeout) {
        Assert.notNull(request, "SAML Authentication Request must not be null");
        Assert.notNull(client, "SAML registered service provider must not be null");
        Assert.notNull(context, "SAML assertion issuance context must not be null");
        Assert.notNull(timeout, "SAML assertion issuance timeout must not be null");
        final org.miaixz.bus.auth.Subject subject = context.authenticatedSubject().getOrNull();
        final Context.Authentication authentication = context.authentication().getOrNull();
        if (subject == null || authentication == null || timeout.expired()) {
            return CompletableFuture.completedFuture(
                    Outcome.failed(
                            failure(
                                    ErrorCode._408,
                                    "SAML assertion issuance lacks authenticated context or remaining time")));
        }
        return Outcome
                .mapStage(
                        () -> services.attributeLoader()
                                .load(new AttributeLoader.Request(services.entry(), subject.key()), context, timeout),
                        loaded -> services.attributeParser().parse(services.entry(), subject.key(), loaded))
                .thenApply(outcome -> switch (outcome) {
                    case Outcome.Succeeded<JsonValue.ObjectValue> success -> Outcome.succeeded(
                            response(request, client, subject, authentication, success.value(), context, timeout));
                    case Outcome.Rejected<JsonValue.ObjectValue> rejected -> Outcome.rejected(rejected.failure());
                    case Outcome.Failed<JsonValue.ObjectValue> failed -> Outcome.failed(failed.failure());
                    default -> throw new IllegalStateException("Unsupported Outcome implementation");
                });
    }

    /**
     * Constructs a response whose subject, audience, recipient, session, and time bounds are registration-derived.
     *
     * @param request          validated Authentication Request
     * @param client           registered service provider
     * @param frameworkSubject authenticated framework subject
     * @param authentication   authenticated session facts
     * @param attributes       current resolved subject attributes
     * @param context          authenticated invocation context supplying the network confirmation facts
     * @param timeout          shared clock and deadline
     * @return unsigned semantic success Response
     */
    private Response response(
            final AuthnRequest request,
            final ConsumerMetadata client,
            final org.miaixz.bus.auth.Subject frameworkSubject,
            final Context.Authentication authentication,
            final JsonValue.ObjectValue attributes,
            final Context context,
            final Timeout timeout) {
        final Instant now = timeout.clock().now();
        final Instant configuredExpiry = now.plus(options.assertionLifetime());
        final Instant expiresAt = authentication.session().expiresAt().isBefore(configuredExpiry)
                ? authentication.session().expiresAt()
                : configuredExpiry;
        final String destination = request.assertionConsumerServiceUrl().getOrNull();
        final Issuer issuer = issuer();
        final NameID nameId = new NameID(frameworkSubject.reference().value(), Optional.of(options.entityId()),
                Optional.of(client.id()), Optional.of(Saml.NameIdFormats.PERSISTENT), Optional.empty());
        final SubjectConfirmationData confirmationData = new SubjectConfirmationData(Optional.empty(),
                Optional.of(expiresAt), Optional.of(destination), Optional.of(request.id()),
                Optional.of(context.network().remoteAddress()), List.of(), Map.of());
        final Subject subject = new Subject(Optional.of(new Subject.NamedIdentifier(nameId)),
                List.of(
                        new SubjectConfirmation(Optional.empty(), Optional.of(confirmationData),
                                Saml.ConfirmationMethods.BEARER)));
        final Conditions conditions = new Conditions(Optional.of(now.minus(options.clockSkew())),
                Optional.of(expiresAt),
                List.of(new Conditions.Audience(new AudienceRestriction(List.of(client.id())))));
        final String contextClass = authentication.authenticationContextClass().isPresent()
                ? authentication.authenticationContextClass().getOrNull()
                : UNSPECIFIED_AUTHN_CONTEXT;
        final AuthnStatement authenticationStatement = new AuthnStatement(authentication.session().issuedAt(),
                Optional.of(authentication.session().key().value()), Optional.of(authentication.session().expiresAt()),
                Optional.of(context.network().remoteAddress()), Optional.empty(),
                new AuthnContext(Optional.of(contextClass), Optional.empty(), Optional.empty(), List.of()));
        final List<Assertion.StatementContent> statements = new ArrayList<>();
        statements.add(new Assertion.AuthenticationStatement(authenticationStatement));
        final List<AttributeStatement.AttributeContent> attributeValues = attributes(attributes);
        if (!attributeValues.isEmpty()) {
            statements.add(new Assertion.AttributesStatement(new AttributeStatement(attributeValues)));
        }
        final Assertion assertion = new Assertion("2.0", identifier(), now, issuer, Optional.empty(),
                Optional.of(subject), Optional.of(conditions), Optional.empty(), statements);
        return new Response(identifier(), Optional.of(request.id()), "2.0", now, Optional.of(destination),
                Optional.empty(), Optional.of(issuer), Optional.empty(), List.of(),
                new Status(new StatusCode(Saml.Statuses.SUCCESS, Optional.empty()), Optional.empty(), Optional.empty()),
                List.of(new Response.PlainAssertion(assertion)));
    }

    /**
     * Converts the implementation-neutral attribute snapshot into ordered standard SAML Attributes.
     *
     * @param object resolved implementation-neutral attributes
     * @return immutable ordered plain Attribute entries
     */
    private List<AttributeStatement.AttributeContent> attributes(final JsonValue.ObjectValue object) {
        final List<AttributeStatement.AttributeContent> result = new ArrayList<>(object.values().size());
        object.values().forEach((name, value) -> {
            final List<byte[]> values = new ArrayList<>();
            if (value instanceof JsonValue.ArrayValue array) {
                for (JsonValue item : array.values()) {
                    values.add(messageCodec.attributeValue(item));
                }
            } else {
                values.add(messageCodec.attributeValue(value));
            }
            result.add(
                    new AttributeStatement.PlainAttribute(new Attribute(name, Optional.of(UNSPECIFIED_ATTRIBUTE),
                            Optional.empty(), Map.of(), values)));
        });
        return List.copyOf(result);
    }

    /**
     * Creates the standard identity-provider Issuer value.
     *
     * @return entity-format SAML Issuer
     */
    private Issuer issuer() {
        return new Issuer(new NameID(options.entityId(), Optional.empty(), Optional.empty(),
                Optional.of(Saml.NameIdFormats.ENTITY), Optional.empty()));
    }

}
