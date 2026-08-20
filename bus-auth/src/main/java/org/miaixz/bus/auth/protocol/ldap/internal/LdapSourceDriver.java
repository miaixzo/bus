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
package org.miaixz.bus.auth.protocol.ldap.internal;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.protocol.ldap.*;
import org.miaixz.bus.auth.protocol.ldap.client.LdapClient;
import org.miaixz.bus.auth.protocol.ldap.client.LdapIdentityResolver;
import org.miaixz.bus.auth.protocol.ldap.client.LdapSourceProfile;
import org.miaixz.bus.auth.protocol.ldap.client.LdapSourceSettings;
import org.miaixz.bus.auth.protocol.ldap.codec.BerCodec;
import org.miaixz.bus.auth.protocol.ldap.codec.LdapMessageDecoder;
import org.miaixz.bus.auth.protocol.ldap.codec.LdapMessageEncoder;
import org.miaixz.bus.auth.shared.ExecutionServices;
import org.miaixz.bus.auth.shared.SecretLease;
import org.miaixz.bus.auth.shared.internal.RuntimeProvider;
import org.miaixz.bus.auth.source.*;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Compiles one LDAPv3 Source into a direct password authentication runtime using standard Bind and Search operations.
 * <p>
 * Every invocation owns one exclusive client connection. Service and user passwords are acquired as short-lived
 * SecretLease values, copied into immutable protocol requests, and erased before the asynchronous transport stage is
 * crossed. The runtime never pools bound LDAP sessions or parses a textual LDAP filter.
 * </p>
 *
 * @author Kimi Liu
 */
public final class LdapSourceDriver implements SourceDriver<LdapSourceSettings> {

    /**
     * Creates the stateless LDAP Source driver.
     */
    public LdapSourceDriver() {
        // No initialization required.
    }

    /**
     * Returns the LDAP client profile bound to this driver.
     *
     * @return immutable LDAP Source profile
     */
    @Override
    public LdapSourceProfile profile() {
        return new LdapSourceProfile();
    }

    /**
     * Validates typed settings, enforces the LDAP baseline, and assembles one direct-authentication runtime.
     *
     * @param record   validated complete Source registration
     * @param provider resolved optional associated Provider
     * @param library  resolved owning Provider Library
     * @param services externally owned runtime dependencies
     * @return immutable executable LDAP Source
     * @throws IllegalArgumentException if an argument is {@code null}
     * @throws ValidateException        if registration, settings, or baseline validation fails
     */
    @Override
    public RuntimeProvider compile(
            final Registration.Record<Source> record,
            final Provider provider,
            final Library library,
            final ExecutionServices services) {
        Assert.notNull(record, "LDAP Source registration must not be null");
        Assert.notNull(provider, "LDAP Source Provider container must not be null");
        Assert.notNull(library, "LDAP Source Library container must not be null");
        Assert.notNull(services, "LDAP Source execution services must not be null");
        final Source source = record.resource();
        if (!profile().id().equals(source.getType()) || !supports(source.getProtocol())
                || source.getNamespace_id() == null || source.getNamespace_id().isBlank()
                || !provider.getId().equals(source.getProvider_id())
                || !library.getId().equals(provider.getLibrary_id())) {
            throw new ValidateException("LDAP Source driver requires a matching Source registration");
        }
        final LdapSourceSettings settings = decode(source);
        if (settings.maximumMessageBytes() > services.securityBaseline().require(Protocol.LDAP).maximumMessageBytes()) {
            throw new ValidateException("LDAP Source message limit exceeds the shared security baseline");
        }
        final BerCodec frameCodec = new BerCodec(settings.maximumMessageBytes(), settings.maximumBerDepth());
        final LdapMessageEncoder encoder = new LdapMessageEncoder(settings.maximumMessageBytes(),
                settings.maximumBerDepth());
        final LdapMessageDecoder decoder = new LdapMessageDecoder(settings.maximumMessageBytes(),
                settings.maximumBerDepth());
        final LdapIdentityResolver identity = new LdapIdentityResolver(settings, services.fabricContext().clock());
        return new CompiledSource(source.getId(), settings, services, frameCodec, encoder, decoder, identity);
    }

    /**
     * Executes the single direct LDAP Source capability.
     *
     * @author Kimi Liu
     */
    private static final class CompiledSource implements RuntimeProvider {

