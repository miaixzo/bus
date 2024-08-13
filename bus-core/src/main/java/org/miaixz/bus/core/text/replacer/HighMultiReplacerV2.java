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
package org.miaixz.bus.core.text.replacer;

import java.util.Map;

import org.miaixz.bus.core.text.finder.MultiStringFinder;

/**
 * 高效替换器，通过查找指定关键字，替换对应的值 基于AC自动机算法实现，需要被替换的原字符串越大，替换的键值对越多，效率提升越明显 注意: 如果需要被替换的关键字出现交叉,最先匹配中的关键字会被替换 1、"abc","ab"
 * 会优先替换"ab" 2、"abed","be" 会优先替换"abed" 3、"abc", "bc" 会优先替换"abc"
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class HighMultiReplacerV2 extends StringReplacer {

    private static final long serialVersionUID = -1L;

    private final AhoCorasickAutomaton ahoCorasickAutomaton;

    /**
     * 构造
     *
     * @param map key为需要被查找的字符串，value为对应的替换的值
     */
    public HighMultiReplacerV2(final Map<String, String> map) {
        ahoCorasickAutomaton = new AhoCorasickAutomaton(map);
    }

    @Override
    protected int replace(final CharSequence text, final int pos, final StringBuilder out) {
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
     * AC 自动机
     */
    protected static class AhoCorasickAutomaton extends MultiStringFinder {

        protected final Map<String, String> replaceMap;

        public AhoCorasickAutomaton(final Map<String, String> replaceMap) {
            super(replaceMap.keySet());
            this.replaceMap = replaceMap;
        }

        public void replace(final CharSequence text, final StringBuilder stringBuilder) {
            Node currentNode = root;
            // 临时字符串存储空间
            final StringBuilder temp = new StringBuilder();
            for (int i = 0; i < text.length(); i++) {
                final char ch = text.charAt(i);
                final Integer index = charIndexMap.get(ch);
                // 下一个字符在候选转换字符串中都不存在 ch字符一定不会被替换
                if (index < 0) {
                    // 临时缓存空间中的数据写入到输出的 StringBuilder
                    if (temp.length() > 0) {
                        stringBuilder.append(temp);
                        // 数据写入后清空临时空间
                        temp.delete(0, temp.length());
                    }
                    // 将一个一定不会替换的字符 ch 写入输出
                    stringBuilder.append(ch);
                    // 匹配失败 将当前节点重新指向根节点
                    currentNode = root;
                    continue;
                }

                // 这个逻辑分支表示 已经匹配到了下一跳
                currentNode = currentNode.directRouter[index];

                // 当前是root节点表示匹配中断 清理临时空间 写入到输出
                if (currentNode.nodeIndex == 0) {
                    if (temp.length() > 0) {
                        stringBuilder.append(temp);
                        // 数据写入后清空临时空间
                        temp.delete(0, temp.length());
                        // 当前情况表示该字符存在在候选转换字符中 但是前一个字符到这里是不存在路径
                        stringBuilder.append(ch);
                        continue;
                    }
                }

                // 表示匹配到 现在进行字符串替换工作
                if (currentNode.isEnd) {
                    final int length = currentNode.tagetString.length();
                    // 先清理匹配到的字符 最后一个字符未加入临时空间
                    temp.delete(temp.length() - length + 1, length - 1);
                    if (temp.length() > 0) {
                        stringBuilder.append(temp);
                    }
                    // 写入被替换的字符串
                    stringBuilder.append(replaceMap.get(currentNode.tagetString));
                    // 因为字符串被替换过了 所以当前节点重新指向 root
                    currentNode = root;
                    continue;
                }

                temp.append(ch);
            }
        }
    }

}
