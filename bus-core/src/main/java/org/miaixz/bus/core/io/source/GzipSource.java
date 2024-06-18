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
package org.miaixz.bus.core.io.source;

import org.miaixz.bus.core.io.SectionBuffer;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.timout.Timeout;
import org.miaixz.bus.core.xyz.IoKit;

import java.io.EOFException;
import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.Inflater;

/**
 * 解压读取数据
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class GzipSource implements Source {

    private static final byte FHCRC = 1;
    private static final byte FEXTRA = 2;
    private static final byte FNAME = 3;
    private static final byte FCOMMENT = 4;

    private static final byte SECTION_HEADER = 0;
    private static final byte SECTION_BODY = 1;
    private static final byte SECTION_TRAILER = 2;
    private static final byte SECTION_DONE = 3;

    /**
     *
     * 源压缩字节
     */
    private final BufferSource source;
    /**
     * 用于进行解压的缓冲区
     */
    private final Inflater inflater;
    /**
     * 负责在压缩源缓冲区和解压缩接收缓冲区之间交换数据
     */
    private final InflaterSource inflaterSource;
    /**
     * 用于检查 GZIP 标头和解压缩主体的校验
     */
    private final CRC32 crc = new CRC32();
    /**
     * 当前部分
     */
    private int section = SECTION_HEADER;

    public GzipSource(Source source) {
        if (source == null) throw new IllegalArgumentException("source == null");
        this.inflater = new Inflater(true);
        this.source = IoKit.buffer(source);
        this.inflaterSource = new InflaterSource(this.source, inflater);
    }

    @Override
    public long read(Buffer sink, long byteCount) throws IOException {
        if (byteCount < 0) throw new IllegalArgumentException("byteCount < 0: " + byteCount);
        if (byteCount == 0) return 0;

        // 如果还没有使用标题，必须在做其他事情之前使用它。
        if (section == SECTION_HEADER) {
            consumeHeader();
            section = SECTION_BODY;
        }

        if (section == SECTION_BODY) {
            long offset = sink.size;
            long result = inflaterSource.read(sink, byteCount);
            if (result != -1) {
                updateCrc(sink, offset, result);
                return result;
            }
            section = SECTION_TRAILER;
        }

        // 主体已耗尽；读取尾部了。总是在返回 -1 耗尽结果之前使用
        // 尾部；这样，如果您读到 GzipSource 的末尾，就可以保证 CRC 已经过检查。
        if (section == SECTION_TRAILER) {
            consumeTrailer();
            section = SECTION_DONE;

            // Gzip 流会自行终止：在底层源返回 -1 之前返回 -1。
            // 在这里，尝试强制底层流返回 -1，这可能会触发它释放其资源。
            // 如果它不返回 -1，那么我们的 Gzip 数据就会过早结束！
            if (!source.exhausted()) {
                throw new IOException("gzip finished without exhausting source");
            }
        }

        return -1;
    }

    private void consumeHeader() throws IOException {
        // 读取 10 字节的标头。首先查看标志字节，以便知道是否需要对整个标头进行 CRC 校验。
        // 然后读取神奇的 ID1 ID2 序列。可以跳过前 10 个字节中的所有其他内容。
        // +---+---+---+---+---+---+---+---+---+---+
        // |ID1|ID2|CM |FLG|     MTIME     |XFL|OS | (more-->)
        // +---+---+---+---+---+---+---+---+---+---+
        source.require(10);
        byte flags = source.getBuffer().getByte(3);
        boolean fhcrc = ((flags >> FHCRC) & 1) == 1;
        if (fhcrc) updateCrc(source.getBuffer(), 0, 10);

        short id1id2 = source.readShort();
        checkEqual("ID1ID2", (short) 0x1f8b, id1id2);
        source.skip(8);

        if (((flags >> FEXTRA) & 1) == 1) {
            source.require(2);
            if (fhcrc) updateCrc(source.getBuffer(), 0, 2);
            int xlen = source.getBuffer().readShortLe();
            source.require(xlen);
            if (fhcrc) updateCrc(source.getBuffer(), 0, xlen);
            source.skip(xlen);
        }

        if (((flags >> FNAME) & 1) == 1) {
            long index = source.indexOf((byte) 0);
            if (index == -1) throw new EOFException();
            if (fhcrc) updateCrc(source.getBuffer(), 0, index + 1);
            source.skip(index + 1);
        }

        if (((flags >> FCOMMENT) & 1) == 1) {
            long index = source.indexOf((byte) 0);
            if (index == -1) throw new EOFException();
            if (fhcrc) updateCrc(source.getBuffer(), 0, index + 1);
            source.skip(index + 1);
        }

        if (fhcrc) {
            checkEqual("FHCRC", source.readShortLe(), (short) crc.getValue());
            crc.reset();
        }
    }

    private void consumeTrailer() throws IOException {
        checkEqual("CRC", source.readIntLe(), (int) crc.getValue());
        checkEqual("ISIZE", source.readIntLe(), (int) inflater.getBytesWritten());
    }

    @Override
    public Timeout timeout() {
        return source.timeout();
    }

    @Override
    public void close() throws IOException {
        inflaterSource.close();
    }

    /**
     * 使用给定的字节更新 CRC
     */
    private void updateCrc(Buffer buffer, long offset, long byteCount) {
        // 跳过我们未进行校验的
        SectionBuffer s = buffer.head;
        for (; offset >= (s.limit - s.pos); s = s.next) {
            offset -= (s.limit - s.pos);
        }

        // 每次对一个段进行校验
        for (; byteCount > 0; s = s.next) {
            int pos = (int) (s.pos + offset);
            int toUpdate = (int) Math.min(s.limit - pos, byteCount);
            crc.update(s.data, pos, toUpdate);
            byteCount -= toUpdate;
            offset = 0;
        }
    }

    private void checkEqual(String name, int expected, int actual) throws IOException {
        if (actual != expected) {
            throw new IOException(String.format("%s: actual 0x%08x != expected 0x%08x", name, actual, expected));
        }
    }

}
