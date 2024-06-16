/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.image.metric.internal.hl7;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class MLLPOutputStream extends FilterOutputStream {

    /**
     * 消息开始
     */
    private static final int SOM = 0x0b;
    /**
     * 消息结束
     */
    private static final byte[] EOM = {0x1c, 0x0d};

    private boolean somWritten;

    public MLLPOutputStream(OutputStream out) {
        super(out);
    }

    @Override
    public synchronized void write(int b) throws IOException {
        writeStartBlock();
        out.write(b);
    }

    @Override
    public synchronized void write(byte[] b, int off, int len)
            throws IOException {
        writeStartBlock();
        out.write(b, off, len);
    }

    public void writeMessage(byte[] b) throws IOException {
        writeMessage(b, 0, b.length);
    }

    public synchronized void writeMessage(byte[] b, int off, int len)
            throws IOException {
        if (somWritten)
            throw new IllegalStateException();

        byte[] msg = new byte[len + 3];
        msg[0] = SOM;
        System.arraycopy(b, off, msg, 1, len);
        System.arraycopy(EOM, 0, msg, len + 1, 2);
        out.write(msg);
        out.flush();
    }

    private void writeStartBlock() throws IOException {
        if (!somWritten) {
            out.write(SOM);
            somWritten = true;
        }
    }

    public synchronized void finish() throws IOException {
        if (!somWritten)
            throw new IllegalStateException();
        out.write(EOM);
        out.flush();
        somWritten = false;
    }

}
