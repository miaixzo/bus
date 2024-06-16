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
package org.miaixz.bus.image.galaxy.io;

import org.miaixz.bus.image.galaxy.data.Attributes;
import org.xml.sax.SAXException;

import javax.xml.transform.*;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class SAXTransformer {

    private static final SAXTransformerFactory factory =
            (SAXTransformerFactory) TransformerFactory.newInstance();

    public static SAXWriter getSAXWriter(Templates templates, Attributes result)
            throws TransformerConfigurationException {
        return getSAXWriter(templates, result, null);
    }

    public static SAXWriter getSAXWriter(Templates templates, Attributes result,
                                         SetupTransformer setup)
            throws TransformerConfigurationException {
        return getSAXWriter(templates,
                new SAXResult(new ContentHandlerAdapter(result)),
                setup);
    }

    public static SAXWriter getSAXWriter(Templates templates, Result result)
            throws TransformerConfigurationException {
        return getSAXWriter(templates, result, null);
    }

    public static SAXWriter getSAXWriter(Templates templates, Result result,
                                         SetupTransformer setup)
            throws TransformerConfigurationException {
        return getSAXWriter(factory.newTransformerHandler(templates),
                result,
                setup);
    }

    public static SAXWriter getSAXWriter(Result result)
            throws TransformerConfigurationException {
        return getSAXWriter(result, null);
    }

    public static SAXWriter getSAXWriter(Result result, SetupTransformer setup)
            throws TransformerConfigurationException {
        return getSAXWriter(factory.newTransformerHandler(), result, setup);
    }

    private static SAXWriter getSAXWriter(TransformerHandler th, Result result,
                                          SetupTransformer setup) {
        th.setResult(result);
        if (null != setup)
            setup.setup(th.getTransformer());
        return new SAXWriter(th);
    }

    public static Attributes transform(Attributes ds, Templates templates,
                                       boolean includeNameSpaceDeclaration, boolean includeKeword)
            throws SAXException, TransformerConfigurationException {
        return transform(ds, templates,
                includeNameSpaceDeclaration, includeKeword, null);
    }

    public static Attributes transform(Attributes ds, Templates templates,
                                       boolean includeNameSpaceDeclaration, boolean includeKeword,
                                       SetupTransformer setup)
            throws SAXException, TransformerConfigurationException {
        Attributes modify = new Attributes();
        SAXWriter w = SAXTransformer.getSAXWriter(
                templates, modify, setup);
        w.setIncludeNamespaceDeclaration(includeNameSpaceDeclaration);
        w.setIncludeKeyword(includeKeword);
        w.write(ds);
        return modify;
    }

    public static Templates newTemplates(Source source)
            throws TransformerConfigurationException {
        return factory.newTemplates(source);
    }

    public interface SetupTransformer {
        void setup(Transformer transformer);
    }

}
