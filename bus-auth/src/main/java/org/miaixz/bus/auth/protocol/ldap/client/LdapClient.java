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
package org.miaixz.bus.auth.protocol.ldap.client;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.protocol.ldap.*;
import org.miaixz.bus.auth.protocol.ldap.codec.BerCodec;
import org.miaixz.bus.auth.protocol.ldap.codec.LdapMessageDecoder;
import org.miaixz.bus.auth.protocol.ldap.codec.LdapMessageEncoder;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.extra.json.JsonValue;
import org.miaixz.bus.fabric.Call;
import org.miaixz.bus.fabric.Message;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.network.tls.TlsPolicy;
import org.miaixz.bus.fabric.protocol.socket.SocketOptions;
import org.miaixz.bus.fabric.protocol.socket.SocketSession;
import org.miaixz.bus.fabric.protocol.socket.SocketX;

/**
 * Executes standard LDAPv3 Bind, Search, and Unbind operations on one exclusive Fabric socket session.
 * <p>
 * A client instance is intentionally not pooled or shared between authentication attempts because LDAP Bind state is
 * connection-scoped. Operations are serialized in submission order, message identifiers are assigned by the client, and
 * every response must carry the corresponding identifier. Implicit TLS or the standard StartTLS operation is completed
 * before the first Bind.
 * </p>
 *
 * @author Kimi Liu
 */
public final class LdapClient implements AutoCloseable {

    /**
     * Shared Fabric transport context owned by the integrating runtime.
     */
    private final org.miaixz.bus.fabric.Context fabricContext;

    /**
     * Caller-owned executor used for every blocking Fabric Call.
     */
    private final Executor executor;

    /**
     * Immutable remote directory and security options.
     */
    private final LdapClientOptions options;

    /**
     * LDAP-aware Fabric stream frame codec.
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
     * Positive message identifier sequence used by this exclusive connection.
     */
    private final AtomicInteger nextMessageId;

    /**
     * Lock protecting the serialized stage tail and socket lifecycle.
     */
    private final Object lifecycleLock;

    /**
     * Completion tail that orders every submitted LDAP operation.
     */
    private CompletableFuture<Void> tail;

    /**
     * Lazily opened exclusive socket session, or {@code null} before the first operation.
     */
    private SocketSession session;

    /**
     * Whether this client has been permanently closed.
     */
    private volatile boolean closed;

    /**
     * Creates one exclusive lazy LDAP connection owner.
     *
     * @param fabricContext shared Fabric transport context
     * @param executor      caller-owned executor for blocking Fabric operations
     * @param options       immutable LDAP Source options
     * @param frameCodec    LDAP BER stream frame codec
     * @param encoder       complete LDAPMessage encoder
     * @param decoder       complete LDAPMessage decoder
     * @throws IllegalArgumentException if a dependency is {@code null}
     */
    public LdapClient(final org.miaixz.bus.fabric.Context fabricContext, final Executor executor,
            final LdapClientOptions options, final BerCodec frameCodec, final LdapMessageEncoder encoder,
            final LdapMessageDecoder decoder) {
        this.fabricContext = Assert.notNull(fabricContext, "LDAP Fabric context must not be null");
        this.executor = Assert.notNull(executor, "LDAP client executor must not be null");
        this.options = Assert.notNull(options, "LDAP client options must not be null");
        this.frameCodec = Assert.notNull(frameCodec, "LDAP client BER frame codec must not be null");
        this.encoder = Assert.notNull(encoder, "LDAP client message encoder must not be null");
        this.decoder = Assert.notNull(decoder, "LDAP client message decoder must not be null");
        this.nextMessageId = new AtomicInteger(1);
        this.lifecycleLock = new Object();
        this.tail = CompletableFuture.completedFuture(null);
    }

    /**
     * Executes and waits for one Fabric Call within the remaining shared operation budget.
     *
     * @param call    single-use Fabric Call
     * @param timeout shared operation budget
     * @param <T>     Fabric result type
     * @return completed Fabric value
     */
    private static <T> T await(final Call<T> call, final Timeout.Budget timeout) {
        final Duration remaining = timeout.remaining();
        if (remaining.isZero()) {
            throw new ValidateException("LDAP operation time budget has expired");
        }
        return call.await(remaining);
    }

    /**
     * Creates a safe operational failure without exception text or secret details.
     *
     * @param error       existing Bus error code
     * @param description fixed non-sensitive failure description
     * @param <T>         absent success type
     * @return failed operation outcome
     */
    private static <T> Outcome<T> failed(final Errors error, final String description) {
        return Outcome.failed(new Outcome.Failure(error, description, new JsonValue.ObjectValue(Map.of())));
    }

