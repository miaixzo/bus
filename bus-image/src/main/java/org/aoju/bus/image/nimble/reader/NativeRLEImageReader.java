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
package org.aoju.bus.image.nimble.reader;

import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.toolkit.ByteKit;
import org.aoju.bus.logger.Logger;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.*;
import java.io.EOFException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class NativeRLEImageReader extends javax.imageio.ImageReader {

    private static final String UNKNOWN_IMAGE_TYPE =
            "RLE Image Reader needs ImageReadParam.destination or "
                    + "ImageReadParam.destinationType specified";
    private static final String UNSUPPORTED_DATA_TYPE =
            "Unsupported Data Type of ImageReadParam.destination or "
                    + "ImageReadParam.destinationType: ";
    private static final String MISMATCH_NUM_RLE_SEGMENTS =
            "Number of RLE Segments does not match image type: ";

    private final int[] header = new int[Normal._16];

    private final byte[] buf = new byte[8192];

    private long headerPos;

    private long bufOff;

    private int bufPos;

    private int bufLen;

    private ImageInputStream iis;

    private int width;

    private int height;

    protected NativeRLEImageReader(ImageReaderSpi originatingProvider) {
        super(originatingProvider);
    }

    @Override
    public void setInput(Object input, boolean seekForwardOnly,
                         boolean ignoreMetadata) {
        super.setInput(input, seekForwardOnly, ignoreMetadata);
        resetInternalState();
        iis = (ImageInputStream) input;
        try {
            headerPos = iis.getStreamPosition();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void resetInternalState() {
        width = 0;
        height = 0;
    }

    @Override
    public int getNumImages(boolean allowSearch) {
        return 1;
    }

    @Override
    public int getWidth(int imageIndex) {
        return width;
    }

    @Override
    public int getHeight(int imageIndex) {
        return height;
    }

    @Override
    public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex) {
        return null;
    }

    @Override
    public IIOMetadata getStreamMetadata() {
        return null;
    }

    @Override
    public IIOMetadata getImageMetadata(int imageIndex) {
        return null;
    }


    @Override
    public boolean canReadRaster() {
        return true;
    }

    @Override
    public Raster readRaster(int imageIndex, ImageReadParam param)
            throws IOException {
        checkIndex(imageIndex);

        WritableRaster raster = getDestinationRaster(param);
        read(raster.getDataBuffer());
        return raster;
    }

    @Override
    public BufferedImage read(int imageIndex, ImageReadParam param)
            throws IOException {
        checkIndex(imageIndex);

        BufferedImage bi = getDestination(param);
        read(bi.getRaster().getDataBuffer());
        return bi;
    }

    private void checkIndex(int imageIndex) {
        if (imageIndex != 0)
            throw new IndexOutOfBoundsException("imageIndex: " + imageIndex);
    }

    private BufferedImage getDestination(ImageReadParam param) {
        if (null == param)
            throw new IllegalArgumentException(UNKNOWN_IMAGE_TYPE);

        BufferedImage bi = param.getDestination();
        if (null != bi) {
            width = bi.getWidth();
            height = bi.getHeight();
            return bi;
        }

        ImageTypeSpecifier imageType = param.getDestinationType();
        if (null != imageType) {
            SampleModel sm = imageType.getSampleModel();
            width = sm.getWidth();
            height = sm.getHeight();
            return imageType.createBufferedImage(width, height);
        }
        throw new IllegalArgumentException(UNKNOWN_IMAGE_TYPE);
    }

    private WritableRaster getDestinationRaster(ImageReadParam param) {
        if (null == param)
            throw new IllegalArgumentException(UNKNOWN_IMAGE_TYPE);

        BufferedImage bi = param.getDestination();
        if (null != bi) {
            width = bi.getWidth();
            height = bi.getHeight();
            return bi.getRaster();
        }

        ImageTypeSpecifier imageType = param.getDestinationType();
        if (null != imageType) {
            SampleModel sm = imageType.getSampleModel();
            width = sm.getWidth();
            height = sm.getHeight();
            return Raster.createWritableRaster(sm, null);
        }
        throw new IllegalArgumentException(UNKNOWN_IMAGE_TYPE);
    }

    private void read(DataBuffer db) throws IOException {
        switch (db.getDataType()) {
            case DataBuffer.TYPE_BYTE:
                read(((DataBufferByte) db).getBankData());
                break;
            case DataBuffer.TYPE_USHORT:
                read(((DataBufferUShort) db).getData());
                break;
            case DataBuffer.TYPE_SHORT:
                read(((DataBufferShort) db).getData());
                break;
            default:
                throw new IllegalArgumentException(
                        UNSUPPORTED_DATA_TYPE + db.getDataType());
        }
    }

    private void read(byte[][] bands) throws IOException {
        readRLEHeader(bands.length);
        for (int i = 0; i < bands.length; i++)
            unrle(i + 1, bands[i]);
    }

    private void read(short[] data) throws IOException {
        readRLEHeader(2);
        Arrays.fill(data, (short) 0);
        unrle(1, data);
        unrle(2, data);
    }

    private void seekSegment(int seg) throws IOException {
        long streamPos = headerPos + (header[seg] & 0xffffffffL);
        int bufPos = (int) (streamPos - bufOff);
        if (bufPos >= 0 && bufPos <= bufLen)
            this.bufPos = bufPos;
        else {
            iis.seek(streamPos);
            this.bufPos = bufLen; // force fillBuffer on nextByte()
        }
    }


    private void readRLEHeader(int numSegments) throws IOException {
        fillBuffer();
        if (bufLen < Normal._64)
            throw new EOFException();
        for (int i = 0, off = 0; i < header.length; i++, off += 4)
            header[i] = ByteKit.bytesToIntLE(buf, off);
        bufPos = Normal._64;
        if (header[0] != numSegments)
            throw new IOException(MISMATCH_NUM_RLE_SEGMENTS + header[0]);
    }

    private void unrle(int seg, byte[] data) throws IOException {
        seekSegment(seg);
        int pos = 0;
        try {
            int n;
            int end;
            byte val;
            while (pos < data.length) {
                n = nextByte();
                if (n >= 0) {
                    read(data, pos, ++n);
                    pos += n;
                } else if (n != -Normal._128) {
                    end = pos + 1 - n;
                    val = nextByte();
                    while (pos < end)
                        data[pos++] = val;
                }
            }
        } catch (EOFException e) {
            Logger.info("RLE Segment #{} too short, set missing {} bytes to 0",
                    seg, data.length - pos);
        } catch (IndexOutOfBoundsException e) {
            Logger.info("RLE Segment #{} too long, truncate surplus bytes", seg);
        }
    }

    private void read(byte[] data, int pos, int len) throws IOException {
        int remaining = len;
        int n;
        while (remaining > 0) {
            n = bufLen - bufPos;
            if (n <= 0) {
                fillBuffer();
                n = bufLen - bufPos;
            }
            if ((remaining -= n) < 0)
                n += remaining;
            System.arraycopy(buf, bufPos, data, pos, n);
            bufPos += n;
            pos += n;
        }
    }

    private void unrle(int seg, short[] data) throws IOException {
        seekSegment(seg);
        int pos = 0;
        try {
            int shift = seg == 1 ? 8 : 0;
            int n;
            int end;
            int val;
            while (pos < data.length) {
                n = nextByte();
                if (n >= 0) {
                    read(data, pos, ++n, shift);
                    pos += n;
                } else if (n != -Normal._128) {
                    end = pos + 1 - n;
                    val = (nextByte() & 0xff) << shift;
                    while (pos < end)
                        data[pos++] |= val;
                }
            }
        } catch (EOFException e) {
            Logger.info("RLE Segment #{} too short, set missing {} bytes to 0",
                    seg, data.length - pos);
        } catch (IndexOutOfBoundsException e) {
            Logger.info("RLE Segment #{} to long, truncate surplus bytes", seg);
        }
    }

    private void read(short[] data, int pos, int len, int shift) throws IOException {
        int remaining = len;
        int n;
        while (remaining > 0) {
            n = bufLen - bufPos;
            if (n <= 0) {
                fillBuffer();
                n = bufLen - bufPos;
            }
            if ((remaining -= n) < 0)
                n += remaining;
            while (n-- > 0)
                data[pos++] |= (buf[bufPos++] & 0xff) << shift;
        }
    }

    private void fillBuffer() throws IOException {
        bufOff = iis.getStreamPosition();
        bufPos = 0;
        bufLen = iis.read(buf);
        if (bufLen <= 0)
            throw new EOFException();
    }

    private byte nextByte() throws IOException {
        if (bufPos >= bufLen)
            fillBuffer();

        return buf[bufPos++];
    }

}
