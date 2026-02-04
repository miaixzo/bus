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
