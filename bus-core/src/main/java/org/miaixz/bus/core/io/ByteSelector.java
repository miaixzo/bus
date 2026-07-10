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
package org.miaixz.bus.core.io;

import java.util.*;

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.source.BufferSource;

/**
 * A selector that stores byte string candidates for fast {@link BufferSource#select} matching.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class ByteSelector extends AbstractList<ByteString> implements RandomAccess {

    /**
     * The candidate byte strings used by this selector.
     */
    public final ByteString[] candidates;

    /**
     * The trie structure for efficient lookup.
     */
    public final int[] trie;

    /**
     * Private constructor for {@code ByteSelector}.
     *
     * @param candidates The candidate {@link ByteString} values.
     * @param trie       The trie structure as an array of integers.
     */
    private ByteSelector(ByteString[] candidates, int[] trie) {
        this.candidates = candidates;
        this.trie = trie;
    }

    /**
     * Creates a trie-backed selector from the supplied byte string candidates. Returned indexes always refer to the
     * caller's original option order, even though the trie is built from a sorted copy.
     *
     * @param candidates The {@link ByteString} candidates to match.
     * @return A new {@link ByteSelector} instance.
     * @throws IllegalArgumentException if the option array is null, any option is null, an empty option is provided, or
     *                                  duplicate candidates exist.
     */
    public static ByteSelector of(ByteString... candidates) {
        if (candidates == null) {
            throw new IllegalArgumentException("candidates == null");
        }
        if (candidates.length == 0) {
            // No candidates, we must always return -1. Create an empty selector.
            return new ByteSelector(new ByteString[0], new int[] { 0, -1 });
        }
        for (int i = 0; i < candidates.length; i++) {
            if (candidates[i] == null) {
                throw new IllegalArgumentException("candidates[" + i + "] == null");
            }
        }

        // Sort the byte strings needed for recursive construction. Map the sorted indices to the caller's indices.
        List<ByteString> list = new ArrayList<>(Arrays.asList(candidates));
        Collections.sort(list);
        List<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            indexes.add(-1);
        }
        for (int i = 0; i < list.size(); i++) {
            int sortedIndex = Collections.binarySearch(list, candidates[i]);
            indexes.set(sortedIndex, i);
        }
        if (list.get(0).size() == 0) {
            throw new IllegalArgumentException("the empty byte string is not a supported option");
        }
        // Remove elements that will never be returned because they are prefixes of other elements.
        // For example, if the caller provides ["abc", "abcde"], we will never return "abcde" because we return
        // "abc" as soon as we encounter it.
        for (int a = 0; a < list.size(); a++) {
            ByteString prefix = list.get(a);
            for (int b = a + 1; b < list.size();) {
                ByteString byteString = list.get(b);
                if (!byteString.startsWith(prefix))
                    break;
                if (byteString.size() == prefix.size()) {
                    throw new IllegalArgumentException("duplicate option: " + byteString);
                }
                if (indexes.get(b) > indexes.get(a)) {
                    list.remove(b);
                    indexes.remove(b);
                } else {
                    b++;
                }
            }
        }

        Buffer trieBytes = new Buffer();
        buildTrieRecursive(0L, trieBytes, 0, list, 0, list.size(), indexes);

        int[] trie = new int[intCount(trieBytes)];
        for (int i = 0; i < trie.length; i++) {
            trie[i] = trieBytes.readInt();
        }
        if (!trieBytes.exhausted()) {
            throw new AssertionError();
        }

        return new ByteSelector(candidates.clone(), trie);
    }

    /**
     * Builds a trie encoded as an int array. Nodes in the trie are of two types: SELECT and SCAN.
     * <p>
     * SELECT nodes are encoded as:
     * <ul>
     * <li>selectChoiceCount: The number of bytes available for selection (a positive integer).</li>
     * <li>prefixIndex: The result index for the current position, or -1 if the current position itself is not a
     * result.</li>
     * <li>A sorted list of selectChoiceCount bytes to match against the input string.</li>
     * <li>A heterogeneous list of selectChoiceCount result indices (>= 0) or offsets (< 0) to the next node. Elements
     * in this list correspond to elements in the previous list. Offsets are negative and must be multiplied by -1
     * before use.</li>
     * </ul>
     * SCAN nodes are encoded as:
     * <ul>
     * <li>scanByteCount: The number of bytes to match in sequence. This count is negative and must be multiplied by -1
     * before use.</li>
     * <li>prefixIndex: The result index for the current position, or -1 if the current position itself is not a
     * result.</li>
     * <li>A list of scanByteCount bytes to match.</li>
     * <li>nextStep: The result index (>= 0) or offset (< 0) to the next node. Offsets are negative and must be
     * multiplied by -1 before use.</li>
     * </ul>
     * This structure is used to improve locality and performance when selecting from a candidate list.
     *
     * @param nodeOffset      The offset of the current node.
     * @param node            The buffer to write the trie node to.
     * @param candidateOffset The current offset within the byte strings being processed.
     * @param candidates      The list of {@link ByteString} candidates.
     * @param fromIndex       The starting index (inclusive) in {@code candidates} for the current sub-trie.
     * @param toIndex         The ending index (exclusive) in {@code candidates} for the current sub-trie.
     * @param indexes         A list mapping sorted {@link ByteString} indices to their original indices.
     * @throws AssertionError if an internal consistency check fails.
     */
    private static void buildTrieRecursive(
            long nodeOffset,
            Buffer node,
            int candidateOffset,
            List<ByteString> candidates,
            int fromIndex,
            int toIndex,
            List<Integer> indexes) {
        if (fromIndex >= toIndex)
            throw new AssertionError();
        for (int i = fromIndex; i < toIndex; i++) {
            if (candidates.get(i).size() < candidateOffset)
                throw new AssertionError();
        }

        ByteString from = candidates.get(fromIndex);
        ByteString to = candidates.get(toIndex - 1);
        int prefixIndex = -1;

        // If the first element already matches, it's a prefix.
        if (candidateOffset == from.size()) {
            prefixIndex = indexes.get(fromIndex);
            fromIndex++;
            from = candidates.get(fromIndex);
        }

        if (from.getByte(candidateOffset) != to.getByte(candidateOffset)) {
            // If there are multiple bytes to choose from, encode a SELECT node.
            int selectChoiceCount = 1;
            for (int i = fromIndex + 1; i < toIndex; i++) {
                if (candidates.get(i - 1).getByte(candidateOffset) != candidates.get(i).getByte(candidateOffset)) {
                    selectChoiceCount++;
                }
            }
            // Calculate the offset that childNodes will have when we append it to node.
            long childNodesOffset = nodeOffset + intCount(node) + 2 + (selectChoiceCount * 2);

            node.writeInt(selectChoiceCount);
            node.writeInt(prefixIndex);

            for (int i = fromIndex; i < toIndex; i++) {
                byte rangeByte = candidates.get(i).getByte(candidateOffset);
                if (i == fromIndex || rangeByte != candidates.get(i - 1).getByte(candidateOffset)) {
                    node.writeInt(rangeByte & 0xff);
                }
            }

            Buffer childNodes = new Buffer();
            int rangeStart = fromIndex;
            while (rangeStart < toIndex) {
                byte rangeByte = candidates.get(rangeStart).getByte(candidateOffset);
                int rangeEnd = toIndex;
                for (int i = rangeStart + 1; i < toIndex; i++) {
                    if (rangeByte != candidates.get(i).getByte(candidateOffset)) {
                        rangeEnd = i;
                        break;
                    }
                }

                if (rangeStart + 1 == rangeEnd && candidateOffset + 1 == candidates.get(rangeStart).size()) {
                    // The result is a single index.
                    node.writeInt(indexes.get(rangeStart));
                } else {
                    // The result is another node.
                    node.writeInt((int) (-1 * (childNodesOffset + intCount(childNodes))));
                    buildTrieRecursive(
                            childNodesOffset,
                            childNodes,
                            candidateOffset + 1,
                            candidates,
                            rangeStart,
                            rangeEnd,
                            indexes);
                }

                rangeStart = rangeEnd;
            }

            node.write(childNodes, childNodes.size());

        } else {
            // If all bytes are the same, encode a SCAN node.
            int scanByteCount = 0;
            for (int i = candidateOffset, max = Math.min(from.size(), to.size()); i < max; i++) {
                if (from.getByte(i) == to.getByte(i)) {
                    scanByteCount++;
                } else {
                    break;
                }
            }
            // Calculate the offset that childNodes will have when we append it to node.
            long childNodesOffset = nodeOffset + intCount(node) + 2 + scanByteCount + 1;

            node.writeInt(-scanByteCount);
            node.writeInt(prefixIndex);

            for (int i = candidateOffset; i < candidateOffset + scanByteCount; i++) {
                node.writeInt(from.getByte(i) & 0xff);
            }

            if (fromIndex + 1 == toIndex) {
                // The result is a single index.
                if (candidateOffset + scanByteCount != candidates.get(fromIndex).size()) {
                    throw new AssertionError();
                }
                node.writeInt(indexes.get(fromIndex));
            } else {
                // The result is another node.
                Buffer childNodes = new Buffer();
                node.writeInt((int) (-1 * (childNodesOffset + intCount(childNodes))));
                buildTrieRecursive(
                        childNodesOffset,
                        childNodes,
                        candidateOffset + scanByteCount,
                        candidates,
                        fromIndex,
                        toIndex,
                        indexes);
                node.write(childNodes, childNodes.size());
            }
        }
    }

    /**
     * Returns the number of integers in the given buffer.
     *
     * @param trieBytes The buffer containing the trie bytes.
     * @return The number of integers.
     */
    private static int intCount(Buffer trieBytes) {
        return (int) (trieBytes.size() / 4);
    }

    /**
     * Returns the {@link ByteString} at the specified position in this list.
     *
     * @param i The index of the element to return.
     * @return The {@link ByteString} at the specified position.
     * @throws IndexOutOfBoundsException if the index is out of range ({@code i < 0 || i >= size()}).
     */
    @Override
    public ByteString get(int i) {
        return candidates[i];
    }

    /**
     * Returns the number of elements in this list.
     *
     * @return The number of elements in this list.
     */
    @Override
    public int size() {
        return candidates.length;
    }

}
