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
package org.miaixz.bus.crypto.builtin;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.crypto.builtin.digest.DigesterFactory;

/**
 * Certificate public-key pin helpers.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class CertificatePin {

    /**
     * SHA-256 pin prefix.
     */
    public static final String SHA256_PREFIX = "sha256/";

    /**
     * SHA-1 pin prefix for callers that still need SHA-1 pin strings.
     */
    public static final String SHA1_PREFIX = "sha1/";

    /**
     * SHA-256 digester factory.
     */
    private static final DigesterFactory SHA256 = DigesterFactory.ofJdk("SHA-256");

    /**
     * SHA-1 digester factory.
     */
    private static final DigesterFactory SHA1 = DigesterFactory.ofJdk("SHA-1");

    /**
     * Restricts construction to the static certificate pin operations defined by this class.
     */
    private CertificatePin() {
        // Utility class.
    }

    /**
     * Computes a SHA-256 certificate pin.
     *
     * @param certificate certificate
     * @return SHA-256 pin
     */
    public static String sha256(final Certificate certificate) {
        return SHA256_PREFIX + Base64.encode(digest(SHA256, certificate));
    }

    /**
     * Computes a SHA-1 certificate pin.
     *
     * @param certificate certificate
     * @return SHA-1 pin
     */
    public static String sha1(final Certificate certificate) {
        return SHA1_PREFIX + Base64.encode(digest(SHA1, certificate));
    }

    /**
     * Validates a certificate pin string.
     *
     * @param pin pin
     * @return normalized pin
     */
    public static String validate(final String pin) {
        if (StringKit.isBlank(pin) || StringKit.containsAny(pin, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException("Certificate pin must be non-blank and single-line");
        }
        if (!pin.startsWith(SHA256_PREFIX) && !pin.startsWith(SHA1_PREFIX)) {
            throw new ProtocolException("Certificate pin must use sha256/ or sha1/ prefix");
        }
        final int prefixLength = pin.startsWith(SHA256_PREFIX) ? SHA256_PREFIX.length() : SHA1_PREFIX.length();
        try {
            Base64.decode(pin.substring(prefixLength));
        } catch (final RuntimeException e) {
            throw new ProtocolException("Certificate pin must contain base64 hash", e);
        }
        return pin;
    }

    /**
     * Computes a digest over an X509 certificate public key.
     *
     * @param factory     digester factory
     * @param certificate certificate
     * @return digest
     */
    private static byte[] digest(final DigesterFactory factory, final Certificate certificate) {
        final Certificate checkedCertificate = Assert
                .notNull(certificate, () -> new ValidateException("Certificate must not be null"));
        if (!(checkedCertificate instanceof X509Certificate)) {
            throw new ProtocolException("Certificate pinning requires X509 certificates");
        }
        return factory.createDigester().digest(checkedCertificate.getPublicKey().getEncoded());
    }

}
