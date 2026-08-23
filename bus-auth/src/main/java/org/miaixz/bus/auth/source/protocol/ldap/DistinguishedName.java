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
package org.miaixz.bus.auth.source.protocol.ldap;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;

import org.miaixz.bus.core.lang.Assert;

/**
 * Preserves one RFC 4514 distinguished-name lexical value used by the RFC 4511 {@code LDAPDN} type.
 * <p>
 * The empty value is valid and identifies the root DSE. Validation does not normalize case, escaping, or attribute
 * order, so codecs can reproduce the accepted lexical value and directory matching remains the store's concern.
 * </p>
 *
 * @param value RFC 4514 distinguished name, including the valid empty root DN
 * @author Kimi Liu
 */
public record DistinguishedName(String value) {

    /**
     * Creates a distinguished name after validating its complete RFC 4514-compatible syntax.
     *
     * @param value distinguished-name lexical value
     * @throws IllegalArgumentException if the value is {@code null} or syntactically invalid
     */
    public DistinguishedName {
        Assert.notNull(value, "LDAP distinguished name must not be null");
        try {
            new LdapName(value);
        } catch (final InvalidNameException cause) {
            throw new IllegalArgumentException("LDAP distinguished name is malformed", cause);
        }
    }

}
