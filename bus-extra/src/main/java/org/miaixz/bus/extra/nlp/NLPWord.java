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
package org.miaixz.bus.extra.nlp;

import java.io.Serializable;

/**
 * Represents a single word or token extracted during Natural Language Processing (NLP) word segmentation. This
 * interface defines methods to access the textual content and positional information of the segmented word.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface NLPWord extends Serializable {

    /**
     * Retrieves the textual content of this word.
     *
     * @return The text of the word as a {@link String}.
     */
    String getText();

    /**
     * Retrieves the starting character offset of this word within the original text. The offset is 0-based.
     *
     * @return The starting position (inclusive) of the word.
     */
    int getStartOffset();

    /**
     * Retrieves the ending character offset of this word within the original text. The offset is 0-based and exclusive.
     *
     * @return The ending position (exclusive) of the word.
     */
    int getEndOffset();

}
