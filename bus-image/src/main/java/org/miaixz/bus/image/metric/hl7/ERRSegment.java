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
package org.miaixz.bus.image.metric.hl7;

import org.miaixz.bus.core.lang.Symbol;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class ERRSegment extends HL7Segment {

    public static final String SEGMENT_SEQUENCE_ERROR = "100^Segment sequence error^HL70357";
    public static final String REQUIRED_FIELD_MISSING = "101^Required field missing^HL70357";
    public static final String DATA_TYPE_ERROR = "102^Data type error^HL70357";
    public static final String TABLE_VALUE_NOT_FOUND = "103^Table value not found^HL70357";
    public static final String UNSUPPORTED_MESSAGE_TYPE = "200^Unsupported message type^HL70357";
    public static final String UNSUPPORTED_EVENT_CODE = "201^Unsupported event code^HL70357";
    public static final String UNSUPPORTED_PROCESSING_ID = "202^Unsupported processing id^HL70357";
    public static final String UNSUPPORTED_VERSION_ID = "203^Unsupported version id^HL70357";
    public static final String UNKNOWN_KEY_IDENTIFIER = "204^Unknown key identifier^HL70357";
    public static final String DUPLICATE_KEY_IDENTIFIER = "205^Duplicate key identifier^HL70357";
    public static final String APPLICATION_RECORD_LOCKED = "206^Application record locked^HL70357";
    public static final String APPLICATION_INTERNAL_ERROR = "207^Application internal error^HL70357";

    public static final String SENDING_APPLICATION = "MSH^1^3^1^1";
    public static final String SENDING_FACILITY = "MSH^1^4^1^1";
    public static final String RECEIVING_APPLICATION = "MSH^1^5^1^1";
    public static final String RECEIVING_FACILITY = "MSH^1^6^1^1";
    public static final String MESSAGE_CODE = "MSH^1^9^1^1";
    public static final String TRIGGER_EVENT = "MSH^1^9^1^2";
    public static final String MESSAGE_DATETIME = "MSH^1^7^1^1";
    public static final String MESSAGE_CONTROL_ID = "MSH^1^10^1^1";
    public static final String MESSAGE_PROCESSING_ID = "MSH^1^11^1^1";
    public static final String MESSAGE_VERSION_ID = "MSH^1^12^1^1";

    public ERRSegment(char fieldSeparator, String encodingCharacters) {
        super(9, fieldSeparator, encodingCharacters);
        setField(0, "ERR");
        setHL7ErrorCode(APPLICATION_INTERNAL_ERROR);
        setSeverity("E");
    }

    public ERRSegment() {
        this(Symbol.C_OR, "^~\\&");
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
