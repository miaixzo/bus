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
package org.miaixz.bus.extra.nlp.provider.mmseg;

import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.nlp.NLPProvider;
import org.miaixz.bus.extra.nlp.NLPResult;
import org.miaixz.bus.logger.Logger;

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
 * @since Java 21+
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
        Logger.info(
                true,
                "Extra",
                "component=nlp, Mmseg provider initialization started: segmenterType={}",
                seg == null ? "null" : seg.getClass().getSimpleName());
        this.seg = seg;
        Logger.info(
                false,
                "Extra",
                "component=nlp, Mmseg provider initialized: segmenterType={}",
                this.seg == null ? "null" : this.seg.getClass().getSimpleName());
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
        Logger.debug(
                true,
                "Extra",
                "component=nlp, Mmseg parse started: textLength={}, segmenterType={}",
                text == null ? 0 : text.length(),
                seg == null ? "null" : seg.getClass().getSimpleName());
        final MMSeg mmSeg = new MMSeg(StringKit.getReader(text), seg);
        Logger.debug(
                false,
                "Extra",
                "component=nlp, Mmseg parse completed: textLength={}, resultPresent={}",
                text == null ? 0 : text.length(),
                mmSeg != null);
        return new MmsegResult(mmSeg);
    }

}
