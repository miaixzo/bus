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
package org.miaixz.bus.starter.limiter;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.limiter.Handler;
import org.miaixz.bus.limiter.Parser;
import org.miaixz.bus.limiter.Resolver;
import org.miaixz.bus.limiter.intercept.BeanFactoryLimitedResourceSourceAdvisor;
import org.miaixz.bus.limiter.intercept.LimiterInterceptor;
import org.miaixz.bus.limiter.resource.DefaultLimitedResourceSource;
import org.miaixz.bus.limiter.resource.LimitedResourceSource;
import org.miaixz.bus.logger.Logger;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Role;
import org.springframework.core.io.ResourceLoader;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 限流配置
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class LimiterAwareHandler extends AbstractLimiterAware implements ResourceLoaderAware {

    ResourceLoader resourceLoader;

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public BeanFactoryLimitedResourceSourceAdvisor limiterAdvisor(LimitedResourceSource limitedResourceSource, LimiterInterceptor limiterInterceptor) {
        BeanFactoryLimitedResourceSourceAdvisor advisor =
                new BeanFactoryLimitedResourceSourceAdvisor(limitedResourceSource);
        advisor.setAdvice(limiterInterceptor);
        if (null != this.enableLimiter) {
            advisor.setOrder(this.enableLimiter.<Integer>getNumber("order"));
        }
        return advisor;
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public LimitedResourceSource limitedResourceSource() {
        String[] parsersClassNames = this.enableLimiter.getStringArray("annotationParser");
        List<String> defaultParsers = findDefaultParsers();
        if (!CollKit.isEmpty(defaultParsers)) {
            int len = parsersClassNames.length;
            parsersClassNames = Arrays.copyOf(parsersClassNames, parsersClassNames.length + defaultParsers.size());
            for (int i = 0; i < defaultParsers.size(); i++) {
                parsersClassNames[i + len] = defaultParsers.get(i);
            }
        }
        Parser[] parsers = new Parser[parsersClassNames.length];
        for (int i = 0; i < parsersClassNames.length; i++) {
            try {
                Class<Parser> parserClass = (Class<Parser>) Class.forName(parsersClassNames[i]);
                parsers[i] = parserClass.getConstructor().newInstance();
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException |
                     InvocationTargetException e) {
                throw new InternalException("Class Not Found!");
            }
        }
        return new DefaultLimitedResourceSource(parsers);
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public LimiterInterceptor limiterInterceptor(LimitedResourceSource limitedResourceSource) {
        LimiterInterceptor interceptor = new LimiterInterceptor();
        interceptor.setLimitedResourceSource(limitedResourceSource);
        return interceptor;
    }

    @Bean
    Handler defaultErrorHandler() {
        Handler errorHandler = (throwable, executionContext) -> {
            Logger.info(throwable.getMessage());
            throw new RuntimeException(throwable.getMessage());
        };
        return errorHandler;
    }

    @Bean
    Resolver defaultFallbackResolver() {
        Resolver limitedFallbackResolver
                = (method, clazz, args, limitedResource, target) -> {
            throw new RuntimeException("no message available");
        };
        return limitedFallbackResolver;
    }

    private List<String> findDefaultParsers() {
        String[] parsers = new String[]{
                "LockAnnotationParser",
                "RateLimiterAnnotationParser",
                "PeakLimiterAnnotationParser"
        };
        List<String> ret = new ArrayList<>();
        for (int i = 0; i < parsers.length; i++) {
            try {
                Class.forName(parsers[i]);
                ret.add(parsers[i]);
            } catch (ClassNotFoundException e) {

            }
        }
        return ret;
    }

}
