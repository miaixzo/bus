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
 * @author Kimi Liu
 * @since Java 21+
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

    Temporal toTemporal(Object val, int valueIndex, DatePrecision precision);

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
