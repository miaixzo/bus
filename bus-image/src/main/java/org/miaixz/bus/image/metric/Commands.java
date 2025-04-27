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
package org.miaixz.bus.image.metric;

import org.miaixz.bus.image.Dimse;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.VR;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class Commands {

    public static final int NO_DATASET = 0x0101;
    private static int withDatasetType = 0x0000;

    public static Attributes mkCStoreRQ(int msgId, String cuid, String iuid, int priority) {
        Attributes rq = mkRQ(msgId, 0x0001, withDatasetType);
        rq.setString(Tag.AffectedSOPClassUID, VR.UI, cuid);
        rq.setString(Tag.AffectedSOPInstanceUID, VR.UI, iuid);
        rq.setInt(Tag.Priority, VR.US, priority);
        return rq;
    }

    public static Attributes mkCStoreRQ(int msgId, String cuid, String iuid, int priority, String moveOriginatorAET,
            int moveOriginatorMsgId) {
        Attributes rq = mkCStoreRQ(msgId, cuid, iuid, priority);
        rq.setString(Tag.MoveOriginatorApplicationEntityTitle, VR.AE, moveOriginatorAET);
        rq.setInt(Tag.MoveOriginatorMessageID, VR.US, moveOriginatorMsgId);
        return rq;
    }

    public static Attributes mkCStoreRSP(Attributes cmd, int status) {
        return mkRSP(cmd, status, Dimse.C_STORE_RQ);
    }

    public static Attributes mkCFindRQ(int msgId, String cuid, int priority) {
        Attributes rq = mkRQ(msgId, 0x0020, withDatasetType);
        rq.setString(Tag.AffectedSOPClassUID, VR.UI, cuid);
        rq.setInt(Tag.Priority, VR.US, priority);
        return rq;
    }

    public static Attributes mkCFindRSP(Attributes cmd, int status) {
        return mkRSP(cmd, status, Dimse.C_FIND_RQ);
    }

    public static Attributes mkCGetRQ(int msgId, String cuid, int priority) {
        Attributes rq = mkRQ(msgId, 0x0010, withDatasetType);
        rq.setString(Tag.AffectedSOPClassUID, VR.UI, cuid);
        rq.setInt(Tag.Priority, VR.US, priority);
        return rq;
    }

    public static Attributes mkCGetRSP(Attributes cmd, int status) {
        return mkRSP(cmd, status, Dimse.C_GET_RQ);
    }

    public static Attributes mkCMoveRQ(int msgId, String cuid, int priority, String destination) {
        Attributes rq = mkRQ(msgId, 0x0021, withDatasetType);
        rq.setString(Tag.AffectedSOPClassUID, VR.UI, cuid);
        rq.setInt(Tag.Priority, VR.US, priority);
        rq.setString(Tag.MoveDestination, VR.AE, destination);
        return rq;
    }

    public static Attributes mkCMoveRSP(Attributes cmd, int status) {
        return mkRSP(cmd, status, Dimse.C_MOVE_RQ);
    }

    public static Attributes mkCCancelRQ(int msgId) {
        Attributes rq = new Attributes();
        rq.setInt(Tag.CommandField, VR.US, Dimse.C_CANCEL_RQ.commandField());
        rq.setInt(Tag.CommandDataSetType, VR.US, NO_DATASET);
        rq.setInt(Tag.MessageIDBeingRespondedTo, VR.US, msgId);
        return rq;
    }

    public static Attributes mkCEchoRQ(int msgId, String cuid) {
        Attributes rq = mkRQ(msgId, 0x0030, NO_DATASET);
        rq.setString(Tag.AffectedSOPClassUID, VR.UI, cuid);
        return rq;
    }

    public static Attributes mkEchoRSP(Attributes cmd, int status) {
        return mkRSP(cmd, status, Dimse.C_ECHO_RQ);
    }

    public static Attributes mkNEventReportRQ(int msgId, String cuid, String iuid, int eventTypeID, Attributes data) {
        Attributes rq = mkRQ(msgId, 0x0100, data == null ? NO_DATASET : withDatasetType);
        rq.setString(Tag.AffectedSOPClassUID, VR.UI, cuid);
        rq.setString(Tag.AffectedSOPInstanceUID, VR.UI, iuid);
        rq.setInt(Tag.EventTypeID, VR.US, eventTypeID);
        return rq;
    }

    public static Attributes mkNEventReportRSP(Attributes cmd, int status) {
        return mkRSP(cmd, status, Dimse.N_EVENT_REPORT_RQ);
    }

    public static Attributes mkNGetRQ(int msgId, String cuid, String iuid, int[] tags) {
        Attributes rq = mkRQ(msgId, 0x0110, NO_DATASET);
        rq.setString(Tag.RequestedSOPClassUID, VR.UI, cuid);
        rq.setString(Tag.RequestedSOPInstanceUID, VR.UI, iuid);
        if (tags != null)
            rq.setInt(Tag.AttributeIdentifierList, VR.AT, tags);
        return rq;
    }

    public static Attributes mkNGetRSP(Attributes cmd, int status) {
        return mkRSP(cmd, status, Dimse.N_GET_RQ);
    }

    public static Attributes mkNSetRQ(int msgId, String cuid, String iuid) {
        Attributes rq = mkRQ(msgId, 0x0120, withDatasetType);
        rq.setString(Tag.RequestedSOPClassUID, VR.UI, cuid);
        rq.setString(Tag.RequestedSOPInstanceUID, VR.UI, iuid);
        return rq;
    }

    public static Attributes mkNSetRSP(Attributes cmd, int status) {
        return mkRSP(cmd, status, Dimse.N_SET_RQ);
    }

    public static Attributes mkNActionRQ(int msgId, String cuid, String iuid, int actionTypeID, Attributes data) {
        Attributes rq = mkRQ(msgId, 0x0130, data == null ? NO_DATASET : withDatasetType);
        rq.setString(Tag.RequestedSOPClassUID, VR.UI, cuid);
        rq.setString(Tag.RequestedSOPInstanceUID, VR.UI, iuid);
        rq.setInt(Tag.ActionTypeID, VR.US, actionTypeID);
        return rq;
    }

    public static Attributes mkNActionRSP(Attributes cmd, int status) {
        return mkRSP(cmd, status, Dimse.N_ACTION_RQ);
    }

    public static Attributes mkNCreateRQ(int msgId, String cuid, String iuid) {
        Attributes rq = mkRQ(msgId, 0x0140, withDatasetType);
        rq.setString(Tag.AffectedSOPClassUID, VR.UI, cuid);
        if (iuid != null)
            rq.setString(Tag.AffectedSOPInstanceUID, VR.UI, iuid);
        return rq;
    }

    public static Attributes mkNCreateRSP(Attributes cmd, int status) {
        String iuid = cmd.getString(Tag.AffectedSOPInstanceUID);
        if (iuid == null)
            cmd.setString(Tag.AffectedSOPInstanceUID, VR.UI, UID.createUID());
        return mkRSP(cmd, status, Dimse.N_CREATE_RQ);
    }

    public static Attributes mkNDeleteRQ(int msgId, String cuid, String iuid) {
        Attributes rq = mkRQ(msgId, 0x0150, NO_DATASET);
        rq.setString(Tag.RequestedSOPClassUID, VR.UI, cuid);
        rq.setString(Tag.RequestedSOPInstanceUID, VR.UI, iuid);
        return rq;
    }

    public static Attributes mkNDeleteRSP(Attributes cmd, int status) {
        return mkRSP(cmd, status, Dimse.N_DELETE_RQ);
    }

    private static Attributes mkRQ(int msgId, int cmdField, int datasetType) {
        Attributes rsp = new Attributes();
        rsp.setInt(Tag.MessageID, VR.US, msgId);
        rsp.setInt(Tag.CommandField, VR.US, cmdField);
        rsp.setInt(Tag.CommandDataSetType, VR.US, datasetType);
        return rsp;
    }

    public static Attributes mkRSP(Attributes rq, int status, Dimse rqCmd) {
        Attributes rsp = new Attributes();
        rsp.setInt(Tag.CommandField, VR.US, rqCmd.commandFieldOfRSP());
        rsp.setInt(Tag.Status, VR.US, status);
        rsp.setInt(Tag.MessageIDBeingRespondedTo, VR.US, rq.getInt(Tag.MessageID, 0));
        rsp.setString(Tag.AffectedSOPClassUID, VR.UI, rq.getString(rqCmd.tagOfSOPClassUID()));
        int tagOfIUID = rqCmd.tagOfSOPInstanceUID();
        if (tagOfIUID != 0)
            rsp.setString(Tag.AffectedSOPInstanceUID, VR.UI, rq.getString(tagOfIUID));
        return rsp;
    }

    public static void initNumberOfSuboperations(Attributes rsp, int remaining) {
        rsp.setInt(Tag.NumberOfRemainingSuboperations, VR.US, remaining);
        rsp.setInt(Tag.NumberOfCompletedSuboperations, VR.US, 0);
        rsp.setInt(Tag.NumberOfFailedSuboperations, VR.US, 0);
        rsp.setInt(Tag.NumberOfWarningSuboperations, VR.US, 0);
    }

    public static void incNumberOfSuboperations(int tag, Attributes rsp) {
        synchronized (rsp) {
            rsp.setInt(tag, VR.US, rsp.getInt(tag, 0) + 1);
            rsp.setInt(Tag.NumberOfRemainingSuboperations, VR.US,
                    rsp.getInt(Tag.NumberOfRemainingSuboperations, 1) - 1);
        }
    }

    public static int getWithDatasetType() {
        return withDatasetType;
    }

    public static void setWithDatasetType(int withDatasetType) {
        if (withDatasetType == NO_DATASET || (withDatasetType & 0xffff0000) != 0)
            throw new IllegalArgumentException("withDatasetType: " + Integer.toHexString(withDatasetType) + "H");
        Commands.withDatasetType = withDatasetType;
    }

    public static boolean hasDataset(Attributes cmd) {
        return cmd.getInt(Tag.CommandDataSetType, 0) != NO_DATASET;
    }

}
