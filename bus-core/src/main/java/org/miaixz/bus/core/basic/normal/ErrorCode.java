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

/**
 * Defines global and common error codes. This class can be extended to include product-specific error codes.
 *
 * @author Kimi Liu
 */
public class ErrorCode {

    /**
     * ErrorCode with default settings.
     */
    public ErrorCode() {
        // No initialization required.
    }

    // =================================================================================================================
    // Global & System Codes (-1 ~ -99)
    // =================================================================================================================

    /**
     * Request was successful.
     */
    public static final Errors _SUCCESS = ErrorRegistry.register(Symbol.ZERO, "OK");

    /**
     * System is busy. Please try again later.
     */
    public static final Errors _FAILURE = ErrorRegistry.register("-1", "System is busy. Please try again later");

    /**
     * Too many requests. Please try again later.
     */
    public static final Errors _LIMITER = ErrorRegistry.register("-2", "Too many requests. Please try again later");

    /**
     * Request blocked. Please check your input.
     */
    public static final Errors _BLOCKED = ErrorRegistry.register("-3", "Request blocked. Please check your input");

    /**
     * Something went wrong. Please try again later.
     */
    public static final Errors _UNKNOWN = ErrorRegistry.register("-4", "Something went wrong. Please try again later");

    /**
     * Service under maintenance. Please try again later.
     */
    public static final Errors _MAINTENANCE = ErrorRegistry
            .register("-5", "Service under maintenance. Please try again later");

    /**
     * Service temporarily degraded.
     */
    public static final Errors _DEGRADATION = ErrorRegistry.register("-6", "Service temporarily degraded");

    // =================================================================================================================
    // HTTP Status Codes (Standard RFC 7231)
    // =================================================================================================================

    /**
     * HTTP 200: OK.
     */
    public static final Errors _200 = ErrorRegistry.register("200", "OK");

    /**
     * HTTP 201: Created.
     */
    public static final Errors _201 = ErrorRegistry.register("201", "Created");

    /**
     * HTTP 202: Accepted.
     */
    public static final Errors _202 = ErrorRegistry.register("202", "Accepted");

    /**
     * HTTP 204: No Content.
     */
    public static final Errors _204 = ErrorRegistry.register("204", "No Content");

    /**
     * HTTP 301: Moved Permanently.
     */
    public static final Errors _301 = ErrorRegistry.register("301", "Moved Permanently");

    /**
     * HTTP 302: Found.
     */
    public static final Errors _302 = ErrorRegistry.register("302", "Found");

    /**
     * HTTP 304: Not Modified.
     */
    public static final Errors _304 = ErrorRegistry.register("304", "Not Modified");

    /**
     * HTTP 400: Bad Request.
     */
    public static final Errors _400 = ErrorRegistry.register("400", "Bad Request");

    /**
     * HTTP 401: Unauthorized.
     */
    public static final Errors _401 = ErrorRegistry.register("401", "Unauthorized");

    /**
     * HTTP 403: Forbidden.
     */
    public static final Errors _403 = ErrorRegistry.register("403", "Forbidden");

    /**
     * HTTP 404: Not Found.
     */
    public static final Errors _404 = ErrorRegistry.register("404", "Not Found");

    /**
     * HTTP 405: Method Not Allowed.
     */
    public static final Errors _405 = ErrorRegistry.register("405", "Method Not Allowed");

    /**
     * HTTP 406: Not Acceptable.
     */
    public static final Errors _406 = ErrorRegistry.register("406", "Not Acceptable");

    /**
     * HTTP 408: Request Timeout.
     */
    public static final Errors _408 = ErrorRegistry.register("408", "Request Timeout");

    /**
     * HTTP 409: Conflict.
     */
    public static final Errors _409 = ErrorRegistry.register("409", "Conflict");

    /**
     * HTTP 410: Gone.
     */
    public static final Errors _410 = ErrorRegistry.register("410", "Gone");

