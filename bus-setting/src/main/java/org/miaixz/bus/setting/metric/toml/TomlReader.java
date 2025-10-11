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
package org.miaixz.bus.setting.metric.toml;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;

/**
 * A reader for TOML (Tom's Obvious, Minimal Language) files. This implementation is based on the TOML-javalib project.
 * <p>
 * Supported date formats:
 * <ul>
 * <li>{@code 2021-03-21} is parsed as a {@link LocalDate}.</li>
 * <li>{@code 2021-03-21T19:06:30} is parsed as a {@link LocalDateTime}.</li>
 * <li>{@code 2021-03-21T19:06:30+01:00} is parsed as a {@link ZonedDateTime}.</li>
 * </ul>
 * <p>
 * This class supports a lenient bare key syntax in addition to the standard {@code A-Za-z0-9_-} characters.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TomlReader {

    /**
     * The TOML string data to be parsed.
     */
    private final String data;
    /**
     * Flag indicating whether to enforce strict ASCII for bare keys.
     */
    private final boolean strictAsciiBareKeys;
    /**
     * The current parsing position in the data string.
     */
    private int pos = 0;
    /**
     * The current line number for error reporting.
     */
    private int line = 1;

    /**
     * Constructs a new {@code TomlReader}.
     * <p>
     * Bare key character rules:
     * 
     * <pre>
     * Strict mode (true): [A-Za-z0-9_-]
     * Lenient mode (false): Any character except . [ ] # =
     * </pre>
     *
     * @param data                The TOML data as a string.
     * @param strictAsciiBareKeys If {@code true}, only allows strict bare key format; if {@code false}, supports a more
     *                            lenient format.
     */
    public TomlReader(final String data, final boolean strictAsciiBareKeys) {
        this.data = data;
        this.strictAsciiBareKeys = strictAsciiBareKeys;
    }

    /**
     * Reads and parses the entire TOML data string into a map.
     *
     * @return A map representing the parsed TOML data.
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

            // Reads the key
            final List<String> keyParts = new ArrayList<>(4);
            boolean insideSquareBrackets = true;
            while (insideSquareBrackets) {
                if (!hasNext())
                    throw new InternalException("Invalid table declaration at line " + line + ": it never ends");

                String name;
                final char nameFirstChar = nextUseful(false);
                switch (nameFirstChar) {
                    case '"':
                        if (pos + 1 < data.length() && data.charAt(pos) == '"' && data.charAt(pos + 1) == '"') {
                            pos += 2;
                            name = nextBasicMultilineString();
                        } else {
                            name = nextBasicString();
                        }
                        break;

                    case '\'':
                        if (pos + 1 < data.length() && data.charAt(pos) == '\'' && data.charAt(pos + 1) == '\'') {
                            pos += 2;
                            name = nextLiteralMultilineString();
                        } else {
                            name = nextLiteralString();
                        }
                        break;

                    default:
                        pos--; // Go back to include the first character.
                        name = nextBareKey(']', '.').trim();
                        if (data.charAt(pos) == ']') {
                            if (!name.isEmpty())
                                keyParts.add(name);
                            insideSquareBrackets = false;
                        } else if (name.isEmpty()) {
                            throw new InternalException("Invalid empty key at line " + line);
                        }
                        pos++; // Move past the character we stopped at.
                        break;
                }
                if (insideSquareBrackets)
                    keyParts.add(name.trim());
            }

            if (keyParts.isEmpty())
                throw new InternalException("Invalid empty key at line " + line);

            if (twoBrackets && next() != ']') {
                throw new InternalException("Missing character ']' at line " + line);
            }

            // Reads the table content
            final Map<String, Object> value = nextTableContent();

            // Saves the value
            Map<String, Object> currentMap = map;
            for (int i = 0; i < keyParts.size() - 1; i++) {
                final String part = keyParts.get(i);
                final Object child = currentMap.get(part);
                final Map<String, Object> childMap;
                if (child == null) { // Implicit table
                    childMap = new LinkedHashMap<>(4);
                    currentMap.put(part, childMap);
                } else if (child instanceof Map) { // Existing table
                    childMap = (Map<String, Object>) child;
                } else { // Array of tables
                    final List<Map<String, Object>> list = (List<Map<String, Object>>) child;
                    childMap = list.get(list.size() - 1);
                }
                currentMap = childMap;
            }
            if (twoBrackets) { // Array of tables
                final String name = keyParts.get(keyParts.size() - 1);
                Collection<Map<String, Object>> tableArray = (Collection<Map<String, Object>>) currentMap.get(name);
                if (tableArray == null) {
                    tableArray = new ArrayList<>(2);
                    currentMap.put(name, tableArray);
                }
                tableArray.add(value);
            } else { // Standard table
                currentMap.put(keyParts.get(keyParts.size() - 1), value);
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
        while (hasNext() && (Character.isWhitespace(c) || (c == Symbol.C_HASH && skipComments))) {
            c = next();
            if (skipComments && c == Symbol.C_HASH) {
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
            if (!hasNext())
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
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9':
                return nextNumberOrDate(firstChar);

            case '"':
                if (pos + 1 < data.length() && data.charAt(pos) == '"' && data.charAt(pos + 1) == '"') {
                    pos += 2;
                    return nextBasicMultilineString();
                }
                return nextBasicString();

            case '\'':
                if (pos + 1 < data.length() && data.charAt(pos) == '\'' && data.charAt(pos + 1) == '\'') {
                    pos += 2;
                    return nextLiteralMultilineString();
                }
                return nextLiteralString();

            case '[':
                return nextArray();

            case '{':
                return nextInlineTable();

            case 't': // "true"
                if (pos + 3 > data.length() || next() != 'r' || next() != 'u' || next() != 'e') {
                    throw new InternalException("Invalid value at line " + line);
                }
                return true;

            case 'f': // "false"
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
            if (!list.isEmpty() && !list.get(0).getClass().isAssignableFrom(value.getClass())) {
                throw new InternalException("Invalid array at line " + line + ": all values must have the same type");
            }
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
            String name;
            if (nameFirstChar == '}') {
                return map;
            }
            switch (nameFirstChar) {
                case '"':
                    name = nextBasicString();
                    break;

                case '\'':
                    name = nextLiteralString();
                    break;

                default:
                    pos--; // Go back to include the first character.
                    name = nextBareKey(Symbol.C_SPACE, '\t', Symbol.C_EQUAL);
                    if (name.isEmpty())
                        throw new InternalException("Invalid empty key at line " + line);
                    break;
            }

            final char separator = nextUsefulOrLinebreak();
            if (separator != Symbol.C_EQUAL) {
                throw new InternalException(
                        "Invalid character '" + toString(separator) + "' at line " + line + ": expected '='");
            }

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
            String name;
            switch (nameFirstChar) {
                case '"':
                    if (pos + 1 < data.length() && data.charAt(pos) == '"' && data.charAt(pos + 1) == '"') {
                        pos += 2;
                        name = nextBasicMultilineString();
                    } else {
                        name = nextBasicString();
                    }
                    break;

                case '\'':
                    if (pos + 1 < data.length() && data.charAt(pos) == '\'' && data.charAt(pos + 1) == '\'') {
                        pos += 2;
                        name = nextLiteralMultilineString();
                    } else {
                        name = nextLiteralString();
                    }
                    break;

                default:
                    pos--; // Go back to include the first character.
                    name = nextBareKey(Symbol.C_SPACE, '\t', Symbol.C_EQUAL);
                    if (name.isEmpty())
                        throw new InternalException("Invalid empty key at line " + line);
                    break;
            }
            final char separator = nextUsefulOrLinebreak();
            if (separator != Symbol.C_EQUAL) {
                throw new InternalException(
                        "Invalid character '" + toString(separator) + "' at line " + line + ": expected '='");
            }
            final char valueFirstChar = nextUsefulOrLinebreak();
            if (valueFirstChar == '\n') {
                throw new InternalException("Invalid newline before a value at line " + line);
            }
            final Object value = nextValue(valueFirstChar);

            final char afterEntry = nextUsefulOrLinebreak();
            if (afterEntry == Symbol.C_HASH) {
                pos--;
            } else if (afterEntry != '\n') {
                throw new InternalException(
                        "Invalid character '" + toString(afterEntry) + "' after a value at line " + line);
            }
            if (map.containsKey(name))
                throw new InternalException("Duplicate key \"" + name + "\"");

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
                case Symbol.C_COLON, 'T', 'Z':
                    maybeInteger = maybeDouble = false;
                    break;

                case 'e', 'E':
                    maybeInteger = maybeDate = false;
                    break;

                case '.':
                    maybeInteger = false;
                    break;

                case Symbol.C_MINUS:
                    if (pos > 1 && data.charAt(pos - 2) != 'e' && data.charAt(pos - 2) != 'E') {
                        maybeInteger = maybeDouble = false;
                    }
                    break;

                case Symbol.C_COMMA, Symbol.C_SPACE, '\t', '\n', '\r', ']', '}':
                    pos--;
                    break whileLoop;
            }
            if (c == Symbol.C_UNDERLINE) {
                maybeDate = false;
            } else {
                sb.append(c);
            }
        }
        final String valueStr = sb.toString();
        try {
            if (maybeInteger) {
                if (valueStr.length() < 10)
                    return Integer.parseInt(valueStr);
                return Long.parseLong(valueStr);
            }
            if (maybeDouble)
                return Double.parseDouble(valueStr);
            if (maybeDate) {
                return Toml.DATE_FORMATTER
                        .parseBest(valueStr, ZonedDateTime::from, LocalDateTime::from, LocalDate::from);
            }
        } catch (final Exception ex) {
            throw new InternalException("Invalid value: \"" + valueStr + "\" at line " + line, ex);
        }

        throw new InternalException("Invalid value: \"" + valueStr + "\" at line " + line);
    }

    private String nextBareKey(final char... allowedEnds) {
        final String keyName;
        for (int i = pos; i < data.length(); i++) {
            final char c = data.charAt(i);
            for (final char allowedEnd : allowedEnds) {
                if (c == allowedEnd) {
                    keyName = data.substring(pos, i);
                    pos = i;
                    return keyName;
                }
            }
            if (strictAsciiBareKeys) {
                if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')
                        || c == Symbol.C_UNDERLINE || c == Symbol.C_MINUS)) {
                    throw new InternalException(
                            "Forbidden character '" + toString(c) + "' in strict bare key at line " + line);
                }
            } else if (c <= Symbol.C_SPACE || c == Symbol.C_HASH || c == Symbol.C_EQUAL || c == '.' || c == '['
                    || c == ']') {
                throw new InternalException(
                        "Forbidden character '" + toString(c) + "' in lenient bare key at line " + line);
            }
        }
        throw new InternalException("Invalid key-value pair at line " + line + ": unexpected end of data");
    }

    private String nextLiteralString() {
        final int index = data.indexOf('\'', pos);
        if (index == -1)
            throw new InternalException("Invalid literal String at line " + line + ": it never ends");
        final String text = data.substring(pos, index);
        if (text.indexOf('\n') != -1) {
            throw new InternalException("Invalid literal String at line " + line + ": newlines are not allowed here");
        }
        pos = index + 1;
        return text;
    }

    private String nextLiteralMultilineString() {
        final int index = data.indexOf("'''", pos);
        if (index == -1) {
            throw new InternalException("Invalid multiline literal String at line " + line + ": it never ends");
        }
        final String text;
        if (data.charAt(pos) == '\r' && pos + 1 < data.length() && data.charAt(pos + 1) == '\n') { // Starts with "\r\n"
            text = data.substring(pos + 2, index);
            line++;
        } else if (data.charAt(pos) == '\n') { // Starts with '\n'
            text = data.substring(pos + 1, index);
            line++;
        } else {
            text = data.substring(pos, index);
        }
        for (int i = 0; i < text.length(); i++) { // count lines
            if (text.charAt(i) == '\n')
                line++;
        }
        pos = index + 3;
        return text;
    }

    private String nextBasicString() {
        final StringBuilder sb = new StringBuilder();
        boolean escape = false;
        while (hasNext()) {
            final char c = next();
            if (c == '\n' || c == '\r') {
                throw new InternalException("Invalid basic String at line " + line + ": newlines not allowed");
            }
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
        throw new InternalException("Invalid basic String at line " + line + ": it never ends");
    }

    private String nextBasicMultilineString() {
        final StringBuilder sb = new StringBuilder();
        boolean first = true, escape = false;
        while (hasNext()) {
            final char c = next();
            if (first && (c == '\r' || c == '\n')) {
                if (c == '\r' && hasNext() && data.charAt(pos) == '\n')
                    pos++;
                else
                    line++;
                first = false;
                continue;
            }
            if (escape) {
                if (c == '\r' || c == '\n' || c == Symbol.C_SPACE || c == '\t') {
                    if (c == '\r' && hasNext() && data.charAt(pos) == '\n')
                        pos++;
                    else if (c == '\n')
                        line++;
                    nextUseful(false);
                    pos--;
                } else {
                    sb.append(unescape(c));
                }
                escape = false;
            } else if (c == '\\') {
                escape = true;
            } else if (c == '"') {
                if (pos + 1 < data.length() && data.charAt(pos) == '"' && data.charAt(pos + 1) == '"') {
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

            case 'u': { // unicode uXXXX
                if (data.length() - pos < 4)
                    throw new InternalException("Invalid unicode code point at line " + line);
                final String unicode = data.substring(pos, pos + 4);
                pos += 4;
                try {
                    return (char) Integer.parseInt(unicode, 16);
                } catch (final NumberFormatException ex) {
                    throw new InternalException("Invalid unicode code point at line " + line, ex);
                }
            }

            case 'U': { // unicode UXXXXXXXX
                if (data.length() - pos < 8)
                    throw new InternalException("Invalid unicode code point at line " + line);
                final String unicode = data.substring(pos, pos + 8);
                pos += 8;
                try {
                    return (char) Integer.parseInt(unicode, 16);
                } catch (final NumberFormatException ex) {
                    throw new InternalException("Invalid unicode code point at line " + line, ex);
                }
            }

            default:
                throw new InternalException("Invalid escape sequence: \"\\" + c + "\" at line " + line);
        }
    }

    /**
     * Converts a character to its string representation, escaping it if necessary.
     * 
     * @param c The character to convert.
     * @return The string representation.
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
