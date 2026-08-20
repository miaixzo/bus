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
package org.miaixz.bus.auth.protocol.saml.security;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.protocol.saml.*;
import org.miaixz.bus.auth.protocol.saml.client.SamlSourceSettings;
import org.miaixz.bus.auth.shared.SecurityBaseline;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Enforces the SAML 2.0 Web Browser SSO assertion-consumer security profile.
 * <p>
 * This validator runs only after XML signature validation and encrypted-assertion replacement. It evaluates issuer,
 * freshness, Conditions, audience, bearer confirmation, subject identity, and authentication-session semantics using
 * the shared clock. It deliberately performs no identity mapping or application authorization.
 * </p>
 *
 * @author Kimi Liu
 */
public final class SamlAssertionValidator {

    /**
     * Shared non-relaxable SAML timing policy.
     */
    private final SecurityBaseline securityBaseline;

    /**
     * Creates a SAML assertion profile validator.
     *
     * @param securityBaseline shared protocol security baseline
     * @throws IllegalArgumentException if the baseline is {@code null}
     */
    public SamlAssertionValidator(final SecurityBaseline securityBaseline) {
        this.securityBaseline = Assert.notNull(securityBaseline, "SAML security baseline must not be null");
    }

    /**
     * Validates one plaintext assertion against trusted Source coordinates.
     *
     * @param assertion            assertion to validate
     * @param expectedInResponseTo expected request identifier
     * @param settings             trusted Source settings
     * @param context              invocation context
     * @param now                  shared-clock current instant
     * @return non-sensitive rejection reason, or {@code null} on success
     */
    private static String assertion(
            final Assertion assertion,
            final String expectedInResponseTo,
            final SamlSourceSettings settings,
            final Context context,
            final Instant now) {
        if (!settings.identityProviderEntityId().equals(assertion.issuer().nameId().value())) {
            return "SAML Assertion issuer does not match the trusted identity provider";
        }
        final Duration skew = settings.clockSkew();
        if (assertion.issueInstant().isAfter(now.plus(skew))
                || assertion.issueInstant().isBefore(now.minus(settings.maximumAssertionAge()).minus(skew))) {
            return "SAML Assertion IssueInstant is outside the accepted freshness interval";
        }
        if (!assertion.conditions().isPresent())
            return "SAML Assertion requires Conditions";
        final String conditions = conditions(assertion.conditions().getOrNull(), settings, now);
        if (conditions != null)
            return conditions;
        if (!assertion.subject().isPresent())
            return "SAML Assertion requires Subject";
        final String subject = subject(assertion.subject().getOrNull(), expectedInResponseTo, settings, context, now);
        if (subject != null)
            return subject;
        boolean authentication = false;
        for (Assertion.StatementContent content : assertion.statements()) {
            if (content instanceof Assertion.AuthenticationStatement statement) {
                authentication = true;
                final String problem = authentication(statement.statement(), skew, now);
                if (problem != null)
                    return problem;
            }
        }
        return authentication ? null : "SAML Assertion requires AuthnStatement";
    }

    /**
     * Evaluates assertion validity bounds and every standard condition.
     *
     * @param value    assertion Conditions
     * @param settings trusted Source settings
     * @param now      shared-clock current instant
     * @return non-sensitive rejection reason, or {@code null}
     */
    private static String conditions(final Conditions value, final SamlSourceSettings settings, final Instant now) {
        final Duration skew = settings.clockSkew();
        if (value.notBefore().isPresent() && now.plus(skew).isBefore(value.notBefore().getOrNull())) {
            return "SAML Assertion Conditions are not yet valid";
        }
        if (value.notOnOrAfter().isPresent() && !now.minus(skew).isBefore(value.notOnOrAfter().getOrNull())) {
            return "SAML Assertion Conditions have expired";
        }
        int audiences = 0;
        for (Conditions.Condition condition : value.conditions()) {
            switch (condition) {
                case Conditions.Audience audience -> {
                    audiences++;
                    if (!audience.restriction().audiences().contains(settings.entityId())) {
                        return "SAML AudienceRestriction does not permit this service provider";
                    }
                }
                case Conditions.OneTimeUse ignored -> {
                    // The following ReplayGuard stage supplies the required one-time enforcement.
                }
                case Conditions.ProxyRestriction ignored -> {
                    return "SAML ProxyRestriction cannot be consumed as a final service-provider condition";
                }
                case Conditions.Extension ignored -> {
                    return "SAML Assertion contains an unsupported extension Condition";
                }
            }
        }
        return audiences == 0 ? "SAML Assertion requires AudienceRestriction" : null;
    }

