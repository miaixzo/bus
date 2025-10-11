/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
 * The {@code MppsSCU} class implements a Service Class User (SCU) for the Modality Performed Procedure Step (MPPS) SOP
 * Class. It is responsible for creating (N-CREATE) and updating (N-SET) MPPS instances on a remote Service Class
 * Provider (SCP).
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MppsSCU {

    /**
     * The standard DICOM element dictionary.
     */
    private static final ElementDictionary dict = ElementDictionary.getStandardElementDictionary();
    /**
     * Constant for the "IN PROGRESS" status.
     */
    private static final String IN_PROGRESS = "IN PROGRESS";
    /**
     * Constant for the "COMPLETED" status.
     */
    private static final String COMPLETED = "COMPLETED";
    /**
     * Constant for the "DISCONTINUED" status.
     */
    private static final String DISCONTINUED = "DISCONTINUED";
    /**
     * Tags for top-level MPPS attributes.
     */
    private static final int[] MPPS_TOP_LEVEL_ATTRS = { Tag.SpecificCharacterSet, Tag.Modality,
            Tag.ProcedureCodeSequence, Tag.ReferencedPatientSequence, Tag.PatientName, Tag.PatientID,
            Tag.IssuerOfPatientID, Tag.IssuerOfPatientIDQualifiersSequence, Tag.PatientBirthDate, Tag.PatientSex,
            Tag.StudyID, Tag.AdmissionID, Tag.IssuerOfAdmissionIDSequence, Tag.ServiceEpisodeID,
            Tag.IssuerOfServiceEpisodeID, Tag.ServiceEpisodeDescription, Tag.PerformedProcedureStepStartDate,
            Tag.PerformedProcedureStepStartTime, Tag.PerformedProcedureStepID, Tag.PerformedProcedureStepDescription,
            Tag.PerformedProtocolCodeSequence, Tag.CommentsOnThePerformedProcedureStep, };
    /**
     * Tags for Type 2 top-level MPPS attributes.
     */
    private static final int[] MPPS_TOP_LEVEL_TYPE_2_ATTRS = { Tag.ProcedureCodeSequence, Tag.ReferencedPatientSequence,
            Tag.PatientName, Tag.PatientID, Tag.PatientBirthDate, Tag.PatientSex, Tag.StudyID, Tag.PerformedStationName,
            Tag.PerformedLocation, Tag.PerformedProcedureStepDescription, Tag.PerformedProcedureTypeDescription,
            Tag.PerformedProtocolCodeSequence, };
    /**
     * Tags for attributes that should be empty in an N-CREATE request.
     */
    private static final int[] CREATE_MPPS_TOP_LEVEL_EMPTY_ATTRS = { Tag.PerformedProcedureStepEndDate,
            Tag.PerformedProcedureStepEndTime, Tag.PerformedProcedureStepDiscontinuationReasonCodeSequence,
            Tag.PerformedSeriesSequence };
    /**
     * Tags for attributes relevant to the final state of an MPPS.
     */
    private static final int[] FINAL_MPPS_TOP_LEVEL_ATTRS = { Tag.SpecificCharacterSet,
            Tag.PerformedProcedureStepEndDate, Tag.PerformedProcedureStepEndTime, Tag.PerformedProcedureStepStatus,
            Tag.PerformedProcedureStepDiscontinuationReasonCodeSequence, Tag.PerformedSeriesSequence };
    /**
     * Tags for Scheduled Step Attributes.
     */
    private static final int[] SSA_ATTRS = { Tag.AccessionNumber, Tag.IssuerOfAccessionNumberSequence,
            Tag.ReferencedStudySequence, Tag.StudyInstanceUID, Tag.RequestedProcedureDescription,
            Tag.RequestedProcedureCodeSequence, Tag.ScheduledProcedureStepDescription,
            Tag.ScheduledProtocolCodeSequence, Tag.ScheduledProcedureStepID, Tag.OrderPlacerIdentifierSequence,
            Tag.OrderFillerIdentifierSequence, Tag.RequestedProcedureID, Tag.PlacerOrderNumberImagingServiceRequest,
            Tag.FillerOrderNumberImagingServiceRequest, };
    /**
     * Tags for Type 2 Scheduled Step Attributes.
     */
    private static final int[] SSA_TYPE_2_ATTRS = { Tag.AccessionNumber, Tag.ReferencedStudySequence,
            Tag.StudyInstanceUID, Tag.RequestedProcedureDescription, Tag.RequestedProcedureID,
            Tag.ScheduledProcedureStepDescription, Tag.ScheduledProtocolCodeSequence, Tag.ScheduledProcedureStepID, };
    /**
     * Tags for Performed Series Attributes.
     */
    private static final int[] PERF_SERIES_ATTRS = { Tag.SeriesDescription, Tag.PerformingPhysicianName,
            Tag.OperatorsName, Tag.ProtocolName, Tag.SeriesInstanceUID };
    /**
     * Tags for Type 2 Performed Series Attributes.
     */
    private static final int[] PERF_SERIES_TYPE_2_ATTRS = { Tag.RetrieveAETitle, Tag.SeriesDescription,
            Tag.PerformingPhysicianName, Tag.OperatorsName, Tag.ReferencedNonImageCompositeSOPInstanceSequence };
    /**
     * The start date of the performed procedure step, initialized at class loading.
     */
    private static final String ppsStartDate;
    /**
     * The start time of the performed procedure step, initialized at class loading.
     */
    private static final String ppsStartTime;

    static {
        Date now = new Date();
        ppsStartDate = Format.formatDA(null, now);
        ppsStartTime = Format.formatTM(null, now);
    }

    /**
     * The Application Entity of this SCU.
     */
    private final ApplicationEntity ae;
    /**
     * The remote connection configuration.
     */
    private final Connection remote;
    /**
     * The A-ASSOCIATE-RQ message.
     */
    private final AAssociateRQ rq = new AAssociateRQ();
    /**
     * A map to hold MPPS objects, keyed by Study Instance UID.
     */
    private final HashMap<String, MppsWithIUID> map = new HashMap<>();
    /**
     * A list of successfully created MPPS instances.
     */
    private final ArrayList<MppsWithIUID> created = new ArrayList<>();
    /**
     * Additional attributes to be merged into the MPPS objects.
     */
    private Attributes attrs;
    /**
     * A suffix for generated UIDs.
     */
    private String uidSuffix;
    /**
     * A flag to force creation of a new Performed Procedure Step ID.
     */
    private boolean newPPSID;
    /**
     * A serial number for generating unique PPS IDs.
     */
    private int serialNo = (int) (System.currentTimeMillis() & 0x7FFFFFFFL);
    /**
     * The SOP Instance UID for the MPPS object.
     */
    private String ppsuid;
    /**
     * The Performed Procedure Step ID.
     */
    private String ppsid;
    /**
     * A formatter for generating PPS IDs.
     */
    private DecimalFormat ppsidFormat = new DecimalFormat("PPS-0000000000");
    /**
     * The name of the protocol performed.
     */
    private String protocolName = "UNKNOWN";
    /**
     * The archive requested status.
     */
    private String archiveRequested;
    /**
     * The final status of the MPPS (e.g., COMPLETED or DISCONTINUED).
     */
    private String finalStatus = COMPLETED;
    /**
     * Attributes for the discontinuation reason.
     */
    private Attributes discontinuationReason;
    /**
     * A properties object for mapping discontinuation reason codes to meanings.
     */
    private Properties codes;
    /**
     * The active DICOM association.
     */
    private Association as;
    /**
     * A factory for creating DIMSE response handlers.
     */
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
                            mppsWithUID.iuid = cmd.getString(Tag.AffectedSOPInstanceUID, mppsWithUID.iuid);
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

    /**
     * Constructs a new {@code MppsSCU} with the given Application Entity.
     *
     * @param ae The Application Entity for this SCU.
     */
    public MppsSCU(ApplicationEntity ae) {
        this.remote = new Connection();
        this.ae = ae;
    }

    /**
     * Sets the factory for creating DIMSE response handlers.
     *
     * @param rspHandlerFactory The response handler factory.
     */
    public void setRspHandlerFactory(RSPHandlerFactory rspHandlerFactory) {
        this.rspHandlerFactory = rspHandlerFactory;
    }

    /**
     * Gets the remote connection configuration.
     *
     * @return The remote connection.
     */
    public Connection getRemoteConnection() {
        return remote;
    }

    /**
     * Gets the A-ASSOCIATE-RQ message.
     *
     * @return The A-ASSOCIATE-RQ.
     */
    public AAssociateRQ getAAssociateRQ() {
        return rq;
    }

    /**
     * Adds a successfully created MPPS instance to the internal list for subsequent updates.
     *
     * @param mpps The MPPS instance with its UID.
     */
    public void addCreatedMpps(MppsWithIUID mpps) {
        created.add(mpps);
    }

    /**
     * Sets a suffix to be appended to generated UIDs.
     *
     * @param uidSuffix The UID suffix.
     */
    public final void setUIDSuffix(String uidSuffix) {
        this.uidSuffix = uidSuffix;
    }

    /**
     * Sets a specific SOP Instance UID for the MPPS to be created.
     *
     * @param ppsuid The Performed Procedure Step SOP Instance UID.
     */
    public final void setPPSUID(String ppsuid) {
        this.ppsuid = ppsuid;
    }

    /**
     * Sets a specific Performed Procedure Step ID.
     *
     * @param ppsid The Performed Procedure Step ID.
     */
    public final void setPPSID(String ppsid) {
        this.ppsid = ppsid;
    }

    /**
     * Sets the starting number for generating serial PPS IDs.
     *
     * @param ppsidStart The starting serial number.
     */
    public final void setPPSIDStart(int ppsidStart) {
        this.serialNo = ppsidStart;
    }

    /**
     * Sets the format for generating Performed Procedure Step IDs.
     *
     * @param ppsidFormat A {@link DecimalFormat} pattern.
     */
    public final void setPPSIDFormat(String ppsidFormat) {
        this.ppsidFormat = new DecimalFormat(ppsidFormat);
    }

    /**
     * Sets whether to always generate a new Performed Procedure Step ID, even if one exists.
     *
     * @param newPPSID {@code true} to force new ID generation.
     */
    public final void setNewPPSID(boolean newPPSID) {
        this.newPPSID = newPPSID;
    }

    /**
     * Sets the name of the protocol that was performed.
     *
     * @param protocolName The protocol name.
     */
    public final void setProtocolName(String protocolName) {
        this.protocolName = protocolName;
    }

    /**
     * Sets the value for the Archive Requested attribute in the Performed Series Sequence.
     *
     * @param archiveRequested The archive requested value (e.g., "YES", "NO").
     */
    public final void setArchiveRequested(String archiveRequested) {
        this.archiveRequested = archiveRequested;
    }

    /**
     * Sets the final status to be used for the MPPS N-SET operation (e.g., COMPLETED, DISCONTINUED).
     *
     * @param finalStatus The final status.
     */
    public final void setFinalStatus(String finalStatus) {
        this.finalStatus = finalStatus;
    }

    /**
     * Sets the properties for looking up discontinuation reason code meanings.
     *
     * @param codes The properties containing code-meaning pairs.
     */
    public final void setCodes(Properties codes) {
        this.codes = codes;
    }

    /**
     * Sets additional attributes to be merged into the MPPS object.
     *
     * @param attrs The attributes to merge.
     */
    public void setAttributes(Attributes attrs) {
        this.attrs = attrs;
    }

    /**
     * Sets the discontinuation reason from a code value.
     *
     * @param codeValue The code value, optionally prefixed with the coding scheme designator.
     * @throws IllegalStateException    if the code properties have not been set.
     * @throws IllegalArgumentException if the code value is undefined.
     */
    public final void setDiscontinuationReason(String codeValue) {
        if (codes == null)
            throw new IllegalStateException("codes not initialized");
        String codeMeaning = codes.getProperty(codeValue);
        if (codeMeaning == null)
            throw new IllegalArgumentException("undefined code value: " + codeValue);
        int endDesignator = codeValue.indexOf(Symbol.C_MINUS);
        Attributes attrs = new Attributes(3);
        attrs.setString(Tag.CodeValue, VR.SH, endDesignator >= 0 ? codeValue.substring(endDesignator + 1) : codeValue);
        attrs.setString(
                Tag.CodingSchemeDesignator,
                VR.SH,
                endDesignator >= 0 ? codeValue.substring(0, endDesignator) : "DCM");
        attrs.setString(Tag.CodeMeaning, VR.LO, codeMeaning);
        this.discontinuationReason = attrs;
    }

    /**
     * Sets the transfer syntaxes to be proposed for MPPS and Verification SOP classes.
     *
     * @param tss An array of transfer syntax UIDs.
     */
    public void setTransferSyntaxes(String[] tss) {
        rq.addPresentationContext(new PresentationContext(1, UID.Verification.uid, UID.ImplicitVRLittleEndian.uid));
        rq.addPresentationContext(new PresentationContext(3, UID.ModalityPerformedProcedureStep.uid, tss));
    }

    /**
     * Scans a list of files and directories, adding instances to the MPPS map.
     *
     * @param fnames   A list of file/directory paths.
     * @param printout {@code true} to print the names of scanned files to the console.
     */
    public void scanFiles(List<String> fnames, boolean printout) {
        DicomFiles.scan(fnames, printout, (f, fmi, dsPos, ds) -> {
            if (UID.ModalityPerformedProcedureStep.equals(fmi.getString(Tag.MediaStorageSOPClassUID))) {
                return addMPPS(fmi.getString(Tag.MediaStorageSOPInstanceUID), ds);
            }
            return addInstance(ds);
        });
    }

    /**
     * Establishes a DICOM association with the remote AE.
     *
     * @throws IOException              if an I/O error occurs.
     * @throws InterruptedException     if the connection is interrupted.
     * @throws InternalException        if a configuration error occurs.
     * @throws GeneralSecurityException if a security error occurs.
     */
    public void open() throws IOException, InterruptedException, InternalException, GeneralSecurityException {
        as = ae.connect(remote, rq);
    }

    /**
     * Closes the DICOM association.
     *
     * @throws IOException          if an I/O error occurs.
     * @throws InterruptedException if the operation is interrupted.
     */
    public void close() throws IOException, InterruptedException {
        if (as != null) {
            as.waitForOutstandingRSP();
            as.release();
            as.waitForSocketClose();
        }
    }

    /**
     * Performs a C-ECHO verification to check the connection.
     *
     * @throws IOException          if an I/O error occurs.
     * @throws InterruptedException if the operation is interrupted.
     */
    public void echo() throws IOException, InterruptedException {
        as.cecho().next();
    }

    /**
     * Sends N-CREATE messages for all collected MPPS instances.
     *
     * @throws IOException          if an I/O error occurs.
     * @throws InterruptedException if the operation is interrupted.
     */
    public void createMpps() throws IOException, InterruptedException {
        for (MppsWithIUID mppsWithUID : map.values())
            createMpps(mppsWithUID);
        as.waitForOutstandingRSP();
    }

    /**
     * Sends a single MPPS N-CREATE message.
     *
     * @param mppsWithUID The MPPS instance to create.
     * @throws IOException          if an I/O error occurs.
     * @throws InterruptedException if the operation is interrupted.
     */
    private void createMpps(final MppsWithIUID mppsWithUID) throws IOException, InterruptedException {
        final String iuid = mppsWithUID.iuid;
        Attributes mpps = mppsWithUID.mpps;
        mppsWithUID.mpps = new Attributes(mpps, FINAL_MPPS_TOP_LEVEL_ATTRS);
        mpps.setString(Tag.PerformedProcedureStepStatus, VR.CS, IN_PROGRESS);
        for (int tag : CREATE_MPPS_TOP_LEVEL_EMPTY_ATTRS)
            mpps.setNull(tag, dict.vrOf(tag));

        as.ncreate(
                UID.ModalityPerformedProcedureStep.uid,
                iuid,
                mpps,
                null,
                rspHandlerFactory.createDimseRSPHandlerForNCreate(mppsWithUID));
    }

    /**
     * Sends N-SET messages for all successfully created MPPS instances.
     *
     * @throws IOException          if an I/O error occurs.
     * @throws InterruptedException if the operation is interrupted.
     */
    public void updateMpps() throws IOException, InterruptedException {
        for (MppsWithIUID mppsWithIUID : created)
            setMpps(mppsWithIUID);
    }

    /**
     * Sends a single MPPS N-SET message.
     *
     * @param mppsWithIUID The MPPS instance to update.
     * @throws IOException          if an I/O error occurs.
     * @throws InterruptedException if the operation is interrupted.
     */
    private void setMpps(MppsWithIUID mppsWithIUID) throws IOException, InterruptedException {
        as.nset(
                UID.ModalityPerformedProcedureStep.uid,
                mppsWithIUID.iuid,
                mppsWithIUID.mpps,
                null,
                rspHandlerFactory.createDimseRSPHandlerForNSet());
    }

    /**
     * Adds a DICOM instance to an MPPS object. Instances are grouped by Study Instance UID.
     *
     * @param inst The attributes of the DICOM instance.
     * @return {@code true} if the instance was added successfully, {@code false} otherwise.
     */
    public boolean addInstance(Attributes inst) {
        String suid = inst.getString(Tag.StudyInstanceUID);
        if (suid == null)
            return false;
        MppsWithIUID mppsWithIUID = map.computeIfAbsent(suid, k -> new MppsWithIUID(ppsuid(null), createMPPS(inst)));
        updateMPPS(mppsWithIUID.mpps, inst);
        return true;
    }

    /**
     * Adds a pre-existing MPPS object to the map.
     *
     * @param iuid The SOP Instance UID of the MPPS.
     * @param mpps The MPPS dataset.
     * @return {@code true} always.
     */
    public boolean addMPPS(String iuid, Attributes mpps) {
        map.put(iuid, new MppsWithIUID(ppsuid(iuid), mpps));
        return true;
    }

    /**
     * Generates a unique SOP Instance UID for a new MPPS object.
     *
     * @param defval A default value to use if no UID is configured.
     * @return A unique SOP Instance UID.
     */
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

    /**
     * Generates a Performed Procedure Step ID.
     *
     * @return The generated ID.
     */
    private String mkPPSID() {
        if (ppsid != null)
            return ppsid;
        String id = ppsidFormat.format(serialNo);
        if (++serialNo < 0)
            serialNo = 0;
        return id;
    }

    /**
     * Creates a new MPPS dataset based on the attributes of a DICOM instance.
     *
     * @param inst The instance attributes.
     * @return The newly created MPPS dataset.
     */
    private Attributes createMPPS(Attributes inst) {
        Attributes mpps = new Attributes();
        mpps.setString(Tag.PerformedStationAETitle, VR.AE, ae.getAETitle());
        mpps.setString(Tag.PerformedProcedureStepStartDate, VR.DA, inst.getString(Tag.StudyDate, ppsStartDate));
        mpps.setString(Tag.PerformedProcedureStepStartTime, VR.TM, inst.getString(Tag.StudyTime, ppsStartTime));
        for (int tag : MPPS_TOP_LEVEL_TYPE_2_ATTRS)
            mpps.setNull(tag, dict.vrOf(tag));
        mpps.addSelected(inst, MPPS_TOP_LEVEL_ATTRS);
        if (newPPSID || !mpps.containsValue(Tag.PerformedProcedureStepID))
            mpps.setString(Tag.PerformedProcedureStepID, VR.CS, mkPPSID());
        mpps.setString(Tag.PerformedProcedureStepEndDate, VR.DA, mpps.getString(Tag.PerformedProcedureStepStartDate));
        mpps.setString(Tag.PerformedProcedureStepEndTime, VR.TM, mpps.getString(Tag.PerformedProcedureStepStartTime));
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

    /**
     * Updates an MPPS dataset with information from a new instance.
     *
     * @param mpps The MPPS dataset to update.
     * @param inst The new instance to add.
     */
    private void updateMPPS(Attributes mpps, Attributes inst) {
        String endTime = inst.getString(Tag.AcquisitionTime);
        if (endTime == null) {
            endTime = inst.getString(Tag.ContentTime);
            if (endTime == null)
                endTime = inst.getString(Tag.SeriesTime);
        }
        if (endTime != null && endTime.compareTo(mpps.getString(Tag.PerformedProcedureStepEndTime)) > 0)
            mpps.setString(Tag.PerformedProcedureStepEndTime, VR.TM, endTime);
        Sequence prefSeriesSeq = mpps.getSequence(Tag.PerformedSeriesSequence);
        Attributes prefSeries = getPerfSeries(prefSeriesSeq, inst);
        Sequence refSOPSeq = prefSeries.getSequence(Tag.ReferencedImageSequence);
        Attributes refSOP = new Attributes();
        refSOPSeq.add(refSOP);
        refSOP.setString(Tag.ReferencedSOPClassUID, VR.UI, inst.getString(Tag.SOPClassUID));
        refSOP.setString(Tag.ReferencedSOPInstanceUID, VR.UI, inst.getString(Tag.SOPInstanceUID));
    }

    /**
     * Finds or creates a Performed Series Sequence item for a given instance.
     *
     * @param prefSeriesSeq The sequence of performed series.
     * @param inst          The instance whose series is to be found or created.
     * @return The found or newly created Performed Series item.
     */
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

    /**
     * A factory for creating DIMSE response handlers for MPPS N-CREATE and N-SET operations.
     */
    public interface RSPHandlerFactory {

        /**
         * Creates a response handler for an N-CREATE operation.
         *
         * @param mppsWithUID The MPPS instance being created.
         * @return A new {@link DimseRSPHandler}.
         */
        DimseRSPHandler createDimseRSPHandlerForNCreate(MppsWithIUID mppsWithUID);

        /**
         * Creates a response handler for an N-SET operation.
         *
         * @return A new {@link DimseRSPHandler}.
         */
        DimseRSPHandler createDimseRSPHandlerForNSet();
    }

    /**
     * A simple container class to hold an MPPS dataset along with its SOP Instance UID.
     */
    public static final class MppsWithIUID {

        /**
         * The SOP Instance UID of the MPPS object.
         */
        public String iuid;
        /**
         * The attributes of the MPPS object.
         */
        public Attributes mpps;

        /**
         * Constructs a new container.
         *
         * @param iuid The SOP Instance UID.
         * @param mpps The MPPS attributes.
         */
        MppsWithIUID(String iuid, Attributes mpps) {
            this.iuid = iuid;
            this.mpps = mpps;
        }
    }

}
