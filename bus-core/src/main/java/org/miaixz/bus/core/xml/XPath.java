/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Utility class for {@link javax.xml.xpath.XPath}.
 *
 * @author Kimi Liu
 * @see <a href="https://www.ibm.com/developerworks/cn/xml/x-javaxpathapi.html">Introduction to the JAXP XPath API</a>
 * @since Java 17+
 */
public class XPath {

    /**
     * Creates a new {@link javax.xml.xpath.XPath} instance.
     *
     * @return A new {@link javax.xml.xpath.XPath} instance.
     */
    public static javax.xml.xpath.XPath createXPath() {
        return XPathFactory.newInstance().newXPath();
    }

    /**
     * Selects a single XML {@link Element} using an XPath expression.
     *
     * @param expression The XPath expression.
     * @param source     The source object, which can be a {@link org.w3c.dom.Document}, {@link Node}, etc.
     * @return The first matching {@link Element}, or {@code null} if no match is found.
     */
    public static Element getElementByXPath(final String expression, final Object source) {
        return (Element) getNodeByXPath(expression, source);
    }

    /**
     * Selects a {@link NodeList} of XML nodes using an XPath expression.
     *
     * @param expression The XPath expression.
     * @param source     The source object, which can be a {@link org.w3c.dom.Document}, {@link Node}, etc.
     * @return The matching {@link NodeList}.
     */
    public static NodeList getNodeListByXPath(final String expression, final Object source) {
        return (NodeList) getByXPath(expression, source, XPathConstants.NODESET);
    }

    /**
     * Selects a single XML {@link Node} using an XPath expression.
     *
     * @param expression The XPath expression.
     * @param source     The source object, which can be a {@link org.w3c.dom.Document}, {@link Node}, etc.
     * @return The first matching {@link Node}, or {@code null} if no match is found.
     */
    public static Node getNodeByXPath(final String expression, final Object source) {
        return (Node) getByXPath(expression, source, XPathConstants.NODE);
    }

    /**
     * Selects an object from an XML source using an XPath expression and a specified return type. This method
     * automatically handles namespaces if the source is a {@link Node}.
     *
     * @param expression The XPath expression.
     * @param source     The source object, which can be a {@link org.w3c.dom.Document}, {@link Node}, etc.
     * @param returnType The desired return type, as defined in {@link XPathConstants}.
     * @return The result of the XPath evaluation, cast to the specified return type.
     */
    public static Object getByXPath(final String expression, final Object source, final QName returnType) {
        NamespaceContext nsContext = null;
        if (source instanceof Node) {
            nsContext = new UniversalNamespace((Node) source, false);
        }
        return getByXPath(expression, source, returnType, nsContext);
    }

    /**
     * Selects an object from an XML source using an XPath expression, a specified return type, and a namespace context.
     *
     * @param expression The XPath expression.
     * @param source     The source object, which can be a {@link org.w3c.dom.Document}, {@link Node},
     *                   {@link InputSource}, etc.
     * @param returnType The desired return type, as defined in {@link XPathConstants}.
     * @param nsContext  The {@link NamespaceContext} to use for namespace resolution.
     * @return The result of the XPath evaluation, cast to the specified return type.
     * @throws InternalException if an {@link XPathExpressionException} occurs.
     * @see <a href="https://www.ibm.com/developerworks/cn/xml/x-nmspccontext/">Using the JAXP NamespaceContext
     *      Interface</a>
     */
    public static Object getByXPath(
            final String expression,
            final Object source,
            final QName returnType,
            final NamespaceContext nsContext) {
        final javax.xml.xpath.XPath xPath = createXPath();
        if (null != nsContext) {
            xPath.setNamespaceContext(nsContext);
        }
        try {
            if (source instanceof InputSource) {
                return xPath.evaluate(expression, (InputSource) source, returnType);
            } else {
                return xPath.evaluate(expression, source, returnType);
            }
        } catch (final XPathExpressionException e) {
            throw new InternalException(e);
        }
    }

}
