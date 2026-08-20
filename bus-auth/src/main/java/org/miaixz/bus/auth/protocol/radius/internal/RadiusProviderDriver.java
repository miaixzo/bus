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
package org.miaixz.bus.auth.protocol.radius.internal;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.protocol.radius.AccessRequest;
import org.miaixz.bus.auth.protocol.radius.AccountingRequest;
import org.miaixz.bus.auth.protocol.radius.RadiusPacket;
import org.miaixz.bus.auth.protocol.radius.codec.EapMessageCodec;
import org.miaixz.bus.auth.protocol.radius.codec.RadiusAttributeCodec;
import org.miaixz.bus.auth.protocol.radius.codec.RadiusPacketEncoder;
import org.miaixz.bus.auth.protocol.radius.server.*;
import org.miaixz.bus.auth.provider.ProviderDriver;
import org.miaixz.bus.auth.shared.ExecutionServices;
import org.miaixz.bus.auth.shared.internal.RuntimeProvider;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Compiles one server-role RADIUS Source registration into exact Access and Accounting server operations.
 * <p>
 * Transport, ALPN, client data, user policy, and accounting persistence remain external. The compiled runtime owns only
 * standard packet codecs, hop-by-hop security, and closed Registry capability routing.
 * </p>
 *
 * @author Kimi Liu
 */
public final class RadiusProviderDriver implements ProviderDriver<RadiusProviderSettings> {

    /**
     * Creates the stateless RADIUS Provider driver.
     */
    public RadiusProviderDriver() {
        // No initialization required.
    }

    /**
     * Returns the RADIUS server profile bound to this driver.
     *
     * @return immutable RADIUS Provider profile
     */
    @Override
    public RadiusProviderProfile profile() {
        return new RadiusProviderProfile();
    }

    /**
     * Consumes typed settings, resolves the exact handler binding, and assembles both RADIUS operations.
     *
     * @param record   validated complete server-role Source registration
     * @param library  resolved Library owned by the Provider
     * @param services externally owned runtime dependencies
     * @return immutable executable RADIUS server-role Source runtime
     * @throws IllegalArgumentException if an argument is {@code null}
     * @throws ValidateException        if registration, settings, baseline, or binding validation fails
     */
    @Override
    public RuntimeProvider compile(
            final Registration.Record<Source> record,
            final Provider provider,
            final Library library,
            final ExecutionServices services) {
        Assert.notNull(record, "RADIUS Provider registration must not be null");
        Assert.notNull(library, "RADIUS Provider Library must not be null");
        Assert.notNull(services, "RADIUS Provider execution services must not be null");
        final Source source = record.resource();
        if (!profile().id().equals(source.getType()) || !supports(source.getProtocol())
                || !provider.getId().equals(source.getProvider_id())
                || !library.getId().equals(provider.getLibrary_id())) {
            throw new ValidateException("RADIUS server driver requires a matching Source registration");
        }
        final RadiusProviderSettings settings = decode(source);
        if (settings.maximumPacketBytes() > services.securityBaseline().require(Protocol.RADIUS)
                .maximumMessageBytes()) {
            throw new ValidateException("RADIUS Provider packet limit exceeds the shared security baseline");
        }
        final RadiusRequestHandler handler = Assert.notNull(
                services.bindingResolver().resolve(record, RadiusRequestHandler.class),
                "RADIUS external request handler binding must not be null");
        final RadiusAttributeCodec attributes = new RadiusAttributeCodec();
        final RadiusPacketEncoder encoder = new RadiusPacketEncoder(settings.maximumPacketBytes(),
                settings.versions().contains(RadiusPacket.Version.RADIUS_1_1), attributes);
        final RadiusAuthenticator authenticator = new RadiusAuthenticator(encoder);
        final AccessService access = new AccessService(source.getId(), settings, handler, services.secretResolver(),
                authenticator, new EapMessageCodec());
        final AccountingService accounting = new AccountingService(source.getId(), settings, handler,
                services.secretResolver(), authenticator);
        return new CompiledProvider(new RadiusProviderProfile().manifest(), access, accounting);
    }

    /**
     * Routes the exact RADIUS capability manifest to its two immutable services.
     *
     * @author Kimi Liu
     */
    private static final class CompiledProvider implements RuntimeProvider {

