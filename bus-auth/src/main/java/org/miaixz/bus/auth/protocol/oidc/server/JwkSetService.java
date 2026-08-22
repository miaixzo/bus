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
package org.miaixz.bus.auth.protocol.oidc.server;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.shared.jose.Jwk;
import org.miaixz.bus.auth.shared.jose.JwkSet;
import org.miaixz.bus.auth.source.DriverServices;
import org.miaixz.bus.auth.worker.loader.KeyLoader;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Publishes the validated public signing-key view of an OpenID Provider's external key inventory.
 * <p>
 * The external {@link KeyLoader} remains the single source for both execution and publication keys. This service never
 * derives a JWK from a JCA execution key and fails closed if a loader attempts to publish symmetric, private, unknown,
 * ambiguously identified, or non-advertised signing material.
 * </p>
 *
 * @author Kimi Liu
 */
public class JwkSetService {

    /**
     * Standard public-key use value for signature keys.
     */
    private static final String SIGNATURE_USE = Builder.SIGNATURE;

    /**
     * Frozen OpenID Provider signing options.
     */
    private final OpenIdServerOptions options;

    /**
     * External key inventory and shared clock dependencies.
     */
    private final DriverServices services;

    /**
     * Creates a public JWK Set service for one compiled OpenID Provider.
     *
     * @param options  validated OpenID Provider options
     * @param services externally implemented runtime dependencies
     * @throws IllegalArgumentException if a collaborator is {@code null}
     */
    public JwkSetService(final OpenIdServerOptions options, final DriverServices services) {
        this.options = Assert.notNull(options, "OpenID Connect JWK Set options must not be null");
        this.services = Assert.notNull(services, "OpenID Connect JWK Set execution services must not be null");
    }

    /**
     * Creates a safe key-publication failure without retaining loader details or key material.
     *
     * @param error       shared Bus error definition
     * @param description non-sensitive diagnostic description
     * @return immutable safe failure
     */
    private static Outcome.Failure failure(final Errors error, final String description) {
        return new Outcome.Failure(error, description, new JsonValue.ObjectValue(Map.of()));
    }

    /**
     * Creates an already completed JWK Set outcome stage.
     *
     * @param outcome completed outcome
     * @return completed stage
     */
    private static CompletionStage<Outcome<JwkSet>> completed(final Outcome<JwkSet> outcome) {
        return CompletableFuture.completedFuture(outcome);
    }

    /**
     * Resolves and validates the Provider's current public signing keys.
     *
     * @param context immutable invocation context
     * @param timeout shared end-to-end operation timeout
     * @return stage containing a public-only standard JWK Set or a closed failure
     */
    public CompletionStage<Outcome<JwkSet>> jwks(final Context context, final Timeout timeout) {
        Assert.notNull(context, "OpenID Connect JWK Set context must not be null");
        Assert.notNull(timeout, "OpenID Connect JWK Set timeout must not be null");
        if (timeout.expired()) {
            return completed(
                    Outcome.failed(failure(ErrorCode._408, "OpenID Connect JWK Set request has no remaining timeout")));
        }
        final Instant now = timeout.clock().now();
        final CompletionStage<Outcome<JwkSet>> resolution;
        try {
            final KeyLoader.Criteria criteria = new KeyLoader.Criteria(services.registration(), options.issuer(),
                    SIGNATURE_USE, now);
            resolution = Outcome.mapStage(
                    () -> services.keyLoader().list(criteria, context, timeout),
                    listing -> services.keyParser().parsePublic(services.registration(), criteria, listing));
        } catch (RuntimeException exception) {
            return completed(Outcome.failed(failure(ErrorCode._500, "OpenID Connect public key resolution failed")));
        }
        return resolution
                .handle(
                        (outcome, thrown) -> thrown == null && outcome != null ? outcome
                                : Outcome.<JwkSet>failed(
                                        failure(ErrorCode._500, "OpenID Connect public key resolution failed")))
                .thenApply(this::validate);
    }

    /**
     * Preserves loader outcome classification while validating a successful public JWK Set.
     *
     * @param outcome external loading and parsing outcome
     * @return sanitized and validated public JWK Set outcome
     */
    private Outcome<JwkSet> validate(final Outcome<JwkSet> outcome) {
        return switch (outcome) {
            case Outcome.Succeeded<JwkSet> success -> validateSet(success.value());
            case Outcome.Rejected<JwkSet> rejected -> Outcome.rejected(
                    failure(rejected.failure().error(), "OpenID Connect public signing keys are unavailable"));
            case Outcome.Failed<JwkSet> failed -> Outcome
                    .failed(failure(failed.failure().error(), "OpenID Connect public key resolution failed"));
            default -> throw new IllegalStateException("Unsupported Outcome implementation");
        };
    }

    /**
     * Validates every published key and requires one unambiguous configured ID Token signing key.
     *
     * @param source loader-supplied JWK Set
     * @return public-only detached JWK Set outcome
     */
    private Outcome<JwkSet> validateSet(final JwkSet source) {
        if (source == null || source.keys().isEmpty()) {
            return Outcome.failed(failure(ErrorCode._500, "OpenID Connect public signing key set is empty"));
        }
        try {
            final List<Jwk> publicKeys = new ArrayList<>(source.keys().size());
            int configuredMatches = 0;
            for (Jwk key : source.keys()) {
                if (key == null || key.hasPrivateMaterial() || "oct".equals(key.keyType())) {
                    throw new ValidateException("OpenID Connect JWK Set contains non-public key material");
                }
                final String use = key.publicKeyUse().orElse(SIGNATURE_USE);
                final String algorithm = key.algorithm().orElseThrow(
                        () -> new ValidateException("OpenID Connect published signing key must declare alg"));
                if (!SIGNATURE_USE.equals(use) || !options.idTokenSigningAlgorithm().name().equals(algorithm)) {
                    throw new ValidateException("OpenID Connect JWK Set contains a non-advertised signing key");
                }
                if (!key.keyOperations().isEmpty()
                        && (key.keyOperations().size() != 1 || !key.keyOperations().contains(Builder.VERIFY))) {
                    throw new ValidateException("OpenID Connect public JWK key_ops must contain only verify");
                }
                final Jwk publicKey = key.publicOnly();
                publicKeys.add(publicKey);
                if (publicKey.keyId().filter(options.idTokenSigningKeyId()::equals).isPresent()) {
                    configuredMatches++;
                }
            }
            if (configuredMatches != 1) {
                throw new ValidateException(
                        "OpenID Connect configured ID Token signing key must have exactly one public JWK");
            }
            return Outcome.succeeded(new JwkSet(publicKeys, source.extensions()));
        } catch (RuntimeException exception) {
            return Outcome.failed(failure(ErrorCode._500, "OpenID Connect public signing key set failed validation"));
        }
    }

}
