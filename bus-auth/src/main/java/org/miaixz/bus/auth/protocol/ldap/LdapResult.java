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
package org.miaixz.bus.auth.protocol.ldap;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;

/**
 * Represents the common RFC 4511 {@code LDAPResult} sequence embedded by LDAP response operations.
 *
 * @param resultCode        extensible LDAP result code received or generated on the wire
 * @param matchedDn         deepest matched directory name, or the empty LDAP DN when no name is applicable
 * @param diagnosticMessage human-readable diagnostic text that is not a stable programmatic error identifier
 * @param referral          optional non-empty referral URI sequence
 * @author Kimi Liu
 */
public record LdapResult(LdapResultCode resultCode, DistinguishedName matchedDn, String diagnosticMessage,
        Optional<Referral> referral) {

    /**
     * Creates an immutable LDAP result while retaining all standard result fields.
     *
     * @param resultCode        LDAP result code
     * @param matchedDn         matched directory name
     * @param diagnosticMessage diagnostic message, which may be empty
     * @param referral          optional referral sequence
     * @throws IllegalArgumentException if any component is {@code null}
     */
    public LdapResult {
        Assert.notNull(resultCode, "LDAP result code must not be null");
        Assert.notNull(matchedDn, "LDAP matched DN must not be null");
        Assert.notNull(diagnosticMessage, "LDAP diagnostic message must not be null");
        Assert.notNull(referral, "LDAP referral option must not be null");
    }

    /**
     * Represents the context-specific RFC 4511 {@code Referral} sequence.
     *
     * @param uris one or more absolute referral URIs in original wire order
     * @author Kimi Liu
     */
    public record Referral(List<String> uris) {

        /**
         * Creates an immutable, non-empty referral sequence.
         *
         * @param uris absolute referral URIs
         * @throws IllegalArgumentException if the list is null or empty, or a value is not an absolute URI
         */
        public Referral {
            Assert.notNull(uris, "LDAP referral URIs must not be null");
            Assert.notEmpty(uris, "LDAP referral URIs must not be empty");
            uris.forEach(Referral::requireAbsoluteUri);
            uris = List.copyOf(uris);
        }

        /**
         * Validates one RFC 3986 absolute referral URI without changing its lexical form.
         *
         * @param value referral URI lexical value
         * @throws IllegalArgumentException if the value is null, blank, malformed, or relative
         */
        static void requireAbsoluteUri(final String value) {
            Assert.notBlank(value, "LDAP referral URI must not be blank");
            try {
                Assert.isTrue(new URI(value).isAbsolute(), "LDAP referral URI must be absolute: {}", value);
            } catch (final URISyntaxException cause) {
                throw new IllegalArgumentException("LDAP referral URI is malformed", cause);
            }
        }

    }

}
