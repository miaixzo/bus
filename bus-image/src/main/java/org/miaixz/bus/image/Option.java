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
package org.miaixz.bus.image;

import java.util.List;

import lombok.*;
import lombok.Builder;

import org.miaixz.bus.image.metric.Connection;

/**
 * Represents configurable options for a DICOM connection or request.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Option {

    /**
     * Default TLS cipher suites kept for legacy DICOM peer compatibility.
     */
    public static final List<String> TLS = List.of(
            Connection.TLS_RSA_WITH_NULL_SHA,
            Connection.TLS_RSA_WITH_AES_128_CBC_SHA,
            Connection.TLS_RSA_WITH_3DES_EDE_CBC_SHA);

    /**
     * Null encryption cipher suite, mainly useful for compatibility tests.
     */
    public static final List<String> TLS_NULL = List.of(Connection.TLS_RSA_WITH_NULL_SHA);

    /**
     * 3DES cipher suite.
     */
    public static final List<String> TLS_3DES = List.of(Connection.TLS_RSA_WITH_3DES_EDE_CBC_SHA);

    /**
     * AES-first cipher suite order.
     */
    public static final List<String> TLS_AES = List
            .of(Connection.TLS_RSA_WITH_AES_128_CBC_SHA, Connection.TLS_RSA_WITH_3DES_EDE_CBC_SHA);

    /**
     * Legacy protocol defaults.
     */
    public static final List<String> DEFAULT_PROTOCOLS = List.of("TLSv1", "SSLv3");

    /**
     * The tls 1 0 value.
     */
    public static final List<String> TLS_1_0 = List.of("TLSv1");

    /**
     * The tls 1 1 value.
     */
    public static final List<String> TLS_1_1 = List.of("TLSv1.1");

    /**
     * The tls 1 2 value.
     */
    public static final List<String> TLS_1_2 = List.of("TLSv1.2");

    /**
     * The tls 1 3 value.
     */
    public static final List<String> TLS_1_3 = List.of("TLSv1.3");

    /**
     * The ssl 3 value.
     */
    public static final List<String> SSL_3 = List.of("SSLv3");

    /**
     * The ssl2 hello value.
     */
    public static final List<String> SSL2_HELLO = List.of("SSLv2Hello", "SSLv3", "TLSv1", "TLSv1.1", "TLSv1.2");

    /**
     * Recommended protocol set for new connections.
     */
    public static final List<String> MODERN_TLS = List.of("TLSv1.2", "TLSv1.3");

    /**
     * The maximum number of operations that can be invoked asynchronously on this AE. 0 means unlimited, 1 means
     * synchronous mode.
     */
    @Builder.Default
    private int maxOpsInvoked = Connection.SYNCHRONOUS_MODE;

    /**
     * The maximum number of operations that can be performed asynchronously on this AE. 0 means unlimited, 1 means
     * synchronous mode.
     */
    @Builder.Default
    private int maxOpsPerformed = Connection.SYNCHRONOUS_MODE;

    /**
     * Maximum PDU length for receiving.
     */
    @Builder.Default
    private int maxPdulenRcv = Connection.DEF_MAX_PDU_LENGTH;

    /**
     * Maximum PDU length for sending.
     */
    @Builder.Default
    private int maxPdulenSnd = Connection.DEF_MAX_PDU_LENGTH;

    /**
     * Whether to pack PDVs.
     */
    @Builder.Default
    private boolean packPDV = true;

    /**
     * The socket connection backlog.
     */
    @Builder.Default
    private int backlog = Connection.DEF_BACKLOG;

    /**
     * Connection timeout in milliseconds.
     */
    private int connectTimeout;

    /**
     * Request timeout in milliseconds.
     */
    private int requestTimeout;

    /**
     * Accept timeout in milliseconds.
     */
    private int acceptTimeout;

    /**
     * Release timeout in milliseconds.
     */
    private int releaseTimeout;

    /**
     * Response timeout in milliseconds.
     */
    private int responseTimeout;

    /**
     * Retrieve timeout in milliseconds.
     */
    private int retrieveTimeout;

    /**
     * Idle timeout in milliseconds.
     */
    private int idleTimeout;

    /**
     * Socket close delay (SO_LINGER) in seconds.
     */
    @Builder.Default
    private int socloseDelay = Connection.DEF_SOCKETDELAY;

    /**
     * Socket send buffer size (SO_SNDBUF).
     */
    private int sosndBuffer;

    /**
     * Socket receive buffer size (SO_RCVBUF).
     */
    private int sorcvBuffer;

    /**
     * Whether to enable TCP_NODELAY.
     */
    @Builder.Default
    private boolean tcpNoDelay = true;

    /**
     * A list of enabled TLS cipher suites.
     */
    @Builder.Default
    private List<String> cipherSuites = TLS;

    /**
     * A list of enabled TLS protocols.
     */
    @Builder.Default
    private List<String> tlsProtocols = DEFAULT_PROTOCOLS;

    /**
     * Whether client authentication is required for TLS.
     */
    private boolean tlsNeedClientAuth;

    /**
     * URL of the keystore.
     */
    private String keystoreURL;

    /**
     * Type of the keystore (e.g., JKS, PKCS12).
     */
    private String keystoreType;

    /**
     * Password for the keystore.
     */
    private String keystorePass;

    /**
     * Password for the private key within the keystore.
     */
    private String keyPass;

    /**
     * URL of the truststore.
     */
    private String truststoreURL;

    /**
     * Type of the truststore.
     */
    private String truststoreType;

    /**
     * Password for the truststore.
     */
    private String truststorePass;

    /**
     * Creates connection options with bus-image defaults.
     *
     * @return A new option instance.
     */
    public static Option connectDefaults() {
        return new Option();
    }

    /**
     * Creates TLS options using the modern protocol set while keeping the existing Option carrier type.
     *
     * @param tlsNeedClientAuth whether client authentication is required.
     * @param keystoreURL       keystore location.
     * @param keystoreType      keystore type.
     * @param keystorePass      keystore password.
     * @param keyPass           private key password.
     * @param truststoreURL     truststore location.
     * @param truststoreType    truststore type.
     * @param truststorePass    truststore password.
     * @return A new option instance.
     */
    public static Option modernTls(
            boolean tlsNeedClientAuth,
            String keystoreURL,
            String keystoreType,
            String keystorePass,
            String keyPass,
            String truststoreURL,
            String truststoreType,
            String truststorePass) {
        return new Option().withTls(
                TLS,
                MODERN_TLS,
                tlsNeedClientAuth,
                keystoreURL,
                keystoreType,
                keystorePass,
                keyPass,
                truststoreURL,
                truststoreType,
                truststorePass);
    }

    /**
     * Creates a copy configured with the async ops.
     *
     * @param maxOpsInvoked   the max ops invoked.
     * @param maxOpsPerformed the max ops performed.
     * @return the operation result.
     */
    public Option withAsyncOps(int maxOpsInvoked, int maxOpsPerformed) {
        this.maxOpsInvoked = maxOpsInvoked;
        this.maxOpsPerformed = maxOpsPerformed;
        return this;
    }

    /**
     * Creates a copy configured with the pdu length.
     *
     * @param maxPdulenRcv the max pdulen rcv.
     * @param maxPdulenSnd the max pdulen snd.
     * @return the operation result.
     */
    public Option withPduLength(int maxPdulenRcv, int maxPdulenSnd) {
        this.maxPdulenRcv = maxPdulenRcv;
        this.maxPdulenSnd = maxPdulenSnd;
        return this;
    }

    /**
     * Creates a copy configured with the timeouts.
     *
     * @param connectTimeout  the connect timeout.
     * @param requestTimeout  the request timeout.
     * @param acceptTimeout   the accept timeout.
     * @param releaseTimeout  the release timeout.
     * @param responseTimeout the response timeout.
     * @param retrieveTimeout the retrieve timeout.
     * @param idleTimeout     the idle timeout.
     * @return the operation result.
     */
    public Option withTimeouts(
            int connectTimeout,
            int requestTimeout,
            int acceptTimeout,
            int releaseTimeout,
            int responseTimeout,
            int retrieveTimeout,
            int idleTimeout) {
        this.connectTimeout = connectTimeout;
        this.requestTimeout = requestTimeout;
        this.acceptTimeout = acceptTimeout;
        this.releaseTimeout = releaseTimeout;
        this.responseTimeout = responseTimeout;
        this.retrieveTimeout = retrieveTimeout;
        this.idleTimeout = idleTimeout;
        return this;
    }

    /**
     * Creates a copy configured with the socket options.
     *
     * @param sosndBuffer  the sosnd buffer.
     * @param sorcvBuffer  the sorcv buffer.
     * @param socloseDelay the soclose delay.
     * @param tcpNoDelay   the tcp no delay.
     * @return the operation result.
     */
    public Option withSocketOptions(int sosndBuffer, int sorcvBuffer, int socloseDelay, boolean tcpNoDelay) {
        this.sosndBuffer = sosndBuffer;
        this.sorcvBuffer = sorcvBuffer;
        this.socloseDelay = socloseDelay;
        this.tcpNoDelay = tcpNoDelay;
        return this;
    }

    /**
     * Creates a copy configured with the tls.
     *
     * @param cipherSuites      the cipher suites.
     * @param tlsProtocols      the tls protocols.
     * @param tlsNeedClientAuth the tls need client auth.
     * @param keystoreURL       the keystore url.
     * @param keystoreType      the keystore type.
     * @param keystorePass      the keystore pass.
     * @param keyPass           the key pass.
     * @param truststoreURL     the truststore url.
     * @param truststoreType    the truststore type.
     * @param truststorePass    the truststore pass.
     * @return the operation result.
     */
    public Option withTls(
            List<String> cipherSuites,
            List<String> tlsProtocols,
            boolean tlsNeedClientAuth,
            String keystoreURL,
            String keystoreType,
            String keystorePass,
            String keyPass,
            String truststoreURL,
            String truststoreType,
            String truststorePass) {
        setCipherSuites(cipherSuites);
        setTlsProtocols(tlsProtocols);
        this.tlsNeedClientAuth = tlsNeedClientAuth;
        this.keystoreURL = keystoreURL;
        this.keystoreType = keystoreType;
        this.keystorePass = keystorePass;
        this.keyPass = keyPass;
        this.truststoreURL = truststoreURL;
        this.truststoreType = truststoreType;
        this.truststorePass = truststorePass;
        return this;
    }

    /**
     * Gets the cipher suites.
     *
     * @return the cipher suites.
     */
    public List<String> getCipherSuites() {
        return immutableList(cipherSuites);
    }

    /**
     * Sets the cipher suites.
     *
     * @param cipherSuites the cipher suites.
     */
    public void setCipherSuites(List<String> cipherSuites) {
        this.cipherSuites = immutableList(cipherSuites);
    }

    /**
     * Gets the tls protocols.
     *
     * @return the tls protocols.
     */
    public List<String> getTlsProtocols() {
        return immutableList(tlsProtocols);
    }

    /**
     * Sets the tls protocols.
     *
     * @param tlsProtocols the tls protocols.
     */
    public void setTlsProtocols(List<String> tlsProtocols) {
        this.tlsProtocols = immutableList(tlsProtocols);
    }

    /**
     * Executes the immutable list operation.
     *
     * @param values the values.
     * @return the operation result.
     */
    private static List<String> immutableList(List<String> values) {
        return values == null ? null : List.copyOf(values);
    }

}
