/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
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
package org.aoju.bus.http.offers;

import org.aoju.bus.core.io.segment.ByteString;
import org.aoju.bus.http.Internal;
import org.aoju.bus.http.Url;
import org.aoju.bus.http.secure.CertificateChainCleaner;

import javax.net.ssl.SSLPeerUnverifiedException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.*;

/**
 * Constrains which certificates are trusted. Pinning certificates defends against attacks on
 * certificate authorities. It also prevents connections through man-in-the-middle certificate
 * authorities either known or unknown to the application's user.
 *
 * <p>This class currently pins a certificate's Subject Public Key Info as described on <a
 * href="http://goo.gl/AIx3e5">Adam Langley's Weblog</a>. Pins are either base64 SHA-256 hashes as
 * in <a href="http://tools.ietf.org/html/rfc7469">HTTP Public Key Pinning (HPKP)</a> or SHA-1
 * base64 hashes as in Chromium's <a href="http://goo.gl/XDh6je">static certificates</a>.
 *
 * <h3>Setting up Certificate Pinning</h3>
 *
 * <p>The easiest way to pin a host is turn on pinning with a broken configuration and read the
 * expected configuration when the connection fails. Be sure to do this on a trusted network, and
 * without man-in-the-middle tools like <a href="http://charlesproxy.com">Charles</a> or <a
 * href="http://fiddlertool.com">Fiddler</a>.
 *
 * <p>For example, to pin {@code https://publicobject.com}, start with a broken
 * configuration: <pre>   {@code
 *
 *     String hostname = "publicobject.com";
 *     CertificatePinner certificatePinner = new CertificatePinner.Builder()
 *         .add(hostname, "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
 *         .build();
 *     HttpClient client = HttpClient.Builder()
 *         .certificatePinner(certificatePinner)
 *         .build();
 *
 *     Request request = new Request.Builder()
 *         .url("https://" + hostname)
 *         .build();
 *     client.newCall(request).execute();
 * }</pre>
 * <p>
 * As expected, this fails with a certificate pinning exception: <pre>   {@code
 *
 * javax.net.ssl.SSLPeerUnverifiedException: Certificate pinning failure!
 *   Peer certificate chain:
 *     sha256/afwiKY3RxoMmLkuRW1l7QsPZTJPwDS2pdDROQjXw8ig=: CN=publicobject.com, OU=PositiveSSL
 *     sha256/klO23nT2ehFDXCfx3eHTDRESMz3asj1muO+4aIdjiuY=: CN=COMODO RSA Secure Server CA
 *     sha256/grX4Ta9HpZx6tSHkmCrvpApTQGo67CYDnvprLg5yRME=: CN=COMODO RSA Certification Authority
 *     sha256/lCppFqbkrlJ3EcVFAkeip0+44VaoJUymbnOaEUk7tEU=: CN=AddTrust External CA Root
 *   Pinned certificates for publicobject.com:
 *     sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=
 *   at CertificatePinner.check(CertificatePinner.java)
 *   at Connection.upgradeToTls(Connection.java)
 *   at Connection.connect(Connection.java)
 *   at Connection.connectAndSetOwner(Connection.java)
 * }</pre>
 * <p>
 * Follow up by pasting the public key hashes from the exception into the
 * certificate pinner's configuration: <pre>   {@code
 *
 *     CertificatePinner certificatePinner = new CertificatePinner.Builder()
 *       .add("publicobject.com", "sha256/afwiKY3RxoMmLkuRW1l7QsPZTJPwDS2pdDROQjXw8ig=")
 *       .add("publicobject.com", "sha256/klO23nT2ehFDXCfx3eHTDRESMz3asj1muO+4aIdjiuY=")
 *       .add("publicobject.com", "sha256/grX4Ta9HpZx6tSHkmCrvpApTQGo67CYDnvprLg5yRME=")
 *       .add("publicobject.com", "sha256/lCppFqbkrlJ3EcVFAkeip0+44VaoJUymbnOaEUk7tEU=")
 *       .build();
 * }</pre>
 * <p>
 * Pinning is per-hostname and/or per-wildcard pattern. To pin both {@code publicobject.com} and
 * {@code www.publicobject.com}, you must configure both hostnames.
 *
 * <p>Wildcard pattern rules:
 * <ol>
 * <li>Asterisk {@code *} is only permitted in the left-most entity name label and must be the
 * only character in that label (i.e., must match the whole left-most label). For example,
 * {@code *.example.com} is permitted, while {@code *a.example.com}, {@code a*.example.com},
 * {@code a*b.example.com}, {@code a.*.example.com} are not permitted.
 * <li>Asterisk {@code *} cannot match across entity name labels. For example,
 * {@code *.example.com} matches {@code test.example.com} but does not match
 * {@code sub.test.example.com}.
 * <li>Wildcard patterns for single-label entity names are not permitted.
 * </ol>
 * <p>
 * If hostname pinned directly and via wildcard pattern, both direct and wildcard pins will be used.
 * For example: {@code *.example.com} pinned with {@code pin1} and {@code a.example.com} pinned with
 * {@code pin2}, to check {@code a.example.com} both {@code pin1} and {@code pin2} will be used.
 *
 * <h3>Warning: Certificate Pinning is Dangerous!</h3>
 *
 * <p>Pinning certificates limits your server team's abilities to update their TLS certificates. By
 * pinning certificates, you take on additional operational complexity and limit your ability to
 * migrate between certificate authorities. Do not use certificate pinning without the blessing of
 * your server's TLS administrator!
 *
 * <h4>Note about self-signed certificates</h4>
 *
 * <p>{@link CertificatePinner} can not be used to pin self-signed certificate if such certificate
 * is not accepted by {@link javax.net.ssl.TrustManager}.
 *
 * @author Kimi Liu
 * @version 5.3.6
 * @since JDK 1.8+
 */
