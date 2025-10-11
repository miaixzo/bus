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
package org.miaixz.bus.extra.nlp.provider.mmseg;

import java.io.IOException;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.extra.nlp.AbstractResult;
import org.miaixz.bus.extra.nlp.NLPWord;

import com.chenlb.mmseg4j.MMSeg;

/**
 * mmseg4j word segmentation result implementation. This class adapts the mmseg4j {@link MMSeg} result to the common
 * {@link org.miaixz.bus.extra.nlp.NLPResult} interface. Project homepage:
 * <a href="https://github.com/chenlb/mmseg4j-core">https://github.com/chenlb/mmseg4j-core</a>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MmsegResult extends AbstractResult {

    /**
     * The underlying mmseg4j {@link MMSeg} instance, which provides the segmentation result.
     */
    private final MMSeg mmSeg;

    /**
     * Constructs a {@code MmsegResult} instance by wrapping an mmseg4j segmentation result.
     *
     * @param mmSeg The {@link MMSeg} object obtained from mmseg4j word segmentation.
     */
    public MmsegResult(final MMSeg mmSeg) {
        this.mmSeg = mmSeg;
    }

    /**
     * Retrieves the next word from the mmseg4j segmentation result. This method calls the {@code next()} method of the
     * underlying {@link MMSeg} and wraps the resulting {@link com.chenlb.mmseg4j.Word} in a {@link MmsegWord}.
     *
     * @return The next {@link NLPWord} in the sequence, or {@code null} if the iteration has no more elements.
     * @throws InternalException if an {@link IOException} occurs during the operation.
     */
    @Override
    protected NLPWord nextWord() {
        final com.chenlb.mmseg4j.Word next;
        try {
            next = this.mmSeg.next();
        } catch (final IOException e) {
            throw new InternalException(e);
        }
        if (null == next) {
            return null;
        }
        return new MmsegWord(next);
    }

}
