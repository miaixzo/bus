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
package org.miaixz.bus.extra.nlp.provider.word;

import org.apdplat.word.segmentation.Segmentation;
import org.apdplat.word.segmentation.SegmentationAlgorithm;
import org.apdplat.word.segmentation.SegmentationFactory;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.nlp.NLPProvider;
import org.miaixz.bus.extra.nlp.NLPResult;

/**
 * Word word segmentation engine implementation. This class serves as a concrete {@link NLPProvider} for the Word NLP
 * library, adapting its word segmentation capabilities to the common NLP interface. The underlying {@link Segmentation}
 * is thread-safe. Project homepage: <a href="https://github.com/ysc/word">https://github.com/ysc/word</a>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class WordProvider implements NLPProvider {

    /**
     * The underlying Word {@link Segmentation} instance used for performing word segmentation.
     */
    private final Segmentation segmentation;

    /**
     * Constructs a new {@code WordProvider} instance with the default segmentation algorithm, which is
     * {@link SegmentationAlgorithm#BidirectionalMaximumMatching}.
     */
    public WordProvider() {
        this(SegmentationAlgorithm.BidirectionalMaximumMatching);
    }

    /**
     * Constructs a new {@code WordProvider} instance with a specified segmentation algorithm.
     *
     * @param algorithm The {@link SegmentationAlgorithm} to use for word segmentation.
     */
    public WordProvider(final SegmentationAlgorithm algorithm) {
        this(SegmentationFactory.getSegmentation(algorithm));
    }

    /**
     * Constructs a new {@code WordProvider} instance with a custom {@link Segmentation} implementation.
     *
     * @param segmentation The custom {@link Segmentation} object to use for word segmentation.
     */
    public WordProvider(final Segmentation segmentation) {
        this.segmentation = segmentation;
    }

    /**
     * Performs word segmentation on the given text using the configured Word {@link Segmentation} instance. The result
     * is wrapped in a {@link WordResult} to conform to the {@link NLPResult} interface.
     *
     * @param text The input text {@link CharSequence} to be segmented.
     * @return An {@link NLPResult} object containing the segmented words from the Word library.
     */
    @Override
    public NLPResult parse(final CharSequence text) {
        return new WordResult(this.segmentation.seg(StringKit.toStringOrEmpty(text)));
    }

}
