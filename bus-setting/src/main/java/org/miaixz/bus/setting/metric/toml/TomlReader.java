/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.setting.metric.toml;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;

/**
 * TOML文件读取 来自：https://github.com/TheElectronWill/TOML-javalib
 * <p>
 * 日期格式支持：
 * <ul>
 * <li>2015-03-20 转为：{@link LocalDate}</li>
 * <li>2015-03-20T19:04:35 转为：{@link LocalDateTime}</li>
 * <li>2015-03-20T19:04:35+01:00 转为：{@link ZonedDateTime}</li>
 * </ul>
 * <p>
 * 此类支持更加宽松的key，除了{@code A-Za-z0-9_- }，还支持' ','.', '[', ']' 和 '='
 * <p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TomlReader {

    private final String data;
    private final boolean strictAsciiBareKeys;
    private int pos = 0;// current position
    private int line = 1;// current line

    /**
     * 构造
     * 
     * <pre>
     *     严格模式：[A-Za-z0-9_-]
     *     宽松模式：所有字符但是不包括. [ ] # =
     * </pre>
     *
     * @param data                TOML数据
     * @param strictAsciiBareKeys {@code true} 只允许严格的key格式, {@code false} 支持宽松格式.
     */
    public TomlReader(final String data, final boolean strictAsciiBareKeys) {
        this.data = data;
        this.strictAsciiBareKeys = strictAsciiBareKeys;
    }

    /**
     * 读取TOML
     *
     * @return TOML
     */
    public Map<String, Object> read() {
        final Map<String, Object> map = nextTableContent();

        if (!hasNext() && pos > 0 && data.charAt(pos - 1) == '[') {
            throw new InternalException("Invalid table declaration at line " + line + ": it never ends");
        }

        while (hasNext()) {
            char c = nextUseful(true);
            final boolean twoBrackets;
            if (c == '[') {
                twoBrackets = true;
                c = nextUseful(false);
            } else {
                twoBrackets = false;
            }
            pos--;

            // --- Reads the data --
            final List<String> keyParts = new ArrayList<>(4);
            boolean insideSquareBrackets = true;
            while (insideSquareBrackets) {
                if (!hasNext())
                    throw new InternalException("Invalid table declaration at line " + line + ": it never ends");

                String name = null;
                final char nameFirstChar = nextUseful(false);
                switch (nameFirstChar) {
                case '"': {
                    if (pos + 1 < data.length()) {
                        final char c2 = data.charAt(pos);
                        final char c3 = data.charAt(pos + 1);
                        if (c2 == '"' && c3 == '"') {
                            pos += 2;
                            name = nextBasicMultilineString();
                        }
                    }
                    if (name == null) {
                        name = nextBasicString();
                    }
                    break;
                }
                case '\'': {
                    if (pos + 1 < data.length()) {
                        final char c2 = data.charAt(pos);
                        final char c3 = data.charAt(pos + 1);
                        if (c2 == '\'' && c3 == '\'') {
                            pos += 2;
                            name = nextLiteralMultilineString();
                        }
                    }
                    if (name == null) {
                        name = nextLiteralString();
                    }
                    break;
                }
                default:
                    pos--;// to include the first (already read) non-space character
                    name = nextBareKey(']', '.').trim();
                    if (data.charAt(pos) == ']') {
                        if (!name.isEmpty())
                            keyParts.add(name);
                        insideSquareBrackets = false;
                    } else if (name.isEmpty()) {
                        throw new InternalException("Invalid empty data at line " + line);
                    }

                    pos++;// to go after the character we stopped at in nextBareKey()
                    break;
                }
                if (insideSquareBrackets)
                    keyParts.add(name.trim());
            }

            // -- Checks --
            if (keyParts.isEmpty())
                throw new InternalException("Invalid empty data at line " + line);

            if (twoBrackets && next() != ']') {// 2 brackets at the start but only one at the end!
                throw new InternalException("Missing character ']' at line " + line);
            }

            // -- Reads the value (table content) --
            final Map<String, Object> value = nextTableContent();

            // -- Saves the value --
            Map<String, Object> valueMap = map;// the map that contains the value
            for (int i = 0; i < keyParts.size() - 1; i++) {
                final String part = keyParts.get(i);
                final Object child = valueMap.get(part);
                final Map<String, Object> childMap;
                if (child == null) {// implicit table
                    childMap = new LinkedHashMap<>(4);
                    valueMap.put(part, childMap);
                } else if (child instanceof Map) {// table
                    childMap = (Map<String, Object>) child;
                } else {// array
                    final List<Map<String, Object>> list = (List<Map<String, Object>>) child;
                    childMap = list.get(list.size() - 1);
                }
                valueMap = childMap;
            }
            if (twoBrackets) {// element of a table array
                final String name = keyParts.get(keyParts.size() - 1);
                Collection<Map<String, Object>> tableArray = (Collection<Map<String, Object>>) valueMap.get(name);
                if (tableArray == null) {
                    tableArray = new ArrayList<>(2);
                    valueMap.put(name, tableArray);
                }
                tableArray.add(value);
            } else {// just a table
                valueMap.put(keyParts.get(keyParts.size() - 1), value);
            }

        }
        return map;
    }

    private boolean hasNext() {
        return pos < data.length();
    }

    private char next() {
        return data.charAt(pos++);
    }

    private char nextUseful(final boolean skipComments) {
        char c = Symbol.C_SPACE;
        while (hasNext() && (c == Symbol.C_SPACE || c == '\t' || c == '\r' || c == '\n'
                || (c == Symbol.C_SHAPE && skipComments))) {
            c = next();
            if (skipComments && c == Symbol.C_SHAPE) {
                final int nextLinebreak = data.indexOf('\n', pos);
                if (nextLinebreak == -1) {
                    pos = data.length();
                } else {
                    pos = nextLinebreak + 1;
                    line++;
                }
            } else if (c == '\n') {
                line++;
            }
        }
        return c;
    }

    private char nextUsefulOrLinebreak() {
        char c = Symbol.C_SPACE;
        while (c == Symbol.C_SPACE || c == '\t' || c == '\r') {
            if (!hasNext())// fixes error when no '\n' at the end of the file
                return '\n';
            c = next();
        }
        if (c == '\n')
            line++;
        return c;
    }

    private Object nextValue(final char firstChar) {
        switch (firstChar) {
        case Symbol.C_PLUS:
        case Symbol.C_MINUS:
        case '0':
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
            return nextNumberOrDate(firstChar);
        case '"':
            if (pos + 1 < data.length()) {
                final char c2 = data.charAt(pos);
                final char c3 = data.charAt(pos + 1);
                if (c2 == '"' && c3 == '"') {
                    pos += 2;
                    return nextBasicMultilineString();
                }
            }
            return nextBasicString();
        case '\'':
            if (pos + 1 < data.length()) {
                final char c2 = data.charAt(pos);
                final char c3 = data.charAt(pos + 1);
                if (c2 == '\'' && c3 == '\'') {
                    pos += 2;
                    return nextLiteralMultilineString();
                }
            }
            return nextLiteralString();
        case '[':
            return nextArray();
        case '{':
            return nextInlineTable();
        case 't':// Must be "true"
            if (pos + 3 > data.length() || next() != 'r' || next() != 'u' || next() != 'e') {
                throw new InternalException("Invalid value at line " + line);
            }
            return true;
        case 'f':// Must be "false"
            if (pos + 4 > data.length() || next() != 'a' || next() != 'l' || next() != 's' || next() != 'e') {
                throw new InternalException("Invalid value at line " + line);
            }
            return false;
        default:
            throw new InternalException("Invalid character '" + toString(firstChar) + "' at line " + line);
        }
    }

    private List<Object> nextArray() {
        final ArrayList<Object> list = new ArrayList<>();
        while (true) {
            final char c = nextUseful(true);
            if (c == ']') {
                pos++;
                break;
            }
            final Object value = nextValue(c);
            if (!list.isEmpty() && !(list.get(0).getClass().isAssignableFrom(value.getClass())))
                throw new InternalException(
                        "Invalid array at line " + line + ": all the values must have the same type");
            list.add(value);

            final char afterEntry = nextUseful(true);
            if (afterEntry == ']') {
                pos++;
                break;
            }
            if (afterEntry != Symbol.C_COMMA) {
                throw new InternalException("Invalid array at line " + line + ": expected a comma after each value");
            }
        }
        pos--;
        list.trimToSize();
        return list;
    }

    private Map<String, Object> nextInlineTable() {
        final Map<String, Object> map = new LinkedHashMap<>();
        while (true) {
            final char nameFirstChar = nextUsefulOrLinebreak();
            String name = null;
            switch (nameFirstChar) {
            case '}':
                return map;
            case '"': {
                if (pos + 1 < data.length()) {
                    final char c2 = data.charAt(pos);
                    final char c3 = data.charAt(pos + 1);
                    if (c2 == '"' && c3 == '"') {
                        pos += 2;
                        name = nextBasicMultilineString();
                    }
                }
                if (name == null)
                    name = nextBasicString();
                break;
            }
            case '\'': {
                if (pos + 1 < data.length()) {
                    final char c2 = data.charAt(pos);
                    final char c3 = data.charAt(pos + 1);
                    if (c2 == '\'' && c3 == '\'') {
                        pos += 2;
                        name = nextLiteralMultilineString();
                    }
                }
                if (name == null)
                    name = nextLiteralString();
                break;
            }
            default:
                pos--;// to include the first (already read) non-space character
                name = nextBareKey(Symbol.C_SPACE, '\t', Symbol.C_EQUAL);
                if (name.isEmpty())
                    throw new InternalException("Invalid empty data at line " + line);
                break;
            }

            final char separator = nextUsefulOrLinebreak();// tries to find the '=' sign
            if (separator != Symbol.C_EQUAL)
                throw new InternalException(
                        "Invalid character '" + toString(separator) + "' at line " + line + ": expected '='");

            final char valueFirstChar = nextUsefulOrLinebreak();
            final Object value = nextValue(valueFirstChar);
            map.put(name, value);

            final char after = nextUsefulOrLinebreak();
            if (after == '}' || !hasNext()) {
                return map;
            } else if (after != Symbol.C_COMMA) {
                throw new InternalException("Invalid inline table at line " + line + ": missing comma");
            }
        }
    }

    private Map<String, Object> nextTableContent() {
        final Map<String, Object> map = new LinkedHashMap<>();
        while (true) {
            final char nameFirstChar = nextUseful(true);
            if (!hasNext() || nameFirstChar == '[') {
                return map;
            }
            String name = null;
            switch (nameFirstChar) {
            case '"': {
                if (pos + 1 < data.length()) {
                    final char c2 = data.charAt(pos);
                    final char c3 = data.charAt(pos + 1);
                    if (c2 == '"' && c3 == '"') {
                        pos += 2;
                        name = nextBasicMultilineString();
                    }
                }
                if (name == null) {
                    name = nextBasicString();
                }
                break;
            }
            case '\'': {
                if (pos + 1 < data.length()) {
                    final char c2 = data.charAt(pos);
                    final char c3 = data.charAt(pos + 1);
                    if (c2 == '\'' && c3 == '\'') {
                        pos += 2;
                        name = nextLiteralMultilineString();
                    }
                }
                if (name == null) {
                    name = nextLiteralString();
                }
                break;
            }
            default:
                pos--;// to include the first (already read) non-space character
                name = nextBareKey(Symbol.C_SPACE, '\t', Symbol.C_EQUAL);
                if (name.isEmpty())
                    throw new InternalException("Invalid empty data at line " + line);
                break;
            }
            final char separator = nextUsefulOrLinebreak();// tries to find the '=' sign
            if (separator != Symbol.C_EQUAL)// an other character
                throw new InternalException(
                        "Invalid character '" + toString(separator) + "' at line " + line + ": expected '='");

            final char valueFirstChar = nextUsefulOrLinebreak();
            if (valueFirstChar == '\n') {
                throw new InternalException("Invalid newline before the value at line " + line);
            }
            final Object value = nextValue(valueFirstChar);

            final char afterEntry = nextUsefulOrLinebreak();
            if (afterEntry == Symbol.C_SHAPE) {
                pos--;// to make the next nextUseful() call read the # character
            } else if (afterEntry != '\n') {
                throw new InternalException(
                        "Invalid character '" + toString(afterEntry) + "' after the value at line " + line);
            }
            if (map.containsKey(name))
                throw new InternalException("Duplicate data \"" + name + "\"");

            map.put(name, value);
        }
    }

    private Object nextNumberOrDate(final char first) {
        boolean maybeDouble = true, maybeInteger = true, maybeDate = true;
        final StringBuilder sb = new StringBuilder();
        sb.append(first);
        char c;
        whileLoop: while (hasNext()) {
            c = next();
            switch (c) {
            case Symbol.C_COLON:
            case 'T':
            case 'Z':
                maybeInteger = maybeDouble = false;
                break;
            case 'e':
            case 'E':
                maybeInteger = maybeDate = false;
                break;
            case '.':
                maybeInteger = false;
                break;
            case Symbol.C_MINUS:
                if (pos != 0 && data.charAt(pos - 1) != 'e' && data.charAt(pos - 1) != 'E')
                    maybeInteger = maybeDouble = false;
                break;
            case Symbol.C_COMMA:
            case Symbol.C_SPACE:
            case '\t':
            case '\n':
            case '\r':
            case ']':
            case '}':
                pos--;
                break whileLoop;
            }
            if (c == Symbol.C_UNDERLINE)
                maybeDate = false;
            else
                sb.append(c);
        }
        final String values = sb.toString();
        try {
            if (maybeInteger) {
                if (values.length() < 10)
                    return Integer.parseInt(values);
                return Long.parseLong(values);
            }

            if (maybeDouble)
                return Double.parseDouble(values);

            if (maybeDate)
                return Toml.DATE_FORMATTER.parseBest(values, ZonedDateTime::from, LocalDateTime::from, LocalDate::from);

        } catch (final Exception ex) {
            throw new InternalException("Invalid value: \"" + values + "\" at line " + line, ex);
        }

        throw new InternalException("Invalid value: \"" + values + "\" at line " + line);
    }

    private String nextBareKey(final char... allowedEnds) {
        final String keyName;
        for (int i = pos; i < data.length(); i++) {
            final char c = data.charAt(i);
            for (final char allowedEnd : allowedEnds) {
                if (c == allowedEnd) {// checks if this character allowed to end this bare data
                    keyName = data.substring(pos, i);
                    pos = i;
                    return keyName;
                }
            }
            if (strictAsciiBareKeys) {
                if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')
                        || c == Symbol.C_UNDERLINE || c == Symbol.C_MINUS))
                    throw new InternalException(
                            "Forbidden character '" + toString(c) + "' in strict bare-data at line " + line);
            } else if (c <= Symbol.C_SPACE || c == Symbol.C_SHAPE || c == Symbol.C_EQUAL || c == '.' || c == '['
                    || c == ']') {// lenient bare data
                throw new InternalException(
                        "Forbidden character '" + toString(c) + "' in lenient bare-data at line " + line);
            } // else continue reading
        }
        throw new InternalException("Invalid data/value pair at line " + line
                + " end of data reached before the value attached to the data was found");
    }

    private String nextLiteralString() {
        final int index = data.indexOf('\'', pos);
        if (index == -1)
            throw new InternalException("Invalid literal String at line " + line + ": it never ends");

        final String text = data.substring(pos, index);
        if (text.indexOf('\n') != -1)
            throw new InternalException("Invalid literal String at line " + line + ": newlines are not allowed here");

        pos = index + 1;
        return text;
    }

    private String nextLiteralMultilineString() {
        final int index = data.indexOf("'''", pos);
        if (index == -1)
            throw new InternalException("Invalid multiline literal String at line " + line + ": it never ends");
        final String text;
        if (data.charAt(pos) == '\r' && data.charAt(pos + 1) == '\n') {// "\r\n" at the beginning of the string
            text = data.substring(pos + 2, index);
            line++;
        } else if (data.charAt(pos) == '\n') {// '\n' at the beginning of the string
            text = data.substring(pos + 1, index);
            line++;
        } else {
            text = data.substring(pos, index);
        }
        for (int i = 0; i < text.length(); i++) {// count lines
            final char c = text.charAt(i);
            if (c == '\n')
                line++;
        }
        pos = index + 3;// goes after the 3 quotes
        return text;
    }

    private String nextBasicString() {
        final StringBuilder sb = new StringBuilder();
        boolean escape = false;
        while (hasNext()) {
            final char c = next();
            if (c == '\n' || c == '\r')
                throw new InternalException("Invalid basic String at line " + line + ": newlines not allowed");
            if (escape) {
                sb.append(unescape(c));
                escape = false;
            } else if (c == '\\') {
                escape = true;
            } else if (c == '"') {
                return sb.toString();
            } else {
                sb.append(c);
            }
        }
        throw new InternalException("Invalid basic String at line " + line + ": it nerver ends");
    }

    private String nextBasicMultilineString() {
        final StringBuilder sb = new StringBuilder();
        boolean first = true, escape = false;
        while (hasNext()) {
            final char c = next();
            if (first && (c == '\r' || c == '\n')) {
                if (c == '\r' && hasNext() && data.charAt(pos) == '\n')// "\r\n"
                    pos++;// so that it is NOT read by the next call to next()
                else
                    line++;
                first = false;
                continue;
            }
            if (escape) {
                if (c == '\r' || c == '\n' || c == Symbol.C_SPACE || c == '\t') {
                    if (c == '\r' && hasNext() && data.charAt(pos) == '\n')// "\r\n"
                        pos++;
                    else if (c == '\n')
                        line++;
                    nextUseful(false);
                    pos--;// so that it is read by the next call to next()
                } else {
                    sb.append(unescape(c));
                }
                escape = false;
            } else if (c == '\\') {
                escape = true;
            } else if (c == '"') {
                if (pos + 1 >= data.length())
                    break;
                if (data.charAt(pos) == '"' && data.charAt(pos + 1) == '"') {
                    pos += 2;
                    return sb.toString();
                }
            } else if (c == '\n') {
                line++;
                sb.append(c);
            } else {
                sb.append(c);
            }
        }
        throw new InternalException("Invalid multiline basic String at line " + line + ": it never ends");
    }

    private char unescape(final char c) {
        switch (c) {
        case 'b':
            return '\b';
        case 't':
            return '\t';
        case 'n':
            return '\n';
        case 'f':
            return '\f';
        case 'r':
            return '\r';
        case '"':
            return '"';
        case '\\':
            return '\\';
        case 'u': {// unicode uXXXX
            if (data.length() - pos < 5)
                throw new InternalException("Invalid unicode code point at line " + line);
            final String unicode = data.substring(pos, pos + 4);
            pos += 4;
            try {
                final int hexVal = Integer.parseInt(unicode, 16);
                return (char) hexVal;
            } catch (final NumberFormatException ex) {
                throw new InternalException("Invalid unicode code point at line " + line, ex);
            }
        }
        case 'U': {// unicode UXXXXXXXX
            if (data.length() - pos < 9)
                throw new InternalException("Invalid unicode code point at line " + line);
            final String unicode = data.substring(pos, pos + 8);
            pos += 8;
            try {
                final int hexVal = Integer.parseInt(unicode, 16);
                return (char) hexVal;
            } catch (final NumberFormatException ex) {
                throw new InternalException("Invalid unicode code point at line " + line, ex);
            }
        }
        default:
            throw new InternalException("Invalid escape sequence: \"\\" + c + "\" at line " + line);
        }
    }

    /**
     * Converts a char to a String. The char is escaped if needed.
     */
    private String toString(final char c) {
        switch (c) {
        case '\b':
            return "\\b";
        case '\t':
            return "\\t";
        case '\n':
            return "\\n";
        case '\r':
            return "\\r";
        case '\f':
            return "\\f";
        default:
            return String.valueOf(c);
        }
    }

}
