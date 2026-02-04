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
package org.miaixz.bus.extra.nlp.provider.jcseg;

import java.io.Serial;

import org.lionsoul.jcseg.IWord;
import org.miaixz.bus.extra.nlp.NLPWord;

/**
 * Wrapper class for a single word (IWord) from Jcseg word segmentation. This class adapts the Jcseg {@link IWord}
 * object to the common {@link NLPWord} interface, providing a unified way to access segmented word information.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JcsegWord implements NLPWord {

    /**
     * The serialization version identifier for this class.
     */
    @Serial
    private static final long serialVersionUID = 2852286128127L;

    /**
     * The underlying Jcseg {@link IWord} object.
     */
    private final IWord word;

    /**
     * Constructs a {@code JcsegWord} instance by wrapping a Jcseg {@link IWord}.
     *
     * @param word The {@link IWord} object from Jcseg word segmentation.
     */
    public JcsegWord(final IWord word) {
        this.word = word;
    }

    /**
     * Retrieves the text of the word from the wrapped Jcseg {@link IWord}.
     *
     * @return The text of the word as a {@link String}.
     */
    @Override
    public String getText() {
        return word.getValue();
    }

    /**
     * Retrieves the starting character offset of this word within the original text. This delegates to the
     * {@code getPosition()} method of the Jcseg {@link IWord}.
     *
     * @return The starting position (inclusive) of the word.
     */
    @Override
    public int getStartOffset() {
        return word.getPosition();
    }

    /**
     * Retrieves the ending character offset of this word within the original text. This is calculated based on the
     * starting offset and the length of the word.
     *
     * @return The ending position (exclusive) of the word.
     */
    @Override
    public int getEndOffset() {
        return getStartOffset() + word.getLength();
    }

    /**
     * Returns the textual representation of this word, which is the same as {@link #getText()}.
     *
     * @return The text of the word.
     */
    @Override
    public String toString() {
        return getText();
    }

}
