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
package org.miaixz.bus.starter.notify;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import jakarta.annotation.Resource;

/**
 * 消息通知自动配置类，用于配置消息通知相关的Bean。
 *
 * <p>
 * 该类负责创建并配置消息通知服务提供者工厂，用于管理和创建各种消息通知服务。
 * </p>
 *
 * <p>
 * 使用示例：
 * </p>
 * 
 * <pre>
 * // 在application.yml中配置
 * bus:
 *   notify:
 *     # 消息通知相关配置
 *
 * // 在代码中直接注入使用
 * &#64;Autowired
 * private NotifyProviderService notifyProviderService;
 *
 * // 发送邮件通知
 * notifyProviderService.send(NotifyRegistry.EMAIL, "邮件内容");
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@EnableConfigurationProperties(value = { NotifyProperties.class })
public class NotifyConfiguration {

    /**
     * 消息通知配置属性，包含各种消息通知组件的配置信息。 通过{@link EnableConfigurationProperties}注解自动注入。
     */
    @Resource
    NotifyProperties properties;

    /**
     * 创建消息通知服务提供者工厂Bean。
     *
     * <p>
     * 该方法创建一个{@link NotifyService}实例，用于管理和创建各种消息通知服务提供者。 该实例会使用配置属性来初始化。
     * </p>
     *
     * @return 配置好的消息通知服务提供者工厂实例
     */
    @Bean
    public NotifyService notifyProviderFactory() {
        return new NotifyService(this.properties);
    }

}