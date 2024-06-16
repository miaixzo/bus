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
package org.miaixz.bus.image.metric.internal.hl7;

import org.miaixz.bus.core.lang.Symbol;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.Writer;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class HL7ContentHandler extends DefaultHandler {

    private final Writer writer;
    private final char[] escape = {Symbol.C_BACKSLASH, 0, Symbol.C_BACKSLASH};
    private final char[] delimiters = Delimiter.DEFAULT.toCharArray();
    private boolean ignoreCharacters = true;

    public HL7ContentHandler(Writer writer) {
        this.writer = writer;
    }

    @Override
    public void startElement(String uri, String localName, String qName,
                             Attributes atts) throws SAXException {
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
            if (null != value)
                delimiters[i] = value.charAt(0);
        }
        this.escape[0] = this.escape[2] = delimiters[3];
        writer.write(seg);
        writer.write(delimiters);
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        ignoreCharacters = true;
        try {
            switch (qName.charAt(0)) {
                case 'f':
                    if (qName.equals("field")) return;
                    break;
                case 'c':
                    if (qName.equals("component")) return;
                    break;
                case 'r':
                    if (qName.equals("repeat")) return;
                    break;
                case 'e':
                    if (qName.equals("escape")) {
                        writer.write(delimiters[3]);
                        ignoreCharacters = false;
                        return;
                    }
                    break;
                case 's':
                    if (qName.equals("subcomponent")) return;
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
    public void characters(char[] cbuf, int start, int length)
            throws SAXException {
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
