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
package org.miaixz.bus.extra.nlp.provider.ikanalyzer;

import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.nlp.NLPProvider;
import org.miaixz.bus.extra.nlp.NLPResult;
import org.wltea.analyzer.cfg.Configuration;
import org.wltea.analyzer.cfg.DefaultConfig;
import org.wltea.analyzer.core.IKSegmenter;

/**
 * IKAnalyzer分词引擎实现 项目地址：https://github.com/yozhao/IKAnalyzer {@link IKSegmenter} 非线程全，因此每次单独创建对象
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class IKAnalyzerProvider implements NLPProvider {

    private final Configuration cfg;

    /**
     * 构造
     */
    public IKAnalyzerProvider() {
        this(createDefaultConfig());
    }

    /**
     * 构造
     *
     * @param cfg 配置
     */
    public IKAnalyzerProvider(final Configuration cfg) {
        cfg.setUseSmart(true);
        this.cfg = cfg;
    }

    /**
     * 创建默认配置
     *
     * @return {@link Configuration}
     */
    private static Configuration createDefaultConfig() {
        final Configuration configuration = DefaultConfig.getInstance();
        configuration.setUseSmart(true);
        return configuration;
    }

    @Override
    public NLPResult parse(final CharSequence text) {
        final IKSegmenter seg = new IKSegmenter(StringKit.getReader(text), cfg);
        return new IKAnalyzerResult(seg);
    }

}
