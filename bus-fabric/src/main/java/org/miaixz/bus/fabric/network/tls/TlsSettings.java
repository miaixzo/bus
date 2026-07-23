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
package org.miaixz.bus.fabric.network.tls;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.tls.TlsCipherSuite;
import org.miaixz.bus.core.net.tls.TlsClientAuth;
import org.miaixz.bus.core.net.tls.TlsVersion;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.network.tls.cert.CertificatePolicy;

/**
 * Immutable TLS settings selected before handshake.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class TlsSettings {

    /**
     * Immutable, de-duplicated Java TLS protocol names in preference order.
     */
    private final List<String> versions;

    /**
     * Immutable, de-duplicated JDK cipher-suite names in preference order.
     */
    private final List<String> ciphers;

    /**
     * Server-side client-certificate authentication mode.
     */
    private final TlsClientAuth clientAuth;

    /**
     * Whether client handshakes verify the peer hostname.
     */
    private final boolean verifyHostname;

    /**
     * Trust and local-certificate policy used to build TLS contexts.
     */
    private final CertificatePolicy certificate;

    /**
     * Immutable, de-duplicated ALPN protocol identifiers in preference order.
     */
    private final List<String> applicationProtocols;

    /**
     * Whether SNI and ALPN extensions may be configured on the engine.
     */
    private final boolean tlsExtensions;

    /**
     * Stable certificate-policy identity captured at construction.
     */
    private final Object certificateIdentity;

    /**
     * Precomputed complete configuration hash.
     */
    private final int hashCode;

    /**
     * Creates immutable TLS settings.
     *
     * @param versions             TLS protocol names to validate and snapshot
     * @param ciphers              cipher-suite names, or an empty list to select JDK defaults
     * @param clientAuth           client authentication mode
     * @param verifyHostname       whether peer hostnames are verified
     * @param certificate          trust and local-certificate policy
     * @param applicationProtocols ALPN identifiers in preference order
     * @param tlsExtensions        whether SNI and ALPN extensions are supported
     */
    private TlsSettings(final List<String> versions, final List<String> ciphers, final TlsClientAuth clientAuth,
            final boolean verifyHostname, final CertificatePolicy certificate, final List<String> applicationProtocols,
            final boolean tlsExtensions) {
        this.versions = validateVersions(versions);
        this.ciphers = validateCiphers(ciphers, true);
        this.clientAuth = Assert.notNull(clientAuth, () -> new ValidateException("Client auth mode must not be null"));
        this.verifyHostname = verifyHostname;
        this.certificate = Assert
                .notNull(certificate, () -> new ValidateException("Certificate policy must not be null"));
        this.applicationProtocols = validateApplicationProtocols(applicationProtocols);
        this.tlsExtensions = tlsExtensions;
        this.certificateIdentity = this.certificate.reuseIdentity();
        this.hashCode = computeHashCode();
    }

    /**
     * Returns default TLS settings.
     *
     * @return immutable settings using safe protocol, trust, verification, and extension defaults
     */
    public static TlsSettings defaults() {
        return builder().build();
    }

    /**
     * Creates a builder.
     *
     * @return a new TLS settings builder initialized with safe defaults
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns TLS version snapshot.
     *
     * @return immutable TLS protocol-name snapshot in preference order
     */
    public List<String> versions() {
        return versions;
    }

    /**
     * Returns cipher suite snapshot.
     *
     * @return immutable enabled cipher-suite snapshot in preference order
     */
    public List<String> ciphers() {
        return ciphers;
    }

    /**
     * Returns whether client authentication is enabled.
     *
     * @return {@code true} when the configured mode requests or requires a client certificate
     */
    public boolean clientAuth() {
        return clientAuth.enabled();
    }

    /**
     * Returns client authentication mode.
     *
     * @return client authentication mode
     */
    public TlsClientAuth clientAuthMode() {
        return clientAuth;
    }

    /**
     * Returns whether hostname verification is enabled.
     *
     * @return {@code true} when peer hostname verification is enabled
     */
    public boolean verifyHostname() {
        return verifyHostname;
    }

    /**
     * Returns the certificate policy.
     *
     * @return certificate policy
     */
    public CertificatePolicy certificate() {
        return certificate;
    }

    /**
     * Returns ALPN application protocol snapshot.
     *
     * @return immutable ALPN identifier snapshot in preference order
     */
    public List<String> applicationProtocols() {
        return applicationProtocols;
    }

    /**
     * Returns whether TLS extensions such as SNI and ALPN are enabled.
     *
     * @return {@code true} when SNI and ALPN extensions may be configured
     */
    public boolean supportsTlsExtensions() {
        return tlsExtensions;
    }

    /**
     * Returns whether another object has the same complete TLS configuration identity.
     *
     * @param object other object
     * @return true when the complete configuration is equivalent
     */
    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof TlsSettings other) || hashCode != other.hashCode) {
            return false;
        }
        return verifyHostname == other.verifyHostname && tlsExtensions == other.tlsExtensions
                && clientAuth == other.clientAuth && certificateIdentity == other.certificateIdentity
                && versions.equals(other.versions) && ciphers.equals(other.ciphers)
                && applicationProtocols.equals(other.applicationProtocols);
    }

    /**
     * Returns the precomputed complete TLS configuration hash.
     *
     * @return configuration hash
     */
    @Override
    public int hashCode() {
        return hashCode;
    }

    /**
     * Computes the complete TLS configuration hash once during construction.
     *
     * @return configuration hash
     */
    private int computeHashCode() {
        int result = versions.hashCode();
        result = 31 * result + ciphers.hashCode();
        result = 31 * result + clientAuth.hashCode();
        result = 31 * result + Boolean.hashCode(verifyHostname);
        result = 31 * result + System.identityHashCode(certificateIdentity);
        result = 31 * result + applicationProtocols.hashCode();
        return 31 * result + Boolean.hashCode(tlsExtensions);
    }

    /**
     * Returns default cipher suites from the current JDK TLS engine.
     *
     * @return immutable snapshot of cipher suites enabled by a default JDK TLS engine
     */
    private static List<String> defaultCiphers() {
        final SSLEngine engine = defaultEngine();
        final ArrayList<String> ciphers = new ArrayList<>(Arrays.asList(engine.getEnabledCipherSuites()));
        // AES-128-GCM is the modern JSSE/HTTP client baseline and materially reduces full-handshake CPU.
        final String preferred = TlsCipherSuite.TLS_AES_128_GCM_SHA256.javaName();
        if (ciphers.remove(preferred))
            ciphers.add(0, preferred);
        return List.copyOf(ciphers);
    }

    /**
     * Returns supported cipher suites from the current JDK TLS engine.
     *
     * @return immutable set of cipher suites supported by a default JDK TLS engine
     */
    private static Set<String> supportedCiphers() {
        final SSLEngine engine = defaultEngine();
        return Set.copyOf(Arrays.asList(engine.getSupportedCipherSuites()));
    }

    /**
     * Creates a default engine without opening a connection.
     *
     * @return unconnected engine created from the process default TLS context
     * @throws ProtocolException if the default TLS context algorithm is unavailable
     */
    private static SSLEngine defaultEngine() {
        try {
            return SSLContext.getDefault().createSSLEngine();
        } catch (final java.security.NoSuchAlgorithmException e) {
            throw new ProtocolException("Default TLS engine is not available", e);
        }
    }

    /**
     * Validates TLS versions.
     *
     * @param versions non-empty TLS protocol names to validate
     * @return immutable, de-duplicated Java protocol-name snapshot preserving input order
     * @throws ValidateException if the list is empty or contains an unsupported or invalid protocol
     */
    private static List<String> validateVersions(final List<String> versions) {
        if (versions == null || versions.isEmpty()) {
            throw new ValidateException("TLS versions must not be null or empty");
        }
        final LinkedHashSet<String> checked = new LinkedHashSet<>();
        for (final String version : versions) {
            final String token = validateToken(version, "TLS version");
            final TlsVersion tlsVersion;
            try {
                tlsVersion = TlsVersion.forJavaName(token);
            } catch (final IllegalArgumentException e) {
                throw new ValidateException("Unsupported TLS version: " + token);
            }
            if (tlsVersion == TlsVersion.SSLv3) {
                throw new ValidateException("Unsupported TLS version: " + token);
            }
            checked.add(tlsVersion.javaName());
        }
        return List.copyOf(checked);
    }

    /**
     * Validates cipher suites.
     *
     * @param ciphers      cipher-suite names to validate
     * @param allowDefault whether an empty list selects the JDK enabled defaults
     * @return immutable, de-duplicated JDK cipher-name snapshot preserving input order
     * @throws ValidateException if the list is invalid, empty when prohibited, or contains an unsupported cipher
     */
    private static List<String> validateCiphers(final List<String> ciphers, final boolean allowDefault) {
        final List<String> checkedCiphers = Assert
                .notNull(ciphers, () -> new ValidateException("TLS ciphers must not be null or empty"));
        if (!allowDefault && checkedCiphers.isEmpty()) {
            throw new ValidateException("TLS ciphers must not be null or empty");
        }
        if (checkedCiphers.isEmpty()) {
            return defaultCiphers();
        }
        final Set<String> supported = supportedCiphers();
        final LinkedHashSet<String> checked = new LinkedHashSet<>();
        for (final String cipher : checkedCiphers) {
            final String token = validateToken(cipher, "TLS cipher");
            final String javaName = TlsCipherSuite.resolveJavaName(token, supported);
            if (!supported.contains(javaName)) {
                throw new ValidateException("Unsupported TLS cipher: " + token);
            }
            checked.add(javaName);
        }
        return List.copyOf(checked);
    }

    /**
     * Validates ALPN application protocols.
     *
     * @param protocols ALPN identifiers to validate
     * @return immutable, de-duplicated protocol snapshot preserving input order
     * @throws ValidateException if the list is {@code null} or contains an invalid identifier
     */
    private static List<String> validateApplicationProtocols(final List<String> protocols) {
        final List<String> checkedProtocols = Assert
                .notNull(protocols, () -> new ValidateException("TLS application protocols must not be null"));
        final LinkedHashSet<String> checked = new LinkedHashSet<>();
        for (final String protocol : checkedProtocols) {
            checked.add(validateToken(protocol, "TLS application protocol"));
        }
        return List.copyOf(checked);
    }

    /**
     * Validates a single-line token.
     *
     * @param value token text to validate and trim
     * @param name  logical field name included in the validation error
     * @return trimmed, non-blank, single-line token
     * @throws ValidateException if the token is blank or contains a line break
     */
    private static String validateToken(final String value, final String name) {
        if (StringKit.isBlank(value) || StringKit.containsAny(value, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException(name + " must be non-blank and single-line");
        }
        return value.trim();
    }

    /**
     * Builder for TLS settings.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public static final class Builder {

        /**
         * Validated TLS protocol names in preference order.
         */
        private List<String> versions;

        /**
         * Validated cipher-suite names, or an empty list to select JDK enabled defaults at build time.
         */
        private List<String> ciphers;

        /**
         * Server-side client-certificate authentication mode.
         */
        private TlsClientAuth clientAuth;

        /**
         * Whether client handshakes verify the peer hostname.
         */
        private boolean verifyHostname;

        /**
         * Trust and local-certificate policy.
         */
        private CertificatePolicy certificate;

        /**
         * Validated ALPN identifiers in preference order.
         */
        private List<String> applicationProtocols;

        /**
         * Whether SNI and ALPN extensions may be configured.
         */
        private boolean tlsExtensions;

        /**
         * Creates a builder with safe defaults.
         */
        private Builder() {
            this.versions = org.miaixz.bus.fabric.Builder.TLS_SETTINGS_DEFAULT_VERSIONS;
            this.ciphers = List.of(TlsCipherSuite.TLS_AES_128_GCM_SHA256.javaName());
            this.clientAuth = TlsClientAuth.NONE;
            this.verifyHostname = true;
            this.certificate = CertificatePolicy.trustSystem();
            this.applicationProtocols = List.of();
            this.tlsExtensions = true;
        }

        /**
         * Sets TLS versions.
         *
         * @param versions non-empty Java or standard TLS protocol names in preference order
         * @return this builder
         * @throws ValidateException if the list is empty or contains an unsupported or invalid protocol
         */
        public Builder versions(final List<String> versions) {
            this.versions = validateVersions(versions);
            return this;
        }

        /**
         * Sets TLS cipher suites.
         *
         * @param ciphers non-empty JDK or standard cipher-suite names in preference order
         * @return this builder
         * @throws ValidateException if the list is empty or contains an unsupported or invalid cipher
         */
        public Builder ciphers(final List<String> ciphers) {
            this.ciphers = validateCiphers(ciphers, false);
            return this;
        }

        /**
         * Sets TLS cipher suites.
         *
         * @param ciphers non-empty cipher-suite constants in preference order
         * @return this builder
         * @throws NullPointerException if any array element is {@code null}
         * @throws ValidateException    if the array is {@code null}, empty, or resolves to an unsupported cipher
         */
        public Builder ciphers(final TlsCipherSuite... ciphers) {
            return cipherSuites(
                    List.of(
                            Assert.notNull(
                                    ciphers,
                                    () -> new ValidateException("TLS cipher suites must not be null"))));
        }

        /**
         * Sets TLS cipher suites.
         *
         * @param ciphers non-empty cipher-suite constants in preference order
         * @return this builder
         * @throws ValidateException if the list is empty or resolves to an unsupported cipher
         */
        public Builder cipherSuites(final List<TlsCipherSuite> ciphers) {
            this.ciphers = validateCiphers(TlsCipherSuite.javaNames(ciphers), false);
            return this;
        }

        /**
         * Uses the JDK default enabled cipher suites.
         *
         * @return this builder
         */
        public Builder allEnabledCipherSuites() {
            this.ciphers = List.of();
            return this;
        }

        /**
         * Sets client authentication.
         *
         * @param enabled {@code true} to require client certificates, or {@code false} to disable client authentication
         * @return this builder
         */
        public Builder clientAuth(final boolean enabled) {
            this.clientAuth = enabled ? TlsClientAuth.REQUIRE : TlsClientAuth.NONE;
            return this;
        }

        /**
         * Sets client authentication mode.
         *
         * @param mode explicit client-certificate authentication mode
         * @return this builder
         * @throws ValidateException if {@code mode} is {@code null}
         */
        public Builder clientAuth(final TlsClientAuth mode) {
            this.clientAuth = Assert.notNull(mode, () -> new ValidateException("Client auth mode must not be null"));
            return this;
        }

        /**
         * Sets hostname verification.
         *
         * @param enabled whether client handshakes verify the peer hostname
         * @return this builder
         */
        public Builder verifyHostname(final boolean enabled) {
            this.verifyHostname = enabled;
            return this;
        }

        /**
         * Sets certificate policy.
         *
         * @param policy trust and local-certificate policy
         * @return this builder
         * @throws ValidateException if {@code policy} is {@code null}
         */
        public Builder certificate(final CertificatePolicy policy) {
            this.certificate = Assert
                    .notNull(policy, () -> new ValidateException("Certificate policy must not be null"));
            return this;
        }

        /**
         * Sets ALPN application protocols.
         *
         * @param protocols ALPN identifiers in preference order; an empty list disables ALPN offerings
         * @return this builder
         * @throws ValidateException if the list is {@code null} or contains an invalid identifier
         */
        public Builder applicationProtocols(final List<String> protocols) {
            this.applicationProtocols = validateApplicationProtocols(protocols);
            return this;
        }

        /**
         * Sets TLS extension support for SNI and ALPN.
         *
         * @param enabled whether SNI and ALPN extensions may be configured
         * @return this builder
         */
        public Builder supportsTlsExtensions(final boolean enabled) {
            this.tlsExtensions = enabled;
            return this;
        }

        /**
         * Builds immutable TLS settings.
         *
         * @return immutable, fully validated TLS settings
         */
        public TlsSettings build() {
            return new TlsSettings(versions, ciphers, clientAuth, verifyHostname, certificate, applicationProtocols,
                    tlsExtensions);
        }

    }

}
