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
package org.miaixz.bus.core.io.compress;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;

import org.miaixz.bus.core.lang.Charset;

/**
 * Excel compatible Zip64 implementation. Based on: https://github.com/rzymek/opczip
 *
 * @author Kimi Liu
 * @since Java 17+
 */
class Zip64 {

    /**
     * Signature for Central File Header.
     */
    private static final long PK0102 = 0x02014b50L;
    /**
     * Signature for Local File Header.
     */
    private static final long PK0304 = 0x04034b50L;
    /**
     * Signature for End of Central Directory Record.
     */
    private static final long PK0506 = 0x06054b50L;
    /**
     * Signature for Data Descriptor.
     */
    private static final long PK0708 = 0x08074b50L;

    /**
     * Version 2.0 of the ZIP format specification.
     */
    private static final int VERSION_20 = 20;
    /**
     * Version 4.5 of the ZIP format specification, required for Zip64.
     */
    private static final int VERSION_45 = 45;
    /**
     * Flag indicating that a data descriptor is used.
     */
    private static final int DATA_DESCRIPTOR_USED = 0x08;
    /**
     * Extra field ID for Zip64 extended information.
     */
    private static final int ZIP64_FIELD = 0x0001;
    /**
     * Maximum 32-bit unsigned integer value.
     */
    private static final long MAX32 = 0xffffffffL;

    /**
     * The output stream to which ZIP data is written.
     */
    private final OutputStream out;
    /**
     * The number of bytes written in the current operation.
     */
    private int written = 0;

    /**
     * Constructs a new Zip64 instance.
     *
     * @param out The output stream to write ZIP data to.
     */
    Zip64(final OutputStream out) {
        this.out = out;
    }

    /**
     * Writes the Local File Header (LFH) for a given entry.
     *
     * @param entry The entry for which to write the LFH.
     * @return The total number of bytes written for the LFH.
     * @throws IOException If an I/O error occurs.
     */
    int writeLFH(final Entry entry) throws IOException {
        written = 0;
        writeInt(PK0304); // "PK\003\004"
        writeShort(VERSION_45); // version required: 4.5
        writeShort(DATA_DESCRIPTOR_USED); // flags: 8 = data descriptor used
        writeShort(ZipEntry.DEFLATED); // compression method: 8 = deflate
        writeInt(0); // file modification time & date
        writeInt(entry.crc); // CRC-32
        writeInt(0); // compressed file size
        writeInt(0); // uncompressed file size
        writeShort(entry.filename.length()); // filename length
        writeShort(0); // extra flags size
        final byte[] filenameBytes = entry.filename.getBytes(Charset.US_ASCII);
        out.write(filenameBytes); // filename characters
        return written + filenameBytes.length;
    }

    /**
     * Writes the Data Descriptor for a given entry.
     *
     * @param entry The entry for which to write the Data Descriptor.
     * @return The total number of bytes written for the Data Descriptor.
     * @throws IOException If an I/O error occurs.
     */
    int writeDAT(final Entry entry) throws IOException {
        written = 0;
        writeInt(PK0708); // data descriptor signature "PK\007\008"
        writeInt(entry.crc); // crc-32
        writeLong(entry.compressedSize); // compressed size (zip64)
        writeLong(entry.size); // uncompressed size (zip64)
        return written;
    }

