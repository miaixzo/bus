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
package org.miaixz.bus.fabric.network.dns.forward;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.fabric.network.dns.message.DnsCodec;
import org.miaixz.bus.fabric.network.dns.message.DnsQuery;
import org.miaixz.bus.fabric.network.dns.message.DnsResponse;
import org.miaixz.bus.fabric.network.dns.message.DnsResponseCode;
import org.miaixz.bus.fabric.network.dns.observe.DnsMetrics;
import org.miaixz.bus.fabric.network.dns.recursive.DnsRetryBudget;
import org.miaixz.bus.fabric.network.dns.secure.quic.DnsQuicRuntime;
import org.miaixz.bus.fabric.network.dns.server.DnsTransport;
import org.miaixz.bus.fabric.network.tls.TlsPolicy;
import org.miaixz.bus.fabric.network.tls.TlsSettings;

/**
 * DNS forwarder that sends wire-format queries to configured upstream servers.
 *
 * @author Kimi Liu
 */
public final class DnsForwarder {

    /**
     * Shared upstream health index.
     */
    private static final DnsUpstreamHealth HEALTH = new DnsUpstreamHealth();

    /**
     * Upstream server list.
     */
    private final List<DnsUpstream> upstreams;

    /**
     * Optional DNS Server metrics facade.
     */
    private final DnsMetrics metrics;

    /**
     * Creates a forwarder.
     *
     * @param upstreams upstream DNS servers
     */
    public DnsForwarder(final List<DnsUpstream> upstreams) {
        this(upstreams, DnsMetrics.disabled());
    }

    /**
     * Creates a forwarder with optional DNS metrics.
     *
     * @param upstreams upstream DNS servers
     * @param metrics   optional DNS Server metrics facade
     */
    public DnsForwarder(final List<DnsUpstream> upstreams, final DnsMetrics metrics) {
        if (upstreams == null || upstreams.isEmpty()) {
            throw new ValidateException("DNS forwarder upstreams must not be empty");
        }
        for (final DnsUpstream upstream : upstreams) {
            if (upstream == null) {
                throw new ValidateException("DNS forwarder upstreams must not contain null");
            }
        }
        if (metrics == null) {
            throw new ValidateException("DNS forwarder metrics must not be null");
        }
        this.upstreams = List.copyOf(upstreams);
        this.metrics = metrics;
    }

    /**
     * Forwards one wire-format DNS request.
     *
     * @param request DNS request bytes
     * @return upstream DNS response bytes
     * @throws SocketException if all upstreams fail
     */
    public byte[] forward(final byte[] request) {
        return forward(request, DnsRetryBudget.forwarding());
    }

    /**
     * Forwards one wire-format DNS request while consuming a retry budget.
     *
     * @param request DNS request bytes
     * @param budget  retry budget shared by this forwarding flow
     * @return upstream DNS response bytes, or SERVFAIL when the budget is exhausted
     * @throws SocketException if all upstreams fail before the budget is exhausted
     */
    public byte[] forward(final byte[] request, final DnsRetryBudget budget) {
        if (request == null || request.length == 0 || request.length > DnsCodec.MAX_MESSAGE_BYTES) {
            throw new ValidateException("DNS forward request length is invalid");
        }
        if (budget == null) {
            throw new ValidateException("DNS forward retry budget must not be null");
        }
        RuntimeException failure = null;
        DnsRetryBudget cursor = budget;
        final List<DnsUpstream> selected = HEALTH.select(upstreams);
        if (selected.isEmpty()) {
            return servfail(request);
        }
        for (final DnsUpstream upstream : selected) {
            if (cursor.exhausted()) {
                return servfail(request);
            }
            final DnsRetryBudget.Attempt attempt = cursor.reserve(upstream.timeout());
            cursor = attempt.budget();
            final long started = System.nanoTime();
            try {
                final byte[] response = forward(upstream, request, attempt.timeout());
                final long elapsedNanos = System.nanoTime() - started;
                HEALTH.markSuccess(upstream, elapsedNanos);
                metrics.forwardUpstreamLatency(upstream, elapsedNanos);
                return response;
            } catch (final RuntimeException e) {
                metrics.forwardUpstreamLatency(upstream, System.nanoTime() - started);
                HEALTH.markFailure(upstream);
                if (failure == null) {
                    failure = e;
                } else {
                    failure.addSuppressed(e);
                }
            }
        }
        if (cursor.exhausted()) {
            return servfail(request);
        }
        throw new SocketException("All DNS upstreams failed", failure);
    }

    /**
     * Forwards one request to one upstream.
     *
     * @param upstream upstream DNS server
     * @param request  DNS request bytes
     * @param timeout  effective request timeout
     * @return upstream DNS response bytes
     */
    private static byte[] forward(final DnsUpstream upstream, final byte[] request, final Duration timeout) {
        if (upstream.transport() == DnsUpstreamTransport.TCP) {
            return forwardTcp(upstream, request, timeout);
        }
        if (upstream.transport() == DnsUpstreamTransport.DOT) {
            return forwardDot(upstream, request, timeout);
        }
        if (upstream.transport() == DnsUpstreamTransport.DOH) {
            return forwardDoh(upstream, request, timeout);
        }
        if (upstream.transport() == DnsUpstreamTransport.DOQ) {
            return forwardDoq(upstream);
        }
        return forwardUdp(upstream, request, timeout);
    }

