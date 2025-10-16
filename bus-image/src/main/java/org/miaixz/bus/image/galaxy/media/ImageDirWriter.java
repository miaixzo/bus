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
package org.miaixz.bus.image.galaxy.media;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import org.miaixz.bus.core.xyz.ByteKit;
import org.miaixz.bus.image.Builder;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.VR;
import org.miaixz.bus.image.galaxy.io.ImageEncodingOptions;
import org.miaixz.bus.image.galaxy.io.ImageOutputStream;
import org.miaixz.bus.image.galaxy.io.RAFOutputStreamAdapter;
import org.miaixz.bus.logger.Logger;

/**
 * A writer for DICOMDIR files, extending {@link ImageDirReader} to provide write capabilities. This class allows
 * adding, modifying, and deleting directory records within a DICOMDIR file, managing file set information, and handling
 * consistency flags.
 * 
 * @author Kimi Liu
 * @since Java 17+
 */
public class ImageDirWriter extends ImageDirReader {

    /**
     * Constant for FileSetConsistencyFlag (0004,1212) indicating known inconsistencies.
     */
    private final static int KNOWN_INCONSISTENCIES = 0xFFFF;
    /**
     * Constant for FileSetConsistencyFlag (0004,1212) indicating no known inconsistencies.
     */
    private final static int NO_KNOWN_INCONSISTENCIES = 0;
    /**
     * Constant for RecordInUseFlag (0004,1410) indicating a record is in use.
     */
    private final static int IN_USE = 0xFFFF;
    /**
     * Constant for RecordInUseFlag (0004,1410) indicating a record is inactive.
     */
    private final static int INACTIVE = 0;
    /**
     * Comparator for sorting {@link Attributes} based on their item position.
     */
    private static final Comparator<Attributes> offsetComparator = (item1, item2) -> {
        long d = item1.getItemPosition() - item2.getItemPosition();
        return d < 0 ? -1 : d > 0 ? 1 : 0;
    };
    /**
     * Byte array representing the header for Directory Information.
     */
    private final byte[] dirInfoHeader = { 0x04, 0x00, 0x00, 0x12, 'U', 'L', 4, 0, 0, 0, 0, 0, 0x04, 0x00, 0x02, 0x12,
            'U', 'L', 4, 0, 0, 0, 0, 0, 0x04, 0x00, 0x12, 0x12, 'U', 'S', 2, 0, 0, 0, 0x04, 0x00, 0x20, 0x12, 'S', 'Q',
            0, 0, 0, 0, 0, 0 };
    /**
     * Byte array representing the header for Directory Records.
     */
    private final byte[] dirRecordHeader = { 0x04, 0x00, 0x00, 0x14, 'U', 'L', 4, 0, 0, 0, 0, 0, 0x04, 0x00, 0x10, 0x14,
            'U', 'S', 2, 0, 0, 0, 0x04, 0x00, 0x20, 0x14, 'U', 'L', 4, 0, 0, 0, 0, 0 };
    /**
     * The {@link ImageOutputStream} used for writing to the DICOMDIR file.
     */
    private final ImageOutputStream out;
    /**
     * The file pointer position of the first directory record.
     */
    private final int firstRecordPos;
    /**
     * A list of dirty (modified) directory records that need to be written to the file.
     */
    private final List<Attributes> dirtyRecords = new ArrayList<>();
    /**
     * A map to keep track of the last child record added to a parent record, used for efficient appending.
     */
    private final IdentityHashMap<Attributes, Attributes> lastChildRecords = new IdentityHashMap<>();
    /**
     * The file pointer position where the next record will be written.
     */
    private int nextRecordPos;
    /**
     * The length of the file before any modifications, used for rollback.
     */
    private int rollbackLen = -1;

    /**
     * Constructs an {@code ImageDirWriter} for the specified DICOMDIR file.
     * 
     * @param file The DICOMDIR file to write to.
     * @throws IOException if an I/O error occurs.
     */
    private ImageDirWriter(File file) throws IOException {
        super(file, "rw");
        out = new ImageOutputStream(new RAFOutputStreamAdapter(raf), super.getTransferSyntaxUID());
        int seqLen = in.length();
        boolean undefSeqLen = seqLen <= 0;
        setEncodingOptions(new ImageEncodingOptions(false, undefSeqLen, false, undefSeqLen, false));
        this.nextRecordPos = this.firstRecordPos = (int) in.getPosition();
        if (!isEmpty()) {
            if (seqLen > 0)
                this.nextRecordPos += seqLen;
            else
                this.nextRecordPos = (int) (raf.length() - 8);
        }
        updateDirInfoHeader();
    }

