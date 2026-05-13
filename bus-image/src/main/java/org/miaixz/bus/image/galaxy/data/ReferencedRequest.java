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
 * Referenced request sequence item for procedure traceability.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ReferencedRequest extends DicomModule {

    /**
     * Creates a new instance.
     */
    public ReferencedRequest() {
        this(new Attributes());
    }

    /**
     * Creates a new instance.
     *
     * @param dcmItems the dcm items.
     */
    public ReferencedRequest(Attributes dcmItems) {
        super(dcmItems);
    }

    /**
     * Creates a value from the supplied input.
     *
     * @param sequence the sequence.
     * @return the operation result.
     */
    public static Collection<ReferencedRequest> fromSequence(Sequence sequence) {
        return mapSequence(sequence, ReferencedRequest::new);
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
     * @param studyUID the study uid.
     */
    public void setStudyInstanceUID(String studyUID) {
        dcmItems.setString(Tag.StudyInstanceUID, VR.UI, studyUID);
    }

    /**
     * Gets the referenced study sop instance.
     *
     * @return the referenced study sop instance.
     */
    public SopInstanceReference getReferencedStudySOPInstance() {
        Attributes item = dcmItems.getNestedDataset(Tag.ReferencedStudySequence);
        return item == null ? null : new SopInstanceReference(item);
    }

    /**
     * Sets the referenced study sop instance.
     *
     * @param referencedStudy the referenced study.
     */
    public void setReferencedStudySOPInstance(SopInstanceReference referencedStudy) {
        updateSequence(Tag.ReferencedStudySequence, referencedStudy);
    }

    /**
     * Gets the accession number.
     *
     * @return the accession number.
     */
    public String getAccessionNumber() {
        return dcmItems.getString(Tag.AccessionNumber);
    }

    /**
     * Sets the accession number.
     *
     * @param accessionNumber the accession number.
     */
    public void setAccessionNumber(String accessionNumber) {
        dcmItems.setString(Tag.AccessionNumber, VR.SH, accessionNumber);
    }

    /**
     * Gets the placer order number imaging service request.
     *
     * @return the placer order number imaging service request.
     */
    public String getPlacerOrderNumberImagingServiceRequest() {
        return dcmItems.getString(Tag.PlacerOrderNumberImagingServiceRequest);
    }

    /**
     * Sets the placer order number imaging service request.
     *
     * @param placerOrderNumber the placer order number.
     */
    public void setPlacerOrderNumberImagingServiceRequest(String placerOrderNumber) {
        dcmItems.setString(Tag.PlacerOrderNumberImagingServiceRequest, VR.LO, placerOrderNumber);
    }

    /**
     * Gets the filler order number imaging service request.
     *
     * @return the filler order number imaging service request.
     */
    public String getFillerOrderNumberImagingServiceRequest() {
        return dcmItems.getString(Tag.FillerOrderNumberImagingServiceRequest);
    }

    /**
     * Sets the filler order number imaging service request.
     *
     * @param fillerOrderNumber the filler order number.
     */
    public void setFillerOrderNumberImagingServiceRequest(String fillerOrderNumber) {
        dcmItems.setString(Tag.FillerOrderNumberImagingServiceRequest, VR.LO, fillerOrderNumber);
    }

    /**
     * Gets the requested procedure id.
     *
     * @return the requested procedure id.
     */
    public String getRequestedProcedureID() {
        return dcmItems.getString(Tag.RequestedProcedureID);
    }

    /**
     * Sets the requested procedure id.
     *
     * @param procedureId the procedure id.
     */
    public void setRequestedProcedureID(String procedureId) {
        dcmItems.setString(Tag.RequestedProcedureID, VR.SH, procedureId);
    }

    /**
     * Gets the requested procedure description.
     *
     * @return the requested procedure description.
     */
    public String getRequestedProcedureDescription() {
        return dcmItems.getString(Tag.RequestedProcedureDescription);
    }

    /**
     * Sets the requested procedure description.
     *
     * @param description the description.
     */
    public void setRequestedProcedureDescription(String description) {
        dcmItems.setString(Tag.RequestedProcedureDescription, VR.LO, description);
    }

    /**
     * Gets the requested procedure code.
     *
     * @return the requested procedure code.
     */
    public Code getRequestedProcedureCode() {
        return nestedCode(dcmItems, Tag.RequestedProcedureCodeSequence);
    }

    /**
     * Sets the requested procedure code.
     *
     * @param procedureCode the procedure code.
     */
    public void setRequestedProcedureCode(Code procedureCode) {
        updateCodeSequence(Tag.RequestedProcedureCodeSequence, procedureCode);
    }

    /**
     * Gets the reason for the requested procedure.
     *
     * @return the reason for the requested procedure.
     */
    public String getReasonForTheRequestedProcedure() {
        return dcmItems.getString(Tag.ReasonForTheRequestedProcedure);
    }

    /**
     * Sets the reason for the requested procedure.
     *
     * @param reason the reason.
     */
    public void setReasonForTheRequestedProcedure(String reason) {
        dcmItems.setString(Tag.ReasonForTheRequestedProcedure, VR.LO, reason);
    }

    /**
     * Gets the reason for requested procedure code.
     *
     * @return the reason for requested procedure code.
     */
    public Code getReasonForRequestedProcedureCode() {
        return nestedCode(dcmItems, Tag.ReasonForRequestedProcedureCodeSequence);
    }

    /**
     * Sets the reason for requested procedure code.
     *
     * @param reasonCode the reason code.
     */
    public void setReasonForRequestedProcedureCode(Code reasonCode) {
        updateCodeSequence(Tag.ReasonForRequestedProcedureCodeSequence, reasonCode);
    }

}
