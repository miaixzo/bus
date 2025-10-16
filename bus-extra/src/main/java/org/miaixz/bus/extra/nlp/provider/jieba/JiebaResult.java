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
package org.miaixz.bus.extra.nlp.provider.jieba;

import java.util.Iterator;
import java.util.List;

import org.miaixz.bus.extra.nlp.NLPResult;
import org.miaixz.bus.extra.nlp.NLPWord;

import com.huaban.analysis.jieba.SegToken;

/**
 * Implementation of {@link NLPResult} for Jieba word segmentation results. This class wraps a {@link List} of
 * {@link SegToken} objects from Jieba and provides an iterator over {@link NLPWord} objects, adapting Jieba's results
 * to the common interface. Project homepage:
 * <a href="https://github.com/huaban/jieba-analysis">https://github.com/huaban/jieba-analysis</a>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JiebaResult implements NLPResult {

    /**
     * The iterator over Jieba {@link SegToken} objects, representing the segmented words.
     */
    private final Iterator<SegToken> result;

    /**
     * Constructs a {@code JiebaResult} instance by wrapping a list of segmentation results from Jieba.
     *
     * @param segTokenList A {@link List} of {@link SegToken} objects obtained from Jieba segmentation.
     */
    public JiebaResult(final List<SegToken> segTokenList) {
        this.result = segTokenList.iterator();
    }

    /**
     * Checks if there are more words in the segmentation result.
     *
     * @return {@code true} if there are more words; {@code false} otherwise.
     */
    @Override
    public boolean hasNext() {
        return result.hasNext();
    }

    /**
     * Returns the next word in the segmentation result as an {@link NLPWord}. This method wraps the Jieba
     * {@link SegToken} into a {@link JiebaWord}.
     *
     * @return The next {@link NLPWord} in the iteration.
     */
    @Override
    public NLPWord next() {
        return new JiebaWord(result.next());
    }

    /**
     * Removes the last word returned by this iterator from the underlying collection. This operation is delegated to
     * the underlying iterator.
     */
    @Override
    public void remove() {
        result.remove();
    }

}
