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
package org.aoju.bus.image.galaxy;

import org.aoju.bus.core.toolkit.IoKit;
import org.aoju.bus.image.Tag;
import org.aoju.bus.image.galaxy.data.Attributes;
import org.aoju.bus.image.galaxy.data.VR;
import org.aoju.bus.image.galaxy.io.ImageInputStream;
import org.aoju.bus.image.galaxy.io.RAFInputStreamAdapter;
import org.aoju.bus.image.galaxy.media.RecordFactory;
import org.aoju.bus.image.galaxy.media.RecordType;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class DirReader implements Closeable {

    protected final File file;
    protected final RandomAccessFile raf;
    protected final ImageInputStream in;
    protected final Attributes fmi;
    protected final Attributes fsInfo;
    protected final Capacity<Attributes> cache = new Capacity<>();

    public DirReader(File file) throws IOException {
        this(file, "r");
    }

    protected DirReader(File file, String mode) throws IOException {
        this.file = file;
        this.raf = new RandomAccessFile(file, mode);
        try {
            this.in = new ImageInputStream(new RAFInputStreamAdapter(raf));
            this.fmi = in.readFileMetaInformation();
            this.fsInfo = in.readDataset(-1, Tag.DirectoryRecordSequence);
            if (in.tag() != Tag.DirectoryRecordSequence)
                throw new IOException("Missing Directory Record Sequence");
        } catch (IOException e) {
            IoKit.close(raf);
            throw e;
        }
    }

    public static boolean inUse(Attributes rec) {
        return rec.getInt(Tag.RecordInUseFlag, 0) != 0;
    }

    public static boolean isPrivate(Attributes rec) {
        return "PRIVATE".equals(rec.getString(Tag.DirectoryRecordType));
    }

    public final File getFile() {
        return file;
    }

    public final Attributes getFileMetaInformation() {
        return fmi;
    }

    public final Attributes getFileSetInformation() {
        return fsInfo;
    }

    public void close() throws IOException {
        raf.close();
    }

    public String getFileSetUID() {
        return fmi.getString(Tag.MediaStorageSOPInstanceUID, null);
    }

    public String getTransferSyntaxUID() {
        return fmi.getString(Tag.TransferSyntaxUID, null);
    }

    public String getFileSetID() {
        return fsInfo.getString(Tag.FileSetID, null);
    }

    public File getDescriptorFile() {
        return toFile(fsInfo.getStrings(Tag.FileSetDescriptorFileID));
    }

    public File toFile(String[] fileIDs) {
        if (null == fileIDs || fileIDs.length == 0)
            return null;

        return new File(file.getParent(),
                Property.concat(fileIDs, File.separatorChar));
    }

    public String getDescriptorFileCharacterSet() {
        return fsInfo.getString(
                Tag.SpecificCharacterSetOfFileSetDescriptorFile, null);
    }

    public int getFileSetConsistencyFlag() {
        return fsInfo.getInt(Tag.FileSetConsistencyFlag, 0);
    }

    protected void setFileSetConsistencyFlag(int i) {
        fsInfo.setInt(Tag.FileSetConsistencyFlag, VR.US, i);
    }

    public boolean knownInconsistencies() {
        return getFileSetConsistencyFlag() != 0;
    }

    public int getOffsetOfFirstRootDirectoryRecord() {
        return fsInfo.getInt(
                Tag.OffsetOfTheFirstDirectoryRecordOfTheRootDirectoryEntity, 0);
    }

    protected void setOffsetOfFirstRootDirectoryRecord(int i) {
        fsInfo.setInt(
                Tag.OffsetOfTheFirstDirectoryRecordOfTheRootDirectoryEntity,
                VR.UL, i);
    }

    public int getOffsetOfLastRootDirectoryRecord() {
        return fsInfo.getInt(
                Tag.OffsetOfTheLastDirectoryRecordOfTheRootDirectoryEntity, 0);
    }

    protected void setOffsetOfLastRootDirectoryRecord(int i) {
        fsInfo.setInt(
                Tag.OffsetOfTheLastDirectoryRecordOfTheRootDirectoryEntity,
                VR.UL, i);
    }

    public boolean isEmpty() {
        return getOffsetOfFirstRootDirectoryRecord() == 0;
    }

    public void clearCache() {
        cache.clear();
    }

    public Attributes readFirstRootDirectoryRecord() throws IOException {
        return readRecord(getOffsetOfFirstRootDirectoryRecord());
    }

    public Attributes readLastRootDirectoryRecord() throws IOException {
        return readRecord(getOffsetOfLastRootDirectoryRecord());
    }

    public Attributes readNextDirectoryRecord(Attributes rec)
            throws IOException {
        return readRecord(
                rec.getInt(Tag.OffsetOfTheNextDirectoryRecord, 0));
    }

    public Attributes readLowerDirectoryRecord(Attributes rec)
            throws IOException {
        return readRecord(
                rec.getInt(Tag.OffsetOfReferencedLowerLevelDirectoryEntity, 0));
    }

    protected Attributes findLastLowerDirectoryRecord(Attributes rec)
            throws IOException {
        Attributes lower = readLowerDirectoryRecord(rec);
        if (null == lower)
            return null;

        Attributes next;
        while (null != (next = readNextDirectoryRecord(lower)))
            lower = next;
        return lower;
    }

    public Attributes findFirstRootDirectoryRecordInUse(boolean ignorePrivate) throws IOException {
        return findRootDirectoryRecord(ignorePrivate, null, false, false);
    }

    public Attributes findRootDirectoryRecord(Attributes keys, boolean ignorePrivate,
                                              boolean ignoreCaseOfPN, boolean matchNoValue)
            throws IOException {
        return findRecordInUse(getOffsetOfFirstRootDirectoryRecord(), ignorePrivate,
                keys, ignoreCaseOfPN, matchNoValue);
    }

    public Attributes findRootDirectoryRecord(boolean ignorePrivate, Attributes keys,
                                              boolean ignoreCaseOfPN, boolean matchNoValue) throws IOException {
        return findRootDirectoryRecord(keys, ignorePrivate, ignoreCaseOfPN, matchNoValue);
    }

    public Attributes findNextDirectoryRecordInUse(Attributes rec, boolean ignorePrivate)
            throws IOException {
        return findNextDirectoryRecord(rec, ignorePrivate, null, false, false);
    }

    public Attributes findNextDirectoryRecord(Attributes rec, boolean ignorePrivate,
                                              Attributes keys, boolean ignoreCaseOfPN, boolean matchNoValue) throws IOException {
        return findRecordInUse(
                rec.getInt(Tag.OffsetOfTheNextDirectoryRecord, 0), ignorePrivate,
                keys, ignoreCaseOfPN, matchNoValue);
    }

    public Attributes findLowerDirectoryRecordInUse(Attributes rec, boolean ignorePrivate)
            throws IOException {
        return findLowerDirectoryRecord(rec, ignorePrivate, null, false, false);
    }

    public Attributes findLowerDirectoryRecord(Attributes rec, boolean ignorePrivate,
                                               Attributes keys, boolean ignoreCaseOfPN, boolean matchNoValue)
            throws IOException {
        return findRecordInUse(
                rec.getInt(Tag.OffsetOfReferencedLowerLevelDirectoryEntity, 0), ignorePrivate,
                keys, ignoreCaseOfPN, matchNoValue);
    }

    public Attributes findPatientRecord(String... ids) throws IOException {
        return findRootDirectoryRecord(false,
                pk("PATIENT", Tag.PatientID, VR.LO, ids), false, false);
    }

    public Attributes findPatientRecord(Attributes keys, RecordFactory recFact,
                                        boolean ignoreCaseOfPN, boolean matchNoValue) throws IOException {
        return findRootDirectoryRecord(false,
                keys(RecordType.PATIENT, keys, recFact), ignoreCaseOfPN, matchNoValue);
    }

    public Attributes findNextPatientRecord(Attributes patRec, String... ids) throws IOException {
        return findNextDirectoryRecord(patRec, false,
                pk("PATIENT", Tag.PatientID, VR.LO, ids), false, false);
    }

    public Attributes findNextPatientRecord(Attributes patRec, Attributes keys, RecordFactory recFact,
                                            boolean ignoreCaseOfPN, boolean matchNoValue) throws IOException {
        return findNextDirectoryRecord(patRec, false,
                keys(RecordType.PATIENT, keys, recFact), ignoreCaseOfPN, matchNoValue);
    }

    public Attributes findStudyRecord(Attributes patRec, String... iuids)
            throws IOException {
        return findLowerDirectoryRecord(patRec, false,
                pk("STUDY", Tag.StudyInstanceUID, VR.UI, iuids),
                false, false);
    }

    public Attributes findStudyRecord(Attributes patRec, Attributes keys, RecordFactory recFact,
                                      boolean ignoreCaseOfPN, boolean matchNoValue) throws IOException {
        return findLowerDirectoryRecord(patRec, false,
                keys(RecordType.STUDY, keys, recFact), ignoreCaseOfPN, matchNoValue);
    }

    public Attributes findNextStudyRecord(Attributes studyRec, String... iuids)
            throws IOException {
        return findNextDirectoryRecord(studyRec, false,
                pk("STUDY", Tag.StudyInstanceUID, VR.UI, iuids),
                false, false);
    }

    public Attributes findNextStudyRecord(Attributes studyRec, Attributes keys, RecordFactory recFact,
                                          boolean ignoreCaseOfPN, boolean matchNoValue) throws IOException {
        return findNextDirectoryRecord(studyRec, false,
                keys(RecordType.STUDY, keys, recFact), ignoreCaseOfPN, matchNoValue);
    }

    public Attributes findSeriesRecord(Attributes studyRec, String... iuids)
            throws IOException {
        return findLowerDirectoryRecord(studyRec, false,
                pk("SERIES", Tag.SeriesInstanceUID, VR.UI, iuids),
                false, false);
    }

    public Attributes findSeriesRecord(Attributes studyRec, Attributes keys, RecordFactory recFact,
                                       boolean ignoreCaseOfPN, boolean matchNoValue) throws IOException {
        return findLowerDirectoryRecord(studyRec, false,
                keys(RecordType.SERIES, keys, recFact), ignoreCaseOfPN, matchNoValue);
    }

    public Attributes findNextSeriesRecord(Attributes seriesRec, String... iuids)
            throws IOException {
        return findNextDirectoryRecord(seriesRec, false,
                pk("SERIES", Tag.SeriesInstanceUID, VR.UI, iuids),
                false, false);
    }

    public Attributes findNextSeriesRecord(Attributes seriesRec, Attributes keys, RecordFactory recFact,
                                           boolean ignoreCaseOfPN, boolean matchNoValue) throws IOException {
        return findNextDirectoryRecord(seriesRec, false,
                keys(RecordType.SERIES, keys, recFact), ignoreCaseOfPN, matchNoValue);
    }

    public Attributes findLowerInstanceRecord(Attributes seriesRec, boolean ignorePrivate,
                                              String... iuids) throws IOException {
        return findLowerDirectoryRecord(seriesRec, ignorePrivate, pk(iuids), false, false);
    }

    public Attributes findLowerInstanceRecord(Attributes seriesRec, Attributes keys, RecordFactory recFact,
                                              boolean ignoreCaseOfPN, boolean matchNoValue) throws IOException {
        return findLowerDirectoryRecord(seriesRec, false,
                keys(keys, recFact), ignoreCaseOfPN, matchNoValue);
    }

    public Attributes findNextInstanceRecord(Attributes instRec, boolean ignorePrivate,
                                             String... iuids) throws IOException {
        return findNextDirectoryRecord(instRec, ignorePrivate, pk(iuids), false, false);
    }

    public Attributes findNextInstanceRecord(Attributes instRec, Attributes keys, RecordFactory recFact,
                                             boolean ignoreCaseOfPN, boolean matchNoValue) throws IOException {
        return findNextDirectoryRecord(instRec, false,
                keys(keys, recFact), ignoreCaseOfPN, matchNoValue);
    }

    public Attributes findRootInstanceRecord(boolean ignorePrivate, String... iuids)
            throws IOException {
        return findRootDirectoryRecord(ignorePrivate, pk(iuids), false, false);
    }

    private Attributes pk(String type, int tag, VR vr, String... ids) {
        Attributes pk = new Attributes(2);
        pk.setString(Tag.DirectoryRecordType, VR.CS, type);
        if (null != ids && ids.length != 0)
            pk.setString(tag, vr, ids);
        return pk;
    }

    private Attributes pk(String... iuids) {
        if (null == iuids || iuids.length == 0)
            return null;

        Attributes pk = new Attributes(1);
        pk.setString(Tag.ReferencedSOPInstanceUIDInFile, VR.UI, iuids);
        return pk;
    }

    private Attributes keys(RecordType type, Attributes attrs, RecordFactory recFact) {
        int[] selection = recFact.getRecordKeys(type);
        Attributes keys = new Attributes(selection.length + 1);
        keys.setString(Tag.DirectoryRecordType, VR.CS, type.name());
        keys.addSelected(attrs, selection);
        return keys;
    }

    private Attributes keys(Attributes attrs, RecordFactory recFact) {
        int[] selection = recFact.getRecordKeys(RecordType.SR_DOCUMENT);
        Attributes keys = new Attributes(selection.length + 1);
        String[] iuids = keys.getStrings(Tag.SOPInstanceUID);
        if (null != iuids && iuids.length > 0)
            keys.setString(Tag.ReferencedSOPInstanceUIDInFile, VR.CS, iuids);
        keys.addSelected(attrs, selection);
        return keys;
    }

    private Attributes findRecordInUse(int offset, boolean ignorePrivate, Attributes keys,
                                       boolean ignoreCaseOfPN, boolean matchNoValue)
            throws IOException {
        while (offset != 0) {
            Attributes item = readRecord(offset);
            if (inUse(item) && !(ignorePrivate && isPrivate(item))
                    && (null == keys || item.matches(keys, ignoreCaseOfPN, matchNoValue)))
                return item;
            offset = item.getInt(Tag.OffsetOfTheNextDirectoryRecord, 0);
        }
        return null;
    }

    private synchronized Attributes readRecord(int offset) throws IOException {
        if (offset == 0)
            return null;

        Attributes item = cache.get(offset);
        if (null == item) {
            long off = offset & 0xffffffffL;
            raf.seek(off);
            in.setPosition(off);
            item = in.readItem();
            cache.put(offset, item);
        }
        return item;
    }

}
