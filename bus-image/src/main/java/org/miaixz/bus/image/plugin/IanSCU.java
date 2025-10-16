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

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.image.Device;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.galaxy.data.Attributes;
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
import java.util.HashMap;

/**
 * The {@code IanSCU} class implements a Service Class User (SCU) for the Instance Availability Notification (IAN) SOP
 * Class. It collects information about DICOM instances and sends IAN N-CREATE messages to a Service Class Provider
 * (SCP).
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class IanSCU {

    /**
     * The DICOM device for this SCU.
     */
    private final Device device = new Device("ianscu");
    /**
     * The Application Entity for this SCU.
     */
    private final ApplicationEntity ae = new ApplicationEntity("IANSCU");
    /**
     * The local network connection configuration.
     */
    private final Connection conn = new Connection();
    /**
     * The remote network connection configuration.
     */
    private final Connection remote = new Connection();
    /**
     * The A-ASSOCIATE-RQ message to be sent.
     */
    private final AAssociateRQ rq = new AAssociateRQ();
    /**
     * The attributes for the IAN message (not directly used, kept for potential future use).
     */
    private final Attributes attrs = new Attributes();
    /**
     * A map to hold IAN datasets, keyed by Study Instance UID.
     */
    private final HashMap<String, Attributes> map = new HashMap<>();
    /**
     * A suffix for generated UIDs.
     */
    private String uidSuffix;
    /**
     * The SOP Instance UID of the referenced Performed Procedure Step.
     */
    private String refPpsIUID;
    /**
     * The SOP Class UID of the referenced Performed Procedure Step.
     */
    private String refPpsCUID = UID.ModalityPerformedProcedureStep.uid;
    /**
     * The availability status of the instances (e.g., "ONLINE").
     */
    private String availability = "ONLINE";
    /**
     * The AET of the location from which instances can be retrieved.
     */
    private String retrieveAET;
    /**
     * The URI for retrieving the instances.
     */
    private String retrieveURI;
    /**
     * The URL for retrieving the instances.
     */
    private String retrieveURL;
    /**
     * The Retrieve Location UID.
     */
    private String retrieveUID;
    /**
     * The active DICOM association.
     */
    private Association as;

    /**
     * Constructs a new {@code IanSCU} instance and initializes its device and application entity.
     */
    public IanSCU() {
        device.addConnection(conn);
        device.addApplicationEntity(ae);
        ae.addConnection(conn);
    }

    /**
     * Sets a suffix to be used for generating UIDs. (Not currently used in this implementation).
     *
     * @param uidSuffix The UID suffix string.
     */
    public final void setUIDSuffix(String uidSuffix) {
        this.uidSuffix = uidSuffix;
    }

    /**
     * Sets the transfer syntaxes to be proposed in the association request for IAN and Verification SOP classes.
     *
     * @param tss An array of transfer syntax UIDs.
     */
    public void setTransferSyntaxes(String[] tss) {
        rq.addPresentationContext(new PresentationContext(1, UID.Verification.uid, UID.ImplicitVRLittleEndian.uid));
        rq.addPresentationContext(new PresentationContext(3, UID.InstanceAvailabilityNotification.uid, tss));
    }

    /**
     * Sets the SOP Instance UID of the Performed Procedure Step to be referenced in the IAN.
     *
     * @param refPpsIUID The SOP Instance UID.
     */
    public void setRefPpsIUID(String refPpsIUID) {
        this.refPpsIUID = refPpsIUID;
    }

    /**
     * Sets the SOP Class UID of the Performed Procedure Step to be referenced.
     *
     * @param refPpsCUID The SOP Class UID.
     */
    public void setRefPpsCUID(String refPpsCUID) {
        this.refPpsCUID = refPpsCUID;
    }

    /**
     * Sets the availability status for the instances in the notification (e.g., "ONLINE", "OFFLINE").
     *
     * @param availability The availability status string.
     */
    public void setAvailability(String availability) {
        this.availability = availability;
    }

    /**
     * Gets the Retrieve AE Title. If not explicitly set, it returns the AE title of this SCU.
     *
     * @return The Retrieve AE Title.
     */
    public String getRetrieveAET() {
        return retrieveAET != null ? retrieveAET : ae.getAETitle();
    }

    /**
     * Sets the AE Title from which the instances can be retrieved.
     *
     * @param retrieveAET The Retrieve AE Title.
     */
    public void setRetrieveAET(String retrieveAET) {
        this.retrieveAET = retrieveAET;
    }

    /**
     * Sets the URL from which the instances can be retrieved.
     *
     * @param retrieveURL The Retrieve URL.
     */
    public void setRetrieveURL(String retrieveURL) {
        this.retrieveURL = retrieveURL;
    }

    /**
     * Sets the URI from which the instances can be retrieved.
     *
     * @param retrieveURI The Retrieve URI.
     */
    public void setRetrieveURI(String retrieveURI) {
        this.retrieveURI = retrieveURI;
    }

    /**
     * Sets the Retrieve Location UID.
     *
     * @param retrieveUID The Retrieve Location UID.
     */
    public void setRetrieveUID(String retrieveUID) {
        this.retrieveUID = retrieveUID;
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
        as = ae.connect(conn, remote, rq);
    }

    /**
     * Releases the DICOM association.
     *
     * @throws IOException          if an I/O error occurs.
     * @throws InterruptedException if the operation is interrupted.
     */
    public void close() throws IOException, InterruptedException {
        if (as != null) {
            as.release();
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
     * Sends all collected Instance Availability Notifications.
     *
     * @throws IOException          if an I/O error occurs.
     * @throws InterruptedException if the operation is interrupted.
     */
    public void sendIans() throws IOException, InterruptedException {
        for (Attributes ian : map.values())
            sendIan(ian);
    }

    /**
     * Sends a single IAN N-CREATE message.
     *
     * @param ian The IAN dataset to send.
     * @throws IOException          if an I/O error occurs.
     * @throws InterruptedException if the operation is interrupted.
     */
    private void sendIan(Attributes ian) throws IOException, InterruptedException {
        as.ncreate(UID.InstanceAvailabilityNotification.uid, null, ian, null, new DimseRSPHandler(as.nextMessageID()));
    }

    /**
     * Adds a DICOM instance to the list of notifications to be sent. Instances are grouped by Study Instance UID.
     *
     * @param inst The attributes of the DICOM instance.
     * @return {@code true} if the instance was added successfully, {@code false} if it lacks a Study Instance UID.
     */
    public boolean addInstance(Attributes inst) {
        String suid = inst.getString(Tag.StudyInstanceUID);
        if (suid == null)
            return false;

        Attributes ian = map.computeIfAbsent(suid, k -> createIAN(inst));
        updateIAN(ian, inst);
        return true;
    }

    /**
     * Adds a pre-formatted IAN dataset to the map of notifications to be sent.
     *
     * @param iuid The SOP Instance UID of the IAN object.
     * @param ian  The IAN dataset.
     * @return {@code true} always.
     */
    public boolean addIAN(String iuid, Attributes ian) {
        map.put(iuid, ian);
        return true;
    }

    /**
     * Creates a new IAN dataset for a given study.
     *
     * @param inst The first instance of the study, used to get the Study Instance UID.
     * @return A new IAN dataset with the basic structure.
     */
    private Attributes createIAN(Attributes inst) {
        Attributes ian = new Attributes(3);
        Sequence refPpsSeq = ian.newSequence(Tag.ReferencedPerformedProcedureStepSequence, 1);
        if (refPpsIUID != null) {
            Attributes refPps = new Attributes(3);
            refPps.setString(Tag.ReferencedSOPClassUID, VR.UI, refPpsCUID);
            refPps.setString(Tag.ReferencedSOPInstanceUID, VR.UI, refPpsIUID);
            refPps.setNull(Tag.PerformedWorkitemCodeSequence, VR.SQ);
            refPpsSeq.add(refPps);
        }
        ian.newSequence(Tag.ReferencedSeriesSequence, 1);
        ian.setString(Tag.StudyInstanceUID, VR.UI, inst.getString(Tag.StudyInstanceUID));
        return ian;
    }

    /**
     * Updates an IAN dataset with information from a new instance. It adds a reference to the instance within the
     * correct series.
     *
     * @param ian  The IAN dataset to update.
     * @param inst The new instance to add.
     */
    private void updateIAN(Attributes ian, Attributes inst) {
        Sequence refSeriesSeq = ian.getSequence(Tag.ReferencedSeriesSequence);
        Attributes refSeries = getRefSeries(refSeriesSeq, inst);
        Sequence refSOPSeq = refSeries.getSequence(Tag.ReferencedSOPSequence);
        Attributes refSOP = new Attributes(6);
        refSOP.setString(Tag.RetrieveAETitle, VR.AE, getRetrieveAET());
        refSOP.setString(Tag.InstanceAvailability, VR.CS, availability);
        refSOP.setString(Tag.ReferencedSOPClassUID, VR.UI, inst.getString(Tag.SOPClassUID));
        refSOP.setString(Tag.ReferencedSOPInstanceUID, VR.UI, inst.getString(Tag.SOPInstanceUID));
        if (retrieveURL != null)
            refSOP.setString(Tag.RetrieveURL, VR.UR, retrieveURL);
        if (retrieveURI != null)
            refSOP.setString(Tag.RetrieveURI, VR.UR, retrieveURI);
        if (retrieveUID != null)
            refSOP.setString(Tag.RetrieveLocationUID, VR.UI, retrieveUID);
        refSOPSeq.add(refSOP);
    }

    /**
     * Finds or creates a Referenced Series Sequence item for a given instance.
     *
     * @param refSeriesSeq The sequence of referenced series.
     * @param inst         The instance whose series is to be found or created.
     * @return The found or newly created Referenced Series item.
     */
    private Attributes getRefSeries(Sequence refSeriesSeq, Attributes inst) {
        String suid = inst.getString(Tag.SeriesInstanceUID);
        for (Attributes refSeries : refSeriesSeq) {
            if (suid.equals(refSeries.getString(Tag.SeriesInstanceUID)))
                return refSeries;
        }
        Attributes refSeries = new Attributes(2);
        refSeries.newSequence(Tag.ReferencedSOPSequence, 10);
        refSeries.setString(Tag.SeriesInstanceUID, VR.CS, suid);
        refSeriesSeq.add(refSeries);
        return refSeries;
    }

}
