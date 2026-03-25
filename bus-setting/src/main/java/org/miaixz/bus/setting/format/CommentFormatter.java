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
import org.miaixz.bus.setting.metric.ini.IniComment;
import org.miaixz.bus.setting.metric.ini.IniCommentService;

/**
 * A formatter that parses a string value into an {@link IniComment} object.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class CommentFormatter extends AbstractFormatter<IniComment> {

    /**
     * The character that indicates the start of a comment.
     */
    private final char start;

    /**
     * Constructs a CommentFormatter with a specific starting character.
     *
     * @param startChar The character that identifies a comment line.
     */
    public CommentFormatter(char startChar) {
        super(null); // A comment formatter does not need another comment formatter.
        start = startChar;
    }

    /**
     * Constructs a CommentFormatter with the default starting character ('#').
     */
    public CommentFormatter() {
        super(null);
        start = Symbol.C_HASH;
    }

    /**
     * Formats a comment string into an {@link IniComment} object.
     *
     * @param value The raw string line, including the starting comment character.
     * @param line  The line number in the original file.
     * @return The parsed {@link IniComment} object.
     */
    @Override
    public IniComment format(String value, int line) {
        return new IniCommentService(value.substring(1), value, line);
    }

    /**
     * Checks if the given string value is a comment.
     *
     * @param value The string to check.
     * @return {@code true} if the string starts with the configured comment character.
     */
    @Override
    public boolean check(String value) {
        return !value.isEmpty() && value.charAt(0) == start;
    }

}
