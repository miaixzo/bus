/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
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
package org.aoju.bus.core.toolkit;

import org.aoju.bus.core.lang.Charset;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;

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
 * @version 6.2.2
 * @since JDK 1.8+
 */
public class ByteKit {

    /**
     * int转byte
     *
     * @param data int值
     * @return byte值
     */
    public static byte getByte(int data) {
        return (byte) data;
    }

    /**
     * int转byte数组
     * 默认以小端序转换
     *
     * @param data int值
     * @return byte数组
     */
    public static byte[] getBytes(int data) {
        return getBytes(data, ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * int转byte数组
     * 默认以小端序转换
     *
     * @param data int值
     * @return byte数组
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
     * @param intValue  int值
     * @param byteOrder 端序
     * @return byte数组
     */
    public static byte[] getBytes(int intValue, ByteOrder byteOrder) {
        if (ByteOrder.LITTLE_ENDIAN == byteOrder) {
            return new byte[]{
                    (byte) ((intValue >> 24) & 0xFF),
                    (byte) ((intValue >> 16) & 0xFF),
                    (byte) ((intValue >> 8) & 0xFF),
                    (byte) (intValue & 0xFF) //
            };
        } else {
            return new byte[]{
                    (byte) (intValue & 0xFF),
                    (byte) ((intValue >> 8) & 0xFF),
                    (byte) ((intValue >> 16) & 0xFF),
                    (byte) ((intValue >> 24) & 0xFF)
            };
        }
    }

    /**
     * long转byte数组
     * 默认以小端序转换
     *
     * @param data long值
     * @return byte数组
     */
    public static byte[] getBytes(long data) {
        return getBytes(data, ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * long转byte数组
     *
     * @param data      long值
     * @param byteOrder 端序
     * @return byte数组
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
     * @param data
     * @return
     */
    public static byte[] getBytes(float data) {
        return getBytes(data, ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * double转byte数组
     *
     * @param data      double值
     * @param byteOrder 端序
     * @return byte数组
     */
    public static byte[] getBytes(float data, ByteOrder byteOrder) {
        return getBytes(Float.floatToIntBits(data), byteOrder);
    }

    /**
     * double转byte数组
     * 默认以小端序转换
     *
     * @param data double值
     * @return byte数组
     */
    public static byte[] getBytes(double data) {
        return getBytes(data, ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * double转byte数组
     *
     * @param data      double值
     * @param byteOrder 端序
     * @return byte数组
     */
    public static byte[] getBytes(double data, ByteOrder byteOrder) {
        return getBytes(Double.doubleToLongBits(data), byteOrder);
    }

    /**
     * short转byte数组
     * 默认以小端序转换
     *
     * @param data short值
     * @return byte数组
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
     * @return byte数组
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
     * @param data
     * @return
     */
    public static byte[] getBytes(char data) {
        byte[] bytes = new byte[2];
        bytes[0] = (byte) (data);
        bytes[1] = (byte) (data >> 8);
        return bytes;
    }

    /**
     * @param data
     * @return
     */
    public static byte[] getBytes(String data) {
        return getBytes(data, Charset.DEFAULT_GBK);
    }

    /**
     * @param data
     * @param charsetName
     * @return
     */
    public static byte[] getBytes(String data, String charsetName) {
        return data.getBytes(Charset.charset(charsetName));
    }

    /**
     * @param data
     * @return
     */
    public static byte[] getBytes(char[] data) {
        CharBuffer cb = CharBuffer.allocate(data.length);
        cb.put(data);
        cb.flip();
        ByteBuffer bb = Charset.UTF_8.encode(cb);
        return bb.array();
    }

    /**
     * byte转无符号int
     *
     * @param byteValue byte值
     * @return 无符号int值
     */
    public static int getInt(byte byteValue) {
        // Java 总是把 byte 当做有符处理；我们可以通过将其和 0xFF 进行二进制与得到它的无符值
        return byteValue & 0xFF;
    }

    /**
     * byte[]转int值
     * 默认以小端序转换
     *
     * @param bytes byte数组
     * @return int值
     */
    public static int getInt(byte[] bytes) {
        return getInt(bytes, ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * byte[]转int值
     * 自定义端序
     *
     * @param bytes     byte数组
     * @param byteOrder 端序
     * @return int值
     */
    public static int getInt(byte[] bytes, ByteOrder byteOrder) {
        if (ByteOrder.LITTLE_ENDIAN == byteOrder) {
            return bytes[3] & 0xFF |
                    (bytes[2] & 0xFF) << 8 |
                    (bytes[1] & 0xFF) << 16 |
                    (bytes[0] & 0xFF) << 24;
        } else {
            return bytes[0] & 0xFF | //
                    (bytes[1] & 0xFF) << 8 |
                    (bytes[2] & 0xFF) << 16 |
                    (bytes[3] & 0xFF) << 24;
        }
    }

    /**
     * byte数组转long
     *
     * @param bytes byte数组
     * @return long值
     */
    public static long getLong(byte[] bytes) {
        return getLong(bytes, ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * byte数组转long
     *
     * @param bytes     byte数组
     * @param byteOrder 端序
     * @return long值
     */
    public static long getLong(byte[] bytes, ByteOrder byteOrder) {
        long values = 0;
        if (ByteOrder.LITTLE_ENDIAN == byteOrder) {
            for (int i = 0; i < Long.BYTES; i++) {
                values <<= Byte.SIZE;
                values |= (bytes[i] & 0xff);
            }
        } else {
            for (int i = (Long.BYTES - 1); i >= 0; i--) {
                values <<= Byte.SIZE;
                values |= (bytes[i] & 0xff);
            }
        }

        return values;
    }

    /**
     * byte数组转Double
     * 默认以小端序转换
     *
     * @param bytes byte数组
     * @return long值
     */
    public static float getFloat(byte[] bytes) {
        return getFloat(bytes, ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * byte数组转double
     * 自定义端序
     *
     * @param bytes     byte数组
     * @param byteOrder 端序
     * @return long值
     */
    public static float getFloat(byte[] bytes, ByteOrder byteOrder) {
        return Float.intBitsToFloat(getInt(bytes, byteOrder));
    }


    /**
     * byte数组转Double
     * 默认以小端序转换
     *
     * @param bytes byte数组
     * @return long值
     */
    public static double getDouble(byte[] bytes) {
        return getDouble(bytes, ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * byte数组转double
     * 自定义端序
     *
     * @param bytes     byte数组
     * @param byteOrder 端序
     * @return long值
     */
    public static double getDouble(byte[] bytes, ByteOrder byteOrder) {
        return Double.longBitsToDouble(getLong(bytes, byteOrder));
    }

    /**
     * byte数组转short
     * 默认以小端序转换
     *
     * @param bytes byte数组
     * @return short值
     */
    public static short getShort(byte[] bytes) {
        return getShort(bytes, ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * byte数组转short
     * 自定义端序
     *
     * @param bytes     byte数组
     * @param byteOrder 端序
     * @return short值
     */
    public static short getShort(byte[] bytes, ByteOrder byteOrder) {
        if (ByteOrder.LITTLE_ENDIAN == byteOrder) {
            return (short) (bytes[1] & 0xff | (bytes[0] & 0xff) << Byte.SIZE);
        } else {
            return (short) (bytes[0] & 0xff | (bytes[1] & 0xff) << Byte.SIZE);
        }
    }

    /**
     * @param bytes
     * @return
     */
    public static char getChar(byte[] bytes) {
        return (char) ((0xff & bytes[0]) | (0xff00 & (bytes[1] << 8)));
    }

    /**
     * @param bytes
     * @return
     */
    public static String getString(byte[] bytes) {
        return getString(bytes, Charset.DEFAULT_GBK);
    }

    /**
     * @param bytes
     * @param charsetName
     * @return
     */
    public static String getString(byte[] bytes, String charsetName) {
        return new String(bytes, Charset.charset(charsetName));
    }

    /**
     * 根据分隔符拆分字节数组
     *
     * @param arr       字节数组
     * @param separator 分隔符
     * @return the byte
     */
    public static byte[][] split(byte[] arr, byte separator) {
        int count = countOf(arr, separator);
        byte[][] result = new byte[count + 1][];
        int index = indexOf(arr, separator, 0, arr.length);
        if (index < 0) {
            result[0] = arr;
        } else {
            result[0] = subArray(arr, 0, index);
            for (int i = 1; i <= count; i++) {
                int from = index + 1;
                index = indexOf(arr, separator, from, arr.length);
                if (index < from) {
                    index = arr.length;
                }
                result[i] = subArray(arr, from, index);
            }
        }
        return result;
    }


    /**
     * 拆分byte数组为几个等份(最后一份可能小于len)
     *
     * @param array 数组
     * @param len   每个小节的长度
     * @return 拆分后的数组
     */
    public static byte[][] split(byte[] array, int len) {
        int x = array.length / len;
        int y = array.length % len;
        int z = 0;
        if (y != 0) {
            z = 1;
        }
        byte[][] arrays = new byte[x + z][];
        byte[] arr;
        for (int i = 0; i < x + z; i++) {
            arr = new byte[len];
            if (i == x + z - 1 && y != 0) {
                System.arraycopy(array, i * len, arr, 0, y);
            } else {
                System.arraycopy(array, i * len, arr, 0, len);
            }
            arrays[i] = arr;
        }
        return arrays;
    }

    /**
     * 截取子数组
     *
     * @param arr  字节数组
     * @param from 开始下标（包含）
     * @return the byte
     */
    public static byte[] subArray(byte[] arr, int from) {
        return subArray(arr, from, arr.length);
    }


    /**
     * 截取子数组
     *
     * @param arr  字节数组
     * @param from 开始下标（包含）
     * @param to   结束下标（不包含）
     * @return the byte
     */
    public static byte[] subArray(byte[] arr, int from, int to) {
        byte[] result = new byte[to - from];
        if (to > from) {
            System.arraycopy(arr, from, result, 0, result.length);
        }
        return result;
    }

    /**
     * 连接多个字节数组
     *
     * @param hyphen 连字符
     * @param arrs   二维数组
     * @return the byte
     */
    public static byte[] join(byte hyphen, byte[]... arrs) {
        int length = 0;
        for (byte[] arr : arrs) {
            length += arr.length;
        }
        byte[] result = new byte[length + arrs.length - 1];
        int index = 0;
        for (int i = 0; i < arrs.length; i++) {
            byte[] arr = arrs[i];
            System.arraycopy(arr, 0, result, index, arr.length);
            index += arr.length;
            if (i < arrs.length - 1) {
                result[index] = hyphen;
                index++;
            }
        }
        return result;
    }

    /**
     * 连接多个字节数组
     *
     * @param arrs 二维数组
     * @return the byte
     */
    public static byte[] join(byte[]... arrs) {
        int length = 0;
        for (byte[] arr : arrs) {
            length += arr.length;
        }
        byte[] result = new byte[length];
        int index = 0;
        for (byte[] arr : arrs) {
            System.arraycopy(arr, 0, result, index, arr.length);
            index += arr.length;
        }
        return result;
    }

    /**
     * 首尾去掉空字符
     *
     * @param src   字节数组
     * @param empty 空数组
     * @return the byte
     */
    public static byte[] trim(byte[] src, byte empty) {
        return trim(src, empty, 0, src.length);
    }

    /**
     * 从 from 到 to 截取子串 并 首尾去掉空字符
     *
     * @param src   字节数组
     * @param empty 空字节
     * @param from  开始下标（包含）
     * @param to    结束下标（不包含）
     * @return the byte
     */
    public static byte[] trim(byte[] src, byte empty, int from, int to) {
        while (from < src.length - 1 && src[from] == empty) {
            from++;
        }
        while (to > from && src[to - 1] == empty) {
            to--;
        }
        return subArray(src, from, to);
    }


    /**
     * 寻找目标字节在字节数组中的下标
     *
     * @param arr    字节数组
     * @param target 目标字节
     * @param from   检索开始下标（包含）
     * @param to     检索结束下标（不包含）
     * @return 找不到则返回-1
     */
    public static int indexOf(byte[] arr, byte target, int from, int to) {
        for (int i = from; i < to; i++) {
            if (arr[i] == target) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 统计目标字节在字节数组中出现的次数
     *
     * @param arr    字节数组
     * @param target 目标字节
     * @return the int
     */
    public static int countOf(byte[] arr, byte target) {
        int count = 0;
        for (byte b : arr) {
            if (b == target) {
                count++;
            }
        }
        return count;
    }


    /**
     * 解析 BCD 码
     *
     * @param src  字节数组
     * @param from 开始下标（包含）
     * @param to   结束下标（不包含）
     * @return the string
     */
    public static String bcd(byte[] src, int from, int to) {
        char[] chars = new char[2 * (to - from)];
        for (int i = from; i < to; i++) {
            int b = unsigned(src[i]);
            chars[2 * (i - from)] = (char) ((b >> 4) + 0x30);
            chars[2 * (i - from) + 1] = (char) ((b & 0xF) + 0x30);
        }
        return new String(chars);
    }

    /**
     * 无符号整数
     *
     * @param value 字节
     * @return the int
     */
    public static int unsigned(byte value) {
        if (value >= 0) {
            return value;
        }
        return 256 + value;
    }

    /**
     * 异或值，返回
     *
     * @param bytes 数组
     * @return 异或值
     */
    public static int xor(byte[] bytes) {
        int temp = 0;
        if (null != bytes) {
            for (int i = 0; i < bytes.length; i++) {
                temp ^= bytes[i];
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
     * 将{@link Number}转换为
     *
     * @param number 数字
     * @return bytes
     */
    public static byte[] getBytes(Number number) {
        if (number instanceof Double) {
            return getBytes((Double) number, ByteOrder.LITTLE_ENDIAN);
        } else if (number instanceof Long) {
            return getBytes((Long) number, ByteOrder.LITTLE_ENDIAN);
        } else if (number instanceof Integer) {
            return getBytes((Integer) number, ByteOrder.LITTLE_ENDIAN);
        } else if (number instanceof Short) {
            return getBytes((Short) number, ByteOrder.LITTLE_ENDIAN);
        } else {
            return getBytes(number.doubleValue(), ByteOrder.LITTLE_ENDIAN);
        }
    }

    public static int bytesToVR(byte[] bytes, int off) {
        return bytesToUShortBE(bytes, off);
    }

    public static int bytesToUShort(byte[] bytes, int off, boolean bigEndian) {
        return bigEndian ? bytesToUShortBE(bytes, off) : bytesToUShortLE(bytes, off);
    }

    public static int bytesToUShortBE(byte[] bytes, int off) {
        return ((bytes[off] & 255) << 8) + (bytes[off + 1] & 255);
    }

    public static int bytesToUShortLE(byte[] bytes, int off) {
        return ((bytes[off + 1] & 255) << 8) + (bytes[off] & 255);
    }

    public static int bytesToShortBE(byte[] bytes, int off) {
        return (bytes[off] << 8) + (bytes[off + 1] & 255);
    }

    public static int bytesToShortLE(byte[] bytes, int off) {
        return (bytes[off + 1] << 8) + (bytes[off] & 255);
    }


    public static int bytesToShort(byte[] bytes, int off, boolean bigEndian) {
        return bigEndian ? bytesToShortBE(bytes, off)
                : bytesToShortLE(bytes, off);
    }


    public static int bytesToInt(byte[] bytes, int off, boolean bigEndian) {
        return bigEndian ? bytesToIntBE(bytes, off) : bytesToIntLE(bytes, off);
    }

    public static int bytesToIntBE(byte[] bytes, int off) {
        return (bytes[off] << 24) + ((bytes[off + 1] & 255) << 16)
                + ((bytes[off + 2] & 255) << 8) + (bytes[off + 3] & 255);
    }

    public static int bytesToIntLE(byte[] bytes, int off) {
        return (bytes[off + 3] << 24) + ((bytes[off + 2] & 255) << 16)
                + ((bytes[off + 1] & 255) << 8) + (bytes[off] & 255);
    }

    public static int bytesToTag(byte[] bytes, int off, boolean bigEndian) {
        return bigEndian ? bytesToTagBE(bytes, off) : bytesToTagLE(bytes, off);
    }

    public static int bytesToTagBE(byte[] bytes, int off) {
        return bytesToIntBE(bytes, off);
    }

    public static int bytesToTagLE(byte[] bytes, int off) {
        return (bytes[off + 1] << 24) + ((bytes[off] & 255) << 16)
                + ((bytes[off + 3] & 255) << 8) + (bytes[off + 2] & 255);
    }

    public static float bytesToFloat(byte[] bytes, int off, boolean bigEndian) {
        return bigEndian ? bytesToFloatBE(bytes, off)
                : bytesToFloatLE(bytes, off);
    }

    public static float bytesToFloatBE(byte[] bytes, int off) {
        return Float.intBitsToFloat(bytesToIntBE(bytes, off));
    }

    public static float bytesToFloatLE(byte[] bytes, int off) {
        return Float.intBitsToFloat(bytesToIntLE(bytes, off));
    }

    public static long bytesToLong(byte[] bytes, int off, boolean bigEndian) {
        return bigEndian ? bytesToLongBE(bytes, off)
                : bytesToLongLE(bytes, off);
    }

    public static long bytesToLongBE(byte[] bytes, int off) {
        return ((long) bytes[off] << 56)
                + ((long) (bytes[off + 1] & 255) << 48)
                + ((long) (bytes[off + 2] & 255) << 40)
                + ((long) (bytes[off + 3] & 255) << 32)
                + ((long) (bytes[off + 4] & 255) << 24)
                + ((bytes[off + 5] & 255) << 16)
                + ((bytes[off + 6] & 255) << 8)
                + (bytes[off + 7] & 255);
    }

    public static long bytesToLongLE(byte[] bytes, int off) {
        return ((long) bytes[off + 7] << 56)
                + ((long) (bytes[off + 6] & 255) << 48)
                + ((long) (bytes[off + 5] & 255) << 40)
                + ((long) (bytes[off + 4] & 255) << 32)
                + ((long) (bytes[off + 3] & 255) << 24)
                + ((bytes[off + 2] & 255) << 16)
                + ((bytes[off + 1] & 255) << 8)
                + (bytes[off] & 255);
    }

    /**
     * double转byte数组
     * 默认以: {@link ByteOrder#BIG_ENDIAN }
     *
     * @param data byte值
     * @return byte数组
     */
    public static double bytesToDouble(byte[] data, int off, boolean bigEndian) {
        return bigEndian ? bytesToDoubleBE(data, off)
                : bytesToDoubleLE(data, off);
    }

    /**
     * byte数组转double
     * 默认以: {@link ByteOrder#BIG_ENDIAN }
     *
     * @param data byte值
     * @return byte数组
     */
    public static double bytesToDoubleBE(byte[] data, int off) {
        return Double.longBitsToDouble(bytesToLongBE(data, off));
    }

    /**
     * byte数组转double
     * 默认以: {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data byte值
     * @return byte数组
     */
    public static double bytesToDoubleLE(byte[] data, int off) {
        return Double.longBitsToDouble(bytesToLongLE(data, off));
    }

    /**
     * int转byte数组
     * 排序: {@link ByteOrder#BIG_ENDIAN } or {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data int值
     * @return byte数组
     */
    public static byte[] shortToBytes(int data, byte[] bytes, int off,
                                      boolean bigEndian) {
        return bigEndian ? shortToBytesBE(data, bytes, off) : shortToBytesLE(data, bytes, off);
    }

    /**
     * int转byte数组
     * 默认排序: {@link ByteOrder#BIG_ENDIAN }
     *
     * @param data int值
     * @return byte数组
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
     * @param data int值
     * @return byte数组
     */
    public static byte[] shortToBytesLE(int data, byte[] bytes, int off) {
        bytes[off + 1] = (byte) (data >> 8);
        bytes[off] = (byte) data;
        return bytes;
    }

    /**
     * int转byte数组
     * 排序: {@link ByteOrder#BIG_ENDIAN } or {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data int值
     * @return byte数组
     */
    public static byte[] intToBytes(int data, byte[] bytes, int off, boolean bigEndian) {
        return bigEndian ? intToBytesBE(data, bytes, off) : intToBytesLE(data, bytes, off);
    }

    /**
     * int转byte数组
     * 默认排序: {@link ByteOrder#BIG_ENDIAN }
     *
     * @param data int值
     * @return byte数组
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
     * @param data int值
     * @return byte数组
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
     * @param data int值
     * @return byte数组
     */
    public static byte[] tagToBytes(int data, byte[] bytes, int off,
                                    boolean bigEndian) {
        return bigEndian ? tagToBytesBE(data, bytes, off) : tagToBytesLE(data, bytes, off);
    }

    /**
     * int转byte数组
     * 默认排序: {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data int值
     * @return byte数组
     */
    public static byte[] tagToBytesLE(int data, byte[] bytes, int off) {
        bytes[off + 1] = (byte) (data >> 24);
        bytes[off] = (byte) (data >> 16);
        bytes[off + 3] = (byte) (data >> 8);
        bytes[off + 2] = (byte) data;
        return bytes;
    }

    /**
     * int转byte数组
     * 默认排序: {@link ByteOrder#BIG_ENDIAN }
     *
     * @param data int值
     * @return byte数组
     */
    public static byte[] tagToBytesBE(int data, byte[] bytes, int off) {
        return intToBytesBE(data, bytes, off);
    }

    /**
     * float转byte数组
     * 排序: {@link ByteOrder#BIG_ENDIAN } or {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data int值
     * @return byte数组
     */
    public static byte[] floatToBytes(float data, byte[] bytes, int off,
                                      boolean bigEndian) {
        return bigEndian ? floatToBytesBE(data, bytes, off) : floatToBytesLE(data, bytes, off);
    }

    /**
     * float转byte数组
     * 默认排序: {@link ByteOrder#BIG_ENDIAN }
     *
     * @param data int值
     * @return byte数组
     */
    public static byte[] floatToBytesBE(float data, byte[] bytes, int off) {
        return intToBytesBE(Float.floatToIntBits(data), bytes, off);
    }

    /**
     * float转byte数组
     * 默认排序: {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data int值
     * @return byte数组L
     */
    public static byte[] floatToBytesLE(float data, byte[] bytes, int off) {
        return intToBytesLE(Float.floatToIntBits(data), bytes, off);
    }

    /**
     * double转byte数组
     * 排序: {@link ByteOrder#BIG_ENDIAN } or {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data int值
     * @return byte数组
     */
    public static byte[] doubleToBytes(double data, byte[] bytes, int off,
                                       boolean bigEndian) {
        return bigEndian ? doubleToBytesBE(data, bytes, off)
                : doubleToBytesLE(data, bytes, off);
    }

    /**
     * double转byte数组
     * 默认排序: {@link ByteOrder#BIG_ENDIAN }
     *
     * @param data double值
     * @return byte数组
     */
    public static byte[] doubleToBytesBE(double data, byte[] bytes, int off) {
        return longToBytesBE(Double.doubleToLongBits(data), bytes, off);
    }

    /**
     * double转byte数组
     * 默认排序: {@link ByteOrder#LITTLE_ENDIAN }
     *
     * @param data double值
     * @return byte数组
     */
    public static byte[] doubleToBytesLE(double data, byte[] bytes, int off) {
        return longToBytesLE(Double.doubleToLongBits(data), bytes, off);
    }

    public static byte[] longToBytes(long l, byte[] bytes, int off,
                                     boolean bigEndian) {
        return bigEndian ? longToBytesBE(l, bytes, off)
                : longToBytesLE(l, bytes, off);
    }

    public static byte[] longToBytesBE(long l, byte[] bytes, int off) {
        bytes[off] = (byte) (l >> 56);
        bytes[off + 1] = (byte) (l >> 48);
        bytes[off + 2] = (byte) (l >> 40);
        bytes[off + 3] = (byte) (l >> 32);
        bytes[off + 4] = (byte) (l >> 24);
        bytes[off + 5] = (byte) (l >> 16);
        bytes[off + 6] = (byte) (l >> 8);
        bytes[off + 7] = (byte) l;
        return bytes;
    }

    public static byte[] longToBytesLE(long l, byte[] bytes, int off) {
        bytes[off + 7] = (byte) (l >> 56);
        bytes[off + 6] = (byte) (l >> 48);
        bytes[off + 5] = (byte) (l >> 40);
        bytes[off + 4] = (byte) (l >> 32);
        bytes[off + 3] = (byte) (l >> 24);
        bytes[off + 2] = (byte) (l >> 16);
        bytes[off + 1] = (byte) (l >> 8);
        bytes[off] = (byte) l;
        return bytes;
    }

    public static byte[] swapInts(byte[] b, int off, int len) {
        checkLength(len, 4);
        for (int i = off, n = off + len; i < n; i += 4) {
            swap(b, i, i + 3);
            swap(b, i + 1, i + 2);
        }
        return b;
    }

    public static byte[] swapLongs(byte[] b, int off, int len) {
        checkLength(len, 8);
        for (int i = off, n = off + len; i < n; i += 8) {
            swap(b, i, i + 7);
            swap(b, i + 1, i + 6);
            swap(b, i + 2, i + 5);
            swap(b, i + 3, i + 4);
        }
        return b;
    }

    public static byte[][] swapShorts(byte[][] bs) {
        int carry = 0;
        for (int i = 0; i < bs.length; i++) {
            byte[] b = bs[i];
            if (carry != 0)
                swapLastFirst(bs[i - 1], b);
            int len = b.length - carry;
            swapShorts(b, carry, len & ~1);
            carry = len & 1;
        }
        return bs;
    }

    public static byte[] swapShorts(byte[] b, int off, int len) {
        checkLength(len, 2);
        for (int i = off, n = off + len; i < n; i += 2)
            swap(b, i, i + 1);
        return b;
    }


    public static void bytesToShortLE(byte[] b, short[] s, int off, int len) {
        int boff = 0;
        for (int j = 0; j < len; j++) {
            int b0 = b[boff + 1];
            int b1 = b[boff] & 0xff;
            s[off + j] = (short) ((b0 << 8) | b1);
            boff += 2;
        }
    }

    public static void bytesToShortsBE(byte[] b, short[] s, int off, int len) {
        int boff = 0;
        for (int j = 0; j < len; j++) {
            int b0 = b[boff];
            int b1 = b[boff + 1] & 0xff;
            s[off + j] = (short) ((b0 << 8) | b1);
            boff += 2;
        }
    }

    public static void bytesToShort(byte[] b, short[] s, int off, int len, boolean bigEndian) {
        if (bigEndian)
            bytesToShortsBE(b, s, off, len);
        else
            bytesToShortLE(b, s, off, len);
    }

    private static void checkLength(int len, int numBytes) {
        if (len < 0 || (len % numBytes) != 0)
            throw new IllegalArgumentException("length: " + len);
    }

    private static void swap(byte[] bytes, int a, int b) {
        byte t = bytes[a];
        bytes[a] = bytes[b];
        bytes[b] = t;
    }

    private static void swapLastFirst(byte[] b1, byte[] b2) {
        int last = b1.length - 1;
        byte t = b2[0];
        b2[0] = b1[last];
        b1[last] = t;
    }

}
