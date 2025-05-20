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
package org.miaixz.bus.logger;

import java.net.URL;

import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.loader.spi.NormalSpiLoader;
import org.miaixz.bus.core.xyz.ReflectKit;
import org.miaixz.bus.core.xyz.ResourceKit;
import org.miaixz.bus.logger.metric.apache.commons.CommonsLoggingFactory;
import org.miaixz.bus.logger.metric.apache.log4j.Log4jLoggingFactory;
import org.miaixz.bus.logger.metric.console.NormalLoggingFactory;
import org.miaixz.bus.logger.metric.jdk.JdkLoggingFactory;
import org.miaixz.bus.logger.metric.slf4j.Slf4jLoggingFactory;

/**
 * 日志引擎简单工厂（静态工厂）类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Holder {

    /**
     * 默认构造
     */
    public Holder() {

    }

    /**
     * 根据用户引入的模板引擎jar，自动创建对应的模板引擎对象 获得的是单例的 {@link Factory}
     *
     * @return {@link Factory}
     */
    public static Factory getFactory() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * 自定义默认日志实现
     *
     * @param clazz 日志工厂类
     * @see Slf4jLoggingFactory
     * @see Log4jLoggingFactory
     * @see CommonsLoggingFactory
     * @see JdkLoggingFactory
     * @see NormalLoggingFactory
     */
    public static void setDefaultFactory(final Class<? extends Factory> clazz) {
        try {
            setDefaultFactory(ReflectKit.newInstance(clazz));
        } catch (final Exception e) {
            throw new IllegalArgumentException("Can not instance LogFactory class!", e);
        }
    }

    /**
     * 自定义日志实现
     *
     * @param factory 日志引擎对象
     * @see Slf4jLoggingFactory
     * @see Log4jLoggingFactory
     * @see CommonsLoggingFactory
     * @see JdkLoggingFactory
     * @see NormalLoggingFactory
     */
    public static void setDefaultFactory(final Factory factory) {
        Instances.put(Holder.class.getName(), factory);
        factory.create(Holder.class).debug("Custom Use [{}] Logger.", factory.getName());
    }

    /**
     * 创建指定日志实现引擎
     *
     * @param clazz 引擎类
     * @return {@link Factory}
     */
    public static Factory create(final Class<? extends Factory> clazz) {
        return ReflectKit.newInstance(clazz);
    }

    /**
     * 决定日志实现 依次按照顺序检查日志库的jar是否被引入，如果未引入任何日志库，则检查ClassPath下的logging.properties， 存在则使用JdkLogFactory，否则使用ConsoleLogFactory
     *
     * @return 日志实现类
     */
    public static Factory create() {
        final Factory factory = doFactory();
        factory.create(Registry.class).debug("Use [{}] Logger As Default.", factory.getName());
        return factory;
    }

    /**
     * 决定日志实现 依次按照顺序检查日志库的jar是否被引入，如果未引入任何日志库，则检查ClassPath下的logging.properties，存在则使用JdkLogFactory，否则使用ConsoleLogFactory
     *
     * @return 日志实现类
     */
    private static Factory doFactory() {
        final Factory factory = NormalSpiLoader.loadFirstAvailable(Factory.class);
        if (null != factory) {
            return factory;
        }

        // 未找到任何可支持的日志库时判断依据：当JDK Logging的配置文件位于classpath中，使用JDK Logging，否则使用Console
        final URL url = ResourceKit.getResourceUrl("logging.properties");
        return (null != url) ? new JdkLoggingFactory() : new NormalLoggingFactory();
    }

    /**
     * 嵌套使用Instances.get时在JDK9+会引起Recursive update问题，此处日志单独使用单例
     */
    private static class InstanceHolder {

        public static final Factory INSTANCE = create();

    }

}
