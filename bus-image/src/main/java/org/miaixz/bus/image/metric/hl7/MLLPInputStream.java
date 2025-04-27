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
package org.miaixz.bus.image.metric.hl7;

import java.io.*;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class MLLPInputStream extends BufferedInputStream {

    private static final int SOM = 0x0b; // 消息开始
    private static final int EOM1 = 0x1c; // 消息字节1的结尾
    private static final int EOM2 = 0x0d; // 消息字节2的结尾
    private final ByteArrayOutputStream readBuffer = new ByteArrayOutputStream();
    private boolean eom = true;

    public MLLPInputStream(InputStream in) {
        super(in);
    }

    public MLLPInputStream(InputStream in, int size) {
        super(in, size);
    }

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

    public synchronized byte[] readMessage() throws IOException {
        if (!hasMoreInput())
            return null;

        readBuffer.reset();
        copyTo(readBuffer);
        return readBuffer.toByteArray();
    }

    private void eom() throws IOException {
        int b = super.read();
        if (b != EOM2)
            throw new IOException("1CH followed by " + Integer.toHexString(b & 0xff) + "H instead by 0DH");
        eom = true;
    }

    private int remaining(int count) {
        for (int i = pos; i < count; i++)
            if (buf[i] == EOM1)
                return i - pos;

        return -1;
    }

}
