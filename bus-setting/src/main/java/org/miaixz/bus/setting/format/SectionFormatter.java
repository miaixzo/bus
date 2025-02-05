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
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.setting.metric.ini.IniComment;
import org.miaixz.bus.setting.metric.ini.IniSection;
import org.miaixz.bus.setting.metric.ini.IniSectionService;

/**
 * 将字符串值格式设置为{@link IniSection}
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SectionFormatter extends AbstractFormatter<IniSection> {

    /**
     * 节点开始字符, {@link Symbol#C_BRACKET_LEFT}
     */
    private char head;
    /**
     * 节点结束字符, {@link Symbol#C_BRACKET_RIGHT}
     */
    private char end;

    public SectionFormatter(CommentFormatter commentElementFormatter) {
        super(commentElementFormatter);
        head = Symbol.C_BRACKET_LEFT;
        end = Symbol.C_BRACKET_RIGHT;
    }

    public SectionFormatter() {
        head = Symbol.C_BRACKET_LEFT;
        end = Symbol.C_BRACKET_RIGHT;
    }

    public SectionFormatter(char head, char end, CommentFormatter commentElementFormatter) {
        super(commentElementFormatter);
        this.head = head;
        this.end = end;
    }

    public SectionFormatter(char head, char end) {
        this.head = head;
        this.end = end;
    }

    /**
     * check this value. if this value's first char == {@code HEAD} value, pass.
     *
     * @param value value
     * @return true if can.
     */
    @Override
    public boolean check(String value) {
        return value.charAt(0) == head;
    }

    /**
     * this method will not check value, so you should {@link #check(String)} first. However, not checking will not
     * necessarily report an error, but may result in non-compliance.
     *
     * @param value a String value
     * @param line  line number
     * @return {@link IniSection}, can not be null.
     */
    @Override
    public IniSection format(String value, int line) {
        int indexOfEnd = value.indexOf(end);
        if (indexOfEnd <= 0) {
            throw new InternalException(
                    "can not found the end character '" + end + "' for section line " + line + " : " + value);
        }

        String sectionValue = value.substring(0, indexOfEnd + 1).trim();
        String endOfValue = value.substring(indexOfEnd + 1).trim();
        IniComment comment = null;
        if (endOfValue.length() > 0) {
            CommentFormatter commentElementFormatter = getCommentElementFormatter();
            if (commentElementFormatter.check(endOfValue)) {
                comment = commentElementFormatter.format(endOfValue, line);
            } else {
                throw new InternalException("can not format the value end of section value (" + line + Symbol.COLON
                        + (indexOfEnd + 1) + ") :" + endOfValue);
            }
        }

        return new IniSectionService(sectionValue.substring(1, indexOfEnd), sectionValue, line, comment);
    }

}
