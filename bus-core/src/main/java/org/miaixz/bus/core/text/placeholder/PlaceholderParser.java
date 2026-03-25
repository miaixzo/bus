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
package org.miaixz.bus.core.text.placeholder;

import java.util.Objects;
import java.util.function.UnaryOperator;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.text.CharsValidator;

/**
 * A simple placeholder parser. Given the left and right boundary symbols of a placeholder and an escape character, it
 * allows parsing and replacing placeholders in a string with specified content. It supports using the specified escape
 * character to escape boundary symbols.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class PlaceholderParser implements UnaryOperator<String> {

    /**
     * The processor function that handles the extracted placeholder expression. This function takes the content within
     * the placeholder (e.g., "name" from "${name}") and returns the replacement string.
     */
    private final UnaryOperator<String> processor;

    /**
     * The opening symbol of the placeholder (e.g., "${").
     */
    private final String open;

    /**
     * The length of the opening symbol.
     */
    private final int openLength;

    /**
     * The closing symbol of the placeholder (e.g., "}").
     */
    private final String close;

    /**
     * The length of the closing symbol.
     */
    private final int closeLength;

    /**
     * The escape character used to escape placeholder boundary symbols (e.g., '\\').
     */
    private final char escape;

    /**
     * Creates a placeholder parser with a default escape character of {@code '\\'}.
     *
     * @param processor The placeholder processor function.
     * @param prefix    The opening symbol of the placeholder, must not be empty.
     * @param suffix    The closing symbol of the placeholder, must not be empty.
     * @throws IllegalArgumentException if prefix or suffix is empty.
     * @throws NullPointerException     if processor is null.
     */
    public PlaceholderParser(final UnaryOperator<String> processor, final String prefix, final String suffix) {
        this(processor, prefix, suffix, Symbol.C_BACKSLASH);
    }

    /**
     * Creates a placeholder parser with specified opening, closing, and escape characters.
     *
     * @param processor The placeholder processor function.
     * @param prefix    The opening symbol of the placeholder, must not be empty.
     * @param suffix    The closing symbol of the placeholder, must not be empty.
     * @param escape    The escape character.
     * @throws IllegalArgumentException if prefix or suffix is empty.
     * @throws NullPointerException     if processor is null.
     */
    public PlaceholderParser(final UnaryOperator<String> processor, final String prefix, final String suffix,
            final char escape) {
        Assert.isFalse(CharsValidator.isEmpty(prefix), "Prefix symbol cannot be empty");
        Assert.isFalse(CharsValidator.isEmpty(suffix), "Suffix symbol cannot be empty");
        this.processor = Objects.requireNonNull(processor);
        this.open = prefix;
        this.openLength = prefix.length();
        this.close = suffix;
        this.closeLength = suffix.length();
        this.escape = escape;
    }

    /**
     * Parses and replaces placeholders in the given string. This method iterates through the input text, identifies
     * placeholders defined by the opening and closing symbols, handles escape characters, and replaces the placeholder
     * content using the provided processor function.
     *
     * @param text The string to be parsed.
     * @return The processed string with placeholders replaced.
     * @throws InternalException if an opening placeholder symbol is found without a corresponding closing symbol.
     */
    @Override
    public String apply(final String text) {
        if (CharsValidator.isEmpty(text)) {
            return Normal.EMPTY;
        }

        // Find the first opening symbol
        int closeCursor = 0;
        int openCursor = text.indexOf(open, closeCursor);
        if (openCursor == -1) {
            return text;
        }

        // Start matching
        final char[] src = text.toCharArray();
        final StringBuilder result = new StringBuilder(src.length);
        final StringBuilder expression = new StringBuilder();
        while (openCursor > -1) {

            // Check if the opening symbol is escaped; if so, skip and find the next opening symbol.
            if (openCursor > 0 && src[openCursor - 1] == escape) {
                result.append(src, closeCursor, openCursor - closeCursor - 1).append(open);
                closeCursor = openCursor + openLength;
                openCursor = text.indexOf(open, closeCursor);
                continue;
            }

            // Append the string between the current opening symbol and the previous closing symbol (or start of text).
            result.append(src, closeCursor, openCursor - closeCursor);

            // Reset the closing cursor to the start of the current placeholder.
            closeCursor = openCursor + openLength;

            // Find the index of the closing symbol.
            int end = text.indexOf(close, closeCursor);
            while (end > -1) {
                // If the closing symbol is escaped, append it to the expression and find the next closing symbol.
                if (end > closeCursor && src[end - 1] == escape) {
                    expression.append(src, closeCursor, end - closeCursor - 1).append(close);
                    closeCursor = end + closeLength;
                    end = text.indexOf(close, closeCursor);
                }
                // Closing symbol found.
                else {
                    expression.append(src, closeCursor, end - closeCursor);
                    break;
                }
            }

            // If no closing symbol is found, it indicates a matching error.
            if (end == -1) {
                throw new InternalException("Opening symbol at index {} in \"{}\" has no corresponding closing symbol",
                        openCursor, text);
            }
            // If a closing symbol is found, replace the string between the opening and closing symbols with the
            // processed expression.
            else {
                result.append(processor.apply(expression.toString()));
                expression.setLength(0);
                // Finish processing the current placeholder and look for the next one.
                closeCursor = end + closeLength;
            }

            // Find the next opening symbol.
            openCursor = text.indexOf(open, closeCursor);
        }

        // If there is any unprocessed string remaining after matching, append it directly to the result.
        if (closeCursor < src.length) {
            result.append(src, closeCursor, src.length - closeCursor);
        }
        return result.toString();
    }

}
