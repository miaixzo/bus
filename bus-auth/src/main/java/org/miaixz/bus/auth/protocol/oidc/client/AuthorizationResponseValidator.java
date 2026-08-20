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
package org.miaixz.bus.auth.protocol.oidc.client;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.guard.IssuerValidator;
import org.miaixz.bus.auth.protocol.oauth2.AuthorizationCodeResponse;
import org.miaixz.bus.auth.protocol.oauth2.AuthorizationErrorResponse;
import org.miaixz.bus.auth.protocol.oauth2.AuthorizationResponse;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Validates RFC 9207 issuer and OAuth state bindings on an OIDC code-flow authorization response.
 * <p>
 * One-time correlation consumption remains with the Source callback owner. This validator does not redeem or inspect
 * the authorization code and never converts framework outcomes into wire responses.
 * </p>
 *
 * @author Kimi Liu
 */
public final class AuthorizationResponseValidator {

    /**
     * Shared exact issuer comparison primitive.
     */
    private final IssuerValidator issuerValidator;

    /**
     * Creates a response validator using the shared issuer guard.
     *
     * @param issuerValidator exact issuer validator
     * @throws IllegalArgumentException if {@code issuerValidator} is {@code null}
     */
    public AuthorizationResponseValidator(final IssuerValidator issuerValidator) {
        this.issuerValidator = Assert.notNull(issuerValidator, "OpenID Connect issuer validator must not be null");
    }

    /**
     * Compares sensitive state values without prefix-dependent early return.
     *
     * @param expected expected state
     * @param actual   received state
     * @return whether both strings contain identical UTF-8 octets
     */
    private static boolean constantTime(final String expected, final String actual) {
        final byte[] first = expected.getBytes(Charset.UTF_8);
        final byte[] second = actual.getBytes(Charset.UTF_8);
        int difference = first.length ^ second.length;
        final int length = Math.max(first.length, second.length);
        for (int index = 0; index < length; index++) {
            difference |= first[index % first.length] ^ second[index % second.length];
        }
        return difference == 0;
    }

    /**
     * Creates a completed asynchronous result.
     *
     * @param <T>     success value type
     * @param outcome completed outcome
     * @return completed stage
     */
    private static <T> CompletionStage<Outcome<T>> completed(final Outcome<T> outcome) {
        return CompletableFuture.completedFuture(outcome);
    }

    /**
     * Validates issuer and state before an authorization code can be redeemed.
     *
     * @param request explicit expected response bindings
     * @param context immutable invocation context
     * @param timeout shared end-to-end time budget
     * @return completed stage containing the accepted response or a closed failure
     */
    public CompletionStage<Outcome<AuthorizationResponse>> validate(
            final Request request,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(request, "OpenID Connect authorization response validation request must not be null");
        Assert.notNull(context, "OpenID Connect authorization response context must not be null");
        Assert.notNull(timeout, "OpenID Connect authorization response budget must not be null");
        try {
            if (timeout.expired()) {
                throw new ValidateException("OpenID Connect authorization response budget has expired");
            }
            if (request.response() instanceof AuthorizationErrorResponse) {
                return completed(Outcome.succeeded(request.response()));
            }
            final AuthorizationCodeResponse response = (AuthorizationCodeResponse) request.response();
            final JsonValue issuer = response.extensions().values().get(OAuth2.Parameters.ISS);
            issuerValidator.validate(
                    request.expectedIssuer(),
                    issuer instanceof JsonValue.StringValue value ? value.value() : null);
            final String expected = request.expectedState().getOrNull();
            final String actual = response.state().getOrNull();
            if ((expected == null) != (actual == null) || expected != null && !constantTime(expected, actual)) {
                throw new ValidateException("OpenID Connect authorization response state binding failed");
            }
            return completed(Outcome.succeeded(request.response()));
        } catch (ValidateException exception) {
            return completed(
                    Outcome.rejected(
                            new Outcome.Failure(ErrorCode._401,
                                    "OpenID Connect authorization response validation failed",
                                    new JsonValue.ObjectValue(Map.of()))));
        } catch (RuntimeException exception) {
            return completed(
                    Outcome.failed(
                            new Outcome.Failure(ErrorCode._500,
                                    "OpenID Connect authorization response validation could not complete",
                                    new JsonValue.ObjectValue(Map.of()))));
        }
    }

    /**
     * Carries one decoded authentication response and its trusted expected bindings.
     *
     * @param response       decoded successful OIDC authentication response
     * @param expectedIssuer trusted OpenID Provider issuer
     * @param expectedState  optional state stored for the originating request
     * @author Kimi Liu
     */
    public record Request(AuthorizationResponse response, String expectedIssuer, Optional<String> expectedState) {

        /**
         * Validates and normalizes response binding inputs.
         *
         * @throws IllegalArgumentException if a required component or optional container is {@code null}
         * @throws ValidateException        if a present expected state value is empty
         */
        public Request {
            Assert.notNull(response, "OpenID Connect authorization response must not be null");
            expectedIssuer = Assert.notBlank(expectedIssuer, "OpenID Connect expected issuer must not be blank");
            Assert.notNull(expectedState, "OpenID Connect expected-state container must not be null");
            final String state = expectedState.getOrNull();
            if (state != null) {
                Assert.notEmpty(state, "OpenID Connect expected state must not be empty");
            }
            expectedState = Optional.ofNullable(state);
        }

        /**
         * Returns a diagnostic summary without response code or state values.
         *
         * @return redacted validation request summary
         */
        @Override
        public String toString() {
            return "Request[response=[REDACTED], expectedIssuer=" + expectedIssuer + ", expectedState=[REDACTED]]";
        }

    }

}
