/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
import org.miaixz.bus.image.builtin.FilesetInfo;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.ElementDictionary;
import org.miaixz.bus.image.galaxy.data.VR;
import org.miaixz.bus.image.galaxy.io.ImageEncodingOptions;
import org.miaixz.bus.image.galaxy.io.ImageInputStream;
import org.miaixz.bus.image.galaxy.media.ImageDirReader;
import org.miaixz.bus.image.galaxy.media.ImageDirWriter;
import org.miaixz.bus.image.galaxy.media.RecordFactory;
import org.miaixz.bus.image.galaxy.media.RecordType;
import org.miaixz.bus.logger.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.regex.Pattern;

/**
 * The {@code DcmDir} class provides functionalities to create, read, and manage DICOMDIR files. It supports adding and
 * removing references to DICOM files, listing directory records, and compacting the DICOMDIR file.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DcmDir {

    /**
     * Default number of characters per line for output.
     */
    private static final int DEFAULT_WIDTH = 78;
    /**
     * The standard DICOM element dictionary.
     */
    private static final ElementDictionary DICT = ElementDictionary.getStandardElementDictionary();
    /**
     * Information about the file-set.
     */
    private final FilesetInfo fsInfo = new FilesetInfo();
    /**
     * Flag to indicate if only records in use should be processed.
     */
    private boolean inUse;
    /**
     * The width for formatted output.
     */
    private int width = DEFAULT_WIDTH;
    /**
     * Encoding options for writing DICOM data.
     */
    private ImageEncodingOptions encOpts = ImageEncodingOptions.DEFAULT;
    /**
     * Flag to preserve the original sequence length.
     */
    private boolean origSeqLength;
    /**
     * Flag to check for duplicate instance references.
     */
    private boolean checkDuplicate;

    /**
     * The DICOMDIR file.
     */
    private File file;
    /**
     * Reader for the DICOMDIR.
     */
    private ImageDirReader in;
    /**
     * Writer for the DICOMDIR.
     */
    private ImageDirWriter out;
    /**
     * Factory for creating DICOMDIR records.
     */
    private RecordFactory recFact;

    /**
     * Path to a CSV file for importing records.
     */
    private String csv;
    /**
     * Delimiter character for the CSV file.
     */
    private char delim;
    /**
     * Quote character for the CSV file.
     */
    private char quote;

    /**
     * Reads a CSV file and adds the records to the DICOMDIR.
     *
     * @param num The initial number of records.
     * @return The updated number of records.
     * @throws Exception if an error occurs during file reading or parsing.
     */
    private int readCSVFile(int num) throws Exception {
        try (BufferedReader br = new BufferedReader(new FileReader(csv))) {
            CSVParser parser = new CSVParser(delim, quote, br.readLine());
            String nextLine;
            while ((nextLine = br.readLine()) != null) {
                checkOut();
                checkRecordFactory();
                Attributes dataset = parser.toDataset(nextLine);
                if (dataset != null) {
                    String iuid = dataset.getString(Tag.SOPInstanceUID);
                    char prompt = Symbol.C_DOT;
                    Attributes fmi = null;
                    if (iuid != null) {
                        fmi = dataset.createFileMetaInformation(UID.ImplicitVRLittleEndian.uid);
                        prompt = 'F';
                    }
                    num = addRecords(dataset, num, null, prompt, iuid, fmi);
                }
            }
        }
        return num;
    }

    /**
     * Compacts the DICOMDIR file by removing unused space.
     *
     * @param f   The original DICOMDIR file.
     * @param bak The backup file for the original DICOMDIR.
     * @throws IOException if an I/O error occurs.
     */
    private void compact(File f, File bak) throws IOException {
        File tmp = File.createTempFile("DICOMDIR", null, f.getParentFile());
        try (ImageDirReader r = new ImageDirReader(f)) {
            fsInfo.setFilesetUID(r.getFileSetUID());
            fsInfo.setFilesetID(r.getFileSetID());
            fsInfo.setDescriptorFile(r.getDescriptorFile());
            fsInfo.setDescriptorFileCharset(r.getDescriptorFileCharacterSet());
            create(tmp);
            copyFrom(r);
        } finally {
            close();
        }
        bak.delete();
        rename(f, bak);
        rename(tmp, f);
    }

    /**
     * Renames a file.
     *
     * @param from The source file.
     * @param to   The destination file.
     * @throws IOException if the rename operation fails.
     */
    private void rename(File from, File to) throws IOException {
        if (!from.renameTo(to)) {
            throw new IOException("Failed to rename " + from + " to " + to);
        }
    }

    /**
     * Copies all records from a given {@link ImageDirReader} to the current writer.
     *
     * @param r The source DICOMDIR reader.
     * @throws IOException if an I/O error occurs.
     */
    private void copyFrom(ImageDirReader r) throws IOException {
        Attributes rec = r.findFirstRootDirectoryRecordInUse(false);
        while (rec != null) {
            copyChildsFrom(r, rec, out.addRootDirectoryRecord(new Attributes(rec)));
            rec = r.findNextDirectoryRecordInUse(rec, false);
        }
    }

    /**
     * Recursively copies child records from a source record to a destination record.
     *
     * @param r   The source DICOMDIR reader.
     * @param src The source parent record.
     * @param dst The destination parent record.
     * @throws IOException if an I/O error occurs.
     */
    private void copyChildsFrom(ImageDirReader r, Attributes src, Attributes dst) throws IOException {
        Attributes rec = r.findLowerDirectoryRecordInUse(src, false);
        while (rec != null) {
            copyChildsFrom(r, rec, out.addLowerDirectoryRecord(dst, new Attributes(rec)));
            rec = r.findNextDirectoryRecordInUse(rec, false);
        }
    }

    /**
     * Gets the current DICOMDIR file.
     *
     * @return The file.
     */
    private File getFile() {
        return file;
    }

    /**
     * Sets whether to process only records that are in use.
     *
     * @param inUse {@code true} to process only in-use records.
     */
    private void setInUse(boolean inUse) {
        this.inUse = inUse;
    }

    /**
     * Sets whether to write sequences with their original explicit length.
     *
     * @param origSeqLength {@code true} to use original sequence length.
     */
    private void setOriginalSequenceLength(boolean origSeqLength) {
        this.origSeqLength = origSeqLength;
    }

    /**
     * Sets the encoding options for writing.
     *
     * @param encOpts The encoding options.
     */
    private void setEncodingOptions(ImageEncodingOptions encOpts) {
        this.encOpts = encOpts;
    }

    /**
     * Sets the output width for printing attributes.
     *
     * @param width The width in characters.
     * @throws IllegalArgumentException if width is less than 40.
     */
    private void setWidth(int width) {
        if (width < 40)
            throw new IllegalArgumentException("width must be >= 40");
        this.width = width;
    }

    /**
     * Sets whether to check for duplicate instance references when adding files.
     *
     * @param checkDuplicate {@code true} to enable duplicate checking.
     */
    private void setCheckDuplicate(boolean checkDuplicate) {
        this.checkDuplicate = checkDuplicate;
    }

    /**
     * Sets the record factory to be used for creating DICOMDIR records.
     *
     * @param recFact The record factory.
     */
    private void setRecordFactory(RecordFactory recFact) {
        this.recFact = recFact;
    }

    /**
     * Closes the DICOMDIR reader and writer.
     */
    private void close() {
        IoKit.close(in);
        in = null;
        out = null;
    }

    /**
     * Opens a DICOMDIR file for read-only access.
     *
     * @param file The DICOMDIR file.
     * @throws IOException if an I/O error occurs.
     */
    private void openForReadOnly(File file) throws IOException {
        this.file = file;
        in = new ImageDirReader(file);
    }

    /**
     * Creates a new, empty DICOMDIR file.
     *
     * @param file The file to create.
     * @throws IOException if an I/O error occurs.
     */
    private void create(File file) throws IOException {
        this.file = file;
        ImageDirWriter.createEmptyDirectory(
                file,
                UID.createUIDIfNull(fsInfo.getFilesetUID()),
                fsInfo.getFilesetID(),
                fsInfo.getDescriptorFile(),
                fsInfo.getDescriptorFileCharset());
        in = out = ImageDirWriter.open(file);
        out.setEncodingOptions(encOpts);
        setCheckDuplicate(false);
    }

    /**
     * Opens an existing DICOMDIR file for read-write access.
     *
     * @param file The DICOMDIR file.
     * @throws IOException if an I/O error occurs.
     */
    private void open(File file) throws IOException {
        this.file = file;
        in = out = ImageDirWriter.open(file);
        if (!origSeqLength)
            out.setEncodingOptions(encOpts);
        setCheckDuplicate(true);
    }

    /**
     * Lists the contents of the DICOMDIR to standard output.
     *
     * @throws IOException if an I/O error occurs.
     */
    private void list() throws IOException {
        checkIn();
        list("File Meta Information:", in.getFileMetaInformation());
        list("File-set Information:", in.getFileSetInformation());
        list(
                inUse ? in.findFirstRootDirectoryRecordInUse(false) : in.readFirstRootDirectoryRecord(),
                new StringBuilder());
    }

    /**
     * Prints a formatted list of attributes.
     *
     * @param header The header to print before the attributes.
     * @param attrs  The attributes to print.
     */
    private void list(final String header, final Attributes attrs) {
        System.out.println(header);
        System.out.println(attrs.toString(Integer.MAX_VALUE, width));
    }

    /**
     * Recursively lists directory records.
     *
     * @param rec   The starting record.
     * @param index A {@link StringBuilder} to build the hierarchical index string.
     * @throws IOException if an I/O error occurs.
     */
    private void list(Attributes rec, StringBuilder index) throws IOException {
        int indexLen = index.length();
        int i = 1;
        while (rec != null) {
            index.append(i++).append(Symbol.C_DOT);
            list(heading(rec, index), rec);
            list(inUse ? in.findLowerDirectoryRecordInUse(rec, false) : in.readLowerDirectoryRecord(rec), index);
            rec = inUse ? in.findNextDirectoryRecordInUse(rec, false) : in.readNextDirectoryRecord(rec);
            index.setLength(indexLen);
        }
    }

    /**
     * Creates a heading string for a directory record.
     *
     * @param rec   The directory record.
     * @param index The current hierarchical index.
     * @return The formatted heading string.
     */
    private String heading(Attributes rec, StringBuilder index) {
        int prefixLen = index.length();
        try {
            return index.append(Symbol.C_SPACE).append(rec.getString(Tag.DirectoryRecordType, Normal.EMPTY))
                    .append(Symbol.C_COLON).toString();
        } finally {
            index.setLength(prefixLen);
        }
    }

    /**
     * Adds a reference to a DICOM file or all files in a directory to the DICOMDIR.
     *
     * @param f The file or directory to add.
     * @return The number of records added.
     * @throws IOException if an I/O error occurs.
     */
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
        try (ImageInputStream din = new ImageInputStream(f)) {
            din.setIncludeBulkData(ImageInputStream.IncludeBulkData.NO);
            fmi = din.readFileMetaInformation();
            dataset = din.readDatasetUntilPixelData();
        } catch (IOException e) {
            return 0;
        }
        char prompt = Symbol.C_DOT;
        if (fmi == null) {
            fmi = dataset.createFileMetaInformation(UID.ImplicitVRLittleEndian.uid);
            prompt = 'F';
        }
        String iuid = fmi.getString(Tag.MediaStorageSOPInstanceUID, null);
        if (iuid == null) {
            return 0;
        }

        return addRecords(dataset, n, out.toFileIDs(f), prompt, iuid, fmi);
    }

    /**
     * Adds the necessary directory records for a given DICOM instance.
     *
     * @param dataset The dataset of the DICOM instance.
     * @param num     The current count of added records.
     * @param fileIDs The file IDs for the referenced file.
     * @param prompt  A character to indicate the status.
     * @param iuid    The SOP Instance UID.
     * @param fmi     The File Meta Information.
     * @return The updated count of added records.
     * @throws IOException if an I/O error occurs.
     */
    private int addRecords(Attributes dataset, int num, String[] fileIDs, char prompt, String iuid, Attributes fmi)
            throws IOException {
        String pid = dataset.getString(Tag.PatientID, null);
        String styuid = dataset.getString(Tag.StudyInstanceUID, null);
        String seruid = dataset.getString(Tag.SeriesInstanceUID, null);

        if (styuid != null) {
            if (pid == null) {
                dataset.setString(Tag.PatientID, VR.LO, pid = styuid);
                prompt = prompt == 'F' ? 'P' : 'p';
            }
            Attributes patRec = in.findPatientRecord(pid);
            if (patRec == null) {
                patRec = recFact.createRecord(RecordType.PATIENT, null, dataset, null, null);
                out.addRootDirectoryRecord(patRec);
                num++;
            }
            Attributes studyRec = in.findStudyRecord(patRec, styuid);
            if (studyRec == null) {
                studyRec = recFact.createRecord(RecordType.STUDY, null, dataset, null, null);
                out.addLowerDirectoryRecord(patRec, studyRec);
                num++;
            }

            if (seruid != null) {
                Attributes seriesRec = in.findSeriesRecord(studyRec, seruid);
                if (seriesRec == null) {
                    seriesRec = recFact.createRecord(RecordType.SERIES, null, dataset, null, null);
                    out.addLowerDirectoryRecord(studyRec, seriesRec);
                    num++;
                }

                if (iuid != null) {
                    Attributes instRec;
                    if (checkDuplicate) {
                        instRec = in.findLowerInstanceRecord(seriesRec, false, iuid);
                        if (instRec != null) {
                            System.out.print('-');
                            return 0;
                        }
                    }
                    instRec = recFact.createRecord(dataset, fmi, fileIDs);
                    out.addLowerDirectoryRecord(seriesRec, instRec);
                    num++;
                }
            }
        } else {
            if (iuid != null) {
                if (checkDuplicate) {
                    if (in.findRootInstanceRecord(false, iuid) != null) {
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

    /**
     * Removes the reference to a DICOM file or all files in a directory from the DICOMDIR.
     *
     * @param f The file or directory to remove.
     * @return The number of records removed.
     * @throws IOException if an I/O error occurs.
     */
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
        try (ImageInputStream din = new ImageInputStream(f)) {
            din.setIncludeBulkData(ImageInputStream.IncludeBulkData.NO);
            Attributes fmi = din.readFileMetaInformation();
            Attributes dataset = din.readDataset(o -> o.tag() > Tag.SeriesInstanceUID);
            iuid = (fmi != null) ? fmi.getString(Tag.MediaStorageSOPInstanceUID, null)
                    : dataset.getString(Tag.SOPInstanceUID, null);
            if (iuid == null) {
                return 0;
            }
            pid = dataset.getString(Tag.PatientID, null);
            styuid = dataset.getString(Tag.StudyInstanceUID, null);
            seruid = dataset.getString(Tag.SeriesInstanceUID, null);
        } catch (IOException e) {
            return 0;
        }
        Attributes instRec;
        if (styuid != null && seruid != null) {
            Attributes patRec = in.findPatientRecord(pid == null ? styuid : pid);
            if (patRec == null) {
                return 0;
            }
            Attributes studyRec = in.findStudyRecord(patRec, styuid);
            if (studyRec == null) {
                return 0;
            }
            Attributes seriesRec = in.findSeriesRecord(studyRec, seruid);
            if (seriesRec == null) {
                return 0;
            }
            instRec = in.findLowerInstanceRecord(seriesRec, false, iuid);
        } else {
            instRec = in.findRootInstanceRecord(false, iuid);
        }
        if (instRec == null) {
            return 0;
        }
        out.deleteRecord(instRec);
        System.out.print('x');
        return 1;
    }

    /**
     * Commits all changes made to the DICOMDIR file.
     *
     * @throws IOException if an I/O error occurs.
     */
    public void commit() throws IOException {
        checkOut();
        out.commit();
    }

    /**
     * Purges all records marked as inactive from the DICOMDIR.
     *
     * @return The number of records purged.
     * @throws IOException if an I/O error occurs.
     */
    private int purge() throws IOException {
        checkOut();
        return out.purge();
    }

    /**
     * Checks if the DICOMDIR is open for reading.
     *
     * @throws IllegalStateException if no file is open.
     */
    private void checkIn() {
        if (in == null)
            throw new IllegalStateException("DICOMDIR not open");
    }

    /**
     * Checks if the DICOMDIR is open for writing.
     *
     * @throws IllegalStateException if the file is not open or is read-only.
     */
    private void checkOut() {
        checkIn();
        if (out == null)
            throw new IllegalStateException("DICOMDIR is read-only");
    }

    /**
     * Checks if a record factory has been configured.
     *
     * @throws IllegalStateException if the record factory is not set.
     */
    private void checkRecordFactory() {
        if (recFact == null)
            throw new IllegalStateException("No record factory configured");
    }

    /**
     * Loads a custom configuration for the record factory.
     *
     * @param recordConfig The path to the configuration file.
     * @throws RuntimeException if the configuration cannot be loaded.
     */
    private void loadCustomConfiguration(String recordConfig) {
        try {
            recFact.loadConfiguration(Paths.get(recordConfig).toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * A simple CSV parser to convert lines of a CSV file into DICOM attributes.
     */
    static class CSVParser {

        /**
         * The regex pattern to split CSV fields.
         */
        private final Pattern pattern;
        /**
         * The DICOM tags corresponding to the CSV columns.
         */
        private final int[] tags;
        /**
         * The DICOM VRs corresponding to the CSV columns.
         */
        private final VR[] vrs;
        /**
         * The quote character used in the CSV.
         */
        private final char quot;

        /**
         * Constructs a CSV parser.
         *
         * @param delim  The delimiter character.
         * @param quote  The quote character.
         * @param header The header line of the CSV file.
         */
        CSVParser(char delim, char quote, String header) {
            quot = quote;
            String regex = delim + "(?=(?:[^\\/" + quot + "]*\\/" + quot + "[^\\/" + quot + "]*\\/" + quot + ")*[^\\/"
                    + quot + "]*$)";
            pattern = Pattern.compile(regex);
            String[] headers = parseFields(header);
            tags = new int[headers.length];
            vrs = new VR[headers.length];
            for (int i = 0; i < headers.length; i++) {
                tags[i] = DICT.tagForKeyword(headers[i]);
                vrs[i] = DICT.vrOf(tags[i]);
            }
        }

        /**
         * Converts a line from the CSV file into a DICOM dataset.
         *
         * @param line The CSV line.
         * @return An {@link Attributes} object, or {@code null} if the line is invalid.
         */
        Attributes toDataset(String line) {
            Attributes dataset = new Attributes();
            String[] fields = parseFields(line);
            if (fields.length > tags.length) {
                Logger.warn(
                        "Number of values in line \"" + line
                                + "\" does not match number of headers. Hence line is ignored.");
                return null;
            }
            for (int i = 0; i < fields.length; i++)
                dataset.setString(tags[i], vrs[i], fields[i]);
            return dataset;
        }

        /**
         * Parses the fields from a single line of CSV.
         *
         * @param line The CSV line.
         * @return An array of field values.
         */
        private String[] parseFields(String line) {
            String[] fields = pattern.split(line, -1);
            for (int i = 0; i < fields.length; i++)
                fields[i] = decode(fields[i]);
            return fields;
        }

        /**
         * Decodes a single CSV field, handling quotes.
         *
         * @param field The field to decode.
         * @return The decoded field value.
         */
        private String decode(String field) {
            char[] doubleQuote = { quot, quot };
            return !field.isEmpty() && field.charAt(0) == quot && field.charAt(field.length() - 1) == quot
                    ? field.substring(1, field.length() - 1).replace(String.valueOf(doubleQuote), String.valueOf(quot))
                    : field;
        }
    }

}
