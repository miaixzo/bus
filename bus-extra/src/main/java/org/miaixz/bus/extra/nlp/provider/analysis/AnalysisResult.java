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
package org.miaixz.bus.extra.nlp.provider.analysis;

import java.io.IOException;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.extra.nlp.AbstractResult;
import org.miaixz.bus.extra.nlp.NLPWord;

/**
 * Abstract result wrapper for Lucene-analysis word segmentation. This class adapts the Lucene {@link TokenStream} to
 * the common {@link org.miaixz.bus.extra.nlp.NLPResult} interface. Project homepage: <a href=
 * "https://github.com/apache/lucene-solr/tree/master/lucene/analysis">https://github.com/apache/lucene-solr/tree/master/lucene/analysis</a>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AnalysisResult extends AbstractResult {

    /**
     * The underlying Lucene {@link TokenStream}, which provides the segmentation result.
     */
    private final TokenStream stream;

    /**
     * Constructs an {@code AnalysisResult} instance by wrapping a Lucene {@link TokenStream}.
     *
     * @param stream The {@link TokenStream} obtained from Lucene analysis.
     */
    public AnalysisResult(final TokenStream stream) {
        this.stream = stream;
    }

    /**
     * Retrieves the next word from the Lucene {@link TokenStream}. This method calls {@code incrementToken()} on the
     * stream and wraps the resulting {@link CharTermAttribute} in an {@link AnalysisWord}.
     *
     * @return The next {@link NLPWord} in the sequence, or {@code null} if the iteration has no more elements.
     * @throws InternalException if an {@link IOException} occurs during token processing.
     */
    @Override
    protected NLPWord nextWord() {
        try {
            if (this.stream.incrementToken()) {
                return new AnalysisWord(this.stream.getAttribute(CharTermAttribute.class));
            }
        } catch (final IOException e) {
            throw new InternalException(e);
        }
        return null;
    }

}
