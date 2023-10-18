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
package org.aoju.bus.image.galaxy.data;

import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.image.galaxy.Property;

import java.util.Date;
import java.util.TimeZone;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public enum StringValueType implements ValueType {

    ASCII(Symbol.BACKSLASH, null),
    STRING(Symbol.BACKSLASH, null) {
        @Override
        public boolean useSpecificCharacterSet() {
            return true;
        }

        @Override
        protected SpecificCharacterSet cs(SpecificCharacterSet cs) {
            return cs;
        }
    },
    TEXT("\t\n\f\r", null) {
        @Override
        public boolean useSpecificCharacterSet() {
            return true;
        }

        @Override
        protected SpecificCharacterSet cs(SpecificCharacterSet cs) {
            return cs;
        }

        @Override
        protected Object splitAndTrim(String s, SpecificCharacterSet cs) {
            return cs.toText(Property.trimTrailing(s));
        }

        @Override
        protected Object toMultiValue(String s) {
            return s;
        }
    },
    UR(null, null) {
        @Override
        protected Object splitAndTrim(String s, SpecificCharacterSet cs) {
            return Property.trimTrailing(s);
        }

        @Override
        protected Object toMultiValue(String s) {
            return s;
        }
    },
    DA(Symbol.BACKSLASH, TemporalType.DA),
    DT(Symbol.BACKSLASH, TemporalType.DT),
    TM(Symbol.BACKSLASH, TemporalType.TM),
    PN("^=\\", null) {
        @Override
        public boolean useSpecificCharacterSet() {
            return true;
        }

        @Override
        protected SpecificCharacterSet cs(SpecificCharacterSet cs) {
            return cs;
        }
    },
    DS(Symbol.BACKSLASH, null) {
        @Override
        public byte[] toBytes(Object val, SpecificCharacterSet cs) {

            if (val instanceof double[])
                val = toStrings((double[]) val);
            return super.toBytes(val, cs);
        }

        @Override
        public String toString(Object val, boolean bigEndian, int valueIndex,
                               String defVal) {

            if (val instanceof double[]) {
                double[] ds = (double[]) val;
                return (valueIndex < ds.length
                        && !Double.isNaN(ds[valueIndex]))
                        ? Property.formatDS(ds[valueIndex])
                        : defVal;
            }
            return super.toString(val, bigEndian, valueIndex, defVal);
        }

        @Override
        public Object toStrings(Object val, boolean bigEndian,
                                SpecificCharacterSet cs) {

            return (val instanceof double[])
                    ? toStrings((double[]) val)
                    : super.toStrings(val, bigEndian, cs);
        }

        private Object toStrings(double[] ds) {
            if (ds.length == 1)
                return Property.formatDS(ds[0]);

            String[] ss = new String[ds.length];
            for (int i = 0; i < ds.length; i++)
                ss[i] = !Double.isNaN(ds[i]) ? Property.formatDS(ds[i]) : Normal.EMPTY;

            return ss;
        }

        @Override
        public float toFloat(Object val, boolean bigEndian, int valueIndex,
                             float defVal) {
            double[] ds = (double[]) val;
            return valueIndex < ds.length && !Double.isNaN(ds[valueIndex])
                    ? (float) ds[valueIndex]
                    : defVal;
        }

        @Override
        public float[] toFloats(Object val, boolean bigEndian) {
            double[] ds = (double[]) val;
            float[] fs = new float[ds.length];
            for (int i = 0; i < fs.length; i++)
                fs[i] = (float) ds[i];
            return fs;
        }

        @Override
        public double toDouble(Object val, boolean bigEndian, int valueIndex,
                               double defVal) {
            double[] ds = (double[]) val;
            return valueIndex < ds.length && !Double.isNaN(ds[valueIndex])
                    ? ds[valueIndex]
                    : defVal;
        }

        @Override
        public double[] toDoubles(Object val, boolean bigEndian) {
            return (double[]) val;
        }

        @Override
        public Object toValue(float[] fs, boolean bigEndian) {
            if (null == fs || fs.length == 0)
                return Value.NULL;

            if (fs.length == 1)
                return Property.formatDS(fs[0]);

            String[] ss = new String[fs.length];
            for (int i = 0; i < fs.length; i++)
                ss[i] = Property.formatDS(fs[i]);
            return ss;
        }

        @Override
        public Object toValue(double[] ds, boolean bigEndian) {
            if (null == ds || ds.length == 0)
                return Value.NULL;

            return ds;
        }

        @Override
        public boolean prompt(Object val, boolean bigEndian,
                              SpecificCharacterSet cs, int maxChars, StringBuilder sb) {
            if (val instanceof double[])
                val = toStrings((double[]) val);
            return super.prompt(val, bigEndian, cs, maxChars, sb);
        }
    },
    IS(Symbol.BACKSLASH, null) {
        @Override
        public boolean isIntValue() {
            return true;
        }

        @Override
        public byte[] toBytes(Object val, SpecificCharacterSet cs) {

            if (val instanceof int[])
                val = toStrings((int[]) val);
            return super.toBytes(val, cs);
        }

        @Override
        public String toString(Object val, boolean bigEndian, int valueIndex,
                               String defVal) {

            if (val instanceof int[]) {
                int[] is = (int[]) val;
                return (valueIndex < is.length
                        && is[valueIndex] != Integer.MIN_VALUE)
                        ? Integer.toString(is[valueIndex])
                        : defVal;
            }
            return super.toString(val, bigEndian, valueIndex, defVal);
        }

        @Override
        public Object toStrings(Object val, boolean bigEndian,
                                SpecificCharacterSet cs) {

            return (val instanceof int[])
                    ? toStrings((int[]) val)
                    : super.toStrings(val, bigEndian, cs);
        }

        private Object toStrings(int[] is) {
            if (is.length == 1)
                return Integer.toString(is[0]);

            String[] ss = new String[is.length];
            for (int i = 0; i < is.length; i++)
                ss[i] = is[i] != Integer.MIN_VALUE ? Integer.toString(is[i]) : Normal.EMPTY;

            return ss;
        }

        @Override
        public int toInt(Object val, boolean bigEndian, int valueIndex,
                         int defVal) {
            int[] is = (int[]) val;
            return valueIndex < is.length && is[valueIndex] != Integer.MIN_VALUE
                    ? is[valueIndex]
                    : defVal;
        }

        @Override
        public int[] toInts(Object val, boolean bigEndian) {
            return (int[]) val;
        }

        @Override
        public Object toValue(int[] is, boolean bigEndian) {
            if (null == is || is.length == 0)
                return Value.NULL;

            return is;
        }

        @Override
        public boolean prompt(Object val, boolean bigEndian,
                              SpecificCharacterSet cs, int maxChars, StringBuilder sb) {
            if (val instanceof int[])
                val = toStrings((int[]) val);
            return super.prompt(val, bigEndian, cs, maxChars, sb);
        }
    };

    final String delimiters;
    final TemporalType temporalType;

    StringValueType(String delimiters, TemporalType temperalType) {
        this.delimiters = delimiters;
        this.temporalType = temperalType;
    }

    static boolean prompt(String s, int maxChars, StringBuilder sb) {
        int maxLength = sb.length() + maxChars;
        sb.append(s.trim());
        if (sb.length() > maxLength) {
            sb.setLength(maxLength + 1);
            return false;
        }
        return true;
    }

    static boolean prompt(String[] ss, int maxChars,
                          StringBuilder sb) {
        int maxLength = sb.length() + maxChars;
        for (String s : ss) {
            if (null != s)
                sb.append(s);
            if (sb.length() > maxLength) {
                sb.setLength(maxLength + 1);
                return false;
            }
            sb.append(Symbol.C_BACKSLASH);
        }
        sb.setLength(sb.length() - 1);
        return true;
    }

    @Override
    public boolean isStringValue() {
        return true;
    }

    @Override
    public boolean isIntValue() {
        return false;
    }

    @Override
    public boolean isTemporalType() {
        return null != temporalType;
    }

    @Override
    public int numEndianBytes() {
        return 1;
    }

    @Override
    public byte[] toggleEndian(byte[] b, boolean preserve) {
        return b;
    }

    @Override
    public boolean useSpecificCharacterSet() {
        return false;
    }

    protected SpecificCharacterSet cs(SpecificCharacterSet cs) {
        return SpecificCharacterSet.ASCII;
    }

    @Override
    public byte[] toBytes(Object val, SpecificCharacterSet cs) {

        if (val instanceof byte[])
            return (byte[]) val;

        if (val instanceof String)
            return cs(cs).encode((String) val, delimiters);

        if (val instanceof String[])
            return cs(cs).encode(
                    Property.concat((String[]) val, Symbol.C_BACKSLASH), delimiters);

        throw new UnsupportedOperationException();
    }

    @Override
    public String toString(Object val, boolean bigEndian, int valueIndex,
                           String defVal) {

        if (val instanceof String)
            return (String) (valueIndex == 0 ? val : defVal);

        if (val instanceof String[]) {
            String[] ss = (String[]) val;
            return (valueIndex < ss.length && null != ss[valueIndex] && !ss[valueIndex].isEmpty())
                    ? ss[valueIndex]
                    : defVal;
        }

        throw new UnsupportedOperationException();
    }

    @Override
    public Object toStrings(Object val, boolean bigEndian,
                            SpecificCharacterSet cs) {

        if (val instanceof byte[]) {
            return splitAndTrim(cs(cs).decode((byte[]) val), cs);
        }

        if (val instanceof String
                || val instanceof String[])
            return val;

        throw new UnsupportedOperationException();
    }

    protected Object splitAndTrim(String s, SpecificCharacterSet cs) {
        return Property.splitAndTrim(s, Symbol.C_BACKSLASH);
    }

    @Override
    public int toInt(Object val, boolean bigEndian, int valueIndex,
                     int defVal) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int[] toInts(Object val, boolean bigEndian) {
        throw new UnsupportedOperationException();
    }

    @Override
    public float toFloat(Object val, boolean bigEndian, int valueIndex,
                         float defVal) {
        throw new UnsupportedOperationException();
    }

    @Override
    public float[] toFloats(Object val, boolean bigEndian) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double toDouble(Object val, boolean bigEndian, int valueIndex,
                           double defVal) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double[] toDoubles(Object val, boolean bigEndian) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Date toDate(Object val, TimeZone tz, int valueIndex, boolean ceil,
                       Date defVal, DatePrecision precision) {
        if (null == temporalType)
            throw new UnsupportedOperationException();

        if (val instanceof String) {
            return valueIndex == 0
                    ? temporalType.parse(tz, (String) val, ceil, precision)
                    : defVal;
        }
        if (val instanceof String[]) {
            String[] ss = (String[]) val;
            return (valueIndex < ss.length && null != ss[valueIndex])
                    ? temporalType.parse(tz, ss[valueIndex], ceil, precision)
                    : defVal;
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public Date[] toDate(Object val, TimeZone tz, boolean ceil,
                         DatePrecision precisions) {
        if (null == temporalType)
            throw new UnsupportedOperationException();

        if (val instanceof String) {
            precisions.precisions = new DatePrecision[1];
            return new Date[]{temporalType.parse(tz, (String) val, ceil,
                    precisions.precisions[0] = new DatePrecision())};
        }
        if (val instanceof String[]) {
            String[] ss = (String[]) val;
            Date[] is = new Date[ss.length];
            precisions.precisions = new DatePrecision[ss.length];
            for (int i = 0; i < is.length; i++) {
                if (null != ss[i]) {
                    is[i] = temporalType.parse(tz, ss[i], ceil,
                            precisions.precisions[i] = new DatePrecision());
                }
            }
            return is;
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public Object toValue(byte[] b) {
        return null != b && b.length > 0 ? b : Value.NULL;
    }

    @Override
    public Object toValue(String s, boolean bigEndian) {
        if (null == s || s.isEmpty())
            return Value.NULL;

        return toMultiValue(s);
    }

    protected Object toMultiValue(String s) {
        return Property.splitAndTrim(s, Symbol.C_BACKSLASH);
    }

    @Override
    public Object toValue(String[] ss, boolean bigEndian) {
        if (null == ss || ss.length == 0)
            return Value.NULL;

        if (ss.length == 1)
            return toValue(ss[0], bigEndian);

        return ss;
    }

    @Override
    public Object toValue(int[] is, boolean bigEndian) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object toValue(float[] fs, boolean bigEndian) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object toValue(double[] ds, boolean bigEndian) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object toValue(Date[] ds, TimeZone tz, DatePrecision precision) {
        if (null == temporalType)
            throw new UnsupportedOperationException();

        if (null == ds || ds.length == 0)
            return Value.NULL;

        if (ds.length == 1)
            return temporalType.format(tz, ds[0], precision);

        String[] ss = new String[ds.length];
        for (int i = 0; i < ss.length; i++) {
            ss[i] = temporalType.format(tz, ds[i], precision);
        }
        return ss;
    }

    @Override
    public boolean prompt(Object val, boolean bigEndian,
                          SpecificCharacterSet cs, int maxChars, StringBuilder sb) {
        if (val instanceof byte[])
            return prompt(cs(cs).decode((byte[]) val), maxChars, sb);

        if (val instanceof String)
            return prompt((String) val, maxChars, sb);

        if (val instanceof String[])
            return prompt((String[]) val, maxChars, sb);

        return prompt(val.toString(), maxChars, sb);
    }

    @Override
    public int vmOf(Object val) {
        if (val instanceof String)
            return 1;

        if (val instanceof String[]) {
            String[] ss = (String[]) val;
            return ss.length;
        }

        throw new UnsupportedOperationException();
    }

}
