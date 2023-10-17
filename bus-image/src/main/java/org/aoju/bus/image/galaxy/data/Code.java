/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.image.galaxy.data;

import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.image.Tag;

import java.io.Serializable;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class Code implements Serializable {

    private transient final Key key = new Key();
    private String codeValue;
    private String codingSchemeDesignator;
    private String codingSchemeVersion;
    private String codeMeaning;
    private transient int hashCode;

    public Code() {

    }

    public Code(String s) {
        int len = s.length();
        if (len < 9
                || s.charAt(0) != Symbol.C_PARENTHESE_LEFT
                || s.charAt(len - 2) != Symbol.C_DOUBLE_QUOTES
                || s.charAt(len - 1) != Symbol.C_PARENTHESE_RIGHT)
            throw new IllegalArgumentException(s);

        int endVal = s.indexOf(Symbol.C_COMMA);
        int endScheme = s.indexOf(Symbol.C_COMMA, endVal + 1);
        int startMeaning = s.indexOf(Symbol.C_DOUBLE_QUOTES, endScheme + 1) + 1;
        this.codeValue = trimsubstring(s, 1, endVal);
        this.codingSchemeDesignator = trimsubstring(s, endVal + 1, endScheme);
        this.codeMeaning = trimsubstring(s, startMeaning, len - 2);
        if (codingSchemeDesignator.endsWith(Symbol.BRACKET_RIGHT)) {
            int endVersion = s.lastIndexOf(Symbol.C_BRACKET_RIGHT, endScheme - 1);
            endScheme = s.lastIndexOf(Symbol.C_BRACKET_LEFT, endVersion - 1);
            this.codingSchemeDesignator = trimsubstring(s, endVal + 1, endScheme);
            this.codingSchemeVersion = nullifyDCM01(trimsubstring(s, endScheme + 1, endVersion));
        }
    }

    public Code(Attributes item) {
        this(item.getString(Tag.CodeValue, null),
                item.getString(Tag.CodingSchemeDesignator, null),
                item.getString(Tag.CodingSchemeVersion, null),
                item.getString(Tag.CodeMeaning, "<none>"));
    }

    public Code(String codeValue, String codingSchemeDesignator,
                String codingSchemeVersion, String codeMeaning) {
        if (null == codeValue)
            throw new NullPointerException("Missing Code Value");
        if (null == codingSchemeDesignator)
            throw new NullPointerException("Missing Coding Scheme Designator");
        if (null == codeMeaning)
            throw new NullPointerException("Missing Code Meaning");
        this.codeValue = codeValue;
        this.codingSchemeDesignator = codingSchemeDesignator;
        this.codingSchemeVersion = nullifyDCM01(codingSchemeVersion);
        this.codeMeaning = codeMeaning;
    }

    private String nullifyDCM01(String codingSchemeVersion) {
        return "01".equals(codingSchemeVersion) && "DCM".equals(codingSchemeDesignator) ? null : codingSchemeVersion;
    }

    private String trimsubstring(String s, int start, int end) {
        try {
            String trim = s.substring(start, end).trim();
            if (!trim.isEmpty())
                return trim;
        } catch (StringIndexOutOfBoundsException e) {
        }
        throw new IllegalArgumentException(s);
    }

    public final String getCodeValue() {
        return codeValue;
    }

    public final String getCodingSchemeDesignator() {
        return codingSchemeDesignator;
    }

    public final String getCodingSchemeVersion() {
        return codingSchemeVersion;
    }

    public final String getCodeMeaning() {
        return codeMeaning;
    }

    @Override
    public int hashCode() {
        int result = hashCode;
        if (result == 0) {
            result = 17;
            result = 31 * result + codeValue.hashCode();
            result = 31 * result + codingSchemeDesignator.hashCode();
            result = 31 * result + (null != codingSchemeVersion ? codingSchemeVersion.hashCode() : 0);
            hashCode = result;
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        return equals(o, false);
    }

    public boolean equalsIgnoreMeaning(Code o) {
        return equals(o, true);
    }

    private boolean equals(Object o, boolean ignoreMeaning) {
        if (o == this)
            return true;
        if (!(o instanceof Code))
            return false;
        Code other = (Code) o;
        return codeValue.equals(other.codeValue)
                && codingSchemeDesignator.equals(other.codingSchemeDesignator)
                && equals(codingSchemeVersion, other.codingSchemeVersion)
                && (ignoreMeaning || codeMeaning.equals(other.codeMeaning));
    }

    private boolean equals(String s1, String s2) {
        return s1 == s2 || null != s1 && s1.equals(s2);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Symbol.C_PARENTHESE_LEFT).append(codeValue).append(", ").append(codingSchemeDesignator);
        if (null != codingSchemeVersion)
            sb.append(" [").append(codingSchemeVersion).append(Symbol.C_BRACKET_RIGHT);
        sb.append(", \"").append(codeMeaning).append("\")");
        return sb.toString();
    }

    public Attributes toItem() {
        Attributes codeItem = new Attributes(null != codingSchemeVersion ? 4 : 3);
        codeItem.setString(Tag.CodeValue, VR.SH, codeValue);
        codeItem.setString(Tag.CodingSchemeDesignator, VR.SH, codingSchemeDesignator);
        if (null != codingSchemeVersion)
            codeItem.setString(Tag.CodingSchemeVersion, VR.SH, codingSchemeVersion);
        codeItem.setString(Tag.CodeMeaning, VR.LO, codeMeaning);
        return codeItem;
    }

    public final Key key() {
        return key;
    }

    public final class Key {
        private Key() {
        }

        @Override
        public int hashCode() {
            return outer().hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (o == this)
                return true;
            if (!(o instanceof Key))
                return false;

            Key other = (Key) o;
            return outer().equalsIgnoreMeaning(other.outer());
        }

        private Code outer() {
            return Code.this;
        }
    }

}
