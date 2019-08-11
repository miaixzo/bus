/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.storage.magic;

import org.aoju.bus.core.lang.exception.CommonException;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.*;


/**
 * @author Kimi Liu
 * @version 3.0.5
 * @since JDK 1.8
 */
public class MagicParser extends DefaultHandler implements ContentHandler, ErrorHandler {

    // Namespaces feature id (http://xml.org/sax/features/namespaces).
    protected static final String NAMESPACES_FEATURE_ID = "http://xml.org/sax/features/namespaces";
    // Validation feature id (http://xml.org/sax/features/validation).
    protected static final String VALIDATION_FEATURE_ID = "http://xml.org/sax/features/validation";
    // Schema validate feature id (http://apache.org/xml/features/validation/schema).
    protected static final String SCHEMA_VALIDATION_FEATURE_ID = "http://apache.org/xml/features/validation/schema";
    // Schema full checking feature id (http://apache.org/xml/features/validation/schema-full-checking).
    protected static final String SCHEMA_FULL_CHECKING_FEATURE_ID = "http://apache.org/xml/features/validation/schema-full-checking";
    // Default parser name.
    protected static final String DEFAULT_PARSER_NAME = "org.apache.xerces.parsers.SAXParser";
    // Default namespaces support (true).
    protected static final boolean DEFAULT_NAMESPACES = true;
    // Default validate support (false).
    protected static final boolean DEFAULT_VALIDATION = false;
    // Default Schema validate support (false).
    protected static final boolean DEFAULT_SCHEMA_VALIDATION = false;
    // Default Schema full checking support (false).
    protected static final boolean DEFAULT_SCHEMA_FULL_CHECKING = false;
    private static String magicFile = "/magic.xml";
    private boolean initialized = false;
    private XMLReader parser = null;
    private List<MagicMatcher> stack = new ArrayList<MagicMatcher>();
    private Collection<MagicMatcher> matchers = new ArrayList<MagicMatcher>();
    private MagicMatcher matcher = null;
    private MagicMatch match = null;
    private Map<String, String> properties = null;
    private String finalValue = "";
    private boolean isMimeType = false;
    private boolean isExtension = false;
    private boolean isDescription = false;
    private boolean isTest = false;

    /**
     * constructor
     */
    public MagicParser() {
    }

