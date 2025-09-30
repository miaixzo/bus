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
package org.miaixz.bus.starter;

import java.lang.annotation.Annotation;
import java.util.Map;

import org.miaixz.bus.spring.GeniusBuilder;
import org.miaixz.bus.starter.annotation.*;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * 中心化的复合条件：检查是否有任何一个子功能被启用/激活，从而决定是否需要代理支持。
 * <p>
 * 这是一个实现了 Spring {@link Condition} 接口的自定义条件类。 它的核心职责是作为一个“或”逻辑的聚合器，用于判断一个共享的功能（例如代理支持）是否应该被激活。
 * <p>
 * <b>工作逻辑:</b> 该条件会遍历内部 {@link #FEATURES} 注册表中的所有条目。对于每一个条目，它会检查：
 * <ol>
 * <li>对应的配置文件属性是否为 {@code true}。</li>
 * <li>或者，Spring 容器中是否存在被对应注解所标记的 Bean。</li>
 * </ol>
 * 只要有**任何一个**功能满足其激活条件，此复合条件就会立即返回 {@code true}。
 * 
 * @author Kimi Liu
 * @since Java 17+
 */
public class Nexus implements Condition {

    /**
     * 功能注册表：一个静态不可变的 Map，作为所有相关功能的“单一事实来源”。 Key 为属性名，Value 为功能的启用注解。
     */
    private static final Map<String, Class<? extends Annotation>> FEATURES = Map.ofEntries(
            // 认证授权
            Map.entry(GeniusBuilder.AUTH, EnableAuth.class),
            // 服务桥接
            Map.entry(GeniusBuilder.BRIDGE, EnableBridge.class),
            // 缓存管理
            Map.entry(GeniusBuilder.CACHE, EnableCache.class),
            // 跨域资源共享
            Map.entry(GeniusBuilder.CORS, EnableCors.class),
            // Dubbo 集成
            Map.entry(GeniusBuilder.DUBBO, EnableDubbo.class),
            // Elasticsearch
            Map.entry(GeniusBuilder.ELASTIC, EnableElastic.class),
            // 健康检查
            Map.entry(GeniusBuilder.HEALTH, EnableHealth.class),
            // 国际化(i18n)
            Map.entry(GeniusBuilder.I18N, EnableI18n.class),
            // 图像处理
            Map.entry(GeniusBuilder.IMAGE, EnableImage.class),
            // 接口限流
            Map.entry(GeniusBuilder.LIMITER, EnableLimiter.class),
            // ORM/数据映射
            Map.entry(GeniusBuilder.MAPPER, EnableMapper.class),
            // 消息通知
            Map.entry(GeniusBuilder.NOTIFY, EnableNotify.class),
            // Office文档处理
            Map.entry(GeniusBuilder.OFFICE, EnableOffice.class),
            // 数据脱敏
            Map.entry(GeniusBuilder.SENSITIVE, EnableSensitive.class),
            // WebSocket
            Map.entry(GeniusBuilder.SOCKET, EnableSocket.class),
            // 分布式存储
            Map.entry(GeniusBuilder.STORAGE, EnableStorage.class),
            // 分布式链路追踪
            Map.entry(GeniusBuilder.TRACER, EnableTracer.class),
            // 参数校验
            Map.entry(GeniusBuilder.VALIDATE, EnableValidate.class),
            // Vortex网关
            Map.entry(GeniusBuilder.VORTEX, EnableVortex.class),
            // 请求/响应包装
            Map.entry(GeniusBuilder.WRAPPER, EnableWrapper.class));

    /**
     * 评估此条件是否匹配。
     *
     * @param context  条件上下文，提供访问 Environment, BeanFactory 等核心 Spring 组件的能力。
     * @param metadata 被检查的类或方法的元数据（在此实现中未使用）。
     * @return 如果 {@link #FEATURES} 注册表中有任何一个功能被激活，则返回 {@code true}；否则返回 {@code false}。
     */
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        // 遍历功能注册表中的所有条目
        for (Map.Entry<String, Class<? extends Annotation>> feature : FEATURES.entrySet()) {
            String propertyName = feature.getKey();
            Class<? extends Annotation> annotationType = feature.getValue();

            // 检查方式一：配置文件中的属性是否为 "true"
            if ("true".equalsIgnoreCase(context.getEnvironment().getProperty(propertyName))) {
                // 短路逻辑：只要有一个满足条件，就立即返回 true，无需再检查其他功能
                return true;
            }

            // 检查方式二：Spring 容器中是否存在被相应注解标记的 Bean
            if (!context.getBeanFactory().getBeansWithAnnotation(annotationType).isEmpty()) {
                // 短路逻辑：同样，只要有一个满足，就立即返回 true
                return true;
            }
        }

        // 如果遍历完所有功能，都没有任何一个被激活，则最终返回 false
        return false;
    }

}
