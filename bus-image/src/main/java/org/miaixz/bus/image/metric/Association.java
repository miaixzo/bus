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
package org.miaixz.bus.image.metric;

import org.miaixz.bus.core.exception.InternalException;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.toolkit.IoKit;
import org.miaixz.bus.image.*;
import org.miaixz.bus.image.galaxy.Capacity;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.VR;
import org.miaixz.bus.image.metric.internal.pdu.*;
import org.miaixz.bus.logger.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class Association {

    private static final AtomicInteger prevSerialNo = new AtomicInteger();
    private final AtomicInteger messageID = new AtomicInteger();
    private final AtomicIntegerArray dimseCounters = new AtomicIntegerArray(46);
    private final long connectTime;
    private final int serialNo;
    private final boolean requestor;
    private final Device device;
    private final AssociationMonitor monitor;
    private final Connection conn;
    private final Socket sock;
    private final InputStream in;
    private final OutputStream out;
    private final PDUEncoder encoder;
    private final Capacity<DimseRSPHandler> rspHandlerForMsgId = new Capacity<>();
    private final Capacity<CancelRQHandler> cancelHandlerForMsgId = new Capacity<>();
    private final Map<String, Map<String, Presentation>> pcMap = new HashMap<>();
    private final LinkedList<AssociationListener> listeners = new LinkedList<>();
    private String name;
    private ApplicationEntity ae;
    private PDUDecoder decoder;
    private State state;
    private AAssociateRQ rq;
    private AAssociateAC ac;
    private IOException ex;
    private Map<String, Object> properties;
    private int maxOpsInvoked;
    private int maxPDULength;
    private int performing;
    private Timeout timeout;

    Association(ApplicationEntity ae, Connection local, Socket sock)
            throws IOException {
        this.connectTime = System.currentTimeMillis();
        this.serialNo = prevSerialNo.incrementAndGet();
        this.ae = ae;
        this.requestor = null != ae;
        this.name = Normal.EMPTY + sock.getLocalSocketAddress()
                + delim() + sock.getRemoteSocketAddress()
                + Symbol.C_PARENTHESE_LEFT + serialNo + Symbol.C_PARENTHESE_RIGHT;
        this.conn = local;
        this.device = local.getDevice();
        this.monitor = device.getAssociationMonitor();
        this.sock = sock;
        this.in = sock.getInputStream();
        this.out = sock.getOutputStream();
        this.encoder = new PDUEncoder(this, out);
        if (requestor) {
            enterState(State.Sta4);
        } else {
            enterState(State.Sta2);
            startRequestTimeout();
        }
        activate();
    }

    static int minZeroAsMax(int i1, int i2) {
        return i1 == 0 ? i2 : i2 == 0 ? i1 : Math.min(i1, i2);
    }

    public long getConnectTimeInMillis() {
        return connectTime;
    }

    public int getSerialNo() {
        return serialNo;
    }

    public Device getDevice() {
        return device;
    }

    public int nextMessageID() {
        return messageID.incrementAndGet() & 0xFFFF;
    }

    private String delim() {
        return requestor ? "->" : "<-";
    }

    public int getNumberOfSent(Dimse dimse) {
        return dimseCounters.get(dimse.ordinal());
    }

    public int getNumberOfReceived(Dimse dimse) {
        return dimseCounters.get(23 + dimse.ordinal());
    }

    public void incSentCount(Dimse dimse) {
        dimseCounters.getAndIncrement(dimse.ordinal());
    }

    void incReceivedCount(Dimse dimse) {
        dimseCounters.getAndIncrement(23 + dimse.ordinal());
    }

    @Override
    public String toString() {
        return name;
    }

    public final Socket getSocket() {
        return sock;
    }

    public final Connection getConnection() {
        return conn;
    }

    public final AAssociateRQ getAAssociateRQ() {
        return rq;
    }

    public final AAssociateAC getAAssociateAC() {
        return ac;
    }

    public final IOException getException() {
        return ex;
    }

    public final ApplicationEntity getApplicationEntity() {
        return ae;
    }

    public Set<String> getPropertyNames() {
        return null != properties ? properties.keySet() : Collections.emptySet();
    }

    public Object getProperty(String key) {
        return null != properties ? properties.get(key) : null;
    }

    public <T> T getProperty(Class<T> clazz) {
        return (T) getProperty(clazz.getName());
    }

    public <T> void setProperty(Class<T> clazz, Object value) {
        setProperty(clazz.getName(), value);
    }

    public boolean containsProperty(String key) {
        return null != properties && properties.containsKey(key);
    }

    public Object setProperty(String key, Object value) {
        if (null == properties)
            properties = new HashMap<>();
        return properties.put(key, value);
    }

    public Object clearProperty(String key) {
        return null != properties ? properties.remove(key) : null;
    }

    public void addAssociationListener(AssociationListener listener) {
        listeners.add(listener);
    }

    public void removeAssociationListener(AssociationListener listener) {
        listeners.remove(listener);
    }

    public final boolean isRequestor() {
        return requestor;
    }

    public boolean isReadyForDataTransfer() {
        return state == State.Sta6;
    }

    private void checkIsSCP(String cuid) throws InternalException {
        if (!isSCPFor(cuid)) {
            InternalException ex = new InternalException(cuid, TransferCapability.Role.SCP);
            if (ae.isRoleSelectionNegotiationLenient() && null == ac.getRoleSelectionFor(cuid))
                Logger.info("{}: {}", this, ex.getMessage());
            else
                throw ex;
        }
    }

    public boolean isSCPFor(String cuid) {
        RoleSelection rolsel = ac.getRoleSelectionFor(cuid);
        if (null == rolsel)
            return !requestor;
        return requestor ? rolsel.isSCP() : rolsel.isSCU();
    }

    private void checkIsSCU(String cuid) throws InternalException {
        if (!isSCUFor(cuid)) {
            InternalException ex = new InternalException(cuid, TransferCapability.Role.SCU);
            if (ae.isRoleSelectionNegotiationLenient() && null == ac.getRoleSelectionFor(cuid))
                Logger.info("{}: {}", this, ex.getMessage());
            else
                throw ex;
        }
    }

    public boolean isSCUFor(String cuid) {
        RoleSelection rolsel = ac.getRoleSelectionFor(cuid);
        if (null == rolsel)
            return requestor;
        return requestor ? rolsel.isSCU() : rolsel.isSCP();
    }

    public String getCallingAET() {
        return null != rq ? rq.getCallingAET() : null;
    }

    public String getCalledAET() {
        return null != rq ? rq.getCalledAET() : null;
    }

    public String getRemoteAET() {
        return requestor ? getCalledAET() : getCallingAET();
    }

    public String getLocalAET() {
        return requestor ? getCallingAET() : getCalledAET();
    }

    public String getRemoteImplVersionName() {
        return (requestor ? ac : rq).getImplVersionName();
    }

    public String getRemoteImplClassUID() {
        return (requestor ? ac : rq).getImplClassUID();
    }

    public String getLocalImplVersionName() {
        return (requestor ? rq : ac).getImplVersionName();
    }

    public String getLocalImplClassUID() {
        return (requestor ? rq : ac).getImplClassUID();
    }

    public final int getMaxPDULengthSend() {
        return maxPDULength;
    }

    public boolean isPackPDV() {
        return conn.isPackPDV();
    }

    public void release() throws IOException {
        state.writeAReleaseRQ(this);
    }

    public void abort() {
        abort(new AAbort());
    }

    void abort(AAbort aa) {
        try {
            state.write(this, aa);
        } catch (IOException e) {
            Logger.error(e.getMessage());
        }
    }

    private synchronized void closeSocket() {
        state.closeSocket(this);
    }

    void doCloseSocket() {
        Logger.info("{}: close {}", name, sock);
        IoKit.close(sock);
        enterState(State.Sta1);
    }

    synchronized private void closeSocketDelayed() {
        state.closeSocketDelayed(this);
    }

    void doCloseSocketDelayed() {
        enterState(State.Sta13);
        int delay = conn.getSocketCloseDelay();
        if (delay > 0) {
            device.schedule(() -> closeSocket(), delay, TimeUnit.MILLISECONDS);
            Logger.debug("{}: closing {} in {} ms", name, sock, delay);
        } else
            closeSocket();
    }

    public synchronized void onIOException(IOException e) {
        if (null != ex)
            return;

        ex = e;
        Logger.info("{}: i/o exception: {} in State: {}",
                name, e, state);
        closeSocket();
    }

    void write(AAbort aa) throws IOException {
        Logger.info("{} << {}", name, aa.toString());
        encoder.write(aa);
        ex = aa;
        closeSocketDelayed();
    }

    void writeAReleaseRQ() throws IOException {
        Logger.info("{} << A-RELEASE-RQ", name);
        enterState(State.Sta7);
        stopTimeout();
        encoder.writeAReleaseRQ();
        startReleaseTimeout();
    }

    private void startRequestTimeout() {
        startTimeout("{}: start A-ASSOCIATE-RQ timeout of {}ms",
                "{}: A-ASSOCIATE-RQ timeout expired",
                "{}: stop A-ASSOCIATE-RQ timeout",
                conn.getRequestTimeout(), State.Sta2);
    }

    private void startAcceptTimeout() {
        startTimeout("{}: start A-ASSOCIATE-AC timeout of {}ms",
                "{}: A-ASSOCIATE-AC timeout expired",
                "{}: stop A-ASSOCIATE-AC timeout",
                conn.getAcceptTimeout(), State.Sta5);
    }

    private void startReleaseTimeout() {
        startTimeout("{}: start A-RELEASE-RP timeout of {}ms",
                "{}: A-RELEASE-RP timeout expired",
                "{}: stop A-RELEASE-RP timeout",
                conn.getReleaseTimeout(), State.Sta7);
    }

    private void startIdleTimeout() {
        startTimeout("{}: start idle timeout of {}ms",
                "{}: idle timeout expired",
                "{}: stop idle timeout",
                conn.getIdleTimeout(), State.Sta6);
    }

    private void startTimeout(String startMsg, String expiredMsg,
                              String cancelMsg, int timeout, State state) {
        if (timeout > 0 && performing == 0 && rspHandlerForMsgId.isEmpty()) {
            synchronized (this) {
                if (this.state == state) {
                    stopTimeout();
                    this.timeout = Timeout.start(this, startMsg, expiredMsg,
                            cancelMsg, timeout);
                }
            }
        }
    }

    private void startTimeout(final int msgID, int timeout, boolean stopOnPending) {
        if (timeout > 0) {
            synchronized (rspHandlerForMsgId) {
                DimseRSPHandler rspHandler = rspHandlerForMsgId.get(msgID);
                if (null != rspHandler) {
                    rspHandler.setTimeout(Timeout.start(this,
                            "{}: start " + msgID + ":DIMSE-RSP timeout of {}ms",
                            "{}: " + msgID + ":DIMSE-RSP timeout expired",
                            "{}: stop " + msgID + ":DIMSE-RSP timeout",
                            timeout), stopOnPending);
                }
            }
        }
    }

    private synchronized void stopTimeout() {
        if (null != timeout) {
            timeout.stop();
            timeout = null;
        }
    }

    public void waitForOutstandingRSP() throws InterruptedException {
        synchronized (rspHandlerForMsgId) {
            while (!rspHandlerForMsgId.isEmpty())
                rspHandlerForMsgId.wait();
        }
    }

    void write(AAssociateRQ rq) throws IOException {
        name = rq.getCallingAET() + delim() + rq.getCalledAET() + Symbol.C_PARENTHESE_LEFT + serialNo + Symbol.C_PARENTHESE_RIGHT;
        this.rq = rq;
        Logger.info("{} << A-ASSOCIATE-RQ", name);
        Logger.debug("{}", rq);
        enterState(State.Sta5);
        encoder.write(rq);
        startAcceptTimeout();
    }

    private void write(AAssociateAC ac) throws IOException {
        Logger.info("{} << A-ASSOCIATE-AC", name);
        Logger.debug("{}", ac);
        enterState(State.Sta6);
        encoder.write(ac);
        startIdleTimeout();
    }

    private void write(AAssociateRJ rj) throws IOException {
        Logger.info("{} << {}", name, rj.toString());
        encoder.write(rj);
        closeSocketDelayed();
    }

    private void checkException() throws IOException {
        if (null != ex)
            throw ex;
    }

    private synchronized void enterState(State newState) {
        Logger.debug("{}: enter state: {}", name, newState);
        this.state = newState;
        notifyAll();
    }

    public final State getState() {
        return state;
    }

    synchronized void waitForLeaving(State state)
            throws InterruptedException, IOException {
        while (this.state == state)
            wait();
        checkException();
    }

    synchronized void waitForEntering(State state)
            throws InterruptedException, IOException {
        while (this.state != state)
            wait();
        checkException();
    }

    public void waitForSocketClose()
            throws InterruptedException, IOException {
        waitForEntering(State.Sta1);
    }

    private void activate() {
        device.execute(() -> {
            try {
                decoder = new PDUDecoder(Association.this, in);
                device.addAssociation(Association.this);
                while (!(state == State.Sta1 || state == State.Sta13))
                    decoder.nextPDU();
            } catch (AAbort aa) {
                abort(aa);
            } catch (IOException e) {
                onIOException(e);
            } catch (Exception e) {
                onIOException(new IOException("Unexpected Error", e));
            } finally {
                device.removeAssociation(Association.this);
                onClose();
            }
        });
    }

    private void onClose() {
        stopTimeout();
        synchronized (rspHandlerForMsgId) {
            Capacity.Visitor<DimseRSPHandler> visitor =
                    (key, value) -> {
                        value.onClose(Association.this);
                        return true;
                    };
            rspHandlerForMsgId.accept(visitor);
            rspHandlerForMsgId.clear();
            rspHandlerForMsgId.notifyAll();
        }
        if (null != ae)
            ae.getDevice().getAssociationHandler().onClose(this);
        for (AssociationListener listener : listeners)
            listener.onClose(this);
    }

    public void onAAssociateRQ(AAssociateRQ rq) throws IOException {
        name = rq.getCalledAET() + delim() + rq.getCallingAET() + Symbol.C_PARENTHESE_LEFT + serialNo + Symbol.C_PARENTHESE_RIGHT;
        Logger.info("{} >> A-ASSOCIATE-RQ", name);
        Logger.debug("{}", rq);
        stopTimeout();
        state.onAAssociateRQ(this, rq);
    }

    void handle(AAssociateRQ rq) throws IOException {
        this.rq = rq;
        enterState(State.Sta3);
        try {
            ae = device.getApplicationEntity(rq.getCalledAET(), true);
            ac = device.getAssociationHandler().negotiate(this, rq);
            initPCMap();
            maxOpsInvoked = ac.getMaxOpsPerformed();
            maxPDULength = Association.minZeroAsMax(
                    rq.getMaxPDULength(), conn.getSendPDULength());
            write(ac);
            if (null != monitor)
                monitor.onAssociationAccepted(this);
        } catch (AAssociateRJ e) {
            write(e);
            if (null != monitor)
                monitor.onAssociationRejected(this, e);
        }
    }

    public void onAAssociateAC(AAssociateAC ac) throws IOException {
        Logger.info("{} >> A-ASSOCIATE-AC", name);
        Logger.debug("{}", ac);
        stopTimeout();
        state.onAAssociateAC(this, ac);
    }

    void handle(AAssociateAC ac) {
        this.ac = ac;
        initPCMap();
        maxOpsInvoked = ac.getMaxOpsInvoked();
        maxPDULength = Association.minZeroAsMax(
                ac.getMaxPDULength(), conn.getSendPDULength());
        enterState(State.Sta6);
        startIdleTimeout();
    }

    public void onAAssociateRJ(AAssociateRJ rj) throws IOException {
        Logger.info("{} >> {}", name, rj);
        state.onAAssociateRJ(this, rj);
    }

    void handle(AAssociateRJ rq) {
        ex = rq;
        closeSocket();
    }

    public void onAReleaseRQ() throws IOException {
        Logger.info("{} >> A-RELEASE-RQ", name);
        stopTimeout();
        state.onAReleaseRQ(this);
    }

    void handleAReleaseRQ() throws IOException {
        if (decoder.isPendingPDV()) {
            Logger.info("{}: unexpected A-RELEASE-RQ after P-DATA-TF with pending PDV", this);
            abort();
            return;
        }
        enterState(State.Sta8);
        waitForPerformingOps();
        Logger.info("{} << A-RELEASE-RP", name);
        encoder.writeAReleaseRP();
        closeSocketDelayed();
    }

    private synchronized void waitForPerformingOps() {
        while (performing > 0 && state == State.Sta8) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    void handleAReleaseRQCollision() throws IOException {
        if (isRequestor()) {
            enterState(State.Sta9);
            Logger.info("{} << A-RELEASE-RP", name);
            encoder.writeAReleaseRP();
            enterState(State.Sta11);
        } else {
            enterState(State.Sta10);
        }
    }

    public void onAReleaseRP() throws IOException {
        Logger.info("{} >> A-RELEASE-RP", name);
        stopTimeout();
        state.onAReleaseRP(this);
    }

    void handleAReleaseRP() {
        closeSocket();
    }

    void handleAReleaseRPCollision() throws IOException {
        enterState(State.Sta12);
        Logger.info("{} << A-RELEASE-RP", name);
        encoder.writeAReleaseRP();
        closeSocketDelayed();
    }

    public void onAAbort(AAbort aa) {
        Logger.info("{} >> {}", name, aa);
        stopTimeout();
        ex = aa;
        closeSocket();
    }

    void unexpectedPDU(String pdu) throws AAbort {
        Logger.warn("{} >> unexpected {} in state: {}",
                name, pdu, state);
        throw new AAbort(AAbort.UL_SERIVE_PROVIDER, AAbort.UNEXPECTED_PDU);
    }

    public void onPDataTF() throws IOException {
        state.onPDataTF(this);
    }

    void handlePDataTF() throws IOException {
        decoder.decodeDIMSE();
    }

    public void writePDataTF() throws IOException {
        checkException();
        state.writePDataTF(this);
    }

    void doWritePDataTF() throws IOException {
        encoder.writePDataTF();
    }

    public void onDimseRQ(Presentation pc, Dimse dimse, Attributes cmd,
                          PDVInputStream data) throws IOException {
        stopTimeout();
        incPerforming();
        incReceivedCount(dimse);
        ae.onDimseRQ(this, pc, dimse, cmd, data);
    }

    private synchronized void incPerforming() {
        ++performing;
    }

    private synchronized void decPerforming() {
        --performing;
        notifyAll();
    }

    public void onDimseRSP(Dimse dimse, Attributes cmd, Attributes data) throws AAbort {
        int msgId = cmd.getInt(Tag.MessageIDBeingRespondedTo, -1);
        int status = cmd.getInt(Tag.Status, 0);
        boolean pending = Status.isPending(status);
        DimseRSPHandler rspHandler = getDimseRSPHandler(msgId);
        if (null == rspHandler) {
            Logger.info("{}: unexpected message ID in DIMSE RSP:", name);
            Logger.info("\n{}", cmd);
            throw new AAbort();
        }
        rspHandler.onDimseRSP(this, cmd, data);
        if (pending) {
            if (rspHandler.isStopOnPending())
                startTimeout(msgId, conn.getRetrieveTimeout(), true);
        } else {
            incReceivedCount(dimse);
            removeDimseRSPHandler(msgId);
            if (rspHandlerForMsgId.isEmpty() && performing == 0)
                startIdleOrReleaseTimeout();
        }
    }

    private synchronized void startIdleOrReleaseTimeout() {
        if (state == State.Sta6)
            startIdleTimeout();
        else if (state == State.Sta7)
            startReleaseTimeout();
    }

    private void addDimseRSPHandler(DimseRSPHandler rspHandler)
            throws InterruptedException {
        synchronized (rspHandlerForMsgId) {
            while (maxOpsInvoked > 0
                    && rspHandlerForMsgId.size() >= maxOpsInvoked)
                rspHandlerForMsgId.wait();
            rspHandlerForMsgId.put(rspHandler.getMessageID(), rspHandler);
        }
    }

    private DimseRSPHandler getDimseRSPHandler(int msgId) {
        synchronized (rspHandlerForMsgId) {
            return rspHandlerForMsgId.get(msgId);
        }
    }

    private DimseRSPHandler removeDimseRSPHandler(int msgId) {
        synchronized (rspHandlerForMsgId) {
            DimseRSPHandler tmp = rspHandlerForMsgId.remove(msgId);
            rspHandlerForMsgId.notifyAll();
            return tmp;
        }
    }

    void cancel(Presentation pc, int msgId) throws IOException {
        Attributes cmd = Commands.mkCCancelRQ(msgId);
        encoder.writeDIMSE(pc, cmd, null);
    }

    public boolean tryWriteDimseRSP(Presentation pc, Attributes cmd) {
        return tryWriteDimseRSP(pc, cmd, null);
    }

    public boolean tryWriteDimseRSP(Presentation pc, Attributes cmd,
                                    Attributes data) {
        try {
            writeDimseRSP(pc, cmd, data);
            return true;
        } catch (IOException e) {
            Logger.warn("{} << {} failed: {}", this,
                    Dimse.valueOf(cmd.getInt(Tag.CommandField, 0)),
                    e.getMessage());
            return false;
        }
    }

    public void writeDimseRSP(Presentation pc, Attributes cmd)
            throws IOException {
        writeDimseRSP(pc, cmd, null);
    }

    public void writeDimseRSP(Presentation pc, Attributes cmd,
                              Attributes data) throws IOException {
        DataWriter writer = null;
        int datasetType = Commands.NO_DATASET;
        if (null != data) {
            writer = new DataWriterAdapter(data);
            datasetType = Commands.getWithDatasetType();
        }
        cmd.setInt(Tag.CommandDataSetType, VR.US, datasetType);
        encoder.writeDIMSE(pc, cmd, writer);
        if (!Status.isPending(cmd.getInt(Tag.Status, 0))) {
            decPerforming();
            startIdleTimeout();
        }
    }

    public void onCancelRQ(Attributes cmd) {
        incReceivedCount(Dimse.C_CANCEL_RQ);
        int msgId = cmd.getInt(Tag.MessageIDBeingRespondedTo, -1);
        CancelRQHandler handler = removeCancelRQHandler(msgId);
        if (null != handler)
            handler.onCancelRQ(this);
    }

    public void addCancelRQHandler(int msgId, CancelRQHandler handler) {
        synchronized (cancelHandlerForMsgId) {
            cancelHandlerForMsgId.put(msgId, handler);
        }
    }

    public CancelRQHandler removeCancelRQHandler(int msgId) {
        synchronized (cancelHandlerForMsgId) {
            return cancelHandlerForMsgId.remove(msgId);
        }
    }

    private void initPCMap() {
        for (Presentation pc : ac.getPresentationContexts())
            if (pc.isAccepted()) {
                Presentation rqpc = rq.getPresentationContext(pc.getPCID());
                if (null != rqpc)
                    initTSMap(rqpc.getAbstractSyntax()).put(pc.getTransferSyntax(), pc);
                else
                    Logger.info("{}: Ignore unexpected {} in A-ASSOCIATE-AC", name, pc);
            }
    }

    private Map<String, Presentation> initTSMap(String as) {
        Map<String, Presentation> tsMap = pcMap.get(as);
        if (null == tsMap)
            pcMap.put(as, tsMap = new HashMap<>());
        return tsMap;
    }

    public Presentation pcFor(String cuid, String tsuid)
            throws InternalException {
        Map<String, Presentation> tsMap = pcMap.get(cuid);
        if (null == tsMap)
            throw new InternalException(cuid);
        if (null == tsuid)
            return tsMap.values().iterator().next();
        Presentation pc = tsMap.get(tsuid);
        if (null == pc)
            throw new InternalException(cuid, tsuid);
        return pc;
    }

    public Set<String> getTransferSyntaxesFor(String cuid) {
        Map<String, Presentation> tsMap = pcMap.get(cuid);
        if (null == tsMap)
            return Collections.emptySet();
        return Collections.unmodifiableSet(tsMap.keySet());
    }

    public Presentation getPresentationContext(int pcid) {
        return ac.getPresentationContext(pcid);
    }

    public CommonExtended getCommonExtendedNegotiationFor(
            String cuid) {
        return ac.getCommonExtendedNegotiationFor(cuid);
    }

    public void cstore(String cuid, String iuid, int priority, DataWriter data,
                       String tsuid, DimseRSPHandler rspHandler) throws IOException,
            InterruptedException {
        Presentation pc = pcFor(cuid, tsuid);
        checkIsSCU(cuid);
        Attributes cstorerq = Commands.mkCStoreRQ(rspHandler.getMessageID(),
                cuid, iuid, priority);
        invoke(pc, cstorerq, data, rspHandler, conn.getResponseTimeout());
    }

    public DimseRSP cstore(String cuid, String iuid, int priority,
                           DataWriter data, String tsuid)
            throws IOException, InterruptedException {
        FutureDimseRSP rsp = new FutureDimseRSP(nextMessageID());
        cstore(cuid, iuid, priority, data, tsuid, rsp);
        return rsp;
    }

    public void cstore(String cuid, String iuid, int priority,
                       String moveOriginatorAET, int moveOriginatorMsgId,
                       DataWriter data, String tsuid, DimseRSPHandler rspHandler)
            throws IOException, InterruptedException {
        Presentation pc = pcFor(cuid, tsuid);
        Attributes cstorerq = Commands.mkCStoreRQ(rspHandler.getMessageID(),
                cuid, iuid, priority, moveOriginatorAET, moveOriginatorMsgId);
        invoke(pc, cstorerq, data, rspHandler, conn.getResponseTimeout());
    }

    public DimseRSP cstore(String cuid, String iuid, int priority,
                           String moveOriginatorAET, int moveOriginatorMsgId,
                           DataWriter data, String tsuid) throws IOException,
            InterruptedException {
        FutureDimseRSP rsp = new FutureDimseRSP(nextMessageID());
        cstore(cuid, iuid, priority, moveOriginatorAET,
                moveOriginatorMsgId, data, tsuid, rsp);
        return rsp;
    }

    public void cfind(String cuid, int priority, Attributes data,
                      String tsuid, DimseRSPHandler rspHandler) throws IOException,
            InterruptedException {
        Presentation pc = pcFor(cuid, tsuid);
        checkIsSCU(cuid);
        Attributes cfindrq =
                Commands.mkCFindRQ(rspHandler.getMessageID(), cuid, priority);
        invoke(pc, cfindrq, new DataWriterAdapter(data), rspHandler,
                conn.getResponseTimeout());
    }

    public DimseRSP cfind(String cuid, int priority, Attributes data,
                          String tsuid, int autoCancel) throws IOException,
            InterruptedException {
        return cfind(cuid, priority, data, tsuid, autoCancel, Integer.MAX_VALUE);
    }

    /**
     * 发送C-FIND-RQ返回一个{@link DimseRSP#next()}阻塞的{@code DimseRSP}，直到接收到下一个C-FIND-RSP
     * 如果在返回的{@code DimseRSP}中缓存的接收到的C-FIND-RSP的数量达到指定的{@code capacity}
     * 则从关联块中读取C-FIND-RSP，直到缓存的C-FIND-RSP为通过调用{@link DimseRSP#next()}删除
     *
     * @param cuid       与操作关联的SOP类UID
     * @param priority   C-FIND操作的优先级  0 = MEDIUM, 1 = HIGH, 2 = LOW
     * @param data       编码要匹配的标识符的数据集
     * @param tsuid      用于编码标识符的传输语法
     * @param autoCancel 待发送的C-CINDL-RQ的挂起C-FIND RSP的数量
     * @param capacity   接收的未决C-FIND-RSP的缓冲区大小
     * @return {@link DimseRSP#next()}会阻止，直到接收到下一个C-FIND-RSP
     * @throws IOException          如果发送C-FIND-RQ时出错
     * @throws InterruptedException 如果任何线程在当前*线程正在等待其他调用的操作完成之前或之时中断了当前线程
     */
    public DimseRSP cfind(String cuid, int priority, Attributes data,
                          String tsuid, int autoCancel, int capacity) throws IOException,
            InterruptedException {
        FutureDimseRSP rsp = new FutureDimseRSP(nextMessageID());
        rsp.setAutoCancel(autoCancel);
        rsp.setCapacity(capacity);
        cfind(cuid, priority, data, tsuid, rsp);
        return rsp;
    }

    public void cget(String cuid, int priority, Attributes data,
                     String tsuid, DimseRSPHandler rspHandler)
            throws IOException, InterruptedException {
        Presentation pc = pcFor(cuid, tsuid);
        checkIsSCU(cuid);
        Attributes cgetrq = Commands.mkCGetRQ(rspHandler.getMessageID(),
                cuid, priority);
        invoke(pc, cgetrq, new DataWriterAdapter(data), rspHandler,
                conn.getRetrieveTimeout(), !conn.isRetrieveTimeoutTotal());
    }

    public DimseRSP cget(String cuid, int priority, Attributes data,
                         String tsuid) throws IOException,
            InterruptedException {
        FutureDimseRSP rsp = new FutureDimseRSP(nextMessageID());
        cget(cuid, priority, data, tsuid, rsp);
        return rsp;
    }

    public void cmove(String cuid, int priority, Attributes data,
                      String tsuid, String destination, DimseRSPHandler rspHandler)
            throws IOException, InterruptedException {
        Presentation pc = pcFor(cuid, tsuid);
        checkIsSCU(cuid);
        Attributes cmoverq = Commands.mkCMoveRQ(rspHandler.getMessageID(),
                cuid, priority, destination);
        invoke(pc, cmoverq, new DataWriterAdapter(data), rspHandler,
                conn.getRetrieveTimeout(), !conn.isRetrieveTimeoutTotal());
    }

    public DimseRSP cmove(String cuid, int priority, Attributes data,
                          String tsuid, String destination) throws IOException,
            InterruptedException {
        FutureDimseRSP rsp = new FutureDimseRSP(nextMessageID());
        cmove(cuid, priority, data, tsuid, destination, rsp);
        return rsp;
    }

    public DimseRSP cecho() throws IOException, InterruptedException {
        return cecho(UID.VerificationSOPClass);
    }

    public DimseRSP cecho(String cuid) throws IOException, InterruptedException {
        FutureDimseRSP rsp = new FutureDimseRSP(nextMessageID());
        Presentation pc = pcFor(cuid, null);
        checkIsSCU(cuid);
        Attributes cechorq = Commands.mkCEchoRQ(rsp.getMessageID(), cuid);
        invoke(pc, cechorq, null, rsp, conn.getResponseTimeout());
        return rsp;
    }

    public void neventReport(String cuid, String iuid, int eventTypeId,
                             Attributes data, String tsuid, DimseRSPHandler rspHandler)
            throws IOException, InterruptedException {
        neventReport(cuid, cuid, iuid, eventTypeId, data, tsuid, rspHandler);
    }

    public void neventReport(String asuid, String cuid, String iuid, int eventTypeId,
                             Attributes data, String tsuid, DimseRSPHandler rspHandler)
            throws IOException, InterruptedException {
        Presentation pc = pcFor(asuid, tsuid);
        checkIsSCP(cuid);
        Attributes neventrq =
                Commands.mkNEventReportRQ(rspHandler.getMessageID(), cuid, iuid,
                        eventTypeId, data);
        invoke(pc, neventrq, DataWriterAdapter.forAttributes(data), rspHandler,
                conn.getResponseTimeout());
    }

    public DimseRSP neventReport(String cuid, String iuid, int eventTypeId,
                                 Attributes data, String tsuid) throws IOException,
            InterruptedException {
        return neventReport(cuid, cuid, iuid, eventTypeId, data, tsuid);
    }

    public DimseRSP neventReport(String asuid, String cuid, String iuid,
                                 int eventTypeId, Attributes data, String tsuid)
            throws IOException, InterruptedException {
        FutureDimseRSP rsp = new FutureDimseRSP(nextMessageID());
        neventReport(asuid, cuid, iuid, eventTypeId, data, tsuid, rsp);
        return rsp;
    }

    public void nget(String cuid, String iuid, int[] tags,
                     DimseRSPHandler rspHandler)
            throws IOException, InterruptedException {
        nget(cuid, cuid, iuid, tags, rspHandler);
    }

    public void nget(String asuid, String cuid, String iuid, int[] tags,
                     DimseRSPHandler rspHandler)
            throws IOException, InterruptedException {
        Presentation pc = pcFor(asuid, null);
        checkIsSCU(cuid);
        Attributes ngetrq =
                Commands.mkNGetRQ(rspHandler.getMessageID(), cuid, iuid, tags);
        invoke(pc, ngetrq, null, rspHandler, conn.getResponseTimeout());
    }

    public DimseRSP nget(String cuid, String iuid, int[] tags)
            throws IOException, InterruptedException {
        return nget(cuid, cuid, iuid, tags);
    }

    public DimseRSP nget(String asuid, String cuid, String iuid, int[] tags)
            throws IOException, InterruptedException {
        FutureDimseRSP rsp = new FutureDimseRSP(nextMessageID());
        nget(asuid, cuid, iuid, tags, rsp);
        return rsp;
    }

    public void nset(String cuid, String iuid, Attributes data,
                     String tsuid, DimseRSPHandler rspHandler)
            throws IOException, InterruptedException {
        nset(cuid, cuid, iuid, new DataWriterAdapter(data), tsuid, rspHandler);
    }

    public void nset(String asuid, String cuid, String iuid,
                     Attributes data, String tsuid, DimseRSPHandler rspHandler)
            throws IOException, InterruptedException {
        nset(asuid, cuid, iuid, new DataWriterAdapter(data), tsuid, rspHandler);
    }

    public DimseRSP nset(String cuid, String iuid, Attributes data,
                         String tsuid) throws IOException,
            InterruptedException {
        return nset(cuid, cuid, iuid, new DataWriterAdapter(data), tsuid);
    }

    public DimseRSP nset(String asuid, String cuid, String iuid,
                         Attributes data, String tsuid)
            throws IOException, InterruptedException {
        return nset(asuid, cuid, iuid, new DataWriterAdapter(data), tsuid);
    }

    public void nset(String cuid, String iuid, DataWriter data,
                     String tsuid, DimseRSPHandler rspHandler)
            throws IOException, InterruptedException {
        nset(cuid, cuid, iuid, data, tsuid, rspHandler);
    }

    public void nset(String asuid, String cuid, String iuid,
                     DataWriter data, String tsuid, DimseRSPHandler rspHandler)
            throws IOException, InterruptedException {
        Presentation pc = pcFor(asuid, tsuid);
        checkIsSCU(cuid);
        Attributes nsetrq =
                Commands.mkNSetRQ(rspHandler.getMessageID(), cuid, iuid);
        invoke(pc, nsetrq, data, rspHandler, conn.getResponseTimeout());
    }

    public DimseRSP nset(String cuid, String iuid, DataWriter data,
                         String tsuid) throws IOException,
            InterruptedException {
        return nset(cuid, cuid, iuid, data, tsuid);
    }

    public DimseRSP nset(String asuid, String cuid, String iuid,
                         DataWriter data, String tsuid)
            throws IOException, InterruptedException {
        FutureDimseRSP rsp = new FutureDimseRSP(nextMessageID());
        nset(asuid, cuid, iuid, data, tsuid, rsp);
        return rsp;
    }

    public void naction(String cuid, String iuid, int actionTypeId,
                        Attributes data, String tsuid, DimseRSPHandler rspHandler)
            throws IOException, InterruptedException {
        naction(cuid, cuid, iuid, actionTypeId, data, tsuid, rspHandler);
    }

    public void naction(String asuid, String cuid, String iuid, int actionTypeId,
                        Attributes data, String tsuid, DimseRSPHandler rspHandler)
            throws IOException, InterruptedException {
        Presentation pc = pcFor(asuid, tsuid);
        checkIsSCU(cuid);
        Attributes nactionrq =
                Commands.mkNActionRQ(rspHandler.getMessageID(), cuid, iuid,
                        actionTypeId, data);
        invoke(pc, nactionrq, DataWriterAdapter.forAttributes(data), rspHandler,
                conn.getResponseTimeout());
    }

    public DimseRSP naction(String cuid, String iuid, int actionTypeId,
                            Attributes data, String tsuid) throws IOException,
            InterruptedException {
        return naction(cuid, cuid, iuid, actionTypeId, data, tsuid);
    }

    public DimseRSP naction(String asuid, String cuid, String iuid,
                            int actionTypeId, Attributes data, String tsuid)
            throws IOException, InterruptedException {
        FutureDimseRSP rsp = new FutureDimseRSP(nextMessageID());
        naction(asuid, cuid, iuid, actionTypeId, data, tsuid, rsp);
        return rsp;
    }

    public void ncreate(String cuid, String iuid, Attributes data,
                        String tsuid, DimseRSPHandler rspHandler)
            throws IOException, InterruptedException {
        ncreate(cuid, cuid, iuid, data, tsuid, rspHandler);
    }

    public void ncreate(String asuid, String cuid, String iuid,
                        Attributes data, String tsuid, DimseRSPHandler rspHandler)
            throws IOException, InterruptedException {
        Presentation pc = pcFor(asuid, tsuid);
        checkIsSCU(cuid);
        Attributes ncreaterq =
                Commands.mkNCreateRQ(rspHandler.getMessageID(), cuid, iuid);
        invoke(pc, ncreaterq, DataWriterAdapter.forAttributes(data), rspHandler,
                conn.getResponseTimeout());
    }

    public DimseRSP ncreate(String cuid, String iuid, Attributes data,
                            String tsuid) throws IOException,
            InterruptedException {
        return ncreate(cuid, cuid, iuid, data, tsuid);
    }

    public DimseRSP ncreate(String asuid, String cuid, String iuid,
                            Attributes data, String tsuid)
            throws IOException, InterruptedException {
        FutureDimseRSP rsp = new FutureDimseRSP(nextMessageID());
        ncreate(asuid, cuid, iuid, data, tsuid, rsp);
        return rsp;
    }

    public void ndelete(String cuid, String iuid, DimseRSPHandler rspHandler)
            throws IOException, InterruptedException {
        ndelete(cuid, cuid, iuid, rspHandler);
    }

    public void ndelete(String asuid, String cuid, String iuid,
                        DimseRSPHandler rspHandler)
            throws IOException, InterruptedException {
        Presentation pc = pcFor(asuid, null);
        checkIsSCU(cuid);
        Attributes ndeleterq =
                Commands.mkNDeleteRQ(rspHandler.getMessageID(), cuid, iuid);
        invoke(pc, ndeleterq, null, rspHandler, conn.getResponseTimeout());
    }

    public DimseRSP ndelete(String cuid, String iuid)
            throws IOException, InterruptedException {
        return ndelete(cuid, cuid, iuid);
    }

    public DimseRSP ndelete(String asuid, String cuid, String iuid)
            throws IOException, InterruptedException {
        FutureDimseRSP rsp = new FutureDimseRSP(nextMessageID());
        ndelete(asuid, cuid, iuid, rsp);
        return rsp;
    }

    public void invoke(Presentation pc, Attributes cmd,
                       DataWriter data, DimseRSPHandler rspHandler, int rspTimeout)
            throws IOException, InterruptedException {
        invoke(pc, cmd, data, rspHandler, rspTimeout, true);
    }

    public void invoke(Presentation pc, Attributes cmd,
                       DataWriter data, DimseRSPHandler rspHandler, int rspTimeout, boolean stopOnPending)
            throws IOException, InterruptedException {
        stopTimeout();
        checkException();
        rspHandler.setPC(pc);
        addDimseRSPHandler(rspHandler);
        encoder.writeDIMSE(pc, cmd, data);
        startTimeout(rspHandler.getMessageID(), rspTimeout, stopOnPending);
    }

    public Attributes createFileMetaInformation(String iuid, String cuid,
                                                String tsuid) {
        Attributes fmi = new Attributes(7);
        fmi.setBytes(Tag.FileMetaInformationVersion, VR.OB, new byte[]{0, 1});
        fmi.setString(Tag.MediaStorageSOPClassUID, VR.UI, cuid);
        fmi.setString(Tag.MediaStorageSOPInstanceUID, VR.UI, iuid);
        fmi.setString(Tag.TransferSyntaxUID, VR.UI, tsuid);
        fmi.setString(Tag.ImplementationClassUID, VR.UI,
                getRemoteImplClassUID());
        String versionName = getRemoteImplVersionName();
        if (null != versionName)
            fmi.setString(Tag.ImplementationVersionName, VR.SH, versionName);
        fmi.setString(Tag.SourceApplicationEntityTitle, VR.AE,
                getRemoteAET());
        return fmi;
    }

    public EnumSet<Option.Type> getQueryOptionsFor(String cuid) {
        return Option.Type.toOptions(ac.getExtNegotiationFor(cuid));
    }

    public enum State {
        Sta1("Sta1 - Idle") {
            @Override
            void write(Association as, AAbort aa) {
                // NO OP
            }

            @Override
            void closeSocket(Association as) {
                // NO OP
            }

            @Override
            void closeSocketDelayed(Association as) {
                // NO OP
            }
        },
        Sta2("Sta2 - Transport connection open") {
            @Override
            void onAAssociateRQ(Association as, AAssociateRQ rq)
                    throws IOException {
                as.handle(rq);
            }
        },
        Sta3("Sta3 - Awaiting local A-ASSOCIATE response primitive"),
        Sta4("Sta4 - Awaiting transport connection opening to complete"),
        Sta5("Sta5 - Awaiting A-ASSOCIATE-AC or A-ASSOCIATE-RJ PDU") {
            @Override
            void onAAssociateAC(Association as, AAssociateAC ac)
                    throws IOException {
                as.handle(ac);
            }

            @Override
            void onAAssociateRJ(Association as, AAssociateRJ rj)
                    throws IOException {
                as.handle(rj);
            }
        },
        Sta6("Sta6 - Association established and ready for data transfer") {
            @Override
            void onAReleaseRQ(Association as) throws IOException {
                as.handleAReleaseRQ();
            }

            @Override
            void onPDataTF(Association as) throws IOException {
                as.handlePDataTF();
            }

            @Override
            void writeAReleaseRQ(Association as) throws IOException {
                as.writeAReleaseRQ();
            }

            @Override
            public void writePDataTF(Association as) throws IOException {
                as.doWritePDataTF();
            }
        },
        Sta7("Sta7 - Awaiting A-RELEASE-RP PDU") {
            @Override
            public void onAReleaseRP(Association as) throws IOException {
                as.handleAReleaseRP();
            }

            @Override
            void onAReleaseRQ(Association as) throws IOException {
                as.handleAReleaseRQCollision();
            }

            @Override
            void onPDataTF(Association as) throws IOException {
                as.handlePDataTF();
            }
        },
        Sta8("Sta8 - Awaiting local A-RELEASE response primitive") {
            @Override
            public void writePDataTF(Association as) throws IOException {
                as.doWritePDataTF();
            }
        },
        Sta9("Sta9 - Release collision requestor side; awaiting A-RELEASE response"),
        Sta10("Sta10 - Release collision acceptor side; awaiting A-RELEASE-RP PDU") {
            @Override
            void onAReleaseRP(Association as) throws IOException {
                as.handleAReleaseRPCollision();
            }
        },
        Sta11("Sta11 - Release collision requestor side; awaiting A-RELEASE-RP PDU") {
            @Override
            void onAReleaseRP(Association as) throws IOException {
                as.handleAReleaseRP();
            }
        },
        Sta12("Sta12 - Release collision acceptor side; awaiting A-RELEASE response primitive"),
        Sta13("Sta13 - Awaiting Transport Connection Close Indication") {
            @Override
            public void onAReleaseRP(Association as) throws IOException {
                // NO OP
            }

            @Override
            void onAReleaseRQ(Association as) throws IOException {
                // NO OP
            }

            @Override
            void onPDataTF(Association as) throws IOException {
                // NO OP
            }

            @Override
            void write(Association as, AAbort aa) {
                // NO OP
            }

            @Override
            void closeSocketDelayed(Association as) {
                // NO OP
            }
        };

        private final String name;

        State(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        void onAAssociateRQ(Association as, AAssociateRQ rq)
                throws IOException {
            as.unexpectedPDU("A-ASSOCIATE-RQ");
        }

        void onAAssociateAC(Association as, AAssociateAC ac)
                throws IOException {
            as.unexpectedPDU("A-ASSOCIATE-AC");
        }

        void onAAssociateRJ(Association as, AAssociateRJ rj)
                throws IOException {
            as.unexpectedPDU("A-ASSOCIATE-RJ");
        }

        void onPDataTF(Association as) throws IOException {
            as.unexpectedPDU("P-DATA-TF");
        }

        void onAReleaseRQ(Association as) throws IOException {
            as.unexpectedPDU("A-RELEASE-RQ");
        }

        void onAReleaseRP(Association as) throws IOException {
            as.unexpectedPDU("A-RELEASE-RP");
        }

        void writeAReleaseRQ(Association as) throws IOException {
            throw new InternalException(this.toString());
        }

        void write(Association as, AAbort aa) throws IOException {
            as.write(aa);
        }

        public void writePDataTF(Association as) throws IOException {
            throw new InternalException(this.toString());
        }

        void closeSocket(Association as) {
            as.doCloseSocket();
        }

        void closeSocketDelayed(Association as) {
            as.doCloseSocketDelayed();
        }
    }

}

