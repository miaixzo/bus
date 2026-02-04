/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.image.metric.pdu;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.image.Builder;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class IdentityRQ {

    public static final int USERNAME = 1;
    public static final int USERNAME_PASSCODE = 2;
    public static final int KERBEROS = 3;
    public static final int SAML = 4;
    public static final int JWT = 5;

    private static final String[] TYPES = { "0", "1 - Username", "2 - Username and passcode",
            "3 - Kerberos Service ticket", "4 - SAML Assertion", "5 - JSON Web Token (JWT)" };

    private final int type;
    private final boolean rspReq;
    private final byte[] primaryField;
    private final byte[] secondaryField;

    public IdentityRQ(int type, boolean rspReq, byte[] primaryField, byte[] secondaryField) {
        this.type = type;
        this.rspReq = rspReq;
        this.primaryField = primaryField.clone();
        this.secondaryField = secondaryField != null ? secondaryField.clone() : new byte[0];
    }

    public IdentityRQ(int type, boolean rspReq, byte[] primaryField) {
        this(type, rspReq, primaryField, null);
    }

    public static IdentityRQ username(String username, boolean rspReq) {
        return new IdentityRQ(USERNAME, rspReq, toBytes(username));
    }

    public static IdentityRQ usernamePasscode(String username, char[] passcode, boolean rspReq) {
        return new IdentityRQ(USERNAME_PASSCODE, rspReq, toBytes(username), toBytes(new String(passcode)));
    }

    public static IdentityRQ kerberos(byte[] ticket, boolean rspReq) {
        return new IdentityRQ(KERBEROS, rspReq, ticket);
    }

    public static IdentityRQ saml(String assertion, boolean rspReq) {
        return new IdentityRQ(SAML, rspReq, toBytes(assertion));
    }

    public static IdentityRQ jwt(String token, boolean rspReq) {
        return new IdentityRQ(JWT, rspReq, toBytes(token));
    }

    private static String typeAsString(int type) {
        try {
            return TYPES[type];
        } catch (IndexOutOfBoundsException e) {
            return Integer.toString(type);
        }
    }

    private static byte[] toBytes(String s) {
        return s.getBytes(Charset.UTF_8);
    }

    private static String toString(byte[] b) {
        return new String(b, Charset.UTF_8);
    }

    public final int getType() {
        return type;
    }

    public final boolean isPositiveResponseRequested() {
        return rspReq;
    }

    public final byte[] getPrimaryField() {
        return primaryField.clone();
    }

    public final byte[] getSecondaryField() {
        return secondaryField.clone();
    }

    public final String getUsername() {
        return toString(primaryField);
    }

    public final char[] getPasscode() {
        return toString(secondaryField).toCharArray();
    }

    public int length() {
        return 6 + primaryField.length + secondaryField.length;
    }

    @Override
    public String toString() {
        return promptTo(new StringBuilder()).toString();
    }

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
