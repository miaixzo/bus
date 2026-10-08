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
package org.miaixz.bus.image.galaxy.media;

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;

/**
 * Represents the MultipartInputStream type.
 *
 * @author Kimi Liu
 */
public class MultipartInputStream extends FilterInputStream {

    /**
     * The boundary value.
     */
    private final byte[] boundary;

    /**
     * The buffer values.
     */
    private final byte[][] buffers = new byte[2][];

    /**
     * The mark buffer value.
     */
    private byte[] markBuffer;

    /**
     * The active read buffer value.
     */
    private int rbuf;

    /**
     * The rpos value.
     */
    private int rpos;

    /**
     * The boundary seen value.
     */
    private boolean boundarySeen;

    /**
     * The mark boundary seen value.
     */
    private boolean markBoundarySeen;

    /**
     * Creates a new instance.
     *
     * @param in       the in.
     * @param boundary the boundary.
     * @throws IOException if the operation cannot be completed.
     */
    public MultipartInputStream(InputStream in, String boundary) throws IOException {
        this(in, boundary.getBytes());
    }

    /**
     * Creates a new instance.
     *
     * @param in       the in.
     * @param boundary the boundary.
     * @throws IOException if the operation cannot be completed.
     */
    MultipartInputStream(InputStream in, byte[] boundary) throws IOException {
        this(in, boundary, new byte[boundary.length]);
        readFully(in, this.buffers[0], 0, boundary.length);
    }

    /**
     * Creates a new instance.
     *
     * @param in       the in.
     * @param boundary the boundary.
     * @param b0       the first buffer.
     */
    MultipartInputStream(InputStream in, byte[] boundary, byte[] b0) {
        super(in);
        this.boundary = boundary;
        this.buffers[0] = b0;
        this.buffers[1] = new byte[this.boundary.length];
        this.markBuffer = new byte[this.boundary.length];
    }

    /**
     * Reads the fully.
     *
     * @param in  the in.
     * @param b   the b.
     * @param off the off.
     * @param len the len.
     * @throws IOException if the operation cannot be completed.
     */
    static void readFully(InputStream in, byte[] b, int off, int len) throws IOException {
        if (off < 0 || len < 0 || off + len > b.length)
            throw new IndexOutOfBoundsException();
        while (len > 0) {
            int count = in.read(b, off, len);
            if (count < 0)
                throw new EOFException();
            off += count;
            len -= count;
        }
    }

    /**
     * Executes the unquote operation.
     *
     * @param s the s.
     * @return the operation result.
     */
    private static String unquote(String s) {
        int srcEnd = s.length() - 1;
        if (srcEnd < 0 || s.charAt(0) != Symbol.C_DOUBLE_QUOTES) {
            return s;
        }
        if (srcEnd == 0 || s.charAt(srcEnd) != Symbol.C_DOUBLE_QUOTES) { // missing closing quote
            srcEnd++;
        }
        char[] cs = new char[srcEnd - 1];
        s.getChars(1, srcEnd, cs, 0);
        boolean backslash = false;
        int count = 0;
        for (char c : cs) {
            if (!(backslash = !backslash && c == Symbol.C_BACKSLASH)) {
                cs[count++] = c;
            }
        }
        return new String(cs, 0, count);
    }

    /**
     * Executes the read operation.
     *
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public int read() throws IOException {
        int b;
        if (isBoundary() || (b = in.read()) == -1)
            return -1;

        buffers[1 - rbuf][rpos] = (byte) b;
        b = buffers[rbuf][rpos++] & 0xff;
        switchBufferOnEndOfBuffer();
        return b;
    }

    /**
     * Switches the active buffer when the end of the current buffer is reached.
     */
    private void switchBufferOnEndOfBuffer() {
        if (rpos >= boundary.length) {
            rbuf = 1 - rbuf;
            rpos = 0;
        }
    }

    /**
     * Executes the read operation.
     *
     * @param b   the b.
     * @param off the off.
     * @param len the len.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (isBoundary())
            return -1;

        int l = Math.min(remaining(boundary[0], 1), len);
        System.arraycopy(buffers[rbuf], rpos, b, off, l);
        readFully(in, buffers[1 - rbuf], rpos, l);
        rpos += l;
        switchBufferOnEndOfBuffer();
        return l;
    }

    /**
     * Executes the skip operation.
     *
     * @param n the n.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public long skip(long n) throws IOException {
        if (isBoundary())
            return 0L;

        int l = (int) Math.min(remaining(boundary[0], 1), n);
        readFully(in, buffers[1 - rbuf], rpos, l);
        rpos += l;
        switchBufferOnEndOfBuffer();
        return l;
    }

    /**
     * Executes the mark operation.
     *
     * @param readlimit the readlimit.
     */
    @Override
    public synchronized void mark(int readlimit) {
        super.mark(readlimit);
        System.arraycopy(buffers[0], rpos, markBuffer, 0, markBuffer.length - rpos);
        System.arraycopy(buffers[1], 0, markBuffer, markBuffer.length - rpos, rpos);
        markBoundarySeen = boundarySeen;
    }

