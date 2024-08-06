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

import java.io.File;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.w3c.dom.Node;

/**
 * XML生成器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class XmlWriter {

    private final Source source;
    private java.nio.charset.Charset charset = Charset.UTF_8;
    private int indent;
    private boolean omitXmlDeclaration;

    /**
     * 构造
     *
     * @param source XML数据源
     */
    public XmlWriter(final Source source) {
        this.source = source;
    }

    /**
     * 构建XmlWriter
     *
     * @param node {@link Node} XML文档节点或文档本身
     * @return XmlWriter
     */
    public static XmlWriter of(final Node node) {
        return of(new DOMSource(node));
    }

    /**
     * 构建XmlWriter
     *
     * @param source XML数据源
     * @return XmlWriter
     */
    public static XmlWriter of(final Source source) {
        return new XmlWriter(source);
    }

    /**
     * 设置编码
     *
     * @param charset 编码，null跳过
     * @return this
     */
    public XmlWriter setCharset(final java.nio.charset.Charset charset) {
        if (null != charset) {
            this.charset = charset;
        }
        return this;
    }

    /**
     * 设置缩进
     *
     * @param indent 缩进
     * @return this
     */
    public XmlWriter setIndent(final int indent) {
        this.indent = indent;
        return this;
    }

    /**
     * 设置是否输出 xml Declaration
     *
     * @param omitXmlDeclaration 是否输出 xml Declaration
     * @return this
     */
    public XmlWriter setOmitXmlDeclaration(final boolean omitXmlDeclaration) {
        this.omitXmlDeclaration = omitXmlDeclaration;
        return this;
    }

    /**
     * 获得XML字符串
     *
     * @return XML字符串
     */
    public String getString() {
        final StringWriter writer = StringKit.getWriter();
        write(writer);
        return writer.toString();
    }

    /**
     * 将XML文档写出
     *
     * @param file 目标
     */
    public void write(final File file) {
        write(new StreamResult(file));
    }

    /**
     * 将XML文档写出
     *
     * @param writer 目标
     */
    public void write(final Writer writer) {
        write(new StreamResult(writer));
    }

    /**
     * 将XML文档写出
     *
     * @param out 目标
     */
    public void write(final OutputStream out) {
        write(new StreamResult(out));
    }

    /**
     * 将XML文档写出 格式化输出逻辑参考：https://stackoverflow.com/questions/139076/how-to-pretty-print-xml-from-java
     *
     * @param result 目标
     */
    public void write(final Result result) {
        final TransformerFactory factory = XXE.disableXXE(TransformerFactory.newInstance());
        try {
            final Transformer xformer = factory.newTransformer();
            if (indent > 0) {
                xformer.setOutputProperty(OutputKeys.INDENT, "yes");
                xformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "yes");
                xformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", String.valueOf(indent));
            }
            if (ObjectKit.isNotNull(this.charset)) {
                xformer.setOutputProperty(OutputKeys.ENCODING, charset.name());
            }
            if (omitXmlDeclaration) {
                xformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            }
            xformer.transform(source, result);
        } catch (final Exception e) {
            throw new InternalException(e, "Trans xml document to string error!");
        }
    }

}
