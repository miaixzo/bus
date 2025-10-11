/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.image.plugin;

import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.ElementDictionary;
import org.miaixz.bus.image.galaxy.data.VR;
import org.miaixz.bus.image.galaxy.io.ImageOutputStream;
import org.miaixz.bus.image.galaxy.io.SAXReader;
import org.miaixz.bus.image.nimble.codec.XPEGParser;
import org.miaixz.bus.image.nimble.codec.jpeg.JPEG;
import org.miaixz.bus.image.nimble.codec.jpeg.JPEGParser;
import org.miaixz.bus.image.nimble.codec.mp4.MP4Parser;
import org.miaixz.bus.image.nimble.codec.mpeg.MPEG2Parser;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

/**
 * The {@code Jpg2Dcm} class provides functionality to encapsulate JPEG, MPEG, and MP4 files into a DICOM file format.
 * It reads the source media file, combines it with metadata, and writes a new DICOM file with the media data
 * encapsulated in the Pixel Data element.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Jpg2Dcm {

    /**
     * The buffer size for copying data.
     */
    private static final int BUFFER_SIZE = 8162;
    /**
     * The standard DICOM element dictionary.
     */
    private static final ElementDictionary DICT = ElementDictionary.getStandardElementDictionary();

    /**
     * Tags for UIDs that need to be generated if missing.
     */
    private static final int[] IUID_TAGS = { Tag.StudyInstanceUID, Tag.SeriesInstanceUID, Tag.SOPInstanceUID };

    /**
     * Type 2 tags that need to be present, even if with a null value.
     */
    private static final int[] TYPE2_TAGS = { Tag.ContentDate, Tag.ContentTime };
    /**
     * A set of static metadata to be merged into every created DICOM object.
     */
    private final Attributes staticMetadata = new Attributes();
    /**
     * A buffer for file I/O operations.
     */
    private final byte[] buf = new byte[BUFFER_SIZE];
    /**
     * A flag to exclude APPn segments from JPEG streams.
     */
    private boolean noAPPn;
    /**
     * A flag to indicate if the content is a VL Photographic Image.
     */
    private boolean photo;
    /**
     * The target Transfer Syntax UID.
     */
    private String tsuid;
    /**
     * The content type of the source file (e.g., image/jpeg).
     */
    private ContentType contentType;
    /**
     * The maximum length for each fragment of the encapsulated pixel data.
     */
    private long fragmentLength = 4294967294L; // 2^32-2;

    /**
     * Supplements the metadata with UIDs for Study, Series, and SOP Instance if they are missing.
     *
     * @param metadata The attributes to supplement.
     */
    private static void supplementMissingUIDs(Attributes metadata) {
        for (int tag : IUID_TAGS)
            if (!metadata.containsValue(tag))
                metadata.setString(tag, VR.UI, UID.createUID());
    }

    /**
     * Supplements a missing attribute in the metadata with a default value.
     *
     * @param metadata The attributes to supplement.
     * @param tag      The tag of the attribute.
     * @param value    The default value to set if the attribute is missing.
     */
    private static void supplementMissingValue(Attributes metadata, int tag, String value) {
        if (!metadata.containsValue(tag))
            metadata.setString(tag, DICT.vrOf(tag), value);
    }

    /**
     * Ensures that all Type 2 tags are present in the metadata, setting them to null if absent.
     *
     * @param metadata The attributes to supplement.
     */
    private static void supplementType2Tags(Attributes metadata) {
        for (int tag : TYPE2_TAGS)
            if (!metadata.contains(tag))
                metadata.setNull(tag, DICT.vrOf(tag));
    }

    /**
     * Sets whether to strip APPn markers from JPEG files.
     *
     * @param noAPPn {@code true} to strip APPn markers.
     */
    private void setNoAPPn(boolean noAPPn) {
        this.noAPPn = noAPPn;
    }

    /**
     * Sets whether to create a VL Photographic Image Storage SOP Instance.
     *
     * @param photo {@code true} for VL Photographic Image, {@code false} for Secondary Capture.
     */
    private void setPhoto(boolean photo) {
        this.photo = photo;
    }

    /**
     * Sets the Transfer Syntax UID for the output DICOM file.
     *
     * @param tsuid The Transfer Syntax UID.
     */
    private void setTSUID(String tsuid) {
        this.tsuid = tsuid;
    }

    /**
     * Sets the content type of the source file.
     *
     * @param s The content type string (e.g., "image/jpeg").
     * @throws IllegalArgumentException if the content type is not supported.
     */
    public void setContentType(String s) {
        ContentType contentType = ContentType.of(s);
        if (contentType == null)
            throw new IllegalArgumentException("Unsupported content type: " + s);
        this.contentType = contentType;
    }

    /**
     * Sets the maximum length for each pixel data fragment.
     *
     * @param fragmentLength The maximum fragment length.
     * @throws IllegalArgumentException if the length is out of the valid range.
     */
    public void setFragmentLength(long fragmentLength) {
        if (fragmentLength < 1024 || fragmentLength > 4294967294L)
            throw new IllegalArgumentException("Maximal Fragment Length must be in the range of [1024, 4294967294].");
        this.fragmentLength = fragmentLength & ~1;
    }

    /**
     * Converts a list of source files/directories to DICOM files.
     *
     * @param args A list where the last element is the destination and the others are sources.
     * @throws Exception if an error occurs during conversion.
     */
    private void convert(List<String> args) throws Exception {
        int argsSize = args.size();
        Path destPath = Paths.get(args.get(argsSize - 1));
        for (String src : args.subList(0, argsSize - 1)) {
            Path srcPath = Paths.get(src);
            if (Files.isDirectory(srcPath))
                Files.walkFileTree(srcPath, new Jpg2DcmFileVisitor(srcPath, destPath));
            else if (Files.isDirectory(destPath))
                convert(srcPath, destPath.resolve(srcPath.getFileName() + ".dcm"));
            else
                convert(srcPath, destPath);
        }
    }

    /**
     * Converts a single source file to a DICOM file.
     *
     * @param srcFilePath  The path to the source media file.
     * @param destFilePath The path for the destination DICOM file.
     * @throws Exception if an error occurs during conversion.
     */
    private void convert(Path srcFilePath, Path destFilePath) throws Exception {
        ContentType type = this.contentType;
        if (type == null) {
            String probeContentType = Files.probeContentType(srcFilePath);
            type = ContentType.of(probeContentType);
        }
        if (type == null) {
            throw new IOException("Cannot determine content type of " + srcFilePath);
        }

        Attributes fileMetadata = SAXReader.parse(IoKit.openFileOrURL(type.getSampleMetadataFile(photo)));
        fileMetadata.addAll(staticMetadata);
        supplementMissingValue(fileMetadata, Tag.SOPClassUID, type.getSOPClassUID(photo));
        try (SeekableByteChannel channel = Files.newByteChannel(srcFilePath);
                ImageOutputStream dos = new ImageOutputStream(destFilePath.toFile())) {
            XPEGParser parser = type.newParser(channel);
            parser.getAttributes(fileMetadata);
            byte[] prefix = {};
            if (noAPPn && parser.getPositionAfterAPPSegments() > 0) {
                channel.position(parser.getPositionAfterAPPSegments());
                prefix = new byte[] { (byte) 0xFF, (byte) JPEG.SOI };
            } else {
                channel.position(parser.getCodeStreamPosition());
            }
            long codeStreamSize = channel.size() - channel.position() + prefix.length;
            dos.writeDataset(
                    fileMetadata.createFileMetaInformation(
                            tsuid != null ? tsuid : parser.getTransferSyntaxUID(codeStreamSize > fragmentLength)),
                    fileMetadata);
            dos.writeHeader(Tag.PixelData, VR.OB, -1);
            dos.writeHeader(Tag.Item, null, 0);
            do {
                long len = Math.min(codeStreamSize, fragmentLength);
                dos.writeHeader(Tag.Item, null, (int) ((len + 1) & ~1));
                dos.write(prefix);
                copy(channel, len - prefix.length, dos);
                if ((len & 1) != 0)
                    dos.write(0);
                prefix = new byte[] {};
                codeStreamSize -= len;
            } while (codeStreamSize > 0);
            dos.writeHeader(Tag.SequenceDelimitationItem, null, 0);
        }
    }

    /**
     * Copies a specified number of bytes from a channel to an output stream.
     *
     * @param in  The source byte channel.
     * @param len The number of bytes to copy.
     * @param out The destination output stream.
     * @throws IOException if an I/O error occurs.
     */
    private void copy(ByteChannel in, long len, OutputStream out) throws IOException {
        ByteBuffer bb = ByteBuffer.wrap(buf);
        int read;
        while (len > 0) {
            bb.clear();
            bb.limit((int) Math.min(len, buf.length));
            read = in.read(bb);
            out.write(buf, 0, read);
            len -= read;
        }
    }

    /**
     * An enumeration of supported content types for encapsulation.
     */
    private enum ContentType {

        /**
         * Represents JPEG image content.
         */
        IMAGE_JPEG {

            @Override
            String getSampleMetadataFile(boolean photo) {
                return photo ? "resource:vlPhotographicImageMetadata.xml"
                        : "resource:secondaryCaptureImageMetadata.xml";
            }

            @Override
            String getSOPClassUID(boolean photo) {
                return photo ? UID.VLPhotographicImageStorage.uid : UID.SecondaryCaptureImageStorage.uid;
            }

            @Override
            XPEGParser newParser(SeekableByteChannel channel) throws IOException {
                return new JPEGParser(channel);
            }
        },
        /**
         * Represents MPEG video content.
         */
        VIDEO_MPEG {

            @Override
            XPEGParser newParser(SeekableByteChannel channel) throws IOException {
                return new MPEG2Parser(channel);
            }
        },
        /**
         * Represents MP4 video content.
         */
        VIDEO_MP4 {

            @Override
            XPEGParser newParser(SeekableByteChannel channel) throws IOException {
                return new MP4Parser(channel);
            }
        };

        /**
         * Gets the ContentType enum constant for a given MIME type string.
         *
         * @param type The MIME type string.
         * @return The corresponding ContentType, or {@code null} if not supported.
         */
        static ContentType of(String type) {
            if (type == null)
                return null;
            switch (type.toLowerCase()) {
                case "image/jpeg":
                case "image/jp2":
                case "image/j2c":
                case "image/jph":
                case "image/jphc":
                    return ContentType.IMAGE_JPEG;

                case "video/mpeg":
                    return ContentType.VIDEO_MPEG;

                case "video/mp4":
                case "video/quicktime":
                    return ContentType.VIDEO_MP4;
            }
            return null;
        }

        /**
         * Gets the path to a sample metadata file for this content type.
         *
         * @param photo {@code true} if the content is a photographic image.
         * @return The resource path to the metadata file.
         */
        String getSampleMetadataFile(boolean photo) {
            return "resource:vlPhotographicImageMetadata.xml";
        }

        /**
         * Gets the appropriate SOP Class UID for this content type.
         *
         * @param photo {@code true} if the content is a photographic image.
         * @return The SOP Class UID.
         */
        String getSOPClassUID(boolean photo) {
            return UID.VideoPhotographicImageStorage.uid;
        }

        /**
         * Creates a new parser for this content type.
         *
         * @param channel The channel containing the media data.
         * @return A new {@link XPEGParser} instance.
         * @throws IOException if an I/O error occurs.
         */
        abstract XPEGParser newParser(SeekableByteChannel channel) throws IOException;
    }

    /**
     * A file visitor to recursively find and convert media files in a directory.
     */
    class Jpg2DcmFileVisitor extends SimpleFileVisitor<Path> {

        private final Path srcPath;
        private final Path destPath;

        Jpg2DcmFileVisitor(Path srcPath, Path destPath) {
            this.srcPath = srcPath;
            this.destPath = destPath;
        }

        @Override
        public FileVisitResult visitFile(Path srcFilePath, BasicFileAttributes attrs) throws IOException {
            Path destFilePath = resolveDestFilePath(srcFilePath);
            if (!Files.isDirectory(destFilePath))
                Files.createDirectories(destFilePath);
            try {
                convert(srcFilePath, destFilePath.resolve(srcFilePath.getFileName() + ".dcm"));
            } catch (SAXException | ParserConfigurationException e) {
                e.printStackTrace(System.out);
                return FileVisitResult.TERMINATE;
            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
            return FileVisitResult.CONTINUE;
        }

        private Path resolveDestFilePath(Path srcFilePath) {
            int srcPathNameCount = srcPath.getNameCount();
            int srcFilePathNameCount = srcFilePath.getNameCount() - 1;
            if (srcPathNameCount == srcFilePathNameCount)
                return destPath;

            return destPath.resolve(srcFilePath.subpath(srcPathNameCount, srcFilePathNameCount));
        }
    }

}
