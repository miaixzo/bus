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
package org.miaixz.bus.image.metric.hl7;

import org.miaixz.bus.core.lang.Symbol;

/**
 * Represents the ERRSegment type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ERRSegment extends HL7Segment {

    /**
     * The segment sequence error value.
     */
    public static final String SEGMENT_SEQUENCE_ERROR = "100^Segment sequence error^HL70357";

    /**
     * The required field missing value.
     */
    public static final String REQUIRED_FIELD_MISSING = "101^Required field missing^HL70357";

    /**
     * The data type error value.
     */
    public static final String DATA_TYPE_ERROR = "102^Data type error^HL70357";

    /**
     * The table value not found value.
     */
    public static final String TABLE_VALUE_NOT_FOUND = "103^Table value not found^HL70357";

    /**
     * The unsupported message type value.
     */
    public static final String UNSUPPORTED_MESSAGE_TYPE = "200^Unsupported message type^HL70357";

    /**
     * The unsupported event code value.
     */
    public static final String UNSUPPORTED_EVENT_CODE = "201^Unsupported event code^HL70357";

    /**
     * The unsupported processing id value.
     */
    public static final String UNSUPPORTED_PROCESSING_ID = "202^Unsupported processing id^HL70357";

    /**
     * The unsupported version id value.
     */
    public static final String UNSUPPORTED_VERSION_ID = "203^Unsupported version id^HL70357";

    /**
     * The unknown key identifier value.
     */
    public static final String UNKNOWN_KEY_IDENTIFIER = "204^Unknown key identifier^HL70357";

    /**
     * The duplicate key identifier value.
     */
    public static final String DUPLICATE_KEY_IDENTIFIER = "205^Duplicate key identifier^HL70357";

    /**
     * The application record locked value.
     */
    public static final String APPLICATION_RECORD_LOCKED = "206^Application record locked^HL70357";

    /**
     * The application internal error value.
     */
    public static final String APPLICATION_INTERNAL_ERROR = "207^Application internal error^HL70357";

    /**
     * The sending application value.
     */
    public static final String SENDING_APPLICATION = "MSH^1^3^1^1";

    /**
     * The sending facility value.
     */
    public static final String SENDING_FACILITY = "MSH^1^4^1^1";

    /**
     * The receiving application value.
     */
    public static final String RECEIVING_APPLICATION = "MSH^1^5^1^1";

    /**
     * The receiving facility value.
     */
    public static final String RECEIVING_FACILITY = "MSH^1^6^1^1";

    /**
     * The message code value.
     */
    public static final String MESSAGE_CODE = "MSH^1^9^1^1";

    /**
     * The trigger event value.
     */
    public static final String TRIGGER_EVENT = "MSH^1^9^1^2";

    /**
     * The message datetime value.
     */
    public static final String MESSAGE_DATETIME = "MSH^1^7^1^1";

    /**
     * The message control id value.
     */
    public static final String MESSAGE_CONTROL_ID = "MSH^1^10^1^1";

    /**
     * The message processing id value.
     */
    public static final String MESSAGE_PROCESSING_ID = "MSH^1^11^1^1";

    /**
     * The message version id value.
     */
    public static final String MESSAGE_VERSION_ID = "MSH^1^12^1^1";

    /**
     * Creates a new instance.
     *
     * @param fieldSeparator     the field separator.
     * @param encodingCharacters the encoding characters.
     */
    public ERRSegment(char fieldSeparator, String encodingCharacters) {
        super(9, fieldSeparator, encodingCharacters);
        setField(0, "ERR");
        setHL7ErrorCode(APPLICATION_INTERNAL_ERROR);
        setSeverity("E");
    }

    /**
     * Creates a new instance.
     */
    public ERRSegment() {
        this(Symbol.C_OR, "^~\\&");
    }

    /**
     * Creates a new instance.
     *
     * @param msh the msh.
     */
    public ERRSegment(HL7Segment msh) {
        this(msh.getFieldSeparator(), msh.getEncodingCharacters());
    }

    /**
     * Sets the error location.
     *
     * @param errorLocation the error location.
     * @return the operation result.
     */
    public ERRSegment setErrorLocation(String errorLocation) {
        setField(2, errorLocation.replace('^', getComponentSeparator()));
        return this;
    }

    /**
     * Sets the hl7 error code.
     *
     * @param hl7ErrorCode the hl7 error code.
     * @return the operation result.
     */
    public ERRSegment setHL7ErrorCode(String hl7ErrorCode) {
        setField(3, hl7ErrorCode);
        return this;
    }

    /**
     * Sets the severity.
     *
     * @param severity the severity.
     * @return the operation result.
     */
    public ERRSegment setSeverity(String severity) {
        setField(4, severity);
        return this;
    }

    /**
     * Sets the application error code.
     *
     * @param applicationErrorCode the application error code.
     * @return the operation result.
     */
    public ERRSegment setApplicationErrorCode(String applicationErrorCode) {
        setField(5, applicationErrorCode);
        return this;
    }

    /**
     * Sets the application error parameter.
     *
     * @param applicationErrorParameter the application error parameter.
     * @return the operation result.
     */
    public ERRSegment setApplicationErrorParameter(String applicationErrorParameter) {
        setField(6, applicationErrorParameter);
        return this;
    }

    /**
     * Sets the diagnostic information.
     *
     * @param diagnosticInformation the diagnostic information.
     * @return the operation result.
     */
    public ERRSegment setDiagnosticInformation(String diagnosticInformation) {
        setField(7, diagnosticInformation);
        return this;
    }

    /**
     * Sets the user message.
     *
     * @param userMessage the user message.
     * @return the operation result.
     */
    public ERRSegment setUserMessage(String userMessage) {
        setField(8, userMessage);
        return this;
    }

}
