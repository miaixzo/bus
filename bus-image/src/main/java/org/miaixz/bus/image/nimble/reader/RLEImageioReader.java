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
package org.miaixz.bus.image.nimble.reader;

import java.awt.image.*;
import java.io.EOFException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

import org.miaixz.bus.core.xyz.ByteKit;
import org.miaixz.bus.logger.Logger;

/**
 * Represents the RLEImageioReader type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class RLEImageioReader extends ImageReader {

    /**
     * The unknown image type value.
     */
    private static final String UNKNOWN_IMAGE_TYPE = "RLE Image Reader needs ImageReadParam.destination or "
            + "ImageReadParam.destinationType specified";

    /**
     * The unsupported data type value.
     */
    private static final String UNSUPPORTED_DATA_TYPE = "Unsupported Data Type of ImageReadParam.destination or "
            + "ImageReadParam.destinationType: ";

    /**
     * The mismatch num rle segments value.
     */
    private static final String MISMATCH_NUM_RLE_SEGMENTS = "Number of RLE Segments does not match image type: ";

    /**
     * The header value.
     */
    private final int[] header = new int[16];

    /**
     * The buf value.
     */
    private final byte[] buf = new byte[8192];

    /**
     * The header pos value.
     */
    private long headerPos;

    /**
     * The buf off value.
     */
    private long bufOff;

    /**
     * The buf pos value.
     */
    private int bufPos;

    /**
     * The buf len value.
     */
    private int bufLen;

    /**
     * The iis value.
     */
    private ImageInputStream iis;

    /**
     * The width value.
     */
    private int width;

    /**
     * The height value.
     */
    private int height;

    /**
     * Creates a new instance.
     *
     * @param originatingProvider the originating provider.
     */
    protected RLEImageioReader(ImageReaderSpi originatingProvider) {
        super(originatingProvider);
    }

    /**
     * Sets the input.
     *
     * @param input           the input.
     * @param seekForwardOnly the seek forward only.
     * @param ignoreMetadata  the ignore metadata.
     */
    @Override
    public void setInput(Object input, boolean seekForwardOnly, boolean ignoreMetadata) {
        super.setInput(input, seekForwardOnly, ignoreMetadata);
        resetInternalState();
        iis = (ImageInputStream) input;
        try {
            headerPos = iis.getStreamPosition();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Resets the internal state.
     */
    private void resetInternalState() {
        width = 0;
        height = 0;
    }

    /**
     * Gets the num images.
     *
     * @param allowSearch the allow search.
     * @return the num images.
     */
    @Override
    public int getNumImages(boolean allowSearch) {
        return 1;
    }

    /**
     * Gets the width.
     *
     * @param imageIndex the image index.
     * @return the width.
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public int getWidth(int imageIndex) throws IOException {
        return width;
    }

    /**
     * Gets the height.
     *
     * @param imageIndex the image index.
     * @return the height.
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public int getHeight(int imageIndex) throws IOException {
        return height;
    }

    /**
     * Gets the image types.
     *
     * @param imageIndex the image index.
     * @return the image types.
     */
    @Override
    public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex) {
        return null;
    }

    /**
     * Gets the stream metadata.
     *
     * @return the stream metadata.
     */
    @Override
    public IIOMetadata getStreamMetadata() {
        return null;
    }

    /**
     * Gets the image metadata.
     *
     * @param imageIndex the image index.
     * @return the image metadata.
     */
    @Override
    public IIOMetadata getImageMetadata(int imageIndex) {
        return null;
    }

    /**
     * Determines whether read raster.
     *
     * @return true if the condition is met; otherwise false.
     */
    @Override
    public boolean canReadRaster() {
        return true;
    }

    /**
     * Reads the raster.
     *
     * @param imageIndex the image index.
     * @param param      the param.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public Raster readRaster(int imageIndex, ImageReadParam param) throws IOException {
        checkIndex(imageIndex);

        WritableRaster raster = getDestinationRaster(param);
        read(raster.getDataBuffer());
        return raster;
    }

    /**
     * Executes the read operation.
     *
     * @param imageIndex the image index.
     * @param param      the param.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public BufferedImage read(int imageIndex, ImageReadParam param) throws IOException {
        checkIndex(imageIndex);

        BufferedImage bi = getDestination(param);
        read(bi.getRaster().getDataBuffer());
        return bi;
    }

    /**
     * Executes the check index operation.
     *
     * @param imageIndex the image index.
     */
    private void checkIndex(int imageIndex) {
        if (imageIndex != 0)
            throw new IndexOutOfBoundsException("imageIndex: " + imageIndex);
    }

    /**
     * Gets the destination.
     *
     * @param param the param.
     * @return the destination.
     */
    private BufferedImage getDestination(ImageReadParam param) {
        if (param == null)
            throw new IllegalArgumentException(UNKNOWN_IMAGE_TYPE);

        BufferedImage bi = param.getDestination();
        if (bi != null) {
            width = bi.getWidth();
            height = bi.getHeight();
            return bi;
        }

        ImageTypeSpecifier imageType = param.getDestinationType();
        if (imageType != null) {
            SampleModel sm = imageType.getSampleModel();
            width = sm.getWidth();
            height = sm.getHeight();
            return imageType.createBufferedImage(width, height);
        }
        throw new IllegalArgumentException(UNKNOWN_IMAGE_TYPE);
    }

    /**
     * Gets the destination raster.
     *
     * @param param the param.
     * @return the destination raster.
     */
    private WritableRaster getDestinationRaster(ImageReadParam param) {
        if (param == null)
            throw new IllegalArgumentException(UNKNOWN_IMAGE_TYPE);

        BufferedImage bi = param.getDestination();
        if (bi != null) {
            width = bi.getWidth();
            height = bi.getHeight();
            return bi.getRaster();
        }

        ImageTypeSpecifier imageType = param.getDestinationType();
        if (imageType != null) {
            SampleModel sm = imageType.getSampleModel();
            width = sm.getWidth();
            height = sm.getHeight();
            return Raster.createWritableRaster(sm, null);
        }
        throw new IllegalArgumentException(UNKNOWN_IMAGE_TYPE);
    }

    /**
     * Executes the read operation.
     *
     * @param db the db.
     * @throws IOException if the operation cannot be completed.
     */
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
                throw new IllegalArgumentException(UNSUPPORTED_DATA_TYPE + db.getDataType());
        }
    }

    /**
     * Executes the read operation.
     *
     * @param bands the bands.
     * @throws IOException if the operation cannot be completed.
     */
    private void read(byte[][] bands) throws IOException {
        readRLEHeader(bands.length);
        for (int i = 0; i < bands.length; i++)
            unrle(i + 1, bands[i]);
    }

    /**
     * Executes the read operation.
     *
     * @param data the data.
     * @throws IOException if the operation cannot be completed.
     */
    private void read(short[] data) throws IOException {
        readRLEHeader(2);
        Arrays.fill(data, (short) 0);
        unrle(1, data);
        unrle(2, data);
    }

    /**
     * Executes the seek segment operation.
     *
     * @param seg the seg.
     * @throws IOException if the operation cannot be completed.
     */
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

    /**
     * Reads the rle header.
     *
     * @param numSegments the num segments.
     * @throws IOException if the operation cannot be completed.
     */
    private void readRLEHeader(int numSegments) throws IOException {
        fillBuffer();
        if (bufLen < 64)
            throw new EOFException();
        for (int i = 0, off = 0; i < header.length; i++, off += 4)
            header[i] = ByteKit.bytesToIntLE(buf, off);
        bufPos = 64;
        if (header[0] != numSegments)
            throw new IOException(MISMATCH_NUM_RLE_SEGMENTS + header[0]);
    }

    /**
     * Executes the unrle operation.
     *
     * @param seg  the seg.
     * @param data the data.
     * @throws IOException if the operation cannot be completed.
     */
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
                } else if (n != -128) {
                    end = pos + 1 - n;
                    val = nextByte();
                    while (pos < end)
                        data[pos++] = val;
                }
            }
        } catch (EOFException e) {
            Logger.info(false, "Image", "RLE Segment #{} too short, set missing {} bytes to 0", seg, data.length - pos);
        } catch (IndexOutOfBoundsException e) {
            Logger.info(false, "Image", "RLE Segment #{} too long, truncate surplus bytes", seg);
        }
    }

    /**
     * Executes the read operation.
     *
     * @param data the data.
     * @param pos  the pos.
     * @param len  the len.
     * @throws IOException if the operation cannot be completed.
     */
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

    /**
     * Executes the unrle operation.
     *
     * @param seg  the seg.
     * @param data the data.
     * @throws IOException if the operation cannot be completed.
     */
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
                } else if (n != -128) {
                    end = pos + 1 - n;
                    val = (nextByte() & 0xff) << shift;
                    while (pos < end)
                        data[pos++] |= val;
                }
            }
        } catch (EOFException e) {
            Logger.info(false, "Image", "RLE Segment #{} too short, set missing {} bytes to 0", seg, data.length - pos);
        } catch (IndexOutOfBoundsException e) {
            Logger.info(false, "Image", "RLE Segment #{} to long, truncate surplus bytes", seg);
        }
    }

    /**
     * Executes the read operation.
     *
     * @param data  the data.
     * @param pos   the pos.
     * @param len   the len.
     * @param shift the shift.
     * @throws IOException if the operation cannot be completed.
     */
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

    /**
     * Executes the fill buffer operation.
     *
     * @throws IOException if the operation cannot be completed.
     */
    private void fillBuffer() throws IOException {
        bufOff = iis.getStreamPosition();
        bufPos = 0;
        bufLen = iis.read(buf);
        if (bufLen <= 0)
            throw new EOFException();
    }

    /**
     * Executes the next byte operation.
     *
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    private byte nextByte() throws IOException {
        if (bufPos >= bufLen)
            fillBuffer();

        return buf[bufPos++];
    }

}
