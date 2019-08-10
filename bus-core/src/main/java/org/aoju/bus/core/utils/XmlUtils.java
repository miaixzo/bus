/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
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
package org.aoju.bus.core.utils;

import org.aoju.bus.core.consts.RegEx;
import org.aoju.bus.core.lang.exception.CommonException;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.bind.*;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.beans.XMLDecoder;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * XML工具类<br>
 * 此工具使用w3c dom工具，不需要依赖第三方包。<br>
 * 工具类封装了XML文档的创建、读取、写出和部分XML操作
 *
 * @author Kimi Liu
 * @version 3.0.0
 * @since JDK 1.8
 */
public class XmlUtils {

    /**
     * 读取解析XML文件<br>
     * 编码在XML中定义
     *
     * @param inputStream XML流
     * @return XML文档对象
     * @throws CommonException IO异常或转换异常
     * @since 3.0.9
     */
    public static Document readXML(InputStream inputStream) throws CommonException {
        return readXML(new InputSource(inputStream));
    }

    /**
     * 读取解析XML文件
     *
     * @param reader XML流
     * @return XML文档对象
     * @throws CommonException IO异常或转换异常
     * @since 3.0.9
     */
    public static Document readXML(Reader reader) throws CommonException {
        return readXML(new InputSource(reader));
    }

    /**
     * 读取解析XML文件<br>
     * 编码在XML中定义
     *
     * @param source {@link InputSource}
     * @return XML文档对象
     * @since 3.0.9
     */
    public static Document readXML(InputSource source) {
        final DocumentBuilder builder = createDocumentBuilder();
        try {
            return builder.parse(source);
        } catch (Exception e) {
            throw new CommonException("Parse XML from stream error!");
        }
    }

    /**
     * 从XML中读取对象 Reads serialized object from the XML file.
     *
     * @param <T>    对象类型
     * @param source {@link InputSource}
     * @return 对象
     * @since 3.2.0
     */
    public static <T> T readObjectFromXml(InputSource source) {
        Object result = null;
        XMLDecoder xmldec = null;
        try {
            xmldec = new XMLDecoder(source);
            result = xmldec.readObject();
        } finally {
            xmldec.close();
        }
        return (T) result;
    }

    /**
     * 创建XML文档<br>
     * 创建的XML默认是utf8编码，修改编码的过程是在toStr和toFile方法里，既XML在转为文本的时候才定义编码
     *
     * @return XML文档
     * @since 4.0.8
     */
    public static Document createXml() {
        return createDocumentBuilder().newDocument();
    }

    /**
     * 创建 DocumentBuilder
     *
     * @return DocumentBuilder
     * @since 4.1.2
     */
    public static DocumentBuilder createDocumentBuilder() {
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        disableXXE(dbf);
        DocumentBuilder builder = null;
        try {
            builder = dbf.newDocumentBuilder();
        } catch (Exception e) {
            throw new CommonException("Create xml document error!");
        }
        return builder;
    }

    /**
     * 创建XML文档<br>
     * 创建的XML默认是utf8编码，修改编码的过程是在toStr和toFile方法里，既XML在转为文本的时候才定义编码
     *
     * @param rootElementName 根节点名称
     * @return XML文档
     */
    public static Document createXml(String rootElementName) {
        final Document doc = createXml();
        doc.appendChild(doc.createElement(rootElementName));

        return doc;
    }

    /**
     * 获得XML文档根节点
     *
     * @param doc {@link Document}
     * @return 根节点
     * @see Document#getDocumentElement()
     * @since 3.0.8
     */
    public static Element getRootElement(Document doc) {
        return (null == doc) ? null : doc.getDocumentElement();
    }

    /**
     * 去除XML文本中的无效字符
     *
     * @param xmlContent XML文本
     * @return 当传入为null时返回null
     */
    public static String cleanInvalid(String xmlContent) {
        if (xmlContent == null) {
            return null;
        }
        return xmlContent.replaceAll(RegEx.INVALID_REGEX, "");
    }

