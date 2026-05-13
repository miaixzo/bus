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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

/**
 * Represents the PDUDecoder type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class PDUDecoder extends PDVInputStream {

    /**
     * The unrecognized pdu value.
     */
    private static final String UNRECOGNIZED_PDU = "{}: unrecognized PDU[type={}, len={}]";

    /**
     * The invalid pdu length value.
     */
    private static final String INVALID_PDU_LENGTH = "{}: invalid length of PDU[type={}, len={}]";

    /**
     * The invalid common extended negotiation value.
     */
    private static final String INVALID_COMMON_EXTENDED_NEGOTIATION = "{}: invalid Common Extended Negotiation sub-item in PDU[type={}, len={}]";

    /**
     * The invalid user identity value.
     */
    private static final String INVALID_USER_IDENTITY = "{}: invalid User Identity sub-item in PDU[type={}, len={}]";

    /**
     * The invalid pdv value.
     */
    private static final String INVALID_PDV = "{}: invalid PDV in PDU[type={}, len={}]";

    /**
     * The unexpected pdv type value.
     */
    private static final String UNEXPECTED_PDV_TYPE = "{}: unexpected PDV type in PDU[type={}, len={}]";

    /**
     * The unexpected pdv pcid value.
     */
    private static final String UNEXPECTED_PDV_PCID = "{}: unexpected pcid in PDV in PDU[type={}, len={}]";

    /**
     * 16MiB
     */
    private static final int MAX_PDU_LEN = 0x1000000;

    /**
     * The as value.
     */
    private final Association as;

    /**
     * The in value.
     */
    private final InputStream in;

    /**
     * The th value.
     */
    private final Thread th;

    /**
     * The buf value.
     */
    private byte[] buf = new byte[6 + Connection.DEF_MAX_PDU_LENGTH];

    /**
     * The pos value.
     */
    private int pos;

    /**
     * The pdutype value.
     */
    private int pdutype;

    /**
     * The pdulen value.
     */
    private int pdulen;

    /**
     * The pcid value.
     */
    private int pcid = -1;

    /**
     * The pdvmch value.
     */
    private int pdvmch;

    /**
     * The pdvend value.
     */
    private int pdvend;

    /**
     * Creates a new instance.
     *
     * @param as the as.
     * @param in the in.
     */
    public PDUDecoder(Association as, InputStream in) {
        this.as = as;
        this.in = in;
        this.th = Thread.currentThread();
    }

    /**
     * Executes the remaining operation.
     *
     * @return the operation result.
     */
    private int remaining() {
        return pdulen + 6 - pos;
    }

    /**
     * Determines whether remaining.
     *
     * @return true if the condition is met; otherwise false.
     */
    private boolean hasRemaining() {
        return pos < pdulen + 6;
    }

    /**
     * Executes the get operation.
     *
     * @return the operation result.
     */
    private int get() {
        if (!hasRemaining())
            throw new IndexOutOfBoundsException();
        return buf[pos++] & 0xFF;
    }

    /**
     * Executes the get operation.
     *
     * @param b   the b.
     * @param off the off.
     * @param len the len.
     */
    private void get(byte[] b, int off, int len) {
        if (len > remaining())
            throw new IndexOutOfBoundsException();
        System.arraycopy(buf, pos, b, off, len);
        pos += len;
    }

    /**
     * Executes the skip operation.
     *
     * @param len the len.
     */
    private void skip(int len) {
        if (len > remaining())
            throw new IndexOutOfBoundsException();
        pos += len;
    }

    /**
     * Gets the unsigned short.
     *
     * @return the unsigned short.
     */
    private int getUnsignedShort() {
        int val = ByteKit.bytesToUShortBE(buf, pos);
        pos += 2;
        return val;
    }

    /**
     * Gets the int.
     *
     * @return the int.
     */
    private int getInt() {
        int val = ByteKit.bytesToIntBE(buf, pos);
        pos += 4;
        return val;
    }

    /**
     * Gets the bytes.
     *
     * @param len the len.
     * @return the bytes.
     */
    private byte[] getBytes(int len) {
        byte[] bs = new byte[len];
        get(bs, 0, len);
        return bs;
    }

    /**
     * Executes the decode bytes operation.
     *
     * @return the operation result.
     */
    private byte[] decodeBytes() {
        return getBytes(getUnsignedShort());
    }

    /**
     * Executes the next pdu operation.
     *
     * @throws IOException if the operation cannot be completed.
     */
    public void nextPDU() throws IOException {
        checkThread();
        Logger.trace(false, "Image", "PDU wait started: protocol=pdu, association={}", as);
        readFully(0, 10);
        pos = 0;
        pdutype = get();
        get();
        pdulen = getInt();
        Logger.trace(
                false,
                "Image",
                "PDU received: protocol=pdu, association={}, type={}, length={}",
                as,
                pdutype,
                pdulen & 0xFFFFFFFFL);
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

    /**
     * Executes the check thread operation.
     */
    private void checkThread() {
        if (th != Thread.currentThread())
            throw new IllegalStateException("Entered by wrong thread");
    }

    /**
     * Executes the check pdu length operation.
     *
     * @param len the len.
     * @throws AAbort if the operation cannot be completed.
     */
    private void checkPDULength(int len) throws AAbort {
        if (pdulen != len)
            abort(AAbort.INVALID_PDU_PARAMETER_VALUE, INVALID_PDU_LENGTH);
    }

    /**
     * Reads the pdu.
     *
     * @throws IOException if the operation cannot be completed.
     */
    private void readPDU() throws IOException {
        if (pdulen < 4 || pdulen > MAX_PDU_LEN)
            abort(AAbort.INVALID_PDU_PARAMETER_VALUE, INVALID_PDU_LENGTH);

        if (6 + pdulen > buf.length)
            buf = Arrays.copyOf(buf, 6 + pdulen);

        readFully(10, pdulen - 4);
    }

    /**
     * Reads the fully.
     *
     * @param off the off.
     * @param len the len.
     * @throws IOException if the operation cannot be completed.
     */
    private void readFully(int off, int len) throws IOException {
        try {
            StreamKit.readFully(in, buf, off, len);
        } catch (IOException e) {
            Logger.warn(
                    false,
                    "Image",
                    e,
                    "PDU read failed: association={}, offset={}, length={}, pduType={}, pduLength={}, exception={}",
                    as,
                    off,
                    len,
                    pdutype,
                    pdulen & 0xFFFFFFFFL,
                    e.getClass().getSimpleName());
            throw e;
        }
    }

    /**
     * Executes the abort operation.
     *
     * @param reason the reason.
     * @param logmsg the logmsg.
     * @throws AAbort if the operation cannot be completed.
     */
    private void abort(int reason, String logmsg) throws AAbort {
        Logger.warn(
                false,
                "Image",
                "PDU validation failed: protocol=pdu, association={}, pduType={}, pduLength={}, reasonCode={}, rule={}",
                as,
                pdutype,
                pdulen & 0xFFFFFFFFL,
                reason,
                logmsg);
        throw new AAbort(AAbort.UL_SERIVE_PROVIDER, reason);
    }

    /**
     * Gets the string.
     *
     * @param len the len.
     * @return the string.
     */
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

    /**
     * Executes the decode string operation.
     *
     * @return the operation result.
     */
    private String decodeString() {
        return getString(getUnsignedShort());
    }

    /**
     * Executes the decode operation.
     *
     * @param rqac the rqac.
     * @return the operation result.
     * @throws AAbort if the operation cannot be completed.
     */
    private AAssociateRQAC decode(AAssociateRQAC rqac) throws AAbort {
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

    /**
     * Executes the decode item operation.
     *
     * @param rqac the rqac.
     * @throws AAbort if the operation cannot be completed.
     */
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

    /**
     * Executes the decode pc operation.
     *
     * @param itemLen the item len.
     * @return the operation result.
     */
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
            get(); // Skip reserved bytes
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
        return new PresentationContext(pcid, result, as, tss.toArray(new String[tss.size()]));
    }

    /**
     * Executes the decode user info operation.
     *
     * @param itemLength the item length.
     * @param rqac       the rqac.
     * @throws AAbort if the operation cannot be completed.
     */
    private void decodeUserInfo(int itemLength, AAssociateRQAC rqac) throws AAbort {
        int endpos = pos + itemLength;
        while (pos < endpos)
            decodeUserInfoSubItem(rqac);
    }

    /**
     * Executes the decode user info sub item operation.
     *
     * @param rqac the rqac.
     * @throws AAbort if the operation cannot be completed.
     */
    private void decodeUserInfoSubItem(AAssociateRQAC rqac) throws AAbort {
        int itemType = get();
        get(); // DICOM
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

    /**
     * Executes the decode role selection operation.
     *
     * @param itemLen the item len.
     * @return the operation result.
     */
    private RoleSelection decodeRoleSelection(int itemLen) {
        String cuid = decodeString();
        boolean scu = get() != 0;
        boolean scp = get() != 0;
        return new RoleSelection(cuid, scu, scp);
    }

    /**
     * Executes the decode ext neg operation.
     *
     * @param itemLen the item len.
     * @return the operation result.
     */
    private ExtendedNegotiation decodeExtNeg(int itemLen) {
        int uidLength = getUnsignedShort();
        String cuid = getString(uidLength);
        byte[] info = getBytes(itemLen - uidLength - 2);
        return new ExtendedNegotiation(cuid, info);
    }

    /**
     * Executes the decode common ext neg operation.
     *
     * @param itemLen the item len.
     * @return the operation result.
     * @throws AAbort if the operation cannot be completed.
     */
    private CommonExtended decodeCommonExtNeg(int itemLen) throws AAbort {
        int endPos = pos + itemLen;
        String sopCUID = getString(getUnsignedShort());
        String serviceCUID = getString(getUnsignedShort());
        List<String> relSopCUIDs = new ArrayList<>(1);
        int relSopCUIDsLen = getUnsignedShort();
        int endRelSopCUIDs = pos + relSopCUIDsLen;
        while (pos < endRelSopCUIDs)
            relSopCUIDs.add(decodeString());
        if (pos != endRelSopCUIDs || pos > endPos)
            abort(AAbort.INVALID_PDU_PARAMETER_VALUE, INVALID_COMMON_EXTENDED_NEGOTIATION);
        skip(endPos - pos);
        return new CommonExtended(sopCUID, serviceCUID, relSopCUIDs.toArray(new String[relSopCUIDs.size()]));
    }

    /**
     * Executes the decode user identity rq operation.
     *
     * @param itemLen the item len.
     * @return the operation result.
     * @throws AAbort if the operation cannot be completed.
     */
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

    /**
     * Executes the decode user identity ac operation.
     *
     * @param itemLen the item len.
     * @return the operation result.
     * @throws AAbort if the operation cannot be completed.
     */
    private IdentityAC decodeUserIdentityAC(int itemLen) throws AAbort {
        int endPos = pos + itemLen;
        byte[] serverResponse = decodeBytes();
        if (pos != endPos)
            abort(AAbort.INVALID_PDU_PARAMETER_VALUE, INVALID_USER_IDENTITY);
        return new IdentityAC(serverResponse);
    }

    /**
     * Executes the decode dimse operation.
     *
     * @throws IOException if the operation cannot be completed.
     */
    public void decodeDIMSE() throws IOException {
        checkThread();
        if (pcid != -1)
            return; // Already in DIMSE decoder

        nextPDV(PDVType.COMMAND, -1);

        PresentationContext pc = as.getPresentationContext(pcid);
        if (pc == null) {
            Logger.warn(
                    false,
                    "Image",
                    "Presentation context missing: protocol=pdu, association={}, pcid={}",
                    as,
                    pcid);
            throw new AAbort();
        }

        if (!pc.isAccepted()) {
            Logger.warn(
                    false,
                    "Image",
                    "Accepted presentation context missing: protocol=pdu, association={}, pcid={}",
                    as,
                    pcid);
            throw new AAbort();
        }

        Attributes cmd = readCommand();
        Dimse dimse = dimseOf(cmd);
        String tsuid = pc.getTransferSyntax();
        if (Logger.isInfoEnabled()) {
            Logger.info(
                    false,
                    "Image",
                    "DIMSE message received: protocol=pdu, association={}, dimse={}",
                    as,
                    dimse.toString(cmd, pcid, tsuid));
            if (Logger.isDebugEnabled()) {
                Logger.debug(
                        false,
                        "Image",
                        "DIMSE command received: protocol=pdu, association={}, dimse={}, commandAttributes={}, pcid={}, transferSyntax={}",
                        as,
                        dimse.toString(cmd),
                        cmd == null ? 0 : cmd.size(),
                        pcid,
                        tsuid);
            }
        }
        if (dimse == Dimse.C_CANCEL_RQ) {
            as.onCancelRQ(cmd);
        } else if (Commands.hasDataset(cmd)) {
            nextPDV(PDVType.DATA, pcid);
            if (dimse.isRSP()) {
                Attributes data = readDataset(tsuid);
                if (Logger.isDebugEnabled()) {
                    Logger.debug(
                            false,
                            "Image",
                            "DIMSE dataset received: protocol=pdu, association={}, dimse={}, datasetAttributes={}, transferSyntax={}",
                            as,
                            dimse.toString(cmd),
                            data == null ? 0 : data.size(),
                            tsuid);
                }
                as.onDimseRSP(dimse, cmd, data);
            } else {
                if (Logger.isDebugEnabled()) {
                    Logger.debug(
                            false,
                            "Image",
                            "DIMSE dataset receiving: protocol=pdu, association={}, dimse={}",
                            as,
                            dimse.toString(cmd));
                }
                as.onDimseRQ(pc, dimse, cmd, this);
                long skipped = skipAll();
                if (skipped > 0)
                    Logger.debug(
                            false,
                            "Image",
                            "DIMSE data left unread: protocol=pdu, association={}, bytes={}",
                            as,
                            skipped);
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

    /**
     * Executes the dimse of operation.
     *
     * @param cmd the cmd.
     * @return the operation result.
     * @throws AAbort if the operation cannot be completed.
     */
    private Dimse dimseOf(Attributes cmd) throws AAbort {
        try {
            return Dimse.valueOf(cmd.getInt(Tag.CommandField, 0));
        } catch (IllegalArgumentException e) {
            Logger.info(
                    false,
                    "Image",
                    "Illegal DIMSE received: protocol=pdu, association={}, commandAttributes={}",
                    as,
                    cmd == null ? 0 : cmd.size());
            throw new AAbort();
        }
    }

    /**
     * Reads the command.
     *
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    private Attributes readCommand() throws IOException {
        ImageInputStream in = new ImageInputStream(this, UID.ImplicitVRLittleEndian.uid);
        try {
            return in.readCommand();
        } finally {
            IoKit.close(in);
        }
    }

    /**
     * Reads the dataset.
     *
     * @param tsuid the tsuid.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public Attributes readDataset(String tsuid) throws IOException {
        ImageInputStream in = new ImageInputStream(this, tsuid);
        try {
            return in.readDataset();
        } finally {
            IoKit.close(in);
        }
    }

    /**
     * Executes the next pdv operation.
     *
     * @param expectedPDVType the expected pdv type.
     * @param expectedPCID    the expected pcid.
     * @throws IOException if the operation cannot be completed.
     */
    private void nextPDV(int expectedPDVType, int expectedPCID) throws IOException {
        if (!hasRemaining()) {
            nextPDU();
            if (pdutype != PDUType.P_DATA_TF) {
                Logger.info(
                        false,
                        "Image",
                        "Unexpected PDU type received: protocol=pdu, association={}, pduType={}",
                        as,
                        pdutype);
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
        Logger.trace(
                false,
                "Image",
                "PDV received: protocol=pdu, association={}, length={}, pcid={}, messageControlHeader={}",
                as,
                pdvlen,
                pcid,
                pdvmch);
        if ((pdvmch & PDVType.COMMAND) != expectedPDVType)
            abort(AAbort.UNEXPECTED_PDU_PARAMETER, UNEXPECTED_PDV_TYPE);
        if (expectedPCID != -1 && pcid != expectedPCID)
            abort(AAbort.UNEXPECTED_PDU_PARAMETER, UNEXPECTED_PDV_PCID);
    }

    /**
     * Determines whether last pdv.
     *
     * @return true if the condition is met; otherwise false.
     * @throws IOException if the operation cannot be completed.
     */
    private boolean isLastPDV() throws IOException {
        while (pos == pdvend) {
            if ((pdvmch & PDVType.LAST) != 0)
                return true;
            nextPDV(pdvmch & PDVType.COMMAND, pcid);
        }
        return false;
    }

    /**
     * Determines whether pending pdv.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isPendingPDV() {
        return pcid != -1 && (pdvmch & PDVType.LAST) == 0;
    }

    /**
     * Executes the read operation.
     *
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public int read() throws IOException {
        if (th != Thread.currentThread())
            throw new IllegalStateException("Entered by wrong thread");
        if (isLastPDV())
            return -1;

        return get();
    }

    /**
     * Executes the read operation.
     *
     * @param b   the b.
     * @param off the off.
     * @param len the len.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
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

    /**
     * Executes the available operation.
     *
     * @return the operation result.
     */
    @Override
    public final int available() {
        return pdvend - pos;
    }

    /**
     * Executes the skip operation.
     *
     * @param n the n.
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
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

    /**
     * Executes the close operation.
     *
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public void close() throws IOException {
        if (th != Thread.currentThread())
            throw new IllegalStateException("Entered by wrong thread");
        skipAll();
    }

    /**
     * Executes the skip all operation.
     *
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
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

    /**
     * Copies the to.
     *
     * @param out    the out.
     * @param length the length.
     * @throws IOException if the operation cannot be completed.
     */
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

    /**
     * Copies the to.
     *
     * @param out the out.
     * @throws IOException if the operation cannot be completed.
     */
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
