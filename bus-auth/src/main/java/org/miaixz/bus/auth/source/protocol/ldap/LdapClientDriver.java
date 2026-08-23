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
package org.miaixz.bus.auth.source.protocol.ldap;

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
import org.miaixz.bus.auth.Identity;
import org.miaixz.bus.auth.Scheme.Options;
import org.miaixz.bus.auth.shared.SecretLease;
import org.miaixz.bus.auth.source.DriverServices;
import org.miaixz.bus.auth.source.SourceWorkflow;
import org.miaixz.bus.auth.source.protocol.ProtocolDriver;
import org.miaixz.bus.auth.source.protocol.ldap.client.LdapClient;
import org.miaixz.bus.auth.source.protocol.ldap.client.LdapClientOptions;
import org.miaixz.bus.auth.source.protocol.ldap.client.LdapClientScheme;
import org.miaixz.bus.auth.source.protocol.ldap.client.LdapIdentityParser;
import org.miaixz.bus.auth.source.protocol.ldap.codec.BerCodec;
import org.miaixz.bus.auth.source.protocol.ldap.codec.LdapMessageDecoder;
import org.miaixz.bus.auth.source.protocol.ldap.codec.LdapMessageEncoder;
import org.miaixz.bus.auth.worker.SourceWorker;
import org.miaixz.bus.auth.worker.WorkerSlots;
import org.miaixz.bus.auth.worker.loader.SecretLoader;
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
public class LdapClientDriver implements ProtocolDriver<LdapClientOptions> {

    /**
     * Creates the stateless LDAP Source driver.
     */
    public LdapClientDriver() {
        // No initialization required.
    }

    /**
     * Returns the LDAP client scheme bound to this driver.
     *
     * @return immutable LDAP Source scheme
     */
    @Override
    public LdapClientScheme scheme() {
        return new LdapClientScheme();
    }

    /**
     * Narrows generic Source options to LDAP client options.
     *
     * @param options generic Source options
     * @return validated LDAP client options
     */
    @Override
    public LdapClientOptions require(final Options<?> options) {
        if (options instanceof LdapClientOptions value) {
            return value;
        }
        throw new ValidateException("LDAP client driver requires LdapClientOptions");
    }

    /**
     * Declares the secret slot required by LDAP client authentication.
     *
     * @param source  Source configuration
     * @param options validated LDAP client options
     * @return exact project integration slots
     */
    @Override
    public WorkerSlots slots(final Source source, final LdapClientOptions options) {
        return WorkerSlots.of(WorkerSlots.Slot.SECRET);
    }

    /**
     * Declares framework execution and security-policy dependencies for the LDAP client.
     *
     * @param source  Source configuration
     * @param options validated LDAP client options
     * @return exact framework dependencies
     */
    @Override
    public Dependencies dependencies(final Source source, final LdapClientOptions options) {
        return Dependencies.of(Dependencies.Service.EXECUTOR, Dependencies.Service.POLICIES);
    }

    /**
     * Validates typed options, enforces the LDAP security rule, and assembles one direct-authentication runtime.
     *
     * @param prepared one-time validated Source graph, Options and dependency declaration
     * @param services dependency-scoped runtime services
     * @return immutable executable LDAP Source
     * @throws IllegalArgumentException if an argument is {@code null}
     * @throws ValidateException        if Source configuration, options, or security-rule validation fails
     */
    @Override
    public SourceWorker compile(final Prepared<LdapClientOptions> prepared, final DriverServices services) {
        Assert.notNull(prepared, "LDAP Source preparation must not be null");
        Assert.notNull(services, "LDAP Source execution services must not be null");
        final Blueprint.SourceEntry entry = prepared.entry();
        final Provider provider = prepared.provider();
        final Library library = prepared.library();
        final Source source = entry.resource();
        if (!scheme().id().equals(source.getType()) || !supports(source.getProtocol())
                || !provider.getId().equals(source.getProvider_id())
                || !library.getId().equals(provider.getLibrary_id())) {
            throw new ValidateException("LDAP Source driver requires a matching Source configuration");
        }
        final LdapClientOptions options = prepared.options();
        if (options.maximumMessageBytes() > services.policies().require(Protocol.LDAP).maximumMessageBytes()) {
            throw new ValidateException("LDAP Source message limit exceeds its shared security rule");
        }
        final BerCodec frameCodec = new BerCodec(options.maximumMessageBytes(), options.maximumBerDepth());
        final LdapMessageEncoder encoder = new LdapMessageEncoder(options.maximumMessageBytes(),
                options.maximumBerDepth());
        final LdapMessageDecoder decoder = new LdapMessageDecoder(options.maximumMessageBytes(),
                options.maximumBerDepth());
        final LdapIdentityParser identity = new LdapIdentityParser(options, FabricX.clock());
        return new CompiledClient(source.getId(), options, services, frameCodec, encoder, decoder, identity);
    }

