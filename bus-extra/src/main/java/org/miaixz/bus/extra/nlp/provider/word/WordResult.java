/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.extra.nlp.provider.word;

import java.util.Iterator;
import java.util.List;

import org.miaixz.bus.extra.nlp.NLPResult;
import org.miaixz.bus.extra.nlp.NLPWord;

/**
 * Implementation of {@link NLPResult} for Word word segmentation results. This class wraps a {@link List} of
 * {@link org.apdplat.word.segmentation.Word} objects from the Word library and provides an iterator over
 * {@link NLPWord} objects, adapting Word's results to the common interface. Project homepage:
 * <a href="https://github.com/ysc/word">https://github.com/ysc/word</a>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class WordResult implements NLPResult {

    /**
     * The iterator over the Word library's {@link org.apdplat.word.segmentation.Word} objects.
     */
    private final Iterator<org.apdplat.word.segmentation.Word> wordIter;

    /**
     * Constructs a {@code WordResult} instance by wrapping a list of segmentation results from the Word library.
     *
     * @param result A {@link List} of {@link org.apdplat.word.segmentation.Word} objects obtained from Word
     *               segmentation.
     */
    public WordResult(final List<org.apdplat.word.segmentation.Word> result) {
        this.wordIter = result.iterator();
    }

    /**
     * Checks if there are more words in the segmentation result.
     *
     * @return {@code true} if there are more words; {@code false} otherwise.
     */
    @Override
    public boolean hasNext() {
        return this.wordIter.hasNext();
    }

    /**
     * Returns the next word in the segmentation result as an {@link NLPWord}. This method wraps the
     * {@link org.apdplat.word.segmentation.Word} into a {@link WordWord}.
     *
     * @return The next {@link NLPWord} in the iteration.
     */
    @Override
    public NLPWord next() {
        return new WordWord(this.wordIter.next());
    }

    /**
     * Removes the last word returned by this iterator from the underlying collection. This operation is delegated to
     * the underlying iterator.
     */
    @Override
    public void remove() {
        this.wordIter.remove();
    }

}
