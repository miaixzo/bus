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
package org.miaixz.bus.auth.metric.shared.security;

import java.util.Locale;
import java.util.Set;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.crypto.builtin.digest.Digester;

/**
 * Derives the fixed tenant-isolated replay key format {@code auth:9:{tenantHash}:{protocol}:{kind}:{identifierHash}}.
 * Tenant and identifier values are hashed independently with SHA-256 and never appear in the returned key. Protocol and
 * kind are normalized to lowercase ASCII tokens before joining the unambiguous fixed fields.
 * <p>
 * <strong>Bus dependencies:</strong> {@link Builder#sha256()} and {@link Digester} provide the security digest,
 * {@link Charset#UTF_8} fixes text encoding, and {@link Symbol} and {@link Normal} provide framework punctuation and
 * empty-text constants.
 *
 * @author Kimi Liu
 */
public final class ReplayKey {

    /**
     * Exact protocol names permitted in shared replay keys.
     */
    private static final Set<String> PROTOCOLS = Set
            .of("jwt", "oauth2", "oidc", "ldap", "scim", "proxy", "radius", "ssf");

    /**
     * Fixed state-key namespace and format version.
     */
    private static final String PREFIX = "auth:9:";

    /**
     * Prevents instantiation of the replay-key utility.
     */
    private ReplayKey() {
        // No initialization required.
    }

    /**
     * Derives one deterministic tenant and identifier protected state key.
     *
     * @param tenant     tenant identifier preserved exactly for hashing
     * @param protocol   supported authentication protocol name
     * @param kind       caller-defined lowercase-safe state kind
     * @param identifier opaque replay identifier preserved exactly for hashing
     * @return fixed lowercase replay-state key
     */
    public static String derive(
            final String tenant,
            final String protocol,
            final String kind,
            final String identifier) {
        final String tenantValue = sensitive(tenant, "Tenant identifier");
        final String identifierValue = sensitive(identifier, "Replay identifier");
        final String protocolValue = token(protocol, "Protocol");
        if (!PROTOCOLS.contains(protocolValue)) {
            throw new ValidateException("Protocol is not supported for replay state");
        }
        final String kindValue = token(kind, "State kind");
        final Digester digester = Builder.sha256();
        final String tenantHash = digester.digestHex(tenantValue, Charset.UTF_8).toLowerCase(Locale.ROOT);
        final String identifierHash = digester.digestHex(identifierValue, Charset.UTF_8).toLowerCase(Locale.ROOT);
        return PREFIX + Symbol.C_BRACE_LEFT + tenantHash + Symbol.C_BRACE_RIGHT + Symbol.C_COLON + protocolValue
                + Symbol.C_COLON + kindValue + Symbol.C_COLON + identifierHash;
    }

    /**
     * Validates sensitive input without normalizing or returning it in an error.
     *
     * @param value sensitive source value
     * @param label safe diagnostic label
     * @return unchanged value
     */
    private static String sensitive(final String value, final String label) {
        if (value == null || value.isBlank()) {
            throw new ValidateException(label + " must not be blank");
        }
        return value;
    }

    /**
     * Normalizes and validates one public lowercase ASCII key token.
     *
     * @param value source token
     * @param label safe diagnostic label
     * @return normalized token
     */
    private static String token(final String value, final String label) {
        if (value == null || value.isBlank()) {
            throw new ValidateException(label + " must not be blank");
        }
        final String normalized = value.trim().toLowerCase(Locale.ROOT);
        for (int index = Normal._0; index < normalized.length(); index++) {
            final char current = normalized.charAt(index);
            final boolean letter = current >= 'a' && current <= 'z';
            final boolean digit = current >= Symbol.C_ZERO && current <= '9';
            if (!letter && !digit && current != Symbol.C_MINUS && current != '_') {
                throw new ValidateException(label + " must be an ASCII key token");
            }
        }
        return normalized;
    }

}
