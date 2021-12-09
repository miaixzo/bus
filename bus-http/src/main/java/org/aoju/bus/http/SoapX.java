/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
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
package org.aoju.bus.http;

import org.aoju.bus.core.lang.Header;
import org.aoju.bus.core.lang.Http;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.toolkit.*;
import org.aoju.bus.http.magic.HttpResponse;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.soap.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SOAP客户端
 *
 * <p>
 * 此对象用于构建一个SOAP消息，并通过HTTP接口发出消息内容。
 * SOAP消息本质上是一个XML文本，可以通过调用{@link #getMessage(boolean)} 方法获取消息体
 * <p>
 * 使用方法：
 *
 * <pre>
 * SoapX client = SoapX.create(url)
 * .method(methodName, namespaceURI)
 * .charset(CharsetKit.CHARSET_GBK)
 * .param("param1", "XXX");
 *
 * String response = client.send(true);
 *
 * </pre>
 *
 * @author Kimi Liu
 * @version 6.3.2
 * @since JDK 1.8+
 */
public class SoapX {

    /**
     * XML消息体的Content-Type
     * soap1.1 : text/xml
     * soap1.2 : application/soap+xml
     * soap1.1与soap1.2区别:  https://www.cnblogs.com/qlqwjy/p/7577147.html
     */
    private static final String CONTENT_TYPE_SOAP11_TEXT_XML = "text/xml;charset=";
    private static final String CONTENT_TYPE_SOAP12_SOAP_XML = "application/soap+xml;charset=";
    /**
     * 编码
     */
    private String charset = org.aoju.bus.core.lang.Charset.DEFAULT_UTF_8;
    /**
     * http版本
     */
    private String version = Http.HTTP_1_1;
    /**
     * Soap协议
     * soap1.1 : text/xml
     * soap1.2 : application/soap+xml
     */
    private Protocol protocol;
    /**
     * 请求的URL地址
     */
    private String url;
    /**
     * 应用于方法上的命名空间URI
     */
    private String namespaceURI;
    /**
     * 消息工厂，用于创建消息
     */
    private MessageFactory factory;
    /**
     * SOAP消息
     */
    private SOAPMessage message;
    /**
     * 消息方法节点
     */
    private SOAPBodyElement methodEle;
    /**
     * 存储头信息
     */
    private Map<String, String> headers = new HashMap<>();

    /**
     * 构造，默认使用soap1.1版本协议
     *
     * @param url WS的URL地址
     */
    public SoapX(String url) {
        this(url, Protocol.SOAP_1_1);
    }

    /**
     * 构造
     *
     * @param url      WS的URL地址
     * @param protocol 协议版本，见{@link Protocol}
     */
    public SoapX(String url, Protocol protocol) {
        this(url, protocol, null);
    }

    /**
     * 构造
     *
     * @param url          WS的URL地址
     * @param protocol     协议版本，见{@link Protocol}
     * @param namespaceURI 方法上的命名空间URI
     */
    public SoapX(String url, Protocol protocol, String namespaceURI) {
        this.url = url;
        this.namespaceURI = namespaceURI;
        this.protocol = protocol;
        // 创建消息工厂
        try {
            this.factory = MessageFactory.newInstance(this.protocol.toString());
            // 根据消息工厂创建SoapMessage
            this.message = factory.createMessage();
        } catch (SOAPException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 创建SOAP客户端，默认使用soap1.1版本协议
     *
     * @param url WS的URL地址
     * @return this
     */
    public static SoapX create(String url) {
        return new SoapX(url);
    }

    /**
     * 创建SOAP客户端
     *
     * @param url      WS的URL地址
     * @param protocol 协议，见{@link Protocol}
     * @return this
     */
    public static SoapX create(String url, Protocol protocol) {
        return new SoapX(url, protocol);
    }

    /**
     * 创建SOAP客户端
     *
     * @param url          WS的URL地址
     * @param protocol     协议，见{@link Protocol}
     * @param namespaceURI 方法上的命名空间URI
     * @return this
     */
    public static SoapX create(String url, Protocol protocol, String namespaceURI) {
        return new SoapX(url, protocol, namespaceURI);
    }

    /**
     * 设置Webservice请求地址
     *
     * @param url Webservice请求地址
     * @return this
     */
    public SoapX url(String url) {
        this.url = url;
        return this;
    }

    /**
     * 设置编码
     *
     * @param charset 编码
     * @return this
     */
    public SoapX charset(String charset) {
        if (null != charset) {
            this.charset = charset;
        }
        try {
            this.message.setProperty(SOAPMessage.CHARACTER_SET_ENCODING, this.charset);
            this.message.setProperty(SOAPMessage.WRITE_XML_DECLARATION, "true");
        } catch (SOAPException e) {
            // ignore
        }

        return this;
    }

    /**
     * 设置请求方法
     * 方法名自动识别前缀，前缀和方法名使用“:”分隔
     * 当识别到前缀后，自动添加xmlns属性，关联到默认的namespaceURI
     *
     * @param methodName 方法名
     * @return this
     */
    public SoapX method(String methodName) {
        return method(methodName, ObjectKit.defaultIfNull(this.namespaceURI, XMLConstants.NULL_NS_URI));
    }

    /**
     * 设置请求方法
     *
     * @param name 方法名及其命名空间
     * @return this
     */
    public SoapX method(QName name) {
        try {
            this.methodEle = this.message.getSOAPBody().addBodyElement(name);
        } catch (SOAPException e) {
            throw new InstrumentException(e);
        }

        return this;
    }

    /**
     * 设置请求方法
     * 方法名自动识别前缀，前缀和方法名使用':'分隔
     * 当识别到前缀后，自动添加xmlns属性，关联到传入的namespaceURI
     *
     * @param methodName   方法名（可有前缀也可无）
     * @param namespaceURI 命名空间URI
     * @return this
     */
    public SoapX method(String methodName, String namespaceURI) {
        final List<String> methodNameList = StringKit.split(methodName, Symbol.C_COLON);
        final QName qName;
        if (2 == methodNameList.size()) {
            qName = new QName(namespaceURI, methodNameList.get(1), methodNameList.get(0));
        } else {
            qName = new QName(namespaceURI, methodName);
        }
        return method(qName);
    }

    /**
     * 设置请求方法
     *
     * @param name   方法名及其命名空间
     * @param params 参数
     * @return this
     */
    public SoapX method(Name name, Map<String, Object> params) {
        return method(new QName(name.getURI(), name.getLocalName(), name.getPrefix()), params);
    }

    /**
     * 设置请求方法
     *
     * @param name   方法名及其命名空间
     * @param params 参数
     * @return this
     */
    public SoapX method(QName name, Map<String, Object> params) {
        method(name);
        final String prefix = name.getPrefix();
        final SOAPBodyElement methodEle = this.methodEle;
        for (Map.Entry<String, Object> entry : MapKit.wrap(params)) {
            param(methodEle, entry.getKey(), entry.getValue(), prefix);
        }
        return this;
    }

    /**
     * 设置请求方法
     *
     * @param name            方法名及其命名空间
     * @param params          参数
     * @param useMethodPrefix 是否使用方法的命名空间前缀
     * @return this
     */
    public SoapX method(Name name, Map<String, Object> params, boolean useMethodPrefix) {
        return method(new QName(name.getURI(), name.getLocalName(), name.getPrefix()), params, useMethodPrefix);
    }

    /**
     * 设置请求方法
     *
     * @param name            方法名及其命名空间
     * @param params          参数
     * @param useMethodPrefix 是否使用方法的命名空间前缀
     * @return this
     */
    public SoapX method(QName name, Map<String, Object> params, boolean useMethodPrefix) {
        method(name);
        final String prefix = useMethodPrefix ? name.getPrefix() : null;
        final SOAPBodyElement methodEle = this.methodEle;
        for (Map.Entry<String, Object> entry : MapKit.wrap(params)) {
            param(methodEle, entry.getKey(), entry.getValue(), prefix);
        }
        return this;
    }

    /**
     * 设置方法参数，使用方法的前缀
     *
     * @param name  参数名
     * @param value 参数值，可以是字符串或Map或{@link SOAPElement}
     * @return this
     */
    public SoapX param(String name, Object value) {
        return param(name, value, true);
    }

    /**
     * 设置方法参数
     *
     * @param name            参数名
     * @param value           参数值，可以是字符串或Map或{@link SOAPElement}
     * @param useMethodPrefix 是否使用方法的命名空间前缀
     * @return this
     */
    public SoapX param(String name, Object value, boolean useMethodPrefix) {
        param(this.methodEle, name, value, useMethodPrefix ? this.methodEle.getPrefix() : null);
        return this;
    }

    /**
     * 批量设置参数，使用方法的前缀
     *
     * @param params 参数列表
     * @return this
     */
    public SoapX param(Map<String, Object> params) {
        return param(params, true);
    }

    /**
     * 批量设置参数
     *
     * @param params          参数列表
     * @param useMethodPrefix 是否使用方法的命名空间前缀
     * @return this
     */
    public SoapX param(Map<String, Object> params, boolean useMethodPrefix) {
        for (Map.Entry<String, Object> entry : MapKit.wrap(params)) {
            param(entry.getKey(), entry.getValue(), useMethodPrefix);
        }
        return this;
    }

    /**
     * 设置方法参数
     *
     * @param ele    方法节点
     * @param name   参数名
     * @param value  参数值
     * @param prefix 命名空间前缀， {@code null}表示不使用前缀
     * @return {@link SOAPElement}子节点
     */
    public SOAPElement param(SOAPElement ele, String name, Object value, String prefix) {
        final SOAPElement childEle;
        try {
            if (StringKit.isNotBlank(prefix)) {
                childEle = ele.addChildElement(name, prefix);
            } else {
                childEle = ele.addChildElement(name);
            }
        } catch (SOAPException e) {
            throw new InstrumentException(e);
        }

        if (null != value) {
            if (value instanceof SOAPElement) {
                // 单个子节点
                try {
                    ele.addChildElement((SOAPElement) value);
                } catch (SOAPException e) {
                    throw new InstrumentException(e);
                }
            } else if (value instanceof Map) {
                // 多个字节点
                Map.Entry entry;
                for (Object obj : ((Map) value).entrySet()) {
                    entry = (Map.Entry) obj;
                    param(childEle, entry.getKey().toString(), entry.getValue(), prefix);
                }
            } else {
                // 单个值
                childEle.setValue(value.toString());
            }
        }
        return childEle;
    }

    /**
     * 返回http版本
     *
     * @return String
     */
    public String version() {
        return version;
    }

    /**
     * 设置http版本
     *
     * @param version Http版本，{@link Http#HTTP_1_0}，{@link Http#HTTP_1_1}
     * @return this
     */
    public SoapX version(String version) {
        this.version = version;
        return this;
    }

    /**
     * 设置一个header
     * 如果覆盖模式，则替换之前的值，否则加入到值列表中
     *
     * @param name       Header名
     * @param value      Header值
     * @param isOverride 是否覆盖已有值
     * @return this
     */
    public SoapX headers(String name, String value, boolean isOverride) {
        if (null != name && null != value) {
            final String values = headers.get(name.trim());
            if (isOverride || StringKit.isEmpty(values)) {
                headers.put(name.trim(), value);
            }
        }
        return this;
    }

    /**
     * 获取headers
     *
     * @return Headers Map
     */
    public Map<String, String> headers() {
        return Collections.unmodifiableMap(headers);
    }

    /**
     * 设置一个header
     * 覆盖模式，则替换之前的值
     *
     * @param name  Header名
     * @param value Header值
     * @return this
     */
    public SoapX headers(String name, String value) {
        return headers(name, value, true);
    }

    /**
     * 设置请求头
     *
     * @param headers    请求头
     * @param isOverride 是否覆盖已有头信息
     * @return this
     */
    public SoapX headers(Map<String, String> headers, boolean isOverride) {
        if (CollKit.isEmpty(headers)) {
            return this;
        }

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            this.headers(entry.getKey(), StringKit.nullToEmpty(entry.getValue()), isOverride);
        }
        return this;
    }

    /**
     * 新增请求头
     * 不覆盖原有请求头
     *
     * @param headers 请求头
     * @return this
     */
    public SoapX headers(Map<String, String> headers) {
        if (CollKit.isEmpty(headers)) {
            return this;
        }

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            this.headers(entry.getKey(), StringKit.nullToEmpty(entry.getValue()), false);
        }
        return this;
    }

    /**
     * 增加SOAP头信息，方法返回{@link SOAPHeaderElement}可以设置具体属性和子节点
     *
     * @param name           头信息标签名
     * @param actorURI       中间的消息接收者
     * @param roleUri        Role的URI
     * @param mustUnderstand 标题项对于要对其进行处理的接收者来说是强制的还是可选的
     * @param relay          relay属性
     * @return {@link SOAPHeaderElement}
     */
    public SOAPHeaderElement addSOAPHeader(QName name, String actorURI, String roleUri, Boolean mustUnderstand, Boolean relay) {
        final SOAPHeaderElement ele = addSOAPHeader(name);
        try {
            if (StringKit.isNotBlank(roleUri)) {
                ele.setRole(roleUri);
            }
            if (null != relay) {
                ele.setRelay(relay);
            }
        } catch (SOAPException e) {
            throw new InstrumentException(e);
        }

        if (StringKit.isNotBlank(actorURI)) {
            ele.setActor(actorURI);
        }
        if (null != mustUnderstand) {
            ele.setMustUnderstand(mustUnderstand);
        }

        return ele;
    }

    /**
     * 增加SOAP头信息，方法返回{@link SOAPHeaderElement}可以设置具体属性和子节点
     *
     * @param localName 头节点名称
     * @return {@link SOAPHeaderElement}
     */
    public SOAPHeaderElement addSOAPHeader(String localName) {
        return addSOAPHeader(new QName(localName));
    }

    /**
     * 增加SOAP头信息，方法返回{@link SOAPHeaderElement}可以设置具体属性和子节点
     *
     * @param localName 头节点名称
     * @param value     头节点的值
     * @return {@link SOAPHeaderElement}
     */
    public SOAPHeaderElement addSOAPHeader(String localName, String value) {
        final SOAPHeaderElement soapHeaderElement = addSOAPHeader(localName);
        soapHeaderElement.setTextContent(value);
        return soapHeaderElement;
    }

    /**
     * 增加SOAP头信息，方法返回{@link SOAPHeaderElement}可以设置具体属性和子节点
     *
     * @param name 头节点名称
     * @return {@link SOAPHeaderElement}
     */
    public SOAPHeaderElement addSOAPHeader(QName name) {
        SOAPHeaderElement ele;
        try {
            ele = this.message.getSOAPHeader().addHeaderElement(name);
        } catch (SOAPException e) {
            throw new InstrumentException(e);
        }
        return ele;
    }

    /**
     * {@link SOAPMessage} 转为字符串
     *
     * @param message SOAP消息对象
     * @param format  是否格式化
     * @param charset 编码
     * @return SOAP XML字符串
     */
    public String toString(SOAPMessage message, boolean format, String charset) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        String result;
        try {
            message.writeTo(out);
            result = out.toString(charset);
        } catch (SOAPException | IOException e) {
            throw new InstrumentException(e);
        }
        return format ? XmlKit.format(result) : result;
    }

    /**
     * 重置SOAP客户端，用于客户端复用
     * 重置后需调用serMethod方法重新指定请求方法，并调用setParam方法重新定义参数
     *
     * @return this
     */
    public SoapX reset() {
        try {
            this.message = factory.createMessage();
        } catch (SOAPException e) {
            throw new InstrumentException(e);
        }
        this.methodEle = null;

        return this;
    }

    /**
     * 获取方法节点
     * 用于创建子节点等操作
     *
     * @return {@link SOAPBodyElement}
     */
    public SOAPBodyElement getMethodEle() {
        return this.methodEle;
    }

    /**
     * 获取SOAP消息对象 {@link SOAPMessage}
     *
     * @return {@link SOAPMessage}
     */
    public SOAPMessage getMessage() {
        return this.message;
    }

    /**
     * 获取SOAP请求消息
     *
     * @param format 是否格式化
     * @return 消息字符串
     */
    public String getMessage(boolean format) {
        return toString(this.message, format, this.charset);
    }

    /**
     * 移除一个头信息
     *
     * @param name Header名
     * @return this
     */
    public SoapX removeHeader(String name) {
        if (name != null) {
            headers.remove(name.trim());
        }
        return this;
    }

    /**
     * 将SOAP消息的XML内容输出到流
     *
     * @param out 输出流
     * @return this
     */
    public SoapX write(OutputStream out) {
        try {
            this.message.writeTo(out);
        } catch (SOAPException | IOException e) {
            throw new InstrumentException(e);
        }
        return this;
    }

    /**
     * 执行Webservice请求，即发送SOAP内容
     *
     * @return 返回结果
     */
    public SOAPMessage send() {
        try {
            final HttpResponse res = transpond();
            final MimeHeaders headers = new MimeHeaders();
            for (Map.Entry<String, List<String>> entry : res.headers().toMultimap().entrySet()) {
                if (StringKit.isNotEmpty(entry.getKey())) {
                    headers.setHeader(entry.getKey(), CollKit.get(entry.getValue(), 0));
                }
            }
            this.message = this.factory.createMessage(headers, res.body().byteStream());
            return this.message;
        } catch (IOException | SOAPException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 执行Webservice请求，即发送SOAP内容
     *
     * @param format 是否格式化
     * @return 返回结果
     */
    public String send(boolean format) {
        String result = Normal.EMPTY;
        try {
            final HttpResponse response = transpond();
            if (null != response.body()) {
                byte[] bytes = response.body().bytes();
                result = new String(bytes, this.charset);
            }
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
        return format ? XmlKit.format(result) : result;
    }


    /**
     * 执行Webservice请求，即发送SOAP内容
     *
     * @return 返回结果
     */
    public HttpResponse transpond() {
        try {
            this.headers(Header.CONTENT_TYPE, getContentType());
            return Httpz.post()
                    .url(this.url)
                    .headers(this.headers)
                    .body(getMessage(false)).build().execute();
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 获取请求的Content-Type，附加编码信息
     *
     * @return 请求的Content-Type
     */
    public String getContentType() {
        switch (this.protocol) {
            case SOAP_1_1:
                return CONTENT_TYPE_SOAP11_TEXT_XML.concat(this.charset);
            case SOAP_1_2:
                return CONTENT_TYPE_SOAP12_SOAP_XML.concat(this.charset);
            default:
                throw new InstrumentException("Unsupported protocol: " + this.protocol);
        }
    }

}