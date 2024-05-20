/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.core.text.replacer;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * 高效替换器，通过查找指定关键字，替换对应的值
 * 基于AC自动机算法实现，需要被替换的原字符串越大，替换的键值对越多，效率提升越明显
 * 注意: 如果需要被替换的关键字出现交叉,最先匹配中的关键字会被替换
 * 1、"abc","ab"   会优先替换"ab"
 * 2、"abed","be"  会优先替换"abed"
 * 3、"abc", "ciphers"  会优先替换"abc"
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class HighMultiReplacer extends StringReplacer {

    private static final long serialVersionUID = -1L;

    private final AhoCorasickAutomaton ahoCorasickAutomaton;

    /**
     * 构造
     *
     * @param map key为需要被查找的字符串，value为对应的替换的值
     */
    public HighMultiReplacer(final Map<String, Object> map) {
        ahoCorasickAutomaton = new AhoCorasickAutomaton(map);
    }

    /**
     * 生成一个HighMultiReplacer对象
     *
     * @param map key为需要被查找的字符串，value为对应的替换的值
     * @return this
     */
    public static HighMultiReplacer of(final Map<String, Object> map) {
        return new HighMultiReplacer(map);
    }

    @Override
    public int replace(final CharSequence text, final int pos, final StringBuilder out) {
        ahoCorasickAutomaton.replace(text, out);
        return text.length();
    }

    @Override
    public CharSequence apply(final CharSequence text) {
        final StringBuilder builder = new StringBuilder();
        replace(text, 0, builder);
        return builder;
    }

    /**
     * AC自动机
     */
    private static class AhoCorasickAutomaton {

        /**
         * AC自动机的根结点，根结点不存储任何字符信息
         */
        private final Node root;

        /**
         * 待查找的目标字符串集合
         */
        private final Map<String, Object> target;

        /**
         * @param target 待查找的目标字符串集合
         */
        public AhoCorasickAutomaton(final Map<String, Object> target) {
            root = new Node();
            this.target = target;
            buildTrieTree();
            buildAcFromTrie();
        }

        /**
         * 由目标字符串构建Trie树
         */
        private void buildTrieTree() {
            for (final String text : target.keySet()) {
                Node curr = root;
                if (text == null) {
                    continue;
                }
                for (int i = 0; i < text.length(); i++) {
                    final char ch = text.charAt(i);
                    Node node = curr.children.get(ch);
                    if (node == null) {
                        node = new Node();
                        curr.children.put(ch, node);
                    }
                    curr = node;
                }
                // 将每个目标字符串的最后一个字符对应的结点变成终点
                curr.text = text;
            }
        }

        /**
         * 由Trie树构建AC自动机，本质是一个自动机，相当于构建KMP算法的next数组
         */
        private void buildAcFromTrie() {
            // 广度优先遍历所使用的队列
            final LinkedList<Node> queue = new LinkedList<>();

            // 单独处理根结点的所有孩子结点
            for (final Node x : root.children.values()) {
                /*根结点的所有孩子结点的fail都指向根结点*/
                x.fail = root;
                queue.addLast(x);// 所有根结点的孩子结点入列
            }

            while (!queue.isEmpty()) {
                // 确定出列结点的所有孩子结点的fail的指向
                final Node p = queue.removeFirst();
                for (final Map.Entry<Character, Node> entry : p.children.entrySet()) {

                    /*孩子结点入列*/
                    queue.addLast(entry.getValue());
                    // 从p.fail开始找起
                    Node failTo = p.fail;
                    while (true) {
                        // 说明找到了根结点还没有找到
                        if (failTo == null) {
                            entry.getValue().fail = root;
                            break;
                        }

                        // 说明有公共前缀
                        if (failTo.children.get(entry.getKey()) != null) {
                            entry.getValue().fail = failTo.children.get(entry.getKey());
                            break;
                        } else {// 继续向上寻找
                            failTo = failTo.fail;
                        }
                    }

                }
            }
        }

        /**
         * 在文本串中替换所有的目标字符串
         *
         * @param text          被替换的目标字符串
         * @param stringBuilder 替换后的结果
         */
        public void replace(final CharSequence text, final StringBuilder stringBuilder) {
            Node curr = root;
            int i = 0;
            while (i < text.length()) {
                // 文本串中的字符
                final char ch = text.charAt(i);
                // 文本串中的字符和AC自动机中的字符进行比较
                final Node node = curr.children.get(ch);
                if (node != null) {
                    stringBuilder.append(ch);
                    // 若相等，自动机进入下一状态
                    curr = node;
                    if (curr.isWord()) {
                        stringBuilder.delete(stringBuilder.length() - curr.text.length(), stringBuilder.length());
                        stringBuilder.append(target.get(curr.text));
                        curr = root;
                    }
                    // 索引自增，指向下一个文本串中的字符
                    i++;
                } else {
                    // 若不等，找到下一个应该比较的状态
                    curr = curr.fail;
                    /// 到根结点还未找到，说明文本串中以ch作为结束的字符片段不是任何目标字符串的前缀，状态机重置，比较下一个字符
                    if (curr == null) {
                        stringBuilder.append(ch);
                        curr = root;
                        i++;
                    }
                }
            }
        }

        /**
         * 用于表示AC自动机的每个结点，在每个结点中我们并没有存储该结点对应的字符
         */
        private static class Node {
            /**
             * 如果该结点是一个终点，即，从根结点到此结点表示了一个目标字符串，则str != null, 且str就表示该字符串
             */
            String text;

            /**
             * 该节点下的子节点
             */
            Map<Character, Node> children = new HashMap<>();

            /**
             * 当前结点的孩子结点不能匹配文本串中的某个字符时，下一个应该查找的结点
             */
            Node fail;

            public boolean isWord() {
                return text != null;
            }

        }
    }

}
