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
package org.miaixz.bus.auth;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicReference;

import org.miaixz.bus.auth.metric.AuthMetric;
import org.miaixz.bus.auth.metric.AuthMetric.*;
import org.miaixz.bus.auth.metric.AuthMetric.Endpoint;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.*;
import org.miaixz.bus.fabric.Context;
import org.miaixz.bus.fabric.guard.route.AddressPolicy;
import org.miaixz.bus.fabric.network.aio.AioGroup;
import org.miaixz.bus.fabric.network.tls.TlsPolicy;
import org.miaixz.bus.fabric.network.tls.TlsSettings;
import org.miaixz.bus.fabric.network.udp.UdpNetwork;
import org.miaixz.bus.fabric.network.udp.UdpSession;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;
import org.miaixz.bus.fabric.protocol.http.HttpX;
import org.miaixz.bus.fabric.protocol.socket.SocketServer;
import org.miaixz.bus.fabric.protocol.socket.SocketSession;
import org.miaixz.bus.fabric.protocol.socket.SocketX;
import org.miaixz.bus.fabric.runtime.Activity;
import org.miaixz.bus.fabric.runtime.dispatch.DispatchHandle;
import org.miaixz.bus.fabric.runtime.dispatch.Dispatcher;

/**
 * Fabric-backed HTTP support for authorization providers.
 *
 * @author Kimi Liu
 */
public abstract class FabricX {

    /**
     * Constructs a new {@code FabricX} instance.
     */
    public FabricX() {
        // No initialization required.
    }

    /**
     * Shared Fabric context for authorization HTTP calls.
     */
    private static final org.miaixz.bus.fabric.Context CONTEXT = org.miaixz.bus.fabric.Context.create();

    /**
     * Form media used by authorization requests.
     */
    private static final MediaType FORM = MediaType.APPLICATION_FORM_URLENCODED_TYPE;

    /**
     * Sends a GET request.
     *
     * @param url URL
     * @return response body
     */
    protected static String get(final String url) {
        return get(url, null, null);
    }

    /**
     * Sends a GET request.
     *
     * @param url   URL
     * @param query query parameters
     * @return response body
     */
    protected static String get(final String url, final Map<String, ?> query) {
        return get(url, query, null);
    }

    /**
     * Sends a GET request.
     *
     * @param url     URL
     * @param query   query parameters
     * @param headers headers
     * @return response body
     */
    protected static String get(final String url, final Map<String, ?> query, final Map<String, ?> headers) {
        final var builder = Fabric.http(CONTEXT).get(url);
        if (query != null && !query.isEmpty()) {
            query.forEach((name, value) -> {
                if (name != null && value != null) {
                    builder.query(name, value);
                }
            });
        }
        if (headers != null && !headers.isEmpty()) {
            headers.forEach((name, value) -> {
                if (name != null && value != null) {
                    builder.header(name, value);
                }
            });
        }
        return builder.executeText();
    }

    /**
     * Sends a POST request with an empty form body.
     *
     * @param url URL
     * @return response body
     */
    protected static String post(final String url) {
        return Fabric.http(CONTEXT).post(url).body(Payload.empty(), FORM).executeText();
    }

    /**
     * Sends a POST request with form fields.
     *
     * @param url  URL
     * @param form form fields
     * @return response body
     */
    protected static String post(final String url, final Map<String, ?> form) {
        return post(url, form, null);
    }

    /**
     * Sends a POST request with form fields.
     *
     * @param url     URL
     * @param form    form fields
     * @param headers headers
     * @return response body
     */
    protected static String post(final String url, final Map<String, ?> form, final Map<String, ?> headers) {
        final var builder = Fabric.http(CONTEXT).post(url);
        if (headers != null && !headers.isEmpty()) {
            headers.forEach((name, value) -> {
                if (name != null && value != null) {
                    builder.header(name, value);
                }
            });
        }
        if (form == null || form.isEmpty()) {
            builder.body(Payload.empty(), FORM);
        } else {
            form.forEach((name, value) -> {
                if (name != null && value != null) {
                    builder.form(name, value);
                }
            });
        }
        return builder.executeText();
    }

