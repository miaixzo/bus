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

/**
 * Represents the MultipartInputStream type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class MultipartInputStream extends FilterInputStream {

    /**
     * The boundary value.
     */
    private final byte[] boundary;

    /**
     * The buffer value.
     */
    private final byte[] buffer;

    /**
     * The mark buffer value.
     */
    private byte[] markBuffer;

    /**
     * The rpos value.
     */
    private int rpos;

    /**
     * The markpos value.
     */
    private int markpos;

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
     */
    protected MultipartInputStream(InputStream in, String boundary) {
        super(in);
        this.boundary = boundary.getBytes();
        this.buffer = new byte[this.boundary.length];
        this.rpos = buffer.length;
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
    private static void readFully(InputStream in, byte[] b, int off, int len) throws IOException {
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
        if (srcEnd < 0 || s.charAt(0) != '\"') {
            return s;
        }
        if (srcEnd == 0 || s.charAt(srcEnd) != '\"') { // missing closing quote
            srcEnd++;
        }
        char[] cs = new char[srcEnd - 1];
        s.getChars(1, srcEnd, cs, 0);
        boolean backslash = false;
        int count = 0;
        for (char c : cs) {
            if (!(backslash = !backslash && c == '\\')) {
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
        return isBoundary() ? -1 : (buffer[rpos++] & 0xff);
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

        int l = Math.min(remaining(), len);
        System.arraycopy(buffer, rpos, b, off, l);
        rpos += l;
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

        long l = Math.min(remaining(), n);
        rpos += l;
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
        markBuffer = buffer.clone();
        markpos = rpos;
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
        System.arraycopy(markBuffer, 0, buffer, 0, buffer.length);
        rpos = markpos;
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
        while (!isBoundary())
            rpos += remaining();
    }

    /**
     * Determines whether zip.
     *
     * @return true if the condition is met; otherwise false.
     * @throws IOException if the operation cannot be completed.
     */
    public boolean isZIP() throws IOException {
        return !isBoundary() && buffer[rpos] == 'P' && buffer[rpos + 1] == 'K';
    }

    /**
     * Determines whether boundary.
     *
     * @return true if the condition is met; otherwise false.
     * @throws IOException if the operation cannot be completed.
     */
    private boolean isBoundary() throws IOException {
        if (boundarySeen)
            return true;

        if (rpos < buffer.length) {
            if (buffer[rpos] != boundary[0])
                return false;

            System.arraycopy(buffer, rpos, buffer, 0, buffer.length - rpos);
        }
        readFully(in, buffer, buffer.length - rpos, rpos);
        rpos = 0;

        for (int i = 0; i < buffer.length; i++)
            if (buffer[i] != boundary[i])
                return false;

        boundarySeen = true;
        return true;
    }

    /**
     * Executes the remaining operation.
     *
     * @return the operation result.
     */
    private int remaining() {
        for (int i = rpos + 1; i < buffer.length; i++)
            if (buffer[i] == boundary[0])
                return i - rpos;

        return buffer.length - rpos;
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
            String value = "";
            int endName = name.indexOf(':');
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
        OUTER: while (!isBoundary()) {
            field.growBuffer(buffer.length);
            while (rpos < buffer.length)
                if (!field.append(buffer[rpos++]))
                    break OUTER;
        }
        return !field.isEmpty();
    }

    /**
     * Represents the Field type.
     *
     * @author Kimi Liu
     * @since Java 21+
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
            if (b == '\n' && length > 0 && buffer[length - 1] == '\r') {
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
