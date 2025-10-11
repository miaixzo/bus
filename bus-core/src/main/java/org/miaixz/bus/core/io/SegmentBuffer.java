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
package org.miaixz.bus.core.io;

import java.util.*;

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.source.BufferSource;

/**
 * A set of indexed values that can be read by {@link BufferSource#select}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SegmentBuffer extends AbstractList<ByteString> implements RandomAccess {

    /**
     * The array of ByteString options.
     */
    public final ByteString[] byteStrings;
    /**
     * The trie structure for efficient lookup.
     */
    public final int[] trie;

    /**
     * Private constructor for {@code SegmentBuffer}.
     *
     * @param byteStrings The array of {@link ByteString} options.
     * @param trie        The trie structure as an array of integers.
     */
    private SegmentBuffer(ByteString[] byteStrings, int[] trie) {
        this.byteStrings = byteStrings;
        this.trie = trie;
    }

    /**
     * Creates a {@code SegmentBuffer} from an array of {@link ByteString} options.
     *
     * @param byteStrings The {@link ByteString} options.
     * @return A new {@link SegmentBuffer} instance.
     * @throws IllegalArgumentException if an empty byte string is provided, or if duplicate options exist.
     */
    public static SegmentBuffer of(ByteString... byteStrings) {
        if (byteStrings.length == 0) {
            // No options, we must always return -1. Create an empty set.
            return new SegmentBuffer(new ByteString[0], new int[] { 0, -1 });
        }

        // Sort the byte strings needed for recursive construction. Map the sorted indices to the caller's indices.
        List<ByteString> list = new ArrayList<>(Arrays.asList(byteStrings));
        Collections.sort(list);
        List<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            indexes.add(-1);
        }
        for (int i = 0; i < list.size(); i++) {
            int sortedIndex = Collections.binarySearch(list, byteStrings[i]);
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

        return new SegmentBuffer(byteStrings.clone(), trie);
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
     * This structure is used to improve locality and performance when selecting from a list of options.
     *
     * @param nodeOffset       The offset of the current node.
     * @param node             The buffer to write the trie node to.
     * @param byteStringOffset The current offset within the byte strings being processed.
     * @param byteStrings      The list of {@link ByteString} options.
     * @param fromIndex        The starting index (inclusive) in {@code byteStrings} for the current sub-trie.
     * @param toIndex          The ending index (exclusive) in {@code byteStrings} for the current sub-trie.
     * @param indexes          A list mapping sorted {@link ByteString} indices to their original indices.
     * @throws AssertionError if an internal consistency check fails.
     */
    private static void buildTrieRecursive(
            long nodeOffset,
            Buffer node,
            int byteStringOffset,
            List<ByteString> byteStrings,
            int fromIndex,
            int toIndex,
            List<Integer> indexes) {
        if (fromIndex >= toIndex)
            throw new AssertionError();
        for (int i = fromIndex; i < toIndex; i++) {
            if (byteStrings.get(i).size() < byteStringOffset)
                throw new AssertionError();
        }

        ByteString from = byteStrings.get(fromIndex);
        ByteString to = byteStrings.get(toIndex - 1);
        int prefixIndex = -1;

        // If the first element already matches, it's a prefix.
        if (byteStringOffset == from.size()) {
            prefixIndex = indexes.get(fromIndex);
            fromIndex++;
            from = byteStrings.get(fromIndex);
        }

        if (from.getByte(byteStringOffset) != to.getByte(byteStringOffset)) {
            // If there are multiple bytes to choose from, encode a SELECT node.
            int selectChoiceCount = 1;
            for (int i = fromIndex + 1; i < toIndex; i++) {
                if (byteStrings.get(i - 1).getByte(byteStringOffset) != byteStrings.get(i).getByte(byteStringOffset)) {
                    selectChoiceCount++;
                }
            }
            // Calculate the offset that childNodes will have when we append it to node.
            long childNodesOffset = nodeOffset + intCount(node) + 2 + (selectChoiceCount * 2);

            node.writeInt(selectChoiceCount);
            node.writeInt(prefixIndex);

            for (int i = fromIndex; i < toIndex; i++) {
                byte rangeByte = byteStrings.get(i).getByte(byteStringOffset);
                if (i == fromIndex || rangeByte != byteStrings.get(i - 1).getByte(byteStringOffset)) {
                    node.writeInt(rangeByte & 0xff);
                }
            }

            Buffer childNodes = new Buffer();
            int rangeStart = fromIndex;
            while (rangeStart < toIndex) {
                byte rangeByte = byteStrings.get(rangeStart).getByte(byteStringOffset);
                int rangeEnd = toIndex;
                for (int i = rangeStart + 1; i < toIndex; i++) {
                    if (rangeByte != byteStrings.get(i).getByte(byteStringOffset)) {
                        rangeEnd = i;
                        break;
                    }
                }

                if (rangeStart + 1 == rangeEnd && byteStringOffset + 1 == byteStrings.get(rangeStart).size()) {
                    // The result is a single index.
                    node.writeInt(indexes.get(rangeStart));
                } else {
                    // The result is another node.
                    node.writeInt((int) (-1 * (childNodesOffset + intCount(childNodes))));
                    buildTrieRecursive(
                            childNodesOffset,
                            childNodes,
                            byteStringOffset + 1,
                            byteStrings,
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
            for (int i = byteStringOffset, max = Math.min(from.size(), to.size()); i < max; i++) {
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

            for (int i = byteStringOffset; i < byteStringOffset + scanByteCount; i++) {
                node.writeInt(from.getByte(i) & 0xff);
            }

            if (fromIndex + 1 == toIndex) {
                // The result is a single index.
                if (byteStringOffset + scanByteCount != byteStrings.get(fromIndex).size()) {
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
                        byteStringOffset + scanByteCount,
                        byteStrings,
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
        return byteStrings[i];
    }

    /**
     * Returns the number of elements in this list.
     *
     * @return The number of elements in this list.
     */
    @Override
    public final int size() {
        return byteStrings.length;
    }

}
