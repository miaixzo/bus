package org.aoju.bus.image;

import org.aoju.bus.core.utils.StreamUtils;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.image.galaxy.data.Attributes;
import org.aoju.bus.image.galaxy.data.BulkData;
import org.aoju.bus.image.galaxy.data.VR;
import org.aoju.bus.image.galaxy.io.DicomInputStream;
import org.aoju.bus.image.galaxy.io.DicomOutputStream;
import org.aoju.bus.image.metric.params.DicomState;
import org.aoju.bus.image.metric.params.Progress;
import org.aoju.bus.image.nimble.codec.jpeg.JPEG;
import org.aoju.bus.image.nimble.codec.jpeg.JPEGHeader;
import org.aoju.bus.image.nimble.codec.mpeg.MPEGHeader;
import org.aoju.bus.logger.Logger;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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

    public static final String FAILED = "FAILED";
    public static final String WARNING = "WARNING";
    public static final String COMPLETED = "COMPLETED";


    public static final int FILE_BUFFER = 4096;
    private static final int INIT_BUFFER_SIZE = 8192;
    private static final int MAX_BUFFER_SIZE = 10485768;

    public static String[] toUIDs(String s) {
        if (s.equals("*")) {
            return new String[]{"*"};
        }

        String[] uids = (String[]) StringUtils.split(s, ',').toArray();
        for (int i = 0; i < uids.length; i++) {
            uids[i] = toUID(uids[i]);
        }
        return uids;
    }

    public static String toUID(String uid) {
        uid = uid.trim();
        return (uid.equals("*") || Character.isDigit(uid.charAt(0))) ? uid : UID.forName(uid);
    }

    public static void forceGettingAttributes(DicomState dcmState, AutoCloseable closeable) {
        Progress p = dcmState.getProgress();
        if (p != null) {
            Builder.close(closeable);
        }
    }

    public static void close(DicomInputStream in) {
        if (in != null) {
            for (File file : in.getBulkDataFiles()) {
                Builder.delete(file);
            }
        }
    }

    public static void close(final AutoCloseable object) {
        if (object != null) {
            try {
                object.close();
            } catch (Exception e) {
                Logger.error("Cannot close AutoCloseable", e);
            }
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
        if (fileOrDirectory == null || !fileOrDirectory.exists()) {
            return false;
        }

        if (fileOrDirectory.isDirectory()) {
            final File[] files = fileOrDirectory.listFiles();
            if (files != null) {
                for (File child : files) {
                    delete(child);
                }
            }
        }
        return deleteFile(fileOrDirectory);
    }


    public static void safeClose(XMLStreamWriter writer) {
        if (writer != null) {
            try {
                writer.close();
            } catch (XMLStreamException e) {
                Logger.error("Cannot close XMLStreamWriter", e);
            }
        }
    }

    public static void safeClose(XMLStreamReader xmler) {
        if (xmler != null) {
            try {
                xmler.close();
            } catch (XMLStreamException e) {
                Logger.error("Cannot close XMLStreamException", e);
            }
        }
    }

    public static void prepareToWriteFile(File file) throws IOException {
        if (!file.exists()) {
            // Check the file that doesn't exist yet.
            // Create a new file. The file is writable if the creation succeeds.
            File outputDir = file.getParentFile();
            // necessary to check exists otherwise mkdirs() is false when dir exists
            if (outputDir != null && !outputDir.exists() && !outputDir.mkdirs()) {
                throw new IOException("Cannot write parent directory of " + file.getPath());
            }
        }
    }


    public static String humanReadableByte(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");   //$NON-NLS-3$ //$NON-NLS-4$
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public static void notify(DicomState state,
                              String iuid,
                              String cuid,
                              int intStatus,
                              String status,
                              int numberOfSuboperations) {
        state.setStatus(intStatus);
        Progress p = state.getProgress();
        if (p != null) {
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
        if (progress != null && attributes != null) {
            int c;
            int f;
            int r;
            int w;
            if (progress.getAttributes() == null) {
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
        try (DicomOutputStream dos = new DicomOutputStream(dcmFile)) {
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
            try (DicomOutputStream dos = new DicomOutputStream(dcmFile)) {
                ensureString(attrs, Tag.SpecificCharacterSet, VR.CS, "ISO_IR 192");// UTF-8
                ensureUID(attrs, Tag.StudyInstanceUID);
                ensureUID(attrs, Tag.SeriesInstanceUID);
                ensureUID(attrs, Tag.SOPInstanceUID);

                setCreationDate(attrs);

                dos.writeDataset(attrs.createFileMetaInformation(mpeg ? UID.MPEG2 : UID.JPEGBaseline1), attrs);
                dos.writeHeader(Tag.PixelData, VR.OB, -1);
                dos.writeHeader(Tag.Item, null, 0);
                if (p.jpegHeader != null && noAPPn) {
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
                StreamUtils.copy(bis, dos, p.buffer);
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
            p.realBufferLength += StreamUtils.readAvailable(in, p.buffer, p.realBufferLength, p.buffer.length - p.realBufferLength);
            boolean jpgHeader;
            if (mpeg) {
                MPEGHeader mpegHeader = new MPEGHeader(p.buffer);
                jpgHeader = mpegHeader.toAttributes(metadata, p.fileLength) != null;
            } else {
                p.jpegHeader = new JPEGHeader(p.buffer, JPEG.SOS);
                jpgHeader = p.jpegHeader.toAttributes(metadata) != null;
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

    private static class Parameters {
        int realBufferLength = 0;
        byte[] buffer = {};
        int fileLength = 0;
        JPEGHeader jpegHeader;
    }

}
