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
package org.miaixz.bus.image.nimble;

import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.data.Attributes;

import java.awt.color.ColorSpace;
import java.awt.image.*;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PaletteColorModel extends ColorModel {

    private static final int[] opaqueBits = {8, 8, 8};

    private final LUT lut;

    public PaletteColorModel(int bits, int dataType, ColorSpace cs,
                             Attributes ds) {
        super(bits, opaqueBits, cs, false, false, OPAQUE, dataType);
        int[] rDesc = lutDescriptor(ds,
                Tag.RedPaletteColorLookupTableDescriptor);
        int[] gDesc = lutDescriptor(ds,
                Tag.GreenPaletteColorLookupTableDescriptor);
        int[] bDesc = lutDescriptor(ds,
                Tag.BluePaletteColorLookupTableDescriptor);
        byte[] r = lutData(ds, rDesc,
                Tag.RedPaletteColorLookupTableData,
                Tag.SegmentedRedPaletteColorLookupTableData);
        byte[] g = lutData(ds, gDesc,
                Tag.GreenPaletteColorLookupTableData,
                Tag.SegmentedGreenPaletteColorLookupTableData);
        byte[] b = lutData(ds, bDesc,
                Tag.BluePaletteColorLookupTableData,
                Tag.SegmentedBluePaletteColorLookupTableData);
        lut = LUT.create(bits, r, g, b, rDesc[1], gDesc[1], bDesc[1]);
    }

    private PaletteColorModel(PaletteColorModel src, ColorSpace cs) {
        super(src.pixel_bits, opaqueBits, cs, false, false,
                src.getTransparency(), src.transferType);
        int[] rgb = new int[1 << src.pixel_bits];
        for (int i = 0; i < rgb.length; i++) {
            rgb[i] = convertTo(src.getRGB(i), src.getColorSpace(), cs);
        }
        lut = new LUT.Packed(src.pixel_bits, rgb);
    }

    private static int convertTo(int rgb, ColorSpace src, ColorSpace cs) {
        float[] from = {
                scaleRGB(rgb >> 16),
                scaleRGB(rgb >> 8),
                scaleRGB(rgb),
        };
        float[] ciexyz = src.toCIEXYZ(from);
        float[] to = cs.fromCIEXYZ(ciexyz);
        return 0xff000000
                | (unscaleRGB(to[0]) << 16)
                | (unscaleRGB(to[1]) << 8)
                | (unscaleRGB(to[2]));
    }

    private static int unscaleRGB(float value) {
        return Math.min((int) (value * 256), 255);
    }

    private static float scaleRGB(int value) {
        return (value & 0xff) / 255f;
    }

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
                throw new IllegalArgumentException(
                        "Segmented LUT Data with LUT Descriptor: bits=8");
            }
            data = new byte[len];
            new InflateSegmentedLut(segm, 0, data, 0).inflate(-1, 0);
        } else if (bits == 16 || data.length != len) {
            if (data.length != len << 1)
                throw new IllegalArgumentException("Number of actual LUT entries: "
                        + data.length + " mismatch specified value: "
                        + len + " in LUT Descriptor");
            int hilo = ds.bigEndian() ? 0 : 1;
            if (bits == 8)
                hilo = 1 - hilo; // padded high bits -> use low bits
            data = LookupTableFactory.halfLength(data, hilo);
        }
        return data;
    }

    public PaletteColorModel convertTo(ColorSpace cs) {
        return new PaletteColorModel(this, cs);
    }

    private int[] lutDescriptor(Attributes ds, int descTag) {
        int[] desc = ds.getInts(descTag);
        if (desc == null) {
            throw new IllegalArgumentException("Missing LUT Descriptor!");
        }
        if (desc.length != 3) {
            throw new IllegalArgumentException(
                    "Illegal number of LUT Descriptor values: " + desc.length);
        }
        if (desc[0] < 0)
            throw new IllegalArgumentException(
                    "Illegal LUT Descriptor: len=" + desc[0]);
        int bits = desc[2];
        if (bits != 8 && bits != 16)
            throw new IllegalArgumentException(
                    "Illegal LUT Descriptor: bits=" + bits);
        return desc;
    }

    @Override
    public boolean isCompatibleRaster(Raster raster) {
        return isCompatibleSampleModel(raster.getSampleModel());
    }

    @Override
    public boolean isCompatibleSampleModel(SampleModel sm) {
        return sm.getTransferType() == transferType
                && sm.getNumBands() == 1;
    }

    @Override
    public int getRed(int pixel) {
        return lut.getRed(pixel);
    }

    @Override
    public int getGreen(int pixel) {
        return lut.getGreen(pixel);
    }

    @Override
    public int getBlue(int pixel) {
        return lut.getBlue(pixel);
    }

    @Override
    public int getAlpha(int pixel) {
        return lut.getAlpha(pixel);
    }

    @Override
    public int getRGB(int pixel) {
        return lut.getRGB(pixel);
    }

    @Override
    public WritableRaster createCompatibleWritableRaster(int w, int h) {
        return Raster.createInterleavedRaster(
                pixel_bits <= 8
                        ? DataBuffer.TYPE_BYTE
                        : DataBuffer.TYPE_USHORT,
                w, h, 1, null);
    }

    public BufferedImage convertToIntDiscrete(Raster raster) {
        if (!isCompatibleRaster(raster))
            throw new IllegalArgumentException(
                    "This raster is not compatible with this PaletteColorModel.");

        ColorModel cm = new DirectColorModel(getColorSpace(), 24,
                0xff0000, 0x00ff00, 0x0000ff, 0, false, DataBuffer.TYPE_INT);

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

    private static class InflateSegmentedLut {
        final int[] segm;
        final byte[] data;
        int readPos;
        int writePos;

        private InflateSegmentedLut(int[] segm, int readPos, byte[] data, int writePos) {
            this.segm = segm;
            this.data = data;
            this.readPos = readPos;
            this.writePos = writePos;
        }

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
                            throw new IllegalArgumentException(
                                    "Linear segment cannot be the first segment");
                        y0 = linearSegment(n, y0, read());
                        break;
                    case 2:
                        if (segs >= 0)
                            throw new IllegalArgumentException(
                                    "nested indirect segment at index " + segPos);
                        y0 = indirectSegment(n, y0);
                        break;
                    default:
                        throw new IllegalArgumentException(
                                "illegal op code " + op + " at index" + segPos);
                }
            }
            return y0;
        }

        private int read() {
            if (readPos >= segm.length) {
                throw new IllegalArgumentException(
                        "Running out of data inflating segmented LUT");
            }
            return segm[readPos++] & 0xffff;
        }

        private void write(int y) {
            if (writePos >= data.length) {
                throw new IllegalArgumentException(
                        "Number of entries in inflated segmented LUT exceeds specified value: "
                                + data.length + " in LUT Descriptor");
            }
            data[writePos++] = (byte) (y >> 8);
        }

        private int discreteSegment(int n) {
            while (n-- > 0) write(read());
            return segm[readPos - 1] & 0xffff;
        }

        private int linearSegment(int n, int y0, int y1) {
            int dy = y1 - y0;
            for (int j = 1; j <= n; j++)
                write(y0 + dy * j / n);
            return y1;
        }

        private int indirectSegment(int n, int y0) {
            int readPos = read() | (read() << 16);
            return new InflateSegmentedLut(segm, readPos, data, writePos).inflate(n, y0);
        }
    }

    private static abstract class LUT {

        final int mask;

        LUT(int bits) {
            mask = (1 << bits) - 1;
        }

        public static LUT create(int bits, byte[] r, byte[] g, byte[] b,
                                 int rOffset, int gOffset, int bOffset) {

            return r.length == g.length && g.length == b.length
                    && rOffset == gOffset && gOffset == bOffset
                    ? new Packed(bits, r, g, b, rOffset)
                    : new PerColor(bits, r, g, b, rOffset, gOffset, bOffset);
        }

        int index(int pixel, int offset, int length) {
            return Math.min(Math.max(0, (pixel & mask) - offset), length - 1);
        }

        abstract int getRed(int pixel);

        abstract int getGreen(int pixel);

        abstract int getBlue(int pixel);

        abstract int getAlpha(int pixel);

        abstract int getRGB(int pixel);

        static class Packed extends LUT {

            final int offset;
            final int[] rgb;

            Packed(int bits, byte[] r, byte[] g, byte[] b, int offset) {
                super(bits);
                int length = r.length;
                this.offset = offset;
                rgb = new int[length];
                for (int i = 0; i < r.length; i++)
                    rgb[i] = 0xff000000
                            | ((r[i] & 0xff) << 16)
                            | ((g[i] & 0xff) << 8)
                            | (b[i] & 0xff);
            }

            Packed(int bits, int[] rgb) {
                super(bits);
                this.offset = 0;
                this.rgb = rgb;
            }

            @Override
            public int getAlpha(int pixel) {
                return (rgb[index(pixel, offset, rgb.length)] >> 24) & 0xff;
            }

            @Override
            public int getRed(int pixel) {
                return (rgb[index(pixel, offset, rgb.length)] >> 16) & 0xff;
            }

            @Override
            public int getGreen(int pixel) {
                return (rgb[index(pixel, offset, rgb.length)] >> 8) & 0xff;
            }

            @Override
            public int getBlue(int pixel) {
                return rgb[index(pixel, offset, rgb.length)] & 0xff;
            }

            @Override
            public int getRGB(int pixel) {
                return rgb[index(pixel, offset, rgb.length)];
            }
        }

        static class PerColor extends LUT {

            final byte[] r;
            final byte[] g;
            final byte[] b;
            final int rOffset;
            final int gOffset;
            final int bOffset;

            PerColor(int bits, byte[] r, byte[] g, byte[] b, int rOffset,
                     int gbOffset, int bOffset) {
                super(bits);
                this.r = r;
                this.g = g;
                this.b = b;
                this.rOffset = rOffset;
                this.gOffset = gbOffset;
                this.bOffset = bOffset;
            }

            @Override
            public int getAlpha(int pixel) {
                return 0xff;
            }

            @Override
            public int getRed(int pixel) {
                return value(pixel, rOffset, r);
            }

            @Override
            public int getGreen(int pixel) {
                return value(pixel, gOffset, g);
            }

            @Override
            public int getBlue(int pixel) {
                return value(pixel, bOffset, b);
            }

            @Override
            public int getRGB(int pixel) {
                return 0xff000000
                        | (value(pixel, rOffset, r) << 16)
                        | (value(pixel, gOffset, g) << 8)
                        | (value(pixel, bOffset, b));
            }

            int value(int pixel, int offset, byte[] lut) {
                return lut[index(pixel, offset, lut.length)] & 0xff;
            }
        }

    }

}
