/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
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
package org.aoju.bus.sensitive;

import org.aoju.bus.core.text.TextBuilder;
import org.aoju.bus.core.toolkit.CollKit;

import java.util.*;
import java.util.function.Predicate;

/**
 * DFA（Deterministic Finite Automaton 确定有穷自动机）
 * DFA单词树（以下简称单词树），常用于在某大段文字中快速查找某几个关键词是否存在
 * 单词树使用group区分不同的关键字集合，不同的分组可以共享树枝，避免重复建树
 * 单词树使用树状结构表示一组单词
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class WordTree extends HashMap<Character, WordTree> {

    private static final long serialVersionUID = 1L;

    /**
     * 敏感词字符末尾标识，用于标识单词末尾字符
     */
    private final Set<Character> endCharacterSet = new HashSet<>();
    /**
     * 字符过滤规则，通过定义字符串过滤规则，过滤不需要的字符，当accept为false时，此字符不参与匹配
     */
    private Predicate<Character> predicate = StopChar::isNotStopChar;

    /**
     * 默认构造
     */
    public WordTree() {

    }

    /**
     * 设置字符过滤规则，通过定义字符串过滤规则，过滤不需要的字符
     * 当accept为false时，此字符不参与匹配
     *
     * @param predicate 过滤函数
     * @return this
     */
    public WordTree setCharFilter(Predicate<Character> predicate) {
        this.predicate = predicate;
        return this;
    }

    /**
     * 增加一组单词
     *
     * @param words 单词集合
     * @return this
     */
    public WordTree addWords(Collection<String> words) {
        if (false == (words instanceof Set)) {
            words = new HashSet<>(words);
        }
        for (String word : words) {
            addWord(word);
        }
        return this;
    }

    /**
     * 增加一组单词
     *
     * @param words 单词数组
     * @return this
     */
    public WordTree addWords(String... words) {
        for (String word : CollKit.newHashSet(words)) {
            addWord(word);
        }
        return this;
    }

    /**
     * 添加单词，使用默认类型
     *
     * @param word 单词
     * @return this
     */
    public WordTree addWord(String word) {
        final Predicate<Character> charFilter = this.predicate;
        WordTree parent = null;
        WordTree current = this;
        WordTree child;
        char currentChar = 0;
        int length = word.length();
        for (int i = 0; i < length; i++) {
            currentChar = word.charAt(i);
            // 只处理合法字符
            if (charFilter.test(currentChar)) {
                child = current.get(currentChar);
                if (child == null) {
                    // 无子类，新建一个子节点后存放下一个字符
                    child = new WordTree();
                    current.put(currentChar, child);
                }
                parent = current;
                current = child;
            }
        }
        if (null != parent) {
            parent.setEnd(currentChar);
        }
        return this;
    }

    /**
     * 指定文本是否包含树中的词
     *
     * @param text 被检查的文本
     * @return 是否包含
     */
    public boolean isMatch(String text) {
        if (null == text) {
            return false;
        }
        return null != match(text);
    }

    /**
     * 获得第一个匹配的关键字
     *
     * @param text 被检查的文本
     * @return 匹配到的关键字
     */
    public String match(String text) {
        if (null == text) {
            return null;
        }
        List<String> matchAll = matchAll(text, 1);
        if (CollKit.isNotEmpty(matchAll)) {
            return matchAll.get(0);
        }
        return null;
    }

    /**
     * 找出所有匹配的关键字
     *
     * @param text 被检查的文本
     * @return 匹配的词列表
     */
    public List<String> matchAll(String text) {
        return matchAll(text, -1);
    }

    /**
     * 找出所有匹配的关键字
     *
     * @param text  被检查的文本
     * @param limit 限制匹配个数
     * @return 匹配的词列表
     */
    public List<String> matchAll(String text, int limit) {
        return matchAll(text, limit, false, false);
    }

    /**
     * 找出所有匹配的关键字
     * 密集匹配原则：假如关键词有 ab,b，文本是abab，将匹配 [ab,b,ab]
     * 贪婪匹配（最长匹配）原则：假如关键字a,ab，最长匹配将匹配[a, ab]
     *
     * @param text           被检查的文本
     * @param limit          限制匹配个数
     * @param isDensityMatch 是否使用密集匹配原则
     * @param isGreedMatch   是否使用贪婪匹配（最长匹配）原则
     * @return 匹配的词列表
     */
    public List<String> matchAll(String text, int limit, boolean isDensityMatch, boolean isGreedMatch) {
        if (null == text) {
            return null;
        }

        List<String> foundWords = new ArrayList<>();
        WordTree current = this;
        int length = text.length();
        final Predicate<Character> charFilter = this.predicate;
        // 存放查找到的字符缓存。完整出现一个词时加到findedWords中，否则清空
        final TextBuilder wordBuffer = new TextBuilder();
        char currentChar;
        for (int i = 0; i < length; i++) {
            wordBuffer.reset();
            for (int j = i; j < length; j++) {
                currentChar = text.charAt(j);
                if (false == charFilter.test(currentChar)) {
                    if (wordBuffer.length() > 0) {
                        // 做为关键词中间的停顿词被当作关键词的一部分被返回
                        wordBuffer.append(currentChar);
                    } else {
                        // 停顿词做为关键词的第一个字符时需要跳过
                        i++;
                    }
                    continue;
                } else if (false == current.containsKey(currentChar)) {
                    // 非关键字符被整体略过，重新以下个字符开始检查
                    break;
                }
                wordBuffer.append(currentChar);
                if (current.isEnd(currentChar)) {
                    // 到达单词末尾，关键词成立，从此词的下一个位置开始查找
                    foundWords.add(wordBuffer.toString());
                    if (limit > 0 && foundWords.size() >= limit) {
                        // 超过匹配限制个数，直接返回
                        return foundWords;
                    }
                    if (false == isDensityMatch) {
                        // 如果非密度匹配，跳过匹配到的词
                        i = j;
                    }
                    if (false == isGreedMatch) {
                        // 如果懒惰匹配（非贪婪匹配）。当遇到第一个结尾标记就结束本轮匹配
                        break;
                    }
                }
                current = current.get(currentChar);
                if (null == current) {
                    break;
                }
            }
            current = this;
        }
        return foundWords;
    }

    /**
     * 是否末尾
     *
     * @param c 检查的字符
     * @return 是否末尾
     */
    private boolean isEnd(Character c) {
        return this.endCharacterSet.contains(c);
    }

    /**
     * 设置是否到达末尾
     *
     * @param c 设置结尾的字符
     */
    private void setEnd(Character c) {
        if (null != c) {
            this.endCharacterSet.add(c);
        }
    }

    /**
     * 清除所有的词,
     * 此方法调用后, wordTree 将被清空
     * endCharacterSet 也将清空
     */
    @Override
    public void clear() {
        super.clear();
        this.endCharacterSet.clear();
    }

}
