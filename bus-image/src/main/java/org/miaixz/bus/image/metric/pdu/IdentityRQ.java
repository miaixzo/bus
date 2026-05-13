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
package org.miaixz.bus.image.metric.pdu;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.image.Builder;

/**
 * Represents the IdentityRQ type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class IdentityRQ {

    /**
     * The username value.
     */
    public static final int USERNAME = 1;

    /**
     * The username passcode value.
     */
    public static final int USERNAME_PASSCODE = 2;

    /**
     * The kerberos value.
     */
    public static final int KERBEROS = 3;

    /**
     * The saml value.
     */
    public static final int SAML = 4;

    /**
     * The jwt value.
     */
    public static final int JWT = 5;

    /**
     * The types value.
     */
    private static final String[] TYPES = { "0", "1 - Username", "2 - Username and passcode",
            "3 - Kerberos Service ticket", "4 - SAML Assertion", "5 - JSON Web Token (JWT)" };

    /**
     * The type value.
     */
    private final int type;

    /**
     * The rsp req value.
     */
    private final boolean rspReq;

    /**
     * The primary field value.
     */
    private final byte[] primaryField;

    /**
     * The secondary field value.
     */
    private final byte[] secondaryField;

    /**
     * Creates a new instance.
     *
     * @param type           the type.
     * @param rspReq         the rsp req.
     * @param primaryField   the primary field.
     * @param secondaryField the secondary field.
     */
    public IdentityRQ(int type, boolean rspReq, byte[] primaryField, byte[] secondaryField) {
        this.type = type;
        this.rspReq = rspReq;
        this.primaryField = primaryField.clone();
        this.secondaryField = secondaryField != null ? secondaryField.clone() : new byte[0];
    }

    /**
     * Creates a new instance.
     *
     * @param type         the type.
     * @param rspReq       the rsp req.
     * @param primaryField the primary field.
     */
    public IdentityRQ(int type, boolean rspReq, byte[] primaryField) {
        this(type, rspReq, primaryField, null);
    }

    /**
     * Executes the username operation.
     *
     * @param username the username.
     * @param rspReq   the rsp req.
     * @return the operation result.
     */
    public static IdentityRQ username(String username, boolean rspReq) {
        return new IdentityRQ(USERNAME, rspReq, toBytes(username));
    }

    /**
     * Executes the username passcode operation.
     *
     * @param username the username.
     * @param passcode the passcode.
     * @param rspReq   the rsp req.
     * @return the operation result.
     */
    public static IdentityRQ usernamePasscode(String username, char[] passcode, boolean rspReq) {
        return new IdentityRQ(USERNAME_PASSCODE, rspReq, toBytes(username), toBytes(new String(passcode)));
    }

    /**
     * Executes the kerberos operation.
     *
     * @param ticket the ticket.
     * @param rspReq the rsp req.
     * @return the operation result.
     */
    public static IdentityRQ kerberos(byte[] ticket, boolean rspReq) {
        return new IdentityRQ(KERBEROS, rspReq, ticket);
    }

    /**
     * Executes the saml operation.
     *
     * @param assertion the assertion.
     * @param rspReq    the rsp req.
     * @return the operation result.
     */
    public static IdentityRQ saml(String assertion, boolean rspReq) {
        return new IdentityRQ(SAML, rspReq, toBytes(assertion));
    }

    /**
     * Executes the jwt operation.
     *
     * @param token  the token.
     * @param rspReq the rsp req.
     * @return the operation result.
     */
    public static IdentityRQ jwt(String token, boolean rspReq) {
        return new IdentityRQ(JWT, rspReq, toBytes(token));
    }

    /**
     * Executes the type as string operation.
     *
     * @param type the type.
     * @return the operation result.
     */
    private static String typeAsString(int type) {
        try {
            return TYPES[type];
        } catch (IndexOutOfBoundsException e) {
            return Integer.toString(type);
        }
    }

    /**
     * Converts this value to bytes.
     *
     * @param s the s.
     * @return the operation result.
     */
    private static byte[] toBytes(String s) {
        return s.getBytes(Charset.UTF_8);
    }

    /**
     * Returns the string representation.
     *
     * @param b the b.
     * @return the string representation.
     */
    private static String toString(byte[] b) {
        return new String(b, Charset.UTF_8);
    }

    /**
     * Gets the type.
     *
     * @return the type.
     */
    public final int getType() {
        return type;
    }

    /**
     * Determines whether positive response requested.
     *
     * @return true if the condition is met; otherwise false.
     */
    public final boolean isPositiveResponseRequested() {
        return rspReq;
    }

    /**
     * Gets the primary field.
     *
     * @return the primary field.
     */
    public final byte[] getPrimaryField() {
        return primaryField.clone();
    }

    /**
     * Gets the secondary field.
     *
     * @return the secondary field.
     */
    public final byte[] getSecondaryField() {
        return secondaryField.clone();
    }

    /**
     * Gets the username.
     *
     * @return the username.
     */
    public final String getUsername() {
        return toString(primaryField);
    }

    /**
     * Gets the passcode.
     *
     * @return the passcode.
     */
    public final char[] getPasscode() {
        return toString(secondaryField).toCharArray();
    }

    /**
     * Executes the length operation.
     *
     * @return the operation result.
     */
    public int length() {
        return 6 + primaryField.length + secondaryField.length;
    }

    /**
     * Returns the string representation.
     *
     * @return the string representation.
     */
    @Override
    public String toString() {
        return promptTo(new StringBuilder()).toString();
    }

    /**
     * Executes the prompt to operation.
     *
     * @param sb the sb.
     * @return the operation result.
     */
    StringBuilder promptTo(StringBuilder sb) {
        sb.append("  UserIdentity[").append(Builder.LINE_SEPARATOR).append("    type: ").append(typeAsString(type))
                .append(Builder.LINE_SEPARATOR);
        if (type == USERNAME || type == USERNAME_PASSCODE)
            sb.append("    username: ").append(getUsername());
        else
            sb.append("    primaryField: byte[").append(primaryField.length).append(']');
        if (type == USERNAME_PASSCODE) {
            sb.append(Builder.LINE_SEPARATOR).append("    passcode: ");
            for (int i = secondaryField.length; --i >= 0;)
                sb.append('*');
        } else if (secondaryField.length > 0) {
            sb.append(Builder.LINE_SEPARATOR).append("    secondaryField: byte[").append(secondaryField.length)
                    .append(']');
        }
        return sb.append(Builder.LINE_SEPARATOR).append("  ]");
    }

}
