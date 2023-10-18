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
package org.aoju.bus.image;

import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.toolkit.StreamKit;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.image.galaxy.data.Attributes;
import org.aoju.bus.image.galaxy.data.BulkData;
import org.aoju.bus.image.galaxy.data.ElementDictionary;
import org.aoju.bus.image.galaxy.data.VR;
import org.aoju.bus.image.galaxy.io.ImageInputStream;
import org.aoju.bus.image.galaxy.io.ImageOutputStream;
import org.aoju.bus.image.metric.Progress;
import org.aoju.bus.image.nimble.codec.jpeg.JPEG;
import org.aoju.bus.image.nimble.codec.jpeg.JPEGHeader;
import org.aoju.bus.image.nimble.codec.mpeg.MPEGHeader;
import org.aoju.bus.logger.Logger;

import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

/**
 * 方法参数等构建器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Builder {

    public static final int APP_CONTEXT = 0x10;
    public static final int RQ_PRES_CONTEXT = 0x20;
    public static final int AC_PRES_CONTEXT = 0x21;
    public static final int ABSTRACT_SYNTAX = 0x30;
    public static final int TRANSFER_SYNTAX = 0x40;
    public static final int USER_INFO = 0x50;
    public static final int MAX_PDU_LENGTH = 0x51;
    public static final int IMPL_CLASS_UID = 0x52;
    public static final int ASYNC_OPS_WINDOW = 0x53;
    public static final int ROLE_SELECTION = 0x54;
    public static final int IMPL_VERSION_NAME = 0x55;
    public static final int EXT_NEG = 0x56;
    public static final int COMMON_EXT_NEG = 0x57;
    public static final int RQ_USER_IDENTITY = 0x58;
    public static final int AC_USER_IDENTITY = 0x59;
    public static final int MAGIC_LEN = 0xfbfb;

    public final static int KNOWN_INCONSISTENCIES = 0xFFFF;
    public final static int NO_KNOWN_INCONSISTENCIES = 0;
    public final static int IN_USE = 0xFFFF;
    public final static int IN_ACTIVE = 0;

    public static final String FAILED = "FAILED";
    public static final String WARNING = "WARNING";
    public static final String COMPLETED = "COMPLETED";

    public static final int FILE_BUFFER = 4096;
    public static final String SegmentSequenceError = "100";
    public static final String RequiredFieldMissing = "101";
    public static final String DataTypeError = "102";
    public static final String TableValueNotFound = "103";
    public static final String UnsupportedMessageType = "200";
    public static final String UnsupportedEventCode = "201";
    public static final String UnsupportedProcessingID = "202";
    public static final String UnsupportedVersionID = "203";
    public static final String UnknownKeyIdentifier = "204";
    public static final String DuplicateKeyIdentifier = "205";
    public static final String ApplicationRecordLocked = "206";
    public static final String ApplicationInternalError = "207";
    public static final String UnknownSendingApplication = "MSH^1^3";
    public static final String UnknownSendingFacility = "MSH^1^4";
    public static final String UnknownReceivingApplication = "MSH^1^5";
    public static final String UnknownReceivingFacility = "MSH^1^6";
    public static final int MAX_PACKAGE_LEN = 0x10000;
    public static final int A_ASSOCIATE_RQ = 0x01;
    public static final int A_ASSOCIATE_AC = 0x02;
    public static final int A_ASSOCIATE_RJ = 0x03;
    public static final int P_DATA_TF = 0x04;
    public static final int A_RELEASE_RQ = 0x05;
    public static final int A_RELEASE_RP = 0x06;
    public static final int A_ABORT = 0x07;
    public static final int DATA = 0;
    public static final int COMMAND = 1;
    public static final int PENDING = 0;
    public static final int LAST = 2;
    public static final String IMAGE_ORIGINAL_SUFFIX = ".dcm";
    public static final String IMAGE_CONVERT_SUFFIX = ".jpg";
    private static final int INIT_BUFFER_SIZE = 8192;
    private static final int MAX_BUFFER_SIZE = 10485768;

    public static String toUID(String uid) {
        uid = uid.trim();
        return Symbol.STAR.equals(uid) || Character.isDigit(uid.charAt(0)) ? uid : UID.forName(uid);
    }

    public static String[] toUIDs(String s) {
        if (Symbol.STAR.equals(s)) {
            return new String[]{Symbol.STAR};
        }

        String[] uids = StringKit.splitToArray(s, Symbol.COMMA);
        for (int i = 0; i < uids.length; i++) {
            uids[i] = toUID(uids[i]);
        }
        return uids;
    }

    public static void close(ImageInputStream in) {
        if (null != in) {
            for (File file : in.getBulkDataFiles()) {
                Builder.delete(file);
            }
        }
    }

    public static void close(final AutoCloseable object) {
        if (null != object) {
            try {
                object.close();
            } catch (Exception e) {
                Logger.error("Cannot close AutoCloseable", e);
            }
        }
    }

    public static void shutdown(ExecutorService executorService) {
        if (null != executorService) {
            try {
                executorService.shutdown();
            } catch (Exception e) {
                Logger.error("ExecutorService shutdown", e);
            }
        }
    }

    public static void forceGettingAttributes(Status dcmState, AutoCloseable closeable) {
        Progress p = dcmState.getProgress();
        if (null != p) {
            Builder.close(closeable);
        }
    }

    public static void getAllFilesInDirectory(File directory, List<File> files, boolean recursive) {
        File[] fList = directory.listFiles();
        for (File f : fList) {
            if (f.isFile()) {
                files.add(f);
            } else if (recursive && f.isDirectory()) {
                getAllFilesInDirectory(f, files, recursive);
            }
        }
    }

    private static boolean deleteFile(File fileOrDirectory) {
        try {
            Files.delete(fileOrDirectory.toPath());
        } catch (Exception e) {
            Logger.error("Cannot delete", e);
            return false;
        }
        return true;
    }

    public static boolean delete(File fileOrDirectory) {
        if (null == fileOrDirectory || !fileOrDirectory.exists()) {
            return false;
        }

        if (fileOrDirectory.isDirectory()) {
            final File[] files = fileOrDirectory.listFiles();
            if (null != files) {
                for (File child : files) {
                    delete(child);
                }
            }
        }
        return deleteFile(fileOrDirectory);
    }

    public static void prepareToWriteFile(File file) throws IOException {
        if (!file.exists()) {
            // 检查尚不存在的文件
            // 创建一个新文件。如果创建成功，则该文件可写
            File outputDir = file.getParentFile();
            // 需要检查是否存在，否则当dir存在时mkdirs()为false
            if (null != outputDir && !outputDir.exists() && !outputDir.mkdirs()) {
                throw new IOException("Cannot write parent directory of " + file.getPath());
            }
        }
    }

    public static String humanReadableByte(long bytes, boolean si) {
        int unit = si ? 1000 : Normal._1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? Normal.EMPTY : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public static void notify(Status state,
                              String iuid,
                              String cuid,
                              int intStatus,
                              String status,
                              int numberOfSuboperations) {
        state.setStatus(intStatus);
        Progress p = state.getProgress();
        if (null != p) {
            Attributes cmd = Optional.ofNullable(p.getAttributes()).orElseGet(Attributes::new);
            cmd.setInt(Tag.Status, VR.US, intStatus);
            cmd.setString(Tag.AffectedSOPInstanceUID, VR.UI, iuid);
            cmd.setString(Tag.AffectedSOPClassUID, VR.UI, cuid);
            notify(p, cmd, status, numberOfSuboperations);
            p.setAttributes(cmd);
        }
    }

    public static void notify(Progress progress,
                              Attributes attributes,
                              String status,
                              int numberOfSuboperations) {
        if (null != progress && null != attributes) {
            int c;
            int f;
            int r;
            int w;
            if (null == progress.getAttributes()) {
                c = 0;
                f = 0;
                w = 0;
                r = numberOfSuboperations;
            } else {
                c = progress.getNumberOfCompletedSuboperations();
                f = progress.getNumberOfFailedSuboperations();
                w = progress.getNumberOfWarningSuboperations();
                r = numberOfSuboperations - (c + f + w);
            }

            if (r < 1) {
                r = 1;
            }

            if (COMPLETED.equals(status)) {
                c++;
            } else if (FAILED.equals(status)) {
                f++;
            } else if (WARNING.equals(status)) {
                w++;
            }
            attributes.setInt(Tag.NumberOfCompletedSuboperations, VR.US, c);
            attributes.setInt(Tag.NumberOfFailedSuboperations, VR.US, f);
            attributes.setInt(Tag.NumberOfWarningSuboperations, VR.US, w);
            attributes.setInt(Tag.NumberOfRemainingSuboperations, VR.US, r - 1);
        }
    }

    public static void pdf(final Attributes attrs, File pdfFile, File dcmFile) throws IOException {
        attrs.setString(Tag.SOPClassUID, VR.UI, UID.EncapsulatedPDFStorage);
        ensureString(attrs, Tag.SpecificCharacterSet, VR.CS, "ISO_IR 192");// UTF-8
        ensureUID(attrs, Tag.StudyInstanceUID);
        ensureUID(attrs, Tag.SeriesInstanceUID);
        ensureUID(attrs, Tag.SOPInstanceUID);
        setCreationDate(attrs);

        BulkData bulk = new BulkData(pdfFile.toURI().toString(), 0, (int) pdfFile.length(), false);
        attrs.setValue(Tag.EncapsulatedDocument, VR.OB, bulk);
        attrs.setString(Tag.MIMETypeOfEncapsulatedDocument, VR.LO, "application/pdf");
        Attributes fmi = attrs.createFileMetaInformation(UID.ExplicitVRLittleEndian);
        try (ImageOutputStream dos = new ImageOutputStream(dcmFile)) {
            dos.writeDataset(fmi, attrs);
        }
    }

    public static void jpeg(final Attributes attrs, File jpgFile, File dcmFile, boolean noAPPn) {
        build(attrs, jpgFile, dcmFile, noAPPn, false);
    }

    public static void mpeg2(final Attributes attrs, File mpegFile, File dcmFile) {
        build(attrs, mpegFile, dcmFile, false, true);
    }

    private static void build(final Attributes attrs, File jpgFile, File dcmFile, boolean noAPPn, boolean mpeg) {
        Parameters p = new Parameters();
        p.fileLength = (int) jpgFile.length();

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(jpgFile))) {
            if (!readPixelHeader(p, attrs, bis, mpeg)) {
                throw new IOException("Cannot read the header of " + jpgFile.getPath());
            }

            int itemLen = p.fileLength;
            try (ImageOutputStream dos = new ImageOutputStream(dcmFile)) {
                ensureString(attrs, Tag.SpecificCharacterSet, VR.CS, "ISO_IR 192");// UTF-8
                ensureUID(attrs, Tag.StudyInstanceUID);
                ensureUID(attrs, Tag.SeriesInstanceUID);
                ensureUID(attrs, Tag.SOPInstanceUID);

                setCreationDate(attrs);

                dos.writeDataset(attrs.createFileMetaInformation(mpeg ? UID.MPEG2 : UID.JPEGBaseline1), attrs);
                dos.writeHeader(Tag.PixelData, VR.OB, -1);
                dos.writeHeader(Tag.Item, null, 0);
                if (null != p.jpegHeader && noAPPn) {
                    int offset = p.jpegHeader.offsetAfterAPP();
                    itemLen -= offset - 3;
                    dos.writeHeader(Tag.Item, null, (itemLen + 1) & ~1);
                    dos.write((byte) -1);
                    dos.write((byte) JPEG.SOI);
                    dos.write((byte) -1);
                    dos.write(p.buffer, offset, p.realBufferLength - offset);
                } else {
                    dos.writeHeader(Tag.Item, null, (itemLen + 1) & ~1);
                    dos.write(p.buffer, 0, p.realBufferLength);
                }
                StreamKit.copy(bis, dos, p.buffer);
                if ((itemLen & 1) != 0) {
                    dos.write(0);
                }
                dos.writeHeader(Tag.SequenceDelimitationItem, null, 0);
            }
        } catch (Exception e) {
            Logger.error("Building {}", mpeg ? "mpeg" : "jpg", e);
        }
    }

    private static boolean readPixelHeader(Parameters p, Attributes metadata, InputStream in, boolean mpeg) throws IOException {
        int grow = INIT_BUFFER_SIZE;
        while (p.realBufferLength == p.buffer.length && p.realBufferLength < MAX_BUFFER_SIZE) {
            grow += p.realBufferLength;
            p.buffer = Arrays.copyOf(p.buffer, grow);
            p.realBufferLength += StreamKit.readAvailable(in, p.buffer, p.realBufferLength, p.buffer.length - p.realBufferLength);
            boolean jpgHeader;
            if (mpeg) {
                MPEGHeader mpegHeader = new MPEGHeader(p.buffer);
                jpgHeader = null != mpegHeader.toAttributes(metadata, p.fileLength);
            } else {
                p.jpegHeader = new JPEGHeader(p.buffer, JPEG.SOS);
                jpgHeader = null != p.jpegHeader.toAttributes(metadata);
            }
            if (jpgHeader) {
                ensureString(metadata, Tag.SOPClassUID, VR.UI,
                        mpeg ? UID.VideoPhotographicImageStorage : UID.VLPhotographicImageStorage);
                return true;
            }
        }
        return false;
    }

    private static void setCreationDate(Attributes attrs) {
        Date now = new Date();
        attrs.setDate(Tag.InstanceCreationDate, VR.DA, now);
        attrs.setDate(Tag.InstanceCreationTime, VR.TM, now);
    }

    private static void ensureString(Attributes attrs, int tag, VR vr, String value) {
        if (!attrs.containsValue(tag)) {
            attrs.setString(tag, vr, value);
        }
    }

    private static void ensureUID(Attributes attrs, int tag) {
        if (!attrs.containsValue(tag)) {
            attrs.setString(tag, VR.UI, UID.createUID());
        }
    }

    public static boolean updateAttributes(Attributes data, Attributes attrs,
                                           String uidSuffix) {
        if (attrs.isEmpty() && null == uidSuffix)
            return false;
        if (null != uidSuffix) {
            data.setString(Tag.StudyInstanceUID, VR.UI,
                    data.getString(Tag.StudyInstanceUID) + uidSuffix);
            data.setString(Tag.SeriesInstanceUID, VR.UI,
                    data.getString(Tag.SeriesInstanceUID) + uidSuffix);
            data.setString(Tag.SOPInstanceUID, VR.UI,
                    data.getString(Tag.SOPInstanceUID) + uidSuffix);
        }
        data.update(Attributes.UpdatePolicy.OVERWRITE, attrs, null);
        return true;
    }

    public static int toTag(String tagOrKeyword) {
        try {
            return Integer.parseInt(tagOrKeyword, 16);
        } catch (IllegalArgumentException e) {
            int tag = ElementDictionary.tagForKeyword(tagOrKeyword, null);
            if (tag == -1)
                throw new IllegalArgumentException(tagOrKeyword);
            return tag;
        }
    }

    private static class Parameters {
        int realBufferLength = 0;
        byte[] buffer = {};
        int fileLength = 0;
        JPEGHeader jpegHeader;
    }

}
