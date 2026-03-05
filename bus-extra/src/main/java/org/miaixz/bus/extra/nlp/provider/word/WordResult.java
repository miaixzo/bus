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
