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
 * Defines the ValueType contract.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
interface ValueType {

    /**
     * Determines whether string value.
     *
     * @return true if the condition is met; otherwise false.
     */
    boolean isStringValue();

    /**
     * Executes the use specific character set operation.
     *
     * @return true if the condition is met; otherwise false.
     */
    boolean useSpecificCharacterSet();

    /**
     * Determines whether int value.
     *
     * @return true if the condition is met; otherwise false.
     */
    boolean isIntValue();

    /**
     * Determines whether temporal type.
     *
     * @return true if the condition is met; otherwise false.
     */
    boolean isTemporalType();

    /**
     * Executes the num endian bytes operation.
     *
     * @return the operation result.
     */
    int numEndianBytes();

    /**
     * Converts this value to ggle endian.
     *
     * @param b        the b.
     * @param preserve the preserve.
     * @return the operation result.
     */
    byte[] toggleEndian(byte[] b, boolean preserve);

    /**
     * Converts this value to bytes.
     *
     * @param val the val.
     * @param cs  the cs.
     * @return the operation result.
     */
    byte[] toBytes(Object val, SpecificCharacterSet cs);

    /**
     * Returns the string representation.
     *
     * @param val        the val.
     * @param bigEndian  the big endian.
     * @param valueIndex the value index.
     * @param defVal     the def val.
     * @return the string representation.
     */
    String toString(Object val, boolean bigEndian, int valueIndex, String defVal);

    /**
     * Converts this value to strings.
     *
     * @param val       the val.
     * @param bigEndian the big endian.
     * @param cs        the cs.
     * @return the operation result.
     */
    Object toStrings(Object val, boolean bigEndian, SpecificCharacterSet cs);

    /**
     * Converts this value to int.
     *
     * @param val        the val.
     * @param bigEndian  the big endian.
     * @param valueIndex the value index.
     * @param defVal     the def val.
     * @return the operation result.
     */
    int toInt(Object val, boolean bigEndian, int valueIndex, int defVal);

    /**
     * Converts this value to ints.
     *
     * @param val       the val.
     * @param bigEndian the big endian.
     * @return the operation result.
     */
    int[] toInts(Object val, boolean bigEndian);

    /**
     * Converts this value to long.
     *
     * @param val        the val.
     * @param bigEndian  the big endian.
     * @param valueIndex the value index.
     * @param defVal     the def val.
     * @return the operation result.
     */
    long toLong(Object val, boolean bigEndian, int valueIndex, long defVal);

    /**
     * Converts this value to longs.
     *
     * @param val       the val.
     * @param bigEndian the big endian.
     * @return the operation result.
     */
    long[] toLongs(Object val, boolean bigEndian);

    /**
     * Converts this value to float.
     *
     * @param val        the val.
     * @param bigEndian  the big endian.
     * @param valueIndex the value index.
     * @param defVal     the def val.
     * @return the operation result.
     */
    float toFloat(Object val, boolean bigEndian, int valueIndex, float defVal);

    /**
     * Converts this value to floats.
     *
     * @param val       the val.
     * @param bigEndian the big endian.
     * @return the operation result.
     */
    float[] toFloats(Object val, boolean bigEndian);

    /**
     * Converts this value to double.
     *
     * @param val        the val.
     * @param bigEndian  the big endian.
     * @param valueIndex the value index.
     * @param defVal     the def val.
     * @return the operation result.
     */
    double toDouble(Object val, boolean bigEndian, int valueIndex, double defVal);

    /**
     * Converts this value to doubles.
     *
     * @param val       the val.
     * @param bigEndian the big endian.
     * @return the operation result.
     */
    double[] toDoubles(Object val, boolean bigEndian);

    /**
     * Converts this value to temporal.
     *
     * @param val        the val.
     * @param valueIndex the value index.
     * @param precision  the precision.
     * @return the operation result.
     */
    Temporal toTemporal(Object val, int valueIndex, DatePrecision precision);

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
    Date toDate(Object val, TimeZone tz, int valueIndex, boolean ceil, Date defVal, DatePrecision precision);

    /**
     * Converts this value to date.
     *
     * @param val       the val.
     * @param tz        the tz.
     * @param ceil      the ceil.
     * @param precision the precision.
     * @return the operation result.
     */
    Date[] toDate(Object val, TimeZone tz, boolean ceil, DatePrecision precision);

    /**
     * Converts this value to value.
     *
     * @param b the b.
     * @return the operation result.
     */
    Object toValue(byte[] b);

    /**
     * Converts this value to value.
     *
     * @param s         the s.
     * @param bigEndian the big endian.
     * @return the operation result.
     */
    Object toValue(String s, boolean bigEndian);

    /**
     * Converts this value to value.
     *
     * @param ss        the ss.
     * @param bigEndian the big endian.
     * @return the operation result.
     */
    Object toValue(String[] ss, boolean bigEndian);

    /**
     * Converts this value to value.
     *
     * @param is        the is.
     * @param bigEndian the big endian.
     * @return the operation result.
     */
    Object toValue(int[] is, boolean bigEndian);

    /**
     * Converts this value to value.
     *
     * @param is        the is.
     * @param bigEndian the big endian.
     * @return the operation result.
     */
    Object toValue(long[] is, boolean bigEndian);

    /**
     * Converts this value to value.
     *
     * @param fs        the fs.
     * @param bigEndian the big endian.
     * @return the operation result.
     */
    Object toValue(float[] fs, boolean bigEndian);

    /**
     * Converts this value to value.
     *
     * @param ds        the ds.
     * @param bigEndian the big endian.
     * @return the operation result.
     */
    Object toValue(double[] ds, boolean bigEndian);

    /**
     * Converts this value to value.
     *
     * @param ds        the ds.
     * @param tz        the tz.
     * @param precision the precision.
     * @return the operation result.
     */
    Object toValue(Date[] ds, TimeZone tz, DatePrecision precision);

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
    boolean prompt(Object val, boolean bigEndian, SpecificCharacterSet cs, int maxChars, StringBuilder sb);

    /**
     * Executes the vm of operation.
     *
     * @param val the val.
     * @return the operation result.
     */
    int vmOf(Object val);

}
