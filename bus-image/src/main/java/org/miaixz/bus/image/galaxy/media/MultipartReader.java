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
package org.miaixz.bus.image.galaxy.media;

import org.miaixz.bus.core.exception.InternalException;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.logger.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class MultipartReader {

    public static final int HEADER_PART_MAX_SIZE = 16384;

    private final InputStream inputStream;
    private final byte[] boundary;
    private final byte[] buffer;
    private final int bufferSize;
    private String headerEncoding;
    private int currentBoundaryLength;
    private int headBuffer = 0;
    private int tailBuffer = 0;

    /**
     * @param input    多部分交换的<code>nputStream</code>
     * @param boundary 用于分隔多部分流的各个部分的标记
     */
    public MultipartReader(InputStream input, byte[] boundary) {
        this(input, boundary, 4096);
    }

    /**
     * @param input    多部分交换的<code>InputStream</code>
     * @param boundary 用于分隔多部分流的各个部分的标记
     * @param bufSize  缓冲区的大小(以字节为单位)默认值为4096
     */
    public MultipartReader(InputStream input, byte[] boundary, int bufSize) {
        this.inputStream = input;
        this.bufferSize = bufSize;
        this.buffer = new byte[bufSize];
        int blength = MultipartParser.Separator.BOUNDARY.getType().length;
        this.boundary = new byte[boundary.length + blength];
        this.currentBoundaryLength = boundary.length + blength;
        System.arraycopy(MultipartParser.Separator.BOUNDARY.getType(), 0, this.boundary, 0, blength);
        System.arraycopy(boundary, 0, this.boundary, blength, boundary.length);
    }

    protected static boolean compareArrays(byte[] a, byte[] b, int count) {
        for (int i = 0; i < count; i++) {
            if (a[i] != b[i]) {
                return false;
            }
        }
        return true;
    }

    public String getHeaderEncoding() {
        return headerEncoding;
    }

    public void setHeaderEncoding(String encoding) {
        headerEncoding = encoding;
    }

    public byte readByte() throws IOException {
        if (headBuffer == tailBuffer) {
            headBuffer = 0;
            tailBuffer = inputStream.read(buffer, headBuffer, bufferSize);
            if (tailBuffer == -1) {
                throw new InternalException("No more data is available");
            }
        }
        return buffer[headBuffer++];
    }

    public boolean readBoundary() throws IOException {
        headBuffer += currentBoundaryLength;

        byte[] marker = {readByte(), readByte()};
        boolean nextPart = false;
        if (compareArrays(marker, MultipartParser.Separator.STREAM.getType(), 2)) {
            nextPart = false;
        } else if (compareArrays(marker, MultipartParser.Separator.FIELD.getType(), 2)) {
            nextPart = true;
        } else {
            throw new InternalException("Unexpected bytes after the boundary separator");
        }
        return nextPart;
    }

    public String readHeaders() throws IOException {
        int k = 0;
        byte b;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int headerSize = 0;
        byte[] hsep = MultipartParser.Separator.HEADER.getType();
        while (k < hsep.length) {
            b = readByte();
            headerSize++;
            if (headerSize > HEADER_PART_MAX_SIZE) {
                throw new InternalException(
                        "Header content is larger than " + HEADER_PART_MAX_SIZE + " bytes (max size defined in reader)");
            }
            if (b == hsep[k]) {
                k++;
            } else {
                k = 0;
            }
            baos.write(b);
        }

        String headers = null;
        if (null != headerEncoding) {
            try {
                headers = baos.toString(headerEncoding);
            } catch (UnsupportedEncodingException e) {
                Logger.error("Decoding header", e);
            }
        }

        if (null == headers) {
            headers = baos.toString();
        }
        return headers;
    }

    public boolean skipFirstBoundary() throws IOException {
        // 第一个边界定界符的特殊情况=>删除CRLF
        System.arraycopy(boundary, 2, boundary, 0, boundary.length - 2);
        currentBoundaryLength = boundary.length - 2;
        try {
            discardDataBeforeDelimiter();
            return readBoundary();
        } finally {
            // 恢复原始边界
            System.arraycopy(boundary, 0, boundary, 2, boundary.length - 2);
            currentBoundaryLength = boundary.length;
            boundary[0] = MultipartParser.CR;
            boundary[1] = MultipartParser.LF;
        }
    }

    public PartInputStream newPartInputStream() {
        return new PartInputStream();
    }

    protected void discardDataBeforeDelimiter() throws IOException {
        try (InputStream in = newPartInputStream()) {
            byte[] pBuffer = new byte[Normal._1024];
            while (true) {
                if (in.read(pBuffer) == -1) {
                    break;
                }
            }
        }
    }

    protected int findFirstBoundaryCharacter(int start) {
        for (int i = start; i < tailBuffer; i++) {
            if (buffer[i] == boundary[0]) {
                return i;
            }
        }
        return -1;
    }

    protected int findStartingBoundaryPosition() {
        int start;
        int b = 0;
        int end = tailBuffer - currentBoundaryLength;
        for (start = headBuffer; start <= end && b != currentBoundaryLength; start++) {
            start = findFirstBoundaryCharacter(start);
            if (start == -1 || start > end) {
                return -1;
            }
            for (b = 1; b < currentBoundaryLength; b++) {
                if (buffer[start + b] != boundary[b]) {
                    break;
                }
            }
        }
        if (b == currentBoundaryLength) {
            return start - 1;
        }
        return -1;
    }

    public class PartInputStream extends InputStream implements AutoCloseable {
        private static final String STREAM_CLOSED_EX = "PartInputStream has been closed";

        private int position;
        private long total;
        private int offset;
        private boolean closed;

        PartInputStream() {
            moveToBoundary();
        }

        private void moveToBoundary() {
            position = findStartingBoundaryPosition();
            if (position == -1) {
                if (tailBuffer - headBuffer > boundary.length) {
                    offset = boundary.length;
                } else {
                    offset = tailBuffer - headBuffer;
                }
            }
        }

        private int readInputStream() throws IOException {
            if (position != -1) {
                return 0;
            }

            total += tailBuffer - headBuffer - offset;
            System.arraycopy(buffer, tailBuffer - offset, buffer, 0, offset);

            headBuffer = 0;
            tailBuffer = offset;

            while (true) {
                int readBytes = inputStream.read(buffer, tailBuffer, bufferSize - tailBuffer);
                if (readBytes == -1) {
                    throw new InternalException("Unexpect end of stream");
                }

                tailBuffer += readBytes;
                moveToBoundary();
                int k = available();
                if (k > 0 || position != -1) {
                    return k;
                }
            }
        }

        public long getTotal() {
            return total;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (closed) {
                throw new InternalException(STREAM_CLOSED_EX);
            }
            if (len == 0) {
                return 0;
            }
            int k = available();
            if (k == 0) {
                k = readInputStream();
                if (k == 0) {
                    return -1;
                }
            }
            k = Math.min(k, len);
            System.arraycopy(buffer, headBuffer, b, off, k);
            headBuffer += k;
            total += k;
            return k;
        }

        @Override
        public int read() throws IOException {
            if (closed) {
                throw new InternalException(STREAM_CLOSED_EX);
            }
            if (available() == 0 && readInputStream() == 0) {
                return -1;
            }
            total++;
            return buffer[headBuffer++] & 0xFF;
        }

        @Override
        public int available() {
            if (position == -1) {
                return tailBuffer - headBuffer - offset;
            }
            return position - headBuffer;
        }

        @Override
        public long skip(long bytes) throws IOException {
            if (closed) {
                throw new InternalException(STREAM_CLOSED_EX);
            }
            int k = available();
            if (k == 0) {
                k = readInputStream();
                if (k == 0) {
                    return 0;
                }
            }
            long skipBytes = Math.min(k, bytes);
            headBuffer += skipBytes;
            return skipBytes;
        }

        @Override
        public void close() throws IOException {
            if (closed) {
                return;
            }

            while (true) {
                int k = available();
                if (k == 0) {
                    k = readInputStream();
                    if (k == 0) {
                        break;
                    }
                }
                skip(k);
            }
            closed = true;
        }

        public boolean isClosed() {
            return closed;
        }
    }

}
