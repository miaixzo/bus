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
 * @author Kimi Liu
 * @since Java 17+
 */
public class IOD extends ArrayList<IOD.DataElement> {

    @Serial
    private static final long serialVersionUID = 2852238385811L;

    private DataElementType type;
    private Condition condition;
    private int lineNumber = -1;

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

    public static IOD valueOf(Code code) {
        IOD iod = new IOD();
        iod.add(new DataElement(Tag.CodeValue, VR.SH, DataElementType.TYPE_1, 1, 1, 0).setValues(code.getCodeValue()));
        iod.add(new DataElement(Tag.CodingSchemeDesignator, VR.SH, DataElementType.TYPE_1, 1, 1, 0)
                .setValues(code.getCodingSchemeDesignator()));
        String codingSchemeVersion = code.getCodingSchemeVersion();
        if (codingSchemeVersion == null)
            iod.add(new DataElement(Tag.CodingSchemeVersion, VR.SH, DataElementType.TYPE_0, -1, -1, 0));
        else
            iod.add(new DataElement(Tag.CodingSchemeVersion, VR.SH, DataElementType.TYPE_1, 1, 1, 0));

        return iod;
    }

    public DataElementType getType() {
        return type;
    }

    public void setType(DataElementType type) {
        this.type = type;
    }

    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

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

    public enum DataElementType {
        TYPE_0, TYPE_1, TYPE_2, TYPE_3
    }

    public static class DataElement implements Serializable {

        @Serial
        private static final long serialVersionUID = 2852238601653L;

        public final int tag;
        public final VR vr;
        public final DataElementType type;
        public final int minVM;
        public final int maxVM;
        public final int valueNumber;
        private Condition condition;
        private Object values;
        private int lineNumber = -1;

        public DataElement(int tag, VR vr, DataElementType type, int minVM, int maxVM, int valueNumber) {
            this.tag = tag;
            this.vr = vr;
            this.type = type;
            this.minVM = minVM;
            this.maxVM = maxVM;
            this.valueNumber = valueNumber;
        }

        public Condition getCondition() {
            return condition;
        }

        public DataElement setCondition(Condition condition) {
            this.condition = condition;
            return this;
        }

        public int getValueNumber() {
            return valueNumber;
        }

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

        public Object getValues() {
            return values;
        }

        public DataElement setValues(String... values) {
            if (vr == VR.SQ)
                throw new IllegalStateException("vr=SQ");
            this.values = values;
            return this;
        }

        public DataElement setValues(int... values) {
            if (!vr.isIntType())
                throw new IllegalStateException("vr=" + vr);
            this.values = values;
            return this;
        }

        public DataElement setValues(Code... values) {
            if (vr != VR.SQ)
                throw new IllegalStateException("vr=" + vr);
            this.values = values;
            return this;
        }

        public int getLineNumber() {
            return lineNumber;
        }

        public DataElement setLineNumber(int lineNumber) {
            this.lineNumber = lineNumber;
            return this;
        }

    }

    public abstract static class Condition {
        protected String id;
        protected boolean not;

        public Condition id(String id) {
            this.id = id;
            return this;
        }

        public final String id() {
            return id;
        }

        public final Condition not() {
            this.not = !not;
            return this;
        }

        public abstract boolean match(Attributes attrs);

        public void addChild(Condition child) {
            throw new UnsupportedOperationException();
        }

        public Condition trim() {
            return this;
        }

        public boolean isEmpty() {
            return false;
        }

    }

    abstract static class CompositeCondition extends Condition {
        protected final ArrayList<Condition> childs = new ArrayList<Condition>();

        public abstract boolean match(Attributes attrs);

        @Override
        public void addChild(Condition child) {
            childs.add(child);
        }

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

        @Override
        public boolean isEmpty() {
            return childs.isEmpty();
        }
    }

    public static class And extends CompositeCondition {

        public boolean match(Attributes attrs) {
            for (Condition child : childs) {
                if (!child.match(attrs))
                    return not;
            }
            return !not;
        }
    }

    public static class Or extends CompositeCondition {

        public boolean match(Attributes attrs) {
            for (Condition child : childs) {
                if (child.match(attrs))
                    return !not;
            }
            return not;
        }
    }

    public static class Present extends Condition {
        protected final int tag;
        protected final int[] itemPath;

        public Present(int tag, int... itemPath) {
            this.tag = tag;
            this.itemPath = itemPath;
        }

        public boolean match(Attributes attrs) {
            return not != item(attrs).containsValue(tag);
        }

        protected Attributes item(Attributes attrs) {
            for (int sqtag : itemPath) {
                if (sqtag == -1)
                    attrs = (sqtag == -1) ? attrs.getParent() : attrs.getNestedDataset(sqtag);
            }
            return attrs;
        }
    }

    public static class MemberOf extends Present {
        private final VR vr;
        private final int valueIndex;
        private final boolean matchNotPresent;
        private Object values;

        public MemberOf(int tag, VR vr, int valueIndex, boolean matchNotPresent, int... itemPath) {
            super(tag, itemPath);
            this.vr = vr;
            this.valueIndex = valueIndex;
            this.matchNotPresent = matchNotPresent;
        }

        public VR vr() {
            return vr;
        }

        public MemberOf setValues(String... values) {
            if (vr == VR.SQ)
                throw new IllegalStateException("vr=SQ");
            this.values = values;
            return this;
        }

        public MemberOf setValues(int... values) {
            if (!vr.isIntType())
                throw new IllegalStateException("vr=" + vr);
            this.values = values;
            return this;
        }

