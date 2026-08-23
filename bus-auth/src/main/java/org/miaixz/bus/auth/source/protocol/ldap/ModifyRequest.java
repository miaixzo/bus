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
 * Represents the RFC 4511 {@code ModifyRequest} protocol operation with application tag 6.
 *
 * @param object  distinguished name of the entry to modify
 * @param changes ordered changes executed as one atomic directory operation
 * @author Kimi Liu
 */
public record ModifyRequest(DistinguishedName object, List<Change> changes) implements LdapMessage.ProtocolOp {

    /**
     * Creates an immutable modify request while preserving the required execution order.
     *
     * @param object  target entry DN
     * @param changes ordered modification sequence, which ASN.1 permits to be empty
     * @throws IllegalArgumentException if the target, list, or a change is {@code null}
     */
    public ModifyRequest {
        Assert.notNull(object, "LDAP modify object must not be null");
        Assert.notNull(changes, "LDAP modify changes must not be null");
        changes.forEach(change -> Assert.notNull(change, "LDAP modify change must not be null"));
        changes = List.copyOf(changes);
    }

    /**
     * Represents one RFC 4511 modify {@code change} sequence.
     *
     * @param operation    extensible modification operation
     * @param modification partial attribute carrying the affected description and values
     * @author Kimi Liu
     */
    public record Change(Operation operation, LdapAttribute modification) {

        /**
         * Creates one modification and enforces the add-value cardinality rule.
         *
         * @param operation    modification operation
         * @param modification partial attribute
         * @throws IllegalArgumentException if a component is null or add has no values
         */
        public Change {
            Assert.notNull(operation, "LDAP modification operation must not be null");
            Assert.notNull(modification, "LDAP modification attribute must not be null");
            if (Operation.ADD.equals(operation)) {
                Assert.notEmpty(modification.values(), "LDAP add modification values must not be empty");
            }
        }

    }

    /**
     * Represents the extensible RFC 4511 modification-operation ENUMERATED value.
     *
     * @param value non-negative enumerated value
     * @author Kimi Liu
     */
    public record Operation(int value) {

        /**
         * Adds the supplied values, creating the attribute when necessary.
         */
        public static final Operation ADD = new Operation(0);

        /**
         * Deletes the supplied values or the complete attribute when no values are supplied.
         */
        public static final Operation DELETE = new Operation(1);

        /**
         * Replaces the complete attribute, or deletes it when no values are supplied.
         */
        public static final Operation REPLACE = new Operation(2);

        /**
         * Creates an extensible modification-operation value.
         *
         * @param value non-negative ASN.1 enumerated value
         * @throws IllegalArgumentException if {@code value} is negative
         */
        public Operation {
            Assert.isTrue(value >= 0, "LDAP modification operation must not be negative");
        }

    }

}
