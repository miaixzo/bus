/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.tls.TlsVersion;
import org.miaixz.bus.http.secure.CipherSuite;

import javax.net.ssl.SSLSocket;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Specifies the configuration for a socket connection over which HTTP is transported. For {@code https:} URLs, this
 * includes the TLS versions and cipher suites to use when negotiating a secure connection.
 * <p>
 * Only TLS versions configured in the connection specification that are also enabled in the SSL socket will be used.
 * For example, if TLS 1.3 is not enabled in the SSL socket, it will not be used even if it appears in the connection
 * specification. The same policy applies to cipher suites.
 * <p>
 * Use {@link Builder#allEnabledTlsVersions()} and {@link Builder#allEnabledCipherSuites()} to defer all feature
 * selection to the underlying SSL socket.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class ConnectionSuite {

    /**
     * An unencrypted, unauthenticated connection for {@code http:} URLs.
     */
    public static final ConnectionSuite CLEARTEXT = new Builder(false).build();

    /**
     * Cipher suites supported by Chrome 51. All of these suites are available on Android 7.0.
     */
    private static final CipherSuite[] APPROVED_CIPHER_SUITES = new CipherSuite[] {
            // TLSv1.3
            CipherSuite.TLS_AES_128_GCM_SHA256, CipherSuite.TLS_AES_256_GCM_SHA384,
            CipherSuite.TLS_CHACHA20_POLY1305_SHA256,

            // TLSv1.0, TLSv1.1, TLSv1.2
            CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256, CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
            CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384, CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,
            CipherSuite.TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256,
            CipherSuite.TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256,

            // Note that the following cipher suites are all in HTTP/2's "bad cipher suites" list.
            // We'll continue to include them until better suites are universally available.
            // For example, none of the better cipher suites listed above shipped with Android 4.4 or Java 7.
            CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA, CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,
            CipherSuite.TLS_RSA_WITH_AES_128_GCM_SHA256, CipherSuite.TLS_RSA_WITH_AES_256_GCM_SHA384,
            CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA, CipherSuite.TLS_RSA_WITH_AES_256_CBC_SHA,
            CipherSuite.TLS_RSA_WITH_3DES_EDE_CBC_SHA, };
    /**
     * A TLS connection with extensions like SNI and ALPN available.
     */
    public static final ConnectionSuite MODERN_TLS = new Builder(true).cipherSuites(APPROVED_CIPHER_SUITES)
            .tlsVersions(TlsVersion.TLSv1_3, TlsVersion.TLSv1_2).supportsTlsExtensions(true).build();

    final boolean tls;
    final boolean supportsTlsExtensions;
    final String[] cipherSuites;
    final String[] tlsVersions;

    ConnectionSuite(Builder builder) {
        this.tls = builder.tls;
        this.cipherSuites = builder.cipherSuites;
        this.tlsVersions = builder.tlsVersions;
        this.supportsTlsExtensions = builder.supportsTlsExtensions;
    }

    /**
     * Returns true if this connection suite is for TLS connections.
     *
     * @return {@code true} if this is a TLS connection suite.
     */
    public boolean isTls() {
        return tls;
    }

    /**
     * Returns the cipher suites to use for the connection. If all enabled cipher suites of the SSL socket should be
     * used, this returns null.
     *
     * @return A list of cipher suites, or null.
     */
    public List<CipherSuite> cipherSuites() {
        return null != cipherSuites ? CipherSuite.forJavaNames(cipherSuites) : null;
    }

    /**
     * Returns the TLS versions to use when negotiating a connection. If all enabled TLS versions of the SSL socket
     * should be used, this returns null.
     *
     * @return A list of TLS versions, or null.
     */
    public List<TlsVersion> tlsVersions() {
        return null != tlsVersions ? TlsVersion.forJavaNames(tlsVersions) : null;
    }

    /**
     * Returns true if TLS extensions like Server Name Indication (SNI) and Application-Layer Protocol Negotiation
     * (ALPN) should be used.
     *
     * @return {@code true} if TLS extensions are supported.
     */
    public boolean supportsTlsExtensions() {
        return supportsTlsExtensions;
    }

    /**
     * Applies this specification to the {@code sslSocket}.
     *
     * @param sslSocket  The SSL socket to configure.
     * @param isFallback Whether this is a fallback connection.
     */
    public void apply(SSLSocket sslSocket, boolean isFallback) {
        ConnectionSuite specToApply = supportedSuite(sslSocket, isFallback);

        if (null != specToApply.tlsVersions) {
            sslSocket.setEnabledProtocols(specToApply.tlsVersions);
        }
        if (null != specToApply.cipherSuites) {
            sslSocket.setEnabledCipherSuites(specToApply.cipherSuites);
        }
    }

    /**
     * Returns a connection suite that is supported by the {@code sslSocket} and this specification.
     *
     * @param sslSocket  The SSL socket.
     * @param isFallback Whether this is a fallback connection.
     * @return A supported connection suite.
     */
    private ConnectionSuite supportedSuite(SSLSocket sslSocket, boolean isFallback) {
        String[] cipherSuitesIntersection = null != cipherSuites
                ? org.miaixz.bus.http.Builder
                        .intersect(CipherSuite.ORDER_BY_NAME, sslSocket.getEnabledCipherSuites(), cipherSuites)
                : sslSocket.getEnabledCipherSuites();
        String[] tlsVersionsIntersection = null != tlsVersions ? org.miaixz.bus.http.Builder
                .intersect(org.miaixz.bus.http.Builder.NATURAL_ORDER, sslSocket.getEnabledProtocols(), tlsVersions)
                : sslSocket.getEnabledProtocols();

        String[] supportedCipherSuites = sslSocket.getSupportedCipherSuites();
        int indexOfFallbackScsv = org.miaixz.bus.http.Builder
                .indexOf(CipherSuite.ORDER_BY_NAME, supportedCipherSuites, "TLS_FALLBACK_SCSV");
        if (isFallback && indexOfFallbackScsv != -1) {
            cipherSuitesIntersection = org.miaixz.bus.http.Builder
                    .concat(cipherSuitesIntersection, supportedCipherSuites[indexOfFallbackScsv]);
        }

        return new Builder(this).cipherSuites(cipherSuitesIntersection).tlsVersions(tlsVersionsIntersection).build();
    }

    /**
     * Returns {@code true} if the currently configured socket supports this connection specification. For a socket to
     * be compatible, the enabled cipher suites and protocols must intersect. For cipher suites, at least one of the
     * {@link #cipherSuites() required cipher suites} must match an enabled cipher suite on the socket. If no cipher
     * suites are required, the socket must have at least one enabled cipher suite. For protocols, at least one of the
     * {@link #tlsVersions() required protocols} must match an enabled protocol on the socket.
     *
     * @param socket The SSL socket to check.
     * @return {@code true} if the socket is compatible.
     */
    public boolean isCompatible(SSLSocket socket) {
        if (!tls) {
            return false;
        }

        if (null != tlsVersions && !org.miaixz.bus.http.Builder.nonEmptyIntersection(
                org.miaixz.bus.http.Builder.NATURAL_ORDER,
                tlsVersions,
                socket.getEnabledProtocols())) {
            return false;
        }

        if (null != cipherSuites && !org.miaixz.bus.http.Builder
                .nonEmptyIntersection(CipherSuite.ORDER_BY_NAME, cipherSuites, socket.getEnabledCipherSuites())) {
            return false;
        }

        return true;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ConnectionSuite))
            return false;
        if (other == this)
            return true;

        ConnectionSuite that = (ConnectionSuite) other;
        if (this.tls != that.tls)
            return false;

        if (tls) {
            if (!Arrays.equals(this.cipherSuites, that.cipherSuites))
                return false;
            if (!Arrays.equals(this.tlsVersions, that.tlsVersions))
                return false;
            if (this.supportsTlsExtensions != that.supportsTlsExtensions)
                return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = 17;
        if (tls) {
            result = 31 * result + Arrays.hashCode(cipherSuites);
            result = 31 * result + Arrays.hashCode(tlsVersions);
            result = 31 * result + (supportsTlsExtensions ? 0 : 1);
        }
        return result;
    }

    @Override
    public String toString() {
        if (!tls) {
            return "ConnectionSuite()";
        }

        return "ConnectionSuite(" + "cipherSuites=" + Objects.toString(cipherSuites(), "[all enabled]")
                + ", tlsVersions=" + Objects.toString(tlsVersions(), "[all enabled]") + ", supportsTlsExtensions="
                + supportsTlsExtensions + Symbol.PARENTHESE_RIGHT;
    }

    /**
     * A builder for creating {@link ConnectionSuite} instances.
     */
    public static final class Builder {

        boolean tls;
        String[] cipherSuites;
        String[] tlsVersions;
        boolean supportsTlsExtensions;

        Builder(boolean tls) {
            this.tls = tls;
        }

        public Builder(ConnectionSuite connectionSuite) {
            this.tls = connectionSuite.tls;
            this.cipherSuites = connectionSuite.cipherSuites;
            this.tlsVersions = connectionSuite.tlsVersions;
            this.supportsTlsExtensions = connectionSuite.supportsTlsExtensions;
        }

        /**
         * Configures the connection to use all enabled cipher suites of the SSL socket.
         *
         * @return this builder.
         */
        public Builder allEnabledCipherSuites() {
            if (!tls)
                throw new IllegalStateException("no cipher suites for cleartext connections");
            this.cipherSuites = null;
            return this;
        }

        /**
         * Configures the connection to use the specified cipher suites.
         *
         * @param cipherSuites The cipher suites to use.
         * @return this builder.
         */
        public Builder cipherSuites(CipherSuite... cipherSuites) {
            if (!tls)
                throw new IllegalStateException("no cipher suites for cleartext connections");

            String[] strings = new String[cipherSuites.length];
            for (int i = 0; i < cipherSuites.length; i++) {
                strings[i] = cipherSuites[i].javaName;
            }
            return cipherSuites(strings);
        }

        /**
         * Configures the connection to use the specified cipher suites by their Java names.
         *
         * @param cipherSuites The Java names of the cipher suites to use.
         * @return this builder.
         */
        public Builder cipherSuites(String... cipherSuites) {
            if (!tls)
                throw new IllegalStateException("no cipher suites for cleartext connections");

            if (cipherSuites.length == 0) {
                throw new IllegalArgumentException("At least one cipher suite is required");
            }

            this.cipherSuites = cipherSuites.clone();
            return this;
        }

        /**
         * Configures the connection to use all enabled TLS versions of the SSL socket.
         *
         * @return this builder.
         */
        public Builder allEnabledTlsVersions() {
            if (!tls)
                throw new IllegalStateException("no TLS versions for cleartext connections");
            this.tlsVersions = null;
            return this;
        }

        /**
         * Configures the connection to use the specified TLS versions.
         *
         * @param tlsVersions The TLS versions to use.
         * @return this builder.
         */
        public Builder tlsVersions(TlsVersion... tlsVersions) {
            if (!tls)
                throw new IllegalStateException("no TLS versions for cleartext connections");

            String[] strings = new String[tlsVersions.length];
            for (int i = 0; i < tlsVersions.length; i++) {
                strings[i] = tlsVersions[i].javaName;
            }

            return tlsVersions(strings);
        }

        /**
         * Configures the connection to use the specified TLS versions by their Java names.
         *
         * @param tlsVersions The Java names of the TLS versions to use.
         * @return this builder.
         */
        public Builder tlsVersions(String... tlsVersions) {
            if (!tls)
                throw new IllegalStateException("no TLS versions for cleartext connections");

            if (tlsVersions.length == 0) {
                throw new IllegalArgumentException("At least one TLS version is required");
            }

            this.tlsVersions = tlsVersions.clone();
            return this;
        }

        /**
         * Configures whether TLS extensions like Server Name Indication (SNI) and Application-Layer Protocol
         * Negotiation (ALPN) should be used.
         *
         * @param supportsTlsExtensions {@code true} to support TLS extensions.
         * @return this builder.
         */
        public Builder supportsTlsExtensions(boolean supportsTlsExtensions) {
            if (!tls)
                throw new IllegalStateException("no TLS extensions for cleartext connections");
            this.supportsTlsExtensions = supportsTlsExtensions;
            return this;
        }

        /**
         * Builds a new {@link ConnectionSuite} instance.
         *
         * @return A new {@link ConnectionSuite} instance.
         */
        public ConnectionSuite build() {
            return new ConnectionSuite(this);
        }
    }

}