    /**
     * Executes the single direct LDAP Source capability.
     *
     * @author Kimi Liu
     */
    private static final class CompiledClient implements SourceWorker {

        /**
         * Exact application-level Source capability manifest.
         */
        private static final Capability.Manifest MANIFEST = new Capability.Manifest(List.of(SourceWorkflow.INITIATE));

        /**
         * Source identifier.
         */
        private final String sourceId;

        /**
         * Frozen directory connection and mapping options.
         */
        private final LdapClientOptions options;

        /**
         * Capability-limited Source services supplying the executor and security policies.
         */
        private final DriverServices services;

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
        private final LdapIdentityParser identity;

        /**
         * Creates one immutable compiled LDAP Source runtime.
         *
         * @param sourceId   Source identifier
         * @param options    validated Source options
         * @param services   capability-limited Source services
         * @param frameCodec LDAP stream frame codec template
         * @param encoder    LDAPMessage encoder
         * @param decoder    LDAPMessage decoder
         * @param identity   verified identity mapper
         */
        private CompiledClient(final String sourceId, final LdapClientOptions options, final DriverServices services,
                final BerCodec frameCodec, final LdapMessageEncoder encoder, final LdapMessageDecoder decoder,
                final LdapIdentityParser identity) {
            this.sourceId = Assert.notBlank(sourceId, "LDAP Source id must not be blank");
            this.options = Assert.notNull(options, "LDAP Source options must not be null");
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
            return new BindRequest(Ldap.VERSION_3, new DistinguishedName(Normal.EMPTY),
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
         * @param timeout shared timeout
         * @return original outcome after deterministic cleanup
         */
        private static CompletionStage<Outcome<SourceWorkflow.Stage>> finish(
                final LdapClient client,
                final Outcome<SourceWorkflow.Stage> outcome,
                final Context context,
                final Timeout timeout) {
            if (!(outcome instanceof Outcome.Succeeded<SourceWorkflow.Stage>)) {
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
                default -> Outcome.failed(failure(ErrorCode._500, "LDAP Source returned an unsupported outcome"));
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
         * @param timeout    shared end-to-end timeout
         * @param <Q>        declared request type
         * @param <S>        declared success type
         * @return completed direct identity initiation or a closed rejection/failure
         */
        @Override
        public <Q, S> CompletionStage<Outcome<S>> invoke(
                final Capability<Q, S> capability,
                final Q request,
                final Context context,
                final Timeout timeout) {
            Assert.notNull(capability, "LDAP Source capability must not be null");
            Assert.notNull(context, "LDAP Source context must not be null");
            Assert.notNull(timeout, "LDAP Source timeout must not be null");
            if (!MANIFEST.capabilities().contains(capability)) {
                return missing();
            }
            if (capability != SourceWorkflow.INITIATE || capability.requestType() != SourceWorkflow.Request.Start.class
                    || capability.responseType() != SourceWorkflow.Stage.class
                    || !(request instanceof SourceWorkflow.Request.Direct direct) || !sourceId.equals(direct.sourceId())
                    || direct.credential().type() != Credential.Type.PASSWORD) {
                return mismatch();
            }
            return narrow(authenticate(direct, context, timeout), capability.responseType());
        }

        /**
         * Runs service Bind, unique user Search, user re-Bind, identity mapping, and successful Unbind.
         *
         * @param request validated direct request
         * @param context immutable invocation context
         * @param timeout shared operation timeout
         * @return direct completed initiation outcome
         */
        private CompletionStage<Outcome<SourceWorkflow.Stage>> authenticate(
                final SourceWorkflow.Request.Direct request,
                final Context context,
                final Timeout timeout) {
            if (timeout.expired()) {
                return completed(Outcome.failed(failure(ErrorCode._408, "LDAP Source authentication timeout expired")));
            }
            final LdapClient client = new LdapClient(services.executor(), options, frameCodec.fork(), encoder, decoder);
            final CompletionStage<Outcome<BindResponse>> initial = options.bindDn().isEmpty()
                    ? client.bind(anonymousBind(), context, timeout)
                    : bind(
                            client,
                            options.bindDn().getOrThrow(),
                            options.bindCredential().getOrThrow(),
                            context,
                            timeout);
            final CompletionStage<Outcome<SourceWorkflow.Stage>> flow = initial
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
         * @param timeout shared timeout
         * @return next-stage completed initiation outcome
         */
        private CompletionStage<Outcome<SourceWorkflow.Stage>> afterServiceBind(
                final Outcome<BindResponse> outcome,
                final LdapClient client,
                final SourceWorkflow.Request.Direct request,
                final Context context,
                final Timeout timeout) {
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
         * @param timeout shared timeout
         * @return user-Bind continuation
         */
        private CompletionStage<Outcome<SourceWorkflow.Stage>> afterSearch(
                final Outcome<List<LdapMessage>> outcome,
                final LdapClient client,
                final SourceWorkflow.Request.Direct request,
                final Context context,
                final Timeout timeout) {
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
        private Outcome<SourceWorkflow.Stage> afterUserBind(
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
                final Identity external = identity.map(sourceId, entry);
                return Outcome.succeeded(new SourceWorkflow.Stage.Completed(external));
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
         * @param timeout   shared timeout
         * @return standard Bind outcome
         */
        private CompletionStage<Outcome<BindResponse>> bind(
                final LdapClient client,
                final DistinguishedName dn,
                final Credential.Reference reference,
                final Context context,
                final Timeout timeout) {
            final CompletionStage<Outcome<SecretLease>> resolution;
            try {
                resolution = Outcome.mapStage(
                        () -> services.secretLoader()
                                .load(new SecretLoader.Request(services.entry(), reference), context, timeout),
                        loaded -> services.secretParser().parse(services.entry(), reference, loaded));
            } catch (RuntimeException exception) {
                return completed(Outcome.failed(failure(ErrorCode._503, "LDAP Source password resolution failed")));
            }
            if (resolution == null) {
                return completed(
                        Outcome.failed(failure(ErrorCode._503, "LDAP Source password loader returned no stage")));
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
                        default -> completed(
                                Outcome.failed(
                                        failure(ErrorCode._503, "LDAP secret loader returned an unsupported outcome")));
                    });
        }

        /**
         * Consumes and closes one secret lease before returning the asynchronous client Bind stage.
         *
         * @param client  exclusive LDAP client
         * @param dn      Bind distinguished name
         * @param lease   exclusively owned password lease
         * @param context immutable invocation context
         * @param timeout shared timeout
         * @return standard Bind outcome stage
         */
        private CompletionStage<Outcome<BindResponse>> bindLease(
                final LdapClient client,
                final DistinguishedName dn,
                final SecretLease lease,
                final Context context,
                final Timeout timeout) {
            if (lease == null) {
                return completed(
                        Outcome.failed(failure(ErrorCode._503, "LDAP Source password loader returned no lease")));
            }
            byte[] password = null;
            try (lease) {
                if (lease.material().length == 0) {
                    return completed(Outcome.rejected(failure(ErrorCode._401, "LDAP Source password is empty")));
                }
                password = utf8(CharBuffer.wrap(lease.material()));
                final BindRequest request = new BindRequest(Ldap.VERSION_3, dn,
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
                        new SearchRequest.AttributeValueAssertion(options.usernameAttribute(),
                                new SearchRequest.AssertionValue(assertion)));
                return new SearchRequest(options.searchBase(), SearchRequest.Scope.WHOLE_SUBTREE,
                        SearchRequest.DerefAliases.NEVER_DEREF_ALIASES, 2, options.timeLimitSeconds(), false, filter,
                        options.attributes());
            } finally {
                if (assertion != null) {
                    Arrays.fill(assertion, (byte) 0);
                }
            }
        }

    }

}
