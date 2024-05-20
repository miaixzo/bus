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
package org.miaixz.bus.extra.nlp.provider.ikanalyzer;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.extra.nlp.AbstractResult;
import org.miaixz.bus.extra.nlp.NLPWord;
import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

import java.io.IOException;

/**
 * IKAnalyzer分词结果实现
 * 项目地址：https://github.com/yozhao/IKAnalyzer
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class IKAnalyzerResult extends AbstractResult {

    private final IKSegmenter seg;

    /**
     * 构造
     *
     * @param seg 分词结果
     */
    public IKAnalyzerResult(final IKSegmenter seg) {
        this.seg = seg;
    }

    @Override
    protected NLPWord nextWord() {
        final Lexeme next;
        try {
            next = this.seg.next();
        } catch (final IOException e) {
            throw new InternalException(e);
        }
        if (null == next) {
            return null;
        }
        return new IKAnalyzerWord(next);
    }

}
