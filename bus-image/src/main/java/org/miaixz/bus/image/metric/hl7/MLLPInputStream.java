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
package org.miaixz.bus.image.metric.hl7;

import java.io.*;

/**
 * Represents the MLLPInputStream type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class MLLPInputStream extends BufferedInputStream {

    /**
     * The som value.
     */
    private static final int SOM = 0x0b; // Message Start
    /**
     * The eom1 value.
     */
    private static final int EOM1 = 0x1c; // End of Message Byte 1
    /**
     * The eom2 value.
     */
    private static final int EOM2 = 0x0d; // End of Message Byte 2
    /**
     * The read buffer value.
     */
    private final ByteArrayOutputStream readBuffer = new ByteArrayOutputStream();

    /**
     * The eom value.
     */
    private boolean eom = true;

    /**
     * Creates a new instance.
     *
     * @param in the in.
     */
    public MLLPInputStream(InputStream in) {
        super(in);
    }

    /**
     * Creates a new instance.
     *
     * @param in   the in.
     * @param size the size.
     */
    public MLLPInputStream(InputStream in, int size) {
        super(in, size);
    }

    /**
     * Determines whether more input.
     *
     * @return true if the condition is met; otherwise false.
     * @throws IOException if the operation cannot be completed.
     */
    public synchronized boolean hasMoreInput() throws IOException {
        if (!eom)
            throw new IllegalStateException();

        int b = super.read();
        if (b == -1)
            return false;

        if (b != SOM)
            throw new IOException("Missing Start Block character");

        eom = false;
        return true;
    }

    /**
     * Executes the read operation.
     *
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public synchronized int read() throws IOException {
        if (eom)
            return -1;

        int b = super.read();
        if (b == -1)
            throw new EOFException();

        if (b != EOM1)
            return b;

        eom();
        return -1;
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
    public synchronized int read(byte[] b, int off, int len) throws IOException {
        if (b == null)
            throw new NullPointerException();

        if (off < 0 || len < 0 || len > b.length - off)
            throw new IndexOutOfBoundsException();

        if (eom)
            return -1;

        if (len == 0)
            return 0;

        if (read() == -1)
            return -1;

        int rlen = Math.min(count - pos, len - 1);
        int remaining = remaining(pos + rlen);
        if (remaining == -1) {
            System.arraycopy(buf, pos - 1, b, off, rlen + 1);
            pos += rlen;
            return rlen + 1;
        }

        System.arraycopy(buf, pos - 1, b, off, remaining + 1);
        pos += remaining + 1;
        eom();
        return remaining + 1;
    }

    /**
     * Copies the to.
     *
     * @param out the out.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    public synchronized int copyTo(OutputStream out) throws IOException {
        if (eom)
            throw new IllegalStateException();

        int totlen = 0;
        int remaining;
        int leftover = 0;
        while ((remaining = remaining(count)) == -1) {
            int avail = count - pos;
            out.write(buf, pos - leftover, avail + leftover);
            totlen += avail + leftover;
            pos = count;
            if (read() == -1)
                return totlen;
            leftover = 1;
        }
        out.write(buf, pos - leftover, remaining + leftover);
        totlen += remaining + leftover;
        pos += remaining + 1;
        eom();
        return totlen;
    }

    /**
     * Reads the message.
     *
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    public synchronized byte[] readMessage() throws IOException {
        if (!hasMoreInput())
            return null;

        readBuffer.reset();
        copyTo(readBuffer);
        return readBuffer.toByteArray();
    }

    /**
     * Executes the eom operation.
     *
     * @throws IOException if the operation cannot be completed.
     */
    private void eom() throws IOException {
        int b = super.read();
        if (b != EOM2)
            throw new IOException("1CH followed by " + Integer.toHexString(b & 0xff) + "H instead by 0DH");
        eom = true;
    }

    /**
     * Executes the remaining operation.
     *
     * @param count the count.
     * @return the operation result.
     */
    private int remaining(int count) {
        for (int i = pos; i < count; i++)
            if (buf[i] == EOM1)
                return i - pos;

        return -1;
    }

}
