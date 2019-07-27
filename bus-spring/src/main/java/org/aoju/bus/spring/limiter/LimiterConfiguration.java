package org.aoju.bus.spring.limiter;

import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.CollUtils;
import org.aoju.bus.limiter.LimiterAnnotationParser;
import org.aoju.bus.limiter.interceptor.BeanFactoryLimitedResourceSourceAdvisor;
import org.aoju.bus.limiter.interceptor.LimiterInterceptor;
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