    /**
     * HTTP 413: Payload Too Large.
     */
    public static final Errors _413 = ErrorRegistry.register("413", "Payload Too Large");

    /**
     * HTTP 415: Unsupported Media Type.
     */
    public static final Errors _415 = ErrorRegistry.register("415", "Unsupported Media Type");

    /**
     * HTTP 422: Unprocessable Entity.
     */
    public static final Errors _422 = ErrorRegistry.register("422", "Unprocessable Entity");

    /**
     * HTTP 429: Too Many Requests.
     */
    public static final Errors _429 = ErrorRegistry.register("429", "Too Many Requests");

    /**
     * HTTP 500: Internal Server Error.
     */
    public static final Errors _500 = ErrorRegistry.register("500", "Internal Server Error");

    /**
     * HTTP 501: Not Implemented.
     */
    public static final Errors _501 = ErrorRegistry.register("501", "Not Implemented");

    /**
     * HTTP 502: Bad Gateway.
     */
    public static final Errors _502 = ErrorRegistry.register("502", "Bad Gateway");

    /**
     * HTTP 503: Service Unavailable.
     */
    public static final Errors _503 = ErrorRegistry.register("503", "Service Unavailable");

    /**
     * HTTP 504: Gateway Timeout.
     */
    public static final Errors _504 = ErrorRegistry.register("504", "Gateway Timeout");

    /**
     * HTTP 505: HTTP Version Not Supported.
     */
    public static final Errors _505 = ErrorRegistry.register("505", "HTTP Version Not Supported");

    // =================================================================================================================
    // 1001xx: Request Parameters & Validation (Strictly Paired: Missing -> Invalid)
    // =================================================================================================================

    /**
     * Request: Missing parameters.
     */
    public static final Errors _100100 = ErrorRegistry.register("100100", "Missing parameters");

    /**
     * Request: Invalid parameter.
     */
    public static final Errors _100101 = ErrorRegistry.register("100101", "Invalid parameter");

    /**
     * Request: Missing method.
     */
    public static final Errors _100102 = ErrorRegistry.register("100102", "Missing method");

    /**
     * Request: Invalid method.
     */
    public static final Errors _100103 = ErrorRegistry.register("100103", "Invalid method");

    /**
     * Request: Missing format.
     */
    public static final Errors _100104 = ErrorRegistry.register("100104", "Missing format");

    /**
     * Request: Invalid format.
     */
    public static final Errors _100105 = ErrorRegistry.register("100105", "Invalid format");

    /**
     * Request: Missing version.
     */
    public static final Errors _100106 = ErrorRegistry.register("100106", "Missing version");

    /**
     * Request: Invalid version.
     */
    public static final Errors _100107 = ErrorRegistry.register("100107", "Invalid version");

    /**
     * Request: Missing signature.
     */
    public static final Errors _100108 = ErrorRegistry.register("100108", "Missing signature");

    /**
     * Request: Invalid signature.
     */
    public static final Errors _100109 = ErrorRegistry.register("100109", "Invalid signature");

    /**
     * Request: Missing timestamp.
     */
    public static final Errors _100110 = ErrorRegistry.register("100110", "Missing timestamp");

    /**
     * Request: Invalid timestamp.
     */
    public static final Errors _100111 = ErrorRegistry.register("100111", "Invalid timestamp");

    /**
     * Request: Missing token.
     */
    public static final Errors _100112 = ErrorRegistry.register("100112", "Missing token");

    /**
     * Request: Invalid token.
     */
    public static final Errors _100113 = ErrorRegistry.register("100113", "Invalid token");

    /**
     * Request: Missing language.
     */
    public static final Errors _100114 = ErrorRegistry.register("100114", "Missing language");

    /**
     * Request: Invalid language.
     */
    public static final Errors _100115 = ErrorRegistry.register("100115", "Invalid language");

    /**
     * Request: Missing fields.
     */
    public static final Errors _100116 = ErrorRegistry.register("100116", "Missing fields");

