/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.http.accord;

import org.miaixz.bus.http.metric.Internal;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ProtocolException;
import java.net.UnknownServiceException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.List;

/**
 * Handles the fallback strategy for connection specifications: when a secure socket connection fails due to a handshake
 * or protocol issue, the connection may be retried with a different protocol. Instances are stateful and should be
 * created and used for a single connection attempt.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class ConnectionSelector {

    /**
     * The list of connection suites to try.
     */
    private final List<ConnectionSuite> connectionSuites;
    /**
     * The index of the next mode to try.
     */
    private int nextModeIndex;
    /**
     * Whether a fallback is possible.
     */
    private boolean isFallbackPossible;
    /**
     * Whether this is a fallback attempt.
     */
    private boolean isFallback;

    /**
     * Constructor
     *
     * @param connectionSuites The list of connection suites.
     */
    public ConnectionSelector(List<ConnectionSuite> connectionSuites) {
        this.nextModeIndex = 0;
        this.connectionSuites = connectionSuites;
    }

    /**
     * Configures the {@link SSLSocket} with the appropriate {@link ConnectionSuite} for connecting to the specified
     * host. Returns a {@link ConnectionSuite} and will not return {@code null}.
     *
     * @param sslSocket The SSL socket to configure.
     * @return The configuration for the socket connection.
     * @throws IOException if the socket does not support any of the available TLS modes.
     */
    public ConnectionSuite configureSecureSocket(SSLSocket sslSocket) throws IOException {
        ConnectionSuite tlsConfiguration = null;
        for (int i = nextModeIndex, size = connectionSuites.size(); i < size; i++) {
            ConnectionSuite connectionSuite = connectionSuites.get(i);
            if (connectionSuite.isCompatible(sslSocket)) {
                tlsConfiguration = connectionSuite;
                nextModeIndex = i + 1;
                break;
            }
        }

        if (null == tlsConfiguration) {
            // This could be the first attempt to connect, and the socket does not support any required protocols.
            // Or it could be a retry (but this socket supports fewer protocols than the previous socket suggested).
            throw new UnknownServiceException(
                    "Unable to find acceptable protocols. isFallback=" + isFallback + ", modes=" + connectionSuites
                            + ", supported protocols=" + Arrays.toString(sslSocket.getEnabledProtocols()));
        }

        isFallbackPossible = isFallbackPossible(sslSocket);

        Internal.instance.apply(tlsConfiguration, sslSocket, isFallback);

        return tlsConfiguration;
    }

    /**
     * Reports a connection failure. Determines the next {@link ConnectionSuite} to try, if any.
     *
     * @param ex The exception that occurred.
     * @return {@code true} if the connection should be retried with {@link #configureSecureSocket(SSLSocket)}, or
     *         {@code false} if no further retries are necessary.
     */
    public boolean connectionFailed(IOException ex) {
        // Any future attempts to connect using this strategy will be a fallback attempt.
        isFallback = true;

        if (!isFallbackPossible) {
            return false;
        }

        // Don't recover from protocol problems.
        if (ex instanceof ProtocolException) {
            return false;
        }

        // Don't recover from interruptions or timeouts (SocketTimeoutException). For socket connection timeouts,
        // we do not try the same host with a different ConnectionSpec: assume the host is unreachable.
        if (ex instanceof InterruptedIOException) {
            return false;
        }

        // Look for known client or negotiation errors that are unlikely to be fixed by trying again with a different
        // connection specification.
        if (ex instanceof SSLHandshakeException) {
            // If the problem is a certificate exception from the X509TrustManager, then we won't retry.
            if (ex.getCause() instanceof CertificateException) {
                return false;
            }
        }
        if (ex instanceof SSLPeerUnverifiedException) {
            // For example, a certificate not permitted error.
            return false;
        }

        // Retry all other SSL failures.
        return ex instanceof SSLException;
    }

    /**
     * Returns {@code true} if any subsequent {@link ConnectionSuite} in the fallback strategy appears to be possible,
     * given the provided {@link SSLSocket}. Assumes the socket has the same capabilities as the provided socket.
     *
     * @param socket The SSL socket.
     * @return {@code true} if a fallback is possible.
     */
    private boolean isFallbackPossible(SSLSocket socket) {
        for (int i = nextModeIndex; i < connectionSuites.size(); i++) {
            if (connectionSuites.get(i).isCompatible(socket)) {
                return true;
            }
        }
        return false;
    }

}
