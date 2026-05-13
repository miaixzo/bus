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
 * Represents the BasicRetrieveTask type.
 *
 * @param <T> the t type.
 * @author Kimi Liu
 * @since Java 21+
 */
public class BasicRetrieveTask<T extends InstanceLocator> implements RetrieveTask {

    /**
     * The rq value.
     */
    protected final Dimse rq;

    /**
     * The rqas value.
     */
    protected final Association rqas;

    /**
     * The storeas value.
     */
    protected final Association storeas;

    /**
     * The pc value.
     */
    protected final PresentationContext pc;

    /**
     * The rq cmd value.
     */
    protected final Attributes rqCmd;

    /**
     * The msg id value.
     */
    protected final int msgId;

    /**
     * The priority value.
     */
    protected final int priority;

    /**
     * The insts value.
     */
    protected final List<T> insts;

    /**
     * The completed value.
     */
    protected final List<T> completed;

    /**
     * The warning value.
     */
    protected final List<T> warning;

    /**
     * The failed value.
     */
    protected final List<T> failed;

    /**
     * The status value.
     */
    protected int status = Status.Success;

    /**
     * The pending rsp value.
     */
    protected boolean pendingRSP;

    /**
     * The pending rsp interval value.
     */
    protected int pendingRSPInterval;

    /**
     * The canceled value.
     */
    protected boolean canceled;

    /**
     * The outstanding rsp value.
     */
    protected int outstandingRSP = 0;

    /**
     * The outstanding rsp lock value.
     */
    protected Object outstandingRSPLock = new Object();

    /**
     * The write pending rsp value.
     */
    private ScheduledFuture<?> writePendingRSP;

    /**
     * Creates a new instance.
     *
     * @param rq      the rq.
     * @param rqas    the rqas.
     * @param pc      the pc.
     * @param rqCmd   the rq cmd.
     * @param insts   the insts.
     * @param storeas the storeas.
     */
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

    /**
     * Sets the send pending rsp.
     *
     * @param pendingRSP the pending rsp.
     */
    public void setSendPendingRSP(boolean pendingRSP) {
        this.pendingRSP = pendingRSP;
    }

    /**
     * Sets the send pending rsp interval.
     *
     * @param pendingRSPInterval the pending rsp interval.
     */
    public void setSendPendingRSPInterval(int pendingRSPInterval) {
        this.pendingRSPInterval = pendingRSPInterval;
    }

    /**
     * Determines whether c move.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isCMove() {
        return rq == Dimse.C_MOVE_RQ;
    }

    /**
     * Determines whether canceled.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isCanceled() {
        return canceled;
    }

    /**
     * Gets the status.
     *
     * @return the status.
     */
    public int getStatus() {
        return status;
    }

    /**
     * Gets the request association.
     *
     * @return the request association.
     */
    public Association getRequestAssociation() {
        return rqas;
    }

    /**
     * Gets the store association.
     *
     * @return the store association.
     */
    public Association getStoreAssociation() {
        return storeas;
    }

    /**
     * Gets the completed.
     *
     * @return the completed.
     */
    public List<T> getCompleted() {
        return completed;
    }

    /**
     * Gets the warning.
     *
     * @return the warning.
     */
    public List<T> getWarning() {
        return warning;
    }

    /**
     * Gets the failed.
     *
     * @return the failed.
     */
    public List<T> getFailed() {
        return failed;
    }

    /**
     * Executes the on cancel rq operation.
     *
     * @param as the as.
     */
    @Override
    public void onCancelRQ(Association as) {
        canceled = true;
    }

