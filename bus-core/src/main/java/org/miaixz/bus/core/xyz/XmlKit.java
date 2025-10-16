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
package org.miaixz.bus.core.xyz;

import java.beans.XMLEncoder;
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

import org.miaixz.bus.core.bean.copier.CopyOptions;
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

/**
 * XML utility class. This utility uses the W3C DOM tools and does not require third-party libraries. It encapsulates
 * the creation, reading, writing, and some operations of XML documents.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class XmlKit {

    /**
     * Regex for invalid characters in XML.
     */
    public static final Pattern INVALID_PATTERN = Pattern.compile("[\\x00-\\x08\\x0b-\\x0c\\x0e-\\x1f]");
    /**
     * Regex for comments in XML.
     */
    public static final Pattern COMMENT_PATTERN = Pattern.compile("(?s)");

    /**
     * Reads and parses an XML file from a path or content string.
     *
     * @param pathOrContent The content string or the file path.
     * @return The XML `Document` object.
     */
    public static Document readXml(String pathOrContent) {
        pathOrContent = StringKit.trim(pathOrContent);
        if (StringKit.startWith(pathOrContent, '<')) {
            return parseXml(pathOrContent);
        }
        return readXml(FileKit.file(pathOrContent));
    }

    /**
     * Reads and parses an XML file.
     *
     * @param file The XML file.
     * @return The XML `Document` object.
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
     * Reads and parses XML from an `InputStream`.
     *
     * @param inputStream The XML stream.
     * @return The XML `Document` object.
     * @throws InternalException for IO or parsing errors.
     */
    public static Document readXml(final InputStream inputStream) throws InternalException {
        return readXml(new InputSource(inputStream), true);
    }

    /**
     * Reads and parses XML from a `Reader`.
     *
     * @param reader The XML reader.
     * @return The XML `Document` object.
     * @throws InternalException for IO or parsing errors.
     */
    public static Document readXml(final Reader reader) throws InternalException {
        return readXml(new InputSource(reader), true);
    }

    /**
     * Reads and parses XML from an `InputSource`.
     *
     * @param source         The `InputSource`.
     * @param namespaceAware Whether to enable namespace support.
     * @return The XML `Document` object.
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
     * Parses an XML string into a DOM `Document`.
     *
     * @param xmlStr The XML string.
     * @return The XML `Document`.
     */
    public static Document parseXml(final String xmlStr) {
        if (StringKit.isBlank(xmlStr)) {
            throw new IllegalArgumentException("XML content string is blank !");
        }
        return readXml(StringKit.getReader(cleanInvalid(xmlStr)));
    }

    /**
     * Reads the specified XML using a SAX parser.
     *
     * @param file           The XML source file.
     * @param contentHandler The content handler.
     */
    public static void readBySax(final File file, final ContentHandler contentHandler) {
        try (InputStream in = FileKit.getInputStream(file)) {
            readBySax(new InputSource(in), contentHandler);
        } catch (IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Reads the specified XML using a SAX parser.
     *
     * @param reader         The XML source reader.
     * @param contentHandler The content handler.
     */
    public static void readBySax(final Reader reader, final ContentHandler contentHandler) {
        try {
            readBySax(new InputSource(reader), contentHandler);
        } finally {
            IoKit.closeQuietly(reader);
        }
    }

    /**
     * Reads the specified XML using a SAX parser.
     *
     * @param source         The XML source stream.
     * @param contentHandler The content handler.
     */
    public static void readBySax(final InputStream source, final ContentHandler contentHandler) {
        try {
            readBySax(new InputSource(source), contentHandler);
        } finally {
            IoKit.closeQuietly(source);
        }
    }

    /**
     * Reads the specified XML using a SAX parser.
     *
     * @param source         The `InputSource`.
     * @param contentHandler The content handler.
     */
    public static void readBySax(final InputSource source, final ContentHandler contentHandler) {
        XmlSaxReader.of(source).read(contentHandler);
    }

    /**
     * Converts an XML `Document` to a string.
     *
     * @param doc The XML `Document` or `Node`.
     * @return The XML string.
     */
    public static String toString(final Node doc) {
        return toString(doc, false);
    }

    /**
     * Converts an XML `Document` to a string.
     *
     * @param doc      The XML `Document` or `Node`.
     * @param isPretty If `true`, formats the output with indentation.
     * @return The XML string.
     */
    public static String toString(final Node doc, final boolean isPretty) {
        return toString(doc, Charset.UTF_8, isPretty);
    }

    /**
     * Converts an XML `Document` to a string.
     *
     * @param doc      The XML `Document` or `Node`.
     * @param charset  The character set.
     * @param isPretty If `true`, formats the output.
     * @return The XML string.
     */
    public static String toString(final Node doc, final java.nio.charset.Charset charset, final boolean isPretty) {
        return toString(doc, charset, isPretty, false);
    }

    /**
     * Converts an XML `Document` to a string.
     *
     * @param doc                The XML `Document` or `Node`.
     * @param charset            The character set.
     * @param isPretty           If `true`, formats the output.
     * @param omitXmlDeclaration If `true`, omits the XML declaration.
     * @return The XML string.
     */
    public static String toString(
            final Node doc,
            final java.nio.charset.Charset charset,
            final boolean isPretty,
            final boolean omitXmlDeclaration) {
        final StringWriter writer = StringKit.getWriter();
        write(doc, writer, charset, isPretty ? Normal._2 : 0, omitXmlDeclaration);
        return writer.toString();
    }

    /**
     * Formats an XML document for pretty printing.
     *
     * @param doc The XML `Document`.
     * @return The formatted XML string.
     */
    public static String format(final Document doc) {
        return toString(doc, true);
    }

    /**
     * Formats an XML string for pretty printing.
     *
     * @param xmlStr The XML string.
     * @return The formatted XML string.
     */
    public static String format(final String xmlStr) {
        return format(parseXml(xmlStr));
    }

    /**
     * Writes an XML `Document` to a file.
     *
     * @param doc     The XML `Document`.
     * @param file    The destination file.
     * @param charset The character set.
     */
    public static void write(final Document doc, final File file, final java.nio.charset.Charset charset) {
        XmlWriter.of(doc).setCharset(charset).setIndent(Normal._2).setOmitXmlDeclaration(false).write(file);
    }

    /**
     * Writes an XML `Node` to a `Writer`.
     *
     * @param node    The `Node`.
     * @param writer  The `Writer`.
     * @param charset The character set.
     * @param indent  The indentation level (less than 1 means no formatting).
     */
    public static void write(
            final Node node,
            final Writer writer,
            final java.nio.charset.Charset charset,
            final int indent) {
        write(node, writer, charset, indent, false);
    }

    /**
     * Writes an XML `Node` to a `Writer`.
     *
     * @param node               The `Node`.
     * @param writer             The `Writer`.
     * @param charset            The character set.
     * @param indent             The indentation level.
     * @param omitXmlDeclaration If `true`, omits the XML declaration.
     */
    public static void write(
            final Node node,
            final Writer writer,
            final java.nio.charset.Charset charset,
            final int indent,
            final boolean omitXmlDeclaration) {
        XmlWriter.of(node).setCharset(charset).setIndent(indent).setOmitXmlDeclaration(omitXmlDeclaration)
                .write(writer);
    }

    /**
     * Writes an XML `Node` to an `OutputStream`.
     *
     * @param node    The `Node`.
     * @param out     The `OutputStream`.
     * @param charset The character set.
     * @param indent  The indentation level.
     */
    public static void write(
            final Node node,
            final OutputStream out,
            final java.nio.charset.Charset charset,
            final int indent) {
        write(node, out, charset, indent, false);
    }

    /**
     * Writes an XML `Node` to an `OutputStream`.
     *
     * @param node               The `Node`.
     * @param out                The `OutputStream`.
     * @param charset            The character set.
     * @param indent             The indentation level.
     * @param omitXmlDeclaration If `true`, omits the XML declaration.
     */
    public static void write(
            final Node node,
            final OutputStream out,
            final java.nio.charset.Charset charset,
            final int indent,
            final boolean omitXmlDeclaration) {
        XmlWriter.of(node).setCharset(charset).setIndent(indent).setOmitXmlDeclaration(omitXmlDeclaration).write(out);
    }

    /**
     * Creates a new XML `Document`.
     *
     * @return The XML `Document`.
     */
    public static Document createXml() {
        return DocumentBuilder.createDocumentBuilder(true).newDocument();
    }

    /**
     * Creates a new XML `Document` with a specified root element.
     *
     * @param rootElementName The name of the root element.
     * @return The XML `Document`.
     */
    public static Document createXml(final String rootElementName) {
        return createXml(rootElementName, null);
    }

    /**
     * Creates a new XML `Document` with a specified root element and namespace.
     *
     * @param rootElementName The name of the root element.
     * @param namespace       The namespace URI.
     * @return The XML `Document`.
     */
    public static Document createXml(final String rootElementName, final String namespace) {
        final Document doc = createXml();
        doc.appendChild(
                null == namespace ? doc.createElement(rootElementName)
                        : doc.createElementNS(namespace, rootElementName));
        return doc;
    }

    /**
     * Gets the root element of an XML `Document`.
     *
     * @param doc The `Document`.
     * @return The root element.
     */
    public static Element getRootElement(final Document doc) {
        return (null == doc) ? null : doc.getDocumentElement();
    }

    /**
     * Gets the owner `Document` of a `Node`.
     *
     * @param node The node.
     * @return The `Document`.
     */
    public static Document getOwnerDocument(final Node node) {
        return (node instanceof Document) ? (Document) node : node.getOwnerDocument();
    }

    /**
     * Removes invalid XML characters from a string.
     *
     * @param xmlContent The XML content.
     * @return The cleaned string.
     */
    public static String cleanInvalid(final String xmlContent) {
        if (xmlContent == null) {
            return null;
        }
        return PatternKit.replaceAll(xmlContent, INVALID_PATTERN, Normal.EMPTY);
    }

    /**
     * Removes XML comments from a string.
     *
     * @param xmlContent The XML content.
     * @return The content without comments.
     */
    public static String cleanComment(final String xmlContent) {
        if (xmlContent == null) {
            return null;
        }
        return PatternKit.replaceAll(xmlContent, COMMENT_PATTERN, Normal.EMPTY);
    }

    /**
     * Gets a list of child elements by tag name.
     *
     * @param element The parent element.
     * @param tagName The tag name (if blank, returns all child elements).
     * @return A list of elements.
     */
    public static List<Element> getElements(final Element element, final String tagName) {
        final NodeList nodeList = StringKit.isBlank(tagName) ? element.getChildNodes()
                : element.getElementsByTagName(tagName);
        return transElements(element, nodeList);
    }

    /**
     * Gets the first child element with a given tag name.
     *
     * @param element The parent element.
     * @param tagName The tag name.
     * @return The child element, or `null`.
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
     * Gets the text content of the first child element with a given tag name.
     *
     * @param element The parent element.
     * @param tagName The tag name.
     * @return The text content.
     */
    public static String elementText(final Element element, final String tagName) {
        final Element child = getElement(element, tagName);
        return child == null ? null : child.getTextContent();
    }

    /**
     * Gets the text content of the first child element with a given tag name, or a default value.
     *
     * @param element      The parent element.
     * @param tagName      The tag name.
     * @param defaultValue The default value.
     * @return The text content.
     */
    public static String elementText(final Element element, final String tagName, final String defaultValue) {
        final Element child = getElement(element, tagName);
        return child == null ? defaultValue : child.getTextContent();
    }

    /**
     * Converts a `NodeList` to a `List` of `Element`s.
     *
     * @param nodeList The `NodeList`.
     * @return A list of elements.
     */
    public static List<Element> transElements(final NodeList nodeList) {
        return transElements(null, nodeList);
    }

    /**
     * Converts a `NodeList` to a `List` of `Element`s.
     *
     * @param parentEle The parent element.
     * @param nodeList  The `NodeList`.
     * @return A list of elements.
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
     * Writes a serializable object to an XML file.
     *
     * @param dest The destination file.
     * @param bean The object.
     */
    public static void writeObjectAsXml(final File dest, final Object bean) {
        try (XMLEncoder xmlenc = new XMLEncoder(FileKit.getOutputStream(dest))) {
            xmlenc.writeObject(bean);
        }
    }

    /**
     * Converts an XML node to a JavaBean.
     *
     * @param <T>       The bean type.
     * @param node      The XML node.
     * @param beanClass The bean class.
     * @return The bean instance.
     */
    public static <T> T xmlToBean(final Node node, final Class<T> beanClass) {
        return xmlToBean(node, beanClass, null);
    }

    /**
     * Converts an XML node to a JavaBean.
     *
     * @param <T>         The bean type.
     * @param node        The XML node.
     * @param beanClass   The bean class.
     * @param copyOptions Copy options.
     * @return The bean instance.
     */
    public static <T> T xmlToBean(final Node node, final Class<T> beanClass, final CopyOptions copyOptions) {
        return XmlMapper.of(node).toBean(beanClass, copyOptions);
    }

    /**
     * Converts an XML string to a `Map`.
     *
     * @param xmlStr The XML string.
     * @return The resulting map.
     */
    public static Map<String, Object> xmlToMap(final String xmlStr) {
        return xmlToMap(xmlStr, new LinkedHashMap<>());
    }

    /**
     * Converts an XML string to a `Map`.
     *
     * @param xmlStr The XML string.
     * @param result The map to populate.
     * @return The resulting map.
     */
    public static Map<String, Object> xmlToMap(final String xmlStr, final Map<String, Object> result) {
        final Document doc = parseXml(xmlStr);
        final Element root = getRootElement(doc);
        root.normalize();
        return xmlToMap(root, result);
    }

    /**
     * Converts an XML node to a `Map`.
     *
     * @param node The XML node.
     * @return The resulting map.
     */
    public static Map<String, Object> xmlToMap(final Node node) {
        return xmlToMap(node, new LinkedHashMap<>());
    }

    /**
     * Converts an XML node to a `Map`.
     *
     * @param node   The XML node.
     * @param result The map to populate.
     * @return The resulting map.
     */
    public static Map<String, Object> xmlToMap(final Node node, final Map<String, Object> result) {
        XmlMapper.of(node).toMap(result);
        return result;
    }

    /**
     * Converts a `Map` to an XML string.
     *
     * @param data The map data.
     * @return The XML string.
     */
    public static String mapToXmlString(final Map<?, ?> data) {
        return toString(mapToXml(data, "xml"));
    }

    /**
     * Converts a `Map` to an XML string.
     *
     * @param data               The map data.
     * @param omitXmlDeclaration If `true`, omits the XML declaration.
     * @return The XML string.
     */
    public static String mapToXmlString(final Map<?, ?> data, final boolean omitXmlDeclaration) {
        return toString(mapToXml(data, "xml"), Charset.UTF_8, false, omitXmlDeclaration);
    }

    /**
     * Converts a `Map` to an XML string with a specified root name.
     *
     * @param data     The map data.
     * @param rootName The root element name.
     * @return The XML string.
     */
    public static String mapToXmlString(final Map<?, ?> data, final String rootName) {
        return toString(mapToXml(data, rootName));
    }

    /**
     * Converts a `Map` to an XML string with a specified root name and namespace.
     *
     * @param data      The map data.
     * @param rootName  The root element name.
     * @param namespace The namespace URI.
     * @return The XML string.
     */
    public static String mapToXmlString(final Map<?, ?> data, final String rootName, final String namespace) {
        return toString(mapToXml(data, rootName, namespace));
    }

    /**
     * Converts a `Map` to an XML string.
     *
     * @param data               The map data.
     * @param rootName           The root element name.
     * @param namespace          The namespace URI.
     * @param omitXmlDeclaration If `true`, omits the XML declaration.
     * @return The XML string.
     */
    public static String mapToXmlString(
            final Map<?, ?> data,
            final String rootName,
            final String namespace,
            final boolean omitXmlDeclaration) {
        return toString(mapToXml(data, rootName, namespace), Charset.UTF_8, false, omitXmlDeclaration);
    }

    /**
     * Converts a `Map` to an XML string.
     *
     * @param data               The map data.
     * @param rootName           The root element name.
     * @param namespace          The namespace URI.
     * @param isPretty           If `true`, formats the output.
     * @param omitXmlDeclaration If `true`, omits the XML declaration.
     * @return The XML string.
     */
    public static String mapToXmlString(
            final Map<?, ?> data,
            final String rootName,
            final String namespace,
            final boolean isPretty,
            final boolean omitXmlDeclaration) {
        return toString(mapToXml(data, rootName, namespace), Charset.UTF_8, isPretty, omitXmlDeclaration);
    }

    /**
     * Converts a `Map` to an XML string.
     *
     * @param data               The map data.
     * @param rootName           The root element name.
     * @param namespace          The namespace URI.
     * @param charset            The character set.
     * @param isPretty           If `true`, formats the output.
     * @param omitXmlDeclaration If `true`, omits the XML declaration.
     * @return The XML string.
     */
    public static String mapToXmlString(
            final Map<?, ?> data,
            final String rootName,
            final String namespace,
            final java.nio.charset.Charset charset,
            final boolean isPretty,
            final boolean omitXmlDeclaration) {
        return toString(mapToXml(data, rootName, namespace), charset, isPretty, omitXmlDeclaration);
    }

    /**
     * Converts a `Map` to an XML `Document`.
     *
     * @param data     The map data.
     * @param rootName The root element name.
     * @return The XML `Document`.
     */
    public static Document mapToXml(final Map<?, ?> data, final String rootName) {
        return mapToXml(data, rootName, null);
    }

    /**
     * Converts a `Map` to an XML `Document`.
     *
     * @param data      The map data.
     * @param rootName  The root element name.
     * @param namespace The namespace URI.
     * @return The XML `Document`.
     */
    public static Document mapToXml(final Map<?, ?> data, final String rootName, final String namespace) {
        final Document doc = createXml();
        final Element root = appendChild(doc, rootName, namespace);
        appendMap(doc, root, data);
        return doc;
    }

    /**
     * Converts a JavaBean to an XML `Document`.
     *
     * @param bean The bean object.
     * @return The XML `Document`.
     */
    public static Document beanToXml(final Object bean) {
        return beanToXml(bean, null);
    }

    /**
     * Converts a JavaBean to an XML `Document`.
     *
     * @param bean      The bean object.
     * @param namespace The namespace URI.
     * @return The XML `Document`.
     */
    public static Document beanToXml(final Object bean, final String namespace) {
        return beanToXml(bean, namespace, false);
    }

    /**
     * Converts a JavaBean to an XML `Document`.
     *
     * @param bean       The bean object.
     * @param namespace  The namespace URI.
     * @param ignoreNull If `true`, ignores `null` properties.
     * @return The XML `Document`.
     */
    public static Document beanToXml(final Object bean, final String namespace, final boolean ignoreNull) {
        if (null == bean) {
            return null;
        }
        return mapToXml(BeanKit.beanToMap(bean, false, ignoreNull), bean.getClass().getSimpleName(), namespace);
    }

    /**
     * Checks if a `Node` is an `Element`.
     *
     * @param node The node.
     * @return `true` if it is an `Element`.
     */
    public static boolean isElement(final Node node) {
        return (null != node) && Node.ELEMENT_NODE == node.getNodeType();
    }

    /**
     * Appends a child element to a node.
     *
     * @param node    The parent node.
     * @param tagName The tag name of the new element.
     * @return The new child element.
     */
    public static Element appendChild(final Node node, final String tagName) {
        return appendChild(node, tagName, null);
    }

    /**
     * Appends a child element with a namespace to a node.
     *
     * @param node      The parent node.
     * @param tagName   The tag name.
     * @param namespace The namespace URI.
     * @return The new child element.
     */
    public static Element appendChild(final Node node, final String tagName, final String namespace) {
        final Document doc = getOwnerDocument(node);
        final Element child = (null == namespace) ? doc.createElement(tagName)
                : doc.createElementNS(namespace, tagName);
        node.appendChild(child);
        return child;
    }

    /**
     * Appends a text node to a parent node.
     *
     * @param node The parent node.
     * @param text The text content.
     * @return The new text node.
     */
    public static Node appendText(final Node node, final CharSequence text) {
        return appendText(getOwnerDocument(node), node, text);
    }

    /**
     * Appends data (Map, Collection, or text) as child nodes.
     *
     * @param node The parent node.
     * @param data The data to append.
     */
    public static void append(final Node node, final Object data) {
        append(getOwnerDocument(node), node, data);
    }

    /**
     * Appends data as child nodes.
     *
     * @param doc  The owner `Document`.
     * @param node The parent node.
     * @param data The data.
     */
    private static void append(final Document doc, final Node node, final Object data) {
        if (data instanceof Map) {
            appendMap(doc, node, (Map) data);
        } else if (data instanceof Iterator) {
            appendIterator(doc, node, (Iterator) data);
        } else if (data instanceof Iterable) {
            appendIterator(doc, node, ((Iterable) data).iterator());
        } else {
            appendText(doc, node, data.toString());
        }
    }

    /**
     * Appends map data as child elements.
     *
     * @param doc  The owner `Document`.
     * @param node The parent node.
     * @param data The map data.
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
     * Appends collection data as child nodes.
     *
     * @param doc  The owner `Document`.
     * @param node The parent node.
     * @param data The iterator data.
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
     * Appends a text node.
     *
     * @param doc  The owner `Document`.
     * @param node The parent node.
     * @param text The text content.
     * @return The new text node.
     */
    private static Node appendText(final Document doc, final Node node, final CharSequence text) {
        return node.appendChild(doc.createTextNode(StringKit.toStringOrEmpty(text)));
    }

}
