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
package org.miaixz.bus.image.plugin;

import org.miaixz.bus.core.exception.InternalException;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.image.metric.internal.hl7.HL7Charset;
import org.miaixz.bus.image.metric.internal.hl7.HL7Parser;
import org.miaixz.bus.image.metric.internal.hl7.HL7Segment;
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
import java.util.List;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class HL72Xml {

    private URL xslt;
    private boolean indent = false;
    private boolean includeNamespaceDeclaration = false;
    private String charset;


    private static String fname(List<String> argList) throws InternalException {
        int numArgs = argList.size();
        if (numArgs == 0)
            throw new InternalException("missing");
        if (numArgs > 1)
            throw new InternalException("too-many");
        return argList.get(0);
    }

    public final void setXSLT(URL xslt) {
        this.xslt = xslt;
    }

    public final void setIndent(boolean indent) {
        this.indent = indent;
    }

    public final void setIncludeNamespaceDeclaration(boolean includeNamespaceDeclaration) {
        this.includeNamespaceDeclaration = includeNamespaceDeclaration;
    }

    public String getCharacterSet() {
        return charset;
    }

    public void setCharacterSet(String charset) {
        this.charset = charset;
    }

    public void parse(InputStream is) throws IOException,
            TransformerConfigurationException, SAXException {
        byte[] buf = new byte[Normal._256];
        int len = is.read(buf);
        HL7Segment msh = HL7Segment.parseMSH(buf, buf.length);
        String charsetName = HL7Charset.toCharsetName(msh.getField(17, charset));
        Reader reader = new InputStreamReader(
                new SequenceInputStream(
                        new ByteArrayInputStream(buf, 0, len), is),
                charsetName);
        TransformerHandler th = getTransformerHandler();
        th.getTransformer().setOutputProperty(OutputKeys.INDENT,
                indent ? "yes" : "no");
        th.setResult(new StreamResult(System.out));
        HL7Parser hl7Parser = new HL7Parser(th);
        hl7Parser.setIncludeNamespaceDeclaration(includeNamespaceDeclaration);
        hl7Parser.parse(reader);
    }

    private TransformerHandler getTransformerHandler()
            throws TransformerConfigurationException, IOException {
        SAXTransformerFactory tf = (SAXTransformerFactory)
                TransformerFactory.newInstance();
        if (null == xslt)
            return tf.newTransformerHandler();

        TransformerHandler th = tf.newTransformerHandler(
                new StreamSource(xslt.openStream(), xslt.toExternalForm()));
        return th;
    }

}
