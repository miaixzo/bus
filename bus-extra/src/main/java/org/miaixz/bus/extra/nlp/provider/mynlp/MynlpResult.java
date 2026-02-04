/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.extra.nlp.provider.mynlp;

import java.util.Iterator;

import org.miaixz.bus.extra.nlp.NLPResult;
import org.miaixz.bus.extra.nlp.NLPWord;

import com.mayabot.nlp.segment.Sentence;
import com.mayabot.nlp.segment.WordTerm;

/**
 * Implementation of {@link NLPResult} for Mynlp word segmentation results. This class wraps a {@link Sentence} object
 * from Mynlp and provides an iterator over {@link NLPWord} objects, adapting Mynlp's results to the common interface.
 * Project homepage: <a href="https://github.com/mayabot/mynlp/">https://github.com/mayabot/mynlp/</a>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MynlpResult implements NLPResult {

    /**
     * The iterator over Mynlp {@link WordTerm} objects, representing the segmented words.
     */
    private final Iterator<WordTerm> result;

    /**
     * Constructs a {@code MynlpResult} instance by wrapping a segmentation result from Mynlp.
     *
     * @param sentence The {@link Sentence} object obtained from Mynlp segmentation.
     */
    public MynlpResult(final Sentence sentence) {
        this.result = sentence.iterator();
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
     * Returns the next word in the segmentation result as an {@link NLPWord}. This method wraps the Mynlp
     * {@link WordTerm} into a {@link MynlpWord}.
     *
     * @return The next {@link NLPWord} in the iteration.
     */
    @Override
    public NLPWord next() {
        return new MynlpWord(result.next());
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
