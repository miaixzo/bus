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
package org.miaixz.bus.image.galaxy.data;

import java.util.Collection;

import org.miaixz.bus.image.Tag;

/**
 * Series-level reference containing referenced SOP instances.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class SeriesInstanceReference extends DicomModule {

    /**
     * Creates a new instance.
     */
    public SeriesInstanceReference() {
        this(new Attributes());
    }

    /**
     * Creates a new instance.
     *
     * @param dcmItems the dcm items.
     */
    public SeriesInstanceReference(Attributes dcmItems) {
        super(dcmItems);
    }

    /**
     * Creates a value from the supplied input.
     *
     * @param sequence the sequence.
     * @return the operation result.
     */
    public static Collection<SeriesInstanceReference> fromSequence(Sequence sequence) {
        return mapSequence(sequence, SeriesInstanceReference::new);
    }

    /**
     * Gets the series instance uid.
     *
     * @return the series instance uid.
     */
    public String getSeriesInstanceUID() {
        return dcmItems.getString(Tag.SeriesInstanceUID);
    }

    /**
     * Sets the series instance uid.
     *
     * @param seriesUID the series uid.
     */
    public void setSeriesInstanceUID(String seriesUID) {
        dcmItems.setString(Tag.SeriesInstanceUID, VR.UI, seriesUID);
    }

    /**
     * Gets the retrieve ae title.
     *
     * @return the retrieve ae title.
     */
    public String getRetrieveAETitle() {
        return dcmItems.getString(Tag.RetrieveAETitle);
    }

    /**
     * Sets the retrieve ae title.
     *
     * @param aeTitle the ae title.
     */
    public void setRetrieveAETitle(String aeTitle) {
        dcmItems.setString(Tag.RetrieveAETitle, VR.AE, aeTitle);
    }

    /**
     * Gets the storage media file set id.
     *
     * @return the storage media file set id.
     */
    public String getStorageMediaFileSetID() {
        return dcmItems.getString(Tag.StorageMediaFileSetID);
    }

    /**
     * Sets the storage media file set id.
     *
     * @param fileSetId the file set id.
     */
    public void setStorageMediaFileSetID(String fileSetId) {
        dcmItems.setString(Tag.StorageMediaFileSetID, VR.SH, fileSetId);
    }

    /**
     * Gets the storage media file set uid.
     *
     * @return the storage media file set uid.
     */
    public String getStorageMediaFileSetUID() {
        return dcmItems.getString(Tag.StorageMediaFileSetUID);
    }

    /**
     * Sets the storage media file set uid.
     *
     * @param fileSetUID the file set uid.
     */
    public void setStorageMediaFileSetUID(String fileSetUID) {
        dcmItems.setString(Tag.StorageMediaFileSetUID, VR.UI, fileSetUID);
    }

    /**
     * Gets the referenced sop instances.
     *
     * @return the referenced sop instances.
     */
    public Collection<SopInstanceReferenceAndMac> getReferencedSOPInstances() {
        return SopInstanceReferenceAndMac.fromMacSequence(dcmItems.getSequence(Tag.ReferencedSOPSequence));
    }

    /**
     * Sets the referenced sop instances.
     *
     * @param referencedInstances the referenced instances.
     */
    public void setReferencedSOPInstances(Collection<SopInstanceReferenceAndMac> referencedInstances) {
        updateSequence(Tag.ReferencedSOPSequence, referencedInstances);
    }

}
