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
package org.miaixz.bus.image.nimble.stream;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.ByteKit;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.data.BulkData;
import org.miaixz.bus.image.galaxy.data.Fragments;
import org.miaixz.bus.image.galaxy.data.VR;
import org.miaixz.bus.image.nimble.codec.ImageDescriptor;
import org.miaixz.bus.logger.Logger;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageInputStreamImpl;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Objects;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class SegmentedImageStream extends ImageInputStreamImpl {

    private final ImageInputStream stream;
    private final List<Object> fragments;
    private int curSegment = 0;
    private int firstSegment = 1, lastSegment = Integer.MAX_VALUE;
    // The end of the current segment, in streamPos units, not in underlying stream units
    private long curSegmentEnd = -1;
    private byte[] byteFrag;
    private ImageDescriptor imageDescriptor;

    public SegmentedImageStream(ImageInputStream stream,
                                Fragments pixeldataFragments,
                                int frameIndex) throws IOException {
        if (frameIndex == -1) {
            frameIndex = 0;
        } else {
            firstSegment = frameIndex + 1;
            lastSegment = frameIndex + 2;
        }
        this.fragments = pixeldataFragments;
        this.stream = stream;
        this.curSegment = frameIndex;
        seek(0);
    }

    public SegmentedImageStream(ImageInputStream iis, long streamPosition, int length, boolean singleFrame) throws IOException {
        fragments = new Fragments(VR.OB, false, Normal._16);
        if (!singleFrame) {
            lastSegment = 2;
        }
        fragments.add(new byte[0]);
        fragments.add(new BulkData("pixelData://", streamPosition, length, false));
        stream = iis;
        seek(0);
    }

    public SegmentedImageStream(byte[] data) throws IOException {
        stream = null;
        fragments = new Fragments(VR.OB, false, 2);
        fragments.add(new byte[0]);
        fragments.add(data);
        lastSegment = 2;
        seek(0);
    }

    public ImageDescriptor getImageDescriptor() {
        return imageDescriptor;
    }

    public void setImageDescriptor(ImageDescriptor imageDescriptor) {
        this.imageDescriptor = imageDescriptor;
    }

    @Override
    public void seek(long pos) throws IOException {
        super.seek(pos);
        long beforePos = 0;
        for (int i = firstSegment; i < lastSegment; i++) {
            BulkData bulk = null;
            long bulkOffset = -1;
            int bulkLength = -1;
            synchronized (fragments) {
                if (i < fragments.size()) {
                    Object fragment = fragments.get(i);
                    if (fragment instanceof BulkData) {
                        bulk = (BulkData) fragments.get(i);
                        bulkOffset = bulk.offset();
                        bulkLength = bulk.length();
                    } else {
                        byteFrag = (byte[]) fragment;
                        bulkLength = byteFrag.length;
                        bulkOffset = beforePos;
                    }

                }
            }
            if (bulkOffset == -1 || bulkLength == -1) {
                bulk = updateBulkData(i);
                if (null == bulk) {
                    lastSegment = i;
                    curSegment = -1;
                    super.seek(beforePos);
                    return;
                } else {
                    bulkOffset = bulk.offset();
                    bulkLength = bulk.length();
                }
            }
            // We are past end of input, but we didn't know soon enough
            if (i >= lastSegment) {
                curSegment = -1;
                super.seek(beforePos);
                return;
            }
            long deltaInEnd = pos - beforePos;
            beforePos += bulkLength & 0xFFFFFFFFl;
            if (pos < beforePos) {
                curSegment = i;
                curSegmentEnd = beforePos;
                if (null != bulk) {
                    stream.seek(bulk.offset() + deltaInEnd);
                }
                return;
            }
        }
        curSegment = -1;
    }

    BulkData updateBulkData(int endBulk) throws IOException {
        BulkData last = null;
        for (int i = 1; i <= endBulk; i++) {
            BulkData bulk = null;
            long bulkOffset = -1;
            int bulkLength = -1;
            synchronized (fragments) {
                if (i < fragments.size()) {
                    bulk = (BulkData) fragments.get(i);
                    bulkOffset = bulk.offset();
                    bulkLength = bulk.length();
                }
            }
            if (bulkOffset == -1) {
                long testOffset = last.offset() + (0xFFFFFFFFl & last.length());
                bulk = readBulkAt(testOffset, i);
            } else if (bulkLength == -1) {
                bulk = readBulkAt(bulkOffset - 8, i);
            }
            if (null == bulk) {
                return null;
            }
            last = bulk;
        }
        return last;
    }

    BulkData readBulkAt(long testOffset, int at) throws IOException {
        byte[] data = new byte[8];
        stream.seek(testOffset);
        int size = stream.read(data);
        if (size < 8) return null;
        int tag = ByteKit.bytesToTagLE(data, 0);
        if (tag == Tag.SequenceDelimitationItem) {
            // Safe to read un-protected now as we know there are no more items to update.
            lastSegment = fragments.size();
            return null;
        }
        if (tag != Tag.Item) {
            throw new IOException("At " + testOffset + " isn't an Item(" + Integer.toHexString(Tag.Item) + "), but is " + Integer.toHexString(tag));
        }
        int itemLen = ByteKit.bytesToIntLE(data, 4);
        BulkData bulk;
        synchronized (fragments) {
            if (at < fragments.size()) {
                bulk = (BulkData) fragments.get(at);
                bulk.setOffset(testOffset + 8);
                bulk.setLength(itemLen);
            } else {
                bulk = new BulkData("compressedPixelData://", testOffset + 8, itemLen, false);
                fragments.add(bulk);
            }
        }
        return bulk;
    }

    @Override
    public int read() throws IOException {
        if (!prepareRead())
            return -1;

        bitOffset = 0;
        int val = stream.read();
        if (val != -1) {
            ++streamPos;
        }
        return val;
    }

    private boolean prepareRead() throws IOException {
        if (curSegment < 0)
            return false;

        if (streamPos < curSegmentEnd)
            return true;

        seek(streamPos);

        return curSegment >= 0 && curSegment < lastSegment;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (!prepareRead())
            return -1;

        bitOffset = 0;
        int bytesToRead = Math.min(len, (int) (curSegmentEnd - streamPos));
        int nbytes;
        if (null != byteFrag) {
            System.arraycopy(byteFrag, (int) (streamPos - curSegmentEnd + byteFrag.length), b, off, bytesToRead);
            nbytes = bytesToRead;
        } else {
            nbytes = stream.read(b, off, bytesToRead);
        }
        if (nbytes != -1) {
            streamPos += nbytes;
        }
        return nbytes;
    }

    public long getLastSegmentEnd() {
        synchronized (fragments) {
            BulkData bulk = (BulkData) fragments.get(fragments.size() - 1);
            return bulk.getSegmentEnd();
        }
    }

    public long getOffsetPostPixelData() throws IOException {
        long ret = getLastSegmentEnd();
        if (ret != -1) {
            return ret + 8;
        }
        // Support up to 1024 additional fragments of a single image.
        updateBulkData(fragments.size() + 1025);
        ret = getLastSegmentEnd();
        return ret + 8;
    }

    /**
     * Reads all bytes from this input stream and writes the bytes to the given output stream. This method
     * does not close either stream.
     *
     * @param out the output stream, non-null
     * @return the number of bytes transferred
     * @throws IOException if an I/O error occurs when reading or writing
     */
    public long transferTo(OutputStream out) throws IOException {
        Objects.requireNonNull(out, "out");
        long transferred = 0;
        byte[] buffer = new byte[Normal.DEFAULT_BUFFER_SIZE];
        int read;
        while ((read = this.read(buffer, 0, Normal.DEFAULT_BUFFER_SIZE)) > 0) {
            out.write(buffer, 0, read);
            transferred += read;
        }
        return transferred;
    }

    @Override
    public long length() {
        try {
            long wasPos = this.getStreamPosition();
            seek(Long.MAX_VALUE);
            long ret = this.getStreamPosition();
            seek(wasPos);
            Logger.debug("wasPos {} end {}", wasPos, ret);
            return ret;
        } catch (IOException e) {
            Logger.warn("Caught error determining length:{}", e);
            Logger.debug("Stack trace", e);
            return -1;
        }
    }

}
