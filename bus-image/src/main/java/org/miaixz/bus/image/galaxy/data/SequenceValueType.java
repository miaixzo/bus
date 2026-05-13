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

/**
 * Defines the SequenceValueType values.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
enum SequenceValueType implements ValueType {

    /**
     * The sq value.
     */
    SQ;

    /**
     * Determines whether string value.
     *
     * @return true if the condition is met; otherwise false.
     */
    @Override
    public boolean isStringValue() {
        return false;
    }

    /**
     * Executes the use specific character set operation.
     *
     * @return true if the condition is met; otherwise false.
     */
    @Override
    public boolean useSpecificCharacterSet() {
        return false;
    }

    /**
     * Determines whether int value.
     *
     * @return true if the condition is met; otherwise false.
     */
    @Override
    public boolean isIntValue() {
        return false;
    }

    /**
     * Determines whether temporal type.
     *
     * @return true if the condition is met; otherwise false.
     */
    @Override
    public boolean isTemporalType() {
        return false;
    }

    /**
     * Executes the num endian bytes operation.
     *
     * @return the operation result.
     */
    @Override
    public int numEndianBytes() {
        throw new UnsupportedOperationException();
    }

    /**
     * Converts this value to ggle endian.
     *
     * @param b        the b.
     * @param preserve the preserve.
     * @return the operation result.
     */
    @Override
    public byte[] toggleEndian(byte[] b, boolean preserve) {
        throw new UnsupportedOperationException();
    }

    /**
     * Converts this value to bytes.
     *
     * @param val the val.
     * @param cs  the cs.
     * @return the operation result.
     */
    @Override
    public byte[] toBytes(Object val, SpecificCharacterSet cs) {
        throw new UnsupportedOperationException();
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
    @Override
    public String toString(Object val, boolean bigEndian, int valueIndex, String defVal) {
        throw new UnsupportedOperationException();
    }

    /**
     * Converts this value to strings.
     *
     * @param val       the val.
     * @param bigEndian the big endian.
     * @param cs        the cs.
     * @return the operation result.
     */
    @Override
    public Object toStrings(Object val, boolean bigEndian, SpecificCharacterSet cs) {
        throw new UnsupportedOperationException();
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
    @Override
    public int toInt(Object val, boolean bigEndian, int valueIndex, int defVal) {
        throw new UnsupportedOperationException();
    }

    /**
     * Converts this value to ints.
     *
     * @param val       the val.
     * @param bigEndian the big endian.
     * @return the operation result.
     */
    @Override
    public int[] toInts(Object val, boolean bigEndian) {
        throw new UnsupportedOperationException();
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
    @Override
    public long toLong(Object val, boolean bigEndian, int valueIndex, long defVal) {
        throw new UnsupportedOperationException();
    }

    /**
     * Converts this value to longs.
     *
     * @param val       the val.
     * @param bigEndian the big endian.
     * @return the operation result.
     */
    @Override
    public long[] toLongs(Object val, boolean bigEndian) {
        throw new UnsupportedOperationException();
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
    @Override
    public float toFloat(Object val, boolean bigEndian, int valueIndex, float defVal) {
        throw new UnsupportedOperationException();
    }

    /**
     * Converts this value to floats.
     *
     * @param val       the val.
     * @param bigEndian the big endian.
     * @return the operation result.
     */
    @Override
    public float[] toFloats(Object val, boolean bigEndian) {
        throw new UnsupportedOperationException();
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
    @Override
    public double toDouble(Object val, boolean bigEndian, int valueIndex, double defVal) {
        throw new UnsupportedOperationException();
    }

    /**
     * Converts this value to doubles.
     *
     * @param val       the val.
     * @param bigEndian the big endian.
     * @return the operation result.
     */
    @Override
    public double[] toDoubles(Object val, boolean bigEndian) {
        throw new UnsupportedOperationException();
    }

    /**
     * Converts this value to temporal.
     *
     * @param val        the val.
     * @param valueIndex the value index.
     * @param precision  the precision.
     * @return the operation result.
     */
    @Override
    public Temporal toTemporal(Object val, int valueIndex, DatePrecision precision) {
        throw new UnsupportedOperationException();
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
    @Override
    public Date toDate(Object val, TimeZone tz, int valueIndex, boolean ceil, Date defVal, DatePrecision precision) {
        throw new UnsupportedOperationException();
    }

    /**
     * Converts this value to date.
     *
     * @param val       the val.
     * @param tz        the tz.
     * @param ceil      the ceil.
     * @param precision the precision.
     * @return the operation result.
     */
    @Override
    public Date[] toDate(Object val, TimeZone tz, boolean ceil, DatePrecision precision) {
        throw new UnsupportedOperationException();
    }

    /**
     * Converts this value to value.
     *
     * @param b the b.
     * @return the operation result.
     */
    @Override
    public Object toValue(byte[] b) {
        throw new UnsupportedOperationException();
    }

    /**
     * Converts this value to value.
     *
     * @param s         the s.
     * @param bigEndian the big endian.
     * @return the operation result.
     */
    @Override
    public Object toValue(String s, boolean bigEndian) {
        throw new UnsupportedOperationException();
    }

    /**
     * Converts this value to value.
     *
     * @param ss        the ss.
     * @param bigEndian the big endian.
     * @return the operation result.
     */
    @Override
    public Object toValue(String[] ss, boolean bigEndian) {
        throw new UnsupportedOperationException();
    }

    /**
     * Converts this value to value.
     *
     * @param is        the is.
     * @param bigEndian the big endian.
     * @return the operation result.
     */
    @Override
    public Object toValue(int[] is, boolean bigEndian) {
        throw new UnsupportedOperationException();
    }

    /**
     * Converts this value to value.
     *
     * @param ls        the ls.
     * @param bigEndian the big endian.
     * @return the operation result.
     */
    @Override
    public Object toValue(long[] ls, boolean bigEndian) {
        throw new UnsupportedOperationException();
    }

    /**
     * Converts this value to value.
     *
     * @param fs        the fs.
     * @param bigEndian the big endian.
     * @return the operation result.
     */
    @Override
    public Object toValue(float[] fs, boolean bigEndian) {
        throw new UnsupportedOperationException();
    }

    /**
     * Converts this value to value.
     *
     * @param ds        the ds.
     * @param bigEndian the big endian.
     * @return the operation result.
     */
    @Override
    public Object toValue(double[] ds, boolean bigEndian) {
        throw new UnsupportedOperationException();
    }

    /**
     * Converts this value to value.
     *
     * @param ds        the ds.
     * @param tz        the tz.
     * @param precision the precision.
     * @return the operation result.
     */
    @Override
    public Object toValue(Date[] ds, TimeZone tz, DatePrecision precision) {
        throw new UnsupportedOperationException();
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
    @Override
    public boolean prompt(Object val, boolean bigEndian, SpecificCharacterSet cs, int maxChars, StringBuilder sb) {
        sb.append(val);
        return true;
    }

    /**
     * Executes the vm of operation.
     *
     * @param val the val.
     * @return the operation result.
     */
    @Override
    public int vmOf(Object val) {
        return 1;
    }

}
