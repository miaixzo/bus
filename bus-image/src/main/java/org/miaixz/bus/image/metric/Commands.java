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

import org.miaixz.bus.image.Dimse;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.VR;

/**
 * Represents the Commands type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Commands {

    /**
     * Constructs a new Commands instance.
     */
    public Commands() {
        // No initialization required.
    }

    /**
     * The no dataset value.
     */
    public static final int NO_DATASET = 0x0101;

    /**
     * The with dataset type value.
     */
    private static int withDatasetType = 0x0000;

    /**
     * Executes the mk c store rq operation.
     *
     * @param msgId    the msg id.
     * @param cuid     the cuid.
     * @param iuid     the iuid.
     * @param priority the priority.
     * @return the operation result.
     */
    public static Attributes mkCStoreRQ(int msgId, String cuid, String iuid, int priority) {
        Attributes rq = mkRQ(msgId, 0x0001, withDatasetType);
        rq.setString(Tag.AffectedSOPClassUID, VR.UI, cuid);
        rq.setString(Tag.AffectedSOPInstanceUID, VR.UI, iuid);
        rq.setInt(Tag.Priority, VR.US, priority);
        return rq;
    }

    /**
     * Executes the mk c store rq operation.
     *
     * @param msgId               the msg id.
     * @param cuid                the cuid.
     * @param iuid                the iuid.
     * @param priority            the priority.
     * @param moveOriginatorAET   the move originator aet.
     * @param moveOriginatorMsgId the move originator msg id.
     * @return the operation result.
     */
    public static Attributes mkCStoreRQ(
            int msgId,
            String cuid,
            String iuid,
            int priority,
            String moveOriginatorAET,
            int moveOriginatorMsgId) {
        Attributes rq = mkCStoreRQ(msgId, cuid, iuid, priority);
        rq.setString(Tag.MoveOriginatorApplicationEntityTitle, VR.AE, moveOriginatorAET);
        rq.setInt(Tag.MoveOriginatorMessageID, VR.US, moveOriginatorMsgId);
        return rq;
    }

    /**
     * Executes the mk c store rsp operation.
     *
     * @param cmd    the cmd.
     * @param status the status.
     * @return the operation result.
     */
    public static Attributes mkCStoreRSP(Attributes cmd, int status) {
        return mkRSP(cmd, status, Dimse.C_STORE_RQ);
    }

    /**
     * Executes the mk c find rq operation.
     *
     * @param msgId    the msg id.
     * @param cuid     the cuid.
     * @param priority the priority.
     * @return the operation result.
     */
    public static Attributes mkCFindRQ(int msgId, String cuid, int priority) {
        Attributes rq = mkRQ(msgId, 0x0020, withDatasetType);
        rq.setString(Tag.AffectedSOPClassUID, VR.UI, cuid);
        rq.setInt(Tag.Priority, VR.US, priority);
        return rq;
    }

    /**
     * Executes the mk c find rsp operation.
     *
     * @param cmd    the cmd.
     * @param status the status.
     * @return the operation result.
     */
    public static Attributes mkCFindRSP(Attributes cmd, int status) {
        return mkRSP(cmd, status, Dimse.C_FIND_RQ);
    }

    /**
     * Executes the mk c get rq operation.
     *
     * @param msgId    the msg id.
     * @param cuid     the cuid.
     * @param priority the priority.
     * @return the operation result.
     */
    public static Attributes mkCGetRQ(int msgId, String cuid, int priority) {
        Attributes rq = mkRQ(msgId, 0x0010, withDatasetType);
        rq.setString(Tag.AffectedSOPClassUID, VR.UI, cuid);
        rq.setInt(Tag.Priority, VR.US, priority);
        return rq;
    }

    /**
     * Executes the mk c get rsp operation.
     *
     * @param cmd    the cmd.
     * @param status the status.
     * @return the operation result.
     */
    public static Attributes mkCGetRSP(Attributes cmd, int status) {
        return mkRSP(cmd, status, Dimse.C_GET_RQ);
    }

    /**
     * Executes the mk c move rq operation.
     *
     * @param msgId       the msg id.
     * @param cuid        the cuid.
     * @param priority    the priority.
     * @param destination the destination.
     * @return the operation result.
     */
    public static Attributes mkCMoveRQ(int msgId, String cuid, int priority, String destination) {
        Attributes rq = mkRQ(msgId, 0x0021, withDatasetType);
        rq.setString(Tag.AffectedSOPClassUID, VR.UI, cuid);
        rq.setInt(Tag.Priority, VR.US, priority);
        rq.setString(Tag.MoveDestination, VR.AE, destination);
        return rq;
    }

    /**
     * Executes the mk c move rsp operation.
     *
     * @param cmd    the cmd.
     * @param status the status.
     * @return the operation result.
     */
    public static Attributes mkCMoveRSP(Attributes cmd, int status) {
        return mkRSP(cmd, status, Dimse.C_MOVE_RQ);
    }

    /**
     * Executes the mk c cancel rq operation.
     *
     * @param msgId the msg id.
     * @return the operation result.
     */
    public static Attributes mkCCancelRQ(int msgId) {
        Attributes rq = new Attributes();
        rq.setInt(Tag.CommandField, VR.US, Dimse.C_CANCEL_RQ.commandField());
        rq.setInt(Tag.CommandDataSetType, VR.US, NO_DATASET);
        rq.setInt(Tag.MessageIDBeingRespondedTo, VR.US, msgId);
        return rq;
    }

    /**
     * Executes the mk c echo rq operation.
     *
     * @param msgId the msg id.
     * @param cuid  the cuid.
     * @return the operation result.
     */
    public static Attributes mkCEchoRQ(int msgId, String cuid) {
        Attributes rq = mkRQ(msgId, 0x0030, NO_DATASET);
        rq.setString(Tag.AffectedSOPClassUID, VR.UI, cuid);
        return rq;
    }

    /**
     * Executes the mk echo rsp operation.
     *
     * @param cmd    the cmd.
     * @param status the status.
     * @return the operation result.
     */
    public static Attributes mkEchoRSP(Attributes cmd, int status) {
        return mkRSP(cmd, status, Dimse.C_ECHO_RQ);
    }

    /**
     * Executes the mk n event report rq operation.
     *
     * @param msgId       the msg id.
     * @param cuid        the cuid.
     * @param iuid        the iuid.
     * @param eventTypeID the event type id.
     * @param data        the data.
     * @return the operation result.
     */
    public static Attributes mkNEventReportRQ(int msgId, String cuid, String iuid, int eventTypeID, Attributes data) {
        Attributes rq = mkRQ(msgId, 0x0100, data == null ? NO_DATASET : withDatasetType);
        rq.setString(Tag.AffectedSOPClassUID, VR.UI, cuid);
        rq.setString(Tag.AffectedSOPInstanceUID, VR.UI, iuid);
        rq.setInt(Tag.EventTypeID, VR.US, eventTypeID);
        return rq;
    }

    /**
     * Executes the mk n event report rsp operation.
     *
     * @param cmd    the cmd.
     * @param status the status.
     * @return the operation result.
     */
    public static Attributes mkNEventReportRSP(Attributes cmd, int status) {
        return mkRSP(cmd, status, Dimse.N_EVENT_REPORT_RQ);
    }

    /**
     * Executes the mk n get rq operation.
     *
     * @param msgId the msg id.
     * @param cuid  the cuid.
     * @param iuid  the iuid.
     * @param tags  the tags.
     * @return the operation result.
     */
    public static Attributes mkNGetRQ(int msgId, String cuid, String iuid, int[] tags) {
        Attributes rq = mkRQ(msgId, 0x0110, NO_DATASET);
        rq.setString(Tag.RequestedSOPClassUID, VR.UI, cuid);
        rq.setString(Tag.RequestedSOPInstanceUID, VR.UI, iuid);
        if (tags != null)
            rq.setInt(Tag.AttributeIdentifierList, VR.AT, tags);
        return rq;
    }

    /**
     * Executes the mk n get rsp operation.
     *
     * @param cmd    the cmd.
     * @param status the status.
     * @return the operation result.
     */
    public static Attributes mkNGetRSP(Attributes cmd, int status) {
        return mkRSP(cmd, status, Dimse.N_GET_RQ);
    }

    /**
     * Executes the mk n set rq operation.
     *
     * @param msgId the msg id.
     * @param cuid  the cuid.
     * @param iuid  the iuid.
     * @return the operation result.
     */
    public static Attributes mkNSetRQ(int msgId, String cuid, String iuid) {
        Attributes rq = mkRQ(msgId, 0x0120, withDatasetType);
        rq.setString(Tag.RequestedSOPClassUID, VR.UI, cuid);
        rq.setString(Tag.RequestedSOPInstanceUID, VR.UI, iuid);
        return rq;
    }

    /**
     * Executes the mk n set rsp operation.
     *
     * @param cmd    the cmd.
     * @param status the status.
     * @return the operation result.
     */
    public static Attributes mkNSetRSP(Attributes cmd, int status) {
        return mkRSP(cmd, status, Dimse.N_SET_RQ);
    }

    /**
     * Executes the mk n action rq operation.
     *
     * @param msgId        the msg id.
     * @param cuid         the cuid.
     * @param iuid         the iuid.
     * @param actionTypeID the action type id.
     * @param data         the data.
     * @return the operation result.
     */
    public static Attributes mkNActionRQ(int msgId, String cuid, String iuid, int actionTypeID, Attributes data) {
        Attributes rq = mkRQ(msgId, 0x0130, data == null ? NO_DATASET : withDatasetType);
        rq.setString(Tag.RequestedSOPClassUID, VR.UI, cuid);
        rq.setString(Tag.RequestedSOPInstanceUID, VR.UI, iuid);
        rq.setInt(Tag.ActionTypeID, VR.US, actionTypeID);
        return rq;
    }

    /**
     * Executes the mk n action rsp operation.
     *
     * @param cmd    the cmd.
     * @param status the status.
     * @return the operation result.
     */
    public static Attributes mkNActionRSP(Attributes cmd, int status) {
        return mkRSP(cmd, status, Dimse.N_ACTION_RQ);
    }

    /**
     * Executes the mk n create rq operation.
     *
     * @param msgId the msg id.
     * @param cuid  the cuid.
     * @param iuid  the iuid.
     * @return the operation result.
     */
    public static Attributes mkNCreateRQ(int msgId, String cuid, String iuid) {
        Attributes rq = mkRQ(msgId, 0x0140, withDatasetType);
        rq.setString(Tag.AffectedSOPClassUID, VR.UI, cuid);
        if (iuid != null)
            rq.setString(Tag.AffectedSOPInstanceUID, VR.UI, iuid);
        return rq;
    }

    /**
     * Executes the mk n create rsp operation.
     *
     * @param cmd    the cmd.
     * @param status the status.
     * @return the operation result.
     */
    public static Attributes mkNCreateRSP(Attributes cmd, int status) {
        String iuid = cmd.getString(Tag.AffectedSOPInstanceUID);
        if (iuid == null)
            cmd.setString(Tag.AffectedSOPInstanceUID, VR.UI, UID.createUID());
        return mkRSP(cmd, status, Dimse.N_CREATE_RQ);
    }

    /**
     * Executes the mk n delete rq operation.
     *
     * @param msgId the msg id.
     * @param cuid  the cuid.
     * @param iuid  the iuid.
     * @return the operation result.
     */
    public static Attributes mkNDeleteRQ(int msgId, String cuid, String iuid) {
        Attributes rq = mkRQ(msgId, 0x0150, NO_DATASET);
        rq.setString(Tag.RequestedSOPClassUID, VR.UI, cuid);
        rq.setString(Tag.RequestedSOPInstanceUID, VR.UI, iuid);
        return rq;
    }

    /**
     * Executes the mk n delete rsp operation.
     *
     * @param cmd    the cmd.
     * @param status the status.
     * @return the operation result.
     */
    public static Attributes mkNDeleteRSP(Attributes cmd, int status) {
        return mkRSP(cmd, status, Dimse.N_DELETE_RQ);
    }

    /**
     * Executes the mk rq operation.
     *
     * @param msgId       the msg id.
     * @param cmdField    the cmd field.
     * @param datasetType the dataset type.
     * @return the operation result.
     */
    private static Attributes mkRQ(int msgId, int cmdField, int datasetType) {
        Attributes rsp = new Attributes();
        rsp.setInt(Tag.MessageID, VR.US, msgId);
        rsp.setInt(Tag.CommandField, VR.US, cmdField);
        rsp.setInt(Tag.CommandDataSetType, VR.US, datasetType);
        return rsp;
    }

    /**
     * Executes the mk rsp operation.
     *
     * @param rq     the rq.
     * @param status the status.
     * @param rqCmd  the rq cmd.
     * @return the operation result.
     */
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

    /**
     * Executes the init number of suboperations operation.
     *
     * @param rsp       the rsp.
     * @param remaining the remaining.
     */
    public static void initNumberOfSuboperations(Attributes rsp, int remaining) {
        rsp.setInt(Tag.NumberOfRemainingSuboperations, VR.US, remaining);
        rsp.setInt(Tag.NumberOfCompletedSuboperations, VR.US, 0);
        rsp.setInt(Tag.NumberOfFailedSuboperations, VR.US, 0);
        rsp.setInt(Tag.NumberOfWarningSuboperations, VR.US, 0);
    }

    /**
     * Executes the inc number of suboperations operation.
     *
     * @param tag the tag.
     * @param rsp the rsp.
     */
    public static void incNumberOfSuboperations(int tag, Attributes rsp) {
        synchronized (rsp) {
            rsp.setInt(tag, VR.US, rsp.getInt(tag, 0) + 1);
            rsp.setInt(
                    Tag.NumberOfRemainingSuboperations,
                    VR.US,
                    rsp.getInt(Tag.NumberOfRemainingSuboperations, 1) - 1);
        }
    }

    /**
     * Gets the with dataset type.
     *
     * @return the with dataset type.
     */
    public static int getWithDatasetType() {
        return withDatasetType;
    }

    /**
     * Sets the with dataset type.
     *
     * @param withDatasetType the with dataset type.
     */
    public static void setWithDatasetType(int withDatasetType) {
        if (withDatasetType == NO_DATASET || (withDatasetType & 0xffff0000) != 0)
            throw new IllegalArgumentException("withDatasetType: " + Integer.toHexString(withDatasetType) + "H");
        Commands.withDatasetType = withDatasetType;
    }

    /**
     * Determines whether dataset.
     *
     * @param cmd the cmd.
     * @return true if the condition is met; otherwise false.
     */
    public static boolean hasDataset(Attributes cmd) {
        return cmd.getInt(Tag.CommandDataSetType, 0) != NO_DATASET;
    }

}