    /**
     * Request: Invalid fields.
     */
    public static final Errors _100117 = ErrorRegistry.register("100117", "Invalid fields");

    /**
     * Request: Missing API key.
     */
    public static final Errors _100118 = ErrorRegistry.register("100118", "Missing API key");

    /**
     * Request: Invalid API key.
     */
    public static final Errors _100119 = ErrorRegistry.register("100119", "Invalid API key");

    /**
     * Request: Missing verification code.
     */
    public static final Errors _100120 = ErrorRegistry.register("100120", "Missing verification code");

    /**
     * Request: Invalid verification code.
     */
    public static final Errors _100121 = ErrorRegistry.register("100121", "Invalid verification code");

    /**
     * Request: Missing ID parameter.
     */
    public static final Errors _100122 = ErrorRegistry.register("100122", "Missing ID parameter");

    /**
     * Request: Invalid ID parameter.
     */
    public static final Errors _100123 = ErrorRegistry.register("100123", "Invalid ID parameter");

    /**
     * Request: Missing type parameter.
     */
    public static final Errors _100124 = ErrorRegistry.register("100124", "Missing type parameter");

    /**
     * Request: Invalid type parameter.
     */
    public static final Errors _100125 = ErrorRegistry.register("100125", "Invalid type parameter");

    /**
     * Request: Missing date parameter.
     */
    public static final Errors _100126 = ErrorRegistry.register("100126", "Missing date parameter");

    /**
     * Request: Invalid date parameter.
     */
    public static final Errors _100127 = ErrorRegistry.register("100127", "Invalid date parameter");

    /**
     * Request: Missing status value.
     */
    public static final Errors _100128 = ErrorRegistry.register("100128", "Missing status value");

    /**
     * Request: Invalid status value.
     */
    public static final Errors _100129 = ErrorRegistry.register("100129", "Invalid status value");

    /**
     * Request: Missing header information.
     */
    public static final Errors _100130 = ErrorRegistry.register("100130", "Missing header information");

    /**
     * Request: Invalid header information.
     */
    public static final Errors _100131 = ErrorRegistry.register("100131", "Invalid header information");

    /**
     * Request: Missing configuration.
     */
    public static final Errors _100132 = ErrorRegistry.register("100132", "Missing configuration");

    /**
     * Request: Invalid configuration.
     */
    public static final Errors _100133 = ErrorRegistry.register("100133", "Invalid configuration");

    /**
     * Request: Missing username.
     */
    public static final Errors _100134 = ErrorRegistry.register("100134", "Missing username");

    /**
     * Request: Invalid username.
     */
    public static final Errors _100135 = ErrorRegistry.register("100135", "Invalid username");

    /**
     * Request: Missing password.
     */
    public static final Errors _100136 = ErrorRegistry.register("100136", "Missing password");

    /**
     * Request: Invalid password format.
     */
    public static final Errors _100137 = ErrorRegistry.register("100137", "Invalid password format");

    /**
     * Request: Missing phone number.
     */
    public static final Errors _100138 = ErrorRegistry.register("100138", "Missing phone number");

    /**
     * Request: Invalid phone number.
     */
    public static final Errors _100139 = ErrorRegistry.register("100139", "Invalid phone number");

    /**
     * Request: Missing email address.
     */
    public static final Errors _100140 = ErrorRegistry.register("100140", "Missing email address");

    /**
     * Request: Invalid email address.
     */
    public static final Errors _100141 = ErrorRegistry.register("100141", "Invalid email address");

    /**
     * Request: Missing URL.
     */
    public static final Errors _100142 = ErrorRegistry.register("100142", "Missing URL");

    /**
     * Request: Invalid URL.
     */
    public static final Errors _100143 = ErrorRegistry.register("100143", "Invalid URL");

    /**
     * Request: Missing IP address.
     */
    public static final Errors _100144 = ErrorRegistry.register("100144", "Missing IP address");

