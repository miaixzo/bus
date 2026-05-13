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
package org.miaixz.bus.image.nimble;

import java.awt.color.ColorSpace;
import java.awt.image.*;

import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.data.Attributes;

/**
 * Represents the PaletteColorModel type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class PaletteColorModel extends ColorModel {

    /**
     * The opaque bits value.
     */
    private static final int[] opaqueBits = { 8, 8, 8 };

    /**
     * The lut value.
     */
    private final LUT lut;

    /**
     * Creates a new instance.
     *
     * @param bits     the bits.
     * @param dataType the data type.
     * @param cs       the cs.
     * @param ds       the ds.
     */
    public PaletteColorModel(int bits, int dataType, ColorSpace cs, Attributes ds) {
        super(bits, opaqueBits, cs, false, false, OPAQUE, dataType);
        int[] rDesc = lutDescriptor(ds, Tag.RedPaletteColorLookupTableDescriptor);
        int[] gDesc = lutDescriptor(ds, Tag.GreenPaletteColorLookupTableDescriptor);
        int[] bDesc = lutDescriptor(ds, Tag.BluePaletteColorLookupTableDescriptor);
        byte[] r = lutData(ds, rDesc, Tag.RedPaletteColorLookupTableData, Tag.SegmentedRedPaletteColorLookupTableData);
        byte[] g = lutData(
                ds,
                gDesc,
                Tag.GreenPaletteColorLookupTableData,
                Tag.SegmentedGreenPaletteColorLookupTableData);
        byte[] b = lutData(
                ds,
                bDesc,
                Tag.BluePaletteColorLookupTableData,
                Tag.SegmentedBluePaletteColorLookupTableData);
        lut = LUT.create(bits, r, g, b, rDesc[1], gDesc[1], bDesc[1]);
    }

    /**
     * Creates a new instance.
     *
     * @param src the src.
     * @param cs  the cs.
     */
    private PaletteColorModel(PaletteColorModel src, ColorSpace cs) {
        super(src.pixel_bits, opaqueBits, cs, false, false, src.getTransparency(), src.transferType);
        int[] rgb = new int[1 << src.pixel_bits];
        for (int i = 0; i < rgb.length; i++) {
            rgb[i] = convertTo(src.getRGB(i), src.getColorSpace(), cs);
        }
        lut = new LUT.Packed(src.pixel_bits, rgb);
    }

    /**
     * Executes the convert to operation.
     *
     * @param rgb the rgb.
     * @param src the src.
     * @param cs  the cs.
     * @return the operation result.
     */
    private static int convertTo(int rgb, ColorSpace src, ColorSpace cs) {
        float[] from = { scaleRGB(rgb >> 16), scaleRGB(rgb >> 8), scaleRGB(rgb), };
        float[] ciexyz = src.toCIEXYZ(from);
        float[] to = cs.fromCIEXYZ(ciexyz);
        return 0xff000000 | (unscaleRGB(to[0]) << 16) | (unscaleRGB(to[1]) << 8) | (unscaleRGB(to[2]));
    }

    /**
     * Executes the unscale rgb operation.
     *
     * @param value the value.
     * @return the operation result.
     */
    private static int unscaleRGB(float value) {
        return Math.min((int) (value * 256), 255);
    }

    /**
     * Executes the scale rgb operation.
     *
     * @param value the value.
     * @return the operation result.
     */
    private static float scaleRGB(int value) {
        return (value & 0xff) / 255f;
    }

    /**
     * Executes the lut data operation.
     *
     * @param ds      the ds.
     * @param desc    the desc.
     * @param dataTag the data tag.
     * @param segmTag the segm tag.
     * @return the operation result.
     */
    private static byte[] lutData(Attributes ds, int[] desc, int dataTag, int segmTag) {
        int len = desc[0] == 0 ? 0x10000 : desc[0];
        int bits = desc[2];
        byte[] data = ds.getSafeBytes(dataTag);
        if (data == null) {
            int[] segm = ds.getInts(segmTag);
            if (segm == null) {
                throw new IllegalArgumentException("Missing LUT Data!");
            }
            if (bits == 8) {
                throw new IllegalArgumentException("Segmented LUT Data with LUT Descriptor: bits=8");
            }
            data = new byte[len];
            new InflateSegmentedLut(segm, 0, data, 0).inflate(-1, 0);
        } else if (bits == 16 || data.length != len) {
            if (data.length != len << 1)
                throw new IllegalArgumentException("Number of actual LUT entries: " + data.length
                        + " mismatch specified value: " + len + " in LUT Descriptor");
            int hilo = ds.bigEndian() ? 0 : 1;
            if (bits == 8)
                hilo = 1 - hilo; // padded high bits -> use low bits
            data = LookupTableFactory.halfLength(data, hilo);
        }
        return data;
    }

    /**
     * Executes the convert to operation.
     *
     * @param cs the cs.
     * @return the operation result.
     */
    public PaletteColorModel convertTo(ColorSpace cs) {
        return new PaletteColorModel(this, cs);
    }

    /**
     * Executes the lut descriptor operation.
     *
     * @param ds      the ds.
     * @param descTag the desc tag.
     * @return the operation result.
     */
    private int[] lutDescriptor(Attributes ds, int descTag) {
        int[] desc = ds.getInts(descTag);
        if (desc == null) {
            throw new IllegalArgumentException("Missing LUT Descriptor!");
        }
        if (desc.length != 3) {
            throw new IllegalArgumentException("Illegal number of LUT Descriptor values: " + desc.length);
        }
        if (desc[0] < 0)
            throw new IllegalArgumentException("Illegal LUT Descriptor: len=" + desc[0]);
        int bits = desc[2];
        if (bits != 8 && bits != 16)
            throw new IllegalArgumentException("Illegal LUT Descriptor: bits=" + bits);
        return desc;
    }

    /**
     * Determines whether compatible raster.
     *
     * @param raster the raster.
     * @return true if the condition is met; otherwise false.
     */
    @Override
    public boolean isCompatibleRaster(Raster raster) {
        return isCompatibleSampleModel(raster.getSampleModel());
    }

    /**
     * Determines whether compatible sample model.
     *
     * @param sm the sm.
     * @return true if the condition is met; otherwise false.
     */
    @Override
    public boolean isCompatibleSampleModel(SampleModel sm) {
        return sm.getTransferType() == transferType && sm.getNumBands() == 1;
    }

    /**
     * Gets the red.
     *
     * @param pixel the pixel.
     * @return the red.
     */
    @Override
    public int getRed(int pixel) {
        return lut.getRed(pixel);
    }

    /**
     * Gets the green.
     *
     * @param pixel the pixel.
     * @return the green.
     */
    @Override
    public int getGreen(int pixel) {
        return lut.getGreen(pixel);
    }

    /**
     * Gets the blue.
     *
     * @param pixel the pixel.
     * @return the blue.
     */
    @Override
    public int getBlue(int pixel) {
        return lut.getBlue(pixel);
    }

    /**
     * Gets the alpha.
     *
     * @param pixel the pixel.
     * @return the alpha.
     */
    @Override
    public int getAlpha(int pixel) {
        return lut.getAlpha(pixel);
    }

    /**
     * Gets the rgb.
     *
     * @param pixel the pixel.
     * @return the rgb.
     */
    @Override
    public int getRGB(int pixel) {
        return lut.getRGB(pixel);
    }

    /**
     * Creates the compatible writable raster.
     *
     * @param w the w.
     * @param h the h.
     * @return the operation result.
     */
    @Override
    public WritableRaster createCompatibleWritableRaster(int w, int h) {
        return Raster.createInterleavedRaster(
                pixel_bits <= 8 ? DataBuffer.TYPE_BYTE : DataBuffer.TYPE_USHORT,
                w,
                h,
                1,
                null);
    }

    /**
     * Executes the convert to int discrete operation.
     *
     * @param raster the raster.
     * @return the operation result.
     */
    public BufferedImage convertToIntDiscrete(Raster raster) {
        if (!isCompatibleRaster(raster))
            throw new IllegalArgumentException("This raster is not compatible with this PaletteColorModel.");

        ColorModel cm = new DirectColorModel(getColorSpace(), 24, 0xff0000, 0x00ff00, 0x0000ff, 0, false,
                DataBuffer.TYPE_INT);

        int w = raster.getWidth();
        int h = raster.getHeight();
        WritableRaster discreteRaster = cm.createCompatibleWritableRaster(w, h);
        int[] discretData = ((DataBufferInt) discreteRaster.getDataBuffer()).getData();
        DataBuffer data = raster.getDataBuffer();
        if (data instanceof DataBufferByte) {
            byte[] pixels = ((DataBufferByte) data).getData();
            for (int i = 0; i < pixels.length; i++)
                discretData[i] = getRGB(pixels[i]);
        } else {
            short[] pixels = ((DataBufferUShort) data).getData();
            for (int i = 0; i < pixels.length; i++)
                discretData[i] = getRGB(pixels[i]);
        }
        return new BufferedImage(cm, discreteRaster, false, null);
    }

    /**
     * Represents the InflateSegmentedLut type.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    private static class InflateSegmentedLut {

        /**
         * The segm value.
         */
        final int[] segm;

        /**
         * The data value.
         */
        final byte[] data;

        /**
         * The read pos value.
         */
        int readPos;

        /**
         * The write pos value.
         */
        int writePos;

        /**
         * Creates a new instance.
         *
         * @param segm     the segm.
         * @param readPos  the read pos.
         * @param data     the data.
         * @param writePos the write pos.
         */
        private InflateSegmentedLut(int[] segm, int readPos, byte[] data, int writePos) {
            this.segm = segm;
            this.data = data;
            this.readPos = readPos;
            this.writePos = writePos;
        }

        /**
         * Executes the inflate operation.
         *
         * @param segs the segs.
         * @param y0   the y0.
         * @return the operation result.
         */
        private int inflate(int segs, int y0) {
            while (segs < 0 ? (readPos < segm.length) : segs-- > 0) {
                int segPos = readPos;
                int op = read();
                int n = read();
                switch (op) {
                    case 0:
                        y0 = discreteSegment(n);
                        break;

                    case 1:
                        if (writePos == 0)
                            throw new IllegalArgumentException("Linear segment cannot be the first segment");
                        y0 = linearSegment(n, y0, read());
                        break;

                    case 2:
                        if (segs >= 0)
                            throw new IllegalArgumentException("nested indirect segment at index " + segPos);
                        y0 = indirectSegment(n, y0);
                        break;

                    default:
                        throw new IllegalArgumentException("illegal op code " + op + " at index" + segPos);
                }
            }
            return y0;
        }

        /**
         * Executes the read operation.
         *
         * @return the operation result.
         */
        private int read() {
            if (readPos >= segm.length) {
                throw new IllegalArgumentException("Running out of data inflating segmented LUT");
            }
            return segm[readPos++] & 0xffff;
        }

        /**
         * Executes the write operation.
         *
         * @param y the y.
         */
        private void write(int y) {
            if (writePos >= data.length) {
                throw new IllegalArgumentException(
                        "Number of entries in inflated segmented LUT exceeds specified value: " + data.length
                                + " in LUT Descriptor");
            }
            data[writePos++] = (byte) (y >> 8);
        }

        /**
         * Executes the discrete segment operation.
         *
         * @param n the n.
         * @return the operation result.
         */
        private int discreteSegment(int n) {
            while (n-- > 0)
                write(read());
            return segm[readPos - 1] & 0xffff;
        }

        /**
         * Executes the linear segment operation.
         *
         * @param n  the n.
         * @param y0 the y0.
         * @param y1 the y1.
         * @return the operation result.
         */
        private int linearSegment(int n, int y0, int y1) {
            int dy = y1 - y0;
            for (int j = 1; j <= n; j++)
                write(y0 + dy * j / n);
            return y1;
        }

        /**
         * Executes the indirect segment operation.
         *
         * @param n  the n.
         * @param y0 the y0.
         * @return the operation result.
         */
        private int indirectSegment(int n, int y0) {
            int readPos = read() | (read() << 16);
            return new InflateSegmentedLut(segm, readPos, data, writePos).inflate(n, y0);
        }

    }

    /**
     * Represents the LUT type.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    private static abstract class LUT {

        /**
         * The mask value.
         */
        final int mask;

        /**
         * Creates a new instance.
         *
         * @param bits the bits.
         */
        LUT(int bits) {
            mask = (1 << bits) - 1;
        }

        /**
         * Executes the create operation.
         *
         * @param bits    the bits.
         * @param r       the r.
         * @param g       the g.
         * @param b       the b.
         * @param rOffset the r offset.
         * @param gOffset the g offset.
         * @param bOffset the b offset.
         * @return the operation result.
         */
        public static LUT create(int bits, byte[] r, byte[] g, byte[] b, int rOffset, int gOffset, int bOffset) {

            return r.length == g.length && g.length == b.length && rOffset == gOffset && gOffset == bOffset
                    ? new Packed(bits, r, g, b, rOffset)
                    : new PerColor(bits, r, g, b, rOffset, gOffset, bOffset);
        }

        /**
         * Executes the index operation.
         *
         * @param pixel  the pixel.
         * @param offset the offset.
         * @param length the length.
         * @return the operation result.
         */
        int index(int pixel, int offset, int length) {
            return Math.min(Math.max(0, (pixel & mask) - offset), length - 1);
        }

        /**
         * Gets the red.
         *
         * @param pixel the pixel.
         * @return the red.
         */
        abstract int getRed(int pixel);

        /**
         * Gets the green.
         *
         * @param pixel the pixel.
         * @return the green.
         */
        abstract int getGreen(int pixel);

        /**
         * Gets the blue.
         *
         * @param pixel the pixel.
         * @return the blue.
         */
        abstract int getBlue(int pixel);

        /**
         * Gets the alpha.
         *
         * @param pixel the pixel.
         * @return the alpha.
         */
        abstract int getAlpha(int pixel);

        /**
         * Gets the rgb.
         *
         * @param pixel the pixel.
         * @return the rgb.
         */
        abstract int getRGB(int pixel);

        /**
         * Represents the Packed type.
         *
         * @author Kimi Liu
         * @since Java 21+
         */
        static class Packed extends LUT {

            /**
             * The offset value.
             */
            final int offset;

            /**
             * The rgb value.
             */
            final int[] rgb;

            /**
             * Creates a new instance.
             *
             * @param bits   the bits.
             * @param r      the r.
             * @param g      the g.
             * @param b      the b.
             * @param offset the offset.
             */
            Packed(int bits, byte[] r, byte[] g, byte[] b, int offset) {
                super(bits);
                int length = r.length;
                this.offset = offset;
                rgb = new int[length];
                for (int i = 0; i < r.length; i++)
                    rgb[i] = 0xff000000 | ((r[i] & 0xff) << 16) | ((g[i] & 0xff) << 8) | (b[i] & 0xff);
            }

            /**
             * Creates a new instance.
             *
             * @param bits the bits.
             * @param rgb  the rgb.
             */
            Packed(int bits, int[] rgb) {
                super(bits);
                this.offset = 0;
                this.rgb = rgb;
            }

            /**
             * Gets the alpha.
             *
             * @param pixel the pixel.
             * @return the alpha.
             */
            @Override
            public int getAlpha(int pixel) {
                return (rgb[index(pixel, offset, rgb.length)] >> 24) & 0xff;
            }

            /**
             * Gets the red.
             *
             * @param pixel the pixel.
             * @return the red.
             */
            @Override
            public int getRed(int pixel) {
                return (rgb[index(pixel, offset, rgb.length)] >> 16) & 0xff;
            }

            /**
             * Gets the green.
             *
             * @param pixel the pixel.
             * @return the green.
             */
            @Override
            public int getGreen(int pixel) {
                return (rgb[index(pixel, offset, rgb.length)] >> 8) & 0xff;
            }

            /**
             * Gets the blue.
             *
             * @param pixel the pixel.
             * @return the blue.
             */
            @Override
            public int getBlue(int pixel) {
                return rgb[index(pixel, offset, rgb.length)] & 0xff;
            }

            /**
             * Gets the rgb.
             *
             * @param pixel the pixel.
             * @return the rgb.
             */
            @Override
            public int getRGB(int pixel) {
                return rgb[index(pixel, offset, rgb.length)];
            }

        }

        /**
         * Represents the PerColor type.
         *
         * @author Kimi Liu
         * @since Java 21+
         */
        static class PerColor extends LUT {

            /**
             * The r value.
             */
            final byte[] r;

            /**
             * The g value.
             */
            final byte[] g;

            /**
             * The b value.
             */
            final byte[] b;

            /**
             * The r offset value.
             */
            final int rOffset;

            /**
             * The g offset value.
             */
            final int gOffset;

            /**
             * The b offset value.
             */
            final int bOffset;

            /**
             * Creates a new instance.
             *
             * @param bits     the bits.
             * @param r        the r.
             * @param g        the g.
             * @param b        the b.
             * @param rOffset  the r offset.
             * @param gbOffset the gb offset.
             * @param bOffset  the b offset.
             */
            PerColor(int bits, byte[] r, byte[] g, byte[] b, int rOffset, int gbOffset, int bOffset) {
                super(bits);
                this.r = r;
                this.g = g;
                this.b = b;
                this.rOffset = rOffset;
                this.gOffset = gbOffset;
                this.bOffset = bOffset;
            }

            /**
             * Gets the alpha.
             *
             * @param pixel the pixel.
             * @return the alpha.
             */
            @Override
            public int getAlpha(int pixel) {
                return 0xff;
            }

            /**
             * Gets the red.
             *
             * @param pixel the pixel.
             * @return the red.
             */
            @Override
            public int getRed(int pixel) {
                return value(pixel, rOffset, r);
            }

            /**
             * Gets the green.
             *
             * @param pixel the pixel.
             * @return the green.
             */
            @Override
            public int getGreen(int pixel) {
                return value(pixel, gOffset, g);
            }

            /**
             * Gets the blue.
             *
             * @param pixel the pixel.
             * @return the blue.
             */
            @Override
            public int getBlue(int pixel) {
                return value(pixel, bOffset, b);
            }

            /**
             * Gets the rgb.
             *
             * @param pixel the pixel.
             * @return the rgb.
             */
            @Override
            public int getRGB(int pixel) {
                return 0xff000000 | (value(pixel, rOffset, r) << 16) | (value(pixel, gOffset, g) << 8)
                        | (value(pixel, bOffset, b));
            }

            /**
             * Executes the value operation.
             *
             * @param pixel  the pixel.
             * @param offset the offset.
             * @param lut    the lut.
             * @return the operation result.
             */
            int value(int pixel, int offset, byte[] lut) {
                return lut[index(pixel, offset, lut.length)] & 0xff;
            }

        }

    }

}
