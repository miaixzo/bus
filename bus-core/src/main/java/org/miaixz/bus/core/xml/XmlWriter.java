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
 * XML writer for generating XML content.
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
     * Constructs a new XmlWriter with the specified XML source.
     *
     * @param source The XML data source.
     */
    public XmlWriter(final Source source) {
        this.source = source;
    }

    /**
     * Creates a new XmlWriter instance from the given XML node.
     *
     * @param node The XML {@link Node} (document or element).
     * @return A new {@link XmlWriter} instance.
     */
    public static XmlWriter of(final Node node) {
        return of(new DOMSource(node));
    }

    /**
     * Creates a new XmlWriter instance from the given XML source.
     *
     * @param source The XML data source.
     * @return A new {@link XmlWriter} instance.
     */
    public static XmlWriter of(final Source source) {
        return new XmlWriter(source);
    }

    /**
     * Sets the character encoding for the output.
     *
     * @param charset The character encoding. If {@code null}, it will be ignored.
     * @return This {@link XmlWriter} instance.
     */
    public XmlWriter setCharset(final java.nio.charset.Charset charset) {
        if (null != charset) {
            this.charset = charset;
        }
        return this;
    }

    /**
     * Sets the number of spaces to use for indentation.
     *
     * @param indent The number of spaces for indentation.
     * @return This {@link XmlWriter} instance.
     */
    public XmlWriter setIndent(final int indent) {
        this.indent = indent;
        return this;
    }

    /**
     * Sets whether to omit the XML declaration.
     *
     * @param omitXmlDeclaration {@code true} to omit the XML declaration, {@code false} otherwise.
     * @return This {@link XmlWriter} instance.
     */
    public XmlWriter setOmitXmlDeclaration(final boolean omitXmlDeclaration) {
        this.omitXmlDeclaration = omitXmlDeclaration;
        return this;
    }

    /**
     * Gets the XML content as a string.
     *
     * @return The XML string.
     */
    public String getString() {
        final StringWriter writer = StringKit.getWriter();
        write(writer);
        return writer.toString();
    }

    /**
     * Writes the XML document to a file.
     *
     * @param file The target file.
     */
    public void write(final File file) {
        write(new StreamResult(file));
    }

    /**
     * Writes the XML document to a writer.
     *
     * @param writer The target writer.
     */
    public void write(final Writer writer) {
        write(new StreamResult(writer));
    }

    /**
     * Writes the XML document to an output stream.
     *
     * @param out The target output stream.
     */
    public void write(final OutputStream out) {
        write(new StreamResult(out));
    }

    /**
     * Writes the XML document to the specified result, with formatting. For pretty-printing logic, see:
     * <a href="https://stackoverflow.com/questions/139076/how-to-pretty-print-xml-from-java">Stack Overflow</a>
     *
     * @param result The target result.
     * @throws InternalException if an error occurs during transformation.
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
