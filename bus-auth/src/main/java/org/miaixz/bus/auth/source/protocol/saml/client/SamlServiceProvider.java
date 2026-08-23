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
package org.miaixz.bus.auth.source.protocol.saml.client;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.FabricX.Url;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.source.protocol.saml.AuthnRequest;
import org.miaixz.bus.auth.source.protocol.saml.EntityDescriptor;
import org.miaixz.bus.auth.source.protocol.saml.LogoutRequest;
import org.miaixz.bus.auth.source.protocol.saml.Response;
import org.miaixz.bus.auth.source.protocol.saml.codec.RedirectBindingCodec;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Provides the standards-based SAML 2.0 service-provider facade for one compiled Source.
 * <p>
 * Browser SSO and Single Logout initiation produce HTTP-Redirect user-agent URLs. An identity-provider {@link Response}
 * arrives later at the assertion consumer service and is validated independently; neither initiation method pretends
 * that an asynchronous browser exchange has returned a synchronous identity-provider response. RelayState remains an
 * HTTP Binding value and is never inserted into SAML XML.
 * </p>
 *
 * @author Kimi Liu
 */
public class SamlServiceProvider {

    /**
     * Client that retrieves and validates trusted identity-provider metadata.
     */
    private final MetadataClient metadataClient;

    /**
     * Assertion consumer that validates a decoded SAML protocol response.
     */
    private final AssertionConsumerService assertionConsumerService;

    /**
     * HTTP-Redirect codec used for service-provider initiated requests.
     */
    private final RedirectBindingCodec redirectBindingCodec;

    /**
     * Validated deployment and security options for this service provider.
     */
    private final SamlClientOptions options;

    /**
     * Creates a SAML service provider from its four narrowly scoped collaborators.
     *
     * @param metadataClient           trusted identity-provider metadata client
     * @param assertionConsumerService validated assertion consumer service
     * @param redirectBindingCodec     HTTP-Redirect request codec
     * @param options                  validated service-provider options
     * @throws IllegalArgumentException if a collaborator is {@code null}
     */
    public SamlServiceProvider(final MetadataClient metadataClient,
            final AssertionConsumerService assertionConsumerService, final RedirectBindingCodec redirectBindingCodec,
            final SamlClientOptions options) {
        this.metadataClient = Assert.notNull(metadataClient, "SAML Metadata client must not be null");
        this.assertionConsumerService = Assert
                .notNull(assertionConsumerService, "SAML Assertion Consumer Service must not be null");
        this.redirectBindingCodec = Assert
                .notNull(redirectBindingCodec, "SAML Redirect Binding codec must not be null");
        this.options = Assert.notNull(options, "SAML Source options must not be null");
    }

    /**
     * Validates common invocation containers and the SAML Binding RelayState length limit.
     *
     * @param relayState optional opaque RelayState value
     * @param context    immutable invocation context
     * @param timeout    shared operation timeout
     * @throws IllegalArgumentException if a container is {@code null} or RelayState exceeds 80 bytes
     */
    private static void validateInvocation(
            final Optional<String> relayState,
            final Context context,
            final Timeout timeout) {
        Assert.notNull(relayState, "SAML RelayState container must not be null");
        Assert.notNull(context, "SAML invocation context must not be null");
        Assert.notNull(timeout, "SAML invocation timeout must not be null");
        final String value = relayState.getOrNull();
        if (value != null) {
            Assert.isTrue(value.getBytes(Charset.UTF_8).length <= 80, "SAML RelayState must not exceed 80 bytes");
        }
    }

    /**
     * Creates a safe timeout failure without including protocol message or endpoint data.
     *
     * @param operation safe operation label
     * @return closed timeout failure
     */
    private static Outcome.Failure timeoutFailure(final String operation) {
        return new Outcome.Failure(ErrorCode._408, operation + " has no remaining timeout",
                new JsonValue.ObjectValue(Map.of()));
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
     * Retrieves the trusted identity-provider metadata configured for this Source.
     *
     * @param context immutable invocation context
     * @param timeout shared end-to-end timeout
     * @return stage containing validated identity-provider metadata or a closed framework failure
     */
    public CompletionStage<Outcome<EntityDescriptor>> metadata(final Context context, final Timeout timeout) {
        return metadataClient.metadata(context, timeout);
    }

    /**
     * Encodes a SAML Authentication Request for HTTP-Redirect user-agent navigation.
     *
     * @param request    standard SAML Authentication Request
     * @param relayState optional opaque HTTP Binding RelayState value
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end timeout
     * @return stage containing the identity-provider redirect URL or a closed framework failure
     */
    public CompletionStage<Outcome<Url>> singleSignOn(
            final AuthnRequest request,
            final Optional<String> relayState,
            final Context context,
            final Timeout timeout) {
        Assert.notNull(request, "SAML Authentication Request must not be null");
        validateInvocation(relayState, context, timeout);
        if (timeout.expired()) {
            return completed(Outcome.failed(timeoutFailure("SAML Single Sign-On initiation")));
        }
        return redirectBindingCodec.encode(
                options.singleSignOnServiceEndpoint(),
                request,
                relayState,
                options.signAuthnRequests(),
                options.signingKeyId(),
                options.signatureAlgorithm(),
                context,
                timeout);
    }

    /**
     * Validates one decoded SAML Response received by the assertion consumer service.
     *
     * @param response             decoded standard SAML Response
     * @param expectedInResponseTo exact Authentication Request ID retained by the one-time interaction
     * @param context              immutable invocation context carrying callback correlation facts
     * @param timeout              shared end-to-end timeout
     * @return stage containing the validated response or a closed framework failure
     */
    public CompletionStage<Outcome<Response>> consume(
            final Response response,
            final String expectedInResponseTo,
            final Context context,
            final Timeout timeout) {
        return assertionConsumerService.consume(response, expectedInResponseTo, context, timeout);
    }

    /**
     * Encodes a SAML Logout Request for HTTP-Redirect user-agent navigation.
     *
     * @param request    standard SAML Logout Request
     * @param relayState optional opaque HTTP Binding RelayState value
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end timeout
     * @return stage containing the identity-provider redirect URL or a closed framework failure
     */
    public CompletionStage<Outcome<Url>> singleLogout(
            final LogoutRequest request,
            final Optional<String> relayState,
            final Context context,
            final Timeout timeout) {
        Assert.notNull(request, "SAML Logout Request must not be null");
        validateInvocation(relayState, context, timeout);
        if (timeout.expired()) {
            return completed(Outcome.failed(timeoutFailure("SAML Single Logout initiation")));
        }
        final var endpoint = options.singleLogoutServiceEndpoint().getOrNull();
        if (endpoint == null) {
            return completed(
                    Outcome.rejected(
                            new Outcome.Failure(ErrorCode._404,
                                    "SAML Source does not declare a Single Logout Service endpoint",
                                    new JsonValue.ObjectValue(Map.of()))));
        }
        return redirectBindingCodec.encode(
                endpoint,
                request,
                relayState,
                options.signLogoutRequests(),
                options.signingKeyId(),
                options.signatureAlgorithm(),
                context,
                timeout);
    }

}
