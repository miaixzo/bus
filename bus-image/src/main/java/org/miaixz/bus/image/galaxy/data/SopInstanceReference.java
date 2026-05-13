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
 * DICOM SOP instance reference backed by {@link Attributes}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class SopInstanceReference extends DicomModule {

    /**
     * Creates a new instance.
     */
    public SopInstanceReference() {
        this(new Attributes());
    }

    /**
     * Creates a new instance.
     *
     * @param dcmItems the dcm items.
     */
    public SopInstanceReference(Attributes dcmItems) {
        super(dcmItems);
    }

    /**
     * Creates a value from the supplied input.
     *
     * @param sequence the sequence.
     * @return the operation result.
     */
    public static Collection<SopInstanceReference> fromSequence(Sequence sequence) {
        return mapSequence(sequence, SopInstanceReference::new);
    }

    /**
     * Gets the referenced frame number.
     *
     * @return the referenced frame number.
     */
    public int[] getReferencedFrameNumber() {
        return dcmItems.getInts(Tag.ReferencedFrameNumber);
    }

    /**
     * Sets the referenced frame number.
     *
     * @param frameNumbers the frame numbers.
     */
    public void setReferencedFrameNumber(int... frameNumbers) {
        dcmItems.setInt(Tag.ReferencedFrameNumber, VR.IS, frameNumbers);
    }

    /**
     * Gets the referenced sop instance uid.
     *
     * @return the referenced sop instance uid.
     */
    public String getReferencedSOPInstanceUID() {
        return dcmItems.getString(Tag.ReferencedSOPInstanceUID);
    }

    /**
     * Sets the referenced sop instance uid.
     *
     * @param instanceUID the instance uid.
     */
    public void setReferencedSOPInstanceUID(String instanceUID) {
        dcmItems.setString(Tag.ReferencedSOPInstanceUID, VR.UI, instanceUID);
    }

    /**
     * Gets the referenced sop class uid.
     *
     * @return the referenced sop class uid.
     */
    public String getReferencedSOPClassUID() {
        return dcmItems.getString(Tag.ReferencedSOPClassUID);
    }

    /**
     * Sets the referenced sop class uid.
     *
     * @param classUID the class uid.
     */
    public void setReferencedSOPClassUID(String classUID) {
        dcmItems.setString(Tag.ReferencedSOPClassUID, VR.UI, classUID);
    }

    /**
     * Gets the instance number.
     *
     * @return the instance number.
     */
    public Integer getInstanceNumber() {
        return dcmItems.contains(Tag.InstanceNumber) ? dcmItems.getInt(Tag.InstanceNumber, 0) : null;
    }

    /**
     * Sets the instance number.
     *
     * @param instanceNumber the instance number.
     */
    public void setInstanceNumber(Integer instanceNumber) {
        if (instanceNumber != null) {
            dcmItems.setInt(Tag.InstanceNumber, VR.IS, instanceNumber);
        }
    }

}
