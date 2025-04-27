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

import java.io.IOException;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * 协议vs计划 它的名字是:{@link java.net.URL#getProtocol()} 返回{@linkplain java.net.URI#getScheme() scheme} (http, https, etc.)，
 * 而不是协议(http/1.1, spdy/3.1，等等) 请使用这个协议来识别它是如何被分割的 Httpd使用协议这个词来标识HTTP消息是如何构造的
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum Protocol {

    /**
     * 表示使用共享内存的命名管道连接类型. 这种类型的进程间连接比套接字连接稍微快一些， 并且只有在两个进程位于同一台机器上时才能工作. 默认情况下,不能在Java上工作，不支持命名管道.
     */
    PIPE("pipe"),
    /**
     * 表示使用可靠的TCP/IP套接字连接的连接类型.
     */
    SOCKET("socket"),
    /**
     * TCP协议
     */
    TCP("tcp"),
    /**
     *
     */
    UDP("udp"),
    /**
     * http协议
     */
    HTTP("http"),
    /**
     * 过时的plaintext，默认情况下不使用持久套接字
     */
    HTTP_1_0("HTTP/1.0"),
    /**
     * 包含持久连接的plaintext 此版本的Httpd实现了RFC 7230，并跟踪对该规范的修订
     */
    HTTP_1_1("HTTP/1.1"),
    /**
     * IETF的二进制框架协议，包括头压缩、在同一个套接字上多路复用多个请求和服务器推送 HTTP/1.1语义是在HTTP/2上分层的
     */
    HTTP_2("h2"),
    /**
     * HTTP/3 是用于交换信息的超文本传输协议的第三个主要版本 HTTP/3 在 QUIC 上运行，QUIC 以 RFC 9000 的形式发布。
     */
    HTTP_3("h3"),
    /**
     * Chromium的二进制框架协议，包括标头压缩、在同一个套接字上多路复用多个请求和服务器推送 HTTP/1.1语义在SPDY/3上分层.
     */
    SPDY_3("spdy/3.1"),
    /**
     * 明文HTTP/2，没有"upgrade"往返。此选项要求客户端事先知道服务器支持明文HTTP/2
     */
    H2_PRIOR_KNOWLEDGE("h2_prior_knowledge"),
    /**
     * QUIC(快速UDP互联网连接)是一个新的多路复用和UDP之上的安全传输， 从底层设计和优化的HTTP/2语义。HTTP/1.1语义是在HTTP/2上分层的
     */
    QUIC("quic"),
    /**
     * SOAP 1.1协议
     */
    SOAP_1_1("soap 1.1 protocol"),
    /**
     * SOAP 1.2协议
     */
    SOAP_1_2("SOAP 1.2 Protocol"),
    /**
     * the ws
     */
    WS("ws"),
    /**
     * the wss
     */
    WSS("wss"),
    /**
     * https协议
     */
    HTTPS("https"),
    /**
     * Supports some version of SSL
     */
    SSL("ssl"),
    /**
     * Supports SSL version 2
     */
    SSLv2("SSLv2"),
    /**
     * Supports SSL version 3
     */
    SSLv3("SSLv3"),
    /**
     * Supports some version of TLS
     */
    TLS("tls"),
    /**
     * Supports RFC 2246: TLS version 1.0
     */
    TLSv1("TLSv1"),
    /**
     * Supports RFC 4346: TLS version 1.1
     */
    TLSv1_1("TLSv1.1"),
    /**
     * Supports RFC 5246: TLS version 1.2
     */
    TLSv1_2("TLSv1.2"),
    /**
     * Supports RFC 5246: TLS version 1.3
     */
    TLSv1_3("TLSv1.3"),
    /**
     *
     */
    DICOM("dicom"),
    /**
     *
     */
    HL7("hl7");

    /**
     * The prefix http
     */
    public static final String HTTP_PREFIX = HTTP.name + Symbol.COLON + Symbol.FORWARDSLASH;
    /**
     * The prefix https
     */
    public static final String HTTPS_PREFIX = HTTPS.name + Symbol.COLON + Symbol.FORWARDSLASH;
    /**
     * The prefix ws
     */
    public static final String WS_PREFIX = WS.name + Symbol.COLON + Symbol.FORWARDSLASH;
    /**
     * The prefix wss
     */
    public static final String WSS_PREFIX = WSS.name + Symbol.COLON + Symbol.FORWARDSLASH;

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

    public final String name;

    Protocol(String name) {
        this.name = name;
    }

    /**
     * @param protocol 协议标示
     * @return 返回由{@code protocol}标识的协议
     * @throws IOException if {@code protocol} is unknown.
     */
    public static Protocol get(String protocol) throws IOException {
        if (protocol.equals(HTTP_1_0.name)) {
            return HTTP_1_0;
        }
        if (protocol.equals(HTTP_1_1.name)) {
            return HTTP_1_1;
        }
        if (protocol.equals(H2_PRIOR_KNOWLEDGE.name)) {
            return H2_PRIOR_KNOWLEDGE;
        }
        if (protocol.equals(HTTP_2.name)) {
            return HTTP_2;
        }
        if (protocol.equals(SPDY_3.name)) {
            return SPDY_3;
        }
        if (protocol.equals(QUIC.name)) {
            return QUIC;
        }
        if (protocol.equals(SOAP_1_1.name)) {
            return SOAP_1_1;
        }
        if (protocol.equals(SOAP_1_2.name)) {
            return SOAP_1_2;
        }
        throw new IOException("Unexpected protocol: " + protocol);
    }

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
        return url.startsWith(HTTP_PREFIX) || url.startsWith("http%3A%2F%2F");
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
        return url.startsWith(HTTPS_PREFIX) || url.startsWith("https%3A%2F%2F");
    }

    /**
     * 是否为本地主机（域名）
     *
     * @param url 待验证的url
     * @return true: 本地主机（域名）, false: 非本地主机（域名）
     */
    public static boolean isLocalHost(String url) {
        return StringKit.isEmpty(url) || url.contains(HOST_IPV4) || url.contains(HOST_LOCAL);
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

    public boolean isTcp() {
        return this != UDP;
    }

    /**
     * 返回用于识别ALPN协议的字符串，如“http/1.1”、“spdy/3.1”或“http/2.0”.
     */
    @Override
    public String toString() {
        return name;
    }

}
