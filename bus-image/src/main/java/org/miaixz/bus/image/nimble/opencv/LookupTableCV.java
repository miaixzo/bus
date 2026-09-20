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

import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.util.Arrays;
import java.util.Objects;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

/**
 * Defines a lookup table for fast pixel value transformations in image processing.
 * <p>
 * Supports byte and short data types with configurable offsets and signed/unsigned interpretation. Handles multi-band
 * images and adjusts lookup tables for mismatched bands.
 *
 * @author Kimi Liu
 * @see DataBufferByte
 * @see DataBufferUShort
 * @see DataBufferShort
 */
public class LookupTableCV {

    /**
     * The null data array message value.
     */
    public static final String NULL_DATA_ARRAY_MESSAGE = "Data array must not be null";

    /**
     * The offsets value.
     */
    private final int[] offsets;

    /**
     * The data value.
     */
    private final DataBuffer data;

    /**
     * The force reading unsigned value.
     */
    private final boolean forceReadingUnsigned;

    /**
     * Creates a new instance.
     *
     * @param data the data.
     */
    public LookupTableCV(byte[] data) {
        this(data, 0, false);
    }

    /**
     * Creates a new instance.
     *
     * @param data   the data.
     * @param offset the offset.
     */
    public LookupTableCV(byte[] data, int offset) {
        this(data, offset, false);
    }

    /**
     * Constructs a LookupTableCV with the provided byte data and an offset.
     *
     * @param data                 the byte data for the lookup table, must not be null
     * @param offset               the offset for the band, must be non-negative
     * @param forceReadingUnsigned if true, forces reading values as unsigned
     */
    public LookupTableCV(byte[] data, int offset, boolean forceReadingUnsigned) {
        Objects.requireNonNull(data, NULL_DATA_ARRAY_MESSAGE);
        if (data.length == 0) {
            throw new IllegalArgumentException("Data array must not be empty");
        }
        this.offsets = new int[] { offset };
        this.data = new DataBufferByte(data, data.length);
        this.forceReadingUnsigned = forceReadingUnsigned;
    }

    /**
     * Creates a new instance.
     *
     * @param data the data.
     */
    public LookupTableCV(byte[][] data) {
        this(data, new int[data.length], false);
    }

    /**
     * Creates a new instance.
     *
     * @param data   the data.
     * @param offset the offset.
     */
    public LookupTableCV(byte[][] data, int offset) {
        this(data, createOffsets(data.length, offset), false);
    }

    /**
     * Creates a new instance.
     *
     * @param data    the data.
     * @param offsets the offsets.
     */
    public LookupTableCV(byte[][] data, int[] offsets) {
        this(data, offsets, false);
    }

    /**
     * Constructs a LookupTableCV with the provided byte data and offsets.
     *
     * @param data                 the byte data for the lookup table, must not be null or empty
     * @param offsets              the offsets for each band, must not be null or empty
     * @param forceReadingUnsigned if true, forces reading values as unsigned
     */
    public LookupTableCV(byte[][] data, int[] offsets, boolean forceReadingUnsigned) {
        validateByteArrayInput(data, offsets);
        this.offsets = Arrays.copyOf(offsets, data.length);
        this.data = new DataBufferByte(data, data[0].length);
        this.forceReadingUnsigned = forceReadingUnsigned;
    }

    /**
     * Creates a new instance.
     *
     * @param data     the data.
     * @param offset   the offset.
     * @param isUShort the is u short.
     */
    public LookupTableCV(short[] data, int offset, boolean isUShort) {
        this(data, offset, isUShort, false);
    }

    /**
     * Constructs a LookupTableCV with the provided short data and an offset.
     *
     * @param data                 the short data for the lookup table, must not be null
     * @param offset               the offset for the band, must be non-negative
     * @param isUShort             if true, interprets the data as unsigned short
     * @param forceReadingUnsigned if true, forces reading values as unsigned. Bug in some libraries that do not handle
     *                             signed short correctly
     */
    public LookupTableCV(short[] data, int offset, boolean isUShort, boolean forceReadingUnsigned) {
        Objects.requireNonNull(data, NULL_DATA_ARRAY_MESSAGE);
        if (data.length == 0) {
            throw new IllegalArgumentException("Data array must not be empty");
        }
        this.offsets = new int[] { offset };
        this.data = isUShort ? new DataBufferUShort(data, data.length) : new DataBufferShort(data, data.length);
        this.forceReadingUnsigned = forceReadingUnsigned;
    }

