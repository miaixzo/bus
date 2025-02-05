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
package org.miaixz.bus.image.metric.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.miaixz.bus.image.Dimse;
import org.miaixz.bus.image.Status;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.VR;
import org.miaixz.bus.image.galaxy.io.ImageInputStream;
import org.miaixz.bus.image.metric.Association;
import org.miaixz.bus.image.metric.Commands;
import org.miaixz.bus.image.metric.DataWriter;
import org.miaixz.bus.image.metric.DimseRSPHandler;
import org.miaixz.bus.image.metric.net.InputStreamDataWriter;
import org.miaixz.bus.image.metric.pdu.PresentationContext;
import org.miaixz.bus.logger.Logger;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class BasicRetrieveTask<T extends InstanceLocator> implements RetrieveTask {

    protected final Dimse rq;
    protected final Association rqas;
    protected final Association storeas;
    protected final PresentationContext pc;
    protected final Attributes rqCmd;
    protected final int msgId;
    protected final int priority;
    protected final List<T> insts;
    protected final List<T> completed;
    protected final List<T> warning;
    protected final List<T> failed;
    protected int status = Status.Success;
    protected boolean pendingRSP;
    protected int pendingRSPInterval;
    protected boolean canceled;
    protected int outstandingRSP = 0;
    protected Object outstandingRSPLock = new Object();

    private ScheduledFuture<?> writePendingRSP;

    public BasicRetrieveTask(Dimse rq, Association rqas, PresentationContext pc, Attributes rqCmd, List<T> insts,
            Association storeas) {
        this.rq = rq;
        this.rqas = rqas;
        this.storeas = storeas;
        this.pc = pc;
        this.rqCmd = rqCmd;
        this.insts = insts;
        this.msgId = rqCmd.getInt(Tag.MessageID, -1);
        this.priority = rqCmd.getInt(Tag.Priority, 0);
        this.completed = new ArrayList<>(insts.size());
        this.warning = new ArrayList<>(insts.size());
        this.failed = new ArrayList<>(insts.size());
    }

    public void setSendPendingRSP(boolean pendingRSP) {
        this.pendingRSP = pendingRSP;
    }

    public void setSendPendingRSPInterval(int pendingRSPInterval) {
        this.pendingRSPInterval = pendingRSPInterval;
    }

    public boolean isCMove() {
        return rq == Dimse.C_MOVE_RQ;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public int getStatus() {
        return status;
    }

    public Association getRequestAssociation() {
        return rqas;
    }

    public Association getStoreAssociation() {
        return storeas;
    }

    public List<T> getCompleted() {
        return completed;
    }

    public List<T> getWarning() {
        return warning;
    }

    public List<T> getFailed() {
        return failed;
    }

    @Override
    public void onCancelRQ(Association as) {
        canceled = true;
    }

    @Override
    public void run() {
        rqas.addCancelRQHandler(msgId, this);
        try {
            if (pendingRSPInterval > 0)
                startWritePendingRSP();
            for (Iterator<T> iter = insts.iterator(); iter.hasNext();) {
                T inst = iter.next();
                if (canceled) {
                    status = Status.Cancel;
                    break;
                }
                if (pendingRSP)
                    writePendingRSP();
                String tsuid;
                DataWriter dataWriter;
                try {
                    tsuid = selectTransferSyntaxFor(storeas, inst);
                    dataWriter = createDataWriter(inst, tsuid);
                } catch (Exception e) {
                    status = Status.OneOrMoreFailures;
                    Logger.info("{}: Unable to retrieve {}/{} to {}", rqas, UID.nameOf(inst.cuid),
                            UID.nameOf(inst.tsuid), storeas.getRemoteAET(), e);
                    failed.add(inst);
                    continue;
                }
                try {
                    cstore(storeas, inst, tsuid, dataWriter);
                } catch (Exception e) {
                    status = Status.UnableToPerformSubOperations;
                    Logger.warn("{}: Unable to perform sub-operation on association to {}", rqas,
                            storeas.getRemoteAET(), e);
                    failed.add(inst);
                    while (iter.hasNext())
                        failed.add(iter.next());
                }
            }
            waitForOutstandingCStoreRSP(storeas);
            if (isCMove())
                releaseStoreAssociation(storeas);
            stopWritePendingRSP();
            writeRSP(status);
        } finally {
            rqas.removeCancelRQHandler(msgId);
            try {
                close();
            } catch (Throwable e) {
                Logger.warn("Exception thrown by {}.close()", getClass().getName(), e);
            }
        }
    }

    private void startWritePendingRSP() {
        writePendingRSP = rqas.getApplicationEntity().getDevice().scheduleAtFixedRate(
                () -> BasicRetrieveTask.this.writePendingRSP(), 0, pendingRSPInterval, TimeUnit.SECONDS);
    }

    private void stopWritePendingRSP() {
        if (writePendingRSP != null)
            writePendingRSP.cancel(false);
    }

    private void waitForOutstandingCStoreRSP(Association storeas) {
        try {
            synchronized (outstandingRSPLock) {
                while (outstandingRSP > 0)
                    outstandingRSPLock.wait();
            }
        } catch (InterruptedException e) {
            Logger.warn("{}: failed to wait for outstanding RSP on association to {}", rqas, storeas.getRemoteAET(), e);
        }
    }

    protected void releaseStoreAssociation(Association storeas) {
        try {
            if (storeas.isReadyForDataTransfer())
                storeas.release();
        } catch (IOException e) {
            Logger.warn("{}: failed to release association to {}", rqas, storeas.getRemoteAET(), e);
        }
    }

    protected void cstore(Association storeas, T inst, String tsuid, DataWriter dataWriter)
            throws IOException, InterruptedException {
        DimseRSPHandler rspHandler = new CStoreRSPHandler(storeas.nextMessageID(), inst);
        if (isCMove())
            storeas.cstore(inst.cuid, inst.iuid, priority, rqas.getRemoteAET(), msgId, dataWriter, tsuid, rspHandler);
        else
            storeas.cstore(inst.cuid, inst.iuid, priority, dataWriter, tsuid, rspHandler);
        synchronized (outstandingRSPLock) {
            outstandingRSP++;
        }
    }

    protected String selectTransferSyntaxFor(Association storeas, T inst) throws Exception {
        return inst.tsuid;
    }

    protected DataWriter createDataWriter(T inst, String tsuid) throws Exception {
        ImageInputStream in = new ImageInputStream(inst.getFile());
        in.readFileMetaInformation();
        return new InputStreamDataWriter(in);
    }

    public void writePendingRSP() {
        writeRSP(Status.Pending);
    }

    private void writeRSP(int status) {
        Attributes cmd = Commands.mkRSP(rqCmd, status, rq);
        if (status == Status.Pending || status == Status.Cancel)
            cmd.setInt(Tag.NumberOfRemainingSuboperations, VR.US, remaining());
        cmd.setInt(Tag.NumberOfCompletedSuboperations, VR.US, completed.size());
        cmd.setInt(Tag.NumberOfFailedSuboperations, VR.US, failed.size());
        cmd.setInt(Tag.NumberOfWarningSuboperations, VR.US, warning.size());
        Attributes data = null;
        if (!failed.isEmpty() && status != Status.Pending) {
            data = new Attributes(1);
            String[] iuids = new String[failed.size()];
            for (int i = 0; i < iuids.length; i++) {
                iuids[i] = failed.get(i).iuid;
            }
            data.setString(Tag.FailedSOPInstanceUIDList, VR.UI, iuids);
        }
        writeRSP(cmd, data);
    }

    private void writeRSP(Attributes cmd, Attributes data) {
        try {
            rqas.writeDimseRSP(pc, cmd, data);
        } catch (IOException e) {
            pendingRSP = false;
            stopWritePendingRSP();
            Logger.warn("{}: Unable to send C-GET or C-MOVE RSP on association to {}", rqas, rqas.getRemoteAET(), e);
        }
    }

    private int remaining() {
        return insts.size() - completed.size() - warning.size() - failed.size();
    }

    protected void close() {
    }

    private final class CStoreRSPHandler extends DimseRSPHandler {

        private final T inst;

        public CStoreRSPHandler(int msgId, T inst) {
            super(msgId);
            this.inst = inst;
        }

        @Override
        public void onDimseRSP(Association as, Attributes cmd, Attributes data) {
            super.onDimseRSP(as, cmd, data);
            int storeStatus = cmd.getInt(Tag.Status, -1);
            if (storeStatus == Status.Success)
                completed.add(inst);
            else if ((storeStatus & 0xB000) == 0xB000)
                warning.add(inst);
            else {
                failed.add(inst);
                if (status == Status.Success)
                    status = Status.OneOrMoreFailures;
            }
            synchronized (outstandingRSPLock) {
                if (--outstandingRSP == 0)
                    outstandingRSPLock.notify();
            }
        }

        @Override
        public void onClose(Association as) {
            super.onClose(as);
            synchronized (outstandingRSPLock) {
                outstandingRSP = 0;
                outstandingRSPLock.notify();
            }
        }
    }

}
