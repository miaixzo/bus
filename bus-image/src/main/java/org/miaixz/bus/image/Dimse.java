/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
import org.miaixz.bus.image.galaxy.data.Attributes;

/**
 * Represents the standard DICOM Message Service Element (DIMSE) commands used for network communication. This enum
 * provides constants for each command, along with associated metadata such as command field codes and relevant
 * attribute tags.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum Dimse {

    /**
     * C-STORE Request.
     */
    C_STORE_RQ(0x0001, Tag.AffectedSOPClassUID, Tag.AffectedSOPInstanceUID, Tag.MessageID, ":C-STORE-RQ"),
    /**
     * C-STORE Response.
     */
    C_STORE_RSP(0x8001, Tag.AffectedSOPClassUID, Tag.AffectedSOPInstanceUID, Tag.MessageIDBeingRespondedTo,
            ":C-STORE-RSP"),
    /**
     * C-GET Request.
     */
    C_GET_RQ(0x0010, Tag.AffectedSOPClassUID, 0, Tag.MessageID, ":C-GET-RQ"),
    /**
     * C-GET Response.
     */
    C_GET_RSP(0x8010, Tag.AffectedSOPClassUID, 0, Tag.MessageIDBeingRespondedTo, ":C-GET-RSP"),
    /**
     * C-FIND Request.
     */
    C_FIND_RQ(0x0020, Tag.AffectedSOPClassUID, 0, Tag.MessageID, ":C-FIND-RQ"),
    /**
     * C-FIND Response.
     */
    C_FIND_RSP(0x8020, Tag.AffectedSOPClassUID, 0, Tag.MessageIDBeingRespondedTo, ":C-FIND-RSP"),
    /**
     * C-MOVE Request.
     */
    C_MOVE_RQ(0x0021, Tag.AffectedSOPClassUID, 0, Tag.MessageID, ":C-MOVE-RQ"),
    /**
     * C-MOVE Response.
     */
    C_MOVE_RSP(0x8021, Tag.AffectedSOPClassUID, 0, Tag.MessageIDBeingRespondedTo, ":C-MOVE-RSP"),
    /**
     * C-ECHO Request.
     */
    C_ECHO_RQ(0x0030, Tag.AffectedSOPClassUID, 0, Tag.MessageID, ":C-ECHO-RQ"),
    /**
     * C-ECHO Response.
     */
    C_ECHO_RSP(0x8030, Tag.AffectedSOPClassUID, 0, Tag.MessageIDBeingRespondedTo, ":C-ECHO-RSP"),
    /**
     * N-EVENT-REPORT Request.
     */
    N_EVENT_REPORT_RQ(0x0100, Tag.AffectedSOPClassUID, Tag.AffectedSOPInstanceUID, Tag.MessageID, ":N-EVENT-REPORT-RQ"),
    /**
     * N-EVENT-REPORT Response.
     */
    N_EVENT_REPORT_RSP(0x8100, Tag.AffectedSOPClassUID, Tag.AffectedSOPInstanceUID, Tag.MessageIDBeingRespondedTo,
            ":N-EVENT-REPORT-RSP"),
    /**
     * N-GET Request.
     */
    N_GET_RQ(0x0110, Tag.RequestedSOPClassUID, Tag.RequestedSOPInstanceUID, Tag.MessageID, ":N-GET-RQ"),
    /**
     * N-GET Response.
     */
    N_GET_RSP(0x8110, Tag.AffectedSOPClassUID, Tag.AffectedSOPInstanceUID, Tag.MessageIDBeingRespondedTo, ":N-GET-RSP"),
    /**
     * N-SET Request.
     */
    N_SET_RQ(0x0120, Tag.RequestedSOPClassUID, Tag.RequestedSOPInstanceUID, Tag.MessageID, ":N-SET-RQ"),
    /**
     * N-SET Response.
     */
    N_SET_RSP(0x8120, Tag.AffectedSOPClassUID, Tag.AffectedSOPInstanceUID, Tag.MessageIDBeingRespondedTo, ":N-SET-RSP"),
    /**
     * N-ACTION Request.
     */
    N_ACTION_RQ(0x0130, Tag.RequestedSOPClassUID, Tag.RequestedSOPInstanceUID, Tag.MessageID, ":N-ACTION-RQ"),
    /**
     * N-ACTION Response.
     */
    N_ACTION_RSP(0x8130, Tag.AffectedSOPClassUID, Tag.AffectedSOPInstanceUID, Tag.MessageIDBeingRespondedTo,
            ":N-ACTION-RSP"),
    /**
     * N-CREATE Request.
     */
    N_CREATE_RQ(0x0140, Tag.AffectedSOPClassUID, Tag.AffectedSOPInstanceUID, Tag.MessageID, ":N-CREATE-RQ"),
    /**
     * N-CREATE Response.
     */
    N_CREATE_RSP(0x8140, Tag.AffectedSOPClassUID, Tag.AffectedSOPInstanceUID, Tag.MessageIDBeingRespondedTo,
            ":N-CREATE-RSP"),
    /**
     * N-DELETE Request.
     */
    N_DELETE_RQ(0x0150, Tag.RequestedSOPClassUID, Tag.RequestedSOPInstanceUID, Tag.MessageID, ":N-DELETE-RQ"),
    /**
     * N-DELETE Response.
     */
    N_DELETE_RSP(0x8150, Tag.AffectedSOPClassUID, Tag.AffectedSOPInstanceUID, Tag.MessageIDBeingRespondedTo,
            ":N-DELETE-RSP"),
    /**
     * C-CANCEL Request.
     */
    C_CANCEL_RQ(0x0FFF, 0, 0, Tag.MessageIDBeingRespondedTo, ":C-CANCEL-RQ");

    /**
     * The command field value (0000,0100) for this DIMSE message.
     */
    private final int commandField;
    /**
     * The tag for the SOP Class UID in this message (e.g., AffectedSOPClassUID or RequestedSOPClassUID).
     */
    private final int tagOfSOPClassUID;
    /**
     * The tag for the SOP Instance UID in this message (e.g., AffectedSOPInstanceUID or RequestedSOPInstanceUID).
     */
    private final int tagOfSOPInstanceUID;
    /**
     * The tag for the message ID in this message (e.g., MessageID or MessageIDBeingRespondedTo).
     */
    private final int tagOfMessageID;
    /**
     * A short string representation of the command for logging purposes.
     */
    private final String prompt;

    /**
     * Private constructor for the enum.
     *
     * @param commandField        The command field value.
     * @param tagOfSOPClassUID    The tag for the SOP Class UID.
     * @param tagOfSOPInstanceUID The tag for the SOP Instance UID.
     * @param tagOfMessageID      The tag for the Message ID.
     * @param prompt              The string prompt for logging.
     */
    Dimse(int commandField, int tagOfSOPClassUID, int tagOfSOPInstanceUID, int tagOfMessageID, String prompt) {
        this.commandField = commandField;
        this.tagOfSOPClassUID = tagOfSOPClassUID;
        this.tagOfSOPInstanceUID = tagOfSOPInstanceUID;
        this.tagOfMessageID = tagOfMessageID;
        this.prompt = prompt;
    }

    /**
     * Returns the Dimse enum constant corresponding to the given command field value.
     *
     * @param commandField The integer value of the command field (0000,0100).
     * @return The matching Dimse constant.
     * @throws IllegalArgumentException if no Dimse constant matches the command field.
     */
    public static Dimse valueOf(int commandField) {
        return switch (commandField) {
            case 0x0001 -> C_STORE_RQ;
            case 0x8001 -> C_STORE_RSP;
            case 0x0010 -> C_GET_RQ;
            case 0x8010 -> C_GET_RSP;
            case 0x0020 -> C_FIND_RQ;
            case 0x8020 -> C_FIND_RSP;
            case 0x0021 -> C_MOVE_RQ;
            case 0x8021 -> C_MOVE_RSP;
            case 0x0030 -> C_ECHO_RQ;
            case 0x8030 -> C_ECHO_RSP;
            case 0x0100 -> N_EVENT_REPORT_RQ;
            case 0x8100 -> N_EVENT_REPORT_RSP;
            case 0x0110 -> N_GET_RQ;
            case 0x8110 -> N_GET_RSP;
            case 0x0120 -> N_SET_RQ;
            case 0x8120 -> N_SET_RSP;
            case 0x0130 -> N_ACTION_RQ;
            case 0x8130 -> N_ACTION_RSP;
            case 0x0140 -> N_CREATE_RQ;
            case 0x8140 -> N_CREATE_RSP;
            case 0x0150 -> N_DELETE_RQ;
            case 0x8150 -> N_DELETE_RSP;
            case 0x0FFF -> C_CANCEL_RQ;
            default -> throw new IllegalArgumentException("commandField: " + commandField);
        };
    }

    /**
     * Appends an integer attribute to a StringBuilder if it exists and is not zero.
     *
     * @param cmd  The command attributes.
     * @param name The name of the attribute for logging.
     * @param tag  The tag of the attribute.
     * @param sb   The StringBuilder to append to.
     */
    private static void promptIntTo(Attributes cmd, String name, int tag, StringBuilder sb) {
        int val = cmd.getInt(tag, 0);
        if (val != 0 || cmd.containsValue(tag))
            sb.append(name).append(val);
    }

    /**
     * Appends a string attribute to a StringBuilder if it exists.
     *
     * @param cmd  The command attributes.
     * @param name The name of the attribute for logging.
     * @param tag  The tag of the attribute.
     * @param sb   The StringBuilder to append to.
     */
    private static void promptStringTo(Attributes cmd, String name, int tag, StringBuilder sb) {
        String s = cmd.getString(tag, null);
        if (s != null)
            sb.append(name).append(s);
    }

    /**
     * Appends a UID attribute to a StringBuilder in a formatted way if it exists.
     *
     * @param cmd  The command attributes.
     * @param name The name of the UID for logging.
     * @param tag  The tag of the UID attribute.
     * @param sb   The StringBuilder to append to.
     */
    private static void promptUIDTo(Attributes cmd, String name, int tag, StringBuilder sb) {
        if (tag != 0) {
            String uid = cmd.getString(tag, null);
            if (uid != null)
                promptUIDTo(name, uid, sb);
        }
    }

    /**
     * Appends the Move Destination AE Title to a StringBuilder.
     *
     * @param cmd The command attributes for a C-MOVE-RQ.
     * @param sb  The StringBuilder to append to.
     */
    private static void promptMoveDestination(Attributes cmd, StringBuilder sb) {
        sb.append(", dest=").append(cmd.getString(Tag.MoveDestination));
    }

    /**
     * Appends a UID string to a StringBuilder in a formatted way.
     *
     * @param name The name of the UID for logging.
     * @param uid  The UID string.
     * @param sb   The StringBuilder to append to.
     */
    private static void promptUIDTo(String name, String uid, StringBuilder sb) {
        sb.append(Builder.LINE_SEPARATOR).append(name);
        UID.promptTo(uid, sb);
    }

    /**
     * Appends the Move Originator information to a StringBuilder for C-STORE-RQ messages.
     *
     * @param cmd The command attributes.
     * @param sb  The StringBuilder to append to.
     */
    private static void promptMoveOriginatorTo(Attributes cmd, StringBuilder sb) {
        String aet = cmd.getString(Tag.MoveOriginatorApplicationEntityTitle, null);
        if (aet != null)
            sb.append(Builder.LINE_SEPARATOR).append("  orig=").append(aet).append(" >> ")
                    .append(cmd.getInt(Tag.MoveOriginatorMessageID, -1)).append(":C-MOVE-RQ");
    }

    /**
     * Appends the Attribute Identifier List to a StringBuilder.
     *
     * @param cmd The command attributes.
     * @param sb  The StringBuilder to append to.
     */
    private static void promptAttributeIdentifierListTo(Attributes cmd, StringBuilder sb) {
        int[] tags = cmd.getInts(Tag.AttributeIdentifierList);
        if (tags == null)
            return;

        sb.append(Builder.LINE_SEPARATOR).append("  tags=[");
        if (tags.length > 0) {
            for (int tag : tags)
                sb.append(Tag.toString(tag)).append(", ");
            sb.setLength(sb.length() - 2);
        }
        sb.append(Symbol.C_BRACKET_RIGHT);
    }

    /**
     * Appends the number of remaining, completed, failed, and warning sub-operations to a StringBuilder.
     *
     * @param cmd The command attributes for a retrieve response.
     * @param sb  The StringBuilder to append to.
     */
    private static void promptNumberOfSubOpsTo(Attributes cmd, StringBuilder sb) {
        promptIntTo(cmd, ", remaining=", Tag.NumberOfRemainingSuboperations, sb);
        promptIntTo(cmd, ", completed=", Tag.NumberOfCompletedSuboperations, sb);
        promptIntTo(cmd, ", failed=", Tag.NumberOfFailedSuboperations, sb);
        promptIntTo(cmd, ", warning=", Tag.NumberOfWarningSuboperations, sb);
    }

    /**
     * Returns the command field value (0000,0100) of this DIMSE command.
     *
     * @return The command field value as an integer.
     */
    public int commandField() {
        return commandField;
    }

    /**
     * Returns the tag for the SOP Class UID used by this DIMSE command.
     *
     * @return The integer tag value.
     */
    public int tagOfSOPClassUID() {
        return tagOfSOPClassUID;
    }

    /**
     * Returns the tag for the SOP Instance UID used by this DIMSE command.
     *
     * @return The integer tag value.
     */
    public int tagOfSOPInstanceUID() {
        return tagOfSOPInstanceUID;
    }

    /**
     * Checks if this DIMSE command is a response message.
     *
     * @return {@code true} if it is a response (RSP), {@code false} otherwise.
     */
    public boolean isRSP() {
        return (commandField & 0x8000) != 0;
    }

    /**
     * Checks if this DIMSE command is a retrieve request (C-GET-RQ or C-MOVE-RQ).
     *
     * @return {@code true} if it is a retrieve request, {@code false} otherwise.
     */
    public boolean isRetrieveRQ() {
        return this == C_GET_RQ || this == C_MOVE_RQ;
    }

    /**
     * Checks if this DIMSE command is a retrieve response (C-GET-RSP or C-MOVE-RSP).
     *
     * @return {@code true} if it is a retrieve response, {@code false} otherwise.
     */
    public boolean isRetrieveRSP() {
        return this == C_GET_RSP || this == C_MOVE_RSP;
    }

    /**
     * Checks if this DIMSE command is a C-SERVICE (as opposed to an N-SERVICE).
     *
     * @return {@code true} if it is a C-SERVICE, {@code false} otherwise.
     */
    public boolean isCService() {
        return (commandField & 0x100) == 0;
    }

    /**
     * Calculates the command field value for the corresponding response message.
     *
     * @return The command field value for the response.
     */
    public int commandFieldOfRSP() {
        return commandField | 0x8000;
    }

    /**
     * Returns a simple string representation of the command, including its message ID.
     *
     * @param cmdAttrs The command attributes.
     * @return A short string describing the command.
     */
    public String toString(Attributes cmdAttrs) {
        return cmdAttrs.getInt(tagOfMessageID, -1) + prompt;
    }

    /**
     * Returns a detailed, multi-line string representation of the command, including its parameters, Presentation
     * Context ID, and Transfer Syntax UID.
     *
     * @param cmdAttrs The command attributes.
     * @param pcid     The Presentation Context ID under which the command was received.
     * @param tsuid    The Transfer Syntax UID of the associated data set (if any).
     * @return A detailed string describing the command and its context.
     */
    public String toString(Attributes cmdAttrs, int pcid, String tsuid) {
        StringBuilder sb = new StringBuilder();
        sb.append(cmdAttrs.getInt(tagOfMessageID, -1)).append(prompt).append("[pcid=").append(pcid);
        switch (this) {
            case C_STORE_RQ -> {
                promptIntTo(cmdAttrs, ", prior=", Tag.Priority, sb);
                promptMoveOriginatorTo(cmdAttrs, sb);
            }
            case C_FIND_RQ, C_GET_RQ -> promptIntTo(cmdAttrs, ", prior=", Tag.Priority, sb);
            case C_MOVE_RQ -> {
                promptIntTo(cmdAttrs, ", prior=", Tag.Priority, sb);
                promptMoveDestination(cmdAttrs, sb);
            }
            case C_GET_RSP, C_MOVE_RSP -> promptNumberOfSubOpsTo(cmdAttrs, sb);
            case N_EVENT_REPORT_RQ, N_EVENT_REPORT_RSP -> promptIntTo(cmdAttrs, ", eventID=", Tag.EventTypeID, sb);
            case N_GET_RQ -> promptAttributeIdentifierListTo(cmdAttrs, sb);
            case N_ACTION_RQ, N_ACTION_RSP -> promptIntTo(cmdAttrs, ", actionID=", Tag.ActionTypeID, sb);
        }
        if (isRSP()) {
            sb.append(", status=").append(Integer.toHexString(cmdAttrs.getInt(Tag.Status, -1))).append('H');
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
