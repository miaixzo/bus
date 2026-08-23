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
 * Represents the extensible RFC 4511 {@code LDAPResult.resultCode} ENUMERATED value.
 * <p>
 * The constants cover every result code registered by the RFC 4511 ASN.1 module. Decoders retain other non-negative
 * values so that an extension is not confused with a malformed BER encoding; operation policy decides whether an
 * unknown value can be acted upon.
 * </p>
 *
 * @param value non-negative ASN.1 enumerated value
 * @author Kimi Liu
 */
public record LdapResultCode(int value) {

    /**
     * Operation completed successfully.
     */
    public static final LdapResultCode SUCCESS = new LdapResultCode(0);

    /**
     * Server encountered an internal sequencing or operation error.
     */
    public static final LdapResultCode OPERATIONS_ERROR = new LdapResultCode(1);

    /**
     * Peer violated the LDAP protocol.
     */
    public static final LdapResultCode PROTOCOL_ERROR = new LdapResultCode(2);

    /**
     * Operation exceeded its server time limit.
     */
    public static final LdapResultCode TIME_LIMIT_EXCEEDED = new LdapResultCode(3);

    /**
     * Search exceeded its server size limit.
     */
    public static final LdapResultCode SIZE_LIMIT_EXCEEDED = new LdapResultCode(4);

    /**
     * Compare assertion evaluated to false.
     */
    public static final LdapResultCode COMPARE_FALSE = new LdapResultCode(5);

    /**
     * Compare assertion evaluated to true.
     */
    public static final LdapResultCode COMPARE_TRUE = new LdapResultCode(6);

    /**
     * Requested authentication method is unsupported.
     */
    public static final LdapResultCode AUTH_METHOD_NOT_SUPPORTED = new LdapResultCode(7);

    /**
     * Operation requires a stronger authentication mechanism.
     */
    public static final LdapResultCode STRONGER_AUTH_REQUIRED = new LdapResultCode(8);

    /**
     * Result directs the client to one or more referral URIs.
     */
    public static final LdapResultCode REFERRAL = new LdapResultCode(10);

    /**
     * Operation exceeded an administrative limit.
     */
    public static final LdapResultCode ADMIN_LIMIT_EXCEEDED = new LdapResultCode(11);

    /**
     * Server does not support a requested critical control.
     */
    public static final LdapResultCode UNAVAILABLE_CRITICAL_EXTENSION = new LdapResultCode(12);

    /**
     * Operation requires protected confidentiality.
     */
    public static final LdapResultCode CONFIDENTIALITY_REQUIRED = new LdapResultCode(13);

    /**
     * SASL bind exchange requires another client step.
     */
    public static final LdapResultCode SASL_BIND_IN_PROGRESS = new LdapResultCode(14);

    /**
     * Referenced attribute does not exist.
     */
    public static final LdapResultCode NO_SUCH_ATTRIBUTE = new LdapResultCode(16);

    /**
     * Referenced attribute type is undefined.
     */
    public static final LdapResultCode UNDEFINED_ATTRIBUTE_TYPE = new LdapResultCode(17);

    /**
     * Matching rule is inappropriate for the supplied attribute.
     */
    public static final LdapResultCode INAPPROPRIATE_MATCHING = new LdapResultCode(18);

    /**
     * Attribute or entry violates a directory constraint.
     */
    public static final LdapResultCode CONSTRAINT_VIOLATION = new LdapResultCode(19);

    /**
     * Attribute already contains the supplied value.
     */
    public static final LdapResultCode ATTRIBUTE_OR_VALUE_EXISTS = new LdapResultCode(20);

    /**
     * Attribute value does not satisfy its syntax.
     */
    public static final LdapResultCode INVALID_ATTRIBUTE_SYNTAX = new LdapResultCode(21);

    /**
     * Target directory object does not exist.
     */
    public static final LdapResultCode NO_SUCH_OBJECT = new LdapResultCode(32);

    /**
     * Alias cannot be processed as required.
     */
    public static final LdapResultCode ALIAS_PROBLEM = new LdapResultCode(33);

    /**
     * Distinguished name has invalid syntax.
     */
    public static final LdapResultCode INVALID_DN_SYNTAX = new LdapResultCode(34);

    /**
     * Alias dereferencing failed.
     */
    public static final LdapResultCode ALIAS_DEREFERENCING_PROBLEM = new LdapResultCode(36);

    /**
     * Authentication is inappropriate for the requested operation.
     */
    public static final LdapResultCode INAPPROPRIATE_AUTHENTICATION = new LdapResultCode(48);

    /**
     * Supplied credentials are invalid.
     */
    public static final LdapResultCode INVALID_CREDENTIALS = new LdapResultCode(49);

    /**
     * Authenticated identity has insufficient access rights.
     */
    public static final LdapResultCode INSUFFICIENT_ACCESS_RIGHTS = new LdapResultCode(50);

    /**
     * Server is currently too busy to execute the operation.
     */
    public static final LdapResultCode BUSY = new LdapResultCode(51);

    /**
     * Required directory service is unavailable.
     */
    public static final LdapResultCode UNAVAILABLE = new LdapResultCode(52);

    /**
     * Server is unwilling to perform the requested operation.
     */
    public static final LdapResultCode UNWILLING_TO_PERFORM = new LdapResultCode(53);

    /**
     * Referral or alias processing detected a loop.
     */
    public static final LdapResultCode LOOP_DETECT = new LdapResultCode(54);

    /**
     * Entry naming rules would be violated.
     */
    public static final LdapResultCode NAMING_VIOLATION = new LdapResultCode(64);

    /**
     * Entry would violate object class rules.
     */
    public static final LdapResultCode OBJECT_CLASS_VIOLATION = new LdapResultCode(65);

    /**
     * Requested operation is not allowed on a non-leaf entry.
     */
    public static final LdapResultCode NOT_ALLOWED_ON_NON_LEAF = new LdapResultCode(66);

    /**
     * Requested modification is not allowed on the relative distinguished name.
     */
    public static final LdapResultCode NOT_ALLOWED_ON_RDN = new LdapResultCode(67);

    /**
     * Target directory entry already exists.
     */
    public static final LdapResultCode ENTRY_ALREADY_EXISTS = new LdapResultCode(68);

    /**
     * Requested object class modification is prohibited.
     */
    public static final LdapResultCode OBJECT_CLASS_MODS_PROHIBITED = new LdapResultCode(69);

    /**
     * Operation would affect multiple directory system agents.
     */
    public static final LdapResultCode AFFECTS_MULTIPLE_DSAS = new LdapResultCode(71);

    /**
     * Operation failed for another protocol-defined reason.
     */
    public static final LdapResultCode OTHER = new LdapResultCode(80);

    /**
     * Creates a result-code value without restricting future registered extensions.
     *
     * @param value non-negative result-code number
     * @throws IllegalArgumentException if {@code value} is negative
     */
    public LdapResultCode {
        Assert.isTrue(value >= 0, "LDAP result code must not be negative");
    }

}