        /**
         * Exact application-level Source capability manifest.
         */
        private static final Capability.Manifest MANIFEST = new Capability.Manifest(
                List.of(SourceAuthentication.INITIATE));

        /**
         * Registered Source identifier.
         */
        private final String sourceId;

        /**
         * Frozen directory connection and mapping settings.
         */
        private final LdapSourceSettings settings;

        /**
         * External Fabric, executor, resolver, and baseline dependencies.
         */
        private final ExecutionServices services;

        /**
         * LDAP stream frame codec template.
         */
        private final BerCodec frameCodec;

        /**
         * Complete LDAPMessage encoder.
         */
        private final LdapMessageEncoder encoder;

        /**
         * Complete LDAPMessage decoder.
         */
        private final LdapMessageDecoder decoder;

        /**
         * Verified Search entry identity mapper.
         */
        private final LdapIdentityResolver identity;

        /**
         * Creates one immutable compiled LDAP Source runtime.
         *
         * @param sourceId   registered Source identifier
         * @param settings   validated Source settings
         * @param services   external runtime dependencies
         * @param frameCodec LDAP stream frame codec template
         * @param encoder    LDAPMessage encoder
         * @param decoder    LDAPMessage decoder
         * @param identity   verified identity mapper
         */
        private CompiledSource(final String sourceId, final LdapSourceSettings settings,
                final ExecutionServices services, final BerCodec frameCodec, final LdapMessageEncoder encoder,
                final LdapMessageDecoder decoder, final LdapIdentityResolver identity) {
            this.sourceId = Assert.notBlank(sourceId, "LDAP Source id must not be blank");
            this.settings = Assert.notNull(settings, "LDAP Source settings must not be null");
            this.services = Assert.notNull(services, "LDAP Source execution services must not be null");
            this.frameCodec = Assert.notNull(frameCodec, "LDAP Source frame codec must not be null");
            this.encoder = Assert.notNull(encoder, "LDAP Source encoder must not be null");
            this.decoder = Assert.notNull(decoder, "LDAP Source decoder must not be null");
            this.identity = Assert.notNull(identity, "LDAP Source identity mapper must not be null");
        }

        /**
         * Creates the standard anonymous LDAPv3 Bind used when no service account is configured.
         *
         * @return anonymous simple Bind request
         */
        private static BindRequest anonymousBind() {
            return new BindRequest(BindRequest.VERSION_3, new DistinguishedName(Normal.EMPTY),
                    new AuthenticationChoice.Simple(Normal.EMPTY_BYTE_ARRAY));
        }

        /**
         * Extracts exactly one entry and one successful final result while rejecting references or extra messages.
         *
         * @param messages complete Search response sequence
         * @return unique entry, or {@code null} when the sequence is not authentication-safe
         */
        private static SearchResultEntry unique(final List<LdapMessage> messages) {
            if (messages == null || messages.isEmpty()) {
                return null;
            }
            SearchResultEntry entry = null;
            boolean done = false;
            for (LdapMessage message : messages) {
                if (message == null || done || message.protocolOp() instanceof SearchResultReference) {
                    return null;
                }
                if (message.protocolOp() instanceof SearchResultEntry current) {
                    if (entry != null)
                        return null;
                    entry = current;
                } else if (message.protocolOp() instanceof SearchResultDone terminal) {
                    if (!LdapResultCode.SUCCESS.equals(terminal.result().resultCode()))
                        return null;
                    done = true;
                } else {
                    return null;
                }
            }
            return done ? entry : null;
        }

        /**
         * Sends Unbind after a successful authentication, then closes the exclusive client on every path.
         *
         * @param client  exclusive LDAP client
         * @param outcome completed authentication outcome
         * @param context immutable invocation context
         * @param timeout shared budget
         * @return original outcome after deterministic cleanup
         */
        private static CompletionStage<Outcome<SourceAuthenticationInitiation>> finish(
                final LdapClient client,
                final Outcome<SourceAuthenticationInitiation> outcome,
                final Context context,
                final Timeout.Budget timeout) {
            if (!(outcome instanceof Outcome.Succeeded<SourceAuthenticationInitiation>)) {
                client.close();
                return completed(outcome);
            }
            return client.unbind(new UnbindRequest(), context, timeout).handle((ignored, thrown) -> outcome)
                    .whenComplete((ignored, thrown) -> client.close());
        }

