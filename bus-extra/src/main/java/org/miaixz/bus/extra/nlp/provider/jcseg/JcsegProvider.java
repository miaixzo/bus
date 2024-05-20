/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.extra.nlp.provider.jcseg;

import org.lionsoul.jcseg.ISegment;
import org.lionsoul.jcseg.dic.ADictionary;
import org.lionsoul.jcseg.dic.DictionaryFactory;
import org.lionsoul.jcseg.segmenter.SegmenterConfig;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.nlp.NLPProvider;
import org.miaixz.bus.extra.nlp.NLPResult;

import java.io.IOException;
import java.io.StringReader;

/**
 * Jcseg分词引擎实现
 * 项目地址：https://gitee.com/lionsoul/jcseg
 * {@link ISegment}非线程安全，每次单独创建
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JcsegProvider implements NLPProvider {

    private final SegmenterConfig config;
    private final ADictionary dic;

    /**
     * 构造
     */
    public JcsegProvider() {
        // 创建SegmenterConfig分词配置实例，自动查找加载jcseg.properties配置项来初始化
        this(new SegmenterConfig(true));
    }

    /**
     * 构造
     *
     * @param config {@link SegmenterConfig}
     */
    public JcsegProvider(final SegmenterConfig config) {
        this.config = config;
        // 创建默认单例词库实现，并且按照config配置加载词库
        this.dic = DictionaryFactory.createSingletonDictionary(config);
    }

    @Override
    public NLPResult parse(final CharSequence text) {
        // 依据给定的ADictionary和SegmenterConfig来创建ISegment
        final ISegment segment = ISegment.COMPLEX.factory.create(config, dic);
        try {
            segment.reset(new StringReader(StringKit.toString(text)));
        } catch (final IOException e) {
            throw new InternalException(e);
        }
        return new JcsegResult(segment);
    }

}
