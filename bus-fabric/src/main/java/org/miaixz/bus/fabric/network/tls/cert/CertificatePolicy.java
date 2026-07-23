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

import javax.net.ssl.X509TrustManager;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.tls.AnyTrustManager;
import org.miaixz.bus.core.xyz.NetKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.crypto.builtin.CertificateChain;
import org.miaixz.bus.crypto.builtin.CertificateChainCleaner;
import org.miaixz.bus.crypto.builtin.CertificatePin;
import org.miaixz.bus.fabric.Options;
import org.miaixz.bus.fabric.Policy;

/**
 * TLS certificate validation policy with trust manager, hostname, and pin checks.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class CertificatePolicy implements Policy {

    /**
     * Typed option for the TLS certificate policy.
     */
    public static final Options.Key<CertificatePolicy> OPTION = Options
            .key("tls.certificate.policy", CertificatePolicy.class);

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
    private final CertificateChainCleaner chainCleaner;

    /**
     * Stable, non-sensitive TLS context reuse identity.
     */
    private final ReuseIdentity explicitReuseIdentity;

    /**
     * Creates a certificate policy.
     *
     * @param trustManager   trust manager
     * @param hostnameVerify hostname verification flag
     * @param pins           certificate pins
     * @param trustAll       trust all flag
     * @param chainCleaner   chain cleaner
     * @param reuseIdentity  explicit TLS session reuse identity, or {@code null}
     */
    private CertificatePolicy(final X509TrustManager trustManager, final boolean hostnameVerify,
            final Map<String, Set<String>> pins, final boolean trustAll, final CertificateChainCleaner chainCleaner,
            final ReuseIdentity reuseIdentity) {
        this.trustManager = Assert.notNull(trustManager, () -> new ValidateException("Trust manager must not be null"));
        this.hostnameVerify = hostnameVerify;
        this.pins = copyPins(pins);
        this.trustAll = trustAll;
        this.chainCleaner = chainCleaner;
        this.explicitReuseIdentity = reuseIdentity;
    }

    /**
     * Returns the system trust policy.
     *
     * @return policy using the platform trust manager and hostname verification
     */
    public static CertificatePolicy trustSystem() {
        return builder().build();
    }

    /**
     * Resolves the certificate policy from options.
     *
     * @param options option source
     * @return configured policy or system trust policy
     */
    public static CertificatePolicy resolve(final Options options) {
        final Options current = Assert.notNull(options, () -> new ValidateException("Options must not be null"));
        final CertificatePolicy configured = current.get(OPTION);
        return configured == null ? trustSystem() : configured;
    }

    /**
     * Adds this policy to an immutable option snapshot.
     *
     * @param options option source
     * @return updated option snapshot
     */
    @Override
    public Options from(final Options options) {
        return Assert.notNull(options, () -> new ValidateException("Options must not be null")).with(OPTION, this);
    }

    /**
     * Creates a builder.
     *
     * @return new certificate policy builder with system-trust defaults
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates an opaque token that can be shared by explicitly equivalent policy builders.
     *
     * @return new reuse identity
     */
    public static ReuseIdentity newReuseIdentity() {
        return new ReuseIdentity();
    }

    /**
     * Cleans and verifies a certificate chain.
     *
     * @param host  peer host used by chain cleaning and diagnostics
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
     * @param host  peer host used for trust, hostname, and pin validation
     * @param chain certificate chain
     */
    public void check(final String host, final CertificateChain chain) {
        final String normalized = validateHost(host);
        Assert.notNull(chain, () -> new ValidateException("Certificate chain must not be null"));
        final CertificateChain cleaned = clean(normalized, chain.certificates());
        checkPeer(normalized, cleaned);
    }

    /**
     * Checks a peer chain after the SSL context has accepted it.
     *
     * @param host  peer host used for hostname and pin validation
     * @param chain peer certificate chain
     */
    public void checkPeer(final String host, final CertificateChain chain) {
        final String normalized = validateHost(host);
        final CertificateChain checkedChain = Assert
                .notNull(chain, () -> new ValidateException("Certificate chain must not be null"));
        final CertificateChain checked = chainCleaner == null ? checkedChain
                : cleanChain(normalized, checkedChain.certificates());
        final Certificate leaf = checked.leaf();
        if (hostnameVerify && leaf instanceof X509Certificate x509 && !verifyHostname(normalized, x509)) {
            throw new ProtocolException("Certificate hostname does not match " + normalized);
        }
        checkPins(normalized, checked);
    }

    /**
     * Returns configured trust manager.
     *
     * @return X.509 trust manager used for chain validation
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
     * @return configured chain cleaner, or {@code null} when cleaning is disabled
     */
    public CertificateChainCleaner chainCleaner() {
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
     * Returns this policy's stable, non-sensitive TLS context reuse identity.
     *
     * @return explicit shared token, or this policy instance when no token is configured
     */
    public Object reuseIdentity() {
        return explicitReuseIdentity == null ? this : explicitReuseIdentity;
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
     * @param host         normalized peer host supplied to the cleaner
     * @param certificates certificate chain to wrap or clean
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
     * @param host  normalized peer host whose exact and wildcard pins are selected
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
                if (hostPin.startsWith(CertificatePin.SHA256_PREFIX)) {
                    if (sha256 == null) {
                        sha256 = pin(certificate);
                    }
                    if (hostPin.equals(sha256)) {
                        return;
                    }
                } else if (hostPin.startsWith(CertificatePin.SHA1_PREFIX)) {
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
     * @param certificate certificate whose subject public key is hashed
     * @return SHA-256 certificate pin string
     */
    public static String pin(final Certificate certificate) {
        return CertificatePin.sha256(certificate);
    }

    /**
     * Computes a SHA-1 certificate pin string.
     *
     * @param certificate certificate whose subject public key is hashed
     * @return SHA-1 certificate pin string
     */
    public static String sha1Pin(final Certificate certificate) {
        return CertificatePin.sha1(certificate);
    }

    /**
     * Loads the default X509 trust manager.
     *
     * @return trust manager
     */
    private static X509TrustManager defaultTrustManager() {
        final X509TrustManager trustManager = AnyTrustManager.getDefaultTrustManager();
        if (trustManager == null) {
            throw new ProtocolException("Default X509 trust manager is not available");
        }
        return trustManager;
    }

    /**
     * Validates a host.
     *
     * @param host concrete peer host without wildcards
     * @return normalized host
     */
    private static String validateHost(final String host) {
        final String normalized = NetKit.normalizeHost(host);
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
            final String suffix = NetKit.normalizeHost(normalized.substring(Normal._2));
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
     * @param host normalized concrete peer host
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
     * @param pin candidate SHA-256 or SHA-1 pin string
     * @return validated canonical pin string
     */
    private static String validatePin(final String pin) {
        return CertificatePin.validate(pin);
    }

    /**
     * Verifies a hostname against certificate subject names.
     *
     * @param host        normalized host
     * @param certificate leaf X.509 certificate whose subject names are inspected
     * @return true when matched
     */
    private static boolean verifyHostname(final String host, final X509Certificate certificate) {
        try {
            final Collection<List<?>> subjectAltNames = certificate.getSubjectAlternativeNames();
            boolean hasDnsName = false;
            if (subjectAltNames != null) {
                for (final List<?> subjectAltName : subjectAltNames) {
                    if (subjectAltName.size() < Normal._2 || !(subjectAltName.get(Normal._0) instanceof Integer type)
                            || subjectAltName.get(Normal._1) == null) {
                        continue;
                    }
                    final String value = subjectAltName.get(Normal._1).toString();
                    if (type == Normal._2) {
                        hasDnsName = true;
                        if (matchDns(host, value)) {
                            return true;
                        }
                    } else if (type == Normal._7 && host.equalsIgnoreCase(value)) {
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
     * @param certificate X.509 certificate whose subject DN is inspected
     * @return common name or null
     */
    private static String commonName(final X509Certificate certificate) {
        final String subject = certificate.getSubjectX500Principal().getName();
        for (final String part : subject.split(",")) {
            final String trimmed = part.trim();
            if (trimmed.regionMatches(true, Normal._0, "CN=", Normal._0, Normal._3)) {
                return trimmed.substring(Normal._3).trim();
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
        private CertificateChainCleaner chainCleaner;

        /**
         * Explicit reuse identity, or null to isolate every built policy.
         */
        private ReuseIdentity reuseIdentity;

        /**
         * Creates a builder with defaults.
         */
        private Builder() {
            this.trustManager = defaultTrustManager();
            this.hostnameVerify = true;
            this.pins = new LinkedHashMap<>();
            this.trustAll = false;
            this.chainCleaner = null;
            this.reuseIdentity = null;
        }

        /**
         * Adds a certificate pin.
         *
         * @param host exact host or single-label wildcard pattern
         * @param pin  SHA-256 or SHA-1 public-key pin
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
            this.trustManager = Assert
                    .notNull(trustManager, () -> new ValidateException("Trust manager must not be null"));
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
        public Builder chainCleaner(final CertificateChainCleaner chainCleaner) {
            this.chainCleaner = Assert
                    .notNull(chainCleaner, () -> new ValidateException("Certificate chain cleaner must not be null"));
            return this;
        }

        /**
         * Explicitly declares this policy equivalent to other policies built with the same opaque token.
         *
         * @param reuseIdentity shared reuse identity
         * @return this builder
         */
        public Builder reuseIdentity(final ReuseIdentity reuseIdentity) {
            this.reuseIdentity = Assert
                    .notNull(reuseIdentity, () -> new ValidateException("Reuse identity must not be null"));
            return this;
        }

        /**
         * Uses custom trust roots and the current chain cleaner.
         *
         * @param caCerts CA certificates
         * @return this builder
         */
        public Builder trustRoots(final X509Certificate... caCerts) {
            this.chainCleaner = CertificateChainCleaner.of(caCerts);
            this.trustManager = new RootTrustManager(caCerts);
            return this;
        }

        /**
         * Builds a certificate policy.
         *
         * @return immutable certificate policy snapshot
         */
        public CertificatePolicy build() {
            if (trustAll && (!pins.isEmpty() || hostnameVerify)) {
                throw new ValidateException("Trust-all policy cannot be combined with hostname verification or pins");
            }
            return new CertificatePolicy(trustManager, hostnameVerify, pins, trustAll, chainCleaner, reuseIdentity);
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
        private final CertificateChainCleaner cleaner;

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
            this.cleaner = CertificateChainCleaner.of(caCerts);
        }

        /**
         * Checks client trust.
         *
         * @param chain    presented client certificate chain
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
         * @param chain    presented server certificate chain
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
         * @param chain presented X.509 chain to validate against configured roots
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

    /**
     * Opaque capability used to declare that separately built policies are safe to reuse together.
     *
     * <p>
     * Identity equality is deliberately based on the token instance. The token contains no certificate, key, hostname,
     * pin, trust-manager text, or other sensitive material.
     * </p>
     */
    public static final class ReuseIdentity {

        /**
         * Creates an opaque identity token.
         */
        private ReuseIdentity() {
        }

    }

}
