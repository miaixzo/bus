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
package org.miaixz.bus.auth.protocol.radius;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.protocol.radius.codec.EapMessageCodec;
import org.miaixz.bus.auth.protocol.radius.codec.RadiusAttributeCodec;
import org.miaixz.bus.auth.protocol.radius.codec.RadiusPacketEncoder;
import org.miaixz.bus.auth.protocol.radius.server.*;
import org.miaixz.bus.auth.source.DriverServices;
import org.miaixz.bus.auth.source.SourceDriver;
import org.miaixz.bus.auth.worker.BindingResolver;
import org.miaixz.bus.auth.worker.SourceWorker;
import org.miaixz.bus.auth.worker.WorkerSlots;
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
public class RadiusServerDriver implements SourceDriver<RadiusServerOptions> {

    /**
     * Creates the stateless RADIUS Provider driver.
     */
    public RadiusServerDriver() {
        // No initialization required.
    }

    /**
     * Returns the RADIUS server scheme bound to this driver.
     *
     * @return immutable RADIUS Provider scheme
     */
    @Override
    public RadiusServerScheme scheme() {
        return new RadiusServerScheme();
    }

    @Override
    public RadiusServerOptions require(final Options<?> options) {
        if (options instanceof RadiusServerOptions value) {
            return value;
        }
        throw new ValidateException("RADIUS server driver requires RadiusServerOptions");
    }

    @Override
    public WorkerSlots slots(final Source source, final RadiusServerOptions options) {
        return WorkerSlots.of(WorkerSlots.Slot.BINDING, WorkerSlots.Slot.SECRET);
    }

    @Override
    public Dependencies dependencies(final Source source, final RadiusServerOptions options) {
        return Dependencies.of(Dependencies.Service.SECURITY_BASELINE);
    }

    /**
     * Consumes typed options, resolves the exact handler binding, and assembles both RADIUS operations.
     *
     * @param prepared one-time validated Source graph, Options and dependency declaration
     * @param services dependency-scoped runtime services
     * @return immutable executable RADIUS server-role Source runtime
     * @throws IllegalArgumentException if an argument is {@code null}
     * @throws ValidateException        if registration, options, baseline, or binding validation fails
     */
    @Override
    public SourceWorker compile(final Prepared<RadiusServerOptions> prepared, final DriverServices services) {
        Assert.notNull(prepared, "RADIUS Provider preparation must not be null");
        Assert.notNull(services, "RADIUS Provider execution services must not be null");
        final Blueprint.SourceEntry record = prepared.registration();
        final Provider provider = prepared.provider();
        final Library library = prepared.library();
        final Source source = record.resource();
        if (!scheme().id().equals(source.getType()) || !supports(source.getProtocol())
                || !provider.getId().equals(source.getProvider_id())
                || !library.getId().equals(provider.getLibrary_id())) {
            throw new ValidateException("RADIUS server driver requires a matching Source registration");
        }
        final RadiusServerOptions options = prepared.options();
        if (options.maximumPacketBytes() > services.securityBaseline().require(Protocol.RADIUS).maximumMessageBytes()) {
            throw new ValidateException("RADIUS Provider packet limit exceeds the shared security baseline");
        }
        final BindingResolver.Key<RadiusRequestHandler> binding = new BindingResolver.Key<>("radius-request",
                RadiusRequestHandler.class);
        final RadiusRequestHandler handler = Assert.notNull(
                binding.require(services.bindingResolver().resolve(record, binding)),
                "RADIUS external request handler binding must not be null");
        final RadiusAttributeCodec attributes = new RadiusAttributeCodec();
        final RadiusPacketEncoder encoder = new RadiusPacketEncoder(options.maximumPacketBytes(),
                options.versions().contains(RadiusPacket.Version.RADIUS_1_1), attributes);
        final RadiusAuthenticator authenticator = new RadiusAuthenticator(encoder);
        final AccessService access = new AccessService(source.getId(), options, handler, services, authenticator,
                new EapMessageCodec());
        final AccountingService accounting = new AccountingService(source.getId(), options, handler, services,
                authenticator);
        return new CompiledServer(new RadiusServerScheme().manifest(), access, accounting);
    }

    /**
     * Routes the exact RADIUS capability manifest to its two immutable services.
     *
     * @author Kimi Liu
     */
    private static final class CompiledServer implements SourceWorker {

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
        private CompiledServer(final Capability.Manifest manifest, final AccessService access,
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
                default -> throw new IllegalStateException("Unsupported Outcome implementation");
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
         * @param timeout    shared end-to-end timeout
         * @param <Q>        declared request type
         * @param <S>        declared success type
         * @return delegated typed outcome or a closed 400/404 rejection
         */
        @Override
        public <Q, S> CompletionStage<Outcome<S>> invoke(
                final Capability<Q, S> capability,
                final Q request,
                final Context context,
                final Timeout timeout) {
            Assert.notNull(capability, "RADIUS Provider capability must not be null");
            Assert.notNull(context, "RADIUS Provider context must not be null");
            Assert.notNull(timeout, "RADIUS Provider timeout must not be null");
            if (!manifest.capabilities().contains(capability)) {
                return missing();
            }
            final CompletionStage<? extends Outcome<?>> stage;
            if (capability == RadiusServerScheme.ACCESS && request instanceof AccessRequest accessRequest) {
                stage = access.access(accessRequest, context, timeout);
            } else if (capability == RadiusServerScheme.ACCOUNTING
                    && request instanceof AccountingRequest accountingRequest) {
                stage = accounting.accounting(accountingRequest, context, timeout);
            } else {
                return mismatch();
            }
            return narrow(stage, capability.responseType());
        }

    }

}