    /**
     * Validates the byte array input.
     *
     * @param data    the data.
     * @param offsets the offsets.
     */
    private static void validateByteArrayInput(byte[][] data, int[] offsets) {
        Objects.requireNonNull(data, NULL_DATA_ARRAY_MESSAGE);
        Objects.requireNonNull(offsets, "Offsets array must not be null");

        if (data.length == 0 || data[0].length == 0) {
            throw new IllegalArgumentException("Data array must not be empty");
        }
        if (offsets.length != data.length) {
            throw new IllegalArgumentException("Offsets array must match the number of bands");
        }
    }

    /**
     * Creates the offsets.
     *
     * @param length the length.
     * @param offset the offset.
     * @return the operation result.
     */
    private static int[] createOffsets(int length, int offset) {
        int[] offsets = new int[length];
        Arrays.fill(offsets, offset);
        return offsets;
    }

    /**
     * Extracts source data from a Mat.
     *
     * @param src  the source Mat.
     * @param size the element count.
     * @return the extracted primitive array.
     */
    private static Object extractSourceData(Mat src, int size) {
        int depth = CvType.depth(src.type());
        if (depth == CvType.CV_8U || depth == CvType.CV_8S) {
            byte[] byteData = new byte[size];
            src.get(0, 0, byteData);
            return byteData;
        }
        if (depth == CvType.CV_16U || depth == CvType.CV_16S) {
            short[] shortData = new short[size];
            src.get(0, 0, shortData);
            return shortData;
        }
        throw new IllegalArgumentException(
                "Unsupported dataType for LUT transformation: " + CvType.typeToString(src.type()));
    }

    /**
     * Creates an image Mat from byte data.
     *
     * @param height the image height.
     * @param width  the image width.
     * @param type   the OpenCV type.
     * @param data   the image data.
     * @return the created image.
     */
    private static ImageCV toMat(int height, int width, int type, byte[] data) {
        var dst = new ImageCV(height, width, type);
        dst.put(0, 0, data);
        return dst;
    }

    /**
     * Creates an image Mat from short data.
     *
     * @param height the image height.
     * @param width  the image width.
     * @param type   the OpenCV type.
     * @param data   the image data.
     * @return the created image.
     */
    private static ImageCV toMat(int height, int width, int type, short[] data) {
        var dst = new ImageCV(height, width, type);
        dst.put(0, 0, data);
        return dst;
    }

    /**
     * Expands one byte band to the requested number of channels.
     *
     * @param bandData the band data.
     * @param channels the channel count.
     * @return the expanded bands.
     */
    private static byte[][] expandToChannels(byte[] bandData, int channels) {
        byte[][] expanded = new byte[channels][];
        Arrays.fill(expanded, bandData);
        return expanded;
    }

    /**
     * Expands one short band to the requested number of channels.
     *
     * @param bandData the band data.
     * @param channels the channel count.
     * @return the expanded bands.
     */
    private static short[][] expandToChannels(short[] bandData, int channels) {
        short[][] expanded = new short[channels][];
        Arrays.fill(expanded, bandData);
        return expanded;
    }

    /**
     * Clamps a pixel value to a table index.
     *
     * @param pixel    the pixel value.
     * @param offset   the table offset.
     * @param maxIndex the maximum table index.
     * @return the clamped index.
     */
    private static int clampIndex(int pixel, int offset, int maxIndex) {
        return Math.max(0, Math.min(pixel - offset, maxIndex));
    }

    /**
     * Resolves the number of channels to read from the source.
     *
     * @param srcLength the source array length.
     * @param dstLength the destination array length.
     * @param numBands  the destination band count.
     * @return the source channel count.
     */
    private static int sourceChannels(int srcLength, int dstLength, int numBands) {
        return srcLength < dstLength ? 1 : numBands;
    }

