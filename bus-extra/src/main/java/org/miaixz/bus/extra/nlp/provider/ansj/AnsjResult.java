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
 * @since Java 21+
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
