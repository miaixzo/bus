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
package org.miaixz.bus.core.text.dfa;

import org.miaixz.bus.core.lang.range.DefaultSegment;

/**
 * Represents a word found during a DFA-based search. This class stores the actual word from the dictionary, the content
 * matched in the text, and its start and end indices within the text. The indices can be used for further processing,
 * such as replacing the found word.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class FoundWord extends DefaultSegment<Integer> {

    /**
     * The effective word from the dictionary (the keyword that was matched).
     */
    private final String word;
    /**
     * The actual content matched in the text, which might include stop characters if the filter allows.
     */
    private final String foundWord;

    /**
     * Constructs a {@code FoundWord} instance.
     *
     * @param word       The effective word from the dictionary.
     * @param foundWord  The content matched in the text.
     * @param startIndex The starting index (inclusive) of the matched word in the text.
     * @param endIndex   The ending index (inclusive) of the matched word in the text.
     */
    public FoundWord(final String word, final String foundWord, final int startIndex, final int endIndex) {
        super(startIndex, endIndex);
        this.word = word;
        this.foundWord = foundWord;
    }

    /**
     * Retrieves the effective word from the dictionary.
     *
     * @return The effective word.
     */
    public String getWord() {
        return word;
    }

    /**
     * Retrieves the content that was actually matched in the text.
     *
     * @return The matched content.
     */
    public String getFoundWord() {
        return foundWord;
    }

    /**
     * Returns the matched keyword as a string.
     *
     * @return The matched keyword.
     */
    @Override
    public String toString() {
        return this.foundWord;
    }

}