        /**
         * Encodes character data to UTF-8 while rejecting malformed surrogate input.
         *
         * @param characters source characters
         * @return exact UTF-8 octets
         * @throws ValidateException if the character sequence is malformed
         */
        private static byte[] utf8(final CharBuffer characters) {
            try {
                final ByteBuffer encoded = Charset.UTF_8.newEncoder().onMalformedInput(CodingErrorAction.REPORT)
                        .onUnmappableCharacter(CodingErrorAction.REPORT).encode(characters);
                final byte[] value = new byte[encoded.remaining()];
                encoded.get(value);
                return value;
            } catch (CharacterCodingException exception) {
                throw new ValidateException("LDAP Source value is not valid UTF-8", exception);
            }
        }

        /**
         * Narrows one completed application result through the declared response class.
         *
         * @param stage        delegated authentication stage
         * @param responseType exact capability response type
         * @param <S>          expected success type
         * @return type-safe stage
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
         * Returns a safe rejection for an undeclared capability.
         *
         * @param <S> expected success type
         * @return completed not-found outcome
         */
        private static <S> CompletionStage<Outcome<S>> missing() {
            return completed(Outcome.rejected(failure(ErrorCode._404, "LDAP Source capability is not available")));
        }

        /**
         * Returns a safe rejection for a non-direct, wrong-Source, or non-password request.
         *
         * @param <S> expected success type
         * @return completed bad-request outcome
         */
        private static <S> CompletionStage<Outcome<S>> mismatch() {
            return completed(
                    Outcome.rejected(
                            failure(ErrorCode._400, "LDAP Source requires a matching direct password request")));
        }

