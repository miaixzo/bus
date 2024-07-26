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
package org.miaixz.bus.image.nimble.codec.jpeg;

import org.miaixz.bus.core.xyz.ByteKit;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.VR;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class JPEGHeader {

    private final byte[] data;
    private final int[] offsets;

    public JPEGHeader(byte[] data, int lastMarker) {
        int n = 0;
        for (int offset = 0; (offset = nextMarker(data, offset)) != -1;) {
            n++;
            int marker = data[offset++] & 255;
            if (JPEG.isStandalone(marker))
                continue;
            if (offset + 1 >= data.length)
                break;
            if (marker == lastMarker)
                break;
            offset += ByteKit.bytesToUShortBE(data, offset);
        }
        this.data = data;
        this.offsets = new int[n];
        for (int i = 0, offset = 0; i < n; i++) {
            offsets[i] = (offset = nextMarker(data, offset));
            if (!JPEG.isStandalone(data[offset++] & 255))
                offset += ByteKit.bytesToUShortBE(data, offset);
        }
    }

    private static int nextMarker(byte[] data, int from) {
        for (int i = from + 1; i < data.length; i++) {
            if (data[i - 1] == -1 && data[i] != -1 && data[i] != 0) {
                return i;
            }
        }
        return -1;
    }

    public int offsetOf(int marker) {
        for (int i = 0; i < offsets.length; i++) {
            if (marker(i) == marker)
                return offsets[i];
        }
        return -1;
    }

    public int offsetSOF() {
        for (int i = 0; i < offsets.length; i++) {
            if (JPEG.isSOF(marker(i)))
                return offsets[i];
        }
        return -1;
    }

    public int offsetAfterAPP() {
        for (int i = 1; i < offsets.length; i++) {
            if (!JPEG.isAPP(marker(i)))
                return offsets[i];
        }
        return -1;
    }

    public int offset(int index) {
        return offsets[index];
    }

    public int marker(int index) {
        return data[offsets[index]] & 255;
    }

    public int numberOfMarkers() {
        return offsets.length;
    }

    /**
     * Return corresponding Image Pixel Description Macro Attributes
     *
     * @param attrs target {@code Attributes} or {@code null}
     * @return Image Pixel Description Macro Attributes
     */
    public Attributes toAttributes(Attributes attrs) {
        int offsetSOF = offsetSOF();
        if (offsetSOF == -1)
            return null;

        if (attrs == null)
            attrs = new Attributes(10);

        int sof = data[offsetSOF] & 255;
        int p = data[offsetSOF + 3] & 0xff;
        int y = ((data[offsetSOF + 3 + 1] & 0xff) << 8) | (data[offsetSOF + 3 + 2] & 0xff);
        int x = ((data[offsetSOF + 3 + 3] & 0xff) << 8) | (data[offsetSOF + 3 + 4] & 0xff);
        int nf = data[offsetSOF + 3 + 5] & 0xff;
        attrs.setInt(Tag.SamplesPerPixel, VR.US, nf);
        if (nf == 3) {
            attrs.setString(Tag.PhotometricInterpretation, VR.CS,
                    (sof == JPEG.SOF3 || sof == JPEG.SOF55) ? "RGB" : "YBR_FULL_422");
            attrs.setInt(Tag.PlanarConfiguration, VR.US, 0);
        } else {
            attrs.setString(Tag.PhotometricInterpretation, VR.CS, "MONOCHROME2");
        }
        attrs.setInt(Tag.Rows, VR.US, y);
        attrs.setInt(Tag.Columns, VR.US, x);
        attrs.setInt(Tag.BitsAllocated, VR.US, p > 8 ? 16 : 8);
        attrs.setInt(Tag.BitsStored, VR.US, p);
        attrs.setInt(Tag.HighBit, VR.US, p - 1);
        attrs.setInt(Tag.PixelRepresentation, VR.US, 0);
        if (!(sof == JPEG.SOF3 || (sof == JPEG.SOF55 && ss() == 0)))
            attrs.setString(Tag.LossyImageCompression, VR.CS, "01");
        return attrs;
    }

    public String getTransferSyntaxUID() {
        int sofOffset = offsetSOF();
        if (sofOffset == -1)
            return null;

        switch (data[sofOffset] & 255) {
        case JPEG.SOF0:
            return UID.JPEGBaseline8Bit.uid;
        case JPEG.SOF1:
            return UID.JPEGExtended12Bit.uid;
        case JPEG.SOF2:
            return UID.JPEGFullProgressionNonHierarchical1012.uid;
        case JPEG.SOF3:
            return ss() == 1 ? UID.JPEGLosslessSV1.uid : UID.JPEGLossless.uid;
        case JPEG.SOF55:
            return ss() == 0 ? UID.JPEGLSLossless.uid : UID.JPEGLSNearLossless.uid;
        }
        return null;
    }

    private int ss() {
        int offsetSOS = offsetOf(JPEG.SOS);
        return offsetSOS != -1 ? data[offsetSOS + 6] & 255 : -1;
    }

}
