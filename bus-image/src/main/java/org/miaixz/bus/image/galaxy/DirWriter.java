/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
package org.miaixz.bus.image.galaxy;

import org.miaixz.bus.core.toolkit.ByteKit;
import org.miaixz.bus.image.Builder;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.VR;
import org.miaixz.bus.image.galaxy.io.ImageEncodingOptions;
import org.miaixz.bus.image.galaxy.io.ImageOutputStream;
import org.miaixz.bus.image.galaxy.io.RAFOutputStreamAdapter;
import org.miaixz.bus.logger.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class DirWriter extends DirReader {

    private static final Comparator<Attributes> offsetComparator =
            (item1, item2) -> {
                long d = item1.getItemPosition() - item2.getItemPosition();
                return d < 0 ? -1 : d > 0 ? 1 : 0;
            };
    private final byte[] dirInfoHeader = {
            0x04, 0x00, 0x00, 0x12, 'U', 'L', 4, 0, 0, 0, 0, 0,
            0x04, 0x00, 0x02, 0x12, 'U', 'L', 4, 0, 0, 0, 0, 0,
            0x04, 0x00, 0x12, 0x12, 'U', 'S', 2, 0, 0, 0,
            0x04, 0x00, 0x20, 0x12, 'S', 'Q', 0, 0, 0, 0, 0, 0};
    private final byte[] dirRecordHeader = {
            0x04, 0x00, 0x00, 0x14, 'U', 'L', 4, 0, 0, 0, 0, 0,
            0x04, 0x00, 0x10, 0x14, 'U', 'S', 2, 0, 0, 0,
            0x04, 0x00, 0x20, 0x14, 'U', 'L', 4, 0, 0, 0, 0, 0};
    private final ImageOutputStream out;
    private final int firstRecordPos;
    private final ArrayList<Attributes> dirtyRecords = new ArrayList<>();
    private final IdentityHashMap<Attributes, Attributes> lastChildRecords = new IdentityHashMap<>();
    private int nextRecordPos;
    private int rollbackLen = -1;

    private DirWriter(File file) throws IOException {
        super(file, "rw");
        out = new ImageOutputStream(new RAFOutputStreamAdapter(raf),
                super.getTransferSyntaxUID());
        int seqLen = in.length();
        boolean undefSeqLen = seqLen <= 0;
        setEncodingOptions(
                new ImageEncodingOptions(false,
                        undefSeqLen,
                        false,
                        undefSeqLen,
                        false));
        this.nextRecordPos = this.firstRecordPos = (int) in.getPosition();
        if (!isEmpty()) {
            if (seqLen > 0)
                this.nextRecordPos += seqLen;
            else
                this.nextRecordPos = (int) (raf.length() - 12);
        }
        updateDirInfoHeader();
    }

    public static DirWriter open(File file) throws IOException {
        if (!file.isFile())
            throw new FileNotFoundException();

        return new DirWriter(file);
    }

    public static void createEmptyDirectory(File file, String iuid,
                                            String id, File descFile, String charset) throws IOException {
        Attributes fmi = Attributes.createFileMetaInformation(iuid,
                UID.MediaStorageDirectoryStorage, UID.ExplicitVRLittleEndian);
        createEmptyDirectory(file, fmi, id, descFile, charset);
    }

    public static void createEmptyDirectory(File file, Attributes fmi,
                                            String id, File descFile, String charset) throws IOException {
        Attributes fsInfo =
                createFileSetInformation(file, id, descFile, charset);
        ImageOutputStream out = new ImageOutputStream(file);
        try {
            out.writeDataset(fmi, fsInfo);
        } finally {
            out.close();
        }
    }

    private static Attributes createFileSetInformation(File file, String id,
                                                       File descFile, String charset) {
        Attributes fsInfo = new Attributes(7);
        fsInfo.setString(Tag.FileSetID, VR.CS, id);
        if (null != descFile) {
            fsInfo.setString(Tag.FileSetDescriptorFileID, VR.CS,
                    toFileIDs(file, descFile));
            if (null != charset && !charset.isEmpty())
                fsInfo.setString(
                        Tag.SpecificCharacterSetOfFileSetDescriptorFile,
                        VR.CS, charset);
        }
        fsInfo.setInt(
                Tag.OffsetOfTheFirstDirectoryRecordOfTheRootDirectoryEntity,
                VR.UL, 0);
        fsInfo.setInt(
                Tag.OffsetOfTheLastDirectoryRecordOfTheRootDirectoryEntity,
                VR.UL, 0);
        fsInfo.setInt(Tag.FileSetConsistencyFlag, VR.US, 0);
        fsInfo.setNull(Tag.DirectoryRecordSequence, VR.SQ);
        return fsInfo;
    }

    private static String[] toFileIDs(File dfile, File f) {
        String dfilepath = dfile.getAbsolutePath();
        int dend = dfilepath.lastIndexOf(File.separatorChar) + 1;
        String dpath = dfilepath.substring(0, dend);
        String fpath = f.getAbsolutePath();
        if (dend == 0 || !fpath.startsWith(dpath))
            throw new IllegalArgumentException("file: " + fpath
                    + " not in directory: " + dfile.getAbsoluteFile());
        return Property.split(fpath.substring(dend), File.separatorChar);
    }

    public ImageEncodingOptions getEncodingOptions() {
        return out.getEncodingOptions();
    }

    public void setEncodingOptions(ImageEncodingOptions encOpts) {
        out.setEncodingOptions(encOpts);
    }

    public synchronized Attributes addRootDirectoryRecord(Attributes rec)
            throws IOException {
        Attributes lastRootRecord = readLastRootDirectoryRecord();
        if (null == lastRootRecord) {
            writeRecord(firstRecordPos, rec);
            setOffsetOfFirstRootDirectoryRecord(firstRecordPos);
        } else {
            addRecord(Tag.OffsetOfTheNextDirectoryRecord, lastRootRecord, rec);
        }
        setOffsetOfLastRootDirectoryRecord((int) rec.getItemPosition());
        return rec;
    }

    public synchronized Attributes addLowerDirectoryRecord(
            Attributes parentRec, Attributes rec) throws IOException {
        Attributes prevRec = lastChildRecords.get(parentRec);
        if (null == prevRec)
            prevRec = findLastLowerDirectoryRecord(parentRec);

        if (null != prevRec)
            addRecord(Tag.OffsetOfTheNextDirectoryRecord, prevRec, rec);
        else
            addRecord(Tag.OffsetOfReferencedLowerLevelDirectoryEntity,
                    parentRec, rec);

        lastChildRecords.put(parentRec, rec);
        return rec;
    }

    public synchronized Attributes findOrAddPatientRecord(Attributes rec) throws IOException {
        Attributes patRec = super.findPatientRecord(rec.getString(Tag.PatientID));
        return null != patRec ? patRec : addRootDirectoryRecord(rec);
    }

    public synchronized Attributes findOrAddStudyRecord(Attributes patRec, Attributes rec)
            throws IOException {
        Attributes studyRec = super.findStudyRecord(patRec, rec.getString(Tag.StudyInstanceUID));
        return null != studyRec ? studyRec : addLowerDirectoryRecord(patRec, rec);
    }

    public synchronized Attributes findOrAddSeriesRecord(Attributes studyRec, Attributes rec)
            throws IOException {
        Attributes seriesRec = super.findSeriesRecord(studyRec, rec.getString(Tag.SeriesInstanceUID));
        return null != seriesRec ? seriesRec : addLowerDirectoryRecord(studyRec, rec);
    }

    public synchronized boolean deleteRecord(Attributes rec)
            throws IOException {
        if (rec.getInt(Tag.RecordInUseFlag, 0) == Builder.IN_ACTIVE)
            return false;

        for (Attributes lowerRec = readLowerDirectoryRecord(rec);
             lowerRec != null;
             lowerRec = readNextDirectoryRecord(lowerRec))
            deleteRecord(lowerRec);

        rec.setInt(Tag.RecordInUseFlag, VR.US, Builder.IN_ACTIVE);
        markAsDirty(rec);
        return true;
    }

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
            writeFileSetConsistencyFlag(Builder.NO_KNOWN_INCONSISTENCIES);
            rollbackLen = -1;
        }
    }

    public void clearCache() {
        lastChildRecords.clear();
        super.clearCache();
    }

    public synchronized void commit() throws IOException {
        if (dirtyRecords.isEmpty())
            return;

        if (rollbackLen == -1)
            writeFileSetConsistencyFlag(Builder.KNOWN_INCONSISTENCIES);

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

    public String[] toFileIDs(File f) {
        return toFileIDs(file, f);
    }

    private void updateDirInfoHeader() {
        ByteKit.intToBytesLE(
                getOffsetOfFirstRootDirectoryRecord(),
                dirInfoHeader, 8);
        ByteKit.intToBytesLE(
                getOffsetOfLastRootDirectoryRecord(),
                dirInfoHeader, 20);
        ByteKit.intToBytesLE(
                getEncodingOptions().undefSequenceLength
                        ? -1 : nextRecordPos - firstRecordPos,
                dirInfoHeader, 42);
    }

    private void restoreDirInfo() {
        setOffsetOfFirstRootDirectoryRecord(
                ByteKit.bytesToIntLE(dirInfoHeader, 8));
        setOffsetOfLastRootDirectoryRecord(
                ByteKit.bytesToIntLE(dirInfoHeader, 20));
    }

    private void writeDirInfoHeader() throws IOException {
        updateDirInfoHeader();
        raf.seek(firstRecordPos - dirInfoHeader.length);
        raf.write(dirInfoHeader);
    }

    private void writeDirRecordHeader(Attributes rec) throws IOException {
        ByteKit.intToBytesLE(
                rec.getInt(Tag.OffsetOfTheNextDirectoryRecord, 0),
                dirRecordHeader, 8);
        ByteKit.shortToBytesLE(
                rec.getInt(Tag.RecordInUseFlag, 0),
                dirRecordHeader, 20);
        ByteKit.intToBytesLE(
                rec.getInt(Tag.OffsetOfReferencedLowerLevelDirectoryEntity, 0),
                dirRecordHeader, 30);
        raf.seek(rec.getItemPosition() + 8);
        raf.write(dirRecordHeader);
    }

    private void writeSequenceDelimitationItem() throws IOException {
        raf.seek(nextRecordPos);
        out.writeHeader(Tag.SequenceDelimitationItem, null, 0);
    }

    private void addRecord(int tag, Attributes prevRec, Attributes rec)
            throws IOException {
        prevRec.setInt(tag, VR.UL, nextRecordPos);
        markAsDirty(prevRec);
        writeRecord(nextRecordPos, rec);
    }

    private void writeRecord(int offset, Attributes rec) throws IOException {
        Logger.debug("Directory Record:\n{}", rec);
        rec.setItemPosition(offset);
        if (rollbackLen == -1) {
            rollbackLen = offset;
            writeFileSetConsistencyFlag(Builder.KNOWN_INCONSISTENCIES);
        }
        raf.seek(offset);
        rec.setInt(Tag.OffsetOfTheNextDirectoryRecord, VR.UL, 0);
        rec.setInt(Tag.RecordInUseFlag, VR.US, Builder.IN_USE);
        rec.setInt(Tag.OffsetOfReferencedLowerLevelDirectoryEntity, VR.UL, 0);
        rec.writeItemTo(out);
        nextRecordPos = (int) raf.getFilePointer();
        cache.put(offset, rec);
    }

    private void writeFileSetConsistencyFlag(int flag) throws IOException {
        raf.seek(firstRecordPos - 14);
        raf.writeShort(flag);
        setFileSetConsistencyFlag(flag);
    }

    private void markAsDirty(Attributes rec) {
        int index = Collections.binarySearch(dirtyRecords, rec, offsetComparator);
        if (index < 0)
            dirtyRecords.add(-(index + 1), rec);
    }

    public synchronized int purge() throws IOException {
        int[] count = {0};
        purge(findFirstRootDirectoryRecordInUse(false), count);
        return count[0];
    }

    private boolean purge(Attributes rec, int[] count) throws IOException {
        boolean purge = true;
        while (null != rec) {
            if (purge(findLowerDirectoryRecordInUse(rec, false), count)
                    && !rec.containsValue(Tag.ReferencedFileID)) {
                deleteRecord(rec);
                count[0]++;
            } else
                purge = false;
            rec = readNextDirectoryRecord(rec);
        }
        return purge;
    }

}
