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

import java.time.temporal.Temporal;
import java.util.Date;
import java.util.TimeZone;

import org.miaixz.bus.core.lang.Symbol;

/**
 * Enumeration of DICOM Value Representations (VR). VR defines the data type and format of values that can be associated
 * with a DICOM data element. Each VR specifies the value multiplicity, character set, and handling requirements for the
 * corresponding DICOM attribute.
 * <p>
 * Common VRs include:
 * <ul>
 * <li>AE (Application Entity)</li>
 * <li>AS (Age String)</li>
 * <li>CS (Code String)</li>
 * <li>DA (Date)</li>
 * <li>DS (Decimal String)</li>
 * <li>DT (Date Time)</li>
 * <li>FD (Floating Point Double)</li>
 * <li>FL (Floating Point Single)</li>
 * <li>IS (Integer String)</li>
 * <li>LO (Long String)</li>
 * <li>PN (Person Name)</li>
 * <li>SH (Short String)</li>
 * <li>ST (Short Text)</li>
 * <li>SQ (Sequence of Items)</li>
 * <li>TM (Time)</li>
 * <li>UI (Unique Identifier)</li>
 * <li>UT (Unlimited Text)</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 21+
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

    /**
     * The value of value.
     */
    private static final VR[] VALUE_OF = new VR[1024];

    static {
        for (VR vr : VR.values())
            VALUE_OF[indexOf(vr.code)] = vr;
    }

    /**
     * The code value.
     */
    private final int code;

    /**
     * The header length value.
     */
    private final int headerLength;

    /**
     * The padding byte value.
     */
    private final int paddingByte;

    /**
     * The value type value.
     */
    private final ValueType valueType;

    /**
     * The inline binary value.
     */
    private final boolean inlineBinary;

    /**
     * Creates a new instance.
     *
     * @param code         the code.
     * @param headerLength the header length.
     * @param paddingByte  the padding byte.
     * @param valueType    the value type.
     * @param inlineBinary the inline binary.
     */
    VR(int code, int headerLength, int paddingByte, ValueType valueType, boolean inlineBinary) {
        this.code = code;
        this.headerLength = headerLength;
        this.paddingByte = paddingByte;
        this.valueType = valueType;
        this.inlineBinary = inlineBinary;
    }

    /**
     * Executes the index of operation.
     *
     * @param code the code.
     * @return the operation result.
     */
    private static int indexOf(int code) {
        return ((code & 0x1f00) >> 3) | (code & 0x1f);
    }

    /**
     * Executes the value of operation.
     *
     * @param code the code.
     * @return the operation result.
     */
    public static VR valueOf(int code) {
        return ((code ^ 0x4040) & 0xffffe0e0) == 0 ? VALUE_OF[indexOf(code)] : null;
    }

    /**
     * Executes the code operation.
     *
     * @return the operation result.
     */
    public int code() {
        return code;
    }

    /**
     * Executes the header length operation.
     *
     * @return the operation result.
     */
    public int headerLength() {
        return headerLength;
    }

    /**
     * Executes the padding byte operation.
     *
     * @return the operation result.
     */
    public int paddingByte() {
        return paddingByte;
    }

    /**
     * Determines whether temporal type.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isTemporalType() {
        return valueType.isTemporalType();
    }

    /**
     * Determines whether string type.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isStringType() {
        return valueType.isStringValue();
    }

    /**
     * Executes the use specific character set operation.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean useSpecificCharacterSet() {
        return valueType.useSpecificCharacterSet();
    }

    /**
     * Determines whether int type.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isIntType() {
        return valueType.isIntValue();
    }

    /**
     * Determines whether inline binary.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isInlineBinary() {
        return inlineBinary;
    }

    /**
     * Executes the num endian bytes operation.
     *
     * @return the operation result.
     */
    public int numEndianBytes() {
        return valueType.numEndianBytes();
    }

    /**
     * Converts this value to ggle endian.
     *
     * @param b        the b.
     * @param preserve the preserve.
     * @return the operation result.
     */
    public byte[] toggleEndian(byte[] b, boolean preserve) {
        return valueType.toggleEndian(b, preserve);
    }

    /**
     * Converts this value to bytes.
     *
     * @param val the val.
     * @param cs  the cs.
     * @return the operation result.
     */
    public byte[] toBytes(Object val, SpecificCharacterSet cs) {
        return valueType.toBytes(val, cs);
    }

    /**
     * Converts this value to strings.
     *
     * @param val       the val.
     * @param bigEndian the big endian.
     * @param cs        the cs.
     * @return the operation result.
     */
    public Object toStrings(Object val, boolean bigEndian, SpecificCharacterSet cs) {
        return valueType.toStrings(val, bigEndian, cs);
    }

    /**
     * Returns the string representation.
     *
     * @param val        the val.
     * @param bigEndian  the big endian.
     * @param valueIndex the value index.
     * @param defVal     the def val.
     * @return the string representation.
     */
    public String toString(Object val, boolean bigEndian, int valueIndex, String defVal) {
        return valueType.toString(val, bigEndian, valueIndex, defVal);
    }

    /**
     * Converts this value to int.
     *
     * @param val        the val.
     * @param bigEndian  the big endian.
     * @param valueIndex the value index.
     * @param defVal     the def val.
     * @return the operation result.
     */
    public int toInt(Object val, boolean bigEndian, int valueIndex, int defVal) {
        return valueType.toInt(val, bigEndian, valueIndex, defVal);
    }

    /**
     * Converts this value to ints.
     *
     * @param val       the val.
     * @param bigEndian the big endian.
     * @return the operation result.
     */
    public int[] toInts(Object val, boolean bigEndian) {
        return valueType.toInts(val, bigEndian);
    }

    /**
     * Converts this value to long.
     *
     * @param val        the val.
     * @param bigEndian  the big endian.
     * @param valueIndex the value index.
     * @param defVal     the def val.
     * @return the operation result.
     */
    public long toLong(Object val, boolean bigEndian, int valueIndex, long defVal) {
        return valueType.toLong(val, bigEndian, valueIndex, defVal);
    }

    /**
     * Converts this value to longs.
     *
     * @param val       the val.
     * @param bigEndian the big endian.
     * @return the operation result.
     */
    public long[] toLongs(Object val, boolean bigEndian) {
        return valueType.toLongs(val, bigEndian);
    }

    /**
     * Converts this value to float.
     *
     * @param val        the val.
     * @param bigEndian  the big endian.
     * @param valueIndex the value index.
     * @param defVal     the def val.
     * @return the operation result.
     */
    public float toFloat(Object val, boolean bigEndian, int valueIndex, float defVal) {
        return valueType.toFloat(val, bigEndian, valueIndex, defVal);
    }

    /**
     * Converts this value to floats.
     *
     * @param val       the val.
     * @param bigEndian the big endian.
     * @return the operation result.
     */
    public float[] toFloats(Object val, boolean bigEndian) {
        return valueType.toFloats(val, bigEndian);
    }

    /**
     * Converts this value to double.
     *
     * @param val        the val.
     * @param bigEndian  the big endian.
     * @param valueIndex the value index.
     * @param defVal     the def val.
     * @return the operation result.
     */
    public double toDouble(Object val, boolean bigEndian, int valueIndex, double defVal) {
        return valueType.toDouble(val, bigEndian, valueIndex, defVal);
    }

    /**
     * Converts this value to doubles.
     *
     * @param val       the val.
     * @param bigEndian the big endian.
     * @return the operation result.
     */
    public double[] toDoubles(Object val, boolean bigEndian) {
        return valueType.toDoubles(val, bigEndian);
    }

    /**
     * Converts this value to temporal.
     *
     * @param val        the val.
     * @param valueIndex the value index.
     * @param precision  the precision.
     * @return the operation result.
     */
    public Temporal toTemporal(Object val, int valueIndex, DatePrecision precision) {
        return valueType.toTemporal(val, valueIndex, precision);
    }

    /**
     * Converts this value to date.
     *
     * @param val        the val.
     * @param tz         the tz.
     * @param valueIndex the value index.
     * @param ceil       the ceil.
     * @param defVal     the def val.
     * @param precision  the precision.
     * @return the operation result.
     */
    public Date toDate(Object val, TimeZone tz, int valueIndex, boolean ceil, Date defVal, DatePrecision precision) {
        return valueType.toDate(val, tz, valueIndex, ceil, defVal, precision);
    }

    /**
     * Converts this value to dates.
     *
     * @param val       the val.
     * @param tz        the tz.
     * @param ceil      the ceil.
     * @param precision the precision.
     * @return the operation result.
     */
    public Date[] toDates(Object val, TimeZone tz, boolean ceil, DatePrecision precision) {
        return valueType.toDate(val, tz, ceil, precision);
    }

    /**
     * Converts this value to value.
     *
     * @param b the b.
     * @return the operation result.
     */
    Object toValue(byte[] b) {
        return valueType.toValue(b);
    }

    /**
     * Converts this value to value.
     *
     * @param s         the s.
     * @param bigEndian the big endian.
     * @return the operation result.
     */
    Object toValue(String s, boolean bigEndian) {
        return valueType.toValue(s, bigEndian);
    }

    /**
     * Converts this value to value.
     *
     * @param ss        the ss.
     * @param bigEndian the big endian.
     * @return the operation result.
     */
    Object toValue(String[] ss, boolean bigEndian) {
        return valueType.toValue(ss, bigEndian);
    }

    /**
     * Converts this value to value.
     *
     * @param is        the is.
     * @param bigEndian the big endian.
     * @return the operation result.
     */
    Object toValue(int[] is, boolean bigEndian) {
        return valueType.toValue(is, bigEndian);
    }

    /**
     * Converts this value to value.
     *
     * @param ls        the ls.
     * @param bigEndian the big endian.
     * @return the operation result.
     */
    Object toValue(long[] ls, boolean bigEndian) {
        return valueType.toValue(ls, bigEndian);
    }

    /**
     * Converts this value to value.
     *
     * @param fs        the fs.
     * @param bigEndian the big endian.
     * @return the operation result.
     */
    Object toValue(float[] fs, boolean bigEndian) {
        return valueType.toValue(fs, bigEndian);
    }

    /**
     * Converts this value to value.
     *
     * @param ds        the ds.
     * @param bigEndian the big endian.
     * @return the operation result.
     */
    Object toValue(double[] ds, boolean bigEndian) {
        return valueType.toValue(ds, bigEndian);
    }

    /**
     * Converts this value to value.
     *
     * @param ds        the ds.
     * @param tz        the tz.
     * @param precision the precision.
     * @return the operation result.
     */
    public Object toValue(Date[] ds, TimeZone tz, DatePrecision precision) {
        return valueType.toValue(ds, tz, precision);
    }

    /**
     * Executes the prompt operation.
     *
     * @param val       the val.
     * @param bigEndian the big endian.
     * @param cs        the cs.
     * @param maxChars  the max chars.
     * @param sb        the sb.
     * @return true if the condition is met; otherwise false.
     */
    public boolean prompt(Object val, boolean bigEndian, SpecificCharacterSet cs, int maxChars, StringBuilder sb) {
        return valueType.prompt(val, bigEndian, cs, maxChars, sb);
    }

    /**
     * Executes the vm of operation.
     *
     * @param val the val.
     * @return the operation result.
     */
    public int vmOf(Object val) {
        return valueType.vmOf(val);
    }

    /**
     * Represents the Holder type.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public static class Holder {

        /**
         * Constructs a new {@code Holder} instance.
         */
        public Holder() {
            // No initialization required.
        }

        /**
         * The vr value.
         */
        public VR vr;

    }

}
