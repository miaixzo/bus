/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
package org.miaixz.bus.image.plugin;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.toolkit.IoKit;
import org.miaixz.bus.image.*;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.Sequence;
import org.miaixz.bus.image.galaxy.data.VR;
import org.miaixz.bus.image.galaxy.io.ImageOutputStream;
import org.miaixz.bus.image.metric.*;
import org.miaixz.bus.image.metric.internal.pdu.AAssociateRQ;
import org.miaixz.bus.image.metric.internal.pdu.Presentation;
import org.miaixz.bus.image.metric.service.AbstractService;
import org.miaixz.bus.image.metric.service.BasicCEchoSCP;
import org.miaixz.bus.image.metric.service.ImageService;
import org.miaixz.bus.image.metric.service.ServiceHandler;
import org.miaixz.bus.logger.Logger;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class StgSCU {

    private final ApplicationEntity ae;
    private final Connection remote;
    private final AAssociateRQ rq = new AAssociateRQ();
    private final HashSet<String> outstandingResults = new HashSet<String>(2);
    private final HashMap<String, List<String>> map = new HashMap<>();
    private Attributes attrs;
    private String uidSuffix;
    private File storageDir;
    private boolean keepAlive;
    private int splitTag;
    private int status;
    private final ImageService stgcmtResultHandler =
            new AbstractService(UID.StorageCommitmentPushModelSOPClass) {

                @Override
                public void onDimse(Association as, Presentation pc,
                                    Dimse dimse, Attributes cmd, Attributes data)
                        throws IOException {
                    if (dimse != Dimse.N_EVENT_REPORT_RQ)
                        throw new ImageException(Status.UnrecognizedOperation);

                    int eventTypeID = cmd.getInt(Tag.EventTypeID, 0);
                    if (eventTypeID != 1 && eventTypeID != 2)
                        throw new ImageException(Status.NoSuchEventType)
                                .setEventTypeID(eventTypeID);
                    String tuid = data.getString(Tag.TransactionUID);
                    try {
                        Attributes rsp = Commands.mkNEventReportRSP(cmd, status);
                        Attributes rspAttrs = StgSCU.this.eventRecord(as, cmd, data);
                        as.writeDimseRSP(pc, rsp, rspAttrs);
                    } catch (InternalException e) {
                        Logger.warn("{} << N-EVENT-RECORD-RSP failed: {}", as, e.getMessage());
                    } finally {
                        removeOutstandingResult(tuid);
                    }
                }
            };
    private Association as;

    public StgSCU(ApplicationEntity ae) {
        this.remote = new Connection();
        this.ae = ae;
        ServiceHandler serviceHandler = new ServiceHandler();
        serviceHandler.addService(new BasicCEchoSCP());
        serviceHandler.addService(stgcmtResultHandler);
        ae.setDimseRQHandler(serviceHandler);
    }

    public Connection getRemoteConnection() {
        return remote;
    }

    public AAssociateRQ getAAssociateRQ() {
        return rq;
    }

    public File getStorageDirectory() {
        return storageDir;
    }

    public void setStorageDirectory(File storageDir) {
        if (null != storageDir)
            storageDir.mkdirs();
        this.storageDir = storageDir;
    }

    public final void setUIDSuffix(String uidSuffix) {
        this.uidSuffix = uidSuffix;
    }

    public void setAttributes(Attributes attrs) {
        this.attrs = attrs;
    }

    public void setSplitTag(int splitTag) {
        this.splitTag = splitTag;
    }

    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setTransferSyntaxes(String[] tss) {
        rq.addPresentationContext(
                new Presentation(1, UID.VerificationSOPClass,
                        UID.ImplicitVRLittleEndian));
        rq.addPresentationContext(
                new Presentation(2,
                        UID.StorageCommitmentPushModelSOPClass,
                        tss));
        ae.addTransferCapability(
                new TransferCapability(null,
                        UID.VerificationSOPClass,
                        TransferCapability.Role.SCP,
                        UID.ImplicitVRLittleEndian));
        ae.addTransferCapability(
                new TransferCapability(null,
                        UID.StorageCommitmentPushModelSOPClass,
                        TransferCapability.Role.SCU,
                        tss));
    }

    public boolean addInstance(Attributes inst) {
        Builder.updateAttributes(inst, attrs, uidSuffix);
        String cuid = inst.getString(Tag.SOPClassUID);
        String iuid = inst.getString(Tag.SOPInstanceUID);
        String splitkey = splitTag != 0 ? inst.getString(splitTag) : Normal.EMPTY;
        if (null == cuid || null == iuid || null == splitkey)
            return false;

        List<String> refSOPs = map.get(splitkey);
        if (null == refSOPs)
            map.put(splitkey, refSOPs = new ArrayList<>());

        refSOPs.add(cuid);
        refSOPs.add(iuid);
        return true;
    }

    public void open() throws IOException, InterruptedException,
            InternalException, GeneralSecurityException {
        as = ae.connect(remote, rq);
    }

    public void echo() throws IOException, InterruptedException {
        as.cecho().next();
    }

    public void close() throws IOException, InterruptedException {
        if (null != as) {
            if (as.isReadyForDataTransfer()) {
                as.waitForOutstandingRSP();
                if (keepAlive)
                    waitForOutstandingResults();
                as.release();
            }
            as.waitForSocketClose();
        }
        waitForOutstandingResults();
    }

    private void addOutstandingResult(String tuid) {
        synchronized (outstandingResults) {
            outstandingResults.add(tuid);
        }
    }

    private void removeOutstandingResult(String tuid) {
        synchronized (outstandingResults) {
            outstandingResults.remove(tuid);
            outstandingResults.notify();
        }
    }

    private void waitForOutstandingResults() throws InterruptedException {
        synchronized (outstandingResults) {
            while (!outstandingResults.isEmpty()) {
                Logger.info(Normal.EMPTY + outstandingResults.size());
                outstandingResults.wait();
            }
        }
    }

    public Attributes makeActionInfo(List<String> refSOPs) {
        Attributes actionInfo = new Attributes(2);
        actionInfo.setString(Tag.TransactionUID, VR.UI, UID.createUID());
        int n = refSOPs.size() / 2;
        Sequence refSOPSeq = actionInfo.newSequence(Tag.ReferencedSOPSequence, n);
        for (int i = 0, j = 0; j < n; j++) {
            Attributes refSOP = new Attributes(2);
            refSOP.setString(Tag.ReferencedSOPClassUID, VR.UI, refSOPs.get(i++));
            refSOP.setString(Tag.ReferencedSOPInstanceUID, VR.UI, refSOPs.get(i++));
            refSOPSeq.add(refSOP);
        }
        return actionInfo;
    }

    public void sendRequests() throws IOException, InterruptedException {
        for (List<String> refSOPs : map.values())
            sendRequest(makeActionInfo(refSOPs));
    }

    private void sendRequest(Attributes actionInfo) throws IOException, InterruptedException {
        final String tuid = actionInfo.getString(Tag.TransactionUID);
        DimseRSPHandler rspHandler = new DimseRSPHandler(as.nextMessageID()) {

            @Override
            public void onDimseRSP(Association as, Attributes cmd, Attributes data) {
                if (cmd.getInt(Tag.Status, -1) != Status.Success)
                    removeOutstandingResult(tuid);
                super.onDimseRSP(as, cmd, data);
            }
        };

        as.naction(UID.StorageCommitmentPushModelSOPClass,
                UID.StorageCommitmentPushModelSOPInstance,
                1, actionInfo, null, rspHandler);
        addOutstandingResult(tuid);
    }

    private Attributes eventRecord(Association as, Attributes cmd, Attributes eventInfo)
            throws ImageException {
        if (null == storageDir)
            return null;

        String cuid = cmd.getString(Tag.AffectedSOPClassUID);
        String iuid = cmd.getString(Tag.AffectedSOPInstanceUID);
        String tuid = eventInfo.getString(Tag.TransactionUID);
        File file = new File(storageDir, tuid);
        ImageOutputStream out = null;
        Logger.info("{}: M-WRITE {}", as, file);
        try {
            out = new ImageOutputStream(file);
            out.writeDataset(
                    Attributes.createFileMetaInformation(iuid, cuid,
                            UID.ExplicitVRLittleEndian),
                    eventInfo);
        } catch (IOException e) {
            Logger.warn(as + ": Failed to store Storage Commitment Result:", e);
            throw new ImageException(Status.ProcessingFailure, e);
        } finally {
            IoKit.close(out);
        }
        return null;
    }

}
