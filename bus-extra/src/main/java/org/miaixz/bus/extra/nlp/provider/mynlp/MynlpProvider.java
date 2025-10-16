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
package org.miaixz.bus.extra.nlp.provider.mynlp;

import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.nlp.NLPProvider;
import org.miaixz.bus.extra.nlp.NLPResult;

import com.mayabot.nlp.Mynlp;
import com.mayabot.nlp.segment.Lexer;
import com.mayabot.nlp.segment.Sentence;

/**
 * Mynlp word segmentation engine implementation. This class serves as a concrete {@link NLPProvider} for the Mynlp NLP
 * library, adapting its word segmentation capabilities to the common NLP interface. The underlying {@link Lexer} is
 * thread-safe. Project homepage: <a href="https://github.com/mayabot/mynlp/">https://github.com/mayabot/mynlp/</a>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MynlpProvider implements NLPProvider {

    /**
     * The underlying Mynlp {@link Lexer} instance used for performing word segmentation.
     */
    private final Lexer lexer;

    /**
     * Constructs a new {@code MynlpProvider} instance with a default Mynlp lexer. The default lexer is a bigram lexer
     * with part-of-speech tagging and person name recognition enabled.
     */
    public MynlpProvider() {
        // CORE tokenizer builder
        // Enable part-of-speech tagging
        // Enable person name recognition
        this.lexer = Mynlp.instance().bigramLexer();
    }

    /**
     * Constructs a new {@code MynlpProvider} instance with a custom Mynlp {@link Lexer} implementation.
     *
     * @param lexer The custom {@link Lexer} object to use for word segmentation.
     */
    public MynlpProvider(final Lexer lexer) {
        this.lexer = lexer;
    }

    /**
     * Performs word segmentation on the given text using the configured Mynlp {@link Lexer} instance. The result is
     * wrapped in a {@link MynlpResult} to conform to the {@link NLPResult} interface.
     *
     * @param text The input text {@link CharSequence} to be segmented.
     * @return An {@link NLPResult} object containing the segmented words from Mynlp.
     */
    @Override
    public NLPResult parse(final CharSequence text) {
        final Sentence sentence = this.lexer.scan(StringKit.toStringOrEmpty(text));
        return new MynlpResult(sentence);
    }

}
