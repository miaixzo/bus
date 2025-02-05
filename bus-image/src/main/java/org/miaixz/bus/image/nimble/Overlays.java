/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.image.nimble;

import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.util.Arrays;
import java.util.function.Function;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.Sequence;
import org.miaixz.bus.logger.Logger;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class Overlays {

    public static int[] getActiveOverlayGroupOffsets(Attributes psattrs) {
        return getOverlayGroupOffsets(psattrs, Tag.OverlayActivationLayer, -1);
    }

    public static int[] getActiveOverlayGroupOffsets(Attributes attrs, int activationMask) {
        return getOverlayGroupOffsets(attrs, Tag.OverlayRows, activationMask);
    }

    public static int[] getOverlayGroupOffsets(Attributes attrs, int tag, int activationMask) {
        int len = 0;
        int[] result = new int[Normal._16];
        for (int i = 0; i < result.length; i++) {
            int gg0000 = i << 17;
            if ((activationMask & (1 << i)) != 0 && attrs.containsValue(tag | gg0000))
                result[len++] = gg0000;
        }
        return Arrays.copyOf(result, len);
    }

    public static int[] getEmbeddedOverlayGroupOffsets(Attributes attrs) {
        int len = 0;
        int[] result = new int[Normal._16];
        int bitsAllocated = attrs.getInt(Tag.BitsAllocated, 8);
        int bitsStored = attrs.getInt(Tag.BitsStored, bitsAllocated);
        for (int i = 0; i < result.length; i++) {
            int gg0000 = i << 17;
            if (attrs.getInt(Tag.OverlayBitsAllocated | gg0000, 1) != 1) {
                int ovlyBitPosition = attrs.getInt(Tag.OverlayBitPosition | gg0000, 0);
                if (ovlyBitPosition < bitsStored)
                    Logger.info("Ignore embedded overlay #{} from bit #{} < bits stored: {}", (gg0000 >>> 17) + 1,
                            ovlyBitPosition, bitsStored);
                else
                    result[len++] = gg0000;
            }
        }
        return Arrays.copyOf(result, len);
    }

    public static void extractFromPixeldata(Raster raster, int mask, byte[] ovlyData, int off, int length) {
        ComponentSampleModel sm = (ComponentSampleModel) raster.getSampleModel();
        int rows = raster.getHeight();
        int columns = raster.getWidth();
        int stride = sm.getScanlineStride();
        DataBuffer db = raster.getDataBuffer();
        switch (db.getDataType()) {
        case DataBuffer.TYPE_BYTE:
            extractFromPixeldata(((DataBufferByte) db).getData(), rows, columns, stride, mask, ovlyData, off, length);
            break;
        case DataBuffer.TYPE_USHORT:
            extractFromPixeldata(((DataBufferUShort) db).getData(), rows, columns, stride, mask, ovlyData, off, length);
            break;
        case DataBuffer.TYPE_SHORT:
            extractFromPixeldata(((DataBufferShort) db).getData(), rows, columns, stride, mask, ovlyData, off, length);
            break;
        default:
            throw new UnsupportedOperationException("Unsupported DataBuffer type: " + db.getDataType());
        }
    }

    private static void extractFromPixeldata(byte[] pixeldata, int rows, int columns, int stride, int mask,
            byte[] ovlyData, int off, int length) {
        for (int y = 0, i = off, imax = off + length; y < columns && i < imax; y++) {
            for (int j = y * stride, jmax = j + rows; j < jmax && i < imax; j++, i++) {
                if ((pixeldata[j] & mask) != 0)
                    ovlyData[i >>> 3] |= 1 << (i & 7);
            }
        }
    }

    private static void extractFromPixeldata(short[] pixeldata, int rows, int columns, int stride, int mask,
            byte[] ovlyData, int off, int length) {
        for (int y = 0, i = off, imax = off + length; y < rows && i < imax; y++) {
            for (int j = y * stride, jmax = j + columns; j < jmax && i < imax; j++, i++) {
                if ((pixeldata[j] & mask) != 0) {
                    ovlyData[i >>> 3] |= 1 << (i & 7);
                }
            }
        }
    }

    public static int[] getRecommendedGrayscalePixelValue(Attributes psAttrs, int gg0000, int bits) {
        int[] grayscaleValue = getRecommendedPixelValue(Tag.RecommendedDisplayGrayscaleValue, psAttrs, gg0000);
        return grayscaleValue != null && grayscaleValue.length > 0 ? new int[] { grayscaleValue[0] >> (16 - bits) }
                : null;
    }

    public static int[] getRecommendedRGBPixelValue(Attributes psAttrs, int gg0000) {
        return getRecommendedRGBPixelValue(psAttrs, gg0000, Function.identity());
    }

    public static int[] getRecommendedRGBPixelValue(Attributes psAttrs, int gg0000, ColorSpace cspace) {
        return getRecommendedRGBPixelValue(psAttrs, gg0000, cspace::fromRGB);
    }

    private static int[] getRecommendedRGBPixelValue(Attributes psAttrs, int gg0000,
            Function<float[], float[]> fromRGB) {
        int[] cieLabValue = getRecommendedPixelValue(Tag.RecommendedDisplayCIELabValue, psAttrs, gg0000);
        return cieLabValue != null && cieLabValue.length == 3 ? cieLab2RGB(cieLabValue, fromRGB) : null;
    }

    private static int[] cieLab2RGB(int[] cieLabValue, Function<float[], float[]> adjustColorSpace) {
        float[] colorvalue = { (cieLabValue[0] & 0xffff) / 655.35f, ((cieLabValue[1] & 0xffff) - 0x8080) / 257.0f,
                ((cieLabValue[2] & 0xffff) - 0x8080) / 257.0f };
        float[] rgb = CIELabColorSpace.getInstance().toRGB(colorvalue);
        rgb = adjustColorSpace.apply(rgb);
        int[] pixel = new int[3];
        for (int i = 0; i < 3; i++) {
            pixel[i] = (int) (rgb[i] * 255 + 0.5f);
        }
        return pixel;
    }

    private static int[] getRecommendedPixelValue(int tag, Attributes psAttrs, int gg0000) {
        int tagOverlayActivationLayer = Tag.OverlayActivationLayer | gg0000;
        String layerName = psAttrs.getString(tagOverlayActivationLayer);
        if (layerName == null)
            throw new IllegalArgumentException(
                    "Missing " + Tag.toString(tagOverlayActivationLayer) + " Overlay Activation Layer");
        Sequence layers = psAttrs.getSequence(Tag.GraphicLayerSequence);
        if (layers == null)
            throw new IllegalArgumentException(
                    "Missing " + Tag.toString(Tag.GraphicLayerSequence) + " Graphic Layer Sequence");

        for (Attributes layer : layers)
            if (layerName.equals(layer.getString(Tag.GraphicLayer)))
                return layer.getInts(tag);

        throw new IllegalArgumentException("No Graphic Layer: " + layerName);
    }

    public static void applyOverlay(int frameIndex, WritableRaster raster, Attributes attrs, int gg0000, int pixelValue,
            byte[] ovlyData) {
        applyOverlay(frameIndex, raster, attrs, gg0000, new int[] { pixelValue }, ovlyData);
    }

    public static void applyOverlay(int frameIndex, WritableRaster raster, Attributes attrs, int gg0000,
            int[] pixelValue, byte[] ovlyData) {
        int imageFrameOrigin = attrs.getInt(Tag.ImageFrameOrigin | gg0000, 1);
        int framesInOverlay = attrs.getInt(Tag.NumberOfFramesInOverlay | gg0000, 1);
        int ovlyFrameIndex = frameIndex - imageFrameOrigin + 1;
        if (ovlyFrameIndex < 0 || ovlyFrameIndex >= framesInOverlay)
            return;

        int tagOverlayRows = Tag.OverlayRows | gg0000;
        int tagOverlayColumns = Tag.OverlayColumns | gg0000;
        int tagOverlayData = Tag.OverlayData | gg0000;
        int tagOverlayOrigin = Tag.OverlayOrigin | gg0000;

        int ovlyRows = attrs.getInt(tagOverlayRows, -1);
        int ovlyColumns = attrs.getInt(tagOverlayColumns, -1);
        int[] ovlyOrigin = attrs.getInts(tagOverlayOrigin);
        if (ovlyData == null)
            ovlyData = attrs.getSafeBytes(tagOverlayData);

        if (ovlyData == null)
            throw new IllegalArgumentException("Missing " + Tag.toString(tagOverlayData) + " Overlay Data");
        if (ovlyRows <= 0)
            throw new IllegalArgumentException(Tag.toString(tagOverlayRows) + " Overlay Rows [" + ovlyRows + "]");
        if (ovlyColumns <= 0)
            throw new IllegalArgumentException(
                    Tag.toString(tagOverlayColumns) + " Overlay Columns [" + ovlyColumns + "]");
        if (ovlyOrigin == null)
            throw new IllegalArgumentException("Missing " + Tag.toString(tagOverlayOrigin) + " Overlay Origin");
        if (ovlyOrigin.length != 2)
            throw new IllegalArgumentException(
                    Tag.toString(tagOverlayOrigin) + " Overlay Origin " + Arrays.toString(ovlyOrigin));

        int x0 = ovlyOrigin[1] - 1;
        int y0 = ovlyOrigin[0] - 1;

        int ovlyLen = ovlyRows * ovlyColumns;
        int ovlyOff = ovlyLen * ovlyFrameIndex;
        int end = (ovlyOff + ovlyLen + 7) >>> 3;
        if (end > ovlyData.length) {
            Logger.warn("OverlayData to small ({} vs. {})! Skip this overlay:{}", ovlyData.length, end,
                    Tag.toString(tagOverlayData));
            return;
        }
        for (int i = ovlyOff >>> 3; i < end; i++) {
            int ovlyBits = ovlyData[i] & 0xff;
            for (int j = 0; (ovlyBits >>> j) != 0; j++) {
                if ((ovlyBits & (1 << j)) == 0)
                    continue;

                int ovlyIndex = ((i << 3) + j) - ovlyOff;
                if (ovlyIndex >= ovlyLen)
                    continue;

                int y = y0 + ovlyIndex / ovlyColumns;
                int x = x0 + ovlyIndex % ovlyColumns;
                try {
                    raster.setPixel(x, y, pixelValue);
                } catch (ArrayIndexOutOfBoundsException ignore) {
                }
            }
        }
    }

}
