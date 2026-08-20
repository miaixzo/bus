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
package org.miaixz.bus.auth.protocol.ldap;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.protocol.ldap.server.*;
import org.miaixz.bus.auth.runtime.ExecutionServices;
import org.miaixz.bus.auth.source.SourceDriver;
import org.miaixz.bus.auth.worker.SourceWorker;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Compiles one server-role LDAPv3 Source registration into ten complete-message server operations.
 * <p>
 * Directory data and connection authentication state remain in the exact external {@link DirectoryStore} binding. The
 * compiled runtime only validates Registry routing and dispatches standard LDAPMessage models; it owns no socket,
 * global connection map, persistence implementation, or reverse Registry dependency.
 * </p>
 *
 * @author Kimi Liu
 */
public final class LdapProviderDriver implements SourceDriver<LdapServerOptions> {

    /**
     * Creates the stateless LDAP Provider driver.
     */
    public LdapProviderDriver() {
        // No initialization required.
    }

    /**
     * Returns the LDAP server scheme bound to this driver.
     *
     * @return immutable LDAP Provider scheme
     */
    @Override
    public LdapServerScheme scheme() {
        return new LdapServerScheme();
    }

    @Override
    public LdapServerOptions require(final Options<?> options) {
        if (options instanceof LdapServerOptions value) {
            return value;
        }
        throw new ValidateException("LDAP server driver requires LdapServerOptions");
    }

    /**
     * Validates typed options, enforces the shared message baseline, resolves DirectoryStore, and assembles all
     * services.
     *
     * @param record   validated complete server-role Source registration
     * @param library  resolved Library owned by the Provider
     * @param services externally owned runtime dependencies
     * @return immutable executable LDAP server-role Source runtime
     * @throws IllegalArgumentException if an argument is {@code null}
     * @throws ValidateException        if registration, options, baseline, or binding validation fails
     */
    @Override
    public SourceWorker compile(
            final Registration.Record<Source> record,
            final Provider provider,
            final Library library,
            final ExecutionServices services) {
        Assert.notNull(record, "LDAP Provider registration must not be null");
        Assert.notNull(library, "LDAP Provider Library must not be null");
        Assert.notNull(services, "LDAP Provider execution services must not be null");
        final Source source = record.resource();
        if (!scheme().id().equals(source.getType()) || !supports(source.getProtocol())
                || !provider.getId().equals(source.getProvider_id())
                || !library.getId().equals(provider.getLibrary_id())) {
            throw new ValidateException("LDAP server driver requires a matching Source registration");
        }
        final LdapServerOptions options = require(source.getOptions());
        if (options.maximumMessageBytes() > services.securityBaseline().require(Protocol.LDAP).maximumMessageBytes()) {
            throw new ValidateException("LDAP Provider message limit exceeds the shared security baseline");
        }
        final DirectoryStore store = Assert.notNull(
                services.bindingParser().parse(
                        record,
                        DirectoryStore.class,
                        services.bindingLoader().load(record, DirectoryStore.class)),
                "LDAP external DirectoryStore binding must not be null");
        return new CompiledProvider(new LdapServerScheme().manifest(), new BindService(source.getId(), options, store),
                new UnbindService(source.getId(), options, store), new SearchService(source.getId(), options, store),
                new ModifyService(source.getId(), options, store), new AddService(source.getId(), options, store),
                new DeleteService(source.getId(), options, store), new ModifyDNService(source.getId(), options, store),
                new CompareService(source.getId(), options, store), new AbandonService(source.getId(), options, store),
                new ExtendedOperationService(source.getId(), options, store));
    }

    /**
     * Routes exact LDAP capabilities to immutable operation services.
     *
     * @author Kimi Liu
     */
    private static final class CompiledProvider implements SourceWorker {

        /**
         * Exact ten-operation LDAP Provider manifest.
         */
        private final Capability.Manifest manifest;

        /**
         * Bind operation service.
         */
        private final BindService bind;

        /**
         * Unbind operation service.
         */
        private final UnbindService unbind;

        /**
         * Search operation service.
         */
        private final SearchService search;

        /**
         * Modify operation service.
         */
        private final ModifyService modify;

        /**
         * Add operation service.
         */
        private final AddService add;

