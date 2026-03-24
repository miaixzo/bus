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
package org.miaixz.bus.extra.nlp;

import java.util.Iterator;

import org.miaixz.bus.core.center.iterator.ComputeIterator;

/**
 * Abstract base class that decorates a regular result class (one that does not inherently implement {@link Iterator})
 * as an {@link NLPResult}. This class simplifies the implementation of {@link NLPResult} by requiring subclasses to
 * only implement the {@link #nextWord()} method to provide the next segmented word.
 *
 * @author Kimi Liu
 * @since Java 21+
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
