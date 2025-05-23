/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.image.galaxy.data;

import java.io.Serial;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

import org.miaixz.bus.image.Tag;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class Code implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852550785925L;

    private static final String NO_CODE_MEANING = "<none>";
    private transient final Key key = new Key();
    private String codeValue;
    private String codingSchemeDesignator;
    private String codingSchemeVersion;
    private String codeMeaning;

    public Code(String codeValue, String codingSchemeDesignator, String codingSchemeVersion, String codeMeaning) {
        if (codeValue == null)
            throw new NullPointerException("Missing Code Value");
        if (isURN(codeValue)) {
            if (codingSchemeDesignator != null || codingSchemeVersion != null)
                throw new IllegalArgumentException("URN Code Value with Coding Scheme Designator");
        } else {
            if (codingSchemeDesignator == null)
                throw new NullPointerException("Missing Coding Scheme Designator");
        }
        if (codeMeaning == null)
            throw new NullPointerException("Missing Code Meaning");
        this.codeValue = codeValue;
        this.codingSchemeDesignator = codingSchemeDesignator;
        this.codingSchemeVersion = nullifyDCM01(codingSchemeDesignator, codingSchemeVersion);
        this.codeMeaning = codeMeaning;
    }

    public Code(String s) {
        int len = s.length();
        if (len < 9 || s.charAt(0) != '(' || s.charAt(len - 2) != '"' || s.charAt(len - 1) != ')')
            throw new IllegalArgumentException(s);

        int endVal = s.indexOf(',');
        int endScheme = s.indexOf(',', endVal + 1);
        int startMeaning = s.indexOf('"', endScheme + 1) + 1;
        this.codeValue = trimsubstring(s, 1, endVal, false);
        if (isURN(codeValue)) {
            trimsubstring(s, endVal + 1, endScheme, true);
        } else {
            this.codingSchemeDesignator = trimsubstring(s, endVal + 1, endScheme, false);
            if (codingSchemeDesignator.endsWith("]")) {
                int endVersion = s.lastIndexOf(']', endScheme - 1);
                endScheme = s.lastIndexOf('[', endVersion - 1);
                this.codingSchemeDesignator = trimsubstring(s, endVal + 1, endScheme, false);
                this.codingSchemeVersion = nullifyDCM01(codingSchemeDesignator,
                        trimsubstring(s, endScheme + 1, endVersion, false));
            }
        }
        this.codeMeaning = trimsubstring(s, startMeaning, len - 2, false);
    }

    public Code(Attributes item) {
        this(codeValueOf(item), item.getString(Tag.CodingSchemeDesignator, null),
                item.getString(Tag.CodingSchemeVersion, null), item.getString(Tag.CodeMeaning, NO_CODE_MEANING));
    }

    protected Code() {
    } // needed for JPA

    private static String nullifyDCM01(String codingSchemeDesignator, String codingSchemeVersion) {
        return "01".equals(codingSchemeVersion) && "DCM".equals(codingSchemeDesignator) ? null : codingSchemeVersion;
    }

    private static String trimsubstring(String s, int start, int end, boolean empty) {
        try {
            String trim = s.substring(start, end).trim();
            if (trim.isEmpty() == empty)
                return trim;
        } catch (StringIndexOutOfBoundsException e) {
        }
        throw new IllegalArgumentException(s);
    }

    private static String codeValueOf(Attributes item) {
        String codeValue;
        return (codeValue = item.getString(Tag.CodeValue)) != null ? codeValue
                : (codeValue = item.getString(Tag.LongCodeValue)) != null ? codeValue
                        : item.getString(Tag.URNCodeValue);
    }

    private static boolean isURN(String codeValue) {
        if (codeValue.indexOf(':') > 0)
            try {
                if (!codeValue.startsWith("urn:"))
                    new URL(codeValue);
                return true;
            } catch (MalformedURLException e) {
            }
        return false;
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
        return codeValue.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Code other))
            return false;
        return equalsIgnoreMeaning(other) && Objects.equals(codeMeaning, other.getCodeMeaning());
    }

    public boolean equalsIgnoreMeaning(Code other) {
        if (other == this)
            return true;
        return Objects.equals(codeValue, other.getCodeValue())
                && Objects.equals(codingSchemeDesignator, other.getCodingSchemeDesignator())
                && Objects.equals(codingSchemeVersion, other.getCodingSchemeVersion());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('(').append(codeValue).append(", ");
        if (codingSchemeDesignator != null) {
            sb.append(codingSchemeDesignator);
            if (codingSchemeVersion != null)
                sb.append(" [").append(codingSchemeVersion).append(']');
        }
        sb.append(", \"").append(codeMeaning).append("\")");
        return sb.toString();
    }

    public Attributes toItem() {
        Attributes codeItem = new Attributes(codingSchemeVersion != null ? 4 : 3);
        if (codingSchemeDesignator == null) {
            codeItem.setString(Tag.URNCodeValue, VR.UR, codeValue);
        } else {
            if (codeValue.length() > 16) {
                codeItem.setString(Tag.LongCodeValue, VR.UC, codeValue);
            } else {
                codeItem.setString(Tag.CodeValue, VR.SH, codeValue);
            }
            codeItem.setString(Tag.CodingSchemeDesignator, VR.SH, codingSchemeDesignator);
            if (codingSchemeVersion != null) {
                codeItem.setString(Tag.CodingSchemeVersion, VR.SH, codingSchemeVersion);
            }
        }
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
            return codeValue.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (o == this)
                return true;
            if (!(o instanceof Key other))
                return false;

            return equalsIgnoreMeaning(other.outer());
        }

        private Code outer() {
            return Code.this;
        }
    }

}
