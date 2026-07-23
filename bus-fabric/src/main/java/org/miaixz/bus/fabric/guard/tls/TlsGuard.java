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
package org.miaixz.bus.fabric.guard.tls;

import java.util.Locale;

import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.core.xyz.UrlKit;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Builder;
import org.miaixz.bus.fabric.guard.GuardResult;
import org.miaixz.bus.fabric.network.tls.TlsSettings;

/**
 * Guard that rejects plaintext routes and weak TLS settings.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class TlsGuard {

    /**
     * Creates a TLS guard.
     */
    private TlsGuard() {
        // No initialization required.
    }

    /**
     * Returns the secure TLS guard.
     *
     * @return process-wide stateless secure-TLS guard
     */
    public static TlsGuard requireSecure() {
        return Instances.get(TlsGuard.class.getName() + ".secure", TlsGuard::new);
    }

    /**
     * Checks whether an address requires TLS.
     *
     * @param address route address whose scheme is validated and security flag is checked
     * @return passing result for a secure address, or rejection naming the normalized plaintext scheme
     * @throws ValidateException if {@code address} is {@code null}
     * @throws ProtocolException if the address scheme is invalid
     */
    public GuardResult check(final Address address) {
        final Address checkedAddress = Assert.notNull(address, () -> new ValidateException("Address must not be null"));
        final String scheme = scheme(checkedAddress);
        if (checkedAddress.secure()) {
            return GuardResult.pass();
        }
        return GuardResult.reject("tls required for scheme: " + scheme);
    }

    /**
     * Checks TLS versions, hostname verification, and cipher suites in that order for weak configuration.
     *
     * @param settings immutable TLS configuration to inspect
     * @return first weak-version, disabled-hostname-verification, or weak-cipher rejection; otherwise a passing result
     * @throws ValidateException if {@code settings} is {@code null}
     * @throws ProtocolException if a configured cipher name is blank or multi-line
     */
    public GuardResult check(final TlsSettings settings) {
        final TlsSettings checkedSettings = Assert
                .notNull(settings, () -> new ValidateException("TLS settings must not be null"));
        for (final String version : checkedSettings.versions()) {
            if (Protocol.TLSv1.name.equals(version) || Protocol.TLSv1_1.name.equals(version)) {
                return GuardResult.reject("weak tls version: " + version);
            }
        }
        if (!checkedSettings.verifyHostname()) {
            return GuardResult.reject("hostname verification required");
        }
        for (final String cipher : checkedSettings.ciphers()) {
            if (weakCipher(cipher)) {
                return GuardResult.reject("weak tls cipher: " + cipher);
            }
        }
        return GuardResult.pass();
    }

    /**
     * Returns rule name.
     *
     * @return {@link Builder#TLS_GUARD_NAME}
     */
    public String name() {
        return Builder.TLS_GUARD_NAME;
    }

    /**
     * Returns a validated address scheme.
     *
     * @param address non-null address supplying the scheme
     * @return validated lower-case address scheme
     * @throws ProtocolException if the scheme is invalid
     */
    private static String scheme(final Address address) {
        final String scheme = address.scheme();
        Assert.isTrue(UrlKit.isScheme(scheme), () -> new ProtocolException("Invalid TLS address scheme"));
        return scheme.toLowerCase(Locale.ROOT);
    }

    /**
     * Returns whether a cipher suite is weak.
     *
     * @param cipher non-blank, single-line cipher-suite name
     * @return {@code true} when the upper-case name contains NULL, anonymous, export, RC4, MD5, or DES weakness markers
     * @throws ProtocolException if {@code cipher} is blank or multi-line
     */
    private static boolean weakCipher(final String cipher) {
        Assert.isTrue(
                !StringKit.isBlank(cipher) && !StringKit.containsAny(cipher, Symbol.C_CR, Symbol.C_LF),
                () -> new ProtocolException("Invalid TLS cipher suite"));
        final String upper = cipher.toUpperCase(Locale.ROOT);
        return upper.contains("_NULL_") || upper.contains("_ANON_") || upper.contains("_EXPORT_")
                || upper.contains("_RC4_") || upper.contains("_MD5") || upper.contains("_DES_");
    }

}
