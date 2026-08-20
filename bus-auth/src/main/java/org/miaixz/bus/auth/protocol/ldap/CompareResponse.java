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

import org.miaixz.bus.core.lang.Assert;

/**
 * Represents the RFC 4511 {@code CompareResponse} operation with application tag 15.
 *
 * @param result LDAP result whose code distinguishes compareTrue, compareFalse, and operation failures
 * @author Kimi Liu
 */
public record CompareResponse(LdapResult result) implements LdapMessage.ProtocolOp {

    /**
     * Creates a compare response without converting compare outcomes into Java booleans.
     *
     * @param result standard LDAP result
     * @throws IllegalArgumentException if {@code result} is {@code null}
     */
    public CompareResponse {
        Assert.notNull(result, "LDAP compare result must not be null");
    }

}
