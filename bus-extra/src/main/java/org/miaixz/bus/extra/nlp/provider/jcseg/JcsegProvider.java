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
 * @since Java 21+
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
