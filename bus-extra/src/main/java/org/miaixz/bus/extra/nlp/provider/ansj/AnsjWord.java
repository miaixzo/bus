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

import java.io.Serial;

import org.ansj.domain.Term;
import org.miaixz.bus.extra.nlp.NLPWord;

/**
 * Wrapper for a word in Ansj segmentation.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class AnsjWord implements NLPWord {

    @Serial
    private static final long serialVersionUID = 2852285815563L;

    /**
     * The Term object from the Ansj library.
     */
    private final Term term;

    /**
     * Constructor.
     *
     * @param term The {@link Term} object.
     */
    public AnsjWord(final Term term) {
        this.term = term;
    }

    /**
     * Gets the text of the word.
     *
     * @return The text of the word.
     */
    @Override
    public String getText() {
        return term.getName();
    }

    /**
     * Gets the start offset of the word in the original text.
     *
     * @return The start offset.
     */
    @Override
    public int getStartOffset() {
        return this.term.getOffe();
    }

    /**
     * Gets the end offset of the word in the original text.
     *
     * @return The end offset.
     */
    @Override
    public int getEndOffset() {
        return getStartOffset() + getText().length();
    }

    /**
     * Returns the text of the word.
     *
     * @return The text of the word.
     */
    @Override
    public String toString() {
        return getText();
    }

}
