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
package org.miaixz.bus.extra.nlp.provider.hanlp;

import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.nlp.NLPProvider;
import org.miaixz.bus.extra.nlp.NLPResult;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.Segment;

/**
 * HanLP word segmentation engine implementation. This class serves as a concrete {@link NLPProvider} for the HanLP NLP
 * library, adapting its word segmentation capabilities to the common NLP interface. The underlying
 * {@link Segment#seg(String)} method is thread-safe. Project homepage:
 * <a href="https://github.com/hankcs/HanLP">https://github.com/hankcs/HanLP</a>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class HanLPProvider implements NLPProvider {

    /**
     * The underlying HanLP {@link Segment} instance used for performing word segmentation.
     */
    private final Segment seg;

    /**
     * Constructs a new {@code HanLPProvider} instance with a default HanLP segmenter, created via
     * {@link HanLP#newSegment()}.
     */
    public HanLPProvider() {
        this(HanLP.newSegment());
    }

    /**
     * Constructs a new {@code HanLPProvider} instance with a custom HanLP {@link Segment} implementation.
     *
     * @param seg The custom {@link Segment} object to use for word segmentation.
     */
    public HanLPProvider(final Segment seg) {
        this.seg = seg;
    }

    /**
     * Performs word segmentation on the given text using the configured HanLP {@link Segment} instance. The result is
     * wrapped in a {@link HanLPResult} to conform to the {@link NLPResult} interface.
     *
     * @param text The input text {@link CharSequence} to be segmented.
     * @return An {@link NLPResult} object containing the segmented words from HanLP.
     */
    @Override
    public NLPResult parse(final CharSequence text) {
        return new HanLPResult(this.seg.seg(StringKit.toStringOrEmpty(text)));
    }

}
