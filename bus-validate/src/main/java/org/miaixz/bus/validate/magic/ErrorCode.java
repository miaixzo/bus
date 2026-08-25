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
package org.miaixz.bus.validate.magic;

import org.miaixz.bus.core.basic.normal.ErrorRegistry;
import org.miaixz.bus.core.basic.normal.Errors;

/**
 * Validation error codes, starting from 115xxx.
 *
 * @author Kimi Liu
 */
public class ErrorCode extends org.miaixz.bus.core.basic.normal.ErrorCode {

    /**
     * Error code for a generic parameter validation failure.
     */
    public static final String _PARAMETER_VALIDATE = "115000";

    /**
     * Default message template for a generic parameter validation failure.
     */
    public static final String _PARAMETER_VALIDATE_VALUE = "Parameter validation failed";

    /**
     * Error code for a value that must be blank.
     */
    public static final String _BLANK = "115001";

    /**
     * Default message template for a value that must be blank.
     */
    public static final String _BLANK_VALUE = "${field} must be blank";

    /**
     * Error code for Chinese text validation failure.
     */
    public static final String _CHINESE = "115002";

    /**
     * Default message template for Chinese text validation failure.
     */
    public static final String _CHINESE_VALUE = "${field} must be in Chinese";

    /**
     * Error code for citizen ID validation failure.
     */
    public static final String _CITIZEN_ID = "115003";

    /**
     * Default message template for citizen ID validation failure.
     */
    public static final String _CITIZEN_ID_VALUE = "${field} is not a valid citizen ID number";

    /**
     * Error code for comparison validation failure.
     */
    public static final String _COMPARE = "115004";

    /**
     * Default message template for comparison validation failure.
     */
    public static final String _COMPARE_VALUE = "The value of ${field} does not meet the comparison rule";

    /**
     * Error code for date format validation failure.
     */
    public static final String _DATE = "115005";

    /**
     * Default message template for date format validation failure.
     */
    public static final String _DATE_VALUE = "${field} is not in the correct format";

    /**
     * Error code for element validation failure.
     */
    public static final String _EACH = "115006";

    /**
     * Default message template for element validation failure.
     */
    public static final String _EACH_VALUE = "Validation failed for parameter ${field}";

    /**
     * Error code for email format validation failure.
     */
    public static final String _EMAIL = "115007";

    /**
     * Default message template for email format validation failure.
     */
    public static final String _EMAIL_VALUE = "${field} is not a valid email format";

    /**
     * Error code for English text validation failure.
     */
    public static final String _ENGLISH = "115008";

    /**
     * Default message template for English text validation failure.
     */
    public static final String _ENGLISH_VALUE = "${field} must be in English";

    /**
     * Error code for equality validation failure.
     */
    public static final String _EQUALS = "115009";

    /**
     * Default message template for equality validation failure.
     */
    public static final String _EQUALS_VALUE = "${field} must be equal to the specified string: ${value}";

    /**
     * Error code for false value validation failure.
     */
    public static final String _FALSE = "115010";

    /**
     * Default message template for false value validation failure.
     */
    public static final String _FALSE_VALUE = "${field} must be false";

    /**
     * Error code for inclusion validation failure.
     */
    public static final String _IN = "115011";

    /**
     * Default message template for inclusion validation failure.
     */
    public static final String _IN_VALUE = "${field} must be one of the specified strings: ${value}";

    /**
     * Error code for enum inclusion validation failure.
     */
    public static final String _IN_ENUM = "115012";

    /**
     * Default message template for enum inclusion validation failure.
     */
    public static final String _IN_ENUM_VALUE = "${field} must be a value of the specified enum type: ${enumClass}";

    /**
     * Error code for integer range validation failure.
     */
    public static final String _INT_RANGE = "115013";

    /**
     * Default message template for integer range validation failure.
     */
    public static final String _INT_RANGE_VALUE = "${field} must be within the specified range, min: ${min}, max: ${max}";

    /**
     * Error code for IP address validation failure.
     */
    public static final String _IP_ADDRESS = "115014";

