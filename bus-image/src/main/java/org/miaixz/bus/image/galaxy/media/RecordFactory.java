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
package org.miaixz.bus.image.galaxy.media;

import org.miaixz.bus.core.xyz.ResourceKit;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.Sequence;
import org.miaixz.bus.image.galaxy.data.VR;
import org.miaixz.bus.image.galaxy.io.ContentHandlerAdapter;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class RecordFactory {

    private static final int IN_USE = 0xffff;

    private EnumMap<RecordType, int[]> recordKeys;

    private HashMap<String, RecordType> recordTypes;

    private HashMap<String, String> privateRecordUIDs;

    private HashMap<String, int[]> privateRecordKeys;

    private void lazyLoadDefaultConfiguration() {
        if (recordTypes == null)
            loadDefaultConfiguration();
    }

    public void loadDefaultConfiguration() {
        try {
            loadConfiguration(ResourceKit.getResourceUrl("RecordFactory.xml", RecordFactory.class).toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void loadConfiguration(String uri)
            throws ParserConfigurationException, SAXException, IOException {
        Attributes attrs = parseXML(uri);
        Sequence sq = attrs.getSequence(Tag.DirectoryRecordSequence);
        if (sq == null)
            throw new IllegalArgumentException(
                    "Missing Directory Record Sequence in " + uri);

        EnumMap<RecordType, int[]> recordKeys = new EnumMap<>(
                RecordType.class);
        HashMap<String, RecordType> recordTypes = new HashMap<>(
                134);
        HashMap<String, String> privateRecordUIDs = new HashMap<>();
        HashMap<String, int[]> privateRecordKeys = new HashMap<>();
        for (Attributes item : sq) {
            RecordType type = RecordType.forCode(item.getString(
                    Tag.DirectoryRecordType, null));
            String privuid = type == RecordType.PRIVATE ? item.getString(
                    Tag.PrivateRecordUID, null) : null;
            String[] cuids = item.getStrings(Tag.ReferencedSOPClassUIDInFile);
            if (cuids != null) {
                if (type != RecordType.PRIVATE) {
                    for (String cuid : cuids) {
                        recordTypes.put(cuid, type);
                    }
                } else if (privuid != null) {
                    for (String cuid : cuids) {
                        privateRecordUIDs.put(cuid, privuid);
                    }
                }
            }
            item.remove(Tag.DirectoryRecordType);
            item.remove(Tag.PrivateRecordUID);
            item.remove(Tag.ReferencedSOPClassUIDInFile);
            int[] keys = item.tags();
            if (privuid != null) {
                if (privateRecordKeys.put(privuid, keys) != null)
                    throw new IllegalArgumentException(
                            "Duplicate Private Record UID: " + privuid);
            } else {
                if (recordKeys.put(type, keys) != null)
                    throw new IllegalArgumentException(
                            "Duplicate Record Type: " + type);
            }
        }
        EnumSet<RecordType> missingTypes = EnumSet.allOf(RecordType.class);
        missingTypes.removeAll(recordKeys.keySet());
        if (!missingTypes.isEmpty())
            throw new IllegalArgumentException("Missing Record Types: "
                    + missingTypes);
        this.recordTypes = recordTypes;
        this.recordKeys = recordKeys;
        this.privateRecordUIDs = privateRecordUIDs;
        this.privateRecordKeys = privateRecordKeys;
    }

    private Attributes parseXML(String uri)
            throws ParserConfigurationException, SAXException, IOException {
        Attributes attrs = new Attributes();
        SAXParserFactory f = SAXParserFactory.newInstance();
        SAXParser parser = f.newSAXParser();
        parser.parse(uri, new ContentHandlerAdapter(attrs));
        return attrs;
    }

    public RecordType getRecordType(String cuid) {
        if (cuid == null)
            throw new NullPointerException();
        lazyLoadDefaultConfiguration();
        RecordType recordType = recordTypes.get(cuid);
        return recordType != null ? recordType : RecordType.PRIVATE;
    }

    public RecordType setRecordType(String cuid, RecordType type) {
        if (cuid == null || type == null)
            throw new NullPointerException();
        lazyLoadDefaultConfiguration();
        return recordTypes.put(cuid, type);
    }

    public void setRecordKeys(RecordType type, int[] keys) {
        if (type == null)
            throw new NullPointerException();
        int[] tmp = keys.clone();
        Arrays.sort(tmp);
        lazyLoadDefaultConfiguration();
        recordKeys.put(type, keys);
    }

    public int[] getRecordKeys(RecordType type) {
        lazyLoadDefaultConfiguration();
        return recordKeys.get(type);
    }

    public String getPrivateRecordUID(String cuid) {
        if (cuid == null)
            throw new NullPointerException();

        lazyLoadDefaultConfiguration();
        String uid = privateRecordUIDs.get(cuid);
        return uid != null ? uid : cuid;
    }

    public String setPrivateRecordUID(String cuid, String uid) {
        if (cuid == null || uid == null)
            throw new NullPointerException();

        lazyLoadDefaultConfiguration();
        return privateRecordUIDs.put(cuid, uid);
    }

    public int[] setPrivateRecordKeys(String uid, int[] keys) {
        if (uid == null)
            throw new NullPointerException();

        int[] tmp = keys.clone();
        Arrays.sort(tmp);
        lazyLoadDefaultConfiguration();
        return privateRecordKeys.put(uid, tmp);
    }

    public Attributes createRecord(Attributes dataset, Attributes fmi,
                                   String[] fileIDs) {
        String cuid = fmi.getString(Tag.MediaStorageSOPClassUID, null);
        RecordType type = getRecordType(cuid);
        return createRecord(type,
                type == RecordType.PRIVATE ? getPrivateRecordUID(cuid) : null,
                dataset, fmi, fileIDs);
    }

    public Attributes createRecord(RecordType type, String privRecUID,
                                   Attributes dataset, Attributes fmi, String[] fileIDs) {
        if (type == null)
            throw new NullPointerException("type");
        if (dataset == null)
            throw new NullPointerException("dataset");

        lazyLoadDefaultConfiguration();
        int[] keys = null;
        if (type == RecordType.PRIVATE) {
            if (privRecUID == null)
                throw new NullPointerException(
                        "privRecUID must not be null for type = PRIVATE");
            keys = privateRecordKeys.get(privRecUID);
        } else {
            if (privRecUID != null)
                throw new IllegalArgumentException(
                        "privRecUID must be null for type != PRIVATE");
        }
        if (keys == null)
            keys = recordKeys.get(type);
        Attributes rec = new Attributes(keys.length + (fileIDs != null ? 9 : 5));
        rec.setInt(Tag.OffsetOfTheNextDirectoryRecord, VR.UL, 0);
        rec.setInt(Tag.RecordInUseFlag, VR.US, IN_USE);
        rec.setInt(Tag.OffsetOfReferencedLowerLevelDirectoryEntity, VR.UL, 0);
        rec.setString(Tag.DirectoryRecordType, VR.CS, type.code());
        if (privRecUID != null)
            rec.setString(Tag.PrivateRecordUID, VR.UI, privRecUID);
        if (fileIDs != null) {
            rec.setString(Tag.ReferencedFileID, VR.CS, fileIDs);
            rec.setString(Tag.ReferencedSOPClassUIDInFile, VR.UI,
                    fmi.getString(Tag.MediaStorageSOPClassUID, null));
            rec.setString(Tag.ReferencedSOPInstanceUIDInFile, VR.UI,
                    fmi.getString(Tag.MediaStorageSOPInstanceUID, null));
            rec.setString(Tag.ReferencedTransferSyntaxUIDInFile, VR.UI,
                    fmi.getString(Tag.TransferSyntaxUID, null));
        }
        rec.addSelected(dataset, keys, 0, keys.length);
        Sequence contentSeq = dataset.getSequence(Tag.ContentSequence);
        if (contentSeq != null)
            copyConceptMod(contentSeq, rec);
        return rec;
    }

    private void copyConceptMod(Sequence srcSeq, Attributes rec) {
        Sequence dstSeq = null;
        for (Attributes item : srcSeq) {
            if ("HAS CONCEPT MOD".equals(item.getString(Tag.RelationshipType,
                    null))) {
                if (dstSeq == null)
                    dstSeq = rec.newSequence(Tag.ContentSequence, 1);
                dstSeq.add(new Attributes(item, false));
            }
        }
    }

}
