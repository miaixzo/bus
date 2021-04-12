package org.aoju.bus.http;

import org.aoju.bus.core.lang.Console;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.map.CaseInsensitiveMap;
import org.aoju.bus.core.toolkit.*;
import org.aoju.bus.http.magic.HttpResponse;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.soap.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SOAP客户端
 *
 * <p>
 * 此对象用于构建一个SOAP消息，并通过HTTP接口发出消息内容。
 * SOAP消息本质上是一个XML文本，可以通过调用{@link #getMsgStr(boolean)} 方法获取消息体
 * <p>
 * 使用方法：
 *
 * <pre>
 * SoapX client = SoapX.create(url)
 * .setMethod(methodName, namespaceURI)
 * .setCharset(CharsetKit.CHARSET_GBK)
 * .setParam(param1, "XXX");
 *
 * String response = client.send(true);
 *
 * </pre>
 */
public class SoapX<T> {

    /**
     * HTTP/1.0
     */
    public static final String HTTP_1_0 = "HTTP/1.0";
    /**
     * HTTP/1.1
     */
    public static final String HTTP_1_1 = "HTTP/1.1";
    /**
     * XML消息体的Content-Type
     * soap1.1 : text/xml
     * soap1.2 : application/soap+xml
     * soap1.1与soap1.2区别:  https://www.cnblogs.com/qlqwjy/p/7577147.html
     */
    private static final String CONTENT_TYPE_SOAP11_TEXT_XML = "text/xml;charset=";
    private static final String CONTENT_TYPE_SOAP12_SOAP_XML = "application/soap+xml;charset=";
    /**
     * 应用于方法上的命名空间URI
     */
    private final String namespaceURI;
    /**
     * Soap协议
     * soap1.1 : text/xml
     * soap1.2 : application/soap+xml
     */
    private final Protocol protocol;
    /**
     * 存储头信息
     */
    protected Map<String, String> headers = new HashMap<>();
    /**
     * 编码
     */
    protected Charset charset = org.aoju.bus.core.lang.Charset.UTF_8;
    /**
     * http版本
     */
    protected String version = HTTP_1_1;
    /**
     * 请求的URL地址
     */
    private String url;
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
        init(protocol);
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
     * 设置方法参数
     *
     * @param ele    方法节点
     * @param name   参数名
     * @param value  参数值
     * @param prefix 命名空间前缀
     * @return {@link SOAPElement}子节点
     */
    private static SOAPElement setParam(SOAPElement ele, String name, Object value, String prefix) {
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
                    setParam(childEle, entry.getKey().toString(), entry.getValue(), prefix);
                }
            } else {
                // 单个值
                childEle.setValue(value.toString());
            }
        }

        return childEle;
    }

    /**
     * {@link SOAPMessage} 转为字符串
     *
     * @param message SOAP消息对象
     * @param pretty  是否格式化
     * @return SOAP XML字符串
     */
    public static String toString(SOAPMessage message, boolean pretty) {
        return toString(message, pretty, org.aoju.bus.core.lang.Charset.UTF_8);
    }

    /**
     * {@link SOAPMessage} 转为字符串
     *
     * @param message SOAP消息对象
     * @param pretty  是否格式化
     * @param charset 编码
     * @return SOAP XML字符串
     */
    public static String toString(SOAPMessage message, boolean pretty, Charset charset) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            message.writeTo(out);
        } catch (SOAPException | IOException e) {
            throw new InstrumentException(e);
        }
        String messageToString;
        try {
            messageToString = out.toString(charset.toString());
        } catch (UnsupportedEncodingException e) {
            throw new InstrumentException(e);
        }
        return pretty ? XmlKit.format(messageToString) : messageToString;
    }

    /**
     * 初始化
     *
     * @param protocol 协议版本枚举，见{@link Protocol}
     * @return this
     */
    public SoapX init(Protocol protocol) {
        // 创建消息工厂
        try {
            this.factory = MessageFactory.newInstance(protocol.toString());
            // 根据消息工厂创建SoapMessage
            this.message = factory.createMessage();
        } catch (SOAPException e) {
            throw new InstrumentException(e);
        }

        return this;
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
     * 设置编码
     *
     * @param charset 编码
     * @return this
     */
    public SoapX setCharset(Charset charset) {
        return this.charset(charset);
    }

    public SoapX charset(Charset charset) {
        if (null != charset) {
            this.charset = charset;
        }
        try {
            this.message.setProperty(SOAPMessage.CHARACTER_SET_ENCODING, this.charset());
            this.message.setProperty(SOAPMessage.WRITE_XML_DECLARATION, "true");
        } catch (SOAPException e) {
            // ignore
        }

        return this;
    }

    /**
     * 设置Webservice请求地址
     *
     * @param url Webservice请求地址
     * @return this
     */
    public SoapX setUrl(String url) {
        this.url = url;
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
     * 设置请求方法
     *
     * @param name            方法名及其命名空间
     * @param params          参数
     * @param useMethodPrefix 是否使用方法的命名空间前缀
     * @return this
     */
    public SoapX setMethod(Name name, Map<String, Object> params, boolean useMethodPrefix) {
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
    public SoapX setMethod(QName name, Map<String, Object> params, boolean useMethodPrefix) {
        setMethod(name);
        final String prefix = name.getPrefix();
        final SOAPBodyElement methodEle = this.methodEle;
        for (Map.Entry<String, Object> entry : MapKit.wrap(params)) {
            setParam(methodEle, entry.getKey(), entry.getValue(), prefix);
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
    public SoapX setMethod(String methodName) {
        return setMethod(methodName, ObjectKit.defaultIfNull(this.namespaceURI, XMLConstants.NULL_NS_URI));
    }

    /**
     * 设置请求方法
     * 方法名自动识别前缀，前缀和方法名使用“:”分隔
     * 当识别到前缀后，自动添加xmlns属性，关联到传入的namespaceURI
     *
     * @param methodName   方法名（可有前缀也可无）
     * @param namespaceURI 命名空间URI
     * @return this
     */
    public SoapX setMethod(String methodName, String namespaceURI) {
        final List<String> methodNameList = StringKit.split(methodName, ':');
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
    public SoapX setMethod(QName name) {
        try {
            this.methodEle = this.message.getSOAPBody().addBodyElement(name);
        } catch (SOAPException e) {
            throw new InstrumentException(e);
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
    public SoapX setParam(String name, Object value) {
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
    public SoapX setParam(String name, Object value, boolean useMethodPrefix) {
        setParam(this.methodEle, name, value, useMethodPrefix ? this.methodEle.getPrefix() : null);
        return this;
    }

    /**
     * 批量设置参数，使用方法的前缀
     *
     * @param params 参数列表
     * @return this
     */
    public SoapX setParams(Map<String, Object> params) {
        return setParams(params, true);
    }

    /**
     * 批量设置参数
     *
     * @param params          参数列表
     * @param useMethodPrefix 是否使用方法的命名空间前缀
     * @return this
     */
    public SoapX setParams(Map<String, Object> params, boolean useMethodPrefix) {
        for (Map.Entry<String, Object> entry : MapKit.wrap(params)) {
            setParam(entry.getKey(), entry.getValue(), useMethodPrefix);
        }
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
     * @param pretty 是否格式化
     * @return 消息字符串
     */
    public String getMsgStr(boolean pretty) {
        return toString(this.message, pretty, this.charset);
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
    public SOAPMessage sendForMessage() {
        final HttpResponse res = sendForResponse();
        final MimeHeaders headers = new MimeHeaders();
        for (Map.Entry<String, List<String>> entry : res.headers().toMultimap().entrySet()) {
            if (StringKit.isNotEmpty(entry.getKey())) {
                headers.setHeader(entry.getKey(), CollKit.get(entry.getValue(), 0));
            }
        }
        try {
            return this.factory.createMessage(headers, res.body().byteStream());
        } catch (IOException | SOAPException e) {
            throw new InstrumentException(e);
        } finally {
            IoKit.close(res.getResponse());
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
     * 发送请求，获取异步响应
     *
     * @return 响应对象
     */
    private HttpResponse sendForResponse() {
        try {
            return Httpz.post()
                    .url(this.url)
                    .headers(this.headers())
                    .body(getMsgStr(false)).build().execute();
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 获取请求的Content-Type，附加编码信息
     *
     * @return 请求的Content-Type
     */
    private String getXmlContentType() {
        switch (this.protocol) {
            case SOAP_1_1:
                return CONTENT_TYPE_SOAP11_TEXT_XML.concat(this.charset.toString());
            case SOAP_1_2:
                return CONTENT_TYPE_SOAP12_SOAP_XML.concat(this.charset.toString());
            default:
                throw new InstrumentException("Unsupported protocol: " + this.protocol);
        }
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
     * 根据name获取头信息
     * 根据RFC2616规范，header的name不区分大小写
     *
     * @param name Header名
     * @return Header值
     */
    public String header(String name) {
        final List<String> values = headerList(name);
        if (CollKit.isEmpty(values)) {
            return null;
        }
        return values.get(0);
    }

    /**
     * 根据name获取头信息列表
     *
     * @param name header名
     * @return header值
     */
    public List<String> headerList(String name) {
        if (StringKit.isBlank(name)) {
            return null;
        }

        final CaseInsensitiveMap<String, List<String>> headersIgnoreCase = new CaseInsensitiveMap<>();
        return headersIgnoreCase.get(name.trim());
    }

    /**
     * 设置一个header
     * 如果覆盖模式，则替换之前的值，否则加入到值列表中
     *
     * @param name       Header名
     * @param value      Header值
     * @param isOverride 是否覆盖已有值
     * @return T 本身
     */
    public T header(String name, String value, boolean isOverride) {
        if (null != name && null != value) {
            final String values = headers.get(name.trim());
            if (isOverride || StringKit.isEmpty(values)) {
                headers.put(name.trim(), values);
            }
        }
        return (T) this;
    }

    /**
     * 设置一个header
     * 覆盖模式，则替换之前的值
     *
     * @param name  Header名
     * @param value Header值
     * @return T 本身
     */
    public T header(String name, String value) {
        return header(name, value, true);
    }

    /**
     * 设置请求头
     *
     * @param headers    请求头
     * @param isOverride 是否覆盖已有头信息
     * @return this
     */
    public T headerMap(Map<String, String> headers, boolean isOverride) {
        if (CollKit.isEmpty(headers)) {
            return (T) this;
        }

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            this.header(entry.getKey(), StringKit.nullToEmpty(entry.getValue()), isOverride);
        }
        return (T) this;
    }

    /**
     * 设置请求头
     * 不覆盖原有请求头
     *
     * @param headers 请求头
     * @return this
     */
    public T header(Map<String, List<String>> headers) {
        return header(headers, false);
    }

    /**
     * 设置请求头
     *
     * @param headers    请求头
     * @param isOverride 是否覆盖已有头信息
     * @return this
     */
    public T header(Map<String, List<String>> headers, boolean isOverride) {
        if (CollKit.isEmpty(headers)) {
            return (T) this;
        }

        String name;
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            name = entry.getKey();
            for (String value : entry.getValue()) {
                this.header(name, StringKit.nullToEmpty(value), isOverride);
            }
        }
        return (T) this;
    }

    /**
     * 新增请求头
     * 不覆盖原有请求头
     *
     * @param headers 请求头
     * @return this
     */
    public T addHeaders(Map<String, String> headers) {
        if (CollKit.isEmpty(headers)) {
            return (T) this;
        }

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            this.header(entry.getKey(), StringKit.nullToEmpty(entry.getValue()), false);
        }
        return (T) this;
    }

    /**
     * 移除一个头信息
     *
     * @param name Header名
     * @return this
     */
    public T removeHeader(String name) {
        if (name != null) {
            headers.remove(name.trim());
        }
        return (T) this;
    }

    /**
     * 返回http版本
     *
     * @return String
     */
    public String getVersion() {
        return version;
    }

    /**
     * 设置http版本
     *
     * @param version Http版本，{@link #HTTP_1_0}，{@link #HTTP_1_1}
     * @return this
     */
    public T setVersion(String version) {
        this.version = version;
        return (T) this;
    }

    /**
     * 返回字符集
     *
     * @return 字符集
     */
    public String charset() {
        return charset.name();
    }

    /**
     * 设置字符集
     *
     * @param charset 字符集
     * @return T 自己
     */
    public T charset(String charset) {
        if (StringKit.isNotBlank(charset)) {
            charset(Charset.forName(charset));
        }
        return (T) this;
    }

    /**
     * 执行Webservice请求，即发送SOAP内容
     *
     * @param pretty 是否格式化
     * @return 返回结果
     */
    public String send(boolean pretty) {
        final HttpResponse response = sendForResponse();
        byte[] bytes = new byte[0];
        try {
            bytes = response.body().bytes();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String result = new String(bytes, org.aoju.bus.core.lang.Charset.UTF_8);
        Console.log(result);

        return pretty ? XmlKit.format(result) : result;
    }

    public static void main(String[] args) {
        SoapX client = SoapX.create("http://www.webxml.com.cn/WebServices/WeatherWebService.asmx")
                .setMethod("web:getSupportCity", "http://WebXml.com.cn/")
                .setCharset(org.aoju.bus.core.lang.Charset.GBK)
                .setParam("byProvinceName", "58367");

        Console.log(client.getMsgStr(true));
        Console.log(client.send(true));
    }

}
