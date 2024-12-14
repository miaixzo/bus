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
package org.miaixz.bus.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.XmlKit;
import org.miaixz.bus.http.plugin.soap.SoapBuilder;

import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPMessage;

/**
 * SOAP相关工具类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SoapX {

    /**
     * 创建SOAP客户端，默认使用soap1.2版本协议
     *
     * @param url WS的URL地址
     * @return {@link SoapBuilder}
     */
    public static SoapBuilder create(final String url) {
        return SoapBuilder.of(url);
    }

    /**
     * 创建SOAP客户端
     *
     * @param url      WS的URL地址
     * @param protocol 协议，见{@link Protocol}
     * @return {@link SoapBuilder}
     */
    public static SoapBuilder create(final String url, final Protocol protocol) {
        return SoapBuilder.of(url, protocol);
    }

    /**
     * 创建SOAP客户端
     *
     * @param url          WS的URL地址
     * @param protocol     协议，见{@link Protocol}
     * @param namespaceURI 方法上的命名空间URI
     * @return {@link SoapBuilder}
     */
    public static SoapBuilder create(final String url, final Protocol protocol, final String namespaceURI) {
        return SoapBuilder.of(url, protocol, namespaceURI);
    }

    /**
     * {@link SOAPMessage} 转为字符串
     *
     * @param message SOAP消息对象
     * @param pretty  是否格式化
     * @return SOAP XML字符串
     */
    public static String toString(final SOAPMessage message, final boolean pretty) {
        return toString(message, pretty, Charset.UTF_8);
    }

    /**
     * {@link SOAPMessage} 转为字符串
     *
     * @param message SOAP消息对象
     * @param pretty  是否格式化
     * @param charset 编码
     * @return SOAP XML字符串
     */
    public static String toString(final SOAPMessage message, final boolean pretty,
            final java.nio.charset.Charset charset) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            message.writeTo(out);
        } catch (final SOAPException | IOException e) {
            throw new InternalException(e);
        }
        final String messageToString;
        try {
            messageToString = out.toString(charset.toString());
        } catch (final UnsupportedEncodingException e) {
            throw new InternalException(e);
        }
        return pretty ? XmlKit.format(messageToString) : messageToString;
    }

}
