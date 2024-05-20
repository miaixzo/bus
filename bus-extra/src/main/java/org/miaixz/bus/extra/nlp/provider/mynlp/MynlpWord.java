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
package org.miaixz.bus.extra.nlp.provider.mynlp;

import com.mayabot.nlp.segment.WordTerm;
import org.miaixz.bus.extra.nlp.NLPWord;

/**
 * mmseg分词中的一个单词包装
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MynlpWord implements NLPWord {

    private static final long serialVersionUID = -1L;

    private final WordTerm word;

    /**
     * 构造
     *
     * @param word {@link WordTerm}
     */
    public MynlpWord(final WordTerm word) {
        this.word = word;
    }

    @Override
    public String getText() {
        return word.getWord();
    }

    @Override
    public int getStartOffset() {
        return this.word.offset;
    }

    @Override
    public int getEndOffset() {
        return getStartOffset() + word.word.length();
    }

    @Override
    public String toString() {
        return getText();
    }

}
