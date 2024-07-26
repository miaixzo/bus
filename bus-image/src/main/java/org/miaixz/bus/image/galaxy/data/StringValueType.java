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

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.image.Builder;

import java.util.Date;
import java.util.TimeZone;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public enum StringValueType implements ValueType {

    ASCII(Symbol.BACKSLASH, null), STRING(Symbol.BACKSLASH, null) {
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
            return cs.toText(Builder.trimTrailing(s));
        }

        @Override
        protected Object toMultiValue(String s) {
            return s;
        }
    },
    UR(null, null) {
        @Override
        protected Object splitAndTrim(String s, SpecificCharacterSet cs) {
            return Builder.trimTrailing(s);
        }

        @Override
        protected Object toMultiValue(String s) {
            return s;
        }
    },
    DA(Symbol.BACKSLASH, TemporalType.DA), DT(Symbol.BACKSLASH, TemporalType.DT), TM(Symbol.BACKSLASH, TemporalType.TM),
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
        public String toString(Object val, boolean bigEndian, int valueIndex, String defVal) {

            if (val instanceof double[] ds) {
                return (valueIndex < ds.length && !Double.isNaN(ds[valueIndex])) ? Builder.formatDS(ds[valueIndex])
                        : defVal;
            }
            return super.toString(val, bigEndian, valueIndex, defVal);
        }

        @Override
        public Object toStrings(Object val, boolean bigEndian, SpecificCharacterSet cs) {

            return (val instanceof double[]) ? toStrings((double[]) val) : super.toStrings(val, bigEndian, cs);
        }

        private Object toStrings(double[] ds) {
            if (ds.length == 1)
                return Builder.formatDS(ds[0]);

            String[] ss = new String[ds.length];
            for (int i = 0; i < ds.length; i++)
                ss[i] = !Double.isNaN(ds[i]) ? Builder.formatDS(ds[i]) : Normal.EMPTY;

            return ss;
        }

        @Override
        public float toFloat(Object val, boolean bigEndian, int valueIndex, float defVal) {
            double[] ds = (double[]) val;
            return valueIndex < ds.length && !Double.isNaN(ds[valueIndex]) ? (float) ds[valueIndex] : defVal;
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
        public double toDouble(Object val, boolean bigEndian, int valueIndex, double defVal) {
            double[] ds = (double[]) val;
            return valueIndex < ds.length && !Double.isNaN(ds[valueIndex]) ? ds[valueIndex] : defVal;
        }

        @Override
        public double[] toDoubles(Object val, boolean bigEndian) {
            return (double[]) val;
        }

        @Override
        public Object toValue(float[] fs, boolean bigEndian) {
            if (fs == null || fs.length == 0)
                return Value.NULL;

            if (fs.length == 1)
                return Builder.formatDS(fs[0]);

            String[] ss = new String[fs.length];
            for (int i = 0; i < fs.length; i++)
                ss[i] = Builder.formatDS(fs[i]);
            return ss;
        }

        @Override
        public Object toValue(double[] ds, boolean bigEndian) {
            if (ds == null || ds.length == 0)
                return Value.NULL;

            return ds;
        }

        @Override
        public boolean prompt(Object val, boolean bigEndian, SpecificCharacterSet cs, int maxChars, StringBuilder sb) {
            if (val instanceof double[])
                val = toStrings((double[]) val);
            return super.prompt(val, bigEndian, cs, maxChars, sb);
        }
    },
    IS("\\", null) {
        @Override
        public boolean isIntValue() {
            return true;
        }

        @Override
        public byte[] toBytes(Object val, SpecificCharacterSet cs) {

            if (val instanceof long[])
                val = toStrings((long[]) val);
            return super.toBytes(val, cs);
        }

        @Override
        public String toString(Object val, boolean bigEndian, int valueIndex, String defVal) {

            if (val instanceof long[] ls) {
                return (valueIndex < ls.length && ls[valueIndex] != Integer.MIN_VALUE) ? Long.toString(ls[valueIndex])
                        : defVal;
            }
            return super.toString(val, bigEndian, valueIndex, defVal);
        }

        @Override
        public Object toStrings(Object val, boolean bigEndian, SpecificCharacterSet cs) {

            return (val instanceof long[]) ? toStrings((long[]) val) : super.toStrings(val, bigEndian, cs);
        }

        private Object toStrings(long[] ls) {
            if (ls.length == 1)
                return Long.toString(ls[0]);

            String[] ss = new String[ls.length];
            for (int i = 0; i < ls.length; i++)
                ss[i] = ls[i] != Integer.MIN_VALUE ? Long.toString(ls[i]) : "";

            return ss;
        }

        @Override
        public int toInt(Object val, boolean bigEndian, int valueIndex, int defVal) {
            return (int) toLong(val, bigEndian, valueIndex, defVal);
        }

        @Override
        public int[] toInts(Object val, boolean bigEndian) {
            return longsToInts((long[]) val);
        }

        @Override
        public Object toValue(int[] is, boolean bigEndian) {
            if (is == null || is.length == 0)
                return Value.NULL;

            return intsToLong(is);
        }

        @Override
        public long toLong(Object val, boolean bigEndian, int valueIndex, long defVal) {
            long[] is = (long[]) val;
            return valueIndex < is.length && is[valueIndex] != Integer.MIN_VALUE ? is[valueIndex] : defVal;
        }

        @Override
        public long[] toLongs(Object val, boolean bigEndian) {
            return (long[]) val;
        }

        @Override
        public Object toValue(long[] ls, boolean bigEndian) {
            if (ls == null || ls.length == 0)
                return Value.NULL;

            return ls;
        }

        @Override
        public boolean prompt(Object val, boolean bigEndian, SpecificCharacterSet cs, int maxChars, StringBuilder sb) {
            if (val instanceof long[])
                val = toStrings((long[]) val);
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

    static boolean prompt(String[] ss, int maxChars, StringBuilder sb) {
        int maxLength = sb.length() + maxChars;
        for (String s : ss) {
            if (s != null)
                sb.append(s);
            if (sb.length() > maxLength) {
                sb.setLength(maxLength + 1);
                return false;
            }
            sb.append('\\');
        }
        sb.setLength(sb.length() - 1);
        return true;
    }

    /**
     * ~170% faster than LongStream.of(in).mapToInt(l -> (int) l).toArray()
     */
    public static int[] longsToInts(long[] in) {
        int[] out = new int[in.length];
        for (int i = 0; i < in.length; i++)
            out[i] = (int) in[i];
        return out;
    }

    /**
     * ~60% faster than IntStream.of(in).asLongStream().toArray()
     */
    public static long[] intsToLong(int[] in) {
        long[] out = new long[in.length];
        for (int i = 0; i < in.length; i++)
            out[i] = in[i];
        return out;
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
        return temporalType != null;
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
            return cs(cs).encode(Builder.concat((String[]) val, '\\'), delimiters);

        throw new UnsupportedOperationException();
    }

    @Override
    public String toString(Object val, boolean bigEndian, int valueIndex, String defVal) {

        if (val instanceof String)
            return (String) (valueIndex == 0 ? val : defVal);

        if (val instanceof String[] ss) {
            return (valueIndex < ss.length && ss[valueIndex] != null && !ss[valueIndex].isEmpty()) ? ss[valueIndex]
                    : defVal;
        }

        throw new UnsupportedOperationException();
    }

    @Override
    public Object toStrings(Object val, boolean bigEndian, SpecificCharacterSet cs) {

        if (val instanceof byte[]) {
            return splitAndTrim(cs(cs).decode((byte[]) val), cs);
        }

        if (val instanceof String || val instanceof String[])
            return val;

        throw new UnsupportedOperationException();
    }

    protected Object splitAndTrim(String s, SpecificCharacterSet cs) {
        return Builder.splitAndTrim(s, '\\');
    }

    @Override
    public int toInt(Object val, boolean bigEndian, int valueIndex, int defVal) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int[] toInts(Object val, boolean bigEndian) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long toLong(Object val, boolean bigEndian, int valueIndex, long defVal) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long[] toLongs(Object val, boolean bigEndian) {
        throw new UnsupportedOperationException();
    }

    @Override
    public float toFloat(Object val, boolean bigEndian, int valueIndex, float defVal) {
        throw new UnsupportedOperationException();
    }

    @Override
    public float[] toFloats(Object val, boolean bigEndian) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double toDouble(Object val, boolean bigEndian, int valueIndex, double defVal) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double[] toDoubles(Object val, boolean bigEndian) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Date toDate(Object val, TimeZone tz, int valueIndex, boolean ceil, Date defVal, DatePrecision precision) {
        if (temporalType == null)
            throw new UnsupportedOperationException();

        if (val instanceof String) {
            return valueIndex == 0 ? temporalType.parse(tz, (String) val, ceil, precision) : defVal;
        }
        if (val instanceof String[] ss) {
            return (valueIndex < ss.length && ss[valueIndex] != null)
                    ? temporalType.parse(tz, ss[valueIndex], ceil, precision)
                    : defVal;
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public Date[] toDate(Object val, TimeZone tz, boolean ceil, DatePrecision precision) {
        if (temporalType == null)
            throw new UnsupportedOperationException();

        if (val instanceof String) {
            precision.precisions = new DatePrecision[1];
            return new Date[] {
                    temporalType.parse(tz, (String) val, ceil, precision.precisions[0] = new DatePrecision()) };
        }
        if (val instanceof String[] ss) {
            Date[] is = new Date[ss.length];
            precision.precisions = new DatePrecision[ss.length];
            for (int i = 0; i < is.length; i++) {
                if (ss[i] != null) {
                    is[i] = temporalType.parse(tz, ss[i], ceil, precision.precisions[i] = new DatePrecision());
                }
            }
            return is;
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public Object toValue(byte[] b) {
        return b != null && b.length > 0 ? b : Value.NULL;
    }

    @Override
    public Object toValue(String s, boolean bigEndian) {
        if (s == null || s.isEmpty())
            return Value.NULL;

        return toMultiValue(s);
    }

    protected Object toMultiValue(String s) {
        return Builder.splitAndTrim(s, Symbol.C_BACKSLASH);
    }

    @Override
    public Object toValue(String[] ss, boolean bigEndian) {
        if (ss == null || ss.length == 0)
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
    public Object toValue(long[] ls, boolean bigEndian) {
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
        if (temporalType == null)
            throw new UnsupportedOperationException();

        if (ds == null || ds.length == 0)
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
    public boolean prompt(Object val, boolean bigEndian, SpecificCharacterSet cs, int maxChars, StringBuilder sb) {
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

        if (val instanceof String[] ss) {
            return ss.length;
        }

        throw new UnsupportedOperationException();
    }

}
