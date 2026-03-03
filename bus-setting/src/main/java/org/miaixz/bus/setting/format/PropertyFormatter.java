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
package org.miaixz.bus.setting.format;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.setting.metric.ini.IniProperty;
import org.miaixz.bus.setting.metric.ini.IniPropertyService;

/**
 * A formatter that parses a string value into an {@link IniProperty} object (a key-value pair).
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PropertyFormatter extends AbstractFormatter<IniProperty> {

    /**
     * The character used to separate the key from the value (e.g., '=').
     */
    private final char split;

    /**
     * Constructs a PropertyFormatter with a specific comment formatter and default split character ('=').
     *
     * @param commentElementFormatter The formatter for parsing comments.
     */
    public PropertyFormatter(CommentFormatter commentElementFormatter) {
        super(commentElementFormatter);
        this.split = Symbol.C_EQUAL;
    }

    /**
     * Constructs a PropertyFormatter with a default comment formatter and split character ('=').
     */
    public PropertyFormatter() {
        this.split = Symbol.C_EQUAL;
    }

    /**
     * Constructs a PropertyFormatter with a specific split character and comment formatter.
     *
     * @param split                   The character separating key and value.
     * @param commentElementFormatter The formatter for parsing comments.
     */
    public PropertyFormatter(char split, CommentFormatter commentElementFormatter) {
        super(commentElementFormatter);
        this.split = split;
    }

    /**
     * Constructs a PropertyFormatter with a specific split character and a default comment formatter.
     *
     * @param split The character separating key and value.
     */
    public PropertyFormatter(char split) {
        this.split = split;
    }

    /**
     * Checks if the given string value represents a property.
     *
     * @param value The string to check.
     * @return {@code true} if the string contains the split character.
     */
    @Override
    public boolean check(String value) {
        return value.indexOf(split) > 0;
    }

    /**
     * Formats the string value into an {@link IniProperty}. This method assumes that {@link #check(String)} has already
     * returned true.
     *
     * @param value A string value in "key=value" format.
     * @param line  The line number where the value originated.
     * @return The parsed {@link IniProperty}, which cannot be null.
     */
    @Override
    public IniProperty format(String value, int line) {
        String[] split = value.split(String.valueOf(this.split), 2);
        if (split.length == 1) {
            split = new String[] { split[0], null }; // Handle keys with no value after the '='
        }
        final String propKey = split[0].trim();
        final String propValue = split[1] != null ? split[1].trim() : null;

        return new IniPropertyService(propKey, propValue, value, line);
    }

}
