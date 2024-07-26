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

/**
 * {@link javax.xml.parsers.SAXParserFactory} 工具
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SAXParserFactory {

    /**
     * Sax读取器工厂缓存
     */
    private static volatile javax.xml.parsers.SAXParserFactory factory;

    /**
     * 获取全局{@link javax.xml.parsers.SAXParserFactory}
     * <ul>
     * <li>默认不验证</li>
     * <li>默认打开命名空间支持</li>
     * </ul>
     *
     * @return {@link javax.xml.parsers.SAXParserFactory}
     */
    public static javax.xml.parsers.SAXParserFactory getFactory() {
        if (null == factory) {
            synchronized (SAXParserFactory.class) {
                if (null == factory) {
                    factory = createFactory(false, true);
                }
            }
        }

        return factory;
    }

    /**
     * 创建{@link javax.xml.parsers.SAXParserFactory}
     *
     * @param validating     是否验证
     * @param namespaceAware 是否打开命名空间支持
     * @return {@link javax.xml.parsers.SAXParserFactory}
     */
    public static javax.xml.parsers.SAXParserFactory createFactory(final boolean validating,
            final boolean namespaceAware) {
        final javax.xml.parsers.SAXParserFactory factory = javax.xml.parsers.SAXParserFactory.newInstance();
        factory.setValidating(validating);
        factory.setNamespaceAware(namespaceAware);

        return XXE.disableXXE(factory);
    }

}
