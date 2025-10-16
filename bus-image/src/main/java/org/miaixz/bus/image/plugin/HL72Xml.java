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
package org.miaixz.bus.image.plugin;

import org.miaixz.bus.image.metric.hl7.HL7Charset;
import org.miaixz.bus.image.metric.hl7.HL7Parser;
import org.miaixz.bus.image.metric.hl7.HL7Segment;
import org.xml.sax.SAXException;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.URL;

/**
 * The {@code HL72Xml} class provides functionality to convert an HL7 message into its XML representation. It can
 * optionally apply an XSLT transformation during the conversion process.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class HL72Xml {

    /**
     * The URL of the XSLT stylesheet to apply.
     */
    private URL xslt;
    /**
     * Whether to format the XML output with indentation.
     */
    private boolean indent = false;
    /**
     * Whether to include the XML namespace declaration.
     */
    private boolean includeNamespaceDeclaration = false;
    /**
     * The character set to use if not specified in the MSH segment.
     */
    private String charset;

    /**
     * Sets the URL of an XSLT stylesheet to be applied to the generated XML.
     *
     * @param xslt The URL of the XSLT stylesheet.
     */
    public final void setXSLT(URL xslt) {
        this.xslt = xslt;
    }

    /**
     * Sets whether to indent the XML output for pretty-printing.
     *
     * @param indent {@code true} to enable indentation, {@code false} otherwise.
     */
    public final void setIndent(boolean indent) {
        this.indent = indent;
    }

    /**
     * Sets whether to include the XML namespace declaration in the root element.
     *
     * @param includeNamespaceDeclaration {@code true} to include the namespace, {@code false} otherwise.
     */
    public final void setIncludeNamespaceDeclaration(boolean includeNamespaceDeclaration) {
        this.includeNamespaceDeclaration = includeNamespaceDeclaration;
    }

    /**
     * Gets the default character set.
     *
     * @return The name of the default character set.
     */
    public String getCharacterSet() {
        return charset;
    }

    /**
     * Sets the default character set to be used if it's not specified in the MSH-18 field of the HL7 message.
     *
     * @param charset The name of the character set.
     */
    public void setCharacterSet(String charset) {
        this.charset = charset;
    }

    /**
     * Parses an HL7 message from an input stream and writes the corresponding XML representation to standard output. It
     * automatically detects the character set from the MSH segment.
     *
     * @param is The input stream containing the HL7 message.
     * @throws IOException                       if an I/O error occurs during reading.
     * @throws TransformerConfigurationException if there is a problem with the transformer configuration.
     * @throws SAXException                      if a SAX error occurs during parsing.
     */
    public void parse(InputStream is) throws IOException, TransformerConfigurationException, SAXException {
        byte[] buf = new byte[256];
        int len = is.read(buf);
        HL7Segment msh = HL7Segment.parseMSH(buf, buf.length);
        String charsetName = HL7Charset.toCharsetName(msh.getField(17, charset));
        Reader reader = new InputStreamReader(new SequenceInputStream(new ByteArrayInputStream(buf, 0, len), is),
                charsetName);
        TransformerHandler th = getTransformerHandler();
        th.getTransformer().setOutputProperty(OutputKeys.INDENT, indent ? "yes" : "no");
        th.setResult(new StreamResult(System.out));
        HL7Parser hl7Parser = new HL7Parser(th);
        hl7Parser.setIncludeNamespaceDeclaration(includeNamespaceDeclaration);
        hl7Parser.parse(reader);
    }

    /**
     * Creates and configures a {@link TransformerHandler} for SAX-based XML processing. If an XSLT URL is provided, the
     * handler is configured to apply the transformation.
     *
     * @return A configured {@code TransformerHandler}.
     * @throws TransformerConfigurationException if a suitable {@code TransformerFactory} cannot be created or if the
     *                                           handler cannot be created.
     * @throws IOException                       if an I/O error occurs when opening the XSLT stream.
     */
    private TransformerHandler getTransformerHandler() throws TransformerConfigurationException, IOException {
        SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();
        if (xslt == null)
            return tf.newTransformerHandler();

        return tf.newTransformerHandler(new StreamSource(xslt.openStream(), xslt.toExternalForm()));
    }

}