        /**
         * Exact two-operation RADIUS Provider manifest.
         */
        private final Capability.Manifest manifest;

        /**
         * Access operation service.
         */
        private final AccessService access;

        /**
         * Accounting operation service.
         */
        private final AccountingService accounting;

        /**
         * Creates one fully routed immutable RADIUS runtime.
         *
         * @param manifest   exact capability manifest
         * @param access     Access service
         * @param accounting Accounting service
         */
        private CompiledProvider(final Capability.Manifest manifest, final AccessService access,
                final AccountingService accounting) {
            this.manifest = Assert.notNull(manifest, "RADIUS Provider manifest must not be null");
            this.access = Assert.notNull(access, "RADIUS Access service must not be null");
            this.accounting = Assert.notNull(accounting, "RADIUS Accounting service must not be null");
        }

        /**
         * Narrows a delegated outcome through the exact declared response class.
         *
         * @param stage        delegated operation stage
         * @param responseType exact capability response type
         * @param <S>          expected success type
         * @return type-safe delegated outcome
         */
        private static <S> CompletionStage<Outcome<S>> narrow(
                final CompletionStage<? extends Outcome<?>> stage,
                final Class<S> responseType) {
            return stage.thenApply(outcome -> switch (outcome) {
                case Outcome.Succeeded<?> success -> Outcome
                        .succeeded(success.value() == null ? null : responseType.cast(success.value()));
                case Outcome.Rejected<?> rejected -> Outcome.rejected(rejected.failure());
                case Outcome.Failed<?> failed -> Outcome.failed(failed.failure());
            });
        }

        /**
         * Returns a safe rejection for an undeclared capability.
         *
         * @param <S> expected success type
         * @return completed not-found outcome
         */
        private static <S> CompletionStage<Outcome<S>> missing() {
            return completed(
                    Outcome.rejected(
                            new Outcome.Failure(ErrorCode._404, "RADIUS Provider capability is not available",
                                    new JsonValue.ObjectValue(Map.of()))));
        }

        /**
         * Returns a safe rejection for a request or response type mismatch.
         *
         * @param <S> expected success type
         * @return completed bad-request outcome
         */
        private static <S> CompletionStage<Outcome<S>> mismatch() {
            return completed(
                    Outcome.rejected(
                            new Outcome.Failure(ErrorCode._400,
                                    "RADIUS Provider capability type does not match the request",
                                    new JsonValue.ObjectValue(Map.of()))));
        }

        /**
         * Creates an already completed stage.
         *
         * @param outcome completed outcome
         * @param <T>     success type
         * @return completed stage
         */
        private static <T> CompletionStage<Outcome<T>> completed(final Outcome<T> outcome) {
            return CompletableFuture.completedFuture(outcome);
        }

        /**
         * Returns the exact RADIUS Provider manifest.
         *
         * @return immutable capability manifest
         */
        @Override
        public Capability.Manifest manifest() {
            return manifest;
        }

        /**
         * Routes one exact RADIUS capability to Access or Accounting.
         *
         * @param capability exact declared capability
         * @param request    exact complete packet request
         * @param context    immutable invocation context
         * @param timeout    shared end-to-end budget
         * @param <Q>        declared request type
         * @param <S>        declared success type
         * @return delegated typed outcome or a closed 400/404 rejection
         */
        @Override
        public <Q, S> CompletionStage<Outcome<S>> invoke(
                final Capability<Q, S> capability,
                final Q request,
                final Context context,
                final Timeout.Budget timeout) {
            Assert.notNull(capability, "RADIUS Provider capability must not be null");
            Assert.notNull(context, "RADIUS Provider context must not be null");
            Assert.notNull(timeout, "RADIUS Provider time budget must not be null");
            if (!manifest.capabilities().contains(capability)) {
                return missing();
            }
            final CompletionStage<? extends Outcome<?>> stage;
            if (capability == RadiusProviderProfile.ACCESS && request instanceof AccessRequest accessRequest) {
                stage = access.access(accessRequest, context, timeout);
            } else if (capability == RadiusProviderProfile.ACCOUNTING
                    && request instanceof AccountingRequest accountingRequest) {
                stage = accounting.accounting(accountingRequest, context, timeout);
            } else {
                return mismatch();
            }
            return narrow(stage, capability.responseType());
        }

    }

}
