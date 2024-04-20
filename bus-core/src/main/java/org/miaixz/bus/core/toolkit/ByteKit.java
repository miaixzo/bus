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
package org.miaixz.bus.core.toolkit;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;

/**
 * 对数字和字节进行转换
 * 假设数据存储是以大端模式存储的：
 * <ul>
 *     <li>byte: 字节类型 占8位二进制 00000000</li>
 *     <li>char: 字符类型 占2个字节 16位二进制 byte[0] byte[1]</li>
 *     <li>int : 整数类型 占4个字节 32位二进制 byte[0] byte[1] byte[2] byte[3]</li>
 *     <li>long: 长整数类型 占8个字节 64位二进制 byte[0] byte[1] byte[2] byte[3] byte[4] byte[5]</li>
 *     <li>long: 长整数类型 占8个字节 64位二进制 byte[0] byte[1] byte[2] byte[3] byte[4] byte[5] byte[6] byte[7]</li>
 *     <li>float: 浮点数(小数) 占4个字节 32位二进制 byte[0] byte[1] byte[2] byte[3]</li>
 *     <li>double: 双精度浮点数(小数) 占8个字节 64位二进制 byte[0] byte[1] byte[2] byte[3] byte[4]byte[5] byte[6] byte[7]</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ByteKit {

    /**
     * int转byte
     *
     * @param data int值
     * @return the byte
     */
    public static byte getByte(int data) {
        return (byte) data;
    }

    /**
     * int转byte数组
     * 默认以小端序转换
     *
     * @param data int值
     * @return the byte
     */
    public static byte[] getBytes(int data) {
        return getBytes(data, ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * int转byte数组
     * 默认以小端序转换
     *
     * @param data int值
     * @return the byte
     */
    public static byte[] getBytes(int... data) {
        byte[] ret = new byte[4 * data.length];
        for (int i = 0; i < data.length; i++) {
            intToBytesLE(data[i], ret, 4 * i);
        }
        return ret;
    }

    /**
     * int转byte数组
     * 自定义端序
     *
     * @param data      int值
     * @param byteOrder 端序
     * @return the byte
     */
    public static byte[] getBytes(int data, ByteOrder byteOrder) {
        if (ByteOrder.LITTLE_ENDIAN == byteOrder) {
            return new byte[]{
                    (byte) ((data >> 24) & 0xFF),
                    (byte) ((data >> Normal._16) & 0xFF),
                    (byte) ((data >> 8) & 0xFF),
                    (byte) (data & 0xFF)
            };
        } else {
            return new byte[]{
                    (byte) (data & 0xFF),
                    (byte) ((data >> 8) & 0xFF),
                    (byte) ((data >> Normal._16) & 0xFF),
                    (byte) ((data >> 24) & 0xFF)
            };
        }
    }

    /**
     * long转byte数组
     * 默认以小端序转换
     *
     * @param data long值
     * @return the byte
     */
    public static byte[] getBytes(long data) {
        return getBytes(data, ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * long转byte数组
     *
     * @param data      long值
     * @param byteOrder 端序
     * @return the byte
     */
    public static byte[] getBytes(long data, ByteOrder byteOrder) {
        byte[] result = new byte[Long.BYTES];
        if (ByteOrder.LITTLE_ENDIAN == byteOrder) {
            for (int i = (result.length - 1); i >= 0; i--) {
                result[i] = (byte) (data & 0xFF);
                data >>= Byte.SIZE;
            }
        } else {
            for (int i = 0; i < result.length; i++) {
                result[i] = (byte) (data & 0xFF);
                data >>= Byte.SIZE;
            }
        }
        return result;
    }

    /**
     * float转byte数组
     *
     * @param data float值
     * @return the byte
     */
    public static byte[] getBytes(float data) {
        return getBytes(data, ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * double转byte数组
     *
     * @param data      double值
     * @param byteOrder 端序
     * @return the byte
     */
    public static byte[] getBytes(float data, ByteOrder byteOrder) {
        return getBytes(Float.floatToIntBits(data), byteOrder);
    }

    /**
     * double转byte数组
     * 默认以小端序转换
     *
     * @param data double值
     * @return the byte
     */
    public static byte[] getBytes(double data) {
        return getBytes(data, ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * double转byte数组
     *
     * @param data      double值
     * @param byteOrder 端序
     * @return the byte
     */
    public static byte[] getBytes(double data, ByteOrder byteOrder) {
        return getBytes(Double.doubleToLongBits(data), byteOrder);
    }

    /**
     * short转byte数组
     * 默认以小端序转换
     *
     * @param data short值
     * @return the byte
     */
    public static byte[] getBytes(short data) {
        return getBytes(data, ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * short转byte数组
     * 自定义端序
     *
     * @param data      short值
     * @param byteOrder 端序
     * @return the byte
     */
    public static byte[] getBytes(short data, ByteOrder byteOrder) {
        byte[] b = new byte[Short.BYTES];
        if (ByteOrder.LITTLE_ENDIAN == byteOrder) {
            b[1] = (byte) (data & 0xff);
            b[0] = (byte) ((data >> Byte.SIZE) & 0xff);
        } else {
            b[0] = (byte) (data & 0xff);
            b[1] = (byte) ((data >> Byte.SIZE) & 0xff);
        }
        return b;
    }

    /**
     * char转byte数组
     *
     * @param data char值
     * @return the byte
     */
    public static byte[] getBytes(char data) {
        byte[] bytes = new byte[2];
        bytes[0] = (byte) (data);
        bytes[1] = (byte) (data >> 8);
        return bytes;
    }

    /**
     * char转byte数组
     *
     * @param data char值
     * @return the byte
     */
    public static byte[] getBytes(char[] data) {
        CharBuffer cb = CharBuffer.allocate(data.length);
        cb.put(data);
        cb.flip();
        ByteBuffer bb = Charset.UTF_8.encode(cb);
        return bb.array();
    }

    /**
     * string转byte数组
     *
     * @param data string值
     * @return the byte
     */
    public static byte[] getBytes(String data) {
        return getBytes(data, Charset.DEFAULT_GBK);
    }

    /**
     * string转byte数组
     *
     * @param data    string值
     * @param charset 字符集
     * @return the byte
     */
    public static byte[] getBytes(String data, String charset) {
        return data.getBytes(Charset.charset(charset));
    }

    /**
     * byte转无符号int
     *
     * @param data byte值
     * @return the int
     */
    public static int getInt(byte data) {
        return data & 0xFF;
    }

    /**
     * byte[]转int值
     * 默认以小端序转换
     *
     * @param data byte数组
     * @return the int
     */
    public static int getInt(byte[] data) {
        return getInt(data, ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * byte[]转int值
     * 自定义端序
     *
     * @param data      byte数组
     * @param byteOrder 端序
     * @return the int
     */
    public static int getInt(byte[] data, ByteOrder byteOrder) {
        return bytesToInt(data, 0, byteOrder);
    }

    /**
     * byte[]转int值
     *
     * @param data      byte数组
     * @param start     计算数组开始位置
     * @param byteOrder 端序
     * @return int值
     */
    public static int bytesToInt(byte[] data, int start, ByteOrder byteOrder) {
        if (ByteOrder.LITTLE_ENDIAN == byteOrder) {
            return data[start] & 0xFF |
                    (data[1 + start] & 0xFF) << 8 |
                    (data[2 + start] & 0xFF) << 16 |
                    (data[3 + start] & 0xFF) << 24;
        } else {
            return data[3 + start] & 0xFF |
                    (data[2 + start] & 0xFF) << 8 |
                    (data[1 + start] & 0xFF) << 16 |
                    (data[start] & 0xFF) << 24;
        }
    }

    /**
     * byte数组转long
     *
     * @param data byte数组
     * @return the long
     */
    public static long getLong(byte[] data) {
        return getLong(data, ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * byte数组转long
     *
     * @param data      byte数组
     * @param byteOrder 端序
     * @return the long
     */
    public static long getLong(byte[] data, ByteOrder byteOrder) {
        return bytesToLong(data, 0, byteOrder);
    }

    /**
     * byte数组转long
     *
     * @param data      byte数组
     * @param start     计算数组开始位置
     * @param byteOrder 端序
     * @return long值
     */
    public static long bytesToLong(byte[] data, int start, ByteOrder byteOrder) {
        long values = 0;
        if (ByteOrder.LITTLE_ENDIAN == byteOrder) {
            for (int i = (Long.BYTES - 1); i >= 0; i--) {
                values <<= Byte.SIZE;
                values |= (data[i + start] & 0xff);
            }
        } else {
            for (int i = 0; i < Long.BYTES; i++) {
                values <<= Byte.SIZE;
                values |= (data[i + start] & 0xff);
            }
        }

        return values;
    }

    /**
     * byte数组转Double
     * 默认以小端序转换
     *
     * @param data byte数组
     * @return the float
     */
    public static float getFloat(byte[] data) {
        return getFloat(data, ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * byte数组转double
     * 自定义端序
     *
     * @param data      byte数组
     * @param byteOrder 端序
     * @return the float
     */
    public static float getFloat(byte[] data, ByteOrder byteOrder) {
        return Float.intBitsToFloat(getInt(data, byteOrder));
    }

    /**
     * byte数组转Double
     * 默认以小端序转换
     *
     * @param data byte数组
     * @return the double
     */
    public static double getDouble(byte[] data) {
        return getDouble(data, ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * byte数组转double
     * 自定义端序
     *
     * @param data      byte数组
     * @param byteOrder 端序
     * @return the double
     */
    public static double getDouble(byte[] data, ByteOrder byteOrder) {
        return Double.longBitsToDouble(getLong(data, byteOrder));
    }

    /**
     * byte数组转short
     * 默认以小端序转换
     *
     * @param data byte数组
     * @return the short
     */
    public static short getShort(byte[] data) {
        return getShort(data, ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * byte数组转short
     * 自定义端序
     *
     * @param data      byte数组
     * @param byteOrder 端序
     * @return the short
     */
    /**
     * byte数组转short
     * 自定义端序
     *
     * @param bytes     byte数组，长度必须为2
     * @param byteOrder 端序
     * @return short值
     */
    public static short getShort(final byte[] bytes, final ByteOrder byteOrder) {
        return getShort(bytes, 0, byteOrder);
    }

    /**
     * byte数组转short
     * 自定义端序
     *
     * @param bytes     byte数组，长度必须大于2
     * @param start     开始位置
     * @param byteOrder 端序
     * @return short值
     */
    public static short getShort(final byte[] bytes, final int start, final ByteOrder byteOrder) {
        if (ByteOrder.LITTLE_ENDIAN == byteOrder) {
            // 小端模式，数据的高字节保存在内存的高地址中，而数据的低字节保存在内存的低地址中
            return (short) (bytes[start] & 0xff | (bytes[start + 1] & 0xff) << Byte.SIZE);
        } else {
            return (short) (bytes[start + 1] & 0xff | (bytes[start] & 0xff) << Byte.SIZE);
        }
    }

    /**
     * byte数组转char
     *
     * @param data 字节数组
     * @return the char
     */
    public static char getChar(byte[] data) {
        return (char) ((0xff & data[0]) | (0xff00 & (data[1] << 8)));
    }

    /**
     * byte数组转string
     *
     * @param data 字节数组
     * @return the string
     */
    public static String getString(byte[] data) {
        return getString(data, Charset.DEFAULT_GBK);
    }

    /**
     * byte数组转string
     *
     * @param data    字节数组
     * @param charset 字符集
     * @return the string
     */
    public static String getString(byte[] data, String charset) {
        return new String(data, Charset.charset(charset));
    }

    /**
     * 将{@link Number}转换为byte数组
     *
     * @param number byte数组
     * @return the byte
     */
    public static byte[] getBytes(Number number) {
        return get(number, ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * byte数组转换为指定类型数字
     *
     * @param <T>         数字类型
     * @param bytes       byte数组
     * @param targetClass 目标数字类型
     * @return 转换后的数字
     * @throws IllegalArgumentException 不支持的数字类型，如用户自定义数字类型
     */
    public static <T extends Number> T getNumber(byte[] bytes, Class<T> targetClass) {
        return get(bytes, targetClass, ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * 将{@link Number}转换为
     *
     * @param number    数字
     * @param byteOrder 端序
     * @return bytes
     */
    public static byte[] get(Number number, ByteOrder byteOrder) {
        if (number instanceof Byte) {
            return new byte[]{number.byteValue()};
        } else if (number instanceof Double) {
            return getBytes((Double) number, byteOrder);
        } else if (number instanceof Long) {
            return getBytes((Long) number, byteOrder);
        } else if (number instanceof Integer) {
            return getBytes((Integer) number, byteOrder);
        } else if (number instanceof Short) {
            return getBytes((Short) number, byteOrder);
        } else if (number instanceof Float) {
            return getBytes((Float) number, byteOrder);
        } else {
            return getBytes(number.doubleValue(), byteOrder);
        }
    }

    /**
     * byte数组转换为指定类型数字
     *
     * @param <T>         数字类型
     * @param bytes       byte数组
     * @param targetClass 目标数字类型
     * @param byteOrder   端序
     * @return 转换后的数字
     * @throws IllegalArgumentException 不支持的数字类型，如用户自定义数字类型
     */
    public static <T extends Number> T get(byte[] bytes, Class<T> targetClass, ByteOrder byteOrder) {
        Number number;
        if (Byte.class == targetClass) {
            number = bytes[0];
        } else if (Short.class == targetClass) {
            number = getShort(bytes, byteOrder);
        } else if (Integer.class == targetClass) {
            number = getInt(bytes, byteOrder);
        } else if (AtomicInteger.class == targetClass) {
            number = new AtomicInteger(getInt(bytes, byteOrder));
        } else if (Long.class == targetClass) {
            number = getLong(bytes, byteOrder);
        } else if (AtomicLong.class == targetClass) {
            number = new AtomicLong(getLong(bytes, byteOrder));
        } else if (LongAdder.class == targetClass) {
            final LongAdder longValue = new LongAdder();
            longValue.add(getLong(bytes, byteOrder));
            number = longValue;
        } else if (Float.class == targetClass) {
            number = getFloat(bytes, byteOrder);
        } else if (Double.class == targetClass) {
            number = getDouble(bytes, byteOrder);
        } else if (DoubleAdder.class == targetClass) {
            final DoubleAdder doubleAdder = new DoubleAdder();
            doubleAdder.add(getDouble(bytes, byteOrder));
            number = doubleAdder;
        } else if (BigDecimal.class == targetClass) {
            number = MathKit.toBigDecimal(getDouble(bytes, byteOrder));
        } else if (BigInteger.class == targetClass) {
            number = BigInteger.valueOf(getLong(bytes, byteOrder));
        } else if (Number.class == targetClass) {
            // 用户没有明确类型具体类型，默认Double
            number = getDouble(bytes, byteOrder);
        } else {
            // 用户自定义类型不支持
            throw new IllegalArgumentException("Unsupported Number type: " + targetClass.getName());
        }

        return (T) number;
    }

    /**
     * 根据分隔符拆分字节数组
     *
     * @param data      字节数组
     * @param separator 分隔符
     * @return the byte
     */
    public static byte[][] split(byte[] data, byte separator) {
        int count = countOf(data, separator);
        byte[][] result = new byte[count + 1][];
        int index = indexOf(data, separator, 0, data.length);
        if (index < 0) {
            result[0] = data;
        } else {
            result[0] = subArray(data, 0, index);
            for (int i = 1; i <= count; i++) {
                int from = index + 1;
                index = indexOf(data, separator, from, data.length);
                if (index < from) {
                    index = data.length;
                }
                result[i] = subArray(data, from, index);
            }
        }
        return result;
    }

    /**
     * 拆分byte数组为几个等份(最后一份可能小于len)
     *
     * @param data 数组
     * @param len  每个小节的长度
     * @return 拆分后的数组
     */
    public static byte[][] split(byte[] data, int len) {
        int amount = data.length / len;
        final int remainder = data.length % len;
        // 兼容切片长度大于原数组长度的情况
        final boolean hasRemainder = remainder > 0;
        final byte[][] arrays = new byte[hasRemainder ? (amount + 1) : amount][];
        byte[] arr;
        int start = 0;
        for (int i = 0; i < amount; i++) {
            arr = new byte[len];
            System.arraycopy(data, start, arr, 0, len);
            arrays[i] = arr;
            start += len;
        }
        if (hasRemainder) {
            // 有剩余，按照实际长度创建
            arr = new byte[remainder];
            System.arraycopy(data, start, arr, 0, remainder);
            arrays[amount] = arr;
        }
        return arrays;
    }

    /**
     * 截取子数组
     *
     * @param data 字节数组
     * @param from 开始下标（包含）
     * @return the byte
     */
    public static byte[] subArray(byte[] data, int from) {
        return subArray(data, from, data.length);
    }


    /**
     * 截取子数组
     *
     * @param data 字节数组
     * @param from 开始下标（包含）
     * @param to   结束下标（不包含）
     * @return the byte
     */
    public static byte[] subArray(byte[] data, int from, int to) {
        byte[] result = new byte[to - from];
        if (to > from) {
            System.arraycopy(data, from, result, 0, result.length);
        }
        return result;
    }

    /**
     * 连接多个字节数组
     *
     * @param data  连字符
     * @param datas 二维数组
     * @return the byte
     */
    public static byte[] join(byte data, byte[]... datas) {
        int length = 0;
        for (byte[] arr : datas) {
            length += arr.length;
        }
        byte[] result = new byte[length + datas.length - 1];
        int index = 0;
        for (int i = 0; i < datas.length; i++) {
            byte[] arr = datas[i];
            System.arraycopy(arr, 0, result, index, arr.length);
            index += arr.length;
            if (i < datas.length - 1) {
                result[index] = data;
                index++;
            }
        }
        return result;
    }

    /**
     * 连接多个字节数组
     *
     * @param data 二维数组
     * @return the byte
     */
    public static byte[] join(byte[]... data) {
        int length = 0;
        for (byte[] arr : data) {
            length += arr.length;
        }
        byte[] result = new byte[length];
        int index = 0;
        for (byte[] arr : data) {
            System.arraycopy(arr, 0, result, index, arr.length);
            index += arr.length;
        }
        return result;
    }

    /**
     * 首尾去掉空字符
     *
     * @param data  字节数组
     * @param empty 空数组
     * @return the byte
     */
    public static byte[] trim(byte[] data, byte empty) {
        return trim(data, empty, 0, data.length);
    }

    /**
     * 从 from 到 to 截取子串 并 首尾去掉空字符
     *
     * @param data  字节数组
     * @param empty 空字节
     * @param from  开始下标（包含）
     * @param to    结束下标（不包含）
     * @return the byte
     */
    public static byte[] trim(byte[] data, byte empty, int from, int to) {
        while (from < data.length - 1 && data[from] == empty) {
            from++;
        }
        while (to > from && data[to - 1] == empty) {
            to--;
        }
        return subArray(data, from, to);
    }

    /**
     * byte数组转int
     * 默认以: {@link ByteOrder#BIG_ENDIAN } or {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data      byte数组
     * @param off       偏移量
     * @param bigEndian 是否大字节序列
     * @return the int
     */
    public static int bytesToInt(byte[] data, int off, boolean bigEndian) {
        return bigEndian ? bytesToIntBE(data, off) : bytesToIntLE(data, off);
    }

    /**
     * byte数组转int
     * 默认以: {@link ByteOrder#BIG_ENDIAN }
     *
     * @param data byte数组
     * @param off  偏移量
     * @return the int
     */
    public static int bytesToIntBE(byte[] data, int off) {
        return (data[off] << 24) + ((data[off + 1] & 255) << 16)
                + ((data[off + 2] & 255) << 8) + (data[off + 3] & 255);
    }

    /**
     * byte数组转int
     * 默认以: {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data byte数组
     * @param off  偏移量
     * @return the int
     */
    public static int bytesToIntLE(byte[] data, int off) {
        return (data[off + 3] << 24) + ((data[off + 2] & 255) << 16)
                + ((data[off + 1] & 255) << 8) + (data[off] & 255);
    }

    /**
     * byte数组转int
     * 默认以: {@link ByteOrder#BIG_ENDIAN } or {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data      byte数组
     * @param off       偏移量
     * @param bigEndian 是否大字节序列
     * @return the int
     */
    public static int bytesToShort(byte[] data, int off, boolean bigEndian) {
        return bigEndian ? bytesToShortBE(data, off) : bytesToShortLE(data, off);
    }

    /**
     * byte数组处理
     * 排序: {@link ByteOrder#BIG_ENDIAN } or {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data      long值
     * @param s         字符
     * @param off       偏移量
     * @param len       字符长度
     * @param bigEndian 是否大字节序列
     */
    public static void bytesToShort(byte[] data, short[] s, int off, int len, boolean bigEndian) {
        if (bigEndian)
            bytesToShortsBE(data, s, off, len);
        else
            bytesToShortLE(data, s, off, len);
    }

    /**
     * byte数组转int
     * 默认以: {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data byte数组
     * @param off  偏移量
     * @return the int
     */
    public static int bytesToShortBE(byte[] data, int off) {
        return (data[off] << 8) + (data[off + 1] & 255);
    }

    /**
     * byte数组转int
     * 默认以: {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data byte数组
     * @param off  偏移量
     * @return the int
     */
    public static int bytesToShortLE(byte[] data, int off) {
        return (data[off + 1] << 8) + (data[off] & 255);
    }

    /**
     * byte数组处理
     * 默认排序: {@link ByteOrder#BIG_ENDIAN }
     *
     * @param data double值
     * @param s    字符
     * @param off  偏移量
     * @param len  字符长度
     */
    public static void bytesToShortsBE(byte[] data, short[] s, int off, int len) {
        int boff = 0;
        for (int j = 0; j < len; j++) {
            int b0 = data[boff];
            int b1 = data[boff + 1] & 0xff;
            s[off + j] = (short) ((b0 << 8) | b1);
            boff += 2;
        }
    }

    /**
     * byte数组处理
     * 默认排序: {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data double值
     * @param s    字符
     * @param off  偏移量
     * @param len  字符长度
     */
    public static void bytesToShortLE(byte[] data, short[] s, int off, int len) {
        int boff = 0;
        for (int j = 0; j < len; j++) {
            int b0 = data[boff + 1];
            int b1 = data[boff] & 0xff;
            s[off + j] = (short) ((b0 << 8) | b1);
            boff += 2;
        }
    }

    /**
     * byte数组转int
     * 默认以: {@link ByteOrder#BIG_ENDIAN }
     *
     * @param data      byte数组
     * @param off       偏移量
     * @param bigEndian 是否大字节序列
     * @return the int
     */
    public static int bytesToUShort(byte[] data, int off, boolean bigEndian) {
        return bigEndian ? bytesToUShortBE(data, off) : bytesToUShortLE(data, off);
    }

    /**
     * byte数组转int
     * 默认以: {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data byte数组
     * @param off  偏移量
     * @return the int
     */
    public static int bytesToUShortBE(byte[] data, int off) {
        return ((data[off] & 255) << 8) + (data[off + 1] & 255);
    }

    /**
     * byte数组转int
     * 默认以: {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data byte数组
     * @param off  偏移量
     * @return the int
     */
    public static int bytesToUShortLE(byte[] data, int off) {
        return ((data[off + 1] & 255) << 8) + (data[off] & 255);
    }

    /**
     * byte数组转float
     * 默认以: {@link ByteOrder#BIG_ENDIAN } or {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data      byte数组
     * @param off       偏移量
     * @param bigEndian 是否大字节序列
     * @return the float
     */
    public static float bytesToFloat(byte[] data, int off, boolean bigEndian) {
        return bigEndian ? bytesToFloatBE(data, off) : bytesToFloatLE(data, off);
    }

    /**
     * byte数组转float
     * 默认以: {@link ByteOrder#BIG_ENDIAN }
     *
     * @param data byte数组
     * @param off  偏移量
     * @return the float
     */
    public static float bytesToFloatBE(byte[] data, int off) {
        return Float.intBitsToFloat(bytesToIntBE(data, off));
    }

    /**
     * byte数组转float
     * 默认以: {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data byte数组
     * @param off  偏移量
     * @return the float
     */
    public static float bytesToFloatLE(byte[] data, int off) {
        return Float.intBitsToFloat(bytesToIntLE(data, off));
    }

    /**
     * byte数组转long
     * 默认以: {@link ByteOrder#BIG_ENDIAN } or {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data      byte数组
     * @param off       偏移量
     * @param bigEndian 是否大字节序列
     * @return the long
     */
    public static long bytesToLong(byte[] data, int off, boolean bigEndian) {
        return bigEndian ? bytesToLongBE(data, off) : bytesToLongLE(data, off);
    }

    /**
     * byte数组转long
     * 默认以: {@link ByteOrder#BIG_ENDIAN }
     *
     * @param data byte数组
     * @param off  偏移量
     * @return the long
     */
    public static long bytesToLongBE(byte[] data, int off) {
        return ((long) data[off] << 56)
                + ((long) (data[off + 1] & 255) << 48)
                + ((long) (data[off + 2] & 255) << 40)
                + ((long) (data[off + 3] & 255) << Normal._32)
                + ((long) (data[off + 4] & 255) << 24)
                + ((data[off + 5] & 255) << Normal._16)
                + ((data[off + 6] & 255) << 8)
                + (data[off + 7] & 255);
    }

    /**
     * byte数组转long
     * 默认以: {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data byte数组
     * @param off  偏移量
     * @return the long
     */
    public static long bytesToLongLE(byte[] data, int off) {
        return ((long) data[off + 7] << 56)
                + ((long) (data[off + 6] & 255) << 48)
                + ((long) (data[off + 5] & 255) << 40)
                + ((long) (data[off + 4] & 255) << Normal._32)
                + ((long) (data[off + 3] & 255) << 24)
                + ((data[off + 2] & 255) << Normal._16)
                + ((data[off + 1] & 255) << 8)
                + (data[off] & 255);
    }

    /**
     * byte数组转double
     * 默认以: {@link ByteOrder#BIG_ENDIAN } or {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data      byte数组
     * @param off       偏移量
     * @param bigEndian 是否大字节序列
     * @return the double
     */
    public static double bytesToDouble(byte[] data, int off, boolean bigEndian) {
        return bigEndian ? bytesToDoubleBE(data, off) : bytesToDoubleLE(data, off);
    }

    /**
     * byte数组转double
     * 默认以: {@link ByteOrder#BIG_ENDIAN }
     *
     * @param data byte数组
     * @param off  偏移量
     * @return the double
     */
    public static double bytesToDoubleBE(byte[] data, int off) {
        return Double.longBitsToDouble(bytesToLongBE(data, off));
    }

    /**
     * byte数组转double
     * 默认以: {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data byte数组
     * @param off  偏移量
     * @return the double
     */
    public static double bytesToDoubleLE(byte[] data, int off) {
        return Double.longBitsToDouble(bytesToLongLE(data, off));
    }

    /**
     * byte数组转int
     * 默认以: {@link ByteOrder#BIG_ENDIAN }
     *
     * @param data byte数组
     * @param off  偏移量
     * @return the int
     */
    public static int bytesToVR(byte[] data, int off) {
        return bytesToUShortBE(data, off);
    }

    /**
     * byte数组转int
     * 默认以: {@link ByteOrder#BIG_ENDIAN } or {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data      byte数组
     * @param off       偏移量
     * @param bigEndian 是否大字节序列
     * @return the int
     */
    public static int bytesToTag(byte[] data, int off, boolean bigEndian) {
        return bigEndian ? bytesToTagBE(data, off) : bytesToTagLE(data, off);
    }

    /**
     * byte数组转int
     * 默认以: {@link ByteOrder#BIG_ENDIAN }
     *
     * @param data byte数组
     * @param off  偏移量
     * @return the int
     */
    public static int bytesToTagBE(byte[] data, int off) {
        return bytesToIntBE(data, off);
    }

    /**
     * byte数组转int
     * 默认以: {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data byte数组
     * @param off  偏移量
     * @return the int
     */
    public static int bytesToTagLE(byte[] data, int off) {
        return (data[off + 1] << 24) + ((data[off] & 255) << 16)
                + ((data[off + 3] & 255) << 8) + (data[off + 2] & 255);
    }

    /**
     * int转byte数组
     * 排序: {@link ByteOrder#BIG_ENDIAN } or {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data      float值
     * @param bytes     目标字节
     * @param off       偏移量
     * @param bigEndian 是否大字节序列
     * @return the byte
     */
    public static byte[] intToBytes(int data, byte[] bytes, int off, boolean bigEndian) {
        return bigEndian ? intToBytesBE(data, bytes, off) : intToBytesLE(data, bytes, off);
    }

    /**
     * int转byte数组
     * 默认排序: {@link ByteOrder#BIG_ENDIAN }
     *
     * @param data  int值
     * @param bytes 目标字节
     * @param off   偏移量
     * @return the byte
     */
    public static byte[] intToBytesBE(int data, byte[] bytes, int off) {
        bytes[off] = (byte) (data >> 24);
        bytes[off + 1] = (byte) (data >> 16);
        bytes[off + 2] = (byte) (data >> 8);
        bytes[off + 3] = (byte) data;
        return bytes;
    }

    /**
     * int转byte数组
     * 默认排序: {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data  int值
     * @param bytes 目标字节
     * @param off   偏移量
     * @return the byte
     */
    public static byte[] intToBytesLE(int data, byte[] bytes, int off) {
        bytes[off + 3] = (byte) (data >> 24);
        bytes[off + 2] = (byte) (data >> 16);
        bytes[off + 1] = (byte) (data >> 8);
        bytes[off] = (byte) data;
        return bytes;
    }

    /**
     * int转byte数组
     * 排序: {@link ByteOrder#BIG_ENDIAN } or {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data      int值
     * @param bytes     目标字节
     * @param off       偏移量
     * @param bigEndian 是否大字节序列
     * @return the byte
     */
    public static byte[] shortToBytes(int data, byte[] bytes, int off, boolean bigEndian) {
        return bigEndian ? shortToBytesBE(data, bytes, off) : shortToBytesLE(data, bytes, off);
    }

    /**
     * int转byte数组
     * 默认排序: {@link ByteOrder#BIG_ENDIAN }
     *
     * @param data  int值
     * @param bytes 目标字节
     * @param off   偏移量
     * @return the byte
     */
    public static byte[] shortToBytesBE(int data, byte[] bytes, int off) {
        bytes[off] = (byte) (data >> 8);
        bytes[off + 1] = (byte) data;
        return bytes;
    }

    /**
     * int转byte数组
     * 默认排序: {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data  int值
     * @param bytes 目标字节
     * @param off   偏移量
     * @return the byte
     */
    public static byte[] shortToBytesLE(int data, byte[] bytes, int off) {
        bytes[off + 1] = (byte) (data >> 8);
        bytes[off] = (byte) data;
        return bytes;
    }

    /**
     * long转byte数组
     * 排序: {@link ByteOrder#BIG_ENDIAN } or {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data      long值
     * @param bytes     目标字节
     * @param off       偏移量
     * @param bigEndian 是否大字节序列
     * @return the byte
     */
    public static byte[] longToBytes(long data, byte[] bytes, int off, boolean bigEndian) {
        return bigEndian ? longToBytesBE(data, bytes, off) : longToBytesLE(data, bytes, off);
    }

    /**
     * long转byte数组
     * 默认排序: {@link ByteOrder#BIG_ENDIAN }
     *
     * @param data  long值
     * @param bytes 目标字节
     * @param off   偏移量
     * @return the byte
     */
    public static byte[] longToBytesBE(long data, byte[] bytes, int off) {
        bytes[off] = (byte) (data >> 56);
        bytes[off + 1] = (byte) (data >> 48);
        bytes[off + 2] = (byte) (data >> 40);
        bytes[off + 3] = (byte) (data >> 32);
        bytes[off + 4] = (byte) (data >> 24);
        bytes[off + 5] = (byte) (data >> 16);
        bytes[off + 6] = (byte) (data >> 8);
        bytes[off + 7] = (byte) data;
        return bytes;
    }

    /**
     * long转byte数组
     * 默认排序: {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data  long值
     * @param bytes 目标字节
     * @param off   偏移量
     * @return the byte
     */
    public static byte[] longToBytesLE(long data, byte[] bytes, int off) {
        bytes[off + 7] = (byte) (data >> 56);
        bytes[off + 6] = (byte) (data >> 48);
        bytes[off + 5] = (byte) (data >> 40);
        bytes[off + 4] = (byte) (data >> 32);
        bytes[off + 3] = (byte) (data >> 24);
        bytes[off + 2] = (byte) (data >> 16);
        bytes[off + 1] = (byte) (data >> 8);
        bytes[off] = (byte) data;
        return bytes;
    }

    /**
     * float转byte数组
     * 排序: {@link ByteOrder#BIG_ENDIAN } or {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data      float值
     * @param bytes     目标字节
     * @param off       偏移量
     * @param bigEndian 是否大字节序列
     * @return the byte
     */
    public static byte[] floatToBytes(float data, byte[] bytes, int off, boolean bigEndian) {
        return bigEndian ? floatToBytesBE(data, bytes, off) : floatToBytesLE(data, bytes, off);
    }

    /**
     * float转byte数组
     * 默认排序: {@link ByteOrder#BIG_ENDIAN }
     *
     * @param data  double值
     * @param bytes 目标字节
     * @param off   偏移量
     * @return the byte
     */
    public static byte[] floatToBytesBE(float data, byte[] bytes, int off) {
        return intToBytesBE(Float.floatToIntBits(data), bytes, off);
    }

    /**
     * float转byte数组
     * 默认排序: {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data  double值
     * @param bytes 目标字节
     * @param off   偏移量
     * @return the byte
     */
    public static byte[] floatToBytesLE(float data, byte[] bytes, int off) {
        return intToBytesLE(Float.floatToIntBits(data), bytes, off);
    }

    /**
     * double转byte数组
     * 排序: {@link ByteOrder#BIG_ENDIAN } or {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data      double值
     * @param bytes     目标字节
     * @param off       偏移量
     * @param bigEndian 是否大字节序列
     * @return the byte
     */
    public static byte[] doubleToBytes(double data, byte[] bytes, int off, boolean bigEndian) {
        return bigEndian ? doubleToBytesBE(data, bytes, off) : doubleToBytesLE(data, bytes, off);
    }

    /**
     * double转byte数组
     * 默认排序: {@link ByteOrder#BIG_ENDIAN }
     *
     * @param data  double值
     * @param bytes 目标字节
     * @param off   偏移量
     * @return the byte
     */
    public static byte[] doubleToBytesBE(double data, byte[] bytes, int off) {
        return longToBytesBE(Double.doubleToLongBits(data), bytes, off);
    }

    /**
     * double转byte数组
     * 默认排序: {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data  double值
     * @param bytes 目标字节
     * @param off   偏移量
     * @return the byte
     */
    public static byte[] doubleToBytesLE(double data, byte[] bytes, int off) {
        return longToBytesLE(Double.doubleToLongBits(data), bytes, off);
    }


    /**
     * int转byte数组
     * 排序: {@link ByteOrder#BIG_ENDIAN } or {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data      float值
     * @param bytes     目标字节
     * @param off       偏移量
     * @param bigEndian 是否大字节序列
     * @return the byte
     */
    public static byte[] tagToBytes(int data, byte[] bytes, int off, boolean bigEndian) {
        return bigEndian ? tagToBytesBE(data, bytes, off) : tagToBytesLE(data, bytes, off);
    }

    /**
     * int转byte数组
     * 默认排序: {@link ByteOrder#BIG_ENDIAN }
     *
     * @param data  int值
     * @param bytes 目标字节
     * @param off   偏移量
     * @return the byte
     */
    public static byte[] tagToBytesBE(int data, byte[] bytes, int off) {
        return intToBytesBE(data, bytes, off);
    }

    /**
     * int转byte数组
     * 默认排序: {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data  int值
     * @param bytes 目标字节
     * @param off   偏移量
     * @return the byte
     */
    public static byte[] tagToBytesLE(int data, byte[] bytes, int off) {
        bytes[off + 1] = (byte) (data >> 24);
        bytes[off] = (byte) (data >> 16);
        bytes[off + 3] = (byte) (data >> 8);
        bytes[off + 2] = (byte) data;
        return bytes;
    }

    /**
     * 字符交换
     *
     * @param data byte值
     * @param off  偏移量
     * @param len  长度
     * @return the byte
     */
    public static byte[] swapInts(byte[] data, int off, int len) {
        checkLength(len, 4);
        for (int i = off, n = off + len; i < n; i += 4) {
            swap(data, i, i + 3);
            swap(data, i + 1, i + 2);
        }
        return data;
    }

    /**
     * 字符交换
     *
     * @param data byte值
     * @param off  偏移量
     * @param len  长度
     * @return the byte
     */
    public static byte[] swapLongs(byte[] data, int off, int len) {
        checkLength(len, 8);
        for (int i = off, n = off + len; i < n; i += 8) {
            swap(data, i, i + 7);
            swap(data, i + 1, i + 6);
            swap(data, i + 2, i + 5);
            swap(data, i + 3, i + 4);
        }
        return data;
    }

    /**
     * 字符交换
     *
     * @param data byte值
     * @return the byte
     */
    public static byte[][] swapShorts(byte[][] data) {
        int carry = 0;
        for (int i = 0; i < data.length; i++) {
            byte[] b = data[i];
            if (carry != 0)
                swapLastFirst(data[i - 1], b);
            int len = b.length - carry;
            swapShorts(b, carry, len & ~1);
            carry = len & 1;
        }
        return data;
    }

    /**
     * 字符交换
     *
     * @param data byte值
     * @param off  偏移量
     * @param len  长度
     * @return the byte
     */
    public static byte[] swapShorts(byte[] data, int off, int len) {
        checkLength(len, 2);
        for (int i = off, n = off + len; i < n; i += 2)
            swap(data, i, i + 1);
        return data;
    }


    /**
     * 寻找目标字节在字节数组中的下标
     *
     * @param data   字节数组
     * @param target 目标字节
     * @param from   检索开始下标（包含）
     * @param to     检索结束下标（不包含）
     * @return 找不到则返回-1
     */
    public static int indexOf(byte[] data, byte target, int from, int to) {
        for (int i = from; i < to; i++) {
            if (data[i] == target) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 统计目标字节在字节数组中出现的次数
     *
     * @param data   字节数组
     * @param target 目标字节
     * @return the int
     */
    public static int countOf(byte[] data, byte target) {
        int count = 0;
        for (byte b : data) {
            if (b == target) {
                count++;
            }
        }
        return count;
    }

    /**
     * 解析 BCD 码
     *
     * @param data 字节数组
     * @param from 开始下标（包含）
     * @param to   结束下标（不包含）
     * @return the string
     */
    public static String bcd(byte[] data, int from, int to) {
        char[] chars = new char[2 * (to - from)];
        for (int i = from; i < to; i++) {
            int b = unsigned(data[i]);
            chars[2 * (i - from)] = (char) ((b >> 4) + 0x30);
            chars[2 * (i - from) + 1] = (char) ((b & 0xF) + 0x30);
        }
        return new String(chars);
    }

    /**
     * 无符号整数
     *
     * @param data 字节
     * @return the int
     */
    public static int unsigned(byte data) {
        if (data >= 0) {
            return data;
        }
        return Normal._256 + data;
    }

    /**
     * 异或值，返回
     *
     * @param data 数组
     * @return 异或值
     */
    public static int xor(byte[] data) {
        int temp = 0;
        if (null != data) {
            for (int i = 0; i < data.length; i++) {
                temp ^= data[i];
            }
        }
        return temp;
    }

    /**
     * 将两个字节数组连接到一个新的字节数组
     *
     * @param buf1 字节数组
     * @param buf2 字节数组
     * @return the byte
     */
    public static byte[] concat(byte[] buf1, byte[] buf2) {
        byte[] buffer = new byte[buf1.length + buf2.length];
        int offset = 0;
        System.arraycopy(buf1, 0, buffer, offset, buf1.length);
        offset += buf1.length;
        System.arraycopy(buf2, 0, buffer, offset, buf2.length);
        return buffer;
    }

    /**
     * 字符交换
     *
     * @param len      长度
     * @param numBytes 字符
     */
    private static void checkLength(int len, int numBytes) {
        if (len < 0 || (len % numBytes) != 0)
            throw new IllegalArgumentException("length: " + len);
    }

    /**
     * 字符交换
     *
     * @param data byte值
     * @param a    字符A
     * @param b    字符B
     */
    private static void swap(byte[] data, int a, int b) {
        byte t = data[a];
        data[a] = data[b];
        data[b] = t;
    }

    /**
     * 字符交换
     *
     * @param b1 byte值
     * @param b2 byte值
     */
    private static void swapLastFirst(byte[] b1, byte[] b2) {
        int last = b1.length - 1;
        byte t = b2[0];
        b2[0] = b1[last];
        b1[last] = t;
    }

}
