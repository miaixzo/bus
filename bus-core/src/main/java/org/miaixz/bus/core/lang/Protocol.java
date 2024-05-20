/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.core.lang;

import org.miaixz.bus.core.toolkit.StringKit;

/**
 * HTTP 相关常量
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Protocol {

    /**
     * The http
     */
    public static final String HTTP = "http";
    /**
     * The https
     */
    public static final String HTTPS = "https";
    /**
     * The https
     */
    public static final String WS = "ws";
    /**
     * The https
     */
    public static final String WSS = "wss";
    /**
     * The prefix http
     */
    public static final String HTTP_PREFIX = HTTP + Symbol.COLON + Symbol.FORWARDSLASH;
    /**
     * The prefix https
     */
    public static final String HTTPS_PREFIX = HTTPS + Symbol.COLON + Symbol.FORWARDSLASH;
    /**
     * The prefix ws
     */
    public static final String WS_PREFIX = WS + Symbol.COLON + Symbol.FORWARDSLASH;
    /**
     * The prefix wss
     */
    public static final String WSS_PREFIX = WSS + Symbol.COLON + Symbol.FORWARDSLASH;
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
     * HTTP Method ALL
     */
    public static final String ALL = "ALL";
    /**
     * HTTP Method NONE
     */
    public static final String NONE = "NONE";
    /**
     * HTTP/1.0
     */
    public static final String HTTP_1_0 = "HTTP/1.0";
    /**
     * HTTP/1.1
     */
    public static final String HTTP_1_1 = "HTTP/1.1";
    /**
     * HTTP/2.0
     */
    public static final String HTTP_2_0 = "HTTP/2.0";
    /**
     * SPDY/3.1
     */
    public static final String SPDY_3_1 = "SPDY/3.1";
    /**
     * SOAP 1.1
     */
    public static final String SOAP_1_1 = "SOAP 1.1 Protocol";
    /**
     * SOAP 1.2
     */
    public static final String SOAP_1_2 = "SOAP 1.2 Protocol";
    /**
     * QUIC
     */
    public static final String QUIC = "QUIC";
    /**
     * 明文HTTP/2，没有"upgrade"往返。此选项要求客户端事先知道服务器支持明文HTTP/2
     */
    public static final String H2_PRIOR_KNOWLEDGE = "H2_PRIOR_KNOWLEDGE";
    /**
     * Supports some version of SSL; may support other versions
     */
    public static final String SSL = "SSL";
    /**
     * Supports some version of TLS; may support other versions
     */
    public static final String TLS = "TLS";
    /**
     * Supports RFC 2246: TLS version 1.0 ; may support other versions
     */
    public static final String TLS_V_10 = "TLSv1";
    /**
     * Supports RFC 4346: TLS version 1.1 ; may support other versions
     */
    public static final String TLS_V_11 = "TLSv1.1";
    /**
     * Supports RFC 5246: TLS version 1.2 ; may support other versions
     */
    public static final String TLS_V_12 = "TLSv1.2";
    /**
     * Supports RFC 5246: TLS version 1.3 ; may support other versions
     */
    public static final String TLS_V_13 = "TLSv1.3";
    /**
     * Supports SSL version 2 or later; may support other versions
     */
    public static final String SSL_V_20 = "SSLv2";
    /**
     * Supports SSL version 3; may support other versions
     */
    public static final String SSL_V_30 = "SSLv3";
    /**
     * The use status
     */
    public static final String RESPONSE_STATUS_UTF8 = ":status";
    /**
     * The use method
     */
    public static final String TARGET_METHOD_UTF8 = ":method";
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
     * The IPV4 127.0.0.1
     */
    public static final String HOST_IPV4 = "127.0.0.1";
    /**
     * The localhost
     */
    public static final String HOST_LOCAL = "localhost";
    /**
     * Ipv4地址最小值字符串形式
     */
    public static final String IPV4_STR_MIN = "0.0.0.0";
    /**
     * Ipv4地址最大值字符串形式
     */
    public static final String IPV4_STR_MAX = "255.255.255.255";
    /**
     * Ipv4最大值数值形式
     */
    public static final long IPV4_NUM_MAX = 0xffffffffL;
    /**
     * Ipv4未使用地址最大值字符串形式
     */
    public static final String IPV4_UNUSED_STR_MAX = "0.255.255.255";
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
     * The form data
     */
    public static final String FORM = "form";
    /**
     * The json data
     */
    public static final String JSON = "json";
    /**
     * The xml data
     */
    public static final String XML = "xml";
    /**
     * The protobuf data
     */
    public static final String PROTOBUF = "protobuf";

    /**
     * 是否为http协议
     *
     * @param url 待验证的url
     * @return true: http协议, false: 非http协议
     */
    public static boolean isHttp(String url) {
        if (StringKit.isEmpty(url)) {
            return false;
        }
        return url.startsWith(Protocol.HTTP_PREFIX) || url.startsWith("http%3A%2F%2F");
    }

    /**
     * 是否为https协议
     *
     * @param url 待验证的url
     * @return true: https协议, false: 非https协议
     */
    public static boolean isHttps(String url) {
        if (StringKit.isEmpty(url)) {
            return false;
        }
        return url.startsWith(Protocol.HTTPS_PREFIX) || url.startsWith("https%3A%2F%2F");
    }

    /**
     * 是否为本地主机（域名）
     *
     * @param url 待验证的url
     * @return true: 本地主机（域名）, false: 非本地主机（域名）
     */
    public static boolean isLocalHost(String url) {
        return StringKit.isEmpty(url) || url.contains(Protocol.HOST_IPV4) || url.contains(Protocol.HOST_LOCAL);
    }


    /**
     * 是否为https协议或本地主机（域名）
     *
     * @param url 待验证的url
     * @return true: https协议或本地主机 false: 非https协议或本机主机
     */
    public static boolean isHttpsOrLocalHost(String url) {
        if (StringKit.isEmpty(url)) {
            return false;
        }
        return isHttps(url) || isLocalHost(url);
    }

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
