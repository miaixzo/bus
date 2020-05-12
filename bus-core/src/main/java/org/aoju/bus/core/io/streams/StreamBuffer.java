/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.core.io.streams;

import org.aoju.bus.core.lang.Charset;
import org.aoju.bus.core.lang.Lang;
import org.aoju.bus.core.utils.IoUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kimi Liu
 * @version 5.9.0
 * @since JDK 1.8+
 */
public class StreamBuffer extends InputStream {

    private OutputStreamBuffer buffer = new OutputStreamBuffer();
    private int index = 0;
    private int cursor = 0;

    public OutputStream getBuffer() {
        return buffer;
    }

    public void write(int b) throws IOException {
        buffer.write(b);
    }

    @Override
    public int read() throws IOException {
        if (cursor > buffer.width) {
            index++;
            cursor = 0;
        }
        if (index > buffer.index)
            return -1;
        if (index < buffer.bytes.size()) {
            byte[] cs = buffer.bytes.get(index);
            if (cursor < buffer.cursor)
                return cs[cursor++];
        }
        return -1;
    }

    @Override
    public int available() {
        return buffer.size();
    }

    @Override
    public synchronized void reset() {
        index = 0;
        cursor = 0;
    }

    @Override
    public String toString() {
        try {
            return toString(Charset.DEFAULT_CHARSET);
        } catch (IOException e) {
            throw Lang.wrapThrow(e);
        }
    }

    public String toString(String charset) throws IOException {
        index = 0;
        cursor = 0;
        StringBuilder sb = new StringBuilder();
        StringOutputStream sos = new StringOutputStream(sb, charset);
        byte c;
        while ((c = (byte) this.read()) != -1)
            sos.write(c);
        sos.flush();
        IoUtils.close(sos);
        return sb.toString();
    }

    private static class OutputStreamBuffer extends OutputStream {

        private List<byte[]> bytes = new ArrayList<>();
        private int width = 1024;
        private int index = 0;
        private int cursor = 0;

        @Override
        public void write(int b) throws IOException {
            if (cursor >= width)
                index++;
            byte[] row = bytes.size() > index ? bytes.get(index) : null;
            if (null == row) {
                row = new byte[width];
                bytes.add(row);
                cursor = 0;
            }
            row[cursor++] = (byte) b;
        }

        private int size() {
            return index > 0 ? width * (index - 1) + cursor : cursor;
        }

    }

}