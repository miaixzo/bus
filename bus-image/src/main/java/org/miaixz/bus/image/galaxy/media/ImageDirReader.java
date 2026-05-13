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
package org.miaixz.bus.image.galaxy.media;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.miaixz.bus.core.center.map.IntHashMap;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.image.Builder;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.VR;
import org.miaixz.bus.image.galaxy.io.ImageInputStream;
import org.miaixz.bus.image.galaxy.io.RAFInputStreamAdapter;

/**
 * Represents the ImageDirReader type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ImageDirReader implements Closeable {

    /**
     * The file value.
     */
    protected final File file;

    /**
     * The raf value.
     */
    protected final RandomAccessFile raf;

    /**
     * The in value.
     */
    protected final ImageInputStream in;

    /**
     * The fmi value.
     */
    protected final Attributes fmi;

    /**
     * The fs info value.
     */
    protected final Attributes fsInfo;

    /**
     * The cache value.
     */
    protected final IntHashMap<Attributes> cache = new IntHashMap<>();

    /**
     * Creates a new instance.
     *
     * @param file the file.
     * @throws IOException if the operation cannot be completed.
     */
    public ImageDirReader(File file) throws IOException {
        this(file, "r");
    }

    /**
     * Creates a new instance.
     *
     * @param file the file.
     * @param mode the mode.
     * @throws IOException if the operation cannot be completed.
     */
    protected ImageDirReader(File file, String mode) throws IOException {
        this.file = file;
        this.raf = new RandomAccessFile(file, mode);
        try {
            this.in = new ImageInputStream(new RAFInputStreamAdapter(raf));
            this.fmi = in.readFileMetaInformation();
            this.fsInfo = in.readDataset(o -> o.tag() == Tag.DirectoryRecordSequence);
            if (in.tag() != Tag.DirectoryRecordSequence)
                throw new IOException("Missing Directory Record Sequence");
        } catch (IOException e) {
            IoKit.close(raf);
            throw e;
        }
    }

    /**
     * Executes the in use operation.
     *
     * @param rec the rec.
     * @return true if the condition is met; otherwise false.
     */
    public static boolean inUse(Attributes rec) {
        return rec.getInt(Tag.RecordInUseFlag, 0) != 0;
    }

    /**
     * Determines whether private.
     *
     * @param rec the rec.
     * @return true if the condition is met; otherwise false.
     */
    public static boolean isPrivate(Attributes rec) {
        return "PRIVATE".equals(rec.getString(Tag.DirectoryRecordType));
    }

    /**
     * Gets the file.
     *
     * @return the file.
     */
    public final File getFile() {
        return file;
    }

    /**
     * Gets the file meta information.
     *
     * @return the file meta information.
     */
    public final Attributes getFileMetaInformation() {
        return fmi;
    }

    /**
     * Gets the file set information.
     *
     * @return the file set information.
     */
    public final Attributes getFileSetInformation() {
        return fsInfo;
    }

    /**
     * Executes the close operation.
     *
     * @throws IOException if the operation cannot be completed.
     */
    public void close() throws IOException {
        raf.close();
    }

    /**
     * Gets the file set uid.
     *
     * @return the file set uid.
     */
    public String getFileSetUID() {
        return fmi.getString(Tag.MediaStorageSOPInstanceUID, null);
    }

    /**
     * Gets the transfer syntax uid.
     *
     * @return the transfer syntax uid.
     */
    public String getTransferSyntaxUID() {
        return fmi.getString(Tag.TransferSyntaxUID, null);
    }

    /**
     * Gets the file set id.
     *
     * @return the file set id.
     */
    public String getFileSetID() {
        return fsInfo.getString(Tag.FileSetID, null);
    }

    /**
     * Gets the descriptor file.
     *
     * @return the descriptor file.
     */
    public File getDescriptorFile() {
        return toFile(fsInfo.getStrings(Tag.FileSetDescriptorFileID));
    }

    /**
     * Converts this value to file.
     *
     * @param fileIDs the file i ds.
     * @return the operation result.
     */
    public File toFile(String[] fileIDs) {
        if (fileIDs == null || fileIDs.length == 0)
            return null;

        return new File(file.getParent(), Builder.concat(fileIDs, File.separatorChar));
    }

    /**
     * Gets the descriptor file character set.
     *
     * @return the descriptor file character set.
     */
    public String getDescriptorFileCharacterSet() {
        return fsInfo.getString(Tag.SpecificCharacterSetOfFileSetDescriptorFile, null);
    }

    /**
     * Gets the file set consistency flag.
     *
     * @return the file set consistency flag.
     */
    public int getFileSetConsistencyFlag() {
        return fsInfo.getInt(Tag.FileSetConsistencyFlag, 0);
    }

    /**
     * Sets the file set consistency flag.
     *
     * @param i the i.
     */
    protected void setFileSetConsistencyFlag(int i) {
        fsInfo.setInt(Tag.FileSetConsistencyFlag, VR.US, i);
    }

    /**
     * Executes the known inconsistencies operation.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean knownInconsistencies() {
        return getFileSetConsistencyFlag() != 0;
    }

    /**
     * Gets the offset of first root directory record.
     *
     * @return the offset of first root directory record.
     */
    public int getOffsetOfFirstRootDirectoryRecord() {
        return fsInfo.getInt(Tag.OffsetOfTheFirstDirectoryRecordOfTheRootDirectoryEntity, 0);
    }

    /**
     * Sets the offset of first root directory record.
     *
     * @param i the i.
     */
    protected void setOffsetOfFirstRootDirectoryRecord(int i) {
        fsInfo.setInt(Tag.OffsetOfTheFirstDirectoryRecordOfTheRootDirectoryEntity, VR.UL, i);
    }

    /**
     * Gets the offset of last root directory record.
     *
     * @return the offset of last root directory record.
     */
    public int getOffsetOfLastRootDirectoryRecord() {
        return fsInfo.getInt(Tag.OffsetOfTheLastDirectoryRecordOfTheRootDirectoryEntity, 0);
    }

    /**
     * Sets the offset of last root directory record.
     *
     * @param i the i.
     */
    protected void setOffsetOfLastRootDirectoryRecord(int i) {
        fsInfo.setInt(Tag.OffsetOfTheLastDirectoryRecordOfTheRootDirectoryEntity, VR.UL, i);
    }

    /**
     * Determines whether empty.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isEmpty() {
        return getOffsetOfFirstRootDirectoryRecord() == 0;
    }

    /**
     * Executes the clear cache operation.
     */
    public void clearCache() {
        cache.clear();
    }

    /**
     * Reads the first root directory record.
     *
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    public Attributes readFirstRootDirectoryRecord() throws IOException {
        return readRecord(getOffsetOfFirstRootDirectoryRecord());
    }

    /**
     * Reads the last root directory record.
     *
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    public Attributes readLastRootDirectoryRecord() throws IOException {
        return readRecord(getOffsetOfLastRootDirectoryRecord());
    }

    /**
     * Reads the next directory record.
     *
     * @param rec the rec.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    public Attributes readNextDirectoryRecord(Attributes rec) throws IOException {
        return readRecord(rec.getInt(Tag.OffsetOfTheNextDirectoryRecord, 0));
    }

    /**
     * Reads the lower directory record.
     *
     * @param rec the rec.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    public Attributes readLowerDirectoryRecord(Attributes rec) throws IOException {
        return readRecord(rec.getInt(Tag.OffsetOfReferencedLowerLevelDirectoryEntity, 0));
    }

    /**
     * Finds the last lower directory record.
     *
     * @param rec the rec.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    protected Attributes findLastLowerDirectoryRecord(Attributes rec) throws IOException {
        Attributes lower = readLowerDirectoryRecord(rec);
        if (lower == null)
            return null;

        Attributes next;
        while ((next = readNextDirectoryRecord(lower)) != null)
            lower = next;
        return lower;
    }

    /**
     * Finds the first root directory record in use.
     *
     * @param ignorePrivate the ignore private.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    public Attributes findFirstRootDirectoryRecordInUse(boolean ignorePrivate) throws IOException {
        return findRootDirectoryRecord(ignorePrivate, null, false, false);
    }

    /**
     * Finds the root directory record.
     *
     * @param keys           the keys.
     * @param ignorePrivate  the ignore private.
     * @param ignoreCaseOfPN the ignore case of pn.
     * @param matchNoValue   the match no value.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    public Attributes findRootDirectoryRecord(
            Attributes keys,
            boolean ignorePrivate,
            boolean ignoreCaseOfPN,
            boolean matchNoValue) throws IOException {
        return findRecordInUse(
                getOffsetOfFirstRootDirectoryRecord(),
                ignorePrivate,
                keys,
                ignoreCaseOfPN,
                matchNoValue);
    }

    /**
     * Finds the root directory record.
     *
     * @param ignorePrivate  the ignore private.
     * @param keys           the keys.
     * @param ignoreCaseOfPN the ignore case of pn.
     * @param matchNoValue   the match no value.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    public Attributes findRootDirectoryRecord(
            boolean ignorePrivate,
            Attributes keys,
            boolean ignoreCaseOfPN,
            boolean matchNoValue) throws IOException {
        return findRootDirectoryRecord(keys, ignorePrivate, ignoreCaseOfPN, matchNoValue);
    }

    /**
     * Finds the next directory record in use.
     *
     * @param rec           the rec.
     * @param ignorePrivate the ignore private.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    public Attributes findNextDirectoryRecordInUse(Attributes rec, boolean ignorePrivate) throws IOException {
        return findNextDirectoryRecord(rec, ignorePrivate, null, false, false);
    }

    /**
     * Finds the next directory record.
     *
     * @param rec            the rec.
     * @param ignorePrivate  the ignore private.
     * @param keys           the keys.
     * @param ignoreCaseOfPN the ignore case of pn.
     * @param matchNoValue   the match no value.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    public Attributes findNextDirectoryRecord(
            Attributes rec,
            boolean ignorePrivate,
            Attributes keys,
            boolean ignoreCaseOfPN,
            boolean matchNoValue) throws IOException {
        return findRecordInUse(
                rec.getInt(Tag.OffsetOfTheNextDirectoryRecord, 0),
                ignorePrivate,
                keys,
                ignoreCaseOfPN,
                matchNoValue);
    }

    /**
     * Finds the lower directory record in use.
     *
     * @param rec           the rec.
     * @param ignorePrivate the ignore private.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    public Attributes findLowerDirectoryRecordInUse(Attributes rec, boolean ignorePrivate) throws IOException {
        return findLowerDirectoryRecord(rec, ignorePrivate, null, false, false);
    }

    /**
     * Finds the lower directory record.
     *
     * @param rec            the rec.
     * @param ignorePrivate  the ignore private.
     * @param keys           the keys.
     * @param ignoreCaseOfPN the ignore case of pn.
     * @param matchNoValue   the match no value.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    public Attributes findLowerDirectoryRecord(
            Attributes rec,
            boolean ignorePrivate,
            Attributes keys,
            boolean ignoreCaseOfPN,
            boolean matchNoValue) throws IOException {
        return findRecordInUse(
                rec.getInt(Tag.OffsetOfReferencedLowerLevelDirectoryEntity, 0),
                ignorePrivate,
                keys,
                ignoreCaseOfPN,
                matchNoValue);
    }

    /**
     * Finds the patient record.
     *
     * @param ids the ids.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    public Attributes findPatientRecord(String... ids) throws IOException {
        return findRootDirectoryRecord(false, pk("PATIENT", Tag.PatientID, VR.LO, ids), false, false);
    }

    /**
     * Finds the patient record.
     *
     * @param keys           the keys.
     * @param recFact        the rec fact.
     * @param ignoreCaseOfPN the ignore case of pn.
     * @param matchNoValue   the match no value.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    public Attributes findPatientRecord(
            Attributes keys,
            RecordFactory recFact,
            boolean ignoreCaseOfPN,
            boolean matchNoValue) throws IOException {
        return findRootDirectoryRecord(false, keys(RecordType.PATIENT, keys, recFact), ignoreCaseOfPN, matchNoValue);
    }

    /**
     * Finds the next patient record.
     *
     * @param patRec the pat rec.
     * @param ids    the ids.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    public Attributes findNextPatientRecord(Attributes patRec, String... ids) throws IOException {
        return findNextDirectoryRecord(patRec, false, pk("PATIENT", Tag.PatientID, VR.LO, ids), false, false);
    }

    /**
     * Finds the next patient record.
     *
     * @param patRec         the pat rec.
     * @param keys           the keys.
     * @param recFact        the rec fact.
     * @param ignoreCaseOfPN the ignore case of pn.
     * @param matchNoValue   the match no value.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    public Attributes findNextPatientRecord(
            Attributes patRec,
            Attributes keys,
            RecordFactory recFact,
            boolean ignoreCaseOfPN,
            boolean matchNoValue) throws IOException {
        return findNextDirectoryRecord(
                patRec,
                false,
                keys(RecordType.PATIENT, keys, recFact),
                ignoreCaseOfPN,
                matchNoValue);
    }

    /**
     * Finds the study record.
     *
     * @param patRec the pat rec.
     * @param iuids  the iuids.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    public Attributes findStudyRecord(Attributes patRec, String... iuids) throws IOException {
        return findLowerDirectoryRecord(patRec, false, pk("STUDY", Tag.StudyInstanceUID, VR.UI, iuids), false, false);
    }

    /**
     * Finds the study record.
     *
     * @param patRec         the pat rec.
     * @param keys           the keys.
     * @param recFact        the rec fact.
     * @param ignoreCaseOfPN the ignore case of pn.
     * @param matchNoValue   the match no value.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    public Attributes findStudyRecord(
            Attributes patRec,
            Attributes keys,
            RecordFactory recFact,
            boolean ignoreCaseOfPN,
            boolean matchNoValue) throws IOException {
        return findLowerDirectoryRecord(
                patRec,
                false,
                keys(RecordType.STUDY, keys, recFact),
                ignoreCaseOfPN,
                matchNoValue);
    }

    /**
     * Finds the next study record.
     *
     * @param studyRec the study rec.
     * @param iuids    the iuids.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    public Attributes findNextStudyRecord(Attributes studyRec, String... iuids) throws IOException {
        return findNextDirectoryRecord(studyRec, false, pk("STUDY", Tag.StudyInstanceUID, VR.UI, iuids), false, false);
    }

    /**
     * Finds the next study record.
     *
     * @param studyRec       the study rec.
     * @param keys           the keys.
     * @param recFact        the rec fact.
     * @param ignoreCaseOfPN the ignore case of pn.
     * @param matchNoValue   the match no value.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    public Attributes findNextStudyRecord(
            Attributes studyRec,
            Attributes keys,
            RecordFactory recFact,
            boolean ignoreCaseOfPN,
            boolean matchNoValue) throws IOException {
        return findNextDirectoryRecord(
                studyRec,
                false,
                keys(RecordType.STUDY, keys, recFact),
                ignoreCaseOfPN,
                matchNoValue);
    }

    /**
     * Finds the series record.
     *
     * @param studyRec the study rec.
     * @param iuids    the iuids.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    public Attributes findSeriesRecord(Attributes studyRec, String... iuids) throws IOException {
        return findLowerDirectoryRecord(
                studyRec,
                false,
                pk("SERIES", Tag.SeriesInstanceUID, VR.UI, iuids),
                false,
                false);
    }

    /**
     * Finds the series record.
     *
     * @param studyRec       the study rec.
     * @param keys           the keys.
     * @param recFact        the rec fact.
     * @param ignoreCaseOfPN the ignore case of pn.
     * @param matchNoValue   the match no value.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    public Attributes findSeriesRecord(
            Attributes studyRec,
            Attributes keys,
            RecordFactory recFact,
            boolean ignoreCaseOfPN,
            boolean matchNoValue) throws IOException {
        return findLowerDirectoryRecord(
                studyRec,
                false,
                keys(RecordType.SERIES, keys, recFact),
                ignoreCaseOfPN,
                matchNoValue);
    }

    /**
     * Finds the next series record.
     *
     * @param seriesRec the series rec.
     * @param iuids     the iuids.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    public Attributes findNextSeriesRecord(Attributes seriesRec, String... iuids) throws IOException {
        return findNextDirectoryRecord(
                seriesRec,
                false,
                pk("SERIES", Tag.SeriesInstanceUID, VR.UI, iuids),
                false,
                false);
    }

    /**
     * Finds the next series record.
     *
     * @param seriesRec      the series rec.
     * @param keys           the keys.
     * @param recFact        the rec fact.
     * @param ignoreCaseOfPN the ignore case of pn.
     * @param matchNoValue   the match no value.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    public Attributes findNextSeriesRecord(
            Attributes seriesRec,
            Attributes keys,
            RecordFactory recFact,
            boolean ignoreCaseOfPN,
            boolean matchNoValue) throws IOException {
        return findNextDirectoryRecord(
                seriesRec,
                false,
                keys(RecordType.SERIES, keys, recFact),
                ignoreCaseOfPN,
                matchNoValue);
    }

    /**
     * Finds the lower instance record.
     *
     * @param seriesRec     the series rec.
     * @param ignorePrivate the ignore private.
     * @param iuids         the iuids.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    public Attributes findLowerInstanceRecord(Attributes seriesRec, boolean ignorePrivate, String... iuids)
            throws IOException {
        return findLowerDirectoryRecord(seriesRec, ignorePrivate, pk(iuids), false, false);
    }

    /**
     * Finds the lower instance record.
     *
     * @param seriesRec      the series rec.
     * @param keys           the keys.
     * @param recFact        the rec fact.
     * @param ignoreCaseOfPN the ignore case of pn.
     * @param matchNoValue   the match no value.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    public Attributes findLowerInstanceRecord(
            Attributes seriesRec,
            Attributes keys,
            RecordFactory recFact,
            boolean ignoreCaseOfPN,
            boolean matchNoValue) throws IOException {
        return findLowerDirectoryRecord(seriesRec, false, keys(keys, recFact), ignoreCaseOfPN, matchNoValue);
    }

    /**
     * Finds the next instance record.
     *
     * @param instRec       the inst rec.
     * @param ignorePrivate the ignore private.
     * @param iuids         the iuids.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    public Attributes findNextInstanceRecord(Attributes instRec, boolean ignorePrivate, String... iuids)
            throws IOException {
        return findNextDirectoryRecord(instRec, ignorePrivate, pk(iuids), false, false);
    }

    /**
     * Finds the next instance record.
     *
     * @param instRec        the inst rec.
     * @param keys           the keys.
     * @param recFact        the rec fact.
     * @param ignoreCaseOfPN the ignore case of pn.
     * @param matchNoValue   the match no value.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    public Attributes findNextInstanceRecord(
            Attributes instRec,
            Attributes keys,
            RecordFactory recFact,
            boolean ignoreCaseOfPN,
            boolean matchNoValue) throws IOException {
        return findNextDirectoryRecord(instRec, false, keys(keys, recFact), ignoreCaseOfPN, matchNoValue);
    }

    /**
     * Finds the root instance record.
     *
     * @param ignorePrivate the ignore private.
     * @param iuids         the iuids.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    public Attributes findRootInstanceRecord(boolean ignorePrivate, String... iuids) throws IOException {
        return findRootDirectoryRecord(ignorePrivate, pk(iuids), false, false);
    }

    /**
     * Executes the pk operation.
     *
     * @param type the type.
     * @param tag  the tag.
     * @param vr   the vr.
     * @param ids  the ids.
     * @return the operation result.
     */
    private Attributes pk(String type, int tag, VR vr, String... ids) {
        Attributes pk = new Attributes(2);
        pk.setString(Tag.DirectoryRecordType, VR.CS, type);
        if (ids != null && ids.length != 0)
            pk.setString(tag, vr, ids);
        return pk;
    }

    /**
     * Executes the pk operation.
     *
     * @param iuids the iuids.
     * @return the operation result.
     */
    private Attributes pk(String... iuids) {
        if (iuids == null || iuids.length == 0)
            return null;

        Attributes pk = new Attributes(1);
        pk.setString(Tag.ReferencedSOPInstanceUIDInFile, VR.UI, iuids);
        return pk;
    }

    /**
     * Executes the keys operation.
     *
     * @param type    the type.
     * @param attrs   the attrs.
     * @param recFact the rec fact.
     * @return the operation result.
     */
    private Attributes keys(RecordType type, Attributes attrs, RecordFactory recFact) {
        int[] selection = recFact.getRecordKeys(type);
        Attributes keys = new Attributes(selection.length + 1);
        keys.setString(Tag.DirectoryRecordType, VR.CS, type.name());
        keys.addSelected(attrs, selection);
        return keys;
    }

    /**
     * Executes the keys operation.
     *
     * @param attrs   the attrs.
     * @param recFact the rec fact.
     * @return the operation result.
     */
    private Attributes keys(Attributes attrs, RecordFactory recFact) {
        int[] selection = recFact.getRecordKeys(RecordType.SR_DOCUMENT);
        Attributes keys = new Attributes(selection.length + 1);
        String[] iuids = attrs.getStrings(Tag.SOPInstanceUID);
        if (iuids != null && iuids.length > 0)
            keys.setString(Tag.ReferencedSOPInstanceUIDInFile, VR.CS, iuids);
        keys.addSelected(attrs, selection);
        return keys;
    }

    /**
     * Finds the record in use.
     *
     * @param offset         the offset.
     * @param ignorePrivate  the ignore private.
     * @param keys           the keys.
     * @param ignoreCaseOfPN the ignore case of pn.
     * @param matchNoValue   the match no value.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    private Attributes findRecordInUse(
            int offset,
            boolean ignorePrivate,
            Attributes keys,
            boolean ignoreCaseOfPN,
            boolean matchNoValue) throws IOException {
        while (offset != 0) {
            Attributes item = readRecord(offset);
            if (inUse(item) && !(ignorePrivate && isPrivate(item))
                    && (keys == null || item.matches(keys, ignoreCaseOfPN, matchNoValue)))
                return item;
            offset = item.getInt(Tag.OffsetOfTheNextDirectoryRecord, 0);
        }
        return null;
    }

    /**
     * Reads the record.
     *
     * @param offset the offset.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    private synchronized Attributes readRecord(int offset) throws IOException {
        if (offset == 0)
            return null;

        Attributes item = cache.get(offset);
        if (item == null) {
            long off = offset & 0xffffffffL;
            raf.seek(off);
            in.setPosition(off);
            item = in.readItem();
            cache.put(offset, item);
        }
        return item;
    }

}
