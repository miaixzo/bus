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
