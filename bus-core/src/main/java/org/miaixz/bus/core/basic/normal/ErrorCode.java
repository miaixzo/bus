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
package org.miaixz.bus.core.basic.normal;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AlreadyExistsException;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Defines global and common error codes. This class can be extended to include product-specific error codes.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ErrorCode {

    /**
     * Constructs a new ErrorCode with default settings.
     */
    public ErrorCode() {
        // No initialization required.
    }

    /**
     * Creates and registers an error code entry using the common {@link ErrorRegistry} builder flow.
     * <p>
     * This method is a convenience factory for predefined constants and extension modules that need to define
     * additional error codes. It validates that both {@code key} and {@code value} are not blank, builds an
     * {@link ErrorRegistry}, and relies on {@link ErrorRegistry}'s construction process to register the entry in the
     * global {@link Errors#ERRORS_CACHE}. Because registration is global, the supplied {@code key} must be unique
     * across all registered error codes.
     *
     * @param key   unique error code used for lookup, localization, and duplicate detection
     * @param value default error message used when no localized message is available
     * @return registered error descriptor containing the supplied code and default message
     * @throws IllegalArgumentException if {@code key} or {@code value} is blank
     * @throws AlreadyExistsException   if the same {@code key} has already been registered
     */
    public static Errors registry(String key, String value) {
        if (StringKit.isBlank(key)) {
            throw new IllegalArgumentException("Key cannot be blank");
        }
        if (StringKit.isBlank(value)) {
            throw new IllegalArgumentException("Value cannot be blank");
        }
        return ErrorRegistry.builder().key(key).value(value).build();
    }

    // =================================================================================================================
    // Global & System Codes (-1 ~ -99)
    // =================================================================================================================

    /**
     * Request was successful.
     */
    public static final Errors _SUCCESS = registry(Symbol.ZERO, "OK");

    /**
     * System is busy, please try again later.
     */
    public static final Errors _FAILURE = registry("-1", "System is busy, please try again later");

    /**
     * Request is too frequent, please try again later.
     */
    public static final Errors _LIMITER = registry("-2", "Request frequency too high, please wait and try again later");

    /**
     * Illegal request, please try again later.
     */
    public static final Errors _BLOCKED = registry(
            "-3",
            "Illegal or blocked request, please check parameters and try again");

    /**
     * An unknown error has occurred, please try again later.
     */
    public static final Errors _UNKNOWN = registry("-4", "An unknown error has occurred, please try again later");

    /**
     * Service maintenance, please try again later.
     */
    public static final Errors _MAINTENANCE = registry("-5", "Service under maintenance, please try again later");

    /**
     * Service degradation triggered.
     */
    public static final Errors _DEGRADATION = registry("-6", "Service degradation triggered");

    // =================================================================================================================
    // HTTP Status Codes (Standard RFC 7231)
    // =================================================================================================================

    /**
     * HTTP 200: OK.
     */
    public static final Errors _200 = registry("200", "OK");

    /**
     * HTTP 201: Created.
     */
    public static final Errors _201 = registry("201", "Created");

    /**
     * HTTP 202: Accepted.
     */
    public static final Errors _202 = registry("202", "Accepted");

    /**
     * HTTP 204: No Content.
     */
    public static final Errors _204 = registry("204", "No Content");

    /**
     * HTTP 301: Moved Permanently.
     */
    public static final Errors _301 = registry("301", "Moved Permanently");

    /**
     * HTTP 302: Found.
     */
    public static final Errors _302 = registry("302", "Found");

    /**
     * HTTP 304: Not Modified.
     */
    public static final Errors _304 = registry("304", "Not Modified");

    /**
     * HTTP 400: Bad Request.
     */
    public static final Errors _400 = registry("400", "Bad Request");

    /**
     * HTTP 401: Unauthorized.
     */
    public static final Errors _401 = registry("401", "Unauthorized");

    /**
     * HTTP 403: Forbidden.
     */
    public static final Errors _403 = registry("403", "Forbidden");

    /**
     * HTTP 404: Not Found.
     */
    public static final Errors _404 = registry("404", "Not Found");

    /**
     * HTTP 405: Method Not Allowed.
     */
    public static final Errors _405 = registry("405", "Method Not Allowed");

    /**
     * HTTP 406: Not Acceptable.
     */
    public static final Errors _406 = registry("406", "Not Acceptable");

    /**
     * HTTP 408: Request Timeout.
     */
    public static final Errors _408 = registry("408", "Request Timeout");

    /**
     * HTTP 409: Conflict.
     */
    public static final Errors _409 = registry("409", "Conflict");

    /**
     * HTTP 410: Gone.
     */
    public static final Errors _410 = registry("410", "Gone");

    /**
     * HTTP 413: Payload Too Large.
     */
    public static final Errors _413 = registry("413", "Payload Too Large");

    /**
     * HTTP 415: Unsupported Media Type.
     */
    public static final Errors _415 = registry("415", "Unsupported Media Type");

    /**
     * HTTP 422: Unprocessable Entity.
     */
    public static final Errors _422 = registry("422", "Unprocessable Entity");

    /**
     * HTTP 429: Too Many Requests.
     */
    public static final Errors _429 = registry("429", "Too Many Requests");

    /**
     * HTTP 500: Internal Server Error.
     */
    public static final Errors _500 = registry("500", "Internal Server Error");

    /**
     * HTTP 501: Not Implemented.
     */
    public static final Errors _501 = registry("501", "Not Implemented");

    /**
     * HTTP 502: Bad Gateway.
     */
    public static final Errors _502 = registry("502", "Bad Gateway");

    /**
     * HTTP 503: Service Unavailable.
     */
    public static final Errors _503 = registry("503", "Service Unavailable");

    /**
     * HTTP 504: Gateway Timeout.
     */
    public static final Errors _504 = registry("504", "Gateway Timeout");

    /**
     * HTTP 505: HTTP Version Not Supported.
     */
    public static final Errors _505 = registry("505", "HTTP Version Not Supported");

    // =================================================================================================================
    // 1001xx: Request Parameters & Validation (Strictly Paired: Missing -> Invalid)
    // =================================================================================================================

    /**
     * Request: Missing parameters.
     */
    public static final Errors _100100 = registry("100100", "Missing parameters");

    /**
     * Request: Invalid parameter.
     */
    public static final Errors _100101 = registry("100101", "Invalid parameter");

    /**
     * Request: Missing method.
     */
    public static final Errors _100102 = registry("100102", "Missing method");

    /**
     * Request: Invalid method.
     */
    public static final Errors _100103 = registry("100103", "Invalid method");

    /**
     * Request: Missing format.
     */
    public static final Errors _100104 = registry("100104", "Missing format");

    /**
     * Request: Invalid format.
     */
    public static final Errors _100105 = registry("100105", "Invalid format");

    /**
     * Request: Missing version.
     */
    public static final Errors _100106 = registry("100106", "Missing version");

    /**
     * Request: Invalid version.
     */
    public static final Errors _100107 = registry("100107", "Invalid version");

    /**
     * Request: Missing sign.
     */
    public static final Errors _100108 = registry("100108", "Missing sign");

    /**
     * Request: Invalid sign.
     */
    public static final Errors _100109 = registry("100109", "Invalid sign");

    /**
     * Request: Missing timestamp.
     */
    public static final Errors _100110 = registry("100110", "Missing timestamp");

    /**
     * Request: Invalid timestamp.
     */
    public static final Errors _100111 = registry("100111", "Invalid timestamp");

    /**
     * Request: Missing token.
     */
    public static final Errors _100112 = registry("100112", "Missing token");

    /**
     * Request: Invalid token.
     */
    public static final Errors _100113 = registry("100113", "Invalid token");

    /**
     * Request: Missing language.
     */
    public static final Errors _100114 = registry("100114", "Missing language");

    /**
     * Request: Invalid language.
     */
    public static final Errors _100115 = registry("100115", "Invalid language");

    /**
     * Request: Missing fields.
     */
    public static final Errors _100116 = registry("100116", "Missing fields");

    /**
     * Request: Invalid fields.
     */
    public static final Errors _100117 = registry("100117", "Invalid fields");

    /**
     * Request: Missing ApiKey.
     */
    public static final Errors _100118 = registry("100118", "Missing ApiKey");

    /**
     * Request: Invalid ApiKey.
     */
    public static final Errors _100119 = registry("100119", "Invalid ApiKey");

    /**
     * Request: Missing verification code.
     */
    public static final Errors _100120 = registry("100120", "Missing verification code");

    /**
     * Request: Invalid verification code.
     */
    public static final Errors _100121 = registry("100121", "Invalid verification code");

    /**
     * Request: Missing ID parameter.
     */
    public static final Errors _100122 = registry("100122", "Missing ID parameter");

    /**
     * Request: Invalid ID parameter.
     */
    public static final Errors _100123 = registry("100123", "Invalid ID parameter");

    /**
     * Request: Missing type parameter.
     */
    public static final Errors _100124 = registry("100124", "Missing type parameter");

    /**
     * Request: Invalid type parameter.
     */
    public static final Errors _100125 = registry("100125", "Invalid type parameter");

    /**
     * Request: Missing date parameter.
     */
    public static final Errors _100126 = registry("100126", "Missing date parameter");

    /**
     * Request: Invalid date parameter.
     */
    public static final Errors _100127 = registry("100127", "Invalid date parameter");

    /**
     * Request: Missing status value.
     */
    public static final Errors _100128 = registry("100128", "Missing status value");

    /**
     * Request: Invalid status value.
     */
    public static final Errors _100129 = registry("100129", "Invalid status value");

    /**
     * Request: Missing header info.
     */
    public static final Errors _100130 = registry("100130", "Missing header info");

    /**
     * Request: Invalid header info.
     */
    public static final Errors _100131 = registry("100131", "Invalid header info");

    /**
     * Request: Missing config info.
     */
    public static final Errors _100132 = registry("100132", "Missing config info");

    /**
     * Request: Invalid config info.
     */
    public static final Errors _100133 = registry("100133", "Invalid config info");

    /**
     * Request: Missing username.
     */
    public static final Errors _100134 = registry("100134", "Missing username");

    /**
     * Request: Invalid username.
     */
    public static final Errors _100135 = registry("100135", "Invalid username");

    /**
     * Request: Missing password.
     */
    public static final Errors _100136 = registry("100136", "Missing password");

    /**
     * Request: Invalid password format.
     */
    public static final Errors _100137 = registry("100137", "Invalid password format");

    /**
     * Request: Missing phone number.
     */
    public static final Errors _100138 = registry("100138", "Missing phone number");

    /**
     * Request: Invalid phone number.
     */
    public static final Errors _100139 = registry("100139", "Invalid phone number");

    /**
     * Request: Missing email address.
     */
    public static final Errors _100140 = registry("100140", "Missing email address");

    /**
     * Request: Invalid email address.
     */
    public static final Errors _100141 = registry("100141", "Invalid email address");

    /**
     * Request: Missing URL.
     */
    public static final Errors _100142 = registry("100142", "Missing URL");

    /**
     * Request: Invalid URL.
     */
    public static final Errors _100143 = registry("100143", "Invalid URL");

    /**
     * Request: Missing IP address.
     */
    public static final Errors _100144 = registry("100144", "Missing IP address");

    /**
     * Request: Invalid IP address.
     */
    public static final Errors _100145 = registry("100145", "Invalid IP address");

    /**
     * Request: Missing port number.
     */
    public static final Errors _100146 = registry("100146", "Missing port number");

    /**
     * Request: Invalid port number.
     */
    public static final Errors _100147 = registry("100147", "Invalid port number");

    /**
     * Request: Missing amount.
     */
    public static final Errors _100148 = registry("100148", "Missing amount");

    /**
     * Request: Invalid amount.
     */
    public static final Errors _100149 = registry("100149", "Invalid amount");

    /**
     * Request: Missing currency.
     */
    public static final Errors _100150 = registry("100150", "Missing currency");

    /**
     * Request: Invalid currency.
     */
    public static final Errors _100151 = registry("100151", "Invalid currency");

    /**
     * Request: Missing order ID.
     */
    public static final Errors _100152 = registry("100152", "Missing order ID");

    /**
     * Request: Invalid order ID.
     */
    public static final Errors _100153 = registry("100153", "Invalid order ID");

    /**
     * Request: Missing user ID.
     */
    public static final Errors _100154 = registry("100154", "Missing user ID");

    /**
     * Request: Invalid user ID.
     */
    public static final Errors _100155 = registry("100155", "Invalid user ID");

    /**
     * Request: Missing path.
     */
    public static final Errors _100156 = registry("100156", "Missing path");

    /**
     * Request: Invalid path.
     */
    public static final Errors _100157 = registry("100157", "Invalid path");

    /**
     * Request: Missing image.
     */
    public static final Errors _100158 = registry("100158", "Missing image");

    /**
     * Request: Invalid image.
     */
    public static final Errors _100159 = registry("100159", "Invalid image");

    // =================================================================================================================
    // 1002xx: HTTP & Protocol
    // =================================================================================================================

    /**
     * Please use GET request.
     */
    public static final Errors _100200 = registry("100200", "Please use GET request");

    /**
     * Please use POST request.
     */
    public static final Errors _100201 = registry("100201", "Please use POST request");

    /**
     * Please use PUT request.
     */
    public static final Errors _100202 = registry("100202", "Please use PUT request");

    /**
     * Please use DELETE request.
     */
    public static final Errors _100203 = registry("100203", "Please use DELETE request");

    /**
     * Please use OPTIONS request.
     */
    public static final Errors _100204 = registry("100204", "Please use OPTIONS request");

    /**
     * Please use HEAD request.
     */
    public static final Errors _100205 = registry("100205", "Please use HEAD request");

    /**
     * Please use PATCH request.
     */
    public static final Errors _100206 = registry("100206", "Please use PATCH request");

    /**
     * Please use TRACE request.
     */
    public static final Errors _100207 = registry("100207", "Please use TRACE request");

    /**
     * Please use CONNECT request.
     */
    public static final Errors _100208 = registry("100208", "Please use CONNECT request");

    /**
     * Please use HTTPS protocol.
     */
    public static final Errors _100209 = registry("100209", "Please use HTTPS protocol");

    /**
     * Protocol version not supported.
     */
    public static final Errors _100210 = registry("100210", "Protocol version not supported");

    /**
     * Media type not supported.
     */
    public static final Errors _100211 = registry("100211", "Media type not supported");

    /**
     * WebSocket connection failed.
     */
    public static final Errors _100212 = registry("100212", "WebSocket connection failed");

    /**
     * WebSocket connection closed.
     */
    public static final Errors _100213 = registry("100213", "WebSocket connection closed");

    // =================================================================================================================
    // 1003xx: Data Format & Encoding
    // =================================================================================================================

    /**
     * Data parsing error.
     */
    public static final Errors _100300 = registry("100300", "Data parsing error");

    /**
     * Date format error.
     */
    public static final Errors _100301 = registry("100301", "Date format error");

    /**
     * JSON format error.
     */
    public static final Errors _100302 = registry("100302", "JSON format error");

    /**
     * File format error.
     */
    public static final Errors _100303 = registry("100303", "File format error");

    /**
     * Error converting JSON/XML.
     */
    public static final Errors _100304 = registry("100304", "Error converting JSON/XML");

    /**
     * Encoding error.
     */
    public static final Errors _100305 = registry("100305", "Encoding error");

    /**
     * Encryption failed.
     */
    public static final Errors _100306 = registry("100306", "Encryption failed");

    /**
     * Decryption failed.
     */
    public static final Errors _100307 = registry("100307", "Decryption failed");

    /**
     * Serialization failed.
     */
    public static final Errors _100308 = registry("100308", "Serialization failed");

    /**
     * Deserialization failed.
     */
    public static final Errors _100309 = registry("100309", "Deserialization failed");

    /**
     * Compression failed.
     */
    public static final Errors _100310 = registry("100310", "Compression failed");

    /**
     * Decompression failed.
     */
    public static final Errors _100311 = registry("100311", "Decompression failed");

    // =================================================================================================================
    // 1004xx: File & IO Operations
    // =================================================================================================================

    /**
     * File upload failed.
     */
    public static final Errors _100400 = registry("100400", "File upload failed");

    /**
     * File is empty.
     */
    public static final Errors _100401 = registry("100401", "File is empty");

    /**
     * File type not allowed.
     */
    public static final Errors _100402 = registry("100402", "File type not allowed");

    /**
     * File size exceeds limit.
     */
    public static final Errors _100403 = registry("100403", "File size exceeds limit");

    /**
     * File download failed.
     */
    public static final Errors _100404 = registry("100404", "File download failed");

    /**
     * File not found on server.
     */
    public static final Errors _100405 = registry("100405", "File not found on server");

    /**
     * Directory creation failed.
     */
    public static final Errors _100406 = registry("100406", "Directory creation failed");

    /**
     * File read failed.
     */
    public static final Errors _100407 = registry("100407", "File read failed");

    /**
     * File write failed.
     */
    public static final Errors _100408 = registry("100408", "File write failed");

    /**
     * File path traversal detected.
     */
    public static final Errors _100409 = registry("100409", "File path traversal detected");

    /**
     * File is locked by another process.
     */
    public static final Errors _100410 = registry("100410", "File is locked by another process");

    // =================================================================================================================
    // 1005xx: User, Account & Business Logic
    // =================================================================================================================

    /**
     * No data available.
     */
    public static final Errors _100500 = registry("100500", "No data available");

    /**
     * Data already exists.
     */
    public static final Errors _100501 = registry("100501", "Data already exists");

    /**
     * Data does not exist.
     */
    public static final Errors _100502 = registry("100502", "Data does not exist");

    /**
     * Account is frozen.
     */
    public static final Errors _100503 = registry("100503", "Account is frozen");

    /**
     * Account already exists.
     */
    public static final Errors _100504 = registry("100504", "Account already exists");

    /**
     * Account does not exist.
     */
    public static final Errors _100505 = registry("100505", "Account does not exist");

    /**
     * Account not bound.
     */
    public static final Errors _100506 = registry("100506", "Account not bound");

    /**
     * Current token has expired.
     */
    public static final Errors _100507 = registry("100507", "Current token has expired");

    /**
     * Current account is already logged in.
     */
    public static final Errors _100508 = registry("100508", "Current account is already logged in");

    /**
     * Account is abnormal, please contact the administrator.
     */
    public static final Errors _100509 = registry("100509", "Account is abnormal, please contact the administrator");

    /**
     * Account is locked, please try again later.
     */
    public static final Errors _100510 = registry("100510", "Account is locked, please try again later");

    /**
     * Incorrect username or password.
     */
    public static final Errors _100511 = registry("100511", "Incorrect username or password");

    /**
     * Failed to send verification code.
     */
    public static final Errors _100512 = registry("100512", "Failed to send verification code");

    /**
     * Incorrect verification code.
     */
    public static final Errors _100513 = registry("100513", "Incorrect verification code");

    /**
     * Password length does not meet requirements.
     */
    public static final Errors _100514 = registry("100514", "Password length does not meet requirements");

    /**
     * Password must contain both uppercase and lowercase letters.
     */
    public static final Errors _100515 = registry(
            "100515",
            "Password must contain both uppercase and lowercase letters");

    /**
     * Password must contain special characters.
     */
    public static final Errors _100516 = registry("100516", "Password must contain special characters");

    /**
     * Duplicate mobile number.
     */
    public static final Errors _100517 = registry("100517", "Duplicate mobile number");

    /**
     * Duplicate name.
     */
    public static final Errors _100518 = registry("100518", "Duplicate name");

    /**
     * Invalid credential.
     */
    public static final Errors _100519 = registry("100519", "Invalid credential");

    /**
     * Department already exists.
     */
    public static final Errors _100520 = registry("100520", "Department already exists");

    /**
     * Employee ID already exists.
     */
    public static final Errors _100521 = registry("100521", "Employee ID already exists");

    /**
     * Incorrect login-free authorization code.
     */
    public static final Errors _100522 = registry("100522", "Incorrect login-free authorization code");

    /**
     * Mobile number not bound.
     */
    public static final Errors _100523 = registry("100523", "Mobile number not bound");

    /**
     * Invalid license.
     */
    public static final Errors _100524 = registry("100524", "Invalid license");

    /**
     * License has expired.
     */
    public static final Errors _100525 = registry("100525", "License has expired");

    /**
     * License verification failed.
     */
    public static final Errors _100526 = registry("100526", "License verification failed");

    /**
     * Please contact the official provider to activate.
     */
    public static final Errors _100527 = registry("100527", "Please contact the official provider to activate");

    /**
     * License issuance failed.
     */
    public static final Errors _100528 = registry("100528", "License issuance failed");

    /**
     * Incorrect license information.
     */
    public static final Errors _100529 = registry("100529", "Incorrect license information");

    /**
     * Request body is too large.
     */
    public static final Errors _100530 = registry("100530", "Request body is too large");

    /**
     * Token signature invalid.
     */
    public static final Errors _100531 = registry("100531", "Token signature invalid");

    /**
     * Token format error.
     */
    public static final Errors _100532 = registry("100532", "Token format error");

    /**
     * Refresh token has expired.
     */
    public static final Errors _100533 = registry("100533", "Refresh token has expired");

    /**
     * Session timed out.
     */
    public static final Errors _100534 = registry("100534", "Session timed out");

    /**
     * Account has been kicked out.
     */
    public static final Errors _100535 = registry("100535", "Account has been kicked out");

    /**
     * Invalid email format.
     */
    public static final Errors _100536 = registry("100536", "Invalid email format");

    /**
     * Email already registered.
     */
    public static final Errors _100537 = registry("100537", "Email already registered");

    /**
     * Guest access denied.
     */
    public static final Errors _100538 = registry("100538", "Guest access denied");

    /**
     * Multi-device login restriction.
     */
    public static final Errors _100539 = registry("100539", "Multi-device login restriction");

    /**
     * Old password incorrect.
     */
    public static final Errors _100540 = registry("100540", "Old password incorrect");

    // =================================================================================================================
    // 1006xx: Database & Storage
    // =================================================================================================================

    /**
     * Database connection failed.
     */
    public static final Errors _100600 = registry("100600", "Database connection failed");

    /**
     * Database operation failed.
     */
    public static final Errors _100601 = registry("100601", "Database operation failed");

    /**
     * SQL execution error.
     */
    public static final Errors _100602 = registry("100602", "SQL execution error");

    /**
     * Duplicate key exception.
     */
    public static final Errors _100603 = registry("100603", "Duplicate key exception");

    /**
     * Transaction execution failed.
     */
    public static final Errors _100604 = registry("100604", "Transaction execution failed");

    /**
     * Data integrity violation.
     */
    public static final Errors _100605 = registry("100605", "Data integrity violation");

    /**
     * Data connection pool exhausted.
     */
    public static final Errors _100606 = registry("100606", "Data connection pool exhausted");

    /**
     * Deadlock detected.
     */
    public static final Errors _100607 = registry("100607", "Deadlock detected");

    /**
     * Lock wait timeout.
     */
    public static final Errors _100608 = registry("100608", "Lock wait timeout");

    // =================================================================================================================
    // 1007xx: Third Party & Remote Services
    // =================================================================================================================

    /**
     * Remote service invocation failed.
     */
    public static final Errors _100700 = registry("100700", "Remote service invocation failed");

    /**
     * Remote service timeout.
     */
    public static final Errors _100701 = registry("100701", "Remote service timeout");

    /**
     * Remote service unavailable.
     */
    public static final Errors _100702 = registry("100702", "Remote service unavailable");

    /**
     * Third-party authentication failed.
     */
    public static final Errors _100703 = registry("100703", "Third-party authentication failed");

    /**
     * Third-party API limit reached.
     */
    public static final Errors _100704 = registry("100704", "Third-party API limit reached");

    /**
     * Remote service return error.
     */
    public static final Errors _100705 = registry("100705", "Remote service return error");

    // =================================================================================================================
    // 1008xx: System & Operation
    // =================================================================================================================

    /**
     * Permission denied.
     */
    public static final Errors _100800 = registry("100800", "Permission denied");

    /**
     * Unsupported operation.
     */
    public static final Errors _100801 = registry("100801", "Unsupported operation");

    /**
     * Request method not supported.
     */
    public static final Errors _100802 = registry("100802", "Request method not supported");

    /**
     * This type is not supported.
     */
    public static final Errors _100803 = registry("100803", "This type is not supported");

    /**
     * Resource not found.
     */
    public static final Errors _100804 = registry("100804", "Resource not found");

    /**
     * Internal processing exception.
     */
    public static final Errors _100805 = registry("100805", "Internal processing exception");

    /**
     * Authorization processing exception.
     */
    public static final Errors _100806 = registry("100806", "Authorization exception");

    /**
     * Business processing failed.
     */
    public static final Errors _100807 = registry("100807", "Business processing failed");

    /**
     * Task execution failed.
     */
    public static final Errors _100808 = registry("100808", "Task execution failed");

    /**
     * Parameter binding exception.
     */
    public static final Errors _100809 = registry("100809", "Parameter binding exception");

    /**
     * Link has expired.
     */
    public static final Errors _100810 = registry("100810", "Link has expired");

    /**
     * Request timed out.
     */
    public static final Errors _100811 = registry("100811", "Request timed out");

    /**
     * Abnormal tenant information processing.
     */
    public static final Errors _100812 = registry("100812", "Abnormal tenant information processing");

    /**
     * Service limit exceeded.
     */
    public static final Errors _100813 = registry("100813", "Service limit exceeded");

    /**
     * Queue is full.
     */
    public static final Errors _100814 = registry("100814", "Queue is full");

    /**
     * Idempotency key conflict.
     */
    public static final Errors _100815 = registry("100815", "Idempotency key conflict");

    // =================================================================================================================
    // 1009xx: Network & Configuration
    // =================================================================================================================

    /**
     * Network connection exception.
     */
    public static final Errors _100900 = registry("100900", "Network connection exception");

    /**
     * System configuration missing.
     */
    public static final Errors _100901 = registry("100901", "System configuration missing");

    /**
     * Configuration load failed.
     */
    public static final Errors _100902 = registry("100902", "Configuration load failed");

    /**
     * IP address is not allowed.
     */
    public static final Errors _100903 = registry("100903", "IP address is not allowed");

    /**
     * DNS resolution failed.
     */
    public static final Errors _100904 = registry("100904", "DNS resolution failed");

}
