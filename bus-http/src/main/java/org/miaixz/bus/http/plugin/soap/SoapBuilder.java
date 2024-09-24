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
package org.miaixz.bus.http.plugin.soap;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.*;
import org.miaixz.bus.http.Httpz;
import org.miaixz.bus.http.Response;
import org.miaixz.bus.http.SoapX;

import jakarta.xml.soap.*;

/**
 * SOAP 支持 此对象用于构建一个SOAP消息，并通过HTTP接口发出消息内容。 SOAP消息本质上是一个XML文本，可以通过调用{@link #getString(boolean)} 方法获取消息体
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SoapBuilder {

    /**
     * Soap协议 soap1.1 : text/xml soap1.2 : application/soap+xml
     */
    private final Protocol protocol;
    /**
     * 应用于方法上的命名空间URI
     */
    private final String namespaceURI;
    /**
     * 存储头信息
     */
    private final Map<String, String> headers = new HashMap<>();
    /**
     * 请求的URL地址
     */
    private String url;
    /**
     * SOAP消息
     */
    private SOAPMessage message;
    /**
     * 消息工厂，用于创建消息
     */
    private MessageFactory factory;
    /**
     * 消息方法节点
     */
    private SOAPBodyElement methodEle;
    /**
     * 默认字符编码
     */
    private java.nio.charset.Charset charset = Charset.UTF_8;

    /**
     * 构造，默认使用soap1.2版本协议
     *
     * @param url WS的URL地址
     */
    public SoapBuilder(final String url) {
        this(url, Protocol.SOAP_1_2);
    }

    /**
     * 构造
     *
     * @param url      WS的URL地址
     * @param protocol 协议版本，见{@link Protocol}
     */
    public SoapBuilder(final String url, final Protocol protocol) {
        this(url, protocol, null);
    }

    /**
     * 构造
     *
     * @param url          WS的URL地址
     * @param protocol     协议版本，见{@link Protocol}
     * @param namespaceURI 方法上的命名空间URI
     */
    public SoapBuilder(final String url, final Protocol protocol, final String namespaceURI) {
        this.url = url;
        this.namespaceURI = namespaceURI;
        this.protocol = protocol;
        init(protocol);
    }

    /**
     * 创建SOAP客户端，默认使用soap1.2版本协议
     *
     * @param url WS的URL地址
     * @return this
     */
    public static SoapBuilder of(final String url) {
        return new SoapBuilder(url);
    }

    /**
     * 创建SOAP客户端
     *
     * @param url      WS的URL地址
     * @param protocol 协议，见{@link Protocol}
     * @return this
     */
    public static SoapBuilder of(final String url, final Protocol protocol) {
        return new SoapBuilder(url, protocol);
    }

    /**
     * 创建SOAP客户端
     *
     * @param url          WS的URL地址
     * @param protocol     协议，见{@link Protocol}
     * @param namespaceURI 方法上的命名空间URI
     * @return this
     */
    public static SoapBuilder of(final String url, final Protocol protocol, final String namespaceURI) {
        return new SoapBuilder(url, protocol, namespaceURI);
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
    private static SOAPElement setParam(final SOAPElement ele, final String name, final Object value,
            final String prefix) {
        final SOAPElement childEle;
        try {
            if (StringKit.isNotBlank(prefix)) {
                childEle = ele.addChildElement(name, prefix);
            } else {
                childEle = ele.addChildElement(name);
            }
        } catch (final SOAPException e) {
            throw new InternalException(e);
        }

        if (null != value) {
            if (value instanceof SOAPElement) {
                // 单个子节点
                try {
                    ele.addChildElement((SOAPElement) value);
                } catch (final SOAPException e) {
                    throw new InternalException(e);
                }
            } else if (value instanceof Map) {
                // 多个字节点
                Entry entry;
                for (final Object object : ((Map) value).entrySet()) {
                    entry = (Entry) object;
                    setParam(childEle, StringKit.toStringOrNull(entry.getKey()), entry.getValue(), prefix);
                }
            } else {
                // 单个值
                childEle.setValue(value.toString());
            }
        }

        return childEle;
    }

    /**
     * 初始化
     *
     * @param protocol 协议版本枚举，见{@link Protocol}
     * @return this
     */
    public SoapBuilder init(final Protocol protocol) {
        // 创建消息工厂
        try {
            this.factory = MessageFactory.newInstance(protocol.name);
            // 根据消息工厂创建SoapMessage
            this.message = factory.createMessage();
        } catch (final SOAPException e) {
            throw new InternalException(e);
        }

        return this;
    }

    /**
     * 重置SOAP客户端，用于客户端复用 重置后需调用serMethod方法重新指定请求方法，并调用setParam方法重新定义参数
     *
     * @return this
     */
    public SoapBuilder reset() {
        try {
            this.message = factory.createMessage();
        } catch (final SOAPException e) {
            throw new InternalException(e);
        }
        this.methodEle = null;

        return this;
    }

    /**
     * 设置编码
     *
     * @param charset 编码
     * @return this
     */
    public SoapBuilder charset(final java.nio.charset.Charset charset) {
        if (null != charset) {
            this.charset = charset;
            try {
                this.message.setProperty(SOAPMessage.CHARACTER_SET_ENCODING, charset.name());
                this.message.setProperty(SOAPMessage.WRITE_XML_DECLARATION, "true");
            } catch (final SOAPException e) {
                // ignore
            }
        }

        return this;
    }

    /**
     * 设置Webservice请求地址
     *
     * @param url Webservice请求地址
     * @return this
     */
    public SoapBuilder setUrl(final String url) {
        this.url = url;
        return this;
    }

    /**
     * 设置一个header 如果覆盖模式，则替换之前的值，否则加入到值列表中
     *
     * @param name       Header名
     * @param value      Header值
     * @param isOverride 是否覆盖已有值
     * @return this
     */
    public SoapBuilder header(final String name, final String value, final boolean isOverride) {
        if (null != name && null != value) {
            final String values = headers.get(name.trim());
            if (isOverride || StringKit.isEmpty(values)) {
                headers.put(name.trim(), values);
            }
        }
        return this;
    }

    /**
     * 获取headers
     *
     * @return Header Map
     */
    public Map<String, String> headers() {
        this.headers.put(HTTP.CONTENT_TYPE, getXmlContentType());
        return Collections.unmodifiableMap(headers);
    }

    /**
     * 清除所有头信息，包括全局头信息
     *
     * @return this
     */
    public SoapBuilder clearHeaders() {
        this.headers.clear();
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
    public SOAPHeaderElement addSOAPHeader(final QName name, final String actorURI, final String roleUri,
            final Boolean mustUnderstand, final Boolean relay) {
        final SOAPHeaderElement ele = addSOAPHeader(name);
        try {
            if (StringKit.isNotBlank(roleUri)) {
                ele.setRole(roleUri);
            }
            if (null != relay) {
                ele.setRelay(relay);
            }
        } catch (final SOAPException e) {
            throw new InternalException(e);
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
    public SOAPHeaderElement addSOAPHeader(final String localName) {
        return addSOAPHeader(new QName(localName));
    }

    /**
     * 增加SOAP头信息，方法返回{@link SOAPHeaderElement}可以设置具体属性和子节点
     *
     * @param localName 头节点名称
     * @param value     头节点的值
     * @return {@link SOAPHeaderElement}
     */
    public SOAPHeaderElement addSOAPHeader(final String localName, final String value) {
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
    public SOAPHeaderElement addSOAPHeader(final QName name) {
        final SOAPHeaderElement ele;
        try {
            ele = this.message.getSOAPHeader().addHeaderElement(name);
        } catch (final SOAPException e) {
            throw new InternalException(e);
        }
        return ele;
    }

    /**
     * 设置请求方法
     *
     * @param name            方法名及其命名空间
     * @param params          参数
     * @param useMethodPrefix 是否使用方法的命名空间前缀
     * @return this
     */
    public SoapBuilder setMethod(final Name name, final Map<String, Object> params, final boolean useMethodPrefix) {
        return setMethod(new QName(name.getURI(), name.getLocalName(), name.getPrefix()), params, useMethodPrefix);
    }

    /**
     * 设置请求方法
     *
     * @param name            方法名及其命名空间
     * @param params          参数
     * @param useMethodPrefix 是否使用方法的命名空间前缀
     * @return this
     */
    public SoapBuilder setMethod(final QName name, final Map<String, Object> params, final boolean useMethodPrefix) {
        setMethod(name);
        final String prefix = useMethodPrefix ? name.getPrefix() : null;
        final SOAPBodyElement methodEle = this.methodEle;
        for (final Entry<String, Object> entry : MapKit.wrap(params)) {
            setParam(methodEle, entry.getKey(), entry.getValue(), prefix);
        }

        return this;
    }

    /**
     * 设置请求方法 方法名自动识别前缀，前缀和方法名使用“:”分隔 当识别到前缀后，自动添加xmlns属性，关联到默认的namespaceURI
     *
     * @param methodName 方法名
     * @return this
     */
    public SoapBuilder setMethod(final String methodName) {
        return setMethod(methodName, ObjectKit.defaultIfNull(this.namespaceURI, XMLConstants.NULL_NS_URI));
    }

    /**
     * 设置请求方法 方法名自动识别前缀，前缀和方法名使用“:”分隔 当识别到前缀后，自动添加xmlns属性，关联到传入的namespaceURI
     *
     * @param methodName   方法名（可有前缀也可无）
     * @param namespaceURI 命名空间URI
     * @return this
     */
    public SoapBuilder setMethod(final String methodName, final String namespaceURI) {
        final List<String> methodNameList = StringKit.split(methodName, Symbol.COLON);
        final QName qName;
        if (2 == methodNameList.size()) {
            qName = new QName(namespaceURI, methodNameList.get(1), methodNameList.get(0));
        } else {
            qName = new QName(namespaceURI, methodName);
        }
        return setMethod(qName);
    }

    /**
     * 设置请求方法
     *
     * @param name 方法名及其命名空间
     * @return this
     */
    public SoapBuilder setMethod(final QName name) {
        try {
            this.methodEle = this.message.getSOAPBody().addBodyElement(name);
        } catch (final SOAPException e) {
            throw new InternalException(e);
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
    public SoapBuilder setParam(final String name, final Object value) {
        return setParam(name, value, true);
    }

    /**
     * 设置方法参数
     *
     * @param name            参数名
     * @param value           参数值，可以是字符串或Map或{@link SOAPElement}
     * @param useMethodPrefix 是否使用方法的命名空间前缀
     * @return this
     */
    public SoapBuilder setParam(final String name, final Object value, final boolean useMethodPrefix) {
        setParam(this.methodEle, name, value, useMethodPrefix ? this.methodEle.getPrefix() : null);
        return this;
    }

    /**
     * 批量设置参数，使用方法的前缀
     *
     * @param params 参数列表
     * @return this
     */
    public SoapBuilder setParams(final Map<String, Object> params) {
        return setParams(params, true);
    }

    /**
     * 批量设置参数
     *
     * @param params          参数列表
     * @param useMethodPrefix 是否使用方法的命名空间前缀
     * @return this
     */
    public SoapBuilder setParams(final Map<String, Object> params, final boolean useMethodPrefix) {
        for (final Entry<String, Object> entry : MapKit.wrap(params)) {
            setParam(entry.getKey(), entry.getValue(), useMethodPrefix);
        }
        return this;
    }

    /**
     * 获取方法节点 用于创建子节点等操作
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
     * @param pretty 是否格式化
     * @return 消息字符串
     */
    public String getString(final boolean pretty) {
        return SoapX.toString(this.message, pretty, this.charset);
    }

    /**
     * 将SOAP消息的XML内容输出到流
     *
     * @param out 输出流
     * @return this
     */
    public SoapBuilder write(final OutputStream out) {
        try {
            this.message.writeTo(out);
        } catch (final SOAPException | IOException e) {
            throw new InternalException(e);
        }
        return this;
    }

    /**
     * 执行Webservice请求，即发送SOAP内容
     *
     * @return 返回结果
     */
    public SOAPMessage sendForMessage() {
        final Response res = sendForResponse();
        final MimeHeaders headers = new MimeHeaders();
        for (final Entry<String, List<String>> entry : res.headers().toMultimap().entrySet()) {
            if (StringKit.isNotEmpty(entry.getKey())) {
                headers.setHeader(entry.getKey(), CollKit.get(entry.getValue(), 0));
            }
        }
        try {
            return this.factory.createMessage(headers, res.body().byteStream());
        } catch (final IOException | SOAPException e) {
            throw new InternalException(e);
        } finally {
            IoKit.closeQuietly(res);
        }
    }

    /**
     * 执行Webservice请求，即发送SOAP内容
     *
     * @return 返回结果
     */
    public String send() {
        return send(false);
    }

    /**
     * 执行Webservice请求，即发送SOAP内容
     *
     * @param pretty 是否格式化
     * @return 返回结果
     */
    public String send(final boolean pretty) {
        final String body;
        try {
            body = sendForResponse().body().string();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return pretty ? XmlKit.format(body) : body;
    }

    /**
     * 发送请求，获取异步响应
     *
     * @return 响应对象
     */
    public Response sendForResponse() {
        try {
            return Httpz.post().url(this.url).addHeader(this.headers).addParam(getString(false)).build().execute();
        } catch (Exception e) {
            throw new InternalException(e);
        }
    }

    /**
     * 获取请求的Content-Type，附加编码信息 XML消息体的Content-Type soap1.1 : text/xml soap1.2 : application/soap+xml soap1.1与soap1.2区别:
     * https://www.cnblogs.com/qlqwjy/p/7577147.html
     *
     * @return 请求的Content-Type
     */
    private String getXmlContentType() {
        switch (this.protocol) {
        case SOAP_1_1:
            return MediaType.TEXT_XML.concat(";charset=" + this.charset.toString());
        case SOAP_1_2:
            return MediaType.APPLICATION_SOAP_XML.concat(";charset=" + this.charset.toString());
        default:
            throw new InternalException("Unsupported protocol: {}", this.protocol);
        }
    }

}
