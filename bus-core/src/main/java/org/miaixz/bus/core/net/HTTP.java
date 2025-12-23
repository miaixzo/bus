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
package org.miaixz.bus.core.net;

/**
 * HTTP protocol related constants including HTTP methods, status codes, header fields, and HTTP/2 settings parameters.
 * <p>
 * This class provides standard HTTP method constants, HTTP status code constants, common HTTP header field constants,
 * and HTTP/2 protocol configuration parameters.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class HTTP {

    /**
     * Constructs a new HTTP. Utility class constructor for static access.
     */
    private HTTP() {
    }

    /**
     * HTTP Method: Matches all methods, commonly used in configurations or filters. RFC 7230, Section 5.2
     */
    public static final String ALL = "ALL";
    /**
     * HTTP Method: No method, used for disabling or default configurations.
     */
    public static final String NONE = "NONE";
    /**
     * HTTP Method: GET, used to retrieve resources. RFC 7231, Section 4.3.1
     */
    public static final String GET = "GET";
    /**
     * HTTP Method: POST, used to create resources or submit data. RFC 7231, Section 4.3.3
     */
    public static final String POST = "POST";
    /**
     * HTTP Method: PUT, used to update or create resources. RFC 7231, Section 4.3.4
     */
    public static final String PUT = "PUT";
    /**
     * HTTP Method: PATCH, used for partial resource updates. RFC 5789
     */
    public static final String PATCH = "PATCH";
    /**
     * HTTP Method: DELETE, used to delete resources. RFC 7231, Section 4.3.5
     */
    public static final String DELETE = "DELETE";
    /**
     * HTTP Method: HEAD, retrieves only response headers without body content. RFC 7231, Section 4.3.2
     */
    public static final String HEAD = "HEAD";
    /**
     * HTTP Method: TRACE, used for diagnostics, echoes request content. RFC 7231, Section 4.3.8
     */
    public static final String TRACE = "TRACE";
    /**
     * HTTP Method: CONNECT, establishes a tunnel to the server, typically for HTTPS. RFC 7231, Section 4.3.6
     */
    public static final String CONNECT = "CONNECT";
    /**
     * HTTP Method: OPTIONS, queries server-supported methods. RFC 7231, Section 4.3.7
     */
    public static final String OPTIONS = "OPTIONS";
    /**
     * HTTP Method: BEFORE, custom method, not part of standard HTTP protocol.
     */
    public static final String BEFORE = "BEFORE";
    /**
     * HTTP Method: AFTER, custom method, not part of standard HTTP protocol.
     */
    public static final String AFTER = "AFTER";
    /**
     * HTTP Method: MOVE, WebDAV method for moving resources. RFC 4918, Section 9.8
     */
    public static final String MOVE = "MOVE";
    /**
     * HTTP Method: PROPPATCH, WebDAV method for setting properties. RFC 4918, Section 9.2
     */
    public static final String PROPPATCH = "PROPPATCH";
    /**
     * HTTP Method: REPORT, WebDAV method for generating custom reports. RFC 3253, Section 3
     */
    public static final String REPORT = "REPORT";
    /**
     * HTTP Method: PROPFIND, WebDAV method for retrieving resource properties. RFC 4918, Section 9.1
     */
    public static final String PROPFIND = "PROPFIND";

    /**
     * HTTP Status Code 100: Continue. Server has received request headers, client should continue sending request body.
     * RFC 7231, Section 6.2.1
     */
    public static final int HTTP_CONTINUE = 100;
    /**
     * HTTP Status Code 101: Switching Protocols. Server agrees to switch protocols (e.g., HTTP/1.1 to WebSocket). RFC
     * 7231, Section 6.2.2
     */
    public static final int HTTP_SWITCHING_PROTOCOL = 101;

    /**
     * HTTP Status Code 200: OK. Request has succeeded. RFC 7231, Section 6.3.1
     */
    public static final int HTTP_OK = 200;
    /**
     * HTTP Status Code 201: Created. Request succeeded and created a new resource. RFC 7231, Section 6.3.2
     */
    public static final int HTTP_CREATED = 201;
    /**
     * HTTP Status Code 202: Accepted. Request accepted for processing, but not completed. RFC 7231, Section 6.3.3
     */
    public static final int HTTP_ACCEPTED = 202;
    /**
     * HTTP Status Code 203: Non-Authoritative Information. Transforming proxy has modified the response. RFC 7231,
     * Section 6.3.4
     */
    public static final int HTTP_NOT_AUTHORITATIVE = 203;
    /**
     * HTTP Status Code 204: No Content. Request succeeded but no content returned. RFC 7231, Section 6.3.5
     */
    public static final int HTTP_NO_CONTENT = 204;
    /**
     * HTTP Status Code 205: Reset Content. Request succeeded, client should reset document view. RFC 7231, Section
     * 6.3.6
     */
    public static final int HTTP_RESET = 205;
    /**
     * HTTP Status Code 206: Partial Content. Server successfully handled partial GET request. RFC 7233, Section 4.1
     */
    public static final int HTTP_PARTIAL = 206;

    /**
     * HTTP Status Code 300: Multiple Choices. Resource can be accessed via multiple URIs. RFC 7231, Section 6.4.1
     */
    public static final int HTTP_MULT_CHOICE = 300;
    /**
     * HTTP Status Code 301: Moved Permanently. Resource permanently moved to new location. RFC 7231, Section 6.4.2
     */
    public static final int HTTP_MOVED_PERM = 301;
    /**
     * HTTP Status Code 302: Found (Temporary Redirect). Resource temporarily moved to new location. RFC 7231, Section
     * 6.4.3
     */
    public static final int HTTP_MOVED_TEMP = 302;
    /**
     * HTTP Status Code 303: See Other. Client should use GET method to access new URI. RFC 7231, Section 6.4.4
     */
    public static final int HTTP_SEE_OTHER = 303;
    /**
     * HTTP Status Code 304: Not Modified. Resource not changed, use cached version. RFC 7232, Section 4.1
     */
    public static final int HTTP_NOT_MODIFIED = 304;
    /**
     * HTTP Status Code 305: Use Proxy. Request must access through specified proxy. RFC 7231, Section 6.4.5
     */
    public static final int HTTP_USE_PROXY = 305;
    /**
     * HTTP Status Code 307: Temporary Redirect. Maintain original request method. RFC 7231, Section 6.4.7
     */
    public static final int HTTP_TEMP_REDIRECT = 307;
    /**
     * HTTP Status Code 308: Permanent Redirect. Maintain original request method. RFC 7538
     */
    public static final int HTTP_PERM_REDIRECT = 308;

    /**
     * HTTP Status Code 400: Bad Request. Server cannot understand the request. RFC 7231, Section 6.5.1
     */
    public static final int HTTP_BAD_REQUEST = 400;
    /**
     * HTTP Status Code 401: Unauthorized. Valid authentication required. RFC 7235, Section 3.1
     */
    public static final int HTTP_UNAUTHORIZED = 401;
    /**
     * HTTP Status Code 402: Payment Required. Reserved for future use. RFC 7231, Section 6.5.2
     */
    public static final int HTTP_PAYMENT_REQUIRED = 402;
    /**
     * HTTP Status Code 403: Forbidden. Server understands request but refuses to authorize. RFC 7231, Section 6.5.3
     */
    public static final int HTTP_FORBIDDEN = 403;
    /**
     * HTTP Status Code 404: Not Found. Server cannot find requested resource. RFC 7231, Section 6.5.4
     */
    public static final int HTTP_NOT_FOUND = 404;
    /**
     * HTTP Status Code 405: Method Not Allowed. Request method not supported for target resource. RFC 7231, Section
     * 6.5.5
     */
    public static final int HTTP_BAD_METHOD = 405;
    /**
     * HTTP Status Code 406: Not Acceptable. Server cannot generate content matching Accept headers. RFC 7231, Section
     * 6.5.6
     */
    public static final int HTTP_NOT_ACCEPTABLE = 406;
    /**
     * HTTP Status Code 407: Proxy Authentication Required. Proxy server authentication needed. RFC 7235, Section 3.2
     */
    public static final int HTTP_PROXY_AUTH = 407;
    /**
     * HTTP Status Code 408: Request Timeout. Client request timed out. RFC 7230, Section 6.5.7
     */
    public static final int HTTP_CLIENT_TIMEOUT = 408;
    /**
     * HTTP Status Code 409: Conflict. Request conflicts with current state. RFC 7231, Section 6.5.8
     */
    public static final int HTTP_CONFLICT = 409;
    /**
     * HTTP Status Code 410: Gone. Resource permanently deleted with no forwarding address. RFC 7231, Section 6.5.9
     */
    public static final int HTTP_GONE = 410;
    /**
     * HTTP Status Code 411: Length Required. Request lacks valid Content-Length header. RFC 7231, Section 6.5.10
     */
    public static final int HTTP_LENGTH_REQUIRED = 411;
    /**
     * HTTP Status Code 412: Precondition Failed. Server does not meet request preconditions. RFC 7232, Section 4.2
     */
    public static final int HTTP_PRECON_FAILED = 412;
    /**
     * HTTP Status Code 413: Payload Too Large. Request entity exceeds server limit. RFC 7231, Section 6.5.11
     */
    public static final int HTTP_ENTITY_TOO_LARGE = 413;
    /**
     * HTTP Status Code 414: URI Too Long. Request URI exceeds server limit. RFC 7231, Section 6.5.12
     */
    public static final int HTTP_REQ_TOO_LONG = 414;
    /**
     * HTTP Status Code 415: Unsupported Media Type. Request payload format not supported. RFC 7231, Section 6.5.13
     */
    public static final int HTTP_UNSUPPORTED_TYPE = 415;

    /**
     * HTTP Status Code 500: Internal Server Error. Server encountered unexpected condition. RFC 7231, Section 6.6.1
     */
    public static final int HTTP_INTERNAL_ERROR = 500;
    /**
     * HTTP Status Code 501: Not Implemented. Server does not support requested functionality. RFC 7231, Section 6.6.2
     */
    public static final int HTTP_NOT_IMPLEMENTED = 501;
    /**
     * HTTP Status Code 502: Bad Gateway. Gateway or proxy received invalid response. RFC 7231, Section 6.6.3
     */
    public static final int HTTP_BAD_GATEWAY = 502;
    /**
     * HTTP Status Code 503: Service Unavailable. Server temporarily overloaded or under maintenance. RFC 7231, Section
     * 6.6.4
     */
    public static final int HTTP_UNAVAILABLE = 503;
    /**
     * HTTP Status Code 504: Gateway Timeout. Gateway or proxy request timed out. RFC 7231, Section 6.6.5
     */
    public static final int HTTP_GATEWAY_TIMEOUT = 504;
    /**
     * HTTP Status Code 505: HTTP Version Not Supported. Server does not support requested HTTP version. RFC 7230,
     * Section 6.6.6
     */
    public static final int HTTP_VERSION = 505;

    /**
     * HTTP/2 SETTINGS frame: Header compression table size in bytes. Default: 4096 bytes. RFC 7540, Section 6.5.2
     */
    public static final int HEADER_TABLE_SIZE = 1;
    /**
     * HTTP/2 SETTINGS frame: Enable PUSH_PROMISE frames. 0 = disable push, 1 = enable push. RFC 7540, Section 6.5.2
     */
    public static final int ENABLE_PUSH = 2;
    /**
     * HTTP/2 SETTINGS frame: Maximum number of concurrent streams. Default: unlimited (2^31-1). RFC 7540, Section 6.5.2
     */
    public static final int MAX_CONCURRENT_STREAMS = 4;
    /**
     * HTTP/2 SETTINGS frame: Largest frame payload sender accepts (16KB to 16MB). Default: 16KB (16384). RFC 7540,
     * Section 6.5.2
     */
    public static final int MAX_FRAME_SIZE = 5;
    /**
     * HTTP/2 SETTINGS frame: Maximum header list size sender accepts. Default: unlimited (2^31-1). RFC 7540, Section
     * 6.5.2
     */
    public static final int MAX_HEADER_LIST_SIZE = 6;
    /**
     * HTTP/2 SETTINGS frame: Initial window size for stream flow control. Default: 65535 bytes. RFC 7540, Section 6.5.2
     */
    public static final int INITIAL_WINDOW_SIZE = 7;
    /**
     * HTTP/2 default initial window size for all streams: 64 KiB (65535 bytes). RFC 7540, Section 6.9.2
     */
    public static final int DEFAULT_INITIAL_WINDOW_SIZE = 65535;

    /**
     * HTTP Accept header field name. RFC 7231, Section 5.3.2
     */
    public static final String ACCEPT = "Accept";
    /**
     * HTTP Accept-Charset header field name. RFC 7231, Section 5.3.3
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
     * HTTP Set-Cookie2 header field name (obsolete). RFC 2965 (superseded by RFC 6265)
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
     * Trailers header. RFC 7230, Section 4.1.2
     */
    public static final String TRAILERS = "Trailers";
    /**
     * Content-MD5 header for message integrity check (obsolete). RFC 1864 (deprecated by RFC 7231, Section 6.3)
     */
    public static final String CONTENT_MD5 = "Content-MD5";
    /**
     * Httpd-Preemptive header (Apache specific, non-standard).
     */
    public static final String HTTPD_PREEMPTIVE = "Httpd-Preemptive";

    /**
     * WebSocket Sec-WebSocket-Key header. RFC 6455, Section 4.1
     */
    public static final String SEC_WEBSOCKET_KEY = "Sec-WebSocket-Key";
    /**
     * WebSocket Sec-WebSocket-Accept header. RFC 6455, Section 4.2.2
     */
    public static final String SEC_WEBSOCKET_ACCEPT = "Sec-WebSocket-Accept";
    /**
     * WebSocket Sec-WebSocket-Version header. RFC 6455, Section 4.1
     */
    public static final String SEC_WEBSOCKET_VERSION = "Sec-WebSocket-Version";
    /**
     * HTTP Strict-Transport-Security header for HSTS. RFC 6797
     */
    public static final String STRICT_TRANSPORT_SECURITY = "Strict-Transport-Security";
    /**
     * SOAPAction header for SOAP protocol. SOAP 1.1/1.2 specification
     */
    public static final String SOAPACTION = "SOAPAction";

    /**
     * HTTP/2 pseudo-header: :method. RFC 7540, Section 8.1.2.3
     */
    public static final String TARGET_METHOD_UTF8 = ":method";
    /**
     * HTTP/2 pseudo-header: :status. RFC 7540, Section 8.1.2.4
     */
    public static final String RESPONSE_STATUS_UTF8 = ":status";
    /**
     * HTTP/2 pseudo-header: :path. RFC 7540, Section 8.1.2.3
     */
    public static final String TARGET_PATH_UTF8 = ":path";
    /**
     * HTTP/2 pseudo-header: :scheme. RFC 7540, Section 8.1.2.3
     */
    public static final String TARGET_SCHEME_UTF8 = ":scheme";
    /**
     * HTTP/2 pseudo-header: :authority. RFC 7540, Section 8.1.2.3
     */
    public static final String TARGET_AUTHORITY_UTF8 = ":authority";

    /**
     * Form data content type identifier.
     */
    public static final String FORM = "form";
    /**
     * JSON data content type identifier.
     */
    public static final String JSON = "json";
    /**
     * XML data content type identifier.
     */
    public static final String XML = "xml";
    /**
     * Protocol Buffers data content type identifier.
     */
    public static final String PROTOBUF = "protobuf";

    /**
     * Returns true if the HTTP method invalidates cache entries. Methods that modify resources should invalidate
     * relevant cache entries. RFC 7234, Section 4.4
     *
     * @param method the HTTP method to check
     * @return true if the method invalidates cache
     */
    public static boolean invalidatesCache(String method) {
        return POST.equals(method) || PUT.equals(method) || PATCH.equals(method) || DELETE.equals(method)
                || MOVE.equals(method);
    }

    /**
     * Returns true if the HTTP method requires a request body. Certain methods semantically require a request body to
     * be present. RFC 7231, Section 4.3
     *
     * @param method the HTTP method to check
     * @return true if the method requires a request body
     */
    public static boolean requiresRequestBody(String method) {
        return POST.equals(method) || PUT.equals(method) || PATCH.equals(method) || PROPPATCH.equals(method)
                || REPORT.equals(method);
    }

    /**
     * Returns true if the HTTP method permits a request body. GET and HEAD methods do not permit request bodies. RFC
     * 7231, Section 4.3.1, 4.3.2
     *
     * @param method the HTTP method to check
     * @return true if the method permits a request body
     */
    public static boolean permitsRequestBody(String method) {
        return !GET.equals(method) && !HEAD.equals(method);
    }

    /**
     * Returns true if redirects for this method should maintain the request body. Most methods change to GET on
     * redirect, but PROPFIND maintains its body. RFC 7231, Section 6.4
     *
     * @param method the HTTP method to check
     * @return true if redirect should maintain request body
     */
    public static boolean redirectsWithBody(String method) {
        return PROPFIND.equals(method);
    }

    /**
     * Returns true if this method should be redirected to a GET request. Most methods convert to GET on redirect except
     * PROPFIND. RFC 7231, Section 6.4
     *
     * @param method the HTTP method to check
     * @return true if method should redirect to GET
     */
    public static boolean redirectsToGet(String method) {
        return !PROPFIND.equals(method);
    }

}
