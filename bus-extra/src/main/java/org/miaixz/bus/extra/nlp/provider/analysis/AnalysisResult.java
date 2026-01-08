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
