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
package org.miaixz.bus.extra.nlp.provider.hanlp;

import java.util.Iterator;
import java.util.List;

import org.miaixz.bus.extra.nlp.NLPResult;
import org.miaixz.bus.extra.nlp.NLPWord;

import com.hankcs.hanlp.seg.common.Term;

/**
 * Implementation of {@link NLPResult} for HanLP word segmentation results. This class wraps a {@link List} of
 * {@link Term} objects from HanLP and provides an iterator over {@link NLPWord} objects, adapting HanLP's results to
 * the common interface. Project homepage: <a href="https://github.com/hankcs/HanLP">https://github.com/hankcs/HanLP</a>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class HanLPResult implements NLPResult {

    /**
     * The iterator over HanLP {@link Term} objects, representing the segmented words.
     */
    private final Iterator<Term> result;

    /**
     * Constructs a {@code HanLPResult} instance by wrapping a list of segmentation results from HanLP.
     *
     * @param termList A {@link List} of {@link Term} objects obtained from HanLP segmentation.
     */
    public HanLPResult(final List<Term> termList) {
        this.result = termList.iterator();
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
     * Returns the next word in the segmentation result as an {@link NLPWord}. This method wraps the HanLP {@link Term}
     * into a {@link HanLPWord}.
     *
     * @return The next {@link NLPWord} in the iteration.
     */
    @Override
    public NLPWord next() {
        return new HanLPWord(result.next());
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
