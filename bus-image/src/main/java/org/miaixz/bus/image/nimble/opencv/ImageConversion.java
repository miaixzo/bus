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
import org.opencv.core.MatOfInt;

/**
 * Conversions between OpenCV Mat and Java2D images (gray and RGB, all OpenCV depths).
 *
 * @author Kimi Liu
 */
public class ImageConversion {

    private static final int[] RGB_OFFSETS = { 0, 1, 2 };

    private static final int[] BGR_OFFSETS = { 2, 1, 0 };

    private static final int[] BANDED_RGB_OFFSETS = { 0, 0, 0 };

    /**
     * * Creates a new instance.
     */
    public ImageConversion() {
        // No initialization required.
    }

    /**
     * Converts a Mat to a BufferedImage. Supports 1 channel (gray) and 3 channels (BGR) for all OpenCV depths.
     *
     * @param matrix the Mat to convert, may be null
     * @return the BufferedImage, or null if input is null
     * @throws UnsupportedOperationException if the Mat has an unsupported format
     */
    public static BufferedImage toBufferedImage(Mat matrix) {
        if (matrix == null) {
            return null;
        }
        int cols = matrix.cols();
        int rows = matrix.rows();
        int type = matrix.type();
        int channels = CvType.channels(type);
        int bpp = (CvType.ELEM_SIZE(type) * 8) / channels;
        int dataType = convertToDataType(type);

        ColorModel colorModel = createColorModel(channels, bpp, dataType);
        WritableRaster raster = createRaster(colorModel, channels, cols, rows, dataType);
        populateRasterFromMat(matrix, raster);
        return new BufferedImage(colorModel, raster, false, null);
    }

    public static BufferedImage toBufferedImage(PlanarImage matrix) {
        return matrix == null ? null : toBufferedImage(matrix.toMat());
    }

    /**
     * Releases the native memory of a Mat, ignoring null.
     */
    public static void releaseMat(Mat mat) {
        if (mat != null) {
            mat.release();
        }
    }

    /**
     * Releases the native memory of a PlanarImage, ignoring null.
     */
    public static void releasePlanarImage(PlanarImage img) {
        if (img != null) {
            img.release();
        }
    }

    /**
     * Converts an OpenCV type to the corresponding DataBuffer type.
     *
     * @throws UnsupportedOperationException if the depth is not supported
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

    public static ImageCV toMat(RenderedImage img) {
        return toMat(img, null, true, false);
    }

    public static ImageCV toMat(RenderedImage img, Rectangle region) {
        return toMat(img, region, true, false);
    }

    public static ImageCV toMat(RenderedImage img, Rectangle region, boolean toBGR) {
        return toMat(img, region, toBGR, false);
    }

    /**
     * Converts a RenderedImage to a Mat. Tightly packed rasters are copied in one native call; any other layout (packed
     * int pixels, sub-images, padded rows) is read through the Raster API.
     *
     * @param img            the image to convert
     * @param region         the region to convert, null for the whole image
     * @param toBGR          true to order the channels of 3-band images as BGR, false for RGB
     * @param forceShortType true to store 16-bit unsigned samples as signed short
     * @return the converted image
     */
    public static ImageCV toMat(RenderedImage img, Rectangle region, boolean toBGR, boolean forceShortType) {
        Raster raster = getRaster(img, region);
        SampleModel sampleModel = raster.getSampleModel();
        if (isBinary(sampleModel)) {
            return createBinaryMat(raster);
        }
        if (sampleModel instanceof ComponentSampleModel model && isTightlyPacked(raster, model)) {
            return fromPackedBuffer(raster, model, toBGR, forceShortType);
        }
        return fromSamples(raster, toBGR, forceShortType);
    }

    public static Rectangle getBounds(PlanarImage img) {
        return new Rectangle(0, 0, img.width(), img.height());
    }

    /**
     * Renders a RenderedImage into a new BufferedImage of the given type.
     */
    public static BufferedImage convertTo(RenderedImage src, int imageType) {
        var dst = new BufferedImage(src.getWidth(), src.getHeight(), imageType);
        var g2d = dst.createGraphics();
        try {
            g2d.drawRenderedImage(src, new AffineTransform());
        } finally {
            g2d.dispose();
        }
        return dst;
    }

    /**
     * True for 1 bit per pixel, single band sample models.
     */
    public static boolean isBinary(SampleModel sm) {
        return sm instanceof MultiPixelPackedSampleModel model && model.getPixelBitStride() == 1
                && sm.getNumBands() == 1;
    }

