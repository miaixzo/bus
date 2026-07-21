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
     * TLS versions.
     */
    private final List<String> versions;

    /**
     * TLS cipher suites.
     */
    private final List<String> ciphers;

    /**
     * Client authentication mode.
     */
    private final TlsClientAuth clientAuth;

    /**
     * Hostname verification flag.
     */
    private final boolean verifyHostname;

    /**
     * Certificate policy.
     */
    private final CertificatePolicy certificate;

    /**
     * ALPN application protocols.
     */
    private final List<String> applicationProtocols;

    /**
     * TLS extension flag for SNI and ALPN.
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
     * @param versions             versions
     * @param ciphers              ciphers
     * @param clientAuth           client authentication mode
     * @param verifyHostname       hostname verification flag
     * @param certificate          certificate policy
     * @param applicationProtocols ALPN application protocols
     * @param tlsExtensions        TLS extensions flag
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
     * @return settings
     */
    public static TlsSettings defaults() {
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
     * Returns TLS version snapshot.
     *
     * @return versions
     */
    public List<String> versions() {
        return versions;
    }

    /**
     * Returns cipher suite snapshot.
     *
     * @return ciphers
     */
    public List<String> ciphers() {
        return ciphers;
    }

    /**
     * Returns whether client authentication is enabled.
     *
     * @return true when enabled
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
     * @return true when enabled
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
     * @return application protocols
     */
    public List<String> applicationProtocols() {
        return applicationProtocols;
    }

    /**
     * Returns whether TLS extensions such as SNI and ALPN are enabled.
     *
     * @return true when enabled
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
     * @return default cipher suites
     */
    private static List<String> defaultCiphers() {
        final SSLEngine engine = defaultEngine();
        return List.of(engine.getEnabledCipherSuites());
    }

    /**
     * Returns supported cipher suites from the current JDK TLS engine.
     *
     * @return supported cipher suites
     */
    private static Set<String> supportedCiphers() {
        final SSLEngine engine = defaultEngine();
        return Set.copyOf(Arrays.asList(engine.getSupportedCipherSuites()));
    }

    /**
     * Creates a default engine without opening a connection.
     *
     * @return engine
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
     * @param versions versions
     * @return version snapshot
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
     * @param ciphers      ciphers
     * @param allowDefault whether empty means JDK default
     * @return cipher snapshot
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
     * @param protocols protocols
     * @return protocol snapshot
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
     * @param value value
     * @param name  field name
     * @return token
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
         * TLS versions.
         */
        private List<String> versions;

        /**
         * TLS cipher suites.
         */
        private List<String> ciphers;

        /**
         * Client authentication mode.
         */
        private TlsClientAuth clientAuth;

        /**
         * Hostname verification flag.
         */
        private boolean verifyHostname;

        /**
         * Certificate policy.
         */
        private CertificatePolicy certificate;

        /**
         * ALPN application protocols.
         */
        private List<String> applicationProtocols;

        /**
         * TLS extension flag.
         */
        private boolean tlsExtensions;

        /**
         * Creates a builder with safe defaults.
         */
        private Builder() {
            this.versions = org.miaixz.bus.fabric.Builder.TLS_SETTINGS_DEFAULT_VERSIONS;
            this.ciphers = List.of();
            this.clientAuth = TlsClientAuth.NONE;
            this.verifyHostname = true;
            this.certificate = CertificatePolicy.trustSystem();
            this.applicationProtocols = List.of();
            this.tlsExtensions = true;
        }

        /**
         * Sets TLS versions.
         *
         * @param versions versions
         * @return this builder
         */
        public Builder versions(final List<String> versions) {
            this.versions = validateVersions(versions);
            return this;
        }

        /**
         * Sets TLS cipher suites.
         *
         * @param ciphers ciphers
         * @return this builder
         */
        public Builder ciphers(final List<String> ciphers) {
            this.ciphers = validateCiphers(ciphers, false);
            return this;
        }

        /**
         * Sets TLS cipher suites.
         *
         * @param ciphers ciphers
         * @return this builder
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
         * @param ciphers ciphers
         * @return this builder
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
         * @param enabled true when enabled
         * @return this builder
         */
        public Builder clientAuth(final boolean enabled) {
            this.clientAuth = enabled ? TlsClientAuth.REQUIRE : TlsClientAuth.NONE;
            return this;
        }

        /**
         * Sets client authentication mode.
         *
         * @param mode client authentication mode
         * @return this builder
         */
        public Builder clientAuth(final TlsClientAuth mode) {
            this.clientAuth = Assert.notNull(mode, () -> new ValidateException("Client auth mode must not be null"));
            return this;
        }

        /**
         * Sets hostname verification.
         *
         * @param enabled true when enabled
         * @return this builder
         */
        public Builder verifyHostname(final boolean enabled) {
            this.verifyHostname = enabled;
            return this;
        }

        /**
         * Sets certificate policy.
         *
         * @param policy certificate policy
         * @return this builder
         */
        public Builder certificate(final CertificatePolicy policy) {
            this.certificate = Assert
                    .notNull(policy, () -> new ValidateException("Certificate policy must not be null"));
            return this;
        }

        /**
         * Sets ALPN application protocols.
         *
         * @param protocols protocols
         * @return this builder
         */
        public Builder applicationProtocols(final List<String> protocols) {
            this.applicationProtocols = validateApplicationProtocols(protocols);
            return this;
        }

        /**
         * Sets TLS extension support for SNI and ALPN.
         *
         * @param enabled true when enabled
         * @return this builder
         */
        public Builder supportsTlsExtensions(final boolean enabled) {
            this.tlsExtensions = enabled;
            return this;
        }

        /**
         * Builds immutable TLS settings.
         *
         * @return settings
         */
        public TlsSettings build() {
            return new TlsSettings(versions, ciphers, clientAuth, verifyHostname, certificate, applicationProtocols,
                    tlsExtensions);
        }

    }

}
