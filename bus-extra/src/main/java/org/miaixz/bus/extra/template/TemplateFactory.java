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
package org.miaixz.bus.extra.template;

import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.loader.spi.NormalSpiLoader;
import org.miaixz.bus.core.xyz.ReflectKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;

/**
 * 简单模板引擎工厂，用于根据用户引入的模板引擎jar，自动创建对应的模板引擎对象 使用简单工厂（Simple Factory）模式
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TemplateFactory {

    /**
     * 根据用户引入的模板引擎jar，自动创建对应的模板引擎对象 获得的是单例
     *
     * @return 单例
     */
    public static TemplateProvider get() {
        final TemplateProvider engine = Instances.get(TemplateProvider.class.getName(), TemplateFactory::create);
        Logger.debug("Use [{}] Template Engine As Default.",
                StringKit.removeSuffix(engine.getClass().getSimpleName(), "Engine"));
        return engine;
    }

    /**
     * 根据用户引入的模板引擎jar，自动创建对应的模板引擎对象 推荐创建的引擎单例使用，此方法每次调用会返回新的引擎
     *
     * @return {@link TemplateProvider}
     */
    public static TemplateProvider create() {
        return create(TemplateConfig.DEFAULT);
    }

    /**
     * 根据用户引入的模板引擎jar，自动创建对应的模板引擎对象 推荐创建的引擎单例使用，此方法每次调用会返回新的引擎
     *
     * @param config 模板配置，包括编码、模板文件path等信息
     * @return {@link TemplateProvider}
     */
    public static TemplateProvider create(final TemplateConfig config) {
        return doCreate(config);
    }

    /**
     * 根据用户引入的模板引擎jar，自动创建对应的模板引擎对象 推荐创建的引擎单例使用，此方法每次调用会返回新的引擎
     *
     * @param config 模板配置，包括编码、模板文件path等信息
     * @return {@link TemplateProvider}
     */
    private static TemplateProvider doCreate(final TemplateConfig config) {
        final Class<? extends TemplateProvider> customEngineClass = config.getProvider();
        final TemplateProvider engine;
        if (null != customEngineClass) {
            // 自定义模板引擎
            engine = ReflectKit.newInstance(customEngineClass);
        } else {
            // SPI引擎查找
            engine = NormalSpiLoader.loadFirstAvailable(TemplateProvider.class);
        }
        if (null != engine) {
            return engine.init(config);
        }

        throw new InternalException("No template found! Please add one of template jar to your project !");
    }

}
