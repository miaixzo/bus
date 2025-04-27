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
package org.miaixz.bus.extra.nlp;

import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.loader.spi.ServiceLoader;
import org.miaixz.bus.core.xyz.SPIKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;

/**
 * 简单分词引擎工厂，用于根据用户引入的分词引擎jar，自动创建对应的引擎
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class NLPFactory {

    /**
     * 根据用户引入的模板引擎jar，自动创建对应的分词引擎对象 获得的是单例的TokenizerEngine
     *
     * @return 单例的TokenizerEngine
     */
    public static NLPProvider getEngine() {
        final NLPProvider engine = Instances.get(NLPProvider.class.getName(), NLPFactory::createEngine);
        Logger.debug("Use [{}] Tokenizer Engine As Default.",
                StringKit.removeSuffix(engine.getClass().getSimpleName(), "Engine"));
        return engine;
    }

    /**
     * 根据用户引入的分词引擎jar，自动创建对应的分词引擎对象
     *
     * @return {@link NLPProvider}
     */
    public static NLPProvider createEngine() {
        return doCreateEngine();
    }

    /**
     * 创建自定义引擎
     *
     * @param engineName 引擎名称，忽略大小写，如`Analysis`、`Ansj`、`HanLP`、`IKAnalyzer`、`Jcseg`、`Jieba`、`Mmseg`、`Mynlp`、`Word`
     * @return 引擎
     * @throws InternalException 无对应名称的引擎
     */
    public static NLPProvider createEngine(String engineName) throws InternalException {
        if (!StringKit.endWithIgnoreCase(engineName, "Engine")) {
            engineName = engineName + "Engine";
        }
        final ServiceLoader<NLPProvider> list = SPIKit.loadList(NLPProvider.class);
        for (final String serviceName : list.getServiceNames()) {
            if (StringKit.endWithIgnoreCase(serviceName, engineName)) {
                return list.getService(serviceName);
            }
        }
        throw new InternalException("No such engine named: " + engineName);
    }

    /**
     * 根据用户引入的分词引擎jar，自动创建对应的分词引擎对象
     *
     * @return {@link NLPProvider}
     */
    private static NLPProvider doCreateEngine() {
        final NLPProvider engine = SPIKit.loadFirstAvailable(NLPProvider.class);
        if (null != engine) {
            return engine;
        }

        throw new InternalException("No tokenizer found !Please add some tokenizer jar to your project !");
    }

}
