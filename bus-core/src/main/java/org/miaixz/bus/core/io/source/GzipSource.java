/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
     * Our source should yield a GZIP header (which we consume directly), followed
     * by deflated bytes (which we consume via an InflaterSource), followed by a
     * GZIP trailer (which we also consume directly).
     */
    private final BufferSource source;
    /**
     * The inflater used to decompress the deflated body.
     */
    private final Inflater inflater;
    /**
     * The inflater source takes care of moving data between compressed source and
     * decompressed sink buffers.
     */
    private final InflaterSource inflaterSource;
    /**
     * Checksum used to check both the GZIP header and decompressed body.
     */
    private final CRC32 crc = new CRC32();
    /**
     * The current section. Always progresses forward.
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

        // If we haven't consumed the header, we must consume it before anything else.
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

        // The body is exhausted; time to read the trailer. We always consume the
        // trailer before returning a -1 exhausted result; that way if you read to
        // the end of a GzipSource you guarantee that the CRC has been checked.
        if (section == SECTION_TRAILER) {
            consumeTrailer();
            section = SECTION_DONE;

            // Gzip streams self-terminate: they return -1 before their underlying
            // source returns -1. Here we attempt to force the underlying stream to
            // return -1 which may trigger it to release its resources. If it doesn't
            // return -1, then our Gzip data finished prematurely!
            if (!source.exhausted()) {
                throw new IOException("gzip finished without exhausting source");
            }
        }

        return -1;
    }

    private void consumeHeader() throws IOException {
        // Read the 10-byte header. We peek at the flags byte first so we know if we
        // need to CRC the entire header. Then we read the magic ID1ID2 sequence.
        // We can skip everything else in the first 10 bytes.
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

        // Skip optional extra fields.
        // +---+---+=================================+
        // | XLEN  |...XLEN bytes of "extra field"...| (more-->)
        // +---+---+=================================+
        if (((flags >> FEXTRA) & 1) == 1) {
            source.require(2);
            if (fhcrc) updateCrc(source.getBuffer(), 0, 2);
            int xlen = source.getBuffer().readShortLe();
            source.require(xlen);
            if (fhcrc) updateCrc(source.getBuffer(), 0, xlen);
            source.skip(xlen);
        }

        // Skip an optional 0-terminated name.
        // +=========================================+
        // |...original file name, zero-terminated...| (more-->)
        // +=========================================+
        if (((flags >> FNAME) & 1) == 1) {
            long index = source.indexOf((byte) 0);
            if (index == -1) throw new EOFException();
            if (fhcrc) updateCrc(source.getBuffer(), 0, index + 1);
            source.skip(index + 1);
        }

        // Skip an optional 0-terminated comment.
        // +===================================+
        // |...file comment, zero-terminated...| (more-->)
        // +===================================+
        if (((flags >> FCOMMENT) & 1) == 1) {
            long index = source.indexOf((byte) 0);
            if (index == -1) throw new EOFException();
            if (fhcrc) updateCrc(source.getBuffer(), 0, index + 1);
            source.skip(index + 1);
        }

        // Confirm the optional header CRC.
        // +---+---+
        // | CRC16 |
        // +---+---+
        if (fhcrc) {
            checkEqual("FHCRC", source.readShortLe(), (short) crc.getValue());
            crc.reset();
        }
    }

    private void consumeTrailer() throws IOException {
        // Read the eight-byte trailer. Confirm the body's CRC and size.
        // +---+---+---+---+---+---+---+---+
        // |     CRC32     |     ISIZE     |
        // +---+---+---+---+---+---+---+---+
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
     * Updates the CRC with the given bytes.
     */
    private void updateCrc(Buffer buffer, long offset, long byteCount) {
        // Skip segments that we aren't checksumming.
        SectionBuffer s = buffer.head;
        for (; offset >= (s.limit - s.pos); s = s.next) {
            offset -= (s.limit - s.pos);
        }

        // Checksum one segment at a time.
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
            throw new IOException(String.format(
                    "%s: actual 0x%08x != expected 0x%08x", name, actual, expected));
        }
    }

}
