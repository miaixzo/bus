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
package org.miaixz.bus.image.nimble.codec.mp4;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.Date;

import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.galaxy.SafeBuffer;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.VR;
import org.miaixz.bus.image.nimble.codec.XPEGParser;
import org.miaixz.bus.image.nimble.codec.mpeg.MPEGHeader;

/**
 * Parser for MP4 video file format, extracting metadata from MPEG-4 container files.
 * <p>
 * This parser reads the MP4 box structure to extract video metadata such as dimensions, frame rate, codec information,
 * and timing data. It supports both AVC (H.264) and HEVC (H.265) video codecs.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class MP4Parser implements XPEGParser {

    /**
     * The file box type value.
     */
    private static final int FileBoxType = 0x66747970; // ftyp;

    /**
     * The movie box type value.
     */
    private static final int MovieBoxType = 0x6d6f6f76; // moov;

    /**
     * The track box type value.
     */
    private static final int TrackBoxType = 0x7472616b; // trak
    /**
     * The media box type value.
     */
    private static final int MediaBoxType = 0x6d646961; // mdia
    /**
     * The media header box type value.
     */
    private static final int MediaHeaderBoxType = 0x6d646864; // mdhd
    /**
     * The media information box type value.
     */
    private static final int MediaInformationBoxType = 0x6d696e66; // minf
    /**
     * The sample table box type value.
     */
    private static final int SampleTableBoxType = 0x7374626c; // stbl
    /**
     * The sample description box type value.
     */
    private static final int SampleDescriptionBoxType = 0x73747364; // stsd
    /**
     * The visual sample entry type avc1 value.
     */
    private static final int VisualSampleEntryTypeAVC1 = 0x61766331; // avc1
    /**
     * The avc configuration box type value.
     */
    private static final int AvcConfigurationBoxType = 0x61766343; // avcC
    /**
     * The visual sample entry type hvc1 value.
     */
    private static final int VisualSampleEntryTypeHVC1 = 0x68766331; // hvc1
    /**
     * The hevc configuration box type value.
     */
    private static final int HevcConfigurationBoxType = 0x68766343; // hvcC
    /**
     * The sample size box type value.
     */
    private static final int SampleSizeBoxType = 0x7374737a; // stsz

    /**
     * The buf value.
     */
    private final ByteBuffer buf = ByteBuffer.allocate(8);

    /**
     * The mp4 file type value.
     */
    private MP4FileType mp4FileType;

    /**
     * The creation time value.
     */
    private Date creationTime;

    /**
     * The modification time value.
     */
    private Date modificationTime;

    /**
     * The timescale value.
     */
    private int timescale;

    /**
     * The duration value.
     */
    private long duration;

    /**
     * The fp1000s value.
     */
    private int fp1000s;

    /**
     * The rows value.
     */
    private int rows;

    /**
     * The columns value.
     */
    private int columns;

    /**
     * The num frames value.
     */
    private int numFrames;

    /**
     * The visual sample entry type value.
     */
    private int visualSampleEntryType;

    /**
     * The configuration version value.
     */
    private int configurationVersion;

    /**
     * The profile idc value.
     */
    private int profile_idc;

    /**
     * The level idc value.
     */
    private int level_idc;

    /**
     * Creates a new instance.
     *
     * @param channel the channel.
     * @throws IOException if the operation cannot be completed.
     */
    public MP4Parser(SeekableByteChannel channel) throws IOException {
        long position = channel.position();
        Box box = nextBox(channel, channel.size() - position);
        if (box.type == FileBoxType) {
            mp4FileType = new MP4FileType(readInt(channel), readInt(channel), readInts(channel, box.end));
        } else {
            channel.position(position);
        }
        parseMovieBox(channel, findBox(channel, channel.size(), MovieBoxType));
    }

    /**
     * Executes the box not found operation.
     *
     * @param type the type.
     * @return the operation result.
     */
    private static String boxNotFound(int type) {
        return String.format(
                "%c%c%c%c box not found",
                (type >> 24) & 0xff,
                (type >> 16) & 0xff,
                (type >> 8) & 0xff,
                type & 0xff);
    }

    /**
     * Converts this value to date.
     *
     * @param val the val.
     * @return the operation result.
     */
    private static Date toDate(long val) {
        return val > 0 ? new Date((val - 2082844800L) * 1000L) : null;
    }

    /**
     * Gets the creation time.
     *
     * @return the creation time.
     */
    public Date getCreationTime() {
        return creationTime;
    }

    /**
     * Gets the modification time.
     *
     * @return the modification time.
     */
    public Date getModificationTime() {
        return modificationTime;
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
        return mp4FileType;
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
            attrs = new Attributes(14);

        attrs.setInt(Tag.CineRate, VR.IS, (fp1000s + 500) / 1000);
        attrs.setFloat(Tag.FrameTime, VR.DS, 1_000_000.f / fp1000s);
        return MPEGHeader.setImageAttributes(attrs, numFrames, rows, columns);
    }

    /**
     * Gets the transfer syntax uid.
     *
     * @param fragmented the fragmented.
     * @return the transfer syntax uid.
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public String getTransferSyntaxUID(boolean fragmented) throws IOException {
        switch (visualSampleEntryType) {
            case VisualSampleEntryTypeAVC1:
                switch (profile_idc) {
                    case 100: // High Profile
                        if (level_idc <= 41)
                            return isBDCompatible() ? fragmented ? UID.MPEG4HP41BDF.uid : UID.MPEG4HP41BD.uid
                                    : fragmented ? UID.MPEG4HP41F.uid : UID.MPEG4HP41.uid;
                        else if (level_idc <= 42)
                            // TODO: distinguish between MPEG4HP422D
                            // and MPEG4HP423D
                            return fragmented ? UID.MPEG4HP422DF.uid : UID.MPEG4HP422D.uid;
                        break;

                    case 128: // Stereo High Profile
                        if (level_idc <= 42)
                            return UID.MPEG4HP42STEREO.uid;
                        break;
                }
                throw profileLevelNotSupported("MPEG-4 AVC profile_idc/level_idc: %d/%d not supported");

            case VisualSampleEntryTypeHVC1:
                if (level_idc <= 51) {
                    switch (profile_idc) {
                        case 1: // Main Profile
                            return UID.HEVCMP51.uid;

                        case 2: // Main 10 Profile
                            return UID.HEVCM10P51.uid;
                    }
                }
                throw profileLevelNotSupported("MPEG-4 HEVC profile_idc/level_idc: %d/%d not supported");
        }
        throw new AssertionError("visualSampleEntryType:" + visualSampleEntryType);
    }

    /**
     * Executes the profile level not supported operation.
     *
     * @param format the format.
     * @return the operation result.
     */
    private IOException profileLevelNotSupported(String format) {
        return new IOException(String.format(format, profile_idc, level_idc));
    }

    /**
     * Determines whether bd compatible.
     *
     * @return true if the condition is met; otherwise false.
     */
    private boolean isBDCompatible() {
        return rows == 1080
                ? columns == 1920 && (fp1000s == 23976 || fp1000s == 24000 || fp1000s == 25000 || fp1000s == 29970)
                : rows == 720 && columns == 1280
                        && (fp1000s == 23976 || fp1000s == 24000 || fp1000s == 50000 || fp1000s == 59940);
    }

    /**
     * Executes the next box operation.
     *
     * @param channel   the channel.
     * @param remaining the remaining.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    private Box nextBox(SeekableByteChannel channel, long remaining) throws IOException {
        long pos = channel.position();
        long type = readLong(channel);
        long size = type >>> 32;
        return new Box((int) type, pos + (size == 0 ? remaining : size == 1 ? readLong(channel) : size));
    }

    /**
     * Finds the box.
     *
     * @param channel the channel.
     * @param end     the end.
     * @param type    the type.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    private Box findBox(SeekableByteChannel channel, long end, int type) throws IOException {
        long remaining;
        while ((remaining = end - channel.position()) > 0) {
            Box box = nextBox(channel, remaining);
            if (box.type == type)
                return box;
            channel.position(box.end);
        }
        throw new IOException(boxNotFound(type));
    }

    /**
     * Reads the ints.
     *
     * @param channel the channel.
     * @param end     the end.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    private int[] readInts(SeekableByteChannel channel, long end) throws IOException {
        int[] values = new int[(int) ((end - channel.position()) / 4)];
        for (int i = 0; i < values.length; i++) {
            values[i] = readInt(channel);
        }
        return values;
    }

    /**
     * Reads the byte.
     *
     * @param channel the channel.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    private byte readByte(SeekableByteChannel channel) throws IOException {
        SafeBuffer.clear(buf).limit(1);
        channel.read(buf);
        SafeBuffer.rewind(buf);
        return buf.get();
    }

    /**
     * Reads the short.
     *
     * @param channel the channel.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    private short readShort(SeekableByteChannel channel) throws IOException {
        SafeBuffer.clear(buf).limit(2);
        channel.read(buf);
        SafeBuffer.rewind(buf);
        return buf.getShort();
    }

    /**
     * Reads the int.
     *
     * @param channel the channel.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    private int readInt(SeekableByteChannel channel) throws IOException {
        SafeBuffer.clear(buf).limit(4);
        channel.read(buf);
        SafeBuffer.rewind(buf);
        return buf.getInt();
    }

    /**
     * Reads the long.
     *
     * @param channel the channel.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    private long readLong(SeekableByteChannel channel) throws IOException {
        SafeBuffer.clear(buf);
        channel.read(buf);
        SafeBuffer.rewind(buf);
        return buf.getLong();
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
     * Parses the movie box.
     *
     * @param channel the channel.
     * @param box     the box.
     * @throws IOException if the operation cannot be completed.
     */
    private void parseMovieBox(SeekableByteChannel channel, Box box) throws IOException {
        do {
            parseTrackBox(channel, findBox(channel, box.end, TrackBoxType));
        } while (visualSampleEntryType == 0);
        channel.position(box.end);
    }

    /**
     * Parses the track box.
     *
     * @param channel the channel.
     * @param box     the box.
     * @throws IOException if the operation cannot be completed.
     */
    private void parseTrackBox(SeekableByteChannel channel, Box box) throws IOException {
        parseMediaBox(channel, findBox(channel, box.end, MediaBoxType));
        channel.position(box.end);
    }

    /**
     * Parses the media box.
     *
     * @param channel the channel.
     * @param box     the box.
     * @throws IOException if the operation cannot be completed.
     */
    private void parseMediaBox(SeekableByteChannel channel, Box box) throws IOException {
        parseMediaHeaderBox(channel, findBox(channel, box.end, MediaHeaderBoxType));
        parseMediaInformationBox(channel, findBox(channel, box.end, MediaInformationBoxType));
        channel.position(box.end);
    }

    /**
     * Parses the media header box.
     *
     * @param channel the channel.
     * @param box     the box.
     * @throws IOException if the operation cannot be completed.
     */
    private void parseMediaHeaderBox(SeekableByteChannel channel, Box box) throws IOException {
        if ((readInt(channel) >>> 24) == 1) {
            creationTime = toDate(readLong(channel));
            modificationTime = toDate(readLong(channel));
            timescale = readInt(channel);
            duration = readLong(channel);
        } else {
            creationTime = toDate(readInt(channel) & 0xffffffffL);
            modificationTime = toDate(readInt(channel) & 0xffffffffL);
            timescale = readInt(channel);
            duration = readInt(channel) & 0xffffffffL;
        }
        channel.position(box.end);
    }

    /**
     * Parses the media information box.
     *
     * @param channel the channel.
     * @param box     the box.
     * @throws IOException if the operation cannot be completed.
     */
    private void parseMediaInformationBox(SeekableByteChannel channel, Box box) throws IOException {
        parseSampleTableBox(channel, findBox(channel, box.end, SampleTableBoxType));
        channel.position(box.end);
    }

    /**
     * Parses the sample table box.
     *
     * @param channel the channel.
     * @param box     the box.
     * @throws IOException if the operation cannot be completed.
     */
    private void parseSampleTableBox(SeekableByteChannel channel, Box box) throws IOException {
        parseSampleDescriptionBox(channel, findBox(channel, box.end, SampleDescriptionBoxType));
        parseSampleSizeBox(channel, findBox(channel, box.end, SampleSizeBoxType));
        channel.position(box.end);
    }

    /**
     * Parses the sample description box.
     *
     * @param channel the channel.
     * @param box     the box.
     * @throws IOException if the operation cannot be completed.
     */
    private void parseSampleDescriptionBox(SeekableByteChannel channel, Box box) throws IOException {
        skip(channel, 8);
        parseVisualSampleEntry(channel, nextBox(channel, box.end));
        channel.position(box.end);
    }

    /**
     * Parses the visual sample entry.
     *
     * @param channel the channel.
     * @param box     the box.
     * @throws IOException if the operation cannot be completed.
     */
    private void parseVisualSampleEntry(SeekableByteChannel channel, Box box) throws IOException {
        switch (box.type) {
            case VisualSampleEntryTypeAVC1:
                parseVisualSampleEntryHeader(channel, box);
                parseAvcConfigurationBox(channel, findBox(channel, box.end, AvcConfigurationBoxType));
                break;

            case VisualSampleEntryTypeHVC1:
                parseVisualSampleEntryHeader(channel, box);
                parseHevcConfigurationBox(channel, findBox(channel, box.end, HevcConfigurationBoxType));
                break;
        }
        channel.position(box.end);
    }

    /**
     * Parses the visual sample entry header.
     *
     * @param channel the channel.
     * @param box     the box.
     * @throws IOException if the operation cannot be completed.
     */
    private void parseVisualSampleEntryHeader(SeekableByteChannel channel, Box box) throws IOException {
        visualSampleEntryType = box.type;
        skip(channel, 24);
        int val = readInt(channel);
        columns = val >>> 16;
        rows = val & 0xffff;
        skip(channel, 50);
    }

    /**
     * Parses the avc configuration box.
     *
     * @param channel the channel.
     * @param box     the box.
     * @throws IOException if the operation cannot be completed.
     */
    private void parseAvcConfigurationBox(SeekableByteChannel channel, Box box) throws IOException {
        int val = readInt(channel);
        configurationVersion = val >>> 24;
        profile_idc = (val >> 16) & 0xff;
        level_idc = val & 0xff;
        channel.position(box.end);
    }

    /**
     * Parses the hevc configuration box.
     *
     * @param channel the channel.
     * @param box     the box.
     * @throws IOException if the operation cannot be completed.
     */
    private void parseHevcConfigurationBox(SeekableByteChannel channel, Box box) throws IOException {
        int val = readShort(channel);
        configurationVersion = val >>> 8;
        profile_idc = val & 0x1F;
        skip(channel, 10);
        level_idc = readByte(channel) & 0xff;
        channel.position(box.end);
    }

    /**
     * Parses the sample size box.
     *
     * @param channel the channel.
     * @param box     the box.
     * @throws IOException if the operation cannot be completed.
     */
    private void parseSampleSizeBox(SeekableByteChannel channel, Box box) throws IOException {
        skip(channel, 8);
        numFrames = readInt(channel);
        fp1000s = (int) ((numFrames * 1000L * timescale + (duration >> 1)) / duration);
        channel.position(box.end);
    }

    /**
     * Represents the Box type.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    private static class Box {

        /**
         * The type value.
         */
        final int type;

        /**
         * The end value.
         */
        final long end;

        /**
         * Creates a new instance.
         *
         * @param type the type.
         * @param end  the end.
         */
        Box(int type, long end) {
            this.type = type;
            this.end = end;
        }

    }

}