        public MemberOf setValues(Code... values) {
            if (vr != VR.SQ)
                throw new IllegalStateException("vr=" + vr);
            this.values = values;
            return this;
        }

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

        private boolean match(Attributes item, Code[] codes) {
            Sequence seq = item.getSequence(tag);
            if (seq != null)
                for (Attributes codeItem : seq) {
                    try {
                        Code val = new Code(codeItem);
                        for (Code code : codes) {
                            if (code.equals(val))
                                return !not;
                        }
                    } catch (NullPointerException npe) {
                    }
                }
            return not;
        }

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

    private static class SAXHandler extends DefaultHandler {

        private final StringBuilder sb = new StringBuilder();
        private final List<String> values = new ArrayList<>();
        private final List<Code> codes = new ArrayList<>();
        private final LinkedList<IOD> iodStack = new LinkedList<>();
        private final LinkedList<Condition> conditionStack = new LinkedList<>();
        private final Map<String, IOD> id2iod = new HashMap<>();
        private final Map<String, Condition> id2cond = new HashMap<>();
        private boolean processCharacters;
        private boolean elementConditions;
        private boolean itemConditions;
        private String idref;
        private Locator locator;

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
                startCode(atts.getValue("codeValue"), atts.getValue("codingSchemeDesignator"),
                        atts.getValue("codingSchemeVersion"), atts.getValue("codeMeaning"));
                break;
            case "DataElement":
                startDataElement(atts.getValue("tag"), atts.getValue("vr"), atts.getValue("type"), atts.getValue("vm"),
                        atts.getValue("items"), atts.getValue("valueNumber"));
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

        private Present present(org.xml.sax.Attributes atts) throws SAXException {
            int[] tagPath = tagPathOf(atts.getValue("tag"));
            int lastIndex = tagPath.length - 1;
            return new Present(tagPath[lastIndex], lastIndex > 0 ? Arrays.copyOf(tagPath, lastIndex) : new int[] {});
        }

        private MemberOf memberOf(org.xml.sax.Attributes atts) throws SAXException {
            int[] tagPath = tagPathOf(atts.getValue("tag"));
            int lastIndex = tagPath.length - 1;
            return new MemberOf(tagPath[lastIndex], vrOf(atts.getValue("vr")),
                    valueNumberOf(atts.getValue("valueNumber"), 1) - 1,
                    matchNotPresentOf(atts.getValue("matchNotPresent")),
                    lastIndex > 0 ? Arrays.copyOf(tagPath, lastIndex) : new int[] {});
        }

        private void startCode(String codeValue, String codingSchemeDesignator, String codingSchemeVersion,
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

        private void startDataElement(String tagStr, String vrStr, String typeStr, String vmStr, String items,
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

        private DataElementType typeOf(String s) throws SAXException {
            if (s == null)
                throw new SAXException("missing type attribute");
            try {
                return DataElementType.valueOf("TYPE_" + s);
            } catch (IllegalArgumentException e) {
                throw new SAXException("unrecognized type=\"" + s + Symbol.C_DOUBLE_QUOTES);
            }
        }

        private VR vrOf(String s) throws SAXException {
            try {
                return VR.valueOf(s);
            } catch (NullPointerException e) {
                throw new SAXException("missing vr attribute");
            } catch (IllegalArgumentException e) {
                throw new SAXException("unrecognized vr=\"" + s + Symbol.C_DOUBLE_QUOTES);
            }
        }

        private int tagOf(String s) throws SAXException {
            try {
                return (int) Long.parseLong(s, Normal._16);
            } catch (NullPointerException e) {
                throw new SAXException("missing tag attribute");
            } catch (IllegalArgumentException e) {
                throw new SAXException("invalid tag=\"" + s + Symbol.C_DOUBLE_QUOTES);
            }
        }

        private int[] tagPathOf(String s) throws SAXException {
            String[] ss = Builder.split(s, Symbol.C_SLASH);
            if (ss.length == 0)
                throw new SAXException("missing tag attribute");

            try {
                int[] tagPath = new int[ss.length];
                for (int i = 0; i < tagPath.length; i++)
                    tagPath[i] = ss[i].equals(Symbol.DOUBLE_DOT) ? -1 : (int) Long.parseLong(s, Normal._16);
                return tagPath;
            } catch (IllegalArgumentException e) {
                throw new SAXException("invalid tag=\"" + s + Symbol.C_DOUBLE_QUOTES);
            }
        }

        private int valueNumberOf(String s, int def) throws SAXException {
            try {
                return s != null ? Integer.parseInt(s) : def;
            } catch (IllegalArgumentException e) {
                throw new SAXException("invalid valueNumber=\"" + s + Symbol.C_DOUBLE_QUOTES);
            }
        }

        private boolean matchNotPresentOf(String s) {
            return s != null && s.equalsIgnoreCase("true");
        }

        private DataElement getLastDataElement() {
            IOD iod = iodStack.getLast();
            return iod.get(iod.size() - 1);
        }

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

        private int[] parseInts(List<String> list) {
            int[] is = new int[list.size()];
            for (int i = 0; i < is.length; i++)
                is[i] = Integer.parseInt(list.get(i));
            return is;
        }

        private void startValue() {
            sb.setLength(0);
            processCharacters = true;
        }

        private void endValue() {
            values.add(sb.toString());
        }

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

        private void endItem() {
            iodStack.removeLast().trimToSize();
            itemConditions = false;
        }

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

        private void startCondition(String name, Condition cond) throws SAXException {
            if (!(elementConditions || itemConditions))
                throw new SAXException("unexpected <" + name + '>');

            conditionStack.add(cond);
        }

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
