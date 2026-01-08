/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.text.dfa;

import org.miaixz.bus.core.lang.range.DefaultSegment;

/**
 * Represents a word found during a DFA-based search. This class stores the actual word from the dictionary, the content
 * matched in the text, and its start and end indices within the text. The indices can be used for further processing,
 * such as replacing the found word.
 *
 * @author Kimi Liu
 * @since Java 17+
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
