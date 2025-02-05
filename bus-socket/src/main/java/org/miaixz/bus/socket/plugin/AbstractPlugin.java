/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org sandao and other contributors.             ~
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
package org.miaixz.bus.socket.plugin;

import java.nio.channels.AsynchronousSocketChannel;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.socket.Plugin;
import org.miaixz.bus.socket.Session;
import org.miaixz.bus.socket.Status;

/**
 * 抽象插件
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractPlugin<T> implements Plugin<T> {

    private final static char[] DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
            'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };

    /**
     * 将字节转换成16进制显示
     *
     * @param b byte
     * @return String
     */
    public static String toHex(byte b) {
        final char[] buf = new char[Normal._2];
        for (int i = 0; i < buf.length; i++) {
            buf[1 - i] = DIGITS[b & 0xF];
            b = (byte) (b >>> 4);
        }
        return new String(buf);
    }

    /**
     * 以16进制 打印字节数组
     *
     * @param bytes byte[]
     */
    public static String toHexString(final byte[] bytes) {
        final StringBuilder buffer = new StringBuilder(bytes.length);
        buffer.append("\r\n\t\t   0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f\r\n");
        int startIndex = 0;
        int column = 0;
        for (int i = 0; i < bytes.length; i++) {
            column = i % 16;
            switch (column) {
            case 0:
                startIndex = i;
                buffer.append(fixHexString(Integer.toHexString(i), 8)).append(": ");
                buffer.append(toHex(bytes[i]));
                buffer.append(' ');
                break;
            case 15:
                buffer.append(toHex(bytes[i]));
                buffer.append(" ; ");
                buffer.append(filterString(bytes, startIndex, column + 1));
                buffer.append("\r\n");
                break;
            default:
                buffer.append(toHex(bytes[i]));
                buffer.append(' ');
            }
        }
        if (column != 15) {
            for (int i = 0; i < 15 - column; i++) {
                buffer.append("   ");
            }
            buffer.append("; ").append(filterString(bytes, startIndex, column + 1));
            buffer.append("\r\n");
        }

        return buffer.toString();
    }

    /**
     * 过滤掉字节数组中0x0 - 0x1F的控制字符，生成字符串
     *
     * @param bytes  byte[]
     * @param offset int
     * @param count  int
     * @return String
     */
    private static String filterString(final byte[] bytes, final int offset, final int count) {
        final byte[] buffer = new byte[count];
        System.arraycopy(bytes, offset, buffer, 0, count);
        for (int i = 0; i < count; i++) {
            if (buffer[i] >= 0x0 && buffer[i] <= 0x1F) {
                buffer[i] = 0x2e;
            }
        }
        return new String(buffer);
    }

    /**
     * 将hexStr格式化成length长度16进制数，并在后边加上h
     *
     * @param hexStr String
     * @return String
     */
    private static String fixHexString(final String hexStr, final int length) {
        if (hexStr == null || hexStr.length() == 0) {
            return "00000000h";
        } else {
            final StringBuilder buf = new StringBuilder(length);
            final int strLen = hexStr.length();
            for (int i = 0; i < length - strLen; i++) {
                buf.append('0');
            }
            buf.append(hexStr).append('h');
            return buf.toString();
        }
    }

    @Override
    public boolean process(Session session, T data) {
        return true;
    }

    @Override
    public void stateEvent(Status status, Session session, Throwable throwable) {

    }

    @Override
    public AsynchronousSocketChannel shouldAccept(AsynchronousSocketChannel channel) {
        return channel;
    }

    @Override
    public void afterRead(Session session, int readSize) {

    }

    @Override
    public void afterWrite(Session session, int writeSize) {

    }

    @Override
    public void beforeRead(Session session) {

    }

    @Override
    public void beforeWrite(Session session) {

    }

}
