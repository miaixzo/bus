/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.spring.limiter;

import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.CollUtils;
import org.aoju.bus.limiter.LimiterAnnotationParser;
import org.aoju.bus.limiter.intercept.BeanFactoryLimitedResourceSourceAdvisor;
import org.aoju.bus.limiter.intercept.LimiterInterceptor;
import org.aoju.bus.limiter.source.DefaultLimitedResourceSource;
import org.aoju.bus.limiter.source.LimitedResourceSource;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Role;
import org.springframework.core.io.ResourceLoader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Kimi Liu
 * @version 3.2.0
 * @since JDK 1.8
 */
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@Import(DefaultConfiguration.class)
public class LimiterConfiguration extends AbstractLimiterConfiguration implements ResourceLoaderAware {

    ResourceLoader resourceLoader;

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public BeanFactoryLimitedResourceSourceAdvisor limiterAdvisor() {
        BeanFactoryLimitedResourceSourceAdvisor advisor =
                new BeanFactoryLimitedResourceSourceAdvisor(limitedResourceSource());
        advisor.setAdvice(limiterInterceptor());
        if (this.enableLimiter != null) {
            advisor.setOrder(this.enableLimiter.<Integer>getNumber("order"));
        }
        return advisor;
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public LimitedResourceSource limitedResourceSource() {
        String[] parsersClassNames = this.enableLimiter.getStringArray("annotationParser");
        List<String> defaultParsers = findDefaultParsers();
        if (!CollUtils.isEmpty(defaultParsers)) {
            int len = parsersClassNames.length;
            parsersClassNames = Arrays.copyOf(parsersClassNames, parsersClassNames.length + defaultParsers.size());
            for (int i = 0; i < defaultParsers.size(); i++) {
                parsersClassNames[i + len] = defaultParsers.get(i);
            }
        }
        LimiterAnnotationParser[] parsers = new LimiterAnnotationParser[parsersClassNames.length];
        for (int i = 0; i < parsersClassNames.length; i++) {
            try {
                Class<LimiterAnnotationParser> parserClass = (Class<LimiterAnnotationParser>) Class.forName(parsersClassNames[i]);
                parsers[i] = parserClass.newInstance();
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                throw new InstrumentException("Class Not Found!");
            }
        }
        return new DefaultLimitedResourceSource(parsers);
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

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public LimiterInterceptor limiterInterceptor() {
        LimiterInterceptor interceptor = new LimiterInterceptor();
        interceptor.setLimitedResourceSource(limitedResourceSource());
        return interceptor;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

}
