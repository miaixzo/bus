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
package org.miaixz.bus.image.nimble.opencv;

import org.miaixz.bus.image.galaxy.data.BulkData;
import org.miaixz.bus.image.nimble.codec.BytesWithImageImageDescriptor;
import org.miaixz.bus.image.nimble.codec.ImageDescriptor;
import org.miaixz.bus.image.nimble.stream.SegmentedInputImageStream;
import org.miaixz.bus.logger.Logger;

import javax.imageio.ImageReadParam;
import javax.imageio.stream.FileCacheImageInputStream;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class StreamSegment {

    private final long[] segPosition;
    private final long[] segLength;
    private final ImageDescriptor imageDescriptor;

    StreamSegment(long[] startPos, long[] length, ImageDescriptor imageDescriptor) {
        this.segPosition = startPos;
        this.segLength = length;
        this.imageDescriptor = imageDescriptor;
    }

    public static StreamSegment getStreamSegment(ImageInputStream iis, ImageReadParam param) throws IOException {

        if (iis instanceof SegmentedImageStream) {
            return new FileStreamSegment((SegmentedImageStream) iis);
        } else if (iis instanceof SegmentedInputImageStream) {
            return getFileStreamSegment((SegmentedInputImageStream) iis);
        } else if (iis instanceof FileCacheImageInputStream) {
            throw new IllegalArgumentException("No adaptor implemented yet for FileCacheImageInputStream");
        } else if (iis instanceof BytesWithImageImageDescriptor stream) {
            return new MemoryStreamSegment(stream.getBytes(), stream.getImageDescriptor());
        }
        throw new IllegalArgumentException("No stream adaptor found for " + iis.getClass().getName() + "!");
    }

    public static boolean supportsInputStream(Object iis) {
        // This list must reflect getStreamSegment()'s implementation
        return
                (iis instanceof SegmentedImageStream) ||
                        (iis instanceof SegmentedInputImageStream) ||
                        (iis instanceof BytesWithImageImageDescriptor)
                ;
    }

    private static StreamSegment getFileStreamSegment(SegmentedInputImageStream iis) {
        try {

            ImageInputStream fstream = iis.getStream();
            Field fRaf = null;
            if (fstream instanceof FileImageInputStream) {
                fRaf = FileImageInputStream.class.getDeclaredField("raf");
            } else if (fstream instanceof FileCacheImageInputStream) {
                fRaf = FileCacheImageInputStream.class.getDeclaredField("cache");
            }

            if (fRaf != null) {
                fRaf.setAccessible(true);
                long[][] seg = getSegments(iis);
                if (seg != null) {
                    RandomAccessFile raf = (RandomAccessFile) fRaf.get(fstream);
                    /*
                     * PS 3.5.8.2 Though a fragment may not contain encoded data from more than one frame, the
                     * encoded data from one frame may span multiple fragments. See note in Section 8.2.
                     */
                    return new FileStreamSegment(raf, seg[0], seg[1], iis.getImageDescriptor());
                }
            }
            if (fstream instanceof MemoryCacheImageInputStream mstream) {
                byte[] b = MemoryStreamSegment.getByte(MemoryStreamSegment.getByteArrayInputStream(mstream));
                if (b != null) {
                    long[][] seg = getSegments(iis);
                    if (seg != null) {
                        int offset = (int) seg[0][0];
                        return new MemoryStreamSegment(
                                ByteBuffer.wrap(Arrays.copyOfRange(b, offset, offset + (int) seg[1][0])),
                                iis.getImageDescriptor());
                    }
                }
            }
            Logger.error("Cannot read SegmentedInputImageStream with {} ", fstream.getClass());
        } catch (Exception e) {
            Logger.error("Building FileStreamSegment from SegmentedInputImageStream", e);
        }
        return null;
    }

    private static long[][] getSegments(SegmentedInputImageStream iis) throws IOException {
        Integer curSegment = iis.getCurSegment();
        if (curSegment != null && curSegment >= 0) {
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
                        if (fragment instanceof BulkData bulk) {
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
            return new long[][]{segPositions, segLength};
        }
        return null;
    }

    public static byte[] getByte(ByteArrayInputStream inputStream) {
        if (inputStream != null) {
            try {
                Field fid = ByteArrayInputStream.class.getDeclaredField("buf");
                if (fid != null) {
                    fid.setAccessible(true);
                    return (byte[]) fid.get(inputStream);
                }
            } catch (Exception e) {
                Logger.error("Cannot get bytes from inputstream", e);
            }
        }
        return null;
    }

    public ImageDescriptor getImageDescriptor() {
        return imageDescriptor;
    }

    public long[] getSegPosition() {
        return segPosition;
    }

    public long[] getSegLength() {
        return segLength;
    }

}
