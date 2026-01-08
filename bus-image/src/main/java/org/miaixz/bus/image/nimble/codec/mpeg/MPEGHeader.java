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
package org.miaixz.bus.image.nimble.codec.mpeg;

import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.VR;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class MPEGHeader {

    private static final String[] ASPECT_RATIO_1_1 = { "1", "1" };
    private static final String[] ASPECT_RATIO_4_3 = { "4", "3" };
    private static final String[] ASPECT_RATIO_16_9 = { "16", "9" };
    private static final String[] ASPECT_RATIO_221_100 = { "221", "100" };
    private static final String[][] ASPECT_RATIOS = { ASPECT_RATIO_1_1, ASPECT_RATIO_4_3, ASPECT_RATIO_16_9,
            ASPECT_RATIO_221_100 };
    static final int[] FPS = { 24, 1001, 24, 1000, 25, 1000, 30, 1001, 30, 1000, 50, 1000, 60, 1001, 60, 1000 };
    private final byte[] data;
    private final int seqHeaderOffset;

    public MPEGHeader(byte[] data) {
        this.data = data;
        this.seqHeaderOffset = findSequenceHeaderOffset(data);
    }

    /**
     * Finds the offset of the MPEG sequence header start code (0x00 0x00 0x01 0xB3).
     *
     * @param data the byte array to search in
     * @return the offset after the start code, or -1 if not found
     */
    private static int findSequenceHeaderOffset(byte[] data) {
        if (data == null || data.length < 4) {
            return -1;
        }

        // Search for the sequence header start code: 0x00 0x00 0x01 0xB3
        for (int i = 0; i <= data.length - 4; i++) {
            if (data[i] == 0x00 && data[i + 1] == 0x00 && data[i + 2] == 0x01 && data[i + 3] == (byte) 0xB3) {
                return i + 4;
            }
        }

        return -1;
    }

    public boolean isValid() {
        return seqHeaderOffset != -1;
    }

    /**
     * Return corresponding Image Pixel Description Macro Attributes
     *
     * @param attrs  target {@code Attributes} or {@code null}
     * @param length MPEG stream length
     * @return Image Pixel Description Macro Attributes
     */
    public Attributes toAttributes(Attributes attrs, long length) {
        if (seqHeaderOffset == -1)
            return null;

        if (attrs == null)
            attrs = new Attributes(15);

        int off = seqHeaderOffset;
        int x = ((data[off + 1] & 0xFF) << 4) | ((data[off + 2] & 0xF0) >> 4);
        int y = ((data[off + 2] & 0x0F) << 8) | (data[off + 3] & 0xFF);
        int aspectRatio = (data[off + 4] >> 4) & 0x0F;
        int frameRate = data[off + 4] & 0x0F;
        int bitRate = ((data[off + 5] & 0xFF) << 10) | ((data[off + 6] & 0xFF) << 2) | ((data[off + 7] & 0xC0) >> 6);
        int numFrames = 9999;
        if (frameRate > 0 && frameRate < 9) {
            int frameRate2 = (frameRate - 1) << 1;
            attrs.setInt(Tag.CineRate, VR.IS, FPS[frameRate2]);
            attrs.setFloat(Tag.FrameTime, VR.DS, ((float) FPS[frameRate2 + 1]) / FPS[frameRate2]);
            if (bitRate > 0)
                numFrames = (int) (20 * length * FPS[frameRate2] / FPS[frameRate2 + 1] / bitRate);
        }
        if (aspectRatio > 0 && aspectRatio < 5)
            attrs.setString(Tag.PixelAspectRatio, VR.IS, ASPECT_RATIOS[aspectRatio - 1]);
        return setImageAttributes(attrs, numFrames, y, x);
    }

    public static Attributes setImageAttributes(Attributes attrs, int numFrames, int rows, int columns) {
        attrs.setInt(Tag.SamplesPerPixel, VR.US, 3);
        attrs.setString(Tag.PhotometricInterpretation, VR.CS, "YBR_PARTIAL_420");
        attrs.setInt(Tag.PlanarConfiguration, VR.US, 0);
        attrs.setInt(Tag.FrameIncrementPointer, VR.AT, Tag.FrameTime);
        attrs.setInt(Tag.NumberOfFrames, VR.IS, numFrames);
        attrs.setInt(Tag.Rows, VR.US, rows);
        attrs.setInt(Tag.Columns, VR.US, columns);
        attrs.setInt(Tag.BitsAllocated, VR.US, 8);
        attrs.setInt(Tag.BitsStored, VR.US, 8);
        attrs.setInt(Tag.HighBit, VR.US, 7);
        attrs.setInt(Tag.PixelRepresentation, VR.US, 0);
        attrs.setString(Tag.LossyImageCompression, VR.CS, "01");
        return attrs;
    }

}
