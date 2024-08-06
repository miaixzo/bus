/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
 * 可以读取的一组索引值 {@link BufferSource#select}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SegmentBuffer extends AbstractList<ByteString> implements RandomAccess {

    public final ByteString[] byteStrings;
    public final int[] trie;

    private SegmentBuffer(ByteString[] byteStrings, int[] trie) {
        this.byteStrings = byteStrings;
        this.trie = trie;
    }

    /**
     * 构造
     *
     * @param byteStrings 字符串
     * @return the object
     */
    public static SegmentBuffer of(ByteString... byteStrings) {
        if (byteStrings.length == 0) {
            // 没有选择，我们必须总是返回-1，创建一个空集合
            return new SegmentBuffer(new ByteString[0], new int[] { 0, -1 });
        }

        // 对在递归构建时需要的字节字符串排序。将排序后的索引映射到调用者的索引
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
        // 删除那些永远不会返回的元素，因为它们遵循自己的前缀。例如，
        // 如果调用者提供["abc"， "abcde"]，我们将永远不会返回"abcde"，因为我们一遇到"abc"就返回
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
     * 构建一个被编码为int数组的trie，trie中的节点有两种类型:SELECT和SCAN SELECT 节点编码为: - selectChoiceCount:可供选择的字节数(一个正整数) -
     * prefixIndex:当前位置的结果索引，如果当前位置本身不是结果，则为-1 - 已排序的selectChoiceCount字节列表，用于匹配输入字符串 - 下一个节点的selectChoiceCount结果索引(>=
     * 0)或偏移量(< 0)的异构列表。 此列表中的元素对应于前一个列表中的元素。偏移量为负，在使用前必须乘以-1。 SCAN 节点编码为: - scanByteCount:按顺序匹配的字节数。此计数为负数，在使用之前必须乘以-1。
     * - prefixIndex:当前位置的结果索引，如果当前位置本身不是结果，则为-1 - 要匹配的scanByteCount字节的列表 - nextStep:下一个节点的结果索引(>= 0)或偏移量(<
     * 0)。偏移量为负，在使用前必须乘以-1。 当从选项列表中进行选择时，此结构用于改进局部性和性能
     *
     * @param nodeOffset       节点偏移量
     * @param node             节点信息
     * @param byteStringOffset 缓冲偏移量
     * @param byteStrings      换取区信息
     * @param fromIndex        前缀
     * @param toIndex          查找索引
     * @param indexes          索引信息
     */
    private static void buildTrieRecursive(long nodeOffset, Buffer node, int byteStringOffset,
            List<ByteString> byteStrings, int fromIndex, int toIndex, List<Integer> indexes) {
        if (fromIndex >= toIndex)
            throw new AssertionError();
        for (int i = fromIndex; i < toIndex; i++) {
            if (byteStrings.get(i).size() < byteStringOffset)
                throw new AssertionError();
        }

        ByteString from = byteStrings.get(fromIndex);
        ByteString to = byteStrings.get(toIndex - 1);
        int prefixIndex = -1;

        // // 如果第一个元素已经匹配，那就是前缀
        if (byteStringOffset == from.size()) {
            prefixIndex = indexes.get(fromIndex);
            fromIndex++;
            from = byteStrings.get(fromIndex);
        }

        if (from.getByte(byteStringOffset) != to.getByte(byteStringOffset)) {
            // 如果有多个字节可供选择，则对SELECT节点进行编码
            int selectChoiceCount = 1;
            for (int i = fromIndex + 1; i < toIndex; i++) {
                if (byteStrings.get(i - 1).getByte(byteStringOffset) != byteStrings.get(i).getByte(byteStringOffset)) {
                    selectChoiceCount++;
                }
            }
            // 计算当我们将它附加到node时，childNodes将得到的偏移量
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
                    // 结果是一个单一的指数
                    node.writeInt(indexes.get(rangeStart));
                } else {
                    // 结果是另一个节点
                    node.writeInt((int) (-1 * (childNodesOffset + intCount(childNodes))));
                    buildTrieRecursive(childNodesOffset, childNodes, byteStringOffset + 1, byteStrings, rangeStart,
                            rangeEnd, indexes);
                }

                rangeStart = rangeEnd;
            }

            node.write(childNodes, childNodes.size());

        } else {
            // 如果所有字节都相同，则对扫描节点进行编码
            int scanByteCount = 0;
            for (int i = byteStringOffset, max = Math.min(from.size(), to.size()); i < max; i++) {
                if (from.getByte(i) == to.getByte(i)) {
                    scanByteCount++;
                } else {
                    break;
                }
            }
            // 计算当我们将它附加到node时，childNodes将得到的偏移量
            long childNodesOffset = nodeOffset + intCount(node) + 2 + scanByteCount + 1;

            node.writeInt(-scanByteCount);
            node.writeInt(prefixIndex);

            for (int i = byteStringOffset; i < byteStringOffset + scanByteCount; i++) {
                node.writeInt(from.getByte(i) & 0xff);
            }

            if (fromIndex + 1 == toIndex) {
                // 结果是一个单一的指数
                if (byteStringOffset + scanByteCount != byteStrings.get(fromIndex).size()) {
                    throw new AssertionError();
                }
                node.writeInt(indexes.get(fromIndex));
            } else {
                // 结果是另一个节点
                Buffer childNodes = new Buffer();
                node.writeInt((int) (-1 * (childNodesOffset + intCount(childNodes))));
                buildTrieRecursive(childNodesOffset, childNodes, byteStringOffset + scanByteCount, byteStrings,
                        fromIndex, toIndex, indexes);
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
