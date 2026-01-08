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
package org.miaixz.bus.extra.nlp.provider.ansj;

import java.util.Iterator;

import org.ansj.domain.Term;
import org.miaixz.bus.extra.nlp.NLPResult;
import org.miaixz.bus.extra.nlp.NLPWord;

/**
 * Implementation of {@link NLPResult} for Ansj word segmentation results. This class wraps the
 * {@link org.ansj.domain.Result} from Ansj and provides an iterator over {@link NLPWord} objects, adapting Ansj's
 * {@link Term} to the common interface. Project homepage:
 * <a href="https://github.com/NLPchina/ansj_seg">https://github.com/NLPchina/ansj_seg</a>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AnsjResult implements NLPResult {

    /**
     * The iterator over Ansj {@link Term} objects, representing the segmented words.
     */
    private final Iterator<Term> result;

    /**
     * Constructs an {@code AnsjResult} instance by wrapping an Ansj segmentation result.
     *
     * @param ansjResult The {@link org.ansj.domain.Result} object obtained from Ansj word segmentation.
     */
    public AnsjResult(final org.ansj.domain.Result ansjResult) {
        this.result = ansjResult.iterator();
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
     * Returns the next word in the segmentation result as an {@link NLPWord}. This method wraps the Ansj {@link Term}
     * into an {@link AnsjWord}.
     *
     * @return The next {@link NLPWord} in the iteration.
     */
    @Override
    public NLPWord next() {
        return new AnsjWord(result.next());
    }

    /**
     * Removes the last word returned by this iterator from the underlying collection. This operation is optional and
     * may throw an {@link UnsupportedOperationException}.
     */
    @Override
    public void remove() {
        result.remove();
    }

}
