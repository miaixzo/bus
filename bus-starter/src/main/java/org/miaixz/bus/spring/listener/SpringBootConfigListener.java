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
package org.miaixz.bus.spring.listener;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.miaixz.bus.spring.banner.BannerPrinter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;

/**
 * 监听 {@link ApplicationEnvironmentPreparedEvent} 事件，注册基于 {@link ConfigurableEnvironment} 的配置源， 并通过
 * {@link BannerPrinter} 处理 Spring Boot Banner 打印。 确保在环境准备阶段触发配置注册和 Banner 打印，且只执行一次。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SpringBootConfigListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent>, Ordered {

    /**
     * 用于确保配置注册和 Banner 打印只执行一次的标志。
     */
    private final AtomicBoolean registered = new AtomicBoolean();

    /**
     * SpringApplication 实例，用于传递给 {@link BannerPrinter} 以设置和获取 Banner。
     */
    public SpringApplication application;

    /**
     * 处理 {@link ApplicationEnvironmentPreparedEvent} 事件，初始化 SpringApplication 并触发配置注册。 通过 {@link AtomicBoolean}
     * 确保事件处理逻辑只执行一次。
     *
     * @param event 环境准备完成事件，包含 SpringApplication 和 ConfigurableEnvironment
     */
    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        this.application = event.getSpringApplication();
        if (registered.compareAndSet(false, true)) {
            registerConfigs(event.getEnvironment());
        }
    }

    /**
     * 定义监听器优先级，高于默认值以确保在环境准备阶段早期执行。
     *
     * @return 优先级值，HIGHEST_PRECEDENCE + 13
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 13;
    }

    /**
     * 注册配置并触发 Banner 打印。 调用 {@link BannerPrinter} 处理 Banner 打印逻辑，并执行其他配置注册。
     *
     * @param environment Spring 环境配置，用于 Banner 打印和配置注册
     */
    public void registerConfigs(ConfigurableEnvironment environment) {
        // 注册bus.version属性
        Properties props = new Properties();
        props.setProperty("bus.version", "8.1.6");
        MutablePropertySources sources = environment.getPropertySources();
        sources.addLast(new PropertiesPropertySource("bus", props));

        // 设置并打印 Banner
        new BannerPrinter().printBanner(this.application, environment);
        // 其他配置注册逻辑（待实现）
    }

}