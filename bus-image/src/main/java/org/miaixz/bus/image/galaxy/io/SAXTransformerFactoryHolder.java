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

import javax.xml.XMLConstants;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;

import org.miaixz.bus.image.Builder;

/**
 * Holds the shared SAX transformer factory.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class SAXTransformerFactoryHolder {

    /**
     * The factory value.
     */
    public static final SAXTransformerFactory factory;

    static {
        factory = (SAXTransformerFactory) TransformerFactory.newInstance();
        try {
            factory.setFeature(
                    XMLConstants.FEATURE_SECURE_PROCESSING,
                    !"false".equalsIgnoreCase(
                            Builder.getPropertyOrEnv(
                                    "javax.xml.featureSecureProcessing",
                                    "JAVAX_XML_FEATURE_SECURE_PROCESSING",
                                    null)));
        } catch (TransformerConfigurationException e) {
            throw new AssertionError(
                    "All implementations are required to support the XMLConstants.FEATURE_SECURE_PROCESSING feature",
                    e);
        }
        factory.setAttribute(
                XMLConstants.ACCESS_EXTERNAL_STYLESHEET,
                Builder.getPropertyOrEnv(
                        "javax.xml.accessExternalStylesheet",
                        "JAVAX_XML_ACCESS_EXTERNAL_STYLESHEET",
                        "file"));
    }

}
