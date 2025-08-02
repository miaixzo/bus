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
package org.miaixz.bus.extra.mq;

import org.miaixz.bus.core.lang.exception.MQueueException;
import org.miaixz.bus.core.lang.loader.spi.NormalSpiLoader;
import org.miaixz.bus.core.xyz.ReflectKit;

/**
 * MQ引擎工厂类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MQFactory {

    /**
     * 根据用户引入的MQ引擎jar，自动创建对应的模板引擎对象<br>
     * 推荐创建的引擎单例使用，此方法每次调用会返回新的引擎
     *
     * @param config MQ配置
     * @return {@link MQProvider}
     */
    public static MQProvider createEngine(final MQConfig config) {
        return doCreateEngine(config);
    }

    /**
     * 根据用户引入的MQ引擎jar，自动创建对应的MQ引擎对象
     *
     * @param config MQ配置
     * @return {@link MQProvider}
     */
    private static MQProvider doCreateEngine(final MQConfig config) {
        final Class<? extends MQProvider> customEngineClass = config.getCustomEngine();
        final MQProvider engine;
        if (null != customEngineClass) {
            // 自定义模板引擎
            engine = ReflectKit.newInstance(customEngineClass);
        } else {
            // SPI引擎查找
            engine = NormalSpiLoader.loadFirstAvailable(MQProvider.class);
        }
        if (null != engine) {
            return engine.init(config);
        }

        throw new MQueueException("No MQ implement found! Please add one of MQ jar to your project !");
    }

}
