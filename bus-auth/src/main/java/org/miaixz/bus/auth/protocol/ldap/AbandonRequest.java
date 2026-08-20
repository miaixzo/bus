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
 * Represents the RFC 4511 {@code AbandonRequest} operation with application tag 16.
 * <p>
 * This identifier names an earlier in-progress operation and is distinct from the identifier on the enclosing abandon
 * {@link LdapMessage}. The operation has no protocol response.
 * </p>
 *
 * @param messageId non-zero identifier of the operation requested for abandonment
 * @author Kimi Liu
 */
public record AbandonRequest(int messageId) implements LdapMessage.ProtocolOp {

    /**
     * Creates an abandon request for one previously issued operation.
     *
     * @param messageId target operation identifier
     * @throws IllegalArgumentException if the identifier is not positive
     */
    public AbandonRequest {
        Assert.isTrue(messageId > 0, "LDAP abandon target message identifier must be positive");
    }

}
