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
package org.miaixz.bus.extra.nlp.provider.jcseg;

import java.io.IOException;
import java.io.StringReader;

import org.lionsoul.jcseg.ISegment;
import org.lionsoul.jcseg.dic.ADictionary;
import org.lionsoul.jcseg.dic.DictionaryFactory;
import org.lionsoul.jcseg.segmenter.SegmenterConfig;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.nlp.NLPProvider;
import org.miaixz.bus.extra.nlp.NLPResult;

/**
 * Jcseg word segmentation engine implementation. This class serves as a concrete {@link NLPProvider} for the Jcseg NLP
 * library. Note that {@link ISegment} is not thread-safe, so a new instance is created for each segmentation request.
 * Project homepage: <a href="https://gitee.com/lionsoul/jcseg">https://gitee.com/lionsoul/jcseg</a>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JcsegProvider implements NLPProvider {

    /**
     * The Jcseg segmenter configuration.
     */
    private final SegmenterConfig config;
    /**
     * The Jcseg dictionary.
     */
    private final ADictionary dic;

    /**
     * Constructs a new {@code JcsegProvider} instance with a default configuration. It automatically finds and loads
     * the `jcseg.properties` configuration file.
     */
    public JcsegProvider() {
        // Create a SegmenterConfig instance, automatically finding and loading jcseg.properties
        this(new SegmenterConfig(true));
    }

    /**
     * Constructs a new {@code JcsegProvider} instance with a custom {@link SegmenterConfig}.
     *
     * @param config The custom {@link SegmenterConfig} to use for word segmentation.
     */
    public JcsegProvider(final SegmenterConfig config) {
        this.config = config;
        // Create a default singleton dictionary instance and load the dictionary according to the config
        this.dic = DictionaryFactory.createSingletonDictionary(config);
    }

    /**
     * Performs word segmentation on the given text using the Jcseg engine. A new {@link ISegment} instance is created
     * for each call to ensure thread safety. The result is wrapped in a {@link JcsegResult} to conform to the
     * {@link NLPResult} interface.
     *
     * @param text The input text {@link CharSequence} to be segmented.
     * @return An {@link NLPResult} object containing the segmented words from Jcseg.
     * @throws InternalException if an {@link IOException} occurs during the operation.
     */
    @Override
    public NLPResult parse(final CharSequence text) {
        // Create an ISegment instance based on the given ADictionary and SegmenterConfig
        final ISegment segment = ISegment.COMPLEX.factory.create(config, dic);
        try {
            segment.reset(new StringReader(StringKit.toStringOrEmpty(text)));
        } catch (final IOException e) {
            throw new InternalException(e);
        }
        return new JcsegResult(segment);
    }

}
