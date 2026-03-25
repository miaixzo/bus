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

import java.io.Serial;

import org.miaixz.bus.extra.nlp.NLPWord;

/**
 * Wrapper class for a single word from the Word word segmentation library. This class adapts the
 * {@link org.apdplat.word.segmentation.Word} object to the common {@link NLPWord} interface, providing a unified way to
 * access segmented word information.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class WordWord implements NLPWord {

    /**
     * The serialization version identifier for this class.
     */
    @Serial
    private static final long serialVersionUID = 2852286932330L;

    /**
     * The underlying {@link org.apdplat.word.segmentation.Word} object from the Word library.
     */
    private final org.apdplat.word.segmentation.Word word;

    /**
     * Constructs a {@code WordWord} instance by wrapping a Word library's {@link org.apdplat.word.segmentation.Word}
     * object.
     *
     * @param word The {@link org.apdplat.word.segmentation.Word} object from Word word segmentation.
     */
    public WordWord(final org.apdplat.word.segmentation.Word word) {
        this.word = word;
    }

    /**
     * Retrieves the text of the word from the wrapped Word library's {@link org.apdplat.word.segmentation.Word}.
     *
     * @return The text of the word as a {@link String}.
     */
    @Override
    public String getText() {
        return word.getText();
    }

    /**
     * Retrieves the starting character offset of this word within the original text. Note: The Word library's
     * {@link org.apdplat.word.segmentation.Word} does not directly provide offset information, so this method returns
     * -1.
     *
     * @return The starting position (inclusive) of the word, or -1 if not available.
     */
    @Override
    public int getStartOffset() {
        return -1;
    }

    /**
     * Retrieves the ending character offset of this word within the original text. Note: The Word library's
     * {@link org.apdplat.word.segmentation.Word} does not directly provide offset information, so this method returns
     * -1.
     *
     * @return The ending position (exclusive) of the word, or -1 if not available.
     */
    @Override
    public int getEndOffset() {
        return -1;
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
