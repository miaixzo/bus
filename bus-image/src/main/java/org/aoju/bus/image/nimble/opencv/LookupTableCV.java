/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.image.nimble.opencv;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.util.Objects;

/**
 * @author Kimi Liu
 * @version 5.9.3
 * @since JDK 1.8+
 */
public class LookupTableCV {

    private final int[] offsets;
    private final DataBuffer data;

    public LookupTableCV(byte[] data) {
        this.offsets = new int[1];
        this.initOffsets(0);
        this.data = new DataBufferByte(Objects.requireNonNull(data), data.length);
    }

    public LookupTableCV(byte[] data, int offset) {
        this.offsets = new int[1];
        this.initOffsets(offset);
        this.data = new DataBufferByte(Objects.requireNonNull(data), data.length);
    }

    public LookupTableCV(byte[][] data) {
        this.offsets = new int[data.length];
        this.initOffsets(0);
        this.data = new DataBufferByte(Objects.requireNonNull(data), data[0].length);
    }

    public LookupTableCV(byte[][] data, int offset) {
        this.offsets = new int[data.length];
        this.initOffsets(offset);
        this.data = new DataBufferByte(Objects.requireNonNull(data), data[0].length);
    }

    public LookupTableCV(byte[][] data, int[] offsets) {
        this.offsets = new int[data.length];
        this.initOffsets(offsets);
        this.data = new DataBufferByte(Objects.requireNonNull(data), data[0].length);
    }

    public LookupTableCV(short[] data, int offset, boolean isUShort) {
        this.offsets = new int[1];
        this.initOffsets(offset);
        if (isUShort) {
            this.data = new DataBufferUShort(Objects.requireNonNull(data), data.length);
        } else {
            this.data = new DataBufferShort(Objects.requireNonNull(data), data.length);
        }
    }

    private void initOffsets(int offset) {

        for (int i = 0; i < offsets.length; i++) {
            offsets[i] = offset;
        }
    }

