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
package org.miaixz.bus.fabric.network.tls.cert;

import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * TLS certificate validation policy with trust manager, hostname, and pin checks.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class CertificatePolicy {

    /**
     * SHA-256 pin prefix.
     */
    private static final String SHA256_PREFIX = "sha256/";

    /**
     * SHA-1 pin prefix for callers that still need SHA-1 pin strings.
     */
    private static final String SHA1_PREFIX = "sha1/";

    /**
     * Trust manager.
     */
    private final X509TrustManager trustManager;

    /**
     * Hostname verification flag.
     */
    private final boolean hostnameVerify;

    /**
     * Certificate pins by host.
     */
    private final Map<String, Set<String>> pins;

    /**
     * Trust all flag.
     */
    private final boolean trustAll;

    /**
     * Optional chain cleaner.
     */
    private final CertificateChainCleanerAdapter chainCleaner;

    /**
     * Creates a certificate policy.
     *
     * @param trustManager   trust manager
     * @param hostnameVerify hostname verification flag
     * @param pins           certificate pins
     * @param trustAll       trust all flag
     * @param chainCleaner   chain cleaner
     */
    private CertificatePolicy(final X509TrustManager trustManager, final boolean hostnameVerify,
            final Map<String, Set<String>> pins, final boolean trustAll,
            final CertificateChainCleanerAdapter chainCleaner) {
        if (trustManager == null) {
            throw new ValidateException("Trust manager must not be null");
        }
        this.trustManager = trustManager;
        this.hostnameVerify = hostnameVerify;
        this.pins = copyPins(pins);
        this.trustAll = trustAll;
        this.chainCleaner = chainCleaner;
    }

    /**
     * Returns the system trust policy.
     *
     * @return trust policy
     */
    public static CertificatePolicy trustSystem() {
        return builder().build();
    }

    /**
     * Creates a builder.
     *
     * @return builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Cleans and verifies a certificate chain.
     *
     * @param host  host
     * @param chain certificate chain
     * @return cleaned certificate chain
     */
    public CertificateChain clean(final String host, final List<Certificate> chain) {
        final String normalized = validateHost(host);
        if (chain == null || chain.isEmpty() || chain.stream().anyMatch(certificate -> certificate == null)) {
            throw new ValidateException("Certificate chain must be non-null, non-empty, and contain no null elements");
        }
        final X509Certificate[] certificates = x509Certificates(chain);
        final CertificateChain cleaned = cleanChain(normalized, List.of(certificates));
        if (!trustAll) {
            try {
                final X509Certificate[] cleanedCertificates = x509Certificates(cleaned.certificates());
                trustManager
                        .checkServerTrusted(cleanedCertificates, cleanedCertificates[0].getPublicKey().getAlgorithm());
            } catch (final CertificateException e) {
                throw new ProtocolException("Untrusted certificate chain for " + normalized, e);
            }
        }
        return cleaned;
    }

    /**
     * Checks a certificate chain against this policy.
     *
     * @param host  host
     * @param chain certificate chain
     */
    public void check(final String host, final CertificateChain chain) {
        final String normalized = validateHost(host);
        if (chain == null) {
            throw new ValidateException("Certificate chain must not be null");
        }
        final CertificateChain cleaned = clean(normalized, chain.certificates());
        checkPeer(normalized, cleaned);
    }

    /**
     * Checks a peer chain after the SSL context has accepted it.
     *
     * @param host  host
     * @param chain peer certificate chain
     */
    public void checkPeer(final String host, final CertificateChain chain) {
        final String normalized = validateHost(host);
        if (chain == null) {
            throw new ValidateException("Certificate chain must not be null");
        }
        final CertificateChain checked = chainCleaner == null ? chain : cleanChain(normalized, chain.certificates());
        final Certificate leaf = checked.leaf();
        if (hostnameVerify && leaf instanceof X509Certificate x509 && !verifyHostname(normalized, x509)) {
            throw new ProtocolException("Certificate hostname does not match " + normalized);
        }
        checkPins(normalized, checked);
    }

    /**
     * Returns configured trust manager.
     *
     * @return trust manager
     */
    public X509TrustManager trustManager() {
        return trustManager;
    }

    /**
     * Returns whether hostname verification is enabled.
     *
     * @return true when enabled
     */
    public boolean hostnameVerify() {
        return hostnameVerify;
    }

    /**
     * Returns whether this policy trusts all chains.
     *
     * @return true when trust-all is enabled
     */
    public boolean trustAll() {
        return trustAll;
    }

    /**
     * Returns configured chain cleaner.
     *
     * @return chain cleaner or null
     */
    public CertificateChainCleanerAdapter chainCleaner() {
        return chainCleaner;
    }

    /**
     * Returns configured pins.
     *
     * @return pins by host pattern
     */
    public Map<String, Set<String>> pins() {
        return pins;
    }

    /**
     * Copies pin entries.
     *
     * @param source source pins
     * @return copied pins
     */
    private static Map<String, Set<String>> copyPins(final Map<String, Set<String>> source) {
        final LinkedHashMap<String, Set<String>> copy = new LinkedHashMap<>();
        if (source != null) {
            for (final Map.Entry<String, Set<String>> entry : source.entrySet()) {
                final String host = validatePinHost(entry.getKey());
                final LinkedHashSet<String> values = new LinkedHashSet<>();
                if (entry.getValue() == null) {
                    throw new ValidateException("Certificate pin values must not be null");
                }
                for (final String pin : entry.getValue()) {
                    values.add(validatePin(pin));
                }
                copy.put(host, Set.copyOf(values));
            }
        }
        return Map.copyOf(copy);
    }

    /**
     * Cleans the chain with an adapter when one is configured.
     *
     * @param host         host
     * @param certificates certificates
     * @return cleaned chain
     */
    private CertificateChain cleanChain(final String host, final List<Certificate> certificates) {
        if (chainCleaner == null) {
            return CertificateChain.of(certificates);
        }
        return chainCleaner.clean(certificates, host);
    }

    /**
     * Converts certificates to X509 certificates.
     *
     * @param chain certificate chain
     * @return X509 certificates
     */
    private static X509Certificate[] x509Certificates(final List<Certificate> chain) {
        final X509Certificate[] certificates = new X509Certificate[chain.size()];
        for (int i = 0; i < chain.size(); i++) {
            final Certificate certificate = chain.get(i);
            if (!(certificate instanceof X509Certificate x509)) {
                throw new ProtocolException("Certificate chain must contain X509 certificates");
            }
            certificates[i] = x509;
        }
        return certificates;
    }

    /**
     * Checks configured pins.
     *
     * @param host  host
     * @param chain certificate chain
     */
    private void checkPins(final String host, final CertificateChain chain) {
        final Set<String> hostPins = matchingPins(host);
        if (hostPins.isEmpty()) {
            return;
        }
        for (final Certificate certificate : chain.certificates()) {
            String sha256 = null;
            String sha1 = null;
            for (final String hostPin : hostPins) {
                if (hostPin.startsWith(SHA256_PREFIX)) {
                    if (sha256 == null) {
                        sha256 = pin(certificate);
                    }
                    if (hostPin.equals(sha256)) {
                        return;
                    }
                } else if (hostPin.startsWith(SHA1_PREFIX)) {
                    if (sha1 == null) {
                        sha1 = sha1Pin(certificate);
                    }
                    if (hostPin.equals(sha1)) {
                        return;
                    }
                }
            }
        }
        throw new ProtocolException("Certificate pin does not match " + host);
    }

    /**
     * Computes a certificate pin.
     *
     * @param certificate certificate
     * @return pin
     */
    public static String pin(final Certificate certificate) {
        if (certificate == null) {
            throw new ValidateException("Certificate must not be null");
        }
        if (!(certificate instanceof X509Certificate)) {
            throw new ProtocolException("Certificate pinning requires X509 certificates");
        }
        final byte[] hash = sha256(certificate.getPublicKey().getEncoded());
        return SHA256_PREFIX + Base64.encode(hash);
    }

    /**
     * Computes a SHA-1 certificate pin string.
     *
     * @param certificate certificate
     * @return pin
     */
    public static String sha1Pin(final Certificate certificate) {
        if (certificate == null) {
            throw new ValidateException("Certificate must not be null");
        }
        if (!(certificate instanceof X509Certificate)) {
            throw new ProtocolException("Certificate pinning requires X509 certificates");
        }
        final byte[] hash = digest("SHA-1", certificate.getPublicKey().getEncoded());
        return SHA1_PREFIX + Base64.encode(hash);
    }

    /**
     * Loads the default X509 trust manager.
     *
     * @return trust manager
     */
    private static X509TrustManager defaultTrustManager() {
        try {
            final TrustManagerFactory factory = TrustManagerFactory
                    .getInstance(TrustManagerFactory.getDefaultAlgorithm());
            factory.init((KeyStore) null);
            for (final TrustManager manager : factory.getTrustManagers()) {
                if (manager instanceof X509TrustManager x509) {
                    return x509;
                }
            }
            throw new ProtocolException("Default X509 trust manager is not available");
        } catch (final GeneralSecurityException e) {
            throw new ProtocolException("Default X509 trust manager is not available", e);
        }
    }

    /**
     * Validates a host.
     *
     * @param host host
     * @return normalized host
     */
    private static String validateHost(final String host) {
        if (StringKit.isBlank(host) || StringKit.containsAny(host, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException("Certificate host must be non-blank and single-line");
        }
        final String normalized = host.trim().toLowerCase(Locale.ROOT);
        if (normalized.indexOf('*') >= 0) {
            throw new ValidateException("Certificate host must not contain wildcards");
        }
        return normalized;
    }

    /**
     * Validates a pin host pattern.
     *
     * @param host host or wildcard pattern
     * @return normalized pattern
     */
    private static String validatePinHost(final String host) {
        if (StringKit.isBlank(host) || StringKit.containsAny(host, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException("Certificate pin host must be non-blank and single-line");
        }
        final String normalized = host.trim().toLowerCase(Locale.ROOT);
        if (normalized.startsWith("*.")) {
            final String suffix = normalized.substring(2);
            if (suffix.isBlank() || suffix.indexOf('*') >= 0 || suffix.indexOf(Symbol.C_DOT) < 0) {
                throw new ValidateException("Certificate pin wildcard must cover a concrete domain");
            }
            return "*." + suffix;
        }
        if (normalized.indexOf('*') >= 0) {
            throw new ValidateException("Certificate pin wildcard must start with *.");
        }
        return normalized;
    }

    /**
     * Returns pins that apply to a concrete host.
     *
     * @param host host
     * @return matching pins
     */
    private Set<String> matchingPins(final String host) {
        final LinkedHashSet<String> matched = new LinkedHashSet<>();
        final Set<String> exact = pins.get(host);
        if (exact != null) {
            matched.addAll(exact);
        }
        final int dot = host.indexOf(Symbol.C_DOT);
        if (dot > 0 && dot < host.length() - 1) {
            final Set<String> wildcard = pins.get("*." + host.substring(dot + 1));
            if (wildcard != null) {
                matched.addAll(wildcard);
            }
        }
        return Set.copyOf(matched);
    }

    /**
     * Validates a pin.
     *
     * @param pin pin
     * @return pin
     */
    private static String validatePin(final String pin) {
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
     * Computes a SHA-256 digest.
     *
     * @param value value
     * @return digest
     */
    private static byte[] sha256(final byte[] value) {
        try {
            return digest("SHA-256", value);
        } catch (final ProtocolException e) {
            throw e;
        }
    }

    /**
     * Computes a digest.
     *
     * @param algorithm algorithm
     * @param value     value
     * @return digest
     */
    private static byte[] digest(final String algorithm, final byte[] value) {
        try {
            return MessageDigest.getInstance(algorithm).digest(value);
        } catch (final NoSuchAlgorithmException e) {
            throw new ProtocolException(algorithm + " digest is not available", e);
        }
    }

    /**
     * Verifies a hostname against certificate subject names.
     *
     * @param host        normalized host
     * @param certificate certificate
     * @return true when matched
     */
    private static boolean verifyHostname(final String host, final X509Certificate certificate) {
        try {
            final Collection<List<?>> subjectAltNames = certificate.getSubjectAlternativeNames();
            boolean hasDnsName = false;
            if (subjectAltNames != null) {
                for (final List<?> subjectAltName : subjectAltNames) {
                    if (subjectAltName.size() < 2 || !(subjectAltName.get(0) instanceof Integer type)
                            || subjectAltName.get(1) == null) {
                        continue;
                    }
                    final String value = subjectAltName.get(1).toString();
                    if (type == 2) {
                        hasDnsName = true;
                        if (matchDns(host, value)) {
                            return true;
                        }
                    } else if (type == 7 && host.equalsIgnoreCase(value)) {
                        return true;
                    }
                }
            }
            if (hasDnsName) {
                return false;
            }
            final String commonName = commonName(certificate);
            return commonName != null && matchDns(host, commonName);
        } catch (final CertificateParsingException e) {
            throw new ProtocolException("Unable to parse certificate subject names", e);
        }
    }

    /**
     * Matches a DNS name with optional single-label wildcard support.
     *
     * @param host    normalized host
     * @param pattern certificate DNS pattern
     * @return true when matched
     */
    private static boolean matchDns(final String host, final String pattern) {
        final String value = pattern.toLowerCase(Locale.ROOT);
        if (!value.startsWith("*.")) {
            return host.equals(value);
        }
        final String suffix = value.substring(1);
        if (!host.endsWith(suffix)) {
            return false;
        }
        final String prefix = host.substring(0, host.length() - suffix.length());
        return !prefix.isEmpty() && prefix.indexOf(Symbol.C_DOT) < 0;
    }

    /**
     * Extracts a simple CN from the subject DN.
     *
     * @param certificate certificate
     * @return common name or null
     */
    private static String commonName(final X509Certificate certificate) {
        final String subject = certificate.getSubjectX500Principal().getName();
        for (final String part : subject.split(",")) {
            final String trimmed = part.trim();
            if (trimmed.regionMatches(true, 0, "CN=", 0, 3)) {
                return trimmed.substring(3).trim();
            }
        }
        return null;
    }

    /**
     * Builder for certificate policies.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public static final class Builder {

        /**
         * Trust manager.
         */
        private X509TrustManager trustManager;

        /**
         * Hostname verification flag.
         */
        private boolean hostnameVerify;

        /**
         * Certificate pins.
         */
        private Map<String, Set<String>> pins;

        /**
         * Trust all flag.
         */
        private boolean trustAll;

        /**
         * Chain cleaner.
         */
        private CertificateChainCleanerAdapter chainCleaner;

        /**
         * Creates a builder with defaults.
         */
        private Builder() {
            this.trustManager = defaultTrustManager();
            this.hostnameVerify = true;
            this.pins = new LinkedHashMap<>();
            this.trustAll = false;
            this.chainCleaner = null;
        }

        /**
         * Adds a certificate pin.
         *
         * @param host host
         * @param pin  pin
         * @return this builder
         */
        public Builder pin(final String host, final String pin) {
            final String normalizedHost = validatePinHost(host);
            final String normalizedPin = validatePin(pin);
            final LinkedHashMap<String, Set<String>> next = new LinkedHashMap<>(pins);
            final LinkedHashSet<String> values = new LinkedHashSet<>(next.getOrDefault(normalizedHost, Set.of()));
            values.add(normalizedPin);
            next.put(normalizedHost, values);
            this.pins = next;
            return this;
        }

        /**
         * Sets a trust manager.
         *
         * @param trustManager trust manager
         * @return this builder
         */
        public Builder trustManager(final X509TrustManager trustManager) {
            if (trustManager == null) {
                throw new ValidateException("Trust manager must not be null");
            }
            this.trustManager = trustManager;
            return this;
        }

        /**
         * Sets hostname verification.
         *
         * @param enabled true when enabled
         * @return this builder
         */
        public Builder hostnameVerify(final boolean enabled) {
            this.hostnameVerify = enabled;
            return this;
        }

        /**
         * Sets trust-all mode for isolated integration scenarios.
         *
         * @param enabled true when enabled
         * @return this builder
         */
        public Builder trustAll(final boolean enabled) {
            this.trustAll = enabled;
            return this;
        }

        /**
         * Sets a chain cleaner.
         *
         * @param chainCleaner chain cleaner
         * @return this builder
         */
        public Builder chainCleaner(final CertificateChainCleanerAdapter chainCleaner) {
            if (chainCleaner == null) {
                throw new ValidateException("Certificate chain cleaner must not be null");
            }
            this.chainCleaner = chainCleaner;
            return this;
        }

        /**
         * Uses custom trust roots and the current chain cleaner.
         *
         * @param caCerts CA certificates
         * @return this builder
         */
        public Builder trustRoots(final X509Certificate... caCerts) {
            this.chainCleaner = CertificateChainCleanerAdapter.of(caCerts);
            this.trustManager = new RootTrustManager(caCerts);
            return this;
        }

        /**
         * Builds a certificate policy.
         *
         * @return certificate policy
         */
        public CertificatePolicy build() {
            if (trustAll && (!pins.isEmpty() || hostnameVerify)) {
                throw new ValidateException("Trust-all policy cannot be combined with hostname verification or pins");
            }
            return new CertificatePolicy(trustManager, hostnameVerify, pins, trustAll, chainCleaner);
        }

    }

    /**
     * Trust manager backed by configured root certificates.
     */
    private static final class RootTrustManager implements X509TrustManager {

        /**
         * Accepted issuers.
         */
        private final X509Certificate[] acceptedIssuers;

        /**
         * Chain cleaner.
         */
        private final CertificateChainCleanerAdapter cleaner;

        /**
         * Creates a trust manager.
         *
         * @param caCerts CA certificates
         */
        private RootTrustManager(final X509Certificate... caCerts) {
            if (caCerts == null || caCerts.length == 0
                    || java.util.Arrays.stream(caCerts).anyMatch(cert -> cert == null)) {
                throw new ValidateException(
                        "CA certificates must be non-null, non-empty, and contain no null elements");
            }
            this.acceptedIssuers = caCerts.clone();
            this.cleaner = CertificateChainCleanerAdapter.of(caCerts);
        }

        /**
         * Checks client trust.
         *
         * @param chain    chain
         * @param authType auth type
         */
        @Override
        public void checkClientTrusted(final X509Certificate[] chain, final String authType)
                throws CertificateException {
            checkTrusted(chain);
        }

        /**
         * Checks server trust.
         *
         * @param chain    chain
         * @param authType auth type
         */
        @Override
        public void checkServerTrusted(final X509Certificate[] chain, final String authType)
                throws CertificateException {
            checkTrusted(chain);
        }

        /**
         * Returns accepted issuers.
         *
         * @return accepted issuers
         */
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return acceptedIssuers.clone();
        }

        /**
         * Checks a chain against the configured cleaner.
         *
         * @param chain chain
         * @throws CertificateException when not trusted
         */
        private void checkTrusted(final X509Certificate[] chain) throws CertificateException {
            try {
                cleaner.clean(List.of(chain), "trusted");
            } catch (final RuntimeException e) {
                throw new CertificateException("Certificate chain is not trusted by configured roots", e);
            }
        }

    }

}
