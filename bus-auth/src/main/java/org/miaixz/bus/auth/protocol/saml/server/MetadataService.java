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
package org.miaixz.bus.auth.protocol.saml.server;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.protocol.saml.*;
import org.miaixz.bus.auth.protocol.saml.codec.MetadataCodec;
import org.miaixz.bus.auth.resolver.CertificateResolver;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Publishes the SAML 2.0 Metadata entity descriptor for one identity Provider.
 * <p>
 * Certificate inventory is resolved externally on every publication operation so key rotation is reflected without
 * rebuilding the Provider. The Metadata codec converts the trusted Bus certificate chain into standard
 * {@code ds:KeyInfo}; this service never assembles certificate XML from strings.
 * </p>
 *
 * @author Kimi Liu
 */
public final class MetadataService {

    /**
     * Validated identity-provider settings.
     */
    private final SamlProviderSettings settings;

    /**
     * External certificate-chain and trust-root resolver.
     */
    private final CertificateResolver certificateResolver;

    /**
     * Strict SAML Metadata codec and KeyInfo encoder.
     */
    private final MetadataCodec metadataCodec;

    /**
     * Creates a Metadata publication service.
     *
     * @param settings            validated SAML Provider settings
     * @param certificateResolver external certificate material resolver
     * @param metadataCodec       strict SAML Metadata codec
     * @throws IllegalArgumentException if a collaborator is {@code null}
     */
    public MetadataService(final SamlProviderSettings settings, final CertificateResolver certificateResolver,
            final MetadataCodec metadataCodec) {
        this.settings = Assert.notNull(settings, "SAML Provider settings must not be null");
        this.certificateResolver = Assert.notNull(certificateResolver, "SAML certificate resolver must not be null");
        this.metadataCodec = Assert.notNull(metadataCodec, "SAML Metadata codec must not be null");
    }

    /**
     * Resolves the current signing certificate and creates a standard EntityDescriptor.
     *
     * @param context immutable invocation context
     * @param timeout shared end-to-end time budget
     * @return stage containing the current standard Metadata entity or a closed framework failure
     */
    public CompletionStage<Outcome<EntityDescriptor>> metadata(final Context context, final Timeout.Budget timeout) {
        Assert.notNull(context, "SAML Metadata publication context must not be null");
        Assert.notNull(timeout, "SAML Metadata publication time budget must not be null");
        if (timeout.expired()) {
            return CompletableFuture.completedFuture(
                    Outcome.failed(
                            new Outcome.Failure(ErrorCode._408,
                                    "SAML Metadata publication has no remaining time budget",
                                    new JsonValue.ObjectValue(Map.of()))));
        }
        final CertificateResolver.Query query = new CertificateResolver.Query(settings.entityId(), "signing",
                timeout.clock().now());
        return certificateResolver.resolve(query, context, timeout).thenApply(outcome -> switch (outcome) {
            case Outcome.Succeeded<CertificateResolver.ResolvedCertificate> success -> Outcome
                    .succeeded(descriptor(success.value()));
            case Outcome.Rejected<CertificateResolver.ResolvedCertificate> rejected -> Outcome
                    .rejected(rejected.failure());
            case Outcome.Failed<CertificateResolver.ResolvedCertificate> failed -> Outcome.failed(failed.failure());
        });
    }

    /**
     * Builds the standard identity-provider role and entity from resolved certificate material.
     *
     * @param certificate externally resolved certificate chain and trust roots
     * @return unsigned semantic EntityDescriptor for final Metadata encoding and signing
     */
    private EntityDescriptor descriptor(final CertificateResolver.ResolvedCertificate certificate) {
        final KeyDescriptor key = new KeyDescriptor(Optional.of(KeyDescriptor.Use.SIGNING),
                new KeyDescriptor.KeyInfo(metadataCodec.keyInfo(certificate.chain())), List.of());
        final SingleSignOnServiceEndpoint signOn = new SingleSignOnServiceEndpoint(settings.requestBinding(),
                settings.singleSignOnServiceEndpoint().url().toString(), Optional.empty(), List.of(), Map.of());
        final List<SingleLogoutServiceEndpoint> logout = settings.singleLogoutServiceEndpoint().isEmpty() ? List.of()
                : List.of(
                        new SingleLogoutServiceEndpoint(settings.requestBinding(),
                                settings.singleLogoutServiceEndpoint().getOrNull().url().toString(), Optional.empty(),
                                List.of(), Map.of()));
        final IdpSsoDescriptor identityProvider = new IdpSsoDescriptor(Optional.empty(), Optional.empty(),
                Optional.empty(), List.of(IdpSsoDescriptor.PROTOCOL), Optional.empty(), Optional.empty(), List.of(),
                List.of(key), Optional.empty(), List.of(), logout, List.of(), List.of(signOn),
                Optional.of(settings.wantAuthnRequestsSigned()));
        return new EntityDescriptor(settings.entityId(), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), List.of(), List.of(identityProvider), List.of(), Optional.empty(), List.of(),
                List.of(), Map.of());
    }

}
