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
package org.miaixz.bus.setting.metric.toml;

import java.io.IOException;
import java.io.Writer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * A writer for generating TOML (Tom's Obvious, Minimal Language) data from a Map.
 * <p>
 * Supported date/time types for writing:
 * <ul>
 * <li>{@link LocalDate} (e.g., 2015-03-20)</li>
 * <li>{@link LocalDateTime} (e.g., 2015-03-20T19:04:35)</li>
 * <li>{@link ZonedDateTime} (e.g., 2015-03-20T19:04:35+01:00)</li>
 * </ul>
 * <p>
 * This writer supports bare keys consisting of {@code A-Za-z0-9_-}. Any other keys will be enclosed in double quotes.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TomlWriter {

    /**
     * The underlying writer where the TOML data will be written.
     */
    private final Writer writer;
    /**
     * The number of characters to use for each level of indentation.
     */
    private final int indentSize;
    /**
     * The character to use for indentation (either a space or a tab).
     */
    private final char indentCharacter;
    /**
     * The string used for line breaks (e.g., "\n" or "\r\n").
     */
    private final String lineSeparator;
    /**
     * A linked list that keeps track of the current table path (e.g., ["table", "subtable"]).
     */
    private final LinkedList<String> tablesNames = new LinkedList<>();
    /**
     * A counter to prevent writing more than two consecutive newlines.
     */
    private int lineBreaks = 0;
    /**
     * The current level of indentation for nested tables. Initialized to -1 to prevent indenting the root level.
     */
    private int indentationLevel = -1;

    /**
     * Creates a new TomlWriter with default parameters. The system line separator is used. This is equivalent to
     * {@code TomlWriter(writer, 1, false, System.lineSeparator())}.
     *
     * @param writer where to write the data
     */
    public TomlWriter(final Writer writer) {
        this(writer, 1, false, System.lineSeparator());
    }

    /**
     * Creates a new TomlWriter with specified indentation parameters. The system line separator is used. This is
     * equivalent to {@code TomlWriter(writer, indentSize, indentWithSpaces, System.lineSeparator())}.
     *
     * @param writer           where to write the data
     * @param indentSize       the number of characters for each indent level
     * @param indentWithSpaces true to indent with spaces, false to indent with tabs
     */
    public TomlWriter(final Writer writer, final int indentSize, final boolean indentWithSpaces) {
        this(writer, indentSize, indentWithSpaces, System.lineSeparator());
    }

    /**
     * Creates a new TomlWriter with the specified parameters.
     *
     * @param writer           where to write the data
     * @param indentSize       the number of characters for each indent level
     * @param indentWithSpaces true to indent with spaces, false to indent with tabs
     * @param lineSeparator    the string to use for line breaks
     */
    public TomlWriter(final Writer writer, final int indentSize, final boolean indentWithSpaces,
            final String lineSeparator) {
        this.writer = writer;
        this.indentSize = indentSize;
        this.indentCharacter = indentWithSpaces ? Symbol.C_SPACE : Symbol.C_TAB;
        this.lineSeparator = lineSeparator;
    }

    private static boolean isValidCharOfKey(final char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == Symbol.C_MINUS
                || c == Symbol.C_UNDERLINE;
    }

    static void addEscaped(final char c, final StringBuilder sb) {
        switch (c) {
            case '\b':
                sb.append("\\b");
                break;

            case '\t':
                sb.append("\\t");
                break;

            case '\n':
                sb.append("\\n");
                break;

            case '\\':
                sb.append("\\\\");
                break;

            case '\r':
                sb.append("\\r");
                break;

            case '\f':
                sb.append("\\f");
                break;

            case '"':
                sb.append("\\\"");
                break;

            default:
                sb.append(c);
                break;
        }
    }

    /**
     * Closes the underlying writer, flushing it first.
     *
     * @throws IOException if an I/O error occurs
     */
    public void close() throws IOException {
        writer.close();
    }

    /**
     * Flushes the underlying writer.
     *
     * @throws IOException if an I/O error occurs
     */
    public void flush() throws IOException {
        writer.flush();
    }

    /**
     * Writes the specified data map to the writer in TOML format.
     *
     * @param data the data to write
     * @throws InternalException if an I/O error occurs during writing
     */
    public void write(final Map<String, Object> data) throws InternalException {
        writeTableContent(data);
    }

    private void writeTableName() throws InternalException {
        final Iterator<String> it = tablesNames.iterator();
        while (it.hasNext()) {
            final String namePart = it.next();
            writeKey(namePart);
            if (it.hasNext()) {
                write('.');
            }
        }
    }

    private void writeTableContent(final Map<String, Object> table) throws InternalException {
        writeTableContent(table, true); // First pass: write simple key-value pairs and simple arrays.
        writeTableContent(table, false); // Second pass: write tables and arrays of tables.
    }

    /**
     * Writes the content of a table, separating simple values from nested tables.
     *
     * @param table        the table to write
     * @param simpleValues true to write only the simple values and arrays of simple values; false to write only nested
     *                     tables and arrays of tables.
     */
    private void writeTableContent(final Map<String, Object> table, final boolean simpleValues)
            throws InternalException {
        for (final Map.Entry<String, Object> entry : table.entrySet()) {
            final String name = entry.getKey();
            final Object value = entry.getValue();
            if (value instanceof Collection<?> c) {
                if (!c.isEmpty() && c.iterator().next() instanceof Map) { // Array of tables
                    if (simpleValues)
                        continue;
                    tablesNames.addLast(name);
                    indentationLevel++;
                    for (final Object element : c) {
                        indent();
                        write("[[");
                        writeTableName();
                        write("]]\n");
                        final Map<String, Object> map = (Map<String, Object>) element;
                        writeTableContent(map);
                    }
                    indentationLevel--;
                    tablesNames.removeLast();
                } else { // Simple array
                    if (!simpleValues)
                        continue;
                    indent();
                    writeKey(name);
                    write(" = ");
                    writeArray(c);
                }
            } else if (value instanceof Object[] array) {
                if (array.length > 0 && array[0] instanceof Map) { // Array of tables
                    if (simpleValues)
                        continue;
                    tablesNames.addLast(name);
                    indentationLevel++;
                    for (final Object element : array) {
                        indent();
                        write("[[");
                        writeTableName();
                        write("]]\n");
                        final Map<String, Object> map = (Map<String, Object>) element;
                        writeTableContent(map);
                    }
                    indentationLevel--;
                    tablesNames.removeLast();
                } else { // Simple array
                    if (!simpleValues)
                        continue;
                    indent();
                    writeKey(name);
                    write(" = ");
                    writeString(StringKit.toStringOrEmpty(ArrayKit.toString(array)));
                }
            } else if (value instanceof Map) { // Table
                if (simpleValues)
                    continue;
                tablesNames.addLast(name);
                indentationLevel++;
                indent();
                write('[');
                writeTableName();
                write(']');
                newLine();
                writeTableContent((Map<String, Object>) value);
                indentationLevel--;
                tablesNames.removeLast();
            } else { // Simple value
                if (!simpleValues)
                    continue;
                indent();
                writeKey(name);
                write(" = ");
                writeValue(value);
            }
            newLine();
        }
        newLine();
    }

    private void writeKey(final String key) throws InternalException {
        for (int i = 0; i < key.length(); i++) {
            final char c = key.charAt(i);
            if (!isValidCharOfKey(c)) {
                // If the key contains invalid characters for a bare key, quote it.
                writeString(key);
                return;
            }
        }
        write(key);
    }

    private void writeString(final String text) throws InternalException {
        final StringBuilder sb = new StringBuilder();
        sb.append('"');
        for (int i = 0; i < text.length(); i++) {
            final char c = text.charAt(i);
            addEscaped(c, sb);
        }
        sb.append('"');
        write(sb.toString());
    }

    private void writeArray(final Collection<?> c) throws InternalException {
        write('[');
        Iterator<?> it = c.iterator();
        while (it.hasNext()) {
            writeValue(it.next());
            if (it.hasNext()) {
                write(", ");
            }
        }
        write(']');
    }

    private void writeValue(final Object value) throws InternalException {
        if (value instanceof String) {
            writeString((String) value);
        } else if (value instanceof Number || value instanceof Boolean) {
            write(value.toString());
        } else if (value instanceof TemporalAccessor) {
            String formatted = Toml.DATE_FORMATTER.format((TemporalAccessor) value);
            if (formatted.endsWith("T")) { // A lone 'T' at the end is invalid.
                formatted = formatted.substring(0, formatted.length() - 1);
            }
            write(formatted);
        } else if (value instanceof Collection) {
            writeArray((Collection<?>) value);
        } else if (ArrayKit.isArray(value)) {
            write(ArrayKit.toString(value));
        } else if (value instanceof Map) {
            throw new InternalException("Unexpected nested map value: " + value);
        } else {
            throw new InternalException(
                    "Unsupported value of type " + (value == null ? "null" : value.getClass().getCanonicalName()));
        }
    }

    private void newLine() throws InternalException {
        if (lineBreaks <= 1) {
            try {
                writer.write(lineSeparator);
            } catch (final IOException e) {
                throw new InternalException(e);
            }
            lineBreaks++;
        }
    }

    private void write(final char c) throws InternalException {
        try {
            writer.write(c);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
        lineBreaks = 0;
    }

    private void write(final String text) throws InternalException {
        try {
            writer.write(text);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
        lineBreaks = 0;
    }

    private void indent() throws InternalException {
        for (int i = 0; i < indentationLevel; i++) {
            for (int j = 0; j < indentSize; j++) {
                write(indentCharacter);
            }
        }
    }

}
