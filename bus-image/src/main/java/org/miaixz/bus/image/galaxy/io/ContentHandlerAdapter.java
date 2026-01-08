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
 * A SAX {@link org.xml.sax.ContentHandler} adapter that parses DICOM XML representations into an {@link Attributes}
 * object structure. This class handles the conversion of XML elements and attributes into DICOM tags, VRs, and values,
 * including nested sequences and bulk data.
 * 
 * @author Kimi Liu
 * @since Java 17+
 */
public class ContentHandlerAdapter extends DefaultHandler {

    /**
     * Flag indicating whether parsing should be lenient, ignoring certain errors.
     */
    private final boolean lenient;
    /**
     * Stack of {@link Attributes} objects, representing the current nesting level in DICOM sequences.
     */
    private final LinkedList<Attributes> items = new LinkedList<>();
    /**
     * Stack of {@link Sequence} objects, representing the current sequence being parsed.
     */
    private final LinkedList<Sequence> seqs = new LinkedList<>();
    /**
     * Output stream used to collect binary data from inline binary elements.
     */
    private final ByteArrayOutputStream bout = new ByteArrayOutputStream(64);
    /**
     * Buffer for handling partial character data when decoding inline binary.
     */
    private final char[] carry = new char[4];
    /**
     * String builder for collecting character data for string values.
     */
    private final StringBuilder sb = new StringBuilder(64);
    /**
     * List to store multiple values for a multi-valued DICOM attribute.
     */
    private final ArrayList<String> values = new ArrayList<>();
    /**
     * Creator for {@link BulkData} objects, allowing custom handling of bulk data storage.
     */
    private BulkData.Creator bulkDataCreator = BulkData::new;
    /**
     * Stores File Meta Information attributes.
     */
    private Attributes fmi;
    /**
     * Flag indicating the endianness of the current dataset.
     */
    private boolean bigEndian;
    /**
     * Length of the {@code carry} buffer currently in use.
     */
    private int carryLen;
    /**
     * Current {@link PersonName} object being parsed.
     */
    private PersonName pn;
    /**
     * Current {@link PersonName.Group} being parsed.
     */
    private PersonName.Group pnGroup;
    /**
     * The DICOM tag of the attribute currently being parsed.
     */
    private int tag;
    /**
     * The private creator of the attribute currently being parsed.
     */
    private String privateCreator;
    /**
     * The Value Representation (VR) of the attribute currently being parsed.
     */
    private VR vr;
    /**
     * Current {@link BulkData} object being parsed.
     */
    private BulkData bulkData;
    /**
     * Current {@link Fragments} object being parsed.
     */
    private Fragments dataFragments;
    /**
     * Flag indicating whether character data should be processed.
     */
    private boolean processCharacters;
    /**
     * Flag indicating whether the current element is an inline binary element.
     */
    private boolean inlineBinary;

    /**
     * Constructs a {@code ContentHandlerAdapter} with a given initial {@link Attributes} object.
     * 
     * @param attrs The initial {@link Attributes} object to populate.
     */
    public ContentHandlerAdapter(Attributes attrs) {
        this(attrs, false);
    }

    /**
     * Constructs a {@code ContentHandlerAdapter} with a given initial {@link Attributes} object and leniency setting.
     * 
     * @param attrs   The initial {@link Attributes} object to populate.
     * @param lenient {@code true} if parsing should be lenient, {@code false} otherwise.
     */
    public ContentHandlerAdapter(Attributes attrs, boolean lenient) {
        if (attrs != null) {
            items.add(attrs);
            bigEndian = attrs.bigEndian();
        }
        this.lenient = lenient;
    }

    /**
     * Determines the endianness from the File Meta Information. If the Transfer Syntax UID is Explicit VR Big Endian,
     * then big-endian is assumed.
     * 
     * @param fmi The File Meta Information attributes.
     * @return {@code true} if big-endian, {@code false} otherwise.
     */
    private static boolean bigEndian(Attributes fmi) {
        return fmi != null && UID.ExplicitVRBigEndian.equals(fmi.getString(Tag.TransferSyntaxUID));
    }

    /**
     * Generates a prefix string for logging or display based on the private creator and nesting level.
     * 
     * @param privateCreator The private creator string.
     * @param level          The current nesting level.
     * @return A prefix string.
     */
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

    /**
     * Sets the {@link BulkData.Creator} to be used for creating {@link BulkData} objects.
     * 
     * @param bulkDataCreator The {@link BulkData.Creator} instance.
     */
    public void setBulkDataCreator(BulkData.Creator bulkDataCreator) {
        this.bulkDataCreator = Objects.requireNonNull(bulkDataCreator);
    }

    /**
     * Returns the parsed File Meta Information attributes.
     * 
     * @return The {@link Attributes} object containing File Meta Information.
     */
    public Attributes getFileMetaInformation() {
        return fmi;
    }

    /**
     * Returns the root dataset (the top-level {@link Attributes} object).
     * 
     * @return The root {@link Attributes} object.
     */
    public Attributes getDataset() {
        return items.getFirst();
    }

