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
package org.aoju.bus.image.plugin;

import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.image.Builder;
import org.aoju.bus.image.Tag;
import org.aoju.bus.image.UID;
import org.aoju.bus.image.galaxy.data.Attributes;
import org.aoju.bus.image.galaxy.data.Sequence;
import org.aoju.bus.image.galaxy.data.VR;
import org.aoju.bus.image.galaxy.io.ImageEncodingOptions;
import org.aoju.bus.image.galaxy.io.ImageOutputStream;

import java.io.BufferedOutputStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class MkKOS {

    private static final int[] PATIENT_AND_STUDY_ATTRS = {
            Tag.SpecificCharacterSet,
            Tag.StudyDate,
            Tag.StudyTime,
            Tag.AccessionNumber,
            Tag.IssuerOfAccessionNumberSequence,
            Tag.ReferringPhysicianName,
            Tag.PatientName,
            Tag.PatientID,
            Tag.IssuerOfPatientID,
            Tag.PatientBirthDate,
            Tag.PatientSex,
            Tag.StudyInstanceUID,
            Tag.StudyID
    };
    private final Attributes attrs = new Attributes();
    private String uidSuffix;
    private String fname;
    private boolean nofmi;
    private ImageEncodingOptions encOpts;
    private String tsuid;
    private String seriesNumber;
    private String instanceNumber;
    private String keyObjectDescription;
    private String retrieveAET;
    private String retrieveURL;
    private String locationUID;
    private Attributes documentTitle;
    private Attributes documentTitleModifier;
    private Properties codes;

    private Attributes kos;
    private Sequence evidenceSeq;
    private Sequence contentSeq;

    public String getFname() {
        return fname;
    }

    public final void setUIDSuffix(String uidSuffix) {
        this.uidSuffix = uidSuffix;
    }

    public void setOutputFile(String fname) {
        this.fname = fname;
    }

    public void setNoFileMetaInformation(boolean nofmi) {
        this.nofmi = nofmi;
    }

    public final void setEncodingOptions(ImageEncodingOptions encOpts) {
        this.encOpts = encOpts;
    }

    public final void setTransferSyntax(String tsuid) {
        this.tsuid = tsuid;
    }

    public final void setSeriesNumber(String seriesNumber) {
        this.seriesNumber = seriesNumber;
    }

    public final void setInstanceNumber(String instanceNumber) {
        this.instanceNumber = instanceNumber;
    }

    public final void setKeyObjectDescription(String keyObjectDescription) {
        this.keyObjectDescription = keyObjectDescription;
    }

    public void setRetrieveAET(String retrieveAET) {
        this.retrieveAET = retrieveAET;
    }

    public void setRetrieveURL(String retrieveURL) {
        this.retrieveURL = retrieveURL;
    }

    public void setLocationUID(String locationUID) {
        this.locationUID = locationUID;
    }

    public final void setCodes(Properties codes) {
        this.codes = codes;
    }

    public final void setDocumentTitle(Attributes codeItem) {
        this.documentTitle = codeItem;
    }

    public final void setDocumentTitleModifier(Attributes codeItem) {
        this.documentTitleModifier = codeItem;
    }

    public Attributes toCodeItem(String codeValue) {
        if (null == codes)
            throw new IllegalStateException("codec not initialized");
        String codeMeaning = codes.getProperty(codeValue);
        if (null == codeMeaning)
            throw new IllegalArgumentException("undefined internal value: "
                    + codeValue);
        int endDesignator = codeValue.indexOf(Symbol.C_MINUS);
        Attributes attrs = new Attributes(3);
        attrs.setString(Tag.CodeValue, VR.SH,
                endDesignator >= 0
                        ? codeValue.substring(endDesignator + 1)
                        : codeValue);
        attrs.setString(Tag.CodingSchemeDesignator, VR.SH,
                endDesignator >= 0
                        ? codeValue.substring(0, endDesignator)
                        : "DCM");
        attrs.setString(Tag.CodeMeaning, VR.LO, codeMeaning);
        return attrs;
    }

    public boolean addInstance(Attributes inst) {
        Builder.updateAttributes(inst, attrs, uidSuffix);
        String studyIUID = inst.getString(Tag.StudyInstanceUID);
        String seriesIUID = inst.getString(Tag.SeriesInstanceUID);
        String iuid = inst.getString(Tag.SOPInstanceUID);
        String cuid = inst.getString(Tag.SOPClassUID);
        if (null == studyIUID || null == seriesIUID || null == iuid || null == cuid)
            return false;
        if (null == kos)
            kos = createKOS(inst);
        refSOPSeq(refSeriesSeq(studyIUID), seriesIUID).add(refSOP(cuid, iuid));
        contentSeq.add(contentItem(valueTypeOf(inst), refSOP(cuid, iuid)));
        return true;
    }

    public void writeKOS() throws IOException {
        ImageOutputStream dos = new ImageOutputStream(
                new BufferedOutputStream(null != fname
                        ? new FileOutputStream(fname)
                        : new FileOutputStream(FileDescriptor.out)),
                nofmi ? UID.ImplicitVRLittleEndian
                        : UID.ExplicitVRLittleEndian);
        dos.setEncodingOptions(encOpts);
        try {
            dos.writeDataset(
                    nofmi ? null : kos.createFileMetaInformation(tsuid),
                    kos);
        } finally {
            dos.close();
        }
    }

    private Sequence refSeriesSeq(String studyIUID) {
        for (Attributes refStudy : evidenceSeq)
            if (studyIUID.equals(refStudy.getString(Tag.StudyInstanceUID)))
                return refStudy.getSequence(Tag.ReferencedSeriesSequence);

        Attributes refStudy = new Attributes(2);
        Sequence refSeriesSeq = refStudy.newSequence(Tag.ReferencedSeriesSequence, 10);
        refStudy.setString(Tag.StudyInstanceUID, VR.UI, studyIUID);
        evidenceSeq.add(refStudy);
        return refSeriesSeq;
    }

    private Sequence refSOPSeq(Sequence refSeriesSeq, String seriesIUID) {
        for (Attributes refSeries : refSeriesSeq)
            if (seriesIUID.equals(refSeries.getString(Tag.SeriesInstanceUID)))
                return refSeries.getSequence(Tag.ReferencedSOPSequence);

        Attributes refSeries = new Attributes(5);
        if (null != retrieveAET)
            refSeries.setString(Tag.RetrieveAETitle, VR.AE, retrieveAET);
        if (null != retrieveURL)
            refSeries.setString(Tag.RetrieveURL, VR.UR, retrieveURL);
        Sequence refSOPSeq = refSeries.newSequence(Tag.ReferencedSOPSequence, 100);
        refSeries.setString(Tag.SeriesInstanceUID, VR.UI, seriesIUID);
        if (null != locationUID)
            refSeries.setString(Tag.RetrieveLocationUID, VR.UI, locationUID);
        refSeriesSeq.add(refSeries);
        return refSOPSeq;
    }

    private String valueTypeOf(Attributes inst) {
        return inst.contains(Tag.PhotometricInterpretation) ? "IMAGE"
                : inst.contains(Tag.WaveformSequence) ? "WAVEFORM"
                : "COMPOSITE";
    }

    private Attributes refSOP(String cuid, String iuid) {
        Attributes item = new Attributes(2);
        item.setString(Tag.ReferencedSOPClassUID, VR.UI, cuid);
        item.setString(Tag.ReferencedSOPInstanceUID, VR.UI, iuid);
        return item;
    }

    private Attributes createKOS(Attributes inst) {
        Attributes attrs = new Attributes(inst, PATIENT_AND_STUDY_ATTRS);
        attrs.setString(Tag.SOPClassUID, VR.UI, UID.KeyObjectSelectionDocumentStorage);
        attrs.setString(Tag.SOPInstanceUID, VR.UI, UID.createUID());
        attrs.setDate(Tag.ContentDateAndTime, new Date());
        attrs.setString(Tag.Modality, VR.CS, "KO");
        attrs.setNull(Tag.ReferencedPerformedProcedureStepSequence, VR.SQ);
        attrs.setString(Tag.SeriesInstanceUID, VR.UI, UID.createUID());
        attrs.setString(Tag.SeriesNumber, VR.IS, seriesNumber);
        attrs.setString(Tag.InstanceNumber, VR.IS, instanceNumber);
        attrs.setString(Tag.ValueType, VR.CS, "CONTAINER");
        attrs.setString(Tag.ContinuityOfContent, VR.CS, "SEPARATE");
        attrs.newSequence(Tag.ConceptNameCodeSequence, 1).add(documentTitle);
        evidenceSeq = attrs.newSequence(Tag.CurrentRequestedProcedureEvidenceSequence, 1);
        attrs.newSequence(Tag.ContentTemplateSequence, 1).add(templateIdentifier());
        contentSeq = attrs.newSequence(Tag.ContentSequence, 1);
        if (null != documentTitleModifier) {
            contentSeq.add(documentTitleModifier());
        }

        if (null != keyObjectDescription) {
            contentSeq.add(keyObjectDescription());
        }
        return attrs;
    }

    private Attributes templateIdentifier() {
        Attributes attrs = new Attributes(2);
        attrs.setString(Tag.MappingResource, VR.CS, "DCMR");
        attrs.setString(Tag.TemplateIdentifier, VR.CS, "2010");
        return attrs;
    }

    private Attributes documentTitleModifier() {
        Attributes item = new Attributes(4);
        item.setString(Tag.RelationshipType, VR.CS, "HAS CONCEPT MOD");
        item.setString(Tag.ValueType, VR.CS, "CODE");
        item.newSequence(Tag.ConceptNameCodeSequence, 1).add(toCodeItem("DCM-113011"));
        item.newSequence(Tag.ConceptCodeSequence, 1).add(documentTitleModifier);
        return item;
    }

    private Attributes keyObjectDescription() {
        Attributes item = new Attributes(4);
        item.setString(Tag.RelationshipType, VR.CS, "CONTAINS");
        item.setString(Tag.ValueType, VR.CS, "TEXT");
        item.newSequence(Tag.ConceptNameCodeSequence, 1).add(toCodeItem("DCM-113012"));
        item.setString(Tag.TextValue, VR.UT, keyObjectDescription);
        return item;
    }

    private Attributes contentItem(String valueType, Attributes refSOP) {
        Attributes item = new Attributes(3);
        item.setString(Tag.RelationshipType, VR.CS, "CONTAINS");
        item.setString(Tag.ValueType, VR.CS, valueType);
        item.newSequence(Tag.ReferencedSOPSequence, 1).add(refSOP);
        return item;
    }

}
