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
package org.miaixz.bus.image.metric.hl7.net;

import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.VR;
import org.miaixz.bus.image.galaxy.io.ContentHandlerAdapter;
import org.miaixz.bus.image.galaxy.io.SAXTransformer;
import org.miaixz.bus.image.galaxy.io.SAXWriter;
import org.miaixz.bus.image.metric.hl7.HL7Charset;
import org.miaixz.bus.image.metric.hl7.HL7ContentHandler;
import org.miaixz.bus.image.metric.hl7.HL7Parser;
import org.xml.sax.SAXException;

import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import java.io.*;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class HL7SAXTransformer {

    private static final SAXTransformerFactory factory = (SAXTransformerFactory) TransformerFactory.newInstance();

    private HL7SAXTransformer() {
    }

    public static Attributes transform(byte[] data, String hl7charset, String dicomCharset, Templates templates,
                                       SAXTransformer.SetupTransformer setup)
            throws TransformerConfigurationException, IOException, SAXException {
        Attributes attrs = new Attributes();
        if (dicomCharset != null)
            attrs.setString(Tag.SpecificCharacterSet, VR.CS, dicomCharset);
        TransformerHandler th = factory.newTransformerHandler(templates);
        th.setResult(new SAXResult(new ContentHandlerAdapter(attrs)));
        if (setup != null)
            setup.setup(th.getTransformer());
        new HL7Parser(th).parse(new InputStreamReader(
                new ByteArrayInputStream(data),
                HL7Charset.toCharsetName(hl7charset)));
        return attrs;
    }

    public static byte[] transform(Attributes attrs, String hl7charset, Templates templates,
                                   boolean includeNameSpaceDeclaration, boolean includeKeword,
                                   SAXTransformer.SetupTransformer setup)
            throws TransformerConfigurationException, SAXException, UnsupportedEncodingException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        TransformerHandler th = factory.newTransformerHandler(templates);
        th.setResult(new SAXResult(new HL7ContentHandler(new OutputStreamWriter(out, HL7Charset.toCharsetName(hl7charset)))));
        if (setup != null)
            setup.setup(th.getTransformer());

        SAXWriter saxWriter = new SAXWriter(th);
        saxWriter.setIncludeKeyword(includeKeword);
        saxWriter.setIncludeNamespaceDeclaration(includeNameSpaceDeclaration);
        saxWriter.write(attrs);

        return out.toByteArray();
    }

}
