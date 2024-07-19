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
package org.miaixz.bus.image.plugin;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.image.Format;
import org.miaixz.bus.image.Status;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.builtin.DicomFiles;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.ElementDictionary;
import org.miaixz.bus.image.galaxy.data.Sequence;
import org.miaixz.bus.image.galaxy.data.VR;
import org.miaixz.bus.image.metric.Association;
import org.miaixz.bus.image.metric.Connection;
import org.miaixz.bus.image.metric.DimseRSPHandler;
import org.miaixz.bus.image.metric.net.ApplicationEntity;
import org.miaixz.bus.image.metric.pdu.AAssociateRQ;
import org.miaixz.bus.image.metric.pdu.PresentationContext;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.DecimalFormat;
import java.util.*;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class MppsSCU {

    private static final ElementDictionary dict =
            ElementDictionary.getStandardElementDictionary();
    private static final String IN_PROGRESS = "IN PROGRESS";
    private static final String COMPLETED = "COMPLETED";
    private static final String DISCONTINUED = "DISCONTINUED";
    private static final int[] MPPS_TOP_LEVEL_ATTRS = {
            Tag.SpecificCharacterSet,
            Tag.Modality,
            Tag.ProcedureCodeSequence,
            Tag.ReferencedPatientSequence,
            Tag.PatientName,
            Tag.PatientID,
            Tag.IssuerOfPatientID,
            Tag.IssuerOfPatientIDQualifiersSequence,
            Tag.PatientBirthDate,
            Tag.PatientSex,
            Tag.StudyID,
            Tag.AdmissionID,
            Tag.IssuerOfAdmissionIDSequence,
            Tag.ServiceEpisodeID,
            Tag.IssuerOfServiceEpisodeID,
            Tag.ServiceEpisodeDescription,
            Tag.PerformedProcedureStepStartDate,
            Tag.PerformedProcedureStepStartTime,
            Tag.PerformedProcedureStepID,
            Tag.PerformedProcedureStepDescription,
            Tag.PerformedProtocolCodeSequence,
            Tag.CommentsOnThePerformedProcedureStep,
    };
    private static final int[] MPPS_TOP_LEVEL_TYPE_2_ATTRS = {
            Tag.ProcedureCodeSequence,
            Tag.ReferencedPatientSequence,
            Tag.PatientName,
            Tag.PatientID,
            Tag.PatientBirthDate,
            Tag.PatientSex,
            Tag.StudyID,
            Tag.PerformedStationName,
            Tag.PerformedLocation,
            Tag.PerformedProcedureStepDescription,
            Tag.PerformedProcedureTypeDescription,
            Tag.PerformedProtocolCodeSequence,
    };
    private static final int[] CREATE_MPPS_TOP_LEVEL_EMPTY_ATTRS = {
            Tag.PerformedProcedureStepEndDate,
            Tag.PerformedProcedureStepEndTime,
            Tag.PerformedProcedureStepDiscontinuationReasonCodeSequence,
            Tag.PerformedSeriesSequence
    };
    private static final int[] FINAL_MPPS_TOP_LEVEL_ATTRS = {
            Tag.SpecificCharacterSet,
            Tag.PerformedProcedureStepEndDate,
            Tag.PerformedProcedureStepEndTime,
            Tag.PerformedProcedureStepStatus,
            Tag.PerformedProcedureStepDiscontinuationReasonCodeSequence,
            Tag.PerformedSeriesSequence
    };
    private static final int[] SSA_ATTRS = {
            Tag.AccessionNumber,
            Tag.IssuerOfAccessionNumberSequence,
            Tag.ReferencedStudySequence,
            Tag.StudyInstanceUID,
            Tag.RequestedProcedureDescription,
            Tag.RequestedProcedureCodeSequence,
            Tag.ScheduledProcedureStepDescription,
            Tag.ScheduledProtocolCodeSequence,
            Tag.ScheduledProcedureStepID,
            Tag.OrderPlacerIdentifierSequence,
            Tag.OrderFillerIdentifierSequence,
            Tag.RequestedProcedureID,
            Tag.PlacerOrderNumberImagingServiceRequest,
            Tag.FillerOrderNumberImagingServiceRequest,
    };
    private static final int[] SSA_TYPE_2_ATTRS = {
            Tag.AccessionNumber,
            Tag.ReferencedStudySequence,
            Tag.StudyInstanceUID,
            Tag.RequestedProcedureDescription,
            Tag.RequestedProcedureID,
            Tag.ScheduledProcedureStepDescription,
            Tag.ScheduledProtocolCodeSequence,
            Tag.ScheduledProcedureStepID,
    };
    private static final int[] PERF_SERIES_ATTRS = {
            Tag.SeriesDescription,
            Tag.PerformingPhysicianName,
            Tag.OperatorsName,
            Tag.ProtocolName,
            Tag.SeriesInstanceUID
    };
    private static final int[] PERF_SERIES_TYPE_2_ATTRS = {
            Tag.RetrieveAETitle,
            Tag.SeriesDescription,
            Tag.PerformingPhysicianName,
            Tag.OperatorsName,
            Tag.ReferencedNonImageCompositeSOPInstanceSequence
    };
    private static final String ppsStartDate;
    private static final String ppsStartTime;

    static {
        Date now = new Date();
        ppsStartDate = Format.formatDA(null, now);
        ppsStartTime = Format.formatTM(null, now);
    }

    private final ApplicationEntity ae;
    private final Connection remote;
    private final AAssociateRQ rq = new AAssociateRQ();
    private final HashMap<String, MppsWithIUID> map = new HashMap<>();
    private final ArrayList<MppsWithIUID> created = new ArrayList<>();
    private Attributes attrs;
    private String uidSuffix;
    private boolean newPPSID;
    private int serialNo = (int) (System.currentTimeMillis() & 0x7FFFFFFFL);
    private String ppsuid;
    private String ppsid;
    private DecimalFormat ppsidFormat = new DecimalFormat("PPS-0000000000");
    private String protocolName = "UNKNOWN";
    private String archiveRequested;
    private String finalStatus = COMPLETED;
    private Attributes discontinuationReason;
    private Properties codes;
    private Association as;
    //default response handler
    private RSPHandlerFactory rspHandlerFactory = new RSPHandlerFactory() {

        @Override
        public DimseRSPHandler createDimseRSPHandlerForNCreate(final MppsWithIUID mppsWithUID) {

            return new DimseRSPHandler(as.nextMessageID()) {

                @Override
                public void onDimseRSP(Association as, Attributes cmd, Attributes data) {
                    switch (cmd.getInt(Tag.Status, -1)) {
                        case Status.Success:
                        case Status.AttributeListError:
                        case Status.AttributeValueOutOfRange:
                            mppsWithUID.iuid = cmd.getString(
                                    Tag.AffectedSOPInstanceUID, mppsWithUID.iuid);
                            addCreatedMpps(mppsWithUID);
                    }
                    super.onDimseRSP(as, cmd, data);
                }
            };
        }

        @Override
        public DimseRSPHandler createDimseRSPHandlerForNSet() {

            return new DimseRSPHandler(as.nextMessageID());
        }
    };

    public MppsSCU(ApplicationEntity ae) {
        this.remote = new Connection();
        this.ae = ae;
    }

    public void setRspHandlerFactory(RSPHandlerFactory rspHandlerFactory) {
        this.rspHandlerFactory = rspHandlerFactory;
    }

    public Connection getRemoteConnection() {
        return remote;
    }

    public AAssociateRQ getAAssociateRQ() {
        return rq;
    }

    public void addCreatedMpps(MppsWithIUID mpps) {
        created.add(mpps);
    }

    public final void setUIDSuffix(String uidSuffix) {
        this.uidSuffix = uidSuffix;
    }

    public final void setPPSUID(String ppsuid) {
        this.ppsuid = ppsuid;
    }

    public final void setPPSID(String ppsid) {
        this.ppsid = ppsid;
    }

    public final void setPPSIDStart(int ppsidStart) {
        this.serialNo = ppsidStart;
    }

    public final void setPPSIDFormat(String ppsidFormat) {
        this.ppsidFormat = new DecimalFormat(ppsidFormat);
    }

    public final void setNewPPSID(boolean newPPSID) {
        this.newPPSID = newPPSID;
    }

    public final void setProtocolName(String protocolName) {
        this.protocolName = protocolName;
    }

    public final void setArchiveRequested(String archiveRequested) {
        this.archiveRequested = archiveRequested;
    }

    public final void setFinalStatus(String finalStatus) {
        this.finalStatus = finalStatus;
    }

    public final void setCodes(Properties codes) {
        this.codes = codes;
    }

    public void setAttributes(Attributes attrs) {
        this.attrs = attrs;
    }

    public final void setDiscontinuationReason(String codeValue) {
        if (codes == null)
            throw new IllegalStateException("codes not initialized");
        String codeMeaning = codes.getProperty(codeValue);
        if (codeMeaning == null)
            throw new IllegalArgumentException("undefined code value: "
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
        this.discontinuationReason = attrs;
    }

    public void setTransferSyntaxes(String[] tss) {
        rq.addPresentationContext(
                new PresentationContext(1, UID.Verification.uid,
                        UID.ImplicitVRLittleEndian.uid));
        rq.addPresentationContext(
                new PresentationContext(3,
                        UID.ModalityPerformedProcedureStep.uid,
                        tss));
    }

    public void scanFiles(List<String> fnames, boolean printout) {
        DicomFiles.scan(fnames, printout, (f, fmi, dsPos, ds) -> {
            if (UID.ModalityPerformedProcedureStep.equals(
                    fmi.getString(Tag.MediaStorageSOPClassUID))) {
                return addMPPS(
                        fmi.getString(Tag.MediaStorageSOPInstanceUID),
                        ds);
            }
            return addInstance(ds);
        });
    }

    public void open() throws IOException, InterruptedException,
            InternalException, GeneralSecurityException {
        as = ae.connect(remote, rq);
    }

    public void close() throws IOException, InterruptedException {
        if (as != null) {
            as.waitForOutstandingRSP();
            as.release();
            as.waitForSocketClose();
        }
    }

    public void echo() throws IOException, InterruptedException {
        as.cecho().next();
    }

    public void createMpps() throws IOException, InterruptedException {
        for (MppsWithIUID mppsWithUID : map.values())
            createMpps(mppsWithUID);
        as.waitForOutstandingRSP();
    }

    private void createMpps(final MppsWithIUID mppsWithUID)
            throws IOException, InterruptedException {
        final String iuid = mppsWithUID.iuid;
        Attributes mpps = mppsWithUID.mpps;
        mppsWithUID.mpps = new Attributes(mpps, FINAL_MPPS_TOP_LEVEL_ATTRS);
        mpps.setString(Tag.PerformedProcedureStepStatus, VR.CS, IN_PROGRESS);
        for (int tag : CREATE_MPPS_TOP_LEVEL_EMPTY_ATTRS)
            mpps.setNull(tag, dict.vrOf(tag));

        as.ncreate(UID.ModalityPerformedProcedureStep.uid,
                iuid, mpps, null, rspHandlerFactory.createDimseRSPHandlerForNCreate(mppsWithUID));
    }

    public void updateMpps() throws IOException, InterruptedException {
        for (MppsWithIUID mppsWithIUID : created)
            setMpps(mppsWithIUID);
    }

    private void setMpps(MppsWithIUID mppsWithIUID)
            throws IOException, InterruptedException {
        as.nset(UID.ModalityPerformedProcedureStep.uid,
                mppsWithIUID.iuid, mppsWithIUID.mpps, null, rspHandlerFactory.createDimseRSPHandlerForNSet());
    }

    public boolean addInstance(Attributes inst) {
        String suid = inst.getString(Tag.StudyInstanceUID);
        if (suid == null)
            return false;
        MppsWithIUID mppsWithIUID = map.get(suid);
        if (mppsWithIUID == null)
            map.put(suid, mppsWithIUID = new MppsWithIUID(ppsuid(null), createMPPS(inst)));
        updateMPPS(mppsWithIUID.mpps, inst);
        return true;
    }

    public boolean addMPPS(String iuid, Attributes mpps) {
        map.put(iuid, new MppsWithIUID(ppsuid(iuid), mpps));
        return true;
    }

    private String ppsuid(String defval) {
        if (ppsuid == null)
            return defval;

        int size = map.size();
        switch (size) {
            case 0:
                return ppsuid;
            case 1:
                map.values().iterator().next().iuid += ".1";
        }
        return ppsuid + '.' + (size + 1);
    }

    private String mkPPSID() {
        if (ppsid != null)
            return ppsid;
        String id = ppsidFormat.format(serialNo);
        if (++serialNo < 0)
            serialNo = 0;
        return id;
    }

    private Attributes createMPPS(Attributes inst) {
        Attributes mpps = new Attributes();
        mpps.setString(Tag.PerformedStationAETitle, VR.AE, ae.getAETitle());
        mpps.setString(Tag.PerformedProcedureStepStartDate, VR.DA,
                inst.getString(Tag.StudyDate, ppsStartDate));
        mpps.setString(Tag.PerformedProcedureStepStartTime, VR.TM,
                inst.getString(Tag.StudyTime, ppsStartTime));
        for (int tag : MPPS_TOP_LEVEL_TYPE_2_ATTRS)
            mpps.setNull(tag, dict.vrOf(tag));
        mpps.addSelected(inst, MPPS_TOP_LEVEL_ATTRS);
        if (newPPSID || !mpps.containsValue(Tag.PerformedProcedureStepID))
            mpps.setString(Tag.PerformedProcedureStepID, VR.CS, mkPPSID());
        mpps.setString(Tag.PerformedProcedureStepEndDate, VR.DA,
                mpps.getString(Tag.PerformedProcedureStepStartDate));
        mpps.setString(Tag.PerformedProcedureStepEndTime, VR.TM,
                mpps.getString(Tag.PerformedProcedureStepStartTime));
        mpps.setString(Tag.PerformedProcedureStepStatus, VR.CS, finalStatus);
        Sequence dcrSeq = mpps.newSequence(Tag.PerformedProcedureStepDiscontinuationReasonCodeSequence, 1);
        if (discontinuationReason != null)
            dcrSeq.add(new Attributes(discontinuationReason));

        Sequence raSeq = inst.getSequence(Tag.RequestAttributesSequence);
        Attributes ssa1 = inst.getNestedDataset(Tag.ScheduledStepAttributesSequence);
        if (raSeq == null || raSeq.isEmpty()) {
            Sequence ssaSeq = mpps.newSequence(Tag.ScheduledStepAttributesSequence, 1);
            Attributes ssa = ssa1 == null ? new Attributes() : new Attributes(ssa1);
            ssaSeq.add(ssa);
            for (int tag : SSA_TYPE_2_ATTRS)
                if (!ssa.containsValue(tag))
                    ssa.setNull(tag, dict.vrOf(tag));
            ssa.addSelected(inst, SSA_ATTRS);
        } else {
            Sequence ssaSeq = mpps.newSequence(Tag.ScheduledStepAttributesSequence, raSeq.size());
            for (Attributes ra : raSeq) {
                Attributes ssa = ssa1 == null ? new Attributes() : new Attributes(ssa1);
                ssaSeq.add(ssa);
                for (int tag : SSA_TYPE_2_ATTRS)
                    if (!ssa.containsValue(tag))
                        ssa.setNull(tag, dict.vrOf(tag));
                ssa.addSelected(inst, SSA_ATTRS);
                ssa.addSelected(ra, SSA_ATTRS);
            }
        }
        mpps.newSequence(Tag.PerformedSeriesSequence, 1);
        return mpps;
    }

    private void updateMPPS(Attributes mpps, Attributes inst) {
        String endTime = inst.getString(Tag.AcquisitionTime);
        if (endTime == null) {
            endTime = inst.getString(Tag.ContentTime);
            if (endTime == null)
                endTime = inst.getString(Tag.SeriesTime);
        }
        if (endTime != null && endTime.compareTo(
                mpps.getString(Tag.PerformedProcedureStepEndTime)) > 0)
            mpps.setString(Tag.PerformedProcedureStepEndTime, VR.TM, endTime);
        Sequence prefSeriesSeq = mpps.getSequence(Tag.PerformedSeriesSequence);
        Attributes prefSeries = getPerfSeries(prefSeriesSeq, inst);
        Sequence refSOPSeq = prefSeries.getSequence(Tag.ReferencedImageSequence);
        Attributes refSOP = new Attributes();
        refSOPSeq.add(refSOP);
        refSOP.setString(Tag.ReferencedSOPClassUID, VR.UI,
                inst.getString(Tag.SOPClassUID));
        refSOP.setString(Tag.ReferencedSOPInstanceUID, VR.UI,
                inst.getString(Tag.SOPInstanceUID));
    }

    private Attributes getPerfSeries(Sequence prefSeriesSeq, Attributes inst) {
        String suid = inst.getString(Tag.SeriesInstanceUID);
        for (Attributes prefSeries : prefSeriesSeq) {
            if (suid.equals(prefSeries.getString(Tag.SeriesInstanceUID)))
                return prefSeries;
        }
        Attributes prefSeries = new Attributes();
        prefSeriesSeq.add(prefSeries);
        for (int tag : PERF_SERIES_TYPE_2_ATTRS)
            prefSeries.setNull(tag, dict.vrOf(tag));
        prefSeries.setString(Tag.ProtocolName, VR.LO, protocolName);
        prefSeries.addSelected(inst, PERF_SERIES_ATTRS);
        prefSeries.newSequence(Tag.ReferencedImageSequence, 10);
        if (archiveRequested != null)
            prefSeries.setString(Tag.ArchiveRequested, VR.CS, archiveRequested);
        return prefSeries;
    }

    public interface RSPHandlerFactory {

        DimseRSPHandler createDimseRSPHandlerForNCreate(MppsWithIUID mppsWithUID);

        DimseRSPHandler createDimseRSPHandlerForNSet();
    }

    public static final class MppsWithIUID {

        public String iuid;
        public Attributes mpps;

        MppsWithIUID(String iuid, Attributes mpps) {
            this.iuid = iuid;
            this.mpps = mpps;
        }
    }

}
