/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.xml.sax.XMLReader;

/**
 * Utility class for fixing XXE vulnerabilities. See:
 * <a href="https.blog.spoock.com/2018/10/23/java-xxe/">https://blog.spoock.com/2018/10/23/java-xxe/</a>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class XXE {

    /**
     * Disables XXE to prevent vulnerability attacks. See: <a href=
     * "https://www.owasp.org/index.php/XML_External_Entity_(XXE)_Prevention_Cheat_Sheet#JAXP_DocumentBuilderFactory.2C_SAXParserFactory_and_DOM4J">
     * OWASP XXE Prevention Cheat Sheet</a>
     *
     * @param factory The {@link DocumentBuilderFactory} to configure.
     * @return The configured {@link DocumentBuilderFactory}.
     */
    public static DocumentBuilderFactory disableXXE(final DocumentBuilderFactory factory) {
        try {
            // This is the PRIMARY defense. If DTDs (doctypes) are disallowed, almost all XML entity attacks are
            // prevented.
            // Xerces 2 only - http://xerces.apache.org/xerces2-j/features.html#disallow-doctype-decl
            factory.setFeature(XmlFeatures.DISALLOW_DOCTYPE_DECL, true);
            // If you can't completely disable DTDs, then at least do the following:
            // Xerces 1 - http://xerces.apache.org/xerces-j/features.html#external-general-entities
            // Xerces 2 - http://xerces.apache.org/xerces2-j/features.html#external-general-entities
            // JDK7+ - http://xml.org/sax/features/external-general-entities
            factory.setFeature(XmlFeatures.EXTERNAL_GENERAL_ENTITIES, false);
            // Xerces 1 - http://xerces.apache.org/xerces-j/features.html#external-parameter-entities
            // Xerces 2 - http://xerces.apache.org/xerces2-j/features.html#external-parameter-entities
            // JDK7+ - http://xml.org/sax/features/external-parameter-entities
            factory.setFeature(XmlFeatures.EXTERNAL_PARAMETER_ENTITIES, false);
            // Disable external DTDs as well
            factory.setFeature(XmlFeatures.LOAD_EXTERNAL_DTD, false);
        } catch (final ParserConfigurationException e) {
            // ignore
        }

        // and these as well, per Timothy Morgan's 2014 paper: "XML Schema, DTD, and Entity Attacks"
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);

        return factory;
    }

    /**
     * Disables XXE to prevent vulnerability attacks.
     *
     * @param factory The {@link SAXParserFactory} to configure.
     * @return The configured {@link SAXParserFactory}.
     */
    public static SAXParserFactory disableXXE(final SAXParserFactory factory) {
        try {
            factory.setFeature(XmlFeatures.DISALLOW_DOCTYPE_DECL, true);
            factory.setFeature(XmlFeatures.EXTERNAL_GENERAL_ENTITIES, false);
            factory.setFeature(XmlFeatures.EXTERNAL_PARAMETER_ENTITIES, false);
            factory.setFeature(XmlFeatures.LOAD_EXTERNAL_DTD, false);
        } catch (final Exception ignore) {
            // ignore
        }

        factory.setXIncludeAware(false);

        return factory;
    }

    /**
     * Disables XXE to prevent vulnerability attacks.
     *
     * @param reader The {@link XMLReader} to configure.
     * @return The configured {@link XMLReader}.
     */
    public static XMLReader disableXXE(final XMLReader reader) {
        try {
            reader.setFeature(XmlFeatures.DISALLOW_DOCTYPE_DECL, true);
            reader.setFeature(XmlFeatures.EXTERNAL_GENERAL_ENTITIES, false);
            reader.setFeature(XmlFeatures.EXTERNAL_PARAMETER_ENTITIES, false);
            reader.setFeature(XmlFeatures.LOAD_EXTERNAL_DTD, false);
        } catch (final Exception ignore) {
            // ignore
        }

        return reader;
    }

    /**
     * Disables XXE to prevent vulnerability attacks.
     *
     * @param factory The {@link TransformerFactory} to configure.
     * @return The configured {@link TransformerFactory}.
     */
    public static TransformerFactory disableXXE(final TransformerFactory factory) {
        try {
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        } catch (final TransformerConfigurationException e) {
            throw new InternalException(e);
        }
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, Normal.EMPTY);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, Normal.EMPTY);
        return factory;
    }

    /**
     * Disables XXE to prevent vulnerability attacks.
     *
     * @param validator The {@link Validator} to configure.
     * @return The configured {@link Validator}.
     */
    public static Validator disableXXE(final Validator validator) {
        try {
            validator.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, Normal.EMPTY);
            validator.setProperty(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, Normal.EMPTY);
        } catch (final Exception ignore) {
            // ignore
        }
        return validator;
    }

    /**
     * Disables XXE to prevent vulnerability attacks.
     *
     * @param factory The {@link SAXTransformerFactory} to configure.
     * @return The configured {@link SAXTransformerFactory}.
     */
    public static SAXTransformerFactory disableXXE(final SAXTransformerFactory factory) {
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, Normal.EMPTY);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, Normal.EMPTY);
        return factory;
    }

    /**
     * Disables XXE to prevent vulnerability attacks.
     *
     * @param factory The {@link SchemaFactory} to configure.
     * @return The configured {@link SchemaFactory}.
     */
    public static SchemaFactory disableXXE(final SchemaFactory factory) {
        try {
            factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, Normal.EMPTY);
            factory.setProperty(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, Normal.EMPTY);
        } catch (final Exception ignore) {
            // ignore
        }
        return factory;
    }

}
