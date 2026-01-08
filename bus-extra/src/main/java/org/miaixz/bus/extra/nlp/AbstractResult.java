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
package org.miaixz.bus.extra.nlp;

import java.util.Iterator;

import org.miaixz.bus.core.center.iterator.ComputeIterator;

/**
 * Abstract base class that decorates a regular result class (one that does not inherently implement {@link Iterator})
 * as an {@link NLPResult}. This class simplifies the implementation of {@link NLPResult} by requiring subclasses to
 * only implement the {@link #nextWord()} method to provide the next segmented word.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractResult extends ComputeIterator<NLPWord> implements NLPResult {

    /**
     * Retrieves the next word from the word segmentation result. This method should be implemented by subclasses to
     * provide the logic for fetching the next {@link NLPWord}. Returns {@code null} when there are no more words to be
     * processed.
     *
     * @return The next {@link NLPWord} in the sequence, or {@code null} if the iteration has no more elements.
     */
    protected abstract NLPWord nextWord();

    /**
     * Computes the next element in the iteration. This method is called by {@link ComputeIterator} to get the next
     * word. It delegates to the abstract {@link #nextWord()} method.
     *
     * @return The next {@link NLPWord} or {@code null} if the iteration is complete.
     */
    @Override
    protected NLPWord computeNext() {
        return nextWord();
    }

}
