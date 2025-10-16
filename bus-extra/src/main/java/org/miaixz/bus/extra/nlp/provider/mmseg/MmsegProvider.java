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
package org.miaixz.bus.extra.nlp.provider.mmseg;

import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.nlp.NLPProvider;
import org.miaixz.bus.extra.nlp.NLPResult;

import com.chenlb.mmseg4j.ComplexSeg;
import com.chenlb.mmseg4j.Dictionary;
import com.chenlb.mmseg4j.MMSeg;
import com.chenlb.mmseg4j.Seg;

/**
 * mmseg4j word segmentation engine implementation. This class serves as a concrete {@link NLPProvider} for the mmseg4j
 * NLP library. Note that {@link MMSeg} is not thread-safe, so a new instance is created for each segmentation request.
 * Project homepage: <a href="https://github.com/chenlb/mmseg4j-core">https://github.com/chenlb/mmseg4j-core</a>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MmsegProvider implements NLPProvider {

    /**
     * The underlying mmseg4j {@link Seg} instance, which defines the segmentation algorithm (e.g., Complex, Simple).
     */
    private final Seg seg;

    /**
     * Constructs a new {@code MmsegProvider} instance with the default segmentation algorithm, which is
     * {@link ComplexSeg} using the default singleton dictionary.
     */
    public MmsegProvider() {
        this(new ComplexSeg(Dictionary.getInstance()));
    }

    /**
     * Constructs a new {@code MmsegProvider} instance with a specified segmentation algorithm.
     *
     * @param seg The {@link Seg} algorithm to use for word segmentation (e.g., {@link ComplexSeg},
     *            {@link com.chenlb.mmseg4j.SimpleSeg}).
     */
    public MmsegProvider(final Seg seg) {
        this.seg = seg;
    }

    /**
     * Performs word segmentation on the given text using the configured mmseg4j {@link Seg} instance. A new
     * {@link MMSeg} instance is created for each call to ensure thread safety. The result is wrapped in an
     * {@link MmsegResult} to conform to the {@link NLPResult} interface.
     *
     * @param text The input text {@link CharSequence} to be segmented.
     * @return An {@link NLPResult} object containing the segmented words from mmseg4j.
     */
    @Override
    public NLPResult parse(final CharSequence text) {
        final MMSeg mmSeg = new MMSeg(StringKit.getReader(text), seg);
        return new MmsegResult(mmSeg);
    }

}
