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
package org.miaixz.bus.image.galaxy.io;

import javax.xml.transform.*;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;

import org.miaixz.bus.image.galaxy.data.Attributes;
import org.xml.sax.SAXException;

/**
 * @author Kimi Liu
 * @since Java 21+
 */
public abstract class SAXTransformer {

    private static final SAXTransformerFactory factory = (SAXTransformerFactory) TransformerFactory.newInstance();

    public static SAXWriter getSAXWriter(Templates templates, Attributes result)
            throws TransformerConfigurationException {
        return getSAXWriter(templates, result, null);
    }

    public static SAXWriter getSAXWriter(Templates templates, Attributes result, SetupTransformer setup)
            throws TransformerConfigurationException {
        return getSAXWriter(templates, new SAXResult(new ContentHandlerAdapter(result)), setup);
    }

    public static SAXWriter getSAXWriter(Templates templates, Result result) throws TransformerConfigurationException {
        return getSAXWriter(templates, result, null);
    }

    public static SAXWriter getSAXWriter(Templates templates, Result result, SetupTransformer setup)
            throws TransformerConfigurationException {
        return getSAXWriter(factory.newTransformerHandler(templates), result, setup);
    }

    public static SAXWriter getSAXWriter(Result result) throws TransformerConfigurationException {
        return getSAXWriter(result, null);
    }

    public static SAXWriter getSAXWriter(Result result, SetupTransformer setup)
            throws TransformerConfigurationException {
        return getSAXWriter(factory.newTransformerHandler(), result, setup);
    }

    private static SAXWriter getSAXWriter(TransformerHandler th, Result result, SetupTransformer setup) {
        th.setResult(result);
        if (setup != null)
            setup.setup(th.getTransformer());
        return new SAXWriter(th);
    }

    public static Attributes transform(
            Attributes ds,
            Templates templates,
            boolean includeNameSpaceDeclaration,
            boolean includeKeword) throws SAXException, TransformerConfigurationException {
        return transform(ds, templates, includeNameSpaceDeclaration, includeKeword, null);
    }

    public static Attributes transform(
            Attributes ds,
            Templates templates,
            boolean includeNameSpaceDeclaration,
            boolean includeKeword,
            SetupTransformer setup) throws SAXException, TransformerConfigurationException {
        Attributes modify = new Attributes();
        SAXWriter w = SAXTransformer.getSAXWriter(templates, modify, setup);
        w.setIncludeNamespaceDeclaration(includeNameSpaceDeclaration);
        w.setIncludeKeyword(includeKeword);
        w.write(ds);
        return modify;
    }

    public static Templates newTemplates(Source source) throws TransformerConfigurationException {
        return factory.newTemplates(source);
    }

    public interface SetupTransformer {

        void setup(Transformer transformer);
    }

}