    private void initOffsets(int[] offset) {
        for (int i = 0; i < offsets.length; i++) {
            offsets[i] = offset[i];
        }
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

    public int getOffset(int band) {
        return offsets[band];
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

    public ImageCV lookup(Mat src) {
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
            throw new IllegalArgumentException("Not suported dataType for LUT transformation:" + src.toString());
        }

        int lkbBands = getNumBands();
        int lkpDataType = getDataType();

        // Table information.
        int[] tblOffsets = getOffsets();

        byte[][] bTblData = getByteData();
        short[][] sTblData = getShortData();

        if (lkbBands < channels) {
            byte[] b = bTblData[0];
            bTblData = new byte[channels][];
            for (int i = 0; i < bTblData.length; i++) {
                bTblData[i] = b;
            }

            int t = tblOffsets[0];
            tblOffsets = new int[channels];
            for (int i = 0; i < tblOffsets.length; i++) {
                tblOffsets[i] = t;
            }
            lkbBands = channels;
        }

        if (lkpDataType == DataBuffer.TYPE_BYTE) {
            boolean scrByte = srcDataType == DataBuffer.TYPE_BYTE;
            byte[] bDstData = scrByte && channels >= lkbBands ? bSrcData : new byte[width * height * lkbBands];
            if (scrByte && bSrcData != null) {
                lookup(bSrcData, bDstData, tblOffsets, bTblData);
            } else if (srcDataType == DataBuffer.TYPE_USHORT && sSrcData != null && bDstData != null) {
                lookupU(sSrcData, bDstData, tblOffsets, bTblData);
            } else if (srcDataType == DataBuffer.TYPE_SHORT && sSrcData != null && bDstData != null) {
                lookup(sSrcData, bDstData, tblOffsets, bTblData);
            } else {
                throw new IllegalArgumentException("Not supported LUT conversion from source dataType " + srcDataType);
            }

            ImageCV dst = new ImageCV(height, width, CvType.CV_8UC(lkbBands));
            dst.put(0, 0, bDstData);
            return dst;

        } else if (lkpDataType == DataBuffer.TYPE_USHORT || lkpDataType == DataBuffer.TYPE_SHORT) {
            boolean scrByte = srcDataType == DataBuffer.TYPE_BYTE;
            short[] sDstData = !scrByte && channels >= lkbBands ? sSrcData : new short[width * height * lkbBands];
            if (scrByte) {
                lookup(bSrcData, sDstData, tblOffsets, sTblData);
            } else if (srcDataType == DataBuffer.TYPE_USHORT) {
                lookupU(sSrcData, sDstData, tblOffsets, sTblData);
            } else if (srcDataType == DataBuffer.TYPE_SHORT) {
                lookup(sSrcData, sDstData, tblOffsets, sTblData);
            } else {
                throw new IllegalArgumentException("Not supported LUT conversion from source dataType " + srcDataType);
            }

            ImageCV dst = new ImageCV(height, width,
                    lkpDataType == DataBuffer.TYPE_USHORT ? CvType.CV_16UC(channels) : CvType.CV_16SC(channels));
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
                    dstData[i * bOffset + b] = tblData[b][val - tblOffsets[b]];
                }
            }
        } else {
            for (int b = 0; b < bOffset; b++) {
                byte[] t = tblData[b];
                int tblOffset = tblOffsets[b];

                for (int i = b; i < srcData.length; i += bOffset) {
                    dstData[i] = t[(srcData[i] & 0xFF) - tblOffset];
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
                    dstData[i * bOffset + b] = tblData[b][val - tblOffsets[b]];
                }
            }
        } else {
            for (int b = 0; b < bOffset; b++) {
                byte[] t = tblData[b];
                int tblOffset = tblOffsets[b];

                for (int i = b; i < srcData.length; i += bOffset) {
                    dstData[i] = t[(srcData[i] & 0xFFFF) - tblOffset];
                }
            }
        }
    }

    // short to byte
    private void lookup(short[] srcData, byte[] dstData, int[] tblOffsets, byte[][] tblData) {
        int bOffset = tblData.length;

        if (srcData.length < dstData.length) {
            for (int i = 0; i < srcData.length; i++) {
                int val = srcData[i];
                for (int b = 0; b < bOffset; b++) {
                    dstData[i * bOffset + b] = tblData[b][val - tblOffsets[b]];
                }
            }
        } else {
            for (int b = 0; b < bOffset; b++) {
                byte[] t = tblData[b];
                int tblOffset = tblOffsets[b];

                for (int i = b; i < srcData.length; i += bOffset) {
                    dstData[i] = t[(srcData[i]) - tblOffset];
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
                    dstData[i * bOffset + b] = tblData[b][val - tblOffsets[b]];
                }
            }
        } else {
            for (int b = 0; b < bOffset; b++) {
                short[] t = tblData[b];
                int tblOffset = tblOffsets[b];

                for (int i = b; i < srcData.length; i += bOffset) {
                    dstData[i] = t[(srcData[i] & 0xFF) - tblOffset];
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
                    dstData[i * bOffset + b] = tblData[b][val - tblOffsets[b]];
                }
            }
        } else {
            for (int b = 0; b < bOffset; b++) {
                short[] t = tblData[b];
                int tblOffset = tblOffsets[b];

                for (int i = b; i < srcData.length; i += bOffset) {
                    srcData[i] = t[(srcData[i] & 0xFFFF) - tblOffset];
                }
            }
        }
    }

    // short to short or ushort
    private void lookup(short[] srcData, short[] dstData, int[] tblOffsets, short[][] tblData) {
        int bOffset = tblData.length;

        if (srcData.length < dstData.length) {
            for (int i = 0; i < srcData.length; i++) {
                int val = srcData[i];
                for (int b = 0; b < bOffset; b++) {
                    dstData[i * bOffset + b] = tblData[b][val - tblOffsets[b]];
                }
            }
        } else {
            for (int b = 0; b < bOffset; b++) {
                short[] t = tblData[b];
                int tblOffset = tblOffsets[b];

                for (int i = b; i < srcData.length; i += bOffset) {
                    srcData[i] = t[(srcData[i]) - tblOffset];
                }
            }
        }
    }

}
