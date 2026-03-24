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
package org.miaixz.bus.extra.nlp.provider.analysis;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.nlp.NLPProvider;
import org.miaixz.bus.extra.nlp.NLPResult;

/**
 * Abstract provider for Lucene-analysis based word segmentation engines. This class provides a base implementation for
 * integrating various Lucene analyzers. Project homepage: <a href=
 * "https://github.com/apache/lucene-solr/tree/master/lucene/analysis">https://github.com/apache/lucene-solr/tree/master/lucene/analysis</a>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class AnalysisProvider implements NLPProvider {

    /**
     * The underlying Lucene {@link Analyzer} used for tokenization.
     */
    private final Analyzer analyzer;

    /**
     * Constructs a new {@code AnalysisProvider} instance with a specified Lucene {@link Analyzer}.
     *
     * @param analyzer The {@link Analyzer} to use for word segmentation.
     */
    public AnalysisProvider(final Analyzer analyzer) {
        this.analyzer = analyzer;
    }

    /**
     * Performs word segmentation on the given text using the configured Lucene {@link Analyzer}. It creates a
     * {@link TokenStream} from the text and wraps it in an {@link AnalysisResult}.
     *
     * @param text The input text {@link CharSequence} to be segmented.
     * @return An {@link NLPResult} object containing the segmented words from the Lucene analyzer.
     * @throws InternalException if an {@link IOException} occurs during token stream processing.
     */
    @Override
    public NLPResult parse(final CharSequence text) {
        final TokenStream stream;
        try {
            stream = analyzer.tokenStream("text", StringKit.toStringOrEmpty(text));
            stream.reset();
        } catch (final IOException e) {
            throw new InternalException(e);
        }
        return new AnalysisResult(stream);
    }

}
