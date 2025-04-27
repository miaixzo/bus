/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.image.galaxy.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Objects;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.image.Builder;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.galaxy.data.*;
import org.miaixz.bus.logger.Logger;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class ContentHandlerAdapter extends DefaultHandler {

    private final boolean lenient;
    private final LinkedList<Attributes> items = new LinkedList<>();
    private final LinkedList<Sequence> seqs = new LinkedList<>();
    private final ByteArrayOutputStream bout = new ByteArrayOutputStream(64);
    private final char[] carry = new char[4];
    private final StringBuilder sb = new StringBuilder(64);
    private final ArrayList<String> values = new ArrayList<>();
    private BulkData.Creator bulkDataCreator = BulkData::new;
    private Attributes fmi;
    private boolean bigEndian;
    private int carryLen;
    private PersonName pn;
    private PersonName.Group pnGroup;
    private int tag;
    private String privateCreator;
    private VR vr;
    private BulkData bulkData;
    private Fragments dataFragments;
    private boolean processCharacters;
    private boolean inlineBinary;

    public ContentHandlerAdapter(Attributes attrs) {
        this(attrs, false);
    }

    public ContentHandlerAdapter(Attributes attrs, boolean lenient) {
        if (attrs != null) {
            items.add(attrs);
            bigEndian = attrs.bigEndian();
        }
        this.lenient = lenient;
    }

    private static boolean bigEndian(Attributes fmi) {
        return fmi != null && UID.ExplicitVRBigEndian.equals(fmi.getString(Tag.TransferSyntaxUID));
    }

    private static String prefix(String privateCreator, int level) {
        if (privateCreator == null && level == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        if (privateCreator != null) {
            sb.append(privateCreator).append(':');
        }
        for (int i = 0; i < level; i++) {
            sb.append('>');
        }
        return sb.toString();
    }

    public void setBulkDataCreator(BulkData.Creator bulkDataCreator) {
        this.bulkDataCreator = Objects.requireNonNull(bulkDataCreator);
    }

    public Attributes getFileMetaInformation() {
        return fmi;
    }

    public Attributes getDataset() {
        return items.getFirst();
    }

    @Override
    public void startElement(String uri, String localName, String qName, org.xml.sax.Attributes atts) {
        switch (qName) {
        case "DicomAttribute":
            startDicomAttribute((int) Long.parseLong(atts.getValue("tag"), 16), atts.getValue("privateCreator"),
                    atts.getValue("vr"));
            break;
        case "Item":
            startItem(Integer.parseInt(atts.getValue("number")));
            break;
        case "DataFragment":
            startDataFragment(Integer.parseInt(atts.getValue("number")));
            break;
        case "InlineBinary":
            startInlineBinary();
            break;
        case "PersonName":
            startPersonName(Integer.parseInt(atts.getValue("number")));
            break;
        case "Alphabetic":
            startPNGroup(PersonName.Group.Alphabetic);
            break;
        case "Ideographic":
            startPNGroup(PersonName.Group.Ideographic);
            break;
        case "Phonetic":
            startPNGroup(PersonName.Group.Phonetic);
            break;
        case "Value":
            startValue(Integer.parseInt(atts.getValue("number")));
            startText();
            break;
        case "FamilyName":
        case "GivenName":
        case "Length":
        case "MiddleName":
        case "NamePrefix":
        case "NameSuffix":
        case "Offset":
        case "TransferSyntax":
        case "URI":
            startText();
            break;
        case "BulkData":
            bulkData(atts.getValue("uuid"), atts.getValue("uri"));
            break;
        }
    }

    private void bulkData(String uuid, String uri) {
        bulkData = bulkDataCreator.create(uuid, uri, items.getLast().bigEndian());
    }

    private void startInlineBinary() {
        processCharacters = true;
        inlineBinary = true;
        carryLen = 0;
        bout.reset();
    }

    private void startText() {
        processCharacters = true;
        inlineBinary = false;
        sb.setLength(0);
    }

    private void startDicomAttribute(int tag, String privateCreator, String vr) {
        this.tag = tag;
        this.privateCreator = privateCreator;
        this.vr = vr != null ? VR.valueOf(vr) : ElementDictionary.vrOf(tag, privateCreator);
        if (this.vr == VR.SQ)
            seqs.add(items.getLast().newSequence(privateCreator, tag, 10));
    }

    private void startDataFragment(int number) {
        if (dataFragments == null)
            dataFragments = items.getLast().newFragments(privateCreator, tag, vr, 10);
        while (dataFragments.size() < number - 1)
            dataFragments.add(new byte[] {});
    }

    private void startItem(int number) {
        Sequence seq = seqs.getLast();
        while (seq.size() < number - 1)
            seq.add(new Attributes(bigEndian, 0));
        Attributes item = new Attributes(bigEndian);
        seq.add(item);
        items.add(item);
    }

    private void startValue(int number) {
        while (values.size() < number - 1)
            values.add(null);
    }

    private void startPersonName(int number) {
        startValue(number);
        pn = new PersonName();
    }

    private void startPNGroup(PersonName.Group pnGroup) {
        this.pnGroup = pnGroup;
    }

    @Override
    public void characters(char[] ch, int offset, int len) throws SAXException {
        if (processCharacters)
            if (inlineBinary)
                try {
                    if (carryLen != 0) {
                        int copy = Math.min(4 - carryLen, len);
                        System.arraycopy(ch, offset, carry, carryLen, copy);
                        carryLen += copy;
                        offset += copy;
                        len -= copy;
                        if (carryLen == 4)
                            Builder.decode(carry, 0, 4, bout);
                        else
                            return;
                    }
                    if ((carryLen = len & 3) != 0) {
                        len -= carryLen;
                        System.arraycopy(ch, offset + len, carry, 0, carryLen);
                    }
                    Builder.decode(ch, offset, len, bout);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            else
                sb.append(ch, offset, len);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        switch (qName) {
        case "DicomAttribute":
            endDicomAttribute();
            break;
        case "Item":
            endItem();
            break;
        case "DataFragment":
            endDataFragment();
            break;
        case "PersonName":
            endPersonName();
            break;
        case "Value":
            endValue();
            break;
        case "FamilyName":
            endPNComponent(PersonName.Component.FamilyName);
            break;
        case "GivenName":
            endPNComponent(PersonName.Component.GivenName);
            break;
        case "MiddleName":
            endPNComponent(PersonName.Component.MiddleName);
            break;
        case "NamePrefix":
            endPNComponent(PersonName.Component.NamePrefix);
            break;
        case "NameSuffix":
            endPNComponent(PersonName.Component.NameSuffix);
            break;
        }
        processCharacters = false;
    }

    @Override
    public void endDocument() {
        if (fmi != null)
            fmi.trimToSize();
        items.getFirst().trimToSize();
    }

    private void endDataFragment() {
        if (bulkData != null) {
            dataFragments.add(bulkData);
            bulkData = null;
        } else {
            dataFragments.add(getBytes());
        }
    }

    private void endDicomAttribute() throws SAXException {
        if (vr == VR.SQ) {
            seqs.removeLast().trimToSize();
            return;
        }
        if (dataFragments != null) {
            dataFragments.trimToSize();
            dataFragments = null;
            return;
        }
        Attributes attrs = attrs();
        if (bulkData != null) {
            attrs.setValue(privateCreator, tag, vr, bulkData);
            bulkData = null;
        } else if (inlineBinary) {
            attrs.setBytes(privateCreator, tag, vr, getBytes());
            inlineBinary = false;
        } else {
            String[] value = getStrings();
            try {
                attrs.setString(privateCreator, tag, vr, value);
            } catch (RuntimeException e) {
                String message = String.format("Invalid %s(%04X,%04X) %s %s", prefix(privateCreator, items.size() - 1),
                        Tag.groupNumber(tag), Tag.elementNumber(tag), vr, Arrays.toString(value));
                if (lenient) {
                    Logger.info("{} - ignored", message);
                } else {
                    throw new SAXException(message, e);
                }
            }
        }
    }

    private Attributes attrs() {
        if (Tag.isFileMetaInformation(tag)) {
            if (fmi == null) {
                fmi = new Attributes();
            }
            return fmi;
        }
        if (items.isEmpty()) {
            items.add(new Attributes(bigEndian = bigEndian(fmi)));
        }
        return items.getLast();
    }

    private void endItem() {
        items.removeLast().trimToSize();
        vr = VR.SQ;
    }

    private void endPersonName() {
        values.add(pn.toString());
        pn = null;
    }

    private void endValue() {
        values.add(getString());
    }

    private void endPNComponent(PersonName.Component pnComp) {
        pn.set(pnGroup, pnComp, getString());
    }

    private String getString() {
        return sb.toString();
    }

    private byte[] getBytes() {
        byte[] b = bout.toByteArray();
        return bigEndian ? vr.toggleEndian(b, false) : b;
    }

    private String[] getStrings() {
        try {
            return values.toArray(Normal.EMPTY_STRING_ARRAY);
        } finally {
            values.clear();
        }
    }

}