    /**
     * Opens an existing DICOMDIR file for writing.
     * 
     * @param file The DICOMDIR file.
     * @return An {@code ImageDirWriter} instance.
     * @throws IOException if the file does not exist or an I/O error occurs.
     */
    public static ImageDirWriter open(File file) throws IOException {
        if (!file.isFile())
            throw new FileNotFoundException();

        return new ImageDirWriter(file);
    }

    /**
     * Creates an empty DICOMDIR file with the specified file set UID, ID, descriptor file, and character set.
     * 
     * @param file     The DICOMDIR file to create.
     * @param iuid     The File Set UID.
     * @param id       The File Set ID.
     * @param descFile The File Set Descriptor File.
     * @param charset  The Specific Character Set of File Set Descriptor File.
     * @throws IOException if an I/O error occurs.
     */
    public static void createEmptyDirectory(File file, String iuid, String id, File descFile, String charset)
            throws IOException {
        Attributes fmi = Attributes
                .createFileMetaInformation(iuid, UID.MediaStorageDirectoryStorage.uid, UID.ExplicitVRLittleEndian.uid);
        createEmptyDirectory(file, fmi, id, descFile, charset);
    }

    /**
     * Creates an empty DICOMDIR file with the specified File Meta Information, File Set ID, descriptor file, and
     * character set.
     * 
     * @param file     The DICOMDIR file to create.
     * @param fmi      The File Meta Information attributes.
     * @param id       The File Set ID.
     * @param descFile The File Set Descriptor File.
     * @param charset  The Specific Character Set of File Set Descriptor File.
     * @throws IOException if an I/O error occurs.
     */
    public static void createEmptyDirectory(File file, Attributes fmi, String id, File descFile, String charset)
            throws IOException {
        Attributes fsInfo = createFileSetInformation(file, id, descFile, charset);
        ImageOutputStream out = new ImageOutputStream(file);
        try {
            out.writeDataset(fmi, fsInfo);
        } finally {
            out.close();
        }
    }

    /**
     * Creates the File Set Information attributes for a new DICOMDIR file.
     * 
     * @param file     The DICOMDIR file.
     * @param id       The File Set ID.
     * @param descFile The File Set Descriptor File.
     * @param charset  The Specific Character Set of File Set Descriptor File.
     * @return The {@link Attributes} representing the File Set Information.
     */
    private static Attributes createFileSetInformation(File file, String id, File descFile, String charset) {
        Attributes fsInfo = new Attributes(7);
        fsInfo.setString(Tag.FileSetID, VR.CS, id);
        if (descFile != null) {
            fsInfo.setString(Tag.FileSetDescriptorFileID, VR.CS, toFileIDs(file, descFile));
            if (charset != null && !charset.isEmpty())
                fsInfo.setString(Tag.SpecificCharacterSetOfFileSetDescriptorFile, VR.CS, charset);
        }
        fsInfo.setInt(Tag.OffsetOfTheFirstDirectoryRecordOfTheRootDirectoryEntity, VR.UL, 0);
        fsInfo.setInt(Tag.OffsetOfTheLastDirectoryRecordOfTheRootDirectoryEntity, VR.UL, 0);
        fsInfo.setInt(Tag.FileSetConsistencyFlag, VR.US, 0);
        fsInfo.setNull(Tag.DirectoryRecordSequence, VR.SQ);
        return fsInfo;
    }

    /**
     * Converts a file path relative to the DICOMDIR file into an array of file IDs.
     * 
     * @param dfile The DICOMDIR file.
     * @param f     The file to get IDs for.
     * @return An array of strings representing the file IDs.
     * @throws IllegalArgumentException if the file is not within the DICOMDIR's directory.
     */
    private static String[] toFileIDs(File dfile, File f) {
        String dfilepath = dfile.getAbsolutePath();
        int dend = dfilepath.lastIndexOf(File.separatorChar) + 1;
        String dpath = dfilepath.substring(0, dend);
        String fpath = f.getAbsolutePath();
        if (dend == 0 || !fpath.startsWith(dpath))
            throw new IllegalArgumentException("file: " + fpath + " not in directory: " + dfile.getAbsoluteFile());
        return Builder.split(fpath.substring(dend), File.separatorChar);
    }

