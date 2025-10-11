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
 * @since Java 17+
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
