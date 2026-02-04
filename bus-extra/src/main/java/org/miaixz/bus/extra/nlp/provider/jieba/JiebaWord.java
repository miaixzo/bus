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
package org.miaixz.bus.extra.nlp.provider.jieba;

import java.io.Serial;

import org.miaixz.bus.extra.nlp.NLPWord;

import com.huaban.analysis.jieba.SegToken;

/**
 * Wrapper class for a single word (SegToken) from Jieba word segmentation. This class adapts the Jieba {@link SegToken}
 * object to the common {@link NLPWord} interface, providing a unified way to access segmented word information.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JiebaWord implements NLPWord {

    /**
     * The serialization version identifier for this class.
     */
    @Serial
    private static final long serialVersionUID = 2852286255273L;

    /**
     * The underlying Jieba {@link SegToken} object.
     */
    private final SegToken segToken;

    /**
     * Constructs a {@code JiebaWord} instance by wrapping a Jieba {@link SegToken}.
     *
     * @param segToken The {@link SegToken} object from Jieba word segmentation.
     */
    public JiebaWord(final SegToken segToken) {
        this.segToken = segToken;
    }

    /**
     * Retrieves the text of the word from the wrapped Jieba {@link SegToken}.
     *
     * @return The text of the word as a {@link String}.
     */
    @Override
    public String getText() {
        return segToken.word;
    }

    /**
     * Retrieves the starting character offset of this word within the original text. This delegates to the
     * {@code startOffset} field of the Jieba {@link SegToken}.
     *
     * @return The starting position (inclusive) of the word.
     */
    @Override
    public int getStartOffset() {
        return segToken.startOffset;
    }

    /**
     * Retrieves the ending character offset of this word within the original text. This delegates to the
     * {@code endOffset} field of the Jieba {@link SegToken}.
     *
     * @return The ending position (exclusive) of the word.
     */
    @Override
    public int getEndOffset() {
        return segToken.endOffset;
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
