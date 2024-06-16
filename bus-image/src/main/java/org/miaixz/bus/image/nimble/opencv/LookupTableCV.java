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

import org.miaixz.bus.core.xyz.ObjectKit;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class LookupTableCV {

    private final int[] offsets;
    private final DataBuffer data;
    private final boolean forceReadingUnsigned;

    public LookupTableCV(byte[] data) {
        this(data, 0);
    }

    public LookupTableCV(byte[] data, int offset) {
        this(data, offset, false);
    }

    public LookupTableCV(byte[] data, int offset, boolean forceReadingUnsigned) {
        this.offsets = new int[1];
        Arrays.fill(offsets, offset);
        this.data = new DataBufferByte(Objects.requireNonNull(data), data.length);
        this.forceReadingUnsigned = forceReadingUnsigned;
    }

    public LookupTableCV(byte[][] data) {
        this(data, new int[data.length]);
    }

    public LookupTableCV(byte[][] data, int offset) {
        this(data, new int[data.length]);
        Arrays.fill(offsets, offset);
    }

    public LookupTableCV(byte[][] data, int[] offsets) {
        this(data, offsets, false);
    }

    public LookupTableCV(byte[][] data, int[] offsets, boolean forceReadingUnsigned) {
        this.offsets = Arrays.copyOf(offsets, data.length);
        this.data = new DataBufferByte(Objects.requireNonNull(data), data[0].length);
        this.forceReadingUnsigned = forceReadingUnsigned;
    }

    public LookupTableCV(short[] data, int offset, boolean isUShort) {
        this(data, offset, isUShort, false);
    }

    public LookupTableCV(short[] data, int offset, boolean isUShort, boolean forceReadingUnsigned) {
        this.offsets = new int[1];
        Arrays.fill(offsets, offset);
        if (isUShort) {
            this.data = new DataBufferUShort(Objects.requireNonNull(data), data.length);
        } else {
            this.data = new DataBufferShort(Objects.requireNonNull(data), data.length);
        }
        this.forceReadingUnsigned = forceReadingUnsigned;
    }

    private static int index(int pixel, int offset, int length) {
        int val = pixel - offset;
        if (val < 0) {
            val = 0;
        } else if (val > length) {
            val = length;
        }
        return val;
    }

    public DataBuffer getData() {
        return data;
    }

    public byte[][] getByteData() {
        return data instanceof DataBufferByte ? ((DataBufferByte) data).getBankData() : null;
    }

    public byte[] getByteData(int band) {
        return data instanceof DataBufferByte ? ((DataBufferByte) data).getData(band) : null;
    }

    public short[][] getShortData() {
        if (data instanceof DataBufferUShort) {
            return ((DataBufferUShort) data).getBankData();
        } else if (data instanceof DataBufferShort) {
            return ((DataBufferShort) data).getBankData();
        } else {
            return null;
        }
    }

    public short[] getShortData(int band) {
        if (data instanceof DataBufferUShort) {
            return ((DataBufferUShort) data).getData(band);
        } else if (data instanceof DataBufferShort) {
            return ((DataBufferShort) data).getData(band);
        } else {
            return null;
        }
    }

    public int[] getOffsets() {
        return offsets;
    }

    public int getOffset() {
        return offsets[0];
    }

    public int getNumBands() {
        return data.getNumBanks();
    }

    public int getNumEntries() {
        return data.getSize();
    }

    public int getDataType() {
        return data.getDataType();
    }

    public int lookup(int band, int value) {
        return data.getElem(band, value - offsets[band]);
    }

    public int getOffset(int band) {
        return offsets[band];
    }

    public ImageCV lookup(Mat src) {
        // Validate source.
        Objects.requireNonNull(src);

        int width = src.width();
        int height = src.height();
        int cvType = src.type();
        int channels = CvType.channels(cvType);
        int srcDataType = ImageConversion.convertToDataType(cvType);

        byte[] bSrcData = null;
        short[] sSrcData = null;
        if (CvType.depth(cvType) == CvType.CV_8U || CvType.depth(cvType) == CvType.CV_8S) {
            bSrcData = new byte[width * height * channels];
            src.get(0, 0, bSrcData);
        } else if (CvType.depth(cvType) == CvType.CV_16U || CvType.depth(cvType) == CvType.CV_16S) {
            sSrcData = new short[width * height * channels];
            src.get(0, 0, sSrcData);
        } else {
            throw new IllegalArgumentException(
                    "Not suported dataType for LUT transformation:" + src.toString());
        }

        int lkbBands = getNumBands();
        int lkpDataType = getDataType();

        // Table information.
        int[] tblOffsets = getOffsets();

        byte[][] bTblData = getByteData();
        short[][] sTblData = getShortData();

        if (lkbBands < channels) {
            if (null == sTblData) {
                byte[] b = bTblData[0];
                bTblData = new byte[channels][];
                Arrays.fill(bTblData, b);
            } else {
                short[] b = sTblData[0];
                sTblData = new short[channels][];
                Arrays.fill(sTblData, b);
            }

            int t = tblOffsets[0];
            tblOffsets = new int[channels];
            Arrays.fill(tblOffsets, t);
            lkbBands = channels;
        }

        if (lkpDataType == DataBuffer.TYPE_BYTE) {
            boolean scrByte = srcDataType == DataBuffer.TYPE_BYTE;
            byte[] bDstData =
                    scrByte && channels >= lkbBands ? bSrcData : new byte[width * height * lkbBands];
            if (scrByte && null != bSrcData) {
                lookup(bSrcData, bDstData, tblOffsets, bTblData);
            } else if (srcDataType == DataBuffer.TYPE_USHORT && null != sSrcData && null != bDstData) {
                lookupU(sSrcData, bDstData, tblOffsets, bTblData);
            } else if (srcDataType == DataBuffer.TYPE_SHORT && null != sSrcData && null != bDstData) {
                lookup(sSrcData, bDstData, tblOffsets, bTblData);
            } else {
                throw new IllegalArgumentException(
                        "Not supported LUT conversion from source dataType " + srcDataType);
            }

            ImageCV dst = new ImageCV(height, width, CvType.CV_8UC(lkbBands));
            dst.put(0, 0, bDstData);
            return dst;

        } else if (lkpDataType == DataBuffer.TYPE_USHORT || lkpDataType == DataBuffer.TYPE_SHORT) {
            boolean scrByte = srcDataType == DataBuffer.TYPE_BYTE;
            short[] sDstData =
                    !scrByte && channels >= lkbBands ? sSrcData : new short[width * height * lkbBands];
            if (scrByte && ObjectKit.isNotNull(bSrcData) && ObjectKit.isNotNull(sTblData)) {
                lookup(bSrcData, sDstData, tblOffsets, sTblData);
            } else if (srcDataType == DataBuffer.TYPE_USHORT && ObjectKit.isNotNull(sSrcData) && ObjectKit.isNotNull(sTblData)) {
                lookupU(sSrcData, sDstData, tblOffsets, sTblData);
            } else if (srcDataType == DataBuffer.TYPE_SHORT && ObjectKit.isNotNull(sSrcData) && ObjectKit.isNotNull(sTblData)) {
                lookup(sSrcData, sDstData, tblOffsets, sTblData);
            } else {
                throw new IllegalArgumentException(
                        "Not supported LUT conversion from source dataType " + srcDataType);
            }

            ImageCV dst =
                    new ImageCV(
                            height,
                            width,
                            lkpDataType == DataBuffer.TYPE_USHORT
                                    ? CvType.CV_16UC(channels)
                                    : CvType.CV_16SC(channels));
            dst.put(0, 0, sDstData);
            return dst;
        }

        return null;
    }

    // byte to byte
    private void lookup(byte[] srcData, byte[] dstData, int[] tblOffsets, byte[][] tblData) {
        int bOffset = tblData.length;

        if (srcData.length < dstData.length) {
            for (int i = 0; i < srcData.length; i++) {
                int val = (srcData[i] & 0xFF);
                for (int b = 0; b < bOffset; b++) {
                    dstData[i * bOffset + b] = tblData[b][index(val, tblOffsets[b], tblData[b].length - 1)];
                }
            }
        } else {
            for (int b = 0; b < bOffset; b++) {
                byte[] t = tblData[b];
                int tblOffset = tblOffsets[b];
                int maxLength = t.length - 1;

                for (int i = b; i < srcData.length; i += bOffset) {
                    dstData[i] = t[index((srcData[i] & 0xFF), tblOffset, maxLength)];
                }
            }
        }
    }

    // ushort to byte
    private void lookupU(short[] srcData, byte[] dstData, int[] tblOffsets, byte[][] tblData) {
        int bOffset = tblData.length;

        if (srcData.length < dstData.length) {
            for (int i = 0; i < srcData.length; i++) {
                int val = (srcData[i] & 0xFFFF);
                for (int b = 0; b < bOffset; b++) {
                    dstData[i * bOffset + b] = tblData[b][index(val, tblOffsets[b], tblData[b].length - 1)];
                }
            }
        } else {
            for (int b = 0; b < bOffset; b++) {
                byte[] t = tblData[b];
                int tblOffset = tblOffsets[b];
                int maxLength = t.length - 1;

                for (int i = b; i < srcData.length; i += bOffset) {
                    dstData[i] = t[index((srcData[i] & 0xFFFF), tblOffset, maxLength)];
                }
            }
        }
    }

    // short to byte
    private void lookup(short[] srcData, byte[] dstData, int[] tblOffsets, byte[][] tblData) {
        int bOffset = tblData.length;
        int mask = forceReadingUnsigned ? 0xFFFF : 0xFFFFFFFF;

        if (srcData.length < dstData.length) {
            for (int i = 0; i < srcData.length; i++) {
                int val = srcData[i] & mask;
                for (int b = 0; b < bOffset; b++) {
                    dstData[i * bOffset + b] = tblData[b][index(val, tblOffsets[b], tblData[b].length - 1)];
                }
            }
        } else {
            for (int b = 0; b < bOffset; b++) {
                byte[] t = tblData[b];
                int tblOffset = tblOffsets[b];
                int maxLength = t.length - 1;

                for (int i = b; i < srcData.length; i += bOffset) {
                    dstData[i] = t[index((srcData[i] & mask), tblOffset, maxLength)];
                }
            }
        }
    }

    // byte to short or ushort
    private void lookup(byte[] srcData, short[] dstData, int[] tblOffsets, short[][] tblData) {
        int bOffset = tblData.length;

        if (srcData.length < dstData.length) {
            for (int i = 0; i < srcData.length; i++) {
                int val = (srcData[i] & 0xFF);
                for (int b = 0; b < bOffset; b++) {
                    dstData[i * bOffset + b] = tblData[b][index(val, tblOffsets[b], tblData[b].length - 1)];
                }
            }
        } else {
            for (int b = 0; b < bOffset; b++) {
                short[] t = tblData[b];
                int tblOffset = tblOffsets[b];
                int maxLength = t.length - 1;

                for (int i = b; i < srcData.length; i += bOffset) {
                    dstData[i] = t[index((srcData[i] & 0xFF), tblOffset, maxLength)];
                }
            }
        }
    }

    // ushort to short or ushort
    private void lookupU(short[] srcData, short[] dstData, int[] tblOffsets, short[][] tblData) {
        int bOffset = tblData.length;
        if (srcData.length < dstData.length) {
            for (int i = 0; i < srcData.length; i++) {
                int val = (srcData[i] & 0xFFFF);
                for (int b = 0; b < bOffset; b++) {
                    dstData[i * bOffset + b] = tblData[b][index(val, tblOffsets[b], tblData[b].length - 1)];
                }
            }
        } else {
            for (int b = 0; b < bOffset; b++) {
                short[] t = tblData[b];
                int tblOffset = tblOffsets[b];
                int maxLength = t.length - 1;

                for (int i = b; i < srcData.length; i += bOffset) {
                    dstData[i] = t[index((srcData[i] & 0xFFFF), tblOffset, maxLength)];
                }
            }
        }
    }

    // short to short or ushort
    private void lookup(short[] srcData, short[] dstData, int[] tblOffsets, short[][] tblData) {
        int bOffset = tblData.length;
        int mask = forceReadingUnsigned ? 0xFFFF : 0xFFFFFFFF;

        if (srcData.length < dstData.length) {
            for (int i = 0; i < srcData.length; i++) {
                int val = (srcData[i] & mask);
                for (int b = 0; b < bOffset; b++) {
                    dstData[i * bOffset + b] = tblData[b][index(val, tblOffsets[b], tblData[b].length - 1)];
                }
            }
        } else {
            for (int b = 0; b < bOffset; b++) {
                short[] t = tblData[b];
                int tblOffset = tblOffsets[b];
                int maxLength = t.length - 1;

                for (int i = b; i < srcData.length; i += bOffset) {
                    dstData[i] = t[index((srcData[i] & mask), tblOffset, maxLength)];
                }
            }
        }
    }

}
