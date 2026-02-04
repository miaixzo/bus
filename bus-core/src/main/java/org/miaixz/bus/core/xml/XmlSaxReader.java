/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.xml;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * XML SAX reader for parsing XML documents.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class XmlSaxReader {

    /**
     * The SAX parser factory used to create SAX parsers.
     */
    private final javax.xml.parsers.SAXParserFactory factory;

    /**
     * The XML input source to be parsed.
     */
    private final InputSource source;

    /**
     * Constructs a new XmlSaxReader.
     *
     * @param factory The {@link javax.xml.parsers.SAXParserFactory} to use.
     * @param source  The XML source, which can be a file, stream, path, etc.
     */
    public XmlSaxReader(final javax.xml.parsers.SAXParserFactory factory, final InputSource source) {
        this.factory = factory;
        this.source = source;
    }

    /**
     * Creates a new XmlSaxReader using the global default {@link javax.xml.parsers.SAXParserFactory}.
     *
     * @param source The XML source, which can be a file, stream, path, etc.
     * @return A new {@link XmlSaxReader} instance.
     */
    public static XmlSaxReader of(final InputSource source) {
        return of(SAXParserFactory.getFactory(), source);
    }

    /**
     * Creates a new XmlSaxReader.
     *
     * @param factory The {@link javax.xml.parsers.SAXParserFactory} to use.
     * @param source  The XML source, which can be a file, stream, path, etc.
     * @return A new {@link XmlSaxReader} instance.
     */
    public static XmlSaxReader of(final javax.xml.parsers.SAXParserFactory factory, final InputSource source) {
        return new XmlSaxReader(factory, source);
    }

    /**
     * Reads the XML content using the provided {@link ContentHandler}.
     *
     * @param contentHandler The XML stream handler for processing XML elements.
     * @throws InternalException if a {@link ParserConfigurationException}, {@link SAXException}, or {@link IOException}
     *                           occurs.
     */
    public void read(final ContentHandler contentHandler) {
        final SAXParser parse;
        final XMLReader reader;
        try {
            parse = factory.newSAXParser();
            if (contentHandler instanceof DefaultHandler) {
                parse.parse(source, (DefaultHandler) contentHandler);
                return;
            }

            // Get the XML reader
            reader = XXE.disableXXE(parse.getXMLReader());
            reader.setContentHandler(contentHandler);
            reader.parse(source);
        } catch (final ParserConfigurationException | SAXException e) {
            throw new InternalException(e);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

}
