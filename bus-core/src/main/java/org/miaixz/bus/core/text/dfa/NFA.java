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
package org.miaixz.bus.core.text.dfa;

import java.util.*;

/**
 * A multi-pattern matching tool based on the Nondeterministic Finite Automaton (NFA) model. This implementation uses an
 * Aho-Corasick automaton for efficient searching of multiple keywords in a text.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class NFA {

    /**
     * The root node of the Aho-Corasick tree.
     */
    private final Node root;
    /**
     * A lock to prevent concurrent builds of the Aho-Corasick automaton, which could lead to unpredictable results.
     */
    private final Object buildAcLock;
    /**
     * A lock to prevent concurrent insertions, which could cause new nodes to be modified before they are properly
     * linked into the tree.
     */
    private final Object insertTreeLock;
    /**
     * A flag indicating whether the Aho-Corasick automaton needs to be rebuilt. This is set to true when new words are
     * inserted.
     */
    private volatile boolean needBuildAc;

    /**
     * Default constructor. Initializes an empty NFA.
     */
    public NFA() {
        this.root = new Node();
        this.needBuildAc = true;
        this.buildAcLock = new Object();
        this.insertTreeLock = new Object();
    }

    /**
     * Constructs an NFA and initializes it with a collection of words.
     *
     * @param words The words to add to the dictionary.
     */
    public NFA(final String... words) {
        this();
        this.insert(words);
    }

    /**
     * Inserts a new word into the dictionary (Trie).
     *
     * @param word The word to add.
     */
    public void insert(final String word) {
        synchronized (insertTreeLock) {
            needBuildAc = true;
            Node p = root;
            for (final char curr : word.toCharArray()) {
                p.next.computeIfAbsent((int) curr, k -> new Node());
                p = p.next.get((int) curr);
            }
            p.flag = true;
            p.text = word;
        }
    }

    /**
     * Inserts multiple words into the dictionary.
     *
     * @param words The words to add.
     */
    public void insert(final String... words) {
        for (final String word : words) {
            this.insert(word);
        }
    }

    /**
     * Builds the Aho-Corasick automaton by creating failure links. This method optimizes the trie for efficient
     * multi-pattern searching.
     */
    private void buildAc() {
        final Queue<Node> queue = new LinkedList<>();
        final Node p = root;
        for (final Integer key : p.next.keySet()) {
            p.next.get(key).fail = root;
            queue.offer(p.next.get(key));
        }
        while (!queue.isEmpty()) {
            final Node curr = queue.poll();
            for (final Integer key : curr.next.keySet()) {
                Node fail = curr.fail;
                // Find the failure link for the current node's child.
                while (fail != null && fail.next.get(key) == null) {
                    fail = fail.fail;
                }
                // If a failure link is found, set it. Otherwise, point to the root.
                if (fail != null) {
                    fail = fail.next.get(key);
                } else {
                    fail = root;
                }
                curr.next.get(key).fail = fail;
                queue.offer(curr.next.get(key));
            }
        }
        needBuildAc = false;
    }

    /**
     * Finds all occurrences of the keywords in the given text. This method performs a dense match, finding all possible
     * matches.
     *
     * @param text The text to search within.
     * @return A list of {@link FoundWord} objects representing the found keywords.
     */
    public List<FoundWord> find(final String text) {
        return this.find(text, true);
    }

    /**
     * Finds all occurrences of the keywords in the given text.
     *
     * @param text           The text to search within.
     * @param isDensityMatch If true, performs a dense match (finding all overlapping matches). If false, performs a
     *                       sparse match (resets search after a match is found).
     * @return A list of {@link FoundWord} objects representing the found keywords.
     */
    public List<FoundWord> find(final String text, final boolean isDensityMatch) {
        // Double-checked locking to prevent unnecessary builds.
        if (needBuildAc) {
            synchronized (buildAcLock) {
                if (needBuildAc) {
                    this.buildAc();
                }
            }
        }
        final List<FoundWord> ans = new ArrayList<>();
        Node p = root, k;
        for (int i = 0, len = text.length(); i < len; i++) {
            final int ind = text.charAt(i);
            // State transition (following failure links, which distinguishes this from a DFA).
            while (p != null && p.next.get(ind) == null) {
                p = p.fail;
            }
            if (p == null) {
                p = root;
            } else {
                p = p.next.get(ind);
            }
            // Extract results (following failure links to find all matching patterns ending at this position).
            k = p;
            while (k != null) {
                if (k.flag) {
                    ans.add(new FoundWord(k.text, k.text, i - k.text.length() + 1, i));
                    if (!isDensityMatch) {
                        p = root;
                        break;
                    }
                }
                k = k.fail;
            }
        }
        return ans;
    }

    /**
     * Represents a node in the Aho-Corasick tree.
     */
    private static class Node {

        /**
         * If true, this node marks the end of a complete word.
         */
        boolean flag;
        /**
         * The failure link, pointing to the next node to check upon a character mismatch.
         */
        Node fail;
        /**
         * The full word if this node is the end of a word.
         */
        String text;
        /**
         * The children of this node, keyed by character (as an integer).
         */
        Map<Integer, Node> next;

        /**
         * Default constructor for a new node.
         */
        public Node() {
            this.flag = false;
            next = new HashMap<>();
        }
    }

}