    /**
     * Forwards one request to a UDP upstream.
     *
     * @param upstream UDP upstream DNS server
     * @param request  DNS request bytes
     * @param timeout  effective request timeout
     * @return upstream DNS response bytes
     */
    private static byte[] forwardUdp(final DnsUpstream upstream, final byte[] request, final Duration timeout) {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(timeoutMillis(timeout));
            final DatagramPacket query = new DatagramPacket(request, request.length, upstream.socketAddress());
            socket.send(query);
            final byte[] response = new byte[DnsCodec.MAX_MESSAGE_BYTES];
            final DatagramPacket packet = new DatagramPacket(response, response.length);
            socket.receive(packet);
            final byte[] copy = new byte[packet.getLength()];
            System.arraycopy(packet.getData(), packet.getOffset(), copy, 0, packet.getLength());
            return copy;
        } catch (final SocketTimeoutException e) {
            throw new SocketException("DNS upstream timed out: " + upstream.authority(), e);
        } catch (final IOException e) {
            throw new SocketException("DNS upstream failed: " + upstream.authority(), e);
        }
    }

    /**
     * Forwards one request to a TCP upstream.
     *
     * @param upstream TCP upstream DNS server
     * @param request  DNS request bytes
     * @param timeout  effective request timeout
     * @return upstream DNS response bytes
     */
    private static byte[] forwardTcp(final DnsUpstream upstream, final byte[] request, final Duration timeout) {
        try (Socket socket = new Socket()) {
            socket.connect(upstream.socketAddress(), timeoutMillis(timeout));
            socket.setSoTimeout(timeoutMillis(timeout));
            return exchangeLengthPrefixed(socket, request, "DNS TCP upstream");
        } catch (final SocketTimeoutException e) {
            throw new SocketException("DNS TCP upstream timed out: " + upstream.authority(), e);
        } catch (final IOException e) {
            throw new SocketException("DNS TCP upstream failed: " + upstream.authority(), e);
        }
    }

    /**
     * Forwards one request to a DNS-over-TLS upstream.
     *
     * @param upstream DoT upstream DNS server
     * @param request  DNS request bytes
     * @param timeout  effective request timeout
     * @return upstream DNS response bytes
     */
    private static byte[] forwardDot(final DnsUpstream upstream, final byte[] request, final Duration timeout) {
        try (SSLSocket socket = (SSLSocket) socketFactory(upstream.tlsPolicy()).createSocket()) {
            socket.connect(upstream.socketAddress(), timeoutMillis(timeout));
            socket.setSoTimeout(timeoutMillis(timeout));
            configureTlsSocket(socket, upstream);
            socket.startHandshake();
            return exchangeLengthPrefixed(socket, request, "DNS-over-TLS upstream");
        } catch (final SocketTimeoutException e) {
            throw new SocketException("DNS-over-TLS upstream timed out: " + upstream.authority(), e);
        } catch (final IOException e) {
            throw new SocketException("DNS-over-TLS upstream failed: " + upstream.authority(), e);
        }
    }

    /**
     * Forwards one request to a DNS-over-HTTPS upstream.
     *
     * @param upstream DoH upstream DNS server
     * @param request  DNS request bytes
     * @param timeout  effective request timeout
     * @return upstream DNS response bytes
     */
    private static byte[] forwardDoh(final DnsUpstream upstream, final byte[] request, final Duration timeout) {
        try {
            final HttpRequest httpRequest = HttpRequest.newBuilder(upstream.endpointUri()).timeout(timeout)
                    .header(Http.Header.ACCEPT, MediaType.APPLICATION_DNS_MESSAGE)
                    .header(Http.Header.CONTENT_TYPE, MediaType.APPLICATION_DNS_MESSAGE)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(request)).build();
            final HttpResponse<byte[]> response = httpClient(upstream, timeout)
                    .send(httpRequest, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() != 200) {
                throw new SocketException("DNS-over-HTTPS upstream returned HTTP " + response.statusCode());
            }
            final byte[] body = response.body();
            if (body == null || body.length == 0 || body.length > DnsCodec.MAX_MESSAGE_BYTES) {
                throw new SocketException("DNS-over-HTTPS upstream returned an invalid DNS message length");
            }
            return body;
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SocketException("DNS-over-HTTPS upstream was interrupted", e);
        } catch (final IOException e) {
            throw new SocketException("DNS-over-HTTPS upstream failed: " + upstream.endpointUri(), e);
        }
    }

    /**
     * Fails a DNS-over-QUIC upstream when the isolated QUIC adapter is unavailable.
     *
     * @param upstream DoQ upstream definition
     * @return never returns normally
     */
    private static byte[] forwardDoq(final DnsUpstream upstream) {
        final String label = "upstream " + upstream.authority();
        DnsQuicRuntime.requireAvailable(label);
        throw DnsQuicRuntime.adapterUnavailable(label);
    }

    /**
     * Exchanges one length-prefixed DNS request over an already connected stream socket.
     *
     * @param socket  connected stream socket
     * @param request DNS request bytes
     * @param label   diagnostic upstream label
     * @return DNS response bytes
     * @throws IOException if the stream fails
     */
    private static byte[] exchangeLengthPrefixed(final Socket socket, final byte[] request, final String label)
            throws IOException {
        final DataOutputStream output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        final DataInputStream input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        output.writeShort(request.length);
        output.write(request);
        output.flush();
        final int length = input.readUnsignedShort();
        if (length <= 0 || length > DnsCodec.MAX_MESSAGE_BYTES) {
            throw new SocketException(label + " returned an invalid response length");
        }
        final byte[] response = input.readNBytes(length);
        if (response.length != length) {
            throw new SocketException(label + " response was truncated");
        }
        return response;
    }

    /**
     * Builds an HTTP client for one DNS-over-HTTPS forwarding attempt.
     *
     * @param upstream DoH upstream definition
     * @param timeout  effective connection timeout
     * @return configured HTTP client
     */
    private static HttpClient httpClient(final DnsUpstream upstream, final Duration timeout) {
        final HttpClient.Builder builder = HttpClient.newBuilder().connectTimeout(timeout)
                .version(HttpClient.Version.HTTP_2);
        if (upstream.tlsPolicy() != null) {
            builder.sslContext(upstream.tlsPolicy().context().context());
            builder.sslParameters(sslParameters(upstream.tlsPolicy()));
        }
        return builder.build();
    }

    /**
     * Builds a SERVFAIL response when no more budgeted attempts may be sent.
     *
     * @param request original DNS request wire bytes
     * @return SERVFAIL response wire bytes
     */
    private static byte[] servfail(final byte[] request) {
        final DnsQuery query = DnsCodec.decodeQuery(request);
        return DnsCodec.encodeResponse(DnsResponse.empty(query, DnsResponseCode.SERVFAIL, false));
    }

    /**
     * Converts a positive duration to a socket timeout in milliseconds.
     *
     * @param timeout effective timeout
     * @return timeout milliseconds from 1 through {@link Integer#MAX_VALUE}
     */
    private static int timeoutMillis(final Duration timeout) {
        if (timeout == null || timeout.isNegative() || timeout.isZero()) {
            throw new ValidateException("DNS upstream effective timeout must be positive");
        }
        final long millis = timeout.toMillis();
        if (millis <= 0L) {
            return 1;
        }
        return millis > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) millis;
    }

    /**
     * Creates an SSL socket factory for a secure upstream.
     *
     * @param tlsPolicy TLS policy, or {@code null} for the JDK default context
     * @return SSL socket factory
     */
    private static SSLSocketFactory socketFactory(final TlsPolicy tlsPolicy) {
        return tlsPolicy == null ? (SSLSocketFactory) SSLSocketFactory.getDefault()
                : tlsPolicy.context().context().getSocketFactory();
    }

    /**
     * Configures a TLS client socket from an upstream TLS policy.
     *
     * @param socket   TLS socket
     * @param upstream upstream definition
     */
    private static void configureTlsSocket(final SSLSocket socket, final DnsUpstream upstream) {
        socket.setUseClientMode(true);
        final SSLParameters parameters = sslParameters(upstream.tlsPolicy());
        if (parameters.getApplicationProtocols().length == 0) {
            parameters.setApplicationProtocols(new String[] { DnsTransport.DOT.alpn() });
        }
        socket.setSSLParameters(parameters);
        if (upstream.tlsPolicy() != null) {
            final TlsSettings settings = upstream.tlsPolicy().settings();
            if (!settings.versions().isEmpty()) {
                socket.setEnabledProtocols(settings.versions().toArray(String[]::new));
            }
            if (!settings.ciphers().isEmpty()) {
                socket.setEnabledCipherSuites(settings.ciphers().toArray(String[]::new));
            }
        }
    }

    /**
     * Builds SSL parameters for an upstream TLS client.
     *
     * @param tlsPolicy TLS policy, or {@code null} for default protocol and cipher settings
     * @return SSL parameters with endpoint identification enabled
     */
    private static SSLParameters sslParameters(final TlsPolicy tlsPolicy) {
        final SSLParameters parameters = tlsPolicy == null ? new SSLParameters()
                : tlsPolicy.context().context().getDefaultSSLParameters();
        if (tlsPolicy != null) {
            final TlsSettings settings = tlsPolicy.settings();
            if (!settings.versions().isEmpty()) {
                parameters.setProtocols(settings.versions().toArray(String[]::new));
            }
            if (!settings.ciphers().isEmpty()) {
                parameters.setCipherSuites(settings.ciphers().toArray(String[]::new));
            }
            if (settings.supportsTlsExtensions() && !settings.applicationProtocols().isEmpty()) {
                parameters.setApplicationProtocols(settings.applicationProtocols().toArray(String[]::new));
            }
        }
        if (tlsPolicy == null || tlsPolicy.settings().verifyHostname()) {
            parameters.setEndpointIdentificationAlgorithm("HTTPS");
        }
        return parameters;
    }

}
