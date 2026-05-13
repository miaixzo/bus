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
package org.miaixz.bus.image.galaxy.data;

import java.io.Serial;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

import org.miaixz.bus.image.Tag;

/**
 * Represents the Code type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Code implements Serializable {

    /**
     * The serial version uid value.
     */
    @Serial
    private static final long serialVersionUID = 2852262087160L;

    /**
     * The no code meaning value.
     */
    private static final String NO_CODE_MEANING = "<none>";

    /**
     * The key value.
     */
    private transient final Key key = new Key();

    /**
     * The code value value.
     */
    private String codeValue;

    /**
     * The coding scheme designator value.
     */
    private String codingSchemeDesignator;

    /**
     * The coding scheme version value.
     */
    private String codingSchemeVersion;

    /**
     * The code meaning value.
     */
    private String codeMeaning;

    /**
     * Creates a new instance.
     *
     * @param codeValue              the code value.
     * @param codingSchemeDesignator the coding scheme designator.
     * @param codingSchemeVersion    the coding scheme version.
     * @param codeMeaning            the code meaning.
     */
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

    /**
     * Creates a new instance.
     *
     * @param s the s.
     */
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
                this.codingSchemeVersion = nullifyDCM01(
                        codingSchemeDesignator,
                        trimsubstring(s, endScheme + 1, endVersion, false));
            }
        }
        this.codeMeaning = trimsubstring(s, startMeaning, len - 2, false);
    }

    /**
     * Creates a new instance.
     *
     * @param item the item.
     */
    public Code(Attributes item) {
        this(codeValueOf(item), item.getString(Tag.CodingSchemeDesignator, null),
                item.getString(Tag.CodingSchemeVersion, null), item.getString(Tag.CodeMeaning, NO_CODE_MEANING));
    }

    /**
     * Creates a new instance.
     */
    protected Code() {
    } // needed for JPA

    /**
     * Executes the nullify dcm01 operation.
     *
     * @param codingSchemeDesignator the coding scheme designator.
     * @param codingSchemeVersion    the coding scheme version.
     * @return the operation result.
     */
    private static String nullifyDCM01(String codingSchemeDesignator, String codingSchemeVersion) {
        return "01".equals(codingSchemeVersion) && "DCM".equals(codingSchemeDesignator) ? null : codingSchemeVersion;
    }

    /**
     * Executes the trimsubstring operation.
     *
     * @param s     the s.
     * @param start the start.
     * @param end   the end.
     * @param empty the empty.
     * @return the operation result.
     */
    private static String trimsubstring(String s, int start, int end, boolean empty) {
        try {
            String trim = s.substring(start, end).trim();
            if (trim.isEmpty() == empty)
                return trim;
        } catch (StringIndexOutOfBoundsException e) {
        }
        throw new IllegalArgumentException(s);
    }

    /**
     * Executes the code value of operation.
     *
     * @param item the item.
     * @return the operation result.
     */
    private static String codeValueOf(Attributes item) {
        String codeValue;
        return (codeValue = item.getString(Tag.CodeValue)) != null ? codeValue
                : (codeValue = item.getString(Tag.LongCodeValue)) != null ? codeValue
                        : item.getString(Tag.URNCodeValue);
    }

    /**
     * Determines whether urn.
     *
     * @param codeValue the code value.
     * @return true if the condition is met; otherwise false.
     */
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

    /**
     * Gets the code value.
     *
     * @return the code value.
     */
    public final String getCodeValue() {
        return codeValue;
    }

    /**
     * Gets the coding scheme designator.
     *
     * @return the coding scheme designator.
     */
    public final String getCodingSchemeDesignator() {
        return codingSchemeDesignator;
    }

    /**
     * Gets the coding scheme version.
     *
     * @return the coding scheme version.
     */
    public final String getCodingSchemeVersion() {
        return codingSchemeVersion;
    }

    /**
     * Gets the code meaning.
     *
     * @return the code meaning.
     */
    public final String getCodeMeaning() {
        return codeMeaning;
    }

    /**
     * Returns the hash code.
     *
     * @return true if the condition is met; otherwise false.
     */
    @Override
    public int hashCode() {
        return codeValue.hashCode();
    }

    /**
     * Compares this instance with another object for equality.
     *
     * @param o the o.
     * @return true if the condition is met; otherwise false.
     */
    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Code other))
            return false;
        return equalsIgnoreMeaning(other) && Objects.equals(codeMeaning, other.getCodeMeaning());
    }

    /**
     * Executes the equals ignore meaning operation.
     *
     * @param other the other.
     * @return true if the condition is met; otherwise false.
     */
    public boolean equalsIgnoreMeaning(Code other) {
        if (other == this)
            return true;
        return Objects.equals(codeValue, other.getCodeValue())
                && Objects.equals(codingSchemeDesignator, other.getCodingSchemeDesignator())
                && Objects.equals(codingSchemeVersion, other.getCodingSchemeVersion());
    }

    /**
     * Returns the string representation.
     *
     * @return the string representation.
     */
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

    /**
     * Converts this value to item.
     *
     * @return the operation result.
     */
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

    /**
     * Executes the key operation.
     *
     * @return the operation result.
     */
    public final Key key() {
        return key;
    }

    /**
     * Represents the Key type.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public final class Key {

        /**
         * Creates a new instance.
         */
        private Key() {
        }

        /**
         * Returns the hash code.
         *
         * @return true if the condition is met; otherwise false.
         */
        @Override
        public int hashCode() {
            return codeValue.hashCode();
        }

        /**
         * Compares this instance with another object for equality.
         *
         * @param o the o.
         * @return true if the condition is met; otherwise false.
         */
        @Override
        public boolean equals(Object o) {
            if (o == this)
                return true;
            if (!(o instanceof Key other))
                return false;

            return equalsIgnoreMeaning(other.outer());
        }

        /**
         * Executes the outer operation.
         *
         * @return the operation result.
         */
        private Code outer() {
            return Code.this;
        }

    }

}
