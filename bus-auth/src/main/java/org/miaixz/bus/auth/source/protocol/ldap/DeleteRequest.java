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

import org.miaixz.bus.core.lang.Assert;

/**
 * Represents the RFC 4511 ASN.1 {@code DelRequest} protocol operation with application tag 10.
 * <p>
 * The Java name spells out “Delete” while retaining the wire operation's primitive LDAPDN content and exact tag.
 * </p>
 *
 * @param entry distinguished name of the entry to delete
 * @author Kimi Liu
 */
public record DeleteRequest(DistinguishedName entry) implements LdapMessage.ProtocolOp {

    /**
     * Creates a delete request.
     *
     * @param entry target entry DN
     * @throws IllegalArgumentException if {@code entry} is {@code null}
     */
    public DeleteRequest {
        Assert.notNull(entry, "LDAP delete entry must not be null");
    }

}