    /**
     * Writes the Central Directory File Header (CEN) for a given entry.
     *
     * @param entry The entry for which to write the CEN.
     * @return The total number of bytes written for the CEN.
     * @throws IOException If an I/O error occurs.
     */
    int writeCEN(final Entry entry) throws IOException {
        written = 0;
        final boolean useZip64 = entry.size > MAX32;
        writeInt(PK0102); // "PK\001\002"
        writeShort(VERSION_45); // version made by: 4.5
        writeShort(useZip64 ? VERSION_45 : VERSION_20);// version required: 4.5
        writeShort(DATA_DESCRIPTOR_USED); // flags: 8 = data descriptor used
        writeShort(ZipEntry.DEFLATED); // compression method: 8 = deflate
        writeInt(0); // file modification time & date
        writeInt(entry.crc); // CRC-32
        writeInt(entry.compressedSize); // compressed size
        writeInt(useZip64 ? MAX32 : entry.size); // uncompressed size
        writeShort(entry.filename.length()); // filename length
        writeShort(
                useZip64 ? (2 + 2 + 8) /* short + short + long */
                        : 0); // extra field len
        writeShort(0); // comment length
        writeShort(0); // disk number where file starts
        writeShort(0); // internal file attributes (unused)
        writeInt(0); // external file attributes (unused)
        writeInt(entry.offset); // LFH offset
        final byte[] filenameBytes = entry.filename.getBytes(Charset.US_ASCII);
        out.write(filenameBytes); // filename characters
        if (useZip64) {
            // Extra field:
            writeShort(ZIP64_FIELD); // ZIP64 field signature
            writeShort(8); // size of extra field (below)
            writeLong(entry.size); // uncompressed size
        }
        return written + filenameBytes.length;
    }

    /**
     * Writes the End of Central Directory Record (EOCD).
     *
     * @param entriesCount The total number of entries in the central directory.
     * @param offset       The offset of the central directory from the start of the archive.
     * @param length       The size of the central directory.
     * @return The total number of bytes written for the EOCD.
     * @throws IOException If an I/O error occurs.
     */
    int writeEND(final int entriesCount, final int offset, final int length) throws IOException {
        written = 0;
        writeInt(PK0506); // "PK\005\006"
        writeShort(0); // number of this disk
        writeShort(0); // central directory start disk
        writeShort(entriesCount); // number of directory entries on disk
        writeShort(entriesCount); // total number of directory entries
        writeInt(length); // length of central directory
        writeInt(offset); // offset of central directory
        writeShort(0); // comment length
        return written;
    }

    /**
     * Writes a 16-bit short to the output stream in little-endian byte order.
     *
     * @param v The short value to write.
     * @throws IOException If an I/O error occurs.
     */
    private void writeShort(final int v) throws IOException {
        final OutputStream out = this.out;
        out.write((v) & 0xff);
        out.write((v >>> 8) & 0xff);
        written += 2;
    }

    /**
     * Writes a 32-bit int to the output stream in little-endian byte order.
     *
     * @param v The int value to write.
     * @throws IOException If an I/O error occurs.
     */
    private void writeInt(final long v) throws IOException {
        final OutputStream out = this.out;
        out.write((int) ((v) & 0xff));
        out.write((int) ((v >>> 8) & 0xff));
        out.write((int) ((v >>> 16) & 0xff));
        out.write((int) ((v >>> 24) & 0xff));
        written += 4;
    }

    /**
     * Writes a 64-bit long to the output stream in little-endian byte order.
     *
     * @param v The long value to write.
     * @throws IOException If an I/O error occurs.
     */
    private void writeLong(final long v) throws IOException {
        final OutputStream out = this.out;
        out.write((int) ((v) & 0xff));
        out.write((int) ((v >>> 8) & 0xff));
        out.write((int) ((v >>> 16) & 0xff));
        out.write((int) ((v >>> 24) & 0xff));
        out.write((int) ((v >>> 32) & 0xff));
        out.write((int) ((v >>> 40) & 0xff));
        out.write((int) ((v >>> 48) & 0xff));
        out.write((int) ((v >>> 56) & 0xff));
        written += 8;
    }

    /**
     * Represents an entry within the ZIP archive.
     */
    static class Entry {

        /**
         * The filename of the entry.
         */
        final String filename;
        /**
         * The CRC-32 checksum of the uncompressed data.
         */
        long crc;
        /**
         * The uncompressed size of the entry data.
         */
        long size;
        /**
         * The compressed size of the entry data.
         */
        int compressedSize;
        /**
         * The offset of the local file header from the start of the archive.
         */
        int offset;

        /**
         * Constructs a new Entry with the specified filename.
         *
         * @param filename The filename of the entry.
         */
        Entry(final String filename) {
            this.filename = filename;
        }
    }

}
