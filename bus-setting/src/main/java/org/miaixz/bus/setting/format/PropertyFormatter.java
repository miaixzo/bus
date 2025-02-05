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
import org.miaixz.bus.setting.metric.ini.IniProperty;
import org.miaixz.bus.setting.metric.ini.IniPropertyService;

/**
 * 将字符串值格式设置为{@link IniProperty}
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PropertyFormatter extends AbstractFormatter<IniProperty> {

    /**
     * 键值分割字符串{@link Symbol＃C_EQUAL}
     */
    private char split;

    public PropertyFormatter(CommentFormatter commentElementFormatter) {
        super(commentElementFormatter);
        this.split = Symbol.C_EQUAL;
    }

    public PropertyFormatter() {
        this.split = Symbol.C_EQUAL;
    }

    public PropertyFormatter(char split, CommentFormatter commentElementFormatter) {
        super(commentElementFormatter);
        this.split = split;
    }

    public PropertyFormatter(char split) {
        this.split = split;
    }

    @Override
    public boolean check(String value) {
        return value.indexOf(split) > 0;
    }

    /**
     * 此方法不会检查值，因此您应该首先{@link #check(String)} 但是，不检查并不一定会报告错误，但可能会导致违规
     *
     * @param value a String value
     * @param line  line number
     * @return {@link IniProperty}, can not be null.
     */
    @Override
    public IniProperty format(String value, int line) {
        String[] split = value.split(String.valueOf(Symbol.C_EQUAL), 2);
        if (split.length == 1) {
            split = new String[] { split[0], null };
        }
        final String propKey = split[0];
        final String propValue = split[1];

        return new IniPropertyService(propKey, propValue, value, line);
    }

}