    /**
     * Sends a POST request with raw body.
     *
     * @param url         URL
     * @param data        body
     * @param contentType content type
     * @return response body
     */
    protected static String post(final String url, final String data, final String contentType) {
        return post(url, data, null, contentType);
    }

    /**
     * Sends a POST request with raw body.
     *
     * @param url         URL
     * @param data        body
     * @param headers     headers
     * @param contentType content type
     * @return response body
     */
    protected static String post(
            final String url,
            final String data,
            final Map<String, ?> headers,
            final String contentType) {
        final var builder = Fabric.http(CONTEXT).post(url).body(data == null ? Normal.EMPTY : data, media(contentType));
        if (headers != null && !headers.isEmpty()) {
            headers.forEach((name, value) -> {
                if (name != null && value != null) {
                    builder.header(name, value);
                }
            });
        }
        return builder.executeText();
    }

    /**
     * Parses a valid content type.
     *
     * @param contentType content type
     * @return media type
     */
    private static MediaType media(final String contentType) {
        if (StringKit.isBlank(contentType) || StringKit.containsAny(contentType, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException("Content-Type must be non-blank and single-line");
        }
        return MediaType.parse(contentType);
    }

    /**
     * Creates the five authentication transport ports over one caller-owned Fabric context.
     *
     * <p>
     * This method constructs adapters only. It performs no network operation and never closes the supplied context.
     * </p>
     *
     * @param context caller-owned Fabric context
     * @return immutable authentication transport aggregate
     */
    public static AuthMetric.Transports transports(final Context context) {
        final Context current = Assert.notNull(context, () -> new ValidateException("Fabric context must not be null"));
        return new AuthMetric.Transports(new HttpProtocolAdapter(current), new StreamClientAdapter(current),
                new StreamServerAdapter(current), new DatagramClientAdapter(current),
                new DatagramServerAdapter(current));
    }

    /**
     * Submits one blocking Fabric call to its owning context dispatcher and mirrors cancellation in both directions.
     *
     * @param context   owning context
     * @param operation stable operation name
     * @param tag       dispatch correlation tag
     * @param call      Fabric call
     * @param <T>       call result type
     * @return independently cancellable completion stage
     */
    private static <T> CompletionStage<T> submit(
            final Context context,
            final String operation,
            final Object tag,
            final Call<T> call) {
        final Context current = Assert.notNull(context, () -> new ValidateException("Fabric context must not be null"));
        final Call<T> currentCall = Assert.notNull(call, () -> new ValidateException("Fabric call must not be null"));
        final String currentOperation = Assert
                .notBlank(operation, () -> new ValidateException("Fabric operation must not be blank"));
        final Dispatcher dispatcher = current.reactor().dispatcher();
        final CompletableFuture<T> result = new CompletableFuture<>();
        final AtomicReference<DispatchHandle> handle = new AtomicReference<>();
        final Activity activity = Activity.of(currentOperation, () -> {
            if (result.isCancelled()) {
                return;
            }
            try {
                result.complete(currentCall.execute());
            } catch (final Throwable failure) {
                result.completeExceptionally(failure);
            }
        });
        result.whenComplete((value, failure) -> {
            if (result.isCancelled()) {
                currentCall.cancel();
                final DispatchHandle currentHandle = handle.get();
                if (currentHandle != null) {
                    dispatcher.cancel(currentHandle);
                }
            }
        });
        try {
            final DispatchHandle submitted = dispatcher.background(currentOperation, tag, activity);
            handle.set(submitted);
            if (result.isCancelled()) {
                currentCall.cancel();
                dispatcher.cancel(submitted);
            }
        } catch (final Throwable failure) {
            currentCall.cancel();
            result.completeExceptionally(failure);
        }
        return result;
    }

    /**
     * Converts an authentication transport policy to the Fabric address subset.
     *
     * @param policy authentication transport policy
     * @return Fabric address policy
     */
    private static AddressPolicy addressPolicy(final TransportPolicy policy) {
        final TransportPolicy current = Assert
                .notNull(policy, () -> new ValidateException("Transport policy must not be null"));
        final LinkedHashSet<Protocol> schemes = new LinkedHashSet<>();
        for (final Protocol protocol : current.allowedSchemes()) {
            schemes.add(switch (protocol) {
                case LDAP -> Protocol.TCP;
                case LDAPS -> Protocol.TLS;
                case RADIUS -> Protocol.UDP;
                default -> protocol;
            });
        }
        return new AddressPolicy(Set.copyOf(schemes), current.allowedPorts(), current.allowedTargetCidrs(),
                current.allowedPeerCidrs());
    }

    /**
     * Converts an authentication server policy to the Fabric address subset used during listener binding. Fabric
     * validates a numeric bind address through its target-address path before it starts admitting peers. The server
     * peer networks therefore also form the explicit local bind exception set, while the original peer set remains
     * unchanged for accepted connection and datagram validation.
     *
     * @param policy authentication server transport policy
     * @return Fabric server address policy
     */
    private static AddressPolicy serverAddressPolicy(final TransportPolicy policy) {
        final TransportPolicy current = Assert
                .notNull(policy, () -> new ValidateException("Transport policy must not be null"));
        final LinkedHashSet<String> targets = new LinkedHashSet<>(current.allowedTargetCidrs());
        targets.addAll(current.allowedPeerCidrs());
        final AddressPolicy base = addressPolicy(current);
        return new AddressPolicy(base.allowedSchemes(), base.allowedPorts(), Set.copyOf(targets),
                current.allowedPeerCidrs());
    }

    /**
     * Converts authentication timeouts to one Fabric timeout value.
     *
     * @param policy     authentication transport policy
     * @param invocation operation invocation
     * @return bounded Fabric timeout
     */
    private static Timeout timeout(final TransportPolicy policy, final Invocation invocation) {
        final Duration call = invocation.timeout().compareTo(policy.readTimeout()) < Normal._0 ? invocation.timeout()
                : policy.readTimeout();
        return Timeout.builder().connect(policy.connectTimeout()).read(policy.readTimeout()).write(policy.readTimeout())
                .call(call).close(call).build();
    }

    /**
     * Resolves the required caller-installed TLS context and rebuilds constrained handshake settings.
     *
     * @param context owning context
     * @param policy  authentication policy
     * @return constrained Fabric TLS policy
     */
    private static TlsPolicy tlsPolicy(final Context context, final TransportPolicy policy) {
        final TlsPolicy installed = context.options().get(TlsPolicy.OPTION);
        if (installed == null) {
            throw new ProtocolException(ErrorCode._100805);
        }
        final List<String> versions = policy.tlsVersions().stream().map(version -> version.javaName).toList();
        final TlsSettings source = installed.settings();
        final TlsSettings settings = TlsSettings.builder().versions(versions).cipherSuites(policy.cipherSuites())
                .clientAuth(policy.clientAuth()).verifyHostname(policy.verifyHostname())
                .certificate(source.certificate()).applicationProtocols(source.applicationProtocols())
                .supportsTlsExtensions(source.supportsTlsExtensions()).build();
        return TlsPolicy.of(installed.context(), settings);
    }

    /**
     * Converts a protocol endpoint to a Fabric address.
     *
     * @param endpoint authentication endpoint
     * @return Fabric address
     */
    private static Address address(final Endpoint endpoint) {
        final Endpoint current = Assert.notNull(endpoint, () -> new ValidateException("Endpoint must not be null"));
        final String scheme = switch (current.protocol()) {
            case LDAP -> Protocol.TCP.name().toLowerCase(Locale.ROOT);
            case LDAPS -> Protocol.TLS.name().toLowerCase(Locale.ROOT);
            case RADIUS -> Protocol.UDP.name().toLowerCase(Locale.ROOT);
            default -> current.protocol().name().toLowerCase(Locale.ROOT);
        };
        return new Address(scheme, current.host(), current.port(), Normal.EMPTY);
    }

    /**
     * Converts a Fabric address to an authentication endpoint.
     *
     * @param address  Fabric address
     * @param protocol authentication protocol
     * @return authentication endpoint
     */
    private static Endpoint endpoint(final Address address, final Protocol protocol) {
        final Address current = Assert.notNull(address, () -> new ValidateException("Address must not be null"));
        return new Endpoint(protocol, current.host(), current.port());
    }

    /**
     * Creates an invocation for an accepted peer while preserving immutable tenant context.
     *
     * @param invocation listener invocation
     * @param peer       accepted peer
     * @param protocol   accepted authentication protocol
     * @return peer-specific invocation
     */
    private static Invocation peerInvocation(final Invocation invocation, final Address peer, final Protocol protocol) {
        return new Invocation(invocation.tenantId(), invocation.correlationId(), invocation.timeout(),
                endpoint(peer, protocol), invocation.attributes());
    }

    /**
     * Maps authentication HTTP operations to Fabric HTTP calls.
     */
    private static final class HttpProtocolAdapter implements AuthMetric.ProtocolTransport {

        /**
         * Caller-owned Fabric context.
         */
        private final Context context;

        /**
         * Creates an HTTP adapter.
         *
         * @param context caller-owned context
         */
        private HttpProtocolAdapter(final Context context) {
            this.context = context;
        }

        /**
         * Executes one bounded guarded HTTP exchange.
         *
         * @param invocation operation context
         * @param request    immutable request
         * @param policy     transport policy
         * @return asynchronous immutable response
         */
        @Override
        public CompletionStage<Response> exchange(
                final Invocation invocation,
                final Request request,
                final TransportPolicy policy) {
            try {
                final HttpX.Builder builder = Fabric.http(context).method(request.method())
                        .url(request.uri().toString()).addressPolicy(addressPolicy(policy))
                        .timeout(timeout(policy, invocation));
                request.headers().forEach((name, values) -> values.forEach(value -> builder.header(name, value)));
                if (request.body().length > Normal._0) {
                    builder.body(Payload.of(request.body()));
                }
                return submit(context, "auth-http-exchange", invocation.correlationId(), builder.call())
                        .thenApply(response -> response(response, policy.maxResponseBytes()));
            } catch (final Throwable failure) {
                return CompletableFuture.failedFuture(failure);
            }
        }

        /**
         * Materializes and closes one Fabric HTTP response.
         *
         * @param response Fabric response
         * @param maximum  maximum body bytes
         * @return immutable authentication response
         */
        private static Response response(final HttpResponse response, final long maximum) {
            try (response) {
                return new Response(response.code(), response.headers().asMap(), response.bytes(maximum));
            }
        }
    }

    /**
     * Maps authentication stream opens to Fabric socket calls.
     */
    private static final class StreamClientAdapter implements AuthMetric.StreamTransport {

        /**
         * Caller-owned Fabric context.
         */
        private final Context context;

        /**
         * Creates a stream client adapter.
         *
         * @param context caller-owned context
         */
        private StreamClientAdapter(final Context context) {
            this.context = context;
        }

        /**
         * Opens one guarded raw stream.
         *
         * @param invocation operation context
         * @param endpoint   remote endpoint
         * @param policy     transport policy
         * @return asynchronous stream handle
         */
        @Override
        public CompletionStage<AuthMetric.StreamSession> open(
                final Invocation invocation,
                final Endpoint endpoint,
                final TransportPolicy policy) {
            final SocketX.Builder builder = Fabric.socket(context).addressPolicy(addressPolicy(policy))
                    .timeout(timeout(policy, invocation)).rawFrame();
            if (endpoint.protocol() == Protocol.LDAPS) {
                tlsPolicy(context, policy);
                builder.tls(endpoint.host(), endpoint.port());
            } else {
                if (policy.requireStartTls() || policy.requireLocalIdentity()) {
                    tlsPolicy(context, policy);
                }
                builder.tcp(endpoint.host(), endpoint.port());
            }
            return submit(context, "auth-stream-open", invocation.correlationId(), builder.call())
                    .thenApply(session -> new StreamSessionHandle(context, session));
        }
    }

    /**
     * Maps authentication stream listeners to Fabric socket servers.
     */
    private static final class StreamServerAdapter implements AuthMetric.StreamServerTransport {

        /**
         * Caller-owned Fabric context.
         */
        private final Context context;

        /**
         * Creates a stream server adapter.
         *
         * @param context caller-owned context
         */
        private StreamServerAdapter(final Context context) {
            this.context = context;
        }

        /**
         * Binds one guarded raw stream listener.
         *
         * @param invocation listener context
         * @param endpoint   local endpoint
         * @param policy     transport policy
         * @param handler    accepted-session handler
         * @return asynchronous listener binding
         */
        @Override
        public CompletionStage<AuthMetric.StreamServerBinding> bind(
                final Invocation invocation,
                final Endpoint endpoint,
                final TransportPolicy policy,
                final AuthMetric.StreamSessionHandler handler) {
            try {
                final SocketServer.Builder builder = Fabric.socketServer(context).bind(address(endpoint))
                        .addressPolicy(serverAddressPolicy(policy)).timeout(timeout(policy, invocation)).rawFrame()
                        .onOpen(session -> accept(invocation, endpoint.protocol(), handler, session));
                if (endpoint.protocol() == Protocol.LDAPS) {
                    final TlsPolicy tls = tlsPolicy(context, policy);
                    builder.tls(tls.context(), tls.settings());
                } else if (policy.requireStartTls() || policy.clientAuth().enabled()) {
                    tlsPolicy(context, policy);
                }
                final SocketServer server = builder.start();
                return CompletableFuture.completedFuture(new StreamBindingHandle(server, endpoint.protocol()));
            } catch (final Throwable failure) {
                return CompletableFuture.failedFuture(failure);
            }
        }

        /**
         * Delivers one accepted session and closes it when handler dispatch fails.
         *
         * @param invocation listener context
         * @param protocol   accepted authentication protocol
         * @param handler    accepted-session handler
         * @param session    accepted Fabric session
         */
        private void accept(
                final Invocation invocation,
                final Protocol protocol,
                final AuthMetric.StreamSessionHandler handler,
                final SocketSession session) {
            try {
                handler.onSession(
                        peerInvocation(invocation, session.address(), protocol),
                        new StreamSessionHandle(context, session)).whenComplete((value, failure) -> {
                            if (failure != null) {
                                session.close();
                            }
                        });
            } catch (final Throwable failure) {
                session.close();
            }
        }
    }

    /**
     * Maps authentication datagram exchanges to Fabric UDP sessions.
     */
    private static final class DatagramClientAdapter implements AuthMetric.DatagramTransport {

        /**
         * Caller-owned Fabric context.
         */
        private final Context context;

        /**
         * Creates a datagram client adapter.
         *
         * @param context caller-owned context
         */
        private DatagramClientAdapter(final Context context) {
            this.context = context;
        }

        /**
         * Exchanges one complete guarded datagram.
         *
         * @param invocation operation context
         * @param endpoint   remote endpoint
         * @param datagram   outbound datagram
         * @param policy     transport policy
         * @return asynchronous response datagram
         */
        @Override
        public CompletionStage<Datagram> exchange(
                final Invocation invocation,
                final Endpoint endpoint,
                final Datagram datagram,
                final TransportPolicy policy) {
            final AioGroup group = AioGroup.create(Normal._1, context.reactor().dispatcher());
            final UdpNetwork network = UdpNetwork.create(group, context.listener());
            final CompletionStage<Datagram> result;
            try {
                result = network.connect(address(endpoint), addressPolicy(policy), context.reactor().resolver())
                        .thenCompose(session -> exchange(invocation, datagram, policy, session));
            } catch (final Throwable failure) {
                network.close();
                group.close();
                return CompletableFuture.failedFuture(failure);
            }
            return result.whenComplete((value, failure) -> {
                network.close();
                group.close();
            });
        }

        /**
         * Sends and receives one datagram through an established UDP session.
         *
         * @param invocation operation context
         * @param datagram   outbound datagram
         * @param policy     transport policy
         * @param session    established UDP session
         * @return response datagram
         */
        private CompletionStage<Datagram> exchange(
                final Invocation invocation,
                final Datagram datagram,
                final TransportPolicy policy,
                final UdpSession session) {
            return submit(
                    context,
                    "auth-datagram-send",
                    invocation.correlationId(),
                    session.sendDatagram(Payload.of(datagram.bytes())))
                            .thenCompose(
                                    sent -> submit(
                                            context,
                                            "auth-datagram-receive",
                                            invocation.correlationId(),
                                            session.receive()))
                            .thenApply(
                                    message -> datagram(
                                            message,
                                            policy.maxResponseBytes(),
                                            datagram.peer().protocol()));
        }
    }

    /**
     * Maps authentication datagram listeners to Fabric UDP bindings.
     */
    private static final class DatagramServerAdapter implements AuthMetric.DatagramServerTransport {

        /**
         * Maximum concurrent handler stages admitted by the generic adapter.
         */
        private static final int MAX_IN_FLIGHT = 1_024;

        /**
         * Caller-owned Fabric context.
         */
        private final Context context;

        /**
         * Creates a datagram server adapter.
         *
         * @param context caller-owned context
         */
        private DatagramServerAdapter(final Context context) {
            this.context = context;
        }

        /**
         * Binds one guarded datagram listener.
         *
         * @param invocation listener context
         * @param endpoint   local endpoint
         * @param policy     transport policy
         * @param handler    datagram handler
         * @return asynchronous datagram binding
         */
        @Override
        public CompletionStage<AuthMetric.DatagramServerBinding> bind(
                final Invocation invocation,
                final Endpoint endpoint,
                final TransportPolicy policy,
                final AuthMetric.DatagramHandler handler) {
            final AioGroup group = AioGroup.create(Normal._1, context.reactor().dispatcher());
            final UdpNetwork network = UdpNetwork.create(group, context.listener());
            final CompletionStage<UdpNetwork.ServerBinding> binding;
            try {
                binding = network.bind(
                        address(endpoint),
                        serverAddressPolicy(policy),
                        context.reactor().resolver(),
                        Math.toIntExact(policy.maxResponseBytes()),
                        MAX_IN_FLIGHT,
                        message -> handle(invocation, policy, handler, message));
            } catch (final Throwable failure) {
                network.close();
                group.close();
                return CompletableFuture.failedFuture(failure);
            }
            return binding.thenApply(
                    value -> (AuthMetric.DatagramServerBinding) new DatagramBindingHandle(value, network, group,
                            endpoint.protocol()))
                    .whenComplete((value, failure) -> {
                        if (failure != null) {
                            network.close();
                            group.close();
                        }
                    });
        }

        /**
         * Converts one Fabric datagram and maps the optional response payload.
         *
         * @param invocation listener context
         * @param policy     transport policy
         * @param handler    authentication datagram handler
         * @param message    Fabric datagram
         * @return optional response payload
         */
        private CompletionStage<Optional<Payload>> handle(
                final Invocation invocation,
                final TransportPolicy policy,
                final AuthMetric.DatagramHandler handler,
                final Message message) {
            final Datagram input = datagram(message, policy.maxResponseBytes(), Protocol.RADIUS);
            return handler.onDatagram(peerInvocation(invocation, message.address(), Protocol.RADIUS), input)
                    .thenApply(result -> result.map(value -> Payload.of(value.bytes())));
        }
    }

    /**
     * Adapts one Fabric socket session to the authentication stream handle.
     */
    private static final class StreamSessionHandle implements AuthMetric.StreamSession {

        /**
         * Caller-owned Fabric context.
         */
        private final Context context;

        /**
         * Fabric socket session.
         */
        private final SocketSession session;

        /**
         * Creates a stream session handle.
         *
         * @param context caller-owned context
         * @param session Fabric session
         */
        private StreamSessionHandle(final Context context, final SocketSession session) {
            this.context = context;
            this.session = session;
        }

        /**
         * Writes one complete raw chunk.
         *
         * @param bytes output bytes
         * @return asynchronous completion
         */
        @Override
        public CompletionStage<Void> write(final byte[] bytes) {
            return submit(context, "auth-stream-write", session.address(), session.send(Payload.of(bytes)));
        }

        /**
         * Reads one bounded raw chunk.
         *
         * @param maxBytes maximum bytes
         * @return asynchronous chunk or EOF
         */
        @Override
        public CompletionStage<AuthMetric.StreamRead> readChunk(final int maxBytes) {
            if (maxBytes <= Normal._0) {
                return CompletableFuture.failedFuture(new ValidateException("Maximum stream bytes must be positive"));
            }
            return submit(context, "auth-stream-read", session.address(), session.receive()).thenApply(message -> {
                if (message == null) {
                    return new AuthMetric.StreamRead(new byte[Normal._0], true);
                }
                return new AuthMetric.StreamRead(message.payload().bytes(maxBytes), false);
            });
        }

        /**
         * Upgrades one plaintext stream with the caller-installed constrained TLS policy.
         *
         * @param policy authentication TLS policy
         * @return asynchronous upgrade completion
         */
        @Override
        public CompletionStage<Void> upgradeTls(final TransportPolicy policy) {
            return submit(
                    context,
                    "auth-stream-starttls",
                    session.address(),
                    session.upgradeTls(tlsPolicy(context, policy))).thenApply(handshake -> null);
        }

        /**
         * Closes the stream without closing its context.
         *
         * @return completed close stage
         */
        @Override
        public CompletionStage<Void> close() {
            session.close();
            return CompletableFuture.completedFuture(null);
        }
    }

    /**
     * Adapts one Fabric socket server to the authentication binding handle.
     */
    private static final class StreamBindingHandle implements AuthMetric.StreamServerBinding {

        /**
         * Fabric socket server.
         */
        private final SocketServer server;

        /**
         * Authentication protocol exposed by the binding.
         */
        private final Protocol protocol;

        /**
         * Creates a stream binding handle.
         *
         * @param server   Fabric server
         * @param protocol authentication protocol
         */
        private StreamBindingHandle(final SocketServer server, final Protocol protocol) {
            this.server = server;
            this.protocol = protocol;
        }

        /**
         * Returns the numeric local endpoint.
         *
         * @return local endpoint
         */
        @Override
        public Endpoint localEndpoint() {
            return endpoint(server.address(), protocol);
        }

        /**
         * Closes the listener without closing its context.
         *
         * @return completed close stage
         */
        @Override
        public CompletionStage<Void> close() {
            server.close();
            return CompletableFuture.completedFuture(null);
        }
    }

    /**
     * Adapts one Fabric UDP server binding and owns only its per-binding AIO resources.
     */
    private static final class DatagramBindingHandle implements AuthMetric.DatagramServerBinding {

        /**
         * Fabric UDP binding.
         */
        private final UdpNetwork.ServerBinding binding;

        /**
         * Per-binding UDP network.
         */
        private final UdpNetwork network;

        /**
         * Per-binding AIO group sharing the caller dispatcher.
         */
        private final AioGroup group;

        /**
         * Authentication protocol exposed by the binding.
         */
        private final Protocol protocol;

        /**
         * Creates a datagram binding handle.
         *
         * @param binding  Fabric binding
         * @param network  owned UDP network
         * @param group    owned AIO group
         * @param protocol authentication protocol
         */
        private DatagramBindingHandle(final UdpNetwork.ServerBinding binding, final UdpNetwork network,
                final AioGroup group, final Protocol protocol) {
            this.binding = binding;
            this.network = network;
            this.group = group;
            this.protocol = protocol;
        }

        /**
         * Returns the numeric local endpoint.
         *
         * @return local endpoint
         */
        @Override
        public Endpoint localEndpoint() {
            return endpoint(binding.local(), protocol);
        }

        /**
         * Shuts down the binding and then releases only its per-binding resources.
         *
         * @return shared asynchronous close completion
         */
        @Override
        public CompletionStage<Void> close() {
            return binding.shutdown().whenComplete((value, failure) -> {
                network.close();
                group.close();
            });
        }
    }

    /**
     * Converts one bounded Fabric message to an immutable authentication datagram.
     *
     * @param message  Fabric datagram
     * @param maximum  maximum payload bytes
     * @param protocol authentication protocol
     * @return authentication datagram
     */
    private static Datagram datagram(final Message message, final long maximum, final Protocol protocol) {
        final Message current = Assert.notNull(message, () -> new ValidateException("Datagram must not be null"));
        return new Datagram(endpoint(current.address(), protocol), current.payload().bytes(maximum));
    }

}
