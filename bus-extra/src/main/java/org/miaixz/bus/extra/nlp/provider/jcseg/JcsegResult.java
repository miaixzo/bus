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
package org.miaixz.bus.extra.nlp.provider.jcseg;

import java.io.IOException;

import org.lionsoul.jcseg.ISegment;
import org.lionsoul.jcseg.IWord;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.extra.nlp.AbstractResult;
import org.miaixz.bus.extra.nlp.NLPWord;

/**
 * Jcseg word segmentation result wrapper. This class adapts the Jcseg {@link ISegment} result to the common
 * {@link org.miaixz.bus.extra.nlp.NLPResult} interface. Project homepage:
 * <a href="https://gitee.com/lionsoul/jcseg">https://gitee.com/lionsoul/jcseg</a>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JcsegResult extends AbstractResult {

    /**
     * The underlying Jcseg {@link ISegment} instance, which provides the segmentation result.
     */
    private final ISegment result;

    /**
     * Constructs a {@code JcsegResult} instance by wrapping a Jcseg segmentation result.
     *
     * @param segment The {@link ISegment} object obtained from Jcseg word segmentation.
     */
    public JcsegResult(final ISegment segment) {
        this.result = segment;
    }

    /**
     * Retrieves the next word from the Jcseg segmentation result. This method calls the {@code next()} method of the
     * underlying {@link ISegment} and wraps the resulting {@link IWord} in a {@link JcsegWord}.
     *
     * @return The next {@link NLPWord} in the sequence, or {@code null} if the iteration has no more elements.
     * @throws InternalException if an {@link IOException} occurs during the operation.
     */
    @Override
    protected NLPWord nextWord() {
        final IWord word;
        try {
            word = this.result.next();
        } catch (final IOException e) {
            throw new InternalException(e);
        }
        if (null == word) {
            return null;
        }
        return new JcsegWord(word);
    }

}
