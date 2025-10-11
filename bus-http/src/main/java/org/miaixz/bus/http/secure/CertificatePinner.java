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
package org.miaixz.bus.http.secure;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.http.UnoUrl;

import javax.net.ssl.SSLPeerUnverifiedException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.*;

/**
 * Constrains which certificates are trusted. Certificate pinning increases security, but also limits your server team's
 * abilities to update their TLS certificates. <strong>Do not use certificate pinning without the blessing of your
 * server's TLS administrator!</strong>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CertificatePinner {

    /**
     * A default certificate pinner that trusts any certificate chain.
     */
    public static final CertificatePinner DEFAULT = new Builder().build();

    /**
     * The set of configured pins.
     */
    private final Set<Pin> pins;
    /**
     * An optional cleaner for the certificate chain.
     */
    private final CertificateChainCleaner certificateChainCleaner;

    /**
     * Constructs a new CertificatePinner.
     * 
     * @param pins                    The set of pins.
     * @param certificateChainCleaner The certificate chain cleaner.
     */
    CertificatePinner(Set<Pin> pins, CertificateChainCleaner certificateChainCleaner) {
        this.pins = pins;
        this.certificateChainCleaner = certificateChainCleaner;
    }

    /**
     * Returns the SHA-256 of {@code certificate}'s public key.
     *
     * @param certificate The certificate to hash.
     * @return A string in the format "sha256/&lt;base64-hash&gt;".
     */
    public static String pin(Certificate certificate) {
        if (!(certificate instanceof X509Certificate)) {
            throw new IllegalArgumentException("Certificate pinning requires X509 certificates");
        }
        return "sha256/" + sha256((X509Certificate) certificate).base64();
    }

    /**
     * Computes the SHA-1 hash of a certificate's public key.
     * 
     * @param x509Certificate The certificate.
     * @return The SHA-1 hash as a ByteString.
     */
    static ByteString sha1(X509Certificate x509Certificate) {
        return ByteString.of(x509Certificate.getPublicKey().getEncoded()).sha1();
    }

    /**
     * Computes the SHA-256 hash of a certificate's public key.
     * 
     * @param x509Certificate The certificate.
     * @return The SHA-256 hash as a ByteString.
     */
    static ByteString sha256(X509Certificate x509Certificate) {
        return ByteString.of(x509Certificate.getPublicKey().getEncoded()).sha256();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this)
            return true;
        return other instanceof CertificatePinner
                && (Objects.equals(certificateChainCleaner, ((CertificatePinner) other).certificateChainCleaner)
                        && pins.equals(((CertificatePinner) other).pins));
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(certificateChainCleaner);
        result = 31 * result + pins.hashCode();
        return result;
    }

    /**
     * Confirms that at least one of the certificates pinned for {@code hostname} is present in
     * {@code peerCertificates}. This method is called after a successful TLS handshake, but before the connection is
     * used.
     *
     * @param hostname         The hostname of the peer.
     * @param peerCertificates The certificate chain presented by the peer.
     * @throws SSLPeerUnverifiedException if {@code peerCertificates} does not match the certificates pinned for
     *                                    {@code hostname}.
     */
    public void check(String hostname, List<Certificate> peerCertificates) throws SSLPeerUnverifiedException {
        List<Pin> pins = findMatchingPins(hostname);
        if (pins.isEmpty())
            return;

        List<Certificate> checkedPeerCertificates = peerCertificates;
        if (null != certificateChainCleaner) {
            checkedPeerCertificates = certificateChainCleaner.clean(peerCertificates, hostname);
        }

        for (int c = 0, certsSize = checkedPeerCertificates.size(); c < certsSize; c++) {
            X509Certificate x509Certificate = (X509Certificate) checkedPeerCertificates.get(c);

            // Lazily compute the hashes for each certificate.
            ByteString sha1 = null;
            ByteString sha256 = null;

            for (int p = 0, pinsSize = pins.size(); p < pinsSize; p++) {
                Pin pin = pins.get(p);
                if (pin.hashAlgorithm.equals("sha256/")) {
                    if (sha256 == null)
                        sha256 = sha256(x509Certificate);
                    if (pin.hash.equals(sha256))
                        return; // Success!
                } else if (pin.hashAlgorithm.equals("sha1/")) {
                    if (sha1 == null)
                        sha1 = sha1(x509Certificate);
                    if (pin.hash.equals(sha1))
                        return; // Success!
                } else {
                    throw new AssertionError("unsupported hashAlgorithm: " + pin.hashAlgorithm);
                }
            }
        }

        // If we couldn't find a matching pin, format a nice exception.
        StringBuilder message = new StringBuilder().append("Certificate pinning failure!")
                .append("\n  Peer certificate chain:");
        for (int c = 0, certsSize = checkedPeerCertificates.size(); c < certsSize; c++) {
            X509Certificate x509Certificate = (X509Certificate) checkedPeerCertificates.get(c);
            message.append("\n    ").append(pin(x509Certificate)).append(": ")
                    .append(x509Certificate.getSubjectDN().getName());
        }
        message.append("\n  Pinned certificates for ").append(hostname).append(Symbol.COLON);
        for (int p = 0, pinsSize = pins.size(); p < pinsSize; p++) {
            Pin pin = pins.get(p);
            message.append("\n    ").append(pin);
        }
        throw new SSLPeerUnverifiedException(message.toString());
    }

    /**
     * Returns a list of matching certificates' pins for the hostname.
     *
     * @param hostname The hostname to match against.
     * @return An empty list if the hostname does not have pinned certificates.
     */
    List<Pin> findMatchingPins(String hostname) {
        List<Pin> result = Collections.emptyList();
        for (Pin pin : pins) {
            if (pin.matches(hostname)) {
                if (result.isEmpty())
                    result = new ArrayList<>();
                result.add(pin);
            }
        }
        return result;
    }

    /**
     * Returns a new certificate pinner that uses {@code certificateChainCleaner}.
     *
     * @param certificateChainCleaner The cleaner to use.
     * @return a new {@code CertificatePinner} instance.
     */
    public CertificatePinner withCertificateChainCleaner(CertificateChainCleaner certificateChainCleaner) {
        return Objects.equals(this.certificateChainCleaner, certificateChainCleaner) ? this
                : new CertificatePinner(pins, certificateChainCleaner);
    }

    /**
     * Represents a single certificate pin.
     */
    static final class Pin {

        private static final String WILDCARD = "*.";
        /**
         * A hostname pattern like {@code example.com} or {@code *.example.com}.
         */
        final String pattern;
        /**
         * The canonical hostname, extracted from the pattern.
         */
        final String canonicalHostname;
        /**
         * The hash algorithm, either {@code sha1/} or {@code sha256/}.
         */
        final String hashAlgorithm;
        /**
         * The base64-encoded hash of the certificate's public key.
         */
        final ByteString hash;

        Pin(String pattern, String pin) {
            this.pattern = pattern;
            this.canonicalHostname = pattern.startsWith(WILDCARD)
                    ? UnoUrl.get(Protocol.HTTP_PREFIX + pattern.substring(WILDCARD.length())).host()
                    : UnoUrl.get(Protocol.HTTP_PREFIX + pattern).host();
            if (pin.startsWith("sha1/")) {
                this.hashAlgorithm = "sha1/";
                this.hash = ByteString.decodeBase64(pin.substring("sha1/".length()));
            } else if (pin.startsWith("sha256/")) {
                this.hashAlgorithm = "sha256/";
                this.hash = ByteString.decodeBase64(pin.substring("sha256/".length()));
            } else {
                throw new IllegalArgumentException("pins must start with 'sha256/' or 'sha1/': " + pin);
            }

            if (null == this.hash) {
                throw new IllegalArgumentException("pins must be base64: " + pin);
            }
        }

        boolean matches(String hostname) {
            if (pattern.startsWith(WILDCARD)) {
                int firstDot = hostname.indexOf(Symbol.C_DOT);
                return (hostname.length() - firstDot - 1) == canonicalHostname.length() && hostname
                        .regionMatches(false, firstDot + 1, canonicalHostname, 0, canonicalHostname.length());
            }
            return hostname.equals(canonicalHostname);
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof Pin && pattern.equals(((Pin) other).pattern)
                    && hashAlgorithm.equals(((Pin) other).hashAlgorithm) && hash.equals(((Pin) other).hash);
        }

        @Override
        public int hashCode() {
            int result = 17;
            result = 31 * result + pattern.hashCode();
            result = 31 * result + hashAlgorithm.hashCode();
            result = 31 * result + hash.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return hashAlgorithm + hash.base64();
        }
    }

    /**
     * A builder for creating a {@link CertificatePinner}.
     */
    public static final class Builder {

        private final List<Pin> pins = new ArrayList<>();

        /**
         * Pins certificates for a given hostname pattern.
         *
         * @param pattern A lowercase hostname or wildcard pattern (e.g., {@code *.example.com}).
         * @param pins    One or more SHA-256 or SHA-1 hashes. Each pin is a base64-encoded hash of a certificate's
         *                Subject Public Key Info, prefixed with {@code sha256/} or {@code sha1/}.
         * @return This builder instance.
         */
        public Builder add(String pattern, String... pins) {
            if (null == pattern)
                throw new NullPointerException("pattern == null");

            for (String pin : pins) {
                this.pins.add(new Pin(pattern, pin));
            }

            return this;
        }

        /**
         * Builds the {@link CertificatePinner} with the configured pins.
         *
         * @return A new {@code CertificatePinner} instance.
         */
        public CertificatePinner build() {
            return new CertificatePinner(new LinkedHashSet<>(pins), null);
        }
    }

}
