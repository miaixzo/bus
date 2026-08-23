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
import org.miaixz.bus.auth.FabricX;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.source.DriverServices;
import org.miaixz.bus.auth.source.protocol.saml.EntityDescriptor;
import org.miaixz.bus.auth.source.protocol.saml.IdpSsoDescriptor;
import org.miaixz.bus.auth.source.protocol.saml.SamlBinding;
import org.miaixz.bus.auth.source.protocol.saml.codec.MetadataCodec;
import org.miaixz.bus.auth.source.protocol.saml.codec.SamlMessageCodec;
import org.miaixz.bus.auth.source.protocol.saml.security.SamlSignatureValidator;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Retrieves and validates the trusted SAML 2.0 identity-provider Metadata document for one Source.
 * <p>
 * The fetched entity must exactly match the configured identity-provider entity and must declare the exact SSO and
 * optional SLO endpoint locations used by the compiled Source. XML signature verification is delegated to the shared
 * SAML signature validator backed by external certificate and key loaders.
 * </p>
 *
 * @author Kimi Liu
 */
public class MetadataClient {

    /**
     * Validated SAML Source deployment options.
     */
    private final SamlClientOptions options;

    /**
     * Capability-limited Source services supplying security policies and execution dependencies.
     */
    private final DriverServices services;

    /**
     * Strict secure XML Metadata codec.
     */
    private final MetadataCodec codec;

    /**
     * XML signature and certificate-chain validator.
     */
    private final SamlSignatureValidator signatureValidator;

    /**
     * Creates a SAML Metadata client for one compiled Source.
     *
     * @param options            validated SAML Source options
     * @param services           externally owned execution services
     * @param codec              strict SAML Metadata codec
     * @param signatureValidator SAML XML signature validator
     * @throws IllegalArgumentException if a collaborator is {@code null}
     */
    public MetadataClient(final SamlClientOptions options, final DriverServices services, final MetadataCodec codec,
            final SamlSignatureValidator signatureValidator) {
        this.options = Assert.notNull(options, "SAML Source options must not be null");
        this.services = Assert.notNull(services, "SAML execution services must not be null");
        this.codec = Assert.notNull(codec, "SAML Metadata codec must not be null");
        this.signatureValidator = Assert.notNull(signatureValidator, "SAML signature validator must not be null");
    }

    /**
     * Creates a non-sensitive framework failure.
     *
     * @param code        shared Bus error code
     * @param description safe diagnostic description
     * @return closed failure value
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
     * Retrieves, decodes, validates, and trust-binds identity-provider metadata.
     *
     * @param context immutable invocation context
     * @param timeout shared end-to-end timeout
     * @return stage containing trusted metadata or a closed framework failure
     */
    public CompletionStage<Outcome<EntityDescriptor>> metadata(final Context context, final Timeout timeout) {
        Assert.notNull(context, "SAML Metadata invocation context must not be null");
        Assert.notNull(timeout, "SAML Metadata timeout must not be null");
        if (timeout.expired()) {
            return completed(Outcome.failed(failure(ErrorCode._408, "SAML Metadata request has no remaining timeout")));
        }
        return CompletableFuture.supplyAsync(() -> retrieve(timeout), services.executor())
                .thenCompose(outcome -> switch (outcome) {
                    case Outcome.Succeeded<SamlMessageCodec.Document<EntityDescriptor>> success -> signatureValidator
                            .validateMetadata(success.value(), options.identityProviderEntityId(), context, timeout);
                    case Outcome.Rejected<SamlMessageCodec.Document<EntityDescriptor>> rejected -> completed(
                            Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<SamlMessageCodec.Document<EntityDescriptor>> failed -> completed(
                            Outcome.failed(failed.failure()));
                    default -> throw new IllegalStateException("Unsupported Outcome implementation");
                }).thenApply(outcome -> switch (outcome) {
                    case Outcome.Succeeded<SamlMessageCodec.Document<EntityDescriptor>> success -> Outcome
                            .succeeded(success.value().message());
                    case Outcome.Rejected<SamlMessageCodec.Document<EntityDescriptor>> rejected -> Outcome
                            .rejected(rejected.failure());
                    case Outcome.Failed<SamlMessageCodec.Document<EntityDescriptor>> failed -> Outcome
                            .failed(failed.failure());
                    default -> throw new IllegalStateException("Unsupported Outcome implementation");
                });
    }

    /**
     * Executes the bounded Fabric request and validates non-cryptographic Metadata invariants.
     *
     * @param timeout decreasing operation timeout
     * @return retrieved metadata outcome before XML signature verification
     */
    private Outcome<SamlMessageCodec.Document<EntityDescriptor>> retrieve(final Timeout timeout) {
        try {
            if (timeout.expired()) {
                return Outcome.failed(failure(ErrorCode._408, "SAML Metadata request exhausted its timeout"));
            }
            final var endpoint = options.identityProviderMetadataEndpoint();
            final var response = FabricX.http(Protocol.SAML, timeout, services.policies())
                    .url(endpoint.url().toString()).method(Http.Method.GET).execute();
            final SamlMessageCodec.Document<EntityDescriptor> document = codec.decode(response);
            final EntityDescriptor metadata = document.message();
            if (!options.identityProviderEntityId().equals(metadata.entityId())) {
                return Outcome.rejected(
                        failure(
                                ErrorCode._400,
                                "SAML Metadata entityID does not match the configured identity provider"));
            }
            final var validUntil = metadata.validUntil().getOrNull();
            if (validUntil != null && !validUntil.isAfter(timeout.clock().now())) {
                return Outcome.rejected(failure(ErrorCode._400, "SAML Metadata document has expired"));
            }
            if (!declaresConfiguredEndpoints(metadata)) {
                return Outcome.rejected(
                        failure(
                                ErrorCode._400,
                                "SAML Metadata does not declare the configured identity-provider endpoints"));
            }
            return Outcome.succeeded(document);
        } catch (RuntimeException exception) {
            return Outcome.failed(failure(ErrorCode._502, "SAML Metadata endpoint request failed"));
        }
    }

    /**
     * Checks that one IdP role declares the exact configured Redirect SSO and optional Redirect SLO locations.
     *
     * @param metadata decoded identity-provider metadata
     * @return {@code true} when one role exactly covers every configured remote endpoint
     */
    private boolean declaresConfiguredEndpoints(final EntityDescriptor metadata) {
        final String signOn = options.singleSignOnServiceEndpoint().url().toString();
        final var configuredLogout = options.singleLogoutServiceEndpoint().getOrNull();
        final String logout = configuredLogout == null ? null : configuredLogout.url().toString();
        for (IdpSsoDescriptor identityProvider : metadata.identityProviders()) {
            final boolean signOnDeclared = identityProvider.singleSignOnServices().stream().anyMatch(
                    endpoint -> SamlBinding.HTTP_REDIRECT.equals(endpoint.binding())
                            && signOn.equals(endpoint.location()));
            final boolean logoutDeclared = logout == null || identityProvider.singleLogoutServices().stream().anyMatch(
                    endpoint -> SamlBinding.HTTP_REDIRECT.equals(endpoint.binding())
                            && logout.equals(endpoint.location()));
            if (signOnDeclared && logoutDeclared) {
                return true;
            }
        }
        return false;
    }

}
