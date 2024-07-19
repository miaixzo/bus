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
package org.miaixz.bus.image.metric.pdu;

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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PDUEncoder extends PDVOutputStream {

    private final Association as;
    private final OutputStream out;
    private final Object dimseLock = new Object();
    private final Lock writeLock = new ReentrantLock(true);
    private byte[] buf = new byte[Connection.DEF_MAX_PDU_LENGTH + 6];
    private int pos;
    private int pdvpcid;
    private int pdvcmd;
    private int pdvpos;
    private int maxpdulen;
    private Thread th;

    public PDUEncoder(Association as, OutputStream out) {
        this.as = as;
        this.out = out;
    }

    public void write(AAssociateRQ rq) throws IOException {
        encode(rq, PDUType.A_ASSOCIATE_RQ, ItemType.RQ_PRES_CONTEXT);
        writePDU(pos - 6);
    }

    public void write(AAssociateAC ac) throws IOException {
        encode(ac, PDUType.A_ASSOCIATE_AC, ItemType.AC_PRES_CONTEXT);
        writePDU(pos - 6);
    }

    public void write(AAssociateRJ rj) throws IOException {
        write(PDUType.A_ASSOCIATE_RJ, rj.getResult(), rj.getSource(), rj.getReason(), true);
    }

    public void writeAReleaseRQ() throws IOException {
        synchronized (dimseLock) {
            write(PDUType.A_RELEASE_RQ, 0, 0, 0, true);
        }
    }

    public void writeAReleaseRP() {
        try {
            write(PDUType.A_RELEASE_RP, 0, 0, 0, false);
        } catch (IOException e) {
            Logger.info("{} << A-RELEASE-RP failed: {}", as, e.getMessage());
        }
    }

    public void write(AAbort aa) {
        try {
            write(PDUType.A_ABORT, 0, aa.getSource(), aa.getReason(), false);
        } catch (IOException e) {
            Logger.info("{} << {} failed: {}", as, aa, e.getMessage());
        }
    }

    private void write(int pdutype, int result, int source, int reason, boolean blocking) throws IOException {
        if (blocking) {
            writeLock.lock();
        } else {
            try {
                int timeout = as.getConnection().getAbortTimeout();
                Logger.debug("{}: start A-ABORT timeout of {}ms", as, timeout);
                if (!writeLock.tryLock(timeout, TimeUnit.MILLISECONDS)) {
                    Logger.info("{}: A-ABORT timeout expired", as);
                    return;
                }
            } catch (InterruptedException e) {
                if (!writeLock.tryLock()) {
                    Logger.info("{}: A-ABORT timeout interrupted: {}", as, e.getMessage());
                    return;
                }
            }
            Logger.debug("{}: stop A-ABORT timeout", as);
        }
        byte[] b = {
                (byte) pdutype,
                0,
                0, 0, 0, 4,
                0,
                (byte) result,
                (byte) source,
                (byte) reason
        };
        try {
            out.write(b);
            out.flush();
        } finally {
            writeLock.unlock();
        }
    }

    private void writePDU(int pdulen) throws IOException {
        writeLock.lock();
        try {
            out.write(buf, 0, 6 + pdulen);
            out.flush();
        } catch (IOException e) {
            as.onIOException(e);
            throw e;
        } finally {
            writeLock.unlock();
        }
        pdvpos = 6;
        pos = 12;
    }

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

    private void put(int ch) {
        buf[pos++] = (byte) ch;
    }

    private void put(byte[] b) {
        put(b, 0, b.length);
    }

    private void put(byte[] b, int off, int len) {
        System.arraycopy(b, off, buf, pos, len);
        pos += len;
    }

    private void putShort(int v) {
        buf[pos++] = (byte) (v >> 8);
        buf[pos++] = (byte) v;
    }

    private void putInt(int v) {
        buf[pos++] = (byte) (v >> 24);
        buf[pos++] = (byte) (v >> 16);
        buf[pos++] = (byte) (v >> 8);
        buf[pos++] = (byte) v;
    }

    private void putString(String s) {
        int len = s.length();
        s.getBytes(0, len, buf, pos);
        pos += len;
    }

    private void encode(byte[] b) {
        putShort(b.length);
        put(b, 0, b.length);
    }

    private void encode(String s) {
        putShort(s.length());
        putString(s);
    }

    private void encodeAET(String aet) {
        int endpos = pos + 16;
        putString(aet);
        while (pos < endpos)
            put(0x20);
    }

    private void encodeItemHeader(int type, int len) {
        put(type);
        put(0);
        putShort(len);
    }

    private void encodeStringItem(int type, String s) {
        if (s == null)
            return;

        encodeItemHeader(type, s.length());
        putString(s);
    }

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
        for (CommonExtended extNeg :
                rqac.getCommonExtendedNegotiations())
            encode(extNeg);
        encode(rqac.getUserIdentityRQ());
        encode(rqac.getUserIdentityAC());
    }

    private void encodeMaxPDULength(int maxPDULength) {
        encodeItemHeader(ItemType.MAX_PDU_LENGTH, 4);
        putInt(maxPDULength);
    }

    private void encodeAsyncOpsWindow(AAssociateRQAC rqac) {
        encodeItemHeader(ItemType.ASYNC_OPS_WINDOW, 4);
        putShort(rqac.getMaxOpsInvoked());
        putShort(rqac.getMaxOpsPerformed());
    }

    private void encode(RoleSelection rs) {
        encodeItemHeader(ItemType.ROLE_SELECTION, rs.length());
        encode(rs.getSOPClassUID());
        put(rs.isSCU() ? 1 : 0);
        put(rs.isSCP() ? 1 : 0);
    }

    private void encode(ExtendedNegotiation extNeg) {
        encodeItemHeader(ItemType.EXT_NEG, extNeg.length());
        encode(extNeg.getSOPClassUID());
        put(extNeg.getInformation());
    }

    private void encode(CommonExtended extNeg) {
        encodeItemHeader(ItemType.COMMON_EXT_NEG, extNeg.length());
        encode(extNeg.getSOPClassUID());
        encode(extNeg.getServiceClassUID());
        putShort(extNeg.getRelatedGeneralSOPClassUIDsLength());
        for (String cuid : extNeg.getRelatedGeneralSOPClassUIDs())
            encode(cuid);
    }

    private void encode(IdentityRQ userIdentity) {
        if (userIdentity == null)
            return;

        encodeItemHeader(ItemType.RQ_USER_IDENTITY, userIdentity.length());
        put(userIdentity.getType());
        put(userIdentity.isPositiveResponseRequested() ? 1 : 0);
        encode(userIdentity.getPrimaryField());
        encode(userIdentity.getSecondaryField());
    }

    private void encode(IdentityAC userIdentity) {
        if (userIdentity == null)
            return;

        encodeItemHeader(ItemType.AC_USER_IDENTITY, userIdentity.length());
        encode(userIdentity.getServerResponse());
    }

    @Override
    public void write(int b) throws IOException {
        checkThread();
        flushPDataTF();
        put(b);
    }

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

    @Override
    public void close() {
        checkThread();
        encodePDVHeader(PDVType.LAST);
    }

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

    @Override
    public void copyFrom(InputStream in) throws IOException {
        checkThread();
        for (; ; ) {
            flushPDataTF();
            int copy = in.read(buf, pos, free());
            if (copy == -1)
                return;
            pos += copy;
        }
    }

    private void checkThread() {
        if (th != Thread.currentThread())
            throw new IllegalStateException("Entered by wrong thread");
    }

    private int free() {
        return maxpdulen + 6 - pos;
    }

    private void flushPDataTF() throws IOException {
        if (free() > 0)
            return;
        encodePDVHeader(PDVType.PENDING);
        as.writePDataTF();
    }

    private void encodePDVHeader(int last) {
        final int endpos = pos;
        final int pdvlen = endpos - pdvpos - 4;
        pos = pdvpos;
        putInt(pdvlen);
        put(pdvpcid);
        put(pdvcmd | last);
        pos = endpos;
        Logger.trace("{} << PDV[len={}, pcid={}, mch={}]",
                as, pdvlen, pdvpcid, (pdvcmd | last));
    }

    public void writePDataTF() throws IOException {
        int pdulen = pos - 6;
        pos = 0;
        put(PDUType.P_DATA_TF);
        put(0);
        putInt(pdulen);
        Logger.trace("{} << P-DATA-TF[len={}]",
                as, pdulen);
        writePDU(pdulen);
    }

    public void writeDIMSE(PresentationContext pc, Attributes cmd,
                           DataWriter dataWriter) throws IOException {
        synchronized (dimseLock) {
            int pcid = pc.getPCID();
            String tsuid = pc.getTransferSyntax();
            Dimse dimse = Dimse.valueOf(cmd.getInt(Tag.CommandField, -1));
            if (!dimse.isRSP() || !Status.isPending(cmd.getInt(Tag.Status, -1)))
                as.incSentCount(dimse);
            if (Logger.isInfo()) {
                Logger.info("{} << {}", as, dimse.toString(cmd, pcid, tsuid));
                if (Logger.isDebug()) {
                    Logger.debug("{} << {} Command:\n{}", as, dimse.toString(cmd), cmd);
                }
            }
            this.th = Thread.currentThread();
            maxpdulen = as.getMaxPDULengthSend();
            if (buf.length < maxpdulen + 6)
                buf = new byte[maxpdulen + 6];

            pdvpcid = pcid;
            pdvcmd = PDVType.COMMAND;
            ImageOutputStream cmdout =
                    new ImageOutputStream(this, UID.ImplicitVRLittleEndian.uid);
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
                if (Logger.isDebug()) {
                    if (dataWriter instanceof DataWriterAdapter)
                        Logger.debug("{} << {} Dataset:\n{}", as, dimse.toString(cmd),
                                ((DataWriterAdapter) dataWriter).getDataset());
                    else
                        Logger.debug("{} << {} Dataset sending...", as, dimse.toString(cmd));
                }
                dataWriter.writeTo(this, tsuid);
                if (Logger.isDebug() && !(dataWriter instanceof DataWriterAdapter))
                    Logger.debug("{} << {} Dataset sent", as, dimse.toString(cmd));
                close();
            }
            as.writePDataTF();
            this.th = null;
        }
    }

}
