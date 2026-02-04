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
package org.miaixz.bus.image.metric.hl7;

import java.io.IOException;
import java.io.Writer;

import org.miaixz.bus.core.lang.Symbol;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class HL7ContentHandler extends DefaultHandler {

    private final Writer writer;
    private final char[] escape = { Symbol.C_BACKSLASH, 0, Symbol.C_BACKSLASH };
    private final char[] delimiters = Delimiter.DEFAULT.toCharArray();
    private boolean ignoreCharacters = true;

    public HL7ContentHandler(Writer writer) {
        this.writer = writer;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        try {
            switch (qName.charAt(0)) {
                case 'f':
                    if (qName.equals("field")) {
                        writer.write(delimiters[0]);
                        ignoreCharacters = false;
                        return;
                    }
                    break;

                case 'c':
                    if (qName.equals("component")) {
                        writer.write(delimiters[1]);
                        ignoreCharacters = false;
                        return;
                    }
                    break;

                case 'r':
                    if (qName.equals("repeat")) {
                        writer.write(delimiters[2]);
                        ignoreCharacters = false;
                        return;
                    }
                    break;

                case 'e':
                    if (qName.equals("escape")) {
                        writer.write(delimiters[3]);
                        ignoreCharacters = false;
                        return;
                    }
                    break;

                case 's':
                    if (qName.equals("subcomponent")) {
                        writer.write(delimiters[4]);
                        ignoreCharacters = false;
                        return;
                    }
                    break;

                case 'M':
                    if (qName.equals("MSH")) {
                        startHeaderSegment(qName, atts);
                        return;
                    }
                    break;

                case 'B':
                    if (qName.equals("BHS")) {
                        startHeaderSegment(qName, atts);
                        return;
                    }
                    break;

                case 'F':
                    if (qName.equals("FHS")) {
                        startHeaderSegment(qName, atts);
                        return;
                    }
                    break;

                case 'h':
                    if (qName.equals("hl7"))
                        return;
            }
            writer.write(qName);
        } catch (Exception e) {
            throw new SAXException(e);
        }
    }

    private void startHeaderSegment(String seg, Attributes atts) throws IOException {
        Delimiter[] values = Delimiter.values();
        for (int i = 0; i < values.length; i++) {
            String value = atts.getValue(values[i].attribute());
            if (value != null)
                delimiters[i] = value.charAt(0);
        }
        this.escape[0] = this.escape[2] = delimiters[3];
        writer.write(seg);
        writer.write(delimiters);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        ignoreCharacters = true;
        try {
            switch (qName.charAt(0)) {
                case 'f':
                    if (qName.equals("field"))
                        return;
                    break;

                case 'c':
                    if (qName.equals("component"))
                        return;
                    break;

                case 'r':
                    if (qName.equals("repeat"))
                        return;
                    break;

                case 'e':
                    if (qName.equals("escape")) {
                        writer.write(delimiters[3]);
                        ignoreCharacters = false;
                        return;
                    }
                    break;

                case 's':
                    if (qName.equals("subcomponent"))
                        return;
                    break;

                case 'h':
                    if (qName.equals("hl7")) {
                        writer.flush();
                        return;
                    }
            }
            writer.write(Symbol.C_CR);
        } catch (Exception e) {
            throw new SAXException(e);
        }
    }

    @Override
    public void characters(char[] cbuf, int start, int length) throws SAXException {
        if (ignoreCharacters)
            return;

        try {
            int off = start;
            int end = start + length;
            char c;
            char[] delims = delimiters;
            for (int i = start; i < end; i++) {
                c = cbuf[i];
                for (int j = 0; j < delims.length; j++) {
                    if (c == delims[j]) {
                        writer.write(cbuf, off, i - off);
                        off = i + 1;
                        escape(j);
                        break;
                    }
                }
            }
            writer.write(cbuf, off, end - off);
        } catch (Exception e) {
            throw new SAXException(e);
        }
    }

    private void escape(int delimIndex) throws IOException {
        escape[1] = Delimiter.ESCAPE.charAt(delimIndex);
        writer.write(escape);
    }

}
