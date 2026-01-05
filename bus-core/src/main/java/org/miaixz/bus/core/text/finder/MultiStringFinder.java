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
package org.miaixz.bus.core.text.finder;

import java.util.*;

import org.miaixz.bus.core.xyz.StringKit;

/**
 * Multi-string finder. Implemented using the Aho-Corasick automaton algorithm.
 * 
 * @author Kimi Liu
 * @since Java 17+
 */
public class MultiStringFinder {

    /**
     * Character index map. Maps characters to their integer indices.
     */
    protected final Map<Character, Integer> charIndexMap = new HashMap<>();
    /**
     * Total number of unique characters.
     */
    protected final int allCharSize;
    /**
     * The root node of the Aho-Corasick automaton.
     */
    protected final Node root;
    /**
     * Total number of nodes in the automaton.
     */
    int nodeSize;

    /**
     * Constructs a multi-string finder.
     *
     * @param source The collection of strings to be searched for.
     */
    public MultiStringFinder(final Collection<String> source) {
        // Strings to be matched
        final Set<String> stringSet = new HashSet<>();

        // All unique characters
        final Set<Character> charSet = new HashSet<>();
        for (final String string : source) {
            stringSet.add(string);
            StringKit.forEach(string, charSet::add);
        }
        allCharSize = charSet.size();
        int index = 0;
        for (final Character c : charSet) {
            charIndexMap.put(c, index);
            index++;
        }
        this.root = Node.createRoot(index);

        buildPrefixTree(stringSet);
        buildFail();
    }

    /**
     * Creates a multi-string finder.
     *
     * @param source The collection of strings to be searched for.
     * @return A new {@code MultiStringFinder} instance.
     */
    public static MultiStringFinder of(final Collection<String> source) {
        return new MultiStringFinder(source);
    }

    /**
     * Builds the prefix tree (Trie) from the given set of strings.
     *
     * @param stringSst The set of strings to build the prefix tree from.
     */
    protected void buildPrefixTree(final Collection<String> stringSst) {
        // Node numbering. The root node is already 0, so numbering starts from 1.
        int nodeIndex = 1;
        for (final String string : stringSst) {
            Node node = root;
            for (final char c : string.toCharArray()) {
                final boolean addValue = node.addValue(c, nodeIndex, charIndexMap);
                if (addValue) {
                    nodeIndex++;
                }
                node = node.directRouter[getIndex(c)];
            }
            node.setEnd(string);
        }
        nodeSize = nodeIndex;
    }

    /**
     * Builds the failure links (fail pointers) for the Aho-Corasick automaton. This process also builds the direct
     * routing table to reduce the number of fail jumps.
     */
    protected void buildFail() {
        final LinkedList<Node> nodeQueue = new LinkedList<>();
        for (int i = 0; i < root.directRouter.length; i++) {
            final Node nextNode = root.directRouter[i];
            if (nextNode == null) {
                root.directRouter[i] = root;
                continue;
            }
            nextNode.fail = root;
            nodeQueue.addLast(nextNode);
        }

        // Perform a breadth-first traversal
        while (!nodeQueue.isEmpty()) {
            final Node parent = nodeQueue.removeFirst();
            // Since charIndex is used to map characters to indices, 'i' can be directly considered the corresponding
            // character.
            for (int i = 0; i < parent.directRouter.length; i++) {
                final Node child = parent.directRouter[i];
                // If child is null, it means there is no child node.
                if (child == null) {
                    parent.directRouter[i] = parent.fail.directRouter[i];
                    continue;
                }
                child.fail = parent.fail.directRouter[i];
                nodeQueue.addLast(child);
                child.fail.failPre.add(child);
            }
        }
    }

