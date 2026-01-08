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
package org.miaixz.bus.core.text.replacer;

import java.io.Serial;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * An efficient replacer that finds and replaces specified keywords with corresponding values. This implementation is
 * based on the Aho-Corasick automaton algorithm, which significantly improves efficiency when the original string to be
 * replaced is large and the number of key-value pairs for replacement is high.
 * <p>
 * Note: If overlapping keywords are present, the first matched keyword will be replaced. For example:
 * <ol>
 * <li>"abc", "ab" will prioritize replacing "ab".</li>
 * <li>"abed", "be" will prioritize replacing "abed".</li>
 * <li>"abc", "ciphers" will prioritize replacing "abc".</li>
 * </ol>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class HighMultiReplacer extends StringReplacer {

    @Serial
    private static final long serialVersionUID = 2852238780005L;

    /**
     * The Aho-Corasick automaton used for efficient keyword searching and replacement.
     */
    private final AhoCorasickAutomaton ahoCorasickAutomaton;

    /**
     * Constructs a new {@code HighMultiReplacer} and initializes the Aho-Corasick automaton.
     *
     * @param map A map where keys are the strings to be searched for, and values are their corresponding replacement
     *            values.
     */
    public HighMultiReplacer(final Map<String, Object> map) {
        ahoCorasickAutomaton = new AhoCorasickAutomaton(map);
    }

    /**
     * Factory method to create an instance of {@code HighMultiReplacer}.
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
    public int replace(final CharSequence text, final int pos, final StringBuilder out) {
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
     * Implements the Aho-Corasick automaton for efficient keyword searching and replacement.
     */
    private static class AhoCorasickAutomaton {

        /**
         * The root node of the Aho-Corasick automaton, which does not store any character information.
         */
        private final Node root;

        /**
         * The map containing keywords to be searched for and their corresponding replacement values.
         */
        private final Map<String, Object> target;

        /**
         * Constructs a new {@code AhoCorasickAutomaton}, initializing the Trie tree and building the fail pointers.
         *
         * @param target A map where keys are the strings to be searched for, and values are their corresponding
         *               replacement values.
         */
        public AhoCorasickAutomaton(final Map<String, Object> target) {
            root = new Node();
            this.target = target;
            buildTrieTree();
            buildAcFromTrie();
        }

        /**
         * Builds the Trie tree from the target keywords. It supports three keyword formats: plain field, ${field}, and
         * {field}.
         */
        private void buildTrieTree() {
            for (final String text : target.keySet()) {
                if (text == null) {
                    continue; // Skip null keywords
                }
                // Add direct keyword format (e.g., field)
                buildTrieTree(text, text);
                // Add ${} wrapped format (e.g., ${field})
                buildTrieTree("${" + text + "}", text);
                // Add {} wrapped format (e.g., {field})
                buildTrieTree("{" + text + "}", text);
            }
        }

        /**
         * Adds a keyword pattern to the Trie tree.
         *
         * @param pattern The pattern to match (e.g., field, ${field}, or {field}).
         * @param key     The original keyword, used to look up the replacement value (e.g., field).
         */
        private void buildTrieTree(final String pattern, final String key) {
            Node curr = root; // Initialize with the root node
            for (int i = 0; i < pattern.length(); i++) {
                final char ch = pattern.charAt(i);
                Node node = curr.children.get(ch);
                if (node == null) {
                    node = new Node();
                    curr.children.put(ch, node);
                }
                curr = node;
            }
            // Store the original keyword for subsequent replacement
            curr.text = key;
        }

        /**
         * Builds the Aho-Corasick automaton from the Trie tree by generating fail pointers, similar to the KMP
         * algorithm's next array.
         */
        private void buildAcFromTrie() {
            final LinkedList<Node> queue = new LinkedList<>();
            // Initialize children of the root node
            for (final Node x : root.children.values()) {
                x.fail = root; // The fail pointer of a child node points to the root node
                queue.addLast(x); // Enqueue
            }

            // Breadth-first traversal to build fail pointers
            while (!queue.isEmpty()) {
                final Node p = queue.removeFirst();
                for (final Map.Entry<Character, Node> entry : p.children.entrySet()) {
                    queue.addLast(entry.getValue());
                    Node failTo = p.fail;
                    while (true) {
                        if (failTo == null) {
                            entry.getValue().fail = root; // No match found, point to the root node
                            break;
                        }
                        if (failTo.children.get(entry.getKey()) != null) {
                            entry.getValue().fail = failTo.children.get(entry.getKey()); // Match found
                            break;
                        }
                        failTo = failTo.fail; // Continue backtracking upwards
                    }
                }
            }
        }

        /**
         * Executes string replacement, replacing matched keywords with their target values.
         *
         * @param text          The string to be processed for replacement.
         * @param stringBuilder The output buffer where the replacement result is stored.
         */
        public void replace(final CharSequence text, final StringBuilder stringBuilder) {
            Node curr = root;
            int i = 0;
            while (i < text.length()) {
                final char ch = text.charAt(i);
                final Node node = curr.children.get(ch);
                if (node != null) {
                    // Character matched, move to the next state
                    curr = node;
                    if (curr.isWord()) {
                        // Full keyword matched, append the replacement value
                        final Object replacement = target.get(curr.text);
                        stringBuilder.append(replacement != null ? replacement : "");
                        curr = root; // Reset the state machine
                    }
                    i++;
                } else {
                    // No match, try the fail pointer
                    if (curr != root) {
                        curr = curr.fail; // Backtrack
                    } else {
                        stringBuilder.append(ch); // No match, append the current character
                        i++;
                    }
                }
            }
        }

        /**
         * Represents a node in the Aho-Corasick automaton, corresponding to a state in the Trie tree.
         */
        private static class Node {

            /**
             * The original keyword string if this node marks the end of a keyword. Null if this node is not the end of
             * a keyword.
             */
            String text;

            /**
             * A map of child nodes, where the key is a character and the value is the corresponding child node.
             */
            Map<Character, Node> children = new HashMap<>();

            /**
             * The fail pointer, indicating the next state to transition to upon a mismatch.
             */
            Node fail;

            /**
             * Checks if the current node represents the end of a complete keyword.
             *
             * @return {@code true} if this node is the end of a keyword, {@code false} otherwise.
             */
            public boolean isWord() {
                return text != null;
            }
        }
    }

}
