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
package org.miaixz.bus.image.metric.hl7.net;

import java.io.*;

import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;

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

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class HL7SAXTransformer {

    private static final SAXTransformerFactory factory = (SAXTransformerFactory) TransformerFactory.newInstance();

    private HL7SAXTransformer() {
    }

    public static Attributes transform(
            byte[] data,
            String hl7charset,
            String dicomCharset,
            Templates templates,
            SAXTransformer.SetupTransformer setup) throws TransformerConfigurationException, IOException, SAXException {
        Attributes attrs = new Attributes();
        if (dicomCharset != null)
            attrs.setString(Tag.SpecificCharacterSet, VR.CS, dicomCharset);
        TransformerHandler th = factory.newTransformerHandler(templates);
        th.setResult(new SAXResult(new ContentHandlerAdapter(attrs)));
        if (setup != null)
            setup.setup(th.getTransformer());
        new HL7Parser(th)
                .parse(new InputStreamReader(new ByteArrayInputStream(data), HL7Charset.toCharsetName(hl7charset)));
        return attrs;
    }

    public static byte[] transform(
            Attributes attrs,
            String hl7charset,
            Templates templates,
            boolean includeNameSpaceDeclaration,
            boolean includeKeword,
            SAXTransformer.SetupTransformer setup)
            throws TransformerConfigurationException, SAXException, UnsupportedEncodingException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        TransformerHandler th = factory.newTransformerHandler(templates);
        th.setResult(
                new SAXResult(
                        new HL7ContentHandler(new OutputStreamWriter(out, HL7Charset.toCharsetName(hl7charset)))));
        if (setup != null)
            setup.setup(th.getTransformer());

        SAXWriter saxWriter = new SAXWriter(th);
        saxWriter.setIncludeKeyword(includeKeword);
        saxWriter.setIncludeNamespaceDeclaration(includeNameSpaceDeclaration);
        saxWriter.write(attrs);

        return out.toByteArray();
    }

}