    /**
     * Returns the current {@link ImageEncodingOptions} used by the writer.
     * 
     * @return The encoding options.
     */
    public ImageEncodingOptions getEncodingOptions() {
        return out.getEncodingOptions();
    }

    /**
     * Sets the {@link ImageEncodingOptions} for the writer.
     * 
     * @param encOpts The encoding options to set.
     */
    public void setEncodingOptions(ImageEncodingOptions encOpts) {
        out.setEncodingOptions(encOpts);
    }

    /**
     * Adds a new root directory record to the DICOMDIR file.
     * 
     * @param rec The {@link Attributes} representing the new record.
     * @return The added record with its item position set.
     * @throws IOException if an I/O error occurs.
     */
    public synchronized Attributes addRootDirectoryRecord(Attributes rec) throws IOException {
        Attributes lastRootRecord = readLastRootDirectoryRecord();
        if (lastRootRecord == null) {
            writeRecord(firstRecordPos, rec);
            setOffsetOfFirstRootDirectoryRecord(firstRecordPos);
        } else {
            addRecord(Tag.OffsetOfTheNextDirectoryRecord, lastRootRecord, rec);
        }
        setOffsetOfLastRootDirectoryRecord((int) rec.getItemPosition());
        return rec;
    }

    /**
     * Adds a new lower-level directory record as a child of the specified parent record.
     * 
     * @param parentRec The parent {@link Attributes} record.
     * @param rec       The new child {@link Attributes} record.
     * @return The added record with its item position set.
     * @throws IOException if an I/O error occurs.
     */
    public synchronized Attributes addLowerDirectoryRecord(Attributes parentRec, Attributes rec) throws IOException {
        Attributes prevRec = lastChildRecords.get(parentRec);
        if (prevRec == null)
            prevRec = findLastLowerDirectoryRecord(parentRec);

        if (prevRec != null)
            addRecord(Tag.OffsetOfTheNextDirectoryRecord, prevRec, rec);
        else
            addRecord(Tag.OffsetOfReferencedLowerLevelDirectoryEntity, parentRec, rec);

        lastChildRecords.put(parentRec, rec);
        return rec;
    }

    /**
     * Finds an existing patient record by Patient ID, or adds a new one if not found.
     * 
     * @param rec The {@link Attributes} representing the patient record to find or add.
     * @return The existing or newly added patient record.
     * @throws IOException if an I/O error occurs.
     */
    public synchronized Attributes findOrAddPatientRecord(Attributes rec) throws IOException {
        Attributes patRec = super.findPatientRecord(rec.getString(Tag.PatientID));
        return patRec != null ? patRec : addRootDirectoryRecord(rec);
    }

    /**
     * Finds an existing study record under a patient record by Study Instance UID, or adds a new one if not found.
     * 
     * @param patRec The parent patient record.
     * @param rec    The {@link Attributes} representing the study record to find or add.
     * @return The existing or newly added study record.
     * @throws IOException if an I/O error occurs.
     */
    public synchronized Attributes findOrAddStudyRecord(Attributes patRec, Attributes rec) throws IOException {
        Attributes studyRec = super.findStudyRecord(patRec, rec.getString(Tag.StudyInstanceUID));
        return studyRec != null ? studyRec : addLowerDirectoryRecord(patRec, rec);
    }

    /**
     * Finds an existing series record under a study record by Series Instance UID, or adds a new one if not found.
     * 
     * @param studyRec The parent study record.
     * @param rec      The {@link Attributes} representing the series record to find or add.
     * @return The existing or newly added series record.
     * @throws IOException if an I/O error occurs.
     */
    public synchronized Attributes findOrAddSeriesRecord(Attributes studyRec, Attributes rec) throws IOException {
        Attributes seriesRec = super.findSeriesRecord(studyRec, rec.getString(Tag.SeriesInstanceUID));
        return seriesRec != null ? seriesRec : addLowerDirectoryRecord(studyRec, rec);
    }

