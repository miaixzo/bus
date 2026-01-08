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
package org.miaixz.bus.core.text.dfa;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.text.CharsBacker;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.core.xyz.ThreadKit;

/**
 * Utility class for sensitive word filtering based on the DFA (Deterministic Finite Automaton) algorithm. This class
 * provides methods to initialize a sensitive word dictionary and perform sensitive word detection and filtering.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Sensitive {

    /**
     * The default separator used for sensitive words in a string, typically a comma.
     */
    public static final String DEFAULT_SEPARATOR = Symbol.COMMA;
    /**
     * The underlying {@link WordTree} instance used to store and search for sensitive words.
     */
    private static final WordTree sensitiveTree = new WordTree();

    /**
     * Checks if the sensitive word tree has been initialized with sensitive words.
     *
     * @return {@code true} if the sensitive word tree is not empty, {@code false} otherwise.
     */
    public static boolean isInitialized() {
        return !sensitiveTree.isEmpty();
    }

    /**
     * Initializes the sensitive word tree with a collection of sensitive words. The initialization can be performed
     * asynchronously.
     *
     * @param sensitiveWords The collection of sensitive words to add.
     * @param isAsync        {@code true} to perform initialization asynchronously, {@code false} for synchronous
     *                       initialization.
     */
    public static void init(final Collection<String> sensitiveWords, final boolean isAsync) {
        if (isAsync) {
            ThreadKit.execAsync(() -> {
                init(sensitiveWords);
                return true;
            });
        } else {
            init(sensitiveWords);
        }
    }

    /**
     * Initializes the sensitive word tree with a collection of sensitive words synchronously.
     *
     * @param sensitiveWords The collection of sensitive words to add.
     */
    public static void init(final Collection<String> sensitiveWords) {
        sensitiveTree.clear();
        sensitiveTree.addWords(sensitiveWords);
    }

    /**
     * Initializes the sensitive word tree from a string of sensitive words, using a specified separator. The
     * initialization can be performed asynchronously.
     *
     * @param sensitiveWords The string containing sensitive words, separated by the given separator.
     * @param separator      The separator character(s) used to split the sensitive words string.
     * @param isAsync        {@code true} to perform initialization asynchronously, {@code false} for synchronous
     *                       initialization.
     */
    public static void init(final String sensitiveWords, final String separator, final boolean isAsync) {
        if (StringKit.isNotBlank(sensitiveWords)) {
            init(CharsBacker.split(sensitiveWords, separator), isAsync);
        }
    }

    /**
     * Initializes the sensitive word tree from a string of sensitive words, using the {@link #DEFAULT_SEPARATOR}
     * (comma). The initialization can be performed asynchronously.
     *
     * @param sensitiveWords The string containing sensitive words, separated by commas.
     * @param isAsync        {@code true} to perform initialization asynchronously, {@code false} for synchronous
     *                       initialization.
     */
    public static void init(final String sensitiveWords, final boolean isAsync) {
        init(sensitiveWords, DEFAULT_SEPARATOR, isAsync);
    }

    /**
     * Sets the character filtering rule for the underlying {@link WordTree}. Characters for which the predicate returns
     * {@code false} will be ignored during sensitive word matching.
     *
     * @param charFilter The filtering function. If {@code charFilter.test(char)} returns {@code false}, the character
     *                   is skipped.
     */
    public static void setCharFilter(final Predicate<Character> charFilter) {
        if (charFilter != null) {
            sensitiveTree.setCharFilter(charFilter);
        }
    }

    /**
     * Checks if the given text contains any sensitive words.
     *
     * @param text The text to check.
     * @return {@code true} if the text contains any sensitive word, {@code false} otherwise.
     */
    public static boolean containsSensitive(final String text) {
        return sensitiveTree.isMatch(text);
    }

    /**
     * Finds the first sensitive word in the given text.
     *
     * @param text The text to search within.
     * @return A {@link FoundWord} object representing the first sensitive word found, or {@code null} if no sensitive
     *         word is found.
     */
    public static FoundWord getFoundFirstSensitive(final String text) {
        return sensitiveTree.matchWord(text);
    }

    /**
     * Finds all sensitive words in the given text.
     *
     * @param text The text to search within.
     * @return A list of {@link FoundWord} objects representing all sensitive words found.
     */
    public static List<FoundWord> getFoundAllSensitive(final String text) {
        return sensitiveTree.matchAllWords(text);
    }

    /**
     * Finds all sensitive words in the given text with specified matching strategies.
     * <p>
     * Dense matching principle: If keywords are "ab", "b", and text is "abab", it will match [ab, b, ab]. Greedy
     * matching (longest match) principle: If keywords are "a", "ab", the longest match will be [ab].
     *
     * @param text           The text to search within.
     * @param isDensityMatch If {@code true}, performs a dense match (finds all overlapping matches).
     * @param isGreedMatch   If {@code true}, performs a greedy match (prefers the longest possible match).
     * @return A list of {@link FoundWord} objects representing all sensitive words found.
     */
    public static List<FoundWord> getFoundAllSensitive(
            final String text,
            final boolean isDensityMatch,
            final boolean isGreedMatch) {
        return sensitiveTree.matchAllWords(text, -1, isDensityMatch, isGreedMatch);
    }

    /**
     * Filters sensitive words in the given text, replacing them with asterisks by default.
     *
     * @param text The text to filter.
     * @return The text with sensitive words filtered (replaced).
     */
    public static String sensitiveFilter(final String text) {
        return sensitiveFilter(text, true, null);
    }

    /**
     * Filters sensitive words in the given text with specified matching strategy and a custom sensitive word processor.
     *
     * @param text               The text to filter.
     * @param isGreedMatch       If {@code true}, performs a greedy match (prefers the longest possible match).
     * @param sensitiveProcessor The sensitive word processor to use for replacing sensitive words. If {@code null}, a
     *                           default processor that replaces with asterisks based on the matched word's length is
     *                           used.
     * @return The text with sensitive words filtered (replaced).
     */
    public static String sensitiveFilter(
            final String text,
            final boolean isGreedMatch,
            SensitiveProcessor sensitiveProcessor) {
        if (StringKit.isEmpty(text)) {
            return text;
        }

        // For sensitive word filtering, dense matching is not typically required.
        final List<FoundWord> foundWordList = getFoundAllSensitive(text, true, isGreedMatch);
        if (CollKit.isEmpty(foundWordList)) {
            return text;
        }
        sensitiveProcessor = sensitiveProcessor == null ? new SensitiveProcessor() {
        } : sensitiveProcessor;

        final Map<Integer, FoundWord> foundWordMap = new HashMap<>(foundWordList.size(), 1);
        foundWordList.forEach(foundWord -> foundWordMap.put(foundWord.getBeginIndex(), foundWord));
        final int length = text.length();
        final StringBuilder textStringBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            final FoundWord fw = foundWordMap.get(i);
            if (fw != null) {
                textStringBuilder.append(sensitiveProcessor.process(fw));
                i = fw.getEndIndex();
            } else {
                textStringBuilder.append(text.charAt(i));
            }
        }
        return textStringBuilder.toString();
    }

}
