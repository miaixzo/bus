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

import org.miaixz.bus.core.lang.Symbol;

/**
 * Interface for processing sensitive words. Implementations can define custom logic for how sensitive words are
 * handled, such as replacing them with a mask character. The default implementation replaces each character of the
 * found sensitive word with an asterisk.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface SensitiveProcessor {

    /**
     * Processes a found sensitive word. The default implementation replaces each character of the
     * {@link FoundWord#getFoundWord()} with an asterisk ({@code *}).
     *
     * @param foundWord The {@link FoundWord} object representing the sensitive word found in the text.
     * @return The processed string, typically a masked version of the sensitive word.
     */
    default String process(final FoundWord foundWord) {
        final int length = foundWord.getFoundWord().length();
        return Symbol.STAR.repeat(length);
    }

}
