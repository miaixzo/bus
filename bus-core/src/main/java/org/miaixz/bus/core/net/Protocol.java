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
 * 网络协议枚举类，用于标识和处理多种网络协议。协议名称对应 {@link java.net.URL#getProtocol()} 或 {@link java.net.URI#getScheme()} 返回的 scheme（如
 * http、https 等），而非具体协议版本（如 http/1.1、spdy/3.1 等）。 本类用于区分 HTTP 消息构造方式及相关协议特性，支持多种通信协议的识别与验证。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum Protocol {

    /**
     * 命名管道协议，使用共享内存进行进程间通信，速度略快于套接字，仅适用于同一机器上的进程。 默认情况下，Java 不支持命名管道。
     */
    PIPE("pipe"),

    /**
     * 可靠的 TCP/IP 套接字连接协议，提供面向连接的通信。
     */
    SOCKET("socket"),

    /**
     * 传输控制协议（TCP），提供可靠的、面向连接的通信，广泛用于网络应用。
     */
    TCP("tcp"),

    /**
     * 用户数据报协议（UDP），提供无连接、不可靠的通信，适用于低延迟场景。
     */
    UDP("udp"),

    /**
     * 超文本传输协议（HTTP），用于网页数据传输，基于明文传输。
     */
    HTTP("http"),

    /**
     * HTTP/1.0 协议，早期明文协议，不支持持久连接，遵循 RFC 1945。
     */
    HTTP_1_0("HTTP/1.0"),

    /**
     * HTTP/1.1 协议，明文协议，支持持久连接和流水线处理，遵循 RFC 7230。
     */
    HTTP_1_1("HTTP/1.1"),

    /**
     * HTTP/2 协议，二进制框架协议，支持头部压缩、请求多路复用和服务器推送，基于 HTTP/1.1 语义，遵循 RFC 7540。
     */
    HTTP_2("h2"),

    /**
     * HTTP/3 协议，基于 QUIC 传输，支持高效多路复用和低延迟，遵循 RFC 9000。
     */
    HTTP_3("h3"),

    /**
     * SPDY/3.1 协议，Chromium 的二进制框架协议，支持头部压缩和请求多路复用，基于 HTTP/1.1 语义（已废弃）。
     */
    SPDY_3("spdy/3.1"),

    /**
     * HTTP/2 明文协议，无需升级握手，要求客户端预知服务器支持明文 HTTP/2，遵循 RFC 7540。
     */
    H2_PRIOR_KNOWLEDGE("h2_prior_knowledge"),

    /**
     * QUIC 协议（快速 UDP 互联网连接），基于 UDP 的安全多路复用传输，优化 HTTP/2 语义，遵循 RFC 9000。
     */
    QUIC("quic"),

    /**
     * SOAP 1.1 协议，基于 XML 的简单对象访问协议，用于分布式系统通信，遵循 W3C 标准。
     */
    SOAP_1_1("soap 1.1 protocol"),

    /**
     * SOAP 1.2 协议，SOAP 的升级版本，改进性能和兼容性，遵循 W3C 标准。
     */
    SOAP_1_2("SOAP 1.2 Protocol"),

    /**
     * WebSocket 协议（明文），用于双向实时通信，基于 TCP，遵循 RFC 6455。
     */
    WS("ws"),

    /**
     * 加密的 WebSocket 协议（安全），基于 TLS 的双向实时通信，遵循 RFC 6455。
     */
    WSS("wss"),

    /**
     * 安全的超文本传输协议（HTTPS），基于 TLS/SSL 的加密 HTTP，遵循 RFC 2818。
     */
    HTTPS("https"),

    /**
     * 通用 SSL 协议，支持某些版本的 SSL 加密（已废弃）。
     */
    SSL("ssl"),

    /**
     * SSL 2.0 协议，早期加密协议，因安全漏洞已废弃。
     */
    SSLv2("SSLv2"),

    /**
     * SSL 3.0 协议，改进的加密协议，因安全漏洞已废弃。
     */
    SSLv3("SSLv3"),

    /**
     * 通用 TLS 协议，支持某些版本的 TLS 加密，提供安全通信。
     */
    TLS("tls"),

    /**
     * TLS 1.0 协议，遵循 RFC 2246，提供安全的加密通信（逐渐废弃）。
     */
    TLSv1("TLSv1"),

    /**
     * TLS 1.1 协议，遵循 RFC 4346，改进安全性（逐渐废弃）。
     */
    TLSv1_1("TLSv1.1"),

    /**
     * TLS 1.2 协议，遵循 RFC 5246，进一步增强安全性，广泛使用。
     */
    TLSv1_2("TLSv1.2"),

    /**
     * TLS 1.3 协议，遵循 RFC 8446，提供更高安全性和性能，推荐使用。
     */
    TLSv1_3("TLSv1.3"),

    /**
     * DICOM 协议，用于医学影像数据的传输和存储，遵循 ISO 12052。
     */
    DICOM("dicom"),

    /**
     * HL7 协议，用于医疗信息交换的标准化协议，遵循 HL7 国际标准。
     */
    HL7("hl7"),

    /**
     * OpenID Connect 协议，基于 OAuth2 的身份验证协议，遵循 OpenID 标准。
     */
    OIDC("OIDC"),

    /**
     * SAML 协议，用于单点登录和身份联合，遵循 OASIS 标准。
     */
    SAML("SAML"),

    /**
     * 轻量目录访问协议（LDAP），用于目录服务访问，遵循 RFC 4511。
     */
    LDAP("LDAP");

    /**
     * HTTP 前缀，格式为 "http://"。
     */
    public static final String HTTP_PREFIX = HTTP.name + Symbol.COLON + Symbol.FORWARDSLASH;

    /**
     * HTTPS 前缀，格式为 "https://"。
     */
    public static final String HTTPS_PREFIX = HTTPS.name + Symbol.COLON + Symbol.FORWARDSLASH;

    /**
     * WebSocket 前缀，格式为 "ws://"。
     */
    public static final String WS_PREFIX = WS.name + Symbol.COLON + Symbol.FORWARDSLASH;

    /**
     * 安全 WebSocket 前缀，格式为 "wss://"。
     */
    public static final String WSS_PREFIX = WSS.name + Symbol.COLON + Symbol.FORWARDSLASH;

    /**
     * 本地 IPv4 地址，值为 "127.0.0.1"，表示本地回环地址。
     */
    public static final String HOST_IPV4 = "127.0.0.1";

    /**
     * 本地主机名，值为 "localhost"，用于本地主机访问。
     */
    public static final String HOST_LOCAL = "localhost";

    /**
     * IPv4 地址最小值，字符串形式，值为 "0.0.0.0"，通常用于监听所有接口。
     */
    public static final String IPV4_STR_MIN = "0.0.0.0";

    /**
     * IPv4 地址最大值，字符串形式，值为 "255.255.255.255"，表示广播地址。
     */
    public static final String IPV4_STR_MAX = "255.255.255.255";

    /**
     * IPv4 地址最大值，数值形式，值为 0xffffffffL，表示最大 IP 地址。
     */
    public static final long IPV4_NUM_MAX = 0xffffffffL;

    /**
     * IPv4 未使用地址最大值，字符串形式，值为 "0.255.255.255"，表示未分配地址范围。
     */
    public static final String IPV4_UNUSED_STR_MAX = "0.255.255.255";

    /**
     * 协议名称，用于标识协议类型。
     */
    public final String name;

    /**
     * 构造函数，初始化协议名称。
     *
     * @param name 协议名称
     */
    Protocol(String name) {
        this.name = name;
    }

    /**
     * 根据协议名称获取对应的协议枚举实例。
     *
     * @param protocol 协议名称
     * @return 对应的协议枚举实例
     * @throws IOException 如果协议名称未知
     */
    public static Protocol get(String protocol) throws IOException {
        if (StringKit.isEmpty(protocol)) {
            throw new IOException("Protocol cannot be null or empty");
        }
        switch (protocol) {
        case "HTTP/1.0":
            return HTTP_1_0;
        case "HTTP/1.1":
            return HTTP_1_1;
        case "h2_prior_knowledge":
            return H2_PRIOR_KNOWLEDGE;
        case "h2":
            return HTTP_2;
        case "spdy/3.1":
            return SPDY_3;
        case "quic":
            return QUIC;
        case "soap 1.1 protocol":
            return SOAP_1_1;
        case "SOAP 1.2 Protocol":
            return SOAP_1_2;
        default:
            throw new IOException("Unexpected protocol: " + protocol);
        }
    }

    /**
     * 判断 URL 是否为 HTTP 协议，支持标准前缀和 URL 编码格式。
     *
     * @param url 待验证的 URL
     * @return true 如果是 HTTP 协议，false 否则
     */
    public static boolean isHttp(String url) {
        if (StringKit.isEmpty(url)) {
            return false;
        }
        return url.startsWith(HTTP_PREFIX) || url.startsWith("http%3A%2F%2F");
    }

    /**
     * 判断 URL 是否为 HTTPS 协议，支持标准前缀和 URL 编码格式。
     *
     * @param url 待验证的 URL
     * @return true 如果是 HTTPS 协议，false 否则
     */
    public static boolean isHttps(String url) {
        if (StringKit.isEmpty(url)) {
            return false;
        }
        return url.startsWith(HTTPS_PREFIX) || url.startsWith("https%3A%2F%2F");
    }

    /**
     * 判断 URL 是否指向本地主机（域名或 IP），支持 IPv4 和 localhost。
     *
     * @param url 待验证的 URL
     * @return true 如果是本地主机，false 否则
     */
    public static boolean isLocalHost(String url) {
        return StringKit.isEmpty(url) || url.contains(HOST_IPV4) || url.contains(HOST_LOCAL);
    }

    /**
     * 判断 URL 是否为 HTTPS 协议或本地主机。
     *
     * @param url 待验证的 URL
     * @return true 如果是 HTTPS 或本地主机，false 否则
     */
    public static boolean isHttpsOrLocalHost(String url) {
        if (StringKit.isEmpty(url)) {
            return false;
        }
        return isHttps(url) || isLocalHost(url);
    }

    /**
     * 判断协议是否基于 TCP（非 UDP）。
     *
     * @return true 如果是 TCP 协议，false 如果是 UDP
     */
    public boolean isTcp() {
        return this != UDP;
    }

    /**
     * 判断协议是否为安全协议（基于 TLS/SSL）。
     *
     * @return true 如果是安全协议（HTTPS, WSS, TLS, SSL 等），false 否则
     */
    public boolean isSecure() {
        return this == HTTPS || this == WSS || this == TLS || this == SSL || this == TLSv1 || this == TLSv1_1
                || this == TLSv1_2 || this == TLSv1_3 || this == SSLv2 || this == SSLv3;
    }

    /**
     * 返回协议名称，用于 ALPN（应用层协议协商）协议标识，如 "http/1.1"、"h2" 等。
     *
     * @return 协议名称
     */
    @Override
    public String toString() {
        return name;
    }

}