    /**
     * Executes one standard Bind request on this connection.
     *
     * @param request standard Bind request
     * @param context immutable authentication invocation context
     * @param timeout shared end-to-end time budget
     * @return stage containing the exact Bind response or a closed transport/protocol failure
     */
    public CompletionStage<Outcome<BindResponse>> bind(
            final BindRequest request,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(request, "LDAP Bind request must not be null");
        return serial(context, timeout, () -> {
            final LdapMessage response = exchange(request, timeout);
            if (!(response.protocolOp() instanceof BindResponse bind)) {
                throw new ProtocolException("LDAP Bind request received a non-Bind response");
            }
            return Outcome.succeeded(bind);
        });
    }

    /**
     * Executes one standard Search and collects its ordered response message sequence through SearchResultDone.
     *
     * @param request standard Search request
     * @param context immutable authentication invocation context
     * @param timeout shared end-to-end time budget
     * @return stage containing immutable Entry/Reference/Done messages or a closed failure
     */
    public CompletionStage<Outcome<List<LdapMessage>>> search(
            final SearchRequest request,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(request, "LDAP Search request must not be null");
        return serial(context, timeout, () -> Outcome.succeeded(searchNow(request, timeout)));
    }

    /**
     * Sends one standard Unbind request, closes the connection, and waits for no response.
     *
     * @param request standard Unbind request
     * @param context immutable authentication invocation context
     * @param timeout shared end-to-end time budget
     * @return stage containing empty success after the request is sent and the session is closed
     */
    public CompletionStage<Outcome<Void>> unbind(
            final UnbindRequest request,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(request, "LDAP Unbind request must not be null");
        return serial(context, timeout, () -> {
            final SocketSession current = requireSession(timeout);
            final int messageId = messageId();
            send(current, new LdapMessage(messageId, request, List.of()), timeout);
            closeSession();
            return Outcome.succeeded(null);
        });
    }

    /**
     * Permanently closes this client and its current exclusive socket session.
     */
    @Override
    public void close() {
        synchronized (lifecycleLock) {
            closed = true;
            closeSession();
        }
    }

    /**
     * Adds one operation to the exclusive serialized connection tail.
     *
     * @param context   immutable invocation context validated before scheduling
     * @param timeout   shared operation budget validated before scheduling
     * @param operation blocking operation executed on the caller-owned executor
     * @param <T>       operation success type
     * @return serialized asynchronous closed Outcome
     */
    private <T> CompletionStage<Outcome<T>> serial(
            final Context context,
            final Timeout.Budget timeout,
            final Supplier<Outcome<T>> operation) {
        Assert.notNull(context, "LDAP client invocation context must not be null");
        Assert.notNull(timeout, "LDAP client time budget must not be null");
        Assert.notNull(operation, "LDAP client operation must not be null");
        synchronized (lifecycleLock) {
            if (closed) {
                return CompletableFuture.completedFuture(failed(ErrorCode._503, "LDAP client connection is closed."));
            }
            final CompletableFuture<Outcome<T>> result = tail.handle((ignored, previousFailure) -> null)
                    .thenApplyAsync(ignored -> execute(operation, timeout), executor);
            tail = result.handle((ignored, failure) -> null);
            return result;
        }
    }

    /**
     * Executes one serialized blocking operation and closes the session after any exceptional failure.
     *
     * @param operation blocking operation body
     * @param timeout   shared operation budget
     * @param <T>       success type
     * @return successful, rejected, or operational failure value
     */
    private <T> Outcome<T> execute(final Supplier<Outcome<T>> operation, final Timeout.Budget timeout) {
        if (timeout.expired()) {
            closeSession();
            return failed(ErrorCode._504, "LDAP operation time budget expired.");
        }
        try {
            return operation.get();
        } catch (RuntimeException exception) {
            closeSession();
            if (exception instanceof ProtocolException) {
                return failed(ErrorCode._502, "LDAP peer returned an invalid protocol message.");
            }
            if (exception instanceof ValidateException && timeout.expired()) {
                return failed(ErrorCode._504, "LDAP operation time budget expired.");
            }
            return failed(ErrorCode._503, "LDAP transport operation failed.");
        }
    }

    /**
     * Exchanges one request for exactly one response with the same message identifier.
     *
     * @param operation standard request protocol operation
     * @param timeout   shared operation budget
     * @return matching complete response message
     */
    private LdapMessage exchange(final LdapMessage.ProtocolOp operation, final Timeout.Budget timeout) {
        final SocketSession current = requireSession(timeout);
        final int messageId = messageId();
        send(current, new LdapMessage(messageId, operation, List.of()), timeout);
        return receive(current, messageId, timeout);
    }

