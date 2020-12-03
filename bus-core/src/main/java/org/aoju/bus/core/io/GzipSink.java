/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
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
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.core.io;

import org.aoju.bus.core.toolkit.IoKit;

import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.Deflater;

/**
 * 这相当于使用{@link Deflater}同步刷新选项
 * 该类不提供任何部分刷新机制 为获得最佳性能,
 * 只在应用程序行为需要时调用{@link #flush}
 *
 * @author Kimi Liu
 * @version 6.1.3
 * @since JDK 1.8+
 */
public final class GzipSink implements Sink {

    private final BufferSink sink;

    private final Deflater deflater;

    private final DeflaterSink deflaterSink;

    private final CRC32 crc = new CRC32();
    private boolean closed;

    public GzipSink(Sink sink) {
        if (sink == null) throw new IllegalArgumentException("sink == null");
        this.deflater = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
        this.sink = IoKit.buffer(sink);
        this.deflaterSink = new DeflaterSink(this.sink, deflater);

        writeHeader();
    }

    @Override
    public void write(Buffer source, long byteCount) throws IOException {
        if (byteCount < 0) throw new IllegalArgumentException("byteCount < 0: " + byteCount);
        if (byteCount == 0) return;

        updateCrc(source, byteCount);
        deflaterSink.write(source, byteCount);
    }

    @Override
    public void flush() throws IOException {
        deflaterSink.flush();
    }

    @Override
    public Timeout timeout() {
        return sink.timeout();
    }

    @Override
    public void close() {
        if (closed) return;

        Throwable thrown = null;
        try {
            deflaterSink.finishDeflate();
            writeFooter();
        } catch (Throwable e) {
            thrown = e;
        }

        try {
            deflater.end();
        } catch (Throwable e) {
            if (thrown == null) thrown = e;
        }

        try {
            sink.close();
        } catch (Throwable e) {
            if (thrown == null) thrown = e;
        }
        closed = true;

        if (thrown != null) IoKit.sneakyRethrow(thrown);
    }

    public final Deflater deflater() {
        return deflater;
    }

    private void writeHeader() {
        Buffer buffer = this.sink.buffer();
        buffer.writeShort(0x1f8b);
        buffer.writeByte(0x08);
        buffer.writeByte(0x00);
        buffer.writeInt(0x00);
        buffer.writeByte(0x00);
        buffer.writeByte(0x00);
    }

    private void writeFooter() throws IOException {
        sink.writeIntLe((int) crc.getValue());
        sink.writeIntLe((int) deflater.getBytesRead());
    }

    private void updateCrc(Buffer buffer, long byteCount) {
        for (Segment head = buffer.head; byteCount > 0; head = head.next) {
            int segmentLength = (int) Math.min(byteCount, head.limit - head.pos);
            crc.update(head.data, head.pos, segmentLength);
            byteCount -= segmentLength;
        }
    }

}
