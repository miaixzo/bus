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
package org.miaixz.bus.extra.nlp.provider.mmseg;

import java.io.Serial;

import org.miaixz.bus.extra.nlp.NLPWord;

/**
 * Wrapper class for a single word from mmseg4j word segmentation. This class adapts the {@link com.chenlb.mmseg4j.Word}
 * object to the common {@link NLPWord} interface, providing a unified way to access segmented word information.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MmsegWord implements NLPWord {

    /**
     * The serialization version identifier for this class.
     */
    @Serial
    private static final long serialVersionUID = 2852286361503L;

    /**
     * The underlying mmseg4j {@link com.chenlb.mmseg4j.Word} object.
     */
    private final com.chenlb.mmseg4j.Word word;

    /**
     * Constructs a {@code MmsegWord} instance by wrapping an mmseg4j {@link com.chenlb.mmseg4j.Word}.
     *
     * @param word The {@link com.chenlb.mmseg4j.Word} object from mmseg4j word segmentation.
     */
    public MmsegWord(final com.chenlb.mmseg4j.Word word) {
        this.word = word;
    }

    /**
     * Retrieves the text of the word from the wrapped mmseg4j {@link com.chenlb.mmseg4j.Word}.
     *
     * @return The text of the word as a {@link String}.
     */
    @Override
    public String getText() {
        return word.getString();
    }

    /**
     * Retrieves the starting character offset of this word within the original text. This delegates to the
     * {@code getStartOffset()} method of the mmseg4j {@link com.chenlb.mmseg4j.Word}.
     *
     * @return The starting position (inclusive) of the word.
     */
    @Override
    public int getStartOffset() {
        return this.word.getStartOffset();
    }

    /**
     * Retrieves the ending character offset of this word within the original text. This delegates to the
     * {@code getEndOffset()} method of the mmseg4j {@link com.chenlb.mmseg4j.Word}.
     *
     * @return The ending position (exclusive) of the word.
     */
    @Override
    public int getEndOffset() {
        return this.word.getEndOffset();
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
