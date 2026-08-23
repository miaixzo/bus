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
package org.miaixz.bus.auth.source.protocol.oidc.server;

import java.util.Base64;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.crypto.Mac;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Subject;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.resolver.ConsumerMetadata;
import org.miaixz.bus.auth.resolver.KeyMaterial;
import org.miaixz.bus.auth.shared.jose.JwaAlgorithm;
import org.miaixz.bus.auth.source.DriverServices;
import org.miaixz.bus.auth.source.protocol.oidc.SubjectType;
import org.miaixz.bus.auth.worker.loader.KeyLoader;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Generates the final OpenID Connect public or pairwise subject identifier for one Consumer.
 *
 * @author Kimi Liu
 */
public class SubjectIssuer {

    /**
     * Registered key-use value for pairwise subject derivation keys.
     */
    private static final String PAIRWISE_USE = "pairwise-sub";

    /**
     * Exact Source identifier used to isolate subject derivation.
     */
    private final String sourceId;
    /**
     * Frozen OpenID Provider options.
     */
    private final OpenIdServerOptions options;
    /**
     * Source-scoped key loader and parser services.
     */
    private final DriverServices services;

    /**
     * Creates a Source-scoped subject issuer.
     *
     * @param sourceId exact Source identifier
     * @param options  frozen OpenID Provider options
     * @param services Source-scoped project loaders and framework services
     */
    public SubjectIssuer(final String sourceId, final OpenIdServerOptions options, final DriverServices services) {
        this.sourceId = Assert.notBlank(sourceId, "OpenID Connect Source id must not be blank");
        this.options = Assert.notNull(options, "OpenID Provider options must not be null");
        this.services = Assert.notNull(services, "OpenID Connect Driver services must not be null");
    }

    /**
     * Creates a safe operational subject-issuance failure.
     *
     * @param description safe failure description
     * @return immutable failure value
     */
    private static Outcome.Failure failure(final String description) {
        return new Outcome.Failure(ErrorCode._500, description, new JsonValue.ObjectValue(Map.of()));
    }

    /**
     * Wraps an outcome in an already completed stage.
     *
     * @param <T>     outcome value type
     * @param outcome outcome to expose
     * @return completed outcome stage
     */
    private static <T> CompletionStage<Outcome<T>> completed(final Outcome<T> outcome) {
        return CompletableFuture.completedFuture(outcome);
    }

    /**
     * Issues a stable wire subject according to the Consumer-selected subject type.
     *
     * @param subject  internal stable subject key
     * @param consumer relying-party consumer metadata
     * @param context  immutable invocation context
     * @param timeout  shared operation timeout
     * @return stage resolving to the public or pairwise wire subject
     */
    public CompletionStage<Outcome<String>> issue(
            final Subject.Key subject,
            final ConsumerMetadata consumer,
            final Context context,
            final Timeout timeout) {
        Assert.notNull(subject, "OpenID Connect internal subject must not be null");
        Assert.notNull(consumer, "OpenID Connect Consumer must not be null");
        Assert.notNull(context, "OpenID Connect subject context must not be null");
        Assert.notNull(timeout, "OpenID Connect subject timeout must not be null");
        final String registeredType = consumer.subjectType().getOrNull();
        if (registeredType == null) {
            return completed(Outcome.rejected(failure("Consumer subject type is required by OpenID Connect")));
        }
        final SubjectType subjectType;
        try {
            subjectType = new SubjectType(registeredType);
        } catch (RuntimeException cause) {
            return completed(Outcome.rejected(failure("Consumer subject type is invalid")));
        }
        if (!options.subjectTypesSupported().contains(subjectType)) {
            return completed(Outcome.rejected(failure("Consumer subject type is disabled by the OpenID Provider")));
        }
        if (SubjectType.PUBLIC.equals(subjectType)) {
            if (consumer.sectorIdentifier().isPresent()) {
                return completed(Outcome.rejected(failure("Public subject type must not declare a sector identifier")));
            }
            return completed(Outcome.succeeded(subject.value()));
        }
        final OpenIdServerOptions.PairwisePolicy policy = options.pairwisePolicy().getOrNull();
        final String sector = consumer.sectorIdentifier().getOrNull();
        if (!SubjectType.PAIRWISE.equals(subjectType) || policy == null || sector == null) {
            return completed(Outcome.failed(failure("Pairwise subject policy is incomplete")));
        }
        final KeyLoader.Request request = new KeyLoader.Request(services.entry(), options.issuer(),
                Optional.of(policy.keyId()), PAIRWISE_USE, JwaAlgorithm.HS256.name(), timeout.clock().now());
        final CompletionStage<Outcome<KeyLoader.Record>> loading;
        try {
            loading = services.keyLoader().load(request, context, timeout);
        } catch (RuntimeException cause) {
            return completed(Outcome.failed(failure("Pairwise subject key loading failed")));
        }
        if (loading == null) {
            return completed(Outcome.failed(failure("Pairwise subject key loader returned no stage")));
        }
        return loading.handle((outcome, cause) -> cause == null ? outcome : null).thenApply(outcome -> {
            if (!(outcome instanceof Outcome.Succeeded<KeyLoader.Record> success)) {
                return Outcome.<String>failed(failure("Pairwise subject key loading failed"));
            }
            try {
                final KeyMaterial material = services.keyParser().parse(services.entry(), request, success.value());
                final Mac mac = Mac.getInstance("HmacSHA256");
                mac.init(material.key());
                final String input = options.issuer() + Symbol.C_NUL + sector + Symbol.C_NUL + subject.value();
                return Outcome.succeeded(
                        Base64.getUrlEncoder().withoutPadding()
                                .encodeToString(mac.doFinal(input.getBytes(Charset.UTF_8))));
            } catch (Exception cause) {
                return Outcome.<String>failed(failure("Pairwise subject generation failed"));
            }
        });
    }

    /**
     * Returns a redacted diagnostic description without exposing pairwise subject material.
     *
     * @return redacted sector description
     */
    @Override
    public String toString() {
        return "SubjectIssuer[sourceId=" + sourceId + ']';
    }

}