    /**
     * parse the xml file and create our MagicMatcher object list
     *
     * @throws CommonException DOCUMENT ME!
     */
    public synchronized void initialize()
            throws CommonException {
        boolean namespaces = DEFAULT_NAMESPACES;
        boolean validation = DEFAULT_VALIDATION;
        boolean schemaValidation = DEFAULT_SCHEMA_VALIDATION;
        boolean schemaFullChecking = DEFAULT_SCHEMA_FULL_CHECKING;

        if (!initialized) {
            // use default parser
            try {
                parser = XMLReaderFactory.createXMLReader();
            } catch (Exception e) {
                try {
                    parser = XMLReaderFactory.createXMLReader(DEFAULT_PARSER_NAME);
                } catch (Exception ee) {
                    throw new CommonException("unable to instantiate parser");
                }
            }

            // set parser features
            try {
                parser.setFeature(NAMESPACES_FEATURE_ID, namespaces);
            } catch (SAXException e) {
                throw new InstrumentException(e);
            }

            try {
                parser.setFeature(VALIDATION_FEATURE_ID, validation);
            } catch (SAXException e) {
                throw new InstrumentException(e);
            }

            try {
                parser.setFeature(SCHEMA_VALIDATION_FEATURE_ID, schemaValidation);
            } catch (SAXNotRecognizedException e) {
                throw new InstrumentException(e);
            } catch (SAXNotSupportedException e) {
                throw new InstrumentException(e);
            }

            try {
                parser.setFeature(SCHEMA_FULL_CHECKING_FEATURE_ID, schemaFullChecking);
            } catch (SAXNotRecognizedException e) {
                // ignore
            } catch (SAXNotSupportedException e) {
                e.printStackTrace();
            }

            // set handlers
            parser.setErrorHandler(this);
            parser.setContentHandler(this);

            // parse file
            try {
                // get the magic file URL
                URL resource = MagicParser.class.getResource(magicFile);
                String magicURL = resource.toString();

                if (magicURL == null) {
                    throw new CommonException("couldn't load '" + magicURL + "'");
                }

                parser.parse(magicURL);
            } catch (Exception e) {
                throw new InstrumentException("parse error occurred - " + e.getMessage());
            }
            initialized = true;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Collection<MagicMatcher> getMatchers() {
        return matchers;
    }

    /**
     * DOCUMENT ME!
     *
     * @throws SAXException DOCUMENT ME!
     */
    public void startDocument()
            throws SAXException {
    }

    /**
     * DOCUMENT ME!
     *
     * @throws SAXException DOCUMENT ME!
     */
    public void endDocument()
            throws SAXException {
    }

    /**
     * DOCUMENT ME!
     *
     * @param target DOCUMENT ME!
     * @param data   DOCUMENT ME!
     * @throws SAXException DOCUMENT ME!
     */
    public void processingInstruction(String target, String data)
            throws SAXException {
        // do nothing
    }

    /**
     * DOCUMENT ME!
     *
     * @param ch     DOCUMENT ME!
     * @param offset DOCUMENT ME!
     * @param length DOCUMENT ME!
     * @throws SAXException DOCUMENT ME!
     */
    public void characters(char[] ch, int offset, int length)
            throws SAXException {
        String value = new String(ch, offset, length);

        finalValue += value;
    }

    /**
     * DOCUMENT ME!
     *
     * @param ch     DOCUMENT ME!
     * @param offset DOCUMENT ME!
     * @param length DOCUMENT ME!
     * @throws SAXException DOCUMENT ME!
     */
    public void ignorableWhitespace(char[] ch, int offset, int length)
            throws SAXException {
        // do nothing
    }

    /**
     * DOCUMENT ME!
     *
     * @param uri        DOCUMENT ME!
     * @param localName  DOCUMENT ME!
     * @param qname      DOCUMENT ME!
     * @param attributes DOCUMENT ME!
     * @throws SAXException DOCUMENT ME!
     */
    public void startElement(String uri, String localName, String qname, Attributes attributes)
            throws SAXException {

        // create a new matcher
        if (localName.equals("match")) {
            // match to hold data
            match = new MagicMatch();
            // our matcher
            matcher = new MagicMatcher();
            matcher.setMatch(match);
        }

        // these are subelements of matcher, but also occur elsewhere
        if (matcher != null) {
            if (localName.equals("mimetype")) {
                isMimeType = true;
            } else if (localName.equals("extension")) {
                isExtension = true;
            } else if (localName.equals("description")) {
                isDescription = true;
            } else if (localName.equals("test")) {
                isTest = true;

                int length = attributes.getLength();

                for (int i = 0; i < length; i++) {
                    String attrLocalName = attributes.getLocalName(i);
                    String attrValue = attributes.getValue(i);

                    if (attrLocalName.equals("offset")) {
                        if (!attrValue.equals("")) {
                            match.setOffset(new Integer(attrValue).intValue());
                        }
                    } else if (attrLocalName.equals("length")) {
                        if (!attrValue.equals("")) {
                            match.setLength(new Integer(attrValue).intValue());
                        }
                    } else if (attrLocalName.equals("type")) {
                        match.setType(attrValue);
                    } else if (attrLocalName.equals("bitmask")) {
                        if (!attrValue.equals("")) {
                            match.setBitmask(attrValue);
                        }
                    } else if (attrLocalName.equals("comparator")) {
                        match.setComparator(attrValue);
                    }
                }
            } else if (localName.equals("property")) {
                int length = attributes.getLength();
                String name = null;
                String value = null;

                for (int i = 0; i < length; i++) {
                    String attrLocalName = attributes.getLocalName(i);
                    String attrValue = attributes.getValue(i);

                    if (attrLocalName.equals("name")) {
                        if (!attrValue.equals("")) {
                            name = attrValue;
                        }
                    } else if (attrLocalName.equals("value")) {
                        if (!attrValue.equals("")) {
                            value = attrValue;
                        }
                    }
                }

                // save the property to our map
                if ((name != null) && (value != null)) {
                    if (properties == null) {
                        properties = new HashMap<String, String>();
                    }

                    if (!properties.containsKey(name)) {
                        properties.put(name, value);
                    }
                }
            } else if (localName.equals("match-list")) {
                stack.add(matcher);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param uri       DOCUMENT ME!
     * @param localName DOCUMENT ME!
     * @param qname     DOCUMENT ME!
     * @throws SAXException DOCUMENT ME!
     */
    public void endElement(String uri, String localName, String qname)
            throws SAXException {

        // determine which tag these chars are for and save them
        if (isMimeType) {
            isMimeType = false;
            match.setMimeType(finalValue);
        } else if (isExtension) {
            isExtension = false;
            match.setExtension(finalValue);
        } else if (isDescription) {
            isDescription = false;
            match.setDescription(finalValue);
        } else if (isTest) {
            isTest = false;
            match.setTest(convertOctals(finalValue));
        }

        finalValue = "";

        // need to save the current matcher here if it is filled out enough and
        // we have an /matcher
        if (localName.equals("match")) {
            // FIXME - make sure the MagicMatcher isValid() test works
            if (matcher.isValid()) {
                // set the collected properties on this matcher
                match.setProperties(properties);

                // add root match
                if (stack.size() == 0) {
                    matchers.add(matcher);
                } else {
                    MagicMatcher m = (MagicMatcher) stack.get(stack.size() - 1);
                    m.addSubMatcher(matcher);
                }
            }

            matcher = null;
            properties = null;

            // restore matcher from the stack if we have an /matcher-list
        } else if (localName.equals("match-list")) {
            if (stack.size() > 0) {
                matcher = (MagicMatcher) stack.get(stack.size() - 1);
                // pop from the stack
                stack.remove(matcher);
            }
        } else if (localName.equals("mimetype")) {
            isMimeType = false;
        } else if (localName.equals("extension")) {
            isExtension = false;
        } else if (localName.equals("description")) {
            isDescription = false;
        } else if (localName.equals("test")) {
            isTest = false;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param ex DOCUMENT ME!
     * @throws SAXException DOCUMENT ME!
     */
    public void warning(SAXParseException ex)
            throws SAXException {
        // FIXME
    }

    /**
     * DOCUMENT ME!
     *
     * @param ex DOCUMENT ME!
     * @throws SAXException DOCUMENT ME!
     */
    public void error(SAXParseException ex)
            throws SAXException {
        // FIXME
        throw ex;
    }

    /**
     * DOCUMENT ME!
     *
     * @param ex DOCUMENT ME!
     * @throws SAXException DOCUMENT ME!
     */
    public void fatalError(SAXParseException ex)
            throws SAXException {
        // FIXME
        throw ex;
    }

    /**
     * replaces octal representations of bytes, written as \ddd to actual byte values.
     *
     * @param s a string with encoded octals
     * @return string with all octals decoded
     */
    private ByteBuffer convertOctals(String s) {
        int beg = 0;
        int end = 0;
        int chr;
        ByteArrayOutputStream buf = new ByteArrayOutputStream();

        while ((end = s.indexOf('\\', beg)) != -1) {
            if (s.charAt(end + 1) != '\\') {
                //log.debug("appending chunk '"+s.substring(beg, end)+"'");
                for (int z = beg; z < end; z++) {
                    buf.write((int) s.charAt(z));
                }

                //log.debug("found \\ at position "+end);
                //log.debug("converting octal '"+s.substring(end, end+4)+"'");
                if ((end + 4) <= s.length()) {
                    try {
                        chr = Integer.parseInt(s.substring(end + 1, end + 4), 8);

                        //log.debug("converted octal '"+s.substring(end+1,end+4)+"' to '"+chr);
                        //log.debug("converted octal back to '"+Integer.toOctalString(chr));

                        //log.debug("converted '"+s.substring(end+1,end+4)+"' to "+chr+"/"+((char)chr));
                        buf.write(chr);
                        beg = end + 4;
                        end = beg;
                    } catch (NumberFormatException nfe) {
                        //log.debug("not an octal");
                        buf.write((int) '\\');
                        beg = end + 1;
                        end = beg;
                    }
                } else {
                    //log.debug("not an octal, not enough chars left in string");
                    buf.write((int) '\\');
                    beg = end + 1;
                    end = beg;
                }
            } else {
                //log.debug("appending \\");
                buf.write((int) '\\');
                beg = end + 1;
                end = beg;
            }
        }

        if (end < s.length()) {
            for (int z = beg; z < s.length(); z++) {
                buf.write((int) s.charAt(z));
            }
        }

        try {
            ByteBuffer b = ByteBuffer.allocate(buf.size());
            return b.put(buf.toByteArray());
        } catch (Exception e) {
            return ByteBuffer.allocate(0);
        }
    }
}
