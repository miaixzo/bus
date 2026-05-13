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
package org.miaixz.bus.image.metric.hl7;

import java.io.Serial;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.miaixz.bus.core.lang.Fields;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;

/**
 * Represents the HL7Segment type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class HL7Segment implements Serializable {

    /**
     * The serial version uid value.
     */
    @Serial
    private static final long serialVersionUID = 2852265199583L;

    /**
     * The next message control id value.
     */
    private static final AtomicInteger nextMessageControlID = new AtomicInteger(new Random().nextInt());

    /**
     * The field separator value.
     */
    private final char fieldSeparator;

    /**
     * The encoding characters value.
     */
    private final String encodingCharacters;

    /**
     * The fields value.
     */
    private String[] fields;

    /**
     * Creates a new instance.
     *
     * @param size               the size.
     * @param fieldSeparator     the field separator.
     * @param encodingCharacters the encoding characters.
     */
    public HL7Segment(int size, char fieldSeparator, String encodingCharacters) {
        if (size <= 0)
            throw new IllegalArgumentException("size: " + size);
        this.fieldSeparator = fieldSeparator;
        this.encodingCharacters = encodingCharacters;
        this.fields = new String[size];
    }

    /**
     * Creates a new instance.
     *
     * @param size the size.
     */
    public HL7Segment(int size) {
        this(size, Symbol.C_OR, "^~\\&");
    }

    /**
     * Creates a new instance.
     *
     * @param s                  the s.
     * @param fieldSeparator     the field separator.
     * @param encodingCharacters the encoding characters.
     */
    public HL7Segment(String s, char fieldSeparator, String encodingCharacters) {
        this.fieldSeparator = fieldSeparator;
        this.encodingCharacters = encodingCharacters;
        this.fields = split(s, fieldSeparator);
    }

    /**
     * Executes the concat operation.
     *
     * @param ss    the ss.
     * @param delim the delim.
     * @return the operation result.
     */
    public static String concat(String[] ss, char delim) {
        int n = ss.length;
        if (n == 0)
            return Normal.EMPTY;
        if (n == 1) {
            String s = ss[0];
            return null != s ? s : Normal.EMPTY;
        }
        int len = n - 1;
        for (String s : ss)
            if (s != null)
                len += s.length();
        char[] cs = new char[len];
        for (int i = 0, off = 0; i < n; ++i) {
            if (i != 0)
                cs[off++] = delim;
            String s = ss[i];
            if (s != null) {
                int l = s.length();
                s.getChars(0, l, cs, off);
                off += l;
            }
        }
        return new String(cs);
    }

    /**
     * Executes the split operation.
     *
     * @param s     the s.
     * @param delim the delim.
     * @return the operation result.
     */
    public static String[] split(String s, char delim) {
        int count = 1;
        int delimPos = -1;
        while ((delimPos = s.indexOf(delim, delimPos + 1)) >= 0)
            count++;

        if (count == 1)
            return new String[] { s };

        String[] ss = new String[count];
        int delimPos2 = s.length();
        while (--count >= 0) {
            delimPos = s.lastIndexOf(delim, delimPos2 - 1);
            ss[count] = s.substring(delimPos + 1, delimPos2);
            delimPos2 = delimPos;
        }
        return ss;
    }

    /**
     * Parses the msh.
     *
     * @param b    the b.
     * @param size the size.
     * @return the operation result.
     */
    public static HL7Segment parseMSH(byte[] b, int size) {
        return parseMSH(b, size, new ParsePosition(0));
    }

    /**
     * Parses the msh.
     *
     * @param b    the b.
     * @param size the size.
     * @param pos  the pos.
     * @return the operation result.
     */
    public static HL7Segment parseMSH(byte[] b, int size, ParsePosition pos) {
        String s = parse(b, size, pos, null);
        if (s.length() < 8)
            throw new IllegalArgumentException("Invalid MSH Segment: " + s);
        return new HL7Segment(s, s.charAt(3), s.substring(4, 8));
    }

    /**
     * Executes the parse operation.
     *
     * @param b                  the b.
     * @param size               the size.
     * @param pos                the pos.
     * @param fieldSeparator     the field separator.
     * @param encodingCharacters the encoding characters.
     * @param charsetName        the charset name.
     * @return the operation result.
     */
    static HL7Segment parse(
            byte[] b,
            int size,
            ParsePosition pos,
            char fieldSeparator,
            String encodingCharacters,
            String charsetName) {
        String s = parse(b, size, pos, charsetName);
        return s != null ? new HL7Segment(s, fieldSeparator, encodingCharacters) : null;
    }

    /**
     * Executes the parse operation.
     *
     * @param b           the b.
     * @param size        the size.
     * @param pos         the pos.
     * @param charsetName the charset name.
     * @return the operation result.
     */
    private static String parse(byte[] b, int size, ParsePosition pos, String charsetName) {
        int off = pos.getIndex();
        int end = off;
        while (end < size && b[end] != Symbol.C_CR && b[end] != Symbol.C_LF)
            end++;

        int len = end - off;
        if (len == 0)
            return null;

        if (++end < size && (b[end] == Symbol.C_CR || b[end] == Symbol.C_LF))
            end++;

        pos.setIndex(end);
        try {
            return charsetName != null ? new String(b, off, len, charsetName) : new String(b, off, len);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("charsetName: " + charsetName);
        }
    }

    /**
     * Executes the next message control id operation.
     *
     * @return the operation result.
     */
    public static String nextMessageControlID() {
        return Integer.toString(nextMessageControlID.getAndIncrement() & 0x7FFFFFFF);
    }

    /**
     * Executes the time stamp operation.
     *
     * @param date the date.
     * @return the operation result.
     */
    public static String timeStamp(Date date) {
        return new SimpleDateFormat(Fields.PURE_DATETIME_TIP_PATTERN).format(date);
    }

    /**
     * Executes the make msh operation.
     *
     * @return the operation result.
     */
    public static HL7Segment makeMSH() {
        return makeMSH(21, Symbol.C_OR, "^~\\&");
    }

    /**
     * Executes the make msh operation.
     *
     * @param size               the size.
     * @param fieldSeparator     the field separator.
     * @param encodingCharacters the encoding characters.
     * @return the operation result.
     */
    public static HL7Segment makeMSH(int size, char fieldSeparator, String encodingCharacters) {
        HL7Segment msh = new HL7Segment(size, fieldSeparator, encodingCharacters);
        msh.setField(0, "MSH");
        msh.setField(1, encodingCharacters);
        msh.setField(6, timeStamp(new Date()));
        msh.setField(9, nextMessageControlID());
        msh.setField(10, "P");
        msh.setField(11, "2.5");
        return msh;
    }

    /**
     * Gets the field separator.
     *
     * @return the field separator.
     */
    public final char getFieldSeparator() {
        return fieldSeparator;
    }

    /**
     * Gets the component separator.
     *
     * @return the component separator.
     */
    public final char getComponentSeparator() {
        return encodingCharacters.charAt(0);
    }

    /**
     * Gets the repetition separator.
     *
     * @return the repetition separator.
     */
    public final char getRepetitionSeparator() {
        return encodingCharacters.charAt(1);
    }

    /**
     * Gets the escape character.
     *
     * @return the escape character.
     */
    public final char getEscapeCharacter() {
        return encodingCharacters.charAt(2);
    }

    /**
     * Gets the subcomponent separator.
     *
     * @return the subcomponent separator.
     */
    public final char getSubcomponentSeparator() {
        return encodingCharacters.charAt(3);
    }

    /**
     * Gets the encoding characters.
     *
     * @return the encoding characters.
     */
    public final String getEncodingCharacters() {
        return encodingCharacters;
    }

    /**
     * Sets the field.
     *
     * @param index the index.
     * @param value the value.
     */
    public void setField(int index, String value) {
        if (index >= fields.length)
            fields = Arrays.copyOf(fields, index + 1);
        fields[index] = value;
    }

    /**
     * Gets the field.
     *
     * @param index  the index.
     * @param defVal the def val.
     * @return the field.
     */
    public String getField(int index, String defVal) {
        String val = index < fields.length ? fields[index] : null;
        return val != null && !val.isEmpty() ? val : defVal;
    }

    /**
     * Executes the size operation.
     *
     * @return the operation result.
     */
    public int size() {
        return fields.length;
    }

    /**
     * Gets the sending application with facility.
     *
     * @return the sending application with facility.
     */
    public String getSendingApplicationWithFacility() {
        return getField(2, Normal.EMPTY) + Symbol.C_OR + getField(3, Normal.EMPTY);
    }

    /**
     * Sets the sending application with facility.
     *
     * @param s the s.
     */
    public void setSendingApplicationWithFacility(String s) {
        String[] ss = split(s, Symbol.C_OR);
        setField(2, ss[0]);
        if (ss.length > 1)
            setField(3, ss[1]);
    }

    /**
     * Gets the receiving application with facility.
     *
     * @return the receiving application with facility.
     */
    public String getReceivingApplicationWithFacility() {
        return getField(4, Normal.EMPTY) + Symbol.C_OR + getField(5, Normal.EMPTY);
    }

    /**
     * Sets the receiving application with facility.
     *
     * @param s the s.
     */
    public void setReceivingApplicationWithFacility(String s) {
        String[] ss = split(s, Symbol.C_OR);
        setField(4, ss[0]);
        if (ss.length > 1)
            setField(5, ss[1]);
    }

    /**
     * Gets the message type.
     *
     * @return the message type.
     */
    public String getMessageType() {
        String s = getField(8, Normal.EMPTY).replace(getComponentSeparator(), Symbol.C_CARET);
        int end = s.indexOf(Symbol.C_CARET, s.indexOf(Symbol.C_CARET) + 1);
        return end > 0 ? s.substring(0, end) : s;
    }

    /**
     * Gets the message control id.
     *
     * @return the message control id.
     */
    public String getMessageControlID() {
        return getField(9, null);
    }

    /**
     * Returns the string representation.
     *
     * @return the string representation.
     */
    public String toString() {
        return concat(fields, fieldSeparator);
    }

}
