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
package org.miaixz.bus.core.io.compress;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Excel compatible ZIP64 OutputStream implementation. Based on: https://github.com/rzymek/opczip
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class OpcZipOutputStream extends ZipOutputStream {

    /**
     * The Zip64 specification handler.
     */
    private final Zip64 spec;
    /**
     * List of Zip64 entries.
     */
    private final List<Zip64.Entry> entries = new ArrayList<>();
    /**
     * CRC32 checksum calculator.
     */
    private final CRC32 crc = new CRC32();
    /**
     * The current Zip64 entry being processed.
     */
    private Zip64.Entry current;
    /**
     * The total number of bytes written to the output stream.
     */
    private int written = 0;
    /**
     * Flag indicating whether the output stream has been finished.
     */
    private boolean finished = false;

    /**
     * Constructs a new OpcZipOutputStream.
     *
     * @param out The output stream to which compressed data will be written.
     */
    public OpcZipOutputStream(final OutputStream out) {
        super(out);
        this.spec = new Zip64(out);
    }

    /**
     * Putnextentry method.
     */
    @Override
    public void putNextEntry(final ZipEntry e) throws IOException {
        if (current != null) {
            closeEntry();
        }
        current = new Zip64.Entry(e.getName());
        current.offset = written;
        written += spec.writeLFH(current);
        entries.add(current);
    }

    /**
     * Closeentry method.
     */
    @Override
    public void closeEntry() throws IOException {
        if (current == null) {
            throw new IllegalStateException("not current zip current");
        }
        def.finish();
        while (!def.finished()) {
            deflate();
        }

        current.size = def.getBytesRead();
        current.compressedSize = (int) def.getBytesWritten();
        current.crc = crc.getValue();

        written += current.compressedSize;
        written += spec.writeDAT(current);
        current = null;
        def.reset();
        crc.reset();
    }

    /**
     * Finish method.
     */
    @Override
    public void finish() throws IOException {
        if (finished) {
            return;
        }
        if (current != null) {
            closeEntry();
        }
        final int offset = written;
        for (final Zip64.Entry entry : entries) {
            written += spec.writeCEN(entry);
        }
        written += spec.writeEND(entries.size(), offset, written - offset);
        finished = true;
    }

    /**
     * Writes an array of bytes to the current ZIP entry.
     *
     * @param b   The data to be written.
     * @param off The start offset in the data.
     * @param len The number of bytes to write.
     * @throws IOException               If an I/O error occurs.
     * @throws IndexOutOfBoundsException If {@code off} is negative, {@code len} is negative, or {@code off + len} is
     *                                   greater than the length of the array {@code b}.
     */
    @Override
    public synchronized void write(final byte[] b, final int off, final int len) throws IOException {
        if (off < 0 || len < 0 || off > b.length - len) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        super.write(b, off, len);
        crc.update(b, off, len);
    }

    /**
     * Close method.
     */
    @Override
    public void close() throws IOException {
        finish();
        out.close();
    }

}
