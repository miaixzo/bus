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
package org.miaixz.bus.extra.nlp.provider.jieba;

import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.nlp.NLPProvider;
import org.miaixz.bus.extra.nlp.NLPResult;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.JiebaSegmenter.SegMode;

/**
 * Jieba word segmentation engine implementation. This class serves as a concrete {@link NLPProvider} for the Jieba NLP
 * library, adapting its word segmentation capabilities to the common NLP interface. The underlying
 * {@link JiebaSegmenter#process(String, SegMode)} method is thread-safe. Project homepage:
 * <a href="https://github.com/huaban/jieba-analysis">https://github.com/huaban/jieba-analysis</a>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class JiebaProvider implements NLPProvider {

    /**
     * The underlying Jieba {@link JiebaSegmenter} instance used for performing word segmentation.
     */
    private final JiebaSegmenter jiebaSegmenter;
    /**
     * The segmentation mode used by the Jieba segmenter (e.g., SEARCH or INDEX).
     */
    private final SegMode mode;

    /**
     * Constructs a new {@code JiebaProvider} instance with the default segmentation mode, which is
     * {@link SegMode#SEARCH}.
     */
    public JiebaProvider() {
        this(SegMode.SEARCH);
    }

    /**
     * Constructs a new {@code JiebaProvider} instance with a specified segmentation mode.
     *
     * @param mode The {@link SegMode} to use for word segmentation (e.g., {@link SegMode#SEARCH} or
     *             {@link SegMode#INDEX}).
     */
    public JiebaProvider(final SegMode mode) {
        this.jiebaSegmenter = new JiebaSegmenter();
        this.mode = mode;
    }

    /**
     * Performs word segmentation on the given text using the configured Jieba {@link JiebaSegmenter} instance. The
     * result is wrapped in a {@link JiebaResult} to conform to the {@link NLPResult} interface.
     *
     * @param text The input text {@link CharSequence} to be segmented.
     * @return An {@link NLPResult} object containing the segmented words from Jieba.
     */
    @Override
    public NLPResult parse(final CharSequence text) {
        return new JiebaResult(jiebaSegmenter.process(StringKit.toStringOrEmpty(text), mode));
    }

}