    /**
     * Returns the image itself when it is a BufferedImage, otherwise copies it into one.
     */
    public static BufferedImage convertRenderedImage(RenderedImage img) {
        if (img == null) {
            return null;
        }
        if (img instanceof BufferedImage bufferedImage) {
            return bufferedImage;
        }
        ColorModel cm = img.getColorModel();
        WritableRaster raster = cm.createCompatibleWritableRaster(img.getWidth(), img.getHeight());
        var result = new BufferedImage(cm, raster, cm.isAlphaPremultiplied(), createImageProperties(img));
        img.copyData(raster);
        return result;
    }

    /**
     * Unpacks 1-bit pixels into one byte per pixel (0 or 1).
     *
     * @throws IllegalArgumentException if the raster is not binary
     */
    public static byte[] getUnpackedBinaryData(Raster raster, Rectangle rect) {
        SampleModel sm = raster.getSampleModel();
        if (!isBinary(sm)) {
            throw new IllegalArgumentException("Not a binary raster!");
        }
        var mpp = (MultiPixelPackedSampleModel) sm;
        DataBuffer dataBuffer = raster.getDataBuffer();

        int dx = rect.x - raster.getSampleModelTranslateX();
        int dy = rect.y - raster.getSampleModelTranslateY();
        int lineStride = mpp.getScanlineStride();
        int eltOffset = dataBuffer.getOffset() + mpp.getOffset(dx, dy);
        int bitOffset = mpp.getBitOffset(dx);

        byte[] result = new byte[rect.width * rect.height];
        if (dataBuffer instanceof DataBufferByte buffer) {
            unpackBits(buffer.getData(), rect, eltOffset, bitOffset, lineStride, result);
        } else if (dataBuffer instanceof DataBufferShort buffer) {
            unpackBits(buffer.getData(), rect, eltOffset, bitOffset, lineStride, result);
        } else if (dataBuffer instanceof DataBufferUShort buffer) {
            unpackBits(buffer.getData(), rect, eltOffset, bitOffset, lineStride, result);
        } else if (dataBuffer instanceof DataBufferInt buffer) {
            unpackBits(buffer.getData(), rect, eltOffset, bitOffset, lineStride, result);
        }
        return result;
    }