public final class CertificatePinner {

    public static final CertificatePinner DEFAULT = new Builder().build();

    private final Set<Pin> pins;
    private final
    CertificateChainCleaner certificateChainCleaner;

    CertificatePinner(Set<Pin> pins, CertificateChainCleaner certificateChainCleaner) {
        this.pins = pins;
        this.certificateChainCleaner = certificateChainCleaner;
    }

    /**
     * Returns the SHA-256 of {@code certificate}'s public key.
     *
     * <p>In HttpClient, this returned a SHA-1 hash of the public key. Both types are
     * supported, but SHA-256 is preferred.
     *
     * @param certificate Certificate
     * @return String
     */
    public static String pin(Certificate certificate) {
        if (!(certificate instanceof X509Certificate)) {
            throw new IllegalArgumentException("Certificate pinning requires X509 certificates");
        }
        return "sha256/" + sha256((X509Certificate) certificate).base64();
    }

    static ByteString sha1(X509Certificate x509Certificate) {
        return ByteString.of(x509Certificate.getPublicKey().getEncoded()).sha1();
    }

    static ByteString sha256(X509Certificate x509Certificate) {
        return ByteString.of(x509Certificate.getPublicKey().getEncoded()).sha256();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        return other instanceof CertificatePinner
                && (Internal.equal(certificateChainCleaner, ((CertificatePinner) other).certificateChainCleaner)
                && pins.equals(((CertificatePinner) other).pins));
    }

    @Override
    public int hashCode() {
        int result = certificateChainCleaner != null ? certificateChainCleaner.hashCode() : 0;
        result = 31 * result + pins.hashCode();
        return result;
    }

    public void check(String hostname, List<Certificate> peerCertificates)
            throws SSLPeerUnverifiedException {
        List<Pin> pins = findMatchingPins(hostname);
        if (pins.isEmpty()) return;

        if (certificateChainCleaner != null) {
            peerCertificates = certificateChainCleaner.clean(peerCertificates, hostname);
        }

        for (int c = 0, certsSize = peerCertificates.size(); c < certsSize; c++) {
            X509Certificate x509Certificate = (X509Certificate) peerCertificates.get(c);

            // Lazily compute the hashes for each certificate.
            ByteString sha1 = null;
            ByteString sha256 = null;

            for (int p = 0, pinsSize = pins.size(); p < pinsSize; p++) {
                Pin pin = pins.get(p);
                if (pin.hashAlgorithm.equals("sha256/")) {
                    if (sha256 == null) sha256 = sha256(x509Certificate);
                    if (pin.hash.equals(sha256)) return; // Success!
                } else if (pin.hashAlgorithm.equals("sha1/")) {
                    if (sha1 == null) sha1 = sha1(x509Certificate);
                    if (pin.hash.equals(sha1)) return; // Success!
                } else {
                    throw new AssertionError("unsupported hashAlgorithm: " + pin.hashAlgorithm);
                }
            }
        }

        // If we couldn't find a matching pin, format a nice exception.
        StringBuilder message = new StringBuilder()
                .append("Certificate pinning failure!")
                .append("\n  Peer certificate chain:");
        for (int c = 0, certsSize = peerCertificates.size(); c < certsSize; c++) {
            X509Certificate x509Certificate = (X509Certificate) peerCertificates.get(c);
            message.append("\n    ").append(pin(x509Certificate))
                    .append(": ").append(x509Certificate.getSubjectDN().getName());
        }
        message.append("\n  Pinned certificates for ").append(hostname).append(":");
        for (int p = 0, pinsSize = pins.size(); p < pinsSize; p++) {
            Pin pin = pins.get(p);
            message.append("\n    ").append(pin);
        }
        throw new SSLPeerUnverifiedException(message.toString());
    }


