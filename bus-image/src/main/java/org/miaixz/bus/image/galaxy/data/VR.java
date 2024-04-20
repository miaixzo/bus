/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
package org.miaixz.bus.image.galaxy.data;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.logger.Logger;

import java.util.Date;
import java.util.TimeZone;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public enum VR {

    AE(0x4145, 8, Symbol.C_SPACE, StringValueType.ASCII, false),
    AS(0x4153, 8, Symbol.C_SPACE, StringValueType.ASCII, false),
    AT(0x4154, 8, 0, BinaryValueType.TAG, false),
    CS(0x4353, 8, Symbol.C_SPACE, StringValueType.ASCII, false),
    DA(0x4441, 8, Symbol.C_SPACE, StringValueType.DA, false),
    DS(0x4453, 8, Symbol.C_SPACE, StringValueType.DS, false),
    DT(0x4454, 8, Symbol.C_SPACE, StringValueType.DT, false),
    FD(0x4644, 8, 0, BinaryValueType.DOUBLE, false),
    FL(0x464c, 8, 0, BinaryValueType.FLOAT, false),
    IS(0x4953, 8, Symbol.C_SPACE, StringValueType.IS, false),
    LO(0x4c4f, 8, Symbol.C_SPACE, StringValueType.STRING, false),
    LT(0x4c54, 8, Symbol.C_SPACE, StringValueType.TEXT, false),
    OB(0x4f42, 12, 0, BinaryValueType.BYTE, true),
    OD(0x4f44, 12, 0, BinaryValueType.DOUBLE, true),
    OF(0x4f46, 12, 0, BinaryValueType.FLOAT, true),
    OL(0x4f4c, 12, 0, BinaryValueType.INT, true),
    OW(0x4f57, 12, 0, BinaryValueType.SHORT, true),
    PN(0x504e, 8, Symbol.C_SPACE, StringValueType.PN, false),
    SH(0x5348, 8, Symbol.C_SPACE, StringValueType.STRING, false),
    SL(0x534c, 8, 0, BinaryValueType.INT, false),
    SQ(0x5351, 12, 0, SequenceValueType.SQ, false),
    SS(0x5353, 8, 0, BinaryValueType.SHORT, false),
    ST(0x5354, 8, Symbol.C_SPACE, StringValueType.TEXT, false),
    TM(0x544d, 8, Symbol.C_SPACE, StringValueType.TM, false),
    UC(0x5543, 12, Symbol.C_SPACE, StringValueType.STRING, false),
    UI(0x5549, 8, 0, StringValueType.ASCII, false),
    UL(0x554c, 8, 0, BinaryValueType.UINT, false),
    UN(0x554e, 12, 0, BinaryValueType.BYTE, true),
    UR(0x5552, 12, Symbol.C_SPACE, StringValueType.UR, false),
    US(0x5553, 8, 0, BinaryValueType.USHORT, false),
    UT(0x5554, 12, Symbol.C_SPACE, StringValueType.TEXT, false);

    private static final VR[] VALUE_OF = new VR[indexOf(UT) + 1];

    static {
        for (VR vr : VR.values())
            VALUE_OF[indexOf(vr)] = vr;
    }

    protected final int code;
    protected final int headerLength;
    protected final int paddingByte;
    protected final ValueType valueType;
    protected final boolean inlineBinary;

    VR(int code, int headerLength, int paddingByte, ValueType valueType,
       boolean inlineBinary) {
        this.code = code;
        this.headerLength = headerLength;
        this.paddingByte = paddingByte;
        this.valueType = valueType;
        this.inlineBinary = inlineBinary;
    }

    private static int indexOf(VR vr) {
        return vr.code - AE.code;
    }

    public static VR valueOf(int code) {
        try {
            VR vr = VALUE_OF[code - AE.code];
            if (null != vr) {
                return vr;
            }
        } catch (IndexOutOfBoundsException e) {
        }
        Logger.warn("Unrecognized VR internal: {}H - treat as UN", Tag.shortToHexString(code));
        return UN;
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

    public Date[] toDates(Object val, TimeZone tz, boolean ceil,
                          DatePrecision precisions) {
        return valueType.toDate(val, tz, ceil, precisions);
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
        return headerLength == 12 ? 1 : valueType.vmOf(val);
    }

    public static class Holder {
        public VR vr;
    }

}