    /**
     * Deletes a directory record by setting its Record In Use Flag (0004,1410) to INACTIVE. Recursively deletes all
     * lower-level records associated with it.
     * 
     * @param rec The {@link Attributes} representing the record to delete.
     * @return {@code true} if the record was deleted (i.e., was in use), {@code false} otherwise.
     * @throws IOException if an I/O error occurs.
     */
    public synchronized boolean deleteRecord(Attributes rec) throws IOException {
        if (rec.getInt(Tag.RecordInUseFlag, 0) == INACTIVE)
            return false; // already disabled

        for (Attributes lowerRec = readLowerDirectoryRecord(rec); lowerRec != null; lowerRec = readNextDirectoryRecord(
                lowerRec))
            deleteRecord(lowerRec);

        rec.setInt(Tag.RecordInUseFlag, VR.US, INACTIVE);
        markAsDirty(rec);
        return true;
    }

    /**
     * Rolls back all changes made since the last commit or opening of the writer. This restores the DICOMDIR to its
     * previous state.
     * 
     * @throws IOException if an I/O error occurs.
     */
    public synchronized void rollback() throws IOException {
        if (dirtyRecords.isEmpty())
            return;

        clearCache();
        dirtyRecords.clear();
        if (rollbackLen != -1) {
            restoreDirInfo();
            nextRecordPos = rollbackLen;
            if (getEncodingOptions().undefSequenceLength) {
                writeSequenceDelimitationItem();
                raf.setLength(raf.getFilePointer());
            } else {
                raf.setLength(rollbackLen);
            }
            writeFileSetConsistencyFlag(NO_KNOWN_INCONSISTENCIES);
            rollbackLen = -1;
        }
    }

    @Override
    public void clearCache() {
        lastChildRecords.clear();
        super.clearCache();
    }

    /**
     * Commits all pending changes to the DICOMDIR file. This writes all dirty records and updates the directory
     * information header.
     * 
     * @throws IOException if an I/O error occurs.
     */
    public synchronized void commit() throws IOException {
        if (dirtyRecords.isEmpty())
            return;

        if (rollbackLen == -1)
            writeFileSetConsistencyFlag(KNOWN_INCONSISTENCIES);

        for (Attributes rec : dirtyRecords)
            writeDirRecordHeader(rec);

        dirtyRecords.clear();

        if (rollbackLen != -1 && getEncodingOptions().undefSequenceLength)
            writeSequenceDelimitationItem();

        writeDirInfoHeader();

        rollbackLen = -1;
    }

    @Override
    public void close() throws IOException {
        commit();
        super.close();
    }

    /**
     * Converts a file path relative to the DICOMDIR file into an array of file IDs. This is a convenience method that
     * delegates to the static {@code toFileIDs} method.
     * 
     * @param f The file to get IDs for.
     * @return An array of strings representing the file IDs.
     */
    public String[] toFileIDs(File f) {
        return toFileIDs(file, f);
    }

    /**
     * Updates the internal byte array representing the Directory Information header with current offsets and lengths.
     */
    private void updateDirInfoHeader() {
        ByteKit.intToBytesLE(getOffsetOfFirstRootDirectoryRecord(), dirInfoHeader, 8);
        ByteKit.intToBytesLE(getOffsetOfLastRootDirectoryRecord(), dirInfoHeader, 20);
        ByteKit.intToBytesLE(
                getEncodingOptions().undefSequenceLength ? -1 : nextRecordPos - firstRecordPos,
                dirInfoHeader,
                42);
    }

    /**
     * Restores the Directory Information from the internal header byte array.
     */
    private void restoreDirInfo() {
        setOffsetOfFirstRootDirectoryRecord(ByteKit.bytesToIntLE(dirInfoHeader, 8));
        setOffsetOfLastRootDirectoryRecord(ByteKit.bytesToIntLE(dirInfoHeader, 20));
    }

    /**
     * Writes the Directory Information header to the DICOMDIR file.
     * 
     * @throws IOException if an I/O error occurs.
     */
    private void writeDirInfoHeader() throws IOException {
        updateDirInfoHeader();
        raf.seek(firstRecordPos - dirInfoHeader.length);
        raf.write(dirInfoHeader);
    }

    /**
     * Writes the header of a directory record to the DICOMDIR file.
     * 
     * @param rec The {@link Attributes} representing the directory record.
     * @throws IOException if an I/O error occurs.
     */
    private void writeDirRecordHeader(Attributes rec) throws IOException {
        ByteKit.intToBytesLE(rec.getInt(Tag.OffsetOfTheNextDirectoryRecord, 0), dirRecordHeader, 8);
        ByteKit.shortToBytesLE(rec.getInt(Tag.RecordInUseFlag, 0), dirRecordHeader, 20);
        ByteKit.intToBytesLE(rec.getInt(Tag.OffsetOfReferencedLowerLevelDirectoryEntity, 0), dirRecordHeader, 30);
        raf.seek(rec.getItemPosition() + 8);
        raf.write(dirRecordHeader);
    }

