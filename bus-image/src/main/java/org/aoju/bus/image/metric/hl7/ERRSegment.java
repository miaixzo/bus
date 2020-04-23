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
package org.aoju.bus.image.metric.hl7;

/**
 * @author Kimi Liu
 * @version 5.8.8
 * @since JDK 1.8+
 */
public class ERRSegment extends HL7Segment {

    public static final String SegmentSequenceError = "100";
    public static final String RequiredFieldMissing = "101";
    public static final String DataTypeError = "102";
    public static final String TableValueNotFound = "103";
    public static final String UnsupportedMessageType = "200";
    public static final String UnsupportedEventCode = "201";
    public static final String UnsupportedProcessingID = "202";
    public static final String UnsupportedVersionID = "203";
    public static final String UnknownKeyIdentifier = "204";
    public static final String DuplicateKeyIdentifier = "205";
    public static final String ApplicationRecordLocked = "206";
    public static final String ApplicationInternalError = "207";

    public static final String UnknownSendingApplication = "MSH^1^3";
    public static final String UnknownSendingFacility = "MSH^1^4";
    public static final String UnknownReceivingApplication = "MSH^1^5";
    public static final String UnknownReceivingFacility = "MSH^1^6";

    public ERRSegment(char fieldSeparator, String encodingCharacters) {
        super(9, fieldSeparator, encodingCharacters);
        setField(0, "ERR");
        setHL7ErrorCode(ApplicationInternalError);
        setSeverity("E");
    }

    public ERRSegment() {
        this('|', "^~\\&");
    }

    public ERRSegment(HL7Segment msh) {
        this(msh.getFieldSeparator(), msh.getEncodingCharacters());
    }

    public ERRSegment setErrorLocation(String errorLocation) {
        setField(2, errorLocation.replace('^', getComponentSeparator()));
        return this;
    }

    public ERRSegment setHL7ErrorCode(String hl7ErrorCode) {
        setField(3, hl7ErrorCode);
        return this;
    }

    public ERRSegment setSeverity(String severity) {
        setField(4, severity);
        return this;
    }

    public ERRSegment setApplicationErrorCode(String applicationErrorCode) {
        setField(5, applicationErrorCode);
        return this;
    }

    public ERRSegment setApplicationErrorParameter(String applicationErrorParameter) {
        setField(6, applicationErrorParameter);
        return this;
    }

    public ERRSegment setDiagnosticInformation(String diagnosticInformation) {
        setField(7, diagnosticInformation);
        return this;
    }

    public ERRSegment setUserMessage(String userMessage) {
        setField(8, userMessage);
        return this;
    }
}
