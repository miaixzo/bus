/*
 * The MIT License
 *
 * Copyright (c) 2015-2020 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.core.io;

import java.util.*;

/**
 * 可以读取的一组索引值 {@link BufferSource#select}.
 *
 * @author Kimi Liu
 * @version 5.5.6
 * @since JDK 1.8+
 */
public final class BufferOption extends AbstractList<ByteString> implements RandomAccess {

    final ByteString[] byteStrings;
    final int[] trie;

    private BufferOption(ByteString[] byteStrings, int[] trie) {
        this.byteStrings = byteStrings;
        this.trie = trie;
    }

    public static BufferOption of(ByteString... byteStrings) {
        if (byteStrings.length == 0) {
            return new BufferOption(new ByteString[0], new int[]{0, -1});
        }

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

        for (int a = 0; a < list.size(); a++) {
            ByteString prefix = list.get(a);
            for (int b = a + 1; b < list.size(); ) {
                ByteString byteString = list.get(b);
                if (!byteString.startsWith(prefix)) break;
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

        return new BufferOption(byteStrings.clone() /* Defensive copy. */, trie);
    }

    private static void buildTrieRecursive(
            long nodeOffset,
            Buffer node,
            int byteStringOffset,
            List<ByteString> byteStrings,
            int fromIndex,
            int toIndex,
            List<Integer> indexes) {
        if (fromIndex >= toIndex) throw new AssertionError();
        for (int i = fromIndex; i < toIndex; i++) {
            if (byteStrings.get(i).size() < byteStringOffset) throw new AssertionError();
        }

        ByteString from = byteStrings.get(fromIndex);
        ByteString to = byteStrings.get(toIndex - 1);
        int prefixIndex = -1;

        if (byteStringOffset == from.size()) {
            prefixIndex = indexes.get(fromIndex);
            fromIndex++;
            from = byteStrings.get(fromIndex);
        }

        if (from.getByte(byteStringOffset) != to.getByte(byteStringOffset)) {
            int selectChoiceCount = 1;
            for (int i = fromIndex + 1; i < toIndex; i++) {
                if (byteStrings.get(i - 1).getByte(byteStringOffset)
                        != byteStrings.get(i).getByte(byteStringOffset)) {
                    selectChoiceCount++;
                }
            }

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

                if (rangeStart + 1 == rangeEnd
                        && byteStringOffset + 1 == byteStrings.get(rangeStart).size()) {
                    node.writeInt(indexes.get(rangeStart));
                } else {
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
            int scanByteCount = 0;
            for (int i = byteStringOffset, max = Math.min(from.size(), to.size()); i < max; i++) {
                if (from.getByte(i) == to.getByte(i)) {
                    scanByteCount++;
                } else {
                    break;
                }
            }

            long childNodesOffset = nodeOffset + intCount(node) + 2 + scanByteCount + 1;

            node.writeInt(-scanByteCount);
            node.writeInt(prefixIndex);

            for (int i = byteStringOffset; i < byteStringOffset + scanByteCount; i++) {
                node.writeInt(from.getByte(i) & 0xff);
            }

            if (fromIndex + 1 == toIndex) {
                if (byteStringOffset + scanByteCount != byteStrings.get(fromIndex).size()) {
                    throw new AssertionError();
                }
                node.writeInt(indexes.get(fromIndex));
            } else {
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

    private static int intCount(Buffer trieBytes) {
        return (int) (trieBytes.size() / 4);
    }

    @Override
    public ByteString get(int i) {
        return byteStrings[i];
    }

    @Override
    public final int size() {
        return byteStrings.length;
    }

}
