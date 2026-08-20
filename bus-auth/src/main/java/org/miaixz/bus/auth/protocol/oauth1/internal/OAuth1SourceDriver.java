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
package org.miaixz.bus.auth.protocol.oauth1.internal;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.protocol.oauth1.ProtectedResourceRequest;
import org.miaixz.bus.auth.protocol.oauth1.ResourceOwnerAuthorizationRequest;
import org.miaixz.bus.auth.protocol.oauth1.TemporaryCredentialsRequest;
import org.miaixz.bus.auth.protocol.oauth1.TokenCredentialsRequest;
import org.miaixz.bus.auth.protocol.oauth1.client.OAuth1Client;
import org.miaixz.bus.auth.protocol.oauth1.client.OAuth1ClientSettings;
import org.miaixz.bus.auth.protocol.oauth1.client.OAuth1SourceProfile;
import org.miaixz.bus.auth.shared.ExecutionServices;
import org.miaixz.bus.auth.shared.internal.RuntimeProvider;
import org.miaixz.bus.auth.source.SourceDriver;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Compiles a validated generic OAuth 1.0 Source registration into an executable RFC 5849 client provider.
 *
 * @author Kimi Liu
 */
public final class OAuth1SourceDriver implements SourceDriver<OAuth1ClientSettings> {

    /**
     * Immutable generic OAuth 1.0 Source profile shared by compiled registrations.
     */
    private final OAuth1SourceProfile profile;

    /**
     * Creates a driver with the deterministic generic OAuth 1.0 profile.
     */
    public OAuth1SourceDriver() {
        this.profile = new OAuth1SourceProfile();
    }

    /**
     * Returns the OAuth 1.0 client profile bound to this driver.
     *
     * @return immutable OAuth 1.0 Source profile
     */
    @Override
    public OAuth1SourceProfile profile() {
        return profile;
    }

    /**
     * Consumes typed settings and compiles one validated complete Source registration.
     *
     * @param record   validated complete Source registration
     * @param provider resolved optional associated Provider
     * @param library  resolved owning Provider Library
     * @param services externally owned runtime dependencies
     * @return executable immutable OAuth 1.0 runtime provider
     */
    @Override
    public RuntimeProvider compile(
            final Registration.Record<Source> record,
            final Provider provider,
            final Library library,
            final ExecutionServices services) {
        Assert.notNull(record, "OAuth 1.0 registration record must not be null");
        Assert.notNull(provider, "OAuth 1.0 Source Provider container must not be null");
        Assert.notNull(library, "OAuth 1.0 Source Library container must not be null");
        Assert.notNull(services, "OAuth 1.0 execution services must not be null");
        final Source source = record.resource();
        if (!profile().id().equals(source.getType()) || !supports(source.getProtocol())
                || source.getNamespace_id() == null || source.getNamespace_id().isBlank()
                || !provider.getId().equals(source.getProvider_id())
                || !library.getId().equals(provider.getLibrary_id())) {
            throw new ValidateException("OAuth 1.0 driver requires a matching Source registration");
        }
        final OAuth1ClientSettings settings = decode(source);
        final OAuth1Client client = new OAuth1Client(source.getNamespace_id(), source.getId(), settings, services);
        return new OAuth1RuntimeProvider(profile, client);
    }

    /**
     * Routes only the four exact RFC 5849 client capabilities implemented by the compiled Source.
     *
     * @author Kimi Liu
     */
    private static final class OAuth1RuntimeProvider implements RuntimeProvider {

        /**
         * Exact immutable profile whose manifest controls routing.
         */
        private final OAuth1SourceProfile profile;

        /**
         * Compiled OAuth 1.0 client bound to one Source.
         */
        private final OAuth1Client client;

        /**
         * Creates an executable provider from a frozen profile and client.
         *
         * @param profile immutable OAuth 1.0 Source profile
         * @param client  compiled OAuth 1.0 client
         */
        private OAuth1RuntimeProvider(final OAuth1SourceProfile profile, final OAuth1Client client) {
            this.profile = Assert.notNull(profile, "OAuth 1.0 Source profile must not be null");
            this.client = Assert.notNull(client, "OAuth 1.0 client must not be null");
        }

        /**
         * Narrows a known delegated outcome through the capability's exact response class.
         *
         * @param stage        delegated outcome stage
         * @param responseType exact capability response class
         * @param <S>          required response type
         * @return type-safe delegated outcome
         */
        private static <S> CompletionStage<Outcome<S>> narrow(
                final CompletionStage<? extends Outcome<?>> stage,
                final Class<S> responseType) {
            return stage.thenApply(outcome -> switch (outcome) {
                case Outcome.Succeeded<?> success -> Outcome.succeeded(responseType.cast(success.value()));
                case Outcome.Rejected<?> rejected -> Outcome.rejected(rejected.failure());
                case Outcome.Failed<?> failed -> Outcome.failed(failed.failure());
            });
        }

        /**
         * Returns the four exact OAuth 1.0 client capabilities.
         *
         * @return immutable profile manifest
         */
        @Override
        public Capability.Manifest manifest() {
            return profile.manifest();
        }

        /**
         * Invokes one exact declared OAuth 1.0 client capability.
         *
         * @param capability exact declared capability
         * @param request    exact request instance
         * @param context    immutable invocation context
         * @param timeout    shared end-to-end time budget
         * @param <Q>        request type
         * @param <S>        success type
         * @return delegated client outcome or a rejected unsupported capability
         */
        @Override
        public <Q, S> CompletionStage<Outcome<S>> invoke(
                final Capability<Q, S> capability,
                final Q request,
                final Context context,
                final Timeout.Budget timeout) {
            Assert.notNull(capability, "OAuth 1.0 capability must not be null");
            Assert.notNull(request, "OAuth 1.0 capability request must not be null");
            Assert.notNull(context, "OAuth 1.0 invocation context must not be null");
            Assert.notNull(timeout, "OAuth 1.0 time budget must not be null");
            if (capability.equals(OAuth1SourceProfile.TEMPORARY_CREDENTIALS)) {
                return narrow(
                        client.temporaryCredentials(TemporaryCredentialsRequest.class.cast(request), context, timeout),
                        capability.responseType());
            }
            if (capability.equals(OAuth1SourceProfile.RESOURCE_OWNER_AUTHORIZATION)) {
                return narrow(
                        client.authorize(ResourceOwnerAuthorizationRequest.class.cast(request), context, timeout),
                        capability.responseType());
            }
            if (capability.equals(OAuth1SourceProfile.TOKEN_CREDENTIALS)) {
                return narrow(
                        client.tokenCredentials(TokenCredentialsRequest.class.cast(request), context, timeout),
                        capability.responseType());
            }
            if (capability.equals(OAuth1SourceProfile.PROTECTED_RESOURCE)) {
                return narrow(
                        client.access(ProtectedResourceRequest.class.cast(request), context, timeout),
                        capability.responseType());
            }
            return CompletableFuture.completedFuture(
                    Outcome.rejected(
                            new Outcome.Failure(ErrorCode._400,
                                    "OAuth 1.0 Source does not implement the requested capability",
                                    new JsonValue.ObjectValue(Map.of()))));
        }

    }

}
