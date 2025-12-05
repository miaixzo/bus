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
package org.miaixz.bus.office.csv;

import java.io.*;
import java.util.*;

import org.miaixz.bus.core.center.iterator.ComputeIterator;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.text.StringTrimer;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * CSV row parser, inspired by FastCSV.
 * <p>
 * This class reads CSV data character by character, handling quoted fields, escaped delimiters, and multi-line fields
 * according to standard CSV rules (RFC 4180).
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class CsvParser extends ComputeIterator<CsvRow> implements Closeable, Serializable {

    @Serial
    private static final long serialVersionUID = 2852282936732L;

    /**
     * Default capacity for a row, used to initialize field lists.
     */
    private static final int DEFAULT_ROW_CAPACITY = 10;

    /**
     * The CSV read configuration.
     */
    private final CsvReadConfig config;
    /**
     * The CSV tokener used to read characters from the input stream.
     */
    private final CsvTokener tokener;
    /**
     * The current field being built.
     */
    private final StringBuilder currentField = new StringBuilder(512);
    /**
     * The previous special delimiter character encountered.
     */
    private int preChar = -1;
    /**
     * Flag indicating whether the parser is currently inside a quoted field.
     */
    private boolean inQuotes;
    /**
     * The header row of the CSV data.
     */
    private CsvRow header;
    /**
     * The current line number being parsed.
     */
    private long lineNo = -1;
    /**
     * The number of lines consumed while inside a quoted field (handling multi-line fields).
     */
    private long inQuotesLineCount;
    /**
     * The number of fields in the first data line, used to check for consistent field counts across rows.
     */
    private int firstLineFieldCount = -1;
    /**
     * The maximum number of fields found in any row so far, used for initial row capacity optimization.
     */
    private int maxFieldCount;
    /**
     * Flag indicating whether the parsing is finished.
     */
    private boolean finished;

    /**
     * Constructs a new {@code CsvParser}.
     *
     * @param reader The {@link Reader} to read CSV data from.
     * @param config The CSV read configuration. If {@code null}, default configuration will be used.
     */
    public CsvParser(final Reader reader, final CsvReadConfig config) {
        this.config = ObjectKit.defaultIfNull(config, CsvReadConfig::of);
        this.tokener = new CsvTokener(reader);
    }

    /**
     * Gets the header fields list. If {@code headerLineNo} in the configuration is less than 0, an exception is thrown.
     *
     * @return The list of header fields.
     * @throws IllegalStateException If header parsing is disabled or {@code nextRow()} has not been called yet.
     */
    public List<String> getHeader() {
        if (config.headerLineNo < 0) {
            throw new IllegalStateException("No header available - header parsing is disabled");
        }
        if (lineNo < config.beginLineNo) {
            throw new IllegalStateException("No header available - call nextRow() first");
        }
        return header.getRaw();
    }

    @Override
    protected CsvRow computeNext() {
        return nextRow();
    }

    /**
     * Reads the next row of data from the CSV. This method handles skipping empty rows, checking field counts, and
     * initializing the header row based on the configuration.
     *
     * @return The next {@link CsvRow}, or {@code null} if the end of the stream has been reached.
     * @throws InternalException If an I/O error occurs during reading or if field count consistency check fails.
     */
    public CsvRow nextRow() throws InternalException {
        List<String> currentFields;
        int fieldCount;
        while (!finished) {
            currentFields = readLine();
            fieldCount = currentFields.size();
            if (fieldCount < 1) {
                // An empty list indicates the end of reading.
                break;
            }

            // Check read range
            if (lineNo < config.beginLineNo) {
                // Not yet reached the starting line for reading, continue.
                continue;
            }
            if (lineNo > config.endLineNo) {
                // Exceeded the end line, reading finished.
                break;
            }

            // Skip empty rows
            if (config.skipEmptyRows && fieldCount == 1 && currentFields.get(0).isEmpty()) {
                // [""] represents an empty row.
                continue;
            }

            // Check if the number of fields in each row is consistent.
            if (config.errorOnDifferentFieldCount) {
                if (firstLineFieldCount < 0) {
                    firstLineFieldCount = fieldCount;
                } else if (fieldCount != firstLineFieldCount) {
                    throw new InternalException(String.format(
                            "Line %d has %d fields, but first line has %d fields",
                            lineNo,
                            fieldCount,
                            firstLineFieldCount));
                }
            }

            // Record the maximum number of fields.
            if (fieldCount > maxFieldCount) {
                maxFieldCount = fieldCount;
            }

            // Initialize header
            if (lineNo == config.headerLineNo && null == header) {
                initHeader(currentFields);
                // After being used as a header row, this row is skipped, and the next row becomes the first data row.
                continue;
            }

            return new CsvRow(lineNo, null == header ? null : header.headerMap, currentFields);
        }

        return null;
    }

    /**
     * Initializes the current line as the header row.
     *
     * @param currentFields The list of fields in the current line.
     */
    private void initHeader(final List<String> currentFields) {
        final Map<String, Integer> localHeaderMap = new LinkedHashMap<>(currentFields.size());
        for (int i = 0; i < currentFields.size(); i++) {
            String field = currentFields.get(i);
            if (MapKit.isNotEmpty(this.config.headerAlias)) {
                // Custom alias processing
                field = ObjectKit.defaultIfNull(this.config.headerAlias.get(field), field);
            }
            if (StringKit.isNotEmpty(field) && !localHeaderMap.containsKey(field)) {
                localHeaderMap.put(field, i);
            }
        }

        header = new CsvRow(this.lineNo, Collections.unmodifiableMap(localHeaderMap),
                Collections.unmodifiableList(currentFields));
    }

    /**
     * Reads a single line of data from the CSV. If the end of the stream is reached, an empty list is returned. An
     * empty line is represented by a list with one empty string element (e.g., {@code [""]}).
     *
     * <p>
     * Line numbers account for comment lines and newlines within quoted content.
     * </p>
     *
     * @return A list of strings representing the fields in the line.
     * @throws InternalException If an I/O error occurs.
     */
    private List<String> readLine() throws InternalException {
        // Correct the line number
        // When a line contains multiple lines of data (due to quoted newlines), the line number of the first line is
        // recorded.
        // However, when reading the next line, the number of lines within the multi-line content must be added.
        if (inQuotesLineCount > 0) {
            this.lineNo += this.inQuotesLineCount;
            this.inQuotesLineCount = 0;
        }

        final List<String> currentFields = new ArrayList<>(maxFieldCount > 0 ? maxFieldCount : DEFAULT_ROW_CAPACITY);

        final StringBuilder currentField = this.currentField;
        int preChar = this.preChar; // Previous special delimiter character
        boolean inComment = false;

        int c;
        while (true) {
            c = tokener.next();
            if (c < 0) {
                if (!currentField.isEmpty() || preChar == config.fieldSeparator) {
                    if (this.inQuotes) {
                        // Unclosed text delimiter, append delimiter at the end to close it or denote issue.
                        currentField.append(config.textDelimiter);
                    }

                    // Treat the remaining part as a field
                    addField(currentFields, currentField.toString());
                    currentField.setLength(0);
                }
                // End of reading
                this.finished = true;
                break;
            }

            // Comment line marker
            if (preChar < 0 || preChar == Symbol.C_CR || preChar == Symbol.C_LF) {
                // Determine if a comment starts with the specified comment character at the beginning of a line.
                // The beginning of a line has two cases:
                // 1. preChar < 0 indicates the very beginning of the stream.
                // 2. A newline character is immediately followed by the beginning of the next line.
                // Note: If the comment character appears within a text delimiter (quoted), it is treated as a normal
                // character.
                if (!inQuotes && null != this.config.commentCharacter && c == this.config.commentCharacter) {
                    inComment = true;
                }
            }
            // Comment line handling
            if (inComment) {
                if (c == Symbol.C_CR || c == Symbol.C_LF) {
                    // Comment line ends with a newline character.
                    lineNo++;
                    inComment = false;
                }
                // Skip any characters within the comment line.
                continue;
            }

            if (inQuotes) {
                // Inside quotes, treat as content until quotes end.
                if (c == config.textDelimiter) {
                    // Encountered a text delimiter (potentially an escaped one or end of quotes)
                    final int next = tokener.next();
                    if (next != config.textDelimiter) {
                        // Not a double delimiter, so it marks the end of quoting
                        inQuotes = false;
                        tokener.back();
                    }
                    // Else: It was a double delimiter (escaped delimiter).
                    // https://datatracker.ietf.org/doc/html/rfc4180#section-2
                    // We skip the escape character (handled by the next() call above) and fall through to append the
                    // single delimiter.
                } else {
                    // Newline within field content.
                    if (isLineEnd(c, preChar)) {
                        inQuotesLineCount++;
                    }
                }
                // Normal field character (or the unescaped delimiter)
                currentField.append((char) c);
            } else {
                // Not inside quotes
                if (c == config.fieldSeparator) {
                    // End of a field
                    addField(currentFields, currentField.toString());
                    currentField.setLength(0);
                } else if (c == config.textDelimiter && isFieldBegin(preChar)) {
                    // Quote starts and appears at the beginning of a field.
                    inQuotes = true;
                    currentField.append((char) c);
                } else if (c == Symbol.C_CR) {
                    // \r
                    addField(currentFields, currentField.toString());
                    currentField.setLength(0);
                    preChar = c;
                    break;
                } else if (c == Symbol.C_LF) {
                    // \n
                    if (preChar != Symbol.C_CR) {
                        addField(currentFields, currentField.toString());
                        currentField.setLength(0);
                        preChar = c;
                        break;
                    }
                    // If the previous character was \r, this field (line end) has already been processed, so skip it.
                } else {
                    currentField.append((char) c);
                }
            }

            preChar = c;
        }

        // restore fields
        this.preChar = preChar;

        lineNo++;
        return currentFields;
    }

    @Override
    public void close() throws IOException {
        tokener.close();
    }

    /**
     * Adds a field to the list of current fields, automatically removing delimiters and unescaping.
     *
     * @param currentFields The list of current fields (representing a row).
     * @param field         The field string to add.
     */
    private void addField(final List<String> currentFields, String field) {
        final char textDelimiter = this.config.textDelimiter;

        // Ignore newline characters after redundant quotes (trim suffix).
        field = StringKit.trim(field, StringTrimer.TrimMode.SUFFIX, (c -> c == Symbol.C_LF || c == Symbol.C_CR));

        // If wrapped in text delimiters, remove them.
        if (StringKit.isWrap(field, textDelimiter)) {
            field = StringKit.sub(field, 1, field.length() - 1);
        }
        if (this.config.trimField) {
            field = StringKit.trim(field);
        }
        currentFields.add(field);
    }

    /**
     * Checks if the given character and previous character form a line end.
     *
     * @param c       The current character.
     * @param preChar The previous character.
     * @return {@code true} if it's a line end, {@code false} otherwise.
     */
    private boolean isLineEnd(final int c, final int preChar) {
        return (c == Symbol.C_CR || c == Symbol.C_LF) && preChar != Symbol.C_CR;
    }

    /**
     * Determines if the current position is the beginning of a field based on the previous character. This includes
     * several scenarios:
     * <ul>
     * <li>Beginning of the document (no previous character).</li>
     * <li>After a field separator (end of the previous field).</li>
     * <li>After a newline character (start of a new line).</li>
     * </ul>
     *
     * @param preChar The previous character.
     * @return {@code true} if it's the beginning of a field, {@code false} otherwise.
     */
    private boolean isFieldBegin(final int preChar) {
        return preChar == -1 || preChar == config.fieldSeparator || preChar == Symbol.C_LF || preChar == Symbol.C_CR;
    }

}
