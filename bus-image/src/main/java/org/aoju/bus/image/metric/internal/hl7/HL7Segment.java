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
package org.aoju.bus.image.metric.internal.hl7;

import org.aoju.bus.core.lang.Fields;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class HL7Segment implements Serializable {

    private static final AtomicInteger nextMessageControlID = new AtomicInteger(new Random().nextInt());

    private final char fieldSeparator;
    private final String encodingCharacters;
    private String[] fields;

    public HL7Segment(int size, char fieldSeparator, String encodingCharacters) {
        if (size <= 0)
            throw new IllegalArgumentException("size: " + size);
        this.fieldSeparator = fieldSeparator;
        this.encodingCharacters = encodingCharacters;
        this.fields = new String[size];
    }

    public HL7Segment(int size) {
        this(size, Symbol.C_OR, "^~\\&");
    }

    public HL7Segment(String s, char fieldSeparator, String encodingCharacters) {
        this.fieldSeparator = fieldSeparator;
        this.encodingCharacters = encodingCharacters;
        this.fields = split(s, fieldSeparator);
    }

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
            if (null != s)
                len += s.length();
        char[] cs = new char[len];
        for (int i = 0, off = 0; i < n; ++i) {
            if (i != 0)
                cs[off++] = delim;
            String s = ss[i];
            if (null != s) {
                int l = s.length();
                s.getChars(0, l, cs, off);
                off += l;
            }
        }
        return new String(cs);
    }

    public static String[] split(String s, char delim) {
        int count = 1;
        int delimPos = -1;
        while ((delimPos = s.indexOf(delim, delimPos + 1)) >= 0)
            count++;

        if (count == 1)
            return new String[]{s};

        String[] ss = new String[count];
        int delimPos2 = s.length();
        while (--count >= 0) {
            delimPos = s.lastIndexOf(delim, delimPos2 - 1);
            ss[count] = s.substring(delimPos + 1, delimPos2);
            delimPos2 = delimPos;
        }
        return ss;
    }

    public static HL7Segment parseMSH(byte[] b, int size) {
        return parseMSH(b, size, new ParsePosition(0));
    }

    public static HL7Segment parseMSH(byte[] b, int size, ParsePosition pos) {
        String s = parse(b, size, pos, null);
        if (s.length() < 8)
            throw new IllegalArgumentException("Invalid MSH Segment: " + s);
        return new HL7Segment(s, s.charAt(3), s.substring(4, 8));
    }

    static HL7Segment parse(byte[] b, int size, ParsePosition pos,
                            char fieldSeparator, String encodingCharacters, String charsetName) {
        String s = parse(b, size, pos, charsetName);
        return null != s
                ? new HL7Segment(s, fieldSeparator, encodingCharacters)
                : null;
    }

    private static String parse(byte[] b, int size, ParsePosition pos,
                                String charsetName) {
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
            return null != charsetName
                    ? new String(b, off, len, charsetName)
                    : new String(b, off, len);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("charsetName: " + charsetName);
        }
    }

    public static String nextMessageControlID() {
        return Integer.toString(
                nextMessageControlID.getAndIncrement() & 0x7FFFFFFF);
    }

    public static String timeStamp(Date date) {
        return new SimpleDateFormat(Fields.PURE_DATETIME_TIP_PATTERN).format(date);
    }

    public static HL7Segment makeMSH() {
        return makeMSH(21, Symbol.C_OR, "^~\\&");
    }

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

    public final char getFieldSeparator() {
        return fieldSeparator;
    }

    public final char getComponentSeparator() {
        return encodingCharacters.charAt(0);
    }

    public final char getRepetitionSeparator() {
        return encodingCharacters.charAt(1);
    }

    public final char getEscapeCharacter() {
        return encodingCharacters.charAt(2);
    }

    public final char getSubcomponentSeparator() {
        return encodingCharacters.charAt(3);
    }

    public final String getEncodingCharacters() {
        return encodingCharacters;
    }

    public void setField(int index, String value) {
        if (index >= fields.length)
            fields = Arrays.copyOf(fields, index + 1);
        fields[index] = value;
    }

    public String getField(int index, String defVal) {
        String val = index < fields.length ? fields[index] : null;
        return null != val && !val.isEmpty() ? val : defVal;
    }

    public int size() {
        return fields.length;
    }

    public String getSendingApplicationWithFacility() {
        return getField(2, Normal.EMPTY) + Symbol.C_OR + getField(3, Normal.EMPTY);
    }

    public void setSendingApplicationWithFacility(String s) {
        String[] ss = split(s, Symbol.C_OR);
        setField(2, ss[0]);
        if (ss.length > 1)
            setField(3, ss[1]);
    }

    public String getReceivingApplicationWithFacility() {
        return getField(4, Normal.EMPTY) + Symbol.C_OR + getField(5, Normal.EMPTY);
    }

    public void setReceivingApplicationWithFacility(String s) {
        String[] ss = split(s, Symbol.C_OR);
        setField(4, ss[0]);
        if (ss.length > 1)
            setField(5, ss[1]);
    }

    public String getMessageType() {
        String s = getField(8, Normal.EMPTY).replace(getComponentSeparator(), Symbol.C_CARET);
        int end = s.indexOf(Symbol.C_CARET, s.indexOf(Symbol.C_CARET) + 1);
        return end > 0 ? s.substring(0, end) : s;
    }

    public String getMessageControlID() {
        return getField(9, null);
    }

    public String toString() {
        return concat(fields, fieldSeparator);
    }

}
