/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
 * 指定HTTP传输通过的套接字连接的配置。对于{@code https:} url，这包括在协商安全连接时要使用 的TLS版本和密码套件,只有在SSL套接字中也启用了连接规范中配置的TLS版本时，才会使用它们。例如，
 * 如果SSL套接字没有启用TLS 1.3，即使它在连接规范中出现，也不会被使用。同样的策略也适用于密码套件
 * 使用{@link Builder#allEnabledTlsVersions()}和{@link Builder#allEnabledCipherSuites} 将所有特性选择延迟到底层SSL套接字
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ConnectionSuite {

    /**
     * 用于{@code http:} url的未加密、未经身份验证的连接
     */
    public static final ConnectionSuite CLEARTEXT = new Builder(false).build();

    /**
     * 等于Chrome 51支持的密码套件 所有这些套件都可以在Android 7.0上使用
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

            // 请注意，以下密码套件都在HTTP/2的“坏密码套件”列表中。我们将继续包括他们，
            // 直到更好的套房是普遍可用的。例如，上面列出的更好的密码套件都没有随Android 4.4或Java 7一起发布
            CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA, CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,
            CipherSuite.TLS_RSA_WITH_AES_128_GCM_SHA256, CipherSuite.TLS_RSA_WITH_AES_256_GCM_SHA384,
            CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA, CipherSuite.TLS_RSA_WITH_AES_256_CBC_SHA,
            CipherSuite.TLS_RSA_WITH_3DES_EDE_CBC_SHA, };
    /**
     * 一个TLS连接与扩展，如SNI和ALPN可用
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

    public boolean isTls() {
        return tls;
    }

    /**
     * @return 用于连接的密码套件。如果应该使用SSL套接字的所有启用密码套件，则返回null
     */
    public List<CipherSuite> cipherSuites() {
        return null != cipherSuites ? CipherSuite.forJavaNames(cipherSuites) : null;
    }

    /**
     * @return 在协商连接时使用的TLS版本。如果应该使用SSL套接字的所有启用的TLS版本，则返回null
     */
    public List<TlsVersion> tlsVersions() {
        return null != tlsVersions ? TlsVersion.forJavaNames(tlsVersions) : null;
    }

    public boolean supportsTlsExtensions() {
        return supportsTlsExtensions;
    }

    /**
     * 将此规范应用于{@code sslSocket}
     *
     * @param sslSocket  安全套接字
     * @param isFallback 是否失败回调
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
     * {@code sslSocket}未启用的密码套件和TLS版本
     *
     * @param sslSocket  安全套接字
     * @param isFallback 是否失败回调
     * @return 返回一个副本
     */
    private ConnectionSuite supportedSuite(SSLSocket sslSocket, boolean isFallback) {
        String[] cipherSuitesIntersection = null != cipherSuites
                ? org.miaixz.bus.http.Builder.intersect(CipherSuite.ORDER_BY_NAME, sslSocket.getEnabledCipherSuites(),
                        cipherSuites)
                : sslSocket.getEnabledCipherSuites();
        String[] tlsVersionsIntersection = null != tlsVersions
                ? org.miaixz.bus.http.Builder.intersect(org.miaixz.bus.http.Builder.NATURAL_ORDER,
                        sslSocket.getEnabledProtocols(), tlsVersions)
                : sslSocket.getEnabledProtocols();

        String[] supportedCipherSuites = sslSocket.getSupportedCipherSuites();
        int indexOfFallbackScsv = org.miaixz.bus.http.Builder.indexOf(CipherSuite.ORDER_BY_NAME, supportedCipherSuites,
                "TLS_FALLBACK_SCSV");
        if (isFallback && indexOfFallbackScsv != -1) {
            cipherSuitesIntersection = org.miaixz.bus.http.Builder.concat(cipherSuitesIntersection,
                    supportedCipherSuites[indexOfFallbackScsv]);
        }

        return new Builder(this).cipherSuites(cipherSuitesIntersection).tlsVersions(tlsVersionsIntersection).build();
    }

    /**
     * 如果当前配置的套接字支持此连接规范，则返回{@code true} 为了使套接字兼容，启用的密码套件和协议必须相交 对于密码套件，{@link #cipherSuites() required cipher
     * suites}中至少有一个必须与套接字启用的密码 套件匹配。如果不需要密码套件，则套接字必须至少启用一个密码套件 对于协议，{@link #tlsVersions() required
     * protocols}中至少有一个必须与套接字启用的协议匹配
     *
     * @param socket 安全套接字
     * @return the true/false
     */
    public boolean isCompatible(SSLSocket socket) {
        if (!tls) {
            return false;
        }

        if (null != tlsVersions
                && !org.miaixz.bus.http.Builder.nonEmptyIntersection(org.miaixz.bus.http.Builder.NATURAL_ORDER,
                        tlsVersions, socket.getEnabledProtocols())) {
            return false;
        }

        if (null != cipherSuites && !org.miaixz.bus.http.Builder.nonEmptyIntersection(CipherSuite.ORDER_BY_NAME,
                cipherSuites, socket.getEnabledCipherSuites())) {
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

    public static class Builder {

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

        public Builder allEnabledCipherSuites() {
            if (!tls)
                throw new IllegalStateException("no cipher suites for cleartext connections");
            this.cipherSuites = null;
            return this;
        }

        public Builder cipherSuites(CipherSuite... cipherSuites) {
            if (!tls)
                throw new IllegalStateException("no cipher suites for cleartext connections");

            String[] strings = new String[cipherSuites.length];
            for (int i = 0; i < cipherSuites.length; i++) {
                strings[i] = cipherSuites[i].javaName;
            }
            return cipherSuites(strings);
        }

        public Builder cipherSuites(String... cipherSuites) {
            if (!tls)
                throw new IllegalStateException("no cipher suites for cleartext connections");

            if (cipherSuites.length == 0) {
                throw new IllegalArgumentException("At least one cipher suite is required");
            }

            this.cipherSuites = cipherSuites.clone();
            return this;
        }

        public Builder allEnabledTlsVersions() {
            if (!tls)
                throw new IllegalStateException("no TLS versions for cleartext connections");
            this.tlsVersions = null;
            return this;
        }

        public Builder tlsVersions(TlsVersion... tlsVersions) {
            if (!tls)
                throw new IllegalStateException("no TLS versions for cleartext connections");

            String[] strings = new String[tlsVersions.length];
            for (int i = 0; i < tlsVersions.length; i++) {
                strings[i] = tlsVersions[i].javaName;
            }

            return tlsVersions(strings);
        }

        public Builder tlsVersions(String... tlsVersions) {
            if (!tls)
                throw new IllegalStateException("no TLS versions for cleartext connections");

            if (tlsVersions.length == 0) {
                throw new IllegalArgumentException("At least one TLS version is required");
            }

            this.tlsVersions = tlsVersions.clone();
            return this;
        }

        public Builder supportsTlsExtensions(boolean supportsTlsExtensions) {
            if (!tls)
                throw new IllegalStateException("no TLS extensions for cleartext connections");
            this.supportsTlsExtensions = supportsTlsExtensions;
            return this;
        }

        public ConnectionSuite build() {
            return new ConnectionSuite(this);
        }
    }

}
