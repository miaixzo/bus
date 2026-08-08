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
package org.miaixz.bus.image.nimble.opencv;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import javax.imageio.ImageReadParam;
import javax.imageio.stream.FileCacheImageInputStream;
import javax.imageio.stream.ImageInputStream;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.image.galaxy.data.BulkData;
import org.miaixz.bus.image.nimble.codec.BytesWithImageImageDescriptor;
import org.miaixz.bus.image.nimble.codec.ImageDescriptor;
import org.miaixz.bus.image.nimble.stream.SegmentedInputImageStream;
import org.miaixz.bus.logger.Logger;

/**
 * Represents the StreamSegment type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public abstract class StreamSegment {

    /**
     * The seg position value.
     */
    private final long[] segPosition;

    /**
     * The seg length value.
     */
    private final long[] segLength;

    /**
     * The image descriptor value.
     */
    private final ImageDescriptor imageDescriptor;

    /**
     * Creates a new instance.
     *
     * @param startPos        the start pos.
     * @param length          the length.
     * @param imageDescriptor the image descriptor.
     */
    StreamSegment(long[] startPos, long[] length, ImageDescriptor imageDescriptor) {
        this.segPosition = startPos;
        this.segLength = length;
        this.imageDescriptor = imageDescriptor;
    }

    /**
     * Gets the stream segment.
     *
     * @param iis   the iis.
     * @param param the param.
     * @return the stream segment.
     * @throws IOException if the operation cannot be completed.
     */
    public static StreamSegment getStreamSegment(ImageInputStream iis, ImageReadParam param) throws IOException {

        if (iis instanceof SegmentedImageStream) {
            return new FileStreamSegment((SegmentedImageStream) iis);
        } else if (iis instanceof SegmentedInputImageStream) {
            return getFileStreamSegment((SegmentedInputImageStream) iis);
        } else if (iis instanceof FileCacheImageInputStream) {
            throw new IllegalArgumentException("No adaptor implemented yet for FileCacheImageInputStream");
        } else if (iis instanceof BytesWithImageImageDescriptor) {
            BytesWithImageImageDescriptor stream = (BytesWithImageImageDescriptor) iis;
            return new MemoryStreamSegment(stream.getBytes(), stream.getImageDescriptor());
        }
        throw new IllegalArgumentException("No stream adaptor found for " + iis.getClass().getName() + Symbol.NOT);
    }

    /**
     * Determines whether input stream.
     *
     * @param iis the iis.
     * @return true if the condition is met; otherwise false.
     */
    public static boolean supportsInputStream(Object iis) {
        // This list must reflect getStreamSegment()'s implementation
        return (iis instanceof SegmentedImageStream) || (iis instanceof SegmentedInputImageStream)
                || (iis instanceof BytesWithImageImageDescriptor);
    }

    /**
     * Gets the file stream segment.
     *
     * @param iis the iis.
     * @return the file stream segment.
     */
    private static StreamSegment getFileStreamSegment(SegmentedInputImageStream iis) {
        try {
            long[][] seg = getSegments(iis);
            if (seg == null) {
                return null;
            }
            File file = iis.getFile();
            if (file != null) {
                return new FileStreamSegment(file, seg[0], seg[1], iis.getImageDescriptor());
            }
            ByteBuffer buffer = readFrame(iis, seg[1]);
            if (buffer != null) {
                return new MemoryStreamSegment(buffer, iis.getImageDescriptor());
            }
            Logger.error(false, "Image", "Cannot read SegmentedInputImageStream from {}", iis.getStream());
        } catch (Exception e) {
            Logger.error(false, "Image", "Building StreamSegment from SegmentedInputImageStream", e);
        }
        return null;
    }

    /**
     * Reads the encoded frame from the segmented stream into memory.
     *
     * @param iis       the segmented input stream
     * @param segLength the segment lengths
     * @return the frame buffer
     * @throws IOException if the operation cannot be completed
     */
    private static ByteBuffer readFrame(SegmentedInputImageStream iis, long[] segLength) throws IOException {
        long total = 0;
        for (long len : segLength) {
            total += len & 0xFFFFFFFFL;
        }
        if (total <= 0 || total > Integer.MAX_VALUE) {
            return null;
        }
        byte[] data = new byte[(int) total];
        long pos = iis.getStreamPosition();
        try {
            iis.seek(0);
            iis.readFully(data);
        } finally {
            iis.seek(pos);
        }
        return ByteBuffer.wrap(data);
    }

    /**
     * Gets the segments.
     *
     * @param iis the iis.
     * @return the segments.
     * @throws IOException if the operation cannot be completed.
     */
    private static long[][] getSegments(SegmentedInputImageStream iis) throws IOException {
        int curSegment = iis.getCurSegment();
        if (curSegment < 0) {
            return null;
        }
        ImageDescriptor desc = iis.getImageDescriptor();
        List<Object> fragments = iis.getFragments();
        Integer lastSegment = iis.getLastSegment();
        if (!desc.isMultiframe() && lastSegment < fragments.size()) {
            lastSegment = fragments.size();
        }
        long[] segPositions = new long[lastSegment - curSegment];
        long[] segLength = new long[segPositions.length];
        long beforePos = 0;

        for (int i = curSegment; i < lastSegment; i++) {
            synchronized (fragments) {
                if (i < fragments.size()) {
                    Object fragment = fragments.get(i);
                    int k = i - curSegment;
                    if (fragment instanceof BulkData) {
                        BulkData bulk = (BulkData) fragment;
                        segPositions[k] = bulk.offset();
                        segLength[k] = bulk.length();
                    } else {
                        byte[] byteFrag = (byte[]) fragment;
                        segPositions[k] = beforePos;
                        segLength[k] = byteFrag.length;
                    }
                    beforePos += segLength[k] & 0xFFFFFFFFL;
                }
            }
        }
        return new long[][] { segPositions, segLength };
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
     * Gets the seg position.
     *
     * @return the seg position.
     */
    public long[] getSegPosition() {
        return segPosition;
    }

    /**
     * Gets the seg length.
     *
     * @return the seg length.
     */
    public long[] getSegLength() {
        return segLength;
    }

}
