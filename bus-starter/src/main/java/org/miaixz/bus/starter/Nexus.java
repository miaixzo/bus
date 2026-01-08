/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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

import org.miaixz.bus.spring.GeniusBuilder;
import org.miaixz.bus.starter.annotation.*;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * Centralized composite condition: checks if any sub-feature is enabled/active to determine whether proxy support is
 * required.
 * <p>
 * This class implements Spring's {@link Condition} interface. Its core responsibility is to act as an "OR" logic
 * aggregator, deciding if a shared functionality (e.g., proxy support) should be activated.
 *
 * <p>
 * <b>Working Logic:</b>
 *
 * <p>
 * This condition iterates through all entries in the internal {@link #FEATURES} registry. For each entry, it checks:
 * <ol>
 * <li>If the corresponding configuration property is set to {@code true}.</li>
 * <li>Or, if there is any Spring bean annotated with the corresponding {@code @Enable*} annotation.</li>
 * </ol>
 * As soon as <b>any one</b> feature meets its activation criteria, this composite condition immediately returns
 * {@code true}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Nexus implements Condition {

    /**
     * Feature registry: a static immutable Map serving as the "single source of truth" for all related features. The
     * key is the property name, and the value is the {@code @Enable*} annotation for that feature.
     */
    private static final Map<String, Class<? extends Annotation>> FEATURES = Map.ofEntries(
            // Authentication and Authorization
            Map.entry(GeniusBuilder.AUTH, EnableAuth.class),
            // Cache Management
            Map.entry(GeniusBuilder.CACHE, EnableCache.class),
            // Cross-Origin Resource Sharing
            Map.entry(GeniusBuilder.CORS, EnableCors.class),
            // Dubbo Integration
            Map.entry(GeniusBuilder.DUBBO, EnableDubbo.class),
            // Elasticsearch
            Map.entry(GeniusBuilder.ELASTIC, EnableElastic.class),
            // Health Check
            Map.entry(GeniusBuilder.HEALTH, EnableHealth.class),
            // Internationalization (i18n)
            Map.entry(GeniusBuilder.I18N, EnableI18n.class),
            // Image Processing
            Map.entry(GeniusBuilder.IMAGE, EnableImage.class),
            // API Rate Limiting
            Map.entry(GeniusBuilder.LIMITER, EnableLimiter.class),
            // ORM/Data Mapping
            Map.entry(GeniusBuilder.MAPPER, EnableMapper.class),
            // Message Notification
            Map.entry(GeniusBuilder.NOTIFY, EnableNotify.class),
            // Office Document Processing
            Map.entry(GeniusBuilder.OFFICE, EnableOffice.class),
            // Data Desensitization
            Map.entry(GeniusBuilder.SENSITIVE, EnableSensitive.class),
            // WebSocket
            Map.entry(GeniusBuilder.SOCKET, EnableSocket.class),
            // Distributed Storage
            Map.entry(GeniusBuilder.STORAGE, EnableStorage.class),
            // Distributed Tracing
            Map.entry(GeniusBuilder.TRACER, EnableTracer.class),
            // Parameter Validation
            Map.entry(GeniusBuilder.VALIDATE, EnableValidate.class),
            // Vortex Gateway
            Map.entry(GeniusBuilder.VORTEX, EnableVortex.class),
            // Request/Response Wrapping
            Map.entry(GeniusBuilder.WRAPPER, EnableWrapper.class));

    /**
     * Evaluates whether this condition matches.
     *
     * @param context  The condition context, providing access to {@link org.springframework.core.env.Environment},
     *                 {@link org.springframework.beans.factory.BeanFactory}, and other core Spring components.
     * @param metadata The metadata of the class or method being checked (unused in this implementation).
     * @return {@code true} if any feature in the {@link #FEATURES} registry is activated (either by property or
     *         annotation), {@code false} otherwise.
     */
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        // Iterate through all entries in the feature registry
        for (Map.Entry<String, Class<? extends Annotation>> feature : FEATURES.entrySet()) {
            String propertyName = feature.getKey();
            Class<? extends Annotation> annotationType = feature.getValue();

            // Check method one: Is the property in the configuration file set to "true"?
            if ("true".equalsIgnoreCase(context.getEnvironment().getProperty(propertyName))) {
                // Short-circuit logic: if one condition is met, immediately return true.
                return true;
            }

            // Check method two: Is there any bean in the Spring container annotated with the corresponding annotation?
            if (!context.getBeanFactory().getBeansWithAnnotation(annotationType).isEmpty()) {
                // Short-circuit logic: if one condition is met, immediately return true.
                return true;
            }
        }

        // If no feature is activated after checking all of them, return false.
        return false;
    }

}
