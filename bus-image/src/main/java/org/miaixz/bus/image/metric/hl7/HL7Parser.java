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
package org.miaixz.bus.image.metric.hl7;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.EnumSet;
import java.util.StringTokenizer;

import org.miaixz.bus.core.lang.Normal;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class HL7Parser {

    private static final String NAMESPACE = "http://aurora.regenstrief.org/xhl7";
    private final ContentHandler ch;
    private final AttributesImpl atts = new AttributesImpl();
    private final EnumSet<Delimiter> open = EnumSet.noneOf(Delimiter.class);
    private String namespace = Normal.EMPTY;
    private String delimiters;

    public HL7Parser(ContentHandler ch) {
        this.ch = ch;
    }

    public final boolean isIncludeNamespaceDeclaration() {
        return namespace == NAMESPACE;
    }

    public final void setIncludeNamespaceDeclaration(boolean includeNameSpaceDeclaration) {
        this.namespace = includeNameSpaceDeclaration ? NAMESPACE : Normal.EMPTY;
    }

    public void parse(Reader reader) throws IOException, SAXException {
        parse(reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader));
    }

    public void parse(BufferedReader reader) throws IOException, SAXException {
        startDocument();
        delimiters = Delimiter.DEFAULT;
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.length() == 0)
                continue;

            if (line.length() < 3)
                throw new IOException("Segment to short: " + line);

            String seg = line.substring(0, 3);
            String[] tks;
            int tkindex = 0;
            if (isHeaderSegment(line)) {
                if (line.length() < 8)
                    throw new IOException("Header Segment to short: " + line);

                seg = line.substring(0, 3);
                setDelimiters(line.substring(3, 8));
                tks = tokenize(line.substring(8));
            } else {
                tks = tokenize(line);
                seg = tks[tkindex++];
            }
            startElement(seg);
            while (tkindex < tks.length) {
                String tk = tks[tkindex++];
                Delimiter d = delimiter(tk);
                if (d != null) {
                    if (d != Delimiter.escape) {
                        endElement(d);
                        startElement(d);
                        continue;
                    }
                    if (tks.length > tkindex + 1 && tks[tkindex + 1].equals(tk)) {
                        tk = tks[tkindex++];
                        int e = escapeIndex(tk);
                        if (e >= 0) {
                            ch.characters(delimiters.toCharArray(), e, 1);
                        } else {
                            startElement(Delimiter.escape.name());
                            ch.characters(tk.toCharArray(), 0, tk.length());
                            endElement(Delimiter.escape.name());
                        }
                        tkindex++;
                        continue;
                    }
                }
                ch.characters(tk.toCharArray(), 0, tk.length());
            }
            endElement(Delimiter.field);
            endElement(seg);
        }
        endDocument();
    }

    private boolean isHeaderSegment(String line) {
        return (line.startsWith("MSH") || line.startsWith("BHS") || line.startsWith("FHS"));
    }

    private void startDocument() throws SAXException {
        ch.startDocument();
        addAttribute("xml-space", "preserved");
        startElement("hl7");
    }

    private void endDocument() throws SAXException {
        endElement("hl7");
        ch.endDocument();
    }

    private void setDelimiters(String delimiters) {
        Delimiter[] a = Delimiter.values();
        for (int i = 0; i < a.length; i++)
            addAttribute(a[i].attribute(), delimiters.substring(i, i + 1));
        this.delimiters = delimiters;
    }

    private void addAttribute(String name, String value) {
        atts.addAttribute(namespace, name, name, "NMTOKEN", value);
    }

    private Delimiter delimiter(String tk) {
        if (tk.length() != 1)
            return null;

        int index = delimiters.indexOf(tk.charAt(0));
        return index >= 0 ? Delimiter.values()[index] : null;
    }

    private int escapeIndex(String tk) {
        return tk.length() != 1 ? Delimiter.ESCAPE.indexOf(tk.charAt(0)) : -1;
    }

    private String[] tokenize(String s) {
        StringTokenizer stk = new StringTokenizer(s, delimiters, true);
        String[] tks = new String[stk.countTokens()];
        for (int i = 0; i < tks.length; i++)
            tks[i] = stk.nextToken();

        return tks;
    }

    private void startElement(Delimiter d) throws SAXException {
        startElement(d.name());
        open.add(d);
    }

    private void startElement(String name) throws SAXException {
        ch.startElement(namespace, name, name, atts);
        atts.clear();
    }

    private void endElement(Delimiter delimiter) throws SAXException {
        Delimiter d = Delimiter.escape;
        do
            if (open.remove(d = d.parent()))
                endElement(d.name());
        while (d != delimiter);
    }

    private void endElement(String name) throws SAXException {
        ch.endElement(namespace, name, name);
    }

}