    /**
     * Writes a Sequence Delimitation Item to the file, typically used when writing undefined length sequences.
     * 
     * @throws IOException if an I/O error occurs.
     */
    private void writeSequenceDelimitationItem() throws IOException {
        raf.seek(nextRecordPos);
        out.writeHeader(Tag.SequenceDelimitationItem, null, 0);
    }

    /**
     * Adds a new record and updates the offset of the previous record to point to it.
     * 
     * @param tag     The tag to update in the previous record (e.g., Offset of the Next Directory Record).
     * @param prevRec The previous {@link Attributes} record.
     * @param rec     The new {@link Attributes} record to add.
     * @throws IOException if an I/O error occurs.
     */
    private void addRecord(int tag, Attributes prevRec, Attributes rec) throws IOException {
        prevRec.setInt(tag, VR.UL, nextRecordPos);
        markAsDirty(prevRec);
        writeRecord(nextRecordPos, rec);
    }

    /**
     * Writes a directory record to the DICOMDIR file at the specified offset.
     * 
     * @param offset The offset in the file to write the record.
     * @param rec    The {@link Attributes} representing the record to write.
     * @throws IOException if an I/O error occurs.
     */
    private void writeRecord(int offset, Attributes rec) throws IOException {
        if (Logger.isInfoEnabled())
            Logger.info("M-UPDATE {}: add {} Record", file, rec.getString(Tag.DirectoryRecordType, null));
        Logger.debug("Directory Record:\n{}", rec);
        rec.setItemPosition(offset);
        if (rollbackLen == -1) {
            rollbackLen = offset;
            writeFileSetConsistencyFlag(KNOWN_INCONSISTENCIES);
        }
        raf.seek(offset);
        rec.setInt(Tag.OffsetOfTheNextDirectoryRecord, VR.UL, 0);
        rec.setInt(Tag.RecordInUseFlag, VR.US, IN_USE);
        rec.setInt(Tag.OffsetOfReferencedLowerLevelDirectoryEntity, VR.UL, 0);
        rec.writeItemTo(out);
        nextRecordPos = (int) raf.getFilePointer();
        cache.put(offset, rec);
    }

    /**
     * Writes the File Set Consistency Flag (0004,1212) to the DICOMDIR file.
     * 
     * @param flag The flag value to write.
     * @throws IOException if an I/O error occurs.
     */
    private void writeFileSetConsistencyFlag(int flag) throws IOException {
        raf.seek(firstRecordPos - 14);
        raf.writeShort(flag);
        setFileSetConsistencyFlag(flag);
    }

    /**
     * Marks a record as dirty, adding it to the list of records that need to be written during commit.
     * 
     * @param rec The {@link Attributes} record to mark as dirty.
     */
    private void markAsDirty(Attributes rec) {
        int index = Collections.binarySearch(dirtyRecords, rec, offsetComparator);
        if (index < 0)
            dirtyRecords.add(-(index + 1), rec);
    }

    /**
     * Purges (deletes) inactive records from the DICOMDIR file.
     * 
     * @return The number of records purged.
     * @throws IOException if an I/O error occurs.
     */
    public synchronized int purge() throws IOException {
        int[] count = { 0 };
        purge(findFirstRootDirectoryRecordInUse(false), count);
        return count[0];
    }

    /**
     * Recursively purges inactive records starting from a given record.
     * 
     * @param rec   The starting {@link Attributes} record.
     * @param count An array to store the total count of purged records.
     * @return {@code true} if the current record and its children were purged, {@code false} otherwise.
     * @throws IOException if an I/O error occurs.
     */
    private boolean purge(Attributes rec, int[] count) throws IOException {
        boolean purge = true;
        while (rec != null) {
            if (purge(findLowerDirectoryRecordInUse(rec, false), count) && !rec.containsValue(Tag.ReferencedFileID)) {
                deleteRecord(rec);
                count[0]++;
            } else
                purge = false;
            rec = readNextDirectoryRecord(rec);
        }
        return purge;
    }

}