    /**
     * Default message template for IP address validation failure.
     */
    public static final String _IP_ADDRESS_VALUE = "${field} is not a valid IP address";

    /**
     * Error code for text length validation failure.
     */
    public static final String _LENGTH = "115015";

    /**
     * Default message template for text length validation failure.
     */
    public static final String _LENGTH_VALUE = "The length of ${field} must be within the specified range. Min: ${min}, Max: ${max}";

    /**
     * Error code for mobile phone number validation failure.
     */
    public static final String _MOBILE = "115016";

    /**
     * Default message template for mobile phone number validation failure.
     */
    public static final String _MOBILE_VALUE = "${field} must be a valid mobile phone number";

    /**
     * Error code for combined validation failure.
     */
    public static final String _MULTIPLE = "115017";

    /**
     * Default message template for combined validation failure.
     */
    public static final String _MULTIPLE_VALUE = "Validation failed for parameter ${field}";

    /**
     * Error code for non-blank validation failure.
     */
    public static final String _NOT_BLANK = "115018";

    /**
     * Default message template for non-blank validation failure.
     */
    public static final String _NOT_BLANK_VALUE = "${field} cannot be blank";

    /**
     * Error code for non-empty validation failure.
     */
    public static final String _NOT_EMPTY = "115019";

    /**
     * Default message template for non-empty validation failure.
     */
    public static final String _NOT_EMPTY_VALUE = "${field} cannot be empty";

    /**
     * Error code for exclusion validation failure.
     */
    public static final String _NOT_IN = "115020";

    /**
     * Default message template for exclusion validation failure.
     */
    public static final String _NOT_IN_VALUE = "${field} must be excluded from the specified array: ${value}";

    /**
     * Error code for non-null validation failure.
     */
    public static final String _NOT_NULL = "115021";

    /**
     * Default message template for non-null validation failure.
     */
    public static final String _NOT_NULL_VALUE = "${field} cannot be null";

    /**
     * Error code for null validation failure.
     */
    public static final String _NULL = "115022";

    /**
     * Default message template for null validation failure.
     */
    public static final String _NULL_VALUE = "${field} must be null";

    /**
     * Error code for landline phone number validation failure.
     */
    public static final String _PHONE = "115023";

    /**
     * Default message template for landline phone number validation failure.
     */
    public static final String _PHONE_VALUE = "${field} must be a valid landline phone number";

    /**
     * Error code for reflective validation failure.
     */
    public static final String _REFLECT = "115024";

    /**
     * Default message template for reflective validation failure.
     */
    public static final String _REFLECT_VALUE = "Validation failed for parameter ${field}";

    /**
     * Error code for regular expression validation failure.
     */
    public static final String _REGEX = "115025";

    /**
     * Default message template for regular expression validation failure.
     */
    public static final String _REGEX_VALUE = "Validation failed for ${field}, please check the data format";

    /**
     * Error code for size validation failure.
     */
    public static final String _SIZE = "115026";

    /**
     * Default message template for size validation failure.
     */
    public static final String _SIZE_VALUE = "The size of ${field} must be within the specified range. Min: ${min}, Max: ${max}";

    /**
     * Error code for true value validation failure.
     */
    public static final String _TRUE = "115027";

    /**
     * Default message template for true value validation failure.
     */
    public static final String _TRUE_VALUE = "${field} must be true";

    /**
     * Generic parameter validation failure.
     */
    public static final Errors _115000 = ErrorRegistry.register(_PARAMETER_VALIDATE, _PARAMETER_VALIDATE_VALUE);

    /**
     * A value that must be blank is not blank.
     */
    public static final Errors _115001 = ErrorRegistry.register(_BLANK, _BLANK_VALUE);

    /**
     * A value that must be Chinese text is invalid.
     */
    public static final Errors _115002 = ErrorRegistry.register(_CHINESE, _CHINESE_VALUE);

    /**
     * A citizen ID number is invalid.
     */
    public static final Errors _115003 = ErrorRegistry.register(_CITIZEN_ID, _CITIZEN_ID_VALUE);