    /**
     * Executes the reset operation.
     *
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public synchronized void reset() throws IOException {
        super.reset();
        System.arraycopy(markBuffer, 0, buffers[0], 0, markBuffer.length);
        rbuf = 0;
        rpos = 0;
        boundarySeen = markBoundarySeen;
    }

    /**
     * Executes the close operation.
     *
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public void close() throws IOException {
        // NOOP
    }

    /**
     * Executes the skip all operation.
     *
     * @throws IOException if the operation cannot be completed.
     */
    public void skipAll() throws IOException {
        while (!isBoundary()) {
            int l = remaining(boundary[0], 1);
            readFully(in, buffers[1 - rbuf], rpos, l);
            rpos += l;
            switchBufferOnEndOfBuffer();
        }
    }

    /**
     * Determines whether zip.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isZIP() {
        return !isBoundary() && buffers[rbuf][rpos] == 'P'
                && (rpos + 1 < boundary.length ? buffers[rbuf][rpos + 1] : buffers[1 - rbuf][0]) == 'K';
    }

    /**
     * Determines whether boundary.
     *
     * @return true if the condition is met; otherwise false.
     */
    private boolean isBoundary() {
        if (boundarySeen)
            return true;

        for (int i = 0, j = rpos; j < boundary.length;)
            if (buffers[rbuf][j++] != boundary[i++])
                return false;

        for (int i = boundary.length - rpos, j = 0; j < rpos;)
            if (buffers[1 - rbuf][j++] != boundary[i++])
                return false;

        boundarySeen = true;
        return true;
    }

    /**
     * Executes the remaining operation.
     *
     * @return the operation result.
     */
    private int remaining(byte ch1, int min) {
        for (int i = rpos + min; i < boundary.length; i++)
            if (buffers[rbuf][i] == ch1)
                return i - rpos;

        return boundary.length - rpos;
    }

    /**
     * Reads the header params.
     *
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    public Map<String, List<String>> readHeaderParams() throws IOException {
        Map<String, List<String>> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        Field field = new Field();
        while (readHeaderParam(field)) {
            String name = field.toString();
            String value = Normal.EMPTY;
            int endName = name.indexOf(Symbol.C_COLON);
            if (endName != -1) {
                value = unquote(name.substring(endName + 1).trim());
                name = name.substring(0, endName);
            }
            List<String> list = map.get(name);
            if (list == null) {
                map.put(name.toLowerCase(), list = new ArrayList<>(1));
            }
            list.add(value);
        }
        return map;
    }

    /**
     * Reads the header param.
     *
     * @param field the field.
     * @return true if the condition is met; otherwise false.
     * @throws IOException if the operation cannot be completed.
     */
    private boolean readHeaderParam(Field field) throws IOException {
        field.reset();
        boolean append = true;
        while (append) {
            int l = remaining((byte) Symbol.C_LF, 0);
            if (rpos + l < boundary.length)
                l++;
            int i = rpos;
            field.growBuffer(l);
            while (l-- > 0 && (append = field.append(buffers[rbuf][i++])))
                ;
            readFully(in, buffers[1 - rbuf], rpos, i - rpos);
            rpos = i;
            switchBufferOnEndOfBuffer();
        }
        return !field.isEmpty();
    }

    /**
     * Represents the Field type.
     *
     * @author Kimi Liu
     */
    private static final class Field {

        /**
         * The buffer value.
         */
        byte[] buffer = new byte[256];

        /**
         * The length value.
         */
        int length;

        /**
         * Executes the reset operation.
         */
        void reset() {
            length = 0;
        }

        /**
         * Determines whether empty.
         *
         * @return true if the condition is met; otherwise false.
         */
        boolean isEmpty() {
            return length == 0;
        }

        /**
         * Executes the grow buffer operation.
         *
         * @param grow the grow.
         */
        void growBuffer(int grow) {
            if (length + grow > buffer.length) {
                byte[] copy = new byte[length + grow];
                System.arraycopy(buffer, 0, copy, 0, length);
                buffer = copy;
            }
        }

        /**
         * Executes the append operation.
         *
         * @param b the b.
         * @return true if the condition is met; otherwise false.
         */
        boolean append(byte b) {
            if (b == Symbol.C_LF && length > 0 && buffer[length - 1] == Symbol.C_CR) {
                length--;
                return false;
            }

            buffer[length++] = b;
            return true;
        }

        /**
         * Returns the string representation.
         *
         * @return the string representation.
         */
        public String toString() {
            return new String(buffer, 0, length);
        }

    }

}
