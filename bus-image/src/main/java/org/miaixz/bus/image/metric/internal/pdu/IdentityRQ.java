/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 */
package org.miaixz.bus.image.metric.internal.pdu;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.image.galaxy.Material;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class IdentityRQ {

    public static final int USERNAME = 1;
    public static final int USERNAME_PASSCODE = 2;
    public static final int KERBEROS = 3;
    public static final int SAML = 4;

    private static final String[] TYPES = {
            "0",
            "1 - Username",
            "2 - Username and passcode",
            "3 - Kerberos Service ticket",
            "4 - SAML Assertion"
    };

    private final int type;
    private final boolean rspReq;
    private final byte[] primaryField;
    private final byte[] secondaryField;

    public IdentityRQ(int type, boolean rspReq, byte[] primaryField,
                      byte[] secondaryField) {
        this.type = type;
        this.rspReq = rspReq;
        this.primaryField = primaryField.clone();
        this.secondaryField = null != secondaryField
                ? secondaryField.clone()
                : new byte[0];
    }

    public IdentityRQ(int type, boolean rspReq, byte[] primaryField) {
        this(type, rspReq, primaryField, null);
    }

    public IdentityRQ(String username, char[] passcode) {
        this(USERNAME_PASSCODE, true, toBytes(username),
                toBytes(new String(passcode)));
    }

    public IdentityRQ(String username, boolean rspReq) {
        this(USERNAME, rspReq, toBytes(username));
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
        sb.append("  UserIdentity[")
                .append(Material.LINE_SEPARATOR)
                .append("    type: ")
                .append(typeAsString(type))
                .append(Material.LINE_SEPARATOR);
        if (type == USERNAME
                || type == USERNAME_PASSCODE)
            sb.append("    username: ")
                    .append(getUsername());
        else
            sb.append("    primaryField: byte[")
                    .append(primaryField.length)
                    .append(']');
        if (type == USERNAME_PASSCODE) {
            sb.append(Material.LINE_SEPARATOR)
                    .append("    passcode: ");
            for (int i = secondaryField.length; --i >= 0; )
                sb.append(Symbol.C_STAR);
        } else if (secondaryField.length > 0) {
            sb.append(Material.LINE_SEPARATOR)
                    .append("    secondaryField: byte[")
                    .append(secondaryField.length)
                    .append(']');
        }
        return sb.append(Material.LINE_SEPARATOR)
                .append("  ]");
    }

}
