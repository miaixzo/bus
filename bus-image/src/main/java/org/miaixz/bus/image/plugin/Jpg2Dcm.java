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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

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

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class Jpg2Dcm {

    private static final int BUFFER_SIZE = 8162;
    private static final ElementDictionary DICT = ElementDictionary.getStandardElementDictionary();

    private static final int[] IUID_TAGS = { Tag.StudyInstanceUID, Tag.SeriesInstanceUID, Tag.SOPInstanceUID };

    private static final int[] TYPE2_TAGS = { Tag.ContentDate, Tag.ContentTime };
    private final Attributes staticMetadata = new Attributes();
    private final byte[] buf = new byte[BUFFER_SIZE];
    private boolean noAPPn;
    private boolean photo;
    private String tsuid;
    private ContentType contentType;
    private long fragmentLength = 4294967294L; // 2^32-2;

    private static void supplementMissingUIDs(Attributes metadata) {
        for (int tag : IUID_TAGS)
            if (!metadata.containsValue(tag))
                metadata.setString(tag, VR.UI, UID.createUID());
    }

    private static void supplementMissingValue(Attributes metadata, int tag, String value) {
        if (!metadata.containsValue(tag))
            metadata.setString(tag, DICT.vrOf(tag), value);
    }

    private static void supplementType2Tags(Attributes metadata) {
        for (int tag : TYPE2_TAGS)
            if (!metadata.contains(tag))
                metadata.setNull(tag, DICT.vrOf(tag));
    }

    private void setNoAPPn(boolean noAPPn) {
        this.noAPPn = noAPPn;
    }

    private void setPhoto(boolean photo) {
        this.photo = photo;
    }

    private void setTSUID(String tsuid) {
        this.tsuid = tsuid;
    }

    public void setContentType(String s) {
        ContentType contentType = ContentType.of(s);
        if (contentType == null)
            throw new IllegalArgumentException(s);
        this.contentType = contentType;
    }

    public void setFragmentLength(long fragmentLength) {
        if (fragmentLength < 1024 || fragmentLength > 4294967294L)
            throw new IllegalArgumentException("Maximal Fragment Length must be in the range of [1024, 4294967294].");
        this.fragmentLength = fragmentLength & ~1;
    }

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

    private void convert(Path srcFilePath, Path destFilePath) throws Exception {
        ContentType contentType = this.contentType;
        if (contentType == null) {
            String probeContentType = Files.probeContentType(srcFilePath);
            contentType = ContentType.of(probeContentType);
        }
        Attributes fileMetadata = SAXReader.parse(IoKit.openFileOrURL(contentType.getSampleMetadataFile(photo)));
        fileMetadata.addAll(staticMetadata);
        supplementMissingValue(fileMetadata, Tag.SOPClassUID, contentType.getSOPClassUID(photo));
        try (SeekableByteChannel channel = Files.newByteChannel(srcFilePath);
                ImageOutputStream dos = new ImageOutputStream(destFilePath.toFile())) {
            XPEGParser parser = contentType.newParser(channel);
            parser.getAttributes(fileMetadata);
            byte[] prefix = new byte[] {};
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

    private void copy(ByteChannel in, long len, OutputStream out) throws IOException {
        ByteBuffer bb = ByteBuffer.wrap(buf);
        int read;
        while (len > 0) {
            bb.position(0);
            bb.limit((int) Math.min(len, buf.length));
            read = in.read(bb);
            out.write(buf, 0, read);
            len -= read;
        }
    }

    private enum ContentType {
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
        VIDEO_MPEG {
            @Override
            XPEGParser newParser(SeekableByteChannel channel) throws IOException {
                return new MPEG2Parser(channel);
            }
        },
        VIDEO_MP4 {
            @Override
            XPEGParser newParser(SeekableByteChannel channel) throws IOException {
                return new MP4Parser(channel);
            }
        };

        static ContentType of(String type) {
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

        String getSampleMetadataFile(boolean photo) {
            return "resource:vlPhotographicImageMetadata.xml";
        }

        String getSOPClassUID(boolean photo) {
            return UID.VideoPhotographicImageStorage.uid;
        }

        abstract XPEGParser newParser(SeekableByteChannel channel) throws IOException;
    }

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