    /**
     * Executes the run operation.
     */
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
                    Logger.warn(
                            false,
                            "Image",
                            e,
                            "Retrieve instance preparation failed: protocol=dimse, requestAssociation={}, sopClass={}, transferSyntax={}, remoteAET={}, exception={}",
                            rqas,
                            UID.nameOf(inst.cuid),
                            UID.nameOf(inst.tsuid),
                            storeas.getRemoteAET(),
                            e.getClass().getSimpleName());
                    failed.add(inst);
                    continue;
                }
                try {
                    cstore(storeas, inst, tsuid, dataWriter);
                } catch (Exception e) {
                    status = Status.UnableToPerformSubOperations;
                    Logger.warn(
                            false,
                            "Image",
                            e,
                            "Retrieve sub-operation failed: protocol=dimse, requestAssociation={}, remoteAET={}, transferSyntax={}, exception={}",
                            rqas,
                            storeas.getRemoteAET(),
                            tsuid,
                            e.getClass().getSimpleName());
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
                Logger.warn(false, "Image", "Exception thrown by {}.close()", getClass().getName(), e);
            }
        }
    }

    /**
     * Executes the start write pending rsp operation.
     */
    private void startWritePendingRSP() {
        writePendingRSP = rqas.getApplicationEntity().getDevice().scheduleAtFixedRate(
                () -> BasicRetrieveTask.this.writePendingRSP(),
                0,
                pendingRSPInterval,
                TimeUnit.SECONDS);
    }

    /**
     * Executes the stop write pending rsp operation.
     */
    private void stopWritePendingRSP() {
        if (writePendingRSP != null)
            writePendingRSP.cancel(false);
    }

    /**
     * Executes the wait for outstanding c store rsp operation.
     *
     * @param storeas the storeas.
     */
    private void waitForOutstandingCStoreRSP(Association storeas) {
        try {
            synchronized (outstandingRSPLock) {
                while (outstandingRSP > 0)
                    outstandingRSPLock.wait();
            }
        } catch (InterruptedException e) {
            Logger.warn(
                    false,
                    "Image",
                    "{}: failed to wait for outstanding RSP on association to {}",
                    rqas,
                    storeas.getRemoteAET(),
                    e);
        }
    }

    /**
     * Executes the release store association operation.
     *
     * @param storeas the storeas.
     */
    protected void releaseStoreAssociation(Association storeas) {
        try {
            if (storeas.isReadyForDataTransfer())
                storeas.release();
        } catch (IOException e) {
            Logger.warn(false, "Image", "{}: failed to release association to {}", rqas, storeas.getRemoteAET(), e);
        }
    }

    /**
     * Executes the cstore operation.
     *
     * @param storeas    the storeas.
     * @param inst       the inst.
     * @param tsuid      the tsuid.
     * @param dataWriter the data writer.
     * @throws IOException          if the operation cannot be completed.
     * @throws InterruptedException if the operation cannot be completed.
     */
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

    /**
     * Executes the select transfer syntax for operation.
     *
     * @param storeas the storeas.
     * @param inst    the inst.
     * @return the operation result.
     * @throws Exception if the operation cannot be completed.
     */
    protected String selectTransferSyntaxFor(Association storeas, T inst) throws Exception {
        return inst.tsuid;
    }

    /**
     * Creates the data writer.
     *
     * @param inst  the inst.
     * @param tsuid the tsuid.
     * @return the operation result.
     * @throws Exception if the operation cannot be completed.
     */
    protected DataWriter createDataWriter(T inst, String tsuid) throws Exception {
        ImageInputStream in = new ImageInputStream(inst.getFile());
        in.readFileMetaInformation();
        return new InputStreamDataWriter(in);
    }

    /**
     * Writes the pending rsp.
     */
    public void writePendingRSP() {
        writeRSP(Status.Pending);
    }

    /**
     * Writes the rsp.
     *
     * @param status the status.
     */
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

    /**
     * Writes the rsp.
     *
     * @param cmd  the cmd.
     * @param data the data.
     */
    private void writeRSP(Attributes cmd, Attributes data) {
        try {
            rqas.writeDimseRSP(pc, cmd, data);
        } catch (IOException e) {
            pendingRSP = false;
            stopWritePendingRSP();
            Logger.warn(
                    false,
                    "Image",
                    "{}: Unable to send C-GET or C-MOVE RSP on association to {}",
                    rqas,
                    rqas.getRemoteAET(),
                    e);
        }
    }

    /**
     * Executes the remaining operation.
     *
     * @return the operation result.
     */
    private int remaining() {
        return insts.size() - completed.size() - warning.size() - failed.size();
    }

    /**
     * Executes the close operation.
     */
    protected void close() {
    }

    /**
     * Represents the CStoreRSPHandler type.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    private final class CStoreRSPHandler extends DimseRSPHandler {

        /**
         * The inst value.
         */
        private final T inst;

        /**
         * Creates a new instance.
         *
         * @param msgId the msg id.
         * @param inst  the inst.
         */
        public CStoreRSPHandler(int msgId, T inst) {
            super(msgId);
            this.inst = inst;
        }

        /**
         * Executes the on dimse rsp operation.
         *
         * @param as   the as.
         * @param cmd  the cmd.
         * @param data the data.
         */
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

        /**
         * Executes the on close operation.
         *
         * @param as the as.
         */
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
