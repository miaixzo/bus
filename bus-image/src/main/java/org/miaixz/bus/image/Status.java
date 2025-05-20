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
package org.miaixz.bus.image;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.image.galaxy.ImageParam;
import org.miaixz.bus.image.galaxy.ImageProgress;
import org.miaixz.bus.image.galaxy.data.Attributes;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class Status {

    public static final int Success = 0x0000;

    public static final int Pending = 0xFF00;
    public static final int PendingWarning = 0xFF01;

    public static final int Cancel = 0xFE00;

    /**
     * Failure: no such attribute (105H): the Tag for the specified Attribute was not recognized. Used in N-SET-RSP,
     * N-CREATE-RSP. May contain: Attribute Identifier List (0000,1005)
     */
    public static final int NoSuchAttribute = 0x0105;

    /**
     * Failure: invalid attribute value (106H): the Attribute Value specified was out of range or otherwise
     * inappropriate. Used in N-SET-RSP, N-CREATE-RSP. May contain: Modification List/Attribute List (no tag)
     */
    public static final int InvalidAttributeValue = 0x0106;

    /**
     * Warning: attribute list error (107H): one or more Attribute Values were not read/modified/created because the
     * specified Attribute was not recognized. Used in N-GET-RSP, N-SET-RSP, N-CREATE-RSP May contain: Affected SOP
     * Class UID (0000,0002) Affected SOP Instance UID (0000,1000) Attribute Identifier List (0000,1005)
     */
    public static final int AttributeListError = 0x0107;

    /**
     * Failure: processing failure (110H): a general failure in processing the operation was encountered. Used in
     * N-EVENT-REPORT-RSP, N-GET-RSP, N-SET-RSP, N-ACTION-RSP, N-CREATE-RSP, N-DELETE-RSP. May contain: Affected SOP
     * Class UID (0000,0002) Error Comment (0000,0902) Error ID (0000,0903) Affected SOP Instance UID (0000,1000)
     */
    public static final int ProcessingFailure = 0x0110;

    /**
     * Failure: duplicate SOP Instance (111H): the new managed SOP Instance Value supplied by the invoking
     * DIMSE-service-user was already registered for a managed SOP Instance of the specified SOP Class. Used in
     * N-CREATE-RSP. May contain: Affected SOP Instance UID (0000,1000)
     */
    public static final int DuplicateSOPinstance = 0x0111;

    /**
     * Failure: no such SOP Instance (112H): the SOP Instance was not recognized. Used in N-EVENT-REPORT-RSP, N-SET-RSP,
     * N-ACTION-RSP, N-DELETE-RSP. May contain: Affected SOP Instance UID (0000,1000)
     */
    public static final int NoSuchObjectInstance = 0x0112;

    /**
     * Failure: no such event type (113H): the event type specified was not recognized. Used in N-EVENT-REPORT-RSP. May
     * contain: Affected SOP Class UID (0000,0002) Event Type ID (0000,1002)
     */
    public static final int NoSuchEventType = 0x0113;

    /**
     * Failure: no such argument (114H): the event/action information specified was not recognized/supported. Used in
     * N-EVENT-REPORT-RSP, N-ACTION-RSP. May contain: Affected SOP Class UID (0000,0002) Event Type ID (0000,1002)
     * Action Type ID (0000,1008)
     */
    public static final int NoSuchArgument = 0x0114;

    /**
     * Failure: invalid argument value (115H): the event/action information value specified was out of range or
     * otherwise inappropriate. Used in N-EVENT-REPORT-RSP, N-ACTION-RSP. May contain: Affected SOP Class UID
     * (0000,0002) Affected SOP Instance UID (0000,1000) Event Type ID (0000,1002) Event Information (no tag) Action
     * Type ID (0000,1008) Action Information (no tag)
     */
    public static final int InvalidArgumentValue = 0x0115;

    /**
     * Warning: attribute value out of range (116H): the Attribute Value specified was out of range or otherwise
     * inappropriate. Used in N-SET-RSP, N-CREATE-RSP. May contain: Modification List/Attribute List
     */
    public static final int AttributeValueOutOfRange = 0x0116;

    /**
     * Failure: invalid SOP Instance (117H): the SOP Instance UID specified implied a violation of the UID construction
     * rules. Used in N-EVENT-REPORT-RSP, N-GET-RSP, N-SET-RSP, N-ACTION-RSP, N-CREATE-RSP, N-DELETE-RSP. May contain:
     * Affected SOP Instance UID (0000,1000)
     */
    public static final int InvalidObjectInstance = 0x0117;

    /**
     * Failure: no such SOP class (118H): the SOP Class was not recognized. Used in N-EVENT-REPORT-RSP, N-GET-RSP,
     * N-SET-RSP, N-ACTION-RSP, N-CREATE-RSP, N-DELETE-RSP. May contain: Affected SOP Class UID (0000,0002)
     */
    public static final int NoSuchSOPclass = 0x0118;

    /**
     * Failure: class-instance conflict (119H): the specified SOP Instance is not a member of the specified SOP class.
     * Used in N-EVENT-REPORT-RSP, N-GET-RSP, N-SET-RSP, N-ACTION-RSP, N-DELETE-RSP. May contain: Affected SOP Class UID
     * (0000,0002) Affected SOP Instance UID (0000,1000)
     */
    public static final int ClassInstanceConflict = 0x0119;

    /**
     * Failure: missing Attribute (120H): a required Attribute was not supplied. Used in N-CREATE-RSP. May contain:
     * Modification List/Attribute List (no tag)
     */
    public static final int MissingAttribute = 0x0120;

    /**
     * Failure: missing Attribute Value (121H): a required Attribute Value was not supplied and a default value was not
     * available. Used in N-SET-RSP, N-CREATE-RSP. May contain: Attribute Identifier List (0000,1005)
     */
    public static final int MissingAttributeValue = 0x0121;

    /**
     * Refused: SOP Class Not Supported (112H). Used in C-STORE-RSP, C-FIND-RSP, C-GET-RSP, C-MOVE-RSP. May contain:
     * Affected SOP Class UID (0000,0002)
     */
    public static final int SOPclassNotSupported = 0x0122;

    /**
     * Failure: no such action type (123H): the action type specified was not supported. Used in N-ACTION-RSP. May
     * contain: Affected SOP Class UID (0000,0002) Action Type ID (0000,1008)
     */
    public static final int NoSuchActionType = 0x0123;

    /**
     * Refused: not authorized (124H): the DIMSE-service-user was not authorized to invoke the operation. Used in
     * C-STORE-RSP, C-FIND-RSP, C-GET-RSP, C-MOVE-RSP, N-GET-RSP, N-SET-RSP, N-ACTION-RSP, N-CREATE-RSP, -DELETE-RSP.
     * May contain: Error Comment (0000,0902)
     */
    public static final int NotAuthorized = 0x0124;

    /**
     * Failure: duplicate invocation (210H): the Message ID (0000,0110) specified is allocated to another notification
     * or operation. Used in C-STORE-RSP, C-FIND-RSP, C-GET-RSP, C-MOVE-RSP, C-ECHO-RSP, N-EVENT-REPORT-RSP, N-GET-RSP,
     * N-SET-RSP, N-ACTION-RSP, N-CREATE-RSP, N-DELETE-RSP.
     */
    public static final int DuplicateInvocation = 0x0210;

    /**
     * Failure: unrecognized operation (211H): the operation is not one of those agreed between the DIMSE-service-users.
     * Used in C-STORE-RSP, C-FIND-RSP, C-GET-RSP, C-MOVE-RSP, C-ECHO-RSP, N-EVENT-REPORT-RSP, -GET-RSP, N-SET-RSP,
     * N-ACTION-RSP, N-CREATE-RSP, N-DELETE-RSP.
     */
    public static final int UnrecognizedOperation = 0x0211;

    /**
     * Failure: mistyped argument (212H): one of the parameters supplied has not been agreed for use on the Association
     * between the DIMSE-service-users. Used in N-EVENT-REPORT-RSP, N-GET-RSP, N-SET-RSP, N-ACTION-RSP, N-CREATE-RSP,
     * N-DELETE-RSP.
     */
    public static final int MistypedArgument = 0x0212;

    /**
     * Failure: resource limitation (213H): the operation was not performed due to resource limitation.
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

    public static final int UPSCreatedWithModifications = 0xB300;
    public static final int UPSDeletionLockNotGranted = 0xB301;
    public static final int UPSAlreadyInRequestedStateOfCanceled = 0xB304;
    public static final int UPSCoercedInvalidValuesToValidValues = 0xB305;
    public static final int UPSAlreadyInRequestedStateOfCompleted = 0xB306;

    public static final int UPSMayNoLongerBeUpdated = 0xC300;
    public static final int UPSTransactionUIDNotCorrect = 0xC301;
    public static final int UPSAlreadyInProgress = 0xC302;
    public static final int UPSStateMayNotChangedToScheduled = 0xC303;
    public static final int UPSNotMetFinalStateRequirements = 0xC304;
    public static final int UPSDoesNotExist = 0xC307;
    public static final int UPSUnknownReceivingAET = 0xC308;
    public static final int UPSNotScheduled = 0xC309;
    public static final int UPSNotYetInProgress = 0xC310;
    public static final int UPSAlreadyCompleted = 0xC311;
    public static final int UPSPerformerCannotBeContacted = 0xC312;
    public static final int UPSPerformerChoosesNotToCancel = 0xC313;
    public static final int UPSActionNotAppropriate = 0xC314;
    public static final int UPSDoesNotSupportEventReports = 0xC315;

    private final List<Attributes> dicomRSP;
    private final ImageProgress progress;
    private final List<ImageParam> dicomMatchingKeys;

    private volatile int status;
    private String message;
    private String errorMessage;
    private LocalDateTime startConnectionDateTime;
    private LocalDateTime startTransferDateTime;
    private LocalDateTime endTransferDateTime;
    private long bytesSize;

    public Status() {
        this(Status.Pending, null, null);
    }

    public Status(ImageProgress progress) {
        this(Status.Pending, null, progress);
    }

    public Status(int status, String message, ImageProgress progress) {
        this.status = status;
        this.message = message;
        this.progress = progress;
        this.dicomRSP = new ArrayList<>();
        this.dicomMatchingKeys = new ArrayList<>();
        this.bytesSize = -1;
    }

    public static boolean isPending(int status) {
        return (status & Pending) == Pending;
    }

    public static Status buildMessage(Status dcmState, String timeMessage, Exception e) {
        Status state = dcmState;
        if (state == null) {
            state = new Status(Status.UnableToProcess, null, null);
        }

        ImageProgress p = state.getProgress();
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
            msg.append(e.getMessage());
            state.setErrorMessage(e.getMessage());
        }

        if (p != null && p.getAttributes() != null) {
            String error = p.getErrorComment();
            if (StringKit.hasText(error)) {
                hasFailed = true;
                if (msg.length() > 0) {
                    msg.append("\n");
                }
                msg.append("DICOM error");
                msg.append(Symbol.COLON + Symbol.SPACE);
                msg.append(error);
            }

            if (!Status.isPending(s) && s != -1 && s != Status.Success && s != Status.Cancel) {
                if (msg.length() > 0) {
                    msg.append("\n");
                }
                msg.append("DICOM status");
                msg.append(Symbol.COLON + Symbol.SPACE);
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

    /**
     * Get the DICOM status
     *
     * @return the DICOM status of the process
     */
    public int getStatus() {
        if (progress != null && progress.getAttributes() != null) {
            return progress.getStatus();
        }
        return status;
    }

    /**
     * Set the DICOM status
     *
     * @param status DICOM status of the process
     */
    public void setStatus(int status) {
        this.status = status;
    }

    public synchronized String getMessage() {
        return message;
    }

    public synchronized void setMessage(String message) {
        this.message = message;
    }

    public ImageProgress getProgress() {
        return progress;
    }

    public List<Attributes> getDicomRSP() {
        return dicomRSP;
    }

    public List<ImageParam> getDicomMatchingKeys() {
        return dicomMatchingKeys;
    }

    public LocalDateTime getStartTransferDateTime() {
        return startTransferDateTime;
    }

    public void setStartTransferDateTime(LocalDateTime startTransferDateTime) {
        this.startTransferDateTime = startTransferDateTime;
    }

    public LocalDateTime getEndTransferDateTime() {
        return endTransferDateTime;
    }

    public void setEndTransferDateTime(LocalDateTime endTransferDateTime) {
        this.endTransferDateTime = endTransferDateTime;
    }

    public LocalDateTime getStartConnectionDateTime() {
        return startConnectionDateTime;
    }

    public void setStartConnectionDateTime(LocalDateTime startConnectionDateTime) {
        this.startConnectionDateTime = startConnectionDateTime;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public long getBytesSize() {
        return bytesSize;
    }

    public void setBytesSize(long bytesSize) {
        this.bytesSize = bytesSize;
    }

    public void addDicomRSP(Attributes dicomRSP) {
        this.dicomRSP.add(dicomRSP);
    }

    public void addDicomMatchingKeys(ImageParam param) {
        this.dicomMatchingKeys.add(param);
    }

    public void addProcessTime(long startTimeStamp, long endTimeStamp) {
        addProcessTime(0, startTimeStamp, endTimeStamp);
    }

    public void addProcessTime(long connectionTimeStamp, long startTimeStamp, long endTimeStamp) {
        if (connectionTimeStamp > 0) {
            setStartConnectionDateTime(
                    Instant.ofEpochMilli(connectionTimeStamp).atZone(ZoneId.systemDefault()).toLocalDateTime());
        }
        if (startTimeStamp > 0) {
            setStartTransferDateTime(
                    Instant.ofEpochMilli(startTimeStamp).atZone(ZoneId.systemDefault()).toLocalDateTime());
        }
        if (endTimeStamp > 0) {
            setEndTransferDateTime(Instant.ofEpochMilli(endTimeStamp).atZone(ZoneId.systemDefault()).toLocalDateTime());
        }
    }

}
