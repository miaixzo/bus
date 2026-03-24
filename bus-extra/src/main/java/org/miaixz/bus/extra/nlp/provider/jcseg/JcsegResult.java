/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
 * @since Java 21+
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
