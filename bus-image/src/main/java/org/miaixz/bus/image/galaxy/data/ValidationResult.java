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
package org.miaixz.bus.image.galaxy.data;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.image.Builder;
import org.miaixz.bus.image.IOD;
import org.miaixz.bus.image.Tag;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class ValidationResult {

    private ArrayList<IOD.DataElement> missingAttributes;
    private ArrayList<IOD.DataElement> missingAttributeValues;
    private ArrayList<IOD.DataElement> notAllowedAttributes;
    private ArrayList<InvalidAttributeValue> invalidAttributeValues;

    private static StringBuilder errorComment(StringBuilder sb, String prompt, int[] tags) {
        sb.append(prompt);
        String prefix = tags.length > 1 ? "s: " : ": ";
        for (int tag : tags) {
            sb.append(prefix).append(Tag.toString(tag));
            prefix = ", ";
        }
        return sb;
    }

    public boolean hasMissingAttributes() {
        return missingAttributes != null;
    }

    public boolean hasMissingAttributeValues() {
        return missingAttributeValues != null;
    }

    public boolean hasInvalidAttributeValues() {
        return invalidAttributeValues != null;
    }

    public boolean hasNotAllowedAttributes() {
        return notAllowedAttributes != null;
    }

    public boolean isValid() {
        return !hasMissingAttributes() && !hasMissingAttributeValues() && !hasInvalidAttributeValues()
                && !hasNotAllowedAttributes();
    }

    public void addMissingAttribute(IOD.DataElement dataElement) {
        if (missingAttributes == null)
            missingAttributes = new ArrayList<>();
        missingAttributes.add(dataElement);
    }

    public void addMissingAttributeValue(IOD.DataElement dataElement) {
        if (missingAttributeValues == null)
            missingAttributeValues = new ArrayList<>();
        missingAttributeValues.add(dataElement);
    }

    public void addInvalidAttributeValue(IOD.DataElement dataElement, Invalid reason) {
        addInvalidAttributeValue(dataElement, reason, null, null);
    }

    public void addInvalidAttributeValue(IOD.DataElement dataElement, Invalid reason,
            ValidationResult[] itemValidationResult, IOD[] missingItems) {
        if (invalidAttributeValues == null)
            invalidAttributeValues = new ArrayList<>();
        invalidAttributeValues.add(new InvalidAttributeValue(dataElement, reason, itemValidationResult, missingItems));
    }

    public void addNotAllowedAttribute(IOD.DataElement el) {
        if (notAllowedAttributes == null)
            notAllowedAttributes = new ArrayList<>();
        notAllowedAttributes.add(el);
    }

    public int[] tagsOfNotAllowedAttributes() {
        return tagsOf(notAllowedAttributes);
    }

    public int[] tagsOfMissingAttributeValues() {
        return tagsOf(missingAttributeValues);
    }

    public int[] tagsOfMissingAttributes() {
        return tagsOf(missingAttributes);
    }

    public int[] tagsOfInvalidAttributeValues() {
        List<InvalidAttributeValue> list = invalidAttributeValues;
        if (list == null)
            return new int[] {};

        int[] tags = new int[list.size()];
        for (int i = 0; i < tags.length; i++)
            tags[i] = list.get(i).dataElement.tag;
        return tags;
    }

    public int[] getOffendingElements() {
        return cat(tagsOfMissingAttributes(), tagsOfMissingAttributeValues(), tagsOfInvalidAttributeValues(),
                tagsOfNotAllowedAttributes());
    }

    private int[] cat(int[]... iss) {
        int length = 0;
        for (int[] is : iss)
            length += is.length;
        int[] tags = new int[length];
        int off = 0;
        for (int[] is : iss) {
            System.arraycopy(is, 0, tags, off, is.length);
            off += is.length;
        }
        return tags;
    }

    private int[] tagsOf(List<IOD.DataElement> list) {
        if (list == null)
            return new int[] {};

        int[] tags = new int[list.size()];
        for (int i = 0; i < tags.length; i++)
            tags[i] = list.get(i).tag;
        return tags;
    }

    public String getErrorComment() {
        StringBuilder sb = new StringBuilder();
        if (notAllowedAttributes != null)
            return errorComment(sb, "Not allowed Attribute", tagsOfNotAllowedAttributes()).toString();
        if (missingAttributes != null)
            return errorComment(sb, "Missing Attribute", tagsOfMissingAttributes()).toString();
        if (missingAttributeValues != null)
            return errorComment(sb, "Missing Value of Attribute", tagsOfMissingAttributeValues()).toString();
        if (invalidAttributeValues != null)
            return errorComment(sb, "Invalid Attribute", tagsOfInvalidAttributeValues()).toString();
        return null;
    }

    @Override
    public String toString() {
        if (isValid())
            return "VALID";

        StringBuilder sb = new StringBuilder();
        if (notAllowedAttributes != null)
            errorComment(sb, "Not allowed Attribute", tagsOfNotAllowedAttributes()).append(Builder.LINE_SEPARATOR);
        if (missingAttributes != null)
            errorComment(sb, "Missing Attribute", tagsOfMissingAttributes()).append(Builder.LINE_SEPARATOR);
        if (missingAttributeValues != null)
            errorComment(sb, "Missing Value of Attribute", tagsOfMissingAttributeValues())
                    .append(Builder.LINE_SEPARATOR);
        if (invalidAttributeValues != null)
            errorComment(sb, "Invalid Attribute", tagsOfInvalidAttributeValues()).append(Builder.LINE_SEPARATOR);

        return sb.substring(0, sb.length() - 1);
    }

    public String asText(Attributes attrs) {
        if (isValid())
            return "VALID";

        StringBuilder sb = new StringBuilder();
        appendTextTo(0, attrs, sb);
        return sb.substring(0, sb.length() - 1);
    }

    private void appendTextTo(int level, Attributes attrs, StringBuilder sb) {
        if (notAllowedAttributes != null)
            appendTextTo(level, attrs, "Not allowed Attributes:", notAllowedAttributes, sb);
        if (missingAttributes != null)
            appendTextTo(level, attrs, "Missing Attributes:", missingAttributes, sb);
        if (missingAttributeValues != null)
            appendTextTo(level, attrs, "Missing Attribute Values:", missingAttributeValues, sb);
        if (invalidAttributeValues != null)
            appendInvalidAttributeValues(level, attrs, "Invalid Attribute Values:", sb);
    }

    private void appendTextTo(int level, Attributes attrs, String title, List<IOD.DataElement> list, StringBuilder sb) {
        appendPrefixTo(level, sb);
        sb.append(title).append(Builder.LINE_SEPARATOR);
        for (IOD.DataElement el : list) {
            appendAttribute(level, el.tag, sb);
            appendIODRef(el.getLineNumber(), sb);
            sb.append(Builder.LINE_SEPARATOR);
        }
    }

    private void appendIODRef(int lineNumber, StringBuilder sb) {
        if (lineNumber > 0)
            sb.append(" // IOD line #").append(lineNumber);
    }

    private void appendInvalidAttributeValues(int level, Attributes attrs, String title, StringBuilder sb) {
        appendPrefixTo(level, sb);
        sb.append(title);
        sb.append(Builder.LINE_SEPARATOR);
        for (InvalidAttributeValue iav : invalidAttributeValues) {
            int tag = iav.dataElement.tag;
            appendAttribute(level, tag, sb);
            VR.Holder vr = new VR.Holder();
            Object value = attrs.getValue(tag, vr);
            sb.append(Symbol.C_SPACE).append(vr.vr);
            sb.append(" [");
            vr.vr.prompt(value, attrs.bigEndian(), attrs.getSpecificCharacterSet(vr.vr), 200, sb);
            sb.append(Symbol.C_BRACKET_RIGHT);
            if (iav.reason != Invalid.Item) {
                sb.append(" Invalid ").append(iav.reason);
                appendIODRef(iav.dataElement.getLineNumber(), sb);
            }
            sb.append(Builder.LINE_SEPARATOR);
            if (iav.missingItems != null) {
                for (IOD iod : iav.missingItems) {
                    appendPrefixTo(level + 1, sb);
                    sb.append("Missing Item");
                    appendIODRef(iod.getLineNumber(), sb);
                    sb.append(Builder.LINE_SEPARATOR);
                }
            }
            if (iav.itemValidationResults != null) {
                Sequence seq = (Sequence) value;
                for (int i = 0; i < iav.itemValidationResults.length; i++) {
                    ValidationResult itemResult = iav.itemValidationResults[i];
                    if (!itemResult.isValid()) {
                        appendPrefixTo(level + 1, sb);
                        sb.append("Invalid Item ").append(i + 1).append(Symbol.C_COLON).append(Builder.LINE_SEPARATOR);
                        itemResult.appendTextTo(level + 1, seq.get(i), sb);
                    }
                }
            }
        }
    }

    private void appendAttribute(int level, int tag, StringBuilder sb) {
        appendPrefixTo(level, sb);
        sb.append(Tag.toString(tag)).append(Symbol.C_SPACE).append(ElementDictionary.keywordOf(tag, null));
    }

    private void appendPrefixTo(int level, StringBuilder sb) {
        while (level-- > 0)
            sb.append(Symbol.C_GT);
    }

    public enum Invalid {
        VR, VM, Value, Item, MultipleItems, Code
    }

    public class InvalidAttributeValue {
        public final IOD.DataElement dataElement;
        public final Invalid reason;
        public final ValidationResult[] itemValidationResults;
        public final IOD[] missingItems;

        public InvalidAttributeValue(IOD.DataElement dataElement, Invalid reason,
                ValidationResult[] itemValidationResults, IOD[] missingItems) {
            this.dataElement = dataElement;
            this.reason = reason;
            this.itemValidationResults = itemValidationResults;
            this.missingItems = missingItems;
        }
    }

}
