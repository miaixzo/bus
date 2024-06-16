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
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.galaxy.DirReader;
import org.miaixz.bus.image.galaxy.DirWriter;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.ElementDictionary;
import org.miaixz.bus.image.galaxy.data.VR;
import org.miaixz.bus.image.galaxy.io.ImageEncodingOptions;
import org.miaixz.bus.image.galaxy.io.ImageInputStream;
import org.miaixz.bus.image.galaxy.media.RecordFactory;
import org.miaixz.bus.image.galaxy.media.RecordType;
import org.miaixz.bus.logger.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.regex.Pattern;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class DcmDir {

    /**
     * default number of characters per line
     */
    private static final int DEFAULT_WIDTH = 78;
    private static final ElementDictionary DICT = ElementDictionary.getStandardElementDictionary();
    private final FilesetInfo fsInfo = new FilesetInfo();
    private boolean inUse;
    private int width = DEFAULT_WIDTH;
    private ImageEncodingOptions encOpts = ImageEncodingOptions.DEFAULT;
    private boolean origSeqLength;
    private boolean checkDuplicate;

    private File file;
    private DirReader in;
    private DirWriter out;
    private RecordFactory recFact;

    private String csv;
    private String recordConfig;
    private char delim;
    private char quote;

    private int readCSVFile(int num) throws Exception {
        if (null != recordConfig) {
            loadCustomConfiguration();
        }
        try (BufferedReader br = new BufferedReader(new FileReader(csv))) {
            CSVParser parser = new CSVParser(delim, quote, br.readLine());
            String nextLine;
            while (null != (nextLine = br.readLine())) {
                checkOut();
                checkRecordFactory();
                Attributes dataset = parser.toDataset(nextLine);
                if (null != dataset) {
                    String iuid = dataset.getString(Tag.SOPInstanceUID);
                    char prompt = Symbol.C_DOT;
                    Attributes fmi = null;
                    if (null != iuid) {
                        fmi = dataset.createFileMetaInformation(UID.ImplicitVRLittleEndian);
                        prompt = 'F';
                    }
                    num = addRecords(dataset, num, null, prompt, iuid, fmi);
                }
            }
        }
        return num;
    }

    private void compact(File f, File bak) throws IOException {
        File tmp = File.createTempFile("DICOMDIR", null, f.getParentFile());
        DirReader r = new DirReader(f);
        try {
            fsInfo.setFilesetUID(r.getFileSetUID());
            fsInfo.setFilesetID(r.getFileSetID());
            fsInfo.setDescriptorFile(
                    r.getDescriptorFile());
            fsInfo.setDescriptorFileCharset(
                    r.getDescriptorFileCharacterSet());
            create(tmp);
            copyFrom(r);
        } finally {
            close();
            try {
                r.close();
            } catch (IOException ignore) {
            }
        }
        bak.delete();
        rename(f, bak);
        rename(tmp, f);
    }

    private void rename(File from, File to) throws IOException {
        if (!from.renameTo(to))
            throw new IOException(
                    MessageFormat.format("failed to rename {0} to {1}",
                            from, to));
    }

    private void copyFrom(DirReader r) throws IOException {
        Attributes rec = r.findFirstRootDirectoryRecordInUse(false);
        while (null != rec) {
            copyChildsFrom(r, rec,
                    out.addRootDirectoryRecord(new Attributes(rec)));
            rec = r.findNextDirectoryRecordInUse(rec, false);
        }
    }

    private void copyChildsFrom(DirReader r, Attributes src,
                                Attributes dst) throws IOException {
        Attributes rec = r.findLowerDirectoryRecordInUse(src, false);
        while (null != rec) {
            copyChildsFrom(r, rec,
                    out.addLowerDirectoryRecord(dst, new Attributes(rec)));
            rec = r.findNextDirectoryRecordInUse(rec, false);
        }
    }

    private File getFile() {
        return file;
    }

    private void setInUse(boolean inUse) {
        this.inUse = inUse;
    }

    private void setOriginalSequenceLength(boolean origSeqLength) {
        this.origSeqLength = origSeqLength;
    }

    private void setEncodingOptions(ImageEncodingOptions encOpts) {
        this.encOpts = encOpts;
    }

    private void setWidth(int width) {
        if (width < 40)
            throw new IllegalArgumentException();
        this.width = width;
    }

    private void setCheckDuplicate(boolean checkDuplicate) {
        this.checkDuplicate = checkDuplicate;
    }

    private void setRecordFactory(RecordFactory recFact) {
        this.recFact = recFact;
    }

    private void close() {
        IoKit.close(in);
        in = null;
        out = null;
    }

    private void openForReadOnly(File file) throws IOException {
        this.file = file;
        in = new DirReader(file);
    }

    private void create(File file) throws IOException {
        this.file = file;
        DirWriter.createEmptyDirectory(file,
                UID.createUIDIfNull(fsInfo.getFilesetUID()),
                fsInfo.getFilesetID(),
                fsInfo.getDescriptorFile(),
                fsInfo.getDescriptorFileCharset());
        in = out = DirWriter.open(file);
        out.setEncodingOptions(encOpts);
        setCheckDuplicate(false);
    }

    private void open(File file) throws IOException {
        this.file = file;
        in = out = DirWriter.open(file);
        if (!origSeqLength)
            out.setEncodingOptions(encOpts);
        setCheckDuplicate(true);
    }

    private void list() throws IOException {
        checkIn();
        list("File Meta Information:", in.getFileMetaInformation());
        list("File-set Information:", in.getFileSetInformation());
        list(inUse
                        ? in.findFirstRootDirectoryRecordInUse(false)
                        : in.readFirstRootDirectoryRecord(),
                new StringBuilder());
    }

    private void list(final String header, final Attributes attrs) {
        Logger.info(header);
        Logger.info(attrs.toString(Integer.MAX_VALUE, width));
    }

    private void list(Attributes rec, StringBuilder index)
            throws IOException {
        int indexLen = index.length();
        int i = 1;
        while (null != rec) {
            index.append(i++).append(Symbol.C_DOT);
            list(heading(rec, index), rec);
            list(inUse
                            ? in.findLowerDirectoryRecordInUse(rec, false)
                            : in.readLowerDirectoryRecord(rec),
                    index);
            rec = inUse
                    ? in.findNextDirectoryRecordInUse(rec, false)
                    : in.readNextDirectoryRecord(rec);
            index.setLength(indexLen);
        }
    }

    private String heading(Attributes rec, StringBuilder index) {
        int prefixLen = index.length();
        try {
            return index.append(Symbol.C_SPACE)
                    .append(rec.getString(Tag.DirectoryRecordType, Normal.EMPTY))
                    .append(Symbol.C_COLON).toString();
        } finally {
            index.setLength(prefixLen);
        }
    }

    private int addReferenceTo(File f) throws IOException {
        checkOut();
        checkRecordFactory();
        int n = 0;
        if (f.isDirectory()) {
            for (String s : f.list())
                n += addReferenceTo(new File(f, s));
            return n;
        }
        // do not add reference to DICOMDIR
        if (f.equals(file))
            return 0;

        Attributes fmi;
        Attributes dataset;
        ImageInputStream din = null;
        try {
            din = new ImageInputStream(f);
            din.setIncludeBulkData(ImageInputStream.IncludeBulkData.NO);
            fmi = din.readFileMetaInformation();
            dataset = din.readDataset(-1, Tag.PixelData);
        } catch (IOException e) {
            Logger.info(
                    MessageFormat.format("failed to parse {0}: {1}",
                            f, e.getMessage()));
            return 0;
        } finally {
            if (null != din)
                try {
                    din.close();
                } catch (Exception ignore) {
                }
        }
        char prompt = Symbol.C_DOT;
        if (null == fmi) {
            fmi = dataset.createFileMetaInformation(UID.ImplicitVRLittleEndian);
            prompt = 'F';
        }
        String iuid = fmi.getString(Tag.MediaStorageSOPInstanceUID, null);
        if (null == iuid) {
            Logger.info(MessageFormat.format("skip-file", f));
            return 0;
        }

        return addRecords(dataset, n, out.toFileIDs(f), prompt, iuid, fmi);
    }

    private int addRecords(Attributes dataset, int num, String[] fileIDs, char prompt, String iuid, Attributes fmi)
            throws IOException {
        String pid = dataset.getString(Tag.PatientID, null);
        String styuid = dataset.getString(Tag.StudyInstanceUID, null);
        String seruid = dataset.getString(Tag.SeriesInstanceUID, null);

        if (null != styuid) {
            if (null == pid) {
                dataset.setString(Tag.PatientID, VR.LO, pid = styuid);
                prompt = prompt == 'F' ? 'P' : 'p';
            }
            Attributes patRec = in.findPatientRecord(pid);
            if (null == patRec) {
                patRec = recFact.createRecord(RecordType.PATIENT, null,
                        dataset, null, null);
                out.addRootDirectoryRecord(patRec);
                num++;
            }
            Attributes studyRec = in.findStudyRecord(patRec, styuid);
            if (null == studyRec) {
                studyRec = recFact.createRecord(RecordType.STUDY, null,
                        dataset, null, null);
                out.addLowerDirectoryRecord(patRec, studyRec);
                num++;
            }

            if (null != seruid) {
                Attributes seriesRec = in.findSeriesRecord(studyRec, seruid);
                if (null == seriesRec) {
                    seriesRec = recFact.createRecord(RecordType.SERIES, null,
                            dataset, null, null);
                    out.addLowerDirectoryRecord(studyRec, seriesRec);
                    num++;
                }

                if (null != iuid) {
                    Attributes instRec;
                    if (checkDuplicate) {
                        instRec = in.findLowerInstanceRecord(seriesRec, false, iuid);
                        if (null != instRec) {
                            return 0;
                        }
                    }
                    instRec = recFact.createRecord(dataset, fmi, fileIDs);
                    out.addLowerDirectoryRecord(seriesRec, instRec);
                    num++;
                }
            }
        } else {
            if (null != iuid) {
                if (checkDuplicate) {
                    if (null != in.findRootInstanceRecord(false, iuid)) {
                        return 0;
                    }
                }
                Attributes instRec = recFact.createRecord(dataset, fmi, fileIDs);
                out.addRootDirectoryRecord(instRec);
                prompt = prompt == 'F' ? 'R' : 'r';
                num++;
            }
        }
        return num;
    }

    private int removeReferenceTo(File f) throws IOException {
        checkOut();
        int n = 0;
        if (f.isDirectory()) {
            for (String s : f.list())
                n += removeReferenceTo(new File(f, s));
            return n;
        }
        String pid;
        String styuid;
        String seruid;
        String iuid;
        ImageInputStream din = null;
        try {
            din = new ImageInputStream(f);
            din.setIncludeBulkData(ImageInputStream.IncludeBulkData.NO);
            Attributes fmi = din.readFileMetaInformation();
            Attributes dataset = din.readDataset(-1, Tag.StudyID);
            iuid = (null != fmi)
                    ? fmi.getString(Tag.MediaStorageSOPInstanceUID, null)
                    : dataset.getString(Tag.SOPInstanceUID, null);
            if (null == iuid) {
                Logger.info(MessageFormat.format("skip-file", f));
                return 0;
            }
            pid = dataset.getString(Tag.PatientID, null);
            styuid = dataset.getString(Tag.StudyInstanceUID, null);
            seruid = dataset.getString(Tag.SeriesInstanceUID, null);
        } catch (IOException e) {
            Logger.info(
                    MessageFormat.format("failed to parse {0}: {1}",
                            f, e.getMessage()));
            return 0;
        } finally {
            if (null != din)
                try {
                    din.close();
                } catch (Exception ignore) {
                }
        }
        Attributes instRec;
        if (null != styuid && null != seruid) {
            Attributes patRec = in.findPatientRecord(null == pid ? styuid : pid);
            if (null == patRec) {
                return 0;
            }
            Attributes studyRec = in.findStudyRecord(patRec, styuid);
            if (null == studyRec) {
                return 0;
            }
            Attributes seriesRec = in.findSeriesRecord(studyRec, seruid);
            if (null == seriesRec) {
                return 0;
            }
            instRec = in.findLowerInstanceRecord(seriesRec, false, iuid);
        } else {
            instRec = in.findRootInstanceRecord(false, iuid);
        }
        if (null == instRec) {
            return 0;
        }
        out.deleteRecord(instRec);
        return 1;
    }

    public void commit() throws IOException {
        checkOut();
        out.commit();
    }

    private int purge() throws IOException {
        checkOut();
        return out.purge();
    }

    private void checkIn() {
        if (null == in)
            throw new IllegalStateException("no open file");
    }

    private void checkOut() {
        checkIn();
        if (null == out)
            throw new IllegalStateException("file opened for read-only");
    }

    private void checkRecordFactory() {
        if (null == recFact)
            throw new IllegalStateException("no Record Factory initialized");
    }

    private void loadCustomConfiguration() {
        try {
            recFact.loadConfiguration(Paths.get(recordConfig).toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static class CSVParser {
        private final Pattern pattern;
        private final int[] tags;
        private final VR[] vrs;
        private final char quot;

        CSVParser(char delim, char quote, String header) {
            quot = quote;
            String regex = delim + "(?=(?:[^//" + quot + "]*//" + quot + "[^//" + quot + "]*//" + quot + ")*[^//" + quot + "]*$)";
            pattern = Pattern.compile(regex);
            String[] headers = parseFields(header);
            tags = new int[headers.length];
            vrs = new VR[headers.length];
            for (int i = 0; i < headers.length; i++) {
                tags[i] = DICT.tagForKeyword(headers[i]);
                vrs[i] = DICT.vrOf(tags[i]);
            }
        }

        Attributes toDataset(String line) {
            Attributes dataset = new Attributes();
            String[] fields = parseFields(line);
            if (fields.length > tags.length) {
                Logger.warn("Number of values in line " + line + " does not match number of headers. Hence line is ignored.");
                return null;
            }
            for (int i = 0; i < fields.length; i++)
                dataset.setString(tags[i], vrs[i], fields[i]);
            return dataset;
        }

        private String[] parseFields(String line) {
            String[] fields = pattern.split(line, -1);
            for (int i = 0; i < fields.length; i++)
                fields[i] = decode(fields[i]);
            return fields;
        }

        private String decode(String field) {
            char[] doubleQuote = new char[]{quot, quot};
            return !field.isEmpty() && field.charAt(0) == quot && field.charAt(field.length() - 1) == quot
                    ? field.substring(1, field.length() - 1)
                    .replace(String.valueOf(doubleQuote), String.valueOf(quot))
                    : field;
        }
    }

    public class FilesetInfo {

        private String uid;
        private String id;
        private File descFile;
        private String descFileCharset;

        public final String getFilesetUID() {
            return uid;
        }

        public final void setFilesetUID(String uid) {
            this.uid = uid;
        }

        public final String getFilesetID() {
            return id;
        }

        public final void setFilesetID(String id) {
            this.id = id;
        }

        public final File getDescriptorFile() {
            return descFile;
        }

        public final void setDescriptorFile(File descFile) {
            this.descFile = descFile;
        }

        public final String getDescriptorFileCharset() {
            return descFileCharset;
        }

        public final void setDescriptorFileCharset(String descFileCharset) {
            this.descFileCharset = descFileCharset;
        }

    }

}
