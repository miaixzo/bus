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

import javax.naming.InvalidNameException;
import javax.naming.ldap.Rdn;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;

/**
 * Represents the RFC 4511 {@code ModifyDNRequest} protocol operation with application tag 12.
 *
 * @param entry        distinguished name of the entry to rename or move
 * @param newRdn       replacement relative distinguished name
 * @param deleteOldRdn whether old RDN attribute values are removed from the entry
 * @param newSuperior  optional distinguished name of a new immediate superior
 * @author Kimi Liu
 */
public record ModifyDNRequest(DistinguishedName entry, RelativeDistinguishedName newRdn, boolean deleteOldRdn,
        Optional<DistinguishedName> newSuperior) implements LdapMessage.ProtocolOp {

    /**
     * Creates a complete modify-DN request.
     *
     * @param entry        target entry DN
     * @param newRdn       replacement RDN
     * @param deleteOldRdn old-RDN deletion flag
     * @param newSuperior  optional new parent DN
     * @throws IllegalArgumentException if a component is {@code null}
     */
    public ModifyDNRequest {
        Assert.notNull(entry, "LDAP modify-DN entry must not be null");
        Assert.notNull(newRdn, "LDAP modify-DN new RDN must not be null");
        Assert.notNull(newSuperior, "LDAP modify-DN new superior option must not be null");
    }

    /**
     * Preserves one RFC 4514 name-component lexical value used by RFC 4511 {@code RelativeLDAPDN}.
     *
     * @param value non-empty relative distinguished name
     * @author Kimi Liu
     */
    public record RelativeDistinguishedName(String value) {

        /**
         * Creates a relative distinguished name without normalizing its lexical representation.
         *
         * @param value RFC 4514 name component
         * @throws IllegalArgumentException if the value is null, empty, or malformed
         */
        public RelativeDistinguishedName {
            Assert.notBlank(value, "LDAP relative distinguished name must not be blank");
            try {
                new Rdn(value);
            } catch (final InvalidNameException cause) {
                throw new IllegalArgumentException("LDAP relative distinguished name is malformed", cause);
            }
        }

    }

}