    private static ColorModel createColorModel(int channels, int bpp, int dataType) {
        return switch (channels) {
            case 1 -> new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_GRAY), new int[] { bpp }, false,
                    true, Transparency.OPAQUE, dataType);
            case 3 -> new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[] { bpp, bpp, bpp },
                    false, false, Transparency.OPAQUE, dataType);
            default -> throw new UnsupportedOperationException("No implementation to handle " + channels + " channels");
        };
    }

    private static WritableRaster createRaster(ColorModel colorModel, int channels, int cols, int rows, int dataType) {
        if (channels == 1) {
            return colorModel.createCompatibleWritableRaster(cols, rows);
        }
        // Interleaved BGR layout matching the OpenCV memory order, for every data type
        var sampleModel = new PixelInterleavedSampleModel(dataType, cols, rows, channels, cols * channels, BGR_OFFSETS);
        return Raster.createWritableRaster(sampleModel, null);
    }

    private static void populateRasterFromMat(Mat matrix, WritableRaster raster) {
        DataBuffer buf = raster.getDataBuffer();
        if (buf instanceof DataBufferByte buffer) {
            matrix.get(0, 0, buffer.getData());
        } else if (buf instanceof DataBufferUShort buffer) {
            matrix.get(0, 0, buffer.getData());
        } else if (buf instanceof DataBufferShort buffer) {
            matrix.get(0, 0, buffer.getData());
        } else if (buf instanceof DataBufferInt buffer) {
            matrix.get(0, 0, buffer.getData());
        } else if (buf instanceof DataBufferFloat buffer) {
            matrix.get(0, 0, buffer.getData());
        } else if (buf instanceof DataBufferDouble buffer) {
            matrix.get(0, 0, buffer.getData());
        } else {
            throw new UnsupportedOperationException("Unsupported DataBuffer type: " + buf.getClass().getSimpleName());
        }
    }

    // A BufferedImage exposes its raster directly, other images copy their data
    private static Raster getRaster(RenderedImage img, Rectangle region) {
        if (region != null) {
            return img.getData(region);
        }
        return img instanceof BufferedImage bufferedImage ? bufferedImage.getRaster() : img.getData();
    }

    // True when the data buffer holds the raster pixels row after row, without padding or offset
    private static boolean isTightlyPacked(Raster raster, ComponentSampleModel model) {
        DataBuffer buffer = raster.getDataBuffer();
        int bands = model.getNumBands();
        int width = raster.getWidth();
        boolean originAtZero = raster.getMinX() == raster.getSampleModelTranslateX()
                && raster.getMinY() == raster.getSampleModelTranslateY()
                && Arrays.stream(buffer.getOffsets()).allMatch(offset -> offset == 0);
        if (!originAtZero) {
            return false;
        }
        if (model instanceof BandedSampleModel) {
            return bands == 3 && buffer instanceof DataBufferByte && buffer.getNumBanks() == 3
                    && model.getScanlineStride() == width && Arrays.equals(model.getBandOffsets(), BANDED_RGB_OFFSETS);
        }
        return buffer.getNumBanks() == 1 && model.getPixelStride() == bands
                && model.getScanlineStride() == width * bands;
    }

    private static ImageCV fromPackedBuffer(
            Raster raster,
            ComponentSampleModel model,
            boolean toBGR,
            boolean forceShortType) {
        int rows = raster.getHeight();
        int cols = raster.getWidth();
        int channels = model.getNumBands();
        DataBuffer buffer = raster.getDataBuffer();

        ImageCV mat;
        if (buffer instanceof DataBufferByte bufferByte) {
            if (model instanceof BandedSampleModel) {
                return createBandedRGBMat(bufferByte, rows, cols, toBGR);
            }
            mat = new ImageCV(rows, cols, CvType.CV_8UC(channels));
            mat.put(0, 0, bufferByte.getData());
        } else if (buffer instanceof DataBufferUShort bufferUShort) {
            int type = forceShortType ? CvType.CV_16SC(channels) : CvType.CV_16UC(channels);
            mat = new ImageCV(rows, cols, type);
            mat.put(0, 0, bufferUShort.getData());
        } else if (buffer instanceof DataBufferShort bufferShort) {
            mat = new ImageCV(rows, cols, CvType.CV_16SC(channels));
            mat.put(0, 0, bufferShort.getData());
        } else if (buffer instanceof DataBufferInt bufferInt) {
            mat = new ImageCV(rows, cols, CvType.CV_32SC(channels));
            mat.put(0, 0, bufferInt.getData());
        } else if (buffer instanceof DataBufferFloat bufferFloat) {
            mat = new ImageCV(rows, cols, CvType.CV_32FC(channels));
            mat.put(0, 0, bufferFloat.getData());
        } else if (buffer instanceof DataBufferDouble bufferDouble) {
            mat = new ImageCV(rows, cols, CvType.CV_64FC(channels));
            mat.put(0, 0, bufferDouble.getData());
        } else {
            return fromSamples(raster, toBGR, forceShortType);
        }
        return applyColorConversion(mat, model.getBandOffsets(), toBGR);
    }

    // Reads the samples in band order (R, G, B) through the Raster API: valid for any layout
    private static ImageCV fromSamples(Raster raster, boolean toBGR, boolean forceShortType) {
        int rows = raster.getHeight();
        int cols = raster.getWidth();
        int bands = raster.getNumBands();
        int x = raster.getMinX();
        int y = raster.getMinY();
        int transferType = raster.getTransferType();

        ImageCV mat;
        if (transferType == DataBuffer.TYPE_FLOAT) {
            mat = new ImageCV(rows, cols, CvType.CV_32FC(bands));
            mat.put(0, 0, raster.getPixels(x, y, cols, rows, (float[]) null));
        } else if (transferType == DataBuffer.TYPE_DOUBLE) {
            mat = new ImageCV(rows, cols, CvType.CV_64FC(bands));
            mat.put(0, 0, raster.getPixels(x, y, cols, rows, (double[]) null));
        } else {
            int[] pixels = raster.getPixels(x, y, cols, rows, (int[]) null);
            int depth = integerDepth(raster, forceShortType);
            if (depth == CvType.CV_16U || depth == CvType.CV_16S) {
                // Keeps the 16 bits as stored, like the packed path, instead of saturating on conversion
                mat = new ImageCV(rows, cols, CvType.makeType(depth, bands));
                mat.put(0, 0, toShorts(pixels));
            } else {
                try (var samples = new ImageCV(rows, cols, CvType.CV_32SC(bands))) {
                    samples.put(0, 0, pixels);
                    mat = new ImageCV();
                    samples.convertTo(mat, CvType.makeType(depth, bands));
                }
            }
        }
        return toBGR && bands == 3 ? swapRedBlue(mat) : mat;
    }

    private static short[] toShorts(int[] pixels) {
        var shorts = new short[pixels.length];
        for (int i = 0; i < pixels.length; i++) {
            shorts[i] = (short) pixels[i];
        }
        return shorts;
    }

    private static int integerDepth(Raster raster, boolean forceShortType) {
        int maxBits = Arrays.stream(raster.getSampleModel().getSampleSize()).max().orElse(8);
        if (maxBits <= 8) {
            return CvType.CV_8U;
        }
        if (maxBits <= 16) {
            boolean signed = forceShortType || raster.getTransferType() == DataBuffer.TYPE_SHORT;
            return signed ? CvType.CV_16S : CvType.CV_16U;
        }
        return CvType.CV_32S;
    }

    private static ImageCV createBinaryMat(Raster raster) {
        var mat = new ImageCV(raster.getHeight(), raster.getWidth(), CvType.CV_8UC1);
        mat.put(0, 0, getUnpackedBinaryData(raster, raster.getBounds()));
        return mat;
    }

    private static ImageCV createBandedRGBMat(DataBufferByte bufferByte, int rows, int cols, boolean toBGR) {
        var b = new Mat(rows, cols, CvType.CV_8UC1);
        var g = new Mat(rows, cols, CvType.CV_8UC1);
        var r = new Mat(rows, cols, CvType.CV_8UC1);
        try {
            b.put(0, 0, bufferByte.getData(2));
            g.put(0, 0, bufferByte.getData(1));
            r.put(0, 0, bufferByte.getData(0));
            var result = new ImageCV(rows, cols, CvType.CV_8UC3);
            Core.merge(toBGR ? List.of(b, g, r) : List.of(r, g, b), result);
            return result;
        } finally {
            b.release();
            g.release();
            r.release();
        }
    }

    private static ImageCV applyColorConversion(ImageCV mat, int[] offsets, boolean toBGR) {
        boolean swap = Arrays.equals(offsets, toBGR ? RGB_OFFSETS : BGR_OFFSETS);
        return swap ? swapRedBlue(mat) : mat;
    }

    // Single native pass for every depth; the source is released
    private static ImageCV swapRedBlue(ImageCV mat) {
        var swapped = new ImageCV(mat.size(), mat.type());
        var fromTo = new MatOfInt(0, 2, 1, 1, 2, 0);
        try {
            Core.mixChannels(List.of(mat), List.of(swapped), fromTo);
        } finally {
            fromTo.release();
            mat.release();
        }
        return swapped;
    }

    @SuppressWarnings("java:S1874") // BufferedImage constructor requires Hashtable
    private static Hashtable<String, Object> createImageProperties(RenderedImage img) {
        String[] keys = img.getPropertyNames();
        if (keys == null || keys.length == 0) {
            return null;
        }
        var properties = new Hashtable<String, Object>(keys.length);
        for (String key : keys) {
            properties.put(key, img.getProperty(key));
        }
        return properties;
    }

    private static void unpackBits(
            byte[] data,
            Rectangle rect,
            int eltOffset,
            int bitOffset,
            int lineStride,
            byte[] result) {
        int k = 0;
        for (int y = 0; y < rect.height; y++) {
            int bit = eltOffset * 8 + bitOffset;
            for (int x = 0; x < rect.width; x++, bit++) {
                result[k++] = (byte) ((data[bit >> 3] >> (7 - (bit & 7))) & 1);
            }
            eltOffset += lineStride;
        }
    }

    private static void unpackBits(
            short[] data,
            Rectangle rect,
            int eltOffset,
            int bitOffset,
            int lineStride,
            byte[] result) {
        int k = 0;
        for (int y = 0; y < rect.height; y++) {
            int bit = eltOffset * 16 + bitOffset;
            for (int x = 0; x < rect.width; x++, bit++) {
                result[k++] = (byte) ((data[bit >> 4] >> (15 - (bit & 15))) & 1);
            }
            eltOffset += lineStride;
        }
    }

    private static void unpackBits(
            int[] data,
            Rectangle rect,
            int eltOffset,
            int bitOffset,
            int lineStride,
            byte[] result) {
        int k = 0;
        for (int y = 0; y < rect.height; y++) {
            int bit = eltOffset * 32 + bitOffset;
            for (int x = 0; x < rect.width; x++, bit++) {
                result[k++] = (byte) ((data[bit >> 5] >> (31 - (bit & 31))) & 1);
            }
            eltOffset += lineStride;
        }
    }
}
