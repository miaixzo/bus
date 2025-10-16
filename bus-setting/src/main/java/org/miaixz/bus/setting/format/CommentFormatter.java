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
package org.miaixz.bus.setting.format;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.setting.metric.ini.IniComment;
import org.miaixz.bus.setting.metric.ini.IniCommentService;

/**
 * A formatter that parses a string value into an {@link IniComment} object.
 *
 * @author Kimi Liu
 * @since Java 17+
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