    /**
     * 根据节点名获得第一个子节点
     *
     * @param element 节点
     * @param tagName 节点名
     * @return 节点
     */
    public static Element getElement(Element element, String tagName) {
        final NodeList nodeList = element.getElementsByTagName(tagName);
        if (nodeList == null || nodeList.getLength() < 1) {
            return null;
        }
        int length = nodeList.getLength();
        for (int i = 0; i < length; i++) {
            Element childEle = (Element) nodeList.item(i);
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
    public static String elementText(Element element, String tagName) {
        Element child = getElement(element, tagName);
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
    public static String elementText(Element element, String tagName, String defaultValue) {
        Element child = getElement(element, tagName);
        return child == null ? defaultValue : child.getTextContent();
    }

    /**
     * 将NodeList转换为Element列表
     *
     * @param nodeList NodeList
     * @return Element列表
     */
    public static List<Element> transElements(NodeList nodeList) {
        return transElements(null, nodeList);
    }

    /**
     * 将NodeList转换为Element列表<br>
     * 非Element节点将被忽略
     *
     * @param parentEle 父节点，如果指定将返回此节点的所有直接子节点，nul返回所有就节点
     * @param nodeList  NodeList
     * @return Element列表
     */
    public static List<Element> transElements(Element parentEle, NodeList nodeList) {
        int length = nodeList.getLength();
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
     * 创建XPath<br>
     * Xpath相关文章：https://www.ibm.com/developerworks/cn/xml/x-javaxpathapi.html
     *
     * @return {@link XPath}
     * @since 3.2.0
     */
    public static XPath createXPath() {
        return XPathFactory.newInstance().newXPath();
    }

    /**
     * 通过XPath方式读取XML节点等信息<br>
     * Xpath相关文章：https://www.ibm.com/developerworks/cn/xml/x-javaxpathapi.html
     *
     * @param expression XPath表达式
     * @param source     资源，可以是Docunent、Node节点等
     * @return 匹配返回类型的值
     * @since 4.0.9
     */
    public static Element getElementByXPath(String expression, Object source) {
        return (Element) getNodeByXPath(expression, source);
    }

    /**
     * 通过XPath方式读取XML的NodeList<br>
     * Xpath相关文章：https://www.ibm.com/developerworks/cn/xml/x-javaxpathapi.html
     *
     * @param expression XPath表达式
     * @param source     资源，可以是Docunent、Node节点等
     * @return NodeList
     * @since 4.0.9
     */
    public static NodeList getNodeListByXPath(String expression, Object source) {
        return (NodeList) getByXPath(expression, source, XPathConstants.NODESET);
    }

    /**
     * 通过XPath方式读取XML节点等信息<br>
     * Xpath相关文章：https://www.ibm.com/developerworks/cn/xml/x-javaxpathapi.html
     *
     * @param expression XPath表达式
     * @param source     资源，可以是Docunent、Node节点等
     * @return 匹配返回类型的值
     * @since 4.0.9
     */
    public static Node getNodeByXPath(String expression, Object source) {
        return (Node) getByXPath(expression, source, XPathConstants.NODE);
    }

    /**
     * 通过XPath方式读取XML节点等信息<br>
     * Xpath相关文章：https://www.ibm.com/developerworks/cn/xml/x-javaxpathapi.html
     *
     * @param expression XPath表达式
     * @param source     资源，可以是Docunent、Node节点等
     * @param returnType 返回类型，{@link XPathConstants}
     * @return 匹配返回类型的值
     * @since 3.2.0
     */
    public static Object getByXPath(String expression, Object source, QName returnType) {
        final XPath xPath = createXPath();
        try {
            if (source instanceof InputSource) {
                return xPath.evaluate(expression, (InputSource) source, returnType);
            } else {
                return xPath.evaluate(expression, source, returnType);
            }
        } catch (XPathExpressionException e) {
            throw new CommonException(e);
        }
    }

    /**
     * 转义XML特殊字符:
     *
     * <pre>
     * &amp; (ampersand) 替换为 &amp;amp;
     * &lt; (小于) 替换为 &amp;lt;
     * &gt; (大于) 替换为 &amp;gt;
     * &quot; (双引号) 替换为 &amp;quot;
     * </pre>
     *
     * @param string 被替换的字符串
     * @return 替换后的字符串
     * @since 4.0.8
     */
    public static String escape(String string) {
        final StringBuilder sb = new StringBuilder(string.length());
        for (int i = 0, length = string.length(); i < length; i++) {
            char c = string.charAt(i);
            switch (c) {
                case '&':
                    sb.append("&amp;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;
                case '\'':
                    sb.append("&apos;");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * XML格式字符串转换为Map
     *
     * @param node XML节点
     * @return XML数据转换后的Map
     * @since 4.0.8
     */
    public static Map<String, Object> xmlToMap(Node node) {
        return xmlToMap(node, new HashMap<>());
    }

    /**
     * XML节点转换为Map
     *
     * @param node   XML节点
     * @param result 结果Map类型
     * @return XML数据转换后的Map
     * @since 4.0.8
     */
    public static Map<String, Object> xmlToMap(Node node, Map<String, Object> result) {
        if (null == result) {
            result = new HashMap<>();
        }

        final NodeList nodeList = node.getChildNodes();
        final int length = nodeList.getLength();
        Node childNode;
        Element childEle;
        for (int i = 0; i < length; ++i) {
            childNode = nodeList.item(i);
            if (isElement(childNode)) {
                childEle = (Element) childNode;
                result.put(childEle.getNodeName(), childEle.getTextContent());
            }
        }
        return result;
    }

    /**
     * 将Map转换为XML
     *
     * @param data     Map类型数据
     * @param rootName 节点
     * @return XML
     * @since 4.0.9
     */
    public static Document mapToXml(Map<?, ?> data, String rootName) {
        final Document doc = createXml();
        final Element root = appendChild(doc, rootName);

        mapToXml(doc, root, data);
        return doc;
    }

    /**
     * 给定节点是否为{@link Element} 类型节点
     *
     * @param node 节点
     * @return 是否为{@link Element} 类型节点
     * @since 4.0.8
     */
    public static boolean isElement(Node node) {
        return (null != node) && Node.ELEMENT_NODE == node.getNodeType();
    }

    /**
     * 在已有节点上创建子节点
     *
     * @param node    节点
     * @param tagName 标签名
     * @return 子节点
     * @since 4.0.9
     */
    public static Element appendChild(Node node, String tagName) {
        Document doc = (node instanceof Document) ? (Document) node : node.getOwnerDocument();
        Element child = doc.createElement(tagName);
        node.appendChild(child);
        return child;
    }

    /**
     * 将Map转换为XML格式的字符串
     *
     * @param doc     {@link Document}
     * @param element 节点
     * @param data    Map类型数据
     * @since 4.0.8
     */
    private static void mapToXml(Document doc, Element element, Map<?, ?> data) {
        Element filedEle;
        Object value;
        for (Entry<?, ?> entry : data.entrySet()) {
            filedEle = doc.createElement(entry.getKey().toString());
            element.appendChild(filedEle);
            value = entry.getValue();
            if (value instanceof Map) {
                mapToXml(doc, filedEle, (Map<?, ?>) value);
                element.appendChild(filedEle);
            } else {
                filedEle.appendChild(doc.createTextNode(value.toString()));
            }
        }
    }

    /**
     * 关闭XXE，避免漏洞攻击<br>
     * see: https://www.owasp.org/index.php/XML_External_Entity_(XXE)_Prevention_Cheat_Sheet#JAXP_DocumentBuilderFactory.2C_SAXParserFactory_and_DOM4J
     *
     * @param dbf DocumentBuilderFactory
     * @return DocumentBuilderFactory
     */
    private static DocumentBuilderFactory disableXXE(DocumentBuilderFactory dbf) {
        String feature;
        try {
            // This is the PRIMARY defense. If DTDs (doctypes) are disallowed, almost all XML entity attacks are prevented
            // Xerces 2 only - http://xerces.apache.org/xerces2-j/features.html#disallow-doctype-decl
            feature = "http://apache.org/xml/features/disallow-doctype-decl";
            dbf.setFeature(feature, true);
            // If you can't completely disable DTDs, then at least do the following:
            // Xerces 1 - http://xerces.apache.org/xerces-j/features.html#external-general-entities
            // Xerces 2 - http://xerces.apache.org/xerces2-j/features.html#external-general-entities
            // JDK7+ - http://xml.org/sax/features/external-general-entities
            feature = "http://xml.org/sax/features/external-general-entities";
            dbf.setFeature(feature, false);
            // Xerces 1 - http://xerces.apache.org/xerces-j/features.html#external-parameter-entities
            // Xerces 2 - http://xerces.apache.org/xerces2-j/features.html#external-parameter-entities
            // JDK7+ - http://xml.org/sax/features/external-parameter-entities
            feature = "http://xml.org/sax/features/external-parameter-entities";
            dbf.setFeature(feature, false);
            // Disable external DTDs as well
            feature = "http://apache.org/xml/features/nonvalidating/load-external-dtd";
            dbf.setFeature(feature, false);
            // and these as well, per Timothy Morgan's 2014 paper: "XML Schema, DTD, and Entity Attacks"
            dbf.setXIncludeAware(false);
            dbf.setExpandEntityReferences(false);
        } catch (ParserConfigurationException e) {
            // ignore
        }
        return dbf;
    }

    /**
     * 将字符串装换为对象
     *
     * @param text     字符串
     * @param javaBean 对象
     * @return object对象
     */
    public static Object parseXmlToBean(String text, Object javaBean) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(javaBean.getClass());
            Unmarshaller um = jaxbContext.createUnmarshaller();
            return um.unmarshal(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)));
        } catch (JAXBException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 将字符串装换为对象
     *
     * @param text     字符串
     * @param javaBean 对象
     * @param charset  编码
     * @return object对象
     */
    public static Object parseXmlToBean(String text, Object javaBean, String charset) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(javaBean.getClass());
            Unmarshaller um = jaxbContext.createUnmarshaller();
            return um.unmarshal(new ByteArrayInputStream(text.getBytes(charset)));
        } catch (JAXBException | UnsupportedEncodingException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 将String类型的xml转换成对象
     *
     * @param xml   字符串
     * @param clazz 对象
     * @return object对象
     */
    public static Object convertXmlStrToObject(String xml, Class clazz) {
        try {
            JAXBContext context = JAXBContext.newInstance(clazz);
            // 进行将Xml转成对象的核心接口
            Unmarshaller unmarshaller = context.createUnmarshaller();
            StringReader sr = new StringReader(xml);
            return unmarshaller.unmarshal(sr);
        } catch (JAXBException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 将xml对象根据定义的节点进行拆分,并实现List对象转换
     *
     * @param <T>      对象
     * @param xml      字符串
     * @param rel      拆分节点
     * @param clazz    对象信息
     * @param isSelect 是否拆分
     * @return list对象
     */
    public static <T> List<T> parseXmlToBeanList(String xml, String rel, Class<T> clazz, boolean isSelect) {
        List<T> list = new ArrayList<>();
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
            Unmarshaller um = jaxbContext.createUnmarshaller();
            if (xml != null && xml.contains(rel)) {
                String start_tag = "<" + rel + ">";
                if (isSelect) {
                    start_tag = "<" + rel + " action=\"select\">";
                }
                String end_tag = "</" + rel + ">";
                String[] entrys = xml.split(end_tag);
                for (String val : entrys) {
                    if (null != val && val.contains(start_tag)) {
                        String once = val.substring(val.indexOf(start_tag)) + end_tag;
                        T ob = (T) um.unmarshal(new ByteArrayInputStream(once.getBytes(StandardCharsets.UTF_8)));
                        list.add(ob);
                    }
                }
            }
            return list;
        } catch (JAXBException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 将xml对象根据定义的节点进行拆分,并实现List对象转换
     *
     * @param <T>   对象
     * @param xml   字符串
     * @param rel   拆分节点
     * @param clazz 对象信息
     * @return list对象
     */
    public static <T> List<T> parseXmlToBeanList(String xml, String rel, Class<T> clazz) {
        return parseXmlToBeanList(xml, rel, clazz, true);
    }

    /**
     * 将对象转换为XML
     *
     * @param object 对象信息
     * @return string xml
     * @throws Exception 异常
     */
    public static String parseBeanToXml(Object object) throws Exception {
        JAXBContext context = JAXBContext.newInstance(object.getClass());
        Marshaller m = context.createMarshaller();
        StringWriter sw = new StringWriter();
        m.marshal(object, sw);
        return sw.toString();
    }

    /**
     * 将对象转换为XML
     *
     * @param object   对象信息
     * @param map      map
     * @param listener Listener
     * @return string xml
     * @throws Exception 异常
     */
    public static String parseBeanToXml(Object object, Map<String, Object> map, Marshaller.Listener listener) throws Exception {
        JAXBContext context = JAXBContext.newInstance(object.getClass());
        Marshaller marshaller = context.createMarshaller();
        if (MapUtils.isNotEmpty(map)) {
            map.forEach((key, value) -> {
                try {
                    marshaller.setProperty(key, value);
                } catch (PropertyException e) {
                    throw new InstrumentException(e);
                }
            });
        }
        if (Objects.nonNull(listener)) {
            marshaller.setListener(listener);
        }
        StringWriter sw = new StringWriter();
        marshaller.marshal(object, sw);
        return sw.toString();
    }

    /**
     * 替换xml中特殊字符
     *
     * @param xml 字符串信息
     * @return 字符串
     */
    public static String replaceEscapeCharacter(String xml) {
        return xml.replaceAll("&lt;", "<").replaceAll("&gt;", ">").replaceAll("&quot;", "\"");
    }

    /**
     * 将MAP对象转换为XML格式
     *
     * @param map 对象信息
     * @return xml字符串
     */
    public static String mapToXmlNosign(Map<String, Object> map) {
        StringBuffer sb = new StringBuffer();
        Set<String> set = map.keySet();
        for (Iterator<String> it = set.iterator(); it.hasNext(); ) {
            String key = it.next();
            Object value = null != map.get(key) ? map.get(key) : " ";
            if (!"sign".equals(key)) {
                sb.append("<" + key + ">" + value + "</" + key + ">");
            }
        }
        return sb.toString();
    }

    /**
     * xml转list
     *
     * @param <T>   对象
     * @param xml   字符串
     * @param rel   拆分节点
     * @param clazz 对象
     * @return list对象
     */
    public static <T> List<T> XMLToBeanList(String xml, String rel, Class<T> clazz) {
        List<T> list = new ArrayList<>();
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
            Unmarshaller um = jaxbContext.createUnmarshaller();
            if (xml != null && xml.contains(rel)) {
                String start_tag = "<" + rel + ">";
                String end_tag = "</" + rel + ">";
                String[] entrys = xml.split(end_tag);
                for (String val : entrys) {
                    if (null != val && val.contains(start_tag)) {
                        String once = val.substring(val.indexOf(start_tag)) + end_tag;
                        T ob = (T) um.unmarshal(new StringReader(once));
                        list.add(ob);
                    }
                }
            }
        } catch (JAXBException e) {
            throw new InstrumentException(e);
        }
        return list;
    }

    /**
     * 将xml节点转小写
     *
     * @param xml 字符串
     * @return xml字符串
     */
    public static String xmlToLowerCase(String xml) {
        Pattern pattern = Pattern.compile("<.+?>");
        StringBuilder res = new StringBuilder();
        int lastIdx = 0;
        Matcher matchr = pattern.matcher(xml);
        while (matchr.find()) {
            String str = matchr.group();
            res.append(xml, lastIdx, matchr.start());
            res.append(str.toLowerCase());
            lastIdx = matchr.end();
        }
        res.append(xml.substring(lastIdx));
        return res.toString();
    }

    /**
     * @param xml   字符串
     * @param field 要获取的字段名
     * @return 返回该字段的值
     */
    public static String getFieldByxml(String xml, String field) {
        return new XmlUtils().getfield(xml, field);
    }


    public String requestModel(Object esbEntry) throws JAXBException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        JAXBContext jaxbContext = JAXBContext.newInstance(esbEntry.getClass());
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        marshaller.marshal(esbEntry, byteArrayOutputStream);
        String xmlContent = new String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8);
        return xmlContent.replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"");
    }

    public String getfield(String str, String field) {
        String s1 = str.replace("<map>", "");
        String s2 = s1.replace("</map>", "");
        String[] entrys = s2.split("</entry>");
        for (String s : entrys) {
            if (s.contains(field)) {
                return s.substring(s.indexOf(">") + 1);
            }
        }
        return "";
    }

}