    public void check(String hostname, Certificate... peerCertificates)
            throws SSLPeerUnverifiedException {
        check(hostname, Arrays.asList(peerCertificates));
    }

    public List<Pin> findMatchingPins(String hostname) {
        List<Pin> result = Collections.emptyList();
        for (Pin pin : pins) {
            if (pin.matches(hostname)) {
                if (result.isEmpty()) result = new ArrayList<>();
                result.add(pin);
            }
        }
        return result;
    }

    public CertificatePinner withCertificateChainCleaner(
            CertificateChainCleaner certificateChainCleaner) {
        return Internal.equal(this.certificateChainCleaner, certificateChainCleaner)
                ? this
                : new CertificatePinner(pins, certificateChainCleaner);
    }

    static final class Pin {
        private static final String WILDCARD = "*.";
        /**
         * A hostname like {@code example.com} or a pattern like {@code *.example.com}.
         */
        final String pattern;
        /**
         * The canonical hostname, i.e. {@code EXAMPLE.com} becomes {@code example.com}.
         */
        final String canonicalHostname;
        /**
         * Either {@code sha1/} or {@code sha256/}.
         */
        final String hashAlgorithm;
        /**
         * The hash of the pinned certificate using {@link #hashAlgorithm}.
         */
        final ByteString hash;

        Pin(String pattern, String pin) {
            this.pattern = pattern;
            this.canonicalHostname = pattern.startsWith(WILDCARD)
                    ? Url.get("http://" + pattern.substring(WILDCARD.length())).host()
                    : Url.get("http://" + pattern).host();
            if (pin.startsWith("sha1/")) {
                this.hashAlgorithm = "sha1/";
                this.hash = ByteString.decodeBase64(pin.substring("sha1/".length()));
            } else if (pin.startsWith("sha256/")) {
                this.hashAlgorithm = "sha256/";
                this.hash = ByteString.decodeBase64(pin.substring("sha256/".length()));
            } else {
                throw new IllegalArgumentException("pins must start with 'sha256/' or 'sha1/': " + pin);
            }

            if (this.hash == null) {
                throw new IllegalArgumentException("pins must be base64: " + pin);
            }
        }

        boolean matches(String hostname) {
            if (pattern.startsWith(WILDCARD)) {
                int firstDot = hostname.indexOf('.');
                return (hostname.length() - firstDot - 1) == canonicalHostname.length()
                        && hostname.regionMatches(false, firstDot + 1, canonicalHostname, 0,
                        canonicalHostname.length());
            }

            return hostname.equals(canonicalHostname);
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof Pin
                    && pattern.equals(((Pin) other).pattern)
                    && hashAlgorithm.equals(((Pin) other).hashAlgorithm)
                    && hash.equals(((Pin) other).hash);
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
     * Builds a configured certificate pinner.
     */
    public static final class Builder {
        private final List<Pin> pins = new ArrayList<>();

        /**
         * Pins certificates for {@code pattern}.
         *
         * @param pattern lower-case host name or wildcard pattern such as {@code *.example.com}.
         * @param pins    SHA-256 or SHA-1 hashes. Each pin is a hash of a certificate's Subject Public Key
         *                Info, base64-encoded and prefixed with either {@code sha256/} or {@code sha1/}.
         * @return Builder
         */
        public Builder add(String pattern, String... pins) {
            if (pattern == null) throw new NullPointerException("pattern == null");

            for (String pin : pins) {
                this.pins.add(new Pin(pattern, pin));
            }

            return this;
        }

        public CertificatePinner build() {
            return new CertificatePinner(new LinkedHashSet<>(pins), null);
        }
    }
}
