/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.io.sink;

import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.Deflater;

import org.miaixz.bus.core.io.SectionBuffer;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.timout.Timeout;
import org.miaixz.bus.core.xyz.IoKit;

/**
 * A {@code GzipSink} compresses data into GZIP format using a {@link Deflater}. It optimizes performance by calling
 * {@link #flush()} only when necessary.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class GzipSink implements Sink {

    /**
     * The buffered sink that receives the GZIP formatted data.
     */
    private final BufferSink sink;

    /**
     * The deflater used for compressing data.
     */
    private final Deflater deflater;

    /**
     * The deflater sink responsible for moving data between the uncompressed source and the compressed sink.
     */
    private final DeflaterSink deflaterSink;

    /**
     * The CRC32 checksum calculator for the uncompressed data.
     */
    private final CRC32 crc = new CRC32();

    /**
     * A flag indicating whether this sink has been closed.
     */
    private boolean closed;

    /**
     * Constructs a {@code GzipSink} with the specified underlying sink.
     *
     * @param sink The underlying sink to which the GZIP data will be written.
     * @throws IllegalArgumentException If {@code sink} is {@code null}.
     */
    public GzipSink(Sink sink) {
        if (null == sink) {
            throw new IllegalArgumentException("sink == null");
        }
        this.deflater = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
        this.sink = IoKit.buffer(sink);
        this.deflaterSink = new DeflaterSink(this.sink, deflater);
        writeHeader();
    }

    /**
     * Writes {@code byteCount} bytes from {@code source} to this sink, compressing them into GZIP format. The CRC32
     * checksum is updated for the uncompressed data.
     *
     * @param source    The buffer containing the data to write.
     * @param byteCount The number of bytes to read from {@code source} and write.
     * @throws IOException              If an I/O error occurs during the write or compression operation.
     * @throws IllegalArgumentException If {@code byteCount} is less than 0.
     */
    @Override
    public void write(Buffer source, long byteCount) throws IOException {
        if (byteCount < 0)
            throw new IllegalArgumentException("byteCount < 0: " + byteCount);
        if (byteCount == 0)
            return;
        updateCrc(source, byteCount);
        deflaterSink.write(source, byteCount);
    }

    /**
     * Flushes any buffered compressed data to the underlying sink.
     *
     * @throws IOException If an I/O error occurs during the flush operation.
     */
    @Override
    public void flush() throws IOException {
        deflaterSink.flush();
    }

    /**
     * Returns the timeout for the underlying sink.
     *
     * @return The timeout object associated with the underlying sink.
     */
    @Override
    public Timeout timeout() {
        return sink.timeout();
    }

    /**
     * Closes this sink, finishes the GZIP compression, writes the GZIP footer, and releases any system resources
     * associated with the deflater and the underlying sink.
     *
     * @throws IOException If an I/O error occurs during the close operation.
     */
    @Override
    public void close() throws IOException {
        if (closed)
            return;
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
            if (thrown == null)
                thrown = e;
        }
        try {
            sink.close();
        } catch (Throwable e) {
            if (thrown == null)
                thrown = e;
        }
        closed = true;
        if (thrown != null) {
            IoKit.sneakyRethrow(thrown);
        }
    }

    /**
     * Returns the {@link Deflater} instance used by this sink. This allows access to deflater statistics, dictionary,
     * compression level, etc.
     *
     * @return The {@link Deflater} object.
     */
    public final Deflater deflater() {
        return deflater;
    }

    /**
     * Writes the GZIP file header to the underlying sink. The header includes the GZIP ID, compression method, flags,
     * modification time, extra flags, and OS.
     */
    private void writeHeader() {
        Buffer buffer = this.sink.buffer();
        buffer.writeShort(0x1f8b); // Two-byte Gzip ID.
        buffer.writeByte(0x08); // 8 == Deflate compression method.
        buffer.writeByte(0x00); // No flags.
        buffer.writeInt(0x00); // No modification time.
        buffer.writeByte(0x00); // No extra flags.
        buffer.writeByte(0x00); // No OS.
    }

    /**
     * Writes the GZIP file footer to the underlying sink. The footer includes the CRC32 checksum of the uncompressed
     * data and the total length of the uncompressed data.
     *
     * @throws IOException If an I/O error occurs during the write operation.
     */
    private void writeFooter() throws IOException {
        // CRC32 of original data
        sink.writeIntLe((int) crc.getValue());
        // Original data length
        sink.writeIntLe((int) deflater.getBytesRead());
    }

    /**
     * Updates the CRC32 checksum with the data from the provided buffer.
     *
     * @param buffer    The buffer containing the data to update the checksum with.
     * @param byteCount The number of bytes from the buffer to use for the checksum update.
     */
    private void updateCrc(Buffer buffer, long byteCount) {
        for (SectionBuffer head = buffer.head; byteCount > 0; head = head.next) {
            int segmentLength = (int) Math.min(byteCount, head.limit - head.pos);
            crc.update(head.data, head.pos, segmentLength);
            byteCount -= segmentLength;
        }
    }

}
