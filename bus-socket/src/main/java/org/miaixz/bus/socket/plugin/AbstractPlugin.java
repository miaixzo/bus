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

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.socket.Plugin;
import org.miaixz.bus.socket.Session;
import org.miaixz.bus.socket.Status;

import java.nio.channels.AsynchronousSocketChannel;

/**
 * An abstract base class for plugins, providing default implementations for the {@link Plugin} and
 * {@link org.miaixz.bus.socket.Monitor} interfaces. This class also includes utility methods for hexadecimal string
 * conversion.
 *
 * @param <T> the type of message object entity handled by this plugin
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractPlugin<T> implements Plugin<T> {

    /**
     * Hexadecimal digits for conversion.
     */
    private final static char[] DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
            'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };

    /**
     * Converts a single byte to its hexadecimal string representation.
     *
     * @param b the byte to convert
     * @return the hexadecimal string representation of the byte
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
     * Converts a byte array to its hexadecimal string representation, formatted for display.
     *
     * @param bytes the byte array to convert
     * @return a formatted hexadecimal string representation of the byte array
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
     * Filters out control characters (0x0 - 0x1F) from a byte array and converts it to a string.
     *
     * @param bytes  the byte array to filter
     * @param offset the starting offset in the byte array
     * @param count  the number of bytes to filter
     * @return a string with control characters replaced by periods
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
     * Formats a hexadecimal string to a specified length, padding with leading zeros if necessary, and appends 'h'.
     *
     * @param hexStr the hexadecimal string to format
     * @param length the desired length of the formatted string (excluding 'h')
     * @return the formatted hexadecimal string
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

    /**
     * Description inherited from parent class or interface.
     * <p>
     * This default implementation always returns {@code true}, allowing the message processing pipeline to continue.
     * Subclasses can override this method to implement custom message filtering or preprocessing logic.
     * </p>
     *
     * @param session the communication session through which the message was received
     * @param data    the message object to be processed
     * @return {@code true} to allow the message to proceed to the next handler, {@code false} to stop processing
     */
    @Override
    public boolean process(Session session, T data) {
        return true;
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * This default implementation does nothing. Subclasses can override to handle specific state events.
     * </p>
     *
     * @param status    the status event that occurred
     * @param session   the communication session associated with the event
     * @param throwable the throwable associated with the event, if any
     */
    @Override
    public void stateEvent(Status status, Session session, Throwable throwable) {

    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * This default implementation returns the channel unchanged, allowing all connections. Subclasses can override to
     * implement connection filtering or wrapping logic.
     * </p>
     *
     * @param channel the asynchronous socket channel representing the incoming connection
     * @return the accepted channel, or {@code null} to reject the connection
     */
    @Override
    public AsynchronousSocketChannel shouldAccept(AsynchronousSocketChannel channel) {
        return channel;
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * This default implementation does nothing. Subclasses can override to perform custom post-read operations.
     * </p>
     *
     * @param session  the communication session from which data was read
     * @param readSize the number of bytes read from the channel
     */
    @Override
    public void afterRead(Session session, int readSize) {

    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * This default implementation does nothing. Subclasses can override to perform custom post-write operations.
     * </p>
     *
     * @param session   the communication session to which data was written
     * @param writeSize the number of bytes written to the channel
     */
    @Override
    public void afterWrite(Session session, int writeSize) {

    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * This default implementation does nothing. Subclasses can override to perform custom pre-read operations.
     * </p>
     *
     * @param session the communication session from which data will be read
     */
    @Override
    public void beforeRead(Session session) {

    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * This default implementation does nothing. Subclasses can override to perform custom pre-write operations.
     * </p>
     *
     * @param session the communication session to which data will be written
     */
    @Override
    public void beforeWrite(Session session) {

    }

}
