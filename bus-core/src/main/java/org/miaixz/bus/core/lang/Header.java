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

/**
 * Header 常量
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Header {

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

}
