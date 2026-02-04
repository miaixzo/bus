/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.text.replacer;

import java.io.Serial;
import java.util.Map;

import org.miaixz.bus.core.text.finder.MultiStringFinder;

/**
 * An efficient replacer that finds and replaces specified keywords with corresponding values. This implementation is
 * based on the Aho-Corasick automaton algorithm, which significantly improves efficiency when the original string to be
 * replaced is large and the number of key-value pairs for replacement is high.
 * <p>
 * Note: If overlapping keywords are present, the first matched keyword will be replaced. For example:
 * <ol>
 * <li>"abc", "ab" will prioritize replacing "ab".</li>
 * <li>"abed", "be" will prioritize replacing "abed".</li>
 * <li>"abc", "bc" will prioritize replacing "abc".</li>
 * </ol>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class HighMultiReplacerV2 extends StringReplacer {

    @Serial
    private static final long serialVersionUID = 2852238992618L;

    /**
     * The Aho-Corasick automaton used for efficient keyword searching and replacement.
     */
    private final AhoCorasickAutomaton ahoCorasickAutomaton;

    /**
     * Constructs a new {@code HighMultiReplacerV2} and initializes the Aho-Corasick automaton.
     *
     * @param map A map where keys are the strings to be searched for, and values are their corresponding replacement
     *            values.
     */
    public HighMultiReplacerV2(final Map<String, Object> map) {
        ahoCorasickAutomaton = new AhoCorasickAutomaton(map);
    }

    /**
     * Creates a new {@code HighMultiReplacer} instance.
     *
     * @param map A map where keys are the strings to be searched for, and values are their corresponding replacement
     *            values.
     * @return A new {@code HighMultiReplacer} instance.
     */
    public static HighMultiReplacer of(final Map<String, Object> map) {
        return new HighMultiReplacer(map);
    }

    /**
     * Performs string replacement starting from a specified position, appending the result to the output buffer. This
     * method delegates the actual replacement to the Aho-Corasick automaton.
     *
     * @param text The string to be processed.
     * @param pos  The starting position for replacement (inclusive).
     * @param out  The {@code StringBuilder} to which the replacement result is appended.
     * @return The number of characters consumed by the replacement, which is the length of the input text since the
     *         Aho-Corasick automaton processes the entire text.
     */
    @Override
    protected int replace(final CharSequence text, final int pos, final StringBuilder out) {
        ahoCorasickAutomaton.replace(text, out);
        return text.length();
    }

    /**
     * Applies the replacement rules to the given character sequence and returns the replaced string. This method
     * processes the entire input text using the Aho-Corasick automaton.
     *
     * @param text The character sequence to be processed.
     * @return The character sequence after all replacements have been applied.
     */
    @Override
    public CharSequence apply(final CharSequence text) {
        final StringBuilder builder = new StringBuilder();
        replace(text, 0, builder);
        return builder;
    }

    /**
     * Inner class implementing the Aho-Corasick automaton for efficient multi-string searching and replacement.
     */
    protected static class AhoCorasickAutomaton extends MultiStringFinder {

        /**
         * The map containing keywords to be searched for and their corresponding replacement values.
         */
        protected final Map<String, Object> replaceMap;

        /**
         * Constructs a new {@code AhoCorasickAutomaton} with the given replacement map.
         *
         * @param replaceMap A map where keys are the strings to be searched for, and values are their corresponding
         *                   replacement values.
         */
        public AhoCorasickAutomaton(final Map<String, Object> replaceMap) {
            super(replaceMap.keySet());
            this.replaceMap = replaceMap;
        }

        /**
         * Executes string replacement, replacing matched keywords with their target values.
         *
         * @param text          The string to be processed for replacement.
         * @param stringBuilder The output buffer where the replacement result is stored.
         */
        public void replace(final CharSequence text, final StringBuilder stringBuilder) {
            Node currentNode = root;
            // Temporary storage for characters that might be part of a match
            final StringBuilder temp = new StringBuilder();
            for (int i = 0; i < text.length(); i++) {
                final char ch = text.charAt(i);
                final Integer index = charIndexMap.get(ch);
                // If the next character is not in the candidate transition strings, 'ch' will definitely not be
                // replaced.
                if (index == null || index < 0) {
                    // Write data from the temporary buffer to the output StringBuilder
                    if (temp.length() > 0) {
                        stringBuilder.append(temp);
                        // Clear the temporary buffer after writing
                        temp.delete(0, temp.length());
                    }
                    // Append the character 'ch' that will not be replaced to the output
                    stringBuilder.append(ch);
                    // Reset the current node to the root as the match is broken
                    currentNode = root;
                    continue;
                }

                // This logic branch indicates that a transition to the next state has occurred.
                currentNode = currentNode.directRouter[index];

                // If the current node is the root, it means the match was interrupted. Clear the temporary buffer and
                // write to output.
                if (currentNode.nodeIndex == 0) {
                    if (temp.length() > 0) {
                        stringBuilder.append(temp);
                        // Clear the temporary buffer after writing
                        temp.delete(0, temp.length());
                        // In this case, the character exists in the candidate transition characters, but there was no
                        // path from the previous character.
                        stringBuilder.append(ch);
                        continue;
                    }
                }

                // A match is found, proceed with string replacement.
                if (currentNode.isEnd) {
                    final int length = currentNode.tagetString.length();
                    // Clear the matched characters from the temporary buffer. The last character was not yet added to
                    // temp.
                    temp.delete(temp.length() - length + 1, length - 1);
                    if (temp.length() > 0) {
                        stringBuilder.append(temp);
                    }
                    // Append the replacement string.
                    stringBuilder.append(replaceMap.get(currentNode.tagetString));
                    // Since the string was replaced, reset the current node to the root.
                    currentNode = root;
                    continue;
                }

                temp.append(ch);
            }
        }
    }

}
