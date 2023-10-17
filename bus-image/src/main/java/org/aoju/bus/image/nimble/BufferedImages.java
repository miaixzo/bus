/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
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
package org.aoju.bus.image.nimble;

import org.aoju.bus.core.lang.Normal;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class BufferedImages {

    private BufferedImages() {

    }

    public static BufferedImage convertToIntRGB(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        if (cm instanceof DirectColorModel)
            return bi;

        if (cm.getNumComponents() != 3)
            throw new IllegalArgumentException("ColorModel: " + cm);

        WritableRaster raster = bi.getRaster();
        if (cm instanceof PaletteColorModel)
            return ((PaletteColorModel) cm).convertToIntDiscrete(raster);

        BufferedImage intRGB = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics graphics = intRGB.getGraphics();
        try {
            graphics.drawImage(bi, 0, 0, null);
        } finally {
            graphics.dispose();
        }
        return intRGB;
    }

    public static BufferedImage convertYBRtoRGB(BufferedImage src, BufferedImage dst) {
        if (src.getColorModel().getTransferType() != DataBuffer.TYPE_BYTE) {
            throw new UnsupportedOperationException(
                    "Cannot convert color model to RGB: unsupported transferType" + src.getColorModel().getTransferType());
        }
        if (src.getColorModel().getNumComponents() != 3) {
            throw new IllegalArgumentException("Unsupported colorModel: " + src.getColorModel());
        }

        int width = src.getWidth();
        int height = src.getHeight();
        if (null == dst) {
            ColorModel cmodel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[]{8, 8, 8},
                    false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
            SampleModel sampleModel = cmodel.createCompatibleSampleModel(width, height);
            DataBuffer dataBuffer = sampleModel.createDataBuffer();
            WritableRaster rasterDst = Raster.createWritableRaster(sampleModel, dataBuffer, null);
            dst = new BufferedImage(cmodel, rasterDst, false, null);
        }
        WritableRaster rasterDst = dst.getRaster();
        WritableRaster raster = src.getRaster();
        ColorSpace cs = src.getColorModel().getColorSpace();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                byte[] ba = (byte[]) raster.getDataElements(j, i, null);
                float[] fba = new float[]{(ba[0] & 0xFF) / 255f, (ba[1] & 0xFF) / 255f, (ba[2] & 0xFF) / 255f};
                float[] rgb = cs.toRGB(fba);
                ba[0] = (byte) (rgb[0] * 255);
                ba[1] = (byte) (rgb[1] * 255);
                ba[2] = (byte) (rgb[2] * 255);
                rasterDst.setDataElements(j, i, ba);
            }
        }
        return dst;
    }

    public static BufferedImage convertPalettetoRGB(BufferedImage src, BufferedImage dst) {
        ColorModel pcm = src.getColorModel();
        if (!(pcm instanceof PaletteColorModel)) {
            throw new UnsupportedOperationException(
                    "Cannot convert " + pcm.getClass().getName() + " to RGB");
        }

        int width = src.getWidth();
        int height = src.getHeight();
        if (null == dst) {
            ColorModel cmodel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[]{8, 8, 8},
                    false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
            SampleModel sampleModel = cmodel.createCompatibleSampleModel(width, height);
            DataBuffer dataBuffer = sampleModel.createDataBuffer();
            WritableRaster rasterDst = Raster.createWritableRaster(sampleModel, dataBuffer, null);
            dst = new BufferedImage(cmodel, rasterDst, false, null);
        }
        WritableRaster rasterDst = dst.getRaster();
        WritableRaster raster = src.getRaster();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                byte[] b = convertTo3Bytes(pcm, raster.getDataElements(j, i, null));
                rasterDst.setDataElements(j, i, b);
            }
        }
        return dst;
    }

    private static byte[] convertTo3Bytes(ColorModel pm, Object data) {
        byte[] b = new byte[3];
        int pix;
        if (data instanceof byte[]) {
            byte[] pixels = (byte[]) data;
            pix = pm.getRGB(pixels[0]);

        } else {
            short[] pixels = (short[]) data;
            pix = pm.getRGB(pixels[0]);
        }
        b[0] = (byte) ((pix >> Normal._16) & 0xff);
        b[1] = (byte) ((pix >> 8) & 0xff);
        b[2] = (byte) (pix & 0xff);
        return b;
    }

}
