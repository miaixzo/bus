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
package org.miaixz.bus.core.text.dfa;

import org.miaixz.bus.core.lang.range.DefaultSegment;

/**
 * 匹配到的单词，包含单词，text中匹配单词的内容，以及匹配内容在text中的下标， 下标可以用来做单词的进一步处理，如果替换成**
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FoundWord extends DefaultSegment<Integer> {

    /**
     * 生效的单词，即单词树中的词
     */
    private final String word;
    /**
     * 单词匹配到的内容，即文中的单词
     */
    private final String foundWord;

    /**
     * 构造
     *
     * @param word       生效的单词，即单词树中的词
     * @param foundWord  单词匹配到的内容，即文中的单词
     * @param startIndex 起始位置（包含）
     * @param endIndex   结束位置（包含）
     */
    public FoundWord(final String word, final String foundWord, final int startIndex, final int endIndex) {
        super(startIndex, endIndex);
        this.word = word;
        this.foundWord = foundWord;
    }

    /**
     * 获取生效的单词，即单词树中的词
     *
     * @return 生效的单词
     */
    public String getWord() {
        return word;
    }

    /**
     * 获取单词匹配到的内容，即文中的单词
     *
     * @return 单词匹配到的内容
     */
    public String getFoundWord() {
        return foundWord;
    }

    /**
     * 默认的，只输出匹配到的关键字
     *
     * @return 匹配到的关键字
     */
    @Override
    public String toString() {
        return this.foundWord;
    }

}