    /**
     * Request: Invalid IP address.
     */
    public static final Errors _100145 = ErrorRegistry.register("100145", "Invalid IP address");

    /**
     * Request: Missing port number.
     */
    public static final Errors _100146 = ErrorRegistry.register("100146", "Missing port number");

    /**
     * Request: Invalid port number.
     */
    public static final Errors _100147 = ErrorRegistry.register("100147", "Invalid port number");

    /**
     * Request: Missing amount.
     */
    public static final Errors _100148 = ErrorRegistry.register("100148", "Missing amount");

    /**
     * Request: Invalid amount.
     */
    public static final Errors _100149 = ErrorRegistry.register("100149", "Invalid amount");

    /**
     * Request: Missing currency.
     */
    public static final Errors _100150 = ErrorRegistry.register("100150", "Missing currency");

    /**
     * Request: Invalid currency.
     */
    public static final Errors _100151 = ErrorRegistry.register("100151", "Invalid currency");

    /**
     * Request: Missing order ID.
     */
    public static final Errors _100152 = ErrorRegistry.register("100152", "Missing order ID");

    /**
     * Request: Invalid order ID.
     */
    public static final Errors _100153 = ErrorRegistry.register("100153", "Invalid order ID");

    /**
     * Request: Missing user ID.
     */
    public static final Errors _100154 = ErrorRegistry.register("100154", "Missing user ID");

    /**
     * Request: Invalid user ID.
     */
    public static final Errors _100155 = ErrorRegistry.register("100155", "Invalid user ID");

    /**
     * Request: Missing path.
     */
    public static final Errors _100156 = ErrorRegistry.register("100156", "Missing path");

    /**
     * Request: Invalid path.
     */
    public static final Errors _100157 = ErrorRegistry.register("100157", "Invalid path");

    /**
     * Request: Missing image.
     */
    public static final Errors _100158 = ErrorRegistry.register("100158", "Missing image");

    /**
     * Request: Invalid image.
     */
    public static final Errors _100159 = ErrorRegistry.register("100159", "Invalid image");

    /**
     * Request: Invalid API key or token.
     */
    public static final Errors _100160 = ErrorRegistry.register("100160", "Invalid API key or token");

    // =================================================================================================================
    // 1002xx: HTTP & Protocol
    // =================================================================================================================

    /**
     * GET request required.
     */
    public static final Errors _100200 = ErrorRegistry.register("100200", "GET request required");

    /**
     * POST request required.
     */
    public static final Errors _100201 = ErrorRegistry.register("100201", "POST request required");

    /**
     * PUT request required.
     */
    public static final Errors _100202 = ErrorRegistry.register("100202", "PUT request required");

    /**
     * DELETE request required.
     */
    public static final Errors _100203 = ErrorRegistry.register("100203", "DELETE request required");

    /**
     * OPTIONS request required.
     */
    public static final Errors _100204 = ErrorRegistry.register("100204", "OPTIONS request required");

    /**
     * HEAD request required.
     */
    public static final Errors _100205 = ErrorRegistry.register("100205", "HEAD request required");

    /**
     * PATCH request required.
     */
    public static final Errors _100206 = ErrorRegistry.register("100206", "PATCH request required");

    /**
     * TRACE request required.
     */
    public static final Errors _100207 = ErrorRegistry.register("100207", "TRACE request required");

    /**
     * CONNECT request required.
     */
    public static final Errors _100208 = ErrorRegistry.register("100208", "CONNECT request required");

    /**
     * HTTPS required.
     */
    public static final Errors _100209 = ErrorRegistry.register("100209", "HTTPS required");

    /**
     * Unsupported protocol version.
     */
    public static final Errors _100210 = ErrorRegistry.register("100210", "Unsupported protocol version");

    /**
     * Unsupported media type.
     */
    public static final Errors _100211 = ErrorRegistry.register("100211", "Unsupported media type");

    /**
     * WebSocket connection failed.
     */
    public static final Errors _100212 = ErrorRegistry.register("100212", "WebSocket connection failed");

