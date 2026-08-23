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
 * Represents an RFC 4511 {@code SearchResultEntry} protocol operation with application tag 4.
 *
 * @param objectName distinguished name of the returned entry
 * @param attributes partial attributes in their protocol order
 * @author Kimi Liu
 */
public record SearchResultEntry(DistinguishedName objectName, List<LdapAttribute> attributes)
        implements LdapMessage.ProtocolOp {

    /**
     * Creates an immutable search entry; an empty attribute list remains valid for types-only or selective searches.
     *
     * @param objectName entry distinguished name
     * @param attributes returned partial attributes
     * @throws IllegalArgumentException if a component or attribute is {@code null}
     */
    public SearchResultEntry {
        Assert.notNull(objectName, "LDAP search entry name must not be null");
        Assert.notNull(attributes, "LDAP search entry attributes must not be null");
        attributes.forEach(attribute -> Assert.notNull(attribute, "LDAP search entry attribute must not be null"));
        attributes = List.copyOf(attributes);
    }

}
