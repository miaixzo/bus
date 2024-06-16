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
package org.miaixz.bus.image;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.image.galaxy.Material;
import org.miaixz.bus.image.galaxy.data.Attributes;

/**
 * 复合DIMSE服务
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum Dimse {

    C_STORE_RQ(0x0001, Tag.AffectedSOPClassUID, Tag.AffectedSOPInstanceUID,
            Tag.MessageID, ":C-STORE-RQ"),
    C_STORE_RSP(0x8001, Tag.AffectedSOPClassUID, Tag.AffectedSOPInstanceUID,
            Tag.MessageIDBeingRespondedTo, ":C-STORE-RSP"),
    C_GET_RQ(0x0010, Tag.AffectedSOPClassUID, 0,
            Tag.MessageID, ":C-GET-RQ"),
    C_GET_RSP(0x8010, Tag.AffectedSOPClassUID, 0,
            Tag.MessageIDBeingRespondedTo, ":C-GET-RSP"),
    C_FIND_RQ(0x0020, Tag.AffectedSOPClassUID, 0,
            Tag.MessageID, ":C-FIND-RQ"),
    C_FIND_RSP(0x8020, Tag.AffectedSOPClassUID, 0,
            Tag.MessageIDBeingRespondedTo, ":C-FIND-RSP"),
    C_MOVE_RQ(0x0021, Tag.AffectedSOPClassUID, 0,
            Tag.MessageID, ":C-MOVE-RQ"),
    C_MOVE_RSP(0x8021, Tag.AffectedSOPClassUID, 0,
            Tag.MessageIDBeingRespondedTo, ":C-MOVE-RSP"),
    C_ECHO_RQ(0x0030, Tag.AffectedSOPClassUID, 0,
            Tag.MessageID, ":C-ECHO-RQ"),
    C_ECHO_RSP(0x8030, Tag.AffectedSOPClassUID, 0,
            Tag.MessageIDBeingRespondedTo, ":C-ECHO-RSP"),
    N_EVENT_REPORT_RQ(0x0100, Tag.AffectedSOPClassUID, Tag.AffectedSOPInstanceUID,
            Tag.MessageID, ":N-EVENT-REPORT-RQ"),
    N_EVENT_REPORT_RSP(0x8100, Tag.AffectedSOPClassUID, Tag.AffectedSOPInstanceUID,
            Tag.MessageIDBeingRespondedTo, ":N-EVENT-REPORT-RSP"),
    N_GET_RQ(0x0110, Tag.RequestedSOPClassUID, Tag.RequestedSOPInstanceUID,
            Tag.MessageID, ":N-GET-RQ"),
    N_GET_RSP(0x8110, Tag.AffectedSOPClassUID, Tag.AffectedSOPInstanceUID,
            Tag.MessageIDBeingRespondedTo, ":N-GET-RSP"),
    N_SET_RQ(0x0120, Tag.RequestedSOPClassUID, Tag.RequestedSOPInstanceUID,
            Tag.MessageID, ":N-SET-RQ"),
    N_SET_RSP(0x8120, Tag.AffectedSOPClassUID, Tag.AffectedSOPInstanceUID,
            Tag.MessageIDBeingRespondedTo, ":N-SET-RSP"),
    N_ACTION_RQ(0x0130, Tag.RequestedSOPClassUID, Tag.RequestedSOPInstanceUID,
            Tag.MessageID, ":N-ACTION-RQ"),
    N_ACTION_RSP(0x8130, Tag.AffectedSOPClassUID, Tag.AffectedSOPInstanceUID,
            Tag.MessageIDBeingRespondedTo, ":N-ACTION-RSP"),
    N_CREATE_RQ(0x0140, Tag.AffectedSOPClassUID, Tag.AffectedSOPInstanceUID,
            Tag.MessageID, ":N-CREATE-RQ"),
    N_CREATE_RSP(0x8140, Tag.AffectedSOPClassUID, Tag.AffectedSOPInstanceUID,
            Tag.MessageIDBeingRespondedTo, ":N-CREATE-RSP"),
    N_DELETE_RQ(0x0150, Tag.RequestedSOPClassUID, Tag.RequestedSOPInstanceUID,
            Tag.MessageID, ":N-DELETE-RQ"),
    N_DELETE_RSP(0x8150, Tag.AffectedSOPClassUID, Tag.AffectedSOPInstanceUID,
            Tag.MessageIDBeingRespondedTo, ":N-DELETE-RSP"),
    C_CANCEL_RQ(0x0FFF, 0, 0, Tag.MessageIDBeingRespondedTo, ":C-CANCEL-RQ");

    private final int commandField;
    private final int tagOfSOPClassUID;
    private final int tagOfSOPInstanceUID;
    private final int tagOfMessageID;
    private final String prompt;

    Dimse(int cmdField, int tagOfSOPClassUID, int tagOfSOPInstanceUID,
          int tagOfMessageID, String prompt) {
        this.commandField = cmdField;
        this.tagOfSOPClassUID = tagOfSOPClassUID;
        this.tagOfSOPInstanceUID = tagOfSOPInstanceUID;
        this.tagOfMessageID = tagOfMessageID;
        this.prompt = prompt;
    }

    public static Dimse valueOf(int commandField) {
        switch (commandField) {
            case 0x0001:
                return C_STORE_RQ;
            case 0x8001:
                return C_STORE_RSP;
            case 0x0010:
                return C_GET_RQ;
            case 0x8010:
                return C_GET_RSP;
            case 0x0020:
                return C_FIND_RQ;
            case 0x8020:
                return C_FIND_RSP;
            case 0x0021:
                return C_MOVE_RQ;
            case 0x8021:
                return C_MOVE_RSP;
            case 0x0030:
                return C_ECHO_RQ;
            case 0x8030:
                return C_ECHO_RSP;
            case 0x0100:
                return N_EVENT_REPORT_RQ;
            case 0x8100:
                return N_EVENT_REPORT_RSP;
            case 0x0110:
                return N_GET_RQ;
            case 0x8110:
                return N_GET_RSP;
            case 0x0120:
                return N_SET_RQ;
            case 0x8120:
                return N_SET_RSP;
            case 0x0130:
                return N_ACTION_RQ;
            case 0x8130:
                return N_ACTION_RSP;
            case 0x0140:
                return N_CREATE_RQ;
            case 0x8140:
                return N_CREATE_RSP;
            case 0x0150:
                return N_DELETE_RQ;
            case 0x8150:
                return N_DELETE_RSP;
            case 0x0FFF:
                return C_CANCEL_RQ;
            default:
                throw new IllegalArgumentException("commandField: " + commandField);
        }
    }

    private static void promptIntTo(Attributes cmd, String name, int tag,
                                    StringBuilder sb) {
        int val = cmd.getInt(tag, 0);
        if (val != 0 || cmd.containsValue(tag))
            sb.append(name).append(val);
    }

    private static void promptStringTo(Attributes cmd, String name, int tag,
                                       StringBuilder sb) {
        String s = cmd.getString(tag, null);
        if (null != s)
            sb.append(name).append(s);
    }

    private static void promptUIDTo(Attributes cmd, String name, int tag,
                                    StringBuilder sb) {
        if (tag != 0) {
            String uid = cmd.getString(tag, null);
            if (null != uid)
                promptUIDTo(name, uid, sb);
        }
    }

    private static void promptUIDTo(String name, String uid, StringBuilder sb) {
        sb.append(Material.LINE_SEPARATOR).append(name);
        UID.promptTo(uid, sb);
    }

    private static void promptMoveOriginatorTo(Attributes cmd, StringBuilder sb) {
        String aet = cmd.getString(Tag.MoveOriginatorApplicationEntityTitle,
                null);
        if (null != aet)
            sb.append(Material.LINE_SEPARATOR)
                    .append("  orig=")
                    .append(aet)
                    .append(" >> ")
                    .append(cmd.getInt(Tag.MoveOriginatorMessageID, -1))
                    .append(":C-MOVE-RQ");
    }

    private static void promptAttributeIdentifierListTo(Attributes cmd,
                                                        StringBuilder sb) {
        int[] tags = cmd.getInts(Tag.AttributeIdentifierList);
        if (null == tags)
            return;

        sb.append(Material.LINE_SEPARATOR).append("  tags=[");
        if (tags.length > 0) {
            for (int tag : tags)
                sb.append(Tag.toString(tag)).append(", ");
            sb.setLength(sb.length() - 2);
        }
        sb.append(Symbol.C_BRACKET_RIGHT);
    }

    private static void promptNumberOfSubOpsTo(Attributes cmd, StringBuilder sb) {
        promptIntTo(cmd, ", remaining=", Tag.NumberOfRemainingSuboperations, sb);
        promptIntTo(cmd, ", completed=", Tag.NumberOfCompletedSuboperations, sb);
        promptIntTo(cmd, ", failed=", Tag.NumberOfFailedSuboperations, sb);
        promptIntTo(cmd, ", warning=", Tag.NumberOfWarningSuboperations, sb);
    }

    public int commandField() {
        return commandField;
    }

    public int tagOfSOPClassUID() {
        return tagOfSOPClassUID;
    }

    public int tagOfSOPInstanceUID() {
        return tagOfSOPInstanceUID;
    }

    public boolean isRSP() {
        return (commandField & 0x8000) != 0;
    }

    public boolean isRetrieveRQ() {
        return this == C_GET_RQ || this == C_MOVE_RQ;
    }

    public boolean isRetrieveRSP() {
        return this == C_GET_RSP || this == C_MOVE_RSP;
    }

    public boolean isCService() {
        return (commandField & 0x100) == 0;
    }

    public int commandFieldOfRSP() {
        return commandField | 0x8000;
    }

    public String toString(Attributes cmdAttrs) {
        return cmdAttrs.getInt(tagOfMessageID, -1) + prompt;
    }

    public String toString(Attributes cmdAttrs, int pcid, String tsuid) {
        StringBuilder sb = new StringBuilder();
        sb.append(cmdAttrs.getInt(tagOfMessageID, -1)).append(prompt).append("[pcid=").append(pcid);
        switch (this) {
            case C_STORE_RQ:
                promptIntTo(cmdAttrs, ", prior=", Tag.Priority, sb);
                promptMoveOriginatorTo(cmdAttrs, sb);
                break;
            case C_GET_RQ:
                promptIntTo(cmdAttrs, ", prior=", Tag.Priority, sb);
                promptAttributeIdentifierListTo(cmdAttrs, sb);
                break;
            case C_FIND_RQ:
            case C_MOVE_RQ:
                promptIntTo(cmdAttrs, ", prior=", Tag.Priority, sb);
                break;
            case C_GET_RSP:
            case C_MOVE_RSP:
                promptNumberOfSubOpsTo(cmdAttrs, sb);
                break;
            case N_EVENT_REPORT_RQ:
            case N_EVENT_REPORT_RSP:
                promptIntTo(cmdAttrs, ", eventID=", Tag.EventTypeID, sb);
                break;
            case N_ACTION_RQ:
            case N_ACTION_RSP:
                promptIntTo(cmdAttrs, ", actionID=", Tag.ActionTypeID, sb);
                break;
        }
        if (isRSP()) {
            sb.append(", status=")
                    .append(Integer.toHexString(cmdAttrs.getInt(Tag.Status, -1)))
                    .append('H');
            promptIntTo(cmdAttrs, ", errorID=", Tag.ErrorID, sb);
            promptStringTo(cmdAttrs, ", errorComment=", Tag.ErrorComment, sb);
            promptAttributeIdentifierListTo(cmdAttrs, sb);
        }
        promptUIDTo(cmdAttrs, "  cuid=", tagOfSOPClassUID, sb);
        promptUIDTo(cmdAttrs, "  iuid=", tagOfSOPInstanceUID, sb);
        promptUIDTo("  tsuid=", tsuid, sb);
        sb.append(Symbol.C_BRACKET_RIGHT);
        return sb.toString();
    }

}
