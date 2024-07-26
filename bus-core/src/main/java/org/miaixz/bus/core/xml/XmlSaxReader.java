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
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import java.io.IOException;

/**
 * XML SAX方式读取器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class XmlSaxReader {

    private final javax.xml.parsers.SAXParserFactory factory;
    private final InputSource source;

    /**
     * 构造
     *
     * @param factory {@link javax.xml.parsers.SAXParserFactory}
     * @param source  XML源，可以是文件、流、路径等
     */
    public XmlSaxReader(final javax.xml.parsers.SAXParserFactory factory, final InputSource source) {
        this.factory = factory;
        this.source = source;
    }

    /**
     * 创建XmlSaxReader，使用全局{@link javax.xml.parsers.SAXParserFactory}
     *
     * @param source XML源，可以是文件、流、路径等
     * @return XmlSaxReader
     */
    public static XmlSaxReader of(final InputSource source) {
        return of(SAXParserFactory.getFactory(), source);
    }

    /**
     * 创建XmlSaxReader
     *
     * @param factory {@link javax.xml.parsers.SAXParserFactory}
     * @param source  XML源，可以是文件、流、路径等
     * @return XmlSaxReader
     */
    public static XmlSaxReader of(final javax.xml.parsers.SAXParserFactory factory, final InputSource source) {
        return new XmlSaxReader(factory, source);
    }

    /**
     * 读取内容
     *
     * @param contentHandler XML流处理器，用于按照Element处理xml
     */
    public void read(final ContentHandler contentHandler) {
        final SAXParser parse;
        final XMLReader reader;
        try {
            parse = factory.newSAXParser();
            if (contentHandler instanceof DefaultHandler) {
                parse.parse(source, (DefaultHandler) contentHandler);
                return;
            }

            // 得到解读器
            reader = XXE.disableXXE(parse.getXMLReader());
            reader.setContentHandler(contentHandler);
            reader.parse(source);
        } catch (final ParserConfigurationException | SAXException e) {
            throw new InternalException(e);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

}
