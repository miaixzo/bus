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
