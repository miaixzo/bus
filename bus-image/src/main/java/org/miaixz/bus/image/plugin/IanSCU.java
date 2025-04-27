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

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;

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

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class IanSCU {

    private final Device device = new Device("ianscu");
    private final ApplicationEntity ae = new ApplicationEntity("IANSCU");
    private final Connection conn = new Connection();
    private final Connection remote = new Connection();
    private final AAssociateRQ rq = new AAssociateRQ();
    private final Attributes attrs = new Attributes();
    private final HashMap<String, Attributes> map = new HashMap<>();
    private String uidSuffix;
    private String refPpsIUID;
    private String refPpsCUID = UID.ModalityPerformedProcedureStep.uid;
    private String availability = "ONLINE";
    private String retrieveAET;
    private String retrieveURI;
    private String retrieveURL;
    private String retrieveUID;
    private Association as;

    public IanSCU() {
        device.addConnection(conn);
        device.addApplicationEntity(ae);
        ae.addConnection(conn);
    }

    public final void setUIDSuffix(String uidSuffix) {
        this.uidSuffix = uidSuffix;
    }

    public void setTransferSyntaxes(String[] tss) {
        rq.addPresentationContext(new PresentationContext(1, UID.Verification.uid, UID.ImplicitVRLittleEndian.uid));
        rq.addPresentationContext(new PresentationContext(3, UID.InstanceAvailabilityNotification.uid, tss));
    }

    public void setRefPpsIUID(String refPpsIUID) {
        this.refPpsIUID = refPpsIUID;
    }

    public void setRefPpsCUID(String refPpsCUID) {
        this.refPpsCUID = refPpsCUID;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public String getRetrieveAET() {
        return retrieveAET != null ? retrieveAET : ae.getAETitle();
    }

    public void setRetrieveAET(String retrieveAET) {
        this.retrieveAET = retrieveAET;
    }

    public void setRetrieveURL(String retrieveURL) {
        this.retrieveURL = retrieveURL;
    }

    public void setRetrieveURI(String retrieveURI) {
        this.retrieveURI = retrieveURI;
    }

    public void setRetrieveUID(String retrieveUID) {
        this.retrieveUID = retrieveUID;
    }

    public void open() throws IOException, InterruptedException, InternalException, GeneralSecurityException {
        as = ae.connect(conn, remote, rq);
    }

    public void close() throws IOException, InterruptedException {
        if (as != null) {
            as.release();
        }
    }

    public void echo() throws IOException, InterruptedException {
        as.cecho().next();
    }

    public void sendIans() throws IOException, InterruptedException {
        for (Attributes ian : map.values())
            sendIan(ian);
    }

    private void sendIan(Attributes ian) throws IOException, InterruptedException {
        as.ncreate(UID.InstanceAvailabilityNotification.uid, null, ian, null, new DimseRSPHandler(as.nextMessageID()));
    }

    public boolean addInstance(Attributes inst) {
        String suid = inst.getString(Tag.StudyInstanceUID);
        if (suid == null)
            return false;

        Attributes ian = map.get(suid);
        if (ian == null)
            map.put(suid, ian = createIAN(inst));
        updateIAN(ian, inst);
        return true;
    }

    public boolean addIAN(String iuid, Attributes ian) {
        map.put(iuid, ian);
        return true;
    }

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

    private void updateIAN(Attributes mpps, Attributes inst) {
        Sequence refSeriesSeq = mpps.getSequence(Tag.ReferencedSeriesSequence);
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
