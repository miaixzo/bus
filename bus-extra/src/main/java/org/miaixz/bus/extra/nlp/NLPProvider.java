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

import org.miaixz.bus.core.Provider;
import org.miaixz.bus.core.lang.EnumValue;

/**
 * Interface definition for Natural Language Processing (NLP) word segmentation engines. Users implement this interface
 * to adapt specific word segmentation engines, such as Ansj, HanLP, etc. Since the engine typically uses a singleton
 * pattern, implementations are required to be thread-safe to handle concurrent requests for word segmentation.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface NLPProvider extends Provider {

    /**
     * Performs word segmentation on the given text and returns the result. Implementations should process the input
     * text and produce a structured result containing the segmented words.
     *
     * @param text The input text {@link CharSequence} to be segmented.
     * @return An {@link NLPResult} implementation containing the segmented words.
     */
    NLPResult parse(CharSequence text);

    /**
     * Returns the type of this NLP provider. By default, it returns {@link EnumValue.Povider#NLP}.
     *
     * @return The type of the provider, typically {@link EnumValue.Povider#NLP}.
     */
    @Override
    default Object type() {
        return EnumValue.Povider.NLP;
    }

}
