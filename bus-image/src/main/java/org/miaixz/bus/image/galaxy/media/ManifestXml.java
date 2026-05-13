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
package org.miaixz.bus.image.galaxy.media;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.data.ElementDictionary;

/**
 * XML helpers for DICOM manifest structures.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface ManifestXml {

    /**
     * DICOM manifest hierarchy levels.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    enum Level {

        /**
         * Constant for the patient value.
         */
        PATIENT("Patient"),
        /**
         * Constant for the study value.
         */
        STUDY("Study"),
        /**
         * Constant for the series value.
         */
        SERIES("Series"),
        /**
         * Constant for the instance value.
         */
        INSTANCE("Instance"),
        /**
         * Constant for the frame value.
         */
        FRAME("Frame");

        /**
         * The tag name value.
         */
        private final String tagName;

        /**
         * Creates a new instance.
         *
         * @param tagName the tag name.
         */
        Level(String tagName) {
            this.tagName = tagName;
        }

        /**
         * Gets the tag name.
         *
         * @return the tag name.
         */
        public String getTagName() {
            return tagName;
        }

        /**
         * Returns the string representation.
         *
         * @return the string representation.
         */
        @Override
        public String toString() {
            return tagName;
        }

    }

    /**
     * Write this object as XML.
     *
     * @param writer output writer
     * @throws IOException when writing fails
     */
    void toXml(Writer writer) throws IOException;

    /**
     * @return XML charset name
     */
    default String getCharsetEncoding() {
        return StandardCharsets.UTF_8.name();
    }

    /**
     * Write a DICOM tag keyed XML attribute.
     *
     * @param tag    DICOM tag
     * @param value  attribute value
     * @param writer output writer
     * @throws IOException when writing fails
     */
    static void addXmlAttribute(int tag, String value, Writer writer) throws IOException {
        addXmlAttribute(keywordOf(tag), value, writer);
    }

    /**
     * Write a named XML attribute.
     *
     * @param name   attribute name
     * @param value  attribute value
     * @param writer output writer
     * @throws IOException when writing fails
     */
    static void addXmlAttribute(String name, String value, Writer writer) throws IOException {
        if (hasText(name) && hasText(value)) {
            writer.append(name).append("=\"").append(escape(value)).append("\" ");
        }
    }

    /**
     * Write a named boolean XML attribute.
     *
     * @param name   attribute name
     * @param value  attribute value
     * @param writer output writer
     * @throws IOException when writing fails
     */
    static void addXmlAttribute(String name, Boolean value, Writer writer) throws IOException {
        if (value != null) {
            addXmlAttribute(name, value.toString(), writer);
        }
    }

    /**
     * Write a named int-array XML attribute.
     *
     * @param name   attribute name
     * @param values values
     * @param writer output writer
     * @throws IOException when writing fails
     */
    static void addXmlAttribute(String name, int[] values, Writer writer) throws IOException {
        if (hasText(name) && values != null && values.length > 0) {
            String joined = IntStream.of(values).mapToObj(String::valueOf).collect(Collectors.joining(","));
            addXmlAttribute(name, joined, writer);
        }
    }

    /**
     * Executes the keyword of operation.
     *
     * @param tag the tag.
     * @return the operation result.
     */
    private static String keywordOf(int tag) {
        String keyword = ElementDictionary.getStandardElementDictionary().keywordOf(tag);
        return hasText(keyword) ? keyword : Tag.toString(tag);
    }

    /**
     * Determines whether text.
     *
     * @param value the value.
     * @return true if the condition is met; otherwise false.
     */
    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Executes the escape operation.
     *
     * @param value the value.
     * @return the operation result.
     */
    private static String escape(String value) {
        StringBuilder builder = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);
            switch (current) {
                case '&' -> builder.append("&amp;");
                case '<' -> builder.append("&lt;");
                case '>' -> builder.append("&gt;");
                case '"' -> builder.append("&quot;");
                case '\'' -> builder.append("&apos;");
                default -> builder.append(current);
            }
        }
        return builder.toString();
    }

}
