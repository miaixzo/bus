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

    /**
     * The XML data source to be written.
     */
    private final Source source;

    /**
     * The character encoding for the output.
     */
    private java.nio.charset.Charset charset = Charset.UTF_8;

    /**
     * The number of spaces to use for indentation.
     */
    private int indent;

    /**
     * Whether to omit the XML declaration in the output.
     */
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
