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
