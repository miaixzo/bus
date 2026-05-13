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
import java.util.Date;

import org.miaixz.bus.image.Tag;

/**
 * DICOM Key Object Selection document module.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class KeyObjectDocumentModule extends DicomModule {

    /**
     * Creates a new instance.
     */
    public KeyObjectDocumentModule() {
        this(new Attributes());
    }

    /**
     * Creates a new instance.
     *
     * @param dcmItems the dcm items.
     */
    public KeyObjectDocumentModule(Attributes dcmItems) {
        super(dcmItems);
    }

    /**
     * Gets the instance number.
     *
     * @return the instance number.
     */
    public String getInstanceNumber() {
        return dcmItems.getString(Tag.InstanceNumber);
    }

    /**
     * Sets the instance number.
     *
     * @param instanceNumber the instance number.
     */
    public void setInstanceNumber(String instanceNumber) {
        dcmItems.setString(Tag.InstanceNumber, VR.IS, instanceNumber);
    }

    /**
     * Gets the content date time.
     *
     * @return the content date time.
     */
    public Date getContentDateTime() {
        return dcmItems.getDate(Tag.ContentDateAndTime);
    }

    /**
     * Sets the content date time.
     *
     * @param dateTime the date time.
     */
    public void setContentDateTime(Date dateTime) {
        dcmItems.setDate(Tag.ContentDateAndTime, dateTime);
    }

    /**
     * Gets the referenced requests.
     *
     * @return the referenced requests.
     */
    public Collection<ReferencedRequest> getReferencedRequests() {
        return ReferencedRequest.fromSequence(dcmItems.getSequence(Tag.ReferencedRequestSequence));
    }

    /**
     * Sets the referenced requests.
     *
     * @param referencedRequests the referenced requests.
     */
    public void setReferencedRequests(Collection<ReferencedRequest> referencedRequests) {
        updateSequence(Tag.ReferencedRequestSequence, referencedRequests);
    }

    /**
     * Gets the current requested procedure evidences.
     *
     * @return the current requested procedure evidences.
     */
    public Collection<HierarchicalSopInstanceReference> getCurrentRequestedProcedureEvidences() {
        return HierarchicalSopInstanceReference
                .fromSequence(dcmItems.getSequence(Tag.CurrentRequestedProcedureEvidenceSequence));
    }

    /**
     * Sets the current requested procedure evidences.
     *
     * @param evidenceReferences the evidence references.
     */
    public void setCurrentRequestedProcedureEvidences(Collection<HierarchicalSopInstanceReference> evidenceReferences) {
        updateSequence(Tag.CurrentRequestedProcedureEvidenceSequence, evidenceReferences);
    }

    /**
     * Gets the identical documents.
     *
     * @return the identical documents.
     */
    public Collection<HierarchicalSopInstanceReference> getIdenticalDocuments() {
        return HierarchicalSopInstanceReference.fromSequence(dcmItems.getSequence(Tag.IdenticalDocumentsSequence));
    }

    /**
     * Sets the identical documents.
     *
     * @param identicalReferences the identical references.
     */
    public void setIdenticalDocuments(Collection<HierarchicalSopInstanceReference> identicalReferences) {
        updateSequence(Tag.IdenticalDocumentsSequence, identicalReferences);
    }

}
