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
package org.miaixz.bus.auth.protocol.saml.client;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.protocol.saml.Response;
import org.miaixz.bus.auth.protocol.saml.StatusCode;
import org.miaixz.bus.auth.protocol.saml.security.SamlAssertionValidator;
import org.miaixz.bus.auth.protocol.saml.security.SamlDecryptionService;
import org.miaixz.bus.auth.protocol.saml.security.SamlReplayValidator;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Validates a decoded SAML 2.0 Response delivered to a service provider's Assertion Consumer Service.
 * <p>
 * This service retains the standard SAML message as its result. Mapping assertion subjects and attributes into a Bus
 * external identity belongs to the Source authentication adapter after this complete protocol validation succeeds. The
 * expected request identifier is supplied explicitly from one-time browser correlation state.
 * </p>
 *
 * @author Kimi Liu
 */
public final class AssertionConsumerService {

    /**
     * Validated service-provider trust and response policy.
     */
    private final SamlSourceSettings settings;

    /**
     * Conditions, audience, subject-confirmation, issuer, and time validator.
     */
    private final SamlAssertionValidator assertionValidator;

    /**
     * Atomic response and assertion replay validator.
     */
    private final SamlReplayValidator replayValidator;

    /**
     * EncryptedAssertion decryption and secure re-decoding service.
     */
    private final SamlDecryptionService decryptionService;

    /**
     * Creates an Assertion Consumer Service with an explicit validation pipeline.
     *
     * @param settings           validated SAML Source settings
     * @param assertionValidator SAML assertion profile validator
     * @param replayValidator    atomic SAML replay validator
     * @param decryptionService  SAML EncryptedAssertion decryption service
     * @throws IllegalArgumentException if a collaborator is {@code null}
     */
    public AssertionConsumerService(final SamlSourceSettings settings, final SamlAssertionValidator assertionValidator,
            final SamlReplayValidator replayValidator, final SamlDecryptionService decryptionService) {
        this.settings = Assert.notNull(settings, "SAML Source settings must not be null");
        this.assertionValidator = Assert.notNull(assertionValidator, "SAML assertion validator must not be null");
        this.replayValidator = Assert.notNull(replayValidator, "SAML replay validator must not be null");
        this.decryptionService = Assert.notNull(decryptionService, "SAML decryption service must not be null");
    }

    /**
     * Creates a non-sensitive closed framework failure.
     *
     * @param code        shared Bus error code
     * @param description safe diagnostic description
     * @return immutable failure value
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
     * Validates one solicited SAML Response through the fixed security pipeline.
     *
     * @param response             decoded standard SAML Response
     * @param expectedInResponseTo exact Authentication Request ID retained in one-time correlation state
     * @param context              immutable invocation context
     * @param timeout              shared end-to-end time budget
     * @return stage containing the validated standard response or a closed framework failure
     */
    public CompletionStage<Outcome<Response>> consume(
            final Response response,
            final String expectedInResponseTo,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(response, "SAML Response must not be null");
        Assert.notBlank(expectedInResponseTo, "SAML expected InResponseTo value must not be blank");
        Assert.notNull(context, "SAML assertion-consumer context must not be null");
        Assert.notNull(timeout, "SAML assertion-consumer time budget must not be null");
        final Outcome<Response> envelope = validateEnvelope(response, expectedInResponseTo, timeout);
        if (!(envelope instanceof Outcome.Succeeded<Response>)) {
            return completed(envelope);
        }
        return decryptionService.decrypt(response, settings, context, timeout)
                .thenCompose(decrypted -> switch (decrypted) {
                    case Outcome.Succeeded<Response> success -> assertionValidator
                            .validate(success.value(), expectedInResponseTo, settings, context, timeout);
                    case Outcome.Rejected<Response> rejected -> completed(Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<Response> failed -> completed(Outcome.failed(failed.failure()));
                }).thenCompose(validated -> switch (validated) {
                    case Outcome.Succeeded<Response> success -> replayValidator
                            .validate(success.value(), settings, context, timeout);
                    case Outcome.Rejected<Response> rejected -> completed(Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<Response> failed -> completed(Outcome.failed(failed.failure()));
                });
    }

    /**
     * Validates status, destination, request correlation, and the available time budget before cryptographic work.
     *
     * @param response             decoded SAML Response
     * @param expectedInResponseTo expected request identifier
     * @param timeout              shared operation budget
     * @return successful unchanged response or an expected protocol rejection
     */
    private Outcome<Response> validateEnvelope(
            final Response response,
            final String expectedInResponseTo,
            final Timeout.Budget timeout) {
        if (timeout.expired()) {
            return Outcome.failed(failure(ErrorCode._408, "SAML Response validation has no remaining time budget"));
        }
        if (!StatusCode.SUCCESS.equals(response.status().statusCode().value())) {
            return Outcome.rejected(failure(ErrorCode._401, "SAML identity provider returned a non-success status"));
        }
        if (!expectedInResponseTo.equals(response.inResponseTo().getOrNull())) {
            return Outcome.rejected(
                    failure(ErrorCode._401, "SAML Response InResponseTo does not match the initiated request"));
        }
        if (!settings.assertionConsumerServiceUrl().equals(response.destination().getOrNull())) {
            return Outcome.rejected(
                    failure(ErrorCode._401, "SAML Response Destination does not match the Assertion Consumer Service"));
        }
        if (response.assertions().isEmpty()) {
            return Outcome.rejected(failure(ErrorCode._401, "Successful SAML Response does not contain an assertion"));
        }
        return Outcome.succeeded(response);
    }

}
