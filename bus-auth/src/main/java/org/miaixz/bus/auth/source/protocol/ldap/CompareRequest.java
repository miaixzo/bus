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
 * Represents the RFC 4511 {@code CompareRequest} protocol operation with application tag 14.
 *
 * @param entry distinguished name of the entry to compare
 * @param ava   attribute-value assertion evaluated by the directory
 * @author Kimi Liu
 */
public record CompareRequest(DistinguishedName entry, SearchRequest.AttributeValueAssertion ava)
        implements LdapMessage.ProtocolOp {

    /**
     * Creates a complete compare request.
     *
     * @param entry target entry DN
     * @param ava   attribute-value assertion
     * @throws IllegalArgumentException if a component is {@code null}
     */
    public CompareRequest {
        Assert.notNull(entry, "LDAP compare entry must not be null");
        Assert.notNull(ava, "LDAP compare assertion must not be null");
    }

}
