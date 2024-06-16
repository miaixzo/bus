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
package org.miaixz.bus.core.xml;

import org.miaixz.bus.core.lang.exception.InternalException;

import javax.xml.parsers.DocumentBuilderFactory;

/**
 * {@link javax.xml.parsers.DocumentBuilder} 工具类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DocumentBuilder {

    /**
     * 创建 DocumentBuilder
     *
     * @param namespaceAware 是否打开命名空间支持
     * @return DocumentBuilder
     */
    public static javax.xml.parsers.DocumentBuilder createDocumentBuilder(final boolean namespaceAware) {
        final javax.xml.parsers.DocumentBuilder builder;
        try {
            builder = createDocumentBuilderFactory(namespaceAware).newDocumentBuilder();
        } catch (final Exception e) {
            throw new InternalException(e, "Create xml document error!");
        }
        return builder;
    }

    /**
     * 创建{@link DocumentBuilderFactory}
     * <p>
     * 默认使用"com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl"
     * </p>
     *
     * @param namespaceAware 是否打开命名空间支持
     * @return {@link DocumentBuilderFactory}
     */
    public static DocumentBuilderFactory createDocumentBuilderFactory(final boolean namespaceAware) {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        // 默认打开NamespaceAware，getElementsByTagNameNS可以使用命名空间
        factory.setNamespaceAware(namespaceAware);
        return XXE.disableXXE(factory);
    }

}
