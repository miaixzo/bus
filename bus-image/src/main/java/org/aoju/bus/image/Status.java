/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.image;

import lombok.Data;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.image.galaxy.data.Attributes;
import org.aoju.bus.image.metric.Progress;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kimi Liu
 * @version 5.8.8
 * @since JDK 1.8+
 */
@Data
public class Status {

    public static final int Success = 0x0000;

    public static final int Pending = 0xFF00;
    public static final int PendingWarning = 0xFF01;

    public static final int Cancel = 0xFE00;

    /**
     * Failure: no such attribute (105H): the Tag for the specified Attribute
     * was not recognized.
     * Used in N-SET-RSP, N-CREATE-RSP.
     * May contain:
     * Attribute Identifier List (0000,1005)
     */
    public static final int NoSuchAttribute = 0x0105;

    /**
     * Failure: invalid attribute value (106H): the Attribute Value specified
     * was out of range or otherwise inappropriate.
     * Used in N-SET-RSP, N-CREATE-RSP.
     * May contain:
     * Modification List/Attribute List (no tag)
     */
    public static final int InvalidAttributeValue = 0x0106;

    /**
     * Warning: attribute list error (107H): one or more Attribute Values were
     * not read/modified/created because the specified Attribute was not recognized.
     * Used in N-GET-RSP, N-SET-RSP, N-CREATE-RSP
     * May contain:
     * Affected SOP Class UID (0000,0002)
     * Affected SOP Instance UID (0000,1000)
     * Attribute Identifier List (0000,1005)
     */
    public static final int AttributeListError = 0x0107;

    /**
     * Failure: processing failure (110H): a general failure in processing the
     * operation was encountered.
     * Used in N-EVENT-REPORT-RSP, N-GET-RSP, N-SET-RSP, N-ACTION-RSP,
     * N-CREATE-RSP, N-DELETE-RSP.
     * May contain:
     * Affected SOP Class UID (0000,0002)
     * Error Comment (0000,0902)
     * Error ID (0000,0903)
     * Affected SOP Instance UID (0000,1000)
     */
    public static final int ProcessingFailure = 0x0110;

    /**
     * Failure: duplicate SOP Instance (111H): the new managed SOP Instance
     * Value supplied by the invoking DIMSE-service-user was already registered
     * for a managed SOP Instance of the specified SOP Class.
     * Used in N-CREATE-RSP.
     * May contain:
     * Affected SOP Instance UID (0000,1000)
     */
    public static final int DuplicateSOPinstance = 0x0111;

    /**
     * Failure: no such SOP Instance (112H): the SOP Instance was not recognized.
     * Used in N-EVENT-REPORT-RSP, N-SET-RSP, N-ACTION-RSP, N-DELETE-RSP.
     * May contain:
     * Affected SOP Instance UID (0000,1000)
     */
    public static final int NoSuchObjectInstance = 0x0112;

    /**
     * Failure: no such event type (113H): the event type specified was not
     * recognized.
     * Used in N-EVENT-REPORT-RSP.
     * May contain:
     * Affected SOP Class UID (0000,0002)
     * Event Type ID (0000,1002)
     */
    public static final int NoSuchEventType = 0x0113;

    /**
     * Failure: no such argument (114H): the event/action information specified
     * was not recognized/supported.
     * Used in N-EVENT-REPORT-RSP, N-ACTION-RSP.
     * May contain:
     * Affected SOP Class UID (0000,0002)
     * Event Type ID (0000,1002)
     * Action Type ID (0000,1008)
     */
    public static final int NoSuchArgument = 0x0114;

    /**
     * Failure: invalid argument value (115H): the event/action information
     * value specified was out of range or otherwise inappropriate.
     * Used in N-EVENT-REPORT-RSP, N-ACTION-RSP.
     * May contain:
     * Affected SOP Class UID (0000,0002)
     * Affected SOP Instance UID (0000,1000)
     * Event Type ID (0000,1002)
     * Event Information (no tag)
     * Action Type ID (0000,1008)
     * Action Information (no tag)
     */
    public static final int InvalidArgumentValue = 0x0115;

