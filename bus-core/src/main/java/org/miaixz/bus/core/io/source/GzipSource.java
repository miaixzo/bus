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
package org.miaixz.bus.core.io.source;

import java.io.EOFException;
import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.Inflater;

import org.miaixz.bus.core.io.SectionBuffer;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.timout.Timeout;
import org.miaixz.bus.core.xyz.IoKit;

/**
 * A {@link Source} that decompresses data from a GZIP stream. This class handles the GZIP header, compressed body, and
 * trailer, including CRC checks.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class GzipSource implements Source {

    /**
     * Flag bit for CRC in GZIP header.
     */
    private static final byte FHCRC = 1;
    /**
     * Flag bit for extra field in GZIP header.
     */
    private static final byte FEXTRA = 2;
    /**
     * Flag bit for file name in GZIP header.
     */
    private static final byte FNAME = 3;
    /**
     * Flag bit for file comment in GZIP header.
     */
    private static final byte FCOMMENT = 4;

    /**
     * Represents the header section of the GZIP stream.
     */
    private static final byte SECTION_HEADER = 0;
    /**
     * Represents the body section of the GZIP stream.
     */
    private static final byte SECTION_BODY = 1;
    /**
     * Represents the trailer section of the GZIP stream.
     */
    private static final byte SECTION_TRAILER = 2;
    /**
     * Represents the state where the GZIP stream has been fully processed.
     */
    private static final byte SECTION_DONE = 3;

    /**
     * The underlying source of compressed bytes.
     */
    private final BufferSource source;
    /**
     * The inflater used for decompressing the GZIP data.
     */
    private final Inflater inflater;
    /**
     * An {@link InflaterSource} that mediates data exchange between the compressed source buffer and the decompressed
     * sink buffer.
     */
    private final InflaterSource inflaterSource;
    /**
     * CRC32 checksum calculator for verifying GZIP header and decompressed body.
     */
    private final CRC32 crc = new CRC32();
    /**
     * The current section of the GZIP stream being processed.
     */
    private int section = SECTION_HEADER;

    /**
     * Constructs a {@code GzipSource} that decompresses data from the given {@link Source}.
     *
     * @param source The underlying source of compressed data.
     * @throws IllegalArgumentException If the provided source is null.
     */
    public GzipSource(Source source) {
        if (source == null)
            throw new IllegalArgumentException("source == null");
        this.inflater = new Inflater(true);
        this.source = IoKit.buffer(source);
        this.inflaterSource = new InflaterSource(this.source, inflater);
    }

    /**
     * Reads at least 1 byte and at most {@code byteCount} bytes from this GZIP source and appends them to {@code sink}.
     * Returns the number of bytes read, or -1 if this source has been exhausted.
     *
     * <p>
     * This method handles the GZIP header, body decompression, and trailer verification transparently.
     *
     * @param sink      The buffer to which decompressed bytes will be appended.
     * @param byteCount The maximum number of bytes to read.
     * @return The number of bytes read, or -1 if the source is exhausted.
     * @throws IOException              If an I/O error occurs during decompression or header/trailer processing.
     * @throws IllegalArgumentException If {@code byteCount} is negative.
     */
    @Override
    public long read(Buffer sink, long byteCount) throws IOException {
        if (byteCount < 0)
            throw new IllegalArgumentException("byteCount < 0: " + byteCount);
        if (byteCount == 0)
            return 0;

        // If the header hasn't been consumed yet, consume it before doing anything else.
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

        // The body has been exhausted; read the trailer. Always consume the trailer
        // before exhausting the result; this ensures that if you read to the end of
        // a GzipSource, the CRC has been checked.
        if (section == SECTION_TRAILER) {
            consumeTrailer();
            section = SECTION_DONE;

            // Gzip streams self-terminate: they return -1 before the underlying source
            // returns -1. Here, we attempt to force the underlying stream to return -1
            // which may trigger it to release its resources. If it doesn't return -1,
            // then our Gzip data ended prematurely!
            if (!source.exhausted()) {
                throw new IOException("gzip finished without exhausting source");
            }
        }

        return -1;
    }

    /**
     * Consumes and validates the GZIP header. This method reads the 10-byte header, checks magic numbers, flags, and
     * optional fields, and updates the CRC if the FHCRC flag is set.
     *
     * @throws IOException  If an I/O error occurs or the GZIP header is invalid.
     * @throws EOFException If the source ends prematurely while reading the header.
     */
    private void consumeHeader() throws IOException {
        // Read the 10-byte header. First, look at the flags byte so that we know if
        // we need to compute a CRC on the entire header. Then read the magic ID1 ID2
        // sequence. All other stuff in the first 10 bytes can be skipped.
        // +---+---+---+---+---+---+---+---+---+---+
        // |ID1|ID2|CM |FLG| MTIME |XFL|OS | (more-->)
        // +---+---+---+---+---+---+---+---+---+---+
        source.require(10);
        byte flags = source.getBuffer().getByte(3);
        boolean fhcrc = ((flags >> FHCRC) & 1) == 1;
        if (fhcrc)
            updateCrc(source.getBuffer(), 0, 10);

        short id1id2 = source.readShort();
        checkEqual("ID1ID2", (short) 0x1f8b, id1id2);
        source.skip(8);

        // Skip optional extra field.
        if (((flags >> FEXTRA) & 1) == 1) {
            source.require(2);
            if (fhcrc)
                updateCrc(source.getBuffer(), 0, 2);
            int xlen = source.getBuffer().readShortLe();
            source.require(xlen);
            if (fhcrc)
                updateCrc(source.getBuffer(), 0, xlen);
            source.skip(xlen);
        }

        // Skip optional file name.
        if (((flags >> FNAME) & 1) == 1) {
            long index = source.indexOf((byte) 0);
            if (index == -1)
                throw new EOFException();
            if (fhcrc)
                updateCrc(source.getBuffer(), 0, index + 1);
            source.skip(index + 1);
        }

        // Skip optional file comment.
        if (((flags >> FCOMMENT) & 1) == 1) {
            long index = source.indexOf((byte) 0);
            if (index == -1)
                throw new EOFException();
            if (fhcrc)
                updateCrc(source.getBuffer(), 0, index + 1);
            source.skip(index + 1);
        }

        // Check header CRC if present.
        if (fhcrc) {
            checkEqual("FHCRC", source.readShortLe(), (short) crc.getValue());
            crc.reset();
        }
    }

    /**
     * Consumes and validates the GZIP trailer. This method reads the CRC32 and ISIZE fields from the trailer and
     * compares them with the calculated values.
     *
     * @throws IOException If an I/O error occurs or the GZIP trailer is invalid.
     */
    private void consumeTrailer() throws IOException {
        checkEqual("CRC", source.readIntLe(), (int) crc.getValue());
        checkEqual("ISIZE", source.readIntLe(), (int) inflater.getBytesWritten());
    }

    /**
     * Returns the timeout for this GZIP source.
     *
     * @return The timeout instance associated with the underlying source.
     */
    @Override
    public Timeout timeout() {
        return source.timeout();
    }

    /**
     * Closes this GZIP source and releases any resources held by it. This also closes the underlying
     * {@link InflaterSource}.
     *
     * @throws IOException If an I/O error occurs during closing.
     */
    @Override
    public void close() throws IOException {
        inflaterSource.close();
    }

    /**
     * Updates the CRC32 checksum with the given bytes from the buffer.
     *
     * @param buffer    The buffer containing the data to update the CRC with.
     * @param offset    The starting offset within the buffer from which to read bytes.
     * @param byteCount The number of bytes to read from the buffer.
     */
    private void updateCrc(Buffer buffer, long offset, long byteCount) {
        // Skip segments that we're not checksumming.
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

    /**
     * Checks if the actual value equals the expected value, throwing an {@link IOException} if they are not equal.
     *
     * @param name     The name of the value being checked (for error message).
     * @param expected The expected integer value.
     * @param actual   The actual integer value.
     * @throws IOException If {@code actual} does not equal {@code expected}.
     */
    private void checkEqual(String name, int expected, int actual) throws IOException {
        if (actual != expected) {
            throw new IOException(String.format("%s: actual 0x%08x != expected 0x%08x", name, actual, expected));
        }
    }

}
