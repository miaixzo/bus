/*
 * The MIT License
 *
 * Copyright (c) 2015-2020 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.http.metric;

import org.aoju.bus.http.Builder;
import org.aoju.bus.http.accord.ConnectionSuite;
import org.aoju.bus.http.secure.CipherSuite;
import org.aoju.bus.http.secure.TlsVersion;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.security.Principal;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;

/**
 * TLS握手的记录。对于HTTPS客户机，客户机是local，远程服务器
 * 此值对象描述完成的握手。使用{@link ConnectionSuite}设置新的握手策略
 *
 * @author Kimi Liu
 * @version 5.6.1
 * @since JDK 1.8+
 */
public final class Handshake {

    /**
     * 用于此连接的TLS版本。在Httpd 3.0之前没有跟踪这个值。
     * 对于之前版本缓存的响应，它返回{@link TlsVersion#SSL_3_0}
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

    private Handshake(TlsVersion tlsVersion, CipherSuite cipherSuite,
                      List<Certificate> peerCertificates, List<Certificate> localCertificates) {
        this.tlsVersion = tlsVersion;
        this.cipherSuite = cipherSuite;
        this.peerCertificates = peerCertificates;
        this.localCertificates = localCertificates;
    }

    public static Handshake get(SSLSession session) throws IOException {
        String cipherSuiteString = session.getCipherSuite();
        if (cipherSuiteString == null) throw new IllegalStateException("cipherSuite == null");
        if ("SSL_NULL_WITH_NULL_NULL".equals(cipherSuiteString)) {
            throw new IOException("cipherSuite == SSL_NULL_WITH_NULL_NULL");
        }
        CipherSuite cipherSuite = CipherSuite.forJavaName(cipherSuiteString);

        String tlsVersionString = session.getProtocol();
        if (tlsVersionString == null) throw new IllegalStateException("tlsVersion == null");
        if ("NONE".equals(tlsVersionString)) throw new IOException("tlsVersion == NONE");
        TlsVersion tlsVersion = TlsVersion.forJavaName(tlsVersionString);

        Certificate[] peerCertificates;
        try {
            peerCertificates = session.getPeerCertificates();
        } catch (SSLPeerUnverifiedException ignored) {
            peerCertificates = null;
        }
        List<Certificate> peerCertificatesList = peerCertificates != null
                ? Builder.immutableList(peerCertificates)
                : Collections.emptyList();

        Certificate[] localCertificates = session.getLocalCertificates();
        List<Certificate> localCertificatesList = localCertificates != null
                ? Builder.immutableList(localCertificates)
                : Collections.emptyList();

        return new Handshake(tlsVersion, cipherSuite, peerCertificatesList, localCertificatesList);
    }

    public static Handshake get(TlsVersion tlsVersion, CipherSuite cipherSuite,
                                List<Certificate> peerCertificates, List<Certificate> localCertificates) {
        if (tlsVersion == null) throw new NullPointerException("tlsVersion == null");
        if (cipherSuite == null) throw new NullPointerException("cipherSuite == null");
        return new Handshake(tlsVersion, cipherSuite, Builder.immutableList(peerCertificates),
                Builder.immutableList(localCertificates));
    }

    public TlsVersion tlsVersion() {
        return tlsVersion;
    }

    public CipherSuite cipherSuite() {
        return cipherSuite;
    }

    public List<Certificate> peerCertificates() {
        return peerCertificates;
    }

    public Principal peerPrincipal() {
        return !peerCertificates.isEmpty()
                ? ((X509Certificate) peerCertificates.get(0)).getSubjectX500Principal()
                : null;
    }

    public List<Certificate> localCertificates() {
        return localCertificates;
    }

    public Principal localPrincipal() {
        return !localCertificates.isEmpty()
                ? ((X509Certificate) localCertificates.get(0)).getSubjectX500Principal()
                : null;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Handshake)) return false;
        Handshake that = (Handshake) other;
        return tlsVersion.equals(that.tlsVersion)
                && cipherSuite.equals(that.cipherSuite)
                && peerCertificates.equals(that.peerCertificates)
                && localCertificates.equals(that.localCertificates);
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

}
