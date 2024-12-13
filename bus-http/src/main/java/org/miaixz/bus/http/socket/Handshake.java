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
package org.miaixz.bus.http.socket;

import java.io.IOException;
import java.security.Principal;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;

import org.miaixz.bus.core.net.tls.TlsVersion;
import org.miaixz.bus.http.Builder;
import org.miaixz.bus.http.accord.ConnectionSuite;
import org.miaixz.bus.http.secure.CipherSuite;

/**
 * TLS握手的记录。对于HTTPS客户机，客户机是local，远程服务器 此值对象描述完成的握手。使用{@link ConnectionSuite}设置新的握手策略
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Handshake {

    /**
     * 用于此连接的TLS版本。在Httpd 3.0之前没有跟踪这个值。 对于之前版本缓存的响应，它返回{@link TlsVersion#SSLv3}
     */
    private final TlsVersion tlsVersion;
    /**
     * 用于连接的密码套件
     */
    private final CipherSuite cipherSuite;
    /**
     * 标识远程对等点的证书列表，该列表可能为空
     */
    private final List<Certificate> peerCertificates;
    /**
     * 标识此对等点的证书列表，该列表可能为空
     */
    private final List<Certificate> localCertificates;

    private Handshake(TlsVersion tlsVersion, CipherSuite cipherSuite, List<Certificate> peerCertificates,
            List<Certificate> localCertificates) {
        this.tlsVersion = tlsVersion;
        this.cipherSuite = cipherSuite;
        this.peerCertificates = peerCertificates;
        this.localCertificates = localCertificates;
    }

    public static Handshake get(SSLSession session) throws IOException {
        String cipherSuiteString = session.getCipherSuite();
        if (null == cipherSuiteString) {
            throw new IllegalStateException("cipherSuite == null");
        }
        if ("SSL_NULL_WITH_NULL_NULL".equals(cipherSuiteString)) {
            throw new IOException("cipherSuite == SSL_NULL_WITH_NULL_NULL");
        }
        CipherSuite cipherSuite = CipherSuite.forJavaName(cipherSuiteString);

        String tlsVersionString = session.getProtocol();
        if (null == tlsVersionString) {
            throw new IllegalStateException("tlsVersion == null");
        }
        if ("NONE".equals(tlsVersionString))
            throw new IOException("tlsVersion == NONE");
        TlsVersion tlsVersion = TlsVersion.forJavaName(tlsVersionString);

        Certificate[] peerCertificates;
        try {
            peerCertificates = session.getPeerCertificates();
        } catch (SSLPeerUnverifiedException ignored) {
            peerCertificates = null;
        }
        List<Certificate> peerCertificatesList = null != peerCertificates ? Builder.immutableList(peerCertificates)
                : Collections.emptyList();

        Certificate[] localCertificates = session.getLocalCertificates();
        List<Certificate> localCertificatesList = null != localCertificates ? Builder.immutableList(localCertificates)
                : Collections.emptyList();

        return new Handshake(tlsVersion, cipherSuite, peerCertificatesList, localCertificatesList);
    }

    public static Handshake get(TlsVersion tlsVersion, CipherSuite cipherSuite, List<Certificate> peerCertificates,
            List<Certificate> localCertificates) {
        if (tlsVersion == null)
            throw new NullPointerException("tlsVersion == null");
        if (cipherSuite == null)
            throw new NullPointerException("cipherSuite == null");
        return new Handshake(tlsVersion, cipherSuite, Builder.immutableList(peerCertificates),
                Builder.immutableList(localCertificates));
    }

    /**
     * Returns the TLS version used for this connection. This value wasn't tracked prior to Http 3.0. For responses
     * cached by preceding versions this returns {@link TlsVersion#SSLv3}.
     */
    public TlsVersion tlsVersion() {
        return tlsVersion;
    }

    /**
     * Returns the cipher suite used for the connection.
     */
    public CipherSuite cipherSuite() {
        return cipherSuite;
    }

    /**
     * Returns a possibly-empty list of certificates that identify the remote peer.
     */
    public List<Certificate> peerCertificates() {
        return peerCertificates;
    }

    /**
     * Returns the remote peer's principle, or null if that peer is anonymous.
     */
    public Principal peerPrincipal() {
        return !peerCertificates.isEmpty() ? ((X509Certificate) peerCertificates.get(0)).getSubjectX500Principal()
                : null;
    }

    /**
     * Returns a possibly-empty list of certificates that identify this peer.
     */
    public List<Certificate> localCertificates() {
        return localCertificates;
    }

    /**
     * Returns the local principle, or null if this peer is anonymous.
     */
    public Principal localPrincipal() {
        return !localCertificates.isEmpty() ? ((X509Certificate) localCertificates.get(0)).getSubjectX500Principal()
                : null;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Handshake))
            return false;
        Handshake that = (Handshake) other;
        return tlsVersion.equals(that.tlsVersion) && cipherSuite.equals(that.cipherSuite)
                && peerCertificates.equals(that.peerCertificates) && localCertificates.equals(that.localCertificates);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + tlsVersion.hashCode();
        result = 31 * result + cipherSuite.hashCode();
        result = 31 * result + peerCertificates.hashCode();
        result = 31 * result + localCertificates.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Handshake{" + "tlsVersion=" + tlsVersion + " cipherSuite=" + cipherSuite + " peerCertificates="
                + names(peerCertificates) + " localCertificates=" + names(localCertificates) + '}';
    }

    private List<String> names(List<Certificate> certificates) {
        List<String> strings = new ArrayList<>();

        for (Certificate cert : certificates) {
            if (cert instanceof X509Certificate) {
                strings.add(String.valueOf(((X509Certificate) cert).getSubjectDN()));
            } else {
                strings.add(cert.getType());
            }
        }

        return strings;
    }

}
