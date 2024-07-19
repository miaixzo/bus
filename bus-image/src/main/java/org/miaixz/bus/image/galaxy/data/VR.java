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
package org.miaixz.bus.image.galaxy.data;

import org.miaixz.bus.core.lang.Symbol;

import java.util.Date;
import java.util.TimeZone;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public enum VR {
    /**
     * Application Entity
     */
    AE(0x4145, 8, Symbol.C_SPACE, StringValueType.ASCII, false),

    /**
     * Age String
     */
    AS(0x4153, 8, Symbol.C_SPACE, StringValueType.ASCII, false),

    /**
     * Attribute Tag
     */
    AT(0x4154, 8, 0, BinaryValueType.TAG, false),

    /**
     * Code String
     */
    CS(0x4353, 8, Symbol.C_SPACE, StringValueType.ASCII, false),

    /**
     * Date
     */
    DA(0x4441, 8, Symbol.C_SPACE, StringValueType.DA, false),

    /**
     * Decimal String
     */
    DS(0x4453, 8, Symbol.C_SPACE, StringValueType.DS, false),

    /**
     * Date Time
     */
    DT(0x4454, 8, Symbol.C_SPACE, StringValueType.DT, false),

    /**
     * Floating Point Double
     */
    FD(0x4644, 8, 0, BinaryValueType.DOUBLE, false),

    /**
     * Floating Point Single
     */
    FL(0x464c, 8, 0, BinaryValueType.FLOAT, false),

    /**
     * Integer String
     */
    IS(0x4953, 8, Symbol.C_SPACE, StringValueType.IS, false),

    /**
     * Long String
     */
    LO(0x4c4f, 8, Symbol.C_SPACE, StringValueType.STRING, false),

    /**
     * Long Text
     */
    LT(0x4c54, 8, Symbol.C_SPACE, StringValueType.TEXT, false),

    /**
     * Other Byte
     */
    OB(0x4f42, 12, 0, BinaryValueType.BYTE, true),

    /**
     * Other Double
     */
    OD(0x4f44, 12, 0, BinaryValueType.DOUBLE, true),

    /**
     * Other Float
     */
    OF(0x4f46, 12, 0, BinaryValueType.FLOAT, true),

    /**
     * Other Long
     */
    OL(0x4f4c, 12, 0, BinaryValueType.INT, true),

    /**
     * Other 64-bit Very Long
     */
    OV(0x4f56, 12, 0, BinaryValueType.LONG, true),

    /**
     * Other Word
     */
    OW(0x4f57, 12, 0, BinaryValueType.SHORT, true),

    /**
     * Person Name
     */
    PN(0x504e, 8, Symbol.C_SPACE, StringValueType.PN, false),

    /**
     * Short String
     */
    SH(0x5348, 8, Symbol.C_SPACE, StringValueType.STRING, false),

    /**
     * Signed Long
     */
    SL(0x534c, 8, 0, BinaryValueType.INT, false),

    /**
     * Sequence of Items
     */
    SQ(0x5351, 12, 0, SequenceValueType.SQ, false),

    /**
     * Signed Short
     */
    SS(0x5353, 8, 0, BinaryValueType.SHORT, false),

    /**
     * Short Text
     */
    ST(0x5354, 8, Symbol.C_SPACE, StringValueType.TEXT, false),

    /**
     * Signed 64-bit Long
     */
    SV(0x5356, 12, 0, BinaryValueType.LONG, false),

    /**
     * Time
     */
    TM(0x544d, 8, Symbol.C_SPACE, StringValueType.TM, false),

    /**
     * Unlimited Characters
     */
    UC(0x5543, 12, Symbol.C_SPACE, StringValueType.STRING, false),

    /**
     * Unique Identifier (UID)
     */
    UI(0x5549, 8, 0, StringValueType.ASCII, false),

    /**
     * Unsigned Long
     */
    UL(0x554c, 8, 0, BinaryValueType.UINT, false),

    /**
     * Unknown
     */
    UN(0x554e, 12, 0, BinaryValueType.BYTE, true),

    /**
     * Universal Resource Identifier or Universal Resource Locator (URI/URL)
     */
    UR(0x5552, 12, Symbol.C_SPACE, StringValueType.UR, false),

    /**
     * Unsigned Short
     */
    US(0x5553, 8, 0, BinaryValueType.USHORT, false),

    /**
     * Unlimited Text
     */
    UT(0x5554, 12, Symbol.C_SPACE, StringValueType.TEXT, false),

    /**
     * Unsigned 64-bit Long
     */
    UV(0x5556, 12, 0, BinaryValueType.ULONG, false);

    private static final VR[] VALUE_OF = new VR[1024];

    static {
        for (VR vr : VR.values())
            VALUE_OF[indexOf(vr.code)] = vr;
    }

    private final int code;
    private final int headerLength;
    private final int paddingByte;
    private final ValueType valueType;
    private final boolean inlineBinary;

    VR(int code, int headerLength, int paddingByte, ValueType valueType,
       boolean inlineBinary) {
        this.code = code;
        this.headerLength = headerLength;
        this.paddingByte = paddingByte;
        this.valueType = valueType;
        this.inlineBinary = inlineBinary;
    }

    private static int indexOf(int code) {
        return ((code & 0x1f00) >> 3) | (code & 0x1f);
    }

    public static VR valueOf(int code) {
        return ((code ^ 0x4040) & 0xffffe0e0) == 0 ? VALUE_OF[indexOf(code)] : null;
    }

    public int code() {
        return code;
    }

    public int headerLength() {
        return headerLength;
    }

    public int paddingByte() {
        return paddingByte;
    }

    public boolean isTemporalType() {
        return valueType.isTemporalType();
    }

    public boolean isStringType() {
        return valueType.isStringValue();
    }

    public boolean useSpecificCharacterSet() {
        return valueType.useSpecificCharacterSet();
    }

    public boolean isIntType() {
        return valueType.isIntValue();
    }

    public boolean isInlineBinary() {
        return inlineBinary;
    }

    public int numEndianBytes() {
        return valueType.numEndianBytes();
    }

    public byte[] toggleEndian(byte[] b, boolean preserve) {
        return valueType.toggleEndian(b, preserve);
    }

    public byte[] toBytes(Object val, SpecificCharacterSet cs) {
        return valueType.toBytes(val, cs);
    }

    public Object toStrings(Object val, boolean bigEndian, SpecificCharacterSet cs) {
        return valueType.toStrings(val, bigEndian, cs);
    }

    public String toString(Object val, boolean bigEndian, int valueIndex,
                           String defVal) {
        return valueType.toString(val, bigEndian, valueIndex, defVal);
    }

    public int toInt(Object val, boolean bigEndian, int valueIndex, int defVal) {
        return valueType.toInt(val, bigEndian, valueIndex, defVal);
    }

    public int[] toInts(Object val, boolean bigEndian) {
        return valueType.toInts(val, bigEndian);
    }

    public long toLong(Object val, boolean bigEndian, int valueIndex, long defVal) {
        return valueType.toLong(val, bigEndian, valueIndex, defVal);
    }

    public long[] toLongs(Object val, boolean bigEndian) {
        return valueType.toLongs(val, bigEndian);
    }

    public float toFloat(Object val, boolean bigEndian, int valueIndex, float defVal) {
        return valueType.toFloat(val, bigEndian, valueIndex, defVal);
    }

    public float[] toFloats(Object val, boolean bigEndian) {
        return valueType.toFloats(val, bigEndian);
    }

    public double toDouble(Object val, boolean bigEndian, int valueIndex,
                           double defVal) {
        return valueType.toDouble(val, bigEndian, valueIndex, defVal);
    }

    public double[] toDoubles(Object val, boolean bigEndian) {
        return valueType.toDoubles(val, bigEndian);
    }

    public Date toDate(Object val, TimeZone tz, int valueIndex, boolean ceil,
                       Date defVal, DatePrecision precision) {
        return valueType.toDate(val, tz, valueIndex, ceil, defVal, precision);
    }

    public Date[] toDates(Object val, TimeZone tz, boolean ceil, DatePrecision precision) {
        return valueType.toDate(val, tz, ceil, precision);
    }

    Object toValue(byte[] b) {
        return valueType.toValue(b);
    }

    Object toValue(String s, boolean bigEndian) {
        return valueType.toValue(s, bigEndian);
    }

    Object toValue(String[] ss, boolean bigEndian) {
        return valueType.toValue(ss, bigEndian);
    }

    Object toValue(int[] is, boolean bigEndian) {
        return valueType.toValue(is, bigEndian);
    }

    Object toValue(long[] ls, boolean bigEndian) {
        return valueType.toValue(ls, bigEndian);
    }

    Object toValue(float[] fs, boolean bigEndian) {
        return valueType.toValue(fs, bigEndian);
    }

    Object toValue(double[] ds, boolean bigEndian) {
        return valueType.toValue(ds, bigEndian);
    }

    public Object toValue(Date[] ds, TimeZone tz, DatePrecision precision) {
        return valueType.toValue(ds, tz, precision);
    }

    public boolean prompt(Object val, boolean bigEndian,
                          SpecificCharacterSet cs, int maxChars, StringBuilder sb) {
        return valueType.prompt(val, bigEndian, cs, maxChars, sb);
    }

    public int vmOf(Object val) {
        return valueType.vmOf(val);
    }

    public static class Holder {
        public VR vr;
    }

}
