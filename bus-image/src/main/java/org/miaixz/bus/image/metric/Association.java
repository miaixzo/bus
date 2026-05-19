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
package org.miaixz.bus.image.metric;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

import org.miaixz.bus.core.center.map.IntHashMap;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.image.*;
import org.miaixz.bus.image.galaxy.ReverseDNS;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.VR;
import org.miaixz.bus.image.metric.net.*;
import org.miaixz.bus.image.metric.pdu.*;
import org.miaixz.bus.logger.Logger;

/**
 * Represents the Association type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Association {

    /**
     * The prev serial no value.
     */
    private static final AtomicInteger prevSerialNo = new AtomicInteger();

    /**
     * The message id value.
     */
    private final AtomicInteger messageID = new AtomicInteger();

    /**
     * The dimse counters value.
     */
    private final AtomicIntegerArray dimseCounters = new AtomicIntegerArray(46);

    /**
     * The connect time value.
     */
    private final long connectTime;

    /**
     * The serial no value.
     */
    private final int serialNo;

    /**
     * The requestor value.
     */
    private final boolean requestor;

    /**
     * The device value.
     */
    private final Device device;

    /**
     * The monitor value.
     */
    private final AssociationMonitor monitor;

    /**
     * The conn value.
     */
    private final Connection conn;

    /**
     * The sock value.
     */
    private final Socket sock;

    /**
     * The in value.
     */
    private final InputStream in;

    /**
     * The out value.
     */
    private final OutputStream out;

    /**
     * The encoder value.
     */
    private final PDUEncoder encoder;

    /**
     * The rsp handler for msg id value.
     */
    private final IntHashMap<DimseRSPHandler> rspHandlerForMsgId = new IntHashMap<>();

    /**
     * The cancel handler for msg id value.
     */
    private final IntHashMap<CancelRQHandler> cancelHandlerForMsgId = new IntHashMap<>();

    /**
     * The pc map value.
     */
    private final HashMap<String, HashMap<String, PresentationContext>> pcMap = new HashMap<>();

    /**
     * The listeners value.
     */
    private final LinkedList<AssociationListener> listeners = new LinkedList<>();

    /**
     * The name value.
     */
    private String name;

    /**
     * The ae value.
     */
    private ApplicationEntity ae;

    /**
     * The decoder value.
     */
    private PDUDecoder decoder;

    /**
     * The state value.
     */
    private volatile State state;

    /**
     * The rq value.
     */
    private AAssociateRQ rq;

    /**
     * The ac value.
     */
    private AAssociateAC ac;

    /**
     * The ex value.
     */
    private IOException ex;

    /**
     * The properties value.
     */
    private HashMap<String, Object> properties;

    /**
     * The max ops invoked value.
     */
    private int maxOpsInvoked;

    /**
     * The max pdu length value.
     */
    private int maxPDULength;

    /**
     * The performing value.
     */
    private int performing;

    /**
     * The timeout value.
     */
    private Timeout timeout;

    /**
     * Creates a new instance.
     *
     * @param ae    the ae.
     * @param local the local.
     * @param sock  the sock.
     * @throws IOException if the operation cannot be completed.
     */
    public Association(ApplicationEntity ae, Connection local, Socket sock) throws IOException {
        this.connectTime = System.currentTimeMillis();
        this.serialNo = prevSerialNo.incrementAndGet();
        this.ae = ae;
        this.requestor = ae != null;
        this.name = sock.getLocalSocketAddress() + delim() + sock.getRemoteSocketAddress() + Symbol.C_PARENTHESE_LEFT
                + serialNo + Symbol.C_PARENTHESE_RIGHT;
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

    /**
     * Executes the min zero as max operation.
     *
     * @param i1 the i1.
     * @param i2 the i2.
     * @return the operation result.
     */
    static int minZeroAsMax(int i1, int i2) {
        return i1 == 0 ? i2 : i2 == 0 ? i1 : Math.min(i1, i2);
    }

    /**
     * Gets the connect time in millis.
     *
     * @return the connect time in millis.
     */
    public long getConnectTimeInMillis() {
        return connectTime;
    }

    /**
     * Gets the serial no.
     *
     * @return the serial no.
     */
    public int getSerialNo() {
        return serialNo;
    }

    /**
     * Gets the device.
     *
     * @return the device.
     */
    public Device getDevice() {
        return device;
    }

    /**
     * Executes the next message id operation.
     *
     * @return the operation result.
     */
    public int nextMessageID() {
        return messageID.incrementAndGet() & 0xFFFF;
    }

    /**
     * Executes the delim operation.
     *
     * @return the operation result.
     */
    private String delim() {
        return requestor ? "->" : "<-";
    }

    /**
     * Gets the number of sent.
     *
     * @param dimse the dimse.
     * @return the number of sent.
     */
    public int getNumberOfSent(Dimse dimse) {
        return dimseCounters.get(dimse.ordinal());
    }

    /**
     * Gets the number of received.
     *
     * @param dimse the dimse.
     * @return the number of received.
     */
    public int getNumberOfReceived(Dimse dimse) {
        return dimseCounters.get(23 + dimse.ordinal());
    }

    /**
     * Executes the inc sent count operation.
     *
     * @param dimse the dimse.
     */
    public void incSentCount(Dimse dimse) {
        dimseCounters.getAndIncrement(dimse.ordinal());
    }

    /**
     * Executes the inc received count operation.
     *
     * @param dimse the dimse.
     */
    void incReceivedCount(Dimse dimse) {
        dimseCounters.getAndIncrement(23 + dimse.ordinal());
    }

    /**
     * Returns the string representation.
     *
     * @return the string representation.
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Gets the socket.
     *
     * @return the socket.
     */
    public final Socket getSocket() {
        return sock;
    }

    /**
     * Gets the local host name.
     *
     * @return the local host name.
     */
    public String getLocalHostName() {
        return ReverseDNS.hostNameOf(sock.getLocalAddress());
    }

    /**
     * Gets the remote host name.
     *
     * @return the remote host name.
     */
    public String getRemoteHostName() {
        return ReverseDNS.hostNameOf(sock.getInetAddress());
    }

    /**
     * Gets the connection.
     *
     * @return the connection.
     */
    public final Connection getConnection() {
        return conn;
    }

    /**
     * Gets the a associate rq.
     *
     * @return the a associate rq.
     */
    public final AAssociateRQ getAAssociateRQ() {
        return rq;
    }

    /**
     * Gets the a associate ac.
     *
     * @return the a associate ac.
     */
    public final AAssociateAC getAAssociateAC() {
        return ac;
    }

    /**
     * Gets the exception.
     *
     * @return the exception.
     */
    public final IOException getException() {
        return ex;
    }

    /**
     * Gets the application entity.
     *
     * @return the application entity.
     */
    public final ApplicationEntity getApplicationEntity() {
        return ae;
    }

    /**
     * Gets the property names.
     *
     * @return the property names.
     */
    public Set<String> getPropertyNames() {
        return properties != null ? properties.keySet() : Collections.emptySet();
    }

    /**
     * Gets the property.
     *
     * @param key the key.
     * @return the property.
     */
    public Object getProperty(String key) {
        return properties != null ? properties.get(key) : null;
    }

    /**
     * Gets the property.
     *
     * @param <T>   the property type
     * @param clazz the clazz.
     * @return the property.
     */
    public <T> T getProperty(Class<T> clazz) {
        return (T) getProperty(clazz.getName());
    }

    /**
     * Sets the property.
     *
     * @param <T>   the property type
     * @param clazz the clazz.
     * @param value the value.
     */
    public <T> void setProperty(Class<T> clazz, Object value) {
        setProperty(clazz.getName(), value);
    }

    /**
     * Determines whether property.
     *
     * @param key the key.
     * @return true if the condition is met; otherwise false.
     */
    public boolean containsProperty(String key) {
        return properties != null && properties.containsKey(key);
    }

    /**
     * Sets the property.
     *
     * @param key   the key.
     * @param value the value.
     * @return the operation result.
     */
    public Object setProperty(String key, Object value) {
        if (properties == null)
            properties = new HashMap<>();
        return properties.put(key, value);
    }

    /**
     * Executes the clear property operation.
     *
     * @param key the key.
     * @return the operation result.
     */
    public Object clearProperty(String key) {
        return properties != null ? properties.remove(key) : null;
    }

    /**
     * Adds the association listener.
     *
     * @param listener the listener.
     */
    public void addAssociationListener(AssociationListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes the association listener.
     *
     * @param listener the listener.
     */
    public void removeAssociationListener(AssociationListener listener) {
        listeners.remove(listener);
    }

    /**
     * Determines whether requestor.
     *
     * @return true if the condition is met; otherwise false.
     */
    public final boolean isRequestor() {
        return requestor;
    }

    /**
     * Determines whether ready for data transfer.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isReadyForDataTransfer() {
        return state == State.Sta6;
    }

    /**
     * Executes the check is scp operation.
     *
     * @param cuid the cuid.
     * @throws NoRolesException if the operation cannot be completed.
     */
    private void checkIsSCP(String cuid) throws NoRolesException {
        if (!isSCPFor(cuid)) {
            NoRolesException ex = new NoRolesException(cuid, TransferCapability.Role.SCP);
            if (ae.isRoleSelectionNegotiationLenient() && ac.getRoleSelectionFor(cuid) == null)
                Logger.warn(
                        false,
                        "Image",
                        ex,
                        "Role selection check failed: protocol=dimse, association={}, role={}, exception={}",
                        name,
                        "SCP",
                        ex.getClass().getSimpleName());
            else
                throw ex;
        }
    }

    /**
     * Determines whether scp for.
     *
     * @param cuid the cuid.
     * @return true if the condition is met; otherwise false.
     */
    public boolean isSCPFor(String cuid) {
        RoleSelection rolsel = ac.getRoleSelectionFor(cuid);
        if (rolsel == null)
            return !requestor;
        return requestor ? rolsel.isSCP() : rolsel.isSCU();
    }

    /**
     * Executes the check is scu operation.
     *
     * @param cuid the cuid.
     * @throws NoRolesException if the operation cannot be completed.
     */
    private void checkIsSCU(String cuid) throws NoRolesException {
        if (!isSCUFor(cuid)) {
            NoRolesException ex = new NoRolesException(cuid, TransferCapability.Role.SCU);
            if (ae.isRoleSelectionNegotiationLenient() && ac.getRoleSelectionFor(cuid) == null)
                Logger.warn(
                        false,
                        "Image",
                        ex,
                        "Role selection check failed: protocol=dimse, association={}, role={}, exception={}",
                        name,
                        "SCU",
                        ex.getClass().getSimpleName());
            else
                throw ex;
        }
    }

    /**
     * Determines whether scu for.
     *
     * @param cuid the cuid.
     * @return true if the condition is met; otherwise false.
     */
    public boolean isSCUFor(String cuid) {
        RoleSelection rolsel = ac.getRoleSelectionFor(cuid);
        if (rolsel == null)
            return requestor;
        return requestor ? rolsel.isSCU() : rolsel.isSCP();
    }

    /**
     * Gets the calling aet.
     *
     * @return the calling aet.
     */
    public String getCallingAET() {
        return rq != null ? rq.getCallingAET() : null;
    }

    /**
     * Gets the called aet.
     *
     * @return the called aet.
     */
    public String getCalledAET() {
        return rq != null ? rq.getCalledAET() : null;
    }

    /**
     * Gets the remote aet.
     *
     * @return the remote aet.
     */
    public String getRemoteAET() {
        return requestor ? getCalledAET() : getCallingAET();
    }

    /**
     * Gets the local aet.
     *
     * @return the local aet.
     */
    public String getLocalAET() {
        return requestor ? getCallingAET() : getCalledAET();
    }

    /**
     * Gets the remote impl version name.
     *
     * @return the remote impl version name.
     */
    public String getRemoteImplVersionName() {
        return (requestor ? ac : rq).getImplVersionName();
    }

    /**
     * Gets the remote impl class uid.
     *
     * @return the remote impl class uid.
     */
    public String getRemoteImplClassUID() {
        return (requestor ? ac : rq).getImplClassUID();
    }

    /**
     * Gets the local impl version name.
     *
     * @return the local impl version name.
     */
    public String getLocalImplVersionName() {
        return (requestor ? rq : ac).getImplVersionName();
    }

    /**
     * Gets the local impl class uid.
     *
     * @return the local impl class uid.
     */
    public String getLocalImplClassUID() {
        return (requestor ? rq : ac).getImplClassUID();
    }

    /**
     * Gets the abstract syntax.
     *
     * @param pcid the pcid.
     * @return the abstract syntax.
     */
    public String getAbstractSyntax(int pcid) {
        PresentationContext rqpc = rq.getPresentationContext(pcid);
        return rqpc != null ? rqpc.getAbstractSyntax() : null;
    }

    /**
     * Gets the max pdu length send.
     *
     * @return the max pdu length send.
     */
    public final int getMaxPDULengthSend() {
        return maxPDULength;
    }

    /**
     * Determines whether pack pdv.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isPackPDV() {
        return conn.isPackPDV();
    }

    /**
     * Executes the release operation.
     *
     * @throws IOException if the operation cannot be completed.
     */
    public void release() throws IOException {
        state.writeAReleaseRQ(this);
    }

    /**
     * Executes the abort operation.
     */
    public void abort() {
        abort(new AAbort());
    }

    /**
     * Executes the abort operation.
     *
     * @param aa the aa.
     */
    void abort(AAbort aa) {
        state.write(this, aa);
    }

    /**
     * Closes the socket.
     */
    private synchronized void closeSocket() {
        state.closeSocket(this);
    }

    /**
     * Executes the do close socket operation.
     */
    public void doCloseSocket() {
        Logger.info(false, "Image", "{}: close {}", name, sock);
        IoKit.close(sock);
        enterState(State.Sta1);
    }

    /**
     * Closes the socket delayed.
     */
    synchronized private void closeSocketDelayed() {
        state.closeSocketDelayed(this);
    }

    /**
     * Executes the do close socket delayed operation.
     */
    public void doCloseSocketDelayed() {
        enterState(State.Sta13);
        int delay = conn.getSocketCloseDelay();
        if (delay > 0) {
            device.schedule(() -> closeSocket(), delay, TimeUnit.MILLISECONDS);
            Logger.debug(false, "Image", "{}: closing {} in {} ms", name, sock, delay);
        } else
            closeSocket();
    }

    /**
     * Executes the on io exception operation.
     *
     * @param e the e.
     */
    public synchronized void onIOException(IOException e) {
        if (ex != null)
            return;

        ex = e;
        Logger.warn(
                false,
                "Image",
                e,
                "Association I/O failed: protocol=dimse, association={}, state={}, exception={}",
                name,
                state,
                e.getClass().getSimpleName());
        closeSocket();
    }

    /**
     * Executes the write operation.
     *
     * @param aa the aa.
     */
    public void write(AAbort aa) {
        Logger.info(false, "Image", "{} << {}", name, aa.toString());
        encoder.write(aa);
        ex = aa;
        closeSocketDelayed();
    }

    /**
     * Writes the a release rq.
     *
     * @throws IOException if the operation cannot be completed.
     */
    public void writeAReleaseRQ() throws IOException {
        Logger.info(false, "Image", "{} << A-RELEASE-RQ", name);
        enterState(State.Sta7);
        stopTimeout();
        encoder.writeAReleaseRQ();
        startReleaseTimeout();
    }

    /**
     * Executes the start request timeout operation.
     */
    private void startRequestTimeout() {
        startTimeout(
                "{}: start A-ASSOCIATE-RQ timeout of {}ms",
                "{}: A-ASSOCIATE-RQ timeout expired",
                "{}: stop A-ASSOCIATE-RQ timeout",
                conn.getRequestTimeout(),
                State.Sta2);
    }

    /**
     * Executes the start accept timeout operation.
     */
    private void startAcceptTimeout() {
        startTimeout(
                "{}: start A-ASSOCIATE-AC timeout of {}ms",
                "{}: A-ASSOCIATE-AC timeout expired",
                "{}: stop A-ASSOCIATE-AC timeout",
                conn.getAcceptTimeout(),
                State.Sta5);
    }

    /**
     * Executes the start release timeout operation.
     */
    private void startReleaseTimeout() {
        startTimeout(
                "{}: start A-RELEASE-RP timeout of {}ms",
                "{}: A-RELEASE-RP timeout expired",
                "{}: stop A-RELEASE-RP timeout",
                conn.getReleaseTimeout(),
                State.Sta7);
    }

    /**
     * Executes the start idle timeout operation.
     */
    private void startIdleTimeout() {
        startTimeout(
                "{}: start idle timeout of {}ms",
                "{}: idle timeout expired",
                "{}: stop idle timeout",
                conn.getIdleTimeout(),
                State.Sta6);
    }

    /**
     * Executes the start send timeout operation.
     *
     * @param timeout the timeout.
     */
    private void startSendTimeout(int timeout) {
        if (timeout > 0) {
            synchronized (this) {
                stopTimeout();
                this.timeout = Timeout.start(
                        this,
                        "{}: start send timeout of {}ms",
                        "{}: send timeout expired",
                        "{}: stop send timeout",
                        timeout);
            }
        }
    }

    /**
     * Executes the start timeout operation.
     *
     * @param startMsg   the start msg.
     * @param expiredMsg the expired msg.
     * @param cancelMsg  the cancel msg.
     * @param timeout    the timeout.
     * @param state      the state.
     */
    private void startTimeout(String startMsg, String expiredMsg, String cancelMsg, int timeout, State state) {
        if (timeout > 0 && performing == 0 && rspHandlerForMsgId.isEmpty()) {
            synchronized (this) {
                if (this.state == state) {
                    stopTimeout();
                    this.timeout = Timeout.start(this, startMsg, expiredMsg, cancelMsg, timeout);
                }
            }
        }
    }

    /**
     * Executes the start timeout operation.
     *
     * @param msgID         the msg id.
     * @param timeout       the timeout.
     * @param stopOnPending the stop on pending.
     */
    private void startTimeout(final int msgID, int timeout, boolean stopOnPending) {
        if (timeout > 0) {
            synchronized (rspHandlerForMsgId) {
                DimseRSPHandler rspHandler = rspHandlerForMsgId.get(msgID);
                if (rspHandler != null) {
                    rspHandler.setTimeout(
                            Timeout.start(
                                    this,
                                    "{}: start " + msgID + ":DIMSE-RSP timeout of {}ms",
                                    "{}: " + msgID + ":DIMSE-RSP timeout expired",
                                    "{}: stop " + msgID + ":DIMSE-RSP timeout",
                                    timeout),
                            stopOnPending);
                }
            }
        }
    }

    /**
     * Executes the stop timeout operation.
     */
    private synchronized void stopTimeout() {
        if (timeout != null) {
            timeout.stop();
            timeout = null;
        }
    }

    /**
     * Executes the wait for outstanding rsp operation.
     *
     * @throws InterruptedException if the operation cannot be completed.
     */
    public void waitForOutstandingRSP() throws InterruptedException {
        synchronized (rspHandlerForMsgId) {
            while (!rspHandlerForMsgId.isEmpty())
                rspHandlerForMsgId.wait();
        }
    }

    /**
     * Block if the number of outstanding DIMSE responses has reached the negotiated value for the maximum number of
     * outstanding operations it may invoke asynchronously.
     *
     * @throws InterruptedException if any thread interrupted the current thread before or while the current thread was
     *                              waiting
     */
    public void waitForNonBlockingInvoke() throws InterruptedException {
        if (maxOpsInvoked > 0)
            synchronized (rspHandlerForMsgId) {
                while (rspHandlerForMsgId.size() >= maxOpsInvoked)
                    rspHandlerForMsgId.wait();
            }
    }

    /**
     * Executes the write operation.
     *
     * @param rq the rq.
     * @throws IOException if the operation cannot be completed.
     */
    public void write(AAssociateRQ rq) throws IOException {
        name = rq.getCallingAET() + delim() + rq.getCalledAET() + '(' + serialNo + ')';
        this.rq = rq;
        Logger.info(false, "Image", "{} << A-ASSOCIATE-RQ", name);
        Logger.debug(false, "Image", "{}", rq);
        enterState(State.Sta5);
        encoder.write(rq);
        startAcceptTimeout();
    }

    /**
     * Executes the write operation.
     *
     * @param ac the ac.
     * @throws IOException if the operation cannot be completed.
     */
    private void write(AAssociateAC ac) throws IOException {
        Logger.info(false, "Image", "{} << A-ASSOCIATE-AC", name);
        Logger.debug(false, "Image", "{}", ac);
        enterState(State.Sta6);
        encoder.write(ac);
        startIdleTimeout();
    }

    /**
     * Executes the write operation.
     *
     * @param rj the rj.
     * @throws IOException if the operation cannot be completed.
     */
    private void write(AAssociateRJ rj) throws IOException {
        Logger.info(false, "Image", "{} << {}", name, rj.toString());
        encoder.write(rj);
        closeSocketDelayed();
    }

    /**
     * Executes the check exception operation.
     *
     * @throws IOException if the operation cannot be completed.
     */
    private void checkException() throws IOException {
        if (ex != null)
            throw ex;
    }

    /**
     * Executes the enter state operation.
     *
     * @param newState the new state.
     */
    private synchronized void enterState(State newState) {
        Logger.debug(
                false,
                "Image",
                "Association state changed: protocol=dimse, association={}, state={}",
                name,
                newState);
        this.state = newState;
        notifyAll();
    }

    /**
     * Gets the state.
     *
     * @return the state.
     */
    public final State getState() {
        return state;
    }

    /**
     * Executes the wait for leaving operation.
     *
     * @param state the state.
     * @throws InterruptedException if the operation cannot be completed.
     * @throws IOException          if the operation cannot be completed.
     */
    public synchronized void waitForLeaving(State state) throws InterruptedException, IOException {
        while (this.state == state)
            wait();
        checkException();
    }

    /**
     * Executes the wait for entering operation.
     *
     * @param state the state.
     * @throws InterruptedException if the operation cannot be completed.
     * @throws IOException          if the operation cannot be completed.
     */
    synchronized void waitForEntering(State state) throws InterruptedException, IOException {
        while (this.state != state)
            wait();
        checkException();
    }

    /**
     * Executes the wait for socket close operation.
     *
     * @throws InterruptedException if the operation cannot be completed.
     * @throws IOException          if the operation cannot be completed.
     */
    public void waitForSocketClose() throws InterruptedException, IOException {
        waitForEntering(State.Sta1);
    }

    /**
     * Executes the activate operation.
     */
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

    /**
     * Executes the on close operation.
     */
    private void onClose() {
        stopTimeout();
        synchronized (rspHandlerForMsgId) {
            IntHashMap.Visitor<DimseRSPHandler> visitor = (key, value) -> {
                value.onClose(Association.this);
                return true;
            };
            rspHandlerForMsgId.accept(visitor);
            rspHandlerForMsgId.clear();
            rspHandlerForMsgId.notifyAll();
        }
        if (ae != null)
            ae.getDevice().getAssociationHandler().onClose(this);
        for (AssociationListener listener : listeners)
            listener.onClose(this);
    }

    /**
     * Executes the on a associate rq operation.
     *
     * @param rq the rq.
     * @throws IOException if the operation cannot be completed.
     */
    public void onAAssociateRQ(AAssociateRQ rq) throws IOException {
        name = rq.getCalledAET() + delim() + rq.getCallingAET() + '(' + serialNo + ')';
        Logger.info(false, "Image", "{} >> A-ASSOCIATE-RQ", name);
        Logger.debug(false, "Image", "{}", rq);
        stopTimeout();
        state.onAAssociateRQ(this, rq);
    }

    /**
     * Executes the handle operation.
     *
     * @param rq the rq.
     * @throws IOException if the operation cannot be completed.
     */
    public void handle(AAssociateRQ rq) throws IOException {
        this.rq = rq;
        enterState(State.Sta3);
        try {
            ae = device.getApplicationEntity(rq.getCalledAET(), true);
            ac = device.getAssociationHandler().negotiate(this, rq);
            initPCMap();
            maxOpsInvoked = ac.getMaxOpsPerformed();
            maxPDULength = Association.minZeroAsMax(rq.getMaxPDULength(), conn.getSendPDULength());
            write(ac);
            if (monitor != null)
                monitor.onAssociationAccepted(this);
        } catch (AAssociateRJ e) {
            write(e);
            if (monitor != null)
                monitor.onAssociationRejected(this, e);
        }
    }

    /**
     * Executes the on a associate ac operation.
     *
     * @param ac the ac.
     * @throws IOException if the operation cannot be completed.
     */
    public void onAAssociateAC(AAssociateAC ac) throws IOException {
        Logger.info(false, "Image", "{} >> A-ASSOCIATE-AC", name);
        Logger.debug(false, "Image", "{}", ac);
        stopTimeout();
        state.onAAssociateAC(this, ac);
    }

    /**
     * Executes the handle operation.
     *
     * @param ac the ac.
     */
    public void handle(AAssociateAC ac) {
        this.ac = ac;
        initPCMap();
        maxOpsInvoked = ac.getMaxOpsInvoked();
        maxPDULength = Association.minZeroAsMax(ac.getMaxPDULength(), conn.getSendPDULength());
        enterState(State.Sta6);
        startIdleTimeout();
    }

    /**
     * Executes the on a associate rj operation.
     *
     * @param rj the rj.
     * @throws IOException if the operation cannot be completed.
     */
    public void onAAssociateRJ(AAssociateRJ rj) throws IOException {
        Logger.info(false, "Image", "{} >> {}", name, rj.toString());
        state.onAAssociateRJ(this, rj);
    }

    /**
     * Executes the handle operation.
     *
     * @param rq the rq.
     */
    public void handle(AAssociateRJ rq) {
        ex = rq;
        closeSocket();
    }

    /**
     * Executes the on a release rq operation.
     *
     * @throws IOException if the operation cannot be completed.
     */
    public void onAReleaseRQ() throws IOException {
        Logger.info(false, "Image", "{} >> A-RELEASE-RQ", name);
        stopTimeout();
        state.onAReleaseRQ(this);
    }

    /**
     * Executes the handle a release rq operation.
     */
    public void handleAReleaseRQ() {
        if (decoder.isPendingPDV()) {
            Logger.info(false, "Image", "{}: unexpected A-RELEASE-RQ after P-DATA-TF with pending PDV", this);
            abort();
            return;
        }
        enterState(State.Sta8);
        waitForPerformingOps();
        Logger.info(false, "Image", "{} << A-RELEASE-RP", name);
        encoder.writeAReleaseRP();
        closeSocketDelayed();
    }

    /**
     * Executes the wait for performing ops operation.
     */
    private synchronized void waitForPerformingOps() {
        while (performing > 0 && state == State.Sta8) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Executes the handle a release rq collision operation.
     */
    public void handleAReleaseRQCollision() {
        if (isRequestor()) {
            enterState(State.Sta9);
            Logger.info(false, "Image", "{} << A-RELEASE-RP", name);
            encoder.writeAReleaseRP();
            enterState(State.Sta11);
        } else {
            enterState(State.Sta10);
        }
    }

    /**
     * Executes the on a release rp operation.
     *
     * @throws IOException if the operation cannot be completed.
     */
    public void onAReleaseRP() throws IOException {
        Logger.info(false, "Image", "{} >> A-RELEASE-RP", name);
        stopTimeout();
        state.onAReleaseRP(this);
    }

    /**
     * Executes the handle a release rp operation.
     */
    public void handleAReleaseRP() {
        closeSocket();
    }

    /**
     * Executes the handle a release rp collision operation.
     */
    public void handleAReleaseRPCollision() {
        enterState(State.Sta12);
        Logger.info(false, "Image", "{} << A-RELEASE-RP", name);
        encoder.writeAReleaseRP();
        closeSocketDelayed();
    }

    /**
     * Executes the on a abort operation.
     *
     * @param aa the aa.
     */
    public void onAAbort(AAbort aa) {
        Logger.info(false, "Image", "{} >> {}", name, aa.toString());
        stopTimeout();
        ex = aa;
        closeSocket();
    }

    /**
     * Executes the unexpected pdu operation.
     *
     * @param pdu the pdu.
     * @throws AAbort if the operation cannot be completed.
     */
    public void unexpectedPDU(String pdu) throws AAbort {
        Logger.warn(
                false,
                "Image",
                "Unexpected PDU received: protocol=dimse, association={}, pduType={}, state={}",
                name,
                pdu,
                state);
        throw new AAbort(AAbort.UL_SERIVE_PROVIDER, AAbort.UNEXPECTED_PDU);
    }

    /**
     * Executes the on p data tf operation.
     *
     * @throws IOException if the operation cannot be completed.
     */
    public void onPDataTF() throws IOException {
        state.onPDataTF(this);
    }

    /**
     * Executes the handle p data tf operation.
     *
     * @throws IOException if the operation cannot be completed.
     */
    public void handlePDataTF() throws IOException {
        decoder.decodeDIMSE();
    }

    /**
     * Writes the p data tf.
     *
     * @throws IOException if the operation cannot be completed.
     */
    public void writePDataTF() throws IOException {
        checkException();
        state.writePDataTF(this);
    }

    /**
     * Executes the do write p data tf operation.
     *
     * @throws IOException if the operation cannot be completed.
     */
    public void doWritePDataTF() throws IOException {
        encoder.writePDataTF();
    }

    /**
     * Executes the on dimse rq operation.
     *
     * @param pc    the pc.
     * @param dimse the dimse.
     * @param cmd   the cmd.
     * @param data  the data.
     * @throws IOException if the operation cannot be completed.
     */
    public void onDimseRQ(PresentationContext pc, Dimse dimse, Attributes cmd, PDVInputStream data) throws IOException {
        stopTimeout();
        incPerforming();
        incReceivedCount(dimse);
        ae.onDimseRQ(this, pc, dimse, cmd, data);
    }

    /**
     * Executes the inc performing operation.
     */
    private synchronized void incPerforming() {
        ++performing;
    }

    /**
     * Executes the dec performing operation.
     */
    private synchronized void decPerforming() {
        --performing;
        notifyAll();
    }

    /**
     * Executes the on dimse rsp operation.
     *
     * @param dimse the dimse.
     * @param cmd   the cmd.
     * @param data  the data.
     * @throws AAbort if the operation cannot be completed.
     */
    public void onDimseRSP(Dimse dimse, Attributes cmd, Attributes data) throws AAbort {
        int msgId = cmd.getInt(Tag.MessageIDBeingRespondedTo, -1);
        int status = cmd.getInt(Tag.Status, 0);
        boolean pending = Status.isPending(status);
        DimseRSPHandler rspHandler = getDimseRSPHandler(msgId);
        if (rspHandler == null) {
            Logger.warn(
                    false,
                    "Image",
                    "Unexpected DIMSE response message id: protocol=dimse, association={}, messageId={}, status={}, commandAttributes={}",
                    name,
                    msgId,
                    status,
                    cmd == null ? 0 : cmd.size());
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

    /**
     * Executes the start idle or release timeout operation.
     */
    private synchronized void startIdleOrReleaseTimeout() {
        if (state == State.Sta6)
            startIdleTimeout();
        else if (state == State.Sta7)
            startReleaseTimeout();
    }

    /**
     * Adds the dimse rsp handler.
     *
     * @param rspHandler the rsp handler.
     * @throws InterruptedException if the operation cannot be completed.
     */
    private void addDimseRSPHandler(DimseRSPHandler rspHandler) throws InterruptedException {
        synchronized (rspHandlerForMsgId) {
            while (maxOpsInvoked > 0 && rspHandlerForMsgId.size() >= maxOpsInvoked)
                rspHandlerForMsgId.wait();
            rspHandlerForMsgId.put(rspHandler.getMessageID(), rspHandler);
        }
    }

    /**
     * Gets the dimse rsp handler.
     *
     * @param msgId the msg id.
     * @return the dimse rsp handler.
     */
    private DimseRSPHandler getDimseRSPHandler(int msgId) {
        synchronized (rspHandlerForMsgId) {
            return rspHandlerForMsgId.get(msgId);
        }
    }

    /**
     * Removes the dimse rsp handler.
     *
     * @param msgId the msg id.
     * @return the operation result.
     */
    private DimseRSPHandler removeDimseRSPHandler(int msgId) {
        synchronized (rspHandlerForMsgId) {
            DimseRSPHandler tmp = rspHandlerForMsgId.remove(msgId);
            if (tmp != null) {
                tmp.stopTimeout(this);
            }
            rspHandlerForMsgId.notifyAll();
            return tmp;
        }
    }

    /**
     * Determines whether cel.
     *
     * @param pc    the pc.
     * @param msgId the msg id.
     * @throws IOException if the operation cannot be completed.
     */
    void cancel(PresentationContext pc, int msgId) throws IOException {
        Attributes cmd = Commands.mkCCancelRQ(msgId);
        encoder.writeDIMSE(pc, cmd, null);
    }

    /**
     * Executes the try write dimse rsp operation.
     *
     * @param pc  the pc.
     * @param cmd the cmd.
     * @return true if the condition is met; otherwise false.
     */
    public boolean tryWriteDimseRSP(PresentationContext pc, Attributes cmd) {
        return tryWriteDimseRSP(pc, cmd, null);
    }

    /**
     * Executes the try write dimse rsp operation.
     *
     * @param pc   the pc.
     * @param cmd  the cmd.
     * @param data the data.
     * @return true if the condition is met; otherwise false.
     */
    public boolean tryWriteDimseRSP(PresentationContext pc, Attributes cmd, Attributes data) {
        try {
            writeDimseRSP(pc, cmd, data);
            return true;
        } catch (IOException e) {
            Logger.warn(
                    false,
                    "Image",
                    e,
                    "DIMSE response write failed: protocol=dimse, association={}, dimse={}, exception={}",
                    this,
                    Dimse.valueOf(cmd.getInt(Tag.CommandField, 0)),
                    e.getClass().getSimpleName());
            return false;
        }
    }

    /**
     * Writes the dimse rsp.
     *
     * @param pc  the pc.
     * @param cmd the cmd.
     * @throws IOException if the operation cannot be completed.
     */
    public void writeDimseRSP(PresentationContext pc, Attributes cmd) throws IOException {
        writeDimseRSP(pc, cmd, null);
    }

    /**
     * Writes the dimse rsp.
     *
     * @param pc   the pc.
     * @param cmd  the cmd.
     * @param data the data.
     * @throws IOException if the operation cannot be completed.
     */
    public void writeDimseRSP(PresentationContext pc, Attributes cmd, Attributes data) throws IOException {
        DataWriter writer = null;
        int datasetType = Commands.NO_DATASET;
        if (data != null) {
            writer = new DataWriterAdapter(data);
            datasetType = Commands.getWithDatasetType();
        }
        cmd.setInt(Tag.CommandDataSetType, VR.US, datasetType);
        try {
            encoder.writeDIMSE(pc, cmd, writer);
        } finally {
            if (!Status.isPending(cmd.getInt(Tag.Status, 0))) {
                decPerforming();
                startIdleTimeout();
            }
        }
    }

    /**
     * Executes the on cancel rq operation.
     *
     * @param cmd the cmd.
     * @throws IOException if the operation cannot be completed.
     */
    public void onCancelRQ(Attributes cmd) throws IOException {
        incReceivedCount(Dimse.C_CANCEL_RQ);
        int msgId = cmd.getInt(Tag.MessageIDBeingRespondedTo, -1);
        CancelRQHandler handler = removeCancelRQHandler(msgId);
        if (handler != null)
            handler.onCancelRQ(this);
    }

    /**
     * Adds the cancel rq handler.
     *
     * @param msgId   the msg id.
     * @param handler the handler.
     */
    public void addCancelRQHandler(int msgId, CancelRQHandler handler) {
        synchronized (cancelHandlerForMsgId) {
            cancelHandlerForMsgId.put(msgId, handler);
        }
    }

    /**
     * Removes the cancel rq handler.
     *
     * @param msgId the msg id.
     * @return the operation result.
     */
    public CancelRQHandler removeCancelRQHandler(int msgId) {
        synchronized (cancelHandlerForMsgId) {
            return cancelHandlerForMsgId.remove(msgId);
        }
    }

    /**
     * Executes the init pc map operation.
     */
    private void initPCMap() {
        for (PresentationContext pc : ac.getPresentationContexts())
            if (pc.isAccepted()) {
                PresentationContext rqpc = rq.getPresentationContext(pc.getPCID());
                if (rqpc != null)
                    initTSMap(rqpc.getAbstractSyntax()).put(pc.getTransferSyntax(), pc);
                else
                    Logger.info(false, "Image", "{}: Ignore unexpected {} in A-ASSOCIATE-AC", name, pc);
            }
    }

    /**
     * Executes the init ts map operation.
     *
     * @param as the as.
     * @return the operation result.
     */
    private HashMap<String, PresentationContext> initTSMap(String as) {
        HashMap<String, PresentationContext> tsMap = pcMap.get(as);
        if (tsMap == null)
            pcMap.put(as, tsMap = new HashMap<>());
        return tsMap;
    }

    /**
     * Executes the pc for operation.
     *
     * @param cuid  the cuid.
     * @param tsuid the tsuid.
     * @return the operation result.
     * @throws NoPresentationException if the operation cannot be completed.
     */
    public PresentationContext pcFor(String cuid, String tsuid) throws NoPresentationException {
        HashMap<String, PresentationContext> tsMap = pcMap.get(cuid);
        if (tsMap == null)
            throw new NoPresentationException(cuid);
        if (tsuid == null)
            return tsMap.values().iterator().next();
        PresentationContext pc = tsMap.get(tsuid);
        if (pc == null)
            throw new NoPresentationException(cuid, tsuid);
        return pc;
    }

    /**
     * Gets the transfer syntaxes for.
     *
     * @param cuid the cuid.
     * @return the transfer syntaxes for.
     */
    public Set<String> getTransferSyntaxesFor(String cuid) {
        HashMap<String, PresentationContext> tsMap = pcMap.get(cuid);
        if (tsMap == null)
            return Collections.emptySet();
        return Collections.unmodifiableSet(tsMap.keySet());
    }

    /**
     * Gets the presentation context.
     *
     * @param pcid the pcid.
     * @return the presentation context.
     */
    public PresentationContext getPresentationContext(int pcid) {
        return ac.getPresentationContext(pcid);
    }

    /**
     * Gets the common extended negotiation for.
     *
     * @param cuid the cuid.
     * @return the common extended negotiation for.
     */
    public CommonExtended getCommonExtendedNegotiationFor(String cuid) {
        return ac.getCommonExtendedNegotiationFor(cuid);
    }

    /**
     * Executes the cstore operation.
     *
     * @param cuid       the cuid.
     * @param iuid       the iuid.
     * @param priority   the priority.
     * @param data       the data.
     * @param tsuid      the tsuid.
     * @param rspHandler the rsp handler.
     * @throws IOException          if the operation cannot be completed.
     * @throws InterruptedException if the operation cannot be completed.
     */
    public void cstore(
            String cuid,
            String iuid,
            int priority,
            DataWriter data,
            String tsuid,
            DimseRSPHandler rspHandler) throws IOException, InterruptedException {
        PresentationContext pc = pcFor(cuid, tsuid);
        checkIsSCU(cuid);
        Attributes cstorerq = Commands.mkCStoreRQ(rspHandler.getMessageID(), cuid, iuid, priority);
        invoke(pc, cstorerq, data, rspHandler, conn.getStoreTimeout(), conn.getResponseTimeout());
    }

    /**
     * Executes the cstore operation.
     *
     * @param cuid     the cuid.
     * @param iuid     the iuid.
     * @param priority the priority.
     * @param data     the data.
     * @param tsuid    the tsuid.
     * @return the operation result.
     * @throws IOException          if the operation cannot be completed.
     * @throws InterruptedException if the operation cannot be completed.
     */
    public DimseRSP cstore(String cuid, String iuid, int priority, DataWriter data, String tsuid)
            throws IOException, InterruptedException {
        FutureDimseRSP rsp = new FutureDimseRSP(nextMessageID());
        cstore(cuid, iuid, priority, data, tsuid, rsp);
        return rsp;
    }

    /**
     * Executes the cstore operation.
     *
     * @param cuid                the cuid.
     * @param iuid                the iuid.
     * @param priority            the priority.
     * @param moveOriginatorAET   the move originator aet.
     * @param moveOriginatorMsgId the move originator msg id.
     * @param data                the data.
     * @param tsuid               the tsuid.
     * @param rspHandler          the rsp handler.
     * @throws IOException          if the operation cannot be completed.
     * @throws InterruptedException if the operation cannot be completed.
     */
    public void cstore(
            String cuid,
            String iuid,
            int priority,
            String moveOriginatorAET,
            int moveOriginatorMsgId,
            DataWriter data,
            String tsuid,
            DimseRSPHandler rspHandler) throws IOException, InterruptedException {
        PresentationContext pc = pcFor(cuid, tsuid);
        Attributes cstorerq = Commands
                .mkCStoreRQ(rspHandler.getMessageID(), cuid, iuid, priority, moveOriginatorAET, moveOriginatorMsgId);
        invoke(pc, cstorerq, data, rspHandler, conn.getStoreTimeout(), conn.getResponseTimeout());
    }

    /**
     * Executes the cstore operation.
     *
     * @param cuid                the cuid.
     * @param iuid                the iuid.
     * @param priority            the priority.
     * @param moveOriginatorAET   the move originator aet.
     * @param moveOriginatorMsgId the move originator msg id.
     * @param data                the data.
     * @param tsuid               the tsuid.
     * @return the operation result.
     * @throws IOException          if the operation cannot be completed.
     * @throws InterruptedException if the operation cannot be completed.
     */
    public DimseRSP cstore(
            String cuid,
            String iuid,
            int priority,
            String moveOriginatorAET,
            int moveOriginatorMsgId,
            DataWriter data,
            String tsuid) throws IOException, InterruptedException {
        FutureDimseRSP rsp = new FutureDimseRSP(nextMessageID());
        cstore(cuid, iuid, priority, moveOriginatorAET, moveOriginatorMsgId, data, tsuid, rsp);
        return rsp;
    }

    /**
     * Executes the cfind operation.
     *
     * @param cuid       the cuid.
     * @param priority   the priority.
     * @param data       the data.
     * @param tsuid      the tsuid.
     * @param rspHandler the rsp handler.
     * @throws IOException          if the operation cannot be completed.
     * @throws InterruptedException if the operation cannot be completed.
     */
    public void cfind(String cuid, int priority, Attributes data, String tsuid, DimseRSPHandler rspHandler)
            throws IOException, InterruptedException {
        PresentationContext pc = pcFor(cuid, tsuid);
        checkIsSCU(cuid);
        Attributes cfindrq = Commands.mkCFindRQ(rspHandler.getMessageID(), cuid, priority);
        invoke(pc, cfindrq, new DataWriterAdapter(data), rspHandler, conn.getSendTimeout(), conn.getResponseTimeout());
    }

    /**
     * Executes the cfind operation.
     *
     * @param cuid       the cuid.
     * @param priority   the priority.
     * @param data       the data.
     * @param tsuid      the tsuid.
     * @param autoCancel the auto cancel.
     * @return the operation result.
     * @throws IOException          if the operation cannot be completed.
     * @throws InterruptedException if the operation cannot be completed.
     */
    public DimseRSP cfind(String cuid, int priority, Attributes data, String tsuid, int autoCancel)
            throws IOException, InterruptedException {
        return cfind(cuid, priority, data, tsuid, autoCancel, Integer.MAX_VALUE);
    }

    /**
     * Returns the related value. Returns the related value. Removes the related value.
     *
     * @param cuid       the cuid.
     * @param priority   the priority.
     * @param data       the data.
     * @param tsuid      the tsuid.
     * @param autoCancel the auto cancel.
     * @param capacity   the capacity.
     * @return the result.
     * @throws IOException          if the operation cannot be completed.
     * @throws InterruptedException if the operation cannot be completed.
     */
    public DimseRSP cfind(String cuid, int priority, Attributes data, String tsuid, int autoCancel, int capacity)
            throws IOException, InterruptedException {
        FutureDimseRSP rsp = new FutureDimseRSP(nextMessageID());
        rsp.setAutoCancel(autoCancel);
        rsp.setCapacity(capacity);
        cfind(cuid, priority, data, tsuid, rsp);
        return rsp;
    }

    /**
     * Executes the cget operation.
     *
     * @param cuid       the cuid.
     * @param priority   the priority.
     * @param data       the data.
     * @param tsuid      the tsuid.
     * @param rspHandler the rsp handler.
     * @throws IOException          if the operation cannot be completed.
     * @throws InterruptedException if the operation cannot be completed.
     */
    public void cget(String cuid, int priority, Attributes data, String tsuid, DimseRSPHandler rspHandler)
            throws IOException, InterruptedException {
        PresentationContext pc = pcFor(cuid, tsuid);
        checkIsSCU(cuid);
        Attributes cgetrq = Commands.mkCGetRQ(rspHandler.getMessageID(), cuid, priority);
        invoke(
                pc,
                cgetrq,
                new DataWriterAdapter(data),
                rspHandler,
                conn.getSendTimeout(),
                conn.getRetrieveTimeout(),
                !conn.isRetrieveTimeoutTotal());
    }

    /**
     * Executes the cget operation.
     *
     * @param cuid     the cuid.
     * @param priority the priority.
     * @param data     the data.
     * @param tsuid    the tsuid.
     * @return the operation result.
     * @throws IOException          if the operation cannot be completed.
     * @throws InterruptedException if the operation cannot be completed.
     */
    public DimseRSP cget(String cuid, int priority, Attributes data, String tsuid)
            throws IOException, InterruptedException {
        FutureDimseRSP rsp = new FutureDimseRSP(nextMessageID());
        cget(cuid, priority, data, tsuid, rsp);
        return rsp;
    }

    /**
     * Executes the cmove operation.
     *
     * @param cuid        the cuid.
     * @param priority    the priority.
     * @param data        the data.
     * @param tsuid       the tsuid.
     * @param destination the destination.
     * @param rspHandler  the rsp handler.
     * @throws IOException          if the operation cannot be completed.
     * @throws InterruptedException if the operation cannot be completed.
     */
    public void cmove(
            String cuid,
            int priority,
            Attributes data,
            String tsuid,
            String destination,
            DimseRSPHandler rspHandler) throws IOException, InterruptedException {
        PresentationContext pc = pcFor(cuid, tsuid);
        checkIsSCU(cuid);
        Attributes cmoverq = Commands.mkCMoveRQ(rspHandler.getMessageID(), cuid, priority, destination);
        invoke(
                pc,
                cmoverq,
                new DataWriterAdapter(data),
                rspHandler,
                conn.getSendTimeout(),
                conn.getRetrieveTimeout(),
                !conn.isRetrieveTimeoutTotal());
    }

    /**
     * Executes the cmove operation.
     *
     * @param cuid        the cuid.
     * @param priority    the priority.
     * @param data        the data.
     * @param tsuid       the tsuid.
     * @param destination the destination.
     * @return the operation result.
     * @throws IOException          if the operation cannot be completed.
     * @throws InterruptedException if the operation cannot be completed.
     */
    public DimseRSP cmove(String cuid, int priority, Attributes data, String tsuid, String destination)
            throws IOException, InterruptedException {
        FutureDimseRSP rsp = new FutureDimseRSP(nextMessageID());
        cmove(cuid, priority, data, tsuid, destination, rsp);
        return rsp;
    }

    /**
     * Executes the cecho operation.
     *
     * @return the operation result.
     * @throws IOException          if the operation cannot be completed.
     * @throws InterruptedException if the operation cannot be completed.
     */
    public DimseRSP cecho() throws IOException, InterruptedException {
        return cecho(UID.Verification.uid);
    }

    /**
     * Executes the cecho operation.
     *
     * @param cuid the cuid.
     * @return the operation result.
     * @throws IOException          if the operation cannot be completed.
     * @throws InterruptedException if the operation cannot be completed.
     */
    public DimseRSP cecho(String cuid) throws IOException, InterruptedException {
        FutureDimseRSP rsp = new FutureDimseRSP(nextMessageID());
        PresentationContext pc = pcFor(cuid, null);
        checkIsSCU(cuid);
        Attributes cechorq = Commands.mkCEchoRQ(rsp.getMessageID(), cuid);
        invoke(pc, cechorq, null, rsp, conn.getSendTimeout(), conn.getResponseTimeout());
        return rsp;
    }

    /**
     * Executes the nevent report operation.
     *
     * @param cuid        the cuid.
     * @param iuid        the iuid.
     * @param eventTypeId the event type id.
     * @param data        the data.
     * @param tsuid       the tsuid.
     * @param rspHandler  the rsp handler.
     * @throws IOException          if the operation cannot be completed.
     * @throws InterruptedException if the operation cannot be completed.
     */
    public void neventReport(
            String cuid,
            String iuid,
            int eventTypeId,
            Attributes data,
            String tsuid,
            DimseRSPHandler rspHandler) throws IOException, InterruptedException {
        neventReport(cuid, cuid, iuid, eventTypeId, data, tsuid, rspHandler);
    }

    /**
     * Executes the nevent report operation.
     *
     * @param asuid       the asuid.
     * @param cuid        the cuid.
     * @param iuid        the iuid.
     * @param eventTypeId the event type id.
     * @param data        the data.
     * @param tsuid       the tsuid.
     * @param rspHandler  the rsp handler.
     * @throws IOException          if the operation cannot be completed.
     * @throws InterruptedException if the operation cannot be completed.
     */
    public void neventReport(
            String asuid,
            String cuid,
            String iuid,
            int eventTypeId,
            Attributes data,
            String tsuid,
            DimseRSPHandler rspHandler) throws IOException, InterruptedException {
        PresentationContext pc = pcFor(asuid, tsuid);
        checkIsSCP(asuid);
        Attributes neventrq = Commands.mkNEventReportRQ(rspHandler.getMessageID(), cuid, iuid, eventTypeId, data);
        invoke(
                pc,
                neventrq,
                DataWriterAdapter.forAttributes(data),
                rspHandler,
                conn.getSendTimeout(),
                conn.getResponseTimeout());
    }

    /**
     * Executes the nevent report operation.
     *
     * @param cuid        the cuid.
     * @param iuid        the iuid.
     * @param eventTypeId the event type id.
     * @param data        the data.
     * @param tsuid       the tsuid.
     * @return the operation result.
     * @throws IOException          if the operation cannot be completed.
     * @throws InterruptedException if the operation cannot be completed.
     */
    public DimseRSP neventReport(String cuid, String iuid, int eventTypeId, Attributes data, String tsuid)
            throws IOException, InterruptedException {
        return neventReport(cuid, cuid, iuid, eventTypeId, data, tsuid);
    }

    /**
     * Executes the nevent report operation.
     *
     * @param asuid       the asuid.
     * @param cuid        the cuid.
     * @param iuid        the iuid.
     * @param eventTypeId the event type id.
     * @param data        the data.
     * @param tsuid       the tsuid.
     * @return the operation result.
     * @throws IOException          if the operation cannot be completed.
     * @throws InterruptedException if the operation cannot be completed.
     */
    public DimseRSP neventReport(String asuid, String cuid, String iuid, int eventTypeId, Attributes data, String tsuid)
            throws IOException, InterruptedException {
        FutureDimseRSP rsp = new FutureDimseRSP(nextMessageID());
        neventReport(asuid, cuid, iuid, eventTypeId, data, tsuid, rsp);
        return rsp;
    }

    /**
     * Executes the nget operation.
     *
     * @param cuid       the cuid.
     * @param iuid       the iuid.
     * @param tags       the tags.
     * @param rspHandler the rsp handler.
     * @throws IOException          if the operation cannot be completed.
     * @throws InterruptedException if the operation cannot be completed.
     */
    public void nget(String cuid, String iuid, int[] tags, DimseRSPHandler rspHandler)
            throws IOException, InterruptedException {
        nget(cuid, cuid, iuid, tags, rspHandler);
    }

    /**
     * Executes the nget operation.
     *
     * @param asuid      the asuid.
     * @param cuid       the cuid.
     * @param iuid       the iuid.
     * @param tags       the tags.
     * @param rspHandler the rsp handler.
     * @throws IOException          if the operation cannot be completed.
     * @throws InterruptedException if the operation cannot be completed.
     */
    public void nget(String asuid, String cuid, String iuid, int[] tags, DimseRSPHandler rspHandler)
            throws IOException, InterruptedException {
        PresentationContext pc = pcFor(asuid, null);
        checkIsSCU(asuid);
        Attributes ngetrq = Commands.mkNGetRQ(rspHandler.getMessageID(), cuid, iuid, tags);
        invoke(pc, ngetrq, null, rspHandler, conn.getSendTimeout(), conn.getResponseTimeout());
    }

    /**
     * Executes the nget operation.
     *
     * @param cuid the cuid.
     * @param iuid the iuid.
     * @param tags the tags.
     * @return the operation result.
     * @throws IOException          if the operation cannot be completed.
     * @throws InterruptedException if the operation cannot be completed.
     */
    public DimseRSP nget(String cuid, String iuid, int[] tags) throws IOException, InterruptedException {
        return nget(cuid, cuid, iuid, tags);
    }

    /**
     * Executes the nget operation.
     *
     * @param asuid the asuid.
     * @param cuid  the cuid.
     * @param iuid  the iuid.
     * @param tags  the tags.
     * @return the operation result.
     * @throws IOException          if the operation cannot be completed.
     * @throws InterruptedException if the operation cannot be completed.
     */
    public DimseRSP nget(String asuid, String cuid, String iuid, int[] tags) throws IOException, InterruptedException {
        FutureDimseRSP rsp = new FutureDimseRSP(nextMessageID());
        nget(asuid, cuid, iuid, tags, rsp);
        return rsp;
    }

    /**
     * Executes the nset operation.
     *
     * @param cuid       the cuid.
     * @param iuid       the iuid.
     * @param data       the data.
     * @param tsuid      the tsuid.
     * @param rspHandler the rsp handler.
     * @throws IOException          if the operation cannot be completed.
     * @throws InterruptedException if the operation cannot be completed.
     */
    public void nset(String cuid, String iuid, Attributes data, String tsuid, DimseRSPHandler rspHandler)
            throws IOException, InterruptedException {
        nset(cuid, cuid, iuid, new DataWriterAdapter(data), tsuid, rspHandler);
    }

    /**
     * Executes the nset operation.
     *
     * @param asuid      the asuid.
     * @param cuid       the cuid.
     * @param iuid       the iuid.
     * @param data       the data.
     * @param tsuid      the tsuid.
     * @param rspHandler the rsp handler.
     * @throws IOException          if the operation cannot be completed.
     * @throws InterruptedException if the operation cannot be completed.
     */
    public void nset(String asuid, String cuid, String iuid, Attributes data, String tsuid, DimseRSPHandler rspHandler)
            throws IOException, InterruptedException {
        nset(asuid, cuid, iuid, new DataWriterAdapter(data), tsuid, rspHandler);
    }

    /**
     * Executes the nset operation.
     *
     * @param cuid  the cuid.
     * @param iuid  the iuid.
     * @param data  the data.
     * @param tsuid the tsuid.
     * @return the operation result.
     * @throws IOException          if the operation cannot be completed.
     * @throws InterruptedException if the operation cannot be completed.
     */
    public DimseRSP nset(String cuid, String iuid, Attributes data, String tsuid)
            throws IOException, InterruptedException {
        return nset(cuid, cuid, iuid, new DataWriterAdapter(data), tsuid);
    }

    /**
     * Executes the nset operation.
     *
     * @param asuid the asuid.
     * @param cuid  the cuid.
     * @param iuid  the iuid.
     * @param data  the data.
     * @param tsuid the tsuid.
     * @return the operation result.
     * @throws IOException          if the operation cannot be completed.
     * @throws InterruptedException if the operation cannot be completed.
     */
    public DimseRSP nset(String asuid, String cuid, String iuid, Attributes data, String tsuid)
            throws IOException, InterruptedException {
        return nset(asuid, cuid, iuid, new DataWriterAdapter(data), tsuid);
    }

    /**
     * Executes the nset operation.
     *
     * @param cuid       the cuid.
     * @param iuid       the iuid.
     * @param data       the data.
     * @param tsuid      the tsuid.
     * @param rspHandler the rsp handler.
     * @throws IOException          if the operation cannot be completed.
     * @throws InterruptedException if the operation cannot be completed.
     */
    public void nset(String cuid, String iuid, DataWriter data, String tsuid, DimseRSPHandler rspHandler)
            throws IOException, InterruptedException {
        nset(cuid, cuid, iuid, data, tsuid, rspHandler);
    }

    /**
     * Executes the nset operation.
     *
     * @param asuid      the asuid.
     * @param cuid       the cuid.
     * @param iuid       the iuid.
     * @param data       the data.
     * @param tsuid      the tsuid.
     * @param rspHandler the rsp handler.
     * @throws IOException          if the operation cannot be completed.
     * @throws InterruptedException if the operation cannot be completed.
     */
    public void nset(String asuid, String cuid, String iuid, DataWriter data, String tsuid, DimseRSPHandler rspHandler)
            throws IOException, InterruptedException {
        PresentationContext pc = pcFor(asuid, tsuid);
        checkIsSCU(asuid);
        Attributes nsetrq = Commands.mkNSetRQ(rspHandler.getMessageID(), cuid, iuid);
        invoke(pc, nsetrq, data, rspHandler, conn.getSendTimeout(), conn.getResponseTimeout());
    }

    /**
     * Executes the nset operation.
     *
     * @param cuid  the cuid.
     * @param iuid  the iuid.
     * @param data  the data.
     * @param tsuid the tsuid.
     * @return the operation result.
     * @throws IOException          if the operation cannot be completed.
     * @throws InterruptedException if the operation cannot be completed.
     */
    public DimseRSP nset(String cuid, String iuid, DataWriter data, String tsuid)
            throws IOException, InterruptedException {
        return nset(cuid, cuid, iuid, data, tsuid);
    }

    /**
     * Executes the nset operation.
     *
     * @param asuid the asuid.
     * @param cuid  the cuid.
     * @param iuid  the iuid.
     * @param data  the data.
     * @param tsuid the tsuid.
     * @return the operation result.
     * @throws IOException          if the operation cannot be completed.
     * @throws InterruptedException if the operation cannot be completed.
     */
    public DimseRSP nset(String asuid, String cuid, String iuid, DataWriter data, String tsuid)
            throws IOException, InterruptedException {
        FutureDimseRSP rsp = new FutureDimseRSP(nextMessageID());
        nset(asuid, cuid, iuid, data, tsuid, rsp);
        return rsp;
    }

    /**
     * Executes the naction operation.
     *
     * @param cuid         the cuid.
     * @param iuid         the iuid.
     * @param actionTypeId the action type id.
     * @param data         the data.
     * @param tsuid        the tsuid.
     * @param rspHandler   the rsp handler.
     * @throws IOException          if the operation cannot be completed.
     * @throws InterruptedException if the operation cannot be completed.
     */
    public void naction(
            String cuid,
            String iuid,
            int actionTypeId,
            Attributes data,
            String tsuid,
            DimseRSPHandler rspHandler) throws IOException, InterruptedException {
        naction(cuid, cuid, iuid, actionTypeId, data, tsuid, rspHandler);
    }

    /**
     * Executes the naction operation.
     *
     * @param asuid        the asuid.
     * @param cuid         the cuid.
     * @param iuid         the iuid.
     * @param actionTypeId the action type id.
     * @param data         the data.
     * @param tsuid        the tsuid.
     * @param rspHandler   the rsp handler.
     * @throws IOException          if the operation cannot be completed.
     * @throws InterruptedException if the operation cannot be completed.
     */
    public void naction(
            String asuid,
            String cuid,
            String iuid,
            int actionTypeId,
            Attributes data,
            String tsuid,
            DimseRSPHandler rspHandler) throws IOException, InterruptedException {
        PresentationContext pc = pcFor(asuid, tsuid);
        checkIsSCU(asuid);
        Attributes nactionrq = Commands.mkNActionRQ(rspHandler.getMessageID(), cuid, iuid, actionTypeId, data);
        invoke(
                pc,
                nactionrq,
                DataWriterAdapter.forAttributes(data),
                rspHandler,
                conn.getSendTimeout(),
                conn.getResponseTimeout());
    }

    /**
     * Executes the naction operation.
     *
     * @param cuid         the cuid.
     * @param iuid         the iuid.
     * @param actionTypeId the action type id.
     * @param data         the data.
     * @param tsuid        the tsuid.
     * @return the operation result.
     * @throws IOException          if the operation cannot be completed.
     * @throws InterruptedException if the operation cannot be completed.
     */
    public DimseRSP naction(String cuid, String iuid, int actionTypeId, Attributes data, String tsuid)
            throws IOException, InterruptedException {
        return naction(cuid, cuid, iuid, actionTypeId, data, tsuid);
    }

    /**
     * Executes the naction operation.
     *
     * @param asuid        the asuid.
     * @param cuid         the cuid.
     * @param iuid         the iuid.
     * @param actionTypeId the action type id.
     * @param data         the data.
     * @param tsuid        the tsuid.
     * @return the operation result.
     * @throws IOException          if the operation cannot be completed.
     * @throws InterruptedException if the operation cannot be completed.
     */
    public DimseRSP naction(String asuid, String cuid, String iuid, int actionTypeId, Attributes data, String tsuid)
            throws IOException, InterruptedException {
        FutureDimseRSP rsp = new FutureDimseRSP(nextMessageID());
        naction(asuid, cuid, iuid, actionTypeId, data, tsuid, rsp);
        return rsp;
    }

    /**
     * Executes the ncreate operation.
     *
     * @param cuid       the cuid.
     * @param iuid       the iuid.
     * @param data       the data.
     * @param tsuid      the tsuid.
     * @param rspHandler the rsp handler.
     * @throws IOException          if the operation cannot be completed.
     * @throws InterruptedException if the operation cannot be completed.
     */
    public void ncreate(String cuid, String iuid, Attributes data, String tsuid, DimseRSPHandler rspHandler)
            throws IOException, InterruptedException {
        ncreate(cuid, cuid, iuid, data, tsuid, rspHandler);
    }

    /**
     * Executes the ncreate operation.
     *
     * @param asuid      the asuid.
     * @param cuid       the cuid.
     * @param iuid       the iuid.
     * @param data       the data.
     * @param tsuid      the tsuid.
     * @param rspHandler the rsp handler.
     * @throws IOException          if the operation cannot be completed.
     * @throws InterruptedException if the operation cannot be completed.
     */
    public void ncreate(
            String asuid,
            String cuid,
            String iuid,
            Attributes data,
            String tsuid,
            DimseRSPHandler rspHandler) throws IOException, InterruptedException {
        PresentationContext pc = pcFor(asuid, tsuid);
        checkIsSCU(asuid);
        Attributes ncreaterq = Commands.mkNCreateRQ(rspHandler.getMessageID(), cuid, iuid);
        invoke(
                pc,
                ncreaterq,
                DataWriterAdapter.forAttributes(data),
                rspHandler,
                conn.getSendTimeout(),
                conn.getResponseTimeout());
    }

    /**
     * Executes the ncreate operation.
     *
     * @param cuid  the cuid.
     * @param iuid  the iuid.
     * @param data  the data.
     * @param tsuid the tsuid.
     * @return the operation result.
     * @throws IOException          if the operation cannot be completed.
     * @throws InterruptedException if the operation cannot be completed.
     */
    public DimseRSP ncreate(String cuid, String iuid, Attributes data, String tsuid)
            throws IOException, InterruptedException {
        return ncreate(cuid, cuid, iuid, data, tsuid);
    }

    /**
     * Executes the ncreate operation.
     *
     * @param asuid the asuid.
     * @param cuid  the cuid.
     * @param iuid  the iuid.
     * @param data  the data.
     * @param tsuid the tsuid.
     * @return the operation result.
     * @throws IOException          if the operation cannot be completed.
     * @throws InterruptedException if the operation cannot be completed.
     */
    public DimseRSP ncreate(String asuid, String cuid, String iuid, Attributes data, String tsuid)
            throws IOException, InterruptedException {
        FutureDimseRSP rsp = new FutureDimseRSP(nextMessageID());
        ncreate(asuid, cuid, iuid, data, tsuid, rsp);
        return rsp;
    }

    /**
     * Executes the ndelete operation.
     *
     * @param cuid       the cuid.
     * @param iuid       the iuid.
     * @param rspHandler the rsp handler.
     * @throws IOException          if the operation cannot be completed.
     * @throws InterruptedException if the operation cannot be completed.
     */
    public void ndelete(String cuid, String iuid, DimseRSPHandler rspHandler) throws IOException, InterruptedException {
        ndelete(cuid, cuid, iuid, rspHandler);
    }

    /**
     * Executes the ndelete operation.
     *
     * @param asuid      the asuid.
     * @param cuid       the cuid.
     * @param iuid       the iuid.
     * @param rspHandler the rsp handler.
     * @throws IOException          if the operation cannot be completed.
     * @throws InterruptedException if the operation cannot be completed.
     */
    public void ndelete(String asuid, String cuid, String iuid, DimseRSPHandler rspHandler)
            throws IOException, InterruptedException {
        PresentationContext pc = pcFor(asuid, null);
        checkIsSCU(asuid);
        Attributes ndeleterq = Commands.mkNDeleteRQ(rspHandler.getMessageID(), cuid, iuid);
        invoke(pc, ndeleterq, null, rspHandler, conn.getSendTimeout(), conn.getResponseTimeout());
    }

    /**
     * Executes the ndelete operation.
     *
     * @param cuid the cuid.
     * @param iuid the iuid.
     * @return the operation result.
     * @throws IOException          if the operation cannot be completed.
     * @throws InterruptedException if the operation cannot be completed.
     */
    public DimseRSP ndelete(String cuid, String iuid) throws IOException, InterruptedException {
        return ndelete(cuid, cuid, iuid);
    }

    /**
     * Executes the ndelete operation.
     *
     * @param asuid the asuid.
     * @param cuid  the cuid.
     * @param iuid  the iuid.
     * @return the operation result.
     * @throws IOException          if the operation cannot be completed.
     * @throws InterruptedException if the operation cannot be completed.
     */
    public DimseRSP ndelete(String asuid, String cuid, String iuid) throws IOException, InterruptedException {
        FutureDimseRSP rsp = new FutureDimseRSP(nextMessageID());
        ndelete(asuid, cuid, iuid, rsp);
        return rsp;
    }

    /**
     * Executes the invoke operation.
     *
     * @param pc          the pc.
     * @param cmd         the cmd.
     * @param data        the data.
     * @param rspHandler  the rsp handler.
     * @param sendTimeout the send timeout.
     * @param rspTimeout  the rsp timeout.
     * @throws IOException          if the operation cannot be completed.
     * @throws InterruptedException if the operation cannot be completed.
     */
    public void invoke(
            PresentationContext pc,
            Attributes cmd,
            DataWriter data,
            DimseRSPHandler rspHandler,
            int sendTimeout,
            int rspTimeout) throws IOException, InterruptedException {
        invoke(pc, cmd, data, rspHandler, sendTimeout, rspTimeout, true);
    }

    /**
     * Executes the invoke operation.
     *
     * @param pc            the pc.
     * @param cmd           the cmd.
     * @param data          the data.
     * @param rspHandler    the rsp handler.
     * @param sendTimeout   the send timeout.
     * @param rspTimeout    the rsp timeout.
     * @param stopOnPending the stop on pending.
     * @throws IOException          if the operation cannot be completed.
     * @throws InterruptedException if the operation cannot be completed.
     */
    public void invoke(
            PresentationContext pc,
            Attributes cmd,
            DataWriter data,
            DimseRSPHandler rspHandler,
            int sendTimeout,
            int rspTimeout,
            boolean stopOnPending) throws IOException, InterruptedException {
        stopTimeout();
        checkException();
        rspHandler.setPC(pc);
        addDimseRSPHandler(rspHandler);
        startSendTimeout(sendTimeout);
        try {
            encoder.writeDIMSE(pc, cmd, data);
            stopTimeout();
            startTimeout(rspHandler.getMessageID(), rspTimeout, stopOnPending);
        } catch (IOException | RuntimeException e) {
            // In some scenarios, there might be a zombie thread
            // waiting forever for a spot to write into the queue
            // if we don't handle an exception here.
            removeDimseRSPHandler(rspHandler.getMessageID());
            throw e;
        }
    }

    /**
     * Creates the file meta information.
     *
     * @param iuid  the iuid.
     * @param cuid  the cuid.
     * @param tsuid the tsuid.
     * @return the operation result.
     */
    public Attributes createFileMetaInformation(String iuid, String cuid, String tsuid) {
        Attributes fmi = new Attributes(7);
        fmi.setBytes(Tag.FileMetaInformationVersion, VR.OB, new byte[] { 0, 1 });
        fmi.setString(Tag.MediaStorageSOPClassUID, VR.UI, cuid);
        fmi.setString(Tag.MediaStorageSOPInstanceUID, VR.UI, iuid);
        fmi.setString(Tag.TransferSyntaxUID, VR.UI, tsuid);
        fmi.setString(Tag.ImplementationClassUID, VR.UI, getRemoteImplClassUID());
        String versionName = getRemoteImplVersionName();
        if (versionName != null)
            fmi.setString(Tag.ImplementationVersionName, VR.SH, versionName);
        fmi.setString(Tag.SourceApplicationEntityTitle, VR.AE, getRemoteAET());
        return fmi;
    }

    /**
     * Gets the query options for.
     *
     * @param cuid the cuid.
     * @return the query options for.
     */
    public EnumSet<QueryOption> getQueryOptionsFor(String cuid) {
        return QueryOption.toOptions(ac.getExtNegotiationFor(cuid));
    }

    /**
     * Gets the requested query options for.
     *
     * @param cuid the cuid.
     * @return the requested query options for.
     */
    public EnumSet<QueryOption> getRequestedQueryOptionsFor(String cuid) {
        return QueryOption.toOptions(rq.getExtNegotiationFor(cuid));
    }

    /**
     * Gets the performing operation count.
     *
     * @return the performing operation count.
     */
    public int getPerformingOperationCount() {
        return performing;
    }

}
