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
package org.miaixz.bus.core.basic.normal;

import org.miaixz.bus.core.lang.Symbol;

/**
 * Defines global and common error codes. This class can be extended to include product-specific error codes.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ErrorCode {

    /**
     * Constructs a new AbstractProvider with default settings.
     */
    public ErrorCode() {

    }

    /**
     * Common: Request was successful.
     */
    public static final Errors _SUCCESS = ErrorRegistry.builder().key(Symbol.ZERO).value("Success").build();

    /**
     * Common: System is busy, please try again later.
     */
    public static final Errors _FAILURE = ErrorRegistry.builder().key("-1")
            .value("System is busy, please try again later").build();

    /**
     * Common: Request is too frequent, please try again later.
     */
    public static final Errors _LIMITER = ErrorRegistry.builder().key("-2")
            .value("Request frequency too high, please wait and try again later").build();

    /**
     * Common: Illegal request, please try again later.
     */
    public static final Errors _BLOCKED = ErrorRegistry.builder().key("-3")
            .value("Illegal or blocked request, please check parameters and try again").build();

    /**
     * Common: An unknown error has occurred, please try again later.
     */
    public static final Errors _UNKNOWN = ErrorRegistry.builder().key("-4")
            .value("An unknown error has occurred, please try again later").build();

    /**
     * Request: Invalid token.
     */
    public static final Errors _100100 = ErrorRegistry.builder().key("100100").value("Invalid token").build();

    /**
     * Request: Invalid parameter.
     */
    public static final Errors _100101 = ErrorRegistry.builder().key("100101").value("Invalid parameter").build();

    /**
     * Request: Invalid version.
     */
    public static final Errors _100102 = ErrorRegistry.builder().key("100102").value("Invalid version").build();

    /**
     * Request: Invalid method.
     */
    public static final Errors _100103 = ErrorRegistry.builder().key("100103").value("Invalid method").build();

    /**
     * Request: Invalid language.
     */
    public static final Errors _100104 = ErrorRegistry.builder().key("100104").value("Invalid language").build();

    /**
     * Request: Invalid format type.
     */
    public static final Errors _100105 = ErrorRegistry.builder().key("100105").value("Invalid format type").build();

    /**
     * Request: Missing token.
     */
    public static final Errors _100106 = ErrorRegistry.builder().key("100106").value("Missing token").build();

    /**
     * Request: Missing version.
     */
    public static final Errors _100107 = ErrorRegistry.builder().key("100107").value("Missing version").build();

    /**
     * Request: Missing method.
     */
    public static final Errors _100108 = ErrorRegistry.builder().key("100108").value("Missing method").build();

    /**
     * Request: Missing language.
     */
    public static final Errors _100109 = ErrorRegistry.builder().key("100109").value("Missing language").build();

    /**
     * Request: Missing fields.
     */
    public static final Errors _100110 = ErrorRegistry.builder().key("100110").value("Missing fields").build();

    /**
     * Request: Missing format.
     */
    public static final Errors _100111 = ErrorRegistry.builder().key("100111").value("Missing format").build();

    /**
     * Request: Missing sign.
     */
    public static final Errors _100112 = ErrorRegistry.builder().key("100112").value("Missing sign").build();

    /**
     * Request: Missing nonce.
     */
    public static final Errors _100113 = ErrorRegistry.builder().key("100113").value("Missing nonce").build();

    /**
     * Request: Missing timestamp.
     */
    public static final Errors _100114 = ErrorRegistry.builder().key("100114").value("Missing timestamp").build();

    /**
     * Request: Missing sign (duplicate of 100112, should be reviewed).
     */
    public static final Errors _100115 = ErrorRegistry.builder().key("100115").value("Missing sign").build();

    /**
     * Request: Missing sign (duplicate of 100112, should be reviewed).
     */
    public static final Errors _100116 = ErrorRegistry.builder().key("100116").value("Missing parameters").build();

    /**
     * Request: Invalid ApiKey.
     */
    public static final Errors _100117 = ErrorRegistry.builder().key("100117").value("Invalid ApiKey").build();

    /**
     * Request: Invalid verification code.
     */
    public static final Errors _100118 = ErrorRegistry.builder().key("100118").value("Invalid verification code")
            .build();

    /**
     * Request: Please use GET request.
     */
    public static final Errors _100200 = ErrorRegistry.builder().key("100200").value("Please use GET request").build();

    /**
     * Request: Please use POST request.
     */
    public static final Errors _100201 = ErrorRegistry.builder().key("100201").value("Please use POST request").build();

    /**
     * Request: Please use PUT request.
     */
    public static final Errors _100202 = ErrorRegistry.builder().key("100202").value("Please use PUT request").build();

    /**
     * Request: Please use DELETE request.
     */
    public static final Errors _100203 = ErrorRegistry.builder().key("100203").value("Please use DELETE request")
            .build();

    /**
     * Request: Please use OPTIONS request.
     */
    public static final Errors _100204 = ErrorRegistry.builder().key("100204").value("Please use OPTIONS request")
            .build();

    /**
     * Request: Please use HEAD request.
     */
    public static final Errors _100205 = ErrorRegistry.builder().key("100205").value("Please use HEAD request").build();

    /**
     * Request: Please use PATCH request.
     */
    public static final Errors _100206 = ErrorRegistry.builder().key("100206").value("Please use PATCH request")
            .build();

    /**
     * Request: Please use TRACE request.
     */
    public static final Errors _100207 = ErrorRegistry.builder().key("100207").value("Please use TRACE request")
            .build();

    /**
     * Request: Please use CONNECT request.
     */
    public static final Errors _100208 = ErrorRegistry.builder().key("100208").value("Please use CONNECT request")
            .build();

    /**
     * Request: Please use HTTPS protocol.
     */
    public static final Errors _100209 = ErrorRegistry.builder().key("100209").value("Please use HTTPS protocol")
            .build();

    /**
     * Request: Invalid signature information.
     */
    public static final Errors _100300 = ErrorRegistry.builder().key("100300").value("Invalid signature information")
            .build();

    /**
     * Data: Date formatting error.
     */
    public static final Errors _100301 = ErrorRegistry.builder().key("100301").value("Date formatting error").build();

    /**
     * Data: JSON format error.
     */
    public static final Errors _100302 = ErrorRegistry.builder().key("100302").value("JSON format error").build();

    /**
     * Data: File format error.
     */
    public static final Errors _100303 = ErrorRegistry.builder().key("100303").value("File format error").build();

    /**
     * Data: Error converting JSON/XML.
     */
    public static final Errors _100304 = ErrorRegistry.builder().key("100304").value("Error converting JSON/XML")
            .build();

    /**
     * Data: No data available.
     */
    public static final Errors _100500 = ErrorRegistry.builder().key("100500").value("No data available").build();

    /**
     * Data: Data already exists.
     */
    public static final Errors _100501 = ErrorRegistry.builder().key("100501").value("Data already exists").build();

    /**
     * Data: Data does not exist.
     */
    public static final Errors _100502 = ErrorRegistry.builder().key("100502").value("Data does not exist").build();

    /**
     * User: Account is frozen.
     */
    public static final Errors _100503 = ErrorRegistry.builder().key("100503").value("Account is frozen").build();

    /**
     * User: Account already exists.
     */
    public static final Errors _100504 = ErrorRegistry.builder().key("100504").value("Account already exists").build();

    /**
     * User: Account does not exist.
     */
    public static final Errors _100505 = ErrorRegistry.builder().key("100505").value("Account does not exist").build();

    /**
     * User: Account not bound.
     */
    public static final Errors _100506 = ErrorRegistry.builder().key("100506").value("Account not bound").build();

    /**
     * User: Current token has expired.
     */
    public static final Errors _100507 = ErrorRegistry.builder().key("100507").value("Current token has expired")
            .build();

    /**
     * User: Current account is already logged in.
     */
    public static final Errors _100508 = ErrorRegistry.builder().key("100508")
            .value("Current account is already logged in").build();

    /**
     * User: Account is abnormal, please contact the administrator.
     */
    public static final Errors _100509 = ErrorRegistry.builder().key("100509")
            .value("Account is abnormal, please contact the administrator").build();

    /**
     * User: Account is locked, please try again later.
     */
    public static final Errors _100510 = ErrorRegistry.builder().key("100510")
            .value("Account is locked, please try again later").build();

    /**
     * User: Incorrect username or password.
     */
    public static final Errors _100511 = ErrorRegistry.builder().key("100511").value("Incorrect username or password")
            .build();

    /**
     * User: Failed to send verification code.
     */
    public static final Errors _100512 = ErrorRegistry.builder().key("100512").value("Failed to send verification code")
            .build();

    /**
     * User: Incorrect verification code.
     */
    public static final Errors _100513 = ErrorRegistry.builder().key("100513").value("Incorrect verification code")
            .build();

    /**
     * User: Password length does not meet requirements.
     */
    public static final Errors _100514 = ErrorRegistry.builder().key("100514")
            .value("Password length does not meet requirements").build();

    /**
     * User: Password must contain both uppercase and lowercase letters.
     */
    public static final Errors _100515 = ErrorRegistry.builder().key("100515")
            .value("Password must contain both uppercase and lowercase letters").build();

    /**
     * User: Password must contain special characters.
     */
    public static final Errors _100516 = ErrorRegistry.builder().key("100516")
            .value("Password must contain special characters").build();

    /**
     * User: Duplicate mobile number.
     */
    public static final Errors _100517 = ErrorRegistry.builder().key("100517").value("Duplicate mobile number").build();

    /**
     * Data: Duplicate name.
     */
    public static final Errors _100518 = ErrorRegistry.builder().key("100518").value("Duplicate name").build();

    /**
     * User: Invalid credential.
     */
    public static final Errors _100519 = ErrorRegistry.builder().key("100519").value("Invalid credential").build();

    /**
     * Data: Department already exists.
     */
    public static final Errors _100520 = ErrorRegistry.builder().key("100520").value("Department already exists")
            .build();

    /**
     * Data: Employee ID already exists.
     */
    public static final Errors _100521 = ErrorRegistry.builder().key("100521").value("Employee ID already exists")
            .build();

    /**
     * User: Incorrect login-free authorization code.
     */
    public static final Errors _100522 = ErrorRegistry.builder().key("100522")
            .value("Incorrect login-free authorization code").build();

    /**
     * User: Mobile number not bound.
     */
    public static final Errors _100523 = ErrorRegistry.builder().key("100523").value("Mobile number not bound").build();

    /**
     * License: Invalid license.
     */
    public static final Errors _100524 = ErrorRegistry.builder().key("100524").value("Invalid license").build();

    /**
     * License: License has expired.
     */
    public static final Errors _100525 = ErrorRegistry.builder().key("100525").value("License has expired").build();

    /**
     * License: License verification failed.
     */
    public static final Errors _100526 = ErrorRegistry.builder().key("100526").value("License verification failed")
            .build();

    /**
     * License: Please contact the official provider to activate.
     */
    public static final Errors _100527 = ErrorRegistry.builder().key("100527")
            .value("Please contact the official provider to activate").build();

    /**
     * License: License issuance failed.
     */
    public static final Errors _100528 = ErrorRegistry.builder().key("100528").value("License issuance failed").build();

    /**
     * License: Incorrect license information.
     */
    public static final Errors _100529 = ErrorRegistry.builder().key("100529").value("Incorrect license information")
            .build();

    /**
     * Request body is too large.
     */
    public static final Errors _100530 = ErrorRegistry.builder().key("100530").value("Request body is too large")
            .build();

    /**
     * Operation: Permission denied.
     */
    public static final Errors _100800 = ErrorRegistry.builder().key("100800").value("Permission denied").build();

    /**
     * Operation: Unsupported operation.
     */
    public static final Errors _100801 = ErrorRegistry.builder().key("100801").value("Unsupported operation").build();

    /**
     * Request: Request method not supported.
     */
    public static final Errors _100802 = ErrorRegistry.builder().key("100802").value("Request method not supported")
            .build();

    /**
     * Data: This type is not supported.
     */
    public static final Errors _100803 = ErrorRegistry.builder().key("100803").value("This type is not supported")
            .build();

    /**
     * Resource: Resource not found.
     */
    public static final Errors _100804 = ErrorRegistry.builder().key("100804").value("Resource not found").build();

    /**
     * System: Internal processing exception.
     */
    public static final Errors _100805 = ErrorRegistry.builder().key("100805").value("Internal processing exception")
            .build();

    /**
     * System: Authorization processing exception.
     */
    public static final Errors _100806 = ErrorRegistry.builder().key("100806")
            .value("Authorization processing exception").build();

    /**
     * Business: Business processing failed.
     */
    public static final Errors _100807 = ErrorRegistry.builder().key("100807").value("Business processing failed")
            .build();

    /**
     * System: Task execution failed.
     */
    public static final Errors _100808 = ErrorRegistry.builder().key("100808").value("Task execution failed").build();

    /**
     * Request: Parameter binding exception.
     */
    public static final Errors _100809 = ErrorRegistry.builder().key("100809").value("Parameter binding exception")
            .build();

    /**
     * Request: Link has expired.
     */
    public static final Errors _100810 = ErrorRegistry.builder().key("100810").value("Link has expired").build();

    /**
     * Request: Request timed out.
     */
    public static final Errors _100811 = ErrorRegistry.builder().key("100811").value("Request timed out").build();

}
