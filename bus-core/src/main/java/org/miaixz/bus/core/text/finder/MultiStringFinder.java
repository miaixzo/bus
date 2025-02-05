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
 * 多字符串查询器 底层思路 使用 AC 自动机实现
 * 
 * @author Kimi Liu
 * @since Java 17+
 */
public class MultiStringFinder {

    /**
     * 字符索引
     */
    protected final Map<Character, Integer> charIndexMap = new HashMap<>();
    /**
     * 全部字符数量
     */
    protected final int allCharSize;
    /**
     * 根节点
     */
    protected final Node root;
    /**
     * 全部节点数量
     */
    int nodeSize;

    /**
     * 构建多字符串查询器
     *
     * @param source 字符串集合
     */
    public MultiStringFinder(final Collection<String> source) {
        // 待匹配的字符串
        final Set<String> stringSet = new HashSet<>();

        // 所有字符
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
     * 创建多字符串查询器
     *
     * @param source 字符串集合
     * @return 多字符串查询器
     */
    public static MultiStringFinder of(final Collection<String> source) {
        return new MultiStringFinder(source);
    }

    /**
     * 构建前缀树
     *
     * @param stringSst 待匹配的字符串
     */
    protected void buildPrefixTree(final Collection<String> stringSst) {
        // 节点编号 根节点已经是0了 所以从 1开始编号
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
     * 构建 fail指针过程 构建 directRouter 直接访问路由表 减少跳fail次数 直接跳 router 边
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

        // 进行广度优先遍历
        while (!nodeQueue.isEmpty()) {
            final Node parent = nodeQueue.removeFirst();
            // 因为 使用了 charIndex 进行字符到下标的映射 i 可以直接认为就是对应字符 char
            for (int i = 0; i < parent.directRouter.length; i++) {
                final Node child = parent.directRouter[i];
                // child 为 null 表示没有子节点
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
     * 查询匹配的字符串
     *
     * @param text 返回每个匹配的 字符串 value是字符首字母地址
     * @return 匹配结果
     */
    public Map<String, List<Integer>> findMatch(final String text) {
        // 节点经过次数 放在方法内部声明变量 希望可以一个构建对象 进行多次匹配
        final HashMap<String, List<Integer>> resultMap = new HashMap<>();

        final char[] chars = text.toCharArray();
        Node currentNode = root;
        for (int i = 0; i < chars.length; i++) {
            final char c = chars[i];
            final Integer index = charIndexMap.get(c);
            // 找不到字符索引 认为一定不在匹配字符中存在 直接从根节点开始重新计算
            if (index == null) {
                currentNode = root;
                continue;
            }
            // 进入下一跳 可能是正常下一跳 也可能是fail加上后的 下一跳
            currentNode = currentNode.directRouter[index];
            // 判断是否尾部节点 是尾节点 说明已经匹配到了完整的字符串 将匹配结果写入返回对象
            if (currentNode.isEnd) {
                resultMap.computeIfAbsent(currentNode.tagetString, k -> new ArrayList<>())
                        .add(i - currentNode.tagetString.length() + 1);
            }

        }

        return resultMap;
    }

    /**
     * 获取字符 下标
     *
     * @param c 字符
     * @return 下标
     */
    protected int getIndex(final char c) {
        final Integer i = charIndexMap.get(c);
        if (i == null) {
            return -1;
        }
        return i;
    }

    /**
     * AC 自动机节点
     */
    protected static class Node {
        // 是否是字符串 尾节点
        public boolean isEnd = false;

        // 如果当前节点是尾节点 那么表示 匹配到的字符串 其他情况下 null
        public String tagetString;

        // 失效节点
        public Node fail;

        /**
         * 直接路由表 减少挑 fail过程 使用数组 + charIndex 希望库减少 hash复杂度和内存空间 当初始化 stringSet 数量较大时 字符较多可以一定程度上减少 hashMap 底层实现带来的 内存开销
         * directRouter 大小为 全部字符数量
         */
        public Node[] directRouter;

        // 节点编号 root 为 0
        public int nodeIndex;

        // 值
        public char value;

        // fail指针来源
        public List<Node> failPre = new ArrayList<>();

        public Node() {
        }

        /**
         * 构建根节点
         *
         * @param allCharSize 全部字符数量
         * @return 根Node
         */
        public static Node createRoot(final int allCharSize) {
            final Node node = new Node();
            node.nodeIndex = 0;
            node.fail = node;
            node.directRouter = new Node[allCharSize];
            return node;
        }

        /**
         * 新增子节点
         *
         * @param c         字符
         * @param nodeIndex 节点编号
         * @param charIndex 字符索引
         * @return 如果已经存在子节点 false 新增 ture
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
         * 标记当前节点为 字符串尾节点
         *
         * @param string 字符串
         */
        public void setEnd(final String string) {
            tagetString = string;
            isEnd = true;
        }

        /**
         * 获取下一跳
         *
         * @param c         字符
         * @param charIndex 字符索引
         * @return 下一个Node
         */
        public Node getNext(final char c, final Map<Character, Integer> charIndex) {
            final Integer index = charIndex.get(c);
            if (index == null) {
                return null;
            }
            return directRouter[index];
        }

        @Override
        public String toString() {
            return value + ":" + nodeIndex;
        }
    }

}