    /**
     * Applies a byte table to byte source data.
     *
     * @param src the source data.
     * @param dst the destination data.
     * @param ctx the LUT context.
     */
    private static void lookupByteToByte(byte[] src, byte[] dst, LutContext ctx) {
        int numBands = ctx.numBands();
        int srcChannels = sourceChannels(src.length, dst.length, numBands);
        for (int b = 0; b < numBands; b++) {
            byte[] table = ctx.byteData()[b];
            int offset = ctx.offsets()[b];
            int maxIndex = table.length - 1;
            for (int s = srcChannels == 1 ? 0 : b, d = b; d < dst.length; s += srcChannels, d += numBands) {
                dst[d] = table[clampIndex(src[s] & 0xFF, offset, maxIndex)];
            }
        }
    }

    /**
     * Applies a byte table to short source data.
     *
     * @param src  the source data.
     * @param dst  the destination data.
     * @param ctx  the LUT context.
     * @param mask the source mask.
     */
    private static void lookupShortToByte(short[] src, byte[] dst, LutContext ctx, int mask) {
        int numBands = ctx.numBands();
        int srcChannels = sourceChannels(src.length, dst.length, numBands);
        for (int b = 0; b < numBands; b++) {
            byte[] table = ctx.byteData()[b];
            int offset = ctx.offsets()[b];
            int maxIndex = table.length - 1;
            for (int s = srcChannels == 1 ? 0 : b, d = b; d < dst.length; s += srcChannels, d += numBands) {
                dst[d] = table[clampIndex(src[s] & mask, offset, maxIndex)];
            }
        }
    }

    /**
     * Applies a short table to byte source data.
     *
     * @param src the source data.
     * @param dst the destination data.
     * @param ctx the LUT context.
     */
    private static void lookupByteToShort(byte[] src, short[] dst, LutContext ctx) {
        int numBands = ctx.numBands();
        int srcChannels = sourceChannels(src.length, dst.length, numBands);
        for (int b = 0; b < numBands; b++) {
            short[] table = ctx.shortData()[b];
            int offset = ctx.offsets()[b];
            int maxIndex = table.length - 1;
            for (int s = srcChannels == 1 ? 0 : b, d = b; d < dst.length; s += srcChannels, d += numBands) {
                dst[d] = table[clampIndex(src[s] & 0xFF, offset, maxIndex)];
            }
        }
    }

    /**
     * Applies a short table to short source data.
     *
     * @param src  the source data.
     * @param dst  the destination data.
     * @param ctx  the LUT context.
     * @param mask the source mask.
     */
    private static void lookupShortToShort(short[] src, short[] dst, LutContext ctx, int mask) {
        int numBands = ctx.numBands();
        int srcChannels = sourceChannels(src.length, dst.length, numBands);
        for (int b = 0; b < numBands; b++) {
            short[] table = ctx.shortData()[b];
            int offset = ctx.offsets()[b];
            int maxIndex = table.length - 1;
            for (int s = srcChannels == 1 ? 0 : b, d = b; d < dst.length; s += srcChannels, d += numBands) {
                dst[d] = table[clampIndex(src[s] & mask, offset, maxIndex)];
            }
        }
    }

    /**
     * Gets the data.
     *
     * @return the data.
     */
    public DataBuffer getData() {
        return data;
    }

    /**
     * Gets the byte data.
     *
     * @return the byte data.
     */
    public byte[][] getByteData() {
        if (data instanceof DataBufferByte buffer) {
            return buffer.getBankData();
        }
        return null;
    }

    /**
     * Gets the byte data.
     *
     * @param band the band.
     * @return the byte data.
     */
    public byte[] getByteData(int band) {
        if (data instanceof DataBufferByte buffer) {
            return buffer.getData(band);
        }
        return null;
    }

    /**
     * Gets the short data.
     *
     * @return the short data.
     */
    public short[][] getShortData() {
        if (data instanceof DataBufferUShort bufferUShort) {
            return bufferUShort.getBankData();
        } else if (data instanceof DataBufferShort bufferShort) {
            return bufferShort.getBankData();
        }
        return null;
    }

    /**
     * Gets the short data.
     *
     * @param band the band.
     * @return the short data.
     */
    public short[] getShortData(int band) {
        if (data instanceof DataBufferUShort bufferUShort) {
            return bufferUShort.getData(band);
        } else if (data instanceof DataBufferShort bufferShort) {
            return bufferShort.getData(band);
        }
        return null;
    }

    /**
     * Gets the offsets.
     *
     * @return the offsets.
     */
    public int[] getOffsets() {
        return Arrays.copyOf(offsets, offsets.length);
    }

