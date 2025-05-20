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
 * TLS 握手记录
 * <p>
 * 记录 HTTPS 连接的 TLS 握手信息，包括 TLS 版本、密码套件、远程和本地证书。 用于描述已完成的握手，可通过 {@link ConnectionSuite} 配置新的握手策略。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Handshake {

    /**
     * TLS 版本
     */
    private final TlsVersion tlsVersion;
    /**
     * 密码套件
     */
    private final CipherSuite cipherSuite;
    /**
     * 远程对等点证书列表
     */
    private final List<Certificate> peerCertificates;
    /**
     * 本地证书列表
     */
    private final List<Certificate> localCertificates;

    /**
     * 构造函数，初始化 Handshake 实例
     *
     * @param tlsVersion        TLS 版本
     * @param cipherSuite       密码套件
     * @param peerCertificates  远程对等点证书列表
     * @param localCertificates 本地证书列表
     */
    private Handshake(TlsVersion tlsVersion, CipherSuite cipherSuite, List<Certificate> peerCertificates,
            List<Certificate> localCertificates) {
        this.tlsVersion = tlsVersion;
        this.cipherSuite = cipherSuite;
        this.peerCertificates = peerCertificates;
        this.localCertificates = localCertificates;
    }

    /**
     * 从 SSL 会话创建 Handshake 实例
     *
     * @param session SSL 会话
     * @return Handshake 实例
     * @throws IOException 如果会话无效或解析失败
     */
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

    /**
     * 创建 Handshake 实例
     *
     * @param tlsVersion        TLS 版本
     * @param cipherSuite       密码套件
     * @param peerCertificates  远程对等点证书列表
     * @param localCertificates 本地证书列表
     * @return Handshake 实例
     * @throws NullPointerException 如果 tlsVersion 或 cipherSuite 为 null
     */
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
     * 获取 TLS 版本
     *
     * @return TLS 版本
     */
    public TlsVersion tlsVersion() {
        return tlsVersion;
    }

    /**
     * 获取密码套件
     *
     * @return 密码套件
     */
    public CipherSuite cipherSuite() {
        return cipherSuite;
    }

    /**
     * 获取远程对等点证书列表
     *
     * @return 不可修改的证书列表（可能为空）
     */
    public List<Certificate> peerCertificates() {
        return peerCertificates;
    }

    /**
     * 获取远程对等点主体
     *
     * @return 主体（匿名时为 null）
     */
    public Principal peerPrincipal() {
        return !peerCertificates.isEmpty() ? ((X509Certificate) peerCertificates.get(0)).getSubjectX500Principal()
                : null;
    }

    /**
     * 获取本地证书列表
     *
     * @return 不可修改的证书列表（可能为空）
     */
    public List<Certificate> localCertificates() {
        return localCertificates;
    }

    /**
     * 获取本地主体
     *
     * @return 主体（匿名时为 null）
     */
    public Principal localPrincipal() {
        return !localCertificates.isEmpty() ? ((X509Certificate) localCertificates.get(0)).getSubjectX500Principal()
                : null;
    }

    /**
     * 比较两个 Handshake 对象是否相等
     *
     * @param other 另一个对象
     * @return true 如果两个 Handshake 对象相等
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Handshake))
            return false;
        Handshake that = (Handshake) other;
        return tlsVersion.equals(that.tlsVersion) && cipherSuite.equals(that.cipherSuite)
                && peerCertificates.equals(that.peerCertificates) && localCertificates.equals(that.localCertificates);
    }

    /**
     * 计算 Handshake 对象的哈希码
     *
     * @return 哈希码值
     */
    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + tlsVersion.hashCode();
        result = 31 * result + cipherSuite.hashCode();
        result = 31 * result + peerCertificates.hashCode();
        result = 31 * result + localCertificates.hashCode();
        return result;
    }

    /**
     * 返回 Handshake 的字符串表示
     *
     * @return 包含 TLS 版本、密码套件和证书信息的字符串
     */
    @Override
    public String toString() {
        return "Handshake{" + "tlsVersion=" + tlsVersion + " cipherSuite=" + cipherSuite + " peerCertificates="
                + names(peerCertificates) + " localCertificates=" + names(localCertificates) + '}';
    }

    /**
     * 将证书列表转换为名称列表
     *
     * @param certificates 证书列表
     * @return 名称列表
     */
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