    /**
     * WebSocket connection closed.
     */
    public static final Errors _100213 = ErrorRegistry.register("100213", "WebSocket connection closed");

    // =================================================================================================================
    // 1003xx: Data Format & Encoding
    // =================================================================================================================

    /**
     * Data parsing failed.
     */
    public static final Errors _100300 = ErrorRegistry.register("100300", "Data parsing failed");

    /**
     * Invalid date format.
     */
    public static final Errors _100301 = ErrorRegistry.register("100301", "Invalid date format");

    /**
     * Invalid JSON format.
     */
    public static final Errors _100302 = ErrorRegistry.register("100302", "Invalid JSON format");

    /**
     * Invalid file format.
     */
    public static final Errors _100303 = ErrorRegistry.register("100303", "Invalid file format");

    /**
     * JSON/XML conversion failed.
     */
    public static final Errors _100304 = ErrorRegistry.register("100304", "JSON/XML conversion failed");

    /**
     * Encoding failed.
     */
    public static final Errors _100305 = ErrorRegistry.register("100305", "Encoding failed");

    /**
     * Encryption failed.
     */
    public static final Errors _100306 = ErrorRegistry.register("100306", "Encryption failed");

    /**
     * Decryption failed.
     */
    public static final Errors _100307 = ErrorRegistry.register("100307", "Decryption failed");

    /**
     * Serialization failed.
     */
    public static final Errors _100308 = ErrorRegistry.register("100308", "Serialization failed");

    /**
     * Deserialization failed.
     */
    public static final Errors _100309 = ErrorRegistry.register("100309", "Deserialization failed");

    /**
     * Compression failed.
     */
    public static final Errors _100310 = ErrorRegistry.register("100310", "Compression failed");

    /**
     * Decompression failed.
     */
    public static final Errors _100311 = ErrorRegistry.register("100311", "Decompression failed");

    // =================================================================================================================
    // 1004xx: File & IO Operations
    // =================================================================================================================

    /**
     * File upload failed.
     */
    public static final Errors _100400 = ErrorRegistry.register("100400", "File upload failed");

    /**
     * File is empty.
     */
    public static final Errors _100401 = ErrorRegistry.register("100401", "File is empty");

    /**
     * Unsupported file type.
     */
    public static final Errors _100402 = ErrorRegistry.register("100402", "Unsupported file type");

    /**
     * File size limit exceeded.
     */
    public static final Errors _100403 = ErrorRegistry.register("100403", "File size limit exceeded");

    /**
     * File download failed.
     */
    public static final Errors _100404 = ErrorRegistry.register("100404", "File download failed");

    /**
     * File not found.
     */
    public static final Errors _100405 = ErrorRegistry.register("100405", "File not found");

    /**
     * Directory creation failed.
     */
    public static final Errors _100406 = ErrorRegistry.register("100406", "Directory creation failed");

    /**
     * File read failed.
     */
    public static final Errors _100407 = ErrorRegistry.register("100407", "File read failed");

    /**
     * File write failed.
     */
    public static final Errors _100408 = ErrorRegistry.register("100408", "File write failed");

    /**
     * Unsafe file path detected.
     */
    public static final Errors _100409 = ErrorRegistry.register("100409", "Unsafe file path detected");

    /**
     * File locked by another process.
     */
    public static final Errors _100410 = ErrorRegistry.register("100410", "File locked by another process");

    // =================================================================================================================
    // 1005xx: User, Account & Business Logic
    // =================================================================================================================

    /**
     * No data available.
     */
    public static final Errors _100500 = ErrorRegistry.register("100500", "No data available");

    /**
     * Data already exists.
     */
    public static final Errors _100501 = ErrorRegistry.register("100501", "Data already exists");

    /**
     * Data not found.
     */
    public static final Errors _100502 = ErrorRegistry.register("100502", "Data not found");

