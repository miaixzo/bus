/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.office.excel.cell.setters;

import java.util.regex.Pattern;

import org.miaixz.bus.core.xyz.PatternKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Cell value setter for escaping strings. It uses the {@code _x005F} prefix to escape {@code _xXXXX_} patterns,
 * preventing decoding issues. For example, if a user inputs '_x5116_', it could be garbled; this setter escapes it to
 * '_x005F_x5116_'.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class EscapeStrCellSetter extends CharSequenceCellSetter {

    /**
     * Pattern for matching {@code _xXXXX_} sequences.
     */
    private static final Pattern utfPtrn = Pattern.compile("_x[0-9A-Fa-f]{4}_");

    /**
     * Constructor.
     *
     * @param value The value.
     */
    public EscapeStrCellSetter(final CharSequence value) {
        super(escape(StringKit.toStringOrNull(value)));
    }

    /**
     * Escapes {@code _xXXXX_} patterns with the {@code _x005F} prefix to prevent decoding issues.
     *
     * @param value The string to be escaped.
     * @return The escaped string.
     */
    private static String escape(final String value) {
        if (value == null || !value.contains("_x")) {
            return value;
        }

        // Escape `_xXXXX_` with the `_x005F` prefix to avoid decoding issues.
        return PatternKit.replaceAll(value, utfPtrn, "_x005F$0");
    }

}
