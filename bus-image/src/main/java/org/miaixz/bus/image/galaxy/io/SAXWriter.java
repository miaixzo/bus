/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.image.galaxy.io;

import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.data.*;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class SAXWriter implements ImageInputHandler {

    private static final String NAMESPACE = "http://dicom.nema.org/PS3.19/models/NativeDICOM";
    private static final int BASE64_CHUNK_LENGTH = Normal._256 * 3;
    private static final int BUFFER_LENGTH = Normal._256 * 4;
    private final ContentHandler ch;
    private final AttributesImpl atts = new AttributesImpl();
    private final char[] buffer = new char[BUFFER_LENGTH];
    private boolean includeKeyword = true;
    private String namespace = Normal.EMPTY;

    public SAXWriter(ContentHandler ch) {
        this.ch = ch;
    }

    public static byte[] getBytes(char[] chars) {
        Charset cs = org.miaixz.bus.core.lang.Charset.UTF_8;
        CharBuffer cb = CharBuffer.allocate(chars.length);
        cb.put(chars);
        cb.flip();
        ByteBuffer bb = cs.encode(cb);
        return bb.array();
    }

    public final boolean isIncludeKeyword() {
        return includeKeyword;
    }

    public final void setIncludeKeyword(boolean includeKeyword) {
        this.includeKeyword = includeKeyword;
    }

    public final boolean isIncludeNamespaceDeclaration() {
        return namespace == NAMESPACE;
    }

    public final void setIncludeNamespaceDeclaration(boolean includeNameSpaceDeclaration) {
        this.namespace = includeNameSpaceDeclaration ? NAMESPACE : Normal.EMPTY;
    }

    public void write(Attributes attrs) throws SAXException {
        startDocument();
        writeItem(attrs);
        endDocument();
    }

    private void writeItem(final Attributes item) throws SAXException {
        final SpecificCharacterSet cs = item.getSpecificCharacterSet();
        try {
            item.accept(new Attributes.Visitor() {

                            @Override
                            public boolean visit(Attributes attrs, int tag, VR vr, Object value)
                                    throws Exception {
                                writeAttribute(tag, vr, value, cs, item);
                                return true;
                            }
                        },
                    false);
        } catch (SAXException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void startDataset(ImageInputStream dis) throws IOException {
        try {
            startDocument();
        } catch (SAXException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void endDataset(ImageInputStream dis) throws IOException {
        try {
            endDocument();
        } catch (SAXException e) {
            throw new IOException(e);
        }
    }

    private void startDocument() throws SAXException {
        ch.startDocument();
        startElement("NativeDicomModel", "xml:space", "preserve");
    }

    private void endDocument() throws SAXException {
        endElement("NativeDicomModel");
        ch.endDocument();
    }

    private void startElement(String name, String attrName, int attrValue)
            throws SAXException {
        startElement(name, attrName, Integer.toString(attrValue));
    }

    private void startElement(String name, String attrName, String attrValue)
            throws SAXException {
        addAttribute(attrName, attrValue);
        startElement(name);
    }

    private void startElement(String name) throws SAXException {
        ch.startElement(namespace, name, name, atts);
        atts.clear();
    }

    private void endElement(String name) throws SAXException {
        ch.endElement(namespace, name, name);
    }

    private void addAttribute(String name, String value) {
        atts.addAttribute(namespace, name, name, "NMTOKEN", value);
    }

    private void writeAttribute(int tag, VR vr, Object value,
                                SpecificCharacterSet cs, Attributes attrs) throws SAXException {
        if (Tag.isGroupLength(tag) || Tag.isPrivateCreator(tag))
            return;

        String privateCreator = attrs.getPrivateCreator(tag);
        addAttributes(tag, vr, privateCreator);
        startElement("DicomAttribute");
        if (value instanceof Value)
            writeAttribute((Value) value, attrs.bigEndian());
        else if (!vr.isInlineBinary()) {
            writeValues(vr, value, attrs.bigEndian(),
                    attrs.getSpecificCharacterSet(vr));
        } else if (value instanceof byte[]) {
            writeInlineBinary(attrs.bigEndian()
                    ? vr.toggleEndian((byte[]) value, true)
                    : (byte[]) value);
        } else
            throw new IllegalArgumentException("vr: " + vr + ", value class: "
                    + value.getClass());
        endElement("DicomAttribute");
    }

    private void writeAttribute(Value value, boolean bigEndian)
            throws SAXException {
        if (value.isEmpty())
            return;

        if (value instanceof Sequence) {
            Sequence seq = (Sequence) value;
            int number = 0;
            for (Attributes item : seq) {
                startElement("Item", "number", ++number);
                writeItem(item);
                endElement("Item");
            }
        } else if (value instanceof Fragments) {
            Fragments frags = (Fragments) value;
            int number = 0;
            for (Object frag : frags) {
                ++number;
                if (frag instanceof Value && ((Value) frag).isEmpty())
                    continue;
                startElement("DataFragment", "number", number);
                if (frag instanceof BulkData)
                    writeBulkData((BulkData) frag);
                else {
                    byte[] b = (byte[]) frag;
                    if (bigEndian)
                        frags.vr().toggleEndian(b, true);
                    writeInlineBinary(b);
                }
                endElement("DataFragment");
            }
        } else if (value instanceof BulkData) {
            writeBulkData((BulkData) value);
        }
    }

    @Override
    public void readValue(ImageInputStream dis, Attributes attrs)
            throws IOException {
        int tag = dis.tag();
        VR vr = dis.vr();
        int len = dis.length();
        if (Tag.isGroupLength(tag) || Tag.isPrivateCreator(tag)) {
            dis.readValue(dis, attrs);
        } else if (dis.isExcludeBulkData()) {
            if (len == -1)
                dis.readValue(dis, attrs);
            else
                dis.skipFully(len);
        } else try {
            String privateCreator = attrs.getPrivateCreator(tag);
            addAttributes(tag, vr, privateCreator);
            startElement("DicomAttribute");
            if (vr == VR.SQ || len == -1) {
                dis.readValue(dis, attrs);
            } else if (len > 0) {
                if (dis.isIncludeBulkDataURI()) {
                    writeBulkData(dis.createBulkData(dis));
                } else {
                    byte[] b = dis.readValue();
                    if (tag == Tag.TransferSyntaxUID
                            || tag == Tag.SpecificCharacterSet)
                        attrs.setBytes(tag, vr, b);
                    if (vr.isInlineBinary())
                        writeInlineBinary(dis.bigEndian()
                                ? vr.toggleEndian(b, false)
                                : b);
                    else
                        writeValues(vr, b, dis.bigEndian(),
                                attrs.getSpecificCharacterSet(vr));
                }
            }
            endElement("DicomAttribute");
        } catch (SAXException e) {
            throw new IOException(e);
        }
    }

    private void addAttributes(int tag, VR vr, String privateCreator) {
        if (null != privateCreator)
            tag &= 0xffff00ff;
        if (includeKeyword) {
            String keyword = ElementDictionary.keywordOf(tag, privateCreator);
            if (null != keyword && !keyword.isEmpty())
                addAttribute("keyword", keyword);
        }
        addAttribute("tag", Tag.toHexString(tag));
        if (null != privateCreator)
            addAttribute("privateCreator", privateCreator);
        addAttribute("vr", vr.name());
    }

    @Override
    public void readValue(ImageInputStream dis, Sequence seq)
            throws IOException {
        try {
            startElement("Item", "number", seq.size() + 1);
            dis.readValue(dis, seq);
            endElement("Item");
        } catch (SAXException e) {
            throw new IOException(e);
        }
    }

    private void writeValues(VR vr, Object val, boolean bigEndian,
                             SpecificCharacterSet cs) throws SAXException {
        if (vr.isStringType())
            val = vr.toStrings(val, bigEndian, cs);
        int vm = vr.vmOf(val);
        for (int i = 0; i < vm; i++) {
            String s = vr.toString(val, bigEndian, i, null);
            addAttribute("number", Integer.toString(i + 1));
            if (vr == VR.PN) {
                PersonName pn = new PersonName(s, true);
                startElement("PersonName");
                writePNGroup("Alphabetic", pn, PersonName.Group.Alphabetic);
                writePNGroup("Ideographic", pn, PersonName.Group.Ideographic);
                writePNGroup("Phonetic", pn, PersonName.Group.Phonetic);
                endElement("PersonName");
            } else {
                writeElement("Value", s);
            }
        }
    }

    @Override
    public void readValue(ImageInputStream dis, Fragments frags)
            throws IOException {
        int len = dis.length();
        if (dis.isExcludeBulkData()) {
            dis.skipFully(len);
        } else try {
            frags.add(new byte[]{}); // increment size
            if (len > 0) {
                startElement("DataFragment", "number", frags.size());
                if (dis.isIncludeBulkDataURI()) {
                    writeBulkData(dis.createBulkData(dis));
                } else {
                    byte[] b = dis.readValue();
                    if (dis.bigEndian())
                        frags.vr().toggleEndian(b, false);
                    writeInlineBinary(b);
                }
                endElement("DataFragment");
            }
        } catch (SAXException e) {
            throw new IOException(e);
        }
    }

    private void writeInlineBinary(byte[] b) throws SAXException {
        startElement("InlineBinary");
        char[] buf = buffer;
        for (int off = 0; off < b.length; ) {
            int len = Math.min(b.length - off, BASE64_CHUNK_LENGTH);
            Base64.encode(b, off, len, buf, 0);
            ch.characters(buf, 0, (len * 4 / 3 + 3) & ~3);
            off += len;
        }
        endElement("InlineBinary");
    }

    private void writeBulkData(BulkData bulkData)
            throws SAXException {
        if (null != bulkData.getUUID())
            addAttribute("uuid", bulkData.getUUID());
        if (null != bulkData.getURI())
            addAttribute("uri", bulkData.getURI());
        startElement("BulkData");
        endElement("BulkData");
    }

    private void writeElement(String qname, String s) throws SAXException {
        if (null != s) {
            startElement(qname);
            char[] buf = buffer;
            for (int off = 0, totlen = s.length(); off < totlen; ) {
                int len = Math.min(totlen - off, buf.length);
                s.getChars(off, off += len, buf, 0);
                ch.characters(buf, 0, len);
            }
            endElement(qname);
        }
    }

    private void writePNGroup(String qname, PersonName pn,
                              PersonName.Group group) throws SAXException {
        if (pn.contains(group)) {
            startElement(qname);
            writeElement("FamilyName",
                    pn.get(group, PersonName.Component.FamilyName));
            writeElement("GivenName",
                    pn.get(group, PersonName.Component.GivenName));
            writeElement("MiddleName",
                    pn.get(group, PersonName.Component.MiddleName));
            writeElement("NamePrefix",
                    pn.get(group, PersonName.Component.NamePrefix));
            writeElement("NameSuffix",
                    pn.get(group, PersonName.Component.NameSuffix));
            endElement(qname);
        }
    }

}
