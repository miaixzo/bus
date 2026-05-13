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

import java.io.IOException;

import org.miaixz.bus.image.Builder;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.data.*;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Represents the SAXWriter type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class SAXWriter implements ImageInputHandler {

    /**
     * The namespace value.
     */
    private static final String NAMESPACE = "http://dicom.nema.org/PS3.19/models/NativeDICOM";

    /**
     * The base64 chunk length value.
     */
    private static final int BASE64_CHUNK_LENGTH = 256 * 3;

    /**
     * The buffer length value.
     */
    private static final int BUFFER_LENGTH = 256 * 4;

    /**
     * The ch value.
     */
    private final ContentHandler ch;

    /**
     * The atts value.
     */
    private final AttributesImpl atts = new AttributesImpl();

    /**
     * The buffer value.
     */
    private final char[] buffer = new char[BUFFER_LENGTH];

    /**
     * The include keyword value.
     */
    private boolean includeKeyword = true;

    /**
     * The namespace value.
     */
    private String namespace = "";

    /**
     * Creates a new instance.
     *
     * @param ch the ch.
     */
    public SAXWriter(ContentHandler ch) {
        this.ch = ch;
    }

    /**
     * Determines whether include keyword.
     *
     * @return true if the condition is met; otherwise false.
     */
    public final boolean isIncludeKeyword() {
        return includeKeyword;
    }

    /**
     * Sets the include keyword.
     *
     * @param includeKeyword the include keyword.
     */
    public final void setIncludeKeyword(boolean includeKeyword) {
        this.includeKeyword = includeKeyword;
    }

    /**
     * Determines whether include namespace declaration.
     *
     * @return true if the condition is met; otherwise false.
     */
    public final boolean isIncludeNamespaceDeclaration() {
        return namespace == NAMESPACE;
    }

    /**
     * Sets the include namespace declaration.
     *
     * @param includeNameSpaceDeclaration the include name space declaration.
     */
    public final void setIncludeNamespaceDeclaration(boolean includeNameSpaceDeclaration) {
        this.namespace = includeNameSpaceDeclaration ? NAMESPACE : "";
    }

    /**
     * Executes the write operation.
     *
     * @param attrs the attrs.
     * @throws SAXException if the operation cannot be completed.
     */
    public void write(Attributes attrs) throws SAXException {
        startDocument();
        writeItem(attrs);
        endDocument();
    }

    /**
     * Writes the item.
     *
     * @param item the item.
     * @throws SAXException if the operation cannot be completed.
     */
    private void writeItem(final Attributes item) throws SAXException {
        final SpecificCharacterSet cs = item.getSpecificCharacterSet();
        try {
            item.accept(new Visitor() {

                @Override
                public boolean visit(Attributes attrs, int tag, VR vr, Object value) throws Exception {
                    writeAttribute(tag, vr, value, cs, item);
                    return true;
                }
            }, false);
        } catch (SAXException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Executes the start dataset operation.
     *
     * @param dis the dis.
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public void startDataset(ImageInputStream dis) throws IOException {
        try {
            startDocument();
        } catch (SAXException e) {
            throw new IOException(e);
        }
    }

    /**
     * Executes the end dataset operation.
     *
     * @param dis the dis.
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public void endDataset(ImageInputStream dis) throws IOException {
        try {
            endDocument();
        } catch (SAXException e) {
            throw new IOException(e);
        }
    }

    /**
     * Executes the start document operation.
     *
     * @throws SAXException if the operation cannot be completed.
     */
    private void startDocument() throws SAXException {
        ch.startDocument();
        atts.addAttribute("", "space", "xml:space", "NMTOKEN", "preserve");
        startElement("NativeDicomModel");
    }

    /**
     * Executes the end document operation.
     *
     * @throws SAXException if the operation cannot be completed.
     */
    private void endDocument() throws SAXException {
        endElement("NativeDicomModel");
        ch.endDocument();
    }

    /**
     * Executes the start element operation.
     *
     * @param name      the name.
     * @param attrName  the attr name.
     * @param attrValue the attr value.
     * @throws SAXException if the operation cannot be completed.
     */
    private void startElement(String name, String attrName, int attrValue) throws SAXException {
        startElement(name, attrName, Integer.toString(attrValue));
    }

    /**
     * Executes the start element operation.
     *
     * @param name      the name.
     * @param attrName  the attr name.
     * @param attrValue the attr value.
     * @throws SAXException if the operation cannot be completed.
     */
    private void startElement(String name, String attrName, String attrValue) throws SAXException {
        addAttribute(attrName, attrValue);
        startElement(name);
    }

    /**
     * Executes the start element operation.
     *
     * @param name the name.
     * @throws SAXException if the operation cannot be completed.
     */
    private void startElement(String name) throws SAXException {
        ch.startElement(namespace, name, name, atts);
        atts.clear();
    }

    /**
     * Executes the end element operation.
     *
     * @param name the name.
     * @throws SAXException if the operation cannot be completed.
     */
    private void endElement(String name) throws SAXException {
        ch.endElement(namespace, name, name);
    }

    /**
     * Adds the attribute.
     *
     * @param name  the name.
     * @param value the value.
     */
    private void addAttribute(String name, String value) {
        atts.addAttribute(namespace, name, name, "NMTOKEN", value);
    }

    /**
     * Writes the attribute.
     *
     * @param tag   the tag.
     * @param vr    the vr.
     * @param value the value.
     * @param cs    the cs.
     * @param attrs the attrs.
     * @throws SAXException if the operation cannot be completed.
     */
    private void writeAttribute(int tag, VR vr, Object value, SpecificCharacterSet cs, Attributes attrs)
            throws SAXException {
        if (Tag.isGroupLength(tag) || Tag.isPrivateCreator(tag))
            return;

        String privateCreator = attrs.getPrivateCreator(tag);
        addAttributes(tag, vr, privateCreator);
        startElement("DicomAttribute");
        if (value instanceof Value)
            writeAttribute((Value) value, attrs.bigEndian());
        else if (!vr.isInlineBinary()) {
            writeValues(vr, value, attrs.bigEndian(), attrs.getSpecificCharacterSet(vr));
        } else if (value instanceof byte[]) {
            writeInlineBinary(attrs.bigEndian() ? vr.toggleEndian((byte[]) value, true) : (byte[]) value);
        } else
            throw new IllegalArgumentException("vr: " + vr + ", value class: " + value.getClass());
        endElement("DicomAttribute");
    }

    /**
     * Writes the attribute.
     *
     * @param value     the value.
     * @param bigEndian the big endian.
     * @throws SAXException if the operation cannot be completed.
     */
    private void writeAttribute(Value value, boolean bigEndian) throws SAXException {
        if (value.isEmpty())
            return;

        if (value instanceof Sequence seq) {
            int number = 0;
            for (Attributes item : seq) {
                startElement("Item", "number", ++number);
                writeItem(item);
                endElement("Item");
            }
        } else if (value instanceof Fragments frags) {
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

    /**
     * Reads the value.
     *
     * @param dis   the dis.
     * @param attrs the attrs.
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public void readValue(ImageInputStream dis, Attributes attrs) throws IOException {
        int tag = dis.tag();
        VR vr = dis.vr();
        long len = dis.unsignedLength();
        if (Tag.isGroupLength(tag) || Tag.isPrivateCreator(tag)) {
            dis.readValue(dis, attrs);
        } else if (dis.isExcludeBulkData()) {
            if (len == -1)
                dis.readValue(dis, attrs);
            else
                dis.skipFully(len);
        } else
            try {
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
                        if (tag == Tag.TransferSyntaxUID || tag == Tag.SpecificCharacterSet
                                || tag == Tag.PixelRepresentation)
                            attrs.setBytes(tag, vr, b);
                        if (vr.isInlineBinary())
                            writeInlineBinary(dis.bigEndian() ? vr.toggleEndian(b, false) : b);
                        else
                            writeValues(vr, b, dis.bigEndian(), attrs.getSpecificCharacterSet(vr));
                    }
                }
                endElement("DicomAttribute");
            } catch (SAXException e) {
                throw new IOException(e);
            }
    }

    /**
     * Adds the attributes.
     *
     * @param tag            the tag.
     * @param vr             the vr.
     * @param privateCreator the private creator.
     */
    private void addAttributes(int tag, VR vr, String privateCreator) {
        if (includeKeyword) {
            String keyword = ElementDictionary.keywordOf(tag, privateCreator);
            if (keyword != null && !keyword.isEmpty())
                addAttribute("keyword", keyword);
        }
        if (privateCreator != null)
            tag &= 0xffff00ff;
        addAttribute("tag", Tag.toHexString(tag));
        if (privateCreator != null)
            addAttribute("privateCreator", privateCreator);
        addAttribute("vr", vr.name());
    }

    /**
     * Reads the value.
     *
     * @param dis the dis.
     * @param seq the seq.
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public void readValue(ImageInputStream dis, Sequence seq) throws IOException {
        try {
            startElement("Item", "number", seq.size() + 1);
            dis.readValue(dis, seq);
            endElement("Item");
        } catch (SAXException e) {
            throw new IOException(e);
        }
    }

    /**
     * Reads the value.
     *
     * @param dis   the dis.
     * @param frags the frags.
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public void readValue(ImageInputStream dis, Fragments frags) throws IOException {
        long len = dis.unsignedLength();
        if (dis.isExcludeBulkData()) {
            dis.skipFully(len);
        } else
            try {
                frags.add(new byte[] {}); // increment size
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

    /**
     * Writes the values.
     *
     * @param vr        the vr.
     * @param val       the val.
     * @param bigEndian the big endian.
     * @param cs        the cs.
     * @throws SAXException if the operation cannot be completed.
     */
    private void writeValues(VR vr, Object val, boolean bigEndian, SpecificCharacterSet cs) throws SAXException {
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
                startElement("Value");
                if (s != null)
                    writeText(s);
                endElement("Value");
            }
        }
    }

    /**
     * Writes the inline binary.
     *
     * @param b the b.
     * @throws SAXException if the operation cannot be completed.
     */
    private void writeInlineBinary(byte[] b) throws SAXException {
        startElement("InlineBinary");
        char[] buf = buffer;
        for (int off = 0; off < b.length;) {
            int len = Math.min(b.length - off, BASE64_CHUNK_LENGTH);
            Builder.encode(b, off, len, buf, 0);
            ch.characters(buf, 0, (len * 4 / 3 + 3) & ~3);
            off += len;
        }
        endElement("InlineBinary");
    }

    /**
     * Writes the bulk data.
     *
     * @param bulkData the bulk data.
     * @throws SAXException if the operation cannot be completed.
     */
    private void writeBulkData(BulkData bulkData) throws SAXException {
        if (bulkData.getUUID() != null)
            addAttribute("uuid", bulkData.getUUID());
        if (bulkData.getURI() != null)
            addAttribute("uri", bulkData.getURI());
        startElement("BulkData");
        endElement("BulkData");
    }

    /**
     * Writes the element.
     *
     * @param qname the qname.
     * @param s     the s.
     * @throws SAXException if the operation cannot be completed.
     */
    private void writeElement(String qname, String s) throws SAXException {
        if (s != null) {
            startElement(qname);
            writeText(s);
            endElement(qname);
        }
    }

    /**
     * Writes the text.
     *
     * @param s the s.
     * @throws SAXException if the operation cannot be completed.
     */
    private void writeText(String s) throws SAXException {
        char[] buf = buffer;
        for (int off = 0, totlen = s.length(); off < totlen;) {
            int len = Math.min(totlen - off, buf.length);
            s.getChars(off, off += len, buf, 0);
            ch.characters(buf, 0, len);
        }
    }

    /**
     * Writes the pn group.
     *
     * @param qname the qname.
     * @param pn    the pn.
     * @param group the group.
     * @throws SAXException if the operation cannot be completed.
     */
    private void writePNGroup(String qname, PersonName pn, PersonName.Group group) throws SAXException {
        if (pn.contains(group)) {
            startElement(qname);
            writeElement("FamilyName", pn.get(group, PersonName.Component.FamilyName));
            writeElement("GivenName", pn.get(group, PersonName.Component.GivenName));
            writeElement("MiddleName", pn.get(group, PersonName.Component.MiddleName));
            writeElement("NamePrefix", pn.get(group, PersonName.Component.NamePrefix));
            writeElement("NameSuffix", pn.get(group, PersonName.Component.NameSuffix));
            endElement(qname);
        }
    }

}
