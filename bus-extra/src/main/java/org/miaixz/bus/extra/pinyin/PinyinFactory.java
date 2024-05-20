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
package org.miaixz.bus.extra.pinyin;

import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.loader.spi.ServiceLoader;
import org.miaixz.bus.core.toolkit.SPIKit;
import org.miaixz.bus.core.toolkit.StringKit;
import org.miaixz.bus.logger.Logger;

/**
 * 简单拼音引擎工厂，用于根据用户引入的拼音库jar，自动创建对应的拼音引擎对象
 * 使用简单工厂（Simple Factory）模式
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PinyinFactory {

    /**
     * 获得单例的PinyinEngine
     *
     * @return 单例的PinyinEngine
     */
    public static PinyinProvider getEngine() {
        final PinyinProvider engine = Instances.get(PinyinProvider.class.getName(), PinyinFactory::createEngine);
        Logger.debug("Use [{}] Pinyin Engine As Default.", StringKit.removeSuffix(engine.getClass().getSimpleName(), "Engine"));
        return engine;
    }

    /**
     * 根据用户引入的拼音引擎jar，自动创建对应的拼音引擎对象
     * 推荐创建的引擎单例使用，此方法每次调用会返回新的引擎
     *
     * @return {@link PinyinProvider}
     */
    public static PinyinProvider createEngine() {
        return doCreateEngine();
    }

    /**
     * 创建自定义引擎
     *
     * @param engineName 引擎名称，忽略大小写，如`Bopomofo4j`、`Houbb`、`JPinyin`、`Pinyin4j`、`TinyPinyin`
     * @return 引擎
     * @throws InternalException 无对应名称的引擎
     */
    public static PinyinProvider createEngine(String engineName) throws InternalException {
        if (!StringKit.endWithIgnoreCase(engineName, "Engine")) {
            engineName = engineName + "Engine";
        }
        final ServiceLoader<PinyinProvider> list = SPIKit.loadList(PinyinProvider.class);
        for (final String serviceName : list.getServiceNames()) {
            if (StringKit.endWithIgnoreCase(serviceName, engineName)) {
                return list.getService(serviceName);
            }
        }
        throw new InternalException("No such engine named: " + engineName);
    }

    /**
     * 根据用户引入的拼音引擎jar，自动创建对应的拼音引擎对象
     * 推荐创建的引擎单例使用，此方法每次调用会返回新的引擎
     *
     * @return {@link PinyinProvider}
     */
    private static PinyinProvider doCreateEngine() {
        final PinyinProvider engine = SPIKit.loadFirstAvailable(PinyinProvider.class);
        if (null != engine) {
            return engine;
        }

        throw new InternalException("No pinyin jar found !Please add one of it to your project !");
    }

}
