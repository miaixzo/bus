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
package org.miaixz.bus.extra.nlp;

/**
 * Utility class for Natural Language Processing (NLP) word segmentation. This class provides a facade for accessing
 * various NLP segmentation engines, allowing for easy text parsing and word extraction.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class NLPKit {

    /**
     * Performs word segmentation on the given text using the default NLP engine.
     *
     * @param text The input text {@link String} to be segmented.
     * @return An {@link NLPResult} object containing the segmented words and other NLP analysis results.
     */
    public static NLPResult parse(final String text) {
        return getEngine().parse(text);
    }

    /**
     * Automatically creates and retrieves the corresponding word segmentation engine object. The engine is determined
     * based on the NLP engine JARs introduced by the user via SPI mechanism.
     *
     * @return An {@link NLPProvider} instance, representing the chosen NLP segmentation engine.
     */
    public static NLPProvider getEngine() {
        return NLPFactory.getEngine();
    }

    /**
     * Creates a word segmentation engine object with the specified engine name. This allows for explicit selection of
     * an NLP provider when multiple are available.
     *
     * @param engineName The name of the NLP engine to create (e.g., "Ansj", "HanLP").
     * @return An {@link NLPProvider} instance corresponding to the given engine name.
     */
    public static NLPProvider createEngine(final String engineName) {
        return NLPFactory.createEngine(engineName);
    }

}
