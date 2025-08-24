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
package org.miaixz.bus.spring.boot;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.spring.metrics.BaseMetrics;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.io.ResourceLoader;

/**
 * 扩展 {@link org.springframework.boot.SpringApplication}，计算 {@link ApplicationContextInitializer} 初始化时间。
 * <p>
 * 该类继承自Spring Boot的SpringApplication，增加了对ApplicationContextInitializer初始化时间的统计功能。
 * 通过记录每个初始化器的开始和结束时间，计算初始化耗时，并将统计信息存储在列表中，便于后续分析。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SpringApplication extends org.springframework.boot.SpringApplication {

    /**
     * 存储所有ApplicationContextInitializer的初始化统计信息列表
     */
    private final List<BaseMetrics> initializerStartupStatList = new ArrayList<>();

    /**
     * 使用主源类创建一个新的SpringApplication实例
     *
     * @param primarySources 主源类数组
     */
    public SpringApplication(Class<?>... primarySources) {
        super(primarySources);
    }

    /**
     * 使用资源加载器和主源类创建一个新的SpringApplication实例
     *
     * @param resourceLoader 资源加载器
     * @param primarySources 主源类数组
     */
    public SpringApplication(ResourceLoader resourceLoader, Class<?>... primarySources) {
        super(resourceLoader, primarySources);
    }

    /**
     * 使用主源类和参数运行Spring应用程序
     * <p>
     * 这是一个静态工厂方法，用于创建并运行SpringApplication实例
     * </p>
     *
     * @param primarySource 主源类
     * @param args          命令行参数
     * @return 可配置的应用程序上下文
     */
    public static ConfigurableApplicationContext run(Class<?> primarySource, String... args) {
        return run(new Class<?>[] { primarySource }, args);
    }

    /**
     * 使用主源类数组和参数运行Spring应用程序
     * <p>
     * 这是一个静态工厂方法，用于创建并运行SpringApplication实例
     * </p>
     *
     * @param primarySources 主源类数组
     * @param args           命令行参数
     * @return 可配置的应用程序上下文
     */
    public static ConfigurableApplicationContext run(Class<?>[] primarySources, String[] args) {
        return new SpringApplication(primarySources).run(args);
    }

    /**
     * 运行Spring应用程序
     * <p>
     * 重写父类的run方法，保持原有功能不变
     * </p>
     *
     * @param args 命令行参数
     * @return 可配置的应用程序上下文
     */
    @Override
    public ConfigurableApplicationContext run(String... args) {
        return super.run(args);
    }

    /**
     * 应用所有已注册的ApplicationContextInitializer
     * <p>
     * 重写父类的applyInitializers方法，增加了对初始化器执行时间的统计功能。 在执行每个初始化器之前记录开始时间，执行完成后记录结束时间，计算耗时并保存统计信息。
     * </p>
     *
     * @param context 可配置的应用程序上下文
     */
    @Override
    protected void applyInitializers(ConfigurableApplicationContext context) {
        for (ApplicationContextInitializer initializer : getInitializers()) {
            try {
                // 解析初始化器所需的上下文类型
                Class<?> requiredType = GenericTypeResolver.resolveTypeArgument(initializer.getClass(),
                        ApplicationContextInitializer.class);
                // 验证上下文类型是否匹配
                Assert.isInstanceOf(requiredType, context,
                        "Unable to call initializer: " + initializer.getClass().getName());

                // 创建统计对象并记录开始时间
                BaseMetrics stat = new BaseMetrics();
                stat.setName(initializer.getClass().getName());
                stat.setStartTime(System.currentTimeMillis());

                // 执行初始化
                initializer.initialize(context);

                // 记录结束时间并计算耗时
                stat.setEndTime(System.currentTimeMillis());
                initializerStartupStatList.add(stat);

                // 输出调试日志
                Logger.debug("Initialized {} in {} ms", stat.getName(), stat.getCost());
            } catch (Exception e) {
                // 输出警告日志
                Logger.warn("Failed to initialize {}: {}", initializer.getClass().getName(), e.getMessage());
            }
        }
    }

    /**
     * 获取所有ApplicationContextInitializer的初始化统计信息列表
     *
     * @return 初始化统计信息列表
     */
    public List<BaseMetrics> getInitializerStartupStatList() {
        return initializerStartupStatList;
    }

}