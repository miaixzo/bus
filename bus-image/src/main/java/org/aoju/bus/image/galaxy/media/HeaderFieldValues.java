/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.image.galaxy.media;

import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.toolkit.StringKit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class HeaderFieldValues {

    private int start = 0;
    private int end = 0;
    private int position = 0;
    private char[] chars = null;
    private List<Map<String, String>> values;

    public HeaderFieldValues(String respContentType) {
        values = parse(respContentType);
    }

    protected boolean hasCharacter() {
        return position < chars.length;
    }

    protected String parseValue() {
        start = position;
        end = position;

        char c;
        while (hasCharacter()) {
            c = chars[position];
            if (c == Symbol.C_EQUAL || c == Symbol.C_SEMICOLON) {
                break;
            }
            end++;
            position++;
        }
        return getValue(false);
    }

    protected String parseQuotedValue() {
        start = position;
        end = position;

        boolean quoted = false;
        boolean charEscaped = false;
        char c;
        while (hasCharacter()) {
            c = chars[position];
            if (!quoted && c == Symbol.C_SEMICOLON) {
                break;
            }
            if (!charEscaped && c == Symbol.C_DOUBLE_QUOTES) {
                quoted = !quoted;
            }
            charEscaped = (!charEscaped && c == Symbol.C_BACKSLASH);
            end++;
            position++;
        }
        return getValue(true);
    }

    private String getValue(boolean quoted) {
        // 删除前空格
        while ((start < end) && (Character.isWhitespace(chars[start]))) {
            start++;
        }
        // 删除尾空格
        while ((end > start) && (Character.isWhitespace(chars[end - 1]))) {
            end--;
        }
        // 删除引号(如果存在)
        if (quoted && ((end - start) >= 2)
                && (chars[start] == Symbol.C_DOUBLE_QUOTES)
                && (chars[end - 1] == Symbol.C_DOUBLE_QUOTES)) {
            start++;
            end--;
        }
        String result = null;
        if (end > start) {
            result = new String(chars, start, end - start);
        }
        return result;
    }

    protected List<Map<String, String>> parse(String content) {
        List<Map<String, String>> hvals = new ArrayList<>();
        if (StringKit.hasText(content)) {
            // 除双引号外的拆分
            String[] elements = content.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            for (String element : elements) {
                Map<String, String> params = new HashMap<>();
                hvals.add(params);

                this.chars = element.toCharArray();
                this.position = 0;

                while (hasCharacter()) {
                    String name = parseValue();
                    String value = null;
                    if (hasCharacter() && (chars[position] == Symbol.C_EQUAL)) {
                        position++;
                        value = parseQuotedValue();
                    }
                    if (hasCharacter() && (chars[position] == Symbol.C_SEMICOLON)) {
                        position++;
                    }

                    if (StringKit.hasText(name)) {
                        params.put(name.toLowerCase(), value);
                    }
                }
            }
        }
        return hvals;
    }

    public List<Map<String, String>> getValues() {
        return values;
    }

    public void setValues(List<Map<String, String>> values) {
        this.values = values;
    }

    public boolean hasKey(String key) {
        for (Map<String, String> map : values) {
            if (map.containsKey(key)) {
                return true;
            }
        }
        return false;
    }

    public String getValue(String key) {
        for (Map<String, String> map : values) {
            String val = map.get(key);
            if (StringKit.hasText(val)) {
                return val;
            }
        }
        return null;
    }

    public List<String> getValues(String key) {
        List<String> list = new ArrayList<>();
        for (Map<String, String> map : values) {
            String val = map.get(key);
            if (StringKit.hasText(val)) {
                list.add(val);
            }
        }
        return list;
    }
}