    @Override
    public void startElement(String uri, String localName, String qName, org.xml.sax.Attributes atts) {
        switch (qName) {
            case "DicomAttribute":
                startDicomAttribute(
                        (int) Long.parseLong(atts.getValue("tag"), 16),
                        atts.getValue("privateCreator"),
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

    /**
     * Handles the start of a &lt;BulkData&gt; element, creating a {@link BulkData} object.
     * 
     * @param uuid The UUID attribute from the XML.
     * @param uri  The URI attribute from the XML.
     */
    private void bulkData(String uuid, String uri) {
        bulkData = bulkDataCreator.create(uuid, uri, items.getLast().bigEndian());
    }

    /**
     * Handles the start of an &lt;InlineBinary&gt; element, preparing to read binary data.
     */
    private void startInlineBinary() {
        processCharacters = true;
        inlineBinary = true;
        carryLen = 0;
        bout.reset();
    }

    /**
     * Prepares to read character data for a text-based element.
     */
    private void startText() {
        processCharacters = true;
        inlineBinary = false;
        sb.setLength(0);
    }

    /**
     * Handles the start of a &lt;DicomAttribute&gt; element, setting the current tag, private creator, and VR. If the
     * VR is SQ (Sequence), a new sequence is created.
     * 
     * @param tag            The DICOM tag.
     * @param privateCreator The private creator string.
     * @param vr             The VR string.
     */
    private void startDicomAttribute(int tag, String privateCreator, String vr) {
        this.tag = tag;
        this.privateCreator = privateCreator;
        this.vr = vr != null ? VR.valueOf(vr) : ElementDictionary.vrOf(tag, privateCreator);
        if (this.vr == VR.SQ)
            seqs.add(items.getLast().newSequence(privateCreator, tag, 10));
    }

    /**
     * Handles the start of a &lt;DataFragment&gt; element, initializing data fragments if necessary.
     * 
     * @param number The fragment number.
     */
    private void startDataFragment(int number) {
        if (dataFragments == null)
            dataFragments = items.getLast().newFragments(privateCreator, tag, vr, 10);
        while (dataFragments.size() < number - 1)
            dataFragments.add(new byte[] {});
    }

    /**
     * Handles the start of an &lt;Item&gt; element, adding a new {@link Attributes} object to the current sequence.
     * 
     * @param number The item number.
     */
    private void startItem(int number) {
        Sequence seq = seqs.getLast();
        while (seq.size() < number - 1)
            seq.add(new Attributes(bigEndian, 0));
        Attributes item = new Attributes(bigEndian);
        seq.add(item);
        items.add(item);
    }

    /**
     * Prepares to read a multi-valued attribute by ensuring enough slots in the {@code values} list.
     * 
     * @param number The value number.
     */
    private void startValue(int number) {
        while (values.size() < number - 1)
            values.add(null);
    }

    /**
     * Handles the start of a &lt;PersonName&gt; element, initializing a new {@link PersonName} object.
     * 
     * @param number The value number for the Person Name.
     */
    private void startPersonName(int number) {
        startValue(number);
        pn = new PersonName();
    }

    /**
     * Handles the start of a Person Name group element (Alphabetic, Ideographic, Phonetic).
     * 
     * @param pnGroup The {@link PersonName.Group} being started.
     */
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

    /**
     * Handles the end of a &lt;DataFragment&gt; element, adding the collected data to {@code dataFragments}.
     */
    private void endDataFragment() {
        if (bulkData != null) {
            dataFragments.add(bulkData);
            bulkData = null;
        } else {
            dataFragments.add(getBytes());
        }
    }

    /**
     * Handles the end of a &lt;DicomAttribute&gt; element, setting the value of the attribute.
     * 
     * @throws SAXException if an error occurs during value conversion or if parsing is not lenient.
     */
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
                String message = String.format(
                        "Invalid %s(%04X,%04X) %s %s",
                        prefix(privateCreator, items.size() - 1),
                        Tag.groupNumber(tag),
                        Tag.elementNumber(tag),
                        vr,
                        Arrays.toString(value));
                if (lenient) {
                    Logger.info("{} - ignored", message);
                } else {
                    throw new SAXException(message, e);
                }
            }
        }
    }

    /**
     * Returns the current {@link Attributes} object to which attributes should be added. This method handles the
     * creation of File Meta Information attributes if necessary.
     * 
     * @return The current {@link Attributes} object.
     */
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

    /**
     * Handles the end of an &lt;Item&gt; element, removing the current item from the stack.
     */
    private void endItem() {
        items.removeLast().trimToSize();
        vr = VR.SQ;
    }

    /**
     * Handles the end of a &lt;PersonName&gt; element, adding the parsed Person Name to the values list.
     */
    private void endPersonName() {
        values.add(pn.toString());
        pn = null;
    }

    /**
     * Handles the end of a &lt;Value&gt; element, adding the collected string to the values list.
     */
    private void endValue() {
        values.add(getString());
    }

    /**
     * Handles the end of a Person Name component element (e.g., FamilyName, GivenName).
     * 
     * @param pnComp The {@link PersonName.Component} being ended.
     */
    private void endPNComponent(PersonName.Component pnComp) {
        pn.set(pnGroup, pnComp, getString());
    }

    /**
     * Returns the string collected by the {@code StringBuilder}.
     * 
     * @return The collected string.
     */
    private String getString() {
        return sb.toString();
    }

    /**
     * Returns the byte array collected by the {@code ByteArrayOutputStream}, applying endianness correction if
     * necessary.
     * 
     * @return The collected byte array.
     */
    private byte[] getBytes() {
        byte[] b = bout.toByteArray();
        return bigEndian ? vr.toggleEndian(b, false) : b;
    }

    /**
     * Returns the collected string values as an array and clears the internal list.
     * 
     * @return An array of string values.
     */
    private String[] getStrings() {
        try {
            return values.toArray(Normal.EMPTY_STRING_ARRAY);
        } finally {
            values.clear();
        }
    }

}
