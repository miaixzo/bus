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
package org.miaixz.bus.core.net;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.miaixz.bus.core.lang.EnumValue;

/**
 * HTTP protocol constants, conventional endpoint contracts, and framework-independent authentication helpers.
 * <p>
 * This class centralizes HTTP methods, status codes, header fields, HTTP/2 settings, request parameter names, endpoint
 * paths, cache directives, WebSocket handshake values, and credential extraction from plain request-value maps.
 * Framework-specific request types are intentionally excluded so every Bus module can share the same HTTP vocabulary.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class Http {

    /**
     * Keeps all HTTP definitions and helpers on the static API.
     */
    private Http() {
        // No initialization required.
    }

    /**
     * Resolves the standard HTTP method token from a registry verb code.
     *
     * @param verb registry verb code
     * @return HTTP method token
     */
    public static String methodOf(int verb) {
        return Method.of(verb).value();
    }

    /**
     * Resolves the registry verb code from one HTTP method token.
     *
     * @param method HTTP method token
     * @return registry verb code
     */
    public static int verbOf(String method) {
        return Method.of(method).verb();
    }

    /**
     * Returns whether one registry verb code matches the supplied HTTP method token.
     *
     * @param verb   registry verb code
     * @param method HTTP method token
     * @return {@code true} when both values resolve to the same method
     */
    public static boolean matches(Integer verb, String method) {
        return verb != null && Method.of(verb).matches(method);
    }

    /**
     * Returns whether the HTTP method invalidates cache entries.
     *
     * @param method the HTTP method to check
     * @return {@code true} when the method invalidates cache entries
     */
    public static boolean invalidatesCache(String method) {
        return Method.POST.matches(method) || Method.PUT.matches(method) || Method.PATCH.matches(method)
                || Method.DELETE.matches(method) || Method.MOVE.matches(method);
    }

    /**
     * Returns whether the HTTP method requires a request body.
     *
     * @param method the HTTP method to check
     * @return {@code true} when the method requires a request body
     */
    public static boolean requiresRequestBody(String method) {
        return Method.of(method).requiresBody();
    }

    /**
     * Returns whether the HTTP method permits a request body.
     *
     * @param method the HTTP method to check
     * @return {@code true} when the method permits a request body
     */
    public static boolean permitsRequestBody(String method) {
        return Method.of(method).permitsBody();
    }

    /**
     * Returns whether redirects for this method should preserve the request body.
     *
     * @param method the HTTP method to check
     * @return {@code true} when redirects should preserve the request body
     */
    public static boolean redirectsWithBody(String method) {
        return Method.PROPFIND.matches(method);
    }

    /**
     * Returns whether redirects for this method should use a GET request.
     *
     * @param method the HTTP method to check
     * @return {@code true} when redirects should use a GET request
     */
    public static boolean redirectsToGet(String method) {
        return !Method.PROPFIND.matches(method);
    }

    /**
     * HTTP response status codes.
     */
    public static final class Status {

        /**
         * Prevents instantiation.
         */
        private Status() {
            // No initialization required.
        }

        /**
         * HTTP Status Code 100: Continue. Server has received request headers, client should continue sending request
         * body. RFC 9110, Section 15.2.1.
         */
        public static final int CONTINUE = 100;

        /**
         * HTTP Status Code 101: Switching Protocols. Server agrees to switch protocols (e.g., HTTP/1.1 to WebSocket).
         * RFC 9110, Section 15.2.2.
         */
        public static final int SWITCHING_PROTOCOLS = 101;

        /**
         * HTTP Status Code 102: Processing. Server has accepted the complete request but has not completed it. RFC
         * 2518, Section 10.1.
         */
        public static final int PROCESSING = 102;

        /**
         * HTTP Status Code 103: Early Hints. Supplies preliminary response headers before the final response. RFC 8297,
         * Section 2.
         */
        public static final int EARLY_HINTS = 103;

        /**
         * HTTP Status Code 200: OK. Request has succeeded. RFC 9110, Section 15.3.1.
         */
        public static final int OK = 200;

        /**
         * HTTP Status Code 201: Created. Request succeeded and created a new resource. RFC 9110, Section 15.3.2.
         */
        public static final int CREATED = 201;

        /**
         * HTTP Status Code 202: Accepted. Request accepted for processing, but not completed. RFC 9110, Section 15.3.3.
         */
        public static final int ACCEPTED = 202;

        /**
         * HTTP Status Code 203: Non-Authoritative Information. Transforming proxy has modified the response. RFC 9110,
         * Section 15.3.4.
         */
        public static final int NON_AUTHORITATIVE_INFORMATION = 203;

        /**
         * HTTP Status Code 204: No Content. Request succeeded but no content returned. RFC 9110, Section 15.3.5.
         */
        public static final int NO_CONTENT = 204;

        /**
         * HTTP Status Code 205: Reset Content. Request succeeded, client should reset document view. RFC 9110, Section
         * 15.3.6.
         */
        public static final int RESET_CONTENT = 205;

        /**
         * HTTP Status Code 206: Partial Content. Server successfully handled a partial GET request. RFC 9110, Section
         * 15.3.7.
         */
        public static final int PARTIAL_CONTENT = 206;

        /**
         * HTTP Status Code 207: Multi-Status. Response body contains status information for multiple resources. RFC
         * 4918, Section 11.1.
         */
        public static final int MULTI_STATUS = 207;

        /**
         * HTTP Status Code 208: Already Reported. Members of a binding have already been enumerated. RFC 5842, Section
         * 7.1.
         */
        public static final int ALREADY_REPORTED = 208;

        /**
         * HTTP Status Code 226: IM Used. Response represents one or more instance manipulations. RFC 3229, Section
         * 10.4.1.
         */
        public static final int IM_USED = 226;

        /**
         * HTTP Status Code 300: Multiple Choices. Resource can be accessed via multiple URIs. RFC 9110, Section 15.4.1.
         */
        public static final int MULTIPLE_CHOICES = 300;

        /**
         * HTTP Status Code 301: Moved Permanently. Resource permanently moved to a new location. RFC 9110, Section
         * 15.4.2.
         */
        public static final int MOVED_PERMANENTLY = 301;

        /**
         * HTTP Status Code 302: Found (Temporary Redirect). Resource temporarily moved to new location. RFC 9110,
         * Section 15.4.3.
         */
        public static final int FOUND = 302;

        /**
         * HTTP Status Code 303: See Other. Client should use GET to access the new URI. RFC 9110, Section 15.4.4.
         */
        public static final int SEE_OTHER = 303;

        /**
         * HTTP Status Code 304: Not Modified. Resource has not changed and a stored response can be reused. RFC 9110,
         * Section 15.4.5.
         */
        public static final int NOT_MODIFIED = 304;

        /**
         * HTTP Status Code 305: Use Proxy. Deprecated response requiring access through a specified proxy. RFC 9110,
         * Section 15.4.6.
         */
        public static final int USE_PROXY = 305;

        /**
         * HTTP Status Code 307: Temporary Redirect. Redirect retains the original request method. RFC 9110, Section
         * 15.4.8.
         */
        public static final int TEMPORARY_REDIRECT = 307;

        /**
         * HTTP Status Code 308: Permanent Redirect. Redirect permanently retains the original request method. RFC 9110,
         * Section 15.4.9.
         */
        public static final int PERMANENT_REDIRECT = 308;

        /**
         * HTTP Status Code 400: Bad Request. Server cannot understand the request. RFC 9110, Section 15.5.1.
         */
        public static final int BAD_REQUEST = 400;

        /**
         * HTTP Status Code 401: Unauthorized. Valid authentication is required. RFC 9110, Section 15.5.2.
         */
        public static final int UNAUTHORIZED = 401;

        /**
         * HTTP Status Code 402: Payment Required. Reserved for future use. RFC 9110, Section 15.5.3.
         */
        public static final int PAYMENT_REQUIRED = 402;

        /**
         * HTTP Status Code 403: Forbidden. Server understands the request but refuses to authorize it. RFC 9110,
         * Section 15.5.4.
         */
        public static final int FORBIDDEN = 403;

        /**
         * HTTP Status Code 404: Not Found. Server cannot find the requested resource. RFC 9110, Section 15.5.5.
         */
        public static final int NOT_FOUND = 404;

        /**
         * HTTP Status Code 405: Method Not Allowed. Request method not supported for target resource. RFC 9110, Section
         * 15.5.6.
         */
        public static final int METHOD_NOT_ALLOWED = 405;

        /**
         * HTTP Status Code 406: Not Acceptable. Server cannot generate content matching Accept headers. RFC 9110,
         * Section 15.5.7.
         */
        public static final int NOT_ACCEPTABLE = 406;

        /**
         * HTTP Status Code 407: Proxy Authentication Required. Proxy server authentication needed. RFC 9110, Section
         * 15.5.8.
         */
        public static final int PROXY_AUTHENTICATION_REQUIRED = 407;

        /**
         * HTTP Status Code 408: Request Timeout. Server did not receive a complete request in time. RFC 9110, Section
         * 15.5.9.
         */
        public static final int REQUEST_TIMEOUT = 408;

        /**
         * HTTP Status Code 409: Conflict. Request conflicts with the current resource state. RFC 9110, Section 15.5.10.
         */
        public static final int CONFLICT = 409;

        /**
         * HTTP Status Code 410: Gone. Resource is no longer available and is likely permanently unavailable. RFC 9110,
         * Section 15.5.11.
         */
        public static final int GONE = 410;

        /**
         * HTTP Status Code 411: Length Required. Request lacks a required Content-Length field. RFC 9110, Section
         * 15.5.12.
         */
        public static final int LENGTH_REQUIRED = 411;

        /**
         * HTTP Status Code 412: Precondition Failed. One or more request preconditions evaluated to false. RFC 9110,
         * Section 15.5.13.
         */
        public static final int PRECONDITION_FAILED = 412;

        /**
         * HTTP Status Code 413: Content Too Large. Request content exceeds the server limit. RFC 9110, Section 15.5.14.
         */
        public static final int CONTENT_TOO_LARGE = 413;

        /**
         * HTTP Status Code 414: URI Too Long. Request target exceeds the server limit. RFC 9110, Section 15.5.15.
         */
        public static final int URI_TOO_LONG = 414;

        /**
         * HTTP Status Code 415: Unsupported Media Type. Request content format is not supported. RFC 9110, Section
         * 15.5.16.
         */
        public static final int UNSUPPORTED_MEDIA_TYPE = 415;

        /**
         * HTTP Status Code 416: Range Not Satisfiable. Requested ranges cannot be served. RFC 9110, Section 15.5.17.
         */
        public static final int RANGE_NOT_SATISFIABLE = 416;

        /**
         * HTTP Status Code 417: Expectation Failed. Server cannot satisfy the Expect field. RFC 9110, Section 15.5.18.
         */
        public static final int EXPECTATION_FAILED = 417;

        /**
         * HTTP Status Code 421: Misdirected Request. Request was directed to a server unable to produce a response. RFC
         * 9110, Section 15.5.20.
         */
        public static final int MISDIRECTED_REQUEST = 421;

        /**
         * HTTP Status Code 422: Unprocessable Content. Request syntax is valid but its instructions cannot be
         * processed. RFC 9110, Section 15.5.21.
         */
        public static final int UNPROCESSABLE_CONTENT = 422;

        /**
         * HTTP Status Code 423: Locked. Source or destination resource is locked. RFC 4918, Section 11.3.
         */
        public static final int LOCKED = 423;

        /**
         * HTTP Status Code 424: Failed Dependency. Operation failed because a dependent operation failed. RFC 4918,
         * Section 11.4.
         */
        public static final int FAILED_DEPENDENCY = 424;

        /**
         * HTTP Status Code 425: Too Early. Server is unwilling to risk processing a replayable request. RFC 8470,
         * Section 5.2.
         */
        public static final int TOO_EARLY = 425;

        /**
         * HTTP Status Code 426: Upgrade Required. Server requires the client to switch protocols. RFC 9110, Section
         * 15.5.22.
         */
        public static final int UPGRADE_REQUIRED = 426;

        /**
         * HTTP Status Code 428: Precondition Required. Origin server requires the request to be conditional. RFC 6585,
         * Section 3.
         */
        public static final int PRECONDITION_REQUIRED = 428;

        /**
         * HTTP Status Code 429: Too Many Requests. Client has sent too many requests in a given time. RFC 6585, Section
         * 4.
         */
        public static final int TOO_MANY_REQUESTS = 429;

        /**
         * HTTP Status Code 431: Request Header Fields Too Large. Request headers exceed server limits. RFC 6585,
         * Section 5.
         */
        public static final int REQUEST_HEADER_FIELDS_TOO_LARGE = 431;

        /**
         * HTTP Status Code 451: Unavailable For Legal Reasons. Access is denied because of legal demand. RFC 7725,
         * Section 3.
         */
        public static final int UNAVAILABLE_FOR_LEGAL_REASONS = 451;

        /**
         * HTTP Status Code 500: Internal Server Error. Server encountered an unexpected condition. RFC 9110, Section
         * 15.6.1.
         */
        public static final int INTERNAL_SERVER_ERROR = 500;

        /**
         * HTTP Status Code 501: Not Implemented. Server does not support requested functionality. RFC 9110, Section
         * 15.6.2.
         */
        public static final int NOT_IMPLEMENTED = 501;

        /**
         * HTTP Status Code 502: Bad Gateway. Gateway or proxy received an invalid response. RFC 9110, Section 15.6.3.
         */
        public static final int BAD_GATEWAY = 502;

        /**
         * HTTP Status Code 503: Service Unavailable. Server temporarily overloaded or under maintenance. RFC 9110,
         * Section 15.6.4.
         */
        public static final int SERVICE_UNAVAILABLE = 503;

        /**
         * HTTP Status Code 504: Gateway Timeout. Gateway or proxy did not receive a timely response. RFC 9110, Section
         * 15.6.5.
         */
        public static final int GATEWAY_TIMEOUT = 504;

        /**
         * HTTP Status Code 505: HTTP Version Not Supported. Server does not support the requested HTTP version. RFC
         * 9110, Section 15.6.6.
         */
        public static final int HTTP_VERSION_NOT_SUPPORTED = 505;

        /**
         * HTTP Status Code 506: Variant Also Negotiates. Content negotiation configuration contains a circular
         * reference. RFC 2295, Section 8.1.
         */
        public static final int VARIANT_ALSO_NEGOTIATES = 506;

        /**
         * HTTP Status Code 507: Insufficient Storage. Server cannot store the representation needed to complete the
         * request. RFC 4918, Section 11.5.
         */
        public static final int INSUFFICIENT_STORAGE = 507;

        /**
         * HTTP Status Code 508: Loop Detected. Server detected an infinite loop while processing the request. RFC 5842,
         * Section 7.2.
         */
        public static final int LOOP_DETECTED = 508;

        /**
         * HTTP Status Code 511: Network Authentication Required. Client must authenticate to gain network access. RFC
         * 6585, Section 6.
         */
        public static final int NETWORK_AUTHENTICATION_REQUIRED = 511;

    }

    /**
     * HTTP/2 SETTINGS identifiers and shared initial values defined by RFC 9113.
     */
    public static final class Setting {

        /**
         * Prevents instantiation.
         */
        private Setting() {
            // No initialization required.
        }

        /**
         * Identifier {@code 0x01} for SETTINGS_HEADER_TABLE_SIZE. The associated setting value is the maximum field
         * compression table size in octets. RFC 9113, Section 6.5.2.
         */
        public static final int HEADER_TABLE_SIZE_ID = 0x01;

        /**
         * Identifier {@code 0x02} for SETTINGS_ENABLE_PUSH. The associated value is {@code 0} to disable server push or
         * {@code 1} to enable it. RFC 9113, Section 6.5.2.
         */
        public static final int ENABLE_PUSH_ID = 0x02;

        /**
         * Identifier {@code 0x03} for SETTINGS_MAX_CONCURRENT_STREAMS. RFC 9113, Section 6.5.2.
         */
        public static final int MAX_CONCURRENT_STREAMS_ID = 0x03;

        /**
         * Identifier {@code 0x04} for SETTINGS_INITIAL_WINDOW_SIZE. RFC 9113, Section 6.5.2.
         */
        public static final int INITIAL_WINDOW_SIZE_ID = 0x04;

        /**
         * Identifier {@code 0x05} for SETTINGS_MAX_FRAME_SIZE. RFC 9113, Section 6.5.2.
         */
        public static final int MAX_FRAME_SIZE_ID = 0x05;

        /**
         * Identifier {@code 0x06} for SETTINGS_MAX_HEADER_LIST_SIZE. RFC 9113, Section 6.5.2.
         */
        public static final int MAX_HEADER_LIST_SIZE_ID = 0x06;

        /**
         * HTTP/2 initial stream flow-control window: 65,535 octets, or 64 KiB minus one octet. RFC 9113, Section 6.5.2.
         */
        public static final int DEFAULT_INITIAL_WINDOW_SIZE = 65_535;

    }

    /**
     * HTTP header names and standard header values.
     */
    public static final class Header {

        /**
         * Prevents instantiation.
         */
        private Header() {
            // No initialization required.
        }

        /**
         * HTTP Accept header field name. RFC 7231, Section 5.3.2
         */
        public static final String ACCEPT = "Accept";

        /**
         * Deprecated HTTP Accept-Charset header field name. RFC 9110, Section 12.5.2.
         */
        public static final String ACCEPT_CHARSET = "Accept-Charset";

        /**
         * HTTP Accept-Encoding header field name. RFC 7231, Section 5.3.4
         */
        public static final String ACCEPT_ENCODING = "Accept-Encoding";

        /**
         * HTTP Accept-Language header field name. RFC 7231, Section 5.3.5
         */
        public static final String ACCEPT_LANGUAGE = "Accept-Language";

        /**
         * HTTP Accept-Patch header field name. RFC 5789, Section 3.1
         */
        public static final String ACCEPT_PATCH = "Accept-Patch";

        /**
         * HTTP Accept-Ranges header field name. RFC 7233, Section 2.3
         */
        public static final String ACCEPT_RANGES = "Accept-Ranges";

        /**
         * CORS Access-Control-Allow-Credentials response header. CORS W3C Recommendation
         */
        public static final String ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";

        /**
         * CORS Access-Control-Allow-Headers response header. CORS W3C Recommendation
         */
        public static final String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";

        /**
         * CORS Access-Control-Allow-Methods response header. CORS W3C Recommendation
         */
        public static final String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";

        /**
         * CORS Access-Control-Allow-Origin response header. CORS W3C Recommendation
         */
        public static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";

        /**
         * CORS Access-Control-Expose-Headers response header. CORS W3C Recommendation
         */
        public static final String ACCESS_CONTROL_EXPOSE_HEADERS = "Access-Control-Expose-Headers";

        /**
         * CORS Access-Control-Max-Age response header. CORS W3C Recommendation
         */
        public static final String ACCESS_CONTROL_MAX_AGE = "Access-Control-Max-Age";

        /**
         * CORS Access-Control-Request-Headers request header. CORS W3C Recommendation
         */
        public static final String ACCESS_CONTROL_REQUEST_HEADERS = "Access-Control-Request-Headers";

        /**
         * CORS Access-Control-Request-Method request header. CORS W3C Recommendation
         */
        public static final String ACCESS_CONTROL_REQUEST_METHOD = "Access-Control-Request-Method";

        /**
         * HTTP Age header field name. RFC 7234, Section 5.1
         */
        public static final String AGE = "Age";

        /**
         * HTTP Allow header field name. RFC 7231, Section 7.4.1
         */
        public static final String ALLOW = "Allow";

        /**
         * HTTP Authorization header field name. RFC 7235, Section 4.2
         */
        public static final String AUTHORIZATION = "Authorization";

        /**
         * HTTP Keep-Alive header field name for connection persistence. Non-standard (HTTP/1.0 extension)
         */
        public static final String KEEP_ALIVE = "Keep-Alive";

        /**
         * HTTP Cache-Control header field name. RFC 7234, Section 5.2
         */
        public static final String CACHE_CONTROL = "Cache-Control";

        /**
         * HTTP Connection header field name. RFC 7230, Section 6.1
         */
        public static final String CONNECTION = "Connection";

        /**
         * HTTP Connection header close token. RFC 7230, Section 6.1
         */
        public static final String CONNECTION_CLOSE = "close";

        /**
         * HTTP Connection header keep-alive token. RFC 7230, Section 6.3
         */
        public static final String CONNECTION_KEEP_ALIVE = "keep-alive";

        /**
         * HTTP gzip content coding token. RFC 7231, Section 3.1.2.2
         */
        public static final String CONTENT_CODING_GZIP = "gzip";

        /**
         * HTTP Content-Encoding header field name. RFC 7231, Section 3.1.2.2
         */
        public static final String CONTENT_ENCODING = "Content-Encoding";

        /**
         * HTTP Content-Disposition header field name. RFC 6266
         */
        public static final String CONTENT_DISPOSITION = "Content-Disposition";

        /**
         * HTTP Content-Language header field name. RFC 7231, Section 3.1.3.2
         */
        public static final String CONTENT_LANGUAGE = "Content-Language";

        /**
         * HTTP Content-Length header field name. RFC 7230, Section 3.3.2
         */
        public static final String CONTENT_LENGTH = "Content-Length";

        /**
         * HTTP Content-Location header field name. RFC 7231, Section 3.1.4.2
         */
        public static final String CONTENT_LOCATION = "Content-Location";

        /**
         * HTTP Content-Range header field name. RFC 7233, Section 4.2
         */
        public static final String CONTENT_RANGE = "Content-Range";

        /**
         * HTTP Content-Type header field name. RFC 7231, Section 3.1.1.5
         */
        public static final String CONTENT_TYPE = "Content-Type";

        /**
         * HTTP Cookie header field name. RFC 6265, Section 5.4
         */
        public static final String COOKIE = "Cookie";

        /**
         * HTTP Date header field name. RFC 7231, Section 7.1.1.2
         */
        public static final String DATE = "Date";

        /**
         * HTTP ETag header field name. RFC 7232, Section 2.3
         */
        public static final String ETAG = "ETag";

        /**
         * HTTP Expect header field name. RFC 7231, Section 5.1.1
         */
        public static final String EXPECT = "Expect";

        /**
         * HTTP Expires header field name. RFC 7234, Section 5.3
         */
        public static final String EXPIRES = "Expires";

        /**
         * HTTP From header field name. RFC 7231, Section 5.5.1
         */
        public static final String FROM = "From";

        /**
         * HTTP Host header field name. RFC 7230, Section 5.4
         */
        public static final String HOST = "Host";

        /**
         * HTTP If-Match header field name. RFC 7232, Section 3.1
         */
        public static final String IF_MATCH = "If-Match";

        /**
         * HTTP If-Modified-Since header field name. RFC 7232, Section 3.3
         */
        public static final String IF_MODIFIED_SINCE = "If-Modified-Since";

        /**
         * HTTP If-None-Match header field name. RFC 7232, Section 3.2
         */
        public static final String IF_NONE_MATCH = "If-None-Match";

        /**
         * HTTP If-Range header field name. RFC 7233, Section 3.2
         */
        public static final String IF_RANGE = "If-Range";

        /**
         * HTTP If-Unmodified-Since header field name. RFC 7232, Section 3.4
         */
        public static final String IF_UNMODIFIED_SINCE = "If-Unmodified-Since";

        /**
         * HTTP Last-Modified header field name. RFC 7232, Section 2.2
         */
        public static final String LAST_MODIFIED = "Last-Modified";

        /**
         * HTTP Link header field name. RFC 5988
         */
        public static final String LINK = "Link";

        /**
         * HTTP Location header field name. RFC 7231, Section 7.1.2
         */
        public static final String LOCATION = "Location";

        /**
         * HTTP Max-Forwards header field name. RFC 7231, Section 5.1.2
         */
        public static final String MAX_FORWARDS = "Max-Forwards";

        /**
         * HTTP Origin header field name. RFC 6454
         */
        public static final String ORIGIN = "Origin";

        /**
         * HTTP Pragma header field name. RFC 7234, Section 5.4
         */
        public static final String PRAGMA = "Pragma";

        /**
         * HTTP Proxy-Authenticate header field name. RFC 7235, Section 4.3
         */
        public static final String PROXY_AUTHENTICATE = "Proxy-Authenticate";

        /**
         * HTTP Proxy-Authorization header field name. RFC 7235, Section 4.4
         */
        public static final String PROXY_AUTHORIZATION = "Proxy-Authorization";

        /**
         * HTTP Proxy-Connection header field name (non-standard, deprecated). HTTP/1.0 extension (obsolete)
         */
        public static final String PROXY_CONNECTION = "Proxy-Connection";

        /**
         * HTTP Range header field name. RFC 7233, Section 3.1
         */
        public static final String RANGE = "Range";

        /**
         * HTTP Referer header field name. RFC 7231, Section 5.5.2 (Note: historical misspelling of "referrer")
         */
        public static final String REFERER = "Referer";

        /**
         * HTTP Refresh header field name (non-standard). Browser extension for automatic page refresh.
         */
        public static final String REFRESH = "Refresh";

        /**
         * HTTP Retry-After header field name. RFC 7231, Section 7.1.3
         */
        public static final String RETRY_AFTER = "Retry-After";

        /**
         * HTTP Server header field name. RFC 7231, Section 7.4.2
         */
        public static final String SERVER = "Server";

        /**
         * HTTP Set-Cookie header field name. RFC 6265
         */
        public static final String SET_COOKIE = "Set-Cookie";

        /**
         * Obsolete HTTP Set-Cookie2 header field name. RFC 2965, superseded by RFC 6265.
         */
        public static final String SET_COOKIE2 = "Set-Cookie2";

        /**
         * HTTP TE header field name. RFC 7230, Section 4.3
         */
        public static final String TE = "TE";

        /**
         * HTTP Trailer header field name. RFC 7230, Section 4.4
         */
        public static final String TRAILER = "Trailer";

        /**
         * HTTP Transfer-Encoding header field name. RFC 7230, Section 3.3.1
         */
        public static final String TRANSFER_ENCODING = "Transfer-Encoding";

        /**
         * HTTP chunked transfer coding token. RFC 7230, Section 4.1
         */
        public static final String TRANSFER_CODING_CHUNKED = "chunked";

        /**
         * HTTP Upgrade header field name. RFC 7230, Section 6.7
         */
        public static final String UPGRADE = "Upgrade";

        /**
         * HTTP User-Agent header field name. RFC 7231, Section 5.5.3
         */
        public static final String USER_AGENT = "User-Agent";

        /**
         * HTTP Vary header field name. RFC 7231, Section 7.1.4
         */
        public static final String VARY = "Vary";

        /**
         * HTTP Via header field name. RFC 7230, Section 5.7.1
         */
        public static final String VIA = "Via";

        /**
         * HTTP Warning header field name. RFC 7234, Section 5.5
         */
        public static final String WARNING = "Warning";

        /**
         * HTTP WWW-Authenticate header field name. RFC 7235, Section 4.1
         */
        public static final String WWW_AUTHENTICATE = "WWW-Authenticate";

        /**
         * Encoding header (non-standard).
         */
        public static final String ENCODING = "Encoding";

        /**
         * Case-insensitive {@code trailers} token permitted as the HTTP/2 TE field value. RFC 9113, Section 8.2.1.
         */
        public static final String TE_TRAILERS = "trailers";

        /**
         * Obsolete Content-MD5 header field name formerly used for message integrity. RFC 1864.
         */
        public static final String CONTENT_MD5 = "Content-MD5";

        /**
         * Httpd-Preemptive header (Apache specific, non-standard).
         */
        public static final String HTTPD_PREEMPTIVE = "Httpd-Preemptive";

        /**
         * HTTP Strict-Transport-Security header for HSTS. RFC 6797
         */
        public static final String STRICT_TRANSPORT_SECURITY = "Strict-Transport-Security";

        /**
         * SOAPAction request header field name defined by SOAP 1.1.
         */
        public static final String SOAP_ACTION = "SOAPAction";

        /**
         * HTTP/2 request pseudo-header {@code :method}. RFC 9113, Section 8.3.1.
         */
        public static final String PSEUDO_METHOD = ":method";

        /**
         * HTTP/2 response pseudo-header {@code :status}. RFC 9113, Section 8.3.2.
         */
        public static final String PSEUDO_STATUS = ":status";

        /**
         * HTTP/2 request pseudo-header {@code :path}. RFC 9113, Section 8.3.1.
         */
        public static final String PSEUDO_PATH = ":path";

        /**
         * HTTP/2 request pseudo-header {@code :scheme}. RFC 9113, Section 8.3.1.
         */
        public static final String PSEUDO_SCHEME = ":scheme";

        /**
         * HTTP/2 request pseudo-header {@code :authority}. RFC 9113, Section 8.3.1.
         */
        public static final String PSEUDO_AUTHORITY = ":authority";

    }

    /**
     * HTTP request parameter names used by Bus API contracts.
     */
    public static final class Param {

        /**
         * Prevents instantiation.
         */
        private Param() {
            // No initialization required.
        }

        /**
         * Logical API method parameter name.
         */
        public static final String METHOD = "method";

        /**
         * Desired response format parameter name.
         */
        public static final String FORMAT = "format";

        /**
         * Requested API version parameter name.
         */
        public static final String VERSION = "v";

        /**
         * Request signature parameter name.
         */
        public static final String SIGN = "sign";

        /**
         * Request timestamp parameter name.
         */
        public static final String TIMESTAMP = "timestamp";

    }

    /**
     * Conventional HTTP endpoint paths used by Bus APIs.
     */
    public static final class Path {

        /**
         * Prevents instantiation.
         */
        private Path() {
            // No initialization required.
        }

        /**
         * Resource retrieval path.
         */
        public static final String GET = "/get";
        /**
         * Resource creation path.
         */
        public static final String CREATE = "/create";
        /**
         * Resource deletion path.
         */
        public static final String DELETE = "/delete";
        /**
         * Resource removal path.
         */
        public static final String REMOVE = "/remove";
        /**
         * Resource update path.
         */
        public static final String UPDATE = "/update";
        /**
         * Resource save path.
         */
        public static final String SAVE = "/save";
        /**
         * Resource addition path.
         */
        public static final String ADD = "/add";
        /**
         * Resource editing path.
         */
        public static final String EDIT = "/edit";
        /**
         * Resource detail path.
         */
        public static final String VIEW = "/view";
        /**
         * Resource list path.
         */
        public static final String LIST = "/list";
        /**
         * Resource query path.
         */
        public static final String QUERY = "/query";
        /**
         * Resource search path.
         */
        public static final String SEARCH = "/search";
        /**
         * Paginated resource path.
         */
        public static final String PAGE = "/page";
        /**
         * Data extraction path.
         */
        public static final String EXTRACT = "/extract";
        /**
         * Resource preview path.
         */
        public static final String PREVIEW = "/preview";
        /**
         * Registry base path.
         */
        public static final String REGISTRY = "/registry";
        /**
         * Registry push path.
         */
        public static final String PUSH = "/push";
        /**
         * Registry pull path.
         */
        public static final String PULL = "/pull";
        /**
         * Data fetch path.
         */
        public static final String FETCH = "/fetch";
        /**
         * Upload path.
         */
        public static final String UPLOAD = "/upload";
        /**
         * Download path.
         */
        public static final String DOWNLOAD = "/download";
        /**
         * Image manifest path.
         */
        public static final String MANIFESTS = "/manifests";
        /**
         * Version tag path.
         */
        public static final String TAGS = "/tags";
        /**
         * Repository catalog path.
         */
        public static final String CATALOG = "/catalog";
        /**
         * Upload initiation path.
         */
        public static final String INITIATE = "/initiate";
        /**
         * Upload completion path.
         */
        public static final String COMPLETE = "/complete";
        /**
         * Upload part path.
         */
        public static final String PART = "/part";
        /**
         * Health check path.
         */
        public static final String HEALTH = "/health";
        /**
         * Liveness probe path.
         */
        public static final String LIVENESS = "/liveness";
        /**
         * Readiness probe path.
         */
        public static final String READINESS = "/readiness";
        /**
         * Metrics export path.
         */
        public static final String METRICS = "/metrics";
        /**
         * System information path.
         */
        public static final String INFO = "/info";
        /**
         * Reachability check path.
         */
        public static final String PING = "/ping";
        /**
         * Log access path.
         */
        public static final String LOGS = "/logs";
        /**
         * Heap dump path.
         */
        public static final String HEAP_DUMP = "/dump";
        /**
         * Thread dump path.
         */
        public static final String THREAD_DUMP = "/threaddump";
        /**
         * Environment information path.
         */
        public static final String ENV = "/env";
        /**
         * Task trigger path.
         */
        public static final String TRIGGER = "/trigger";
        /**
         * Operation status path.
         */
        public static final String STATUS = "/status";
        /**
         * Operation cancellation path.
         */
        public static final String CANCEL = "/cancel";
        /**
         * Configuration path.
         */
        public static final String CONFIG = "/config";
        /**
         * Data synchronization path.
         */
        public static final String SYNC = "/sync";
        /**
         * Batch operation path.
         */
        public static final String BATCH = "/batch";
        /**
         * Bulk import path.
         */
        public static final String IMPORT = "/import";
        /**
         * Bulk export path.
         */
        public static final String EXPORT = "/export";
        /**
         * Operation retry path.
         */
        public static final String RETRY = "/retry";
        /**
         * Operation rollback path.
         */
        public static final String ROLLBACK = "/rollback";
        /**
         * Authentication entry path.
         */
        public static final String AUTH = "/auth";
        /**
         * Token issue path.
         */
        public static final String TOKEN = "/token";
        /**
         * Token revocation path.
         */
        public static final String REVOKE = "/revoke";
        /**
         * Verification path.
         */
        public static final String VERIFY = "/verify";
        /**
         * Current user path.
         */
        public static final String ME = "/me";
        /**
         * Public key path.
         */
        public static final String KEYS = "/keys";
        /**
         * Authorization path.
         */
        public static final String AUTHORIZE = "/authorize";
        /**
         * Token introspection path.
         */
        public static final String INTROSPECT = "/introspect";
        /**
         * Session management path.
         */
        public static final String SESSION = "/session";
        /**
         * Resource metadata path.
         */
        public static final String METADATA = "/metadata";
        /**
         * API capabilities path.
         */
        public static final String CAPABILITIES = "/capabilities";
        /**
         * WebSocket entry path.
         */
        public static final String WEBSOCKET = "/ws";
        /**
         * Event stream path.
         */
        public static final String EVENTS = "/events";
        /**
         * Webhook callback path.
         */
        public static final String WEBHOOK = "/webhook";
        /**
         * Resource subscription path.
         */
        public static final String SUBSCRIBE = "/subscribe";
        /**
         * Message publication path.
         */
        public static final String PUBLISH = "/publish";
        /**
         * OpenID Connect discovery path.
         */
        public static final String OPENID_CONFIGURATION = "/.well-known/openid-configuration";
        /**
         * Security policy discovery path.
         */
        public static final String SECURITY_TXT = "/.well-known/security.txt";
        /**
         * Web crawler instruction path.
         */
        public static final String ROBOTS_TXT = "/robots.txt";
        /**
         * Search engine sitemap path.
         */
        public static final String SITEMAP_XML = "/sitemap.xml";
        /**
         * Website icon path.
         */
        public static final String FAVICON_ICO = "/favicon.ico";

    }

    /**
     * HTTP authentication values and credential extraction helpers.
     */
    public static final class Auth {

        /**
         * Prevents instantiation.
         */
        private Auth() {
            // No initialization required.
        }

        /**
         * Bearer authentication scheme name.
         */
        public static final String BEARER = "Bearer";

        /**
         * Bearer authentication scheme prefix, including the separating space.
         */
        public static final String BEARER_PREFIX = BEARER + " ";

        /**
         * Metadata label used for credentials without an explicit authorization scheme.
         */
        private static final String RAW_SCHEME = "Raw";

        /**
         * Candidate names for backward-compatible access-token transport.
         */
        public static final List<String> TOKEN_KEYS = List.of(Header.AUTHORIZATION, "X-Access-Token", "X_Access_Token");

        /**
         * Candidate names for API-key transport.
         */
        public static final List<String> API_KEY_KEYS = List.of(
                "apiKey",
                "api_key",
                "x_api_key",
                "api_id",
                "x_api_id",
                "X-API-ID",
                "X-API-KEY",
                "API-KEY",
                "API-ID");

        /**
         * Resolves the preferred credential from request headers and parameters.
         * <p>
         * Token credentials take precedence over API keys. This overload is intended for request contexts that only
         * expose headers and query/form parameters.
         * </p>
         *
         * @param headers    request headers
         * @param parameters request parameters
         * @return credential, or {@code null} when no credential is present
         */
        public static Credential credential(final Map<String, ?> headers, final Map<String, ?> parameters) {
            return credential(headers, parameters, null, null);
        }

        /**
         * Resolves the preferred credential from all supported request value maps.
         * <p>
         * The lookup order is token first, then API key. Token lookup checks {@link Header#AUTHORIZATION}, generic
         * token headers, token parameters, token JSON body values, then token cookies. API-key lookup checks headers,
         * parameters, JSON body values, then cookies.
         * </p>
         *
         * @param headers    request headers
         * @param parameters request parameters
         * @param jsonBody   JSON body values
         * @param cookies    request cookies
         * @return credential, or {@code null} when no credential is present
         */
        public static Credential credential(
                final Map<String, ?> headers,
                final Map<String, ?> parameters,
                final Map<String, ?> jsonBody,
                final Map<String, ?> cookies) {
            Credential credential = tokenCredential(headers, parameters, jsonBody, cookies);
            if (credential != null) {
                return credential;
            }
            return apiKeyCredential(headers, parameters, jsonBody, cookies);
        }

        /**
         * Resolves a token value from request headers and parameters.
         * <p>
         * This method returns only the credential value. Use {@link #tokenCredential(Map, Map)} when the caller also
         * needs metadata about source, key, or scheme.
         * </p>
         *
         * @param headers    request headers
         * @param parameters request parameters
         * @return token value, or {@code null} when absent
         */
        public static String token(final Map<String, ?> headers, final Map<String, ?> parameters) {
            return value(tokenCredential(headers, parameters));
        }

        /**
         * Resolves a token value from all supported request value maps.
         * <p>
         * This method strips a leading {@link #BEARER_PREFIX} when present and returns only the normalized token value.
         * Use {@link #tokenCredential(Map, Map, Map, Map)} when metadata is required.
         * </p>
         *
         * @param headers    request headers
         * @param parameters request parameters
         * @param jsonBody   JSON body values
         * @param cookies    request cookies
         * @return token value, or {@code null} when absent
         */
        public static String token(
                final Map<String, ?> headers,
                final Map<String, ?> parameters,
                final Map<String, ?> jsonBody,
                final Map<String, ?> cookies) {
            return value(tokenCredential(headers, parameters, jsonBody, cookies));
        }

        /**
         * Resolves an API key value from request headers and parameters.
         * <p>
         * This method returns only the normalized API key value. Use {@link #apiKeyCredential(Map, Map)} when metadata
         * is required.
         * </p>
         *
         * @param headers    request headers
         * @param parameters request parameters
         * @return API key value, or {@code null} when absent
         */
        public static String apiKey(final Map<String, ?> headers, final Map<String, ?> parameters) {
            return value(apiKeyCredential(headers, parameters));
        }

        /**
         * Resolves an API key value from all supported request value maps.
         * <p>
         * This method returns only the normalized API key value. Use {@link #apiKeyCredential(Map, Map, Map, Map)} when
         * metadata is required.
         * </p>
         *
         * @param headers    request headers
         * @param parameters request parameters
         * @param jsonBody   JSON body values
         * @param cookies    request cookies
         * @return API key value, or {@code null} when absent
         */
        public static String apiKey(
                final Map<String, ?> headers,
                final Map<String, ?> parameters,
                final Map<String, ?> jsonBody,
                final Map<String, ?> cookies) {
            return value(apiKeyCredential(headers, parameters, jsonBody, cookies));
        }

        /**
         * Resolves a token credential from request headers and parameters.
         * <p>
         * The standard {@link Header#AUTHORIZATION} header is checked before generic token aliases, and the returned
         * credential includes source, key, and scheme metadata.
         * </p>
         *
         * @param headers    request headers
         * @param parameters request parameters
         * @return token credential, or {@code null} when absent
         */
        public static Credential tokenCredential(final Map<String, ?> headers, final Map<String, ?> parameters) {
            return tokenCredential(headers, parameters, null, null);
        }

        /**
         * Resolves a token credential from all supported request value maps.
         * <p>
         * The lookup order is {@link Header#AUTHORIZATION}, generic token headers, token parameters, token JSON body
         * values, then token cookies. A leading {@link #BEARER_PREFIX} is stripped from the returned value.
         * </p>
         *
         * @param headers    request headers
         * @param parameters request parameters
         * @param jsonBody   JSON body values
         * @param cookies    request cookies
         * @return token credential, or {@code null} when absent
         */
        public static Credential tokenCredential(
                final Map<String, ?> headers,
                final Map<String, ?> parameters,
                final Map<String, ?> jsonBody,
                final Map<String, ?> cookies) {
            Credential credential = authorizationCredential(headers);
            if (credential != null) {
                return credential;
            }
            credential = tokenCredential(headers, EnumValue.Params.HEADER, true);
            if (credential != null) {
                return credential;
            }
            credential = tokenCredential(parameters, EnumValue.Params.PARAMETER, true);
            if (credential != null) {
                return credential;
            }
            credential = tokenCredential(jsonBody, EnumValue.Params.JSON_BODY, true);
            if (credential != null) {
                return credential;
            }
            return tokenCredential(cookies, EnumValue.Params.COOKIE, true);
        }

        /**
         * Resolves an API key credential from request headers and parameters.
         * <p>
         * Header aliases take precedence over parameter aliases.
         * </p>
         *
         * @param headers    request headers
         * @param parameters request parameters
         * @return API key credential, or {@code null} when absent
         */
        public static Credential apiKeyCredential(final Map<String, ?> headers, final Map<String, ?> parameters) {
            return apiKeyCredential(headers, parameters, null, null);
        }

        /**
         * Resolves an API key credential from all supported request value maps.
         * <p>
         * The lookup order is API-key headers, parameters, JSON body values, then cookies.
         * </p>
         *
         * @param headers    request headers
         * @param parameters request parameters
         * @param jsonBody   JSON body values
         * @param cookies    request cookies
         * @return API key credential, or {@code null} when absent
         */
        public static Credential apiKeyCredential(
                final Map<String, ?> headers,
                final Map<String, ?> parameters,
                final Map<String, ?> jsonBody,
                final Map<String, ?> cookies) {
            Credential credential = apiKeyCredential(headers, EnumValue.Params.HEADER);
            if (credential != null) {
                return credential;
            }
            credential = apiKeyCredential(parameters, EnumValue.Params.PARAMETER);
            if (credential != null) {
                return credential;
            }
            credential = apiKeyCredential(jsonBody, EnumValue.Params.JSON_BODY);
            if (credential != null) {
                return credential;
            }
            return apiKeyCredential(cookies, EnumValue.Params.COOKIE);
        }

        /**
         * Resolves a token from the standard {@link Header#AUTHORIZATION} header.
         * <p>
         * {@code Bearer} values are returned with the bearer prefix stripped. Scheme-qualified values that do not use
         * Bearer authentication are rejected so an unknown authorization scheme cannot be mistaken for a raw token.
         * </p>
         *
         * @param headers request headers
         * @return token credential, or {@code null} when the Authorization header is absent or unsupported
         */
        private static Credential authorizationCredential(final Map<String, ?> headers) {
            Match match = match(headers, Header.AUTHORIZATION);
            if (match == null) {
                return null;
            }
            String authorization = normalize(match.value());
            if (authorization == null) {
                return null;
            }
            if (startsWithIgnoreCase(authorization, BEARER_PREFIX)) {
                String value = normalize(authorization.substring(BEARER_PREFIX.length()));
                return value == null ? null
                        : new Credential(EnumValue.Credential.TOKEN, value, EnumValue.Params.HEADER, match.key(),
                                BEARER);
            }
            if (containsWhitespace(authorization)) {
                return null;
            }
            return new Credential(EnumValue.Credential.TOKEN, authorization, EnumValue.Params.HEADER, match.key(),
                    RAW_SCHEME);
        }

        /**
         * Resolves a token from one source map using {@link #TOKEN_KEYS}.
         *
         * @param values            candidate values from one request source
         * @param source            source represented by the candidate map
         * @param skipAuthorization whether the {@link Header#AUTHORIZATION} alias should be skipped because it has
         *                          already been handled as a dedicated channel
         * @return token credential, or {@code null} when no token alias has a non-blank value
         */
        private static Credential tokenCredential(
                final Map<String, ?> values,
                final EnumValue.Params source,
                final boolean skipAuthorization) {
            Match match = match(values, skipAuthorization, TOKEN_KEYS);
            if (match == null) {
                return null;
            }
            String value = normalize(match.value());
            if (value == null) {
                return null;
            }
            String scheme = RAW_SCHEME;
            if (startsWithIgnoreCase(value, BEARER_PREFIX)) {
                value = normalize(value.substring(BEARER_PREFIX.length()));
                scheme = BEARER;
            }
            return value == null ? null
                    : new Credential(EnumValue.Credential.TOKEN, value, source, match.key(), scheme);
        }

        /**
         * Resolves an API key from one source map using {@link #API_KEY_KEYS}.
         *
         * @param values candidate values from one request source
         * @param source source represented by the candidate map
         * @return API key credential, or {@code null} when no API-key alias has a non-blank value
         */
        private static Credential apiKeyCredential(final Map<String, ?> values, final EnumValue.Params source) {
            Match match = match(values, false, API_KEY_KEYS);
            if (match == null) {
                return null;
            }
            String value = normalize(match.value());
            return value == null ? null
                    : new Credential(EnumValue.Credential.API_KEY, value, source, match.key(), RAW_SCHEME);
        }

        /**
         * Finds a non-blank value for one key using exact lookup first and case-insensitive lookup second.
         *
         * @param values candidate values
         * @param key    key to match
         * @return matched key and value, or {@code null} when absent
         */
        private static Match match(final Map<String, ?> values, final String key) {
            return match(values, false, List.of(key));
        }

        /**
         * Finds the first non-blank value for a list of candidate keys.
         * <p>
         * Exact map lookup is attempted before the case-insensitive scan so maps that already support exact or
         * case-insensitive lookup can remain efficient while plain maps still work.
         * </p>
         *
         * @param values            candidate values
         * @param skipAuthorization whether the {@link Header#AUTHORIZATION} alias should be skipped
         * @param keys              candidate keys in lookup priority order
         * @return matched key and value, or {@code null} when absent
         */
        private static Match match(
                final Map<String, ?> values,
                final boolean skipAuthorization,
                final Collection<String> keys) {
            if (values == null || values.isEmpty() || keys == null || keys.isEmpty()) {
                return null;
            }

            for (String key : keys) {
                if (skip(skipAuthorization, key)) {
                    continue;
                }
                String value = firstValue(values.get(key));
                if (value != null) {
                    return new Match(key, value);
                }
            }

            for (String key : keys) {
                if (skip(skipAuthorization, key)) {
                    continue;
                }
                for (Map.Entry<String, ?> entry : values.entrySet()) {
                    String candidateKey = entry.getKey();
                    if (candidateKey != null && key.equalsIgnoreCase(candidateKey)) {
                        String value = firstValue(entry.getValue());
                        if (value != null) {
                            return new Match(candidateKey, value);
                        }
                    }
                }
            }
            return null;
        }

        /**
         * Determines whether the current key should be skipped during generic alias lookup.
         *
         * @param skipAuthorization whether Authorization should be skipped
         * @param key               candidate key
         * @return {@code true} when the key is Authorization and dedicated handling already ran
         */
        private static boolean skip(final boolean skipAuthorization, final String key) {
            return skipAuthorization && key != null && Header.AUTHORIZATION.equalsIgnoreCase(key);
        }

        /**
         * Extracts the first non-blank scalar value from a supported value object.
         * <p>
         * Character sequences, numbers, characters, arrays, primitive arrays, and collections are supported. Arrays and
         * collections are walked in encounter order and the first non-blank normalized scalar is returned.
         * </p>
         *
         * @param value raw map value
         * @return normalized first value, or {@code null} when absent or blank
         */
        private static String firstValue(final Object value) {
            if (value == null) {
                return null;
            }
            if (value instanceof Collection<?> collection) {
                for (Object item : collection) {
                    String text = firstValue(item);
                    if (text != null) {
                        return text;
                    }
                }
                return null;
            }
            if (value.getClass().isArray()) {
                int length = Array.getLength(value);
                for (int i = 0; i < length; i++) {
                    String text = firstValue(Array.get(value, i));
                    if (text != null) {
                        return text;
                    }
                }
                return null;
            }
            if (value instanceof CharSequence || value instanceof Number || value instanceof Character) {
                return normalize(String.valueOf(value));
            }
            return null;
        }

        /**
         * Trims one candidate credential value and converts blanks to {@code null}.
         *
         * @param value raw value
         * @return trimmed value, or {@code null} when the value is blank
         */
        private static String normalize(final String value) {
            if (value == null) {
                return null;
            }
            String normalized = value.trim();
            return normalized.isEmpty() ? null : normalized;
        }

        /**
         * Checks whether a value starts with the supplied prefix ignoring case.
         *
         * @param value  value to inspect
         * @param prefix expected prefix
         * @return {@code true} when the value starts with the prefix
         */
        private static boolean startsWithIgnoreCase(final String value, final String prefix) {
            return value != null && prefix != null && value.regionMatches(true, 0, prefix, 0, prefix.length());
        }

        /**
         * Checks whether a normalized value contains whitespace.
         *
         * @param value normalized value
         * @return {@code true} when at least one whitespace character is present
         */
        private static boolean containsWhitespace(final String value) {
            return value.chars().anyMatch(Character::isWhitespace);
        }

        /**
         * Returns only the value component of a resolved credential.
         *
         * @param credential resolved credential
         * @return credential value, or {@code null} when no credential was resolved
         */
        private static String value(final Credential credential) {
            return credential == null ? null : credential.value();
        }

        /**
         * A credential resolved from request data.
         * <p>
         * The value component contains the real credential and must not be written to logs. For diagnostic logs, prefer
         * metadata such as {@link #type()}, {@link #source()}, {@link #key()}, {@link #scheme()}, or value length.
         * </p>
         *
         * @param type   credential type
         * @param value  credential value
         * @param source matched source
         * @param key    matched key
         * @param scheme matched authorization scheme
         */
        public record Credential(EnumValue.Credential type, String value, EnumValue.Params source, String key,
                String scheme) {

            /**
             * Returns a diagnostic representation with the credential value redacted.
             *
             * @return redacted credential representation
             */
            @Override
            public String toString() {
                return "Credential[type=" + this.type + ", value=<redacted>, source=" + this.source + ", key="
                        + this.key + ", scheme=" + this.scheme + "]";
            }

        }

        /**
         * A matched request value with the key name that actually supplied it.
         *
         * @param key   matched key name
         * @param value normalized matched value
         */
        private record Match(String key, String value) {

        }

    }

    /**
     * HTTP cache directives.
     */
    public static final class Cache {

        /**
         * Prevents instantiation.
         */
        private Cache() {
            // No initialization required.
        }

        /**
         * Cache response content that never changes.
         */
        public static final String IMMUTABLE = "immutable";

        /**
         * Maximum freshness lifetime directive.
         */
        public static final String MAX_AGE = "max-age";

        /**
         * Maximum accepted staleness directive.
         */
        public static final String MAX_STALE = "max-stale";

        /**
         * Minimum remaining freshness directive.
         */
        public static final String MIN_FRESH = "min-fresh";

        /**
         * Mandatory revalidation directive.
         */
        public static final String MUST_REVALIDATE = "must-revalidate";

        /**
         * Disable cached response reuse without validation.
         */
        public static final String NO_CACHE = "no-cache";

        /**
         * Disable cache storage directive.
         */
        public static final String NO_STORE = "no-store";

        /**
         * Disable intermediary transformations directive.
         */
        public static final String NO_TRANSFORM = "no-transform";

        /**
         * Use only an already cached response directive.
         */
        public static final String ONLY_IF_CACHED = "only-if-cached";

        /**
         * Restrict a response to private caches.
         */
        public static final String PRIVATE = "private";

        /**
         * Permit storage by shared caches.
         */
        public static final String PUBLIC = "public";

        /**
         * Shared-cache maximum age directive.
         */
        public static final String S_MAXAGE = "s-maxage";

    }

    /**
     * WebSocket opening-handshake constants.
     */
    public static final class WebSocket {

        /**
         * Prevents instantiation.
         */
        private WebSocket() {
            // No initialization required.
        }

        /**
         * WebSocket client key header name.
         */
        public static final String KEY = "Sec-WebSocket-Key";

        /**
         * Required decoded client key length.
         */
        public static final int KEY_BYTES = 16;

        /**
         * WebSocket server acceptance header name.
         */
        public static final String ACCEPT = "Sec-WebSocket-Accept";

        /**
         * GUID used to derive the WebSocket acceptance value.
         */
        public static final String ACCEPT_GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

        /**
         * WebSocket version header name.
         */
        public static final String VERSION = "Sec-WebSocket-Version";

        /**
         * RFC 6455 protocol version value.
         */
        public static final String VERSION_13 = "13";

        /**
         * WebSocket subprotocol header name.
         */
        public static final String PROTOCOL = "Sec-WebSocket-Protocol";

        /**
         * WebSocket HTTP upgrade token.
         */
        public static final String UPGRADE_TOKEN = "websocket";

    }

    /**
     * HTTP methods and Bus registry routing verbs shared by Bus modules.
     * <p>
     * Each value carries the integer {@code verb} code used by registry assets together with its wire or routing token.
     * {@link #ALL}, {@link #NONE}, {@link #BEFORE}, and {@link #AFTER} are Bus routing controls rather than HTTP
     * request methods. The remaining values model standard HTTP or WebDAV methods.
     * </p>
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum Method {

        /**
         * Wildcard method used to match every HTTP method.
         */
        ALL(0, "ALL", false, false),
        /**
         * Sentinel value representing no HTTP method.
         */
        NONE(-1, "NONE", false, false),
        /**
         * GET request.
         */
        GET(1, "GET", false, false),
        /**
         * POST request.
         */
        POST(2, "POST", true, true),
        /**
         * HEAD request.
         */
        HEAD(3, "HEAD", false, false),
        /**
         * PUT request.
         */
        PUT(4, "PUT", true, true),
        /**
         * PATCH request.
         */
        PATCH(5, "PATCH", true, true),
        /**
         * DELETE request.
         */
        DELETE(6, "DELETE", false, false),
        /**
         * OPTIONS request.
         */
        OPTIONS(7, "OPTIONS", false, false),
        /**
         * TRACE request.
         */
        TRACE(8, "TRACE", false, false),
        /**
         * CONNECT request.
         */
        CONNECT(9, "CONNECT", false, false),
        /**
         * Custom method invoked before the primary operation.
         */
        BEFORE(10, "BEFORE", false, false),
        /**
         * Custom method invoked after the primary operation.
         */
        AFTER(11, "AFTER", false, false),
        /**
         * WebDAV resource move method.
         */
        MOVE(12, "MOVE", false, false),
        /**
         * WebDAV property update method.
         */
        PROPPATCH(13, "PROPPATCH", true, true),
        /**
         * WebDAV report generation method.
         */
        REPORT(14, "REPORT", true, true),
        /**
         * WebDAV property retrieval method.
         */
        PROPFIND(15, "PROPFIND", true, false);

        /**
         * Lookup table keyed by registry verb code.
         */
        private static final Map<Integer, Method> BY_VERB = Stream.of(values())
                .collect(Collectors.toUnmodifiableMap(Method::verb, Function.identity()));

        /**
         * Lookup table keyed by upper-cased method token.
         */
        private static final Map<String, Method> BY_VALUE = Stream.of(values())
                .collect(Collectors.toUnmodifiableMap(Method::value, Function.identity()));

        /**
         * Registry verb code used by stored assets.
         */
        private final int verb;

        /**
         * HTTP wire token or Bus routing token.
         */
        private final String value;

        /**
         * Whether request content is permitted by the Bus HTTP client policy.
         */
        private final boolean permitsBody;

        /**
         * Whether request content is required by the Bus HTTP client policy.
         */
        private final boolean requiresBody;

        /**
         * Creates one canonical HTTP method mapping.
         *
         * @param verb         registry verb code
         * @param value        HTTP method token
         * @param permitsBody  whether the method permits a request body
         * @param requiresBody whether the method requires a request body
         */
        Method(final int verb, final String value, final boolean permitsBody, final boolean requiresBody) {
            this.verb = verb;
            this.value = value;
            this.permitsBody = permitsBody;
            this.requiresBody = requiresBody;
        }

        /**
         * Returns the registry verb code.
         *
         * @return verb code
         */
        public int verb() {
            return this.verb;
        }

        /**
         * Returns the HTTP wire token or Bus routing token.
         *
         * @return method token
         */
        public String value() {
            return this.value;
        }

        /**
         * Returns whether this method permits request content.
         *
         * @return {@code true} when request content is allowed
         */
        public boolean permitsBody() {
            return this.permitsBody;
        }

        /**
         * Returns whether this method requires request content.
         *
         * @return {@code true} when request content is required
         */
        public boolean requiresBody() {
            return this.requiresBody;
        }

        /**
         * Resolves one method from the persisted registry verb code.
         *
         * @param verb registry verb code
         * @return canonical method
         * @throws IllegalArgumentException when the verb is unsupported
         */
        public static Method of(int verb) {
            Method method = BY_VERB.get(verb);
            if (method == null) {
                throw new IllegalArgumentException("Unsupported HTTP verb: " + verb);
            }
            return method;
        }

        /**
         * Resolves one method from a raw HTTP method token.
         *
         * @param method raw method token
         * @return canonical method
         * @throws IllegalArgumentException when the token is blank or unsupported
         */
        public static Method of(String method) {
            if (method == null || method.isBlank()) {
                throw new IllegalArgumentException("HTTP method cannot be blank");
            }
            Method resolved = BY_VALUE.get(method.trim().toUpperCase(Locale.ROOT));
            if (resolved == null) {
                throw new IllegalArgumentException("Unsupported HTTP method: " + method);
            }
            return resolved;
        }

        /**
         * Returns whether the supplied token resolves to this canonical method.
         *
         * @param method raw method token
         * @return {@code true} when the token resolves to this method
         */
        public boolean matches(String method) {
            return method != null && this == BY_VALUE.get(method.trim().toUpperCase(Locale.ROOT));
        }

    }

}