    /**
     * Gets the offset.
     *
     * @return the offset.
     */
    public int getOffset() {
        return offsets[0];
    }

    /**
     * Returns the index offset of entry 0 for a specific band.
     *
     * @param band the band value
     * @return the result
     */
    public int getOffset(int band) {
        return offsets[band];
    }

    /**
     * Gets the num bands.
     *
     * @return the num bands.
     */
    public int getNumBands() {
        return data.getNumBanks();
    }

    /**
     * Gets the num entries.
     *
     * @return the num entries.
     */
    public int getNumEntries() {
        return data.getSize();
    }

    /**
     * Gets the data type.
     *
     * @return the data type.
     */
    public int getDataType() {
        return data.getDataType();
    }

    /**
     * Executes the lookup operation.
     *
     * @param band  the band.
     * @param value the value.
     * @return the operation result.
     */
    public int lookup(int band, int value) {
        return data.getElem(band, value - offsets[band]);
    }

    /**
     * Applies this lookup table to the source image. The table is applied band by band; a single-band table is applied
     * to every channel, and a multi-band table applied to a single-channel image produces one channel per band.
     *
     * @param src source image matrix with 8-bit or 16-bit depth.
     * @return the transformed image.
     * @throws IllegalArgumentException if the table and the image both have several bands, in different numbers.
     */
    public ImageCV lookup(Mat src) {
        Objects.requireNonNull(src, "Source Mat cannot be null.");

        int width = src.width();
        int height = src.height();
        int channels = src.channels();
        int srcDataType = ImageConversion.convertToDataType(src.type());
        int mask = srcDataType == DataBuffer.TYPE_SHORT && !forceReadingUnsigned ? 0xFFFFFFFF : 0xFFFF;

        Object sourceData = extractSourceData(src, width * height * channels);
        LutContext context = prepareLutContext(channels);
        int pixels = width * height;

        if (context.byteData() != null) {
            byte[] dstData = sourceData instanceof byte[] bytes && channels == context.numBands() ? bytes
                    : new byte[pixels * context.numBands()];
            if (sourceData instanceof byte[] bytes) {
                lookupByteToByte(bytes, dstData, context);
            } else {
                lookupShortToByte((short[]) sourceData, dstData, context, mask);
            }
            return toMat(height, width, CvType.CV_8UC(context.numBands()), dstData);
        }

        short[] dstData = sourceData instanceof short[] shorts && channels == context.numBands() ? shorts
                : new short[pixels * context.numBands()];
        if (sourceData instanceof byte[] bytes) {
            lookupByteToShort(bytes, dstData, context);
        } else {
            lookupShortToShort((short[]) sourceData, dstData, context, mask);
        }
        int depth = getDataType() == DataBuffer.TYPE_USHORT ? CvType.CV_16U : CvType.CV_16S;
        return toMat(height, width, CvType.makeType(depth, context.numBands()), dstData);
    }

    /**
     * Prepares the LUT context for the source channel count.
     *
     * @param channels the source channel count.
     * @return the LUT context.
     */
    private LutContext prepareLutContext(int channels) {
        int numBands = getNumBands();
        if (numBands != channels && numBands != 1 && channels != 1) {
            throw new IllegalArgumentException(
                    "A " + numBands + "-band table cannot be applied to a " + channels + "-channel image");
        }
        int[] tblOffsets = getOffsets();
        byte[][] bTblData = getByteData();
        short[][] sTblData = getShortData();

        if (numBands < channels) {
            if (bTblData != null) {
                bTblData = expandToChannels(bTblData[0], channels);
            } else if (sTblData != null) {
                sTblData = expandToChannels(sTblData[0], channels);
            }
            tblOffsets = createOffsets(channels, tblOffsets[0]);
            numBands = channels;
        }
        return new LutContext(numBands, tblOffsets, bTblData, sTblData);
    }

    /**
     * Stores LUT parameters used during one lookup operation.
     *
     * @param numBands  the number of destination bands.
     * @param offsets   the table offsets.
     * @param byteData  the byte table data.
     * @param shortData the short table data.
     * @author Kimi Liu
     */
    private record LutContext(int numBands, int[] offsets, byte[][] byteData, short[][] shortData) {

    }

}
