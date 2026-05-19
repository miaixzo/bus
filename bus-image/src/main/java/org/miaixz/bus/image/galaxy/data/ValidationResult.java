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
package org.miaixz.bus.image.galaxy.data;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.image.Builder;
import org.miaixz.bus.image.IOD;
import org.miaixz.bus.image.Tag;

/**
 * Represents the ValidationResult type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ValidationResult {

    /**
     * Constructs a new {@code ValidationResult} instance.
     */
    public ValidationResult() {
        // No initialization required.
    }

    /**
     * The missing attributes value.
     */
    private ArrayList<IOD.DataElement> missingAttributes;

    /**
     * The missing attribute values value.
     */
    private ArrayList<IOD.DataElement> missingAttributeValues;

    /**
     * The not allowed attributes value.
     */
    private ArrayList<IOD.DataElement> notAllowedAttributes;

    /**
     * The invalid attribute values value.
     */
    private ArrayList<InvalidAttributeValue> invalidAttributeValues;

    /**
     * Executes the error comment operation.
     *
     * @param sb     the sb.
     * @param prompt the prompt.
     * @param tags   the tags.
     * @return the operation result.
     */
    private static StringBuilder errorComment(StringBuilder sb, String prompt, int[] tags) {
        sb.append(prompt);
        String prefix = tags.length > 1 ? "s: " : ": ";
        for (int tag : tags) {
            sb.append(prefix).append(Tag.toString(tag));
            prefix = ", ";
        }
        return sb;
    }

    /**
     * Determines whether missing attributes.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean hasMissingAttributes() {
        return missingAttributes != null;
    }

    /**
     * Determines whether missing attribute values.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean hasMissingAttributeValues() {
        return missingAttributeValues != null;
    }

    /**
     * Determines whether invalid attribute values.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean hasInvalidAttributeValues() {
        return invalidAttributeValues != null;
    }

    /**
     * Determines whether not allowed attributes.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean hasNotAllowedAttributes() {
        return notAllowedAttributes != null;
    }

    /**
     * Determines whether valid.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isValid() {
        return !hasMissingAttributes() && !hasMissingAttributeValues() && !hasInvalidAttributeValues()
                && !hasNotAllowedAttributes();
    }

    /**
     * Adds the missing attribute.
     *
     * @param dataElement the data element.
     */
    public void addMissingAttribute(IOD.DataElement dataElement) {
        if (missingAttributes == null)
            missingAttributes = new ArrayList<>();
        missingAttributes.add(dataElement);
    }

    /**
     * Adds the missing attribute value.
     *
     * @param dataElement the data element.
     */
    public void addMissingAttributeValue(IOD.DataElement dataElement) {
        if (missingAttributeValues == null)
            missingAttributeValues = new ArrayList<>();
        missingAttributeValues.add(dataElement);
    }

    /**
     * Adds the invalid attribute value.
     *
     * @param dataElement the data element.
     * @param reason      the reason.
     */
    public void addInvalidAttributeValue(IOD.DataElement dataElement, Invalid reason) {
        addInvalidAttributeValue(dataElement, reason, null, null);
    }

    /**
     * Adds the invalid attribute value.
     *
     * @param dataElement          the data element.
     * @param reason               the reason.
     * @param itemValidationResult the item validation result.
     * @param missingItems         the missing items.
     */
    public void addInvalidAttributeValue(
            IOD.DataElement dataElement,
            Invalid reason,
            ValidationResult[] itemValidationResult,
            IOD[] missingItems) {
        if (invalidAttributeValues == null)
            invalidAttributeValues = new ArrayList<>();
        invalidAttributeValues.add(new InvalidAttributeValue(dataElement, reason, itemValidationResult, missingItems));
    }

    /**
     * Adds the not allowed attribute.
     *
     * @param el the el.
     */
    public void addNotAllowedAttribute(IOD.DataElement el) {
        if (notAllowedAttributes == null)
            notAllowedAttributes = new ArrayList<>();
        notAllowedAttributes.add(el);
    }

    /**
     * Executes the tags of not allowed attributes operation.
     *
     * @return the operation result.
     */
    public int[] tagsOfNotAllowedAttributes() {
        return tagsOf(notAllowedAttributes);
    }

    /**
     * Executes the tags of missing attribute values operation.
     *
     * @return the operation result.
     */
    public int[] tagsOfMissingAttributeValues() {
        return tagsOf(missingAttributeValues);
    }

    /**
     * Executes the tags of missing attributes operation.
     *
     * @return the operation result.
     */
    public int[] tagsOfMissingAttributes() {
        return tagsOf(missingAttributes);
    }

    /**
     * Executes the tags of invalid attribute values operation.
     *
     * @return the operation result.
     */
    public int[] tagsOfInvalidAttributeValues() {
        List<InvalidAttributeValue> list = invalidAttributeValues;
        if (list == null)
            return new int[] {};

        int[] tags = new int[list.size()];
        for (int i = 0; i < tags.length; i++)
            tags[i] = list.get(i).dataElement.tag;
        return tags;
    }

    /**
     * Gets the offending elements.
     *
     * @return the offending elements.
     */
    public int[] getOffendingElements() {
        return cat(
                tagsOfMissingAttributes(),
                tagsOfMissingAttributeValues(),
                tagsOfInvalidAttributeValues(),
                tagsOfNotAllowedAttributes());
    }

    /**
     * Executes the cat operation.
     *
     * @param iss the iss.
     * @return the operation result.
     */
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

    /**
     * Executes the tags of operation.
     *
     * @param list the list.
     * @return the operation result.
     */
    private int[] tagsOf(List<IOD.DataElement> list) {
        if (list == null)
            return new int[] {};

        int[] tags = new int[list.size()];
        for (int i = 0; i < tags.length; i++)
            tags[i] = list.get(i).tag;
        return tags;
    }

    /**
     * Gets the error comment.
     *
     * @return the error comment.
     */
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

    /**
     * Returns the string representation.
     *
     * @return the string representation.
     */
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

    /**
     * Executes the as text operation.
     *
     * @param attrs the attrs.
     * @return the operation result.
     */
    public String asText(Attributes attrs) {
        if (isValid())
            return "VALID";

        StringBuilder sb = new StringBuilder();
        appendTextTo(0, attrs, sb);
        return sb.substring(0, sb.length() - 1);
    }

    /**
     * Executes the append text to operation.
     *
     * @param level the level.
     * @param attrs the attrs.
     * @param sb    the sb.
     */
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

    /**
     * Executes the append text to operation.
     *
     * @param level the level.
     * @param attrs the attrs.
     * @param title the title.
     * @param list  the list.
     * @param sb    the sb.
     */
    private void appendTextTo(int level, Attributes attrs, String title, List<IOD.DataElement> list, StringBuilder sb) {
        appendPrefixTo(level, sb);
        sb.append(title).append(Builder.LINE_SEPARATOR);
        for (IOD.DataElement el : list) {
            appendAttribute(level, el.tag, sb);
            appendIODRef(el.getLineNumber(), sb);
            sb.append(Builder.LINE_SEPARATOR);
        }
    }

    /**
     * Executes the append iod ref operation.
     *
     * @param lineNumber the line number.
     * @param sb         the sb.
     */
    private void appendIODRef(int lineNumber, StringBuilder sb) {
        if (lineNumber > 0)
            sb.append(" // IOD line #").append(lineNumber);
    }

    /**
     * Executes the append invalid attribute values operation.
     *
     * @param level the level.
     * @param attrs the attrs.
     * @param title the title.
     * @param sb    the sb.
     */
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

    /**
     * Executes the append attribute operation.
     *
     * @param level the level.
     * @param tag   the tag.
     * @param sb    the sb.
     */
    private void appendAttribute(int level, int tag, StringBuilder sb) {
        appendPrefixTo(level, sb);
        sb.append(Tag.toString(tag)).append(Symbol.C_SPACE).append(ElementDictionary.keywordOf(tag, null));
    }

    /**
     * Executes the append prefix to operation.
     *
     * @param level the level.
     * @param sb    the sb.
     */
    private void appendPrefixTo(int level, StringBuilder sb) {
        while (level-- > 0)
            sb.append(Symbol.C_GT);
    }

    /**
     * Defines the Invalid values.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum Invalid {
        /**
         * Constant for the vr value.
         */
        VR,
        /**
         * Constant for the vm value.
         */
        VM,
        /**
         * Constant for the value value.
         */
        Value,
        /**
         * Constant for the item value.
         */
        Item,
        /**
         * Constant for the multiple items value.
         */
        MultipleItems,
        /**
         * Constant for the code value.
         */
        Code

    }

    /**
     * Represents the InvalidAttributeValue type.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public class InvalidAttributeValue {

        /**
         * The data element value.
         */
        public final IOD.DataElement dataElement;

        /**
         * The reason value.
         */
        public final Invalid reason;

        /**
         * The item validation results value.
         */
        public final ValidationResult[] itemValidationResults;

        /**
         * The missing items value.
         */
        public final IOD[] missingItems;

        /**
         * Creates a new instance.
         *
         * @param dataElement           the data element.
         * @param reason                the reason.
         * @param itemValidationResults the item validation results.
         * @param missingItems          the missing items.
         */
        public InvalidAttributeValue(IOD.DataElement dataElement, Invalid reason,
                ValidationResult[] itemValidationResults, IOD[] missingItems) {
            this.dataElement = dataElement;
            this.reason = reason;
            this.itemValidationResults = itemValidationResults;
            this.missingItems = missingItems;
        }

    }

}
