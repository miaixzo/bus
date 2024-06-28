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
 * 限制哪些证书受信任。将证书固定起来可以防御对证书颁发机构的攻击。
 * 它还可以防止通过应用程序用户知道或不知道的中间人证书颁发机构进行连接
 * 固定证书限制了服务器团队更新其TLS证书的能力。通过固定证书，
 * 您将承担额外的操作复杂性，并限制您在证书颁发机构之间迁移的能力。
 * 未经服务器的TLS管理员许可，请勿使用证书固定!
 * 如果{@link javax.net.ssl.TrustManager}不接受自签名证书,
 * 则{@link CertificatePinner}不能用于pin自签名证书
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CertificatePinner {

    public static final CertificatePinner DEFAULT = new Builder().build();

    private final Set<Pin> pins;
    private final CertificateChainCleaner certificateChainCleaner;

    CertificatePinner(Set<Pin> pins, CertificateChainCleaner certificateChainCleaner) {
        this.pins = pins;
        this.certificateChainCleaner = certificateChainCleaner;
    }

    /**
     * Returns the SHA-256 of {@code certificate}'s public key.
     * In Http and earlier, this returned a SHA-1 hash of the public key. Both types are
     * supported, but SHA-256 is preferred.
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
                && (Objects.equals(certificateChainCleaner,
                ((CertificatePinner) other).certificateChainCleaner)
                && pins.equals(((CertificatePinner) other).pins));
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(certificateChainCleaner);
        result = 31 * result + pins.hashCode();
        return result;
    }

    /**
     * 确认{@code hostname}所固定的证书中至少有一个位于{@code peerCertificates}中。
     * 如果没有为{@code hostname}指定证书，则不执行任何操作。Httpd在成功的TLS握手之后调用它，但是在使用连接之前.
     *
     * @param hostname         主机名
     * @param peerCertificates 证书信息
     * @throws SSLPeerUnverifiedException 如果{@code peerCertificates}
     *                                    与{@code hostname}所固定的证书不匹配
     */
    public void check(String hostname, List<Certificate> peerCertificates)
            throws SSLPeerUnverifiedException {
        List<Pin> pins = findMatchingPins(hostname);
        if (pins.isEmpty()) return;

        if (null != certificateChainCleaner) {
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
        message.append("\n  Pinned certificates for ").append(hostname).append(Symbol.COLON);
        for (int p = 0, pinsSize = pins.size(); p < pinsSize; p++) {
            Pin pin = pins.get(p);
            message.append("\n    ").append(pin);
        }
        throw new SSLPeerUnverifiedException(message.toString());
    }

    /**
     * @deprecated replaced with {@link #check(String, List)}.
     */
    public void check(String hostname, Certificate... peerCertificates)
            throws SSLPeerUnverifiedException {
        check(hostname, Arrays.asList(peerCertificates));
    }

    /**
     * Returns list of matching certificates' pins for the hostname. Returns an empty list if the
     * hostname does not have pinned certificates.
     */
    List<Pin> findMatchingPins(String hostname) {
        List<Pin> result = Collections.emptyList();
        for (Pin pin : pins) {
            if (pin.matches(hostname)) {
                if (result.isEmpty()) result = new ArrayList<>();
                result.add(pin);
            }
        }
        return result;
    }

    /**
     * Returns a certificate pinner that uses {@code certificateChainCleaner}.
     */
    public CertificatePinner withCertificateChainCleaner(
            CertificateChainCleaner certificateChainCleaner) {
        return Objects.equals(this.certificateChainCleaner, certificateChainCleaner)
                ? this
                : new CertificatePinner(pins, certificateChainCleaner);
    }

    static class Pin {
        private static final String WILDCARD = "*.";
        /**
         * 像{@code example.com}这样的主机名或{@code *.example.com}这样的模式.
         */
        final String pattern;
        /**
         * 规范主机名，即{@code EXAMPLE.com}变为{@code EXAMPLE.com}.
         */
        final String canonicalHostname;
        /**
         * 要么 {@code sha1/} or {@code sha256/}.
         */
        final String hashAlgorithm;
        /**
         * 使用{@link # hashalgm}的固定证书散列.
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
     * 构建已配置的证书
     */
    public static class Builder {

        private final List<Pin> pins = new ArrayList<>();

        /**
         * 用于{@code pattern} 的证书
         *
         * @param pattern lowner -case主机名或通配符模式，如{@code *.example.com}.
         * @param pins    SHA-256或SHA-1散列。每个pin是证书主题公钥信息的散列，用base64编码，
         *                以{@code sha256/}或{@code sha1/}为前缀
         * @return 构建器
         */
        public Builder add(String pattern, String... pins) {
            if (null == pattern) throw new NullPointerException("pattern == null");

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