    /**
     * Warning: attribute value out of range (116H): the Attribute Value
     * specified was out of range or otherwise inappropriate.
     * Used in N-SET-RSP, N-CREATE-RSP.
     * May contain:
     * Modification List/Attribute List
     */
    public static final int AttributeValueOutOfRange = 0x0116;

    /**
     * Failure: invalid SOP Instance (117H): the SOP Instance UID specified
     * implied a violation of the UID construction rules.
     * Used in N-EVENT-REPORT-RSP, N-GET-RSP, N-SET-RSP, N-ACTION-RSP,
     * N-CREATE-RSP, N-DELETE-RSP.
     * May contain:
     * Affected SOP Instance UID (0000,1000)
     */
    public static final int InvalidObjectInstance = 0x0117;

    /**
     * Failure: no such SOP class (118H): the SOP Class was not recognized.
     * Used in N-EVENT-REPORT-RSP, N-GET-RSP, N-SET-RSP, N-ACTION-RSP,
     * N-CREATE-RSP, N-DELETE-RSP.
     * May contain:
     * Affected SOP Class UID (0000,0002)
     */
    public static final int NoSuchSOPclass = 0x0118;

    /**
     * Failure: class-instance conflict (119H): the specified SOP Instance is
     * not a member of the specified SOP class.
     * Used in N-EVENT-REPORT-RSP, N-GET-RSP, N-SET-RSP, N-ACTION-RSP,
     * N-DELETE-RSP.
     * May contain:
     * Affected SOP Class UID (0000,0002)
     * Affected SOP Instance UID (0000,1000)
     */
    public static final int ClassInstanceConflict = 0x0119;

    /**
     * Failure: missing Attribute (120H): a required Attribute was not
     * supplied.
     * Used in N-CREATE-RSP.
     * May contain:
     * Modification List/Attribute List (no tag)
     */
    public static final int MissingAttribute = 0x0120;

    /**
     * Failure: missing Attribute Value (121H): a required Attribute Value was
     * not supplied and a default value was not available.
     * Used in N-SET-RSP, N-CREATE-RSP.
     * May contain:
     * Attribute Identifier List (0000,1005)
     */
    public static final int MissingAttributeValue = 0x0121;

    /**
     * Refused: SOP Class Not Supported (112H).
     * Used in C-STORE-RSP, C-FIND-RSP, C-GET-RSP, C-MOVE-RSP.
     * May contain:
     * Affected SOP Class UID (0000,0002)
     */
    public static final int SOPclassNotSupported = 0x0122;

    /**
     * Failure: no such action type (123H): the action type specified was not
     * supported.
     * Used in N-ACTION-RSP.
     * May contain:
     * Affected SOP Class UID (0000,0002)
     * Action Type ID (0000,1008)
     */
    public static final int NoSuchActionType = 0x0123;

    /**
     * Refused: not authorized (124H): the DIMSE-service-user was not
     * authorized to invoke the operation.
     * Used in C-STORE-RSP, C-FIND-RSP, C-GET-RSP, C-MOVE-RSP, N-GET-RSP,
     * N-SET-RSP, N-ACTION-RSP, N-CREATE-RSP, -DELETE-RSP.
     * May contain:
     * Error Comment (0000,0902)
     */
    public static final int NotAuthorized = 0x0124;

    /**
     * Failure: duplicate invocation (210H): the Message ID (0000,0110)
     * specified is allocated to another notification or operation.
     * Used in C-STORE-RSP, C-FIND-RSP, C-GET-RSP, C-MOVE-RSP, C-ECHO-RSP,
     * N-EVENT-REPORT-RSP, N-GET-RSP, N-SET-RSP, N-ACTION-RSP, N-CREATE-RSP,
     * N-DELETE-RSP.
     */
    public static final int DuplicateInvocation = 0x0210;

    /**
     * Failure: unrecognized operation (211H): the operation is not one of
     * those agreed between the DIMSE-service-users.
     * Used in C-STORE-RSP, C-FIND-RSP, C-GET-RSP, C-MOVE-RSP, C-ECHO-RSP,
     * N-EVENT-REPORT-RSP, -GET-RSP, N-SET-RSP, N-ACTION-RSP, N-CREATE-RSP,
     * N-DELETE-RSP.
     */
    public static final int UnrecognizedOperation = 0x0211;

