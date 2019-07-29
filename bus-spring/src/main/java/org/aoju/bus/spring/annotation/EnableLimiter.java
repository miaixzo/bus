package org.aoju.bus.spring.annotation;

import org.aoju.bus.spring.limiter.LimiterConfigurationSelector;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;

import java.lang.annotation.*;

import static org.springframework.context.annotation.AdviceMode.PROXY;

/**
 * 限流降级
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(LimiterConfigurationSelector.class)
public @interface EnableLimiter {

    boolean proxyTargetClass() default false;

    int order() default Ordered.LOWEST_PRECEDENCE;

    /**
     * 默认有三种组件
     */
    String[] annotationParser()
            default {"LockAnnotationParser",
            "RateLimiterAnnotationParser",
            "PeakLimiterAnnotationParser"
    };

    AdviceMode mode() default PROXY;

}