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
 * Study-level hierarchical reference to series and SOP instances.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class HierarchicalSopInstanceReference extends DicomModule {

    /**
     * Creates a new instance.
     */
    public HierarchicalSopInstanceReference() {
        this(new Attributes());
    }

    /**
     * Creates a new instance.
     *
     * @param dcmItems the dcm items.
     */
    public HierarchicalSopInstanceReference(Attributes dcmItems) {
        super(dcmItems);
    }

    /**
     * Creates a value from the supplied input.
     *
     * @param sequence the sequence.
     * @return the operation result.
     */
    public static Collection<HierarchicalSopInstanceReference> fromSequence(Sequence sequence) {
        return mapSequence(sequence, HierarchicalSopInstanceReference::new);
    }

    /**
     * Gets the study instance uid.
     *
     * @return the study instance uid.
     */
    public String getStudyInstanceUID() {
        return dcmItems.getString(Tag.StudyInstanceUID);
    }

    /**
     * Sets the study instance uid.
     *
     * @param uid the uid.
     */
    public void setStudyInstanceUID(String uid) {
        dcmItems.setString(Tag.StudyInstanceUID, VR.UI, uid);
    }

    /**
     * Gets the referenced series.
     *
     * @return the referenced series.
     */
    public Collection<SeriesInstanceReference> getReferencedSeries() {
        return SeriesInstanceReference.fromSequence(dcmItems.getSequence(Tag.ReferencedSeriesSequence));
    }

    /**
     * Sets the referenced series.
     *
     * @param referencedSeries the referenced series.
     */
    public void setReferencedSeries(Collection<SeriesInstanceReference> referencedSeries) {
        updateSequence(Tag.ReferencedSeriesSequence, referencedSeries);
    }

}
