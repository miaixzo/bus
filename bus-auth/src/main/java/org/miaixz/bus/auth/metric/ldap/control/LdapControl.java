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
package org.miaixz.bus.auth.metric.ldap.control;

import java.util.regex.Pattern;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Immutable binary-safe LDAP control. The object identifier is a bounded numeric dotted value, criticality is retained
 * exactly, and the optional encoded control value is copied on construction and access.
 *
 * @param oid      registered control object identifier
 * @param critical whether an unsupported control must reject the operation
 * @param value    optional copied BER control value
 * @author Kimi Liu
 */
public record LdapControl(String oid, boolean critical, byte[] value) {

    /**
     * Maximum control object identifier length.
     */
    private static final int MAXIMUM_OID_LENGTH = Normal._128;

    /**
     * Maximum encoded control value length.
     */
    private static final int MAXIMUM_VALUE_LENGTH = 64 * (int) Normal.KIBI;

    /**
     * Numeric dotted object-identifier grammar.
     */
    private static final Pattern OID = Pattern.compile("[0-9]+(?:\\.[0-9]+)+");

    /**
     * Validates and snapshots one control.
     *
     * @param oid      control object identifier
     * @param critical criticality flag
     * @param value    encoded control value
     */
    public LdapControl {
        oid = Assert.notBlank(oid, () -> new ValidateException("LDAP control OID must not be blank"));
        Assert.isTrue(
                oid.length() <= MAXIMUM_OID_LENGTH && OID.matcher(oid).matches(),
                () -> new ValidateException("LDAP control OID is invalid"));
        value = value == null ? null : value.clone();
        Assert.isTrue(
                value == null || value.length <= MAXIMUM_VALUE_LENGTH,
                () -> new ValidateException("LDAP control value exceeds the maximum length"));
    }

    /**
     * Returns an independent encoded-value copy.
     *
     * @return copied encoded value or {@code null}
     */
    @Override
    public byte[] value() {
        return value == null ? null : value.clone();
    }

}
