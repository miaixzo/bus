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
package org.miaixz.bus.extra.nlp.provider.hanlp;

import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.nlp.NLPProvider;
import org.miaixz.bus.extra.nlp.NLPResult;
import org.miaixz.bus.logger.Logger;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.Segment;

/**
 * HanLP word segmentation engine implementation. This class serves as a concrete {@link NLPProvider} for the HanLP NLP
 * library, adapting its word segmentation capabilities to the common NLP interface. The underlying
 * {@link Segment#seg(String)} method is thread-safe. Project homepage:
 * <a href="https://github.com/hankcs/HanLP">https://github.com/hankcs/HanLP</a>
 *
 * @author Kimi Liu
 * @since Java 21+
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
        Logger.info(
                true,
                "Extra",
                "HanLP provider initialization started: segmenterType={}",
                seg == null ? "null" : seg.getClass().getSimpleName());
        this.seg = seg;
        Logger.info(
                false,
                "Extra",
                "HanLP provider initialized: segmenterType={}",
                this.seg == null ? "null" : this.seg.getClass().getSimpleName());
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
        Logger.debug(true, "Extra", "HanLP parse started: textLength={}", text == null ? 0 : text.length());
        final NLPResult result = new HanLPResult(this.seg.seg(StringKit.toStringOrEmpty(text)));
        Logger.debug(
                false,
                "Extra",
                "HanLP parse completed: textLength={}, resultPresent={}",
                text == null ? 0 : text.length(),
                result != null);
        return result;
    }

}