    /**
     * Failure: mistyped argument (212H): one of the parameters supplied has
     * not been agreed for use on the Association between the DIMSE-service-users.
     * Used in N-EVENT-REPORT-RSP, N-GET-RSP, N-SET-RSP, N-ACTION-RSP,
     * N-CREATE-RSP, N-DELETE-RSP.
     */
    public static final int MistypedArgument = 0x0212;

    /**
     * Failure: resource limitation (213H): the operation was not performed due
     * to resource limitation.
     */
    public static final int ResourceLimitation = 0x0213;

    public static final int OutOfResources = 0xA700;
    public static final int UnableToCalculateNumberOfMatches = 0xA701;
    public static final int UnableToPerformSubOperations = 0xA702;
    public static final int MoveDestinationUnknown = 0xA801;
    public static final int IdentifierDoesNotMatchSOPClass = 0xA900;
    public static final int DataSetDoesNotMatchSOPClassError = 0xA900;

    public static final int OneOrMoreFailures = 0xB000;
    public static final int CoercionOfDataElements = 0xB000;
    public static final int ElementsDiscarded = 0xB006;
    public static final int DataSetDoesNotMatchSOPClassWarning = 0xB007;

    public static final int UnableToProcess = 0xC000;
    public static final int CannotUnderstand = 0xC000;

    private final Progress progress;
    private final List<Attributes> list;
    private volatile int status;
    private String message;

    public Status() {
        this(Pending, null, null);
    }

    public Status(Progress progress) {
        this(Pending, null, progress);
    }

    public Status(int status, String message, Progress progress) {
        this.status = status;
        this.message = message;
        this.progress = progress;
        this.list = new ArrayList<>();
    }

    public static Status build(Status dcmState, String timeMessage, Exception e) {
        Status state = dcmState;
        if (state == null) {
            state = new Status(Status.UnableToProcess, null, null);
        }

        Progress p = state.getProgress();
        int s = state.getStatus();

        StringBuilder msg = new StringBuilder();

        boolean hasFailed = false;
        if (p != null) {
            int failed = p.getNumberOfFailedSuboperations();
            int warning = p.getNumberOfWarningSuboperations();
            int remaining = p.getNumberOfRemainingSuboperations();
            if (failed > 0) {
                hasFailed = true;
                msg.append(String.format("%d/%d operations has failed.", failed,
                        failed + p.getNumberOfCompletedSuboperations()));
            } else if (remaining > 0) {
                msg.append(String.format("%d operations remains. ", remaining));
            } else if (warning > 0) {
                msg.append(String.format("%d operations has a warning status. ", warning));
            }
        }
        if (e != null) {
            hasFailed = true;
            if (msg.length() > 0) {
                msg.append(Symbol.SPACE);
            }
            msg.append(e.getLocalizedMessage());
        }

        if (p != null && p.getAttributes() != null) {
            String error = p.getErrorComment();
            if (StringUtils.hasText(error)) {
                hasFailed = true;
                if (msg.length() > 0) {
                    msg.append("\n");
                }
                msg.append("DICOM error : ");
                msg.append(error);
            }

            if (!Status.isPending(s) && s != -1 && s != Status.Success && s != Status.Cancel) {
                if (msg.length() > 0) {
                    msg.append("\n");
                }
                msg.append("DICOM status : ");
                msg.append(s);
            }
        }

        if (!hasFailed) {
            if (timeMessage != null) {
                msg.append(timeMessage);
            }
        } else {
            if (Status.isPending(s) || s == -1) {
                state.setStatus(Status.UnableToProcess);
            }
        }
        state.setMessage(msg.toString());
        return state;
    }

    public static boolean isPending(int status) {
        return (status & Pending) == Pending;
    }

    public int getStatus() {
        if (progress != null && progress.getAttributes() != null) {
            return progress.getStatus();
        }
        return status;
    }

    public void setList(Attributes attributes) {
        if (attributes != null) {
            this.list.add(attributes);
        }
    }

}
