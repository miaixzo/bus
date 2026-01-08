/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.setting.metric.ini.IniComment;
import org.miaixz.bus.setting.metric.ini.IniSection;
import org.miaixz.bus.setting.metric.ini.IniSectionService;

/**
 * A formatter that parses a string value into an {@link IniSection} object.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SectionFormatter extends AbstractFormatter<IniSection> {

    /**
     * The character that marks the beginning of a section header (e.g., '[').
     */
    private final char head;
    /**
     * The character that marks the end of a section header (e.g., ']').
     */
    private final char end;

    /**
     * Constructs a SectionFormatter with a specific comment formatter and default section delimiters ('[' and ']').
     *
     * @param commentElementFormatter The formatter for parsing comments.
     */
    public SectionFormatter(CommentFormatter commentElementFormatter) {
        super(commentElementFormatter);
        head = Symbol.C_BRACKET_LEFT;
        end = Symbol.C_BRACKET_RIGHT;
    }

    /**
     * Constructs a SectionFormatter with default settings.
     */
    public SectionFormatter() {
        head = Symbol.C_BRACKET_LEFT;
        end = Symbol.C_BRACKET_RIGHT;
    }

    /**
     * Constructs a SectionFormatter with custom delimiters and a specific comment formatter.
     *
     * @param head                    The starting delimiter character.
     * @param end                     The ending delimiter character.
     * @param commentElementFormatter The formatter for parsing comments.
     */
    public SectionFormatter(char head, char end, CommentFormatter commentElementFormatter) {
        super(commentElementFormatter);
        this.head = head;
        this.end = end;
    }

    /**
     * Constructs a SectionFormatter with custom delimiters and a default comment formatter.
     *
     * @param head The starting delimiter character.
     * @param end  The ending delimiter character.
     */
    public SectionFormatter(char head, char end) {
        this.head = head;
        this.end = end;
    }

    /**
     * Checks if the given string value is a section header.
     *
     * @param value The string to check.
     * @return {@code true} if the string starts with the configured 'head' character.
     */
    @Override
    public boolean check(String value) {
        return !value.isEmpty() && value.charAt(0) == head;
    }

    /**
     * Formats the string value into an {@link IniSection}. This method assumes that {@link #check(String)} has already
     * returned true.
     *
     * @param value A string value representing a section header (e.g., "[section_name]").
     * @param line  The line number where the value originated.
     * @return The parsed {@link IniSection}, which cannot be null.
     * @throws InternalException if the section format is invalid.
     */
    @Override
    public IniSection format(String value, int line) {
        int indexOfEnd = value.indexOf(end);
        if (indexOfEnd <= 0) {
            throw new InternalException(
                    "Cannot find the end character '" + end + "' for section on line " + line + ": " + value);
        }

        String sectionValue = value.substring(0, indexOfEnd + 1).trim();
        String endOfValue = value.substring(indexOfEnd + 1).trim();
        IniComment comment = null;
        if (!endOfValue.isEmpty()) {
            CommentFormatter commentElementFormatter = getCommentElementFormatter();
            if (commentElementFormatter.check(endOfValue)) {
                comment = commentElementFormatter.format(endOfValue, line);
            } else {
                throw new InternalException("Cannot format the trailing content after section on line " + line
                        + " at column " + (indexOfEnd + 1) + ": " + endOfValue);
            }
        }

        return new IniSectionService(sectionValue.substring(1, indexOfEnd), sectionValue, line, comment);
    }

}
