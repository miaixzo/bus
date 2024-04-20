/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.http.secure;

import javax.net.ssl.SSLPeerUnverifiedException;
import java.security.GeneralSecurityException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.*;

/**
 * 使用一组可信根证书来构建可信链的证书链清理器。
 * 这个类复制了在TLS握手期间执行的clean chain构建。我们更喜欢它们
 * 存在的其他机制，比如{@code AndroidCertificateChainCleaner}
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BasicCertificateChainCleaner extends CertificateChainCleaner {

    /**
     * 链中最大的签名者数。我们使用9表示与OpenSSL的一致性.
     */
    private static final int MAX_SIGNERS = 9;

    private final TrustRootIndex trustRootIndex;

    public BasicCertificateChainCleaner(TrustRootIndex trustRootIndex) {
        this.trustRootIndex = trustRootIndex;
    }

    /**
     * Returns a cleaned chain for {@code chain}.
     * This method throws if the complete chain to a trusted CA certificate cannot be constructed.
     * This is unexpected unless the trust root index in this class has a different trust manager than
     * what was used to establish {@code chain}.
     */
    @Override
    public List<Certificate> clean(List<Certificate> chain, String hostname)
            throws SSLPeerUnverifiedException {
        Deque<Certificate> queue = new ArrayDeque<>(chain);
        List<Certificate> result = new ArrayList<>();
        result.add(queue.removeFirst());
        boolean foundTrustedCertificate = false;

        followIssuerChain:
        for (int c = 0; c < MAX_SIGNERS; c++) {
            X509Certificate toVerify = (X509Certificate) result.get(result.size() - 1);

            // 如果此证书已由可信证书签署，请使用该证书。将受信任证书添加到链的末尾，除非它已经存在
            // (如果链中的第一个证书本身是自签名和受信任的CA证书，则会发生这种情况)
            X509Certificate trustedCert = trustRootIndex.findByIssuerAndSignature(toVerify);
            if (trustedCert != null) {
                if (result.size() > 1 || !toVerify.equals(trustedCert)) {
                    result.add(trustedCert);
                }
                if (verifySignature(trustedCert, trustedCert)) {
                    // 自签名证书是根CA
                    return result;
                }
                foundTrustedCertificate = true;
                continue;
            }

            // 在签署此证书的链中搜索证书。这通常是链中的下一个元素,但它可以是任何元素.
            for (Iterator<Certificate> i = queue.iterator(); i.hasNext(); ) {
                X509Certificate signingCert = (X509Certificate) i.next();
                if (verifySignature(toVerify, signingCert)) {
                    i.remove();
                    result.add(signingCert);
                    continue followIssuerChain;
                }
            }

            // 我们已经到了链条的末端。如果链中的任何证书是可信的,我们就完成了.
            if (foundTrustedCertificate) {
                return result;
            }

            // 最后一个链接不可信,失败
            throw new SSLPeerUnverifiedException(
                    "Failed to find a trusted cert that signed " + toVerify);
        }

        throw new SSLPeerUnverifiedException("Certificate chain too long: " + result);
    }

    /**
     * Returns true if {@code toVerify} was signed by {@code signingCert}'s public key.
     */
    private boolean verifySignature(X509Certificate toVerify, X509Certificate signingCert) {
        if (!toVerify.getIssuerDN().equals(signingCert.getSubjectDN())) return false;
        try {
            toVerify.verify(signingCert.getPublicKey());
            return true;
        } catch (GeneralSecurityException verifyFailed) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return trustRootIndex.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        return other instanceof BasicCertificateChainCleaner
                && ((BasicCertificateChainCleaner) other).trustRootIndex.equals(trustRootIndex);
    }

}