    /**
     * Account is frozen.
     */
    public static final Errors _100503 = ErrorRegistry.register("100503", "Account is frozen");

    /**
     * Account already exists.
     */
    public static final Errors _100504 = ErrorRegistry.register("100504", "Account already exists");

    /**
     * Account not found.
     */
    public static final Errors _100505 = ErrorRegistry.register("100505", "Account not found");

    /**
     * Account not linked.
     */
    public static final Errors _100506 = ErrorRegistry.register("100506", "Account not linked");

    /**
     * Access token expired.
     */
    public static final Errors _100507 = ErrorRegistry.register("100507", "Access token expired");

    /**
     * Account already signed in.
     */
    public static final Errors _100508 = ErrorRegistry.register("100508", "Account already signed in");

    /**
     * Account status invalid. Please contact support.
     */
    public static final Errors _100509 = ErrorRegistry
            .register("100509", "Account status invalid. Please contact support");

    /**
     * Account locked. Please try again later.
     */
    public static final Errors _100510 = ErrorRegistry.register("100510", "Account locked. Please try again later");

    /**
     * Incorrect username or password.
     */
    public static final Errors _100511 = ErrorRegistry.register("100511", "Incorrect username or password");

    /**
     * Verification code delivery failed.
     */
    public static final Errors _100512 = ErrorRegistry.register("100512", "Verification code delivery failed");

    /**
     * Incorrect verification code.
     */
    public static final Errors _100513 = ErrorRegistry.register("100513", "Incorrect verification code");

    /**
     * Password length does not meet requirements.
     */
    public static final Errors _100514 = ErrorRegistry.register("100514", "Password length does not meet requirements");

    /**
     * Password must contain both uppercase and lowercase letters.
     */
    public static final Errors _100515 = ErrorRegistry
            .register("100515", "Password must contain both uppercase and lowercase letters");

    /**
     * Password must contain special characters.
     */
    public static final Errors _100516 = ErrorRegistry.register("100516", "Password must contain special characters");

    /**
     * Mobile number already exists.
     */
    public static final Errors _100517 = ErrorRegistry.register("100517", "Mobile number already exists");

    /**
     * Name already exists.
     */
    public static final Errors _100518 = ErrorRegistry.register("100518", "Name already exists");

    /**
     * Invalid credentials.
     */
    public static final Errors _100519 = ErrorRegistry.register("100519", "Invalid credentials");

    /**
     * Department already exists.
     */
    public static final Errors _100520 = ErrorRegistry.register("100520", "Department already exists");

    /**
     * Employee ID already exists.
     */
    public static final Errors _100521 = ErrorRegistry.register("100521", "Employee ID already exists");

    /**
     * Invalid passwordless authorization code.
     */
    public static final Errors _100522 = ErrorRegistry.register("100522", "Invalid passwordless authorization code");

    /**
     * Mobile number not linked.
     */
    public static final Errors _100523 = ErrorRegistry.register("100523", "Mobile number not linked");

    /**
     * Invalid license.
     */
    public static final Errors _100524 = ErrorRegistry.register("100524", "Invalid license");

    /**
     * License has expired.
     */
    public static final Errors _100525 = ErrorRegistry.register("100525", "License has expired");

    /**
     * License verification failed.
     */
    public static final Errors _100526 = ErrorRegistry.register("100526", "License verification failed");

    /**
     * License activation required. Please contact the provider.
     */
    public static final Errors _100527 = ErrorRegistry
            .register("100527", "License activation required. Please contact the provider");

    /**
     * License issuance failed.
     */
    public static final Errors _100528 = ErrorRegistry.register("100528", "License issuance failed");

    /**
     * Invalid license information.
     */
    public static final Errors _100529 = ErrorRegistry.register("100529", "Invalid license information");

    /**
     * Request body is too large.
     */
    public static final Errors _100530 = ErrorRegistry.register("100530", "Request body is too large");

    /**
     * Response body is too large.
     */
    public static final Errors _100531 = ErrorRegistry.register("100531", "Response body is too large");

