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
 * Represents the RFC 4511 {@code BindRequest} protocol operation with application tag 0.
 * <p>
 * The ASN.1 range is retained so a server can return {@code protocolError} for an unsupported version. The LDAPv3
 * service execution path accepts version 3 and does not silently reinterpret another version.
 * </p>
 *
 * @param version        requested LDAP protocol version in the ASN.1 range 1 through 127
 * @param name           authentication identity expressed as an LDAP distinguished name
 * @param authentication simple or SASL authentication choice
 * @author Kimi Liu
 */
public record BindRequest(int version, DistinguishedName name, AuthenticationChoice authentication)
        implements LdapMessage.ProtocolOp {

    /**
     * Creates a bind request matching the complete standard sequence.
     *
     * @param version        requested protocol version
     * @param name           bind distinguished name
     * @param authentication authentication choice
     * @throws IllegalArgumentException if the version is outside 1 through 127 or a reference is {@code null}
     */
    public BindRequest {
        Assert.isTrue(version >= 1 && version <= 127, "LDAP bind version must be between 1 and 127");
        Assert.notNull(name, "LDAP bind name must not be null");
        Assert.notNull(authentication, "LDAP bind authentication must not be null");
    }

}