    /**
     * A comparison rule is not satisfied.
     */
    public static final Errors _115004 = ErrorRegistry.register(_COMPARE, _COMPARE_VALUE);

    /**
     * A date value has an invalid format.
     */
    public static final Errors _115005 = ErrorRegistry.register(_DATE, _DATE_VALUE);

    /**
     * An element validation rule failed.
     */
    public static final Errors _115006 = ErrorRegistry.register(_EACH, _EACH_VALUE);

    /**
     * An email address has an invalid format.
     */
    public static final Errors _115007 = ErrorRegistry.register(_EMAIL, _EMAIL_VALUE);

    /**
     * A value that must be English text is invalid.
     */
    public static final Errors _115008 = ErrorRegistry.register(_ENGLISH, _ENGLISH_VALUE);

    /**
     * A value does not equal the required value.
     */
    public static final Errors _115009 = ErrorRegistry.register(_EQUALS, _EQUALS_VALUE);

    /**
     * A value that must be false is invalid.
     */
    public static final Errors _115010 = ErrorRegistry.register(_FALSE, _FALSE_VALUE);

    /**
     * A value is not included in the allowed set.
     */
    public static final Errors _115011 = ErrorRegistry.register(_IN, _IN_VALUE);

    /**
     * A value is not included in the allowed enum set.
     */
    public static final Errors _115012 = ErrorRegistry.register(_IN_ENUM, _IN_ENUM_VALUE);

    /**
     * An integer value is outside the allowed range.
     */
    public static final Errors _115013 = ErrorRegistry.register(_INT_RANGE, _INT_RANGE_VALUE);

    /**
     * An IP address is invalid.
     */
    public static final Errors _115014 = ErrorRegistry.register(_IP_ADDRESS, _IP_ADDRESS_VALUE);

    /**
     * A text value length is outside the allowed range.
     */
    public static final Errors _115015 = ErrorRegistry.register(_LENGTH, _LENGTH_VALUE);

    /**
     * A mobile phone number is invalid.
     */
    public static final Errors _115016 = ErrorRegistry.register(_MOBILE, _MOBILE_VALUE);

    /**
     * A combined validation rule failed.
     */
    public static final Errors _115017 = ErrorRegistry.register(_MULTIPLE, _MULTIPLE_VALUE);

    /**
     * A required non-blank value is blank.
     */
    public static final Errors _115018 = ErrorRegistry.register(_NOT_BLANK, _NOT_BLANK_VALUE);

    /**
     * A required non-empty value is empty.
     */
    public static final Errors _115019 = ErrorRegistry.register(_NOT_EMPTY, _NOT_EMPTY_VALUE);

    /**
     * A value is included in the disallowed set.
     */
    public static final Errors _115020 = ErrorRegistry.register(_NOT_IN, _NOT_IN_VALUE);

    /**
     * A required non-null value is null.
     */
    public static final Errors _115021 = ErrorRegistry.register(_NOT_NULL, _NOT_NULL_VALUE);

    /**
     * A value that must be null is not null.
     */
    public static final Errors _115022 = ErrorRegistry.register(_NULL, _NULL_VALUE);

    /**
     * A landline phone number is invalid.
     */
    public static final Errors _115023 = ErrorRegistry.register(_PHONE, _PHONE_VALUE);

    /**
     * A reflective validation rule failed.
     */
    public static final Errors _115024 = ErrorRegistry.register(_REFLECT, _REFLECT_VALUE);

    /**
     * A value does not match the required regular expression.
     */
    public static final Errors _115025 = ErrorRegistry.register(_REGEX, _REGEX_VALUE);

    /**
     * A value size is outside the allowed range.
     */
    public static final Errors _115026 = ErrorRegistry.register(_SIZE, _SIZE_VALUE);

    /**
     * A value that must be true is invalid.
     */
    public static final Errors _115027 = ErrorRegistry.register(_TRUE, _TRUE_VALUE);

    /**
     * Constructs a new ErrorCode instance.
     */
    public ErrorCode() {
        // No initialization required.
    }

}
