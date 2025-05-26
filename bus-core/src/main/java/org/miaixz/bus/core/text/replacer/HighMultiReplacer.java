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
package org.miaixz.bus.core.text.replacer;

import java.io.Serial;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * 高效替换器，通过查找指定关键字，替换对应的值 基于AC自动机算法实现，需要被替换的原字符串越大，替换的键值对越多，效率提升越明显 注意: 如果需要被替换的关键字出现交叉,最先匹配中的关键字会被替换 1、"abc","ab"
 * 会优先替换"ab" 2、"abed","be" 会优先替换"abed" 3、"abc", "ciphers" 会优先替换"abc"
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class HighMultiReplacer extends StringReplacer {

    @Serial
    private static final long serialVersionUID = 2852278738592L;

    private final AhoCorasickAutomaton ahoCorasickAutomaton;

    /**
     * 构造函数，初始化AC自动机。
     *
     * @param map 关键字映射，key为需要查找的字符串，value为替换值
     */
    public HighMultiReplacer(final Map<String, Object> map) {
        ahoCorasickAutomaton = new AhoCorasickAutomaton(map);
    }

    /**
     * 工厂方法，创建HighMultiReplacer实例。
     *
     * @param map 关键字映射，key为需要查找的字符串，value为替换值
     * @return HighMultiReplacer实例
     */
    public static HighMultiReplacer of(final Map<String, Object> map) {
        return new HighMultiReplacer(map);
    }

    /**
     * 执行字符串替换，从指定位置开始，将结果追加到输出缓冲区。
     *
     * @param text 待替换的字符串
     * @param pos  替换起始位置
     * @param out  输出缓冲区，存储替换结果
     * @return 替换后处理的字符数
     */
    @Override
    public int replace(final CharSequence text, final int pos, final StringBuilder out) {
        ahoCorasickAutomaton.replace(text, out);
        return text.length();
    }

    /**
     * 应用替换规则，返回替换后的字符串。
     *
     * @param text 待替换的字符串
     * @return 替换后的字符串
     */
    @Override
    public CharSequence apply(final CharSequence text) {
        final StringBuilder builder = new StringBuilder();
        replace(text, 0, builder);
        return builder;
    }

    /**
     * AC自动机实现，用于高效查找和替换关键字。
     */
    private static class AhoCorasickAutomaton {

        /**
         * AC自动机的根节点，不存储任何字符信息。
         */
        private final Node root;

        /**
         * 关键字映射，key为查找的目标字符串，value为对应的替换值。
         */
        private final Map<String, Object> target;

        /**
         * 构造函数，初始化AC自动机，构建Trie树和fail指针。
         *
         * @param target 关键字映射
         */
        public AhoCorasickAutomaton(final Map<String, Object> target) {
            root = new Node();
            this.target = target;
            buildTrieTree();
            buildAcFromTrie();
        }

        /**
         * 构建Trie树，支持三种关键字格式：field、${field}和{field}。
         */
        private void buildTrieTree() {
            for (final String text : target.keySet()) {
                if (text == null) {
                    continue; // 跳过空关键字
                }
                // 添加直接关键字格式（如 field）
                buildTrieTree(text, text);
                // 添加${}包装格式（如 ${field}）
                buildTrieTree("${" + text + "}", text);
                // 添加{}包装格式（如 {field}）
                buildTrieTree("{" + text + "}", text);
            }
        }

        /**
         * 将关键字模式添加到Trie树。
         *
         * @param pattern 匹配模式（如 field、${field} 或 {field}）
         * @param key     原始关键字，用于查找替换值（如 field）
         */
        private void buildTrieTree(final String pattern, final String key) {
            Node curr = root; // 初始化为根节点
            for (int i = 0; i < pattern.length(); i++) {
                final char ch = pattern.charAt(i);
                Node node = curr.children.get(ch);
                if (node == null) {
                    node = new Node();
                    curr.children.put(ch, node);
                }
                curr = node;
            }
            // 存储原始关键字，用于后续替换
            curr.text = key;
        }

        /**
         * 由Trie树构建AC自动机，生成fail指针，类似KMP算法的next数组。
         */
        private void buildAcFromTrie() {
            final LinkedList<Node> queue = new LinkedList<>();
            // 初始化根节点的子节点
            for (final Node x : root.children.values()) {
                x.fail = root; // 子节点的fail指针指向根节点
                queue.addLast(x); // 入队
            }

            // 广度优先遍历，构建fail指针
            while (!queue.isEmpty()) {
                final Node p = queue.removeFirst();
                for (final Map.Entry<Character, Node> entry : p.children.entrySet()) {
                    queue.addLast(entry.getValue());
                    Node failTo = p.fail;
                    while (true) {
                        if (failTo == null) {
                            entry.getValue().fail = root; // 未找到匹配，指向根节点
                            break;
                        }
                        if (failTo.children.get(entry.getKey()) != null) {
                            entry.getValue().fail = failTo.children.get(entry.getKey()); // 找到匹配
                            break;
                        }
                        failTo = failTo.fail; // 继续向上回溯
                    }
                }
            }
        }

        /**
         * 执行字符串替换，将匹配的关键字替换为目标值。
         *
         * @param text          待替换的字符串
         * @param stringBuilder 输出缓冲区，存储替换结果
         */
        public void replace(final CharSequence text, final StringBuilder stringBuilder) {
            Node curr = root;
            int i = 0;
            while (i < text.length()) {
                final char ch = text.charAt(i);
                final Node node = curr.children.get(ch);
                if (node != null) {
                    // 匹配到字符，进入下一状态
                    curr = node;
                    if (curr.isWord()) {
                        // 匹配到完整关键字，追加替换值
                        final Object replacement = target.get(curr.text);
                        stringBuilder.append(replacement != null ? replacement : "");
                        curr = root; // 重置状态机
                    }
                    i++;
                } else {
                    // 未匹配，尝试fail指针
                    if (curr != root) {
                        curr = curr.fail; // 回溯
                    } else {
                        stringBuilder.append(ch); // 无匹配，追加当前字符
                        i++;
                    }
                }
            }
        }

        /**
         * AC自动机的节点，表示Trie树中的一个状态。
         */
        private static class Node {
            /**
             * 终点标记，表示从根节点到此节点形成一个关键字，存储原始关键字。
             */
            String text;

            /**
             * 子节点映射，key为字符，value为对应的子节点。
             */
            Map<Character, Node> children = new HashMap<>();

            /**
             * fail指针，指向匹配失败时应跳转的节点。
             */
            Node fail;

            /**
             * 判断当前节点是否为关键字的终点。
             *
             * @return 是否为终点
             */
            public boolean isWord() {
                return text != null;
            }
        }
    }

}