    /**
     * Validates a NameID subject and at least one applicable bearer confirmation.
     *
     * @param value                assertion Subject
     * @param expectedInResponseTo expected request identifier
     * @param settings             trusted Source settings
     * @param context              invocation context
     * @param now                  shared-clock current instant
     * @return non-sensitive rejection reason, or {@code null}
     */
    private static String subject(
            final Subject value,
            final String expectedInResponseTo,
            final SamlSourceSettings settings,
            final Context context,
            final Instant now) {
        if (!value.identifier().isPresent()
                || !(value.identifier().getOrNull() instanceof Subject.NamedIdentifier named)
                || named.value().value().isEmpty()) {
            return "SAML Assertion Subject requires a non-empty NameID";
        }
        boolean applicable = false;
        for (SubjectConfirmation confirmation : value.confirmations()) {
            if (!SubjectConfirmation.BEARER.equals(confirmation.method())) {
                return "SAML Web Browser SSO accepts only bearer SubjectConfirmation";
            }
            if (confirmation.data().isPresent()
                    && confirmation(confirmation.data().getOrNull(), expectedInResponseTo, settings, context, now)) {
                applicable = true;
            }
        }
        return applicable ? null : "SAML Assertion has no applicable bearer SubjectConfirmation";
    }

    /**
     * Tests one bearer SubjectConfirmationData against the current request.
     *
     * @param value                bearer confirmation data
     * @param expectedInResponseTo expected request identifier
     * @param settings             trusted Source settings
     * @param context              invocation context
     * @param now                  shared-clock current instant
     * @return whether every bearer constraint is satisfied
     */
    private static boolean confirmation(
            final SubjectConfirmationData value,
            final String expectedInResponseTo,
            final SamlSourceSettings settings,
            final Context context,
            final Instant now) {
        final Duration skew = settings.clockSkew();
        return value.recipient().isPresent()
                && settings.assertionConsumerServiceUrl().equals(value.recipient().getOrNull())
                && value.inResponseTo().isPresent() && expectedInResponseTo.equals(value.inResponseTo().getOrNull())
                && value.notOnOrAfter().isPresent() && now.minus(skew).isBefore(value.notOnOrAfter().getOrNull())
                && (!value.notBefore().isPresent() || !now.plus(skew).isBefore(value.notBefore().getOrNull()))
                && (!value.address().isPresent()
                        || context.network().remoteAddress().equals(value.address().getOrNull()));
    }

    /**
     * Validates authentication time and optional session expiration.
     *
     * @param value authentication statement
     * @param skew  permitted clock skew
     * @param now   shared-clock current instant
     * @return non-sensitive rejection reason, or {@code null}
     */
    private static String authentication(final AuthnStatement value, final Duration skew, final Instant now) {
        if (value.authnInstant().isAfter(now.plus(skew))) {
            return "SAML AuthnStatement AuthnInstant is in the future";
        }
        if (value.sessionNotOnOrAfter().isPresent()
                && !now.minus(skew).isBefore(value.sessionNotOnOrAfter().getOrNull())) {
            return "SAML AuthnStatement session has expired";
        }
        return null;
    }

    /**
     * Creates a completed outcome stage.
     *
     * @param outcome completed outcome
     * @param <T>     success type
     * @return completed stage
     */
    private static <T> CompletionStage<Outcome<T>> completed(final Outcome<T> outcome) {
        return CompletableFuture.completedFuture(outcome);
    }

    /**
     * Creates a safe protocol rejection using a shared Bus error.
     *
     * @param description non-sensitive description
     * @return rejected Response outcome
     */
    private static Outcome<Response> rejected(final String description) {
        return Outcome.rejected(new Outcome.Failure(ErrorCode._400, description, new JsonValue.ObjectValue(Map.of())));
    }

    /**
     * Validates every assertion in a successful correlated SAML Response.
     *
     * @param response             signature-validated response containing only plaintext assertions
     * @param expectedInResponseTo exact one-time Authentication Request ID
     * @param settings             trusted service-provider Source settings
     * @param context              immutable invocation context including observed remote address
     * @param timeout              shared end-to-end operation budget and clock
     * @return completed stage containing the unchanged response or a safe protocol rejection
     */
    public CompletionStage<Outcome<Response>> validate(
            final Response response,
            final String expectedInResponseTo,
            final SamlSourceSettings settings,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(response, "SAML Response must not be null");
        Assert.notBlank(expectedInResponseTo, "Expected SAML InResponseTo must not be blank");
        Assert.notNull(settings, "SAML Source settings must not be null");
        Assert.notNull(context, "SAML assertion context must not be null");
        Assert.notNull(timeout, "SAML assertion budget must not be null");
        if (timeout.expired()) {
            return completed(
                    Outcome.failed(
                            new Outcome.Failure(ErrorCode._408,
                                    "SAML assertion validation has no remaining time budget",
                                    new JsonValue.ObjectValue(Map.of()))));
        }
        final Duration baselineSkew = securityBaseline.require(Protocol.SAML).maximumClockSkew();
        if (settings.clockSkew().compareTo(baselineSkew) > 0) {
            return completed(rejected("SAML Source clock skew exceeds the shared security baseline"));
        }
        if (response.assertions().isEmpty()) {
            return completed(rejected("Successful SAML Response requires at least one assertion"));
        }
        final Instant now = timeout.clock().now();
        for (Response.AssertionContent content : response.assertions()) {
            if (!(content instanceof Response.PlainAssertion plain)) {
                return completed(rejected("SAML Response contains an assertion that was not decrypted"));
            }
            final String problem = assertion(plain.assertion(), expectedInResponseTo, settings, context, now);
            if (problem != null)
                return completed(rejected(problem));
        }
        return completed(Outcome.succeeded(response));
    }

}