        /**
         * Creates a safe shared Bus failure.
         *
         * @param error       existing Bus error code
         * @param description fixed non-sensitive description
         * @return closed failure value
         */
        private static Outcome.Failure failure(final Errors error, final String description) {
            return new Outcome.Failure(error, description, new JsonValue.ObjectValue(Map.of()));
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
         * Returns the single application-level initiation capability.
         *
         * @return immutable LDAP Source manifest
         */
        @Override
        public Capability.Manifest manifest() {
            return MANIFEST;
        }

        /**
         * Accepts only a matching direct password initiation and runs the exclusive LDAP flow.
         *
         * @param capability exact declared capability
         * @param request    direct Source authentication request
         * @param context    immutable invocation context
         * @param timeout    shared end-to-end budget
         * @param <Q>        declared request type
         * @param <S>        declared success type
         * @return completed direct identity initiation or a closed rejection/failure
         */
        @Override
        public <Q, S> CompletionStage<Outcome<S>> invoke(
                final Capability<Q, S> capability,
                final Q request,
                final Context context,
                final Timeout.Budget timeout) {
            Assert.notNull(capability, "LDAP Source capability must not be null");
            Assert.notNull(context, "LDAP Source context must not be null");
            Assert.notNull(timeout, "LDAP Source time budget must not be null");
            if (!MANIFEST.capabilities().contains(capability)) {
                return missing();
            }
            if (capability != SourceAuthentication.INITIATE
                    || capability.requestType() != SourceAuthenticationRequest.Initiation.class
                    || capability.responseType() != SourceAuthenticationInitiation.class
                    || !(request instanceof SourceAuthenticationRequest.Direct direct)
                    || !sourceId.equals(direct.sourceId()) || direct.credential().type() != Credential.Type.PASSWORD) {
                return mismatch();
            }
            return narrow(authenticate(direct, context, timeout), capability.responseType());
        }

        /**
         * Runs service Bind, unique user Search, user re-Bind, identity mapping, and successful Unbind.
         *
         * @param request validated direct request
         * @param context immutable invocation context
         * @param timeout shared operation budget
         * @return direct completed initiation outcome
         */
        private CompletionStage<Outcome<SourceAuthenticationInitiation>> authenticate(
                final SourceAuthenticationRequest.Direct request,
                final Context context,
                final Timeout.Budget timeout) {
            if (timeout.expired()) {
                return completed(
                        Outcome.failed(failure(ErrorCode._408, "LDAP Source authentication time budget expired")));
            }
            final LdapClient client = new LdapClient(services.fabricContext(), services.executor(), settings,
                    frameCodec.fork(), encoder, decoder);
            final CompletionStage<Outcome<BindResponse>> initial = settings.bindDn().isEmpty()
                    ? client.bind(anonymousBind(), context, timeout)
                    : bind(
                            client,
                            settings.bindDn().getOrThrow(),
                            settings.bindCredential().getOrThrow(),
                            context,
                            timeout);
            final CompletionStage<Outcome<SourceAuthenticationInitiation>> flow = initial
                    .thenCompose(outcome -> afterServiceBind(outcome, client, request, context, timeout)).exceptionally(
                            cause -> Outcome.failed(failure(ErrorCode._503, "LDAP Source authentication flow failed")));
            return flow.thenCompose(outcome -> finish(client, outcome, context, timeout));
        }

        /**
         * Continues with the unique Search only after a successful service or anonymous Bind.
         *
         * @param outcome initial Bind outcome
         * @param client  exclusive LDAP client
         * @param request direct authentication request
         * @param context immutable invocation context
         * @param timeout shared budget
         * @return next-stage completed initiation outcome
         */
        private CompletionStage<Outcome<SourceAuthenticationInitiation>> afterServiceBind(
                final Outcome<BindResponse> outcome,
                final LdapClient client,
                final SourceAuthenticationRequest.Direct request,
                final Context context,
                final Timeout.Budget timeout) {
            if (outcome instanceof Outcome.Rejected<BindResponse> rejected) {
                return completed(Outcome.rejected(rejected.failure()));
            }
            if (outcome instanceof Outcome.Failed<BindResponse> failed) {
                return completed(Outcome.failed(failed.failure()));
            }
            final BindResponse response = ((Outcome.Succeeded<BindResponse>) outcome).value();
            if (response == null || !LdapResultCode.SUCCESS.equals(response.result().resultCode())) {
                return completed(
                        Outcome.failed(failure(ErrorCode._503, "LDAP Source service Bind was not successful")));
            }
            final SearchRequest search;
            try {
                search = search(request.principalHint());
            } catch (RuntimeException exception) {
                return completed(
                        Outcome.rejected(
                                failure(ErrorCode._400, "LDAP Source principal cannot form a UTF-8 assertion value")));
            }
            return client.search(search, context, timeout)
                    .thenCompose(result -> afterSearch(result, client, request, context, timeout));
        }

        /**
         * Requires exactly one entry and a successful terminal result before the user password Bind.
         *
         * @param outcome complete Search outcome
         * @param client  exclusive LDAP client
         * @param request direct authentication request
         * @param context immutable invocation context
         * @param timeout shared budget
         * @return user-Bind continuation
         */
        private CompletionStage<Outcome<SourceAuthenticationInitiation>> afterSearch(
                final Outcome<List<LdapMessage>> outcome,
                final LdapClient client,
                final SourceAuthenticationRequest.Direct request,
                final Context context,
                final Timeout.Budget timeout) {
            if (outcome instanceof Outcome.Rejected<List<LdapMessage>> rejected) {
                return completed(Outcome.rejected(rejected.failure()));
            }
            if (outcome instanceof Outcome.Failed<List<LdapMessage>> failed) {
                return completed(Outcome.failed(failed.failure()));
            }
            final SearchResultEntry entry = unique(((Outcome.Succeeded<List<LdapMessage>>) outcome).value());
            if (entry == null) {
                return completed(Outcome.rejected(failure(ErrorCode._401, "LDAP Source credentials are invalid")));
            }
            return bind(client, entry.objectName(), request.credential(), context, timeout)
                    .thenApply(result -> afterUserBind(result, entry));
        }

        /**
         * Maps a successfully rebound unique entry to the protocol-neutral completed identity.
         *
         * @param outcome user Bind outcome
         * @param entry   uniquely selected Search entry
         * @return completed identity or a closed credential rejection/failure
         */
        private Outcome<SourceAuthenticationInitiation> afterUserBind(
                final Outcome<BindResponse> outcome,
                final SearchResultEntry entry) {
            if (outcome instanceof Outcome.Rejected<BindResponse> rejected) {
                return Outcome.rejected(rejected.failure());
            }
            if (outcome instanceof Outcome.Failed<BindResponse> failed) {
                return Outcome.failed(failed.failure());
            }
            final BindResponse response = ((Outcome.Succeeded<BindResponse>) outcome).value();
            if (response == null || !LdapResultCode.SUCCESS.equals(response.result().resultCode())) {
                return Outcome.rejected(failure(ErrorCode._401, "LDAP Source credentials are invalid"));
            }
            try {
                final ExternalIdentity external = identity.map(sourceId, entry);
                return Outcome.succeeded(
                        new SourceAuthenticationInitiation.Completed(new SourceAuthenticationResult(external)));
            } catch (RuntimeException exception) {
                return Outcome.failed(failure(ErrorCode._503, "LDAP Source identity mapping failed"));
            }
        }

        /**
         * Resolves one password lease, creates an immutable simple Bind request, erases temporary bytes, and binds.
         *
         * @param client    exclusive LDAP client
         * @param dn        Bind distinguished name
         * @param reference external PASSWORD reference
         * @param context   immutable invocation context
         * @param timeout   shared budget
         * @return standard Bind outcome
         */
        private CompletionStage<Outcome<BindResponse>> bind(
                final LdapClient client,
                final DistinguishedName dn,
                final Credential.Reference reference,
                final Context context,
                final Timeout.Budget timeout) {
            final CompletionStage<Outcome<SecretLease>> resolution;
            try {
                resolution = services.secretResolver().resolve(reference, context, timeout);
            } catch (RuntimeException exception) {
                return completed(Outcome.failed(failure(ErrorCode._503, "LDAP Source password resolution failed")));
            }
            if (resolution == null) {
                return completed(
                        Outcome.failed(failure(ErrorCode._503, "LDAP Source password resolver returned no stage")));
            }
            return resolution
                    .handle(
                            (outcome, thrown) -> thrown == null && outcome != null ? outcome
                                    : Outcome.<SecretLease>failed(
                                            failure(ErrorCode._503, "LDAP Source password resolution failed")))
                    .thenCompose(outcome -> switch (outcome) {
                        case Outcome.Succeeded<SecretLease> succeeded -> bindLease(
                                client,
                                dn,
                                succeeded.value(),
                                context,
                                timeout);
                        case Outcome.Rejected<SecretLease> rejected -> completed(Outcome.rejected(rejected.failure()));
                        case Outcome.Failed<SecretLease> failed -> completed(Outcome.failed(failed.failure()));
                    });
        }

        /**
         * Consumes and closes one secret lease before returning the asynchronous client Bind stage.
         *
         * @param client  exclusive LDAP client
         * @param dn      Bind distinguished name
         * @param lease   exclusively owned password lease
         * @param context immutable invocation context
         * @param timeout shared budget
         * @return standard Bind outcome stage
         */
        private CompletionStage<Outcome<BindResponse>> bindLease(
                final LdapClient client,
                final DistinguishedName dn,
                final SecretLease lease,
                final Context context,
                final Timeout.Budget timeout) {
            if (lease == null) {
                return completed(
                        Outcome.failed(failure(ErrorCode._503, "LDAP Source password resolver returned no lease")));
            }
            byte[] password = null;
            try (lease) {
                if (lease.material().length == 0) {
                    return completed(Outcome.rejected(failure(ErrorCode._401, "LDAP Source password is empty")));
                }
                password = utf8(CharBuffer.wrap(lease.material()));
                final BindRequest request = new BindRequest(BindRequest.VERSION_3, dn,
                        new AuthenticationChoice.Simple(password));
                return client.bind(request, context, timeout);
            } catch (RuntimeException exception) {
                return completed(Outcome.failed(failure(ErrorCode._503, "LDAP Source password encoding failed")));
            } finally {
                if (password != null) {
                    Arrays.fill(password, (byte) 0);
                }
            }
        }

        /**
         * Creates the binary RFC 4511 equality Search without applying RFC 4515 textual escaping.
         *
         * @param principal non-secret principal hint
         * @return bounded unique-user Search request
         */
        private SearchRequest search(final String principal) {
            byte[] assertion = null;
            try {
                assertion = utf8(CharBuffer.wrap(principal));
                final SearchRequest.Filter filter = new SearchRequest.EqualityMatch(
                        new SearchRequest.AttributeValueAssertion(settings.usernameAttribute(),
                                new SearchRequest.AssertionValue(assertion)));
                return new SearchRequest(settings.searchBase(), SearchRequest.Scope.WHOLE_SUBTREE,
                        SearchRequest.DerefAliases.NEVER_DEREF_ALIASES, 2, settings.timeLimitSeconds(), false, filter,
                        settings.attributes());
            } finally {
                if (assertion != null) {
                    Arrays.fill(assertion, (byte) 0);
                }
            }
        }

    }

}
