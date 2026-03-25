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
 * @since Java 21+
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
