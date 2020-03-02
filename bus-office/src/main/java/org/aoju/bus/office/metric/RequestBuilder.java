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
package org.aoju.bus.office.metric;

/**
 * LibreOffice联机通信的请求配置.
 *
 * @author Kimi Liu
 * @version 5.6.5
 * @since JDK 1.8+
 */
public class RequestBuilder {

    private final String url;
    private final int connectTimeout;
    private final int socketTimeout;

    /**
     * 使用指定的参数构造新配置.
     *
     * @param url            转换的URL
     * @param connectTimeout 超时时间(毫秒)，直到建立连接为止。0的超时值被解释为无限超时。负值被解释为未定义(系统默认值)
     * @param socketTimeout  套接字超时({@code SO_TIMEOUT})，以毫秒为单位，是等待数据的超时，换句话说，
     *                       是两个连续数据包之间的最大不活动周期)。0的超时值被解释为无限超时
     *                       负值被解释为未定义(系统默认值)
     */
    public RequestBuilder(final String url, final int connectTimeout, final int socketTimeout) {
        this.url = url;
        this.connectTimeout = connectTimeout;
        this.socketTimeout = socketTimeout;
    }

    /**
     * 获取可发送转换请求的URL
     *
     * @return 发送转换请求的URL
     */
    public String getUrl() {
        return url;
    }

    /**
     * 获取超时时间(以毫秒为单位)，直到建立连接为止。0的超时值被解释为无限超时
     * 默认值: {@code -1}
     *
     * @return 连接超时时间
     */
    public int getConnectTimeout() {
        return connectTimeout;
    }

    /**
     * 获取套接字超时({@code SO_TIMEOUT})，以毫秒为单位，
     * 这是等待数据的超时，换句话说，是两个连续数据包之间的最大不活动周期)
     * 默认值: {@code -1}
     *
     * @return socket 超时时间
     */
    public int getSocketTimeout() {
        return socketTimeout;
    }

}
