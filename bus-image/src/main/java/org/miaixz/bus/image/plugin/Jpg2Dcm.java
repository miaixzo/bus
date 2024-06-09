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
package org.miaixz.bus.image.plugin;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.StreamKit;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.ElementDictionary;
import org.miaixz.bus.image.galaxy.data.VR;
import org.miaixz.bus.image.galaxy.io.ImageOutputStream;
import org.miaixz.bus.image.nimble.codec.jpeg.JPEG;
import org.miaixz.bus.image.nimble.codec.jpeg.JPEGHeader;
import org.miaixz.bus.image.nimble.codec.mpeg.MPEGHeader;

import java.io.*;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class Jpg2Dcm {

    private static final ElementDictionary DICT = ElementDictionary.getStandardElementDictionary();
    private static final int INIT_BUFFER_SIZE = 8192;
    private static final int MAX_BUFFER_SIZE = 10485768; // 10MiB
    private static final long MAX_FILE_SIZE = 0x7FFFFFFE;

    private static final int[] IUID_TAGS = {
            Tag.StudyInstanceUID,
            Tag.SeriesInstanceUID,
            Tag.SOPInstanceUID
    };

    private static final long[] DA_TM_TAGS = {
            Tag.ContentDateAndTime,
            Tag.InstanceCreationDateAndTime
    };

    private static final int[] TYPE2_TAGS = {
            Tag.StudyID,
            Tag.StudyDate,
            Tag.StudyTime,
            Tag.AccessionNumber,
            Tag.Manufacturer,
            Tag.ReferringPhysicianName,
            Tag.PatientID,
            Tag.PatientName,
            Tag.PatientBirthDate,
            Tag.PatientSex,
    };

    private Attributes metadata;
    private boolean noAPPn;
    private JPEGHeader jpegHeader;
    private byte[] buffer = {};
    private int headerLength;
    private long fileLength;
    private FileType inFileType;

    private static void supplementMissingUIDs(Attributes metadata) {
        for (int tag : IUID_TAGS)
            if (!metadata.containsValue(tag))
                metadata.setString(tag, VR.UI, UID.createUID());
    }

    private static void supplementMissingValue(Attributes metadata, int tag, String value) {
        if (!metadata.containsValue(tag))
            metadata.setString(tag, DICT.vrOf(tag), value);
    }

    private static void supplementMissingDateTime(Attributes metadata) {
        Date now = new Date();
        for (long tag : DA_TM_TAGS)
            if (!metadata.containsValue((int) (tag >>> Normal._32)))
                metadata.setDate(tag, now);
    }

    private static void supplementMissingType2(Attributes metadata) {
        for (int tag : TYPE2_TAGS)
            if (!metadata.contains(tag))
                metadata.setNull(tag, DICT.vrOf(tag));
    }

    private void setMetadata(Attributes metadata) {
        this.metadata = metadata;
    }

    private void setNoAPPn(boolean noAPPn) {
        this.noAPPn = noAPPn;
    }

    private void toFileType(File infile) {
        try {
            inFileType = FileType.valueOf(infile.getName());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(MessageFormat.format("invalid-file-ext", infile));
        }
    }

    private void convert(File infile, File outfile) throws IOException {
        fileLength = infile.length();
        if (fileLength > MAX_FILE_SIZE)
            throw new IllegalArgumentException(infile.getName());

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(infile))) {
            if (!parseHeader(inFileType, bis))
                throw new IOException(MessageFormat.format("failed-to-parse", inFileType, infile));

            int itemLen = (int) fileLength;
            try (ImageOutputStream dos = new ImageOutputStream(outfile)) {
                dos.writeDataset(metadata.createFileMetaInformation(inFileType.getTransferSyntaxUID()), metadata);
                dos.writeHeader(Tag.PixelData, VR.OB, -1);
                dos.writeHeader(Tag.Item, null, 0);
                if (null != jpegHeader && noAPPn) {
                    int offset = jpegHeader.offsetAfterAPP();
                    itemLen -= offset - 3;
                    dos.writeHeader(Tag.Item, null, (itemLen + 1) & ~1);
                    dos.write((byte) -1);
                    dos.write((byte) JPEG.SOI);
                    dos.write((byte) -1);
                    dos.write(buffer, offset, headerLength - offset);
                } else {
                    dos.writeHeader(Tag.Item, null, (itemLen + 1) & ~1);
                    dos.write(buffer, 0, headerLength);
                }
                IoKit.copy(bis, dos, buffer);
                if ((itemLen & 1) != 0)
                    dos.write(0);
                dos.writeHeader(Tag.SequenceDelimitationItem, null, 0);
            }
        }
    }

    private boolean parseHeader(FileType fileType, InputStream in) throws IOException {
        int grow = INIT_BUFFER_SIZE;
        while (headerLength == buffer.length && headerLength < MAX_BUFFER_SIZE) {
            buffer = Arrays.copyOf(buffer, grow += headerLength);
            headerLength += StreamKit.readAvailable(in, buffer, headerLength, buffer.length - headerLength);
            if (fileType.parseHeader(this)) {
                supplementMissingValue(metadata, Tag.SOPClassUID, fileType.getSOPClassUID());
                return true;
            }
        }
        return false;
    }

    private enum FileType {
        jpeg(UID.SecondaryCaptureImageStorage, UID.JPEGBaseline1) {
            @Override
            boolean parseHeader(Jpg2Dcm main) {
                return null != (main.jpegHeader = new JPEGHeader(main.buffer, JPEG.SOS)).toAttributes(main.metadata);
            }
        },
        mpeg(UID.VideoPhotographicImageStorage, UID.MPEG2) {
            @Override
            boolean parseHeader(Jpg2Dcm main) {
                return null != (new MPEGHeader(main.buffer).toAttributes(main.metadata, main.fileLength));
            }
        };

        private final String cuid;
        private final String tsuid;

        FileType(String cuid, String tsuid) {
            this.cuid = cuid;
            this.tsuid = tsuid;
        }

        public String getSOPClassUID() {
            return cuid;
        }

        public String getTransferSyntaxUID() {
            return tsuid;
        }

        abstract boolean parseHeader(Jpg2Dcm main);

    }

}
