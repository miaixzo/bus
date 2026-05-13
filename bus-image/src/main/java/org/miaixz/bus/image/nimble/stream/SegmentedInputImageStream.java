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
package org.miaixz.bus.image.nimble.stream;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Objects;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageInputStreamImpl;

import org.miaixz.bus.core.xyz.ByteKit;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.data.BulkData;
import org.miaixz.bus.image.galaxy.data.Fragments;
import org.miaixz.bus.image.galaxy.data.VR;
import org.miaixz.bus.image.nimble.codec.ImageDescriptor;
import org.miaixz.bus.logger.Logger;

/**
 * Treats a specified portion of an image input stream as it's own input stream. Can handle open-ended specified
 * sub-regions by specifying a -1 frame, which will treat all fragments as though they are part of the segment, and will
 * look for new segments in the DICOM original data once the first part is read past.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class SegmentedInputImageStream extends ImageInputStreamImpl {

    /**
     * The default buffer size value.
     */
    private static final int DEFAULT_BUFFER_SIZE = 8192;

    /**
     * The stream value.
     */
    private final ImageInputStream stream;

    /**
     * The fragments value.
     */
    private final List<Object> fragments;

    /**
     * The cur segment value.
     */
    private int curSegment = 0;

    /**
     * Constants for the first segment, last segment values.
     */
    private int firstSegment = 1, lastSegment = Integer.MAX_VALUE;
    // The end of the current segment, in streamPos units, not in underlying stream units
    /**
     * The cur segment end value.
     */
    private long curSegmentEnd = -1;

    /**
     * The byte frag value.
     */
    private byte[] byteFrag;

    /**
     * The image descriptor value.
     */
    private ImageDescriptor imageDescriptor;

    /**
     * Create a segmented input stream, that updates the bulk data entries as required, frameIndex of -1 means the
     * entire object/value.
     */
    public SegmentedInputImageStream(ImageInputStream stream, Fragments pixeldataFragments, int frameIndex)
            throws IOException {
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

    /**
     * Creates a new instance.
     *
     * @param iis            the iis.
     * @param streamPosition the stream position.
     * @param length         the length.
     * @param singleFrame    the single frame.
     * @throws IOException if the operation cannot be completed.
     */
    public SegmentedInputImageStream(ImageInputStream iis, long streamPosition, int length, boolean singleFrame)
            throws IOException {
        fragments = new Fragments(VR.OB, false, 16);
        if (!singleFrame) {
            lastSegment = 2;
        }
        fragments.add(new byte[0]);
        fragments.add(new BulkData("pixelData://", streamPosition, length, false));
        stream = iis;
        seek(0);
    }

    /**
     * Just read from the raw data segment - this gets converted to an in-memory fragments object, which is then handled
     * as a single fragment, with no basic offset table. Basically just an easy way to get an image input stream on a
     * byte array.
     */
    public SegmentedInputImageStream(byte[] data) throws IOException {
        stream = null;
        fragments = new Fragments(VR.OB, false, 2);
        fragments.add(new byte[0]);
        fragments.add(data);
        lastSegment = 2;
        seek(0);
    }

    /**
     * Gets the image descriptor.
     *
     * @return the image descriptor.
     */
    public ImageDescriptor getImageDescriptor() {
        return imageDescriptor;
    }

    /**
     * Sets the image descriptor.
     *
     * @param imageDescriptor the image descriptor.
     */
    public void setImageDescriptor(ImageDescriptor imageDescriptor) {
        this.imageDescriptor = imageDescriptor;
    }

    /**
     * Executes the seek operation.
     *
     * @param pos the pos.
     * @throws IOException if the operation cannot be completed.
     */
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
                if (bulk == null) {
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
            beforePos += bulkLength & 0xFFFFFFFFL;
            if (pos < beforePos) {
                curSegment = i;
                curSegmentEnd = beforePos;
                if (bulk != null) {
                    stream.seek(bulk.offset() + deltaInEnd);
                }
                return;
            }
        }
        curSegment = -1;
    }

    /**
     * Updates the bulk data.
     *
     * @param endBulk the end bulk.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
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
                long testOffset = last.offset() + (0xFFFFFFFFL & last.length());
                bulk = readBulkAt(testOffset, i);
            } else if (bulkLength == -1) {
                bulk = readBulkAt(bulkOffset - 8, i);
            }
            if (bulk == null) {
                return null;
            }
            last = bulk;
        }
        return last;
    }

    /**
     * Reads the bulk at.
     *
     * @param testOffset the test offset.
     * @param at         the at.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    BulkData readBulkAt(long testOffset, int at) throws IOException {
        byte[] data = new byte[8];
        stream.seek(testOffset);
        int size = stream.read(data);
        if (size < 8)
            return null;
        int tag = ByteKit.bytesToTagLE(data, 0);
        if (tag == Tag.SequenceDelimitationItem) {
            // Safe to read un-protected now as we know there are no more items to update.
            lastSegment = fragments.size();
            return null;
        }
        if (tag != Tag.Item) {
            throw new IOException("At " + testOffset + " isn't an Item(" + Integer.toHexString(Tag.Item) + "), but is "
                    + Integer.toHexString(tag));
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

    /**
     * Executes the read operation.
     *
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
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

    /**
     * Executes the prepare read operation.
     *
     * @return true if the condition is met; otherwise false.
     * @throws IOException if the operation cannot be completed.
     */
    private boolean prepareRead() throws IOException {
        if (curSegment < 0)
            return false;

        if (streamPos < curSegmentEnd)
            return true;

        seek(streamPos);

        return curSegment >= 0 && curSegment < lastSegment;
    }

    /**
     * Executes the read operation.
     *
     * @param b   the b.
     * @param off the off.
     * @param len the len.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (!prepareRead())
            return -1;

        bitOffset = 0;
        int bytesToRead = Math.min(len, (int) (curSegmentEnd - streamPos));
        int nbytes;
        if (byteFrag != null) {
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

    /**
     * Gets the last segment end.
     *
     * @return the last segment end.
     */
    public long getLastSegmentEnd() {
        synchronized (fragments) {
            BulkData bulk = (BulkData) fragments.get(fragments.size() - 1);
            return bulk.getSegmentEnd();
        }
    }

    /**
     * Gets the offset post pixel data.
     *
     * @return the offset post pixel data.
     * @throws IOException if the operation cannot be completed.
     */
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
     * Reads all bytes from this input stream and writes the bytes to the given output stream. This method does not
     * close either stream.
     *
     * @param out the output stream, non-null
     * @return the number of bytes transferred
     * @throws IOException          if an I/O error occurs when reading or writing
     * @throws NullPointerException if out is {@code null}
     */
    public long transferTo(OutputStream out) throws IOException {
        Objects.requireNonNull(out, "out");
        long transferred = 0;
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int read;
        while ((read = this.read(buffer, 0, DEFAULT_BUFFER_SIZE)) > 0) {
            out.write(buffer, 0, read);
            transferred += read;
        }
        return transferred;
    }

    /**
     * Executes the length operation.
     *
     * @return the operation result.
     */
    @Override
    public long length() {
        try {
            long wasPos = this.getStreamPosition();
            seek(Long.MAX_VALUE);
            long ret = this.getStreamPosition();
            seek(wasPos);
            Logger.debug(false, "Image", "wasPos {} end {}", wasPos, ret);
            return ret;
        } catch (IOException e) {
            Logger.warn(false, "Image", "Caught error determining length:{}", e);
            Logger.debug(false, "Image", "Stack trace", e);
            return -1;
        }
    }

    /**
     * Gets the stream.
     *
     * @return the stream.
     */
    public ImageInputStream getStream() {
        return stream;
    }

    /**
     * Gets the cur segment.
     *
     * @return the cur segment.
     */
    public int getCurSegment() {
        return curSegment;
    }

    /**
     * Gets the fragments.
     *
     * @return the fragments.
     */
    public List<Object> getFragments() {
        return fragments;
    }

    /**
     * Gets the last segment.
     *
     * @return the last segment.
     * @throws IOException if the operation cannot be completed.
     */
    public Integer getLastSegment() throws IOException {
        seek(Long.MAX_VALUE);
        return lastSegment;
    }

}
