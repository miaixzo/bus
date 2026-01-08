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

    // =================================================================================================================
    // Global & System Codes (-1 ~ -99)
    // =================================================================================================================

    /**
     * Request was successful.
     */
    public static final Errors _SUCCESS = ErrorRegistry.builder().key(Symbol.ZERO).value("OK").build();

    /**
     * System is busy, please try again later.
     */
    public static final Errors _FAILURE = ErrorRegistry.builder().key("-1")
            .value("System is busy, please try again later").build();

    /**
     * Request is too frequent, please try again later.
     */
    public static final Errors _LIMITER = ErrorRegistry.builder().key("-2")
            .value("Request frequency too high, please wait and try again later").build();

    /**
     * Illegal request, please try again later.
     */
    public static final Errors _BLOCKED = ErrorRegistry.builder().key("-3")
            .value("Illegal or blocked request, please check parameters and try again").build();

    /**
     * An unknown error has occurred, please try again later.
     */
    public static final Errors _UNKNOWN = ErrorRegistry.builder().key("-4")
            .value("An unknown error has occurred, please try again later").build();

    /**
     * Service maintenance, please try again later.
     */
    public static final Errors _MAINTENANCE = ErrorRegistry.builder().key("-5")
            .value("Service under maintenance, please try again later").build();

    /**
     * Service degradation triggered.
     */
    public static final Errors _DEGRADATION = ErrorRegistry.builder().key("-6").value("Service degradation triggered")
            .build();

    // =================================================================================================================
    // 1001xx: Request Parameters & Validation (Strictly Paired: Missing -> Invalid)
    // =================================================================================================================

    /**
     * Request: Missing parameters.
     */
    public static final Errors _100100 = ErrorRegistry.builder().key("100100").value("Missing parameters").build();

    /**
     * Request: Invalid parameter.
     */
    public static final Errors _100101 = ErrorRegistry.builder().key("100101").value("Invalid parameter").build();

    /**
     * Request: Missing method.
     */
    public static final Errors _100102 = ErrorRegistry.builder().key("100102").value("Missing method").build();

    /**
     * Request: Invalid method.
     */
    public static final Errors _100103 = ErrorRegistry.builder().key("100103").value("Invalid method").build();

    /**
     * Request: Missing format.
     */
    public static final Errors _100104 = ErrorRegistry.builder().key("100104").value("Missing format").build();

    /**
     * Request: Invalid format.
     */
    public static final Errors _100105 = ErrorRegistry.builder().key("100105").value("Invalid format").build();

    /**
     * Request: Missing version.
     */
    public static final Errors _100106 = ErrorRegistry.builder().key("100106").value("Missing version").build();

    /**
     * Request: Invalid version.
     */
    public static final Errors _100107 = ErrorRegistry.builder().key("100107").value("Invalid version").build();

    /**
     * Request: Missing sign.
     */
    public static final Errors _100108 = ErrorRegistry.builder().key("100108").value("Missing sign").build();

    /**
     * Request: Invalid sign.
     */
    public static final Errors _100109 = ErrorRegistry.builder().key("100109").value("Invalid sign").build();

    /**
     * Request: Missing timestamp.
     */
    public static final Errors _100110 = ErrorRegistry.builder().key("100110").value("Missing timestamp").build();

    /**
     * Request: Invalid timestamp.
     */
    public static final Errors _100111 = ErrorRegistry.builder().key("100111").value("Invalid timestamp").build();

    /**
     * Request: Missing token.
     */
    public static final Errors _100112 = ErrorRegistry.builder().key("100112").value("Missing token").build();

    /**
     * Request: Invalid token.
     */
    public static final Errors _100113 = ErrorRegistry.builder().key("100113").value("Invalid token").build();

    /**
     * Request: Missing language.
     */
    public static final Errors _100114 = ErrorRegistry.builder().key("100114").value("Missing language").build();

    /**
     * Request: Invalid language.
     */
    public static final Errors _100115 = ErrorRegistry.builder().key("100115").value("Invalid language").build();

    /**
     * Request: Missing fields.
     */
    public static final Errors _100116 = ErrorRegistry.builder().key("100116").value("Missing fields").build();

    /**
     * Request: Invalid fields.
     */
    public static final Errors _100117 = ErrorRegistry.builder().key("100117").value("Invalid fields").build();

    /**
     * Request: Missing ApiKey.
     */
    public static final Errors _100118 = ErrorRegistry.builder().key("100118").value("Missing ApiKey").build();

    /**
     * Request: Invalid ApiKey.
     */
    public static final Errors _100119 = ErrorRegistry.builder().key("100119").value("Invalid ApiKey").build();

    /**
     * Request: Missing verification code.
     */
    public static final Errors _100120 = ErrorRegistry.builder().key("100120").value("Missing verification code")
            .build();

    /**
     * Request: Invalid verification code.
     */
    public static final Errors _100121 = ErrorRegistry.builder().key("100121").value("Invalid verification code")
            .build();

    /**
     * Request: Missing ID parameter.
     */
    public static final Errors _100122 = ErrorRegistry.builder().key("100122").value("Missing ID parameter").build();

    /**
     * Request: Invalid ID parameter.
     */
    public static final Errors _100123 = ErrorRegistry.builder().key("100123").value("Invalid ID parameter").build();

    /**
     * Request: Missing type parameter.
     */
    public static final Errors _100124 = ErrorRegistry.builder().key("100124").value("Missing type parameter").build();

    /**
     * Request: Invalid type parameter.
     */
    public static final Errors _100125 = ErrorRegistry.builder().key("100125").value("Invalid type parameter").build();

    /**
     * Request: Missing date parameter.
     */
    public static final Errors _100126 = ErrorRegistry.builder().key("100126").value("Missing date parameter").build();

    /**
     * Request: Invalid date parameter.
     */
    public static final Errors _100127 = ErrorRegistry.builder().key("100127").value("Invalid date parameter").build();

    /**
     * Request: Missing status value.
     */
    public static final Errors _100128 = ErrorRegistry.builder().key("100128").value("Missing status value").build();

    /**
     * Request: Invalid status value.
     */
    public static final Errors _100129 = ErrorRegistry.builder().key("100129").value("Invalid status value").build();

    /**
     * Request: Missing header info.
     */
    public static final Errors _100130 = ErrorRegistry.builder().key("100130").value("Missing header info").build();

    /**
     * Request: Invalid header info.
     */
    public static final Errors _100131 = ErrorRegistry.builder().key("100131").value("Invalid header info").build();

    /**
     * Request: Missing config info.
     */
    public static final Errors _100132 = ErrorRegistry.builder().key("100132").value("Missing config info").build();

    /**
     * Request: Invalid config info.
     */
    public static final Errors _100133 = ErrorRegistry.builder().key("100133").value("Invalid config info").build();

    /**
     * Request: Missing username.
     */
    public static final Errors _100134 = ErrorRegistry.builder().key("100134").value("Missing username").build();

    /**
     * Request: Invalid username.
     */
    public static final Errors _100135 = ErrorRegistry.builder().key("100135").value("Invalid username").build();

    /**
     * Request: Missing password.
     */
    public static final Errors _100136 = ErrorRegistry.builder().key("100136").value("Missing password").build();

    /**
     * Request: Invalid password format.
     */
    public static final Errors _100137 = ErrorRegistry.builder().key("100137").value("Invalid password format").build();

    /**
     * Request: Missing phone number.
     */
    public static final Errors _100138 = ErrorRegistry.builder().key("100138").value("Missing phone number").build();

    /**
     * Request: Invalid phone number.
     */
    public static final Errors _100139 = ErrorRegistry.builder().key("100139").value("Invalid phone number").build();

    /**
     * Request: Missing email address.
     */
    public static final Errors _100140 = ErrorRegistry.builder().key("100140").value("Missing email address").build();

    /**
     * Request: Invalid email address.
     */
    public static final Errors _100141 = ErrorRegistry.builder().key("100141").value("Invalid email address").build();

    /**
     * Request: Missing URL.
     */
    public static final Errors _100142 = ErrorRegistry.builder().key("100142").value("Missing URL").build();

    /**
     * Request: Invalid URL.
     */
    public static final Errors _100143 = ErrorRegistry.builder().key("100143").value("Invalid URL").build();

    /**
     * Request: Missing IP address.
     */
    public static final Errors _100144 = ErrorRegistry.builder().key("100144").value("Missing IP address").build();

    /**
     * Request: Invalid IP address.
     */
    public static final Errors _100145 = ErrorRegistry.builder().key("100145").value("Invalid IP address").build();

    /**
     * Request: Missing port number.
     */
    public static final Errors _100146 = ErrorRegistry.builder().key("100146").value("Missing port number").build();

    /**
     * Request: Invalid port number.
     */
    public static final Errors _100147 = ErrorRegistry.builder().key("100147").value("Invalid port number").build();

    /**
     * Request: Missing amount.
     */
    public static final Errors _100148 = ErrorRegistry.builder().key("100148").value("Missing amount").build();

    /**
     * Request: Invalid amount.
     */
    public static final Errors _100149 = ErrorRegistry.builder().key("100149").value("Invalid amount").build();

    /**
     * Request: Missing currency.
     */
    public static final Errors _100150 = ErrorRegistry.builder().key("100150").value("Missing currency").build();

    /**
     * Request: Invalid currency.
     */
    public static final Errors _100151 = ErrorRegistry.builder().key("100151").value("Invalid currency").build();

    /**
     * Request: Missing order ID.
     */
    public static final Errors _100152 = ErrorRegistry.builder().key("100152").value("Missing order ID").build();

    /**
     * Request: Invalid order ID.
     */
    public static final Errors _100153 = ErrorRegistry.builder().key("100153").value("Invalid order ID").build();

    /**
     * Request: Missing user ID.
     */
    public static final Errors _100154 = ErrorRegistry.builder().key("100154").value("Missing user ID").build();

    /**
     * Request: Invalid user ID.
     */
    public static final Errors _100155 = ErrorRegistry.builder().key("100155").value("Invalid user ID").build();

    /**
     * Request: Missing path.
     */
    public static final Errors _100156 = ErrorRegistry.builder().key("100156").value("Missing path").build();

    /**
     * Request: Invalid path.
     */
    public static final Errors _100157 = ErrorRegistry.builder().key("100157").value("Invalid path").build();

    /**
     * Request: Missing image.
     */
    public static final Errors _100158 = ErrorRegistry.builder().key("100158").value("Missing image").build();

    /**
     * Request: Invalid image.
     */
    public static final Errors _100159 = ErrorRegistry.builder().key("100159").value("Invalid image").build();

    // =================================================================================================================
    // 1002xx: HTTP & Protocol
    // =================================================================================================================

    /**
     * Please use GET request.
     */
    public static final Errors _100200 = ErrorRegistry.builder().key("100200").value("Please use GET request").build();

    /**
     * Please use POST request.
     */
    public static final Errors _100201 = ErrorRegistry.builder().key("100201").value("Please use POST request").build();

    /**
     * Please use PUT request.
     */
    public static final Errors _100202 = ErrorRegistry.builder().key("100202").value("Please use PUT request").build();

    /**
     * Please use DELETE request.
     */
    public static final Errors _100203 = ErrorRegistry.builder().key("100203").value("Please use DELETE request")
            .build();

    /**
     * Please use OPTIONS request.
     */
    public static final Errors _100204 = ErrorRegistry.builder().key("100204").value("Please use OPTIONS request")
            .build();

    /**
     * Please use HEAD request.
     */
    public static final Errors _100205 = ErrorRegistry.builder().key("100205").value("Please use HEAD request").build();

    /**
     * Please use PATCH request.
     */
    public static final Errors _100206 = ErrorRegistry.builder().key("100206").value("Please use PATCH request")
            .build();

    /**
     * Please use TRACE request.
     */
    public static final Errors _100207 = ErrorRegistry.builder().key("100207").value("Please use TRACE request")
            .build();

    /**
     * Please use CONNECT request.
     */
    public static final Errors _100208 = ErrorRegistry.builder().key("100208").value("Please use CONNECT request")
            .build();

    /**
     * Please use HTTPS protocol.
     */
    public static final Errors _100209 = ErrorRegistry.builder().key("100209").value("Please use HTTPS protocol")
            .build();

    /**
     * Protocol version not supported.
     */
    public static final Errors _100210 = ErrorRegistry.builder().key("100210").value("Protocol version not supported")
            .build();

    /**
     * Media type not supported.
     */
    public static final Errors _100211 = ErrorRegistry.builder().key("100211").value("Media type not supported")
            .build();

    /**
     * WebSocket connection failed.
     */
    public static final Errors _100212 = ErrorRegistry.builder().key("100212").value("WebSocket connection failed")
            .build();

    /**
     * WebSocket connection closed.
     */
    public static final Errors _100213 = ErrorRegistry.builder().key("100213").value("WebSocket connection closed")
            .build();

    // =================================================================================================================
    // 1003xx: Data Format & Encoding
    // =================================================================================================================

    /**
     * Data parsing error.
     */
    public static final Errors _100300 = ErrorRegistry.builder().key("100300").value("Data parsing error").build();

    /**
     * Date format error.
     */
    public static final Errors _100301 = ErrorRegistry.builder().key("100301").value("Date format error").build();

    /**
     * JSON format error.
     */
    public static final Errors _100302 = ErrorRegistry.builder().key("100302").value("JSON format error").build();

    /**
     * File format error.
     */
    public static final Errors _100303 = ErrorRegistry.builder().key("100303").value("File format error").build();

    /**
     * Error converting JSON/XML.
     */
    public static final Errors _100304 = ErrorRegistry.builder().key("100304").value("Error converting JSON/XML")
            .build();

    /**
     * Encoding error.
     */
    public static final Errors _100305 = ErrorRegistry.builder().key("100305").value("Encoding error").build();

    /**
     * Encryption failed.
     */
    public static final Errors _100306 = ErrorRegistry.builder().key("100306").value("Encryption failed").build();

    /**
     * Decryption failed.
     */
    public static final Errors _100307 = ErrorRegistry.builder().key("100307").value("Decryption failed").build();

    /**
     * Serialization failed.
     */
    public static final Errors _100308 = ErrorRegistry.builder().key("100308").value("Serialization failed").build();

    /**
     * Deserialization failed.
     */
    public static final Errors _100309 = ErrorRegistry.builder().key("100309").value("Deserialization failed").build();

    /**
     * Compression failed.
     */
    public static final Errors _100310 = ErrorRegistry.builder().key("100310").value("Compression failed").build();

    /**
     * Decompression failed.
     */
    public static final Errors _100311 = ErrorRegistry.builder().key("100311").value("Decompression failed").build();

    // =================================================================================================================
    // 1004xx: File & IO Operations
    // =================================================================================================================

    /**
     * File upload failed.
     */
    public static final Errors _100400 = ErrorRegistry.builder().key("100400").value("File upload failed").build();

    /**
     * File is empty.
     */
    public static final Errors _100401 = ErrorRegistry.builder().key("100401").value("File is empty").build();

    /**
     * File type not allowed.
     */
    public static final Errors _100402 = ErrorRegistry.builder().key("100402").value("File type not allowed").build();

    /**
     * File size exceeds limit.
     */
    public static final Errors _100403 = ErrorRegistry.builder().key("100403").value("File size exceeds limit").build();

    /**
     * File download failed.
     */
    public static final Errors _100404 = ErrorRegistry.builder().key("100404").value("File download failed").build();

    /**
     * File not found on server.
     */
    public static final Errors _100405 = ErrorRegistry.builder().key("100405").value("File not found on server")
            .build();

    /**
     * Directory creation failed.
     */
    public static final Errors _100406 = ErrorRegistry.builder().key("100406").value("Directory creation failed")
            .build();

    /**
     * File read failed.
     */
    public static final Errors _100407 = ErrorRegistry.builder().key("100407").value("File read failed").build();

    /**
     * File write failed.
     */
    public static final Errors _100408 = ErrorRegistry.builder().key("100408").value("File write failed").build();

    /**
     * File path traversal detected.
     */
    public static final Errors _100409 = ErrorRegistry.builder().key("100409").value("File path traversal detected")
            .build();

    /**
     * File is locked by another process.
     */
    public static final Errors _100410 = ErrorRegistry.builder().key("100410")
            .value("File is locked by another process").build();

    // =================================================================================================================
    // 1005xx: User, Account & Business Logic
    // =================================================================================================================

    /**
     * No data available.
     */
    public static final Errors _100500 = ErrorRegistry.builder().key("100500").value("No data available").build();

    /**
     * Data already exists.
     */
    public static final Errors _100501 = ErrorRegistry.builder().key("100501").value("Data already exists").build();

    /**
     * Data does not exist.
     */
    public static final Errors _100502 = ErrorRegistry.builder().key("100502").value("Data does not exist").build();

    /**
     * Account is frozen.
     */
    public static final Errors _100503 = ErrorRegistry.builder().key("100503").value("Account is frozen").build();

    /**
     * Account already exists.
     */
    public static final Errors _100504 = ErrorRegistry.builder().key("100504").value("Account already exists").build();

    /**
     * Account does not exist.
     */
    public static final Errors _100505 = ErrorRegistry.builder().key("100505").value("Account does not exist").build();

    /**
     * Account not bound.
     */
    public static final Errors _100506 = ErrorRegistry.builder().key("100506").value("Account not bound").build();

    /**
     * Current token has expired.
     */
    public static final Errors _100507 = ErrorRegistry.builder().key("100507").value("Current token has expired")
            .build();

    /**
     * Current account is already logged in.
     */
    public static final Errors _100508 = ErrorRegistry.builder().key("100508")
            .value("Current account is already logged in").build();

    /**
     * Account is abnormal, please contact the administrator.
     */
    public static final Errors _100509 = ErrorRegistry.builder().key("100509")
            .value("Account is abnormal, please contact the administrator").build();

    /**
     * Account is locked, please try again later.
     */
    public static final Errors _100510 = ErrorRegistry.builder().key("100510")
            .value("Account is locked, please try again later").build();

    /**
     * Incorrect username or password.
     */
    public static final Errors _100511 = ErrorRegistry.builder().key("100511").value("Incorrect username or password")
            .build();

    /**
     * Failed to send verification code.
     */
    public static final Errors _100512 = ErrorRegistry.builder().key("100512").value("Failed to send verification code")
            .build();

    /**
     * Incorrect verification code.
     */
    public static final Errors _100513 = ErrorRegistry.builder().key("100513").value("Incorrect verification code")
            .build();

    /**
     * Password length does not meet requirements.
     */
    public static final Errors _100514 = ErrorRegistry.builder().key("100514")
            .value("Password length does not meet requirements").build();

    /**
     * Password must contain both uppercase and lowercase letters.
     */
    public static final Errors _100515 = ErrorRegistry.builder().key("100515")
            .value("Password must contain both uppercase and lowercase letters").build();

    /**
     * Password must contain special characters.
     */
    public static final Errors _100516 = ErrorRegistry.builder().key("100516")
            .value("Password must contain special characters").build();

    /**
     * Duplicate mobile number.
     */
    public static final Errors _100517 = ErrorRegistry.builder().key("100517").value("Duplicate mobile number").build();

    /**
     * Duplicate name.
     */
    public static final Errors _100518 = ErrorRegistry.builder().key("100518").value("Duplicate name").build();

    /**
     * Invalid credential.
     */
    public static final Errors _100519 = ErrorRegistry.builder().key("100519").value("Invalid credential").build();

    /**
     * Department already exists.
     */
    public static final Errors _100520 = ErrorRegistry.builder().key("100520").value("Department already exists")
            .build();

    /**
     * Employee ID already exists.
     */
    public static final Errors _100521 = ErrorRegistry.builder().key("100521").value("Employee ID already exists")
            .build();

    /**
     * Incorrect login-free authorization code.
     */
    public static final Errors _100522 = ErrorRegistry.builder().key("100522")
            .value("Incorrect login-free authorization code").build();

    /**
     * Mobile number not bound.
     */
    public static final Errors _100523 = ErrorRegistry.builder().key("100523").value("Mobile number not bound").build();

    /**
     * Invalid license.
     */
    public static final Errors _100524 = ErrorRegistry.builder().key("100524").value("Invalid license").build();

    /**
     * License has expired.
     */
    public static final Errors _100525 = ErrorRegistry.builder().key("100525").value("License has expired").build();

    /**
     * License verification failed.
     */
    public static final Errors _100526 = ErrorRegistry.builder().key("100526").value("License verification failed")
            .build();

    /**
     * Please contact the official provider to activate.
     */
    public static final Errors _100527 = ErrorRegistry.builder().key("100527")
            .value("Please contact the official provider to activate").build();

    /**
     * License issuance failed.
     */
    public static final Errors _100528 = ErrorRegistry.builder().key("100528").value("License issuance failed").build();

    /**
     * Incorrect license information.
     */
    public static final Errors _100529 = ErrorRegistry.builder().key("100529").value("Incorrect license information")
            .build();

    /**
     * Request body is too large.
     */
    public static final Errors _100530 = ErrorRegistry.builder().key("100530").value("Request body is too large")
            .build();

    /**
     * Token signature invalid.
     */
    public static final Errors _100531 = ErrorRegistry.builder().key("100531").value("Token signature invalid").build();

    /**
     * Token format error.
     */
    public static final Errors _100532 = ErrorRegistry.builder().key("100532").value("Token format error").build();

    /**
     * Refresh token has expired.
     */
    public static final Errors _100533 = ErrorRegistry.builder().key("100533").value("Refresh token has expired")
            .build();

    /**
     * Session timed out.
     */
    public static final Errors _100534 = ErrorRegistry.builder().key("100534").value("Session timed out").build();

    /**
     * Account has been kicked out.
     */
    public static final Errors _100535 = ErrorRegistry.builder().key("100535").value("Account has been kicked out")
            .build();

    /**
     * Invalid email format.
     */
    public static final Errors _100536 = ErrorRegistry.builder().key("100536").value("Invalid email format").build();

    /**
     * Email already registered.
     */
    public static final Errors _100537 = ErrorRegistry.builder().key("100537").value("Email already registered")
            .build();

    /**
     * Guest access denied.
     */
    public static final Errors _100538 = ErrorRegistry.builder().key("100538").value("Guest access denied").build();

    /**
     * Multi-device login restriction.
     */
    public static final Errors _100539 = ErrorRegistry.builder().key("100539").value("Multi-device login restriction")
            .build();

    /**
     * Old password incorrect.
     */
    public static final Errors _100540 = ErrorRegistry.builder().key("100540").value("Old password incorrect").build();

    // =================================================================================================================
    // 1006xx: Database & Storage
    // =================================================================================================================

    /**
     * Database connection failed.
     */
    public static final Errors _100600 = ErrorRegistry.builder().key("100600").value("Database connection failed")
            .build();

    /**
     * Database operation failed.
     */
    public static final Errors _100601 = ErrorRegistry.builder().key("100601").value("Database operation failed")
            .build();

    /**
     * SQL execution error.
     */
    public static final Errors _100602 = ErrorRegistry.builder().key("100602").value("SQL execution error").build();

    /**
     * Duplicate key exception.
     */
    public static final Errors _100603 = ErrorRegistry.builder().key("100603").value("Duplicate key exception").build();

    /**
     * Transaction execution failed.
     */
    public static final Errors _100604 = ErrorRegistry.builder().key("100604").value("Transaction execution failed")
            .build();

    /**
     * Data integrity violation.
     */
    public static final Errors _100605 = ErrorRegistry.builder().key("100605").value("Data integrity violation")
            .build();

    /**
     * Data connection pool exhausted.
     */
    public static final Errors _100606 = ErrorRegistry.builder().key("100606").value("Data connection pool exhausted")
            .build();

    /**
     * Deadlock detected.
     */
    public static final Errors _100607 = ErrorRegistry.builder().key("100607").value("Deadlock detected").build();

    /**
     * Lock wait timeout.
     */
    public static final Errors _100608 = ErrorRegistry.builder().key("100608").value("Lock wait timeout").build();

    // =================================================================================================================
    // 1007xx: Third Party & Remote Services
    // =================================================================================================================

    /**
     * Remote service invocation failed.
     */
    public static final Errors _100700 = ErrorRegistry.builder().key("100700").value("Remote service invocation failed")
            .build();

    /**
     * Remote service timeout.
     */
    public static final Errors _100701 = ErrorRegistry.builder().key("100701").value("Remote service timeout").build();

    /**
     * Remote service unavailable.
     */
    public static final Errors _100702 = ErrorRegistry.builder().key("100702").value("Remote service unavailable")
            .build();

    /**
     * Third-party authentication failed.
     */
    public static final Errors _100703 = ErrorRegistry.builder().key("100703")
            .value("Third-party authentication failed").build();

    /**
     * Third-party API limit reached.
     */
    public static final Errors _100704 = ErrorRegistry.builder().key("100704").value("Third-party API limit reached")
            .build();

    /**
     * Remote service return error.
     */
    public static final Errors _100705 = ErrorRegistry.builder().key("100705").value("Remote service return error")
            .build();

    // =================================================================================================================
    // 1008xx: System & Operation
    // =================================================================================================================

    /**
     * Permission denied.
     */
    public static final Errors _100800 = ErrorRegistry.builder().key("100800").value("Permission denied").build();

    /**
     * Unsupported operation.
     */
    public static final Errors _100801 = ErrorRegistry.builder().key("100801").value("Unsupported operation").build();

    /**
     * Request method not supported.
     */
    public static final Errors _100802 = ErrorRegistry.builder().key("100802").value("Request method not supported")
            .build();

    /**
     * This type is not supported.
     */
    public static final Errors _100803 = ErrorRegistry.builder().key("100803").value("This type is not supported")
            .build();

    /**
     * Resource not found.
     */
    public static final Errors _100804 = ErrorRegistry.builder().key("100804").value("Resource not found").build();

    /**
     * Internal processing exception.
     */
    public static final Errors _100805 = ErrorRegistry.builder().key("100805").value("Internal processing exception")
            .build();

    /**
     * Authorization processing exception.
     */
    public static final Errors _100806 = ErrorRegistry.builder().key("100806").value("Authorization exception").build();

    /**
     * Business processing failed.
     */
    public static final Errors _100807 = ErrorRegistry.builder().key("100807").value("Business processing failed")
            .build();

    /**
     * Task execution failed.
     */
    public static final Errors _100808 = ErrorRegistry.builder().key("100808").value("Task execution failed").build();

    /**
     * Parameter binding exception.
     */
    public static final Errors _100809 = ErrorRegistry.builder().key("100809").value("Parameter binding exception")
            .build();

    /**
     * Link has expired.
     */
    public static final Errors _100810 = ErrorRegistry.builder().key("100810").value("Link has expired").build();

    /**
     * Request timed out.
     */
    public static final Errors _100811 = ErrorRegistry.builder().key("100811").value("Request timed out").build();

    /**
     * Abnormal tenant information processing.
     */
    public static final Errors _100812 = ErrorRegistry.builder().key("100812")
            .value("Abnormal tenant information processing").build();

    /**
     * Service limit exceeded.
     */
    public static final Errors _100813 = ErrorRegistry.builder().key("100813").value("Service limit exceeded").build();

    /**
     * Queue is full.
     */
    public static final Errors _100814 = ErrorRegistry.builder().key("100814").value("Queue is full").build();

    /**
     * Idempotency key conflict.
     */
    public static final Errors _100815 = ErrorRegistry.builder().key("100815").value("Idempotency key conflict")
            .build();

    // =================================================================================================================
    // 1009xx: Network & Configuration
    // =================================================================================================================

    /**
     * Network connection exception.
     */
    public static final Errors _100900 = ErrorRegistry.builder().key("100900").value("Network connection exception")
            .build();

    /**
     * System configuration missing.
     */
    public static final Errors _100901 = ErrorRegistry.builder().key("100901").value("System configuration missing")
            .build();

    /**
     * Configuration load failed.
     */
    public static final Errors _100902 = ErrorRegistry.builder().key("100902").value("Configuration load failed")
            .build();

    /**
     * IP address is not allowed.
     */
    public static final Errors _100903 = ErrorRegistry.builder().key("100903").value("IP address is not allowed")
            .build();

    /**
     * DNS resolution failed.
     */
    public static final Errors _100904 = ErrorRegistry.builder().key("100904").value("DNS resolution failed").build();

}
