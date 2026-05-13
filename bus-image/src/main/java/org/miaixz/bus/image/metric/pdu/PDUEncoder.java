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
package org.miaixz.bus.image.metric.pdu;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.miaixz.bus.image.Dimse;
import org.miaixz.bus.image.Status;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.io.ImageOutputStream;
import org.miaixz.bus.image.metric.Association;
import org.miaixz.bus.image.metric.Connection;
import org.miaixz.bus.image.metric.DataWriter;
import org.miaixz.bus.image.metric.DataWriterAdapter;
import org.miaixz.bus.image.metric.net.ItemType;
import org.miaixz.bus.image.metric.net.PDVOutputStream;
import org.miaixz.bus.image.metric.net.PDVType;
import org.miaixz.bus.logger.Logger;

/**
 * Represents the PDUEncoder type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class PDUEncoder extends PDVOutputStream {

    /**
     * The as value.
     */
    private final Association as;

    /**
     * The out value.
     */
    private final OutputStream out;

    /**
     * The dimse lock value.
     */
    private final Object dimseLock = new Object();

    /**
     * The write lock value.
     */
    private final Lock writeLock = new ReentrantLock(true);

    /**
     * The buf value.
     */
    private byte[] buf = new byte[Connection.DEF_MAX_PDU_LENGTH + 6];

    /**
     * The pos value.
     */
    private int pos;

    /**
     * The pdvpcid value.
     */
    private int pdvpcid;

    /**
     * The pdvcmd value.
     */
    private int pdvcmd;

    /**
     * The pdvpos value.
     */
    private int pdvpos;

    /**
     * The maxpdulen value.
     */
    private int maxpdulen;

    /**
     * The th value.
     */
    private Thread th;

    /**
     * Creates a new instance.
     *
     * @param as  the as.
     * @param out the out.
     */
    public PDUEncoder(Association as, OutputStream out) {
        this.as = as;
        this.out = out;
    }

    /**
     * Executes the write operation.
     *
     * @param rq the rq.
     * @throws IOException if the operation cannot be completed.
     */
    public void write(AAssociateRQ rq) throws IOException {
        encode(rq, PDUType.A_ASSOCIATE_RQ, ItemType.RQ_PRES_CONTEXT);
        writePDU(pos - 6);
    }

    /**
     * Executes the write operation.
     *
     * @param ac the ac.
     * @throws IOException if the operation cannot be completed.
     */
    public void write(AAssociateAC ac) throws IOException {
        encode(ac, PDUType.A_ASSOCIATE_AC, ItemType.AC_PRES_CONTEXT);
        writePDU(pos - 6);
    }

    /**
     * Executes the write operation.
     *
     * @param rj the rj.
     * @throws IOException if the operation cannot be completed.
     */
    public void write(AAssociateRJ rj) throws IOException {
        write(PDUType.A_ASSOCIATE_RJ, rj.getResult(), rj.getSource(), rj.getReason(), true);
    }

    /**
     * Writes the a release rq.
     *
     * @throws IOException if the operation cannot be completed.
     */
    public void writeAReleaseRQ() throws IOException {
        synchronized (dimseLock) {
            write(PDUType.A_RELEASE_RQ, 0, 0, 0, true);
        }
    }

    /**
     * Writes the a release rp.
     */
    public void writeAReleaseRP() {
        try {
            write(PDUType.A_RELEASE_RP, 0, 0, 0, false);
        } catch (IOException e) {
            Logger.warn(
                    false,
                    "Image",
                    e,
                    "A-RELEASE-RP write failed: protocol=pdu, requestor={}, state={}, exception={}",
                    as.isRequestor(),
                    as.getState(),
                    e.getClass().getSimpleName());
        }
    }

    /**
     * Executes the write operation.
     *
     * @param aa the aa.
     */
    public void write(AAbort aa) {
        try {
            write(PDUType.A_ABORT, 0, aa.getSource(), aa.getReason(), false);
        } catch (IOException e) {
            Logger.warn(
                    false,
                    "Image",
                    e,
                    "A-ABORT write failed: protocol=pdu, requestor={}, state={}, source={}, reason={}, exception={}",
                    as.isRequestor(),
                    as.getState(),
                    aa.getSource(),
                    aa.getReason(),
                    e.getClass().getSimpleName());
        }
    }

    /**
     * Executes the write operation.
     *
     * @param pdutype  the pdutype.
     * @param result   the result.
     * @param source   the source.
     * @param reason   the reason.
     * @param blocking the blocking.
     * @throws IOException if the operation cannot be completed.
     */
    private void write(int pdutype, int result, int source, int reason, boolean blocking) throws IOException {
        if (blocking) {
            writeLock.lock();
        } else {
            try {
                int timeout = as.getConnection().getAbortTimeout();
                Logger.debug(
                        false,
                        "Image",
                        "A-ABORT timeout started: protocol=pdu, association={}, timeoutMs={}",
                        as,
                        timeout);
                if (!writeLock.tryLock(timeout, TimeUnit.MILLISECONDS)) {
                    Logger.info(false, "Image", "A-ABORT timeout expired: protocol=pdu, association={}", as);
                    return;
                }
            } catch (InterruptedException e) {
                if (!writeLock.tryLock()) {
                    Logger.warn(
                            false,
                            "Image",
                            e,
                            "A-ABORT timeout interrupted: protocol=pdu, requestor={}, state={}, timeoutMs={}, exception={}",
                            as.isRequestor(),
                            as.getState(),
                            as.getConnection().getAbortTimeout(),
                            e.getClass().getSimpleName());
                    return;
                }
            }
            Logger.debug(false, "Image", "A-ABORT timeout stopped: protocol=pdu, association={}", as);
        }
        byte[] b = { (byte) pdutype, 0, 0, 0, 0, 4, 0, (byte) result, (byte) source, (byte) reason };
        try {
            out.write(b);
            out.flush();
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Writes the pdu.
     *
     * @param pdulen the pdulen.
     * @throws IOException if the operation cannot be completed.
     */
    private void writePDU(int pdulen) throws IOException {
        writeLock.lock();
        try {
            out.write(buf, 0, 6 + pdulen);
            out.flush();
        } catch (IOException e) {
            Logger.warn(
                    false,
                    "Image",
                    e,
                    "PDU write failed: requestor={}, state={}, pduLength={}, exception={}",
                    as.isRequestor(),
                    as.getState(),
                    pdulen,
                    e.getClass().getSimpleName());
            as.onIOException(e);
            throw e;
        } finally {
            writeLock.unlock();
        }
        pdvpos = 6;
        pos = 12;
    }

    /**
     * Executes the encode operation.
     *
     * @param rqac       the rqac.
     * @param pduType    the pdu type.
     * @param pcItemType the pc item type.
     */
    private void encode(AAssociateRQAC rqac, int pduType, int pcItemType) {
        rqac.checkCallingAET();
        rqac.checkCalledAET();

        int pdulen = rqac.length();
        if (buf.length < 6 + pdulen)
            buf = new byte[6 + pdulen];
        pos = 0;
        put(pduType);
        put(0);
        putInt(pdulen);
        putShort(rqac.getProtocolVersion());
        put(0);
        put(0);
        encodeAET(rqac.getCalledAET());
        encodeAET(rqac.getCallingAET());
        put(rqac.getReservedBytes(), 0, 32);
        encodeStringItem(ItemType.APP_CONTEXT, rqac.getApplicationContext());
        for (PresentationContext pc : rqac.getPresentationContexts())
            encode(pc, pcItemType);
        encodeUserInfo(rqac);
    }

    /**
     * Executes the put operation.
     *
     * @param ch the ch.
     */
    private void put(int ch) {
        buf[pos++] = (byte) ch;
    }

    /**
     * Executes the put operation.
     *
     * @param b the b.
     */
    private void put(byte[] b) {
        put(b, 0, b.length);
    }

    /**
     * Executes the put operation.
     *
     * @param b   the b.
     * @param off the off.
     * @param len the len.
     */
    private void put(byte[] b, int off, int len) {
        System.arraycopy(b, off, buf, pos, len);
        pos += len;
    }

    /**
     * Executes the put short operation.
     *
     * @param v the v.
     */
    private void putShort(int v) {
        buf[pos++] = (byte) (v >> 8);
        buf[pos++] = (byte) v;
    }

    /**
     * Executes the put int operation.
     *
     * @param v the v.
     */
    private void putInt(int v) {
        buf[pos++] = (byte) (v >> 24);
        buf[pos++] = (byte) (v >> 16);
        buf[pos++] = (byte) (v >> 8);
        buf[pos++] = (byte) v;
    }

    /**
     * Executes the put string operation.
     *
     * @param s the s.
     */
    private void putString(String s) {
        int len = s.length();
        s.getBytes(0, len, buf, pos);
        pos += len;
    }

    /**
     * Executes the encode operation.
     *
     * @param b the b.
     */
    private void encode(byte[] b) {
        putShort(b.length);
        put(b, 0, b.length);
    }

    /**
     * Executes the encode operation.
     *
     * @param s the s.
     */
    private void encode(String s) {
        putShort(s.length());
        putString(s);
    }

    /**
     * Executes the encode aet operation.
     *
     * @param aet the aet.
     */
    private void encodeAET(String aet) {
        int endpos = pos + 16;
        putString(aet);
        while (pos < endpos)
            put(0x20);
    }

    /**
     * Executes the encode item header operation.
     *
     * @param type the type.
     * @param len  the len.
     */
    private void encodeItemHeader(int type, int len) {
        put(type);
        put(0);
        putShort(len);
    }

    /**
     * Executes the encode string item operation.
     *
     * @param type the type.
     * @param s    the s.
     */
    private void encodeStringItem(int type, String s) {
        if (s == null)
            return;

        encodeItemHeader(type, s.length());
        putString(s);
    }

    /**
     * Executes the encode operation.
     *
     * @param pc         the pc.
     * @param pcItemType the pc item type.
     */
    private void encode(PresentationContext pc, int pcItemType) {
        encodeItemHeader(pcItemType, pc.length());
        put(pc.getPCID());
        put(0);
        put(pc.getResult());
        put(0);
        encodeStringItem(ItemType.ABSTRACT_SYNTAX, pc.getAbstractSyntax());
        for (String ts : pc.getTransferSyntaxes())
            encodeStringItem(ItemType.TRANSFER_SYNTAX, ts);
    }

    /**
     * Executes the encode user info operation.
     *
     * @param rqac the rqac.
     */
    private void encodeUserInfo(AAssociateRQAC rqac) {
        encodeItemHeader(ItemType.USER_INFO, rqac.userInfoLength());
        encodeMaxPDULength(rqac.getMaxPDULength());
        encodeStringItem(ItemType.IMPL_CLASS_UID, rqac.getImplClassUID());
        if (rqac.isAsyncOps())
            encodeAsyncOpsWindow(rqac);
        for (RoleSelection rs : rqac.getRoleSelections())
            encode(rs);
        encodeStringItem(ItemType.IMPL_VERSION_NAME, rqac.getImplVersionName());
        for (ExtendedNegotiation extNeg : rqac.getExtendedNegotiations())
            encode(extNeg);
        for (CommonExtended extNeg : rqac.getCommonExtendedNegotiations())
            encode(extNeg);
        encode(rqac.getUserIdentityRQ());
        encode(rqac.getUserIdentityAC());
    }

    /**
     * Executes the encode max pdu length operation.
     *
     * @param maxPDULength the max pdu length.
     */
    private void encodeMaxPDULength(int maxPDULength) {
        encodeItemHeader(ItemType.MAX_PDU_LENGTH, 4);
        putInt(maxPDULength);
    }

    /**
     * Executes the encode async ops window operation.
     *
     * @param rqac the rqac.
     */
    private void encodeAsyncOpsWindow(AAssociateRQAC rqac) {
        encodeItemHeader(ItemType.ASYNC_OPS_WINDOW, 4);
        putShort(rqac.getMaxOpsInvoked());
        putShort(rqac.getMaxOpsPerformed());
    }

    /**
     * Executes the encode operation.
     *
     * @param rs the rs.
     */
    private void encode(RoleSelection rs) {
        encodeItemHeader(ItemType.ROLE_SELECTION, rs.length());
        encode(rs.getSOPClassUID());
        put(rs.isSCU() ? 1 : 0);
        put(rs.isSCP() ? 1 : 0);
    }

    /**
     * Executes the encode operation.
     *
     * @param extNeg the ext neg.
     */
    private void encode(ExtendedNegotiation extNeg) {
        encodeItemHeader(ItemType.EXT_NEG, extNeg.length());
        encode(extNeg.getSOPClassUID());
        put(extNeg.getInformation());
    }

    /**
     * Executes the encode operation.
     *
     * @param extNeg the ext neg.
     */
    private void encode(CommonExtended extNeg) {
        encodeItemHeader(ItemType.COMMON_EXT_NEG, extNeg.length());
        encode(extNeg.getSOPClassUID());
        encode(extNeg.getServiceClassUID());
        putShort(extNeg.getRelatedGeneralSOPClassUIDsLength());
        for (String cuid : extNeg.getRelatedGeneralSOPClassUIDs())
            encode(cuid);
    }

    /**
     * Executes the encode operation.
     *
     * @param userIdentity the user identity.
     */
    private void encode(IdentityRQ userIdentity) {
        if (userIdentity == null)
            return;

        encodeItemHeader(ItemType.RQ_USER_IDENTITY, userIdentity.length());
        put(userIdentity.getType());
        put(userIdentity.isPositiveResponseRequested() ? 1 : 0);
        encode(userIdentity.getPrimaryField());
        encode(userIdentity.getSecondaryField());
    }

    /**
     * Executes the encode operation.
     *
     * @param userIdentity the user identity.
     */
    private void encode(IdentityAC userIdentity) {
        if (userIdentity == null)
            return;

        encodeItemHeader(ItemType.AC_USER_IDENTITY, userIdentity.length());
        encode(userIdentity.getServerResponse());
    }

    /**
     * Executes the write operation.
     *
     * @param b the b.
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public void write(int b) throws IOException {
        checkThread();
        flushPDataTF();
        put(b);
    }

    /**
     * Executes the write operation.
     *
     * @param b   the b.
     * @param off the off.
     * @param len the len.
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        checkThread();
        int pos = off;
        int remaining = len;
        while (remaining > 0) {
            flushPDataTF();
            int write = Math.min(remaining, free());
            put(b, pos, write);
            pos += write;
            remaining -= write;
        }
    }

    /**
     * Executes the close operation.
     */
    @Override
    public void close() {
        checkThread();
        encodePDVHeader(PDVType.LAST);
    }

    /**
     * Copies the from.
     *
     * @param in  the in.
     * @param len the len.
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public void copyFrom(InputStream in, int len) throws IOException {
        checkThread();
        int remaining = len;
        while (remaining > 0) {
            flushPDataTF();
            int copy = in.read(buf, pos, Math.min(remaining, free()));
            if (copy == -1)
                throw new EOFException();
            pos += copy;
            remaining -= copy;
        }
    }

    /**
     * Copies the from.
     *
     * @param in the in.
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public void copyFrom(InputStream in) throws IOException {
        checkThread();
        for (;;) {
            flushPDataTF();
            int copy = in.read(buf, pos, free());
            if (copy == -1)
                return;
            pos += copy;
        }
    }

    /**
     * Executes the check thread operation.
     */
    private void checkThread() {
        if (th != Thread.currentThread())
            throw new IllegalStateException("Entered by wrong thread");
    }

    /**
     * Executes the free operation.
     *
     * @return the operation result.
     */
    private int free() {
        return maxpdulen + 6 - pos;
    }

    /**
     * Executes the flush p data tf operation.
     *
     * @throws IOException if the operation cannot be completed.
     */
    private void flushPDataTF() throws IOException {
        if (free() > 0)
            return;
        encodePDVHeader(PDVType.PENDING);
        as.writePDataTF();
    }

    /**
     * Executes the encode pdv header operation.
     *
     * @param last the last.
     */
    private void encodePDVHeader(int last) {
        final int endpos = pos;
        final int pdvlen = endpos - pdvpos - 4;
        pos = pdvpos;
        putInt(pdvlen);
        put(pdvpcid);
        put(pdvcmd | last);
        pos = endpos;
        Logger.trace(
                false,
                "Image",
                "PDV sent: protocol=pdu, association={}, length={}, pcid={}, messageControlHeader={}",
                as,
                pdvlen,
                pdvpcid,
                (pdvcmd | last));
    }

    /**
     * Writes the p data tf.
     *
     * @throws IOException if the operation cannot be completed.
     */
    public void writePDataTF() throws IOException {
        int pdulen = pos - 6;
        pos = 0;
        put(PDUType.P_DATA_TF);
        put(0);
        putInt(pdulen);
        Logger.trace(false, "Image", "P-DATA-TF sent: protocol=pdu, association={}, length={}", as, pdulen);
        writePDU(pdulen);
    }

    /**
     * Writes the dimse.
     *
     * @param pc         the pc.
     * @param cmd        the cmd.
     * @param dataWriter the data writer.
     * @throws IOException if the operation cannot be completed.
     */
    public void writeDIMSE(PresentationContext pc, Attributes cmd, DataWriter dataWriter) throws IOException {
        synchronized (dimseLock) {
            int pcid = pc.getPCID();
            String tsuid = pc.getTransferSyntax();
            Dimse dimse = Dimse.valueOf(cmd.getInt(Tag.CommandField, -1));
            if (!dimse.isRSP() || !Status.isPending(cmd.getInt(Tag.Status, -1)))
                as.incSentCount(dimse);
            if (Logger.isInfoEnabled()) {
                Logger.info(
                        false,
                        "Image",
                        "DIMSE message sent: protocol=pdu, association={}, dimse={}",
                        as,
                        dimse.toString(cmd, pcid, tsuid));
                if (Logger.isDebugEnabled()) {
                    Logger.debug(
                            false,
                            "Image",
                            "DIMSE command sending: protocol=pdu, association={}, dimse={}, commandAttributes={}, pcid={}, transferSyntax={}",
                            as,
                            dimse.toString(cmd),
                            cmd == null ? 0 : cmd.size(),
                            pcid,
                            tsuid);
                }
            }
            this.th = Thread.currentThread();
            maxpdulen = as.getMaxPDULengthSend();
            if (buf.length < maxpdulen + 6)
                buf = new byte[maxpdulen + 6];

            pdvpcid = pcid;
            pdvcmd = PDVType.COMMAND;
            ImageOutputStream cmdout = new ImageOutputStream(this, UID.ImplicitVRLittleEndian.uid);
            cmdout.writeCommand(cmd);
            cmdout.close();
            if (dataWriter != null) {
                if (!as.isPackPDV()) {
                    as.writePDataTF();
                } else {
                    pdvpos = pos;
                    pos += 6;
                }
                pdvcmd = PDVType.DATA;
                if (Logger.isDebugEnabled()) {
                    if (dataWriter instanceof DataWriterAdapter)
                        Logger.debug(
                                false,
                                "Image",
                                "DIMSE dataset sending: protocol=pdu, association={}, dimse={}, datasetAttributes={}",
                                as,
                                dimse.toString(cmd),
                                ((DataWriterAdapter) dataWriter).getDataset() == null ? 0
                                        : ((DataWriterAdapter) dataWriter).getDataset().size());
                    else
                        Logger.debug(
                                true,
                                "Image",
                                "DIMSE dataset sending: protocol=pdu, association={}, dimse={}",
                                as,
                                dimse.toString(cmd));
                }
                dataWriter.writeTo(this, tsuid);
                if (Logger.isDebugEnabled() && !(dataWriter instanceof DataWriterAdapter))
                    Logger.debug(
                            false,
                            "Image",
                            "DIMSE dataset sent: protocol=pdu, association={}, dimse={}",
                            as,
                            dimse.toString(cmd));
                close();
            }
            as.writePDataTF();
            this.th = null;
        }
    }

}
