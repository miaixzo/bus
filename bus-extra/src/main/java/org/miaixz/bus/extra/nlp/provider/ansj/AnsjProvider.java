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
package org.miaixz.bus.extra.nlp.provider.ansj;

import org.ansj.splitWord.Analysis;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.nlp.NLPProvider;
import org.miaixz.bus.extra.nlp.NLPResult;

/**
 * Ansj word segmentation engine implementation. This class serves as a concrete {@link NLPProvider} for the Ansj NLP
 * library, adapting its word segmentation capabilities to the common NLP interface. Project homepage:
 * <a href="https://github.com/NLPchina/ansj_seg">https://github.com/NLPchina/ansj_seg</a>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class AnsjProvider implements NLPProvider {

    /**
     * The underlying Ansj {@link Analysis} instance used for performing word segmentation.
     */
    private final Analysis analysis;

    /**
     * Constructs a new {@code AnsjProvider} instance with the default Ansj {@link ToAnalysis} tokenizer.
     */
    public AnsjProvider() {
        this(new ToAnalysis());
    }

    /**
     * Constructs a new {@code AnsjProvider} instance with a custom Ansj {@link Analysis} implementation.
     *
     * @param analysis The custom Ansj {@link Analysis} object to use for word segmentation.
     */
    public AnsjProvider(final Analysis analysis) {
        this.analysis = analysis;
    }

    /**
     * Performs word segmentation on the given text using the configured Ansj {@link Analysis} instance. The result is
     * wrapped in an {@link AnsjResult} to conform to the {@link NLPResult} interface.
     *
     * @param text The input text {@link CharSequence} to be segmented.
     * @return An {@link NLPResult} object containing the segmented words from Ansj.
     */
    @Override
    public NLPResult parse(final CharSequence text) {
        return new AnsjResult(analysis.parseStr(StringKit.toStringOrEmpty(text)));
    }

}
