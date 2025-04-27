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
package org.miaixz.bus.image.nimble.opencv;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class ImageConversion {

    private ImageConversion() {
    }

    /**
     * Converts a <code>Mat</code> into a <code>BufferedImage</code>.
     *
     * @param matrix a <code>Mat</code> to be converted
     * @return a <code>BufferedImage</code>
     */
    public static BufferedImage toBufferedImage(Mat matrix) {
        if (matrix == null) {
            return null;
        }

        int cols = matrix.cols();
        int rows = matrix.rows();
        int type = matrix.type();
        int elemSize = CvType.ELEM_SIZE(type);
        int channels = CvType.channels(type);
        int bpp = (elemSize * 8) / channels;

        ColorSpace cs;
        WritableRaster raster;
        ComponentColorModel colorModel;
        int dataType = convertToDataType(type);

        switch (channels) {
        case 1:
            cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
            colorModel = new ComponentColorModel(cs, new int[] { bpp }, false, true, Transparency.OPAQUE, dataType);
            raster = colorModel.createCompatibleWritableRaster(cols, rows);
            break;
        case 3:
            cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
            colorModel = new ComponentColorModel(cs, new int[] { bpp, bpp, bpp }, false, false, Transparency.OPAQUE,
                    dataType);
            raster = Raster.createInterleavedRaster(dataType, cols, rows, cols * channels, channels,
                    new int[] { 2, 1, 0 }, null);
            break;
        default:
            throw new UnsupportedOperationException("No implementation to handle " + channels + " channels");
        }

        DataBuffer buf = raster.getDataBuffer();

        if (buf instanceof DataBufferByte bufferByte) {
            matrix.get(0, 0, bufferByte.getData());
        } else if (buf instanceof DataBufferUShort bufferUShort) {
            matrix.get(0, 0, bufferUShort.getData());
        } else if (buf instanceof DataBufferShort bufferShort) {
            matrix.get(0, 0, bufferShort.getData());
        } else if (buf instanceof DataBufferInt bufferInt) {
            matrix.get(0, 0, bufferInt.getData());
        } else if (buf instanceof DataBufferFloat bufferFloat) {
            matrix.get(0, 0, bufferFloat.getData());
        } else if (buf instanceof DataBufferDouble bufferDouble) {
            matrix.get(0, 0, bufferDouble.getData());
        }
        return new BufferedImage(colorModel, raster, false, null);
    }

    /**
     * Converts a <code>PlanarImage</code> into a <code>BufferedImage</code>.
     *
     * @param matrix a <code>PlanarImage</code> to be converted
     * @return a <code>BufferedImage</code>
     */
    public static BufferedImage toBufferedImage(PlanarImage matrix) {
        if (matrix == null) {
            return null;
        }
        return toBufferedImage(matrix.toMat());
    }

    /**
     * Free the memory of the content of <code>Mat</code> while preserving the object.
     *
     * @param mat a <code>Mat</code> to be released
     */
    public static void releaseMat(Mat mat) {
        if (mat != null) {
            mat.release();
        }
    }

    /**
     * Free the memory of the content of <code>PlanarImage</code> while preserving the object.
     *
     * @param img a <code>PlanarImage</code> to be released
     */
    public static void releasePlanarImage(PlanarImage img) {
        if (img != null) {
            img.release();
        }
    }

    /**
     * Converts a data type from <code>CvType</code> (OpenCV) to <code>DataBuffer</code> (Java) type.
     *
     * @return the <code>DataBuffer</code> type
     */
    public static int convertToDataType(int cvType) {
        return switch (CvType.depth(cvType)) {
        case CvType.CV_8U, CvType.CV_8S -> DataBuffer.TYPE_BYTE;
        case CvType.CV_16U -> DataBuffer.TYPE_USHORT;
        case CvType.CV_16S -> DataBuffer.TYPE_SHORT;
        case CvType.CV_32S -> DataBuffer.TYPE_INT;
        case CvType.CV_32F -> DataBuffer.TYPE_FLOAT;
        case CvType.CV_64F -> DataBuffer.TYPE_DOUBLE;
        default -> throw new UnsupportedOperationException("Unsupported CvType value: " + cvType);
        };
    }

    /**
     * @see #toMat(RenderedImage, Rectangle, boolean)
     */
    public static ImageCV toMat(RenderedImage img) {
        return toMat(img, null);
    }

    /**
     * @see #toMat(RenderedImage, Rectangle, boolean)
     */
    public static ImageCV toMat(RenderedImage img, Rectangle region) {
        return toMat(img, region, true);
    }

    /**
     * Converts a <code>RenderedImage</code> into a <code>Mat</code>.
     *
     * @param img    a <code>RenderedImage</code> to be converted
     * @param region a Rectangle that specifies the region of the <code>RenderedImage</code> to be converted
     * @param toBGR  a boolean that specifies if the <code>Mat</code> should be converted to BGR. Should be true for
     *               most cases.
     * @return a ImageCV
     */
    public static ImageCV toMat(RenderedImage img, Rectangle region, boolean toBGR) {
        return toMat(img, region, toBGR, false);
    }

    /**
     * Converts a <code>RenderedImage</code> into a <code>Mat</code>.
     *
     * @param img            a <code>RenderedImage</code> to be converted
     * @param region         a Rectangle that specifies the region of the <code>RenderedImage</code> to be converted
     * @param toBGR          a boolean that specifies if the <code>Mat</code> should be converted to BGR. This should be
     *                       true in most cases
     * @param forceShortType a boolean that specifies if the <code>Mat</code> should be converted to a short type. This
     *                       should be false in most cases
     * @return a ImageCV
     */
    public static ImageCV toMat(RenderedImage img, Rectangle region, boolean toBGR, boolean forceShortType) {
        Raster raster = region == null ? img.getData() : img.getData(region);
        DataBuffer buf = raster.getDataBuffer();
        int[] samples = raster.getSampleModel().getSampleSize();
        int[] offsets;
        if (raster.getSampleModel() instanceof ComponentSampleModel model) {
            offsets = model.getBandOffsets();
        } else {
            offsets = new int[samples.length];
            for (int i = 0; i < offsets.length; i++) {
                offsets[i] = i;
            }
        }

        if (isBinary(raster.getSampleModel())) {
            // Sonar false positive: not mandatory to close ImageCV (can be done when dereferenced)
            ImageCV mat = new ImageCV(raster.getHeight(), raster.getWidth(), CvType.CV_8UC1); // NOSONAR
            mat.put(0, 0, getUnpackedBinaryData(raster, raster.getBounds()));
            return mat;
        }

        if (buf instanceof DataBufferByte bufferByte) {
            if (Arrays.equals(offsets, new int[] { 0, 0, 0 })) {
                Mat b = new Mat(raster.getHeight(), raster.getWidth(), CvType.CV_8UC1);
                b.put(0, 0, bufferByte.getData(2));
                Mat g = new Mat(raster.getHeight(), raster.getWidth(), CvType.CV_8UC1);
                g.put(0, 0, bufferByte.getData(1));
                ImageCV r = new ImageCV(raster.getHeight(), raster.getWidth(), CvType.CV_8UC1);
                r.put(0, 0, bufferByte.getData(0));
                List<Mat> mv = toBGR ? Arrays.asList(b, g, r) : Arrays.asList(r, g, b);
                ImageCV dstImg = new ImageCV(raster.getHeight(), raster.getWidth(), CvType.CV_8UC3);
                Core.merge(mv, dstImg);
                return dstImg;
            }

            ImageCV mat = new ImageCV(raster.getHeight(), raster.getWidth(), CvType.CV_8UC(samples.length));
            mat.put(0, 0, ((DataBufferByte) buf).getData());
            if (toBGR && Arrays.equals(offsets, new int[] { 0, 1, 2 })) {
                ImageCV dstImg = new ImageCV();
                Imgproc.cvtColor(mat, dstImg, Imgproc.COLOR_RGB2BGR);
                return dstImg;
            } else if (!toBGR && Arrays.equals(offsets, new int[] { 2, 1, 0 })) {
                ImageCV dstImg = new ImageCV();
                Imgproc.cvtColor(mat, dstImg, Imgproc.COLOR_BGR2RGB);
                return dstImg;
            }
            return mat;
        } else if (buf instanceof DataBufferUShort bufferUShort) {
            ImageCV mat = new ImageCV(raster.getHeight(), raster.getWidth(),
                    forceShortType ? CvType.CV_16SC(samples.length) : CvType.CV_16UC(samples.length));
            mat.put(0, 0, bufferUShort.getData());
            return mat;
        } else if (buf instanceof DataBufferShort bufferShort) {
            ImageCV mat = new ImageCV(raster.getHeight(), raster.getWidth(), CvType.CV_16SC(samples.length));
            mat.put(0, 0, bufferShort.getData());
            return mat;
        } else if (buf instanceof DataBufferInt bufferInt) {
            ImageCV mat = new ImageCV(raster.getHeight(), raster.getWidth(), CvType.CV_32SC(samples.length));
            mat.put(0, 0, bufferInt.getData());
            return mat;
        } else if (buf instanceof DataBufferFloat bufferFloat) {
            ImageCV mat = new ImageCV(raster.getHeight(), raster.getWidth(), CvType.CV_32FC(samples.length));
            mat.put(0, 0, bufferFloat.getData());
            return mat;
        } else if (buf instanceof DataBufferDouble bufferDouble) {
            ImageCV mat = new ImageCV(raster.getHeight(), raster.getWidth(), CvType.CV_64FC(samples.length));
            mat.put(0, 0, bufferDouble.getData());
            return mat;
        }

        return null;
    }

    /**
     * Returns the bounds of the supplied <code>PlanarImage</code>.
     *
     * @param img a <code>PlanarImage</code>
     * @return the bounds of the supplied <code>PlanarImage</code>
     */
    public static Rectangle getBounds(PlanarImage img) {
        return new Rectangle(0, 0, img.width(), img.height());
    }

    /**
     * Converts a <code>RenderedImage</code> into a <code>BufferedImage</code> with a specific BufferedImage type.
     *
     * @param src a <code>RenderedImage</code> to be converted
     * @return a <code>BufferedImage</code>
     */
    public static BufferedImage convertTo(RenderedImage src, int imageType) {
        BufferedImage dst = new BufferedImage(src.getWidth(), src.getHeight(), imageType);
        Graphics2D big = dst.createGraphics();
        try {
            big.drawRenderedImage(src, AffineTransform.getTranslateInstance(0.0, 0.0));
        } finally {
            big.dispose();
        }
        return dst;
    }

    /**
     * Checks if the supplied <code>SampleModel</code> is a binary sample model.
     *
     * @param sm the <code>SampleModel</code> to be tested
     * @return <code>true</code> if the supplied <code>SampleModel</code> is a binary sample model
     */
    public static boolean isBinary(SampleModel sm) {
        return sm instanceof MultiPixelPackedSampleModel model && model.getPixelBitStride() == 1
                && sm.getNumBands() == 1;
    }

    /**
     * Converts a <code>RenderedImage</code> into a <code>BufferedImage</code>.
     *
     * @param img a <code>RenderedImage</code> to be converted
     * @return a <code>BufferedImage</code>
     */
    public static BufferedImage convertRenderedImage(RenderedImage img) {
        if (img == null) {
            return null;
        }
        if (img instanceof BufferedImage bufferedImage) {
            return bufferedImage;
        }
        ColorModel cm = img.getColorModel();
        int width = img.getWidth();
        int height = img.getHeight();
        WritableRaster raster = cm.createCompatibleWritableRaster(width, height);
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        Hashtable<String, Object> properties = new Hashtable<>();
        String[] keys = img.getPropertyNames();
        if (keys != null) {
            for (String key : keys) {
                properties.put(key, img.getProperty(key));
            }
        }
        BufferedImage result = new BufferedImage(cm, raster, isAlphaPremultiplied, properties);
        img.copyData(raster);
        return result;
    }

    /**
     * Returns the binary data unpacked into an array of bytes. The line stride will be the width of the
     * <code>Raster</code>.
     *
     * @throws IllegalArgumentException if <code>isBinary()</code> returns <code>false</code> with the
     *                                  <code>SampleModel</code> of the supplied <code>Raster</code> as argument.
     */
    public static byte[] getUnpackedBinaryData(Raster raster, Rectangle rect) {
        SampleModel sm = raster.getSampleModel();
        if (!isBinary(sm)) {
            throw new IllegalArgumentException("Not a binary raster!");
        }

        int rectX = rect.x;
        int rectY = rect.y;
        int rectWidth = rect.width;
        int rectHeight = rect.height;

        DataBuffer dataBuffer = raster.getDataBuffer();

        int dx = rectX - raster.getSampleModelTranslateX();
        int dy = rectY - raster.getSampleModelTranslateY();

        MultiPixelPackedSampleModel mpp = (MultiPixelPackedSampleModel) sm;
        int lineStride = mpp.getScanlineStride();
        int eltOffset = dataBuffer.getOffset() + mpp.getOffset(dx, dy);
        int bitOffset = mpp.getBitOffset(dx);

        byte[] bData = new byte[rectWidth * rectHeight];
        int maxY = rectY + rectHeight;
        int maxX = rectX + rectWidth;
        int k = 0;

        if (dataBuffer instanceof DataBufferByte bufferByte) {
            byte[] data = bufferByte.getData();
            for (int y = rectY; y < maxY; y++) {
                int bOffset = eltOffset * 8 + bitOffset;
                for (int x = rectX; x < maxX; x++) {
                    byte b = data[bOffset / 8];
                    bData[k++] = (byte) ((b >>> (7 - bOffset & 7)) & 0x0000001);
                    bOffset++;
                }
                eltOffset += lineStride;
            }
        } else if (dataBuffer instanceof DataBufferShort || dataBuffer instanceof DataBufferUShort) {
            short[] data = dataBuffer instanceof DataBufferShort bufferShort ? bufferShort.getData()
                    : ((DataBufferUShort) dataBuffer).getData();
            for (int y = rectY; y < maxY; y++) {
                int bOffset = eltOffset * 16 + bitOffset;
                for (int x = rectX; x < maxX; x++) {
                    short s = data[bOffset / 16];
                    bData[k++] = (byte) ((s >>> (15 - bOffset % 16)) & 0x0000001);
                    bOffset++;
                }
                eltOffset += lineStride;
            }
        } else if (dataBuffer instanceof DataBufferInt bufferInt) {
            int[] data = bufferInt.getData();
            for (int y = rectY; y < maxY; y++) {
                int bOffset = eltOffset * 32 + bitOffset;
                for (int x = rectX; x < maxX; x++) {
                    int i = data[bOffset / 32];
                    bData[k++] = (byte) ((i >>> (31 - bOffset % 32)) & 0x0000001);
                    bOffset++;
                }
                eltOffset += lineStride;
            }
        }

        return bData;
    }

}
