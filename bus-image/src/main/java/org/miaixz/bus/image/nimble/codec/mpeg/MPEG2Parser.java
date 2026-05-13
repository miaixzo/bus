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
package org.miaixz.bus.image.nimble.codec.mpeg;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.galaxy.SafeBuffer;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.VR;
import org.miaixz.bus.image.nimble.codec.XPEGParser;
import org.miaixz.bus.image.nimble.codec.mp4.MP4FileType;

/**
 * Represents the MPEG2Parser type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class MPEG2Parser implements XPEGParser {

    /**
     * The buffer size value.
     */
    private static final int BUFFER_SIZE = 8162;

    /**
     * The sequence header stream id value.
     */
    private static final int SEQUENCE_HEADER_STREAM_ID = (byte) 0xb3;

    /**
     * The gop header stream id value.
     */
    private static final int GOP_HEADER_STREAM_ID = (byte) 0xb8;

    /**
     * The aspect ratio 1 1 value.
     */
    private static final String[] ASPECT_RATIO_1_1 = { "1", "1" };

    /**
     * The aspect ratio 4 3 value.
     */
    private static final String[] ASPECT_RATIO_4_3 = { "4", "3" };

    /**
     * The aspect ratio 16 9 value.
     */
    private static final String[] ASPECT_RATIO_16_9 = { "16", "9" };

    /**
     * The aspect ratio 221 100 value.
     */
    private static final String[] ASPECT_RATIO_221_100 = { "221", "100" };

    /**
     * The aspect ratios value.
     */
    private static final String[][] ASPECT_RATIOS = { ASPECT_RATIO_1_1, ASPECT_RATIO_4_3, ASPECT_RATIO_16_9,
            ASPECT_RATIO_221_100 };

    /**
     * The fps value.
     */
    private static final int[] FPS = { 24, 1001, 24, 1000, 25, 1000, 30, 1001, 30, 1000, 50, 1000, 60, 1001, 60, 1000 };

    /**
     * The data value.
     */
    private final byte[] data = new byte[BUFFER_SIZE];

    /**
     * The buf value.
     */
    private final ByteBuffer buf = ByteBuffer.wrap(data);

    /**
     * The columns value.
     */
    private final int columns;

    /**
     * The rows value.
     */
    private final int rows;

    /**
     * The aspect ratio value.
     */
    private final int aspectRatio;

    /**
     * The frame rate value.
     */
    private final int frameRate;

    /**
     * The duration value.
     */
    private final int duration;

    /**
     * Creates a new instance.
     *
     * @param channel the channel.
     * @throws IOException if the operation cannot be completed.
     */
    public MPEG2Parser(SeekableByteChannel channel) throws IOException {
        int startCode = readStartCode(channel);
        if (!isSequenceHeader(startCode)) {
            while (!isVideoStream(startCode)) {
                skip(channel, packetLength(channel, startCode));
                startCode = readStartCode(channel);
            }
            findSequenceHeader(channel, packetLength(channel, startCode));
        }
        SafeBuffer.clear(buf).limit(7);
        channel.read(buf);
        columns = ((data[0] & 0xff) << 4) | ((data[1] & 0xf0) >> 4);
        rows = ((data[1] & 0x0f) << 8) | (data[2] & 0xff);
        aspectRatio = (data[3] >> 4) & 0x0f;
        frameRate = Math.max(1, Math.min(data[3] & 0x0f, 8));
        int lastGOP = findLastGOP(channel);
        int hh = (data[lastGOP] & 0x7c) >> 2;
        int mm = ((data[lastGOP] & 0x03) << 4) | ((data[lastGOP + 1] & 0xf0) >> 4);
        int ss = ((data[lastGOP + 1] & 0x07) << 3) | ((data[lastGOP + 2] & 0xe0) >> 5);
        duration = hh * 3600 + mm * 60 + ss;
    }

    /**
     * Determines whether pack header.
     *
     * @param startCode the start code.
     * @return true if the condition is met; otherwise false.
     */
    private static boolean isPackHeader(int startCode) {
        return startCode == 0x1ba;
    }

    /**
     * Determines whether sequence header.
     *
     * @param startCode the start code.
     * @return true if the condition is met; otherwise false.
     */
    private static boolean isSequenceHeader(int startCode) {
        return startCode == 0x1b3;
    }

    /**
     * Determines whether video stream.
     *
     * @param startCode the start code.
     * @return true if the condition is met; otherwise false.
     */
    private static boolean isVideoStream(int startCode) {
        return (startCode & 0xfffff0) == 0x1e0;
    }

    /**
     * Gets the code stream position.
     *
     * @return the code stream position.
     */
    @Override
    public long getCodeStreamPosition() {
        return 0;
    }

    /**
     * Gets the position after app segments.
     *
     * @return the position after app segments.
     */
    @Override
    public long getPositionAfterAPPSegments() {
        return -1L;
    }

    /**
     * Gets the mp4 file type.
     *
     * @return the mp4 file type.
     */
    @Override
    public MP4FileType getMP4FileType() {
        return null;
    }

    /**
     * Gets the attributes.
     *
     * @param attrs the attrs.
     * @return the attributes.
     */
    @Override
    public Attributes getAttributes(Attributes attrs) {
        if (attrs == null)
            attrs = new Attributes(15);

        int frameRate2 = (frameRate - 1) << 1;
        int fps = FPS[frameRate2];
        attrs.setInt(Tag.CineRate, VR.IS, fps);
        attrs.setFloat(Tag.FrameTime, VR.DS, ((float) FPS[frameRate2 + 1]) / fps);
        attrs.setInt(Tag.SamplesPerPixel, VR.US, 3);
        attrs.setString(Tag.PhotometricInterpretation, VR.CS, "YBR_PARTIAL_420");
        attrs.setInt(Tag.PlanarConfiguration, VR.US, 0);
        attrs.setInt(Tag.FrameIncrementPointer, VR.AT, Tag.FrameTime);
        attrs.setInt(Tag.NumberOfFrames, VR.IS, (int) (duration * fps * 1000L / FPS[frameRate2 + 1]));
        attrs.setInt(Tag.Rows, VR.US, rows);
        attrs.setInt(Tag.Columns, VR.US, columns);
        if (aspectRatio > 0 && aspectRatio < 5)
            attrs.setString(Tag.PixelAspectRatio, VR.IS, ASPECT_RATIOS[aspectRatio - 1]);
        attrs.setInt(Tag.BitsAllocated, VR.US, 8);
        attrs.setInt(Tag.BitsStored, VR.US, 8);
        attrs.setInt(Tag.HighBit, VR.US, 7);
        attrs.setInt(Tag.PixelRepresentation, VR.US, 0);
        attrs.setString(Tag.LossyImageCompression, VR.CS, "01");
        return attrs;
    }

    /**
     * Gets the transfer syntax uid.
     *
     * @param fragmented the fragmented.
     * @return the transfer syntax uid.
     */
    @Override
    public String getTransferSyntaxUID(boolean fragmented) {
        return frameRate <= 5 && columns <= 720 ? fragmented ? UID.MPEG2MPMLF.uid : UID.MPEG2MPML.uid
                : fragmented ? UID.MPEG2MPHLF.uid : UID.MPEG2MPHL.uid;
    }

    /**
     * Finds the sequence header.
     *
     * @param channel the channel.
     * @param length  the length.
     * @throws IOException if the operation cannot be completed.
     */
    private void findSequenceHeader(SeekableByteChannel channel, int length) throws IOException {
        int remaining = length;
        SafeBuffer.clear(buf).limit(3);
        while ((remaining -= buf.remaining()) > 1) {
            channel.read(buf);
            SafeBuffer.rewind(buf);
            if (((data[0] << 16) | (data[1] << 8) | data[2]) == 1) {
                SafeBuffer.clear(buf).limit(1);
                remaining--;
                channel.read(buf);
                SafeBuffer.rewind(buf);
                if (buf.get() == SEQUENCE_HEADER_STREAM_ID)
                    return;
                SafeBuffer.limit(buf, 3);
            }
            SafeBuffer.position(buf, data[2] == 0 ? data[1] == 0 ? 2 : 1 : 0);
            data[0] = 0;
        }
        throw new IOException("MPEG2 sequence header not found");
    }

    /**
     * Executes the skip operation.
     *
     * @param channel the channel.
     * @param n       the n.
     * @throws IOException if the operation cannot be completed.
     */
    private void skip(SeekableByteChannel channel, long n) throws IOException {
        channel.position(channel.position() + n);
    }

    /**
     * Finds the last gop.
     *
     * @param channel the channel.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    private int findLastGOP(SeekableByteChannel channel) throws IOException {
        long pos = channel.size() - 8;
        do {
            pos = Math.max(0, pos + 8 - BUFFER_SIZE);
            channel.position(pos);
            SafeBuffer.clear(buf);
            channel.read(buf);
            int i = 0;
            while (i + 8 < BUFFER_SIZE) {
                if (((data[i] << 16) | (data[i + 1] << 8) | data[i + 2]) == 1) {
                    if (data[i + 3] == GOP_HEADER_STREAM_ID)
                        return i + 4;
                }
                i += data[i + 2] == 0 ? data[i + 1] == 0 ? 1 : 2 : 3;
            }
        } while (pos > 0);
        throw new IOException("last MPEG2 Group of Pictures not found");
    }

    /**
     * Reads the start code.
     *
     * @param channel the channel.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    private int readStartCode(SeekableByteChannel channel) throws IOException {
        SafeBuffer.clear(buf).limit(4);
        channel.read(buf);
        SafeBuffer.rewind(buf);
        int startCode = buf.getInt();
        if ((startCode & 0xfffffe00) != 0) {
            throw new IOException(
                    String.format("Invalid MPEG2 start code %4XH on position %d", startCode, channel.position() - 4));
        }
        return startCode;
    }

    /**
     * Executes the packet length operation.
     *
     * @param channel   the channel.
     * @param startCode the start code.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    private int packetLength(SeekableByteChannel channel, int startCode) throws IOException {
        SafeBuffer.clear(buf).limit(2);
        channel.read(buf);
        SafeBuffer.rewind(buf);
        return isPackHeader(startCode) ? ((data[0] & 0xc0) != 0) ? 8 : 6 : buf.getShort() & 0xffff;
    }

}
