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

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.ByteKit;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.Property;

import java.util.Date;
import java.util.TimeZone;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public enum BinaryValueType implements ValueType {

    BYTE(1, 1) {
        @Override
        public byte[] toggleEndian(byte[] b, boolean preserve) {
            return b;
        }

        @Override
        protected int toInt(byte[] b, int off, boolean bigEndian) {
            return b[off];
        }

        @Override
        protected byte[] toBytes(int i, byte[] b, int off, boolean bigEndian) {
            b[off] = (byte) i;
            return b;
        }
    },
    SHORT(2, 2) {
        @Override
        public boolean isIntValue() {
            return true;
        }

        @Override
        public byte[] toggleEndian(byte[] b, boolean preserve) {
            return ByteKit.swapShorts(preserve ? b.clone() : b, 0, b.length);
        }

        @Override
        protected int toInt(byte[] b, int off, boolean bigEndian) {
            return ByteKit.bytesToShort(b, off, bigEndian);
        }

        @Override
        protected byte[] toBytes(int i, byte[] b, int off, boolean bigEndian) {
            return ByteKit.shortToBytes(i, b, off, bigEndian);
        }
    },
    USHORT(2, 2) {
        @Override
        public boolean isIntValue() {
            return true;
        }

        @Override
        public byte[] toggleEndian(byte[] b, boolean preserve) {
            return ByteKit.swapShorts(preserve ? b.clone() : b, 0, b.length);
        }

        @Override
        protected int toInt(byte[] b, int off, boolean bigEndian) {
            return ByteKit.bytesToUShort(b, off, bigEndian);
        }

        @Override
        protected byte[] toBytes(int i, byte[] b, int off, boolean bigEndian) {
            return ByteKit.shortToBytes(i, b, off, bigEndian);
        }
    },
    INT(4, 4) {
        @Override
        public boolean isIntValue() {
            return true;
        }

        @Override
        public byte[] toggleEndian(byte[] b, boolean preserve) {
            return ByteKit.swapInts(preserve ? b.clone() : b, 0, b.length);
        }

        @Override
        protected int toInt(byte[] b, int off, boolean bigEndian) {
            return ByteKit.bytesToInt(b, off, bigEndian);
        }

        @Override
        protected byte[] toBytes(int i, byte[] b, int off, boolean bigEndian) {
            return ByteKit.intToBytes(i, b, off, bigEndian);
        }
    },
    UINT(4, 4) {
        @Override
        public boolean isIntValue() {
            return true;
        }

        @Override
        public byte[] toggleEndian(byte[] b, boolean preserve) {
            return ByteKit.swapInts(preserve ? b.clone() : b, 0, b.length);
        }

        @Override
        protected int toInt(byte[] b, int off, boolean bigEndian) {
            return ByteKit.bytesToInt(b, off, bigEndian);
        }

        @Override
        protected byte[] toBytes(int i, byte[] b, int off, boolean bigEndian) {
            return ByteKit.intToBytes(i, b, off, bigEndian);
        }

        @Override
        protected String toString(byte[] b, int off, boolean bigEndian) {
            return Long.toString(toInt(b, off, bigEndian) & 0xffffffffL);
        }
    },
    TAG(4, 2) {
        @Override
        public byte[] toggleEndian(byte[] b, boolean preserve) {
            return ByteKit.swapShorts(preserve ? b.clone() : b, 0, b.length);
        }

        @Override
        protected String toString(byte[] b, int off, boolean bigEndian) {
            return Tag.toHexString(toInt(b, off, bigEndian));
        }

        @Override
        protected int toInt(byte[] b, int off, boolean bigEndian) {
            return ByteKit.bytesToTag(b, off, bigEndian);
        }

        @Override
        protected byte[] toBytes(String s, byte[] b, int off, boolean bigEndian) {
            return toBytes(Integer.parseInt(s, Normal._16), b, off, bigEndian);
        }

        @Override
        protected byte[] toBytes(int i, byte[] b, int off, boolean bigEndian) {
            return ByteKit.tagToBytes(i, b, off, bigEndian);
        }
    },
    FLOAT(4, 4) {
        @Override
        public byte[] toggleEndian(byte[] b, boolean preserve) {
            return ByteKit.swapInts(preserve ? b.clone() : b, 0, b.length);
        }

        @Override
        protected String toString(byte[] b, int off, boolean bigEndian) {
            return Property
                    .formatDS(ByteKit.bytesToFloat(b, off, bigEndian));
        }

        @Override
        protected float toFloat(byte[] b, int off, boolean bigEndian) {
            return ByteKit.bytesToFloat(b, off, bigEndian);
        }

        @Override
        protected double toDouble(byte[] b, int off, boolean bigEndian) {
            return ByteKit.bytesToFloat(b, off, bigEndian);
        }

        @Override
        protected byte[] toBytes(String s, byte[] b, int off, boolean bigEndian) {
            return toBytes(Float.parseFloat(s), b, off, bigEndian);
        }

        @Override
        protected byte[] toBytes(float f, byte[] b, int off, boolean bigEndian) {
            return ByteKit.floatToBytes(f, b, off, bigEndian);
        }

        @Override
        protected byte[] toBytes(double d, byte[] b, int off,
                                 boolean bigEndian) {
            return ByteKit.floatToBytes((float) d, b, off, bigEndian);
        }
    },
    DOUBLE(8, 8) {
        @Override
        public byte[] toggleEndian(byte[] b, boolean preserve) {
            return ByteKit.swapLongs(preserve ? b.clone() : b, 0, b.length);
        }

        @Override
        protected String toString(byte[] b, int off, boolean bigEndian) {
            return Property
                    .formatDS(ByteKit.bytesToDouble(b, off, bigEndian));
        }

        @Override
        protected float toFloat(byte[] b, int off, boolean bigEndian) {
            return (float) ByteKit.bytesToDouble(b, off, bigEndian);
        }

        @Override
        protected double toDouble(byte[] b, int off, boolean bigEndian) {
            return ByteKit.bytesToDouble(b, off, bigEndian);
        }

        @Override
        protected byte[] toBytes(String s, byte[] b, int off,
                                 boolean bigEndian) {
            return toBytes(Double.parseDouble(s), b, off, bigEndian);
        }

        @Override
        protected byte[] toBytes(float f, byte[] b, int off,
                                 boolean bigEndian) {
            return ByteKit.doubleToBytes(f, b, off, bigEndian);
        }

        @Override
        protected byte[] toBytes(double d, byte[] b, int off,
                                 boolean bigEndian) {
            return ByteKit.doubleToBytes(d, b, off, bigEndian);
        }
    };

    final int numBytes;
    final int numEndianBytes;

    BinaryValueType(int numBytes, int numEndianBytes) {
        this.numBytes = numBytes;
        this.numEndianBytes = numEndianBytes;
    }

    @Override
    public boolean isIntValue() {
        return false;
    }

    @Override
    public boolean isStringValue() {
        return false;
    }

    @Override
    public boolean useSpecificCharacterSet() {
        return false;
    }

    @Override
    public boolean isTemporalType() {
        return false;
    }

    @Override
    public int numEndianBytes() {
        return numEndianBytes;
    }

    protected String toString(byte[] b, int off, boolean bigEndian) {
        return Integer.toString(toInt(b, off, bigEndian));
    }

    protected int toInt(byte[] b, int off, boolean bigEndian) {
        throw new UnsupportedOperationException();
    }

    protected float toFloat(byte[] b, int off, boolean bigEndian) {
        throw new UnsupportedOperationException();
    }

    protected double toDouble(byte[] b, int off, boolean bigEndian) {
        throw new UnsupportedOperationException();
    }

    protected byte[] toBytes(String s, byte[] b, int off, boolean bigEndian) {
        return toBytes(Property.parseIS(s), b, off, bigEndian);
    }

    protected byte[] toBytes(int i, byte[] b, int off, boolean bigEndian) {
        throw new UnsupportedOperationException();
    }

    protected byte[] toBytes(float f, byte[] b, int off, boolean bigEndian) {
        throw new UnsupportedOperationException();
    }

    protected byte[] toBytes(double d, byte[] b, int off, boolean bigEndian) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] toBytes(Object val, SpecificCharacterSet cs) {
        if (val instanceof byte[])
            return (byte[]) val;

        throw new UnsupportedOperationException();
    }

    @Override
    public String toString(Object val, boolean bigEndian, int valueIndex,
                           String defVal) {
        if (!(val instanceof byte[]))
            throw new UnsupportedOperationException();

        byte[] b = (byte[]) val;
        int len = b.length;
        int off = valueIndex * numBytes;
        return off + numBytes <= len
                ? toString(b, off, bigEndian)
                : defVal;
    }

    private void checkLength(int len) {
        if (len % numBytes != 0)
            throw new IllegalArgumentException("length: " + len);
    }

    @Override
    public Object toStrings(Object val, boolean bigEndian,
                            SpecificCharacterSet cs) {
        if (!(val instanceof byte[]))
            throw new UnsupportedOperationException();

        byte[] b = (byte[]) val;
        int len = b.length;
        checkLength(len);
        if (len == numBytes)
            return toString(b, 0, bigEndian);

        String[] ss = new String[len / numBytes];
        for (int i = 0, off = 0; i < ss.length; i++, off += numBytes)
            ss[i] = toString(b, off, bigEndian);
        return ss;
    }

    @Override
    public int toInt(Object val, boolean bigEndian, int valueIndex,
                     int defVal) {
        if (!(val instanceof byte[]))
            throw new UnsupportedOperationException();

        byte[] b = (byte[]) val;
        int len = b.length;
        int off = valueIndex * numBytes;
        return off + numBytes <= len
                ? toInt(b, off, bigEndian)
                : defVal;
    }

    @Override
    public int[] toInts(Object val, boolean bigEndian) {
        if (!(val instanceof byte[]))
            throw new UnsupportedOperationException();

        byte[] b = (byte[]) val;
        int len = b.length;
        checkLength(len);
        int[] is = new int[len / numBytes];
        for (int i = 0, off = 0; i < is.length; i++, off += numBytes)
            is[i] = toInt(b, off, bigEndian);
        return is;
    }

    @Override
    public float toFloat(Object val, boolean bigEndian, int valueIndex,
                         float defVal) {
        if (!(val instanceof byte[]))
            throw new UnsupportedOperationException();

        byte[] b = (byte[]) val;
        int len = b.length;
        int off = valueIndex * numBytes;
        return off + numBytes <= len
                ? toFloat(b, off, bigEndian)
                : defVal;
    }

    @Override
    public float[] toFloats(Object val, boolean bigEndian) {
        if (!(val instanceof byte[]))
            throw new UnsupportedOperationException();

        byte[] b = (byte[]) val;
        int len = b.length;
        checkLength(len);
        float[] fs = new float[len / numBytes];
        for (int i = 0, off = 0; i < fs.length; i++, off += numBytes)
            fs[i] = toFloat(b, off, bigEndian);
        return fs;
    }

    @Override
    public double toDouble(Object val, boolean bigEndian, int valueIndex,
                           double defVal) {
        if (!(val instanceof byte[]))
            throw new UnsupportedOperationException();

        byte[] b = (byte[]) val;
        int len = b.length;
        int off = valueIndex * numBytes;
        return off + numBytes <= len
                ? toDouble(b, off, bigEndian)
                : defVal;
    }

    @Override
    public double[] toDoubles(Object val, boolean bigEndian) {
        if (!(val instanceof byte[]))
            throw new UnsupportedOperationException();

        byte[] b = (byte[]) val;
        int len = b.length;
        checkLength(len);
        double[] ds = new double[len / numBytes];
        for (int i = 0, off = 0; i < ds.length; i++, off += numBytes)
            ds[i] = toDouble(b, off, bigEndian);
        return ds;
    }

    @Override
    public Date toDate(Object val, TimeZone tz, int valueIndex, boolean ceil,
                       Date defVal, DatePrecision precision) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Date[] toDate(Object val, TimeZone tz, boolean ceil,
                         DatePrecision precisions) {
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

        return toBytes(s, new byte[numBytes], 0, bigEndian);
    }

    @Override
    public Object toValue(String[] ss, boolean bigEndian) {
        if (null == ss || ss.length == 0)
            return Value.NULL;

        if (ss.length == 1)
            return toValue(ss[0], bigEndian);

        byte[] b = new byte[ss.length * numBytes];
        for (int i = 0, off = 0; i < ss.length; i++, off += numBytes)
            toBytes(ss[i], b, off, bigEndian);

        return b;
    }

    @Override
    public Object toValue(int[] is, boolean bigEndian) {
        if (null == is || is.length == 0)
            return Value.NULL;

        byte[] b = new byte[is.length * numBytes];
        for (int i = 0, off = 0; i < is.length; i++, off += numBytes)
            toBytes(is[i], b, off, bigEndian);

        return b;
    }

    @Override
    public Object toValue(float[] fs, boolean bigEndian) {
        if (null == fs || fs.length == 0)
            return Value.NULL;

        byte[] b = new byte[fs.length * numBytes];
        for (int i = 0, off = 0; i < fs.length; i++, off += numBytes)
            toBytes(fs[i], b, off, bigEndian);

        return b;
    }

    @Override
    public Object toValue(double[] ds, boolean bigEndian) {
        if (null == ds || ds.length == 0)
            return Value.NULL;

        byte[] b = new byte[ds.length * numBytes];
        for (int i = 0, off = 0; i < ds.length; i++, off += numBytes)
            toBytes(ds[i], b, off, bigEndian);

        return b;
    }

    @Override
    public Object toValue(Date[] ds, TimeZone tz, DatePrecision precision) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean prompt(Object val, boolean bigEndian,
                          SpecificCharacterSet cs, int maxChars, StringBuilder sb) {
        if (val instanceof byte[])
            return prompt((byte[]) val, bigEndian, maxChars, sb);

        return StringValueType.prompt(val.toString(), maxChars, sb);
    }

    private boolean prompt(byte[] b, boolean bigEndian, int maxChars,
                           StringBuilder sb) {
        int maxLength = sb.length() + maxChars;
        for (int i = b.length / numBytes, off = 0; i-- > 0; off += numBytes) {
            sb.append(toString(b, off, bigEndian));
            if (sb.length() > maxLength) {
                sb.setLength(maxLength + 1);
                return false;
            }
            if (i > 0)
                sb.append(Symbol.C_BACKSLASH);
        }
        return true;
    }

    @Override
    public int vmOf(Object val) {
        if (val instanceof byte[]) {
            return ((byte[]) val).length / numBytes;
        }
        throw new UnsupportedOperationException();
    }

}
