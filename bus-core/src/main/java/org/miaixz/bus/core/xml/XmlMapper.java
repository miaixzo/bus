/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
package org.miaixz.bus.core.xml;

import org.miaixz.bus.core.xyz.*;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * XML转换器，用于转换Map或Bean等
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class XmlMapper {

    private final Node node;

    /**
     * 构造
     *
     * @param node {@link Node}XML节点
     */
    public XmlMapper(final Node node) {
        this.node = node;
    }

    /**
     * 创建XmlMapper
     *
     * @param node {@link Node}XML节点
     * @return XmlMapper
     */
    public static XmlMapper of(final Node node) {
        return new XmlMapper(node);
    }

    /**
     * XML节点转Map
     *
     * @param result 结果Map
     * @return map
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
                // 子节点继续递归遍历
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
     * XML转Java Bean
     * 如果XML根节点只有一个，且节点名和Bean的名称一致，则直接转换子节点
     *
     * @param <T>  bean类型
     * @param bean bean类
     * @return beans
     */
    public <T> T toBean(final Class<T> bean) {
        final Map<String, Object> map = toMap();
        if (null != map && map.size() == 1) {
            final String nodeName = CollKit.getFirst(map.keySet());
            if (bean.getSimpleName().equalsIgnoreCase(nodeName)) {
                // 只有key和bean的名称匹配时才做单一对象转换
                return BeanKit.toBean(CollKit.get(map.values(), 0), bean);
            }
        }
        return BeanKit.toBean(map, bean);
    }

    /**
     * XML节点转Map
     *
     * @return map
     */
    public Map<String, Object> toMap() {
        return toMap(new LinkedHashMap<>());
    }

    /**
     * XML节点转Map
     *
     * @param result 结果Map
     * @return map
     */
    public Map<String, Object> toMap(final Map<String, Object> result) {
        return toMap(this.node, result);
    }

}
