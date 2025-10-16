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
package org.miaixz.bus.image;

import java.io.*;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.ResourceKit;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.Code;
import org.miaixz.bus.image.galaxy.data.Sequence;
import org.miaixz.bus.image.galaxy.data.VR;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Represents a DICOM Information Object Definition (IOD), which specifies the modules and attributes for a particular
 * type of DICOM object. This class models the IOD as a list of {@link DataElement}s, which can be loaded from an XML
 * definition file.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class IOD extends ArrayList<IOD.DataElement> {

    /**
     * The serial version UID for serialization.
     */
    @Serial
    private static final long serialVersionUID = 2852255015270L;

    /**
     * The type of the IOD, typically for a sequence item.
     */
    private DataElementType type;
    /**
     * The condition under which this IOD (as a sequence item) is required.
     */
    private Condition condition;
    /**
     * The line number in the source XML file where this IOD was defined.
     */
    private int lineNumber = -1;

    /**
     * Loads an IOD from an XML definition file specified by a URI. The URI can be a file path, a URL, or a classpath
     * resource (prefixed with "resource:").
     *
     * @param uri The URI of the XML definition file.
     * @return A new {@code IOD} instance populated from the definition.
     * @throws IOException if an I/O error occurs or the resource is not found.
     */
    public static IOD load(String uri) throws IOException {
        if (uri.startsWith("resource:")) {
            try {
                uri = ResourceKit.getResourceUrl(uri.substring(9), IOD.class).toString();
            } catch (NullPointerException npe) {
                throw new FileNotFoundException(uri);
            }
        } else if (uri.indexOf(Symbol.C_COLON) < 2) {
            uri = new File(uri).toURI().toString();
        }
        IOD iod = new IOD();
        iod.parse(uri);
        iod.trimToSize();
        return iod;
    }

    /**
     * Creates a simple IOD for a {@link Code} sequence item.
     *
     * @param code The code to represent as an IOD.
     * @return A new {@code IOD} instance containing data elements for the code.
     */
    public static IOD valueOf(Code code) {
        IOD iod = new IOD();
        iod.add(new DataElement(Tag.CodeValue, VR.SH, DataElementType.TYPE_1, 1, 1, 0).setValues(code.getCodeValue()));
        iod.add(
                new DataElement(Tag.CodingSchemeDesignator, VR.SH, DataElementType.TYPE_1, 1, 1, 0)
                        .setValues(code.getCodingSchemeDesignator()));
        String codingSchemeVersion = code.getCodingSchemeVersion();
        if (codingSchemeVersion == null)
            iod.add(new DataElement(Tag.CodingSchemeVersion, VR.SH, DataElementType.TYPE_0, -1, -1, 0));
        else
            iod.add(new DataElement(Tag.CodingSchemeVersion, VR.SH, DataElementType.TYPE_1, 1, 1, 0));

        return iod;
    }

    /**
     * Gets the data element type of this IOD, relevant when it represents a sequence item.
     *
     * @return The data element type.
     */
    public DataElementType getType() {
        return type;
    }

    /**
     * Sets the data element type for this IOD.
     *
     * @param type The data element type.
     */
    public void setType(DataElementType type) {
        this.type = type;
    }

    /**
     * Gets the condition under which this IOD is required.
     *
     * @return The condition.
     */
    public Condition getCondition() {
        return condition;
    }

    /**
     * Sets the condition for this IOD.
     *
     * @param condition The condition.
     */
    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    /**
     * Gets the line number from the source XML definition.
     *
     * @return The line number, or -1 if not available.
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * Sets the line number from the source XML definition.
     *
     * @param lineNumber The line number.
     */
    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    /**
     * Parses an XML IOD definition from a given URI and populates this IOD instance.
     *
     * @param uri The URI of the XML definition file.
     * @throws IOException if a parsing error occurs.
     */
    public void parse(String uri) throws IOException {
        try {
            SAXParserFactory f = SAXParserFactory.newInstance();
            SAXParser parser = f.newSAXParser();
            parser.parse(uri, new SAXHandler(this));
        } catch (SAXException e) {
            throw new IOException("Failed to parse " + uri, e);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Defines the DICOM data element types, which specify the requirement level for an attribute in an IOD.
     */
    public enum DataElementType {
        /**
         * Type 0 (Not Used): Placeholder, not used in standard IODs.
         */
        TYPE_0,
        /**
         * Type 1 (Mandatory): The attribute must be present and have a value.
         */
        TYPE_1,
        /**
         * Type 2 (Mandatory if Known): The attribute must be present if the value is known.
         */
        TYPE_2,
        /**
         * Type 3 (Optional): The attribute is optional.
         */
        TYPE_3
    }

    /**
     * Represents a single data element definition within an IOD, including its tag, VR, type, value multiplicity (VM),
     * and any conditions for its inclusion.
     */
    public static class DataElement implements Serializable {

        /**
         * The serial version UID for serialization.
         */
        @Serial
        private static final long serialVersionUID = 2852255091391L;

        /** The DICOM tag of the data element. */
        public final int tag;
        /** The Value Representation (VR) of the data element. */
        public final VR vr;
        /** The requirement type (e.g., mandatory, optional). */
        public final DataElementType type;
        /** The minimum value multiplicity. */
        public final int minVM;
        /** The maximum value multiplicity. */
        public final int maxVM;
        /** The specific value index this definition applies to (for multi-valued attributes). */
        public final int valueNumber;
        /** The condition under which this data element is required. */
        private Condition condition;
        /** The defined values or item IODs for this element. */
        private Object values;
        /** The line number in the source XML file where this element was defined. */
        private int lineNumber = -1;

        /**
         * Constructs a new DataElement.
         *
         * @param tag         The DICOM tag.
         * @param vr          The Value Representation.
         * @param type        The data element type.
         * @param minVM       The minimum value multiplicity.
         * @param maxVM       The maximum value multiplicity.
         * @param valueNumber The value index.
         */
        public DataElement(int tag, VR vr, DataElementType type, int minVM, int maxVM, int valueNumber) {
            this.tag = tag;
            this.vr = vr;
            this.type = type;
            this.minVM = minVM;
            this.maxVM = maxVM;
            this.valueNumber = valueNumber;
        }

        /**
         * Gets the condition for this data element.
         *
         * @return The condition.
         */
        public Condition getCondition() {
            return condition;
        }

        /**
         * Sets the condition for this data element.
         *
         * @param condition The condition to set.
         * @return This {@code DataElement} instance for chaining.
         */
        public DataElement setCondition(Condition condition) {
            this.condition = condition;
            return this;
        }

        /**
         * Gets the value index for this definition.
         *
         * @return The value number.
         */
        public int getValueNumber() {
            return valueNumber;
        }

        /**
         * Adds an IOD for a sequence item to this data element (if VR is SQ).
         *
         * @param iod The IOD of the sequence item.
         * @return This {@code DataElement} instance for chaining.
         */
        public DataElement addItemIOD(IOD iod) {
            if (this.values == null) {
                this.values = new IOD[] { iod };
            } else {
                IOD[] iods = (IOD[]) this.values;
                iods = Arrays.copyOf(iods, iods.length + 1);
                iods[iods.length - 1] = iod;
                this.values = iods;
            }
            return this;
        }

        /**
         * Gets the defined values for this element (e.g., standard values, item IODs).
         *
         * @return The values object.
         */
        public Object getValues() {
            return values;
        }

        /**
         * Sets the defined string values for this element.
         *
         * @param values The string values.
         * @return This {@code DataElement} instance for chaining.
         * @throws IllegalStateException if the VR is SQ.
         */
        public DataElement setValues(String... values) {
            if (vr == VR.SQ)
                throw new IllegalStateException("vr=SQ");
            this.values = values;
            return this;
        }

        /**
         * Sets the defined integer values for this element.
         *
         * @param values The integer values.
         * @return This {@code DataElement} instance for chaining.
         * @throws IllegalStateException if the VR is not an integer type.
         */
        public DataElement setValues(int... values) {
            if (!vr.isIntType())
                throw new IllegalStateException("vr=" + vr);
            this.values = values;
            return this;
        }

        /**
         * Sets the defined Code values for this element (for Code Sequences).
         *
         * @param values The Code values.
         * @return This {@code DataElement} instance for chaining.
         * @throws IllegalStateException if the VR is not SQ.
         */
        public DataElement setValues(Code... values) {
            if (vr != VR.SQ)
                throw new IllegalStateException("vr=" + vr);
            this.values = values;
            return this;
        }

        /**
         * Gets the line number from the source XML definition.
         *
         * @return The line number.
         */
        public int getLineNumber() {
            return lineNumber;
        }

        /**
         * Sets the line number from the source XML definition.
         *
         * @param lineNumber The line number.
         * @return This {@code DataElement} instance for chaining.
         */
        public DataElement setLineNumber(int lineNumber) {
            this.lineNumber = lineNumber;
            return this;
        }

    }

    /**
     * Abstract base class for defining conditions under which a data element or sequence item is mandatory. This is
     * used to model conditional DICOM types like 1C and 2C.
     */
    public abstract static class Condition {

        /** An optional identifier for the condition, used for referencing. */
        protected String id;
        /** A flag to invert the logic of the condition (NOT). */
        protected boolean not;

        /**
         * Sets the identifier for this condition.
         *
         * @param id The identifier string.
         * @return This {@code Condition} instance for chaining.
         */
        public Condition id(String id) {
            this.id = id;
            return this;
        }

        /**
         * Gets the identifier of this condition.
         *
         * @return The identifier string.
         */
        public final String id() {
            return id;
        }

        /**
         * Inverts the logic of this condition (e.g., from 'is present' to 'is not present').
         *
         * @return This {@code Condition} instance for chaining.
         */
        public final Condition not() {
            this.not = !not;
            return this;
        }

        /**
         * Evaluates this condition against a set of DICOM attributes.
         *
         * @param attrs The attributes to check.
         * @return {@code true} if the condition is met, {@code false} otherwise.
         */
        public abstract boolean match(Attributes attrs);

        /**
         * Adds a child condition to this condition (for composite conditions).
         *
         * @param child The child condition to add.
         * @throws UnsupportedOperationException if the condition is not composite.
         */
        public void addChild(Condition child) {
            throw new UnsupportedOperationException();
        }

        /**
         * Simplifies the condition structure, e.g., by unwrapping a composite condition with a single child.
         *
         * @return The trimmed condition.
         */
        public Condition trim() {
            return this;
        }

        /**
         * Checks if the condition is empty (e.g., a composite condition with no children).
         *
         * @return {@code true} if empty, {@code false} otherwise.
         */
        public boolean isEmpty() {
            return false;
        }

    }

    /**
     * Abstract base class for conditions that are composed of multiple child conditions (e.g., AND, OR).
     */
    abstract static class CompositeCondition extends Condition {

        /** The list of child conditions. */
        protected final ArrayList<Condition> childs = new ArrayList<>();

        /**
         * {@inheritDoc}
         */
        public abstract boolean match(Attributes attrs);

        /**
         * {@inheritDoc}
         */
        @Override
        public void addChild(Condition child) {
            childs.add(child);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Condition trim() {
            int size = childs.size();
            if (size == 1) {
                Condition child = childs.get(0).id(id);
                return not ? child.not() : child;
            }
            childs.trimToSize();
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isEmpty() {
            return childs.isEmpty();
        }
    }

    /**
     * A composite condition that is met if all its child conditions are met (logical AND).
     */
    public static class And extends CompositeCondition {

        /**
         * {@inheritDoc}
         */
        public boolean match(Attributes attrs) {
            for (Condition child : childs) {
                if (!child.match(attrs))
                    return not;
            }
            return !not;
        }
    }

    /**
     * A composite condition that is met if at least one of its child conditions is met (logical OR).
     */
    public static class Or extends CompositeCondition {

        /**
         * {@inheritDoc}
         */
        public boolean match(Attributes attrs) {
            for (Condition child : childs) {
                if (child.match(attrs))
                    return !not;
            }
            return not;
        }
    }

    /**
     * A condition that checks for the presence of a specific DICOM tag.
     */
    public static class Present extends Condition {

        /** The DICOM tag to check for presence. */
        protected final int tag;
        /** The path of sequence tags to navigate to the item where the tag should be present. */
        protected final int[] itemPath;

        /**
         * Constructs a Present condition.
         *
         * @param tag      The tag to check.
         * @param itemPath The sequence path to the relevant dataset.
         */
        public Present(int tag, int... itemPath) {
            this.tag = tag;
            this.itemPath = itemPath;
        }

        /**
         * {@inheritDoc}
         */
        public boolean match(Attributes attrs) {
            return not != item(attrs).containsValue(tag);
        }

        /**
         * Navigates to the correct dataset item based on the itemPath.
         *
         * @param attrs The root attributes.
         * @return The target dataset attributes.
         */
        protected Attributes item(Attributes attrs) {
            for (int sqtag : itemPath) {
                attrs = (sqtag == -1) ? attrs.getParent() : attrs.getNestedDataset(sqtag);
            }
            return attrs;
        }
    }

    /**
     * A condition that checks if the value of an attribute is a member of a specified set of values.
     */
    public static class MemberOf extends Present {

        /** The VR of the attribute to check. */
        private final VR vr;
        /** The index of the value to check in a multi-valued attribute. */
        private final int valueIndex;
        /** Flag indicating how to match if the attribute is not present. */
        private final boolean matchNotPresent;
        /** The set of values to check against. */
        private Object values;

        /**
         * Constructs a MemberOf condition.
         *
         * @param tag             The tag to check.
         * @param vr              The VR of the tag.
         * @param valueIndex      The value index.
         * @param matchNotPresent The result if the attribute is not present.
         * @param itemPath        The sequence path.
         */
        public MemberOf(int tag, VR vr, int valueIndex, boolean matchNotPresent, int... itemPath) {
            super(tag, itemPath);
            this.vr = vr;
            this.valueIndex = valueIndex;
            this.matchNotPresent = matchNotPresent;
        }

        /**
         * Gets the VR of the attribute.
         *
         * @return The VR.
         */
        public VR vr() {
            return vr;
        }

        /**
         * Sets the string values to match against.
         *
         * @param values The array of string values.
         * @return This {@code MemberOf} instance for chaining.
         */
        public MemberOf setValues(String... values) {
            if (vr == VR.SQ)
                throw new IllegalStateException("vr=SQ");
            this.values = values;
            return this;
        }

        /**
         * Sets the integer values to match against.
         *
         * @param values The array of integer values.
         * @return This {@code MemberOf} instance for chaining.
         */
        public MemberOf setValues(int... values) {
            if (!vr.isIntType())
                throw new IllegalStateException("vr=" + vr);
            this.values = values;
            return this;
        }

        /**
         * Sets the Code values to match against (for Code Sequences).
         *
         * @param values The array of Code values.
         * @return This {@code MemberOf} instance for chaining.
         */
        public MemberOf setValues(Code... values) {
            if (vr != VR.SQ)
                throw new IllegalStateException("vr=" + vr);
            this.values = values;
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean match(Attributes attrs) {
            if (values == null)
                throw new IllegalStateException("values not initialized");
            Attributes item = item(attrs);
            if (item == null)
                return matchNotPresent;

            if (values instanceof int[])
                return not != match(item, ((int[]) values));
            else if (values instanceof Code[])
                return not != match(item, ((Code[]) values));
            else
                return not != match(item, ((String[]) values));
        }

        /**
         * Private helper to match against string values.
         *
         * @param item The dataset.
         * @param ss   The array of strings to match.
         * @return {@code true} on match.
         */
        private boolean match(Attributes item, String[] ss) {
            String val = item.getString(tag, valueIndex);
            if (val == null)
                return not != matchNotPresent;
            for (String s : ss) {
                if (s.equals(val))
                    return !not;
            }
            return not;
        }

        /**
         * Private helper to match against Code values.
         *
         * @param item  The dataset.
         * @param codes The array of codes to match.
         * @return {@code true} on match.
         */
        private boolean match(Attributes item, Code[] codes) {
            Sequence seq = item.getSequence(tag);
            if (seq != null) {
                for (Attributes codeItem : seq) {
                    try {
                        Code val = new Code(codeItem);
                        for (Code code : codes) {
                            if (code.equals(val))
                                return !not;
                        }
                    } catch (NullPointerException npe) {
                        // Ignore items that don't form a valid Code
                    }
                }
            }
            return not;
        }

        /**
         * Private helper to match against integer values.
         *
         * @param item The dataset.
         * @param is   The array of integers to match.
         * @return {@code true} on match.
         */
        private boolean match(Attributes item, int[] is) {
            int val = item.getInt(tag, valueIndex, Integer.MIN_VALUE);
            if (val == Integer.MIN_VALUE)
                return matchNotPresent;
            for (int i : is) {
                if (i == val)
                    return true;
            }
            return false;
        }
    }

    /**
     * A private SAX handler for parsing XML-based IOD definitions into an {@link IOD} object. It processes elements
     * like {@code <DataElement>}, {@code <Item>}, and various condition types like {@code <And>}, {@code <Present>},
     * and {@code <MemberOf>}.
     */
    private static class SAXHandler extends DefaultHandler {

        /** StringBuilder for collecting character data. */
        private final StringBuilder sb = new StringBuilder();
        /** List for collecting parsed string values. */
        private final List<String> values = new ArrayList<>();
        /** List for collecting parsed Code objects. */
        private final List<Code> codes = new ArrayList<>();
        /** Stack for tracking the current IOD being built (for nested items). */
        private final LinkedList<IOD> iodStack = new LinkedList<>();
        /** Stack for tracking the current Condition being built (for nested conditions). */
        private final LinkedList<Condition> conditionStack = new LinkedList<>();
        /** Map for resolving IOD references by ID. */
        private final Map<String, IOD> id2iod = new HashMap<>();
        /** Map for resolving Condition references by ID. */
        private final Map<String, Condition> id2cond = new HashMap<>();
        /** Flag to enable character processing. */
        private boolean processCharacters;
        /** Flag indicating the current context is for an element's condition. */
        private boolean elementConditions;
        /** Flag indicating the current context is for an item's condition. */
        private boolean itemConditions;
        /** Stores the ID reference for elements like Item or If. */
        private String idref;
        /** SAX locator for tracking line numbers. */
        private Locator locator;

        /**
         * Constructs the SAX handler.
         *
         * @param iod The root IOD object to populate.
         */
        public SAXHandler(IOD iod) {
            iodStack.add(iod);
        }

        @Override
        public void setDocumentLocator(Locator locator) {
            this.locator = locator;
        }

        @Override
        public void startElement(String uri, String localName, String qName, org.xml.sax.Attributes atts)
                throws SAXException {
            switch (qName) {
                case "And":
                    startCondition(qName, new And());
                    break;

                case "Code":
                    startCode(
                            atts.getValue("codeValue"),
                            atts.getValue("codingSchemeDesignator"),
                            atts.getValue("codingSchemeVersion"),
                            atts.getValue("codeMeaning"));
                    break;

                case "DataElement":
                    startDataElement(
                            atts.getValue("tag"),
                            atts.getValue("vr"),
                            atts.getValue("type"),
                            atts.getValue("vm"),
                            atts.getValue("items"),
                            atts.getValue("valueNumber"));
                    break;

                case "If":
                    startIf(atts.getValue("id"), atts.getValue("idref"));
                    break;

                case "Item":
                    startItem(atts.getValue("id"), atts.getValue("idref"), atts.getValue("type"));
                    break;

                case "MemberOf":
                    startCondition(qName, memberOf(atts));
                    break;

                case "NotAnd":
                    startCondition(qName, new And().not());
                    break;

                case "NotMemberOf":
                    startCondition(qName, memberOf(atts).not());
                    break;

                case "NotOr":
                    startCondition(qName, new Or().not());
                    break;

                case "NotPresent":
                    startCondition(qName, present(atts).not());
                    break;

                case "Or":
                    startCondition(qName, new Or());
                    break;

                case "Present":
                    startCondition(qName, present(atts));
                    break;

                case "Value":
                    startValue();
                    break;
            }
        }

        /**
         * Creates a {@link Present} condition from SAX attributes.
         *
         * @param atts The XML element attributes.
         * @return A new {@link Present} condition.
         * @throws SAXException if attributes are invalid.
         */
        private Present present(org.xml.sax.Attributes atts) throws SAXException {
            int[] tagPath = tagPathOf(atts.getValue("tag"));
            int lastIndex = tagPath.length - 1;
            return new Present(tagPath[lastIndex], lastIndex > 0 ? Arrays.copyOf(tagPath, lastIndex) : new int[] {});
        }

        /**
         * Creates a {@link MemberOf} condition from SAX attributes.
         *
         * @param atts The XML element attributes.
         * @return A new {@link MemberOf} condition.
         * @throws SAXException if attributes are invalid.
         */
        private MemberOf memberOf(org.xml.sax.Attributes atts) throws SAXException {
            int[] tagPath = tagPathOf(atts.getValue("tag"));
            int lastIndex = tagPath.length - 1;
            return new MemberOf(tagPath[lastIndex], vrOf(atts.getValue("vr")),
                    valueNumberOf(atts.getValue("valueNumber"), 1) - 1,
                    matchNotPresentOf(atts.getValue("matchNotPresent")),
                    lastIndex > 0 ? Arrays.copyOf(tagPath, lastIndex) : new int[] {});
        }

        /**
         * Handles the start of a {@code <Code>} element.
         *
         * @param codeValue              The code value.
         * @param codingSchemeDesignator The coding scheme designator.
         * @param codingSchemeVersion    The coding scheme version.
         * @param codeMeaning            The code meaning.
         * @throws SAXException if required attributes are missing.
         */
        private void startCode(
                String codeValue,
                String codingSchemeDesignator,
                String codingSchemeVersion,
                String codeMeaning) throws SAXException {
            if (codeValue == null)
                throw new SAXException("missing codeValue attribute");
            if (codingSchemeDesignator == null)
                throw new SAXException("missing codingSchemeDesignator attribute");
            if (codeMeaning == null)
                throw new SAXException("missing codeMeaning attribute");
            codes.add(new Code(codeValue, codingSchemeDesignator, codingSchemeVersion, codeMeaning));
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            switch (qName) {
                case "DataElement":
                    endDataElement();
                    break;

                case "Item":
                    endItem();
                    break;

                case "Value":
                    endValue();
                    break;

                case "And":
                case "If":
                case "MemberOf":
                case "NotAnd":
                case "NotMemberOf":
                case "NotOr":
                case "NotPresent":
                case "Or":
                case "Present":
                    endCondition(qName);
                    break;
            }
            processCharacters = false;
            idref = null;
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            if (processCharacters)
                sb.append(ch, start, length);
        }

        /**
         * Handles the start of a {@code <DataElement>} element.
         *
         * @throws SAXException on parsing errors.
         */
        private void startDataElement(
                String tagStr,
                String vrStr,
                String typeStr,
                String vmStr,
                String items,
                String valueNumberStr) throws SAXException {
            if (idref != null)
                throw new SAXException("<Item> with idref must be empty");

            IOD iod = iodStack.getLast();
            int tag = tagOf(tagStr);
            VR vr = vrOf(vrStr);
            DataElementType type = typeOf(typeStr);

            int minVM = -1;
            int maxVM = -1;
            String vm = vr == VR.SQ ? items : vmStr;
            if (vm != null) {
                try {
                    String[] ss = Builder.split(vm, Symbol.C_MINUS);
                    if (ss[0].charAt(0) != 'n') {
                        minVM = Integer.parseInt(ss[0]);
                        if (ss.length > 1) {
                            if (ss[1].charAt(0) != 'n')
                                maxVM = Integer.parseInt(ss[1]);
                        } else {
                            maxVM = minVM;
                        }
                    }
                } catch (IllegalArgumentException e) {
                    throw new SAXException(
                            (vr == VR.SQ ? "invalid items=\"" : "invalid vm=\"") + vm + Symbol.C_DOUBLE_QUOTES);
                }
            }
            DataElement el = new DataElement(tag, vr, type, minVM, maxVM, valueNumberOf(valueNumberStr, 0));
            if (locator != null)
                el.setLineNumber(locator.getLineNumber());
            iod.add(el);
            elementConditions = true;
            itemConditions = false;
        }

        /**
         * Parses the DataElementType from a string.
         */
        private DataElementType typeOf(String s) throws SAXException {
            if (s == null)
                throw new SAXException("missing type attribute");
            try {
                return DataElementType.valueOf("TYPE_" + s);
            } catch (IllegalArgumentException e) {
                throw new SAXException("unrecognized type=\"" + s + Symbol.C_DOUBLE_QUOTES);
            }
        }

        /**
         * Parses the VR from a string.
         */
        private VR vrOf(String s) throws SAXException {
            try {
                return VR.valueOf(s);
            } catch (NullPointerException e) {
                throw new SAXException("missing vr attribute");
            } catch (IllegalArgumentException e) {
                throw new SAXException("unrecognized vr=\"" + s + Symbol.C_DOUBLE_QUOTES);
            }
        }

        /**
         * Parses the DICOM tag from a hex string.
         */
        private int tagOf(String s) throws SAXException {
            try {
                return (int) Long.parseLong(s, Normal._16);
            } catch (NullPointerException e) {
                throw new SAXException("missing tag attribute");
            } catch (IllegalArgumentException e) {
                throw new SAXException("invalid tag=\"" + s + Symbol.C_DOUBLE_QUOTES);
            }
        }

        /**
         * Parses a tag path (e.g., "00100010/00400009") into an array of integer tags.
         */
        private int[] tagPathOf(String s) throws SAXException {
            String[] ss = Builder.split(s, Symbol.C_SLASH);
            if (ss.length == 0)
                throw new SAXException("missing tag attribute");

            try {
                int[] tagPath = new int[ss.length];
                for (int i = 0; i < tagPath.length; i++)
                    tagPath[i] = ss[i].equals(Symbol.DOUBLE_DOT) ? -1 : (int) Long.parseLong(ss[i], Normal._16);
                return tagPath;
            } catch (IllegalArgumentException e) {
                throw new SAXException("invalid tag=\"" + s + Symbol.C_DOUBLE_QUOTES);
            }
        }

        /**
         * Parses the value number from a string.
         */
        private int valueNumberOf(String s, int def) throws SAXException {
            try {
                return s != null ? Integer.parseInt(s) : def;
            } catch (IllegalArgumentException e) {
                throw new SAXException("invalid valueNumber=\"" + s + Symbol.C_DOUBLE_QUOTES);
            }
        }

        /**
         * Parses the matchNotPresent boolean flag from a string.
         */
        private boolean matchNotPresentOf(String s) {
            return s != null && s.equalsIgnoreCase("true");
        }

        /**
         * Gets the most recently added DataElement.
         */
        private DataElement getLastDataElement() {
            IOD iod = iodStack.getLast();
            return iod.get(iod.size() - 1);
        }

        /**
         * Handles the end of a {@code <DataElement>} element.
         */
        private void endDataElement() throws SAXException {
            DataElement el = getLastDataElement();
            if (!values.isEmpty()) {
                try {
                    if (el.vr.isIntType())
                        el.setValues(parseInts(values));
                    else
                        el.setValues(values.toArray(new String[values.size()]));
                } catch (IllegalStateException e) {
                    throw new SAXException("unexpected <Value>");
                }
                values.clear();
            }
            if (!codes.isEmpty()) {
                try {
                    el.setValues(codes.toArray(new Code[codes.size()]));
                } catch (IllegalStateException e) {
                    throw new SAXException("unexpected <Code>");
                }
                codes.clear();
            }
            elementConditions = false;
        }

        /**
         * Parses a list of strings into an int array.
         */
        private int[] parseInts(List<String> list) {
            int[] is = new int[list.size()];
            for (int i = 0; i < is.length; i++)
                is[i] = Integer.parseInt(list.get(i));
            return is;
        }

        /**
         * Handles the start of a {@code <Value>} element.
         */
        private void startValue() {
            sb.setLength(0);
            processCharacters = true;
        }

        /**
         * Handles the end of a {@code <Value>} element.
         */
        private void endValue() {
            values.add(sb.toString());
        }

        /**
         * Handles the start of an {@code <Item>} element.
         */
        private void startItem(String id, String idref, String type) throws SAXException {
            IOD iod;
            if (idref != null) {
                if (type != null)
                    throw new SAXException("<Item> with idref must not specify type");

                iod = id2iod.get(idref);
                if (iod == null)
                    throw new SAXException("could not resolve <Item idref:\"" + idref + "\"/>");
            } else {
                iod = new IOD();
                if (type != null)
                    iod.setType(typeOf(type));
                if (locator != null)
                    iod.setLineNumber(locator.getLineNumber());
            }
            getLastDataElement().addItemIOD(iod);
            iodStack.add(iod);
            if (id != null)
                id2iod.put(id, iod);

            this.idref = idref;
            itemConditions = true;
            elementConditions = false;
        }

        /**
         * Handles the end of an {@code <Item>} element.
         */
        private void endItem() {
            iodStack.removeLast().trimToSize();
            itemConditions = false;
        }

        /**
         * Handles the start of an {@code <If>} element.
         */
        private void startIf(String id, String idref) throws SAXException {
            if (!conditionStack.isEmpty())
                throw new SAXException("unexpected <If>");

            Condition cond;
            if (idref != null) {
                cond = id2cond.get(idref);
                if (cond == null)
                    throw new SAXException("could not resolve <If idref:\"" + idref + "\"/>");
            } else {
                cond = new And().id(id);
            }
            conditionStack.add(cond);
            if (id != null)
                id2cond.put(id, cond);
            this.idref = idref;
        }

        /**
         * Handles the start of a condition element (e.g., {@code <And>}, {@code <Present>}).
         */
        private void startCondition(String name, Condition cond) throws SAXException {
            if (!(elementConditions || itemConditions))
                throw new SAXException("unexpected <" + name + '>');

            conditionStack.add(cond);
        }

        /**
         * Handles the end of a condition element.
         */
        private void endCondition(String name) throws SAXException {
            Condition cond = conditionStack.removeLast();
            if (cond.isEmpty())
                throw new SAXException(Symbol.C_LT + name + "> must not be empty");

            if (!values.isEmpty()) {
                try {
                    MemberOf memberOf = (MemberOf) cond;
                    if (memberOf.vr.isIntType())
                        memberOf.setValues(parseInts(values));
                    else
                        memberOf.setValues(values.toArray(new String[values.size()]));
                } catch (Exception e) {
                    throw new SAXException("unexpected <Value> contained by <" + name + ">");
                }
                values.clear();
            }

            if (!codes.isEmpty()) {
                try {
                    ((MemberOf) cond).setValues(codes.toArray(new Code[codes.size()]));
                } catch (Exception e) {
                    throw new SAXException("unexpected <Code> contained by <" + name + ">");
                }
                codes.clear();
            }

            if (conditionStack.isEmpty()) {
                if (elementConditions)
                    getLastDataElement().setCondition(cond.trim());
                else
                    iodStack.getLast().setCondition(cond.trim());
                elementConditions = false;
                itemConditions = false;
            } else
                conditionStack.getLast().addChild(cond.trim());
        }
    }

}
