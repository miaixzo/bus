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
 * Represents an RFC 4511 {@code SearchResultReference} protocol operation with application tag 19.
 *
 * @param uris one or more absolute reference URIs in wire order
 * @author Kimi Liu
 */
public record SearchResultReference(List<String> uris) implements LdapMessage.ProtocolOp {

    /**
     * Creates an immutable non-empty search-reference URI sequence.
     *
     * @param uris absolute reference URIs
     * @throws IllegalArgumentException if the list is null or empty, or a URI is malformed or relative
     */
    public SearchResultReference {
        Assert.notNull(uris, "LDAP search-reference URIs must not be null");
        Assert.notEmpty(uris, "LDAP search-reference URIs must not be empty");
        uris.forEach(LdapResult.Referral::requireAbsoluteUri);
        uris = List.copyOf(uris);
    }

}
