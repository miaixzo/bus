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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * EXIF metadata read from a TIFF-structured block (the payload of a JPEG APP1 "Exif" segment, a PNG eXIf chunk or the
 * {@code IMAGE_METADATA_EXIF} block returned by OpenCV).
 *
 * <p>
 * The tags of IFD0 and of the Exif sub-IFD share one namespace; the GPS sub-IFD has its own, since its tag numbers
 * start at 0. The reader is lenient: an entry that is malformed or points outside the block is skipped, and a block
 * that cannot be read gives {@link #EMPTY}.
 *
 * @author Kimi Liu
 */
public final class ExifData {

    /**
     * The empty anatomic imaging region.
     */
    public static final ExifData EMPTY = new ExifData(Map.of(), Map.of());

    /**
     * TIFF tag pointing to the EXIF subdirectory.
     */
    static final int EXIF_IFD_POINTER = 0x8769;
    /**
     * TIFF tag pointing to the GPS subdirectory.
     */
    static final int GPS_IFD_POINTER = 0x8825;

    /**
     * Required TIFF header identifier.
     */
    private static final int TIFF_MAGIC = 42;
    /**
     * Size in bytes of one TIFF image file directory entry.
     */
    private static final int ENTRY_SIZE = 12;
    /**
     * Upper bound on entries read from an untrusted TIFF directory.
     */
    private static final int MAX_ENTRIES = 1000;
    /**
     * JPEG marker opening an image stream.
     */
    private static final int JPEG_SOI = 0xFFD8;
    /**
     * JPEG application marker carrying EXIF metadata.
     */
    private static final int JPEG_APP1 = 0xE1;
    /**
     * JPEG marker starting the compressed scan.
     */
    private static final int JPEG_SOS = 0xDA;
    /**
     * JPEG marker terminating an image stream.
     */
    private static final int JPEG_EOI = 0xD9;
    /**
     * EXIF signature preceding the TIFF payload in a JPEG APP1 segment.
     */
    private static final byte[] EXIF_HEADER = { 'E', 'x', 'i', 'f', 0, 0 };

    /**
     * Decoded TIFF and EXIF tags keyed by tag identifier.
     */
    private final Map<Integer, ExifValue> tags;
    /**
     * Decoded GPS subdirectory tags keyed by tag identifier.
     */
    private final Map<Integer, ExifValue> gpsTags;

    /**
     * Creates a parsed EXIF result from standard and GPS tag maps.
     *
     * @param tags    Decoded TIFF and EXIF tags keyed by tag identifier
     * @param gpsTags Decoded GPS subdirectory tags keyed by tag identifier
     */
    private ExifData(Map<Integer, ExifValue> tags, Map<Integer, ExifValue> gpsTags) {
        this.tags = tags;
        this.gpsTags = gpsTags;
    }

    /**
     * Parses a TIFF-structured EXIF block, starting with the byte order mark ("II" or "MM").
     *
     * @param tiff the block
     * @return the metadata, {@link #EMPTY} when the block is not a valid TIFF structure
     */
    public static ExifData parse(byte[] tiff) {
        if (tiff == null || tiff.length < 8) {
            return EMPTY;
        }
        ByteOrder order;
        if (tiff[0] == 'I' && tiff[1] == 'I') {
            order = ByteOrder.LITTLE_ENDIAN;
        } else if (tiff[0] == 'M' && tiff[1] == 'M') {
            order = ByteOrder.BIG_ENDIAN;
        } else {
            return EMPTY;
        }
        ByteBuffer buf = ByteBuffer.wrap(tiff).order(order);
        if (Short.toUnsignedInt(buf.getShort(2)) != TIFF_MAGIC) {
            return EMPTY;
        }

        Map<Integer, ExifValue> tags = new HashMap<>();
        Map<Integer, ExifValue> gpsTags = new HashMap<>();
        Set<Long> visited = new HashSet<>();
        readIfd(buf, Integer.toUnsignedLong(buf.getInt(4)), tags, visited);
        readSubIfd(buf, tags.get(EXIF_IFD_POINTER), tags, visited);
        readSubIfd(buf, tags.get(GPS_IFD_POINTER), gpsTags, visited);
        if (tags.isEmpty() && gpsTags.isEmpty()) {
            return EMPTY;
        }
        return new ExifData(Collections.unmodifiableMap(tags), Collections.unmodifiableMap(gpsTags));
    }

    /**
     * Reads the EXIF metadata of a JPEG file from its APP1 "Exif" segment. Only the marker segments before the image
     * data are read.
     *
     * @param jpegFile the JPEG file
     * @return the metadata, {@link #EMPTY} when the file is not a JPEG or has no EXIF segment
     * @throws IOException if the file cannot be read
     */
    public static ExifData readJpeg(Path jpegFile) throws IOException {
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(Files.newInputStream(jpegFile)))) {
            if (in.readUnsignedShort() != JPEG_SOI) {
                return EMPTY;
            }
            while (true) {
                if (in.readUnsignedByte() != 0xFF) {
                    return EMPTY; // Not at a marker: corrupted header
                }
                int marker = in.readUnsignedByte();
                while (marker == 0xFF) { // Fill bytes
                    marker = in.readUnsignedByte();
                }
                if (marker == JPEG_SOS || marker == JPEG_EOI) {
                    return EMPTY;
                }
                if (marker == 0x01 || (marker >= 0xD0 && marker <= 0xD7)) {
                    continue; // Standalone markers without length
                }
                int length = in.readUnsignedShort() - 2;
                if (length < 0) {
                    return EMPTY;
                }
                if (marker == JPEG_APP1 && length > EXIF_HEADER.length) {
                    byte[] segment = in.readNBytes(length);
                    if (segment.length == length
                            && Arrays.equals(segment, 0, EXIF_HEADER.length, EXIF_HEADER, 0, EXIF_HEADER.length)) {
                        return parse(Arrays.copyOfRange(segment, EXIF_HEADER.length, segment.length));
                    }
                } else {
                    in.skipNBytes(length);
                }
            }
        } catch (EOFException e) {
            return EMPTY;
        }
    }

    /**
     * Follows a validated EXIF or GPS subdirectory pointer without revisiting directories.
     *
     * @param buf     the TIFF buffer with the source byte order
     * @param pointer the TIFF value pointing to a subdirectory
     * @param target  the map receiving valid EXIF tags
     * @param visited the directory offsets already traversed to prevent cycles
     */
    private static void readSubIfd(
            ByteBuffer buf,
            ExifValue pointer,
            Map<Integer, ExifValue> target,
            Set<Long> visited) {
        if (pointer != null) {
            long[] offset = pointer.asLongs();
            if (offset.length > 0) {
                readIfd(buf, offset[0], target, visited);
            }
        }
    }

    /**
     * Parses bounded TIFF directory entries and preserves valid raw tag values.
     *
     * @param buf     the TIFF buffer with the source byte order
     * @param offset  the byte offset of the TIFF directory
     * @param target  the map receiving valid EXIF tags
     * @param visited the directory offsets already traversed to prevent cycles
     */
    private static void readIfd(ByteBuffer buf, long offset, Map<Integer, ExifValue> target, Set<Long> visited) {
        int limit = buf.limit();
        if (offset < 8 || offset + 2 > limit || !visited.add(offset)) {
            return;
        }
        int entries = Math.min(Short.toUnsignedInt(buf.getShort((int) offset)), MAX_ENTRIES);
        for (int i = 0; i < entries; i++) {
            long pos = offset + 2 + (long) i * ENTRY_SIZE;
            if (pos + ENTRY_SIZE > limit) {
                return;
            }
            int p = (int) pos;
            int tag = Short.toUnsignedInt(buf.getShort(p));
            int type = Short.toUnsignedInt(buf.getShort(p + 2));
            long count = Integer.toUnsignedLong(buf.getInt(p + 4));
            int size = ExifValue.componentSize(type);
            long length = size * count;
            if (size == 0 || count == 0 || length > limit) {
                continue;
            }
            long dataPos = length <= 4 ? p + 8L : Integer.toUnsignedLong(buf.getInt(p + 8));
            if (dataPos + length > limit) {
                continue;
            }
            byte[] data = new byte[(int) length];
            buf.get((int) dataPos, data);
            target.putIfAbsent(tag, new ExifValue(type, (int) count, data, buf.order()));
        }
    }

    /**
     * Returns a standard TIFF or EXIF subdirectory tag.
     *
     * @param tag the tag number, from IFD0 or the Exif sub-IFD
     * @return the value of the tag
     */
    public Optional<ExifValue> get(int tag) {
        return Optional.ofNullable(tags.get(tag));
    }

    /**
     * Returns a GPS subdirectory tag.
     *
     * @param tag the tag number in the GPS sub-IFD
     * @return the value of the tag
     */
    public Optional<ExifValue> getGps(int tag) {
        return Optional.ofNullable(gpsTags.get(tag));
    }

    /**
     * Tests whether neither standard nor GPS EXIF tags were decoded.
     *
     * @return whether both tag maps are empty
     */
    public boolean isEmpty() {
        return tags.isEmpty() && gpsTags.isEmpty();
    }

}
