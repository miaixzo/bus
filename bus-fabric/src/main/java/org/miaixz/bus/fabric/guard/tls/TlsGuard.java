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
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.Address;
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
     * Rule name.
     */
    private static final String NAME = "tls";

    /**
     * Creates a TLS guard.
     */
    private TlsGuard() {
        // No initialization required.
    }

    /**
     * Returns the secure TLS guard.
     *
     * @return TLS guard
     */
    public static TlsGuard requireSecure() {
        return Instances.get(TlsGuard.class.getName() + ".secure", TlsGuard::new);
    }

    /**
     * Checks whether an address requires TLS.
     *
     * @param address address
     * @return guard result
     */
    public GuardResult check(final Address address) {
        if (address == null) {
            throw new ValidateException("Address must not be null");
        }
        final String scheme = scheme(address);
        if (address.secure()) {
            return GuardResult.pass();
        }
        return GuardResult.reject("tls required for scheme: " + scheme);
    }

    /**
     * Checks TLS settings for weak configuration.
     *
     * @param settings TLS settings
     * @return guard result
     */
    public GuardResult check(final TlsSettings settings) {
        if (settings == null) {
            throw new ValidateException("TLS settings must not be null");
        }
        for (final String version : settings.versions()) {
            if (Protocol.TLSv1.name.equals(version) || Protocol.TLSv1_1.name.equals(version)) {
                return GuardResult.reject("weak tls version: " + version);
            }
        }
        if (!settings.verifyHostname()) {
            return GuardResult.reject("hostname verification required");
        }
        for (final String cipher : settings.ciphers()) {
            if (weakCipher(cipher)) {
                return GuardResult.reject("weak tls cipher: " + cipher);
            }
        }
        return GuardResult.pass();
    }

    /**
     * Returns rule name.
     *
     * @return rule name
     */
    public String name() {
        return NAME;
    }

    /**
     * Returns a validated address scheme.
     *
     * @param address address
     * @return scheme
     */
    private static String scheme(final Address address) {
        final String scheme = address.scheme();
        if (StringKit.isBlank(scheme) || StringKit.containsAny(scheme, Symbol.C_CR, Symbol.C_LF)) {
            throw new ProtocolException("Invalid TLS address scheme");
        }
        return scheme.toLowerCase(Locale.ROOT);
    }

    /**
     * Returns whether a cipher suite is weak.
     *
     * @param cipher cipher suite
     * @return true when weak
     */
    private static boolean weakCipher(final String cipher) {
        if (StringKit.isBlank(cipher) || StringKit.containsAny(cipher, Symbol.C_CR, Symbol.C_LF)) {
            throw new ProtocolException("Invalid TLS cipher suite");
        }
        final String upper = cipher.toUpperCase(Locale.ROOT);
        return upper.contains("_NULL_") || upper.contains("_ANON_") || upper.contains("_EXPORT_")
                || upper.contains("_RC4_") || upper.contains("_MD5") || upper.contains("_DES_");
    }

}
