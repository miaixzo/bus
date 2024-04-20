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
import org.xml.sax.helpers.DefaultHandler;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class ContentHandlerAdapter extends DefaultHandler {

    private final boolean bigEndian;
    private final LinkedList<Attributes> items = new LinkedList<>();
    private final LinkedList<Sequence> seqs = new LinkedList<>();
    private final ByteArrayOutputStream bout = new ByteArrayOutputStream(Normal._64);
    private final char[] carry = new char[4];
    private final StringBuilder sb = new StringBuilder(Normal._64);
    private final List<String> values = new ArrayList<>();
    private Attributes fmi;
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
        if (null == attrs)
            throw new NullPointerException();
        items.add(attrs);
        bigEndian = attrs.bigEndian();
    }

    public Attributes getFileMetaInformation() {
        return fmi;
    }

    @Override
    public void startElement(String uri, String localName, String qName,
                             org.xml.sax.Attributes atts) {
        switch (qName.charAt(0)) {
            case 'A':
                if (qName.equals("Alphabetic"))
                    startPNGroup(PersonName.Group.Alphabetic);
                break;
            case 'B':
                if (qName.equals("BulkData"))
                    bulkData(atts.getValue("uuid"), atts.getValue("uri"));
                break;
            case 'D':
                if (qName.equals("DicomAttribute"))
                    startDicomAttribute(
                            (int) Long.parseLong(atts.getValue("tag"), Normal._16),
                            atts.getValue("privateCreator"),
                            atts.getValue("vr"));
                else if (qName.equals("DataFragment"))
                    startDataFragment(Integer.parseInt(atts.getValue("number")));
                break;
            case 'F':
                if (qName.equals("FamilyName"))
                    startText();
                break;
            case 'G':
                if (qName.equals("GivenName"))
                    startText();
                break;
            case 'I':
                if (qName.equals("Item"))
                    startItem(Integer.parseInt(atts.getValue("number")));
                else if (qName.equals("InlineBinary"))
                    startInlineBinary();
                else if (qName.equals("Ideographic"))
                    startPNGroup(PersonName.Group.Ideographic);
                break;
            case 'L':
                if (qName.equals("Length"))
                    startText();
                break;
            case 'M':
                if (qName.equals("MiddleName"))
                    startText();
                break;
            case 'N':
                if (qName.equals("NamePrefix") || qName.equals("NameSuffix"))
                    startText();
                break;
            case 'O':
                if (qName.equals("Offset"))
                    startText();
                break;
            case 'P':
                if (qName.equals("PersonName")) {
                    startPersonName(Integer.parseInt(atts.getValue("number")));
                } else if (qName.equals("Phonetic"))
                    startPNGroup(PersonName.Group.Phonetic);
                break;
            case 'T':
                if (qName.equals("TransferSyntax"))
                    startText();
                break;
            case 'U':
                if (qName.equals("URI"))
                    startText();
                break;
            case 'V':
                if (qName.equals("Value")) {
                    startValue(Integer.parseInt(atts.getValue("number")));
                    startText();
                }
                break;
        }
    }

    private void bulkData(String uuid, String uri) {
        bulkData = new BulkData(uuid, uri, items.getLast().bigEndian());
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

    private void startDicomAttribute(int tag, String privateCreator,
                                     String vr) {
        this.tag = tag;
        this.privateCreator = privateCreator;
        this.vr = null != vr ? VR.valueOf(vr)
                : ElementDictionary.vrOf(tag, privateCreator);
        if (this.vr == VR.SQ)
            seqs.add(items.getLast().newSequence(privateCreator, tag, 10));
    }

    private void startDataFragment(int number) {
        if (null == dataFragments)
            dataFragments = items.getLast()
                    .newFragments(privateCreator, tag, vr, 10);
        while (dataFragments.size() < number - 1)
            dataFragments.add(new byte[]{});
    }

    private void startItem(int number) {
        Sequence seq = seqs.getLast();
        while (seq.size() < number - 1)
            seq.add(new Attributes(0));
        Attributes item = new Attributes();
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
    public void characters(char[] ch, int offset, int len) {
        if (processCharacters)
            if (inlineBinary) {
                if (carryLen != 0) {
                    int copy = Math.min(4 - carryLen, len);
                    System.arraycopy(ch, offset, carry, carryLen, copy);
                    carryLen += copy;
                    offset += copy;
                    len -= copy;
                    if (carryLen == 4)
                        Base64.decode(carry, 0, 4, bout);
                    else return;
                }
                if ((carryLen = len & 3) != 0) {
                    len -= carryLen;
                    System.arraycopy(ch, offset + len, carry, 0, carryLen);
                }
                Base64.decode(ch, offset, len, bout);
            } else
                sb.append(ch, offset, len);
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        switch (qName.charAt(0)) {
            case 'D':
                if (qName.equals("DicomAttribute"))
                    endDicomAttribute();
                else if (qName.equals("DataFragment"))
                    endDataFragment();
                break;
            case 'F':
                if (qName.equals("FamilyName"))
                    endPNComponent(PersonName.Component.FamilyName);
                break;
            case 'G':
                if (qName.equals("GivenName"))
                    endPNComponent(PersonName.Component.GivenName);
                break;
            case 'I':
                if (qName.equals("Item"))
                    endItem();
                break;
            case 'M':
                if (qName.equals("MiddleName"))
                    endPNComponent(PersonName.Component.MiddleName);
                break;
            case 'N':
                if (qName.equals("NamePrefix"))
                    endPNComponent(PersonName.Component.NamePrefix);
                else if (qName.equals("NameSuffix"))
                    endPNComponent(PersonName.Component.NameSuffix);
                break;
            case 'P':
                if (qName.equals("PersonName"))
                    endPersonName();
                break;
            case 'V':
                if (qName.equals("Value")) {
                    endValue();
                }
                break;
        }
        processCharacters = false;
    }

    @Override
    public void endDocument() {
        if (null != fmi)
            fmi.trimToSize();
        items.getFirst().trimToSize();
    }

    private void endDataFragment() {
        if (null != bulkData) {
            dataFragments.add(bulkData);
            bulkData = null;
        } else {
            dataFragments.add(getBytes());
        }
    }

    private void endDicomAttribute() {
        if (vr == VR.SQ) {
            seqs.removeLast().trimToSize();
            return;
        }
        if (null != dataFragments) {
            dataFragments.trimToSize();
            dataFragments = null;
            return;
        }
        Attributes attrs = items.getLast();
        if (Tag.isFileMetaInformation(tag)) {
            if (null == fmi)
                fmi = new Attributes();
            attrs = fmi;
        }
        if (null != bulkData) {
            attrs.setValue(privateCreator, tag, vr, bulkData);
            bulkData = null;
        } else if (inlineBinary) {
            attrs.setBytes(privateCreator, tag, vr, getBytes());
            inlineBinary = false;
        } else {
            attrs.setString(privateCreator, tag, vr, getStrings());
        }
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
            return values.toArray(new String[values.size()]);
        } finally {
            values.clear();
        }
    }

}
