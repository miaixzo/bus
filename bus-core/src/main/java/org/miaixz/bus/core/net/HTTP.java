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
package org.miaixz.bus.core.net;

/**
 * HTTP 相关常量
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class HTTP {

    /**
     * HTTP Method ALL
     */
    public static final String ALL = "ALL";
    /**
     * HTTP Method NONE
     */
    public static final String NONE = "NONE";
    /**
     * HTTP Method GET
     */
    public static final String GET = "GET";
    /**
     * HTTP Method POST
     */
    public static final String POST = "POST";
    /**
     * HTTP Method PUT
     */
    public static final String PUT = "PUT";
    /**
     * HTTP Method PATCH
     */
    public static final String PATCH = "PATCH";
    /**
     * HTTP Method DELETE
     */
    public static final String DELETE = "DELETE";
    /**
     * HTTP Method HEAD
     */
    public static final String HEAD = "HEAD";
    /**
     * HTTP Method TRACE
     */
    public static final String TRACE = "TRACE";
    /**
     * HTTP Method CONNECT
     */
    public static final String CONNECT = "CONNECT";
    /**
     * HTTP Method OPTIONS
     */
    public static final String OPTIONS = "OPTIONS";
    /**
     * HTTP Method BEFORE
     */
    public static final String BEFORE = "BEFORE";
    /**
     * HTTP Method AFTER
     */
    public static final String AFTER = "AFTER";
    /**
     * HTTP Method MOVE
     */
    public static final String MOVE = "MOVE";
    /**
     * HTTP Method PROPPATCH
     */
    public static final String PROPPATCH = "PROPPATCH";
    /**
     * HTTP Method REPORT
     */
    public static final String REPORT = "REPORT";
    /**
     * HTTP Method PROPFIND
     */
    public static final String PROPFIND = "PROPFIND";


    /**
     * HTTP Status-Code 100: Continue.
     */
    public static final int HTTP_CONTINUE = 100;
    /**
     * HTTP Status-Code 101: Switching Protocols.
     */
    public static final int HTTP_SWITCHING_PROTOCOL = 101;
    /**
     * HTTP Status-Code 200: OK.
     */
    public static final int HTTP_OK = 200;
    /**
     * HTTP Status-Code 201: Created.
     */
    public static final int HTTP_CREATED = 201;
    /**
     * HTTP Status-Code 202: Accepted.
     */
    public static final int HTTP_ACCEPTED = 202;
    /**
     * HTTP Status-Code 203: Non-Authoritative Information.
     */
    public static final int HTTP_NOT_AUTHORITATIVE = 203;
    /**
     * HTTP Status-Code 204: No Content.
     */
    public static final int HTTP_NO_CONTENT = 204;
    /**
     * HTTP Status-Code 205: Reset Content.
     */
    public static final int HTTP_RESET = 205;
    /**
     * HTTP Status-Code 206: Partial Content.
     */
    public static final int HTTP_PARTIAL = 206;
    /**
     * HTTP Status-Code 300: Multiple Choices.
     */
    public static final int HTTP_MULT_CHOICE = 300;
    /**
     * HTTP Status-Code 301: Moved Permanently.
     */
    public static final int HTTP_MOVED_PERM = 301;
    /**
     * HTTP Status-Code 302: Temporary Redirect.
     */
    public static final int HTTP_MOVED_TEMP = 302;
    /**
     * HTTP Status-Code 303: See Other.
     */
    public static final int HTTP_SEE_OTHER = 303;
    /**
     * HTTP Status-Code 304: Not Modified.
     */
    public static final int HTTP_NOT_MODIFIED = 304;
    /**
     * HTTP Status-Code 305: Use Proxy.
     */
    public static final int HTTP_USE_PROXY = 305;
    /**
     * HTTP Status-Code 307: Temporary Redirect.
     */
    public static final int HTTP_TEMP_REDIRECT = 307;
    /**
     * HTTP Status-Code 308: Use perm Redirect.
     */
    public static final int HTTP_PERM_REDIRECT = 308;
    /**
     * HTTP Status-Code 400: Bad Request.
     */
    public static final int HTTP_BAD_REQUEST = 400;
    /**
     * HTTP Status-Code 401: Unauthorized.
     */
    public static final int HTTP_UNAUTHORIZED = 401;
    /**
     * HTTP Status-Code 402: Payment Required.
     */
    public static final int HTTP_PAYMENT_REQUIRED = 402;
    /**
     * HTTP Status-Code 403: Forbidden.
     */
    public static final int HTTP_FORBIDDEN = 403;
    /**
     * HTTP Status-Code 404: Not Found.
     */
    public static final int HTTP_NOT_FOUND = 404;
    /**
     * HTTP Status-Code 405: Method Not Allowed.
     */
    public static final int HTTP_BAD_METHOD = 405;
    /**
     * HTTP Status-Code 406: Not Acceptable.
     */
    public static final int HTTP_NOT_ACCEPTABLE = 406;
    /**
     * HTTP Status-Code 407: Proxy Authentication Required.
     */
    public static final int HTTP_PROXY_AUTH = 407;
    /**
     * HTTP Status-Code 408: Request Time-Out.
     */
    public static final int HTTP_CLIENT_TIMEOUT = 408;
    /**
     * HTTP Status-Code 409: Conflict.
     */
    public static final int HTTP_CONFLICT = 409;
    /**
     * HTTP Status-Code 410: Gone.
     */
    public static final int HTTP_GONE = 410;
    /**
     * HTTP Status-Code 411: Length Required.
     */
    public static final int HTTP_LENGTH_REQUIRED = 411;
    /**
     * HTTP Status-Code 412: Precondition Failed.
     */
    public static final int HTTP_PRECON_FAILED = 412;
    /**
     * HTTP Status-Code 413: Request Entity Too Large.
     */
    public static final int HTTP_ENTITY_TOO_LARGE = 413;
    /**
     * HTTP Status-Code 414: Request-URI Too Large.
     */
    public static final int HTTP_REQ_TOO_LONG = 414;
    /**
     * HTTP Status-Code 415: Unsupported Media Type.
     */
    public static final int HTTP_UNSUPPORTED_TYPE = 415;
    /**
     * HTTP Status-Code 500: Internal Server Error.
     */
    public static final int HTTP_INTERNAL_ERROR = 500;
    /**
     * HTTP Status-Code 501: Not Implemented.
     */
    public static final int HTTP_NOT_IMPLEMENTED = 501;
    /**
     * HTTP Status-Code 502: Bad Gateway.
     */
    public static final int HTTP_BAD_GATEWAY = 502;
    /**
     * HTTP Status-Code 503: Service Unavailable.
     */
    public static final int HTTP_UNAVAILABLE = 503;
    /**
     * HTTP Status-Code 504: Gateway Timeout.
     */
    public static final int HTTP_GATEWAY_TIMEOUT = 504;
    /**
     * HTTP Status-Code 505: HTTP Version Not Supported.
     */
    public static final int HTTP_VERSION = 505;
    /**
     * From the HTTP/2 specs, the default initial window size for all streams is 64 KiB. (Chrome 25
     * uses 10 MiB).
     */
    public static final int DEFAULT_INITIAL_WINDOW_SIZE = 65535;
    /**
     * HTTP/2: Size in bytes of the table used to decode the sender's header blocks.
     */
    public static final int HEADER_TABLE_SIZE = 1;
    /**
     * HTTP/2: The peer must not send a PUSH_PROMISE frame when this is 0.
     */
    public static final int ENABLE_PUSH = 2;
    /**
     * Sender's maximum number of concurrent streams.
     */
    public static final int MAX_CONCURRENT_STREAMS = 4;
    /**
     * HTTP/2: Size in bytes of the largest frame payload the sender will accept.
     */
    public static final int MAX_FRAME_SIZE = 5;
    /**
     * HTTP/2: Advisory only. Size in bytes of the largest header list the sender will accept.
     */
    public static final int MAX_HEADER_LIST_SIZE = 6;
    /**
     * Window size in bytes.
     */
    public static final int INITIAL_WINDOW_SIZE = 7;


    /**
     * The header Host
     */
    public static final String HOST = "Host";
    /**
     * The header Server
     */
    public static final String SERVER = "Server";
    /**
     * The header Age
     */
    public static final String AGE = "Age";
    /**
     * The header Allow
     */
    public static final String ALLOW = "Allow";
    /**
     * The header Expires
     */
    public static final String EXPIRES = "Expires";
    /**
     * The header Cookie
     */
    public static final String COOKIE = "Cookie";
    /**
     * The header Set-Cookie
     */
    public static final String SET_COOKIE = "Set-Cookie";
    /**
     * The header Encoding
     */
    public static final String ENCODING = "Encoding";
    /**
     * The header Upgrade
     */
    public static final String UPGRADE = "Upgrade";
    /**
     * The header Trailers
     */
    public static final String TRAILERS = "Trailers";
    /**
     * The header Location
     */
    public static final String LOCATION = "Location";
    /**
     * The header Connection
     */
    public static final String CONNECTION = "Connection";
    /**
     * The header Date
     */
    public static final String DATE = "Date";
    /**
     * The header Etag
     */
    public static final String ETAG = "Etag";
    /**
     * The header Expect
     */
    public static final String EXPECT = "Expect";
    /**
     * The header From
     */
    public static final String FROM = "From";
    /**
     * The header Link
     */
    public static final String LINK = "Link";
    /**
     * The header Vary
     */
    public static final String VARY = "Vary";
    /**
     * The header Via
     */
    public static final String VIA = "Via";
    /**
     * The header Range
     */
    public static final String RANGE = "Range";
    /**
     * The header Referer
     */
    public static final String REFERER = "Referer";
    /**
     * The header Refresh
     */
    public static final String REFRESH = "Refresh";
    /**
     * The header te
     */
    public static final String TE = "te";
    /**
     * The header If-Match
     */
    public static final String IF_MATCH = "If-Match";
    /**
     * The header If-Range
     */
    public static final String IF_RANGE = "If-Range";
    /**
     * The header Accept
     */
    public static final String ACCEPT = "Accept";
    /**
     * The header Accept-Charset
     */
    public static final String ACCEPT_CHARSET = "Accept-Charset";
    /**
     * The header Accept-Encoding
     */
    public static final String ACCEPT_ENCODING = "Accept-Encoding";
    /**
     * The header Accept-Language
     */
    public static final String ACCEPT_LANGUAGE = "Accept-Language";
    /**
     * The header Accept-Ranges
     */
    public static final String ACCEPT_RANGES = "Accept-Ranges";
    /**
     * The header Content-Encoding
     */
    public static final String CONTENT_ENCODING = "Content-Encoding";
    /**
     * The header Content-Language
     */
    public static final String CONTENT_LANGUAGE = "Content-Language";
    /**
     * The header Content-Length
     */
    public static final String CONTENT_LENGTH = "Content-Length";
    /**
     * The header Content-Location
     */
    public static final String CONTENT_LOCATION = "Content-Location";
    /**
     * The header Content-MD5
     */
    public static final String CONTENT_MD5 = "Content-MD5";
    /**
     * The header Content-Range
     */
    public static final String CONTENT_RANGE = "Content-Range";
    /**
     * The header Content-Type
     */
    public static final String CONTENT_TYPE = "Content-Type";
    /**
     * The header Content-Disposition
     */
    public static final String CONTENT_DISPOSITION = "Content-Disposition";
    /**
     * The header Transfer-Encoding
     */
    public static final String TRANSFER_ENCODING = "Transfer-Encoding";
    /**
     * The header Cache-Control
     */
    public static final String CACHE_CONTROL = "Cache-Control";
    /**
     * The header User-Agent
     */
    public static final String USER_AGENT = "User-Agent";
    /**
     * The header Retry-After
     */
    public static final String RETRY_AFTER = "Retry-After";
    /**
     * The header Max-Forwards
     */
    public static final String MAX_FORWARDS = "Max-Forwards";
    /**
     * The header Keep-Alive
     */
    public static final String KEEP_ALIVE = "Keep-Alive";
    /**
     * The header Authorization
     */
    public static final String AUTHORIZATION = "Authorization";
    /**
     * The header Proxy-Authorization
     */
    public static final String PROXY_AUTHORIZATION = "Proxy-Authorization";
    /**
     * The header Proxy-Connection
     */
    public static final String PROXY_CONNECTION = "Proxy-Connection";
    /**
     * The header WWW-Authenticate
     */
    public static final String WWW_AUTHENTICATE = "WWW-Authenticate";
    /**
     * The header Proxy-Authenticate
     */
    public static final String PROXY_AUTHENTICATE = "Proxy-Authenticate";
    /**
     * The header Httpd-Preemptive
     */
    public static final String HTTPD_PREEMPTIVE = "Httpd-Preemptive";
    /**
     * The header Last-Modified
     */
    public static final String LAST_MODIFIED = "Last-Modified";
    /**
     * The header If-Unmodified-Since
     */
    public static final String IF_UNMODIFIED_SINCE = "If-Unmodified-Since";
    /**
     * The header If-Modified-Since
     */
    public static final String IF_MODIFIED_SINCE = "If-Modified-Since";
    /**
     * The header If-None-Match
     */
    public static final String IF_NONE_MATCH = "If-None-Match";
    /**
     * The header Sec-WebSocket-Key
     */
    public static final String SEC_WEBSOCKET_KEY = "Sec-WebSocket-Key";
    /**
     * The header Sec-WebSocket-Accept
     */
    public static final String SEC_WEBSOCKET_ACCEPT = "Sec-WebSocket-Accept";
    /**
     * The header Sec-WebSocket-Version
     */
    public static final String SEC_WEBSOCKET_VERSION = "Sec-WebSocket-Version";
    /**
     * The header Sec-WebSocket-Version
     */
    public static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
    /**
     * The header Sec-WebSocket-Version
     */
    public static final String STRICT_TRANSPORT_SECURITY = "Strict-Transport-Security";
    /**
     * The header SOAPAction
     */
    public static final String SOAPACTION = "SOAPAction";


    /**
     * The use method
     */
    public static final String TARGET_METHOD_UTF8 = ":method";
    /**
     * The use status
     */
    public static final String RESPONSE_STATUS_UTF8 = ":status";
    /**
     * The use path
     */
    public static final String TARGET_PATH_UTF8 = ":path";
    /**
     * The use scheme
     */
    public static final String TARGET_SCHEME_UTF8 = ":scheme";
    /**
     * The use authority
     */
    public static final String TARGET_AUTHORITY_UTF8 = ":authority";

    /**
     * The use form data
     */
    public static final String FORM = "form";
    /**
     * The use json data
     */
    public static final String JSON = "json";
    /**
     * The use xml data
     */
    public static final String XML = "xml";
    /**
     * The use protobuf data
     */
    public static final String PROTOBUF = "protobuf";

    /**
     * WebDAV
     *
     * @param method 请求方式
     * @return the boolean
     */
    public static boolean invalidatesCache(String method) {
        return POST.equals(method)
                || PUT.equals(method)
                || PATCH.equals(method)
                || DELETE.equals(method)
                || MOVE.equals(method);
    }

    /**
     * WebDAV
     * CalDAV/CardDAV(在WebDAV版本中定义)
     *
     * @param method 请求方式
     * @return the boolean
     */
    public static boolean requiresRequestBody(String method) {
        return POST.equals(method)
                || PUT.equals(method)
                || PATCH.equals(method)
                || PROPPATCH.equals(method)
                || REPORT.equals(method);
    }

    /**
     * 许可维护请求体
     *
     * @param method 请求方式
     * @return the boolean
     */
    public static boolean permitsRequestBody(String method) {
        return !GET.equals(method) || HEAD.equals(method);
    }

    /**
     * (WebDAV)重定向也应该维护请求体
     *
     * @param method 请求方式
     * @return the boolean
     */
    public static boolean redirectsWithBody(String method) {
        return PROPFIND.equals(method);
    }

    /**
     * 除了PROPFIND之外的所有请求都应该重定向到GET请求
     *
     * @param method 请求方式
     * @return the boolean
     */
    public static boolean redirectsToGet(String method) {
        return !PROPFIND.equals(method);
    }

}