/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
