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
import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

import org.miaixz.bus.core.center.map.BiMap;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 全局命名空间上下文 见：https://www.ibm.com/developerworks/cn/xml/x-nmspccontext/
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class UniversalNamespace implements NamespaceContext {

    private static final String DEFAULT_NS = "DEFAULT";
    private final BiMap<String, String> prefixUri = new BiMap<>(new HashMap<>());

    /**
     * 此构造函数解析文档并存储其能找到的所有命名空间。 如果 toplevelOnly 为 true，则仅使用根目录中的命名空间。
     *
     * @param node         源节点
     * @param toplevelOnly 限制搜索以提高性能
     */
    public UniversalNamespace(final Node node, final boolean toplevelOnly) {
        examineNode(node.getFirstChild(), toplevelOnly);
    }

    /**
     * 取单个节点，提取并存储命名空间属性。
     *
     * @param node           检查节点
     * @param attributesOnly 如果为真，则不发生递归
     */
    private void examineNode(final Node node, final boolean attributesOnly) {
        final NamedNodeMap attributes = node.getAttributes();
        if (null != attributes) {
            final int length = attributes.getLength();
            for (int i = 0; i < length; i++) {
                final Node attribute = attributes.item(i);
                storeAttribute(attribute);
            }
        }

        if (!attributesOnly) {
            final NodeList childNodes = node.getChildNodes();
            if (null != childNodes) {
                Node item;
                final int childLength = childNodes.getLength();
                for (int i = 0; i < childLength; i++) {
                    item = childNodes.item(i);
                    if (item.getNodeType() == Node.ELEMENT_NODE)
                        examineNode(item, false);
                }
            }
        }
    }

    /**
     * 如果它是命名空间属性，则此方法查看该属性并将其存储。
     *
     * @param node 检查节点
     */
    private void storeAttribute(final Node node) {
        if (null == node) {
            return;
        }
        // 检查命名空间 xmlns 中的属性
        if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(node.getNamespaceURI())) {
            // 默认命名空间 xmlns="uri goes here"
            if (XMLConstants.XMLNS_ATTRIBUTE.equals(node.getNodeName())) {
                prefixUri.put(DEFAULT_NS, node.getNodeValue());
            } else {
                // 定义的前缀存储在这里
                prefixUri.put(node.getLocalName(), node.getNodeValue());
            }
        }

    }

    /**
     * 此方法由 XPath 调用。如果前缀为 null 或“”，则返回默认命名空间。
     *
     * @param prefix 前缀
     * @return 命名空间URI
     */
    @Override
    public String getNamespaceURI(final String prefix) {
        if (prefix == null || XMLConstants.DEFAULT_NS_PREFIX.equals(prefix)) {
            return prefixUri.get(DEFAULT_NS);
        } else {
            return prefixUri.get(prefix);
        }
    }

    /**
     * 在这种情况下不需要这种方法，但可以用类似的方式实现。
     */
    @Override
    public String getPrefix(final String namespaceURI) {
        return prefixUri.getInverse().get(namespaceURI);
    }

    @Override
    public Iterator<String> getPrefixes(final String namespaceURI) {
        return null;
    }

}
