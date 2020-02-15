/*
 * The MIT License
 *
 * Copyright (c) 2015-2020 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.http;

import java.io.IOException;

/**
 * 协议vs计划 它的名字是:{@link java.net.URL#getProtocol()}
 * 返回{@linkplain java.net.URI#getScheme() scheme} (http, https, etc.)，
 * 而不是协议(http/1.1, spdy/3.1，等等) 请使用这个协议来识别它是如何被分割的
 * Httpd使用协议这个词来标识HTTP消息是如何构造的
 *
 * @author Kimi Liu
 * @version 5.6.1
 * @since JDK 1.8+
 */
public enum Protocol {

    /**
     * 一种过时的plaintext，默认情况下不使用持久套接字
     */
    HTTP_1_0("http/1.0"),

    /**
     * 包含持久连接的plaintext
     * 此版本的Httpd实现了RFC 7230，并跟踪对该规范的修订
     */
    HTTP_1_1("http/1.1"),

    /**
     * Chromium的二进制框架协议，包括标头压缩、在同一个套接字上多路复用多个请求和服务器推送
     * HTTP/1.1语义在SPDY/3上分层.
     */
    SPDY_3("spdy/3.1"),

    /**
     * IETF的二进制框架协议，包括头压缩、在同一个套接字上多路复用多个请求和服务器推送
     * HTTP/1.1语义是在HTTP/2上分层的.
     */
    HTTP_2("http/2.0"),

    /**
     * 明文HTTP/2，没有"upgrade"往返。此选项要求客户端事先知道服务器支持明文HTTP/2
     */
    H2_PRIOR_KNOWLEDGE("h2_prior_knowledge"),

    /**
     * QUIC(快速UDP互联网连接)是一个新的多路复用和UDP之上的安全传输，
     * 从底层设计和优化的HTTP/2语义。HTTP/1.1语义是在HTTP/2上分层的
     */
    QUIC("quic");

    private final String protocol;

    Protocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     * @param protocol 协议标示
     * @return 返回由{@code protocol}标识的协议
     * @throws IOException if {@code protocol} is unknown.
     */
    public static Protocol get(String protocol) throws IOException {
        if (protocol.equals(HTTP_1_0.protocol)) return HTTP_1_0;
        if (protocol.equals(HTTP_1_1.protocol)) return HTTP_1_1;
        if (protocol.equals(H2_PRIOR_KNOWLEDGE.protocol)) return H2_PRIOR_KNOWLEDGE;
        if (protocol.equals(HTTP_2.protocol)) return HTTP_2;
        if (protocol.equals(SPDY_3.protocol)) return SPDY_3;
        if (protocol.equals(QUIC.protocol)) return QUIC;
        throw new IOException("Unexpected protocol: " + protocol);
    }

    /**
     * 返回用于识别ALPN协议的字符串，如“http/1.1”、“spdy/3.1”或“http/2.0”.
     */
    @Override
    public String toString() {
        return protocol;
    }

}
