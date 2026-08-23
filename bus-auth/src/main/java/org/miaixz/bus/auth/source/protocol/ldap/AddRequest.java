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

import java.util.List;

import org.miaixz.bus.core.lang.Assert;

/**
 * Represents the RFC 4511 {@code AddRequest} protocol operation with application tag 8.
 *
 * @param entry      distinguished name of the entry to create
 * @param attributes ordered attribute list whose members each contain at least one value
 * @author Kimi Liu
 */
public record AddRequest(DistinguishedName entry, List<LdapAttribute> attributes) implements LdapMessage.ProtocolOp {

    /**
     * Creates an immutable add request and applies the stricter ASN.1 {@code Attribute} value cardinality.
     *
     * @param entry      new entry DN
     * @param attributes entry attributes, with an empty outer sequence retained when decoded
     * @throws IllegalArgumentException if a component is null or an attribute contains no values
     */
    public AddRequest {
        Assert.notNull(entry, "LDAP add entry must not be null");
        Assert.notNull(attributes, "LDAP add attributes must not be null");
        for (LdapAttribute attribute : attributes) {
            Assert.notNull(attribute, "LDAP add attribute must not be null");
            Assert.notEmpty(attribute.values(), "LDAP add attribute values must not be empty");
        }
        attributes = List.copyOf(attributes);
    }

}