    /**
     * Invalid token signature.
     */
    public static final Errors _100532 = ErrorRegistry.register("100532", "Invalid token signature");

    /**
     * Invalid token format.
     */
    public static final Errors _100533 = ErrorRegistry.register("100533", "Invalid token format");

    /**
     * Refresh token expired.
     */
    public static final Errors _100534 = ErrorRegistry.register("100534", "Refresh token expired");

    /**
     * Session expired.
     */
    public static final Errors _100535 = ErrorRegistry.register("100535", "Session expired");

    /**
     * Account signed in elsewhere.
     */
    public static final Errors _100536 = ErrorRegistry.register("100536", "Account signed in elsewhere");

    /**
     * Invalid email format.
     */
    public static final Errors _100537 = ErrorRegistry.register("100537", "Invalid email format");

    /**
     * Email already registered.
     */
    public static final Errors _100538 = ErrorRegistry.register("100538", "Email already registered");

    /**
     * Guest access not allowed.
     */
    public static final Errors _100539 = ErrorRegistry.register("100539", "Guest access not allowed");

    /**
     * Multi-device sign-in restricted.
     */
    public static final Errors _100540 = ErrorRegistry.register("100540", "Multi-device sign-in restricted");

    /**
     * Incorrect current password.
     */
    public static final Errors _100541 = ErrorRegistry.register("100541", "Incorrect current password");

    // =================================================================================================================
    // 1006xx: Database & Storage
    // =================================================================================================================

    /**
     * Database connection failed.
     */
    public static final Errors _100600 = ErrorRegistry.register("100600", "Database connection failed");

    /**
     * Database operation failed.
     */
    public static final Errors _100601 = ErrorRegistry.register("100601", "Database operation failed");

    /**
     * SQL execution failed.
     */
    public static final Errors _100602 = ErrorRegistry.register("100602", "SQL execution failed");

    /**
     * Duplicate key conflict.
     */
    public static final Errors _100603 = ErrorRegistry.register("100603", "Duplicate key conflict");

    /**
     * Transaction failed.
     */
    public static final Errors _100604 = ErrorRegistry.register("100604", "Transaction failed");

    /**
     * Data integrity violation.
     */
    public static final Errors _100605 = ErrorRegistry.register("100605", "Data integrity violation");

    /**
     * Database connection pool exhausted.
     */
    public static final Errors _100606 = ErrorRegistry.register("100606", "Database connection pool exhausted");

    /**
     * Database deadlock detected.
     */
    public static final Errors _100607 = ErrorRegistry.register("100607", "Database deadlock detected");

    /**
     * Database lock wait timed out.
     */
    public static final Errors _100608 = ErrorRegistry.register("100608", "Database lock wait timed out");

    // =================================================================================================================
    // 1007xx: Protocol & Remote Services
    // =================================================================================================================

    /**
     * MQ forwarding failed.
     */
    public static final Errors _100700 = ErrorRegistry.register("100700", "MQ forwarding failed");

    /**
     * gRPC service invocation failed.
     */
    public static final Errors _100701 = ErrorRegistry.register("100701", "gRPC service invocation failed");

    /**
     * MCP tool execution failed.
     */
    public static final Errors _100702 = ErrorRegistry.register("100702", "MCP tool execution failed");

    /**
     * SOAP service call failed.
     */
    public static final Errors _100703 = ErrorRegistry.register("100703", "SOAP service call failed");

    /**
     * Socket communication failed.
     */
    public static final Errors _100704 = ErrorRegistry.register("100704", "Socket communication failed");

    /**
     * SSE stream interrupted.
     */
    public static final Errors _100705 = ErrorRegistry.register("100705", "SSE stream interrupted");

    /**
     * STOMP message delivery failed.
     */
    public static final Errors _100706 = ErrorRegistry.register("100706", "STOMP message delivery failed");