        /**
         * Delete operation service.
         */
        private final DeleteService delete;

        /**
         * Modify DN operation service.
         */
        private final ModifyDNService modifyDn;

        /**
         * Compare operation service.
         */
        private final CompareService compare;

        /**
         * Abandon operation service.
         */
        private final AbandonService abandon;

        /**
         * Extended operation service.
         */
        private final ExtendedOperationService extended;

        /**
         * Creates one immutable fully routed LDAP server-role Source runtime.
         *
         * @param manifest exact capability manifest
         * @param bind     Bind service
         * @param unbind   Unbind service
         * @param search   Search service
         * @param modify   Modify service
         * @param add      Add service
         * @param delete   Delete service
         * @param modifyDn Modify DN service
         * @param compare  Compare service
         * @param abandon  Abandon service
         * @param extended Extended service
         */
        private CompiledProvider(final Capability.Manifest manifest, final BindService bind, final UnbindService unbind,
                final SearchService search, final ModifyService modify, final AddService add,
                final DeleteService delete, final ModifyDNService modifyDn, final CompareService compare,
                final AbandonService abandon, final ExtendedOperationService extended) {
            this.manifest = Assert.notNull(manifest, "LDAP Provider manifest must not be null");
            this.bind = Assert.notNull(bind, "LDAP Bind service must not be null");
            this.unbind = Assert.notNull(unbind, "LDAP Unbind service must not be null");
            this.search = Assert.notNull(search, "LDAP Search service must not be null");
            this.modify = Assert.notNull(modify, "LDAP Modify service must not be null");
            this.add = Assert.notNull(add, "LDAP Add service must not be null");
            this.delete = Assert.notNull(delete, "LDAP Delete service must not be null");
            this.modifyDn = Assert.notNull(modifyDn, "LDAP Modify DN service must not be null");
            this.compare = Assert.notNull(compare, "LDAP Compare service must not be null");
            this.abandon = Assert.notNull(abandon, "LDAP Abandon service must not be null");
            this.extended = Assert.notNull(extended, "LDAP Extended service must not be null");
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
                            new Outcome.Failure(ErrorCode._404, "LDAP Provider capability is not available",
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
                                    "LDAP Provider capability type does not match the request",
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
         * Returns the exact LDAP Provider manifest.
         *
         * @return immutable capability manifest
         */
        @Override
        public Capability.Manifest manifest() {
            return manifest;
        }

        /**
         * Routes one exact complete-message capability to its operation service.
         *
         * @param capability exact declared capability
         * @param request    complete LDAP request message
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
            Assert.notNull(capability, "LDAP Provider capability must not be null");
            Assert.notNull(context, "LDAP Provider context must not be null");
            Assert.notNull(timeout, "LDAP Provider time budget must not be null");
            if (!manifest.capabilities().contains(capability)) {
                return missing();
            }
            if (capability.requestType() != LdapMessage.class || !(request instanceof LdapMessage message)) {
                return mismatch();
            }
            final CompletionStage<? extends Outcome<?>> stage;
            if (capability == LdapServerScheme.BIND)
                stage = bind.bind(message, context, timeout);
            else if (capability == LdapServerScheme.UNBIND)
                stage = unbind.unbind(message, context, timeout);
            else if (capability == LdapServerScheme.SEARCH)
                stage = search.search(message, context, timeout);
            else if (capability == LdapServerScheme.MODIFY)
                stage = modify.modify(message, context, timeout);
            else if (capability == LdapServerScheme.ADD)
                stage = add.add(message, context, timeout);
            else if (capability == LdapServerScheme.DELETE)
                stage = delete.delete(message, context, timeout);
            else if (capability == LdapServerScheme.MODIFY_DN)
                stage = modifyDn.modifyDN(message, context, timeout);
            else if (capability == LdapServerScheme.COMPARE)
                stage = compare.compare(message, context, timeout);
            else if (capability == LdapServerScheme.ABANDON)
                stage = abandon.abandon(message, context, timeout);
            else if (capability == LdapServerScheme.EXTENDED)
                stage = extended.extended(message, context, timeout);
            else
                return missing();
            return narrow(stage, capability.responseType());
        }

    }

}
