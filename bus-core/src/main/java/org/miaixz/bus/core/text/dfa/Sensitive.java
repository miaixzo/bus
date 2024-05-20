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
package org.miaixz.bus.core.text.dfa;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.text.CharsBacker;
import org.miaixz.bus.core.toolkit.CollKit;
import org.miaixz.bus.core.toolkit.StringKit;
import org.miaixz.bus.core.toolkit.ThreadKit;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * 敏感词工具类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class Sensitive {

    /**
     * 默认关键词分隔符
     */
    public static final String DEFAULT_SEPARATOR = Symbol.COMMA;
    private static final WordTree sensitiveTree = new WordTree();

    /**
     * @return 是否已经被初始化
     */
    public static boolean isInited() {
        return !sensitiveTree.isEmpty();
    }

    /**
     * 初始化敏感词树
     *
     * @param isAsync        是否异步初始化
     * @param sensitiveWords 敏感词列表
     */
    public static void init(final Collection<String> sensitiveWords, final boolean isAsync) {
        if (isAsync) {
            ThreadKit.execAsync(() -> {
                init(sensitiveWords);
                return true;
            });
        } else {
            init(sensitiveWords);
        }
    }

    /**
     * 初始化敏感词树
     *
     * @param sensitiveWords 敏感词列表
     */
    public static void init(final Collection<String> sensitiveWords) {
        sensitiveTree.clear();
        sensitiveTree.addWords(sensitiveWords);
    }

    /**
     * 初始化敏感词树
     *
     * @param sensitiveWords 敏感词列表组成的字符串
     * @param isAsync        是否异步初始化
     * @param separator      分隔符
     */
    public static void init(final String sensitiveWords, final String separator, final boolean isAsync) {
        if (StringKit.isNotBlank(sensitiveWords)) {
            init(CharsBacker.split(sensitiveWords, separator), isAsync);
        }
    }

    /**
     * 初始化敏感词树，使用逗号分隔每个单词
     *
     * @param sensitiveWords 敏感词列表组成的字符串
     * @param isAsync        是否异步初始化
     */
    public static void init(final String sensitiveWords, final boolean isAsync) {
        init(sensitiveWords, DEFAULT_SEPARATOR, isAsync);
    }

    /**
     * 设置字符过滤规则，通过定义字符串过滤规则，过滤不需要的字符
     * 当accept为false时，此字符不参与匹配
     *
     * @param charFilter 过滤函数
     */
    public static void setCharFilter(final Predicate<Character> charFilter) {
        if (charFilter != null) {
            sensitiveTree.setCharFilter(charFilter);
        }
    }

    /**
     * 是否包含敏感词
     *
     * @param text 文本
     * @return 是否包含
     */
    public static boolean containsSensitive(final String text) {
        return sensitiveTree.isMatch(text);
    }

    /**
     * 查找敏感词，返回找到的第一个敏感词
     *
     * @param text 文本
     * @return 敏感词
     */
    public static FoundWord getFoundFirstSensitive(final String text) {
        return sensitiveTree.matchWord(text);
    }

    /**
     * 查找敏感词，返回找到的所有敏感词
     *
     * @param text 文本
     * @return 敏感词
     */
    public static List<FoundWord> getFoundAllSensitive(final String text) {
        return sensitiveTree.matchAllWords(text);
    }

    /**
     * 查找敏感词，返回找到的所有敏感词
     * 密集匹配原则：假如关键词有 ab,b，文本是abab，将匹配 [ab,b,ab]
     * 贪婪匹配（最长匹配）原则：假如关键字a,ab，最长匹配将匹配[a, ab]
     *
     * @param text           文本
     * @param isDensityMatch 是否使用密集匹配原则
     * @param isGreedMatch   是否使用贪婪匹配（最长匹配）原则
     * @return 敏感词
     */
    public static List<FoundWord> getFoundAllSensitive(final String text, final boolean isDensityMatch, final boolean isGreedMatch) {
        return sensitiveTree.matchAllWords(text, -1, isDensityMatch, isGreedMatch);
    }

    /**
     * 处理过滤文本中的敏感词，默认替换成*
     *
     * @param text 文本
     * @return 敏感词过滤处理后的文本
     */
    public static String sensitiveFilter(final String text) {
        return sensitiveFilter(text, true, null);
    }

    /**
     * 处理过滤文本中的敏感词，默认替换成*
     *
     * @param text               文本
     * @param isGreedMatch       贪婪匹配（最长匹配）原则：假如关键字a,ab，最长匹配将匹配[a, ab]
     * @param sensitiveProcessor 敏感词处理器，默认按匹配内容的字符数替换成*
     * @return 敏感词过滤处理后的文本
     */
    public static String sensitiveFilter(final String text, final boolean isGreedMatch, SensitiveProcessor sensitiveProcessor) {
        if (StringKit.isEmpty(text)) {
            return text;
        }

        // 敏感词过滤场景下，不需要密集匹配
        final List<FoundWord> foundWordList = getFoundAllSensitive(text, true, isGreedMatch);
        if (CollKit.isEmpty(foundWordList)) {
            return text;
        }
        sensitiveProcessor = sensitiveProcessor == null ? new SensitiveProcessor() {
        } : sensitiveProcessor;

        final Map<Integer, FoundWord> foundWordMap = new HashMap<>(foundWordList.size(), 1);
        foundWordList.forEach(foundWord -> foundWordMap.put(foundWord.getBeginIndex(), foundWord));
        final int length = text.length();
        final StringBuilder textStringBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            final FoundWord fw = foundWordMap.get(i);
            if (fw != null) {
                textStringBuilder.append(sensitiveProcessor.process(fw));
                i = fw.getEndIndex();
            } else {
                textStringBuilder.append(text.charAt(i));
            }
        }
        return textStringBuilder.toString();
    }

}
