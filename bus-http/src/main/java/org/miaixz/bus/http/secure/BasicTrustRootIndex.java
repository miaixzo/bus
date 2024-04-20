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

import javax.security.auth.x500.X500Principal;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * 简单的索引，包含已加载到内存中的受信任根证书
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BasicTrustRootIndex implements TrustRootIndex {

    private final Map<X500Principal, Set<X509Certificate>> subjectToCaCerts;

    public BasicTrustRootIndex(X509Certificate... caCerts) {
        subjectToCaCerts = new LinkedHashMap<>();
        for (X509Certificate caCert : caCerts) {
            X500Principal subject = caCert.getSubjectX500Principal();
            Set<X509Certificate> subjectCaCerts = subjectToCaCerts.get(subject);
            if (null == subjectCaCerts) {
                subjectCaCerts = new LinkedHashSet<>(1);
                subjectToCaCerts.put(subject, subjectCaCerts);
            }
            subjectCaCerts.add(caCert);
        }
    }

    @Override
    public X509Certificate findByIssuerAndSignature(X509Certificate cert) {
        X500Principal issuer = cert.getIssuerX500Principal();
        Set<X509Certificate> subjectCaCerts = subjectToCaCerts.get(issuer);
        if (null == subjectCaCerts) return null;

        for (X509Certificate caCert : subjectCaCerts) {
            PublicKey publicKey = caCert.getPublicKey();
            try {
                cert.verify(publicKey);
                return caCert;
            } catch (Exception ignored) {
            }
        }

        return null;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        return other instanceof BasicTrustRootIndex
                && ((BasicTrustRootIndex) other).subjectToCaCerts.equals(
                subjectToCaCerts);
    }

    @Override
    public int hashCode() {
        return subjectToCaCerts.hashCode();
    }

}
