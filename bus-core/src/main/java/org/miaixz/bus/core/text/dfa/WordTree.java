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

import java.io.Serial;
import java.util.*;
import java.util.function.Predicate;

import org.miaixz.bus.core.center.stream.EasyStream;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.SetKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * A DFA (Deterministic Finite Automaton) word tree, commonly used to quickly find occurrences of a set of keywords
 * within a large body of text.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class WordTree extends HashMap<Character, WordTree> {

    @Serial
    private static final long serialVersionUID = 2852235672251L;

    /**
     * A set of characters that mark the end of a word at this node.
     */
    private Set<Character> endCharacterSet = null;
    /**
     * A filter for characters, allowing certain characters to be ignored during matching.
     */
    private Predicate<Character> charFilter = StopChar::isNotStopChar;

    /**
     * Default constructor.
     */
    public WordTree() {
    }

    /**
     * Constructor with a specified initial capacity.
     *
     * @param initialCapacity The initial capacity, typically the number of keywords.
     */
    public WordTree(final int initialCapacity) {
        super((int) (initialCapacity / Normal.DEFAULT_LOAD_FACTOR) + 1);
    }

    /**
     * Creates a {@code WordTree} from a predefined set of keywords.
     *
     * @param words The initial keywords.
     * @return A new {@code WordTree} instance.
     */
    public static WordTree of(final String... words) {
        final WordTree wordTree = new WordTree(words.length);
        for (final String word : words) {
            wordTree.addWord(word);
        }
        return wordTree;
    }

    /**
     * Sets a character filter to ignore certain characters during matching.
     *
     * @param charFilter The filter function.
     * @return this instance for chaining.
     */
    public WordTree setCharFilter(final Predicate<Character> charFilter) {
        this.charFilter = charFilter;
        return this;
    }

    /**
     * Adds a collection of words to the tree.
     *
     * @param words The collection of words.
     * @return this instance for chaining.
     */
    public WordTree addWords(Collection<String> words) {
        if (!(words instanceof Set)) {
            words = new HashSet<>(words);
        }
        for (final String word : words) {
            addWord(word);
        }
        return this;
    }

    /**
     * Adds an array of words to the tree.
     *
     * @param words The array of words.
     * @return this instance for chaining.
     */
    public WordTree addWords(final String... words) {
        for (final String word : SetKit.of(words)) {
            addWord(word);
        }
        return this;
    }

    /**
     * Adds a single word to the tree.
     *
     * @param word The word to add.
     * @return this instance for chaining.
     */
    public WordTree addWord(final String word) {
        if (null == word) {
            return this;
        }
        final Predicate<Character> charFilter = this.charFilter;
        WordTree parent = null;
        WordTree current = this;
        WordTree child;
        Character lastAcceptedChar = null;
        for (final char c : word.toCharArray()) {
            if (charFilter.test(c)) {
                child = current.computeIfAbsent(c, character -> new WordTree(1));
                parent = current;
                current = child;
                lastAcceptedChar = c;
            }
        }
        if (null != parent) {
            parent.setEnd(lastAcceptedChar);
        }
        return this;
    }

    /**
     * Checks if the given text contains any of the keywords in the tree.
     *
     * @param text The text to be checked.
     * @return {@code true} if a match is found.
     */
    public boolean isMatch(final String text) {
        return null != matchWord(text);
    }

    /**
     * Finds the first occurrence of a keyword in the text.
     *
     * @param text The text to be checked.
     * @return The matched keyword, or null if no match is found.
     */
    public String match(final String text) {
        final FoundWord foundWord = matchWord(text);
        return null != foundWord ? foundWord.toString() : null;
    }

    /**
     * Finds the first occurrence of a keyword and returns it as a {@link FoundWord} object, which includes the start
     * and end indices.
     *
     * @param text The text to be checked.
     * @return The {@link FoundWord} object, or null if no match is found.
     */
    public FoundWord matchWord(final String text) {
        if (null == text) {
            return null;
        }
        final List<FoundWord> matchAll = matchAllWords(text, 1);
        return CollKit.get(matchAll, 0);
    }

    /**
     * Finds all occurrences of keywords in the text.
     *
     * @param text The text to be checked.
     * @return A list of matched keywords.
     */
    public List<String> matchAll(final String text) {
        return matchAll(text, -1);
    }

    /**
     * Finds all occurrences of keywords in the text.
     *
     * @param text The text to be checked.
     * @return A list of {@link FoundWord} objects.
     */
    public List<FoundWord> matchAllWords(final String text) {
        return matchAllWords(text, -1);
    }

    /**
     * Finds all occurrences of keywords in the text, up to a specified limit.
     *
     * @param text  The text to be checked.
     * @param limit The maximum number of matches to find. If less than or equal to 0, all matches are returned.
     * @return A list of matched keywords.
     */
    public List<String> matchAll(final String text, final int limit) {
        return matchAll(text, limit, false, false);
    }

    /**
     * Finds all occurrences of keywords in the text, up to a specified limit.
     *
     * @param text  The text to be checked.
     * @param limit The maximum number of matches to find.
     * @return A list of {@link FoundWord} objects.
     */
    public List<FoundWord> matchAllWords(final String text, final int limit) {
        return matchAllWords(text, limit, false, false);
    }

    /**
     * Finds all matching keywords in the text with configurable matching strategies.
     *
     * @param text           The text to be checked.
     * @param limit          The maximum number of matches to find.
     * @param isDensityMatch If {@code true}, performs a dense match (e.g., for "abab" and keywords "ab", "b", finds
     *                       [ab, b, ab]).
     * @param isGreedMatch   If {@code true}, performs a greedy (longest) match (e.g., for "ab" and keywords "a", "ab",
     *                       finds [ab]).
     * @return A list of matched keywords.
     */
    public List<String> matchAll(
            final String text,
            final int limit,
            final boolean isDensityMatch,
            final boolean isGreedMatch) {
        final List<FoundWord> matchAllWords = matchAllWords(text, limit, isDensityMatch, isGreedMatch);
        return CollKit.map(matchAllWords, FoundWord::toString);
    }

    /**
     * Finds all matching keywords in the text with configurable matching strategies.
     *
     * @param text           The text to be checked.
     * @param limit          The maximum number of matches to find.
     * @param isDensityMatch If {@code true}, performs a dense match.
     * @param isGreedMatch   If {@code true}, performs a greedy (longest) match.
     * @return A list of {@link FoundWord} objects.
     */
    public List<FoundWord> matchAllWords(
            final String text,
            final int limit,
            final boolean isDensityMatch,
            final boolean isGreedMatch) {
        if (null == text) {
            return null;
        }

        final List<FoundWord> foundWords = limit > 0 ? new ArrayList<>(limit) : new ArrayList<>();
        WordTree current;
        final int length = text.length();
        final Predicate<Character> charFilter = this.charFilter;
        final StringBuilder wordBuffer = StringKit.builder();
        final StringBuilder keyBuffer = StringKit.builder();
        char currentChar;
        for (int i = 0; i < length; i++) {
            current = this;
            wordBuffer.setLength(0);
            keyBuffer.setLength(0);

            FoundWord currentFoundWord = null;
            for (int j = i; j < length; j++) {
                currentChar = text.charAt(j);
                if (!charFilter.test(currentChar)) {
                    if (!wordBuffer.isEmpty()) {
                        wordBuffer.append(currentChar);
                    } else {
                        i++;
                    }
                    continue;
                } else if (!current.containsKey(currentChar)) {
                    break;
                }
                wordBuffer.append(currentChar);
                keyBuffer.append(currentChar);
                if (current.isEnd(currentChar)) {
                    currentFoundWord = new FoundWord(keyBuffer.toString(), wordBuffer.toString(), i, j);
                    if (!isDensityMatch) {
                        i = j;
                    }
                    if (!isGreedMatch) {
                        break;
                    }
                }
                current = current.get(currentChar);
            }

            if (null != currentFoundWord) {
                foundWords.add(currentFoundWord);
                if (limit > 0 && foundWords.size() >= limit) {
                    return foundWords;
                }
            }
        }
        return foundWords;
    }

    /**
     * Flattens the WordTree into a list of all contained keywords.
     *
     * @return A list of keywords.
     */
    public List<String> flatten() {
        return EasyStream.of(this.entrySet()).flat(this::innerFlatten).toList();
    }

    /**
     * Recursively flattens an entry of the WordTree.
     *
     * @param entry The entry to flatten.
     * @return An iterable of flattened strings.
     */
    private Iterable<String> innerFlatten(Entry<Character, WordTree> entry) {
        List<String> list = EasyStream.of(entry.getValue().entrySet()).flat(this::innerFlatten)
                .map(v -> entry.getKey() + v).toList();
        if (list.isEmpty()) {
            return EasyStream.of(String.valueOf(entry.getKey()));
        }
        return list;
    }

    /**
     * Checks if a character marks the end of a word at this node.
     *
     * @param c The character to check.
     * @return {@code true} if it's an end character.
     */
    private boolean isEnd(final char c) {
        return null != endCharacterSet && this.endCharacterSet.contains(c);
    }

    /**
     * Marks a character as the end of a word at this node.
     *
     * @param c The character to mark as an end.
     */
    private void setEnd(final char c) {
        if (null == endCharacterSet) {
            endCharacterSet = new HashSet<>(2);
        }
        this.endCharacterSet.add(c);
    }

    /**
     * Clears all words from the tree.
     */
    @Override
    public void clear() {
        super.clear();
        if (null != endCharacterSet) {
            this.endCharacterSet.clear();
        }
    }

}
