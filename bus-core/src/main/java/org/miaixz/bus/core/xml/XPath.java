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

import org.miaixz.bus.core.lang.exception.InternalException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * {@link javax.xml.xpath.XPath}相关工具类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class XPath {

    /**
     * 创建XPath
     * Xpath相关文章：<a href="https://www.ibm.com/developerworks/cn/xml/x-javaxpathapi.html">https://www.ibm.com/developerworks/cn/xml/x-javaxpathapi.html</a>
     *
     * @return {@link javax.xml.xpath.XPath}
     */
    public static javax.xml.xpath.XPath createXPath() {
        return XPathFactory.newInstance().newXPath();
    }

    /**
     * 通过XPath方式读取XML节点等信息
     * Xpath相关文章：<a href="https://www.ibm.com/developerworks/cn/xml/x-javaxpathapi.html">https://www.ibm.com/developerworks/cn/xml/x-javaxpathapi.html</a>
     *
     * @param expression XPath表达式
     * @param source     资源，可以是Docunent、Node节点等
     * @return 匹配返回类型的值
     */
    public static Element getElementByXPath(final String expression, final Object source) {
        return (Element) getNodeByXPath(expression, source);
    }

    /**
     * 通过XPath方式读取XML的NodeList
     * Xpath相关文章：<a href="https://www.ibm.com/developerworks/cn/xml/x-javaxpathapi.html">https://www.ibm.com/developerworks/cn/xml/x-javaxpathapi.html</a>
     *
     * @param expression XPath表达式
     * @param source     资源，可以是Docunent、Node节点等
     * @return NodeList
     */
    public static NodeList getNodeListByXPath(final String expression, final Object source) {
        return (NodeList) getByXPath(expression, source, XPathConstants.NODESET);
    }

    /**
     * 通过XPath方式读取XML节点等信息
     * Xpath相关文章：<a href="https://www.ibm.com/developerworks/cn/xml/x-javaxpathapi.html">https://www.ibm.com/developerworks/cn/xml/x-javaxpathapi.html</a>
     *
     * @param expression XPath表达式
     * @param source     资源，可以是Docunent、Node节点等
     * @return 匹配返回类型的值
     */
    public static Node getNodeByXPath(final String expression, final Object source) {
        return (Node) getByXPath(expression, source, XPathConstants.NODE);
    }

    /**
     * 通过XPath方式读取XML节点等信息
     * Xpath相关文章：<a href="https://www.ibm.com/developerworks/cn/xml/x-javaxpathapi.html">https://www.ibm.com/developerworks/cn/xml/x-javaxpathapi.html</a>
     *
     * @param expression XPath表达式
     * @param source     资源，可以是Docunent、Node节点等
     * @param returnType 返回类型，{@link javax.xml.xpath.XPathConstants}
     * @return 匹配返回类型的值
     */
    public static Object getByXPath(final String expression, final Object source, final QName returnType) {
        NamespaceContext nsContext = null;
        if (source instanceof Node) {
            nsContext = new UniversalNamespaceCache((Node) source, false);
        }
        return getByXPath(expression, source, returnType, nsContext);
    }

    /**
     * 通过XPath方式读取XML节点等信息
     * Xpath相关文章：
     * <a href="https://www.ibm.com/developerworks/cn/xml/x-javaxpathapi.html">https://www.ibm.com/developerworks/cn/xml/x-javaxpathapi.html</a>
     * <a href="https://www.ibm.com/developerworks/cn/xml/x-nmspccontext/">https://www.ibm.com/developerworks/cn/xml/x-nmspccontext/</a>
     *
     * @param expression XPath表达式
     * @param source     资源，可以是Docunent、Node节点等
     * @param returnType 返回类型，{@link javax.xml.xpath.XPathConstants}
     * @param nsContext  {@link NamespaceContext}
     * @return 匹配返回类型的值
     */
    public static Object getByXPath(final String expression, final Object source, final QName returnType, final NamespaceContext nsContext) {
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
