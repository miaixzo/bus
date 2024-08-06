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
package org.miaixz.bus.core.xyz;

import java.beans.XMLEncoder;
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xml.DocumentBuilder;
import org.miaixz.bus.core.xml.XmlMapper;
import org.miaixz.bus.core.xml.XmlSaxReader;
import org.miaixz.bus.core.xml.XmlWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

/**
 * XML工具类 此工具使用w3c dom工具，不需要依赖第三方包。 工具类封装了XML文档的创建、读取、写出和部分XML操作
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class XmlKit {

    /**
     * 在XML中无效的字符 正则
     */
    public static final Pattern INVALID_PATTERN = Pattern.compile("[\\x00-\\x08\\x0b-\\x0c\\x0e-\\x1f]");
    /**
     * 在XML中注释的内容 正则
     */
    public static final Pattern COMMENT_PATTERN = Pattern.compile("(?s)<!--.+?-->");

    /**
     * 读取解析XML文件 如果给定内容以“&lt;”开头，表示这是一个XML内容，直接读取，否则按照路径处理 路径可以为相对路径，也可以是绝对路径，相对路径相对于ClassPath
     *
     * @param pathOrContent 内容或路径
     * @return XML文档对象
     */
    public static Document readXml(String pathOrContent) {
        pathOrContent = StringKit.trim(pathOrContent);
        if (StringKit.startWith(pathOrContent, '<')) {
            return parseXml(pathOrContent);
        }
        return readXml(FileKit.file(pathOrContent));
    }

    /**
     * 读取解析XML文件
     *
     * @param file XML文件
     * @return XML文档对象
     */
    public static Document readXml(final File file) {
        Assert.notNull(file, "Xml file is null !");
        if (!file.exists()) {
            throw new InternalException("File [{}] not a exist!", file.getAbsolutePath());
        }
        if (!file.isFile()) {
            throw new InternalException("[{}] not a file!", file.getAbsolutePath());
        }

        try (final BufferedInputStream in = FileKit.getInputStream(file)) {
            return readXml(in);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 读取解析XML文件 编码在XML中定义
     *
     * @param inputStream XML流
     * @return XML文档对象
     * @throws InternalException IO异常或转换异常
     */
    public static Document readXml(final InputStream inputStream) throws InternalException {
        return readXml(new InputSource(inputStream), true);
    }

    /**
     * 读取解析XML文件
     *
     * @param reader XML流
     * @return XML文档对象
     * @throws InternalException IO异常或转换异常
     */
    public static Document readXml(final Reader reader) throws InternalException {
        return readXml(new InputSource(reader), true);
    }

    /**
     * 读取解析XML文件 编码在XML中定义
     *
     * @param namespaceAware 是否打开命名空间支持
     * @param source         {@link InputSource}
     * @return XML文档对象
     */
    public static Document readXml(final InputSource source, final boolean namespaceAware) {
        final javax.xml.parsers.DocumentBuilder builder = DocumentBuilder.createDocumentBuilder(namespaceAware);
        try {
            return builder.parse(source);
        } catch (final Exception e) {
            throw new InternalException(e, "Parse XML from stream error!");
        }
    }

    /**
     * 将String类型的XML转换为XML文档
     *
     * @param xmlStr XML字符串
     * @return XML文档
     */
    public static Document parseXml(final String xmlStr) {
        if (StringKit.isBlank(xmlStr)) {
            throw new IllegalArgumentException("XML content string is blank !");
        }
        return readXml(StringKit.getReader(cleanInvalid(xmlStr)));
    }

    /**
     * 使用Sax方式读取指定的XML 如果用户传入的contentHandler为{@link DefaultHandler}，则其接口都会被处理
     *
     * @param file           XML源文件,使用后自动关闭
     * @param contentHandler XML流处理器，用于按照Element处理xml
     */
    public static void readBySax(final File file, final ContentHandler contentHandler) {
        InputStream in = null;
        try {
            in = FileKit.getInputStream(file);
            readBySax(new InputSource(in), contentHandler);
        } finally {
            IoKit.closeQuietly(in);
        }
    }

    /**
     * 使用Sax方式读取指定的XML 如果用户传入的contentHandler为{@link DefaultHandler}，则其接口都会被处理
     *
     * @param reader         XML源Reader,使用后自动关闭
     * @param contentHandler XML流处理器，用于按照Element处理xml
     */
    public static void readBySax(final Reader reader, final ContentHandler contentHandler) {
        try {
            readBySax(new InputSource(reader), contentHandler);
        } finally {
            IoKit.closeQuietly(reader);
        }
    }

    /**
     * 使用Sax方式读取指定的XML 如果用户传入的contentHandler为{@link DefaultHandler}，则其接口都会被处理
     *
     * @param source         XML源流,使用后自动关闭
     * @param contentHandler XML流处理器，用于按照Element处理xml
     */
    public static void readBySax(final InputStream source, final ContentHandler contentHandler) {
        try {
            readBySax(new InputSource(source), contentHandler);
        } finally {
            IoKit.closeQuietly(source);
        }
    }

    /**
     * 使用Sax方式读取指定的XML 如果用户传入的contentHandler为{@link DefaultHandler}，则其接口都会被处理
     *
     * @param source         XML源，可以是文件、流、路径等
     * @param contentHandler XML流处理器，用于按照Element处理xml
     */
    public static void readBySax(final InputSource source, final ContentHandler contentHandler) {
        XmlSaxReader.of(source).read(contentHandler);
    }

    /**
     * 将XML文档转换为String 字符编码使用XML文档中的编码，获取不到则使用UTF-8 默认非格式化输出，若想格式化请使用{@link #format(Document)}
     *
     * @param doc XML文档
     * @return XML字符串
     */
    public static String toString(final Node doc) {
        return toString(doc, false);
    }

    /**
     * 将XML文档转换为String 字符编码使用XML文档中的编码，获取不到则使用UTF-8
     *
     * @param doc      XML文档
     * @param isPretty 是否格式化输出
     * @return XML字符串
     */
    public static String toString(final Node doc, final boolean isPretty) {
        return toString(doc, Charset.UTF_8, isPretty);
    }

    /**
     * 将XML文档转换为String 字符编码使用XML文档中的编码，获取不到则使用UTF-8
     *
     * @param doc      XML文档
     * @param charset  编码
     * @param isPretty 是否格式化输出
     * @return XML字符串
     */
    public static String toString(final Node doc, final java.nio.charset.Charset charset, final boolean isPretty) {
        return toString(doc, charset, isPretty, false);
    }

    /**
     * 将XML文档转换为String 字符编码使用XML文档中的编码，获取不到则使用UTF-8 当{@code omitXmlDeclaration}为{@code true}时，表示忽略xml Declaration，即删掉
     * 
     * <pre>{@code
     *     <?xml version="1.0" encoding="utf-8"?>
     * }</pre>
     *
     * @param doc                XML文档
     * @param charset            编码
     * @param isPretty           是否格式化输出
     * @param omitXmlDeclaration 是否忽略 xml Declaration
     * @return XML字符串
     */
    public static String toString(final Node doc, final java.nio.charset.Charset charset, final boolean isPretty,
            final boolean omitXmlDeclaration) {
        final StringWriter writer = StringKit.getWriter();
        write(doc, writer, charset, isPretty ? Normal._2 : 0, omitXmlDeclaration);
        return writer.toString();
    }

    /**
     * 格式化XML输出
     *
     * @param doc {@link Document} XML文档
     * @return 格式化后的XML字符串
     */
    public static String format(final Document doc) {
        return toString(doc, true);
    }

    /**
     * 格式化XML输出
     *
     * @param xmlStr XML字符串
     * @return 格式化后的XML字符串
     */
    public static String format(final String xmlStr) {
        return format(parseXml(xmlStr));
    }

    /**
     * 将XML文档写入到文件 使用Document中的编码
     *
     * @param doc     XML文档
     * @param file    文件
     * @param charset 编码
     */
    public static void write(final Document doc, final File file, final java.nio.charset.Charset charset) {
        XmlWriter.of(doc).setCharset(charset).setIndent(Normal._2).setOmitXmlDeclaration(false).write(file);
    }

    /**
     * 将XML文档写出
     *
     * @param node    {@link Node} XML文档节点或文档本身
     * @param writer  写出的Writer，Writer决定了输出XML的编码
     * @param charset 编码
     * @param indent  格式化输出中缩进量，小于1表示不格式化输出
     */
    public static void write(final Node node, final Writer writer, final java.nio.charset.Charset charset,
            final int indent) {
        write(node, writer, charset, indent, false);
    }

    /**
     * 将XML文档写出 当{@code omitXmlDeclaration}为{@code true}时，表示忽略xml Declaration，即删掉
     * 
     * <pre>{@code
     *     <?xml version="1.0" encoding="utf-8"?>
     * }</pre>
     *
     * @param node               {@link Node} XML文档节点或文档本身
     * @param writer             写出的Writer，Writer决定了输出XML的编码
     * @param charset            编码
     * @param indent             格式化输出中缩进量，小于1表示不格式化输出
     * @param omitXmlDeclaration 是否忽略 xml Declaration
     */
    public static void write(final Node node, final Writer writer, final java.nio.charset.Charset charset,
            final int indent, final boolean omitXmlDeclaration) {
        XmlWriter.of(node).setCharset(charset).setIndent(indent).setOmitXmlDeclaration(omitXmlDeclaration)
                .write(writer);
    }

    /**
     * 将XML文档写出
     *
     * @param node    {@link Node} XML文档节点或文档本身
     * @param out     写出的Writer，Writer决定了输出XML的编码
     * @param charset 编码
     * @param indent  格式化输出中缩进量，小于1表示不格式化输出
     */
    public static void write(final Node node, final OutputStream out, final java.nio.charset.Charset charset,
            final int indent) {
        write(node, out, charset, indent, false);
    }

    /**
     * 将XML文档写出 当{@code omitXmlDeclaration}为{@code true}时，表示忽略xml Declaration，即删掉
     * 
     * <pre>{@code
     *     <?xml version="1.0" encoding="utf-8"?>
     * }</pre>
     *
     * @param node               {@link Node} XML文档节点或文档本身
     * @param out                写出的Writer，Writer决定了输出XML的编码
     * @param charset            编码
     * @param indent             格式化输出中缩进量，小于1表示不格式化输出
     * @param omitXmlDeclaration 是否忽略 xml Declaration
     */
    public static void write(final Node node, final OutputStream out, final java.nio.charset.Charset charset,
            final int indent, final boolean omitXmlDeclaration) {
        XmlWriter.of(node).setCharset(charset).setIndent(indent).setOmitXmlDeclaration(omitXmlDeclaration).write(out);
    }

    /**
     * 创建XML文档 创建的XML默认是utf8编码，修改编码的过程是在toStr和toFile方法里，即XML在转为文本的时候才定义编码
     *
     * @return XML文档
     */
    public static Document createXml() {
        return DocumentBuilder.createDocumentBuilder(true).newDocument();
    }

    /**
     * 创建XML文档 创建的XML默认是utf8编码，修改编码的过程是在toStr和toFile方法里，即XML在转为文本的时候才定义编码
     *
     * @param rootElementName 根节点名称
     * @return XML文档
     */
    public static Document createXml(final String rootElementName) {
        return createXml(rootElementName, null);
    }

    /**
     * 创建XML文档 创建的XML默认是utf8编码，修改编码的过程是在toStr和toFile方法里，即XML在转为文本的时候才定义编码
     *
     * @param rootElementName 根节点名称
     * @param namespace       命名空间，无则传null
     * @return XML文档
     */
    public static Document createXml(final String rootElementName, final String namespace) {
        final Document doc = createXml();
        doc.appendChild(null == namespace ? doc.createElement(rootElementName)
                : doc.createElementNS(namespace, rootElementName));
        return doc;
    }

    /**
     * 获得XML文档根节点
     *
     * @param doc {@link Document}
     * @return 根节点
     * @see Document#getDocumentElement()
     */
    public static Element getRootElement(final Document doc) {
        return (null == doc) ? null : doc.getDocumentElement();
    }

    /**
     * 获取节点所在的Document
     *
     * @param node 节点
     * @return {@link Document}
     */
    public static Document getOwnerDocument(final Node node) {
        return (node instanceof Document) ? (Document) node : node.getOwnerDocument();
    }

    /**
     * 去除XML文本中的无效字符
     *
     * @param xmlContent XML文本
     * @return 当传入为null时返回null
     */
    public static String cleanInvalid(final String xmlContent) {
        if (xmlContent == null) {
            return null;
        }

        return PatternKit.replaceAll(xmlContent, INVALID_PATTERN, Normal.EMPTY);
    }

    /**
     * 去除XML文本中的注释内容
     *
     * @param xmlContent XML文本
     * @return 当传入为null时返回null
     */
    public static String cleanComment(final String xmlContent) {
        if (xmlContent == null) {
            return null;
        }
        return PatternKit.replaceAll(xmlContent, COMMENT_PATTERN, Normal.EMPTY);
    }

    /**
     * 根据节点名获得子节点列表
     *
     * @param element 节点
     * @param tagName 节点名，如果节点名为空（null或blank），返回所有子节点
     * @return 节点列表
     */
    public static List<Element> getElements(final Element element, final String tagName) {
        final NodeList nodeList = StringKit.isBlank(tagName) ? element.getChildNodes()
                : element.getElementsByTagName(tagName);
        return transElements(element, nodeList);
    }

    /**
     * 根据节点名获得第一个子节点
     *
     * @param element 节点
     * @param tagName 节点名
     * @return 节点
     */
    public static Element getElement(final Element element, final String tagName) {
        final NodeList nodeList = element.getElementsByTagName(tagName);
        final int length = nodeList.getLength();
        if (length < 1) {
            return null;
        }
        for (int i = 0; i < length; i++) {
            final Element childEle = (Element) nodeList.item(i);
            if (childEle == null || childEle.getParentNode() == element) {
                return childEle;
            }
        }
        return null;
    }

    /**
     * 根据节点名获得第一个子节点
     *
     * @param element 节点
     * @param tagName 节点名
     * @return 节点中的值
     */
    public static String elementText(final Element element, final String tagName) {
        final Element child = getElement(element, tagName);
        return child == null ? null : child.getTextContent();
    }

    /**
     * 根据节点名获得第一个子节点
     *
     * @param element      节点
     * @param tagName      节点名
     * @param defaultValue 默认值
     * @return 节点中的值
     */
    public static String elementText(final Element element, final String tagName, final String defaultValue) {
        final Element child = getElement(element, tagName);
        return child == null ? defaultValue : child.getTextContent();
    }

    /**
     * 将NodeList转换为Element列表
     *
     * @param nodeList NodeList
     * @return Element列表
     */
    public static List<Element> transElements(final NodeList nodeList) {
        return transElements(null, nodeList);
    }

    /**
     * 将NodeList转换为Element列表 非Element节点将被忽略
     *
     * @param parentEle 父节点，如果指定将返回此节点的所有直接子节点，null返回所有就节点
     * @param nodeList  NodeList
     * @return Element列表
     */
    public static List<Element> transElements(final Element parentEle, final NodeList nodeList) {
        final int length = nodeList.getLength();
        final ArrayList<Element> elements = new ArrayList<>(length);
        Node node;
        Element element;
        for (int i = 0; i < length; i++) {
            node = nodeList.item(i);
            if (Node.ELEMENT_NODE == node.getNodeType()) {
                element = (Element) nodeList.item(i);
                if (parentEle == null || element.getParentNode() == parentEle) {
                    elements.add(element);
                }
            }
        }

        return elements;
    }

    /**
     * 将可序列化的对象转换为XML写入文件，已经存在的文件将被覆盖 Writes serializable object to a XML file. Existing file will be overwritten
     *
     * @param dest 目标文件
     * @param bean 对象
     */
    public static void writeObjectAsXml(final File dest, final Object bean) {
        XMLEncoder xmlenc = null;
        try {
            xmlenc = new XMLEncoder(FileKit.getOutputStream(dest));
            xmlenc.writeObject(bean);
        } finally {
            // 关闭XMLEncoder会相应关闭OutputStream
            IoKit.closeQuietly(xmlenc);
        }
    }

    /**
     * XML转Java Bean 如果XML根节点只有一个，且节点名和Bean的名称一致，则直接转换子节点
     *
     * @param <T>       bean类型
     * @param node      XML节点
     * @param beanClass bean类
     * @return beans
     */
    public static <T> T xmlToBean(final Node node, final Class<T> beanClass) {
        return XmlMapper.of(node).toBean(beanClass);
    }

    /**
     * XML格式字符串转换为Map
     *
     * @param xmlStr XML字符串
     * @return XML数据转换后的Map
     */
    public static Map<String, Object> xmlToMap(final String xmlStr) {
        return xmlToMap(xmlStr, new LinkedHashMap<>());
    }

    /**
     * XML格式字符串转换为Map 只支持第一级别的XML，不支持多级XML
     *
     * @param xmlStr XML字符串
     * @param result 结果Map类型
     * @return XML数据转换后的Map
     */
    public static Map<String, Object> xmlToMap(final String xmlStr, final Map<String, Object> result) {
        final Document doc = parseXml(xmlStr);
        final Element root = getRootElement(doc);
        root.normalize();

        return xmlToMap(root, result);
    }

    /**
     * XML格式字符串转换为Map
     *
     * @param node XML节点
     * @return XML数据转换后的Map
     */
    public static Map<String, Object> xmlToMap(final Node node) {
        return xmlToMap(node, new LinkedHashMap<>());
    }

    /**
     * XML节点转换为Map
     *
     * @param node   XML节点
     * @param result 结果Map类型
     * @return XML数据转换后的Map
     */
    public static Map<String, Object> xmlToMap(final Node node, final Map<String, Object> result) {
        XmlMapper.of(node).toMap(result);
        return result;
    }

    /**
     * 将Map转换为XML格式的字符串
     *
     * @param data Map类型数据
     * @return XML格式的字符串
     */
    public static String mapToXmlString(final Map<?, ?> data) {
        return toString(mapToXml(data, "xml"));
    }

    /**
     * 将Map转换为XML格式的字符串 当{@code omitXmlDeclaration}为{@code true}时，表示忽略xml Declaration，即
     * 
     * <pre>{@code
     *     <?xml version="1.0" encoding="utf-8"?>
     * }</pre>
     *
     * @param data               Map类型数据
     * @param omitXmlDeclaration 是否忽略 xml Declaration
     * @return XML格式的字符串
     */
    public static String mapToXmlString(final Map<?, ?> data, final boolean omitXmlDeclaration) {
        return toString(mapToXml(data, "xml"), Charset.UTF_8, false, omitXmlDeclaration);
    }

    /**
     * 将Map转换为XML格式的字符串
     *
     * @param data     Map类型数据
     * @param rootName 根节点名
     * @return XML格式的字符串
     */
    public static String mapToXmlString(final Map<?, ?> data, final String rootName) {
        return toString(mapToXml(data, rootName));
    }

    /**
     * 将Map转换为XML格式的字符串
     *
     * @param data      Map类型数据
     * @param rootName  根节点名
     * @param namespace 命名空间，可以为null
     * @return XML格式的字符串
     */
    public static String mapToXmlString(final Map<?, ?> data, final String rootName, final String namespace) {
        return toString(mapToXml(data, rootName, namespace));
    }

    /**
     * 将Map转换为XML格式的字符串 当{@code omitXmlDeclaration}为{@code true}时，表示忽略xml Declaration，即删掉
     * 
     * <pre>{@code
     *     <?xml version="1.0" encoding="utf-8"?>
     * }</pre>
     *
     * @param data               Map类型数据
     * @param rootName           根节点名
     * @param namespace          命名空间，可以为null
     * @param omitXmlDeclaration 是否忽略 xml Declaration
     * @return XML格式的字符串
     */
    public static String mapToXmlString(final Map<?, ?> data, final String rootName, final String namespace,
            final boolean omitXmlDeclaration) {
        return toString(mapToXml(data, rootName, namespace), Charset.UTF_8, false, omitXmlDeclaration);
    }

    /**
     * 将Map转换为XML格式的字符串 当{@code omitXmlDeclaration}为{@code true}时，表示忽略xml Declaration，即删掉
     * 
     * <pre>{@code
     *     <?xml version="1.0" encoding="utf-8"?>
     * }</pre>
     *
     * @param data               Map类型数据
     * @param rootName           根节点名
     * @param namespace          命名空间，可以为null
     * @param isPretty           是否格式化输出
     * @param omitXmlDeclaration 是否忽略 xml Declaration
     * @return XML格式的字符串
     */
    public static String mapToXmlString(final Map<?, ?> data, final String rootName, final String namespace,
            final boolean isPretty, final boolean omitXmlDeclaration) {
        return toString(mapToXml(data, rootName, namespace), Charset.UTF_8, isPretty, omitXmlDeclaration);
    }

    /**
     * 将Map转换为XML格式的字符串 当{@code omitXmlDeclaration}为{@code true}时，表示忽略xml Declaration，即删掉
     * 
     * <pre>{@code
     *     <?xml version="1.0" encoding="utf-8"?>
     * }</pre>
     *
     * @param data               Map类型数据
     * @param rootName           根节点名
     * @param namespace          命名空间，可以为null
     * @param charset            编码
     * @param isPretty           是否格式化输出
     * @param omitXmlDeclaration 是否忽略 xml Declaration
     * @return XML格式的字符串
     */
    public static String mapToXmlString(final Map<?, ?> data, final String rootName, final String namespace,
            final java.nio.charset.Charset charset, final boolean isPretty, final boolean omitXmlDeclaration) {
        return toString(mapToXml(data, rootName, namespace), charset, isPretty, omitXmlDeclaration);
    }

    /**
     * 将Map转换为XML
     *
     * @param data     Map类型数据
     * @param rootName 根节点名
     * @return XML
     */
    public static Document mapToXml(final Map<?, ?> data, final String rootName) {
        return mapToXml(data, rootName, null);
    }

    /**
     * 将Map转换为XML
     *
     * @param data      Map类型数据
     * @param rootName  根节点名
     * @param namespace 命名空间，可以为null
     * @return XML
     */
    public static Document mapToXml(final Map<?, ?> data, final String rootName, final String namespace) {
        final Document doc = createXml();
        final Element root = appendChild(doc, rootName, namespace);

        appendMap(doc, root, data);
        return doc;
    }

    /**
     * 将Bean转换为XML
     *
     * @param bean Bean对象
     * @return XML
     */
    public static Document beanToXml(final Object bean) {
        return beanToXml(bean, null);
    }

    /**
     * 将Bean转换为XML
     *
     * @param bean      Bean对象
     * @param namespace 命名空间，可以为null
     * @return XML
     */
    public static Document beanToXml(final Object bean, final String namespace) {
        return beanToXml(bean, namespace, false);
    }

    /**
     * 将Bean转换为XML
     *
     * @param bean       Bean对象
     * @param namespace  命名空间，可以为null
     * @param ignoreNull 忽略值为{@code null}的属性
     * @return XML
     */
    public static Document beanToXml(final Object bean, final String namespace, final boolean ignoreNull) {
        if (null == bean) {
            return null;
        }
        return mapToXml(BeanKit.beanToMap(bean, false, ignoreNull), bean.getClass().getSimpleName(), namespace);
    }

    /**
     * 给定节点是否为{@link Element} 类型节点
     *
     * @param node 节点
     * @return 是否为 {@link Element} 类型节点
     */
    public static boolean isElement(final Node node) {
        return (null != node) && Node.ELEMENT_NODE == node.getNodeType();
    }

    /**
     * 在已有节点上创建子节点
     *
     * @param node    节点
     * @param tagName 标签名
     * @return 子节点
     */
    public static Element appendChild(final Node node, final String tagName) {
        return appendChild(node, tagName, null);
    }

    /**
     * 在已有节点上创建子节点
     *
     * @param node      节点
     * @param tagName   标签名
     * @param namespace 命名空间，无传null
     * @return 子节点
     */
    public static Element appendChild(final Node node, final String tagName, final String namespace) {
        final Document doc = getOwnerDocument(node);
        final Element child = (null == namespace) ? doc.createElement(tagName)
                : doc.createElementNS(namespace, tagName);
        node.appendChild(child);
        return child;
    }

    /**
     * 创建文本子节点
     *
     * @param node 节点
     * @param text 文本
     * @return 子节点
     */
    public static Node appendText(final Node node, final CharSequence text) {
        return appendText(getOwnerDocument(node), node, text);
    }

    /**
     * 追加数据子节点，可以是Map、集合、文本
     *
     * @param node 节点
     * @param data 数据
     */
    public static void append(final Node node, final Object data) {
        append(getOwnerDocument(node), node, data);
    }

    /**
     * 追加数据子节点，可以是Map、集合、文本
     *
     * @param doc  {@link Document}
     * @param node 节点
     * @param data 数据
     */
    private static void append(final Document doc, final Node node, final Object data) {
        if (data instanceof Map) {
            // 如果值依旧为map，递归继续
            appendMap(doc, node, (Map) data);
        } else if (data instanceof Iterator) {
            // 如果值依旧为map，递归继续
            appendIterator(doc, node, (Iterator) data);
        } else if (data instanceof Iterable) {
            // 如果值依旧为map，递归继续
            appendIterator(doc, node, ((Iterable) data).iterator());
        } else {
            appendText(doc, node, data.toString());
        }
    }

    /**
     * 追加Map数据子节点
     *
     * @param doc  {@link Document}
     * @param node 当前节点
     * @param data Map类型数据
     */
    private static void appendMap(final Document doc, final Node node, final Map data) {
        data.forEach((key, value) -> {
            if (null != key) {
                final Element child = appendChild(node, key.toString());
                if (null != value) {
                    append(doc, child, value);
                }
            }
        });
    }

    /**
     * 追加集合节点
     *
     * @param doc  {@link Document}
     * @param node 节点
     * @param data 数据
     */
    private static void appendIterator(final Document doc, final Node node, final Iterator data) {
        final Node parentNode = node.getParentNode();
        boolean isFirst = true;
        Object eleData;
        while (data.hasNext()) {
            eleData = data.next();
            if (isFirst) {
                append(doc, node, eleData);
                isFirst = false;
            } else {
                final Node cloneNode = node.cloneNode(false);
                parentNode.appendChild(cloneNode);
                append(doc, cloneNode, eleData);
            }
        }
    }

    /**
     * 追加文本节点
     *
     * @param doc  {@link Document}
     * @param node 节点
     * @param text 文本内容
     * @return 增加的子节点，即Text节点
     */
    private static Node appendText(final Document doc, final Node node, final CharSequence text) {
        return node.appendChild(doc.createTextNode(StringKit.toStringOrEmpty(text)));
    }

}
