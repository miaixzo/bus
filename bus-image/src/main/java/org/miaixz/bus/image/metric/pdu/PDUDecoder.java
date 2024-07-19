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

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.ByteKit;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.StreamKit;
import org.miaixz.bus.image.Dimse;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.io.ImageInputStream;
import org.miaixz.bus.image.metric.Association;
import org.miaixz.bus.image.metric.Commands;
import org.miaixz.bus.image.metric.Connection;
import org.miaixz.bus.image.metric.net.ItemType;
import org.miaixz.bus.image.metric.net.PDVInputStream;
import org.miaixz.bus.image.metric.net.PDVType;
import org.miaixz.bus.logger.Logger;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PDUDecoder extends PDVInputStream {

    private static final String UNRECOGNIZED_PDU =
            "{}: unrecognized PDU[type={}, len={}]";
    private static final String INVALID_PDU_LENGTH =
            "{}: invalid length of PDU[type={}, len={}]";
    private static final String INVALID_COMMON_EXTENDED_NEGOTIATION =
            "{}: invalid Common Extended Negotiation sub-item in PDU[type={}, len={}]";
    private static final String INVALID_USER_IDENTITY =
            "{}: invalid User Identity sub-item in PDU[type={}, len={}]";
    private static final String INVALID_PDV =
            "{}: invalid PDV in PDU[type={}, len={}]";
    private static final String UNEXPECTED_PDV_TYPE =
            "{}: unexpected PDV type in PDU[type={}, len={}]";
    private static final String UNEXPECTED_PDV_PCID =
            "{}: unexpected pcid in PDV in PDU[type={}, len={}]";

    /**
     * 16MiB
     */
    private static final int MAX_PDU_LEN = 0x1000000;

    private final Association as;
    private final InputStream in;
    private final Thread th;
    private byte[] buf = new byte[6 + Connection.DEF_MAX_PDU_LENGTH];
    private int pos;
    private int pdutype;
    private int pdulen;
    private int pcid = -1;
    private int pdvmch;
    private int pdvend;

    public PDUDecoder(Association as, InputStream in) {
        this.as = as;
        this.in = in;
        this.th = Thread.currentThread();
    }

    private int remaining() {
        return pdulen + 6 - pos;
    }

    private boolean hasRemaining() {
        return pos < pdulen + 6;
    }

    private int get() {
        if (!hasRemaining())
            throw new IndexOutOfBoundsException();
        return buf[pos++] & 0xFF;
    }

    private void get(byte[] b, int off, int len) {
        if (len > remaining())
            throw new IndexOutOfBoundsException();
        System.arraycopy(buf, pos, b, off, len);
        pos += len;
    }

    private void skip(int len) {
        if (len > remaining())
            throw new IndexOutOfBoundsException();
        pos += len;
    }

    private int getUnsignedShort() {
        int val = ByteKit.bytesToUShortBE(buf, pos);
        pos += 2;
        return val;
    }

    private int getInt() {
        int val = ByteKit.bytesToIntBE(buf, pos);
        pos += 4;
        return val;
    }

    private byte[] getBytes(int len) {
        byte[] bs = new byte[len];
        get(bs, 0, len);
        return bs;
    }

    private byte[] decodeBytes() {
        return getBytes(getUnsignedShort());
    }

    public void nextPDU() throws IOException {
        checkThread();
        Logger.trace("{}: waiting for PDU", as);
        readFully(0, 10);
        pos = 0;
        pdutype = get();
        get();
        pdulen = getInt();
        Logger.trace("{} >> PDU[type={}, len={}]",
                as, pdutype, pdulen & 0xFFFFFFFFL);
        switch (pdutype) {
            case PDUType.A_ASSOCIATE_RQ:
                readPDU();
                as.onAAssociateRQ((AAssociateRQ) decode(new AAssociateRQ()));
                return;
            case PDUType.A_ASSOCIATE_AC:
                readPDU();
                as.onAAssociateAC((AAssociateAC) decode(new AAssociateAC()));
                return;
            case PDUType.P_DATA_TF:
                readPDU();
                as.onPDataTF();
                return;
            case PDUType.A_ASSOCIATE_RJ:
                checkPDULength(4);
                get();
                as.onAAssociateRJ(new AAssociateRJ(get(), get(), get()));
                break;
            case PDUType.A_RELEASE_RQ:
                checkPDULength(4);
                as.onAReleaseRQ();
                break;
            case PDUType.A_RELEASE_RP:
                checkPDULength(4);
                as.onAReleaseRP();
                break;
            case PDUType.A_ABORT:
                checkPDULength(4);
                get();
                get();
                as.onAAbort(new AAbort(get(), get()));
                break;
            default:
                abort(AAbort.UNRECOGNIZED_PDU, UNRECOGNIZED_PDU);
        }
    }

    private void checkThread() {
        if (th != Thread.currentThread())
            throw new IllegalStateException("Entered by wrong thread");
    }

    private void checkPDULength(int len) throws AAbort {
        if (pdulen != len)
            abort(AAbort.INVALID_PDU_PARAMETER_VALUE, INVALID_PDU_LENGTH);
    }

    private void readPDU() throws IOException {
        if (pdulen < 4 || pdulen > MAX_PDU_LEN)
            abort(AAbort.INVALID_PDU_PARAMETER_VALUE, INVALID_PDU_LENGTH);

        if (6 + pdulen > buf.length)
            buf = Arrays.copyOf(buf, 6 + pdulen);

        readFully(10, pdulen - 4);
    }

    private void readFully(int off, int len) throws IOException {
        try {
            StreamKit.readFully(in, buf, off, len);
        } catch (IOException e) {
            throw e;
        }
    }

    private void abort(int reason, String logmsg) throws AAbort {
        Logger.warn(logmsg,
                as, pdutype, pdulen & 0xFFFFFFFFL);
        throw new AAbort(AAbort.UL_SERIVE_PROVIDER, reason);
    }

    private String getString(int len) {
        if (pos + len > pdulen + 6)
            throw new IndexOutOfBoundsException();
        String s;
        // Skip illegal trailing NULL
        int len0 = len;
        while (len0 > 0 && buf[pos + len0 - 1] == 0) {
            len0--;
        }
        s = new String(buf, 0, pos, len0);
        pos += len;
        return s;
    }

    private String decodeString() {
        return getString(getUnsignedShort());
    }

    private AAssociateRQAC decode(AAssociateRQAC rqac)
            throws AAbort {
        try {
            rqac.setImplVersionName(null);
            rqac.setProtocolVersion(getUnsignedShort());
            get();
            get();
            rqac.setCalledAET(getString(Normal._16).trim());
            rqac.setCallingAET(getString(Normal._16).trim());
            rqac.setReservedBytes(getBytes(Normal._32));
            while (pos < pdulen)
                decodeItem(rqac);
            checkPDULength(pos - 6);
        } catch (IndexOutOfBoundsException e) {
            abort(AAbort.INVALID_PDU_PARAMETER_VALUE, INVALID_PDU_LENGTH);
        }
        return rqac;
    }

    private void decodeItem(AAssociateRQAC rqac) throws AAbort {
        int itemType = get();
        get(); // skip reserved byte
        int itemLen = getUnsignedShort();
        switch (itemType) {
            case ItemType.APP_CONTEXT:
                rqac.setApplicationContext(getString(itemLen));
                break;
            case ItemType.RQ_PRES_CONTEXT:
            case ItemType.AC_PRES_CONTEXT:
                rqac.addPresentationContext(decodePC(itemLen));
                break;
            case ItemType.USER_INFO:
                decodeUserInfo(itemLen, rqac);
                break;
            default:
                skip(itemLen);
        }
    }

    private PresentationContext decodePC(int itemLen) {
        int pcid = get();
        get(); // skip reserved byte
        int result = get();
        get(); // skip reserved byte
        String as = null;
        List<String> tss = new ArrayList<>(1);
        int endpos = pos + itemLen - 4;
        while (pos < endpos) {
            int subItemType = get() & 0xff;
            get(); // 跳过保留字节
            int subItemLen = getUnsignedShort();
            switch (subItemType) {
                case ItemType.ABSTRACT_SYNTAX:
                    as = getString(subItemLen);
                    break;
                case ItemType.TRANSFER_SYNTAX:
                    tss.add(getString(subItemLen));
                    break;
                default:
                    skip(subItemLen);
            }
        }
        return new PresentationContext(pcid, result, as,
                tss.toArray(new String[tss.size()]));
    }

    private void decodeUserInfo(int itemLength, AAssociateRQAC rqac) throws AAbort {
        int endpos = pos + itemLength;
        while (pos < endpos)
            decodeUserInfoSubItem(rqac);
    }

    private void decodeUserInfoSubItem(AAssociateRQAC rqac) throws AAbort {
        int itemType = get();
        get(); // 跳过保留字节
        int itemLen = getUnsignedShort();
        switch (itemType) {
            case ItemType.MAX_PDU_LENGTH:
                rqac.setMaxPDULength(getInt());
                break;
            case ItemType.IMPL_CLASS_UID:
                rqac.setImplClassUID(getString(itemLen));
                break;
            case ItemType.ASYNC_OPS_WINDOW:
                rqac.setMaxOpsInvoked(getUnsignedShort());
                rqac.setMaxOpsPerformed(getUnsignedShort());
                break;
            case ItemType.ROLE_SELECTION:
                rqac.addRoleSelection(decodeRoleSelection(itemLen));
                break;
            case ItemType.IMPL_VERSION_NAME:
                rqac.setImplVersionName(getString(itemLen));
                break;
            case ItemType.EXT_NEG:
                rqac.addExtendedNegotiation(decodeExtNeg(itemLen));
                break;
            case ItemType.COMMON_EXT_NEG:
                rqac.addCommonExtendedNegotiation(decodeCommonExtNeg(itemLen));
                break;
            case ItemType.RQ_USER_IDENTITY:
                rqac.setIdentityRQ(decodeUserIdentityRQ(itemLen));
                break;
            case ItemType.AC_USER_IDENTITY:
                rqac.setIdentityAC(decodeUserIdentityAC(itemLen));
                break;
            default:
                skip(itemLen);
        }
    }

    private RoleSelection decodeRoleSelection(int itemLen) {
        String cuid = decodeString();
        boolean scu = get() != 0;
        boolean scp = get() != 0;
        return new RoleSelection(cuid, scu, scp);
    }

    private ExtendedNegotiation decodeExtNeg(int itemLen) {
        int uidLength = getUnsignedShort();
        String cuid = getString(uidLength);
        byte[] info = getBytes(itemLen - uidLength - 2);
        return new ExtendedNegotiation(cuid, info);
    }

    private CommonExtended decodeCommonExtNeg(int itemLen)
            throws AAbort {
        int endPos = pos + itemLen;
        String sopCUID = getString(getUnsignedShort());
        String serviceCUID = getString(getUnsignedShort());
        List<String> relSopCUIDs = new ArrayList<>(1);
        int relSopCUIDsLen = getUnsignedShort();
        int endRelSopCUIDs = pos + relSopCUIDsLen;
        while (pos < endRelSopCUIDs)
            relSopCUIDs.add(decodeString());
        if (pos != endRelSopCUIDs || pos > endPos)
            abort(AAbort.INVALID_PDU_PARAMETER_VALUE,
                    INVALID_COMMON_EXTENDED_NEGOTIATION);
        skip(endPos - pos);
        return new CommonExtended(sopCUID, serviceCUID,
                relSopCUIDs.toArray(new String[relSopCUIDs.size()]));
    }

    private IdentityRQ decodeUserIdentityRQ(int itemLen) throws AAbort {
        int endPos = pos + itemLen;
        int type = get() & 0xff;
        boolean rspReq = get() != 0;
        byte[] primaryField = decodeBytes();
        byte[] secondaryField = decodeBytes();
        if (pos != endPos)
            abort(AAbort.INVALID_PDU_PARAMETER_VALUE, INVALID_USER_IDENTITY);
        return new IdentityRQ(type, rspReq, primaryField, secondaryField);
    }

    private IdentityAC decodeUserIdentityAC(int itemLen) throws AAbort {
        int endPos = pos + itemLen;
        byte[] serverResponse = decodeBytes();
        if (pos != endPos)
            abort(AAbort.INVALID_PDU_PARAMETER_VALUE, INVALID_USER_IDENTITY);
        return new IdentityAC(serverResponse);
    }

    public void decodeDIMSE() throws IOException {
        checkThread();
        if (pcid != -1)
            return; // 已经在解码器DIMSE中

        nextPDV(PDVType.COMMAND, -1);

        PresentationContext pc = as.getPresentationContext(pcid);
        if (pc == null) {
            Logger.warn(
                    "{}: No Presentation Context with given ID - {}",
                    as, pcid);
            throw new AAbort();
        }

        if (!pc.isAccepted()) {
            Logger.warn(
                    "{}: No accepted Presentation Context with given ID - {}",
                    as, pcid);
            throw new AAbort();
        }

        Attributes cmd = readCommand();
        Dimse dimse = dimseOf(cmd);
        String tsuid = pc.getTransferSyntax();
        if (Logger.isInfo()) {
            Logger.info("{} >> {}", as, dimse.toString(cmd, pcid, tsuid));
            if (Logger.isDebug()) {
                Logger.debug("{} >> {} Command:\n{}", as, dimse.toString(cmd), cmd);
            }
        }
        if (dimse == Dimse.C_CANCEL_RQ) {
            as.onCancelRQ(cmd);
        } else if (Commands.hasDataset(cmd)) {
            nextPDV(PDVType.DATA, pcid);
            if (dimse.isRSP()) {
                Attributes data = readDataset(tsuid);
                if (Logger.isDebug()) {
                    Logger.debug("{} >> {} Dataset:\n{}", as, dimse.toString(cmd), data);
                }
                as.onDimseRSP(dimse, cmd, data);
            } else {
                if (Logger.isDebug()) {
                    Logger.debug("{} >> {} Dataset receiving...", as, dimse.toString(cmd));
                }
                as.onDimseRQ(pc, dimse, cmd, this);
                long skipped = skipAll();
                if (skipped > 0)
                    Logger.debug(
                            "{}: Service User did not consume {} bytes of DIMSE data.",
                            as, skipped);
            }
        } else {
            if (dimse.isRSP()) {
                as.onDimseRSP(dimse, cmd, null);
            } else {
                as.onDimseRQ(pc, dimse, cmd, null);
            }
        }
        pcid = -1;
    }

    private Dimse dimseOf(Attributes cmd) throws AAbort {
        try {
            return Dimse.valueOf(cmd.getInt(Tag.CommandField, 0));
        } catch (IllegalArgumentException e) {
            Logger.info("{}: illegal DIMSE:", as);
            Logger.info("\n{}", cmd);
            throw new AAbort();
        }
    }

    private Attributes readCommand() throws IOException {
        ImageInputStream in =
                new ImageInputStream(this, UID.ImplicitVRLittleEndian.uid);
        try {
            return in.readCommand();
        } finally {
            IoKit.close(in);
        }
    }

    @Override
    public Attributes readDataset(String tsuid) throws IOException {
        ImageInputStream in = new ImageInputStream(this, tsuid);
        try {
            return in.readDataset();
        } finally {
            IoKit.close(in);
        }
    }

    private void nextPDV(int expectedPDVType, int expectedPCID)
            throws IOException {
        if (!hasRemaining()) {
            nextPDU();
            if (pdutype != PDUType.P_DATA_TF) {
                Logger.info(
                        "{}: Expected P-DATA-TF PDU but received PDU[type={}]",
                        as, pdutype);
                throw new EOFException();
            }
        }
        if (remaining() < 6)
            abort(AAbort.INVALID_PDU_PARAMETER_VALUE, INVALID_PDV);
        int pdvlen = getInt();
        this.pdvend = pos + pdvlen;
        if (pdvlen < 2 || pdvlen > remaining())
            abort(AAbort.INVALID_PDU_PARAMETER_VALUE, INVALID_PDV);
        this.pcid = get();
        this.pdvmch = get();
        Logger.trace("{} >> PDV[len={}, pcid={}, mch={}]",
                as, pdvlen, pcid, pdvmch);
        if ((pdvmch & PDVType.COMMAND) != expectedPDVType)
            abort(AAbort.UNEXPECTED_PDU_PARAMETER, UNEXPECTED_PDV_TYPE);
        if (expectedPCID != -1 && pcid != expectedPCID)
            abort(AAbort.UNEXPECTED_PDU_PARAMETER, UNEXPECTED_PDV_PCID);
    }

    private boolean isLastPDV() throws IOException {
        while (pos == pdvend) {
            if ((pdvmch & PDVType.LAST) != 0)
                return true;
            nextPDV(pdvmch & PDVType.COMMAND, pcid);
        }
        return false;
    }

    public boolean isPendingPDV() {
        return pcid != -1 && (pdvmch & PDVType.LAST) == 0;
    }

    @Override
    public int read() throws IOException {
        if (th != Thread.currentThread())
            throw new IllegalStateException("Entered by wrong thread");
        if (isLastPDV())
            return -1;

        return get();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (th != Thread.currentThread())
            throw new IllegalStateException("Entered by wrong thread");
        if (isLastPDV())
            return -1;

        int read = Math.min(len, pdvend - pos);
        get(b, off, read);
        return read;
    }

    @Override
    public final int available() {
        return pdvend - pos;
    }

    @Override
    public long skip(long n) throws IOException {
        if (th != Thread.currentThread())
            throw new IllegalStateException("Entered by wrong thread");
        if (n <= 0 || isLastPDV())
            return 0;

        int skipped = (int) Math.min(n, pdvend - pos);
        skip(skipped);
        return skipped;
    }

    @Override
    public void close() throws IOException {
        if (th != Thread.currentThread())
            throw new IllegalStateException("Entered by wrong thread");
        skipAll();
    }

    @Override
    public long skipAll() throws IOException {
        if (th != Thread.currentThread())
            throw new IllegalStateException("Entered by wrong thread");
        long n = 0;
        while (!isLastPDV()) {
            n += pdvend - pos;
            pos = pdvend;
        }
        return n;
    }

    @Override
    public void copyTo(OutputStream out, int length) throws IOException {
        if (th != Thread.currentThread())
            throw new IllegalStateException("Entered by wrong thread");
        int remaining = length;
        while (remaining > 0) {
            if (isLastPDV())
                throw new EOFException("remaining: " + remaining);
            int read = Math.min(remaining, pdvend - pos);
            out.write(buf, pos, read);
            remaining -= read;
            pos += read;
        }
    }

    @Override
    public void copyTo(OutputStream out) throws IOException {
        if (th != Thread.currentThread())
            throw new IllegalStateException("Entered by wrong thread");
        while (!isLastPDV()) {
            out.write(buf, pos, pdvend - pos);
            pos = pdvend;
        }
    }

}