    /**
     * TLS handshake failed.
     */
    public static final Errors _100707 = ErrorRegistry.register("100707", "TLS handshake failed");

    /**
     * Remote service call failed.
     */
    public static final Errors _100708 = ErrorRegistry.register("100708", "Remote service call failed");

    /**
     * Remote service timed out.
     */
    public static final Errors _100709 = ErrorRegistry.register("100709", "Remote service timed out");

    /**
     * Remote service unavailable.
     */
    public static final Errors _100710 = ErrorRegistry.register("100710", "Remote service unavailable");

    /**
     * Third-party authentication failed.
     */
    public static final Errors _100711 = ErrorRegistry.register("100711", "Third-party authentication failed");

    /**
     * Third-party rate limit reached.
     */
    public static final Errors _100712 = ErrorRegistry.register("100712", "Third-party rate limit reached");

    /**
     * Remote service returned an error.
     */
    public static final Errors _100713 = ErrorRegistry.register("100713", "Remote service returned an error");

    // =================================================================================================================
    // 1008xx: System & Operation
    // =================================================================================================================

    /**
     * Permission denied.
     */
    public static final Errors _100800 = ErrorRegistry.register("100800", "Permission denied");

    /**
     * Operation not supported.
     */
    public static final Errors _100801 = ErrorRegistry.register("100801", "Operation not supported");

    /**
     * Request method not supported.
     */
    public static final Errors _100802 = ErrorRegistry.register("100802", "Request method not supported");

    /**
     * Type not supported.
     */
    public static final Errors _100803 = ErrorRegistry.register("100803", "Type not supported");

    /**
     * Resource not found.
     */
    public static final Errors _100804 = ErrorRegistry.register("100804", "Resource not found");

    /**
     * Internal processing failed.
     */
    public static final Errors _100805 = ErrorRegistry.register("100805", "Internal processing failed");

    /**
     * Authorization failed.
     */
    public static final Errors _100806 = ErrorRegistry.register("100806", "Authorization failed");

    /**
     * Business operation failed.
     */
    public static final Errors _100807 = ErrorRegistry.register("100807", "Business operation failed");

    /**
     * Task execution failed.
     */
    public static final Errors _100808 = ErrorRegistry.register("100808", "Task execution failed");

    /**
     * Parameter binding failed.
     */
    public static final Errors _100809 = ErrorRegistry.register("100809", "Parameter binding failed");

    /**
     * Link expired.
     */
    public static final Errors _100810 = ErrorRegistry.register("100810", "Link expired");

    /**
     * Request timed out.
     */
    public static final Errors _100811 = ErrorRegistry.register("100811", "Request timed out");

    /**
     * Tenant processing failed.
     */
    public static final Errors _100812 = ErrorRegistry.register("100812", "Tenant processing failed");

    /**
     * Service limit exceeded.
     */
    public static final Errors _100813 = ErrorRegistry.register("100813", "Service limit exceeded");

    /**
     * Queue capacity exceeded.
     */
    public static final Errors _100814 = ErrorRegistry.register("100814", "Queue capacity exceeded");

    /**
     * Duplicate request detected.
     */
    public static final Errors _100815 = ErrorRegistry.register("100815", "Duplicate request detected");

    // =================================================================================================================
    // 1009xx: Network & Configuration
    // =================================================================================================================

    /**
     * Network connection failed.
     */
    public static final Errors _100900 = ErrorRegistry.register("100900", "Network connection failed");

    /**
     * Required system configuration missing.
     */
    public static final Errors _100901 = ErrorRegistry.register("100901", "Required system configuration missing");

    /**
     * Configuration loading failed.
     */
    public static final Errors _100902 = ErrorRegistry.register("100902", "Configuration loading failed");

    /**
     * IP address not allowed.
     */
    public static final Errors _100903 = ErrorRegistry.register("100903", "IP address not allowed");

    /**
     * DNS resolution failed.
     */
    public static final Errors _100904 = ErrorRegistry.register("100904", "DNS resolution failed");

}
