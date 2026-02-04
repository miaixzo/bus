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

import java.io.Serial;

import org.miaixz.bus.extra.nlp.NLPWord;

import com.mayabot.nlp.segment.WordTerm;

/**
 * Wrapper class for a single word (WordTerm) from Mynlp word segmentation. This class adapts the Mynlp {@link WordTerm}
 * object to the common {@link NLPWord} interface, providing a unified way to access segmented word information.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MynlpWord implements NLPWord {

    /**
     * The serialization version identifier for this class.
     */
    @Serial
    private static final long serialVersionUID = 2852286667336L;

    /**
     * The underlying Mynlp {@link WordTerm} object.
     */
    private final WordTerm word;

    /**
     * Constructs a {@code MynlpWord} instance by wrapping a Mynlp {@link WordTerm}.
     *
     * @param word The {@link WordTerm} object from Mynlp word segmentation.
     */
    public MynlpWord(final WordTerm word) {
        this.word = word;
    }

    /**
     * Retrieves the text of the word from the wrapped Mynlp {@link WordTerm}.
     *
     * @return The text of the word as a {@link String}.
     */
    @Override
    public String getText() {
        return word.getWord();
    }

    /**
     * Retrieves the starting character offset of this word within the original text. This delegates to the
     * {@code offset} field of the Mynlp {@link WordTerm}.
     *
     * @return The starting position (inclusive) of the word.
     */
    @Override
    public int getStartOffset() {
        return this.word.offset;
    }

    /**
     * Retrieves the ending character offset of this word within the original text. This is calculated based on the
     * starting offset and the length of the word.
     *
     * @return The ending position (exclusive) of the word.
     */
    @Override
    public int getEndOffset() {
        return getStartOffset() + word.word.length();
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
