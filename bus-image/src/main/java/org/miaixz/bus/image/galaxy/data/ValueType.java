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

import java.util.Date;
import java.util.TimeZone;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
interface ValueType {

    boolean isStringValue();

    boolean useSpecificCharacterSet();

    boolean isIntValue();

    boolean isTemporalType();

    int numEndianBytes();

    byte[] toggleEndian(byte[] b, boolean preserve);

    byte[] toBytes(Object val, SpecificCharacterSet cs);

    String toString(Object val, boolean bigEndian, int valueIndex, String defVal);

    Object toStrings(Object val, boolean bigEndian, SpecificCharacterSet cs);

    int toInt(Object val, boolean bigEndian, int valueIndex, int defVal);

    int[] toInts(Object val, boolean bigEndian);

    long toLong(Object val, boolean bigEndian, int valueIndex, long defVal);

    long[] toLongs(Object val, boolean bigEndian);

    float toFloat(Object val, boolean bigEndian, int valueIndex, float defVal);

    float[] toFloats(Object val, boolean bigEndian);

    double toDouble(Object val, boolean bigEndian, int valueIndex, double defVal);

    double[] toDoubles(Object val, boolean bigEndian);

    Date toDate(Object val, TimeZone tz, int valueIndex, boolean ceil, Date defVal, DatePrecision precision);

    Date[] toDate(Object val, TimeZone tz, boolean ceil, DatePrecision precision);

    Object toValue(byte[] b);

    Object toValue(String s, boolean bigEndian);

    Object toValue(String[] ss, boolean bigEndian);

    Object toValue(int[] is, boolean bigEndian);

    Object toValue(long[] is, boolean bigEndian);

    Object toValue(float[] fs, boolean bigEndian);

    Object toValue(double[] ds, boolean bigEndian);

    Object toValue(Date[] ds, TimeZone tz, DatePrecision precision);

    boolean prompt(Object val, boolean bigEndian, SpecificCharacterSet cs, int maxChars, StringBuilder sb);

    int vmOf(Object val);

}
