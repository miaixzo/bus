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
import org.miaixz.bus.auth.resolver.CertificateMaterial;
import org.miaixz.bus.auth.source.DriverServices;
import org.miaixz.bus.auth.worker.CertificateLoader;
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
     * Validated identity-provider options.
     */
    private final SamlServerOptions options;

    /**
     * External certificate loader and framework-owned certificate parser.
     */
    private final DriverServices services;

    /**
     * Strict SAML Metadata codec and KeyInfo encoder.
     */
    private final MetadataCodec metadataCodec;

    /**
     * Creates a Metadata publication service.
     *
     * @param options       validated SAML Provider options
     * @param services      external loaders and pure parsers
     * @param metadataCodec strict SAML Metadata codec
     * @throws IllegalArgumentException if a collaborator is {@code null}
     */
    public MetadataService(final SamlServerOptions options, final DriverServices services,
            final MetadataCodec metadataCodec) {
        this.options = Assert.notNull(options, "SAML Provider options must not be null");
        this.services = Assert.notNull(services, "SAML execution services must not be null");
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
        final CertificateLoader.Request query = new CertificateLoader.Request(options.entityId(), "signing",
                timeout.clock().now());
        return Outcome.mapStage(
                        () -> services.certificateLoader().load(query, context, timeout),
                        loaded -> services.certificateParser().parse(query, loaded))
                .thenApply(outcome -> switch (outcome) {
                    case Outcome.Succeeded<CertificateMaterial> success -> Outcome
                            .succeeded(descriptor(success.value()));
                    case Outcome.Rejected<CertificateMaterial> rejected -> Outcome.rejected(rejected.failure());
                    case Outcome.Failed<CertificateMaterial> failed -> Outcome.failed(failed.failure());
                });
    }

    /**
     * Builds the standard identity-provider role and entity from resolved certificate material.
     *
     * @param certificate externally resolved certificate chain and trust roots
     * @return unsigned semantic EntityDescriptor for final Metadata encoding and signing
     */
    private EntityDescriptor descriptor(final CertificateMaterial certificate) {
        final KeyDescriptor key = new KeyDescriptor(Optional.of(KeyDescriptor.Use.SIGNING),
                new KeyDescriptor.KeyInfo(metadataCodec.keyInfo(certificate.chain())), List.of());
        final SingleSignOnServiceEndpoint signOn = new SingleSignOnServiceEndpoint(options.requestBinding(),
                options.singleSignOnServiceEndpoint().url().toString(), Optional.empty(), List.of(), Map.of());
        final List<SingleLogoutServiceEndpoint> logout = options.singleLogoutServiceEndpoint().isEmpty() ? List.of()
                : List.of(
                        new SingleLogoutServiceEndpoint(options.requestBinding(),
                                options.singleLogoutServiceEndpoint().getOrNull().url().toString(), Optional.empty(),
                                List.of(), Map.of()));
        final IdpSsoDescriptor identityProvider = new IdpSsoDescriptor(Optional.empty(), Optional.empty(),
                Optional.empty(), List.of(IdpSsoDescriptor.PROTOCOL), Optional.empty(), Optional.empty(), List.of(),
                List.of(key), Optional.empty(), List.of(), logout, List.of(), List.of(signOn),
                Optional.of(options.wantAuthnRequestsSigned()));
        return new EntityDescriptor(options.entityId(), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), List.of(), List.of(identityProvider), List.of(), Optional.empty(), List.of(),
                List.of(), Map.of());
    }

}
