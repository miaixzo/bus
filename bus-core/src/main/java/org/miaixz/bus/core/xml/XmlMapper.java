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
package org.miaixz.bus.core.xml;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.bean.copier.CopyOptions;
import org.miaixz.bus.core.xyz.*;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * XML converter for transforming XML data into Maps, Java Beans, etc.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class XmlMapper {

    private final Node node;

    /**
     * Constructs a new XmlMapper with the specified XML node.
     *
     * @param node The XML {@link Node}.
     */
    public XmlMapper(final Node node) {
        this.node = node;
    }

    /**
     * Creates a new XmlMapper instance from the given XML node.
     *
     * @param node The XML {@link Node}.
     * @return A new {@link XmlMapper} instance.
     */
    public static XmlMapper of(final Node node) {
        return new XmlMapper(node);
    }

    /**
     * Recursively converts an XML {@link Node} and its children to a {@link Map}.
     *
     * @param node   The XML node to convert.
     * @param result The map to store the results in.
     * @return The resulting map.
     */
    private static Map<String, Object> toMap(final Node node, Map<String, Object> result) {
        if (null == result) {
            result = new HashMap<>();
        }
        final NodeList nodeList = node.getChildNodes();
        final int length = nodeList.getLength();
        Node childNode;
        Element childEle;
        for (int i = 0; i < length; ++i) {
            childNode = nodeList.item(i);
            if (!XmlKit.isElement(childNode)) {
                continue;
            }

            childEle = (Element) childNode;
            final Object newValue;
            if (childEle.hasChildNodes()) {
                // Recursively traverse child nodes
                final Map<String, Object> map = toMap(childEle, new LinkedHashMap<>());
                if (MapKit.isNotEmpty(map)) {
                    newValue = map;
                } else {
                    newValue = childEle.getTextContent();
                }
            } else {
                newValue = childEle.getTextContent();
            }

            if (null != newValue) {
                final Object value = result.get(childEle.getNodeName());
                if (null != value) {
                    if (value instanceof List) {
                        ((List<Object>) value).add(newValue);
                    } else {
                        result.put(childEle.getNodeName(), ListKit.of(value, newValue));
                    }
                } else {
                    result.put(childEle.getNodeName(), newValue);
                }
            }
        }
        return result;
    }

    /**
     * Converts the XML to a Java Bean. If the XML root has only one child and its name matches the bean's class name,
     * it converts the child node directly.
     *
     * @param <T>         The type of the bean.
     * @param beanClass   The class of the bean.
     * @param copyOptions The options for copying properties.
     * @return The converted bean.
     */
    public <T> T toBean(final Class<T> beanClass, final CopyOptions copyOptions) {
        final Map<String, Object> map = toMap();
        if (null != map && map.size() == 1) {
            final String nodeName = CollKit.getFirst(map.keySet());
            if (beanClass.getSimpleName().equalsIgnoreCase(nodeName)) {
                // Convert only when the key matches the bean name
                return BeanKit.toBean(CollKit.get(map.values(), 0), beanClass);
            }
        }
        return BeanKit.toBean(map, beanClass, copyOptions);
    }

    /**
     * Converts the XML node to a {@link Map}.
     *
     * @return The resulting map.
     */
    public Map<String, Object> toMap() {
        return toMap(new LinkedHashMap<>());
    }

    /**
     * Converts the XML node to a {@link Map}.
     *
     * @param result The map to store the results in.
     * @return The resulting map.
     */
    public Map<String, Object> toMap(final Map<String, Object> result) {
        return toMap(this.node, result);
    }

}
