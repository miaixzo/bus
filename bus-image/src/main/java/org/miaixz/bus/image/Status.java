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
 * Represents the status of a DICOM operation, providing detailed information about its progress, results, and any
 * errors encountered. This class encapsulates DICOM status codes, human-readable messages, progress tracking objects,
 * and timing information.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Status {

    /**
     * Success: The operation was successful.
     */
    public static final int Success = 0x0000;

    /**
     * Pending: The operation is in progress.
     */
    public static final int Pending = 0xFF00;
    /**
     * Pending with Warning: The operation is in progress but has encountered warnings.
     */
    public static final int PendingWarning = 0xFF01;

    /**
     * Cancel: The operation was canceled.
     */
    public static final int Cancel = 0xFE00;

    /**
     * Failure (0x0105): No such attribute. The Tag for the specified Attribute was not recognized. Used in N-SET-RSP,
     * N-CREATE-RSP. May contain: Attribute Identifier List (0000,1005).
     */
    public static final int NoSuchAttribute = 0x0105;

    /**
     * Failure (0x0106): Invalid attribute value. The Attribute Value specified was out of range or otherwise
     * inappropriate. Used in N-SET-RSP, N-CREATE-RSP. May contain: Modification List/Attribute List.
     */
    public static final int InvalidAttributeValue = 0x0106;

    /**
     * Warning (0x0107): Attribute list error. One or more Attribute Values were not read/modified/created because the
     * specified Attribute was not recognized. Used in N-GET-RSP, N-SET-RSP, N-CREATE-RSP. May contain: Affected SOP
     * Class UID (0000,0002), Affected SOP Instance UID (0000,1000), Attribute Identifier List (0000,1005).
     */
    public static final int AttributeListError = 0x0107;

    /**
     * Failure (0x0110): Processing failure. A general failure in processing the operation was encountered. Used in
     * various N-TYPE responses. May contain: Error Comment (0000,0902), Error ID (0000,0903), etc.
     */
    public static final int ProcessingFailure = 0x0110;

    /**
     * Failure (0x0111): Duplicate SOP Instance. The new managed SOP Instance Value supplied was already registered.
     * Used in N-CREATE-RSP. May contain: Affected SOP Instance UID (0000,1000).
     */
    public static final int DuplicateSOPinstance = 0x0111;

    /**
     * Failure (0x0112): No such SOP Instance. The SOP Instance was not recognized. Used in various N-TYPE responses.
     * May contain: Affected SOP Instance UID (0000,1000).
     */
    public static final int NoSuchObjectInstance = 0x0112;

    /**
     * Failure (0x0113): No such event type. The event type specified was not recognized. Used in N-EVENT-REPORT-RSP.
     * May contain: Event Type ID (0000,1002).
     */
    public static final int NoSuchEventType = 0x0113;

    /**
     * Failure (0x0114): No such argument. The event/action information specified was not recognized/supported. Used in
     * N-EVENT-REPORT-RSP, N-ACTION-RSP. May contain: Action Type ID (0000,1008).
     */
    public static final int NoSuchArgument = 0x0114;

    /**
     * Failure (0x0115): Invalid argument value. The event/action information value was out of range or inappropriate.
     * Used in N-EVENT-REPORT-RSP, N-ACTION-RSP. May contain: Event Information or Action Information.
     */
    public static final int InvalidArgumentValue = 0x0115;

    /**
     * Warning (0x0116): Attribute value out of range. The Attribute Value specified was out of range. Used in
     * N-SET-RSP, N-CREATE-RSP. May contain: Modification List/Attribute List.
     */
    public static final int AttributeValueOutOfRange = 0x0116;

    /**
     * Failure (0x0117): Invalid SOP Instance. The SOP Instance UID specified implied a violation of UID construction
     * rules. Used in various N-TYPE responses. May contain: Affected SOP Instance UID (0000,1000).
     */
    public static final int InvalidObjectInstance = 0x0117;

    /**
     * Failure (0x0118): No such SOP class. The SOP Class was not recognized. Used in various N-TYPE responses. May
     * contain: Affected SOP Class UID (0000,0002).
     */
    public static final int NoSuchSOPclass = 0x0118;

    /**
     * Failure (0x0119): Class-instance conflict. The specified SOP Instance is not a member of the specified SOP class.
     * Used in various N-TYPE responses. May contain: Affected SOP Class UID (0000,0002), Affected SOP Instance UID
     * (0000,1000).
     */
    public static final int ClassInstanceConflict = 0x0119;

    /**
     * Failure (0x0120): Missing Attribute. A required Attribute was not supplied. Used in N-CREATE-RSP. May contain:
     * Modification List/Attribute List.
     */
    public static final int MissingAttribute = 0x0120;

    /**
     * Failure (0x0121): Missing Attribute Value. A required Attribute Value was not supplied and no default was
     * available. Used in N-SET-RSP, N-CREATE-RSP. May contain: Attribute Identifier List (0000,1005).
     */
    public static final int MissingAttributeValue = 0x0121;

    /**
     * Refused (0x0122): SOP Class Not Supported. Used in C-STORE-RSP, C-FIND-RSP, C-GET-RSP, C-MOVE-RSP. May contain:
     * Affected SOP Class UID (0000,0002).
     */
    public static final int SOPclassNotSupported = 0x0122;

    /**
     * Failure (0x0123): No such action type. The action type specified was not supported. Used in N-ACTION-RSP. May
     * contain: Action Type ID (0000,1008).
     */
    public static final int NoSuchActionType = 0x0123;

    /**
     * Refused (0x0124): Not authorized. The DIMSE-service-user was not authorized to invoke the operation. Used in
     * various C-TYPE and N-TYPE responses. May contain: Error Comment (0000,0902).
     */
    public static final int NotAuthorized = 0x0124;

    /**
     * Failure (0x0210): Duplicate invocation. The Message ID is allocated to another operation. Used in various C-TYPE
     * and N-TYPE responses.
     */
    public static final int DuplicateInvocation = 0x0210;

    /**
     * Failure (0x0211): Unrecognized operation. The operation is not one agreed between the DIMSE-service-users. Used
     * in various C-TYPE and N-TYPE responses.
     */
    public static final int UnrecognizedOperation = 0x0211;

    /**
     * Failure (0x0212): Mistyped argument. A parameter was not agreed for use on the Association. Used in various
     * N-TYPE responses.
     */
    public static final int MistypedArgument = 0x0212;

    /**
     * Failure (0x0213): Resource limitation. The operation was not performed due to resource limitation.
     */
    public static final int ResourceLimitation = 0x0213;

    /**
     * Failure (0xA700): Out of Resources.
     */
    public static final int OutOfResources = 0xA700;
    /**
     * Failure (0xA701): Unable to calculate number of matches.
     */
    public static final int UnableToCalculateNumberOfMatches = 0xA701;
    /**
     * Failure (0xA702): Unable to perform sub-operations.
     */
    public static final int UnableToPerformSubOperations = 0xA702;
    /**
     * Failure (0xA801): Move Destination unknown.
     */
    public static final int MoveDestinationUnknown = 0xA801;
    /**
     * Failure (0xA900): Identifier does not match SOP Class.
     */
    public static final int IdentifierDoesNotMatchSOPClass = 0xA900;
    /**
     * Failure (0xA900): Data Set does not match SOP Class Error. (Alias for {@link #IdentifierDoesNotMatchSOPClass}).
     */
    public static final int DataSetDoesNotMatchSOPClassError = 0xA900;

    /**
     * Warning (0xB000): Coercion of Data Elements. (Also used for One or more failures).
     */
    public static final int OneOrMoreFailures = 0xB000;
    /**
     * Warning (0xB000): Coercion of Data Elements.
     */
    public static final int CoercionOfDataElements = 0xB000;
    /**
     * Warning (0xB006): Elements discarded.
     */
    public static final int ElementsDiscarded = 0xB006;
    /**
     * Warning (0xB007): Data Set does not match SOP Class Warning.
     */
    public static final int DataSetDoesNotMatchSOPClassWarning = 0xB007;

    /**
     * Failure (0xC000): Unable to process. (Alias for {@link #CannotUnderstand}).
     */
    public static final int UnableToProcess = 0xC000;
    /**
     * Failure (0xC000): Cannot understand.
     */
    public static final int CannotUnderstand = 0xC000;

    /**
     * Warning (0xB300): UPS created with modifications.
     */
    public static final int UPSCreatedWithModifications = 0xB300;
    /**
     * Failure (0xB301): UPS deletion lock not granted.
     */
    public static final int UPSDeletionLockNotGranted = 0xB301;
    /**
     * Warning (0xB304): UPS already in requested state of CANCELED.
     */
    public static final int UPSAlreadyInRequestedStateOfCanceled = 0xB304;
    /**
     * Warning (0xB305): UPS coerced invalid values to valid values.
     */
    public static final int UPSCoercedInvalidValuesToValidValues = 0xB305;
    /**
     * Warning (0xB306): UPS already in requested state of COMPLETED.
     */
    public static final int UPSAlreadyInRequestedStateOfCompleted = 0xB306;

    /**
     * Failure (0xC300): UPS may no longer be updated.
     */
    public static final int UPSMayNoLongerBeUpdated = 0xC300;
    /**
     * Failure (0xC301): UPS transaction UID not correct.
     */
    public static final int UPSTransactionUIDNotCorrect = 0xC301;
    /**
     * Failure (0xC302): UPS already in progress.
     */
    public static final int UPSAlreadyInProgress = 0xC302;
    /**
     * Failure (0xC303): UPS state may not be changed to SCHEDULED.
     */
    public static final int UPSStateMayNotChangedToScheduled = 0xC303;
    /**
     * Failure (0xC304): UPS has not met final state requirements.
     */
    public static final int UPSNotMetFinalStateRequirements = 0xC304;
    /**
     * Failure (0xC307): UPS does not exist.
     */
    public static final int UPSDoesNotExist = 0xC307;
    /**
     * Failure (0xC308): UPS unknown receiving AET.
     */
    public static final int UPSUnknownReceivingAET = 0xC308;
    /**
     * Failure (0xC309): UPS not scheduled.
     */
    public static final int UPSNotScheduled = 0xC309;
    /**
     * Failure (0xC310): UPS not yet in progress.
     */
    public static final int UPSNotYetInProgress = 0xC310;
    /**
     * Failure (0xC311): UPS already completed.
     */
    public static final int UPSAlreadyCompleted = 0xC311;
    /**
     * Failure (0xC312): UPS performer cannot be contacted.
     */
    public static final int UPSPerformerCannotBeContacted = 0xC312;
    /**
     * Failure (0xC313): UPS performer chooses not to cancel.
     */
    public static final int UPSPerformerChoosesNotToCancel = 0xC313;
    /**
     * Failure (0xC314): UPS action not appropriate.
     */
    public static final int UPSActionNotAppropriate = 0xC314;
    /**
     * Failure (0xC315): UPS does not support event reports.
     */
    public static final int UPSDoesNotSupportEventReports = 0xC315;

    /**
     * A list of DICOM response attributes received during the operation.
     */
    private final List<Attributes> dicomRSP;
    /**
     * The progress tracker for the operation, which may contain sub-operation counts.
     */
    private final ImageProgress progress;
    /**
     * A list of DICOM parameters used as matching keys in a query.
     */
    private final List<ImageParam> dicomMatchingKeys;

    /**
     * The primary DICOM status code of the operation. Volatile for thread safety.
     */
    private volatile int status;
    /**
     * A human-readable message describing the overall status.
     */
    private String message;
    /**
     * A specific error message, typically from an exception.
     */
    private String errorMessage;
    /**
     * The date and time when the network connection was established.
     */
    private LocalDateTime startConnectionDateTime;
    /**
     * The date and time when the data transfer started.
     */
    private LocalDateTime startTransferDateTime;
    /**
     * The date and time when the data transfer ended.
     */
    private LocalDateTime endTransferDateTime;
    /**
     * The total size in bytes of the data transferred.
     */
    private long bytesSize;

    /**
     * Constructs a new Status object with a default status of {@link #Pending}.
     */
    public Status() {
        this(Status.Pending, null, null);
    }

    /**
     * Constructs a new Status object with a progress tracker and a default status of {@link #Pending}.
     *
     * @param progress The progress tracker for the operation.
     */
    public Status(ImageProgress progress) {
        this(Status.Pending, null, progress);
    }

    /**
     * Constructs a new Status object with an initial status, message, and progress tracker.
     *
     * @param status   The initial DICOM status code.
     * @param message  The initial status message.
     * @param progress The progress tracker for the operation.
     */
    public Status(int status, String message, ImageProgress progress) {
        this.status = status;
        this.message = message;
        this.progress = progress;
        this.dicomRSP = new ArrayList<>();
        this.dicomMatchingKeys = new ArrayList<>();
        this.bytesSize = -1;
    }

    /**
     * Checks if the given status code represents a "Pending" state.
     *
     * @param status The DICOM status code to check.
     * @return {@code true} if the status is {@link #Pending} or {@link #PendingWarning}, {@code false} otherwise.
     */
    public static boolean isPending(int status) {
        return (status & Pending) == Pending;
    }

    /**
     * Constructs a comprehensive status message based on the current state, progress, an optional time message, and an
     * exception. It aggregates information from the {@link ImageProgress} object, any exception thrown, and the overall
     * DICOM status to create a user-friendly message.
     *
     * @param dcmState    The initial status object. If null, a new one is created with a failure status.
     * @param timeMessage An optional message related to timing or duration.
     * @param e           An optional exception that occurred during the operation.
     * @return An updated {@code Status} object with a detailed message.
     */
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
                msg.append(
                        String.format(
                                "%d/%d operations has failed.",
                                failed,
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
     * Gets the DICOM status code. If a progress object is available, its status is returned; otherwise, the status of
     * this object is returned.
     *
     * @return The DICOM status code of the process.
     */
    public int getStatus() {
        if (progress != null && progress.getAttributes() != null) {
            return progress.getStatus();
        }
        return status;
    }

    /**
     * Sets the DICOM status code.
     *
     * @param status The DICOM status code of the process.
     */
    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * Gets the synchronized status message.
     *
     * @return The status message.
     */
    public synchronized String getMessage() {
        return message;
    }

    /**
     * Sets the synchronized status message.
     *
     * @param message The status message.
     */
    public synchronized void setMessage(String message) {
        this.message = message;
    }

    /**
     * Gets the progress tracker for the operation.
     *
     * @return The {@link ImageProgress} object, or null if not set.
     */
    public ImageProgress getProgress() {
        return progress;
    }

    /**
     * Gets the list of DICOM response attributes.
     *
     * @return A list of {@link Attributes}.
     */
    public List<Attributes> getDicomRSP() {
        return dicomRSP;
    }

    /**
     * Gets the list of DICOM parameters used as matching keys.
     *
     * @return A list of {@link ImageParam}.
     */
    public List<ImageParam> getDicomMatchingKeys() {
        return dicomMatchingKeys;
    }

    /**
     * Gets the start time of the data transfer.
     *
     * @return The transfer start {@link LocalDateTime}.
     */
    public LocalDateTime getStartTransferDateTime() {
        return startTransferDateTime;
    }

    /**
     * Sets the start time of the data transfer.
     *
     * @param startTransferDateTime The transfer start {@link LocalDateTime}.
     */
    public void setStartTransferDateTime(LocalDateTime startTransferDateTime) {
        this.startTransferDateTime = startTransferDateTime;
    }

    /**
     * Gets the end time of the data transfer.
     *
     * @return The transfer end {@link LocalDateTime}.
     */
    public LocalDateTime getEndTransferDateTime() {
        return endTransferDateTime;
    }

    /**
     * Sets the end time of the data transfer.
     *
     * @param endTransferDateTime The transfer end {@link LocalDateTime}.
     */
    public void setEndTransferDateTime(LocalDateTime endTransferDateTime) {
        this.endTransferDateTime = endTransferDateTime;
    }

    /**
     * Gets the start time of the network connection.
     *
     * @return The connection start {@link LocalDateTime}.
     */
    public LocalDateTime getStartConnectionDateTime() {
        return startConnectionDateTime;
    }

    /**
     * Sets the start time of the network connection.
     *
     * @param startConnectionDateTime The connection start {@link LocalDateTime}.
     */
    public void setStartConnectionDateTime(LocalDateTime startConnectionDateTime) {
        this.startConnectionDateTime = startConnectionDateTime;
    }

    /**
     * Gets the detailed error message.
     *
     * @return The error message string.
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Sets the detailed error message.
     *
     * @param errorMessage The error message string.
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Gets the total size in bytes of the data transferred.
     *
     * @return The size in bytes.
     */
    public long getBytesSize() {
        return bytesSize;
    }

    /**
     * Sets the total size in bytes of the data transferred.
     *
     * @param bytesSize The size in bytes.
     */
    public void setBytesSize(long bytesSize) {
        this.bytesSize = bytesSize;
    }

    /**
     * Adds a DICOM response attribute set to the list of responses.
     *
     * @param dicomRSP The {@link Attributes} object from a DICOM response.
     */
    public void addDicomRSP(Attributes dicomRSP) {
        this.dicomRSP.add(dicomRSP);
    }

    /**
     * Adds a DICOM matching key parameter to the list.
     *
     * @param param The {@link ImageParam} used as a matching key.
     */
    public void addDicomMatchingKeys(ImageParam param) {
        this.dicomMatchingKeys.add(param);
    }

    /**
     * Records the processing timestamps for the operation. Assumes no separate connection time.
     *
     * @param startTimeStamp The timestamp (in milliseconds since epoch) when the data transfer started.
     * @param endTimeStamp   The timestamp (in milliseconds since epoch) when the data transfer ended.
     */
    public void addProcessTime(long startTimeStamp, long endTimeStamp) {
        addProcessTime(0, startTimeStamp, endTimeStamp);
    }

    /**
     * Records the processing timestamps for the operation from epoch millisecond values.
     *
     * @param connectionTimeStamp The timestamp (in milliseconds since epoch) when the connection was established. Can
     *                            be 0 if not applicable.
     * @param startTimeStamp      The timestamp (in milliseconds since epoch) when the data transfer started. Can be 0
     *                            if not applicable.
     * @param endTimeStamp        The timestamp (in milliseconds since epoch) when the data transfer ended. Can be 0 if
     *                            not applicable.
     */
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
