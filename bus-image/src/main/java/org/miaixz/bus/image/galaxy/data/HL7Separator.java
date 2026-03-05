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

/**
 * Enumeration representing the standard separators and their escape sequences used in HL7 messages. This class provides
 * utility methods for escaping and unescaping strings according to HL7 rules.
 * 
 * @author Kimi Liu
 * @since Java 17+
 */
public enum HL7Separator {

    /**
     * Field separator: |
     */
    FIELD("|", "\\F\\"),
    /**
     * Component separator: ^
     */
    COMPONENT("^", "\\S\\"),
    /**
     * Subcomponent separator: &amp;
     */
    SUBCOMPONENT("&", "\\T\\"),
    /**
     * Repetition separator: ~
     */
    REPETITION("~", "\\R\\"),
    /**
     * Escape character: \
     */
    ESCAPE("\\", "\\E\\");

    /**
     * The actual separator character as a string.
     */
    public final String separator;
    /**
     * The HL7 escape sequence for the separator.
     */
    public final String escapeSequence;

    /**
     * Constructs an {@code HL7Separator} enum constant.
     * 
     * @param separator      The character string used as a separator.
     * @param escapeSequence The HL7 escape sequence for this separator.
     */
    HL7Separator(String separator, String escapeSequence) {
        this.separator = separator;
        this.escapeSequence = escapeSequence;
    }

    /**
     * Escapes all standard HL7 separators in the given string. The order of escaping is: ESCAPE, REPETITION,
     * SUBCOMPONENT, COMPONENT, FIELD.
     * 
     * @param s The string to escape.
     * @return The escaped string.
     */
    public static String escapeAll(String s) {
        return FIELD.escape(COMPONENT.escape(SUBCOMPONENT.escape(REPETITION.escape(ESCAPE.escape(s)))));
    }

    /**
     * Unescapes all standard HL7 escape sequences in the given string. The order of unescaping is the reverse of
     * escaping: FIELD, COMPONENT, SUBCOMPONENT, REPETITION, ESCAPE.
     * 
     * @param s The string to unescape.
     * @return The unescaped string.
     */
    public static String unescapeAll(String s) {
        return ESCAPE.unescape(REPETITION.unescape(SUBCOMPONENT.unescape(COMPONENT.unescape(FIELD.unescape(s)))));
    }

    /**
     * Escapes occurrences of this separator in the given string with its corresponding escape sequence.
     * 
     * @param s The string to process.
     * @return The string with this separator escaped.
     */
    public String escape(String s) {
        return s.replace(separator, escapeSequence);
    }

    /**
     * Unescapes occurrences of this separator's escape sequence in the given string with its corresponding separator.
     * 
     * @param s The string to process.
     * @return The string with this separator unescaped.
     */
    public String unescape(String s) {
        return s.replace(escapeSequence, separator);
    }

}
