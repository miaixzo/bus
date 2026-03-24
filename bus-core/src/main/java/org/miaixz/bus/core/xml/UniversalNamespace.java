/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
 * Universal Namespace Context for XML processing. See:
 * <a href="https://www.ibm.com/developerworks/cn/xml/x-nmspccontext/">Using the JAXP NamespaceContext Interface</a>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class UniversalNamespace implements NamespaceContext {

    /**
     * The key used to store the default namespace in the prefix-to-URI mapping.
     */
    private static final String DEFAULT_NS = "DEFAULT";

    /**
     * A bidirectional map storing namespace prefixes and their corresponding URIs.
     */
    private final BiMap<String, String> prefixUri = new BiMap<>(new HashMap<>());

    /**
     * Constructs a new UniversalNamespace by parsing the provided node and storing all found namespaces. If
     * {@code toplevelOnly} is {@code true}, only namespaces in the root element are considered.
     *
     * @param node         The source {@link Node}.
     * @param toplevelOnly {@code true} to limit the search to top-level namespaces for performance, {@code false}
     *                     otherwise.
     */
    public UniversalNamespace(final Node node, final boolean toplevelOnly) {
        examineNode(node.getFirstChild(), toplevelOnly);
    }

    /**
     * Examines a single node, extracting and storing namespace attributes.
     *
     * @param node           The node to examine.
     * @param attributesOnly If {@code true}, recursion does not occur.
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
     * Examines an attribute node and stores it if it is a namespace attribute.
     *
     * @param node The attribute node to examine.
     */
    private void storeAttribute(final Node node) {
        if (null == node) {
            return;
        }
        // Check attributes in the xmlns namespace
        if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(node.getNamespaceURI())) {
            // Default namespace xmlns="uri goes here"
            if (XMLConstants.XMLNS_ATTRIBUTE.equals(node.getNodeName())) {
                prefixUri.put(DEFAULT_NS, node.getNodeValue());
            } else {
                // Defined prefixes are stored here
                prefixUri.put(node.getLocalName(), node.getNodeValue());
            }
        }

    }

    /**
     * This method is called by XPath. If the prefix is {@code null} or an empty string, it returns the default
     * namespace.
     *
     * @param prefix The prefix to look up.
     * @return The namespace URI associated with the prefix.
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
     * This method is not needed in this case but can be implemented in a similar way.
     *
     * @param namespaceURI The namespace URI to look up.
     * @return The prefix associated with the namespace URI.
     */
    @Override
    public String getPrefix(final String namespaceURI) {
        return prefixUri.getInverse().get(namespaceURI);
    }

    /**
     * This method is not needed in this case.
     *
     * @param namespaceURI The namespace URI to look up.
     * @return An iterator over the prefixes associated with the namespace URI.
     */
    @Override
    public Iterator<String> getPrefixes(final String namespaceURI) {
        return null;
    }

}
