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

import java.io.IOException;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import org.miaixz.bus.core.xyz.ResourceKit;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.Sequence;
import org.miaixz.bus.image.galaxy.data.VR;
import org.miaixz.bus.image.galaxy.io.ContentHandlerAdapter;

/**
 * Represents the RecordFactory type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class RecordFactory {

    /**
     * Constructs a new {@code RecordFactory} instance.
     */
    public RecordFactory() {
        // No initialization required.
    }

    /**
     * The in use value.
     */
    private static final int IN_USE = 0xffff;

    /**
     * The record keys value.
     */
    private EnumMap<RecordType, int[]> recordKeys;

    /**
     * The record types value.
     */
    private HashMap<String, RecordType> recordTypes;

    /**
     * The private record ui ds value.
     */
    private HashMap<String, String> privateRecordUIDs;

    /**
     * The private record keys value.
     */
    private HashMap<String, int[]> privateRecordKeys;

    /**
     * Executes the lazy load default configuration operation.
     */
    private void lazyLoadDefaultConfiguration() {
        if (recordTypes == null)
            loadDefaultConfiguration();
    }

    /**
     * Loads the default configuration.
     */
    public void loadDefaultConfiguration() {
        try {
            loadConfiguration(ResourceKit.getResourceUrl("RecordFactory.xml", RecordFactory.class).toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Loads the configuration.
     *
     * @param uri the uri.
     * @throws ParserConfigurationException if the operation cannot be completed.
     * @throws SAXException                 if the operation cannot be completed.
     * @throws IOException                  if the operation cannot be completed.
     */
    public void loadConfiguration(String uri) throws ParserConfigurationException, SAXException, IOException {
        Attributes attrs = parseXML(uri);
        Sequence sq = attrs.getSequence(Tag.DirectoryRecordSequence);
        if (sq == null)
            throw new IllegalArgumentException("Missing Directory Record Sequence in " + uri);

        EnumMap<RecordType, int[]> recordKeys = new EnumMap<>(RecordType.class);
        HashMap<String, RecordType> recordTypes = new HashMap<>(134);
        HashMap<String, String> privateRecordUIDs = new HashMap<>();
        HashMap<String, int[]> privateRecordKeys = new HashMap<>();
        for (Attributes item : sq) {
            RecordType type = RecordType.forCode(item.getString(Tag.DirectoryRecordType, null));
            String privuid = type == RecordType.PRIVATE ? item.getString(Tag.PrivateRecordUID, null) : null;
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
                    throw new IllegalArgumentException("Duplicate Private Record UID: " + privuid);
            } else {
                if (recordKeys.put(type, keys) != null)
                    throw new IllegalArgumentException("Duplicate Record Type: " + type);
            }
        }
        EnumSet<RecordType> missingTypes = EnumSet.allOf(RecordType.class);
        missingTypes.removeAll(recordKeys.keySet());
        if (!missingTypes.isEmpty())
            throw new IllegalArgumentException("Missing Record Types: " + missingTypes);
        this.recordTypes = recordTypes;
        this.recordKeys = recordKeys;
        this.privateRecordUIDs = privateRecordUIDs;
        this.privateRecordKeys = privateRecordKeys;
    }

    /**
     * Parses the xml.
     *
     * @param uri the uri.
     * @return the operation result.
     * @throws ParserConfigurationException if the operation cannot be completed.
     * @throws SAXException                 if the operation cannot be completed.
     * @throws IOException                  if the operation cannot be completed.
     */
    private Attributes parseXML(String uri) throws ParserConfigurationException, SAXException, IOException {
        Attributes attrs = new Attributes();
        SAXParserFactory f = SAXParserFactory.newInstance();
        SAXParser parser = f.newSAXParser();
        parser.parse(uri, new ContentHandlerAdapter(attrs));
        return attrs;
    }

    /**
     * Gets the record type.
     *
     * @param cuid the cuid.
     * @return the record type.
     */
    public RecordType getRecordType(String cuid) {
        if (cuid == null)
            throw new NullPointerException();
        lazyLoadDefaultConfiguration();
        RecordType recordType = recordTypes.get(cuid);
        return recordType != null ? recordType : RecordType.PRIVATE;
    }

    /**
     * Sets the record type.
     *
     * @param cuid the cuid.
     * @param type the type.
     * @return the operation result.
     */
    public RecordType setRecordType(String cuid, RecordType type) {
        if (cuid == null || type == null)
            throw new NullPointerException();
        lazyLoadDefaultConfiguration();
        return recordTypes.put(cuid, type);
    }

    /**
     * Sets the record keys.
     *
     * @param type the type.
     * @param keys the keys.
     */
    public void setRecordKeys(RecordType type, int[] keys) {
        if (type == null)
            throw new NullPointerException();
        int[] tmp = keys.clone();
        Arrays.sort(tmp);
        lazyLoadDefaultConfiguration();
        recordKeys.put(type, keys);
    }

    /**
     * Gets the record keys.
     *
     * @param type the type.
     * @return the record keys.
     */
    public int[] getRecordKeys(RecordType type) {
        lazyLoadDefaultConfiguration();
        return recordKeys.get(type);
    }

    /**
     * Gets the private record uid.
     *
     * @param cuid the cuid.
     * @return the private record uid.
     */
    public String getPrivateRecordUID(String cuid) {
        if (cuid == null)
            throw new NullPointerException();

        lazyLoadDefaultConfiguration();
        String uid = privateRecordUIDs.get(cuid);
        return uid != null ? uid : cuid;
    }

    /**
     * Sets the private record uid.
     *
     * @param cuid the cuid.
     * @param uid  the uid.
     * @return the operation result.
     */
    public String setPrivateRecordUID(String cuid, String uid) {
        if (cuid == null || uid == null)
            throw new NullPointerException();

        lazyLoadDefaultConfiguration();
        return privateRecordUIDs.put(cuid, uid);
    }

    /**
     * Sets the private record keys.
     *
     * @param uid  the uid.
     * @param keys the keys.
     * @return the operation result.
     */
    public int[] setPrivateRecordKeys(String uid, int[] keys) {
        if (uid == null)
            throw new NullPointerException();

        int[] tmp = keys.clone();
        Arrays.sort(tmp);
        lazyLoadDefaultConfiguration();
        return privateRecordKeys.put(uid, tmp);
    }

    /**
     * Creates the record.
     *
     * @param dataset the dataset.
     * @param fmi     the fmi.
     * @param fileIDs the file i ds.
     * @return the operation result.
     */
    public Attributes createRecord(Attributes dataset, Attributes fmi, String[] fileIDs) {
        String cuid = fmi.getString(Tag.MediaStorageSOPClassUID, null);
        RecordType type = getRecordType(cuid);
        return createRecord(type, type == RecordType.PRIVATE ? getPrivateRecordUID(cuid) : null, dataset, fmi, fileIDs);
    }

    /**
     * Creates the record.
     *
     * @param type       the type.
     * @param privRecUID the priv rec uid.
     * @param dataset    the dataset.
     * @param fmi        the fmi.
     * @param fileIDs    the file i ds.
     * @return the operation result.
     */
    public Attributes createRecord(
            RecordType type,
            String privRecUID,
            Attributes dataset,
            Attributes fmi,
            String[] fileIDs) {
        if (type == null)
            throw new NullPointerException("type");
        if (dataset == null)
            throw new NullPointerException("dataset");

        lazyLoadDefaultConfiguration();
        int[] keys = null;
        if (type == RecordType.PRIVATE) {
            if (privRecUID == null)
                throw new NullPointerException("privRecUID must not be null for type = PRIVATE");
            keys = privateRecordKeys.get(privRecUID);
        } else {
            if (privRecUID != null)
                throw new IllegalArgumentException("privRecUID must be null for type != PRIVATE");
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
            rec.setString(Tag.ReferencedSOPClassUIDInFile, VR.UI, fmi.getString(Tag.MediaStorageSOPClassUID, null));
            rec.setString(
                    Tag.ReferencedSOPInstanceUIDInFile,
                    VR.UI,
                    fmi.getString(Tag.MediaStorageSOPInstanceUID, null));
            rec.setString(Tag.ReferencedTransferSyntaxUIDInFile, VR.UI, fmi.getString(Tag.TransferSyntaxUID, null));
        }
        rec.addSelected(dataset, keys, 0, keys.length);
        Sequence contentSeq = dataset.getSequence(Tag.ContentSequence);
        if (contentSeq != null)
            copyConceptMod(contentSeq, rec);
        return rec;
    }

    /**
     * Copies the concept mod.
     *
     * @param srcSeq the src seq.
     * @param rec    the rec.
     */
    private void copyConceptMod(Sequence srcSeq, Attributes rec) {
        Sequence dstSeq = null;
        for (Attributes item : srcSeq) {
            if ("HAS CONCEPT MOD".equals(item.getString(Tag.RelationshipType, null))) {
                if (dstSeq == null)
                    dstSeq = rec.newSequence(Tag.ContentSequence, 1);
                dstSeq.add(new Attributes(item, false));
            }
        }
    }

}
