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
package org.miaixz.bus.auth.protocol.ldap.message;

import java.util.LinkedHashSet;
import java.util.List;

import org.miaixz.bus.auth.protocol.ldap.control.LdapControl;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Immutable LDAP message envelope combining one positive message identifier, one closed protocol operation, and a
 * bounded control list. Duplicate control object identifiers are rejected before encoding or dispatch.
 *
 * @param messageId positive request or response correlation identifier
 * @param operation closed LDAP protocol operation
 * @param controls  bounded immutable controls
 * @author Kimi Liu
 */
public record LdapMessage(int messageId, LdapProtocolOp operation, List<LdapControl> controls) {

    /**
     * Maximum controls carried by one LDAP message.
     */
    private static final int MAXIMUM_CONTROLS = Normal._128;

    /**
     * Validates and snapshots one LDAP message.
     *
     * @param messageId message identifier
     * @param operation protocol operation
     * @param controls  message controls
     */
    public LdapMessage {
        Assert.isTrue(messageId > Normal._0, () -> new ValidateException("LDAP message identifier must be positive"));
        operation = Assert.notNull(operation, () -> new ValidateException("LDAP protocol operation must not be null"));
        controls = List.copyOf(
                Assert.notNull(controls, () -> new ValidateException("LDAP message controls must not be null")));
        Assert.isTrue(
                controls.size() <= MAXIMUM_CONTROLS,
                () -> new ValidateException("LDAP control count exceeds the maximum"));
        final LinkedHashSet<String> identifiers = new LinkedHashSet<>();
        for (final LdapControl control : controls) {
            Assert.notNull(control, () -> new ValidateException("LDAP message control must not be null"));
            Assert.isTrue(
                    identifiers.add(control.oid()),
                    () -> new ValidateException("LDAP message contains a duplicate control"));
        }
    }

    /**
     * Creates a message without controls.
     *
     * @param messageId message identifier
     * @param operation protocol operation
     * @return immutable LDAP message
     */
    public static LdapMessage of(final int messageId, final LdapProtocolOp operation) {
        return new LdapMessage(messageId, operation, List.of());
    }

}
