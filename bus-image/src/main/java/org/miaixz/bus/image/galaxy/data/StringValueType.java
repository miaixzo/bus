/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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

import java.time.temporal.Temporal;
import java.util.Date;
import java.util.TimeZone;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.image.Builder;

/**
 * 字符串值类型枚举，实现了ValueType接口，用于处理DICOM中的各种字符串类型值。 该枚举定义了多种字符串类型，如ASCII、STRING、TEXT、UR、DA、DT、TM、PN、DS和IS等，
 * 每种类型都有其特定的处理方式和分隔符。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum StringValueType implements ValueType {

    /**
     * ASCII字符串类型，使用反斜杠作为分隔符
     */
    ASCII(Symbol.BACKSLASH, null),

    /**
     * 字符串类型，使用反斜杠作为分隔符，使用特定字符集
     */
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

    /**
     * 文本类型，使用制表符、换行符、换页符和回车符作为分隔符，使用特定字符集
     */
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

    /**
     * URI类型，不使用分隔符
     */
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

    /**
     * 日期类型，使用反斜杠作为分隔符
     */
    DA(Symbol.BACKSLASH, TemporalType.DA),

    /**
     * 日期时间类型，使用反斜杠作为分隔符
     */
    DT(Symbol.BACKSLASH, TemporalType.DT),

    /**
     * 时间类型，使用反斜杠作为分隔符
     */
    TM(Symbol.BACKSLASH, TemporalType.TM),

    /**
     * 个人名称类型，使用^、=和\作为分隔符，使用特定字符集
     */
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

    /**
     * 十进制字符串类型，使用反斜杠作为分隔符
     */
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

    /**
     * 整数字符串类型，使用反斜杠作为分隔符
     */
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

    /**
     * 分隔符
     */
    final String delimiters;

    /**
     * 时间类型
     */
    final TemporalType temporalType;

    /**
     * 构造一个字符串值类型
     *
     * @param delimiters   分隔符
     * @param temperalType 时间类型
     */
    StringValueType(String delimiters, TemporalType temperalType) {
        this.delimiters = delimiters;
        this.temporalType = temperalType;
    }

    /**
     * 将字符串追加到字符串构建器，限制最大字符数
     *
     * @param s        字符串
     * @param maxChars 最大字符数
     * @param sb       字符串构建器
     * @return 如果未超过最大字符数则返回true，否则返回false
     */
    static boolean prompt(String s, int maxChars, StringBuilder sb) {
        int maxLength = sb.length() + maxChars;
        sb.append(s.trim());
        if (sb.length() > maxLength) {
            sb.setLength(maxLength + 1);
            return false;
        }
        return true;
    }

    /**
     * 将字符串数组追加到字符串构建器，限制最大字符数
     *
     * @param ss       字符串数组
     * @param maxChars 最大字符数
     * @param sb       字符串构建器
     * @return 如果未超过最大字符数则返回true，否则返回false
     */
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
     * 将long数组转换为int数组 比LongStream.of(in).mapToInt(l -> (int) l).toArray()快约170%
     *
     * @param in long数组
     * @return int数组
     */
    public static int[] longsToInts(long[] in) {
        int[] out = new int[in.length];
        for (int i = 0; i < in.length; i++)
            out[i] = (int) in[i];
        return out;
    }

    /**
     * 将int数组转换为long数组 比IntStream.of(in).asLongStream().toArray()快约60%
     *
     * @param in int数组
     * @return long数组
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

    /**
     * 获取特定字符集
     *
     * @param cs 特定字符集
     * @return 特定字符集
     */
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
            return splitAndTrim(cs(cs).decode((byte[]) val, delimiters), cs);
        }
        if (val instanceof String || val instanceof String[])
            return val;
        throw new UnsupportedOperationException();
    }

    /**
     * 分割并修剪字符串
     *
     * @param s  字符串
     * @param cs 特定字符集
     * @return 分割并修剪后的字符串或字符串数组
     */
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
    public Temporal toTemporal(Object val, int valueIndex, DatePrecision precision) {
        if (temporalType == null)
            throw new UnsupportedOperationException();
        if (val instanceof String) {
            return valueIndex == 0 ? temporalType.parseTemporal((String) val, precision) : null;
        }
        if (val instanceof String[]) {
            String[] ss = (String[]) val;
            return (valueIndex < ss.length && ss[valueIndex] != null)
                    ? temporalType.parseTemporal(ss[valueIndex], precision)
                    : null;
        }
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

    /**
     * 将字符串转换为多值对象
     *
     * @param s 字符串
     * @return 多值对象
     */
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
            return prompt(cs(cs).decode((byte[]) val, delimiters), maxChars, sb);
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
