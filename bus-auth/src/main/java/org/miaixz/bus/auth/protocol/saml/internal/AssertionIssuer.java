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
package org.miaixz.bus.auth.protocol.saml.internal;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.protocol.saml.*;
import org.miaixz.bus.auth.protocol.saml.codec.SamlMessageCodec;
import org.miaixz.bus.auth.protocol.saml.server.SamlProviderSettings;
import org.miaixz.bus.auth.resolver.AttributeResolver;
import org.miaixz.bus.auth.resolver.ClientResolver;
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
 * This internal issuer derives every security-sensitive value from validated registration and invocation state. It
 * creates an unsigned semantic model; the compiled Provider runtime applies XML signatures according to
 * {@link SamlProviderSettings} before the model crosses the Registry boundary.
 * </p>
 *
 * @author Kimi Liu
 */
public final class AssertionIssuer {

    /**
     * Persistent NameID format emitted by the identity-provider assertion issuer.
     */
    private static final String PERSISTENT_NAME_ID = "urn:oasis:names:tc:SAML:2.0:nameid-format:persistent";

    /**
     * Entity NameID format used by SAML Issuer values.
     */
    private static final String ENTITY_NAME_ID = "urn:oasis:names:tc:SAML:2.0:nameid-format:entity";

    /**
     * Unspecified attribute-name format used when external attributes do not declare another SAML format.
     */
    private static final String UNSPECIFIED_ATTRIBUTE = "urn:oasis:names:tc:SAML:2.0:attrname-format:unspecified";

    /**
     * Fallback standard authentication context when the external boundary supplies no class reference.
     */
    private static final String UNSPECIFIED_AUTHN_CONTEXT = "urn:oasis:names:tc:SAML:2.0:ac:classes:unspecified";

    /**
     * Validated identity-provider settings.
     */
    private final SamlProviderSettings settings;

    /**
     * External current subject-attribute resolver.
     */
    private final AttributeResolver attributeResolver;

    /**
     * Strict SAML XML value codec.
     */
    private final SamlMessageCodec messageCodec;

    /**
     * Creates an assertion issuer from Provider policy and external attribute resolution.
     *
     * @param settings          validated SAML Provider settings
     * @param attributeResolver external subject attribute resolver
     * @param messageCodec      strict SAML message and AttributeValue codec
     * @throws IllegalArgumentException if a collaborator is {@code null}
     */
    public AssertionIssuer(final SamlProviderSettings settings, final AttributeResolver attributeResolver,
            final SamlMessageCodec messageCodec) {
        this.settings = Assert.notNull(settings, "SAML Provider settings must not be null");
        this.attributeResolver = Assert.notNull(attributeResolver, "SAML attribute resolver must not be null");
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
     * @param timeout shared end-to-end time budget
     * @return stage containing the successful semantic Response or a closed framework failure
     */
    public CompletionStage<Outcome<Response>> singleSignOn(
            final AuthnRequest request,
            final ClientResolver.Client client,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(request, "SAML Authentication Request must not be null");
        Assert.notNull(client, "SAML registered service provider must not be null");
        Assert.notNull(context, "SAML assertion issuance context must not be null");
        Assert.notNull(timeout, "SAML assertion issuance time budget must not be null");
        final org.miaixz.bus.auth.Subject subject = context.authenticatedSubject().getOrNull();
        final Context.Authentication authentication = context.authentication().getOrNull();
        if (subject == null || authentication == null || timeout.expired()) {
            return CompletableFuture.completedFuture(
                    Outcome.failed(
                            failure(
                                    ErrorCode._408,
                                    "SAML assertion issuance lacks authenticated context or remaining time")));
        }
        return attributeResolver.resolve(subject.key(), context, timeout).thenApply(outcome -> switch (outcome) {
            case Outcome.Succeeded<AttributeResolver.Attributes> success -> Outcome
                    .succeeded(response(request, client, subject, authentication, success.value(), context, timeout));
            case Outcome.Rejected<AttributeResolver.Attributes> rejected -> Outcome.rejected(rejected.failure());
            case Outcome.Failed<AttributeResolver.Attributes> failed -> Outcome.failed(failed.failure());
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
            final ClientResolver.Client client,
            final org.miaixz.bus.auth.Subject frameworkSubject,
            final Context.Authentication authentication,
            final AttributeResolver.Attributes attributes,
            final Context context,
            final Timeout.Budget timeout) {
        final Instant now = timeout.clock().now();
        final Instant configuredExpiry = now.plus(settings.assertionLifetime());
        final Instant expiresAt = authentication.session().expiresAt().isBefore(configuredExpiry)
                ? authentication.session().expiresAt()
                : configuredExpiry;
        final String destination = request.assertionConsumerServiceUrl().getOrNull();
        final Issuer issuer = issuer();
        final NameID nameId = new NameID(frameworkSubject.reference().value(), Optional.of(settings.entityId()),
                Optional.of(client.id()), Optional.of(PERSISTENT_NAME_ID), Optional.empty());
        final SubjectConfirmationData confirmationData = new SubjectConfirmationData(Optional.empty(),
                Optional.of(expiresAt), Optional.of(destination), Optional.of(request.id()),
                Optional.of(context.network().remoteAddress()), List.of(), Map.of());
        final Subject subject = new Subject(Optional.of(new Subject.NamedIdentifier(nameId)), List.of(
                new SubjectConfirmation(Optional.empty(), Optional.of(confirmationData), SubjectConfirmation.BEARER)));
        final Conditions conditions = new Conditions(Optional.of(now.minus(settings.clockSkew())),
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
        final List<AttributeStatement.AttributeContent> attributeValues = attributes(attributes.values());
        if (!attributeValues.isEmpty()) {
            statements.add(new Assertion.AttributesStatement(new AttributeStatement(attributeValues)));
        }
        final Assertion assertion = new Assertion("2.0", identifier(), now, issuer, Optional.empty(),
                Optional.of(subject), Optional.of(conditions), Optional.empty(), statements);
        return new Response(identifier(), Optional.of(request.id()), "2.0", now, Optional.of(destination),
                Optional.empty(), Optional.of(issuer), Optional.empty(), List.of(),
                new Status(new StatusCode(StatusCode.SUCCESS, Optional.empty()), Optional.empty(), Optional.empty()),
                List.of(new Response.PlainAssertion(assertion)));
    }

    /**
     * Converts the provider-neutral attribute snapshot into ordered standard SAML Attributes.
     *
     * @param object resolved provider-neutral attributes
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
        return new Issuer(new NameID(settings.entityId(), Optional.empty(), Optional.empty(),
                Optional.of(ENTITY_NAME_ID), Optional.empty()));
    }

}