    /**
     * Searches for all occurrences of the predefined strings within the given text.
     *
     * @param text The text to search within.
     * @return A map where keys are the matched strings and values are lists of their starting indices in the text.
     */
    public Map<String, List<Integer>> findMatch(final String text) {
        // Node traversal count. Declared inside the method to allow multiple matches for a single built object.
        final HashMap<String, List<Integer>> resultMap = new HashMap<>();

        final char[] chars = text.toCharArray();
        Node currentNode = root;
        for (int i = 0; i < chars.length; i++) {
            final char c = chars[i];
            final Integer index = charIndexMap.get(c);
            // If the character index is not found, it is assumed not to be part of any matching string. Restart from
            // the root node.
            if (index == null) {
                currentNode = root;
                continue;
            }
            // Move to the next node, which could be a normal transition or a transition via a fail link.
            currentNode = currentNode.directRouter[index];
            // If it's an end node, it means a complete string has been matched. Add the match to the result.
            if (currentNode.isEnd) {
                resultMap.computeIfAbsent(currentNode.tagetString, k -> new ArrayList<>())
                        .add(i - currentNode.tagetString.length() + 1);
            }

        }

        return resultMap;
    }

    /**
     * Gets the index of a character.
     *
     * @param c The character.
     * @return The index of the character, or -1 if not found.
     */
    protected int getIndex(final char c) {
        final Integer i = charIndexMap.get(c);
        if (i == null) {
            return -1;
        }
        return i;
    }

    /**
     * Represents a node in the Aho-Corasick automaton.
     */
    protected static class Node {

        /**
         * Indicates whether this node is the end of a matched string.
         */
        public boolean isEnd = false;

        /**
         * If this node is an end node, this field stores the matched string. Otherwise, it is null.
         */
        public String tagetString;

        /**
         * The failure link (fail pointer) for this node.
         */
        public Node fail;

        /**
         * Direct routing table. This table reduces the fail process by directly jumping to the next node. It uses an
         * array + charIndex to potentially reduce hash complexity and memory usage. When the number of initial strings
         * in {@code stringSet} is large, and there are many characters, this can somewhat reduce the memory overhead
         * caused by HashMap's underlying implementation. The size of {@code directRouter} is equal to the total number
         * of unique characters.
         */
        public Node[] directRouter;

        /**
         * The index of this node. The root node has an index of 0.
         */
        public int nodeIndex;

        /**
         * The character value represented by this node.
         */
        public char value;

        /**
         * List of nodes that point to this node via their fail links.
         */
        public List<Node> failPre = new ArrayList<>();

        /**
         * Default constructor for a Node.
         */
        public Node() {
        }

        /**
         * Creates and initializes the root node.
         *
         * @param allCharSize The total number of unique characters.
         * @return The root {@code Node}.
         */
        public static Node createRoot(final int allCharSize) {
            final Node node = new Node();
            node.nodeIndex = 0;
            node.fail = node;
            node.directRouter = new Node[allCharSize];
            return node;
        }

        /**
         * Adds a child node for the given character.
         *
         * @param c         The character for the new child node.
         * @param nodeIndex The index to assign to the new node.
         * @param charIndex A map from characters to their indices.
         * @return {@code false} if a child node for the character already exists, {@code true} if a new child node was
         *         added.
         */
        public boolean addValue(final char c, final int nodeIndex, final Map<Character, Integer> charIndex) {
            final Integer index = charIndex.get(c);
            Node node = directRouter[index];
            if (node != null) {
                return false;
            }
            node = new Node();
            directRouter[index] = node;
            node.nodeIndex = nodeIndex;
            node.directRouter = new Node[directRouter.length];
            node.value = c;
            return true;
        }

        /**
         * Marks the current node as an end node for a matched string.
         *
         * @param string The string that ends at this node.
         */
        public void setEnd(final String string) {
            tagetString = string;
            isEnd = true;
        }

        /**
         * Gets the next node based on the given character.
         *
         * @param c         The character to transition on.
         * @param charIndex A map from characters to their indices.
         * @return The next {@code Node}, or {@code null} if no transition exists for the character.
         */
        public Node getNext(final char c, final Map<Character, Integer> charIndex) {
            final Integer index = charIndex.get(c);
            if (index == null) {
                return null;
            }
            return directRouter[index];
        }

        /**
         * Returns the string representation of this object.
         *
         * @return the string representation
         */
        @Override
        public String toString() {
            return value + ":" + nodeIndex;
        }
    }

}