    /**
     * Executes Search and stops only after a single final SearchResultDone.
     *
     * @param request standard Search request
     * @param timeout shared operation budget
     * @return immutable ordered LDAP Search response messages
     */
    private List<LdapMessage> searchNow(final SearchRequest request, final Timeout.Budget timeout) {
        final SocketSession current = requireSession(timeout);
        final int messageId = messageId();
        send(current, new LdapMessage(messageId, request, List.of()), timeout);
        final List<LdapMessage> responses = new ArrayList<>();
        while (true) {
            final LdapMessage response = receive(current, messageId, timeout);
            if (!(response.protocolOp() instanceof SearchResultEntry)
                    && !(response.protocolOp() instanceof SearchResultReference)
                    && !(response.protocolOp() instanceof SearchResultDone)) {
                throw new ProtocolException("LDAP Search received an invalid response operation");
            }
            responses.add(response);
            if (response.protocolOp() instanceof SearchResultDone) {
                return List.copyOf(responses);
            }
        }
    }

    /**
     * Opens and protects the exclusive socket session on first use.
     *
     * @param timeout shared operation budget used for connection and optional StartTLS
     * @return open TLS-protected socket session
     */
    private SocketSession requireSession(final Timeout.Budget timeout) {
        synchronized (lifecycleLock) {
            if (closed) {
                throw new ValidateException("LDAP client is closed");
            }
            if (session != null) {
                return session;
            }
            final SocketX.Builder builder = SocketX.builder(fabricContext).timeout(timeout.forFabric())
                    .frame(frameCodec);
            if (options.securityMode() == LdapClientOptions.SecurityMode.LDAPS) {
                builder.tls(options.host(), options.port());
            } else {
                builder.tcp(options.host(), options.port());
            }
            session = await(builder.build().call(), timeout);
            if (options.securityMode() == LdapClientOptions.SecurityMode.START_TLS) {
                startTls(session, timeout);
            }
            return session;
        }
    }

    /**
     * Performs the standard StartTLS extended exchange and upgrades the same Fabric session.
     *
     * @param current open plaintext TCP session
     * @param timeout shared operation budget
     */
    private void startTls(final SocketSession current, final Timeout.Budget timeout) {
        final int requestId = messageId();
        send(
                current,
                new LdapMessage(requestId,
                        new ExtendedRequest(ExtendedRequest.START_TLS_OID, org.miaixz.bus.core.lang.Optional.empty()),
                        List.of()),
                timeout);
        final LdapMessage response = receive(current, requestId, timeout);
        if (!(response.protocolOp() instanceof ExtendedResponse extended)
                || !LdapResultCode.SUCCESS.equals(extended.result().resultCode())) {
            throw new ProtocolException("LDAP StartTLS did not return a successful ExtendedResponse");
        }
        final TlsPolicy socketPolicy = fabricContext.options().get(SocketOptions.TLS_POLICY);
        final TlsPolicy policy = socketPolicy == null ? TlsPolicy.resolve(fabricContext.options()) : socketPolicy;
        await(current.upgradeTls(policy), timeout);
    }

    /**
     * Encodes and sends one complete LDAPMessage.
     *
     * @param current open socket session
     * @param message complete request message
     * @param timeout shared operation budget
     */
    private void send(final SocketSession current, final LdapMessage message, final Timeout.Budget timeout) {
        await(current.send(Payload.of(encoder.encode(message))), timeout);
    }

    /**
     * Receives and decodes one complete response with the expected message identifier and no unsupported controls.
     *
     * @param current           open socket session
     * @param expectedMessageId request message identifier
     * @param timeout           shared operation budget
     * @return matching complete response message
     */
    private LdapMessage receive(
            final SocketSession current,
            final int expectedMessageId,
            final Timeout.Budget timeout) {
        final Message message = await(current.receive(), timeout);
        final LdapMessage response = decoder.decode(message.payload().bytes(options.maximumMessageBytes()));
        if (response.messageId() != expectedMessageId) {
            throw new ProtocolException("LDAP response messageID does not match the outstanding request");
        }
        if (!response.controls().isEmpty()) {
            throw new ProtocolException("LDAP Source received unsupported response Controls");
        }
        return response;
    }

    /**
     * Allocates one positive LDAP message identifier and wraps safely after the signed integer maximum.
     *
     * @return next positive message identifier
     */
    private int messageId() {
        return nextMessageId.getAndUpdate(current -> current == Integer.MAX_VALUE ? 1 : current + 1);
    }

    /**
     * Closes and clears the current exclusive socket session without changing permanent client state.
     */
    private void closeSession() {
        synchronized (lifecycleLock) {
            final SocketSession current = session;
            session = null;
            if (current != null) {
                current.close();
            }
        }
    }

}
